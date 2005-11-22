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
package mixconfig;

import java.util.Vector;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import org.bouncycastle.crypto.engines.RSAEngine;
import anon.crypto.MyRSAPrivateKey;
import anon.crypto.PKCS12;
import anon.crypto.X509DistinguishedName;
import anon.crypto.X509Extensions;
import anon.util.Base64;
import gui.GUIUtils;
import gui.JAPMessages;
import anon.util.ClassUtil;
import logging.LogType;

public class EncryptedLogTool extends JDialog implements ActionListener, ChangeListener
{
	private static final String MSG_PASSWD_INFO_MSG = "EncryptedLog_password_info_message";
	private static final String MSG_CERT_HEADLINE = ClassUtil.getClassNameStatic() + "_certificate_headline";
	private static final String MSG_CANNOT_DECRYPT = "EncryptedLog_cannot_decrypt";
	private static final String MSG_NO_CERT = "EncryptedLog_no_cert";
	private static final String MSG_NO_LOG = "EncryptedLog_no_log";

	private JTextArea m_textLogFile;
	private byte[] m_arLog;
	private CertPanel m_privateCertPanel;
	private JButton m_btnChooseFile, m_btnDecrypt;

	public EncryptedLogTool(Frame parent)
	{
		super(parent, "Log Reader for encrypted Mix logs", true);

		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);
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

		m_btnChooseFile = new JButton("Choose...");
		d.gridwidth = 1;
		d.fill = GridBagConstraints.NONE;
		m_btnChooseFile.addActionListener(this);
		layoutFile.setConstraints(m_btnChooseFile, d);
		panel1.add(m_btnChooseFile);

		d.gridy = 1;
		d.anchor = GridBagConstraints.WEST;
		m_btnDecrypt = new JButton("Decrypt");
		m_btnDecrypt.addActionListener(this);
		layoutBttns.setConstraints(m_btnDecrypt, d);
		panel1.add(m_btnDecrypt);


		d.gridx = 0;
		d.gridy = 1;
		d.gridwidth = 10;
		d.fill = GridBagConstraints.HORIZONTAL;
		m_textLogFile = new JTextArea(20, 80);
		m_textLogFile.setEditable(false);
		JScrollPane sp = new JScrollPane(m_textLogFile);
		d.weightx = 1;
		layoutFile.setConstraints(sp, d);
		panel1.add(sp);


		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.NONE;
		m_privateCertPanel = new CertPanel(JAPMessages.getString(MSG_CERT_HEADLINE),
										   "Hint: Private Certificate to sign a Public Certificate",
										   (PKCS12)null, CertPanel.CERT_ALGORITHM_RSA);
		m_privateCertPanel.setCertCreationValidator(new LogCertCreationValidator());
		m_privateCertPanel.addChangeListener(this);
		layout.setConstraints(m_privateCertPanel, c);
		getContentPane().add(m_privateCertPanel);

		pack();
		GUIUtils.positionWindow(this, parent);
		setResizable(false);
		setVisible(true);
	}

	public void stateChanged(ChangeEvent a_event)
	{
		if (a_event.getSource() == m_privateCertPanel)
		{
			pack();
		}
	}

	public void actionPerformed(ActionEvent a_event)
	{
		if (a_event.getSource() == m_btnDecrypt)
		{
			if (m_arLog == null || m_arLog.length == 0)
			{
				MixConfig.handleError(null, JAPMessages.getString(MSG_NO_LOG), LogType.GUI);
			}
			else if (m_privateCertPanel.getCert() == null)
			{
				MixConfig.handleError(null, JAPMessages.getString(MSG_NO_CERT), LogType.GUI);
			}
			else
			{
				try
				{
					doDecrypt();
				}
				catch (Throwable a_e)
				{
					MixConfig.handleError(a_e, JAPMessages.getString(MSG_CANNOT_DECRYPT), LogType.MISC);
				}
			}
		}
		else if (a_event.getSource() == m_btnChooseFile)
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
		rsa.init(false, ((MyRSAPrivateKey)((PKCS12)m_privateCertPanel.getCert()).getPrivateKey()).getParams());
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


	private class LogCertCreationValidator implements ICertCreationValidator
	{
		/**
		 * The certificate data is always valid.
		 * @return an empty Vector
		 */
		public Vector getInvalidityMessages()
		{
			return new Vector();
		}

		/**
		 * The certificate data is always valid.
		 * @return true
		 */
		public boolean isValid()
		{
			return true;
		}

		public String getPasswordInfoMessage()
		{
			return JAPMessages.getString(MSG_PASSWD_INFO_MSG);
		}

		public X509Extensions getExtensions()
		{
			return new X509Extensions(new Vector());
		}
		public X509DistinguishedName getSigName()
		{
			return new X509DistinguishedName("CN=Mixlog Encryption Certificate");
		}

	}
}
