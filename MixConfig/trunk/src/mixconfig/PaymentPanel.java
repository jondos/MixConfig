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
import java.util.Vector;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import anon.crypto.PKCS12;

/**
 * The PaymentPanel is one page in the MixConfig TabbedPane and allows the user to specify
 * the data which is needed for the mix to successfully use payment, namely the JPI Host/Port,
 * and the Postgresql Database Host/Port/DBName/Username.
 *
 * @todo Save JPI private certificate somehow; XML structure provides no element for this
 * @todo Importing certificates is not implemented
 * @author Bastian Voigt
 * @author ronin &lt;ronin2@web.de&gt;
 * @version 0.1
 */
public class PaymentPanel extends MixConfigPanel implements ChangeListener, CertPanel.CertCreationValidator
{
	private JPanel m_miscPanel;
	private JCheckBox m_chkPaymentEnabled;
	private JPanel m_jpiPanel;
	private JTextField m_textJPIName;
	private JTextField m_textJPIHost;
	private JTextField m_textJPIPort;
	private CertPanel m_jpiCertPanel;
	private JPanel m_databasePanel;
	private JTextField m_textDatabaseHost;
	private JTextField m_textDatabasePort;
	private JTextField m_textDatabaseDBName;
	private JTextField m_textDatabaseUsername;

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
		m_miscPanel.setToolTipText("Please select whether you want to enable payment.<br>" +
								   "This is only possible for the FirstMix at the moment");

		m_chkPaymentEnabled = new JCheckBox("Enable Payment");
		m_chkPaymentEnabled.setSelected(false);
		m_chkPaymentEnabled.addItemListener(this);
		m_miscPanel.add(m_chkPaymentEnabled);

		layout.setConstraints(m_miscPanel, c);
		this.add(m_miscPanel);

		// JPI Panel
		GridBagLayout jpiLayout = new GridBagLayout();
		m_jpiPanel = new JPanel(jpiLayout);
		m_jpiPanel.setBorder(new TitledBorder("JPI (Java Payment Instance)"));
		m_jpiPanel.setToolTipText(
			"Please enter the Hostname or IP Address and the port number of the JPI<br> " +
			"that your mix should use.");

		label = new JLabel("JPI Name:");
		d.gridy = 0;
		d.gridx = 0;
		d.weightx = 0;
		jpiLayout.setConstraints(label, d);
		m_jpiPanel.add(label);

		m_textJPIName = new JTextField();
		m_textJPIName.setName("Accounting/PaymentInstance/Name");
		m_textJPIName.addFocusListener(this);
		d.gridx = 1;
		d.weightx = 1;
		jpiLayout.setConstraints(m_textJPIName, d);
		m_jpiPanel.add(m_textJPIName);

		label = new JLabel("JPI Hostname:");
		d.gridy++;
		d.gridx = 0;
		d.weightx = 0;
		jpiLayout.setConstraints(label, d);
		m_jpiPanel.add(label);

		m_textJPIHost = new JTextField();
		m_textJPIHost.setName("Accounting/PaymentInstance/Host");
		m_textJPIHost.addFocusListener(this);
		d.gridx = 1;
		d.weightx = 1;
		jpiLayout.setConstraints(m_textJPIHost, d);
		m_jpiPanel.add(m_textJPIHost);

		label = new JLabel("JPI Portnumber:");
		d.gridy++;
		d.gridx = 0;
		d.weightx = 0;
		jpiLayout.setConstraints(label, d);
		m_jpiPanel.add(label);

		m_textJPIPort = new JTextField();
		m_textJPIPort.setName("Accounting/PaymentInstance/Port");
		m_textJPIPort.addFocusListener(this);
		d.gridx = 1;
		d.weightx = 1;
		jpiLayout.setConstraints(m_textJPIPort, d);
		m_jpiPanel.add(m_textJPIPort);

		c.gridy++;
		layout.setConstraints(m_jpiPanel, c);
		this.add(m_jpiPanel);

		// JPI Certificate Panel
		m_jpiCertPanel = new CertPanel("JPI Certificate",
									   "If you have the Public Certificate of a " +
									   "running JPI, you can import it here.<br> " +
									   "Or, if you want to setup your own JPI, " +
									   "you can generate a certificate here<br> " +
									   "and copy it to your JPI configuration.",
									   (PKCS12)null);
		m_jpiCertPanel.setName("Accounting/JPICertificate");
		m_jpiCertPanel.setCertCreationValidator(this);
		m_jpiCertPanel.addChangeListener(this);
		c.gridy++;
		layout.setConstraints(m_jpiCertPanel, c);
		this.add(m_jpiCertPanel);

		// DATABASE Panel
		GridBagLayout databaseLayout = new GridBagLayout();
		m_databasePanel = new JPanel(databaseLayout);
		m_databasePanel.setBorder(new TitledBorder("PostgreSQL Database for the accounting instance"));
		m_databasePanel.setToolTipText("The accounting instance inside the First Mix needs a PostgreSQL<br> " +
									   "database to store some internal accounting data. Before you start<br> " +
									   "the First Mix with payment enabled, setup a Postgresql DB and enter<br> " +
									   "its connection data here.");

		label = new JLabel("Database Hostname:");
		d.anchor = GridBagConstraints.NORTHWEST;
		d.fill = GridBagConstraints.HORIZONTAL;
		d.gridx = 0;
		d.gridy = 0;
		d.weightx = 0;
		d.gridwidth = 1;
		databaseLayout.setConstraints(label, d);
		m_databasePanel.add(label);

		m_textDatabaseHost = new JTextField();
		m_textDatabaseHost.setName("Accounting/Database/Host");
		m_textDatabaseHost.addFocusListener(this);
		d.gridx = 1;
		d.weightx = 1;
		databaseLayout.setConstraints(m_textDatabaseHost, d);
		m_databasePanel.add(m_textDatabaseHost);

		label = new JLabel("Database Portnumber:");
		d.gridy++;
		d.gridx = 0;
		d.weightx = 0;
		databaseLayout.setConstraints(label, d);
		m_databasePanel.add(label);

		m_textDatabasePort = new JTextField();
		m_textDatabasePort.setName("Accounting/Database/Port");
		m_textDatabasePort.addFocusListener(this);
		d.gridx = 1;
		d.weightx = 1;
		databaseLayout.setConstraints(m_textDatabasePort, d);
		m_databasePanel.add(m_textDatabasePort);

		label = new JLabel("Database DBName:");
		d.gridy++;
		d.gridx = 0;
		d.weightx = 0;
		databaseLayout.setConstraints(label, d);
		m_databasePanel.add(label);

		m_textDatabaseDBName = new JTextField();
		m_textDatabaseDBName.setName("Accounting/Database/DBName");
		m_textDatabaseDBName.addFocusListener(this);
		d.gridx = 1;
		d.weightx = 1;
		databaseLayout.setConstraints(m_textDatabaseDBName, d);
		m_databasePanel.add(m_textDatabaseDBName);

		label = new JLabel("Database Username:");
		d.gridy++;
		d.gridx = 0;
		d.weightx = 0;
		databaseLayout.setConstraints(label, d);
		m_databasePanel.add(label);

		m_textDatabaseUsername = new JTextField();
		m_textDatabaseUsername.setName("Accounting/Database/Username");
		m_textDatabaseUsername.addFocusListener(this);
		d.gridx = 1;
		d.weightx = 1;
		databaseLayout.setConstraints(m_textDatabaseUsername, d);
		m_databasePanel.add(m_textDatabaseUsername);

		c.gridy++;
		layout.setConstraints(m_databasePanel, c);
		this.add(m_databasePanel);

		setEnabled(true);
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
					int i = Integer.valueOf( (String) c.getNewValue()).intValue();
					m_chkPaymentEnabled.setEnabled(i == MixConfiguration.MIXTYPE_FIRST);
					setEnabled(i == MixConfiguration.MIXTYPE_FIRST);
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

	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		// first enable all components to make MixConfigPanel load their data
		m_chkPaymentEnabled.setEnabled(true);
		enableComponents();

		super.setConfiguration(a_conf);

		// make sure this panel is contained only once in the config's listeners list
		a_conf.removeChangeListener(this);
		a_conf.addChangeListener(this);

		// now check if payment should be really enabled
		setAutoSaveEnabled(false);
		String host = a_conf.getAttribute("Accounting/PaymentInstance/Host");
		String mixType = a_conf.getAttribute("General/MixType");
		this.m_chkPaymentEnabled.setEnabled(mixType != null &&
											Integer.valueOf(mixType).intValue() ==
											MixConfiguration.MIXTYPE_FIRST);
		setEnabled(mixType != null &&
				   Integer.valueOf(mixType).intValue() == MixConfiguration.MIXTYPE_FIRST);
		this.m_chkPaymentEnabled.setSelected(host != null && !host.equals(""));
		enableComponents();
		setAutoSaveEnabled(true);
	}

	public Vector check()
	{
		return null; /* nothing to check on this panel */
	}

	public boolean isValid()
	{
		String name = getConfiguration().getAttribute(m_textJPIName.getName());
		return name != null && ! name.equals("");
	}

	public String getSigName()
	{
		return getConfiguration().getAttribute(m_textJPIName.getName());
	}

	public String getPasswordInfoMessage()
	{
		return "This password has to be entered every time the JPI " +
			"server starts.\nIf you want the JPI to start without user " +
			"interaction, you should leave this\npassworld field blank.\n";
	}

	public String getInvalidityMessage()
	{
		return "Please enter JPI Name first!";
	}

	protected void enableComponents()
	{
		int i;
		Component[] co;
		boolean compEnabled = m_chkPaymentEnabled.isSelected() &&
			m_chkPaymentEnabled.isEnabled();

		co = m_jpiPanel.getComponents();
		for (i = 0; i < co.length; i++)
		{
			co[i].setEnabled(compEnabled);
		}

		m_jpiCertPanel.setEnabled(compEnabled);

		co = m_databasePanel.getComponents();
		for (i = 0; i < co.length; i++)
		{
			co[i].setEnabled(compEnabled);
		}
	}
}
