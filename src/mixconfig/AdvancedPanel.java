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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import gui.JAPMessages;
import anon.crypto.JAPCertificate;
import logging.LogType;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.FocusEvent;
import anon.util.Base64;
import javax.swing.JLabel;
import gui.JAPJIntField;
import gui.*;

/**
 * The panel for advanced settings
 * @author Tobias Bayer
 */
public class AdvancedPanel extends MixConfigPanel implements ChangeListener
{
	private static final String XMLPATH_TRAFFIC_SHAPING = "Ressources";
	private static final String XMLPATH_TRAFFIC_SHAPING_LATENCY = XMLPATH_TRAFFIC_SHAPING + "/Latency";
	private static final String XMLPATH_TRAFFIC_SHAPING_INTERVAL = XMLPATH_TRAFFIC_SHAPING + "/Intervall";

	private static final String MSG_TRAFFIC_SHAPING = AdvancedPanel.class.getName() + "_TS";
	private static final String MSG_LATENCY = AdvancedPanel.class.getName() + "_TS_latency";
	private static final String MSG_INTERVAL = AdvancedPanel.class.getName() + "_TS_interval";

	private static final String MSG_SET_UID = AdvancedPanel.class.getName() + "_setUID";
	private static final String MSG_SET_FD = AdvancedPanel.class.getName() + "_setFD";
	private static final String MSG_RUN_DAEMON = AdvancedPanel.class.getName() + "_runDaemon";
	private static final String MSG_ENABLE_LOGGING = AdvancedPanel.class.getName() + "_enableLogging";
	private static final String MSG_LOG_CONSOLE = AdvancedPanel.class.getName() + "_logConsole";
	private static final String MSG_LOG_DIR = AdvancedPanel.class.getName() + "_logDir";
	private static final String MSG_COMPRESS_LOG = AdvancedPanel.class.getName() + "_compressLog";
	private static final String MSG_LOG_SYSLOG = AdvancedPanel.class.getName() + "_logSyslog";

	private JTextField m_tfID, m_tfNumOfFileDes, m_tfFileName, m_tfLatency, m_tfInterval;
	private JCheckBox m_cbDaemon, m_cbCompressLog;
	private JRadioButton m_rbConsole, m_rbFile, m_rbSyslog, m_rbNoLog;
	private ButtonGroup m_loggingButtonGroup;
	private CertPanel m_certPanel;
	private TitledGridBagPanel m_pnlTrafficShaping;

	private String m_logFilePath = "General/Logging/File";

	public AdvancedPanel()
	{
		super("Advanced");
		setAutoSaveEnabled(false);
		this.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = getDefaultInsets();
		constraints.anchor = constraints.NORTHWEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.fill = constraints.HORIZONTAL;
		constraints.gridheight = 2;
		TitledGridBagPanel loggingPanel = new TitledGridBagPanel("Logging");
		this.add(loggingPanel, constraints);

		m_certPanel = new CertPanel("Encrypted log certificate",
									"This is the certificate your Mix will use to encrypt the log file",
									(JAPCertificate)null, CertPanel.CERT_ALGORITHM_RSA);
		m_certPanel.addChangeListener(this);
		m_certPanel.setEnabled(false);
		constraints.gridx++;
		constraints.gridheight = 1;
		this.add(m_certPanel, constraints);

		constraints.gridy += 2;
		constraints.gridx--;
		constraints.weighty = 1;

		m_pnlTrafficShaping = new TitledGridBagPanel(JAPMessages.getString(MSG_TRAFFIC_SHAPING));
		add(m_pnlTrafficShaping, constraints);

		//Latency
		m_tfLatency = new JAPJIntField(
			new JAPJIntField.IntFieldWithoutZeroBounds(JAPJIntField.NO_MAXIMUM_BOUND));
		m_tfLatency.setName(XMLPATH_TRAFFIC_SHAPING_LATENCY);
		m_tfLatency.addFocusListener(this);
		m_pnlTrafficShaping.addRow(new JLabel(JAPMessages.getString(MSG_LATENCY) + " (ms)"), m_tfLatency);

		//Interval
		m_tfInterval = new JAPJIntField(
			new JAPJIntField.IntFieldWithoutZeroBounds(JAPJIntField.NO_MAXIMUM_BOUND));
		m_tfInterval.setName(XMLPATH_TRAFFIC_SHAPING_INTERVAL);
		m_tfInterval.addFocusListener(this);
		m_pnlTrafficShaping.addRow(new JLabel(JAPMessages.getString(MSG_INTERVAL) + " (ms)"), m_tfInterval);

		constraints.gridx++;
		TitledGridBagPanel miscPanel = new TitledGridBagPanel("Miscellaneous");
		this.add(miscPanel, constraints);

		//User ID
		m_tfID = new JTextField(10);
		m_tfID.setName("General/UserID");
		m_tfID.addFocusListener(this);
		miscPanel.addRow(new JLabel(JAPMessages.getString(MSG_SET_UID)), m_tfID);

		//File Descriptors
		m_tfNumOfFileDes = new JAPJIntField(
			new JAPJIntField.IntFieldWithoutZeroBounds(JAPJIntField.NO_MAXIMUM_BOUND));
		m_tfNumOfFileDes.setName("General/NrOfFileDescriptors");
		m_tfNumOfFileDes.addFocusListener(this);
		miscPanel.addRow(new JLabel(JAPMessages.getString(MSG_SET_FD)), m_tfNumOfFileDes);

		// Daemon
		m_cbDaemon = new JCheckBox(JAPMessages.getString(MSG_RUN_DAEMON));
		m_cbDaemon.setName("General/Daemon");
		m_cbDaemon.addItemListener(this);
		miscPanel.addRow(m_cbDaemon, null);

		// Logging
		m_rbNoLog = new JRadioButton(JAPMessages.getString(MSG_ENABLE_LOGGING));
		m_rbNoLog.setSelected(true);
		m_rbNoLog.setModel(new ToggleButtonModel());
		m_rbNoLog.addItemListener(this);
		loggingPanel.addRow(m_rbNoLog, null);

		// Console Logging
		m_rbConsole = new JRadioButton(JAPMessages.getString(MSG_LOG_CONSOLE));
		m_rbConsole.setModel(new ToggleButtonModel());
		m_rbConsole.addItemListener(this);
		loggingPanel.addRow(m_rbConsole, null);

		// Log to Directory JRadioButton
		m_rbFile = new JRadioButton(JAPMessages.getString(MSG_LOG_DIR));
		m_rbFile.setModel(new ToggleButtonModel());
		m_rbFile.addItemListener(this);

		// Log to Directory JTextField
		m_tfFileName = new JTextField();
		m_tfFileName.addFocusListener(this);
		m_tfFileName.setEnabled(false);
		loggingPanel.addRow(m_rbFile, m_tfFileName);

		// Compress Log JCheckBox
		m_cbCompressLog = new JCheckBox(JAPMessages.getString(MSG_COMPRESS_LOG));
		m_cbCompressLog.addItemListener(this);
		m_cbCompressLog.setEnabled(false);
		loggingPanel.addRow(null, m_cbCompressLog);

		// Syslog JRadioButton
		m_rbSyslog = new JRadioButton(JAPMessages.getString(MSG_LOG_SYSLOG));
		m_rbSyslog.setModel(new ToggleButtonModel());
		m_rbSyslog.addItemListener(this);
		loggingPanel.addRow(m_rbSyslog, null);

		m_loggingButtonGroup = new ButtonGroup();
		m_loggingButtonGroup.add(m_rbNoLog);
		m_loggingButtonGroup.add(m_rbConsole);
		m_loggingButtonGroup.add(m_rbFile);
		m_loggingButtonGroup.add(m_rbSyslog);

		//Keep the panels in place
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
			m_cbCompressLog.setEnabled(m_rbFile.isSelected());
			m_certPanel.setEnabled(m_rbFile.isSelected());
			if (!m_rbFile.isSelected())
			{
				m_certPanel.removeCert();
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
			focusLost(new FocusEvent(m_tfFileName, 0));
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

	public void focusLost(FocusEvent a_event)
	{
		MixConfiguration c = getConfiguration();

		if (a_event.getSource() == m_tfFileName)
		{
			c.removeNode("General/Logging/EncryptedLog/File");
			c.removeNode("General/Logging/File");
			c.setAttribute(m_logFilePath, "compressed", m_cbCompressLog.isSelected());
			c.setValue(m_logFilePath, m_tfFileName.getText());
		}
		else if (a_event.getSource() == m_tfNumOfFileDes)
		{
			if (m_tfNumOfFileDes.getText().equals(""))
			{
				c.removeNode("General/NrOfFileDescriptors");
			}
			else
			{
				save(m_tfNumOfFileDes);
			}
		}
		else if (a_event.getSource() == m_tfID)
		{
			if (m_tfID.getText().equals(""))
			{
				c.removeNode("General/UserID");
			}
			else
			{
				save(m_tfID);
			}
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

		String s = c.getValue("General/Logging/Console");
		if (s != null)
		{
			m_rbConsole.setSelected( (new Boolean(s)).booleanValue());
		}
		s = c.getValue("General/Logging/Syslog");
		if (s != null)
		{
			m_rbSyslog.setSelected( (new Boolean(s)).booleanValue());
		}
		s = c.getValue("General/Logging/EncryptedLog/File");
		if (s != null)
		{
			m_tfFileName.setText(s);
			s = c.getValue(m_logFilePath + "/compressed");
			m_cbCompressLog.setSelected( (new Boolean(s)).booleanValue());
			m_rbFile.setSelected(true);
			m_certPanel.setEnabled(true);
			String cert = c.getValue(
				"General/Logging/EncryptedLog/KeyInfo/X509Data/X509Certificate/X509Certificate");

			byte b[] = null;

			if (cert != null && !cert.equals(""))
			{
				b = Base64.decode(cert);
			}

			m_certPanel.setCert(b);

		}
		s = c.getValue("General/Logging/File");
		if (s != null)
		{
			m_tfFileName.setText(s);
			s = c.getValue(m_logFilePath + "/compressed");
			m_cbCompressLog.setSelected( (new Boolean(s)).booleanValue());
			m_rbFile.setSelected(true);
		}

		if (!m_rbConsole.isSelected() && !m_rbSyslog.isSelected() && !m_rbFile.isSelected())
		{
			m_rbNoLog.setSelected(true);
		}
		setAutoSaveEnabled(true);
	}

	public String getHelpContext()
	{
		return JAPHelpContext.INDEX;
	}

	public Vector check()
	{
		Vector errors = new Vector();
		//Check if entry in path field is valid
		if (m_rbFile.isSelected())
		{
			String path = getConfiguration().getValue(m_logFilePath);
			if (path == null ||  path.equals("") || path.indexOf(" ") != -1)
			{
				errors.addElement("Path to logging directory is invalid in " + getName() + " panel.");
			}
		}
		//Check if entry in user id field is valid
		String userId = getConfiguration().getValue("General/UserID");
		if (userId != null && !userId.equals(""))
		{
			if (userId.indexOf(" ") != -1)
			{
				errors.addElement("User ID is invalid in " + getName() + " panel.");
			}
		}

		return errors;
	}

	public void stateChanged(ChangeEvent a_event)
	{
		if (a_event.getSource() == m_certPanel)
		{
			if (m_certPanel.getCert() != null)
			{
				m_logFilePath = "General/Logging/EncryptedLog/File";

				if (isAutoSaveEnabled())
				{
					getConfiguration().setValue(
						"General/Logging/EncryptedLog/KeyInfo/X509Data/X509Certificate/X509Certificate",
						m_certPanel.getCert().getX509Certificate().toByteArray());
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

	protected void enableComponents()
	{
	}

}
