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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.filechooser.FileFilter;
import java.security.SecureRandom;
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
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;


/**
 * The PaymentPanel is one page in the MixConfig TabbedPane and allows the user to specify 
 * the data which is needed for the mix to successfully use payment, namely the JPI Host/Port, 
 * and the Postgresql Database Host/Port/DBName/Username.
 *
 * @author Bastian Voigt
 * @version 0.1
 */
class PaymentPanel 
extends JPanel
implements ActionListener
{
	protected JCheckBox m_chkPaymentEnabled;
	protected JTextField m_textJPIName;
	protected JTextField m_textJPIHost;
	protected JTextField m_textJPIPort;
	protected JTextField m_textDatabaseHost;
	protected JTextField m_textDatabasePort;
	protected JTextField m_textDatabaseDBName;
	protected JTextField m_textDatabaseUsername;	
	
	protected JButton m_bttnCreateJpiCert;
	protected JButton m_bttnImportJpiPub;
	protected JButton m_bttnExportJpiPub;
	protected JButton m_bttnChangePasswd;
	protected JButton m_bttnRemoveJpiCert;
	
	protected JTextField m_textJpiCertCN;
	protected JTextField m_textJpiCertIssuer;
	protected JTextField m_textJpiCertValidFrom;
	protected JTextField m_textJpiCertValidTo;
	
	protected JPanel m_miscPanel;
	protected JPanel m_jpiPanel;
	protected JPanel m_certPanel;
	protected JPanel m_databasePanel;
	
	private PKCS12 m_jpiPrivCert = null;
	private String m_strJpiPrivCertPasswd = null;
	private final String STRING_ZERO = "\u0000";


	public PaymentPanel()
	{
		JLabel label;
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		
		GridBagConstraints d = new GridBagConstraints();
		d.anchor = GridBagConstraints.NORTHWEST;
		d.insets = new Insets(5, 5, 5, 5);
		d.fill = GridBagConstraints.HORIZONTAL;
		d.gridx = 0;
		d.gridy = 0;
		d.weightx = 0;
 		d.weighty = 1;
		
		
		// MISC Panel
		m_miscPanel = new JPanel(new FlowLayout());
		m_miscPanel.setBorder(new TitledBorder("Payment misc"));
		m_miscPanel.setToolTipText("Please select whether you want to enable payment.<br>"+
																"This is only possible for the FirstMix at the moment");

		m_chkPaymentEnabled = new JCheckBox("Enable Payment");
		m_chkPaymentEnabled.setSelected(false);
		m_chkPaymentEnabled.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e) {
					boolean enabled = m_chkPaymentEnabled.isSelected();
					setEnabled(true);
				}
			});	
		m_miscPanel.add(m_chkPaymentEnabled);		
		layout.setConstraints(m_miscPanel, c);
		this.add(m_miscPanel);
		
		if(ConfigFrame.m_GeneralPanel.getMixType()!="FirstMix") { 
			// we are not FirstMix? -> no payment
			m_chkPaymentEnabled.setEnabled(false);
		}
		
				
		// JPI Panel
		GridBagLayout jpiLayout = new GridBagLayout();
		m_jpiPanel = new JPanel(jpiLayout);
		m_jpiPanel.setBorder(new TitledBorder("JPI (Java Payment Instance)"));
		m_jpiPanel.setToolTipText("Please enter the Hostname or IP Address and the port number of the JPI<br> "+
														"that your mix should use.");
														
														
		label = new JLabel("JPI Name:");
		d.gridy=0; d.gridx=0; d.weightx=0;
		jpiLayout.setConstraints(label, d);
		m_jpiPanel.add(label);
		
		m_textJPIName = new JTextField();
		d.gridx=1; d.weightx=1;
		jpiLayout.setConstraints(m_textJPIName, d);
		m_jpiPanel.add(m_textJPIName);		
		
		label = new JLabel("JPI Hostname:");
		d.gridy++; d.gridx=0; d.weightx=0;
		jpiLayout.setConstraints(label, d);
		m_jpiPanel.add(label);

		m_textJPIHost = new JTextField();
		d.gridx=1; d.weightx=1;
		jpiLayout.setConstraints(m_textJPIHost, d);
		m_jpiPanel.add(m_textJPIHost);

		label = new JLabel("JPI Portnumber:");
		d.gridy++; d.gridx=0; d.weightx=0;
		jpiLayout.setConstraints(label, d);
		m_jpiPanel.add(label);
		
		m_textJPIPort = new JTextField();
		d.gridx=1; d.weightx=1;
		jpiLayout.setConstraints(m_textJPIPort, d);
		m_jpiPanel.add(m_textJPIPort);
										
		c.gridy++;						
		layout.setConstraints(m_jpiPanel, c);
		this.add(m_jpiPanel);


		// JPI Certificate Panel
		d.gridx=0; d.gridy=0; d.weightx=0;
		GridBagLayout certLayout = new GridBagLayout();
		m_certPanel = new JPanel(certLayout);
		m_certPanel.setBorder(new TitledBorder("JPI Certificate"));
		m_certPanel.setToolTipText(
			"If you have the Public Certificate of a running JPI, you can import it here.<br> "+
			"Or, if you want to setup your own JPI, you can generate a certificate here<br> "+
			"and copy it to your JPI configuration.");

		m_bttnCreateJpiCert = new JButton("Create a New One");
		d.gridx = 1;
		d.gridy = 0;
		d.gridwidth = 1;
		d.fill = GridBagConstraints.NONE;
		m_bttnCreateJpiCert.addActionListener(this);
		m_bttnCreateJpiCert.setActionCommand("Create");
		certLayout.setConstraints(m_bttnCreateJpiCert, d);
		m_certPanel.add(m_bttnCreateJpiCert);
		
		m_bttnImportJpiPub = new JButton("Import...");
		d.gridx = 2;
		d.gridy = 0;
		d.gridwidth = 1;
		m_bttnImportJpiPub.addActionListener(this);
		m_bttnImportJpiPub.setActionCommand("ImportJpiCert");
		certLayout.setConstraints(m_bttnImportJpiPub, d);
		m_certPanel.add(m_bttnImportJpiPub);
		
		m_bttnExportJpiPub = new JButton("Export...");
		d.gridx = 3;
		d.gridy = 0;
		d.gridwidth = 1;
		m_bttnExportJpiPub.addActionListener(this);
		m_bttnExportJpiPub.setActionCommand("ExportJpiPubCert");
 		m_bttnExportJpiPub.setEnabled(false);
		certLayout.setConstraints(m_bttnExportJpiPub, d);
		m_certPanel.add(m_bttnExportJpiPub);
		
		m_bttnChangePasswd = new JButton("Change Password");
		d.gridx = 4;
		m_bttnChangePasswd.addActionListener(this);
		m_bttnChangePasswd.setActionCommand("passwd");
		m_bttnChangePasswd.setEnabled(false);
		certLayout.setConstraints(m_bttnChangePasswd, d);
		m_certPanel.add(m_bttnChangePasswd);
		
		m_bttnRemoveJpiCert = new JButton("Remove");
		d.gridx = 5;
		m_bttnRemoveJpiCert.addActionListener(this);
		m_bttnRemoveJpiCert.setActionCommand("RemoveJpiCert");
		m_bttnRemoveJpiCert.setEnabled(false);
		certLayout.setConstraints(m_bttnRemoveJpiCert, d);
		m_certPanel.add(m_bttnRemoveJpiCert);

		d.gridx = 0;
		d.gridy = 1;
		d.fill = GridBagConstraints.HORIZONTAL;
		JLabel name1 = new JLabel("Subject Name");
		certLayout.setConstraints(name1, d);
		m_certPanel.add(name1);
		m_textJpiCertCN = new JTextField();
		m_textJpiCertCN.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		certLayout.setConstraints(m_textJpiCertCN, d);
		m_certPanel.add(m_textJpiCertCN);

		d.gridx = 0;
		d.gridy = 2;
		d.fill = GridBagConstraints.HORIZONTAL;
		JLabel name9 = new JLabel("Issuer Name");
		certLayout.setConstraints(name9, d);
		m_certPanel.add(name9);
		m_textJpiCertIssuer = new JTextField();
		m_textJpiCertIssuer.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		certLayout.setConstraints(m_textJpiCertIssuer, d);
		m_certPanel.add(m_textJpiCertIssuer);

		JLabel from1 = new JLabel("Valid From");
		d.gridx = 0;
		d.gridy = 3;
		d.gridwidth = 1;
		d.weightx = 0;
		certLayout.setConstraints(from1, d);
		m_certPanel.add(from1);
		m_textJpiCertValidFrom = new JTextField();
		m_textJpiCertValidFrom.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		certLayout.setConstraints(m_textJpiCertValidFrom, d);
		m_certPanel.add(m_textJpiCertValidFrom);

		JLabel to1 = new JLabel("Valid To");
		d.gridx = 0;
		d.gridy = 4;
		d.gridwidth = 1;
		d.weightx = 0;
		certLayout.setConstraints(to1, d);
		m_certPanel.add(to1);
		m_textJpiCertValidTo = new JTextField();
		m_textJpiCertValidTo.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		certLayout.setConstraints(m_textJpiCertValidTo, d);
		m_certPanel.add(m_textJpiCertValidTo);
		
		c.gridy++;
		layout.setConstraints(m_certPanel, c);
		this.add(m_certPanel);



		// DATABASE Panel
		GridBagLayout databaseLayout = new GridBagLayout();
		m_databasePanel = new JPanel(databaseLayout);
		m_databasePanel.setBorder(new TitledBorder("PostgreSQL Database for the accounting instance"));
		m_databasePanel.setToolTipText("The accounting instance inside the First Mix needs a PostgreSQL<br> "+
																	"database to store some internal accounting data. Before you start<br> "+
																	"the First Mix with payment enabled, setup a Postgresql DB and enter<br> "+
																	"its connection data here.");
																	
		label = new JLabel("Database Hostname:");
		d.anchor = GridBagConstraints.NORTHWEST;
		d.fill = GridBagConstraints.HORIZONTAL;
		d.gridx=0; d.gridy=0; d.weightx=0;
		d.gridwidth=1;		
		databaseLayout.setConstraints(label, d);
		m_databasePanel.add(label);

		m_textDatabaseHost = new JTextField();
		d.gridx=1; d.weightx=1;
		databaseLayout.setConstraints(m_textDatabaseHost, d);
		m_databasePanel.add(m_textDatabaseHost);		
																			
		label = new JLabel("Database Portnumber:");
		d.gridy++; d.gridx=0; d.weightx=0;
		databaseLayout.setConstraints(label, d);
		m_databasePanel.add(label);

		m_textDatabasePort = new JTextField();
		d.gridx=1; d.weightx=1;
		databaseLayout.setConstraints(m_textDatabasePort, d);
		m_databasePanel.add(m_textDatabasePort);				
		
		label = new JLabel("Database DBName:");
		d.gridy++; d.gridx=0; d.weightx=0;
		databaseLayout.setConstraints(label, d);
		m_databasePanel.add(label);
		
		m_textDatabaseDBName = new JTextField();
		d.gridx=1; d.weightx=1;
		databaseLayout.setConstraints(m_textDatabaseDBName, d);
		m_databasePanel.add(m_textDatabaseDBName);

		label = new JLabel("Database Username:");
		d.gridy++; d.gridx=0; d.weightx=0;
		databaseLayout.setConstraints(label, d);
		m_databasePanel.add(label);
		
		m_textDatabaseUsername = new JTextField();
		d.gridx=1; d.weightx=1;
		databaseLayout.setConstraints(m_textDatabaseUsername, d);
		m_databasePanel.add(m_textDatabaseUsername);
																			
		c.gridy++;
		layout.setConstraints(m_databasePanel, c);
		this.add(m_databasePanel);

		setEnabled(true);
	}

	
	public void setJpiPrivCert(byte[] cert)
	{
		if (cert == null)
		{
			setJpiPrivCert( (PKCS12)null, null);
		}
		else
		{
			//Ok the problem is that an empty string (size =0) is different from an empty string (first char=0)
			// so we try both...
			char[] passwd = new char[0];
			if (setJpiPrivCert(cert, passwd))
			{
				return;
			}
			passwd = new char[1];
			passwd[0] = 0;
			while (passwd != null && !setJpiPrivCert(cert, passwd))
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

	private boolean setJpiPrivCert(byte[] cert, char[] passwd)
	{
		try
		{
			if (cert[0] != (DERInputStream.SEQUENCE | DERInputStream.CONSTRUCTED))
			{
				throw (new RuntimeException("Not a PKCS 12 stream."));
			}
			PKCS12 pkcs12 = PKCS12.load(new ByteArrayInputStream(cert), passwd);
			return setJpiPrivCert(pkcs12, new String(passwd));
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
	
	private boolean setJpiPrivCert(PKCS12 pkcs12, String strJpiPrivCertPasswd)
	{
		try
		{
			if (pkcs12 != null)
			{
				X509CertificateStructure jpiPubCert = pkcs12.getX509cert();
				m_textJpiCertValidFrom.setText(
					jpiPubCert.getStartDate().getDate().toString());
				m_textJpiCertValidTo.setText(jpiPubCert.getEndDate().getDate().toString());
				m_textJpiCertCN.setText(jpiPubCert.getSubject().toString());
				m_textJpiCertIssuer.setText(jpiPubCert.getIssuer().toString());
				m_jpiPrivCert = pkcs12;
				m_strJpiPrivCertPasswd = strJpiPrivCertPasswd;
				if (m_strJpiPrivCertPasswd.equals(STRING_ZERO)) //change a passwd which has len=1 and char[0]=0 to
				{

					//to a passwd with len=0 [because both seam to be 'empty'
					//passwds but are not equal!]
					m_strJpiPrivCertPasswd = "";
				}
				m_bttnExportJpiPub.setEnabled(true);
				m_bttnChangePasswd.setEnabled(true);
				m_bttnRemoveJpiCert.setEnabled(true);
			}
			else
			{
				m_textJpiCertValidFrom.setText(null);
				m_textJpiCertValidTo.setText(null);
				m_textJpiCertCN.setText(null);
				m_textJpiCertIssuer.setText(null);
				m_jpiPrivCert = null;
				m_strJpiPrivCertPasswd = null;
				m_bttnExportJpiPub.setEnabled(false);
				m_bttnChangePasswd.setEnabled(false);
				m_bttnRemoveJpiCert.setEnabled(false);
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

	
/*	public void setJPIPubCert() {}
	public byte[] getJPIPubCert() {}*/
	
	/**
	 * Generates a new certificate for the JPI
	 */	
	public void generateNewJPICert()
	{
		if(m_textJPIName.getText().equals("")) {
			javax.swing.JOptionPane.showMessageDialog(
				this,
				"Please enter JPI Name first!",
				"No JPI Name!",
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
			"This password has to be entered every time the JPI server starts.\n" +
			"If you want the JPI to start without user interaction, you should leave this\n"+
			"passworld field blank.\n");
		dialog.show();
		final char[] passwd = dialog.getPassword();
		if (passwd == null)
		{
			return;
		}
		//final String mixid = URLEncoder.encode(oMixid);

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
					new X509Name("CN="+m_textJPIName.getText()));
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
						v3certgen.sign(new X509Name("CN="+m_textJPIName.getText()), privKey); // TODO: Richtigen namen eintragen

					PKCS12 pkcs12 =
						new PKCS12(
						m_textJPIName.getText(), // TODO: richtigen namen
						privKey,
						x509cert
						//,pubKey
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
					setJpiPrivCert( (byte[]) cert, passwd);
				}
				waitWindow.dispose();
			}
		};
		waitWindow.setSwingWorker(worker);
		worker.start();
	}



	public void setEnabled(boolean isEnabled)
	{
		super.setEnabled(isEnabled);
		m_chkPaymentEnabled.setEnabled(isEnabled);
		boolean compEnabled=false;
		if(isEnabled && m_chkPaymentEnabled.isSelected())
			compEnabled=true;

		// enable all components
		Component[] co = m_jpiPanel.getComponents();
		int i;
		for(i=0;i<co.length;i++) co[i].setEnabled(compEnabled);
		co = m_certPanel.getComponents();
		for(i=0;i<co.length;i++) {
			if( co[i]==m_bttnExportJpiPub || co[i]==m_bttnChangePasswd || co[i]==m_bttnRemoveJpiCert ) {
				if(m_jpiPrivCert!=null) co[i].setEnabled(compEnabled);
			}
			else {
				co[i].setEnabled(compEnabled);
			}
		}
		co = m_databasePanel.getComponents();
		for(i=0;i<co.length;i++) co[i].setEnabled(compEnabled);
	}
	
	
	public byte[] getJpiPubCert()
	{
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			new DEROutputStream(out).writeObject(m_jpiPrivCert.getX509cert());
			return out.toByteArray();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public byte[] getJpiPrivCert()
	{
		if (m_jpiPrivCert == null)
		{
			return null;
		}
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			m_jpiPrivCert.store(out, m_strJpiPrivCertPasswd.toCharArray());
			return out.toByteArray();
		}
		catch (Exception e)
		{
			return null;
		}
	}


	public void actionPerformed(ActionEvent ae)
	{
		System.out.println("ActionEvent "+ae.getActionCommand());
		String cmd = ae.getActionCommand();
		if (cmd.equals("Create"))
		{
			generateNewJPICert();
		}
		else if (cmd.equals("passwd"))
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
					if (!m_strJpiPrivCertPasswd.equals(new String(oldpasswd)))
					{
						throw new Exception("Wrong passwd");
					}
					m_strJpiPrivCertPasswd = new String(passwd);
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
		else if(cmd.equals("ExportJpiPubCert")) {
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
								fout.write(getJpiPrivCert());
								break;
							case MixConfig.FILTER_CER:
								fout.write(getJpiPubCert());
								break;
							case MixConfig.FILTER_B64_CER:
								fout.write(
									"-----BEGIN CERTIFICATE-----\n".getBytes());
								fout.write(
									Base64.encode(getJpiPubCert(),true).getBytes());
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
							 Base64.encodeBytes(getJpiPubCert()) +
							 "\n-----END CERTIFICATE-----\n");
				Save.show();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}