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
import java.util.Hashtable;
import java.util.Vector;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import anon.crypto.PKCS12;
import anon.crypto.X509DistinguishedName;
import anon.crypto.X509Extensions;
import gui.JAPHelp;
import gui.JAPMessages;
import anon.crypto.JAPCertificate;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import logging.LogType;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	private static final String ENC_WITH = JAPMessages.getString("encWith");
	private static final String IMPORT = JAPMessages.getString("import");
	private static final String COMPRESS_LOG = JAPMessages.getString("compressLog");
	private static final String LOG_SYSLOG = JAPMessages.getString("logSyslog");

	private JTextField m_tfID, m_tfNumOfFileDes, m_tfFileName;
	private JCheckBox m_cbUserID, m_cbNrOfFileDes, m_cbDaemon, m_cbLogging,
		m_cbCompressLog;
	private JRadioButton m_rbConsole, m_rbFile, m_rbSyslog;
	private ButtonGroup m_loggingButtonGroup;
	private CertPanel m_certPanel;

	public AdvancedPanel()
	{
		super("Advanced");
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.anchor = c.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = c.NONE;

		TitledGridBagPanel loggingPanel = new TitledGridBagPanel("Logging");
		this.add(loggingPanel, c);

		m_certPanel = new CertPanel("Encryption certificate",
									"This is the certificate your Mix will use to encrypt the log file",
									(JAPCertificate)null);
		m_certPanel.setName("General/Logging/EncryptedLog/KeyInfo/X509Data/X509Certificate");
		m_certPanel.addChangeListener(this);
		m_certPanel.setEnabled(false);
		c.gridx++;
		c.weightx = 1;
		this.add(m_certPanel, c);

		c.gridy++;
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
		miscPanel.addRow(m_cbNrOfFileDes, m_tfNumOfFileDes);

		// Daemon
		m_cbDaemon = new JCheckBox(RUN_DAEMON);
		m_cbDaemon.setName("General/Daemon");
		m_cbDaemon.addItemListener(this);
		miscPanel.addRow(m_cbDaemon, null);

		// Logging
		m_cbLogging = new JCheckBox(ENABLE_LOGGING);
		m_cbLogging.addItemListener(this);
		loggingPanel.addRow(m_cbLogging, null);

		// Console Logging
		m_rbConsole = new JRadioButton(LOG_CONSOLE);
		m_rbConsole.setName("General/Logging/Console");
		m_rbConsole.setModel(new ToggleButtonModel());
		m_rbConsole.setSelected(true);
		m_rbConsole.addItemListener(this);
		loggingPanel.addRow(m_rbConsole, null);

		// Log to Directory JRadioButton
		m_rbFile = new JRadioButton(LOG_DIR);
		m_rbFile.setModel(new ToggleButtonModel());
		m_rbFile.setName("General/Logging/File");
		m_rbFile.addItemListener(this);

		// Log to Directory JTextField
		m_tfFileName = new JTextField(20);
		m_tfFileName.setName("General/Logging/File");
		m_tfFileName.addFocusListener(this);
		loggingPanel.addRow(m_rbFile, m_tfFileName);

		// Compress Log JCheckBox
		m_cbCompressLog = new JCheckBox(COMPRESS_LOG);
		m_cbCompressLog.addItemListener(this);
		m_cbCompressLog.setName("General/Logging/File/compress");
		loggingPanel.addRow(m_cbCompressLog, null);

		// Syslog JRadioButton
		m_rbSyslog = new JRadioButton(LOG_SYSLOG);
		m_rbSyslog.setName("General/Logging/Syslog");
		m_rbSyslog.setModel(new ToggleButtonModel());
		m_rbSyslog.addItemListener(this);
		loggingPanel.addRow(m_rbSyslog, null);

		m_loggingButtonGroup = new ButtonGroup();
		m_loggingButtonGroup.add(m_rbConsole);
		m_loggingButtonGroup.add(m_rbFile);
		m_loggingButtonGroup.add(m_rbSyslog);

		m_certPanel.setPreferredSize(new Dimension( (int) m_certPanel.getPreferredSize().width,
			(int) loggingPanel.getPreferredSize().height));

		setAutoSaveEnabled(false);
		itemStateChanged(new ItemEvent(m_cbDaemon, 0, (Object)null, 0));
		setAutoSaveEnabled(true);
	}

	/**
	 *
	 * @return Possible error and warning messages
	 */
	public Vector check()
	{
		return new Vector();
	}

	public void itemStateChanged(ItemEvent a_event)
	{
		if (a_event.getSource() == m_cbLogging && isAutoSaveEnabled())
		{
			save(m_loggingButtonGroup);
		}

		super.itemStateChanged(a_event);
	}

	public void paint(Graphics g)
	{
		super.paint(g);
		JAPHelp.getInstance().getContextObj().setContext("index");
	}

	/**
	 * Enables used and disables unused components.
	 *
	 */
	protected void enableComponents()
	{
		if (m_certPanel.getCert() != null)
		{
			m_certPanel.setEnabled(true);
		}

		boolean log = m_cbLogging.isSelected();
		// FIXME: m_rbFile.isSelected() always returns false here, even if m_rbFile is selected.
		// Find out why.
		boolean file = m_rbFile.isSelected();
		// Workaround: Assume m_rbFile is selected when no radio button returns selected == true
		file = file || (!file && !m_rbSyslog.isSelected() && !m_rbConsole.isSelected());
		boolean daemon = m_cbDaemon.isSelected();

		// enable/disable some controls
		m_rbConsole.setEnabled(log && !daemon);
		m_rbFile.setEnabled(log);
		m_rbSyslog.setEnabled(log);
		m_tfFileName.setEnabled(log && file);
		m_cbCompressLog.setEnabled(log && file);
		/*m_bttnImportEncKey.setEnabled(log && file);
		   m_lbEncrypt.setEnabled(log && file);
		   m_tfLogEncryptKeyName.setEnabled(log && file);*/
		// if mix is run as daemon, we can't log to console so set log output to file.
		if (log && !file)
		{
			// A bug in JDK 1.1.8 causes an infinite event loop here, therefore
			// event casting must be disabled
			m_rbFile.removeItemListener(this);
			m_rbFile.setSelected(m_rbConsole.isSelected() && !m_rbConsole.isEnabled());
			m_rbFile.addItemListener(this);
		}

		m_tfID.setEnabled(m_cbUserID.isSelected());
		m_tfNumOfFileDes.setEnabled(m_cbNrOfFileDes.isSelected());
	}

	protected void save(JCheckBox a)
	{
		if (a == this.m_cbLogging || a == this.m_cbCompressLog)
		{
			save(m_loggingButtonGroup);
		}
		else
		{
			super.save(a);
		}
	}

	protected void save(ButtonGroup a)
	{
		if (a == m_loggingButtonGroup)
		{
			MixConfiguration mixConf = getConfiguration();
			boolean log = this.m_cbLogging.isSelected();

			if (!log)
			{
				mixConf.removeNode(m_rbSyslog.getName());
				mixConf.removeNode(m_rbConsole.getName());
				mixConf.removeNode(m_tfFileName.getName());
				mixConf.removeNode("General/Logging/EncryptedLog");
				m_certPanel.setEnabled(false);
				m_certPanel.removeCert();
				return;
			}

			mixConf.setValue("General/Logging/Console", m_rbConsole.isSelected());
			mixConf.setValue("General/Logging/Syslog", m_rbSyslog.isSelected());

			if (m_rbFile.isSelected())
			{
				String fn = m_tfFileName.getText();
				m_certPanel.setEnabled(true);
				if (fn.equals(""))
				{
					fn = null;
				}

				if (m_certPanel.getCert() != null)
				{
					mixConf.setValue("General/Logging/File", (String)null);
					mixConf.setValue("General/Logging/EncryptedLog/File", fn);
				}
				else
				{
					mixConf.setValue("General/Logging/File", fn);
					mixConf.setAttribute("General/Logging/File", "compressed",
										 m_cbCompressLog.isSelected());
				}
			}
			else
			{
				m_certPanel.setEnabled(false);
				m_certPanel.removeCert();
			}
		}
	}

	/** Loads all values from the MixConfiguration object. This method overrides
	 * mixconfig.MixConfigPanel.load() to take care of the dependencies between
	 * attributes that are specific for this part of the configuration.
	 * @throws IOException If loading an attribute fails
	 */
	public void load() throws IOException
	{
		super.load();

		// disable automatic saving of values to the MixConfiguration
		// to prevent infinite event loops
		setAutoSaveEnabled(false);

		boolean b;
		String s;

		b = m_tfNumOfFileDes.getText().equals("");
		this.m_cbNrOfFileDes.setSelected(!b);

		b = this.m_tfID.getText().equals("");
		this.m_cbUserID.setSelected(!b);

		b = this.m_tfFileName.getText().equals("");
		this.m_rbFile.setSelected(!b);

		b = false;

		s = getConfiguration().getValue("General/Logging/File");
		b = b || (s != null);

		s = getConfiguration().getValue("General/Logging/Console");
		b = b || new Boolean(s).booleanValue();

		s = getConfiguration().getValue("General/Logging/Syslog");
		b = b || new Boolean(s).booleanValue();

		m_cbLogging.setSelected(b);

		// turn on auto saving again
		setAutoSaveEnabled(true);
		enableComponents();
	}

	public void stateChanged(ChangeEvent a_e)
	{
		try
		{
			if (a_e.getSource() instanceof CertPanel)
			{
				save( (CertPanel) a_e.getSource());
				enableComponents();
			}
		}
		catch (Exception e)
		{
			MixConfig.handleError(e, null, LogType.GUI);
		}
	}

}
