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

import java.io.IOException;
import java.util.Vector;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import anon.crypto.JAPCertificate;
import anon.crypto.X509DistinguishedName;
import anon.crypto.X509Extensions;
import gui.JAPHelp;
import logging.LogType;
import gui.*;

/**
 * The PaymentPanel is one page in the MixConfig TabbedPane and allows the user to specify
 * the data which is needed for the mix to successfully use payment, namely the JPI Host/Port,
 * and the Postgresql Database Host/Port/DBName/Username.
 *
 * @todo Save JPI private certificate somehow; XML structure provides no element for this
 * @todo Importing certificates is not implemented
 * @author Bastian Voigt
 * @author ronin &lt;ronin2@web.de&gt;
 * @author Tobias Bayer
 */
public class PaymentPanel extends MixConfigPanel implements ActionListener, ChangeListener,
	ICertCreationValidator
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
	private JPanel m_generalPanel;
	private JTextField m_textSoftLimit;
	private JTextField m_textHardLimit;
	private JTextField m_textSettleInterval;

	public PaymentPanel() throws IOException
	{
		super("Payment");
		JLabel label;
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 0;

		GridBagConstraints d = new GridBagConstraints();
		d.anchor = GridBagConstraints.NORTHWEST;
		d.insets = new Insets(5, 5, 5, 5);
		d.fill = GridBagConstraints.NONE;
		d.gridx = 0;
		d.gridy = 0;
		d.weightx = 0;
		d.weighty = 1;

		// MISC Panel
		m_miscPanel = new JPanel(new FlowLayout());
		//m_miscPanel.setBorder(new TitledBorder("Payment enabled/disabled"));
		m_miscPanel.setToolTipText("<html>Please select whether you want to enable payment.<br>" +
								   "This is only possible for the FirstMix at the moment</html>");

		m_chkPaymentEnabled = new JCheckBox("Enable Payment");
		m_chkPaymentEnabled.setSelected(false);
		m_chkPaymentEnabled.addItemListener(this);
		m_chkPaymentEnabled.addActionListener(this);
		m_miscPanel.add(m_chkPaymentEnabled);

		layout.setConstraints(m_miscPanel, c);
		this.add(m_miscPanel);

		// GENERAL settings panel
		GridBagLayout generalLayout = new GridBagLayout();
		m_generalPanel = new JPanel(generalLayout);
		m_generalPanel.setBorder(new TitledBorder("General settings"));
		m_generalPanel.setToolTipText(
			"General settings regarding the payment system"
			);
		label = new JLabel("SoftLimit (Bytes)");
		label.setToolTipText(
			"<html>This is the soft limit for the number of bytes a Jap can use before <br>" +
			"sending a cost confirmation</html>"
			);
		d.gridy = 0;
		d.gridx = 0;
		d.weightx = 0;
		generalLayout.setConstraints(label, d);
		m_generalPanel.add(label);
		m_textSoftLimit = new JTextField(10);
		m_textSoftLimit.setName("Accounting/SoftLimit");
		m_textSoftLimit.addFocusListener(this);
		d.gridy = 0;
		d.gridx = 1;
		d.weightx = 1;
		d.fill = d.HORIZONTAL;
		generalLayout.setConstraints(m_textSoftLimit, d);
		m_generalPanel.add(m_textSoftLimit);

		label = new JLabel("HardLimit (Bytes)");
		label.setToolTipText(
			"<html>This is the hard limit for the number of bytes a Jap can use without <br>" +
			"sending a cost confirmation, before he will be kicked.</html>"
			);
		d.gridy = 1;
		d.gridx = 0;
		d.weightx = 0;
		generalLayout.setConstraints(label, d);
		m_generalPanel.add(label);
		m_textHardLimit = new JTextField(10);
		m_textHardLimit.setName("Accounting/HardLimit");
		m_textHardLimit.addFocusListener(this);
		d.gridy = 1;
		d.gridx = 1;
		d.weightx = 1;
		generalLayout.setConstraints(m_textHardLimit, d);
		m_generalPanel.add(m_textHardLimit);

		label = new JLabel("SettleInterval (seconds)");
		label.setToolTipText(
			"<html>This is the interval in seconds that the SettleThread will sleep<br>" +
			"before each cycle of transmitting all the open CostConfirmations to the BI</html>"
			);
		d.gridy = 2;
		d.gridx = 0;
		d.weightx = 0;
		generalLayout.setConstraints(label, d);
		m_generalPanel.add(label);
		m_textSettleInterval = new JTextField(10);
		m_textSettleInterval.setName("Accounting/SettleInterval");
		m_textSettleInterval.addFocusListener(this);
		d.gridy = 2;
		d.gridx = 1;
		d.weightx = 1;
		generalLayout.setConstraints(m_textSettleInterval, d);
		m_generalPanel.add(m_textSettleInterval);

		c.gridy++;
		c.fill = c.HORIZONTAL;
		c.weightx = 0;
		c.weighty = 0;
		layout.setConstraints(m_generalPanel, c);
		this.add(m_generalPanel);
		c.fill = c.NONE;

		// JPI Panel
		GridBagLayout jpiLayout = new GridBagLayout();
		m_jpiPanel = new JPanel(jpiLayout);
		m_jpiPanel.setBorder(new TitledBorder("JPI (Java Payment Instance)"));
		m_jpiPanel.setToolTipText(
			"Please enter the Hostname or IP Address and the port number of the JPI<br> " +
			"that your mix should use.");

		label = new JLabel("JPI ID:");
		d.gridy = 0;
		d.gridx = 0;
		d.weightx = 0;
		jpiLayout.setConstraints(label, d);
		m_jpiPanel.add(label);

		m_textJPIName = new JTextField(14);
		m_textJPIName.setName("Accounting/PaymentInstance/id");
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

		m_textJPIHost = new JTextField(14);
		m_textJPIHost.setName("Accounting/PaymentInstance/Network/ListenerInterfaces/ListenerInterface/Host");
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

		m_textJPIPort = new JTextField(14);
		m_textJPIPort.setName("Accounting/PaymentInstance/Network/ListenerInterfaces/ListenerInterface/Port");
		m_textJPIPort.addFocusListener(this);
		d.gridx = 1;
		d.weightx = 1;
		jpiLayout.setConstraints(m_textJPIPort, d);
		m_jpiPanel.add(m_textJPIPort);

		//c.gridy++;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx++;
		layout.setConstraints(m_jpiPanel, c);
		this.add(m_jpiPanel);
		c.gridx--;

		// JPI Certificate Panel
		m_jpiCertPanel = new CertPanel("JPI Certificate",
									   "If you have the Public Certificate of a " +
									   "running JPI, you can import it here.",
									   (JAPCertificate)null);
		m_jpiCertPanel.setName("Accounting/PaymentInstance/Certificate");
		m_jpiCertPanel.setCertCreationValidator(this);
		m_jpiCertPanel.addChangeListener(this);
		c.gridy++;
		layout.setConstraints(m_jpiCertPanel, c);
		this.add(m_jpiCertPanel);

		// DATABASE Panel
		GridBagLayout databaseLayout = new GridBagLayout();
		m_databasePanel = new JPanel(databaseLayout);
		m_databasePanel.setBorder(new TitledBorder("PostgreSQL Database for accounting"));
		m_databasePanel.setToolTipText("The accounting instance inside the First Mix needs a PostgreSQL<br> " +
									   "database to store some internal accounting data. Before you start<br> " +
									   "the First Mix with payment enabled, setup a Postgresql DB and enter<br> " +
									   "its connection data here.");

		label = new JLabel("Database Hostname:");
		d.anchor = GridBagConstraints.NORTHWEST;
		d.fill = GridBagConstraints.NONE;
		d.gridx = 0;
		d.gridy = 0;
		d.weightx = 0;
		d.gridwidth = 1;
		databaseLayout.setConstraints(label, d);
		m_databasePanel.add(label);

		m_textDatabaseHost = new JTextField(10);
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

		m_textDatabasePort = new JTextField(10);
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

		m_textDatabaseDBName = new JTextField(10);
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

		m_textDatabaseUsername = new JTextField(10);
		m_textDatabaseUsername.setName("Accounting/Database/Username");
		m_textDatabaseUsername.addFocusListener(this);
		d.gridx = 1;
		d.weightx = 1;
		databaseLayout.setConstraints(m_textDatabaseUsername, d);
		m_databasePanel.add(m_textDatabaseUsername);

		c.gridx++;
		layout.setConstraints(m_databasePanel, c);
		this.add(m_databasePanel);

		//Keep the panels in place
		JLabel dummyLabel1 = new JLabel("");
		c.gridx++;
		c.gridy++;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = c.BOTH;
		this.add(dummyLabel1, c);

		//Make panels equal
		/*
		  m_jpiPanel.setPreferredSize(new Dimension((int)m_databasePanel.getPreferredSize().width,
		   (int)m_jpiPanel.getPreferredSize().height));
		  m_databasePanel.setPreferredSize(new Dimension((int)m_databasePanel.getPreferredSize().width,
		   (int)m_jpiCertPanel.getPreferredSize().height));
		 */
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
			MixConfig.handleError(ex, null, LogType.GUI);
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
		String mixType = a_conf.getValue("General/MixType");
		this.m_chkPaymentEnabled.setEnabled(mixType != null &&
											Integer.valueOf(mixType).intValue() ==
											MixConfiguration.MIXTYPE_FIRST);
		setEnabled(mixType != null &&
				   Integer.valueOf(mixType).intValue() == MixConfiguration.MIXTYPE_FIRST);
		this.m_chkPaymentEnabled.setSelected(a_conf.isPaymentPresent());
		enableComponents();
		setAutoSaveEnabled(true);
	}

	public void paint(Graphics g)
	{
		super.paint(g);
		JAPHelp.getInstance().getContextObj().setContext("index");
	}

	public Vector check()
	{
		return new Vector(); /* nothing to check on this panel */
	}

	public boolean isValid()
	{
		String name = getConfiguration().getValue(m_textJPIName.getName());
		return name != null && !name.equals("");
	}

	public X509DistinguishedName getSigName()
	{
		return new X509DistinguishedName("CN=" +
										 getConfiguration().getValue(m_textJPIName.getName()));
	}

	public X509Extensions getExtensions()
	{
		return null;
	}

	public String getPasswordInfoMessage()
	{
		return "This password has to be entered every time the JPI " +
			"server starts.\nIf you want the JPI to start without user " +
			"interaction, you should leave this\npassworld field blank.\n";
	}

	public Vector getInvalidityMessages()
	{
		return anon.util.Util.toVector("Please enter JPI Name first!");
	}

	protected void enableComponents()
	{
		int i;
		Component[] co;
		boolean compEnabled = m_chkPaymentEnabled.isSelected() &&
			m_chkPaymentEnabled.isEnabled();

		co = m_generalPanel.getComponents();
		for (i = 0; i < co.length; i++)
		{
			co[i].setEnabled(compEnabled);
		}
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

	public void actionPerformed(ActionEvent a_e)
	{
		if (a_e.getSource() == m_chkPaymentEnabled)
		{
			if (m_chkPaymentEnabled.isSelected())
			{
			}
			else
			{
				boolean delete = true;
				//Check if there is an entry in one or more textfields
				if (!m_textDatabaseDBName.getText().equalsIgnoreCase("") ||
					!m_textDatabaseHost.getText().equalsIgnoreCase("") ||
					!m_textDatabasePort.getText().equalsIgnoreCase("") ||
					!m_textDatabaseUsername.getText().equalsIgnoreCase("") ||
					!m_textHardLimit.getText().equalsIgnoreCase("") ||
					!m_textJPIHost.getText().equalsIgnoreCase("") ||
					!m_textJPIName.getText().equalsIgnoreCase("") ||
					!m_textJPIPort.getText().equalsIgnoreCase("") ||
					!m_textSettleInterval.getText().equalsIgnoreCase("") ||
					m_jpiCertPanel.getCert() != null ||
					!m_textSoftLimit.getText().equalsIgnoreCase(""))
				{
					//Ask user before deleting entries
					int i = JOptionPane.showConfirmDialog(MixConfig.getMainWindow(),
						"Really delete all entries and switch off payment function?",
						"Question",
						JOptionPane.YES_NO_OPTION);
					if (i == JOptionPane.NO_OPTION)
					{
						delete = false;
					}
				}
				if (delete)
				{
					//Delete entries
					m_textDatabaseDBName.setText("");
					m_textDatabaseHost.setText("");
					m_textDatabasePort.setText("");
					m_textDatabaseUsername.setText("");
					m_textHardLimit.setText("");
					m_textJPIHost.setText("");
					m_textJPIName.setText("");
					m_textJPIPort.setText("");
					m_textSettleInterval.setText("");
					m_textSoftLimit.setText("");
					m_jpiCertPanel.removeCert();

					MixConfig.getMixConfiguration().removeNode("Accounting");
				}
				else
				{
					//Recheck box
					m_chkPaymentEnabled.setSelected(true);
				}
			}

		}
	}

	protected void save(JTextField a_tf)
	{
		//Save the Payment Instance id as an attribute
		if (a_tf.getName().equals("Accounting/PaymentInstance/id"))
		{
			getConfiguration().setAttribute("Accounting/PaymentInstance", "id",
											a_tf.getText());
		}
		else
		{
			super.save(a_tf);
		}
	}
}
