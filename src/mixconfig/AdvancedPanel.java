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

/**
 * The panel for advanced settings
 * @author Tobias Bayer
 */
public class AdvancedPanel extends MixConfigPanel implements ChangeListener
{
	private static final String SET_UID = JAPMessages.getString("setUID");
	private static final String SET_FD = JAPMessages.getString("setFD");
	private static final String RUN_DAEMON = JAPMessages.getString("runDaemon");
	private static final String ENABLE_LOGGING = JAPMessages.getString("enableLogging");
	private static final String LOG_CONSOLE = JAPMessages.getString("logConsole");
	private static final String LOG_DIR = JAPMessages.getString("logDir");
	private static final String COMPRESS_LOG = JAPMessages.getString("compressLog");
	private static final String LOG_SYSLOG = JAPMessages.getString("logSyslog");

	private JTextField m_tfID, m_tfNumOfFileDes, m_tfFileName;
	private JCheckBox m_cbUserID, m_cbNrOfFileDes, m_cbDaemon, m_cbCompressLog;
	private JRadioButton m_rbConsole, m_rbFile, m_rbSyslog, m_rbNoLog;
	private ButtonGroup m_loggingButtonGroup;
	private CertPanel m_certPanel;

	private String m_logFilePath = "General/Logging/File";

	public AdvancedPanel()
	{
		super("Advanced");
		setAutoSaveEnabled(false);
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.anchor = c.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = c.NONE;
		c.gridheight = 2;
		TitledGridBagPanel loggingPanel = new TitledGridBagPanel("Logging");
		this.add(loggingPanel, c);

		m_certPanel = new CertPanel("Encrypted log certificate",
									"This is the certificate your Mix will use to encrypt the log file",
									(JAPCertificate)null);
		//m_certPanel.setName("General/Logging/EncryptedLog/KeyInfo/X509Data/X509Certificate");
		m_certPanel.addChangeListener(this);
		m_certPanel.setEnabled(false);
		c.gridx++;
		c.weightx = 1;
		c.gridheight = 1;
		this.add(m_certPanel, c);

		c.gridy += 2;
		c.gridx--;
		c.weighty = 1;
		c.weightx = 0;
		c.fill = c.HORIZONTAL;
		TitledGridBagPanel miscPanel = new TitledGridBagPanel("Miscellaneous");
		this.add(miscPanel, c);
		c.fill = c.NONE;

		//User ID
		m_cbUserID = new JCheckBox(SET_UID);
		m_cbUserID.addItemListener(this);
		m_tfID = new JTextField(10);
		m_tfID.setName("General/UserID");
		m_tfID.addFocusListener(this);
		miscPanel.addRow(m_cbUserID, m_tfID);
		m_tfID.setEnabled(false);

		//File Descriptors
		m_cbNrOfFileDes = new JCheckBox(SET_FD);
		m_cbNrOfFileDes.addItemListener(this);
		m_tfNumOfFileDes = new JTextField(10);
		m_tfNumOfFileDes.setName("General/NrOfFileDescriptors");
		m_tfNumOfFileDes.addFocusListener(this);
		m_tfNumOfFileDes.setEnabled(false);
		miscPanel.addRow(m_cbNrOfFileDes, m_tfNumOfFileDes);

		// Daemon
		m_cbDaemon = new JCheckBox(RUN_DAEMON);
		m_cbDaemon.setName("General/Daemon");
		m_cbDaemon.addItemListener(this);
		miscPanel.addRow(m_cbDaemon, null);

		// Logging
		m_rbNoLog = new JRadioButton(ENABLE_LOGGING);
		m_rbNoLog.setSelected(true);
		m_rbNoLog.setModel(new ToggleButtonModel());
		m_rbNoLog.addItemListener(this);
		loggingPanel.addRow(m_rbNoLog, null);

		// Console Logging
		m_rbConsole = new JRadioButton(LOG_CONSOLE);
		m_rbConsole.setModel(new ToggleButtonModel());
		m_rbConsole.addItemListener(this);
		loggingPanel.addRow(m_rbConsole, null);

		// Log to Directory JRadioButton
		m_rbFile = new JRadioButton(LOG_DIR);
		m_rbFile.setModel(new ToggleButtonModel());
		m_rbFile.addItemListener(this);

		// Log to Directory JTextField
		m_tfFileName = new JTextField(15);
		m_tfFileName.addFocusListener(this);
		m_tfFileName.setEnabled(false);
		loggingPanel.addRow(m_rbFile, m_tfFileName);

		// Compress Log JCheckBox
		m_cbCompressLog = new JCheckBox(COMPRESS_LOG);
		m_cbCompressLog.addItemListener(this);
		m_cbCompressLog.setEnabled(false);
		loggingPanel.addRow(null, m_cbCompressLog);

		// Syslog JRadioButton
		m_rbSyslog = new JRadioButton(LOG_SYSLOG);
		m_rbSyslog.setModel(new ToggleButtonModel());
		m_rbSyslog.addItemListener(this);
		loggingPanel.addRow(m_rbSyslog, null);

		m_loggingButtonGroup = new ButtonGroup();
		m_loggingButtonGroup.add(m_rbNoLog);
		m_loggingButtonGroup.add(m_rbConsole);
		m_loggingButtonGroup.add(m_rbFile);
		m_loggingButtonGroup.add(m_rbSyslog);

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
		else if (s == m_cbNrOfFileDes)
		{
			m_tfNumOfFileDes.setEnabled(m_cbNrOfFileDes.isSelected());
		}
		else if (s == m_cbUserID)
		{
			m_tfID.setEnabled(m_cbUserID.isSelected());
		}
		else
		{
			super.itemStateChanged(a_event);
		}
	}

	public void focusLost(FocusEvent a_event)
	{
		if (a_event.getSource() == m_tfFileName)
		{
			MixConfiguration c = getConfiguration();
			c.removeNode("General/Logging/EncryptedLog/File");
			//c.removeNode("General/Logging/EncryptedLog");
			c.removeNode("General/Logging/File");
			c.setAttribute(m_logFilePath, "compressed", m_cbCompressLog.isSelected());
			c.setValue(m_logFilePath, m_tfFileName.getText());
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

	public Vector check()
	{
		return new Vector();
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
