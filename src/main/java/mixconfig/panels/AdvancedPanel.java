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
package mixconfig.panels;

import gui.GUIUtils;
import gui.JAPHelpContext;
import gui.JAPJIntField;
import gui.MixConfigTextField;
import gui.TitledGridBagPanel;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import mixconfig.ConfigurationEvent;
import mixconfig.MixConfiguration;
import anon.crypto.CertificateContainer;
import anon.crypto.CertificateInfoStructure;
import anon.crypto.JAPCertificate;
import anon.crypto.SignatureVerifier;
import anon.infoservice.ListenerInterface;
import anon.util.Base64;
import anon.util.JAPMessages;

/**
 * The panel for advanced settings
 * 
 * @author Tobias Bayer
 * @author Johannes Renner
 */
public class AdvancedPanel extends MixConfigPanel implements ChangeListener /*,ActionListener*/
{
	/** XML-Paths */
	private static final String XMLPATH_TRAFFIC_SHAPING = "Ressources";
	private static final String XMLPATH_TRAFFIC_SHAPING_LATENCY = XMLPATH_TRAFFIC_SHAPING + "/Latency";
	private static final String XMLPATH_TRAFFIC_SHAPING_TIME_INTERVAL = XMLPATH_TRAFFIC_SHAPING + "/Intervall";
	private static final String XMLPATH_TRAFFIC_SHAPING_PACKET_INTERVAL = XMLPATH_TRAFFIC_SHAPING + "/BytesPerIntervall";
	private static final String XMLPATH_TRAFFIC_SHAPING_UNSHAPED_TRAFFIC = XMLPATH_TRAFFIC_SHAPING + "/UnlimitTraffic";
	
	public static final String XMLPATH_MONITORING_HOST = "Network/ServerMonitoring/Host";
	public static final String XMLPATH_MONITORING_PORT = "Network/ServerMonitoring/Port";
	
	/** Messages */
	private static final String MSG_SERVER_MONITORING = AdvancedPanel.class.getName() + "_SM";
	private static final String MSG_SERVER_MONITORING_TOOLTIP = AdvancedPanel.class.getName() + "_smToolTip";
	private static final String MSG_SERVER_MONITORING_HOST = AdvancedPanel.class.getName() + "_smHost";
	private static final String MSG_SERVER_MONITORING_PORT = AdvancedPanel.class.getName() + "_smPort";
	
	private static final String MSG_TRAFFIC_SHAPING = AdvancedPanel.class.getName() + "_TS";
	private static final String MSG_LATENCY = AdvancedPanel.class.getName() + "_tsLatency";
	private static final String MSG_LATENCY_TOOLTIP = AdvancedPanel.class.getName() + "_tsLatencyToolTip";
	private static final String MSG_TIME_INTERVAL = AdvancedPanel.class.getName() + "_tsTimeInterval";
	private static final String MSG_PACKET_INTERVAL = AdvancedPanel.class.getName() + "_tsPacketInterval";
	private static final String MSG_SHAPING_INTERVAL_TOOLTIP = AdvancedPanel.class.getName() + "_tsShapingIntervalToolTip";
	private static final String MSG_UNSHAPED_TRAFFIC = AdvancedPanel.class.getName() + "_tsUnshapedTraffic";
	private static final String MSG_UNSHAPED_TRAFFIC_TOOLTIP = AdvancedPanel.class.getName() + "_tsUnshapedTrafficToolTip";
	
	private static final String MSG_SET_UID = AdvancedPanel.class.getName() + "_setUID";
	private static final String MSG_SET_FD = AdvancedPanel.class.getName() + "_setFD";
	private static final String MSG_SET_MAXUSERS = AdvancedPanel.class.getName() + "_setMaxUsers";
	private static final String MSG_RUN_DAEMON = AdvancedPanel.class.getName() + "_runDaemon";
	private static final String MSG_ENABLE_ROOT_VERIFICATION = AdvancedPanel.class.getName() + "_enableRootVerification";
	private static final String MSG_ENABLE_LOGGING = AdvancedPanel.class.getName() + "_enableLogging";
	private static final String MSG_LOG_CONSOLE = AdvancedPanel.class.getName() + "_logConsole";
	private static final String MSG_LOG_DIR = AdvancedPanel.class.getName() + "_logDir";
	private static final String MSG_COMPRESS_LOG = AdvancedPanel.class.getName() + "_compressLog";
	private static final String MSG_LOG_SYSLOG = AdvancedPanel.class.getName() + "_logSyslog";
	
	private static final String MSG_LOG_LABEL_MAX_FILE_SIZE = AdvancedPanel.class.getName() + "_logLabelMaxFileSize";
	private static final String MSG_LOG_LABEL_MAX_FILES = AdvancedPanel.class.getName() + "_logLabelMaxFiles";

	private static final String XML_ELEMENT_GENERAL = "General";
	private static final String XML_ELEMENT_LOGGING = "Logging";
	private static final String XML_ATTR_COMPRESSED = "compressed";
	private static final String XML_ATTR_MAX_LOG_FILE_SIZE = "MaxFileSize";
	private static final String XML_ATTR_MAX_LOG_FILES = "MaxFiles";
	
	// Panels
	private TitledGridBagPanel m_panelLogging, m_panelMonitoring, m_panelTrafficShaping, m_panelMisc;
	private CertPanel m_panelLogCert;
	
	// Filename (Logging)
	private MixConfigTextField m_tfFileName;
	// Monitoring host and port
	private MixConfigTextField m_tfMonitoringHost;
	private JAPJIntField m_tfMonitoringPort;
	// Traffic Shaping TextFields
	private JAPJIntField m_tfLatency, m_tfTimeInterval;
	private JAPJIntField m_tfBytesPerInterval, m_tfUnshapedTraffic;
	// User ID, File descriptors and max number of users
	private MixConfigTextField m_tfUID;
	private JAPJIntField m_tfNumFileDesc, m_tfMaxUsers, 
		m_tfMaxLogFileSize, m_tfMaxLogFiles;
	
	// Other components:
	private JLabel m_lblMaxUsers; // Might be disabled
	private JCheckBox m_cbDaemon, m_cbCompressLog, m_cbPathVerification;
	private JRadioButton m_rbConsole, m_rbFile, m_rbSyslog, m_rbNoLog;
	private ButtonGroup m_loggingButtonGroup;
	
	private String m_logFilePath = "General/Logging/File";

	/**
	 * The Constructor
	 */
	public AdvancedPanel()
	{
		// Setup the layout
		super("Advanced");
		setAutoSaveEnabled(false);
		this.setLayout(new GridBagLayout());
		
		// Get the initial constraints
		GridBagConstraints constraints = getInitialConstraints();
										
		// Logging Panel
		m_panelLogging = new TitledGridBagPanel("Logging");		
		constraints.gridheight = 2;
		constraints.fill = GridBagConstraints.BOTH;
		this.add(m_panelLogging, constraints);
		// Radio Button
		m_rbNoLog = new JRadioButton(JAPMessages.getString(MSG_ENABLE_LOGGING));
		m_rbNoLog.setSelected(true);
		m_rbNoLog.setModel(new ToggleButtonModel());
		m_rbNoLog.addItemListener(this);
		m_panelLogging.addRow(m_rbNoLog, null);
		// Console Logging
		m_rbConsole = new JRadioButton(JAPMessages.getString(MSG_LOG_CONSOLE));
		m_rbConsole.setModel(new ToggleButtonModel());
		m_rbConsole.addItemListener(this);
		m_panelLogging.addRow(m_rbConsole, null);
		// Log to Directory JRadioButton
		m_rbFile = new JRadioButton(JAPMessages.getString(MSG_LOG_DIR));
		m_rbFile.setModel(new ToggleButtonModel());
		m_rbFile.addItemListener(this);
		// Log to Directory JTextField
		m_tfFileName = new MixConfigTextField();
		m_tfFileName.addFocusListener(this);
		m_tfFileName.setEnabled(false);
		
		m_tfMaxLogFiles = new JAPJIntField(100);
		m_tfMaxLogFiles.addFocusListener(this);
		m_tfMaxLogFiles.setEnabled(false);
		
		m_tfMaxLogFileSize = new JAPJIntField(2000);
		m_tfMaxLogFileSize.addFocusListener(this);
		m_tfMaxLogFileSize.setEnabled(false);
		
		m_panelLogging.addRow(m_rbFile, m_tfFileName);
		//m_panelLogging.addRow(GUIUtils.createLabel(MSG_LOG_LABEL_MAX_FILE_SIZE), m_tfMaxLogFileSize);
		//m_panelLogging.addRow(GUIUtils.createLabel(MSG_LOG_LABEL_MAX_FILES), m_tfMaxLogFiles);
		m_panelLogging.addRow(GUIUtils.createLabel(MSG_LOG_LABEL_MAX_FILE_SIZE), m_tfMaxLogFileSize, 
				GUIUtils.createLabel(MSG_LOG_LABEL_MAX_FILES), m_tfMaxLogFiles);
		
		// Compress Log JCheckBox
		m_cbCompressLog = new JCheckBox(JAPMessages.getString(MSG_COMPRESS_LOG));
		m_cbCompressLog.addItemListener(this);
		m_cbCompressLog.setEnabled(false);
		m_panelLogging.addRow(null, m_cbCompressLog);
		// Syslog JRadioButton
		m_rbSyslog = new JRadioButton(JAPMessages.getString(MSG_LOG_SYSLOG));
		m_rbSyslog.setModel(new ToggleButtonModel());
		m_rbSyslog.addItemListener(this);
		m_panelLogging.addRow(m_rbSyslog, null);
		// Button Group
		m_loggingButtonGroup = new ButtonGroup();
		m_loggingButtonGroup.add(m_rbNoLog);
		m_loggingButtonGroup.add(m_rbConsole);
		m_loggingButtonGroup.add(m_rbFile);
		m_loggingButtonGroup.add(m_rbSyslog);
		
		// Encrypted Log Certificate
		m_panelLogCert = new CertPanel("Encrypted Log Certificate",
									"This is the certificate your Mix will use to encrypt the log file",
									(JAPCertificate)null, CertPanel.CERT_ALGORITHM_RSA, JAPCertificate.CERTIFICATE_TYPE_ROOT);
		m_panelLogCert.addChangeListener(this);
		m_panelLogCert.setEnabled(false);
		constraints.gridx++;
		constraints.gridheight = 1;
		this.add(m_panelLogCert, constraints);
		
		// Server Monitoring
        m_panelMonitoring = new TitledGridBagPanel(JAPMessages.getString(MSG_SERVER_MONITORING));
        m_panelMonitoring.setToolTipText(JAPMessages.getString(MSG_SERVER_MONITORING_TOOLTIP));
		constraints.gridy++;
        this.add(m_panelMonitoring, constraints);
        // Host
        m_tfMonitoringHost = new MixConfigTextField(15);
        m_tfMonitoringHost.setName(XMLPATH_MONITORING_HOST);
        m_tfMonitoringHost.addFocusListener(this);
        // Port
        m_tfMonitoringPort = new JAPJIntField(ListenerInterface.PORT_MAX_VALUE);
        m_tfMonitoringPort.setName(XMLPATH_MONITORING_PORT);
        m_tfMonitoringPort.addFocusListener(this);
        // Setup the labels
        JLabel lblMonitoringHost = new JLabel(JAPMessages.getString(MSG_SERVER_MONITORING_HOST));
        lblMonitoringHost.setHorizontalAlignment(JLabel.LEFT);
        JLabel lblMonitoringPort = new JLabel(JAPMessages.getString(MSG_SERVER_MONITORING_PORT));
        lblMonitoringPort.setHorizontalAlignment(JLabel.LEFT);
        // Add everything to the panel
        m_panelMonitoring.addRow(lblMonitoringHost, m_tfMonitoringHost);
        m_panelMonitoring.addRow(lblMonitoringPort, m_tfMonitoringPort, GridBagConstraints.NONE);
        
		// Traffic shaping panel
		m_panelTrafficShaping = new TitledGridBagPanel(JAPMessages.getString(MSG_TRAFFIC_SHAPING));
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridy++;
		add(m_panelTrafficShaping, constraints);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		// Latency
		//m_tfLatency = new JAPJIntField(new JAPJIntField.IntFieldUnlimitedZerosBounds(JAPJIntField.NO_MAXIMUM_BOUND));
		m_tfLatency = new JAPJIntField();
		m_tfLatency.setName(XMLPATH_TRAFFIC_SHAPING_LATENCY);
		m_tfLatency.addFocusListener(this);
		JLabel lblLatency = new JLabel(JAPMessages.getString(MSG_LATENCY));
		lblLatency.setToolTipText(JAPMessages.getString(MSG_LATENCY_TOOLTIP));
		m_panelTrafficShaping.addRow(lblLatency, m_tfLatency);
		// Interval
		m_tfTimeInterval = new JAPJIntField();
		m_tfTimeInterval.setName(XMLPATH_TRAFFIC_SHAPING_TIME_INTERVAL);
		m_tfTimeInterval.addFocusListener(this);
		JLabel lblTimeInterval = new JLabel(JAPMessages.getString(MSG_TIME_INTERVAL));
		lblTimeInterval.setToolTipText(JAPMessages.getString(MSG_SHAPING_INTERVAL_TOOLTIP));
		m_panelTrafficShaping.addRow(lblTimeInterval, m_tfTimeInterval);
		// BytesPerInterval
		m_tfBytesPerInterval = new JAPJIntField();
		m_tfBytesPerInterval.setName(XMLPATH_TRAFFIC_SHAPING_PACKET_INTERVAL);
		m_tfBytesPerInterval.addFocusListener(this);
		JLabel lblPacketInterval = new JLabel(JAPMessages.getString(MSG_PACKET_INTERVAL));
		lblPacketInterval.setToolTipText(JAPMessages.getString(MSG_SHAPING_INTERVAL_TOOLTIP));
		m_panelTrafficShaping.addRow(lblPacketInterval, m_tfBytesPerInterval);
		// Amount of traffic that remains 'unshaped'
		m_tfUnshapedTraffic = new JAPJIntField();
		m_tfUnshapedTraffic.setName(XMLPATH_TRAFFIC_SHAPING_UNSHAPED_TRAFFIC);
		m_tfUnshapedTraffic.addFocusListener(this);
		JLabel lblUnshapedTraffic = new JLabel(JAPMessages.getString(MSG_UNSHAPED_TRAFFIC));
		lblUnshapedTraffic.setToolTipText(JAPMessages.getString(MSG_UNSHAPED_TRAFFIC_TOOLTIP));
		m_panelTrafficShaping.addRow(lblUnshapedTraffic, m_tfUnshapedTraffic);
		
		// Miscellaneous
		m_panelMisc = new TitledGridBagPanel("Miscellaneous");
		constraints.gridx--;
		this.add(m_panelMisc, constraints);
		// User ID
		m_tfUID = new MixConfigTextField(10);
		m_tfUID.setName("General/UserID");
		m_tfUID.addFocusListener(this);
		JLabel lblUserID = new JLabel(JAPMessages.getString(MSG_SET_UID));
		lblUserID.setToolTipText("Set a user name that is the owner of the mix process");
		m_panelMisc.addRow(lblUserID, m_tfUID);
		// File Descriptors
		m_tfNumFileDesc = new JAPJIntField(new JAPJIntField.IntFieldWithoutZeroBounds(JAPJIntField.NO_MAXIMUM_BOUND));
		m_tfNumFileDesc.setName("General/NrOfFileDescriptors");
		m_tfNumFileDesc.addFocusListener(this);
		JLabel lblFileDesc = new JLabel(JAPMessages.getString(MSG_SET_FD));
		lblFileDesc.setToolTipText("Set the maximum number of open file descriptors allowed");
		m_panelMisc.addRow(lblFileDesc, m_tfNumFileDesc);
		// Max User Number 
		m_tfMaxUsers = new JAPJIntField(new JAPJIntField.IntFieldWithoutZeroBounds(JAPJIntField.NO_MAXIMUM_BOUND));
		m_tfMaxUsers.setName("General/MaxUsers");
		m_tfMaxUsers.addFocusListener(this);
		m_lblMaxUsers = new JLabel(JAPMessages.getString(MSG_SET_MAXUSERS));
		m_lblMaxUsers.setToolTipText("Set a maximum number of users that can be connected to this cascade at any time");
		m_panelMisc.addRow(m_lblMaxUsers, m_tfMaxUsers);		
		// Daemon
		m_cbDaemon = new JCheckBox(JAPMessages.getString(MSG_RUN_DAEMON));
		m_cbDaemon.setName("General/Daemon");
		m_cbDaemon.addItemListener(this);
		m_panelMisc.addRow(m_cbDaemon, null);
		
		JButton btnRootCerts = new JButton("Reset root certificates");
		
		btnRootCerts.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent a_event)
			{
				CertPanel.resetRootCertificates(getConfiguration());
			}
		});
		
		m_cbPathVerification = new JCheckBox(JAPMessages.getString(MSG_ENABLE_ROOT_VERIFICATION));
		m_cbPathVerification.setName(CertPanel.XMLPATH_CERTIFICATES_PATH_VERIFICATION);
		m_cbPathVerification.addItemListener(this);
		m_panelMisc.addRow(m_cbPathVerification, btnRootCerts);		
			
		
		// Keep the panels in place
		constraints.gridx++;
		constraints.gridy++;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.fill = GridBagConstraints.BOTH;
		add(new JLabel(), constraints);

		setAutoSaveEnabled(true);
	}
	
	public void itemStateChanged(ItemEvent a_event)
	{
		Object s = a_event.getSource();
		MixConfiguration c = getConfiguration();
		if (s == m_rbConsole && m_rbConsole.isSelected() && isAutoSaveEnabled())
		{
			c.setValue("General/Logging/Console", true);
			c.removeNode("General/Logging/Syslog");
			c.removeNode("General/Logging/File");
			c.removeNode("General/Logging/EncryptedLog");
		}
		else if (s == m_rbSyslog && m_rbSyslog.isSelected() && isAutoSaveEnabled())
		{
			c.setValue("General/Logging/Syslog", true);
			c.removeNode("General/Logging/Console");
			c.removeNode("General/Logging/File");
			c.removeNode("General/Logging/EncryptedLog");
		}
		else if (s == m_rbNoLog && m_rbNoLog.isSelected() && isAutoSaveEnabled())
		{
			c.removeNode("General/Logging/Syslog");
			c.removeNode("General/Logging/Console");
			c.removeNode("General/Logging/File");
			c.removeNode("General/Logging/EncryptedLog");
			c.removeNode("General/Logging");
		}
		else if (s == m_rbFile)
		{
			m_tfFileName.setEnabled(m_rbFile.isSelected());
			m_tfMaxLogFileSize.setEnabled(m_rbFile.isSelected());
			m_tfMaxLogFiles.setEnabled(m_rbFile.isSelected());
			m_cbCompressLog.setEnabled(m_rbFile.isSelected());
			m_panelLogCert.setEnabled(m_rbFile.isSelected());
			if (!m_rbFile.isSelected())
			{
				m_panelLogCert.removeCert();
			}
			else
			{
				if (c.isMixOnCDEnabled() && m_tfFileName.getText().equals(""))
				{
					m_tfFileName.setText("/usbstick");
				}
			}
			if (isAutoSaveEnabled())
			{
				focusLost(new FocusEvent(m_tfFileName, 0));
				c.removeNode("General/Logging/Syslog");
				c.removeNode("General/Logging/Console");
			}
		}
		else if (s == m_cbCompressLog)
		{
			setDirectoryLoggingValues();
			//focusLost(new FocusEvent(m_tfFileName, 0));
		}
		else if (s == m_cbDaemon)
		{
			if (m_cbDaemon.isSelected())
			{
				if (m_rbConsole.isSelected())
				{
					m_rbFile.setSelected(true);
				}
				m_rbConsole.setEnabled(false);
			}
			else
			{
				m_rbConsole.setEnabled(true);
			}
			save(m_cbDaemon);
		}
		else
		{
			super.itemStateChanged(a_event);
		}
	}
	
	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		// Enable all components first to load the data
		enableComponents();
		// Set the configuration
		super.setConfiguration(a_conf);
		// Make sure this panel is contained only once in the config's listeners list
		a_conf.removeChangeListener(this);
		a_conf.addChangeListener(this);
		enableComponents();
	}
	
	/**
	 * Listen for ChangeEvents
	 */
	public void stateChanged(ChangeEvent a_event)
	{
		// Check if the MixType has changed
		if (a_event instanceof ConfigurationEvent)
		{
			ConfigurationEvent c = (ConfigurationEvent) a_event;
			if (c.getModifiedXMLPath().equals(GeneralPanel.XMLPATH_GENERAL_MIXTYPE));
			// || c.getChangedAttribute().indexOf(GeneralPanel.XMLPATH_AUTOCONFIGURATION) >= 0)
			{
				enableComponents();
			}
		}
		else if (a_event.getSource() == m_panelLogCert)
		{
			if (m_panelLogCert.getCert() != null)
			{
				m_logFilePath = "General/Logging/EncryptedLog/File";
				if (isAutoSaveEnabled())
				{
					getConfiguration().setValue(
						"General/Logging/EncryptedLog/KeyInfo/X509Data/X509Certificate/X509Certificate",
						m_panelLogCert.getCert().getX509Certificate().toByteArray());
				}
			}
			else
			{
				m_logFilePath = "General/Logging/File";
			}
			if (isAutoSaveEnabled())
			{
				focusLost(new FocusEvent(m_tfFileName, 0));
			}
		}
	}

	/**
	 * Enable 'MaxUsers' only if this is a FirstMix's configuration
	 */
	protected void enableComponents()
	{
		boolean bEnableMaxUsers;
		if (getConfiguration() != null)
		{
			bEnableMaxUsers = (getConfiguration().getMixType() == MixConfiguration.MIXTYPE_FIRST);
		}
		else
		{
			bEnableMaxUsers = true;
		}
		m_tfMaxUsers.setEnabled(bEnableMaxUsers);
		m_lblMaxUsers.setEnabled(bEnableMaxUsers);
	}
	
	public void focusGained(FocusEvent a_event)
	{
		if (a_event.getSource() == m_tfFileName)
		{
			MixConfiguration c = getConfiguration();
			if (c.isMixOnCDEnabled() && m_tfFileName.getText().equals(""))
			{
				m_tfFileName.setText("/usbstick");
			}
		}
		else
		{
			super.focusGained(a_event);
		}
	}

	private void setDirectoryLoggingValues()
	{
		int iMaxLogFileSize;
		int iMaxLogFiles;
		MixConfiguration c = getConfiguration();
		c.removeNode("General/Logging/EncryptedLog/File");
		c.removeNode("General/Logging/File");
		
		String attributes[] = 
			new String[]{
				XML_ATTR_MAX_LOG_FILE_SIZE, 
				XML_ATTR_MAX_LOG_FILES,
				XML_ATTR_COMPRESSED}; 
		
		try
		{
			iMaxLogFileSize = m_tfMaxLogFileSize.getInt();
		}
		catch (NumberFormatException a_e)
		{
			iMaxLogFileSize = -1;
		}
				
		try
		{
			iMaxLogFiles = m_tfMaxLogFiles.getInt();
		}
		catch (NumberFormatException a_e)
		{
			iMaxLogFiles = -1;
		}
		
		String values[] = 
			new String[]{
				iMaxLogFileSize > 0 ? 
						Integer.toString(iMaxLogFileSize*1024*1024) : "", //output value in Mbytes 
				iMaxLogFiles > 0 ?
						Integer.toString(iMaxLogFiles) : "",
				m_cbCompressLog.isSelected() ? "true" : ""};
		
		for (int i = 0; i < attributes.length; i++) 
		{
			if((values[i] == null) || values[i].equals("") )
			{
				c.removeAttribute(m_logFilePath, attributes[i]);
			}
			else
			{
				c.setAttribute(m_logFilePath, attributes[i], values[i]);
			}
		}
		c.setValue(m_logFilePath, m_tfFileName.getText());
	}
	
	public void focusLost(FocusEvent a_event)
	{
		MixConfiguration c = getConfiguration();

		if ( (a_event.getSource() == m_tfFileName) ||
			 (a_event.getSource() == m_tfMaxLogFileSize) ||
			 (a_event.getSource() == m_tfMaxLogFiles) )
		{
			setDirectoryLoggingValues();
		}	
		else if (a_event.getSource() == m_tfNumFileDesc)
		{
			if (m_tfNumFileDesc.getText().equals(""))
			{
				c.removeNode("General/NrOfFileDescriptors");
			}
			else
			{
				save(m_tfNumFileDesc);
			}
		}
		else if (a_event.getSource() == m_tfMaxUsers)
		{
			if (m_tfMaxUsers.getText().equals(""))
			{
				c.removeNode("General/MaxUsers");
			}
			else
			{
				save(m_tfMaxUsers);
			}
		}
		else if (a_event.getSource() == m_tfUID)
		{
			if (m_tfUID.getText().equals(""))
			{
				c.removeNode("General/UserID");
			}
			else
			{
				save(m_tfUID);
			}
		}
		// Server Monitoring
		else if (a_event.getSource() == m_tfMonitoringHost)
		{
			if (m_tfMonitoringHost.getText().equals("")) c.removeNode(m_tfMonitoringHost.getName());
			else save(m_tfMonitoringHost);
		}
		else if (a_event.getSource() == m_tfMonitoringPort)
		{
			if (m_tfMonitoringPort.getText().equals("")) c.removeNode(m_tfMonitoringPort.getName());
			else save(m_tfMonitoringPort);
		}		
		else
		{
			super.focusLost(a_event);
		}
	}

	public void load() throws IOException
	{
		super.load();
		setAutoSaveEnabled(false);
		MixConfiguration c = getConfiguration();

		// Go to an initial state
		m_tfFileName.setText("");
		m_rbNoLog.setSelected(true);
		//m_cbCompressLog.setSelected(false);
		
		String s = c.getValue("General/Logging/Console");
		if (s != null) m_rbConsole.setSelected((new Boolean(s)).booleanValue());
		
		s = c.getValue("General/Logging/Syslog");
		if (s != null) m_rbSyslog.setSelected((new Boolean(s)).booleanValue());
		
		s = c.getValue("General/Logging/EncryptedLog/File");
		if (s != null)
		{
			m_tfFileName.setText(s);
			s = c.getValue(m_logFilePath + "/"+ XML_ATTR_COMPRESSED);
			m_cbCompressLog.setSelected((new Boolean(s)).booleanValue());
			m_rbFile.setSelected(true);
			m_panelLogCert.setEnabled(true);
			String cert = c.getValue("General/Logging/EncryptedLog/KeyInfo/X509Data/X509Certificate/X509Certificate");
			byte b[] = null;
			if (cert != null && !cert.equals(""))
			{
				b = Base64.decode(cert);
			}
			m_panelLogCert.setCert(b);
		}
		
		s = c.getValue("General/Logging/File");
		if (s != null)
		{
			
			m_tfFileName.setText(s);
			s = c.getValue(m_logFilePath +"/"+ XML_ATTR_MAX_LOG_FILE_SIZE);
			
			if(s != null)
			{
				try
				{
					m_tfMaxLogFileSize.setInt((Integer.parseInt(s)/(1024*1024)));
				}
				catch(NumberFormatException nfe){}
			}
			
			s = c.getValue(m_logFilePath +"/"+ XML_ATTR_MAX_LOG_FILES);
			if(s != null)
			{
				try
				{
					m_tfMaxLogFiles.setInt(Integer.parseInt(s));
				}
				catch(NumberFormatException nfe){}
			}
			
			s = c.getValue(m_logFilePath + "/"+ XML_ATTR_COMPRESSED);
			m_cbCompressLog.setSelected( (new Boolean(s)).booleanValue());
			
			m_rbFile.setSelected(true);
		}
		
		setAutoSaveEnabled(true);
	}

	public String getHelpContext()
	{
		return JAPHelpContext.INDEX;
	}

	/**
	 * Check validity of the values on this panel
	 */
	public Vector<String> check()
	{
		Vector<String> errors = new Vector<String>();
		MixConfiguration c = getConfiguration();
		// Check if entry in path field is valid
		if (m_rbFile.isSelected())
		{
			String path = c.getValue(m_logFilePath);
			if (path == null ||  path.equals("") || path.indexOf(" ") != -1)
			{
				errors.addElement("Path to logging directory is invalid in " + getName() + " panel.");
			}
		}
		// Check if entry in user id field is valid
		String userId = c.getValue("General/UserID");
		if (userId != null && !userId.equals(""))
		{
			if (userId.indexOf(" ") != -1)
			{
				errors.addElement("User ID is invalid in " + getName() + " panel.");
			}
		}		
		// Check ServerMonitoring
		String sPort = c.getValue(m_tfMonitoringPort.getName());
		String sHost = c.getValue(m_tfMonitoringHost.getName());
		// If NOT both are empty --> check for inconsistencies
		if (!((sHost == null || sHost.equals("")) && (sPort == null || sPort.equals(""))))
		{
			if (sPort != null && sHost == null) 
				errors.add("You entered a port, but no host for Server Monitoring!");
			if (sPort == null && sHost != null) 
				errors.add("You entered a host, but no port for Server Monitoring!");
			if (sHost != null && (sHost.equals("") || sHost.indexOf(" ") != -1))
				errors.add("Server Monitoring host is invalid!");
		}
		
		if (m_cbPathVerification.isSelected() && !CertPanel.hasRootCertificates(c))
		{
			errors.add("The configuration contains no root certificates for automatic verification. Please reset the root certificates, or your mix will not connect.");
		}
		
		return errors;
	}

	public Container getHelpExtractionDisplayContext() 
	{
		return null;
	}
}
