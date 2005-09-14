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

import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import anon.crypto.JAPCertificate;
import logging.LogType;
import java.io.IOException;
import anon.util.Base64;
import gui.JAPMessages;
import gui.JAPHelp;
import java.awt.Graphics;

/**
 * The panel for advanced settings
 * @author Tobias Bayer
 */
public class AdvancedPanel extends MixConfigPanel implements ActionListener
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

	private byte[] m_certLogEncKey = null;

	private JLabel m_lbEncrypt;
	private JTextField m_tfID, m_tfNumOfFileDes, m_tfFileName,
		m_tfLogEncryptKeyName;
	private JButton  m_bttnImportEncKey;
	private JCheckBox m_cbUserID, m_cbNrOfFileDes, m_cbDaemon, m_cbLogging,
		m_cbCompressLog;
	private JRadioButton m_rbConsole, m_rbFile, m_rbSyslog;
	private ButtonGroup m_loggingButtonGroup;

	public AdvancedPanel()
	{
		super("Advanced");
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.anchor = c.NORTHWEST;

		// User ID JCheckBox
		m_cbUserID = new JCheckBox(SET_UID);
		m_cbUserID.addItemListener(this);
		c.gridy++;
		c.gridx = 0;
		this.add(m_cbUserID, c);

		// User ID JTextField
		m_tfID = new JTextField(20);
		m_tfID.setName("General/UserID");
		m_tfID.addFocusListener(this);
		c.weightx = 0.2;
		c.gridx = 1;
		c.fill = c.NONE;
		this.add(m_tfID, c);
		m_tfID.setEnabled(false);

		// File Descriptors JCheckBox
		m_cbNrOfFileDes = new JCheckBox(SET_FD);
		m_cbNrOfFileDes.addItemListener(this);
		c.weightx = 0;
		c.gridx = 0;
		c.gridy++;
		c.fill = c.NONE;
		this.add(m_cbNrOfFileDes, c);

		// File Descriptors JTextField
		m_tfNumOfFileDes = new JTextField(20);
		m_tfNumOfFileDes.setName("General/NrOfFileDescriptors");
		m_tfNumOfFileDes.addFocusListener(this);
		c.weightx = 0.2;
		c.gridx = 1;
		this.add(m_tfNumOfFileDes, c);

		// Daemon JCheckBox
		m_cbDaemon = new JCheckBox(RUN_DAEMON);
		m_cbDaemon.setName("General/Daemon");
		m_cbDaemon.addItemListener(this);
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0;
		c.fill = c.NONE;
		this.add(m_cbDaemon, c);

		// Logging JCheckBox
		m_cbLogging = new JCheckBox(ENABLE_LOGGING);
		m_cbLogging.addItemListener(this);
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0;
		this.add(m_cbLogging, c);

		// Console Logging JCheckBox
		m_rbConsole = new JRadioButton(LOG_CONSOLE);
		m_rbConsole.setName("General/Logging/Console");
		m_rbConsole.setModel(new ToggleButtonModel());
		m_rbConsole.setSelected(true);
		m_rbConsole.addItemListener(this);
		c.gridx = 0;
		c.gridy++;
		c.weightx = 0;
		this.add(m_rbConsole, c);

		// Log to Directory JRadioButton
		m_rbFile = new JRadioButton(LOG_DIR);
		m_rbFile.setModel(new ToggleButtonModel());
		m_rbFile.setName("General/Logging/File");
		m_rbFile.addItemListener(this);
		c.weightx = 0;
		c.gridy++;
		this.add(m_rbFile, c);

		// Log to Directory JTextField
		m_tfFileName = new JTextField(20);
		m_tfFileName.setName("General/Logging/File");
		m_tfFileName.addFocusListener(this);
		c.gridx = 1;
		c.weightx = 1;
		c.gridwidth = 1;
		c.fill = c.NONE;
		this.add(m_tfFileName, c);

		// Encrypt Log JLabel
		m_lbEncrypt = new JLabel(ENC_WITH);
		m_lbEncrypt.setEnabled(false);
		c.insets.left += 20;
		c.gridx = 0;
		c.gridy++;
		c.weightx = 0;
		c.gridwidth = 1;
		c.fill = c.NONE;
		this.add(m_lbEncrypt, c);

		// Encrypt Log Key JTextField
		m_tfLogEncryptKeyName = new JTextField(20);
		m_tfLogEncryptKeyName.setName("General/Logging/EncryptedLog/" +
									  "KeyInfo/X509Data/X509Certificate");
		m_tfLogEncryptKeyName.addFocusListener(this);
		c.insets.left -= 20;
		c.weightx = 1;
		c.gridx = 1;
		c.fill = c.NONE;
		this.add(m_tfLogEncryptKeyName, c);

		// Encrypt Log Key JButton
		m_bttnImportEncKey = new JButton(IMPORT);
		m_bttnImportEncKey.setEnabled(false);
		m_bttnImportEncKey.addActionListener(this);
		c.weightx = 1;
		c.gridx = 1;
		c.gridy++;
		c.fill = c.NONE;
		this.add(m_bttnImportEncKey, c);

		// Compress Log JCheckBox
		m_cbCompressLog = new JCheckBox(COMPRESS_LOG);
		m_cbCompressLog.addItemListener(this);
		m_cbCompressLog.setName("General/Logging/File/compress");
		c.insets.left += 20;
		c.gridx = 0;
		c.gridy++;
		c.weightx = 0;
		this.add(m_cbCompressLog, c);

		// Syslog JRadioButton
		m_rbSyslog = new JRadioButton(LOG_SYSLOG);
		m_rbSyslog.setName("General/Logging/Syslog");
		m_rbSyslog.setModel(new ToggleButtonModel());
		m_rbSyslog.addItemListener(this);
		c.insets.left -= 20;
		c.gridx = 0;
		c.gridy++;
		c.weightx = 0;
		c.weighty = 1;
		this.add(m_rbSyslog, c);

		m_loggingButtonGroup = new ButtonGroup();
		m_loggingButtonGroup.add(m_rbConsole);
		m_loggingButtonGroup.add(m_rbFile);
		m_loggingButtonGroup.add(m_rbSyslog);

		setAutoSaveEnabled(false);
		itemStateChanged(new ItemEvent(m_cbDaemon, 0, (Object)null, 0));
		setAutoSaveEnabled(true);
	}

	/**
	 *
	 * @return Possible error and warning messages
	 * @todo Implement this mixconfig.MixConfigPanel method
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
	 * @todo Implement this mixconfig.MixConfigPanel method
	 */
	protected void enableComponents()
	{
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
		m_bttnImportEncKey.setEnabled(log && file);
		m_lbEncrypt.setEnabled(log && file);
		m_tfLogEncryptKeyName.setEnabled(log && file);
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

	protected void save(JTextField a)
	{
		if (a == this.m_tfLogEncryptKeyName)
		{
			if (a.isEnabled())
			{
				getConfiguration().setValue(a.getName(), this.m_certLogEncKey);
			}
			else
			{
				getConfiguration().removeNode(a.getName());
			}
		}
		else
		{
			super.save(a);
		}
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
				return;
			}

			mixConf.setValue("General/Logging/Console", m_rbConsole.isSelected());
			mixConf.setValue("General/Logging/Syslog", m_rbSyslog.isSelected());

			if (m_rbFile.isSelected())
			{
				String fn = m_tfFileName.getText();
				if (fn.equals(""))
				{
					fn = null;
				}

				if (this.m_certLogEncKey != null)
				{
					mixConf.setValue("General/Logging/File", (String)null);
					mixConf.setValue("General/Logging/EncryptedLog/File", fn);
					mixConf.setValue("General/Logging/EncryptedLog/" +
										 "KeyInfo/X509Data/X509Certificate",
										 m_certLogEncKey);
				}
				else
				{
					mixConf.setValue("General/Logging/File", fn);
					mixConf.setAttribute("General/Logging/File", "compressed",
										 m_cbCompressLog.isSelected());
				}
			}
		}
	}

	protected void load(JTextField a)
	{
		if (a == m_tfLogEncryptKeyName)
		{
			String value = getConfiguration().getValue(a.getName());
			if (value != null)
			{
				byte b[] = Base64.decode(value);
				if (b != null)
				{
					this.setEncKeyForLog(b);
				}
				else
				{
					JOptionPane.showMessageDialog(MixConfig.getMainWindow(),
												  "Could not decode MixConfiguration/" +
												  a.getName(),
												  "Warning", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else
		{
			super.load(a);
		}
	}

	public void actionPerformed(ActionEvent a_e)
	{
		if (a_e.getSource() == m_bttnImportEncKey)
		{
			this.importEncKeyForLog();
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

	private void importEncKeyForLog()
	{
		byte[] cert;
		try
		{
			cert = MixConfig.openFile(MixConfig.FILTER_CER | MixConfig.FILTER_B64_CER);
		}
		catch (Exception e)
		{
			ClipFrame Open =
				new ClipFrame(
					"Open of file failed. Paste a certificate to be imported " +
					"in the area provided.",
					true);
			Open.show();
			cert = Open.getText().getBytes();
		}
		setEncKeyForLog(cert);
	}

	public void setEncKeyForLog(byte[] cert)
	{
		try
		{
			if (cert != null)
			{
				JAPCertificate cert1 = JAPCertificate.getInstance(cert);
				m_tfLogEncryptKeyName.setText(cert1.getSubject().toString());
				m_certLogEncKey = cert1.toByteArray();
			}
			else
			{
				m_tfLogEncryptKeyName.setText(null);
				m_certLogEncKey = null;
			}
		}
		catch (Exception e)
		{
			MixConfig.handleError(e, "Previous certificate not set.", LogType.MISC);
			setEncKeyForLog(null);
		}
	}

}
