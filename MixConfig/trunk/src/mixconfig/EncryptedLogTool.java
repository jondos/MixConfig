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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import org.bouncycastle.asn1.DERInputStream;
import java.io.*;
import org.bouncycastle.crypto.engines.*;
import anon.crypto.*;
import anon.util.*;
public class EncryptedLogTool extends JDialog implements ActionListener
{
	private JTextField m_textDecryptWithCertCN, m_textDecryptWithCertValidFrom, m_textDecryptWithCertValidTo;
	private JTextArea m_textLogFile;
	private byte[] m_arLog;
	private MyRSAPrivateKey m_keyDecryptWith;

	public EncryptedLogTool(Frame parent)
	{
		super(parent, "Log Reader for encrypted Mix logs", true);

		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);
		GridBagLayout layoutDecryptWith = new GridBagLayout();
		GridBagLayout layoutFile = new GridBagLayout();
		GridBagLayout layoutBttns = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = GridBagConstraints.HORIZONTAL;

		JPanel panel1 = new JPanel(layoutFile);
		GridBagConstraints d = new GridBagConstraints();
		d.anchor = GridBagConstraints.NORTHWEST;
		d.insets = new Insets(5, 5, 5, 5);
		panel1.setBorder(new TitledBorder("Log File to decrypt"));
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		layout.setConstraints(panel1, c);
		getContentPane().add(panel1);

		JButton bttnFile = new JButton("Choose...");
		d.gridx = 1;
		d.gridy = 0;
		d.gridwidth = 1;
		d.fill = GridBagConstraints.NONE;
		bttnFile.addActionListener(this);
		bttnFile.setActionCommand("selectLogFile");
		layoutFile.setConstraints(bttnFile, d);
		panel1.add(bttnFile);

		d.gridx = 0;
		d.gridy = 1;
		d.fill = GridBagConstraints.HORIZONTAL;
		JLabel name1 = new JLabel("Log File");
		layoutFile.setConstraints(name1, d);
		panel1.add(name1);
		m_textLogFile = new JTextArea(20, 80);
		m_textLogFile.setEditable(false);
		JScrollPane sp = new JScrollPane(m_textLogFile);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		layoutFile.setConstraints(sp, d);
		panel1.add(sp);

		c.gridx = 0;
		c.gridy = 1;
		JPanel panel2 = new JPanel(layoutDecryptWith);
		GridBagConstraints e = new GridBagConstraints();
		e.anchor = GridBagConstraints.NORTHWEST;
		e.insets = new Insets(5, 5, 5, 5);
		e.fill = GridBagConstraints.HORIZONTAL;
		panel2.setBorder(new TitledBorder("Decrypt with"));
		layout.setConstraints(panel2, c);
		getContentPane().add(panel2);

		JButton import1 = new JButton("Import...");
		e.gridx = 1;
		e.gridy = 0;
		e.fill = GridBagConstraints.NONE;
		import1.addActionListener(this);
		import1.setActionCommand("importCertSignWith");
		layoutDecryptWith.setConstraints(import1, e);
		panel2.add(import1);
		e.fill = GridBagConstraints.HORIZONTAL;

		JLabel name2 = new JLabel("Name");
		e.gridx = 0;
		e.gridy = 1;
		layoutDecryptWith.setConstraints(name2, e);
		panel2.add(name2);
		m_textDecryptWithCertCN = new JTextField(26);
		m_textDecryptWithCertCN.setEditable(false);
		e.gridx = 1;
		e.gridwidth = 3;
		e.weightx = 1;
		layoutDecryptWith.setConstraints(m_textDecryptWithCertCN, e);
		panel2.add(m_textDecryptWithCertCN);

		JLabel from2 = new JLabel("Valid From");
		e.gridx = 0;
		e.gridy = 2;
		e.gridwidth = 1;
		e.weightx = 0;
		layoutDecryptWith.setConstraints(from2, e);
		panel2.add(from2);
		m_textDecryptWithCertValidFrom = new JTextField(26);
		m_textDecryptWithCertValidFrom.setEditable(false);
		e.gridx = 1;
		e.gridwidth = 3;
		e.weightx = 1;
		layoutDecryptWith.setConstraints(m_textDecryptWithCertValidFrom, e);
		panel2.add(m_textDecryptWithCertValidFrom);

		JLabel to2 = new JLabel("Valid To");
		e.gridx = 0;
		e.gridy = 3;
		e.gridwidth = 1;
		e.weightx = 0;
		layoutDecryptWith.setConstraints(to2, e);
		panel2.add(to2);
		m_textDecryptWithCertValidTo = new JTextField(26);
		m_textDecryptWithCertValidTo.setEditable(false);
		e.gridx = 1;
		e.gridwidth = 3;
		e.weightx = 1;
		layoutDecryptWith.setConstraints(m_textDecryptWithCertValidTo, e);
		panel2.add(m_textDecryptWithCertValidTo);

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

		JButton bttnSign = new JButton("Decrypt");
		bttnSign.setActionCommand("Decrypt");
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

	private void setCertDecryptWithPrivCert(byte[] cert)
	{
		if (cert == null)
		{
			setCertDecryptWithPrivCert(null, null);
		}
		else
		{
			char[] passwd = new char[]
				{
			};
			while (passwd != null && !setCertDecryptWithPrivCert(cert, passwd))
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

	private boolean setCertDecryptWithPrivCert(byte[] cert, char[] passwd)
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

				PKCS12 pkcs12 = PKCS12.getInstance(new ByteArrayInputStream(cert), passwd);

				m_textDecryptWithCertValidFrom.setText(
					pkcs12.getX509Certificate().getStartDate().getDate().toString());
				m_textDecryptWithCertValidTo.setText(
								pkcs12.getX509Certificate().getEndDate().getDate().toString());
				m_textDecryptWithCertCN.setText(
								pkcs12.getX509Certificate().getSubject().toString());
				m_keyDecryptWith = (MyRSAPrivateKey) pkcs12.getPrivateKey();
				return true;

			}
			else
			{
				m_textDecryptWithCertValidFrom.setText(null);
				m_textDecryptWithCertValidTo.setText(null);
				m_textDecryptWithCertCN.setText(null);
				m_keyDecryptWith = null;
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e)
	{
		String strCmd = e.getActionCommand();
		if (strCmd.equals("Cancel"))
		{
			dispose();
		}
		else if (strCmd.equals("Decrypt"))
		{
			doDecrypt();
		}
		else if (strCmd.equals("importCertSignWith"))
		{
			try
			{
				byte[] buff = MixConfig.openFile(MixConfig.FILTER_PFX);
				setCertDecryptWithPrivCert(buff);
			}
			catch (Exception ex)
			{
			}
		}
		else if (strCmd.equals("selectLogFile"))
		{
			try
			{
				byte[] buff = MixConfig.openFile(MixConfig.FILTER_ALL);
				m_textLogFile.setText(new String(buff));
				m_arLog = buff;
			}
			catch (Exception ex)
			{
			}
		}

	}

	private void doDecrypt()
	{
		//search for begin of sym key...
		int i = 0;
		int len = m_arLog.length;
		while (true)
		{
			if (i >= len)
			{
				return;
			}

			int b = m_arLog[i++];
			if (b == '\n')
			{
				if (i >= len + 31)
				{
					return;
				}
				if (new String(m_arLog, i, 31).equals("----Start of EncryptionKey----\n"))
				{
					i += 31;
					break;
				}
			}
		}
		//found beginn --> next bytes are base64 encoded encrypted key....
		int startOfKey = i;
		i++;
		while (true)
		{
			if (i >= len)
			{
				return;
			}

			int b = m_arLog[i++];
			if (b == '\n')
			{
				if (i >= len + 31)
				{
					return;
				}
				if (new String(m_arLog, i, 31).equals("-----End of EncryptionKey-----\n"))
				{
					break;
				}
			}
		}
		//now the have the key....
		byte[] encKey = Base64.decode(m_arLog, startOfKey, i - startOfKey);
		i += 31;
		int startOfMessage = i;
		RSAEngine rsa = new RSAEngine();
		rsa.init(false, m_keyDecryptWith.getParams());
		byte[] arKey = rsa.processBlock(encKey, 0, encKey.length);
		SymCipher c = new SymCipher();
		c.setKey(arKey, 50);
		c.setIV(arKey, 66);
		//search for message to decryt...
		i++;
		while (true)
		{
			if (i >= len)
			{
				break;
			}

			int b = m_arLog[i++];
			if (b == '\n')
			{
				if (i >= len + 31)
				{
					break;
				}
				if (new String(m_arLog, i, 31).equals("----Start of Encryption Key----\n"))
				{
					break;
				}
			}
		}
		byte[] dec = new byte[i - startOfMessage];
		c.crypt(m_arLog, startOfMessage, dec, 0, i - startOfMessage);
		m_textLogFile.setText(new String(dec));
	}

}
