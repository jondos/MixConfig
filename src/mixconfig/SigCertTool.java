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

import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import java.security.PrivateKey;

import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERInputStream;
import anon.crypto.*;
import anon.util.*;

public class SigCertTool extends JDialog implements ActionListener
{
	private JButton m_bttnExportCertToSign;
	private JTextField m_textCertToSignValidFrom, m_textCertToSignCN, m_textCertToSignIssuer,
		m_textCertToSignValidTo;
	private JTextField m_textSignWithCertCN, m_textSignWithCertValidFrom, m_textSignWithCertValidTo;
	private X509CertificateStructure m_certToSign;
	private MyDSAPrivateKey m_keySignWith;
	private X509Name m_x509Issuer;

	public SigCertTool(Frame parent)
	{
		super(parent, "Sign a public key Certificate", true);

		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);
		GridBagLayout layoutToSign = new GridBagLayout();
		GridBagLayout layoutSignWith = new GridBagLayout();
		GridBagLayout layoutBttns = new GridBagLayout();

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = GridBagConstraints.HORIZONTAL;

		JPanel panel1 = new JPanel(layoutToSign);
		GridBagConstraints d = new GridBagConstraints();
		d.anchor = GridBagConstraints.NORTHWEST;
		d.insets = new Insets(5, 5, 5, 5);
		panel1.setBorder(new TitledBorder("Certificate to sign"));
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		layout.setConstraints(panel1, c);
		getContentPane().add(panel1);

		JButton bttnImport = new JButton("Import...");
		d.gridx = 1;
		d.gridy = 0;
		d.gridwidth = 1;
		d.fill = GridBagConstraints.NONE;
		bttnImport.addActionListener(this);
		bttnImport.setActionCommand("importCertToSign");
		layoutToSign.setConstraints(bttnImport, d);
		panel1.add(bttnImport);
		m_bttnExportCertToSign = new JButton("Export...");
		d.gridx = 2;
		d.gridy = 0;
		d.gridwidth = 1;
		m_bttnExportCertToSign.addActionListener(this);
		m_bttnExportCertToSign.setActionCommand("exportCertToSign");
		m_bttnExportCertToSign.setEnabled(false);
		layoutToSign.setConstraints(m_bttnExportCertToSign, d);
		panel1.add(m_bttnExportCertToSign);

		d.gridx = 0;
		d.gridy = 1;
		d.fill = GridBagConstraints.HORIZONTAL;
		JLabel name1 = new JLabel("Subjet Name");
		layoutToSign.setConstraints(name1, d);
		panel1.add(name1);
		m_textCertToSignCN = new JTextField();
		m_textCertToSignCN.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		layoutToSign.setConstraints(m_textCertToSignCN, d);
		panel1.add(m_textCertToSignCN);

		d.gridx = 0;
		d.gridy = 2;
		d.fill = GridBagConstraints.HORIZONTAL;
		JLabel name7 = new JLabel("Issuer Name");
		layoutToSign.setConstraints(name7, d);
		panel1.add(name7);
		m_textCertToSignIssuer = new JTextField();
		m_textCertToSignIssuer.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		layoutToSign.setConstraints(m_textCertToSignIssuer, d);
		panel1.add(m_textCertToSignIssuer);

		JLabel from1 = new JLabel("Valid From");
		d.gridx = 0;
		d.gridy = 3;
		d.gridwidth = 1;
		d.weightx = 0;
		layoutToSign.setConstraints(from1, d);
		panel1.add(from1);
		m_textCertToSignValidFrom = new JTextField();
		m_textCertToSignValidFrom.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		layoutToSign.setConstraints(m_textCertToSignValidFrom, d);
		panel1.add(m_textCertToSignValidFrom);

		JLabel to1 = new JLabel("Valid To");
		d.gridx = 0;
		d.gridy = 4;
		d.gridwidth = 1;
		d.weightx = 0;
		layoutToSign.setConstraints(to1, d);
		panel1.add(to1);
		m_textCertToSignValidTo = new JTextField();
		m_textCertToSignValidTo.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		layoutToSign.setConstraints(m_textCertToSignValidTo, d);
		panel1.add(m_textCertToSignValidTo);

		c.gridx = 0;
		c.gridy = 1;
		JPanel panel2 = new JPanel(layoutSignWith);
		GridBagConstraints e = new GridBagConstraints();
		e.anchor = GridBagConstraints.NORTHWEST;
		e.insets = new Insets(5, 5, 5, 5);
		e.fill = GridBagConstraints.HORIZONTAL;
		panel2.setBorder(new TitledBorder("Sign with"));
		layout.setConstraints(panel2, c);
		getContentPane().add(panel2);

		JButton import1 = new JButton("Import...");
		e.gridx = 1;
		e.gridy = 0;
		e.fill = GridBagConstraints.NONE;
		import1.addActionListener(this);
		import1.setActionCommand("importCertSignWith");
		layoutSignWith.setConstraints(import1, e);
		panel2.add(import1);
		e.fill = GridBagConstraints.HORIZONTAL;

		JLabel name2 = new JLabel("Name");
		e.gridx = 0;
		e.gridy = 1;
		layoutSignWith.setConstraints(name2, e);
		panel2.add(name2);
		m_textSignWithCertCN = new JTextField(26);
		m_textSignWithCertCN.setEditable(false);
		e.gridx = 1;
		e.gridwidth = 3;
		e.weightx = 1;
		layoutSignWith.setConstraints(m_textSignWithCertCN, e);
		panel2.add(m_textSignWithCertCN);

		JLabel from2 = new JLabel("Valid From");
		e.gridx = 0;
		e.gridy = 2;
		e.gridwidth = 1;
		e.weightx = 0;
		layoutSignWith.setConstraints(from2, e);
		panel2.add(from2);
		m_textSignWithCertValidFrom = new JTextField(26);
		m_textSignWithCertValidFrom.setEditable(false);
		e.gridx = 1;
		e.gridwidth = 3;
		e.weightx = 1;
		layoutSignWith.setConstraints(m_textSignWithCertValidFrom, e);
		panel2.add(m_textSignWithCertValidFrom);

		JLabel to2 = new JLabel("Valid To");
		e.gridx = 0;
		e.gridy = 3;
		e.gridwidth = 1;
		e.weightx = 0;
		layoutSignWith.setConstraints(to2, e);
		panel2.add(to2);
		m_textSignWithCertValidTo = new JTextField(26);
		m_textSignWithCertValidTo.setEditable(false);
		e.gridx = 1;
		e.gridwidth = 3;
		e.weightx = 1;
		layoutSignWith.setConstraints(m_textSignWithCertValidTo, e);
		panel2.add(m_textSignWithCertValidTo);

		JPanel panel3 = new JPanel(layoutBttns);
		GridBagConstraints f = new GridBagConstraints();
		f.anchor = GridBagConstraints.NORTHEAST;
		f.insets = new Insets(5, 5, 5, 5);

		JLabel space = new JLabel();
		f.gridx = 0;
		f.gridy = 0;
		f.weightx = 1;
		f.fill = f.HORIZONTAL;
		layoutBttns.setConstraints(space, f);
		panel3.add(space);

		JButton bttnSign = new JButton("Sign");
		bttnSign.setActionCommand("Sign");
		bttnSign.addActionListener(this);
		f.gridx = 0;
		f.gridy = 0;
		f.weightx = 0;
		f.fill = f.NONE;
		layoutBttns.setConstraints(bttnSign, f);
		panel3.add(bttnSign);

		JButton bttnCancel = new JButton("Cancel");
		bttnCancel.setActionCommand("Cancel");
		bttnCancel.addActionListener(this);
		f.gridx = 1;
		f.gridy = 0;
		layoutBttns.setConstraints(bttnCancel, f);
		panel3.add(bttnCancel);

		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1;
		c.weighty = 1;
		layout.setConstraints(panel3, c);
		getContentPane().add(panel3);

		pack();
		setLocationRelativeTo(parent);
		show();
	}

	private void setCertToSign(byte[] cert)
	{
		try
		{
			if (cert != null)
			{
				setCertToSign(MixConfig.readCertificate(cert));
			}
			else
			{
				setCertToSign( (X509CertificateStructure)null);
			}
		}
		catch (Exception e)
		{
			MixConfig.handleException(e);
			System.out.println("Cert to Sign not set: " + e.getMessage());
			setCertToSign( (X509CertificateStructure)null);
		}
	}

	private void setCertToSign(X509CertificateStructure cert)
	{
		try
		{
			if (cert != null)
			{
				m_textCertToSignCN.setText(cert.getSubject().toString());
				m_textCertToSignValidFrom.setText(
					cert.getStartDate().getDate().toString());
				m_textCertToSignValidTo.setText(
					cert.getEndDate().getDate().toString());
				m_textCertToSignIssuer.setText(cert.getIssuer().toString());
				m_certToSign = cert;
				m_bttnExportCertToSign.setEnabled(true);
			}
			else
			{
				m_textCertToSignCN.setText(null);
				m_textCertToSignValidFrom.setText(null);
				m_textCertToSignValidTo.setText(null);
				m_textCertToSignIssuer.setText(null);
				m_certToSign = null;
				m_bttnExportCertToSign.setEnabled(false);
			}
		}
		catch (Exception e)
		{
			MixConfig.handleException(e);
			System.out.println("Cert to Sign not set: " + e.getMessage());
			setCertToSign( (X509CertificateStructure)null);
		}
	}

	private void setCertSignWithPrivCert(byte[] cert)
	{
		if (cert == null)
		{
			setCertSignWithPrivCert(null, null);
		}
		else
		{
			char[] passwd = new char[]
				{
			};
			while (passwd != null && !setCertSignWithPrivCert(cert, passwd))
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

	private boolean setCertSignWithPrivCert(byte[] cert, char[] passwd)
	{
		try
		{
			if (cert != null)
			{
				if (cert[0]
					!= (DERInputStream.SEQUENCE | DERInputStream.CONSTRUCTED))
				{
					throw (new RuntimeException("Not a PKCS 12 stream."));
				}

				PKCS12 pkcs12 = PKCS12.load(new ByteArrayInputStream(cert), passwd);

				m_textSignWithCertValidFrom.setText(
					pkcs12.getX509cert().getStartDate().getDate().toString());
				m_textSignWithCertValidTo.setText(pkcs12.getX509cert().getEndDate().getDate().toString());
				m_textSignWithCertCN.setText(pkcs12.getX509cert().getSubject().toString());
				m_keySignWith = (MyDSAPrivateKey) pkcs12.getPrivKey();
				m_x509Issuer = pkcs12.getX509cert().getSubject();
				return true;

			}
			else
			{
				m_textSignWithCertValidFrom.setText(null);
				m_textSignWithCertValidTo.setText(null);
				m_textSignWithCertCN.setText(null);
				m_keySignWith = null;
			}
		}
		catch (PKCS12.IllegalCertificateException e)
		{
			JOptionPane.showMessageDialog(
				this,
				e.getMessage(),
				"Error while reading the certificate.",
				JOptionPane.ERROR_MESSAGE);
			return true;

		}
		catch (Throwable e)
		{
			return false;
		}
		return true;
	}

	private void sign()
	{
		X509CertGenerator certgen = new X509CertGenerator(m_certToSign);
		m_certToSign = certgen.sign(m_x509Issuer, m_keySignWith);
		setCertToSign(m_certToSign);
	}

	private byte[] getCertToSignAsByteArray() throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new DEROutputStream(out).writeObject(m_certToSign);
		return out.toByteArray();
	}

	public void actionPerformed(ActionEvent e)
	{
		String strCmd = e.getActionCommand();
		if (strCmd.equals("Cancel"))
		{
			dispose();
		}
		else if (strCmd.equals("importCertToSign"))
		{
			byte[] cert;
			try
			{
				cert = MixConfig.openFile(MixConfig.FILTER_CER);
				setCertToSign(cert);
			}
			catch (Exception ex)
			{
			}
		}
		else if (strCmd.equals("importCertSignWith"))
		{
			try
			{
				byte[] buff = MixConfig.openFile(MixConfig.FILTER_PFX);
				setCertSignWithPrivCert(buff);
			}
			catch (Exception ex)
			{
			}
		}
		else if (strCmd.equals("Sign"))
		{
			sign();

		}
		else if (strCmd.equals("exportCertToSign"))
		{
			try
			{
				JFileChooser fd = MixConfig.showFileDialog(
					MixConfig.SAVE_DIALOG,
					MixConfig.FILTER_CER
					| MixConfig.FILTER_B64_CER);
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
								fout.write(getCertToSignAsByteArray());
								break;
							case MixConfig.FILTER_B64_CER:
								fout.write("-----BEGIN CERTIFICATE-----\n".getBytes());
								fout.write(Base64.encode(getCertToSignAsByteArray(),true).getBytes());
								fout.write("\n-----END CERTIFICATE-----\n".getBytes());
								break;
						}
						fout.close();
					}
					catch (Exception ex)
					{
					}
				}
			}
			catch (Exception ie)
			{
			}
		}

	}

}
