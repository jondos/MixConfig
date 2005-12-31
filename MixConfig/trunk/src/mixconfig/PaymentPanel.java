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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import anon.crypto.JAPCertificate;
import anon.infoservice.ListenerInterface;
import gui.JAPDialog;
import gui.JAPHelpContext;
import gui.JAPJIntField;
import gui.TitledGridBagPanel;
import logging.LogType;

/**
 * The PaymentPanel is one page in the MixConfig TabbedPane and allows the user to specify
 * the data which is needed for the mix to successfully use payment, namely the JPI Host/Port,
 * and the Postgresql Database Host/Port/DBName/Username.
 *
 * @author Bastian Voigt
 * @author ronin &lt;ronin2@web.de&gt;
 * @author Tobias Bayer
 */
public class PaymentPanel extends MixConfigPanel implements ActionListener, ChangeListener
{
	private JCheckBox m_chkPaymentEnabled;
	private TitledGridBagPanel m_jpiPanel;
	private JTextField m_textJPIName;
	private JTextField m_textJPIHost;
	private JTextField m_textJPIPort;
	private CertPanel m_jpiCertPanel;
	private TitledGridBagPanel m_databasePanel;
	private JTextField m_textDatabaseHost;
	private JTextField m_textDatabasePort;
	private JTextField m_textDatabaseDBName;
	private JTextField m_textDatabaseUsername;
	private TitledGridBagPanel m_generalPanel;
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
		c.insets = getDefaultInsets();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;

		// ENABLE checkbox
		/*
		m_miscPanel.setToolTipText("<html>Please select whether you want to enable payment.<br>" +
								   "This is only possible for the FirstMix at the moment</html>");*/
		m_chkPaymentEnabled = new JCheckBox("Enable Payment");
		m_chkPaymentEnabled.setSelected(false);
		m_chkPaymentEnabled.addItemListener(this);
		m_chkPaymentEnabled.addActionListener(this);
		this.add(m_chkPaymentEnabled, c);

		// GENERAL settings panel
		m_generalPanel = new TitledGridBagPanel("General settings");
		m_generalPanel.setToolTipText(
			"General settings regarding the payment system"
			);
		label = new JLabel("SoftLimit (Bytes)");
		label.setToolTipText(
			"<html>This is the soft limit for the number of bytes a Jap can use before <br>" +
			"sending a cost confirmation</html>"
			);
		m_textSoftLimit = new JAPJIntField(
			new JAPJIntField.IntFieldWithoutZeroBounds(JAPJIntField.NO_MAXIMUM_BOUND));
		m_textSoftLimit.setName("Accounting/SoftLimit");
		m_textSoftLimit.addFocusListener(this);
		m_generalPanel.addRow(label, m_textSoftLimit);

		label = new JLabel("HardLimit (Bytes)");
		label.setToolTipText(
			"<html>This is the hard limit for the number of bytes a Jap can use without <br>" +
			"sending a cost confirmation, before he will be kicked.</html>"
			);
		m_textHardLimit = new JAPJIntField(
			new JAPJIntField.IntFieldWithoutZeroBounds(JAPJIntField.NO_MAXIMUM_BOUND));
		m_textHardLimit.setName("Accounting/HardLimit");
		m_textHardLimit.addFocusListener(this);
		m_generalPanel.addRow(label, m_textHardLimit);

		label = new JLabel("SettleInterval (seconds)");
		label.setToolTipText(
			"<html>This is the interval in seconds that the SettleThread will sleep<br>" +
			"before each cycle of transmitting all the open CostConfirmations to the BI</html>"
			);
		m_textSettleInterval = new JAPJIntField(
			new JAPJIntField.IntFieldWithoutZeroBounds(JAPJIntField.NO_MAXIMUM_BOUND));
		m_textSettleInterval.setName("Accounting/SettleInterval");
		m_textSettleInterval.addFocusListener(this);
		m_generalPanel.addRow(label, m_textSettleInterval);

		c.gridy++;
		this.add(m_generalPanel, c);

		// DATABASE Panel
		m_databasePanel = new TitledGridBagPanel("PostgreSQL Database for accounting");
		m_databasePanel.setToolTipText("The accounting instance inside the First Mix needs a PostgreSQL<br> " +
									   "database to store some internal accounting data. Before you start<br> " +
									   "the First Mix with payment enabled, setup a Postgresql DB and enter<br> " +
									   "its connection data here.");

		m_textDatabaseHost = new JTextField();
		m_textDatabaseHost.setName("Accounting/Database/Host");
		m_textDatabaseHost.addFocusListener(this);
		m_databasePanel.addRow(new JLabel("Database Hostname:"), m_textDatabaseHost);

		m_textDatabasePort = new JAPJIntField(ListenerInterface.PORT_MAX_VALUE);
		m_textDatabasePort.setName("Accounting/Database/Port");
		m_textDatabasePort.addFocusListener(this);
		m_databasePanel.addRow(new JLabel("Database Portnumber:"), m_textDatabasePort,
							   GridBagConstraints.NONE);

		m_textDatabaseDBName = new JTextField();
		m_textDatabaseDBName.setName("Accounting/Database/DBName");
		m_textDatabaseDBName.addFocusListener(this);
		m_databasePanel.addRow(new JLabel("Database Name:"), m_textDatabaseDBName);

		m_textDatabaseUsername = new JTextField();
		m_textDatabaseUsername.setName("Accounting/Database/Username");
		m_textDatabaseUsername.addFocusListener(this);
		m_databasePanel.addRow(new JLabel("Database Username:"), m_textDatabaseUsername);

		c.gridy++;
		add(m_databasePanel, c);

		// JPI Certificate Panel
		m_jpiCertPanel = new CertPanel("JPI Certificate",
									   "If you have the Public Certificate of a " +
									   "running JPI, you can import it here.",
									   (JAPCertificate)null, CertPanel.CERT_ALGORITHM_DSA);
		m_jpiCertPanel.setName("Accounting/PaymentInstance/Certificate");
		m_jpiCertPanel.addChangeListener(this);
		c.gridy--;
		c.gridx++;
		add(m_jpiCertPanel, c);

		// JPI Panel
		m_jpiPanel = new TitledGridBagPanel("JPI (Java Payment Instance)");
		m_jpiPanel.setToolTipText(
			"Please enter the Hostname or IP Address and the port number of the JPI<br> " +
			"that your mix should use.");

		m_textJPIHost = new JTextField();
		m_textJPIHost.setName("Accounting/PaymentInstance/Network/ListenerInterfaces/ListenerInterface/Host");
		m_textJPIHost.addFocusListener(this);
		m_jpiPanel.addRow(new JLabel("JPI Hostname:"), m_textJPIHost);

		m_textJPIPort = new JAPJIntField(ListenerInterface.PORT_MAX_VALUE);
		m_textJPIPort.setName("Accounting/PaymentInstance/Network/ListenerInterfaces/ListenerInterface/Port");
		m_textJPIPort.addFocusListener(this);
		m_jpiPanel.addRow(new JLabel("JPI Portnumber:"), m_textJPIPort, GridBagConstraints.NONE);
		m_textJPIName = new JTextField();
		m_textJPIName.setName("Accounting/PaymentInstance/id");
		m_textJPIName.addFocusListener(this);
		m_jpiPanel.addRow(new JLabel("JPI ID:"), m_textJPIName);

		c.gridy++;
		add(m_jpiPanel, c);

		//Keep the panels in place
		c.gridx++;
		c.gridy++;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = c.BOTH;
		add(new JLabel(), c);

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
			JAPDialog.showErrorDialog(MixConfig.getMainWindow(), null, LogType.GUI, ex);
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

	public String getHelpContext()
	{
		return JAPHelpContext.INDEX;
	}

	public Vector check()
	{
		return new Vector(); /* nothing to check on this panel */
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
