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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Vector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import anon.crypto.PKCS12;
import java.awt.Container;
import mixconfig.wizard.ConfigWizardPanel;

/** This class provides a panel showing controls for configuring the certificates
 * for previous Mix, own Mix and next Mix.<br>
 * The <CODE>CertificatesPanel</CODE> listens to <CODE>ChangeEvent</CODE>s from the
 * configuration object and enables and disables the appropriate certificate
 * panels. Thus, the next Mix certificate is only enabled when the currently
 * configured Mix is not at the end of the cascade (Mix type is Last Mix), and the first certificate
 * is only enabled if the Mix type is not First Mix.
 */
public class CertificatesPanel extends MixConfigPanel implements ChangeListener,
	CertPanel.CertCreationValidator
{
	/** A <CODE>CertPanel</CODE> for the next Mix's certificate. */
	private CertPanel m_nextCert;

	/** A <CODE>CertPanel</CODE> for this Mix's certificate. */
	private CertPanel m_ownCert;

	/** A <CODE>CertPanel</CODE> for the previous Mix's certificate. */
	private CertPanel m_prevCert;

	/** Indicates whether this panel is part of the ConfigWizard (<code>true</code>) or
	 *  the ConfigFrame (<code>false</code>). */
	boolean m_wizard = false;

	/** Constructs a new instance of <CODE>CertificatesPanel</CODE>.
	 * @throws IOException If an error occurs while loading data from the configuration object into one of
	 * the <CODE>CertPanel</CODE>s.
	 */
	public CertificatesPanel() throws IOException
	{
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = GridBagConstraints.HORIZONTAL;

		m_ownCert = new CertPanel("Own Mix Certificate",
								  "Hint: You have to sent your public test " +
								  "certificate to the operators of your " +
								  "adjacent mixes", (PKCS12)null);
		m_ownCert.setName("Certificates/OwnCertificate");
		m_ownCert.setCertCreationValidator(this);
		m_ownCert.addChangeListener(this);
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 1;
		layout.setConstraints(m_ownCert, c);
		add(m_ownCert);

		m_prevCert = new CertPanel("Previous Mix Certificate",
								   "Hint: You will get the public test " +
								   "certificate from the operator of the " +
								   "previous mix", (byte[])null);
		m_prevCert.setName("Certificates/PrevMixCertificate");
		m_prevCert.setCertCreationValidator(this);
		m_prevCert.addChangeListener(this);
		c.gridx = 0;
		c.gridy = 0;
		layout.setConstraints(m_prevCert, c);
		add(m_prevCert);

		m_nextCert = new CertPanel("Next Mix Certificate",
								   "Hint: You will get the public test " +
								   "certificate from the operator of the " +
								   "next mix", (byte[])null);
		m_nextCert.setName("Certificates/NextMixCertificate");
		m_nextCert.setCertCreationValidator(this);
		m_nextCert.addChangeListener(this);
		c.gridy = 2;
		layout.setConstraints(m_nextCert, c);
		add(m_nextCert);
	}

	public void addNotify()
	{
		super.addNotify();
		Container parent = getParent();
		m_wizard = (parent instanceof ConfigWizardPanel);
		m_prevCert.setVisible(!m_wizard);
		m_nextCert.setVisible(!m_wizard);
	}

	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		// first enable all components to make MixConfigPanel load their data
		enableComponents();

		super.setConfiguration(a_conf);

		// make sure this panel is contained only once in the config's listeners list
		a_conf.removeChangeListener(this);
		a_conf.addChangeListener(this);

		enableComponents();
	}

	public Vector check()
	{
		Vector errors = new Vector();

		if (this.m_ownCert.getCert() == null)
		{
			errors.addElement(
				"Own Mix Certificate is missing in Certificates Panel.");
		}

		// in automatic configuration, certificates are not entered in the configuration;
		// they are to be received from the InfoService instead
		if (!m_wizard)
		{
			if (!m_prevCert.isEnabled() && m_prevCert.getCert() != null)
			{
				errors.addElement(
					"Previous Mix Certificate is present, but there is no previous mix.");
			}
			else if (m_prevCert.isEnabled() && m_prevCert.getCert() == null)
			{
				errors.addElement(
					"Previous Mix Certificate is missing in Certificates Panel.");
			}

			if (!m_nextCert.isEnabled() && m_nextCert.getCert() != null)
			{
				errors.addElement(
					"Next Mix Certificate is present, but there is no next mix.");
			}
			else if (m_nextCert.isEnabled() && m_nextCert.getCert() == null)
			{
				errors.addElement(
					"Next Mix Certificate is missing in Certificates Panel.");
			}
		}

		return errors;
	}

	public void stateChanged(ChangeEvent e)
	{
		try
		{
			if (e instanceof ConfigurationEvent)
			{
				ConfigurationEvent c = (ConfigurationEvent) e;
				if (c.getChangedAttribute().equals("General/MixType"))
				{
					enableComponents();
				}
			}
			else if (e.getSource() instanceof CertPanel)
			{
				save( (CertPanel) e.getSource());
			}
		}
		catch (Exception ex)
		{
			MixConfig.handleException(ex);
		}

	}

	public boolean isValid()
	{
		return getConfiguration().isMixIDValid();
	}

	public String getSigName()
	{
		String mixID = getConfiguration().getAttribute("General/MixID");
		mixID = URLEncoder.encode(mixID);
		/* Needs JDK >= 1.4
		   try
		   {
		 mixID = URLEncoder.encode(mixID, "UTF-8");
		   }
		   catch (UnsupportedEncodingException uee)
		   {
		 // if UTF-8 encoding is not supported, return the unencoded string
		 // (this should not happen actually)
		   }
		 */
		return "<Mix id=\"" + mixID + "\"/>";
	}

	public String getPasswordInfoMessage()
	{
		return "This password has to be entered every time the Mix " +
			"server starts.\nSo if you want to start it automatically " +
			"you shouldn't enter a password.";
	}

	public String getInvalidityMessage()
	{
		return "Please enter a valid Mix ID in general panel,\n" +
			"starting with a 'm' and containing only letters,\n" +
			"digits, dots, underscores and minuses.";
	}

	protected void enableComponents()
	{
		try
		{
			String sMixType = getConfiguration().getAttribute("General/MixType");
			int mixType = Integer.valueOf(sMixType).intValue();
			m_prevCert.setEnabled(mixType != MixConfiguration.MIXTYPE_FIRST);
			m_nextCert.setEnabled(mixType != MixConfiguration.MIXTYPE_LAST);
		}
		catch (NullPointerException npe)
		{
			// do nothing if config is not yet loaded
		}
	}
}
