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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JButton;
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
import gui.JAPMessages;
import logging.LogType;
import gui.dialog.JAPDialog;
import gui.JAPHelpContext;
import gui.JAPHelp;

public class EncryptedLogTool extends JAPDialog
	implements ActionListener, ChangeListener, JAPHelpContext.IHelpContext
{
	private static final String MSG_PASSWD_INFO_MSG = EncryptedLogTool.class.getName() +
		"_passwordInfoMessage";
	private static final String MSG_CERT_HEADLINE = EncryptedLogTool.class.getName() +
		"_certificateHeadline";
	private static final String MSG_CANNOT_DECRYPT = EncryptedLogTool.class.getName() + "_cannotDecrypt";
	private static final String MSG_NO_CERT = EncryptedLogTool.class.getName() + "_noCert";
	private static final String MSG_NO_LOG = EncryptedLogTool.class.getName() + "_noLog";
	private static final String MSG_REALLY_CLOSE = EncryptedLogTool.class.getName() + "_reallyClose";

	private JTextArea m_textLogFile;
	private byte[] m_arLog;
	private CertPanel m_privateCertPanel;
	private JButton m_btnChooseFile, m_btnDecrypt;

	public EncryptedLogTool(Frame parent)
	{
		super(parent, "Log Reader for encrypted Mix logs", true);

		GridBagConstraints constraintsContentPane;
		GridBagConstraints constraintsPanelLog;
		JPanel panelLog;

		getContentPane().setLayout(new GridBagLayout());

		constraintsContentPane = new GridBagConstraints();
		constraintsContentPane.anchor = GridBagConstraints.NORTHWEST;
		constraintsContentPane.insets = new Insets(10, 10, 10, 10);
		constraintsContentPane.fill = GridBagConstraints.BOTH;
		constraintsContentPane.gridx = 0;
		constraintsContentPane.gridy = 0;
		constraintsContentPane.weightx = 1;
		constraintsContentPane.weighty = 1;
		panelLog = new JPanel(new GridBagLayout());
		panelLog.setBorder(new TitledBorder("Log File to decrypt"));
		getContentPane().add(panelLog, constraintsContentPane);

		constraintsPanelLog = new GridBagConstraints();
		constraintsPanelLog.anchor = GridBagConstraints.WEST;
		constraintsPanelLog.insets = new Insets(5, 5, 5, 5);
		constraintsPanelLog.gridx = 0;
		constraintsPanelLog.gridy = 0;
		m_btnChooseFile = new JButton("Choose...");
		constraintsPanelLog.fill = GridBagConstraints.NONE;
		m_btnChooseFile.addActionListener(this);
		panelLog.add(m_btnChooseFile, constraintsPanelLog);

		constraintsPanelLog.gridx++;
		m_btnDecrypt = new JButton("Decrypt");
		m_btnDecrypt.addActionListener(this);
		panelLog.add(m_btnDecrypt, constraintsPanelLog);

		constraintsPanelLog.gridx++;
		panelLog.add(JAPHelp.createHelpButton(this), constraintsPanelLog);


		constraintsPanelLog.gridx = 0;
		constraintsPanelLog.gridy = 1;
		constraintsPanelLog.gridwidth = 3;
		constraintsPanelLog.gridheight = 3;
		constraintsPanelLog.fill = GridBagConstraints.BOTH;
		m_textLogFile = new JTextArea();

		m_textLogFile.setEditable(false);
		JScrollPane sp = new JScrollPane(m_textLogFile);
		sp.setPreferredSize(new Dimension(500, 250));
		constraintsPanelLog.weightx = 10;
		constraintsPanelLog.weighty = 10;
		panelLog.add(sp, constraintsPanelLog);


		constraintsContentPane.gridx = 0;
		constraintsContentPane.gridy = 1;
		constraintsContentPane.weighty = 0;
		constraintsContentPane.fill = GridBagConstraints.NONE;
		m_privateCertPanel =
			new CertPanel(JAPMessages.getString(MSG_CERT_HEADLINE), null,
						  (PKCS12)null, CertPanel.CERT_ALGORITHM_RSA);
		m_privateCertPanel.setCertCreationValidator(new LogCertCreationValidator());
		m_privateCertPanel.addChangeListener(this);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		final EncryptedLogTool thisTool = this;
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent a_event)
			{
				boolean bClose = true;
				if (!m_privateCertPanel.isCertificateSaved())
				{
					bClose = (JAPDialog.showConfirmDialog(thisTool, JAPMessages.getString(MSG_REALLY_CLOSE),
						OPTION_TYPE_CANCEL_OK, MESSAGE_TYPE_QUESTION) == RETURN_VALUE_OK);
				}
				if (bClose)
				{
					dispose();
				}
			}
		});

		getContentPane().add(m_privateCertPanel, constraintsContentPane);
		pack();
		setVisible(true, false);
	}

	public void stateChanged(ChangeEvent a_event)
	{
		if (a_event.getSource() == m_privateCertPanel)
		{
			pack();
		}
	}

	public String getHelpContext()
	{
		return JAPHelpContext.INDEX;
	}

	public void actionPerformed(ActionEvent a_event)
	{
		if (a_event.getSource() == m_btnDecrypt)
		{
			if (m_arLog == null || m_arLog.length == 0)
			{
				JAPDialog.showErrorDialog(this, JAPMessages.getString(MSG_NO_LOG), LogType.GUI);
			}
			else if (m_privateCertPanel.getCert() == null)
			{
				JAPDialog.showErrorDialog(this, JAPMessages.getString(MSG_NO_CERT), LogType.GUI);
			}
			else
			{
				try
				{
					doDecrypt();
				}
				catch (Throwable a_e)
				{
					JAPDialog.showErrorDialog(this, JAPMessages.getString(MSG_CANNOT_DECRYPT),
											  LogType.MISC, a_e);
				}
			}
		}
		else if (a_event.getSource() == m_btnChooseFile)
		{
			try
			{
				byte[] buff = MixConfig.openFile(MixConfig.FILTER_ALL);
				m_textLogFile.setText(new String(buff));
				m_textLogFile.revalidate();
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
