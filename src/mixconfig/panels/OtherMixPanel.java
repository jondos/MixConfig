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
package mixconfig.panels;

import gui.JAPHelpContext;
import gui.MapBox;
import gui.TitledGridBagPanel;
import gui.dialog.JAPDialog;
import gui.help.JAPHelp;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import logging.LogType;
import mixconfig.MixCertificateView;
import mixconfig.MixConfig;
import mixconfig.OperatorCertificateView;
import anon.crypto.JAPCertificate;
import anon.util.JAPMessages;

/**
 * This class is an abstract superclass for NextMixProxyPanel and PreviousMixPanel
 * @author Tobias Bayer
 */
public abstract class OtherMixPanel extends MixConfigPanel implements ChangeListener,
	JAPHelpContext.IHelpContext
{
	public static final String MIX_TYPE_PREVIOUS = "Previous Mix";
	public static final String MIX_TYPE_NEXT = "Next Mix";
	public static final String TYPE_PROXY = "Proxy";

	private static final String MSG_CERT_NOT_VERIFYABLE = OtherMixPanel.class.getName() + "_certNotVerifyable";

	private String m_type;

	private CertPanel m_otherCert;
	private CertPanel m_otherOpCert;

	private JPanel m_operatorPanel;
	private JPanel m_locationPanel;
	private JTextField m_opOrgField;
	private JTextField m_opOrgUnitField;
	private JTextField m_opCountryField;
	private JTextField m_opUrlField;
	private JTextField m_opEmailField;
	private JTextField m_locCityField;
	private JTextField m_locCountryField;
	private JTextField m_locStateField;
	private JTextField m_locLongField;
	private JTextField m_locLatField;
	private JButton m_mapButton;

	private GridBagLayout m_layout = new GridBagLayout();
	private GridBagConstraints m_gbc;

	public OtherMixPanel(String a_mixType)
	{
		super(a_mixType);
		m_type = a_mixType;

		// Set the layout and get the constraints
		setLayout(m_layout);
		m_gbc = getInitialConstraints();

		m_otherCert = new CertPanel(a_mixType + " Certificate",
									"Hint: You will need to receive the public " +
									"certificate from the operator of the " +
									"other mix", (JAPCertificate)null, CertPanel.CERT_ALGORITHM_DSA, 
									JAPCertificate.CERTIFICATE_TYPE_MIX);
		if (a_mixType == MIX_TYPE_PREVIOUS)
		{
			m_otherCert.setName("Certificates/PrevMixCertificate");
		}
		else if (a_mixType == MIX_TYPE_NEXT)
		{
			m_otherCert.setName("Certificates/NextMixCertificate");
		}
		m_otherCert.setChangedCertNotVerifyableMessage(JAPMessages.getString(MSG_CERT_NOT_VERIFYABLE), null);
		m_otherCert.setCertificateView(new MixCertificateView());
		m_otherCert.addChangeListener(this);
		m_gbc.gridx = 0;
		m_gbc.gridy = 0;
		this.add(m_otherCert, m_gbc);

		m_otherOpCert = new CertPanel(a_mixType + " Operator Certificate",
									  "Hint: You will need to receive the public " +
									  "certificate from the operator of the " +
									  "other mix", (JAPCertificate)null, CertPanel.CERT_ALGORITHM_DSA, 
									  JAPCertificate.CERTIFICATE_TYPE_MIX);
		//m_otherOpCert.setEnabled(false); // not used and therefore disabled at the moment
		/** @todo Specify correct name for operator cert*/
		if (a_mixType == MIX_TYPE_PREVIOUS)
		{
			m_otherOpCert.setName("Certificates/PrevOperatorCertificate");
		}
		else if (a_mixType == MIX_TYPE_NEXT)
		{
			m_otherOpCert.setName("Certificates/NextOperatorCertificate");

		}
		m_otherOpCert.setChangedCertNotVerifyableMessage(JAPMessages.getString(MSG_CERT_NOT_VERIFYABLE), null);
		m_otherOpCert.setCertificateView(new OperatorCertificateView());
		m_otherOpCert.addChangeListener(this);
		m_gbc.gridx++;
		this.add(m_otherOpCert, m_gbc);

		m_locationPanel = this.createLocationPanel();
		m_gbc.gridx = 0;
		m_gbc.gridy++;
		this.add(m_locationPanel, m_gbc);
		m_otherCert.setPreferredSize(new Dimension( (int) m_locationPanel.getPreferredSize().width,
			(int) m_otherCert.getPreferredSize().height));

		// Create the 'Operator'-panel
		m_operatorPanel = this.createOperatorPanel();
		// Make it the same size as m_locationPanel
		m_gbc.fill = GridBagConstraints.BOTH;
		m_gbc.gridx++;
		this.add(m_operatorPanel, m_gbc);
	}

	protected JPanel getLocationPanel()
	{
		return m_locationPanel;
	}

	public String getHelpContext()
	{
		return OtherMixPanel.class.getName();
	}

	public JPanel getOperatorPanel()
	{
		return m_operatorPanel;
	}

	private JPanel createLocationPanel()
	{
		TitledGridBagPanel panel = new TitledGridBagPanel("Location");
		// All of these are read-only
		m_locCityField = new JTextField(MAX_COLUMN_LENGTH);
		m_locCityField.setEditable(false);
		m_locCountryField = new JTextField(MAX_COLUMN_LENGTH);
		m_locCountryField.setEditable(false);
		m_locStateField = new JTextField(MAX_COLUMN_LENGTH);
		m_locStateField.setEditable(false);
		m_locLongField = new JTextField(MAX_COORDINATE_FIELD_LENGTH);
		m_locLongField.setEditable(false);
		m_locLatField = new JTextField(MAX_COORDINATE_FIELD_LENGTH);
		m_locLatField.setEditable(false);
		m_mapButton = new JButton("Show on map");
		m_mapButton.setActionCommand("Map");
		// XXX: OK to set it to true?
		m_mapButton.setEnabled(true);
		m_mapButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// XXX: Test necessary?
				if (!(m_locLatField.getText().equals("") || m_locLongField.getText().equals("")))
				{
					try
					{
						final MapBox mapBox = new MapBox(MixConfig.getMainWindow(),
							m_locLatField.getText(), m_locLongField.getText(), 8);
						mapBox.setVisible(true);
					}
					catch (Exception ex)
					{
						// TODO: Rather log something
						JAPDialog.showErrorDialog(MixConfig.getMainWindow(), null, LogType.GUI, ex);
					}					
				}
			}
		});

		panel.addRow(new JLabel("City"), m_locCityField, null);
		panel.addRow(new JLabel("State"), m_locStateField, null);
		panel.addRow(new JLabel("Country"), m_locCountryField, null);
		panel.addRow(new JLabel("Longitude"), m_locLongField, m_mapButton);
		panel.addRow(new JLabel("Latitude"), m_locLatField, null);

		return panel;
	}

	private JPanel createOperatorPanel()
	{
		// All of these are read-only
		m_opOrgField = new JTextField(MAX_COLUMN_LENGTH);
		m_opOrgField.setEditable(false);
		m_opOrgUnitField = new JTextField(MAX_COLUMN_LENGTH);
		m_opOrgUnitField.setEditable(false);
		m_opCountryField = new JTextField(MAX_COLUMN_LENGTH);
		m_opCountryField.setEditable(false);
		m_opUrlField = new JTextField(MAX_COLUMN_LENGTH);
		m_opUrlField.setEditable(false);
		m_opEmailField = new JTextField(MAX_COLUMN_LENGTH);
		m_opEmailField.setEditable(false);

		TitledGridBagPanel panel = new TitledGridBagPanel("Operator");

		panel.addRow(new JLabel("Organisation"), m_opOrgField);
		panel.addRow(new JLabel("Orga. Unit"), m_opOrgUnitField);
		panel.addRow(new JLabel("Country"), m_opCountryField);
		panel.addRow(new JLabel("URL"), m_opUrlField);
		panel.addRow(new JLabel("E-Mail"), m_opEmailField);

		return panel;
	}

	protected void enableCert(boolean a_enabled)
	{
		m_otherCert.setEnabled(a_enabled);
	}

	protected GridBagLayout getGridBagLayout()
	{
		return m_layout;
	}

	protected GridBagConstraints getGridBagConstraints()
	{
		return m_gbc;
	}

	protected CertPanel getMixCertPanel()
	{
		return m_otherCert;
	}

	protected CertPanel getMixOperatorCertPanel()
	{
		return m_otherOpCert;
	}

	/**
	 * @return Possible error and warning messages
	 * @todo Implement this mixconfig.MixConfigPanel method
	 */
	public Vector<String> check()
	{
		Vector<String> errors = new Vector<String>();

		if (!m_otherCert.isEnabled() && m_otherCert.getCert() != null)
		{
			errors.addElement(
				m_type + " Certificate is present, but there is no corresponding mix.");
		}
		else if (!getConfiguration().isAutoConfigurationAllowed() &&
				 m_otherCert.isEnabled() && m_otherCert.getCert() == null)
		{
			errors.addElement(
				m_type + " Certificate is missing in " + getPanelName() + " panel.");
		}

		return errors;
	}

	public void paint(Graphics g)
	{
		super.paint(g);
		JAPHelp.getInstance().setContext(JAPHelpContext.createHelpContext("index"));
	}

	public void stateChanged(ChangeEvent a_e)
	{
		if (a_e.getSource() == m_otherCert)
		{
			/**
			 * Retrieve information about location from the certificate and put it
			 * into the right text fields
			 */
			MixCertificateView certView = (MixCertificateView) m_otherCert.getCertificateView();
			m_locCityField.setText(certView.getLocalityName());
			m_locCountryField.setText(certView.getCountry());
			m_locStateField.setText(certView.getStateOrProvince());
			m_locLongField.setText(certView.getLongitude());
			m_locLatField.setText(certView.getLatitude());
			if (!certView.isMixCertificate())
			{
				JAPDialog.showWarningDialog(MixConfig.getMainWindow(),
											JAPMessages.getString(MSG_WARNING_NO_MIX_CERT));
			}
		}
		else if (a_e.getSource() == m_otherOpCert)
		{
			m_otherCert.setAdditionalVerifier(m_otherOpCert.getCert());
			m_otherCert.updateCertificateIcon(false);
			OperatorCertificateView certView = (OperatorCertificateView) m_otherOpCert.getCertificateView();
			m_opOrgField.setText(certView.getOrganisation());
			m_opOrgUnitField.setText(certView.getOrganisationalUnit());
			m_opCountryField.setText(certView.getCountry());
			m_opUrlField.setText(certView.getURL());
			m_opEmailField.setText(certView.getEMail());
		}
	}
}
