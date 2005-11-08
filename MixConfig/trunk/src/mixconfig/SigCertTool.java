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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import anon.crypto.JAPCertificate;
import anon.crypto.PKCS12;

public class SigCertTool extends JDialog implements ActionListener
{
	private CertPanel m_publicCertPanel;
	private CertPanel m_privateCertPanel;
	private JButton m_btSign;

	public SigCertTool(Frame parent)
	{
		super(parent, "Sign a public certificate", true);
		this.setResizable(false);

		// upper public certificate
		GridBagLayout gbl = new GridBagLayout();
		this.getContentPane().setLayout(gbl);
		GridBagConstraints publicConstraint = new GridBagConstraints();
		publicConstraint.anchor = GridBagConstraints.NORTH;
		publicConstraint.gridx = 0;
		publicConstraint.gridy = 0;
		publicConstraint.insets = new Insets(5, 5, 5, 5);
		m_publicCertPanel = new CertPanel("Public Certificate",
										  "Hint: Public cert ... ", (JAPCertificate)null);
		m_publicCertPanel.setName("Sign Cert / Public Cert");
		this.getContentPane().add(m_publicCertPanel, publicConstraint);

		// middle private certificate
		GridBagConstraints privateConstraint = new GridBagConstraints();
		privateConstraint.anchor = GridBagConstraints.CENTER;
		privateConstraint.gridx = 0;
		privateConstraint.gridy = 1;
		privateConstraint.insets = new Insets(5, 5, 5, 5);
		m_privateCertPanel = new CertPanel("Private Certificate",
										   "Hint: Private Key to sign public Certificate",
										   (PKCS12)null);
		m_privateCertPanel.setName("Private Cert / Private ...");
		this.getContentPane().add(m_privateCertPanel, privateConstraint);

		// bottom button
		m_btSign = new JButton("Sign Certificate");
		m_btSign.addActionListener(this);
		GridBagConstraints btnConstraint = new GridBagConstraints();
		btnConstraint.anchor = GridBagConstraints.SOUTH;
		btnConstraint.gridx = 0;
		btnConstraint.gridy = 3;
		btnConstraint.insets = new Insets(5, 5, 5, 5);
		this.getContentPane().add(m_btSign, btnConstraint);

		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent ae)
	{
		JAPCertificate certificate = (JAPCertificate) m_publicCertPanel.getCert();

		JAPCertificate signedCertificate =
			certificate.sign(((PKCS12) m_privateCertPanel.getCert()));
		m_publicCertPanel.setCert(signedCertificate);
		MixConfig.info("Signature done!", "The public certificate has been signed successfully!");
	}
}
