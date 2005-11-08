/*
 Copyright (c) 2000 - 2005, The JAP-Team
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import anon.crypto.IMyPublicKey;
import anon.crypto.JAPCertificate;
import anon.crypto.X509DistinguishedName;
import anon.crypto.X509Extensions;
import gui.GUIUtils;
import gui.JAPDialog;
import anon.crypto.X509UnknownExtension;
import anon.crypto.AbstractX509Extension;
import org.bouncycastle.asn1.DERString;
import java.util.Vector;
import java.awt.Component;

/** This dialog shows the details of a certificate from the CertPanel.
 * @author Tobias Bayer
 */
public class CertDetailsDialog extends JAPDialog
{
	public CertDetailsDialog(Component a_parent, JAPCertificate a_cert)
	{
		super(a_parent, "Certificate Details");

		JScrollPane scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
												 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(getRootPanel());

		/*
		getRootPanel().setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = gbc.BOTH;
		getRootPanel().add(scrollPane, gbc);
		getRootPanel().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = c.NONE;
		c.anchor = c.NORTHWEST;
		c.insets = new Insets(5, 5, 5, 5);
		c.gridx = 0;
		c.gridy = 0;

		//Subject
		getRootPanel().add(new JLabel("Subject"), c);

		c.insets = new Insets(5, 15, 5, 5);

		X509DistinguishedName dname = new X509DistinguishedName(a_cert.getSubject().toString());

		c.gridy++;
		getRootPanel().add(new JLabel("Country:"), c);
		c.gridx++;
		getRootPanel().add(new JLabel(dname.getCountryCode()), c);
		c.gridx = 0;
		c.gridy++;
		getRootPanel().add(new JLabel("City:"), c);
		c.gridx++;
		getRootPanel().add(new JLabel(dname.getLocalityName()), c);
		c.gridx = 0;
		c.gridy++;
		getRootPanel().add(new JLabel("Organisation:"), c);
		c.gridx++;
		getRootPanel().add(new JLabel(dname.getOrganisation()), c);
		c.gridx = 0;
		c.gridy++;
		getRootPanel().add(new JLabel("Organisational Unit:"), c);
		c.gridx++;
		getRootPanel().add(new JLabel(dname.getOrganisationalUnit()), c);
		c.gridx = 0;
		c.gridy++;
		getRootPanel().add(new JLabel("Common Name:"), c);
		c.gridx++;
		getRootPanel().add(new JLabel(dname.getCommonName()), c);
		c.gridx = 0;

		//Issuer
		dname = new X509DistinguishedName(a_cert.getIssuer().toString());

		c.gridy++;
		c.insets = new Insets(5, 5, 5, 5);
		getRootPanel().add(new JLabel("Issuer"), c);

		c.insets = new Insets(5, 15, 5, 5);

		c.gridy++;
		getRootPanel().add(new JLabel("Organisational Unit:"), c);
		c.gridx++;
		getRootPanel().add(new JLabel(dname.getOrganisationalUnit()), c);
		c.gridx = 0;
		c.gridy++;
		getRootPanel().add(new JLabel("Organisation:"), c);
		c.gridx++;
		getRootPanel().add(new JLabel(dname.getOrganisation()), c);

		//Public  Key
		IMyPublicKey pkey = a_cert.getPublicKey();
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(5, 5, 5, 5);
		getRootPanel().add(new JLabel("Public Key"), c);
		c.insets = new Insets(5, 15, 5, 5);

		c.gridy++;
		getRootPanel().add(new JLabel("Algorithm:"), c);
		c.gridx++;
		getRootPanel().add(new JLabel(pkey.getAlgorithm()), c);

		//Extensions
		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets(5, 5, 5, 5);
		getRootPanel().add(new JLabel("Extensions"), c);
		c.insets = new Insets(5, 15, 5, 5);

		X509Extensions extensions = a_cert.getExtensions();

		for (int i = 0; i < extensions.getSize(); i++)
		{
			c.gridx = 0;
			c.gridy++;
			AbstractX509Extension extension = extensions.getExtension(i);
			getRootPanel().add(new JLabel(extension.getName()), c);
			Vector v = extension.getValues();
			c.gridx++;
			for (int j = 0; j < v.size(); j++)
			{
				if (v.elementAt(j) instanceof String)
				{
					String extValue = (String) v.elementAt(j);
					getRootPanel().add(new JLabel(extValue), c);
					c.gridy++;
				}
			}
		}

		c.gridx = 0;
		c.gridy++;
		getRootPanel().add(new JLabel("..."), c);
		c.gridx++;
		c.weightx = 1;
		c.weighty = 1;
		getRootPanel().add(new JLabel("..."), c);
		c.gridx = 0;

		//Ok Button
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		gbc.gridy++;
		gbc.anchor = gbc.NORTHEAST;
		gbc.weighty = 0;
		gbc.weightx = 0;
		gbc.fill = gbc.NONE;
		getRootPanel().add(okButton, gbc); */
	}
}
