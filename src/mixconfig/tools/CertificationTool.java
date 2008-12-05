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
package mixconfig.tools;

import gui.JAPHelpContext;
import gui.JAPMessages;
import gui.dialog.JAPDialog;
import gui.dialog.ValidityContentPane;
import gui.help.JAPHelp;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mixconfig.panels.CertPanel;
import anon.crypto.JAPCertificate;
import anon.crypto.PKCS12;

public class CertificationTool extends JAPDialog
	implements ActionListener, ChangeListener, JAPHelpContext.IHelpContext
{
	private static final String MSG_CONFIRMATION_TITLE = CertificationTool.class.getName() + "_confirmationTitle";
	private static final String MSG_CONFIRMATION = CertificationTool.class.getName() + "_confirmationMessage";

	private CertPanel m_publicCertPanel;
	private CertPanel m_privateCertPanel;
	private JButton m_btnSign;

	public CertificationTool(Frame parent)
	{
		super(parent, "Sign a public certificate", true);

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		GridBagConstraints buttonConstraints = new GridBagConstraints();
		JPanel panelButtons;

		// upper public certificate
		getContentPane().setLayout(gbl);
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(5, 5, 5, 5);
		m_publicCertPanel = new CertPanel("Public certificate",
										  "Hint: Public Certificate to be signed ", (JAPCertificate)null,
										  CertPanel.CERT_ALGORITHM_BOTH, JAPCertificate.CERTIFICATE_TYPE_UNKNOWN);
		m_publicCertPanel.setName("Public Certificate");
		m_publicCertPanel.addChangeListener(this);
		getContentPane().add(m_publicCertPanel, constraints);

		// middle private certificate
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.gridy++;
		m_privateCertPanel = new CertPanel("Private certificate",
										   "Hint: Private Certificate to sign a Public Certificate",
										   (PKCS12)null, CertPanel.CERT_ALGORITHM_BOTH, 
										   JAPCertificate.CERTIFICATE_TYPE_UNKNOWN);
		m_privateCertPanel.setName("Private Certificate");
		m_privateCertPanel.addChangeListener(this);
		getContentPane().add(m_privateCertPanel, constraints);

		// bottom buttons
		constraints.gridwidth = 1;
		constraints.gridy++;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panelButtons = new JPanel(new GridBagLayout());
		constraints.weightx = 1;
		constraints.weighty = 1;

		buttonConstraints.anchor = GridBagConstraints.WEST;
		buttonConstraints.gridx = 0;
		buttonConstraints.gridy = 0;
		buttonConstraints.insets = constraints.insets;
		m_btnSign = new JButton("Sign Certificate");
		m_btnSign.addActionListener(this);
		panelButtons.add(m_btnSign, buttonConstraints);
		m_btnSign.setEnabled(false);
		buttonConstraints.gridx++;
		panelButtons.add(JAPHelp.createHelpButton(this), buttonConstraints);

		getContentPane().add(panelButtons, constraints);


		pack();
		setResizable(false);
		setVisible(true, false);
	}

	public String getHelpContext()
	{
		//return CertificationTool.class.getName();
		return JAPHelpContext.INDEX;
	}


	public void actionPerformed(ActionEvent ae)
	{
		if (m_publicCertPanel.getCert() != null && m_privateCertPanel.getCert() != null)
		{
			JAPDialog dialog = new JAPDialog(this, "Set a new certificate validity.", true);
			ValidityContentPane contentPane = new ValidityContentPane(dialog);
			contentPane.updateDialog();
			dialog.pack();
			dialog.setResizable(false);
			dialog.setVisible(true);


			if (contentPane.getValidity() != null)
			{
				JAPCertificate signedCertificate =
					( (JAPCertificate) m_publicCertPanel.getCert()).sign(
						( (PKCS12) m_privateCertPanel.getCert()),
						contentPane.getValidity(),
						( (JAPCertificate) m_publicCertPanel.getCert()).getExtensions(), new BigInteger("0"));
				m_publicCertPanel.setCert(signedCertificate);
				JAPDialog.showMessageDialog(this, JAPMessages.getString(MSG_CONFIRMATION),
											JAPMessages.getString(MSG_CONFIRMATION_TITLE));
			}
		}
	}

	public void stateChanged(ChangeEvent ce)
	{
		m_btnSign.setEnabled((m_publicCertPanel.getCert() != null) &&
							 (m_privateCertPanel.getCert() != null));
	}

	public Container getHelpExtractionDisplayContext() 
	{
		return null;
	}
}
