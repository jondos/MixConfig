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
package mixconfig.panels;

import gui.JAPJIntField;
import gui.MixConfigTextField;
import gui.TitledGridBagPanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import mixconfig.ConfigurationEvent;
import mixconfig.MixConfiguration;
import anon.crypto.JAPCertificate;
import anon.infoservice.ListenerInterface;
import anon.pay.xml.XMLPriceCertificate;

/**
 * The PaymentPanel is one page in the MixConfig TabbedPane and allows the user to specify
 * the data which is needed for the mix to successfully use payment, namely the JPI Host/Port,
 * and the Postgresql Database Host/Port/DBName/Username.
 *
 * @author Bastian Voigt
 * @author ronin &lt;ronin2@web.de&gt;
 * @author Tobias Bayer
 * @author Johannes Renner
 */
public class PaymentPanel extends MixConfigPanel implements ChangeListener
{
	// Paths
	public static final String XMLPATH_ACCOUNTING = "Accounting";
	public static final String XMLPATH_SOFTLIMIT = XMLPATH_ACCOUNTING + "/SoftLimit";
	public static final String XMLPATH_HARDLIMIT = XMLPATH_ACCOUNTING + "/HardLimit";
	public static final String XMLPATH_PREPAIDINTERVAL = XMLPATH_ACCOUNTING + "/PrepaidInterval";
	public static final String XMLPATH_SETTLEINTERVAL = XMLPATH_ACCOUNTING + "/SettleInterval";
	
	public static final String XMLPATH_PAYMENT_INSTANCE = XMLPATH_ACCOUNTING + "/PaymentInstance";
	public static final String XMLPATH_PI_HOST = XMLPATH_PAYMENT_INSTANCE + "/Network/ListenerInterfaces/ListenerInterface/Host";
	public static final String XMLPATH_PI_PORT = XMLPATH_PAYMENT_INSTANCE + "/Network/ListenerInterfaces/ListenerInterface/Port";
	public static final String XMLPATH_PI_CERT = XMLPATH_PAYMENT_INSTANCE + "/Certificate";
	
	public static final String XMLPATH_PRICE_CERT = XMLPATH_ACCOUNTING + "/PriceCertificate";
	public static final String XMLPATH_DATABASE = XMLPATH_ACCOUNTING + "/Database";
	public static final String XMLPATH_DATABASE_HOST = XMLPATH_DATABASE + "/Host";
	public static final String XMLPATH_DATABASE_PORT = XMLPATH_DATABASE + "/Port";
	public static final String XMLPATH_DATABASE_NAME = XMLPATH_DATABASE + "/DBName";
	public static final String XMLPATH_DATABASE_USERNAME = XMLPATH_DATABASE + "/Username";
	public static final String XMLPATH_DATABASE_PASSWORD = XMLPATH_DATABASE + "/Password";
	
	// Attributes
	public static final String XML_ATTRIBUTE_ID = "id";
	
	// Path to the JPI certificate
	public static final String FILESYSTEM_PATH_PI_CERT = "certificates/Payment_Instance.cer";
	
	// Password length constant
	private static final int VISIBLE_PASSWORD_LENGTH = 15;
		
	// Certificate panels
	private PriceCertPanel m_panelPriceCert;
	private CertPanel m_panelJPICert;
	
	// JPI panel
	private TitledGridBagPanel m_panelJPI;
	private MixConfigTextField m_tfJPIHost;
	private JAPJIntField m_tfJPIPort;
	// Database panel
	private TitledGridBagPanel m_panelDatabase;
	private MixConfigTextField m_tfDatabaseHost;
	private JAPJIntField m_tfDatabasePort;
	private MixConfigTextField m_tfDatabaseName;
	private MixConfigTextField m_tfDatabaseUsername;
	private JPasswordField m_pwfDatabasePassword;
	// General panel
	private TitledGridBagPanel m_panelGeneral;
	private JAPJIntField m_tfSoftLimit;
	private JAPJIntField m_tfHardLimit;
	private JAPJIntField m_tfPrepaidInterval;
	private JAPJIntField m_tfSettleInterval;

	/**
	 * Constructor
	 * @throws IOException
	 */
	public PaymentPanel() throws IOException
	{
		super("Payment");
		// Set a new layout to the panel
		setLayout(new GridBagLayout());
		// Get the initial constraints
		GridBagConstraints c = getInitialConstraints();

		// JPI Certificate Panel
		m_panelJPICert = new CertPanel("JPI Certificate",
									   "If you have the public certificate of a " +
									   "running JPI, you can import it here.",
									   (JAPCertificate)null, CertPanel.CERT_ALGORITHM_DSA,
									   JAPCertificate.CERTIFICATE_TYPE_PAYMENT);
		m_panelJPICert.setName(XMLPATH_PI_CERT);
		m_panelJPICert.addChangeListener(this);
		// ADD
		c.gridx = 0;
		c.gridy = 0;
		this.add(m_panelJPICert, c);
		
		// JPI Panel
		m_panelJPI = new TitledGridBagPanel("JPI (Java Payment Instance)");
		m_panelJPI.setToolTipText("<html>Please enter the hostname or IP-address and port number of the JPI<br/> " +
			"the mix should use.</html>");
		// Host
		m_tfJPIHost = new MixConfigTextField();
		m_tfJPIHost.setName(XMLPATH_PI_HOST);
		m_tfJPIHost.addFocusListener(this);
		m_panelJPI.addRow(new JLabel("JPI Hostname:"), m_tfJPIHost);
		// Port
		m_tfJPIPort = new JAPJIntField(ListenerInterface.PORT_MAX_VALUE);
		m_tfJPIPort.setName(XMLPATH_PI_PORT);
		m_tfJPIPort.addFocusListener(this);
		m_panelJPI.addRow(new JLabel("JPI Portnumber:"), m_tfJPIPort);//, GridBagConstraints.NONE);
		// ADD
		c.gridx = 0;
		c.gridy = 1;
		this.add(m_panelJPI, c);

		// GENERAL settings panel
		m_panelGeneral = new TitledGridBagPanel("General Settings");
		m_panelGeneral.setToolTipText("General settings concerning the payment system");
		// Soft Limit
		JLabel label = new JLabel("Soft Limit (bytes)");
		label.setToolTipText(
			"<html>This is the soft limit for the number of bytes a Jap can use before <br/>" +
			"sending a cost confirmation</html>");
		m_tfSoftLimit = new JAPJIntField(
			new JAPJIntField.IntFieldWithoutZeroBounds(JAPJIntField.NO_MAXIMUM_BOUND));
		m_tfSoftLimit.setName(XMLPATH_SOFTLIMIT);
		m_tfSoftLimit.addFocusListener(this);
		m_panelGeneral.addRow(label, m_tfSoftLimit);
		// Hard Limit
		label = new JLabel("Hard Limit (bytes)");
		label.setToolTipText(
			"<html>This is the hard limit for the number of bytes a JAP can use without <br/>" +
			"sending a cost confirmation, before he will be kicked.</html>");
		m_tfHardLimit = new JAPJIntField(
			new JAPJIntField.IntFieldWithoutZeroBounds(JAPJIntField.NO_MAXIMUM_BOUND));
		m_tfHardLimit.setName(XMLPATH_HARDLIMIT);
		m_tfHardLimit.addFocusListener(this);
		m_panelGeneral.addRow(label, m_tfHardLimit);
		// Prepaid Interval
		label = new JLabel("Prepaid Interval (bytes)");
		label.setToolTipText("<html>The prepaid interval is given in bytes</html>");
		m_tfPrepaidInterval = new JAPJIntField(
			new JAPJIntField.IntFieldWithoutZeroBounds(JAPJIntField.NO_MAXIMUM_BOUND));
		m_tfPrepaidInterval.setName(XMLPATH_PREPAIDINTERVAL);
		m_tfPrepaidInterval.addFocusListener(this);
		m_panelGeneral.addRow(label, m_tfPrepaidInterval);
		// Settle Interval
		label = new JLabel("Settle Interval (s)");
		label.setToolTipText(
			"<html>This is the interval in seconds that the SettleThread will sleep before" +
			"each<br/> cycle of transmitting all the open cost confirmations to the PI</html>");
		m_tfSettleInterval = new JAPJIntField(
			new JAPJIntField.IntFieldWithoutZeroBounds(JAPJIntField.NO_MAXIMUM_BOUND));
		m_tfSettleInterval.setName(XMLPATH_SETTLEINTERVAL);
		m_tfSettleInterval.addFocusListener(this);
		m_panelGeneral.addRow(label, m_tfSettleInterval);
		// ADD
		c.gridx = 0;
		c.gridy = 2;
		this.add(m_panelGeneral, c);
		
		// PRICE Certificate Panel
		m_panelPriceCert = new PriceCertPanel("Price Certificate", "Import the price certificate here", (XMLPriceCertificate)null);
		m_panelPriceCert.setName(PriceCertPanel.XMLPATH_PRICECERT);
		m_panelPriceCert.addChangeListener(this);
		// ADD
		// Use fill to equalize the dimensions of the panels
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 1;
		c.gridy = 0;
		this.add(m_panelPriceCert, c);
		
		// DATABASE Panel
		m_panelDatabase = new TitledGridBagPanel("PostgreSQL Database for Accounting");
		m_panelDatabase.setToolTipText("<html>The accounting instance inside a FirstMix needs a PostgreSQL<br/> " +
									   "database to store some internal accounting data. Before you start<br/> " +
									   "the FirstMix with payment enabled, setup a PostgreSQL DB and enter<br/> " +
									   "its connection data here.</html>");
		// Host
		m_tfDatabaseHost = new MixConfigTextField();
		m_tfDatabaseHost.setName(XMLPATH_DATABASE_HOST);
		m_tfDatabaseHost.addFocusListener(this);
		m_panelDatabase.addRow(new JLabel("Database Hostname:"), m_tfDatabaseHost);
		// Port
		m_tfDatabasePort = new JAPJIntField(ListenerInterface.PORT_MAX_VALUE);
		m_tfDatabasePort.setName(XMLPATH_DATABASE_PORT);
		m_tfDatabasePort.addFocusListener(this);
		m_panelDatabase.addRow(new JLabel("Database Portnumber:"), m_tfDatabasePort, GridBagConstraints.NONE);
		// DB Name
		m_tfDatabaseName = new MixConfigTextField();
		m_tfDatabaseName.setName(XMLPATH_DATABASE_NAME);
		m_tfDatabaseName.addFocusListener(this);
		m_panelDatabase.addRow(new JLabel("Database Name:"), m_tfDatabaseName);
		// User	
		m_tfDatabaseUsername = new MixConfigTextField();
		m_tfDatabaseUsername.setName(XMLPATH_DATABASE_USERNAME);
		m_tfDatabaseUsername.addFocusListener(this);
		m_panelDatabase.addRow(new JLabel("Database Username:"), m_tfDatabaseUsername);
		// Password
		m_pwfDatabasePassword = new JPasswordField(VISIBLE_PASSWORD_LENGTH);
		m_pwfDatabasePassword.setName(XMLPATH_DATABASE_PASSWORD);
		m_pwfDatabasePassword.addFocusListener(this);
		//String m_passwordHash = new String();
		JTextField m_txtHashedPassword = new JTextField(VISIBLE_PASSWORD_LENGTH); // dummy label ??
		m_txtHashedPassword.setEditable(false);
		m_panelDatabase.addRow(new JLabel("Database Password:"), m_pwfDatabasePassword);
		// ADD
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		c.gridheight = 2;
		this.add(m_panelDatabase, c);
		// Set height back to 1
		c.gridheight = 1;
		
		// Keep the panels in place
		c.fill = GridBagConstraints.BOTH;
		c.gridy = 3;
		c.weighty = 1;
		add(new JLabel(), c);

		setEnabled(true);
	}
	
	// -------------------- PRIVATE METHODS --------------------
	
	/**
	 * Check the 'payment'-attribute in the Mix Type element of the configuration
	 * @return true if payment is enabled, false if attribute is set to false or not existing
	 */
	private boolean isPaymentEnabled() 
	{
		boolean ret = false;
		// Get the attribute
		String sEnablePayment = getConfiguration().getAttributeValue(GeneralPanel.XMLPATH_GENERAL_MIXTYPE, 
				GeneralPanel.XML_ATTRIBUTE_PAYMENT);
		if (sEnablePayment != null && sEnablePayment.equalsIgnoreCase("true"))
		{
			ret = true;
		}
		return ret;
	}
	
	// -------------------- PROTECTED METHODS --------------------

	/**
	 * Enable or disable the single components of this panel
	 */
	protected void enableComponents()
	{
		//boolean compEnabled = m_chkPaymentEnabled.isSelected() &&
		//	m_chkPaymentEnabled.isEnabled();
		// Currently enable always
		boolean compEnabled = true;
		
		// Both of the certificate panels
		m_panelPriceCert.setEnabled(compEnabled);
		m_panelJPICert.setEnabled(compEnabled);
		// The other panels + sub-components
		m_panelGeneral.setEnabled(compEnabled);
		Component[] components = m_panelGeneral.getComponents();
		for (int i = 0; i < components.length; i++)
		{
			components[i].setEnabled(compEnabled);
		}
		m_panelJPI.setEnabled(compEnabled);
		components = m_panelJPI.getComponents();
		for (int i = 0; i < components.length; i++)
		{
			components[i].setEnabled(compEnabled);
		}
		
		// Show database options only if configuring a FirstMix
		if ((getConfiguration() != null) && (getConfiguration().getMixType() != MixConfiguration.MIXTYPE_FIRST))
		{
			compEnabled = false;
		}
		m_panelDatabase.setEnabled(compEnabled);
		components = m_panelDatabase.getComponents();
		for (int i = 0; i < components.length; i++)
		{
			components[i].setEnabled(compEnabled);
		}
	}	
	
	// -------------------- PUBLIC METHODS --------------------
	
	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		// First enable all components to make MixConfigPanel load the data
		this.enableComponents();
		// Set the given configuration
		super.setConfiguration(a_conf);
		// Make sure this panel is contained only once in the config's listeners list
		a_conf.removeChangeListener(this);
		a_conf.addChangeListener(this);
		// Check, if PaymentPanel is currently enabled
		setAutoSaveEnabled(false);
		this.setEnabled(isPaymentEnabled());
		this.enableComponents();
		// Re-enable
		setAutoSaveEnabled(true);
	}
	
	public void stateChanged(ChangeEvent e)
	{
		try
		{
			if (e instanceof ConfigurationEvent)
			{
				ConfigurationEvent c = (ConfigurationEvent) e;
				if (c.getModifiedXMLPath().equals(GeneralPanel.XMLPATH_GENERAL_MIXTYPE))
				{					
					// XXX Currently we are not removing the node 'Accounting' when disabling payment
					//getConfiguration().removeNode(XMLPATH_ACCOUNTING);
					//this.load();
					
					if (isPaymentEnabled())
					{
						if (!getConfiguration().isAccountingNodePresent())
						{
							// Load default values
							this.getConfiguration().addAccounting();
							this.load();
						}
					}
					
					this.setEnabled(isPaymentEnabled());
					this.enableComponents();
				}
			}
			
			// Checkout the source
			if (e.getSource() instanceof CertPanel)
			{
				// Save the JPI-CertPanel first
				CertPanel panel = (CertPanel)e.getSource();
				save(panel);
				// Get the certificate
				JAPCertificate cert = (JAPCertificate)panel.getCert();
				if (cert != null)
				{
					// Set the attribute to Accounting/PaymentInstance
					String ski = cert.getSubjectKeyIdentifierConcatenated();
					if (ski != null)
					{
						getConfiguration().setAttribute(XMLPATH_PAYMENT_INSTANCE, XML_ATTRIBUTE_ID, ski);
					}
				}
				else
				{
					// Certificate is null --> remove the attribute
					getConfiguration().removeAttribute(XMLPATH_PAYMENT_INSTANCE, XML_ATTRIBUTE_ID);
					// FIXME: If there are no more children left, remove the element itself as well?
				}
			}
			else if (e.getSource() instanceof PriceCertPanel)
			{
				save((PriceCertPanel) e.getSource());
			}
		}
		catch (IOException ioe)
		{
			LogHolder.log(LogLevel.ERR, LogType.PAY, "Error: " + ioe.getMessage());
		}
	}

	public String getHelpContext()
	{
		return PaymentPanel.class.getName();
	}

	/**
	 * Check this panel for validity
	 */
	public Vector<String> check()
	{
		//LogHolder.log(LogLevel.DEBUG, LogType.PAY, "Checking PaymentPanel for validity");
		Vector<String> errors = new Vector<String>();
		// Get the configuration
		MixConfiguration mixConf = getConfiguration();
		
		// Check the certificates
		String value = mixConf.getValue(XMLPATH_PI_CERT);
		if (value == null)
		{
			errors.add("The JPI certificate is required!");
		}
		value = mixConf.getValue(XMLPATH_PRICE_CERT);
		if (value == null)
		{
			errors.add("The price certificate is required!");
		}
		
		// Prepaid Interval
		value = mixConf.getValue(XMLPATH_PREPAIDINTERVAL);
		if ((value != null) && (new Integer(value) > 3000000))
		{
			errors.add("The 'Prepaid Interval' can at most be specified to 3000000 bytes!");
		}
		// Settle Interval
		value = mixConf.getValue(XMLPATH_SETTLEINTERVAL);
		if ((value != null) && (new Integer(value) < 1))
		{
			errors.add("The 'Settle Interval' needs to be specified to at least 1 second!");
		}
		
		// Check JPI host and port
		String sPort = mixConf.getValue(XMLPATH_PI_PORT);
		String sHost = mixConf.getValue(XMLPATH_PI_HOST);
		// If NOT both are empty --> check for inconsistencies
		if ((sHost == null || sHost.equals("")) && (sPort == null || sPort.equals("")))
		{
			errors.add("You need to specify a JPI hostname and portnumber!");
		}
		else
		{
			if (sPort != null && sHost == null) 
				errors.add("You entered a port, but no host for Java Payment Instance!");
			if (sPort == null && sHost != null) 
				errors.add("You entered a host, but no port for Java Payment Instance!");
			if (sHost != null && (sHost.equals("") || sHost.indexOf(" ") != -1))
				errors.add("The JPI hostname is invalid!");
		}
		
		// Check database properties
		if (m_panelDatabase.isEnabled())
		{
			value = mixConf.getValue(XMLPATH_DATABASE_HOST);
			if (value == null) errors.add("Database hostname is missing!");
			else if (value.indexOf(" ") != -1) errors.add("Database hostname is invalid!");
			
			value = mixConf.getValue(XMLPATH_DATABASE_PORT);
			if (value == null) errors.add("Database portnumber is missing!");
			
			value = mixConf.getValue(XMLPATH_DATABASE_NAME);
			if (value == null) errors.add("Database name is missing!");
			
			value = mixConf.getValue(XMLPATH_DATABASE_USERNAME);
			if (value == null) errors.add("Database username is missing!");
			
			// Password might be empty
			//value = mixConf.getValue(XMLPATH_DATABASE_PASSWORD);
			//if (value == null) errors.add("Database password is missing!");
		}
		
		return errors;
	}

	public Container getHelpExtractionDisplayContext() 
	{
		return null;
	}

	/*
	public void actionPerformed(ActionEvent a_e)
	{
		if (a_e.getSource() == m_chkPaymentEnabled)
		{
			if (m_chkPaymentEnabled.isSelected())
			{
				LogHolder.log(LogLevel.NOTICE, LogType.PAY, "Configuration of accounting is now enabled");
			}
			else
			{
				boolean delete = true;
				// Check if there is an entry somewhere
				if (m_panelPriceCert.getCert() != null ||
					m_panelJPICert.getCert() != null ||	
					!m_tfDatabaseName.getText().equalsIgnoreCase("") ||
					!m_tfDatabaseHost.getText().equalsIgnoreCase("") ||
					!m_tfDatabasePort.getText().equalsIgnoreCase("") ||
					!m_tfDatabaseUsername.getText().equalsIgnoreCase("") ||
					!(m_pwfDatabasePassword.getPassword().length == 0) ||
					!m_tfHardLimit.getText().equalsIgnoreCase("") ||
					!m_tfJPIHost.getText().equalsIgnoreCase("") ||
					!m_tfJPIName.getText().equalsIgnoreCase("") ||
					!m_tfJPIPort.getText().equalsIgnoreCase("") ||
					!m_tfSettleInterval.getText().equalsIgnoreCase("") ||
					!m_tfPrepaidInterval.getText().equalsIgnoreCase("") ||
					!m_tfSoftLimit.getText().equalsIgnoreCase(""))
				{
					// Ask the user before deleting any entries
					boolean b = JAPDialog.showYesNoDialog(MixConfig.getMainWindow(),
						"Really delete all entries and switch off the payment function?");
					if (!b)
					{
						delete = false;
					}
				}
				if (delete)
				{
					// Delete certificates
					m_panelJPICert.removeCert();
					m_panelPriceCert.removeCert();
					
					// Delete all entries
					m_tfDatabaseHost.setText("");
					m_tfDatabasePort.setText("");
					m_tfDatabaseName.setText("");
					m_tfDatabaseUsername.setText("");
					m_pwfDatabasePassword.setText("");
					m_tfSoftLimit.setText("");
					m_tfHardLimit.setText("");
					m_tfPrepaidInterval.setText("");
					m_tfSettleInterval.setText("");
					m_tfJPIHost.setText("");
					m_tfJPIPort.setText("");
					m_tfJPIName.setText("");

					// Remove the actual configuration node
					MixConfig.getMixConfiguration().removeNode("Accounting");
				}
				else
				{
					// Re-check box
					m_chkPaymentEnabled.setSelected(true);
				}
			}
		}
	} */
}
