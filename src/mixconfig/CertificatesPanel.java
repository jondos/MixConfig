/*
 Copyright (c) 2000, The JAP-Team
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
package mixconfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInputStream;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERUTCTime;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.DSAKeyPairGenerator;
import org.bouncycastle.crypto.generators.DSAParametersGenerator;
import org.bouncycastle.crypto.params.DSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import anon.crypto.MyDSAPrivateKey;
import anon.crypto.MyDSAPublicKey;
import anon.crypto.PKCS12;
import anon.crypto.X509CertGenerator;
import anon.util.*;
class CertificatesPanel extends JPanel implements ActionListener
{
	JTextField m_textOwnCertCN, m_textOwnCertValidFrom, m_textOwnCertValidTo, m_textOwnCertIssuer;
	JTextField m_textPrevCertCN, m_textPrevCertValidFrom, m_textPrevCertValidTo, m_textPrevCertIssuer;
	JTextField m_textNextCertCN, m_textNextCertValidFrom, m_textNextCertValidTo, m_textNextCertIssuer;
	JButton m_bttnPrevCertImport,
		m_btnnNextCertImport,
		m_bttnPrevCertExport,
		m_bttnNextCertExport,
		m_bttnPrevCertRemove,
		m_bttnNextCertRemove,
		m_bttnCreateOwnCert,
		m_bttnExportOwnPub,
		m_bttnImportOwnPub,
		m_bttnChangePasswd,
		m_bttnRemoveOwnCert;

	private PKCS12 m_ownPrivCert;
	private String m_strOwnPrivCertPasswd;
	private byte[] m_nextPubCert;
	private byte[] m_prevPubCert;
	private final String STRING_ZERO = "\u0000"; // a String with len=1 and first char=0

	// which is an 'empty' String in C but NOT
	// in general
	/* Note: We use as 'empty' passwd a String with len=0. This is different to an empty C like String, which
	 has len=1 and first char=0. This are in general to different passwds!!
	 */
	public CertificatesPanel()
	{
		m_ownPrivCert = null;
		m_strOwnPrivCertPasswd = null;
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagLayout Own = new GridBagLayout();
		GridBagLayout Previous = new GridBagLayout();
		GridBagLayout Next = new GridBagLayout();

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = GridBagConstraints.HORIZONTAL;

		JPanel panel1 = new JPanel(Own);
		GridBagConstraints d = new GridBagConstraints();
		d.anchor = GridBagConstraints.NORTHWEST;
		d.insets = new Insets(5, 5, 5, 5);
		panel1.setBorder(new TitledBorder("Own Mix Certificate"));
		panel1.setToolTipText(
			"Hint: You have to sent your public test certificate to the operators of your adjacent mixes");
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 1;
		layout.setConstraints(panel1, c);
		add(panel1);

		m_bttnCreateOwnCert = new JButton("Create a New One");
		d.gridx = 1;
		d.gridy = 0;
		d.gridwidth = 1;
		d.fill = GridBagConstraints.NONE;
		m_bttnCreateOwnCert.addActionListener(this);
		m_bttnCreateOwnCert.setActionCommand("Create");
		Own.setConstraints(m_bttnCreateOwnCert, d);
		panel1.add(m_bttnCreateOwnCert);
		m_bttnImportOwnPub = new JButton("Import...");
		d.gridx = 2;
		d.gridy = 0;
		d.gridwidth = 1;
		m_bttnImportOwnPub.addActionListener(this);
		m_bttnImportOwnPub.setActionCommand("ImportOwnCert");
		Own.setConstraints(m_bttnImportOwnPub, d);
		panel1.add(m_bttnImportOwnPub);
		m_bttnExportOwnPub = new JButton("Export...");
		d.gridx = 3;
		d.gridy = 0;
		d.gridwidth = 1;
		m_bttnExportOwnPub.addActionListener(this);
		m_bttnExportOwnPub.setActionCommand("ExportOwnPubCert");
		m_bttnExportOwnPub.setEnabled(false);
		Own.setConstraints(m_bttnExportOwnPub, d);
		panel1.add(m_bttnExportOwnPub);
		m_bttnChangePasswd = new JButton("Change Password");
		d.gridx = 4;
		m_bttnChangePasswd.addActionListener(this);
		m_bttnChangePasswd.setActionCommand("passwd");
		m_bttnChangePasswd.setEnabled(false);
		Own.setConstraints(m_bttnChangePasswd, d);
		panel1.add(m_bttnChangePasswd);
		m_bttnRemoveOwnCert = new JButton("Remove");
		d.gridx = 5;
		m_bttnRemoveOwnCert.addActionListener(this);
		m_bttnRemoveOwnCert.setActionCommand("RemoveOwnCert");
		m_bttnRemoveOwnCert.setEnabled(false);
		Own.setConstraints(m_bttnRemoveOwnCert, d);
		panel1.add(m_bttnRemoveOwnCert);

		d.gridx = 0;
		d.gridy = 1;
		d.fill = GridBagConstraints.HORIZONTAL;
		JLabel name1 = new JLabel("Subject Name");
		Own.setConstraints(name1, d);
		panel1.add(name1);
		m_textOwnCertCN = new JTextField();
		m_textOwnCertCN.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		Own.setConstraints(m_textOwnCertCN, d);
		panel1.add(m_textOwnCertCN);

		d.gridx = 0;
		d.gridy = 2;
		d.fill = GridBagConstraints.HORIZONTAL;
		JLabel name9 = new JLabel("Issuer Name");
		Own.setConstraints(name9, d);
		panel1.add(name9);
		m_textOwnCertIssuer = new JTextField();
		m_textOwnCertIssuer.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		Own.setConstraints(m_textOwnCertIssuer, d);
		panel1.add(m_textOwnCertIssuer);

		JLabel from1 = new JLabel("Valid From");
		d.gridx = 0;
		d.gridy = 3;
		d.gridwidth = 1;
		d.weightx = 0;
		Own.setConstraints(from1, d);
		panel1.add(from1);
		m_textOwnCertValidFrom = new JTextField();
		m_textOwnCertValidFrom.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		Own.setConstraints(m_textOwnCertValidFrom, d);
		panel1.add(m_textOwnCertValidFrom);

		JLabel to1 = new JLabel("Valid To");
		d.gridx = 0;
		d.gridy = 4;
		d.gridwidth = 1;
		d.weightx = 0;
		Own.setConstraints(to1, d);
		panel1.add(to1);
		m_textOwnCertValidTo = new JTextField();
		m_textOwnCertValidTo.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		Own.setConstraints(m_textOwnCertValidTo, d);
		panel1.add(m_textOwnCertValidTo);

		c.gridx = 0;
		c.gridy = 0;
		JPanel panel2 = new JPanel(Previous);
		GridBagConstraints e = new GridBagConstraints();
		e.anchor = GridBagConstraints.NORTHWEST;
		e.insets = new Insets(5, 5, 5, 5);
		e.fill = GridBagConstraints.HORIZONTAL;
		panel2.setBorder(new TitledBorder("Previous Mix Certificate"));
		panel2.setToolTipText(
			"Hint: You will get the public test certificate from the operator of the previous mix");
		layout.setConstraints(panel2, c);
		add(panel2);

		m_bttnPrevCertImport = new JButton("Import...");
		e.gridx = 1;
		e.gridy = 0;
		e.fill = GridBagConstraints.NONE;
		m_bttnPrevCertImport.addActionListener(this);
		m_bttnPrevCertImport.setActionCommand("Import1");
		m_bttnPrevCertImport.setEnabled(false);
		Previous.setConstraints(m_bttnPrevCertImport, e);
		panel2.add(m_bttnPrevCertImport);
		m_bttnPrevCertExport = new JButton("Export...");
		e.gridx = 2;
		m_bttnPrevCertExport.addActionListener(this);
		m_bttnPrevCertExport.setActionCommand("Export1");
		m_bttnPrevCertExport.setEnabled(false);
		Previous.setConstraints(m_bttnPrevCertExport, e);
		panel2.add(m_bttnPrevCertExport);
		m_bttnPrevCertRemove = new JButton("Remove");
		e.gridx = 3;
		m_bttnPrevCertRemove.addActionListener(this);
		m_bttnPrevCertRemove.setActionCommand("Remove1");
		m_bttnPrevCertRemove.setEnabled(false);
		Previous.setConstraints(m_bttnPrevCertRemove, e);
		panel2.add(m_bttnPrevCertRemove);
		e.fill = GridBagConstraints.HORIZONTAL;

		JLabel name2 = new JLabel("Subject Name");
		e.gridx = 0;
		e.gridy = 1;
		Previous.setConstraints(name2, e);
		panel2.add(name2);
		m_textPrevCertCN = new JTextField(26);
		m_textPrevCertCN.setEditable(false);
		e.gridx = 1;
		e.gridwidth = 3;
		e.weightx = 1;
		Previous.setConstraints(m_textPrevCertCN, e);
		panel2.add(m_textPrevCertCN);

		e.gridx = 0;
		e.gridy = 2;
		e.fill = GridBagConstraints.HORIZONTAL;
		JLabel name7 = new JLabel("Issuer Name");
		Previous.setConstraints(name7, e);
		panel2.add(name7);
		m_textPrevCertIssuer = new JTextField();
		m_textPrevCertIssuer.setEditable(false);
		e.gridx = 1;
		e.gridwidth = 5;
		e.weightx = 1;
		Previous.setConstraints(m_textPrevCertIssuer, e);
		panel2.add(m_textPrevCertIssuer);

		JLabel from2 = new JLabel("Valid From");
		e.gridx = 0;
		e.gridy = 3;
		e.gridwidth = 1;
		e.weightx = 0;
		Previous.setConstraints(from2, e);
		panel2.add(from2);
		m_textPrevCertValidFrom = new JTextField(26);
		m_textPrevCertValidFrom.setEditable(false);
		e.gridx = 1;
		e.gridwidth = 3;
		e.weightx = 1;
		Previous.setConstraints(m_textPrevCertValidFrom, e);
		panel2.add(m_textPrevCertValidFrom);

		JLabel to2 = new JLabel("Valid To");
		e.gridx = 0;
		e.gridy = 4;
		e.gridwidth = 1;
		e.weightx = 0;
		Previous.setConstraints(to2, e);
		panel2.add(to2);
		m_textPrevCertValidTo = new JTextField(26);
		m_textPrevCertValidTo.setEditable(false);
		e.gridx = 1;
		e.gridwidth = 3;
		e.weightx = 1;
		Previous.setConstraints(m_textPrevCertValidTo, e);
		panel2.add(m_textPrevCertValidTo);

		c.gridy = 2;
		JPanel panel3 = new JPanel(Next);
		GridBagConstraints f = new GridBagConstraints();
		f.anchor = GridBagConstraints.NORTHWEST;
		f.insets = new Insets(5, 5, 5, 5);
		f.fill = GridBagConstraints.HORIZONTAL;
		panel3.setBorder(new TitledBorder("Next Mix Certificate"));
		panel3.setToolTipText(
			"Hint: You will get the public test certificate from the operator of the next mix");
		layout.setConstraints(panel3, c);
		add(panel3);

		m_btnnNextCertImport = new JButton("Import...");
		f.gridx = 1;
		f.gridy = 0;
		f.fill = GridBagConstraints.NONE;
		m_btnnNextCertImport.addActionListener(this);
		m_btnnNextCertImport.setActionCommand("Import2");
		Next.setConstraints(m_btnnNextCertImport, f);
		panel3.add(m_btnnNextCertImport);
		m_bttnNextCertExport = new JButton("Export...");
		f.gridx = 2;
		m_bttnNextCertExport.addActionListener(this);
		m_bttnNextCertExport.setActionCommand("Export2");
		m_bttnNextCertExport.setEnabled(false);
		Next.setConstraints(m_bttnNextCertExport, f);
		panel3.add(m_bttnNextCertExport);
		m_bttnNextCertRemove = new JButton("Remove");
		f.gridx = 3;
		m_bttnNextCertRemove.addActionListener(this);
		m_bttnNextCertRemove.setActionCommand("Remove2");
		m_bttnNextCertRemove.setEnabled(false);
		Next.setConstraints(m_bttnNextCertRemove, f);
		panel3.add(m_bttnNextCertRemove);
		f.fill = GridBagConstraints.HORIZONTAL;

		JLabel name3 = new JLabel("Subject Name");
		f.gridx = 0;
		f.gridy = 1;
		f.weightx = 0;
		Next.setConstraints(name3, f);
		panel3.add(name3);
		m_textNextCertCN = new JTextField(26);
		m_textNextCertCN.setEditable(false);
		f.gridx = 1;
		f.gridwidth = 3;
		f.weightx = 1;
		Next.setConstraints(m_textNextCertCN, f);
		panel3.add(m_textNextCertCN);

		f.gridx = 0;
		f.gridy = 2;
		f.fill = GridBagConstraints.HORIZONTAL;
		JLabel name8 = new JLabel("Issuer Name");
		Next.setConstraints(name8, f);
		panel3.add(name8);
		m_textNextCertIssuer = new JTextField();
		m_textNextCertIssuer.setEditable(false);
		f.gridx = 1;
		f.gridwidth = 5;
		f.weightx = 1;
		Next.setConstraints(m_textNextCertIssuer, f);
		panel3.add(m_textNextCertIssuer);

		JLabel from3 = new JLabel("Valid From");
		f.gridx = 0;
		f.gridy = 3;
		f.gridwidth = 1;
		f.weightx = 0;
		Next.setConstraints(from3, f);
		panel3.add(from3);
		m_textNextCertValidFrom = new JTextField(26);
		m_textNextCertValidFrom.setEditable(false);
		f.gridx = 1;
		f.gridwidth = 3;
		f.weightx = 1;
		Next.setConstraints(m_textNextCertValidFrom, f);
		panel3.add(m_textNextCertValidFrom);

		JLabel to3 = new JLabel("Valid To");
		f.gridx = 0;
		f.gridy = 4;
		f.gridwidth = 1;
		f.weightx = 0;
		Next.setConstraints(to3, f);
		panel3.add(to3);
		m_textNextCertValidTo = new JTextField(26);
		m_textNextCertValidTo.setEditable(false);
		f.gridx = 1;
		f.gridwidth = 3;
		f.weightx = 1;
		Next.setConstraints(m_textNextCertValidTo, f);
		panel3.add(m_textNextCertValidTo);
	}

	public void clear()
	{
		setOwnPrivCert( (PKCS12)null, null);
		setPrevPubCert(null);
		setNextPubCert(null);
	}

	public void updateButtons(boolean hasPrevious, boolean hasNext)
	{
		m_bttnPrevCertImport.setEnabled(hasPrevious);
		m_btnnNextCertImport.setEnabled(hasNext);
	}

	public byte[] getOwnPubCert()
	{
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			new DEROutputStream(out).writeObject(m_ownPrivCert.getX509cert());
			return out.toByteArray();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public byte[] getOwnPrivCert()
	{
		if (m_ownPrivCert == null)
		{
			return null;
		}
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			m_ownPrivCert.store(out, m_strOwnPrivCertPasswd.toCharArray());
			return out.toByteArray();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public void setOwnPrivCert(byte[] cert)
	{
		if (cert == null)
		{
			setOwnPrivCert( (PKCS12)null, null);
		}
		else
		{
			//Ok the problem is that an empty string (size =0) is different from an empty string (first char=0)
			// so we try both...
			char[] passwd = new char[0];
			if (setOwnPrivCert(cert, passwd))
			{
				return;
			}
			passwd = new char[1];
			passwd[0] = 0;
			while (passwd != null && !setOwnPrivCert(cert, passwd))
			{
				PasswordBox pb =
					new PasswordBox(
					MixConfig.getMainWindow(),
					"Enter the password",
					PasswordBox.ENTER_PASSWORD, null);
				pb.show();
				passwd = pb.getPassword();
			}
		}
	}

	private boolean setOwnPrivCert(byte[] cert, char[] passwd)
	{
		try
		{
			if (cert[0] != (DERInputStream.SEQUENCE | DERInputStream.CONSTRUCTED))
			{
				throw (new RuntimeException("Not a PKCS 12 stream."));
			}
			PKCS12 pkcs12 = PKCS12.load(new ByteArrayInputStream(cert), passwd);
			return setOwnPrivCert(pkcs12, new String(passwd));
		}
		catch (PKCS12.IllegalCertificateException e)
		{
			JOptionPane.showMessageDialog(
				this,
				e.getMessage(),
				"Error while reading the certificate.",
				JOptionPane.ERROR_MESSAGE);
			return false;
		}
		catch (Throwable e)
		{
			return false;
		}
	}

	private boolean setOwnPrivCert(PKCS12 pkcs12, String strOwnPrivCertPasswd)
	{
		try
		{
			if (pkcs12 != null)
			{
				X509CertificateStructure ownPubCert = pkcs12.getX509cert();
				m_textOwnCertValidFrom.setText(
					ownPubCert.getStartDate().getDate().toString());
				m_textOwnCertValidTo.setText(ownPubCert.getEndDate().getDate().toString());
				m_textOwnCertCN.setText(ownPubCert.getSubject().toString());
				m_textOwnCertIssuer.setText(ownPubCert.getIssuer().toString());
				m_ownPrivCert = pkcs12;
				m_strOwnPrivCertPasswd = strOwnPrivCertPasswd;
				if (m_strOwnPrivCertPasswd.equals(STRING_ZERO)) //change a passwd which has len=1 and char[0]=0 to
				{

					//to a passwd with len=0 [because both seam to be 'empty'
					//passwds but are not equal!]
					m_strOwnPrivCertPasswd = "";
				}
				m_bttnExportOwnPub.setEnabled(true);
				m_bttnChangePasswd.setEnabled(true);
				m_bttnRemoveOwnCert.setEnabled(true);
			}
			else
			{
				m_textOwnCertValidFrom.setText(null);
				m_textOwnCertValidTo.setText(null);
				m_textOwnCertCN.setText(null);
				m_textOwnCertIssuer.setText(null);
				m_ownPrivCert = null;
				m_strOwnPrivCertPasswd = null;
				m_bttnExportOwnPub.setEnabled(false);
				m_bttnChangePasswd.setEnabled(false);
				m_bttnRemoveOwnCert.setEnabled(false);
			}
		}
		catch (PKCS12.IllegalCertificateException e)
		{
			JOptionPane.showMessageDialog(
				this,
				e.getMessage(),
				"Error while reading the certificate.",
				JOptionPane.ERROR_MESSAGE);
			return false;

		}
		catch (Throwable e)
		{
			return false;
		}
		return true;
	}

	public byte[] getPrevPubCert()
	{
		return m_prevPubCert;
	}

	public void setPrevPubCert(byte[] cert)
	{
		try
		{
			if (cert != null)
			{
				X509CertificateStructure cert1 = MixConfig.readCertificate(cert);
				m_textPrevCertCN.setText(cert1.getSubject().toString());
				m_textPrevCertIssuer.setText(cert1.getIssuer().toString());
				m_textPrevCertValidFrom.setText(
					cert1.getStartDate().getDate().toString());
				m_textPrevCertValidTo.setText(
					cert1.getEndDate().getDate().toString());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				new DEROutputStream(out).writeObject(cert1);
				m_prevPubCert = out.toByteArray();
				m_bttnPrevCertExport.setEnabled(true);
				m_bttnPrevCertRemove.setEnabled(true);
			}
			else
			{
				m_textPrevCertCN.setText(null);
				m_textPrevCertValidFrom.setText(null);
				m_textPrevCertValidTo.setText(null);
				m_textPrevCertIssuer.setText(null);
				m_prevPubCert = null;
				m_bttnPrevCertExport.setEnabled(false);
				m_bttnPrevCertRemove.setEnabled(false);
			}
		}
		catch (Exception e)
		{
			System.out.println("Prev Cert not set: " + e.getMessage());
			setPrevPubCert(null);
		}
	}

	public byte[] getNextPubCert()
	{
		return m_nextPubCert;
	}

	public void setNextPubCert(byte[] cert)
	{
		try
		{
			if (cert != null)
			{
				X509CertificateStructure cert1 = MixConfig.readCertificate(cert);
				m_textNextCertCN.setText(cert1.getSubject().toString());
				m_textNextCertIssuer.setText(cert1.getIssuer().toString());
				m_textNextCertValidFrom.setText(
					cert1.getStartDate().getDate().toString());
				m_textNextCertValidTo.setText(
					cert1.getEndDate().getDate().toString());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				new DEROutputStream(out).writeObject(cert1);
				m_nextPubCert = out.toByteArray();
				m_bttnNextCertExport.setEnabled(true);
				m_bttnNextCertRemove.setEnabled(true);
			}
			else
			{
				m_textNextCertCN.setText(null);
				m_textNextCertIssuer.setText(null);
				m_textNextCertValidFrom.setText(null);
				m_textNextCertValidTo.setText(null);
				m_nextPubCert = null;
				m_bttnNextCertExport.setEnabled(false);
				m_bttnNextCertRemove.setEnabled(false);
			}
		}
		catch (Exception e)
		{
			System.out.println("Next Cert not set: " + e.getMessage());
			setNextPubCert(null);
		}
	}



public void generateNewCert()
	{
		String oMixid = ConfigFrame.m_GeneralPanel.getMixID();

		if (oMixid == null || oMixid.length() == 0)
		{
			javax.swing.JOptionPane.showMessageDialog(
				this,
				"Please enter Mix ID in general panel.",
				"No Mix ID!",
				javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}
		else if (!ConfigFrame.m_GeneralPanel.isMixIDValid())
		{
			javax.swing.JOptionPane.showMessageDialog(
				this,
				"Please enter a valid Mix ID in general panel,\n"
				+ "starting with a 'm' and containing only letters,\n"
				+ "digits, dots, underscores and minuses.",
				"Invalid Mix ID!",
				javax.swing.JOptionPane.ERROR_MESSAGE);
			return;
		}

		final ValidityDialog vdialog =
			new ValidityDialog(MixConfig.getMainWindow(), "Validity");
		vdialog.show();
		if (vdialog.from == null)
		{
			return;
		}
		PasswordBox dialog =
			new PasswordBox(
			MixConfig.getMainWindow(),
			"New Password",
			PasswordBox.NEW_PASSWORD,
			"This password has to be entered every time the Mix server starts.\n" +
			"So if you want to start it automatically you shouldn't enter a password.");
		dialog.show();
		final char[] passwd = dialog.getPassword();
		if (passwd == null)
		{
			return;
		}
		final String mixid = URLEncoder.encode(oMixid);

		final BusyWindow waitWindow =
			new BusyWindow(MixConfig.getMainWindow(), "Generating Key Pair.");

		SwingWorker worker = new SwingWorker()
		{
			public Object construct()
			{
				X509CertGenerator v3certgen = new X509CertGenerator();
				v3certgen.setStartDate(new DERUTCTime(vdialog.from.getDate()));
				v3certgen.setEndDate(new DERUTCTime(vdialog.to.getDate()));
				v3certgen.setSubject(
					new X509Name("CN=<Mix id=\"" + mixid + "\"/>"));
				v3certgen.setSerialNumber(new DERInteger(1));

				try
				{
					SecureRandom random = new SecureRandom();
					DSAParametersGenerator pGen = new DSAParametersGenerator();
					DSAKeyPairGenerator kpGen = new DSAKeyPairGenerator();
					pGen.init(1024, 20, random);
					kpGen.init(new DSAKeyGenerationParameters(random, pGen.generateParameters()));
					AsymmetricCipherKeyPair ackp = kpGen.generateKeyPair();
					MyDSAPublicKey pubKey = new MyDSAPublicKey( (DSAPublicKeyParameters) ackp.getPublic());
					MyDSAPrivateKey privKey = new MyDSAPrivateKey( (DSAPrivateKeyParameters) ackp.getPrivate());

					v3certgen.setSubjectPublicKeyInfo(
						new SubjectPublicKeyInfo(
						(ASN1Sequence)new DERInputStream(new ByteArrayInputStream(pubKey.getEncoded()))
						.readObject()));
					X509CertificateStructure x509cert =
						v3certgen.sign(new X509Name("CN=<Mix id=\"" + mixid + "\"/>"), privKey);

					PKCS12 pkcs12 =
						new PKCS12(
						"<Mix id=\"" + mixid + "\"/>",
						privKey,
						x509cert
						/*,pubKey*/
						);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					pkcs12.store(out, passwd);
					out.close();
					return out.toByteArray();
				}
				catch (Exception e)
				{
					if (Thread.interrupted())
					{
						return null;
					}
					System.out.println("Error in Key generation and storage!!");
					e.printStackTrace();
				}
				return null;
			}

			public void finished()
			{
				Object cert = get();
				if (cert != null)
				{
					setOwnPrivCert( (byte[]) cert, passwd);
				}
				waitWindow.dispose();
			}
		};
		waitWindow.setSwingWorker(worker);
		worker.start();
	}

	public void exportCert(byte[] cert)
	{
		if (cert == null)
		{
			return;
		}
		try
		{
			JFileChooser fd =
				MixConfig.showFileDialog(
				MixConfig.SAVE_DIALOG,
				MixConfig.FILTER_CER | MixConfig.FILTER_B64_CER);
			FileFilter ff = fd.getFileFilter();
			int type;
			if (ff instanceof SimpleFileFilter)
			{
				type = ( (SimpleFileFilter) ff).getFilterType();
			}
			else
			{
				type = MixConfig.FILTER_B64_CER;
			}
			File file = fd.getSelectedFile();
			if (file != null)
			{
				String fname = file.getName();
				if (fname.indexOf('.') < 0)
				{
					switch (type)
					{
						case MixConfig.FILTER_CER:
							file = new File(file.getParent(), fname + ".der.cer");
							break;
						case MixConfig.FILTER_B64_CER:
							file = new File(file.getParent(), fname + ".b64.cer");
							break;
					}
				}
				try
				{
					FileOutputStream fout = new FileOutputStream(file);
					switch (type)
					{
						case MixConfig.FILTER_CER:
							fout.write(cert);
							break;
						case MixConfig.FILTER_B64_CER:
							fout.write("-----BEGIN CERTIFICATE-----\n".getBytes());
							fout.write(Base64.encode(cert,true).getBytes());
							fout.write("\n-----END CERTIFICATE-----\n".getBytes());
							break;
					}
					fout.close();
				}
				catch (Exception e)
				{
				}
			}
			return;
		}
		catch (Exception e)
		{
		}
		// Wenn wir hier sind, hat etwas nicht geklapppt.

		try
		{
			ClipFrame Save =
				new ClipFrame(
				"Copy and Save this file in a new Location.",
				false);
			Save.setText("-----BEGIN CERTIFICATE-----\n" +
						 Base64.encode(cert,true) +
						 "\n-----END CERTIFICATE-----\n");
			Save.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getActionCommand().equals("Create"))
		{
			generateNewCert();
		}

		else if (ae.getActionCommand().equals("passwd"))
		{
			PasswordBox dialog =
				new PasswordBox(
				MixConfig.getMainWindow(),
				"Change Password",
				PasswordBox.CHANGE_PASSWORD, null);
			while (true)
			{
				dialog.show();
				char[] passwd = dialog.getPassword();
				char[] oldpasswd = dialog.getOldPassword();
				if (passwd == null)
				{
					break;
				}
				try
				{
					if (!m_strOwnPrivCertPasswd.equals(new String(oldpasswd)))
					{
						throw new Exception("Wrong passwd");
					}
					m_strOwnPrivCertPasswd = new String(passwd);
					break;
				}
				catch (Exception e)
				{
					javax.swing.JOptionPane.showMessageDialog(
						this,
						"Wrong Password.",
						"Password Error",
						javax.swing.JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else if (ae.getActionCommand().equalsIgnoreCase("ExportOwnPubCert"))
		{
			try
			{
				JFileChooser fd =
					MixConfig.showFileDialog(
					MixConfig.SAVE_DIALOG,
					MixConfig.FILTER_CER
					| MixConfig.FILTER_B64_CER
					| MixConfig.FILTER_PFX);
				File file = fd.getSelectedFile();
				FileFilter ff = fd.getFileFilter();
				int type;
				if (ff instanceof SimpleFileFilter)
				{
					type = ( (SimpleFileFilter) ff).getFilterType();
				}
				else
				{
					type = MixConfig.FILTER_B64_CER;
				}
				if (file != null)
				{
					String fname = file.getName();
					if (fname.indexOf('.') < 0)
					{
						switch (type)
						{
							case MixConfig.FILTER_PFX:
								file = new File(file.getParent(), fname + ".pfx");
								break;
							case MixConfig.FILTER_CER:
								file =
									new File(file.getParent(), fname + ".der.cer");
								break;
							case MixConfig.FILTER_B64_CER:
								file =
									new File(file.getParent(), fname + ".b64.cer");
								break;
						}
					}
					try
					{
						FileOutputStream fout = new FileOutputStream(file);
						switch (type)
						{
							case MixConfig.FILTER_PFX:
								fout.write(getOwnPrivCert());
								break;
							case MixConfig.FILTER_CER:
								fout.write(getOwnPubCert());
								break;
							case MixConfig.FILTER_B64_CER:
								fout.write(
									"-----BEGIN CERTIFICATE-----\n".getBytes());
								fout.write(
									Base64.encode(getOwnPubCert(),true).getBytes());
								fout.write(
									"\n-----END CERTIFICATE-----\n".getBytes());
								break;
						}
						fout.close();
					}
					catch (Exception e)
					{
					}
				}
				return;
			}
			catch (Exception e)
			{}

			try
			{
				ClipFrame Save =
					new ClipFrame(
					"Copy and Save this file in a new Location.",
					false);
				Save.setText("-----BEGIN CERTIFICATE-----\n" +
							 Base64.encodeBytes(getOwnPubCert()) +
							 "\n-----END CERTIFICATE-----\n");
				Save.show();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
		else if (ae.getActionCommand().equals("ImportOwnCert"))
		{
			byte[] buff;
			try
			{
				int filter = MixConfig.FILTER_PFX;
				if (m_ownPrivCert != null)
				{
					filter |= (MixConfig.FILTER_B64_CER | MixConfig.FILTER_CER);
				}
				buff = MixConfig.openFile(filter);
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(
					this,
					"Import of a private key with certificate\n" +
					"is not supported when running as an applet.",
					"Not supported!",
					javax.swing.JOptionPane.ERROR_MESSAGE);
				m_bttnImportOwnPub.setEnabled(false);
				return;
			}
			if (buff != null)
			{
				//if own key is already set, than maybe only an other certificate for this key is imported...
				boolean bIsPubCert = false;
				if (m_ownPrivCert != null)
				{
					try
					{
						X509CertificateStructure cert1 = MixConfig.readCertificate(buff);
						if (cert1 != null)
						{
							bIsPubCert = true;
						}
						SubjectPublicKeyInfo currentPubKeyInfo = m_ownPrivCert.getX509cert().
							getSubjectPublicKeyInfo();
						SubjectPublicKeyInfo newPubKeyInfo = cert1.getSubjectPublicKeyInfo();
						if (currentPubKeyInfo.getAlgorithmId().equals(newPubKeyInfo.getAlgorithmId()) &&
							currentPubKeyInfo.getPublicKey().equals(newPubKeyInfo.getPublicKey()))
						{
							m_ownPrivCert.setX509cert(cert1);
							setOwnPrivCert(m_ownPrivCert, m_strOwnPrivCertPasswd);
						}
						else
						{
							JOptionPane.showMessageDialog(
								this,
								"This public key certificate does not\n" +
								"belong to your private key!",
								"Wrong certificate!",
								JOptionPane.ERROR_MESSAGE);
						}
					}
					catch (Exception e)
					{
						bIsPubCert = false;
					}
				}
				if (!bIsPubCert)
				{
					setOwnPrivCert(buff);
				}
			}
		}
		else if (ae.getActionCommand().equals("RemoveOwnCert"))
		{
			setOwnPrivCert(null);
		}
		else if (ae.getActionCommand().equals("Import1"))
		{
			byte[] cert;
			try
			{
				cert = MixConfig.openFile(MixConfig.FILTER_CER | MixConfig.FILTER_B64_CER);
			}
			catch (Exception e)
			{

				ClipFrame Open =
					new ClipFrame(
					"Paste a certificate to be imported in the area provided.",
					true);
				Open.show();
				cert = Open.getText().getBytes();
			}
			setPrevPubCert(cert);
		}
		else if (ae.getActionCommand().equals("Export1"))
		{
			exportCert(getPrevPubCert());
		}
		else if (ae.getActionCommand().equals("Remove1"))
		{
			setPrevPubCert(null);
		}
		else if (ae.getActionCommand().equals("Import2"))
		{
			byte[] cert;
			try
			{
				cert = MixConfig.openFile(MixConfig.FILTER_CER | MixConfig.FILTER_B64_CER);
			}
			catch (Exception e)
			{

				ClipFrame Open =
					new ClipFrame(
					"Paste a certificate to be imported in the area provided.",
					true);
				Open.show();
				cert = Open.getText().getBytes();
			}
			setNextPubCert(cert);
		}
		else if (ae.getActionCommand().equals("Export2"))
		{
			exportCert(getNextPubCert());
		}
		else if (ae.getActionCommand().equals("Remove2"))
		{
			setNextPubCert(null);
		}
	}
}


