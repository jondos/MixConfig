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

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Vector;
import anon.crypto.X509DistinguishedName;
import anon.crypto.X509Extensions;
import anon.crypto.PKCS12;
import gui.JAPDialog;
import gui.JAPMessages;


public class RSACertCreatorPanel extends JAPDialog
{
	private static final String PROP_PASSWD_INFO_MSG = "EncryptedLog_password_info_message";
	private static final String PROP_CERT_HEADLINE = "EncryptedLog_certificate_headline";

	private CertPanel m_privateCertPanel;

	public RSACertCreatorPanel(Frame parent)
	{
		super(parent, "Sign a public certificate");
		setResizable(false);

		GridBagLayout gbl = new GridBagLayout();
		getContentPane().setLayout(gbl);
		GridBagConstraints privateConstraint = new GridBagConstraints();
		privateConstraint.anchor = GridBagConstraints.CENTER;
		privateConstraint.gridx = 0;
		privateConstraint.gridy = 1;
		privateConstraint.insets = new Insets(5, 5, 5, 5);
		m_privateCertPanel = new CertPanel("Private certificate",
										   "Hint: Private Certificate to sign a Public Certificate",
										   (PKCS12)null, CertPanel.CERT_ALGORITHM_RSA);
		m_privateCertPanel.setName(JAPMessages.getString(PROP_CERT_HEADLINE));
		m_privateCertPanel.setCertCreationValidator(new LogCertCreationValidator());
		getContentPane().add(m_privateCertPanel, privateConstraint);

		pack();
		setVisible(true, false);
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
			return JAPMessages.getString(PROP_PASSWD_INFO_MSG);
		}

		public X509Extensions getExtensions()
		{
			return new X509Extensions(new Vector());
		}
		public X509DistinguishedName getSigName()
		{
			return new X509DistinguishedName("CN=MixlogEncryptionCertificate");
		}

	}
}
