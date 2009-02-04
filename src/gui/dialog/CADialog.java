/*
 Copyright (c) 2000-2005, The JAP-Team
 All rights reserved.
 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice,
  this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation and/or
  other materials provided with the distribution.

 - Neither the name of the University of Technology Dresden, Germany nor the names of its contributors
  may be used to endorse or promote products derived from this software without specific
  prior written permission.


 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS
 OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS
 BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 */
package gui.dialog;

import gui.CAListCellRenderer;
import gui.CertDetailsDialog;
import gui.CountryMapper;
import gui.JAPHelpContext;
import gui.JAPMessages;
import gui.dialog.JAPDialog;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mixconfig.panels.CertPanel;

import anon.crypto.CertificateInfoStructure;
import anon.crypto.JAPCertificate;
import anon.crypto.SignatureVerifier;
import anon.crypto.X509DistinguishedName;

/**
 * <p>class CADialog</p>
 *
 * <p>Shows all integrated certification authorities.
 * The Enable/Disable/Import/Remove-features of the dialog
 * do *not* work yet, because the saving of certificates in
 * MixConfig has to be changed in general therefore.
 * Methods and layout were taken from jap.JAPConfCert.</p>
 *
 * @see also jap.JAPConfcert in Project JAP
 * @author Robert Hirschberger
 * @version 1.0
 */
public class CADialog extends JAPDialog implements Observer //JAPHelpContext.IHelpContext,
{
	//private static final String MSG_CONFIRMATION_TITLE = CertificationTool.class.getName() + "_confirmationTitle";
	//private static final String MSG_CONFIRMATION = CertificationTool.class.getName() + "_confirmationMessage";

	private TitledBorder m_borderCert, m_borderInfo;
	private JLabel m_labelDate, m_labelCN, m_labelE, m_labelCSTL, m_labelO, m_labelOU;
	private JLabel m_labelDateData, m_labelCNData, m_labelEData, m_labelCSTLData, m_labelOData, m_labelOUData;
	private JButton m_bttnCertView;
	private DefaultListModel m_listmodelCertList;
	private JList m_listCert;
	private JScrollPane m_scrpaneList;
	private Enumeration m_enumCerts;
	private JPanel m_panelCAInfo, m_panelCAList, m_rootPanel;

	public CADialog(Frame parent)
	{
		super(parent, "View accepted Certification Authorities", true);
		m_rootPanel = new JPanel();
		recreateRootPanel();
		//loadDefaultCertificates();
		SignatureVerifier.getInstance().getVerificationCertificateStore().addObserver(this);
		update(SignatureVerifier.getInstance().getVerificationCertificateStore(), null);
		setSize(550, 500);
	    setResizable(false);
		setVisible(true, false);
	}

	private void updateInfoPanel(JAPCertificate a_cert)
	{
		X509DistinguishedName name;
		String strCSTL = null;
		String country;

		m_labelCNData.setText("");
		m_labelEData.setText("");
		m_labelCSTLData.setText("");
		m_labelOData.setText("");
		m_labelOUData.setText("");
		m_labelDateData.setText("");
		if (a_cert == null)
		{
			return;
		}

		StringBuffer strBuff = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		strBuff.append(sdf.format(a_cert.getValidity().getValidFrom()));
		strBuff.append(" - ");
		strBuff.append(sdf.format(a_cert.getValidity().getValidTo()));
		m_labelDateData.setText(strBuff.toString());
		m_labelDateData.setToolTipText(strBuff.toString());

		name = a_cert.getSubject();
		if (name.getCommonName() != null && name.getCommonName().trim().length() > 0)
		{
			m_labelCNData.setText(name.getCommonName().trim());
			m_labelCNData.setToolTipText(name.getCommonName().trim());
		}
		if (name.getEmailAddress() != null && name.getEmailAddress().trim().length() > 0)
		{
			m_labelEData.setText(name.getEmailAddress().trim());
			m_labelEData.setToolTipText(name.getEmailAddress().trim());
		}
		else if (name.getE_EmailAddress() != null && name.getE_EmailAddress().trim().length() > 0)
		{
			m_labelEData.setText(name.getE_EmailAddress());
			m_labelEData.setToolTipText(name.getE_EmailAddress());
		}
		if (name.getLocalityName() != null && name.getLocalityName().trim().length() > 0)
		{
			strCSTL = name.getLocalityName().trim();
		}
		if (name.getStateOrProvince() != null && name.getStateOrProvince().trim().length() > 0)
		{
			if (strCSTL != null)
			{
				strCSTL += ", ";
			}
			else
			{
				strCSTL = "";
			}
			strCSTL += name.getStateOrProvince().trim();
		}
		if (name.getCountryCode() != null)
		{
			try
			{
				country = new CountryMapper(name.getCountryCode(), JAPMessages.getLocale()).toString();
			}
			catch (IllegalArgumentException a_e)
			{
				country = name.getCountryCode();
			}

			if (country.trim().length() > 0)
			{
				if (strCSTL != null)
				{
					strCSTL += ", ";
				}
				else
				{
					strCSTL = "";
				}
				strCSTL += country.trim();
			}
		}
		m_labelCSTLData.setText(strCSTL);
		m_labelCSTLData.setToolTipText(strCSTL);

		if (name.getOrganisation() != null && name.getOrganisation().trim().length() > 0)
		{
			m_labelOData.setText(name.getOrganisation().trim());
			m_labelOData.setToolTipText(name.getOrganisation().trim());
		}
		if (name.getOrganisationalUnit() != null && name.getOrganisationalUnit().trim().length() > 0)
		{
			m_labelOUData.setText(name.getOrganisationalUnit().trim());
			m_labelOUData.setToolTipText(name.getOrganisationalUnit().trim());
		}
	}

	/**
	 * Creates the cert root panel with all child-panels.
	 */
	public void recreateRootPanel()
	{
		JPanel panelRoot = getRootPanel();

		m_borderCert = new TitledBorder("Accepted Certification Authorities");
		m_panelCAList = createCertCAPanel();
		m_panelCAList.setBorder(m_borderCert);

		m_borderInfo = new TitledBorder("Certificate Authority information");
		m_panelCAInfo = createCertInfoPanel();
		m_panelCAInfo.setBorder(m_borderInfo);

		panelRoot.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 2.0;
		c.weighty = 2.0;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(10, 10, 10, 10);
		panelRoot.add(m_panelCAList, c);

		c.gridy++;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 10, 10, 10);
		panelRoot.add(m_panelCAInfo, c);
		getContentPane().add(m_rootPanel);
	}

	private JPanel createCertCAPanel()
	{
		JPanel r_panelCA = new JPanel();

		GridBagLayout panelLayoutCA = new GridBagLayout();
		r_panelCA.setLayout(panelLayoutCA);

		GridBagConstraints panelConstraintsCA = new GridBagConstraints();

		m_listmodelCertList = new DefaultListModel();

		m_listCert = new JList(m_listmodelCertList);
		m_listCert.setCellRenderer(new CAListCellRenderer());
		m_listCert.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if (m_listmodelCertList.getSize() == 0 || m_listCert.getSelectedValue() == null)
				{
					updateInfoPanel(null);
				}
				else
				{
					CertificateInfoStructure j = (CertificateInfoStructure) m_listCert.getSelectedValue();
					updateInfoPanel(j.getCertificate());
				}
			}
		});
		m_listCert.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent a_event)
			{
				if (a_event.getClickCount() == 2)
				{
					showCert();
				}
			}
		});

		m_scrpaneList = new JScrollPane();
		m_scrpaneList.getViewport().add(m_listCert, null);

		panelConstraintsCA.gridx = 0;
		panelConstraintsCA.gridy = 0;
		panelConstraintsCA.weightx = 1;
		panelConstraintsCA.weighty = 1;
		panelConstraintsCA.gridwidth = 1;
		panelConstraintsCA.insets = new Insets(0, 10, 10, 10);
		panelConstraintsCA.fill = GridBagConstraints.BOTH;
		panelLayoutCA.setConstraints(m_scrpaneList, panelConstraintsCA);
		r_panelCA.add(m_scrpaneList);


		m_bttnCertView = new JButton("View");
		m_bttnCertView.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				showCert();
			}
		});

		panelConstraintsCA.gridx = 0;
		panelConstraintsCA.gridy = 1;
		panelConstraintsCA.weightx = 0;
		panelConstraintsCA.weighty = 0;
		panelConstraintsCA.fill = GridBagConstraints.NONE;
		panelConstraintsCA.insets = new Insets(5, 5, 5, 5);
		panelLayoutCA.setConstraints(m_bttnCertView, panelConstraintsCA);
		r_panelCA.add(m_bttnCertView);

		return r_panelCA;
	}

	private void showCert()
	{
		Object selected = m_listCert.getSelectedValue();
		if (selected != null)
		{
			CertDetailsDialog dialog = new CertDetailsDialog(m_rootPanel.getParent(),
				( (CertificateInfoStructure) m_listCert.getSelectedValue()).getCertificate().
				getX509Certificate(),
				true, null);
			dialog.setVisible(true);
		}
	}

	private JPanel createCertInfoPanel()
	{
		JPanel r_panelInfo = new JPanel();
		GridBagLayout panelLayoutInfo = new GridBagLayout();
		r_panelInfo.setLayout(panelLayoutInfo);

		GridBagConstraints panelConstraintsInfo = new GridBagConstraints();
		panelConstraintsInfo.anchor = GridBagConstraints.WEST;
		panelConstraintsInfo.weightx = 1.0;
		panelConstraintsInfo.insets = new Insets(0, 10, 0, 0);
		panelConstraintsInfo.gridx = 0;
		panelConstraintsInfo.gridy = 0;
		panelConstraintsInfo.gridwidth = 2;

		m_labelDate = new JLabel("Validity:");
		m_labelCN = new JLabel("Name:");
		m_labelE = new JLabel("eMail:");
		m_labelCSTL = new JLabel("Location:");
		m_labelO = new JLabel("Organisation:");
		m_labelOU = new JLabel("Unit:");
		m_labelDateData = new JLabel();
		m_labelCNData = new JLabel();
		m_labelEData = new JLabel();
		m_labelCSTLData = new JLabel();
		m_labelOData = new JLabel();
		m_labelOUData = new JLabel();

		/*		    	gridx
		 0:				1:
		 gridy	0:
		   1:  labelCN			labelCNData
		   2:	labelO			labelOData
		   3:	labelOU			labelOUData
		   4:	labelCSTL		labelCSTLData
		   5:	labelE			labelEData
		   ---------------------------------------
		   6:  labelDate		labelDateData
		 */

		panelConstraintsInfo.anchor = GridBagConstraints.NORTHWEST;
		panelConstraintsInfo.fill = GridBagConstraints.HORIZONTAL;
		panelConstraintsInfo.gridwidth = 1;
		panelConstraintsInfo.gridx = 0;
		panelConstraintsInfo.gridy = 1;
		panelConstraintsInfo.weightx = 0;
		panelConstraintsInfo.insets = new Insets(10, 15, 0, 0);
		panelLayoutInfo.setConstraints(m_labelCN, panelConstraintsInfo);
		r_panelInfo.add(m_labelCN);

		panelConstraintsInfo.gridx = 1;
		panelConstraintsInfo.gridy = 1;
		panelConstraintsInfo.weightx = 1;
		panelConstraintsInfo.insets = new Insets(10, 10, 0, 10);
		panelLayoutInfo.setConstraints(m_labelCNData, panelConstraintsInfo);
		r_panelInfo.add(m_labelCNData);

		panelConstraintsInfo.anchor = GridBagConstraints.NORTHWEST;
		panelConstraintsInfo.gridx = 0;
		panelConstraintsInfo.gridy = 2;
		panelConstraintsInfo.weightx = 0;
		panelConstraintsInfo.insets = new Insets(10, 15, 0, 0);
		panelLayoutInfo.setConstraints(m_labelO, panelConstraintsInfo);
		r_panelInfo.add(m_labelO);

		panelConstraintsInfo.gridx = 1;
		panelConstraintsInfo.gridy = 2;
		panelConstraintsInfo.weightx = 1;
		panelConstraintsInfo.insets = new Insets(10, 10, 0, 10);
		panelLayoutInfo.setConstraints(m_labelOData, panelConstraintsInfo);
		r_panelInfo.add(m_labelOData);

		panelConstraintsInfo.anchor = GridBagConstraints.NORTHWEST;
		panelConstraintsInfo.gridx = 0;
		panelConstraintsInfo.gridy = 3;
		panelConstraintsInfo.weightx = 0;
		panelConstraintsInfo.insets = new Insets(10, 15, 0, 0);
		panelLayoutInfo.setConstraints(m_labelOU, panelConstraintsInfo);
		r_panelInfo.add(m_labelOU);

		panelConstraintsInfo.gridx = 1;
		panelConstraintsInfo.gridy = 3;
		panelConstraintsInfo.weightx = 1;
		panelConstraintsInfo.insets = new Insets(10, 10, 0, 10);
		panelLayoutInfo.setConstraints(m_labelOUData, panelConstraintsInfo);
		r_panelInfo.add(m_labelOUData);

		panelConstraintsInfo.anchor = GridBagConstraints.NORTHWEST;
		panelConstraintsInfo.gridx = 0;
		panelConstraintsInfo.gridy = 4;
		panelConstraintsInfo.weightx = 0;
		panelConstraintsInfo.insets = new Insets(10, 15, 0, 0);
		panelLayoutInfo.setConstraints(m_labelCSTL, panelConstraintsInfo);
		r_panelInfo.add(m_labelCSTL);

		panelConstraintsInfo.gridx = 1;
		panelConstraintsInfo.gridy = 4;
		panelConstraintsInfo.weightx = 1;
		panelConstraintsInfo.insets = new Insets(10, 10, 0, 10);
		panelLayoutInfo.setConstraints(m_labelCSTLData, panelConstraintsInfo);
		r_panelInfo.add(m_labelCSTLData);

		panelConstraintsInfo.anchor = GridBagConstraints.NORTHWEST;
		panelConstraintsInfo.gridx = 0;
		panelConstraintsInfo.gridy = 5;
		panelConstraintsInfo.weightx = 0;
		panelConstraintsInfo.insets = new Insets(10, 15, 0, 0);
		panelLayoutInfo.setConstraints(m_labelE, panelConstraintsInfo);
		r_panelInfo.add(m_labelE);

		panelConstraintsInfo.gridx = 1;
		panelConstraintsInfo.gridy = 5;
		panelConstraintsInfo.weightx = 1;
		panelConstraintsInfo.insets = new Insets(10, 10, 0, 10);
		panelLayoutInfo.setConstraints(m_labelEData, panelConstraintsInfo);
		r_panelInfo.add(m_labelEData);

		panelConstraintsInfo.anchor = GridBagConstraints.NORTHWEST;
		panelConstraintsInfo.gridx = 0;
		panelConstraintsInfo.gridy = 6;
		panelConstraintsInfo.fill = GridBagConstraints.HORIZONTAL;
		panelConstraintsInfo.weightx = 0;
		panelConstraintsInfo.insets = new Insets(10, 15, 10, 0);
		panelLayoutInfo.setConstraints(m_labelDate, panelConstraintsInfo);
		r_panelInfo.add(m_labelDate);

		panelConstraintsInfo.gridx = 1;
		panelConstraintsInfo.gridy = 6;
		panelConstraintsInfo.weightx = 1;
		panelConstraintsInfo.insets = new Insets(10, 10, 10, 10);
		panelLayoutInfo.setConstraints(m_labelDateData, panelConstraintsInfo);
		r_panelInfo.add(m_labelDateData);
		r_panelInfo.setVisible(true);
		return r_panelInfo;

	}

	public String getHelpContext()
	{
		//return CertificationTool.class.getName();
		return JAPHelpContext.INDEX;
	}

	public void update(Observable a_notifier, Object a_message)
	{
		/**
		 * list init, add certificates by issuer name
		 * It is important to place this here as otherwise a deadlock with
		 * CertificateStore.removeCertificate is possible (this class is an observer...).
		 * Therefore the lock on CertificateStore and on this class should not be mixed!
		 */
		Enumeration enumCerts = SignatureVerifier.getInstance().getVerificationCertificateStore().
			getAllCertificates().elements();

		synchronized (this)
		{
			if (a_notifier == SignatureVerifier.getInstance().getVerificationCertificateStore())
			{
				/* the message is from the SignatureVerifier trusted certificates store */
				int lastIndex = m_listCert.getSelectedIndex();
				m_listmodelCertList.clear();
				m_enumCerts = enumCerts;
				while (m_enumCerts.hasMoreElements())
				{
					CertificateInfoStructure j = (CertificateInfoStructure) m_enumCerts.nextElement();
					/* we handle only root certificates */
					if (j.getCertificateType() == JAPCertificate.CERTIFICATE_TYPE_ROOT_MIX)
					{
						m_listmodelCertList.addElement(j);
					}
				}
				/* select the item again that was selected */
				//lastIndex = Math.min(m_listCert.getComponentCount() - 1, lastIndex);
				//if (lastIndex >= 0)
				//if (m_listmodelCertList.getSize() > 0 && lastIndex >= 0)
				{
					m_listCert.setSelectedIndex(lastIndex);
				}
			}
		}
	}

	private JPanel getRootPanel()
	{
		return m_rootPanel;
	}

	/**
	 * loads the integrated certificates in the "certificates/acceptedCAs/" Directory
	 * This is necessary because the certificates are not loaded at the startup
	 * of the application. In the future this should be changed for being able to
	 * practically use the Enable/Disable/Import/Remove-features of this dialog
	 * @TOD fix this !!!!! Certificates should only be loaded once!
	 */
	public void loadDefaultCertificates()
	{
		JAPCertificate defaultRootCert = null;
		/* each certificate in the directory for the default mix-certs is loaded */
		Enumeration mixCertificates = JAPCertificate.getInstance(CertPanel.CERTPATH_MIX, true).elements();
		while (mixCertificates.hasMoreElements())
		{
			defaultRootCert = (JAPCertificate) mixCertificates.nextElement();
			SignatureVerifier.getInstance().getVerificationCertificateStore().
				addCertificateWithoutVerification(defaultRootCert, JAPCertificate.CERTIFICATE_TYPE_ROOT_MIX, true, true);
		}
		// Additionally load the PaymentCAs
		mixCertificates = JAPCertificate.getInstance(CertPanel.CERTPATH_PAYMENT, true).elements();
		while (mixCertificates.hasMoreElements())
		{
			defaultRootCert = (JAPCertificate) mixCertificates.nextElement();
			SignatureVerifier.getInstance().getVerificationCertificateStore().
				addCertificateWithoutVerification(defaultRootCert, JAPCertificate.CERTIFICATE_TYPE_PAYMENT, true, true);
		}
	}
}
