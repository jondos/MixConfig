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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;

import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.w3c.dom.Attr;
import anon.util.Base64;
import anon.crypto.JAPCertificate;
import mixconfig.wizard.ConfigWizardPanel;
import javax.swing.JTabbedPane;

public class GeneralPanel extends MixConfigPanel implements ActionListener
{
	final static String idChars =
		"abcdefghijklmnopqrstuvwxyz0123456789.-_";

	private JComboBox m_comboboxMixType;
	private JTextField m_tfMixName, /*m_tfCascadeName,*/ m_tfMixID, m_tfFileName, m_tfID, m_tfNumOfFiles,
		m_tfLogEncryptKeyName, m_tfCascadeLength;
	private JCheckBox m_checkboxDaemon, m_checkboxLogging, m_checkboxUserID, m_checkboxNrOfFileDes,
		m_compressLog, m_checkboxCascadeLength;
	private JRadioButton m_rbConsole, m_rbFile, m_rbSyslog;
	private JRadioButton m_first_yes, m_first_no, m_middle_yes, m_middle_no, m_last_yes, m_last_no; //byWP
	private JLabel m_labelEnrypt, m_lbMixID;
	private ButtonGroup m_loggingButtonGroup;
	private ButtonGroup m_firstMixButtonGroup, m_middleMixButtonGroup, m_lastMixButtonGroup; //byWP
	private JButton m_bttnImportEncKey, m_bttnGenMixID;
	private byte[] m_certLogEncKey = null;
	private JPanel m_panelStrut = new JPanel();
	private JPanel m_panelAdvanced = new JPanel();

	/**
	 * Constructs a panel with controls for general Mix settings.
	 */
	public GeneralPanel()
	{
		GridBagLayout l = new GridBagLayout();
		setLayout(l);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 0;
		gbc.weightx = 1;


		//Head  //byWP
		GridLayout layout0 = new GridLayout(1,3);
		//GridBagConstraints c_layout0 = new GridBagConstraints();
		//c_layout0.gridwidth = GridBagConstraints.REMAINDER;
		//c_layout0.anchor = GridBagConstraints.EAST;

		JPanel head = new JPanel(layout0);
		head.setBorder(new EtchedBorder());
		JLabel text1 = new JLabel("Specify Config for your Mix");
		JLabel text2 = new JLabel("Step 1/");

		//layout0.setConstraints(text1, c_layout0);
		head.add(text1);
		head.add( new JLabel("") );
		head.add(text2);

		//The Label head should stand on the left side (=WEST)
		GridBagConstraints ccc = new GridBagConstraints();
		ccc.gridwidth = GridBagConstraints.REMAINDER;
		ccc.anchor = GridBagConstraints.CENTER;
		ccc.insets = new Insets(10, 10, 10, 25);
		l.setConstraints(head, ccc);
		add(head);



		GridBagLayout layout = new GridBagLayout();
		GridBagLayout layout2 = new GridBagLayout();

		JPanel p1 = new JPanel(layout);
		JPanel p2 = new JPanel();
		OverlayLayout ol = new OverlayLayout(p2);
		p2.setLayout(ol);
		m_panelAdvanced = new JPanel(layout2);

		p1.setBorder(new TitledBorder("General settings"));
		m_panelAdvanced.setBorder(new TitledBorder("Advanced settings"));

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 10, 10, 25);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 1;
		c.gridy = -1;

		// Mix Name JLabel
		JLabel j2 = new JLabel("Mix Name");
		c.gridy++;
		c.weightx = 1;
		layout.setConstraints(j2, c);
		p1.add(j2);

		// Mix Name JTextField
		m_tfMixName = new JTextField(20);
		m_tfMixName.setText("");
		m_tfMixName.setName("General/MixName");
		m_tfMixName.addFocusListener(this);
		c.weightx = 3;
		layout.setConstraints(m_tfMixName, c);
		p1.add(m_tfMixName);


		// Mix Type JRadiButton //byWP evtl. work with gridy++ //for later releases
		/*
		JLabel m_first = new JLabel ("I am willing to run as a first mix");
		m_first_yes = new JRadioButton("yes", true);
		m_first_no = new JRadioButton("no");
		m_firstMixButtonGroup = new ButtonGroup();
		m_firstMixButtonGroup.add(m_first_yes);
		m_firstMixButtonGroup.add(m_first_no);

		JLabel m_middle = new JLabel ("I am willing to run as a middle mix");
		m_middle_yes = new JRadioButton("yes", true);
		m_middle_no = new JRadioButton("no");
		m_middleMixButtonGroup = new ButtonGroup();
		m_middleMixButtonGroup.add(m_middle_yes);
		m_middleMixButtonGroup.add(m_middle_no);

		JLabel m_last = new JLabel ("I am willing to run as a last mix");
		m_last_yes = new JRadioButton("yes");
		m_last_no = new JRadioButton("no", true);
		m_lastMixButtonGroup = new ButtonGroup();
		m_lastMixButtonGroup.add(m_last_yes);
		m_lastMixButtonGroup.add(m_last_no);

		//Add the Elements to Panel p1
		//for the first mix
		c.gridy++;
		c.weightx = 1;
		layout.setConstraints(m_first, c);
		p1.add(m_first);
		c.weightx = 3;
		layout.setConstraints(m_first_yes, c);
		p1.add(m_first_yes);
		layout.setConstraints(m_first_no, c);
		p1.add(m_first_no);

		//for the middle mix
		c.gridy++;
		c.weightx = 1;
		layout.setConstraints(m_middle, c);
		p1.add(m_middle);
		c.weightx = 3;
		layout.setConstraints(m_middle_yes, c);
		p1.add(m_middle_yes);
		layout.setConstraints(m_middle_no, c);
		p1.add(m_middle_no);

		//for the last mix
		c.gridy++;
		c.weightx = 1;
		layout.setConstraints(m_last, c);
		p1.add(m_last);
		c.weightx = 3;
		layout.setConstraints(m_last_yes, c);
		p1.add(m_last_yes);
		layout.setConstraints(m_last_no, c);
		p1.add(m_last_no);
	  */



		//cascadeSettingButtons.add(m_first_no);
		//cascadeSettingButtons.add(m_middle_yes);
		//cascadeSettingButtons.add(m_middle_no);
		//cascadeSettingButtons.add(m_last_yes);
		//cascadeSettingButtons.add(m_last_no);

		//Add the RadioButtons to p1
		//c.weightx = 3;
		//layout.setConstraints(cascadeSettingButtons, c);
		//p1.add(cascadeSettingButtons);





		// Mix Type label
		JLabel j1 = new JLabel("Cascade setting");
		c.gridy++;
		c.weightx = 1;
		layout.setConstraints(j1, c);
		p1.add(j1);

		// Mix Type JComboBox
		m_comboboxMixType = new JComboBox();
		m_comboboxMixType.setName("General/MixType");
		m_comboboxMixType.addItem("Join existing cascade as first mix");
		m_comboboxMixType.addItem("Join existing cascade as middle mix");
		if (getParent() instanceof ConfigWizardPanel)
		{
			m_comboboxMixType.addItem("Create new cascade and become last Mix");
		}
		else
		{
			m_comboboxMixType.addItem("Manage own cascade and become last Mix");
		}

//		m_comboboxMixType.addItem("Join existing cascade as first or middle mix");
		m_comboboxMixType.addItemListener(this);
		c.weightx = 3;
		layout.setConstraints(m_comboboxMixType, c);
		p1.add(m_comboboxMixType);

		/*
		   // Cascade Name JLabel
		   JLabel j1a = new JLabel("Cascade Name");
		   c.gridy++;
		   c.weightx = 1;
		   layout.setConstraints(j1a, c);
		   p1.add(j1a);

		   // Cascade Name JTextField
		   m_tfCascadeName = new JTextField(20);
		   m_tfCascadeName.setText("");
		   m_tfCascadeName.setName("General/CascadeName");
		   m_tfCascadeName.addFocusListener(this);
		   c.weightx = 3;
		   layout.setConstraints(m_tfCascadeName, c);
		   p1.add(m_tfCascadeName);
		 */

		// Cascade length JCheckBox
		m_checkboxCascadeLength = new JCheckBox("Require min. Cascade length");
		m_checkboxCascadeLength.addItemListener(this);
		c.weightx = 1;
		c.gridy++;
		layout.setConstraints(m_checkboxCascadeLength, c);
		p1.add(m_checkboxCascadeLength);

		// Cascade length JTextField
		m_tfCascadeLength = new JTextField(20);
		m_tfCascadeLength.setName("General/MinCascadeLength");
		m_tfCascadeLength.setDocument(new IntegerDocument());
		m_tfCascadeLength.addFocusListener(this);
		c.weightx = 2;
		layout.setConstraints(m_tfCascadeLength, c);
		p1.add(m_tfCascadeLength);

		gbc.gridy++;
		l.setConstraints(p1, gbc);
		add(p1);

		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 1;

		// Mix ID JLabel
		m_lbMixID = new JLabel("Mix ID");
		c.gridy = 0;
		c.weightx = 1;
		layout2.setConstraints(m_lbMixID, c);
		m_panelAdvanced.add(m_lbMixID);

		// Mix ID JTextField
		m_tfMixID = new JTextField(20);
		m_tfMixID.setName("General/MixID");
		m_tfMixID.setText("");
		m_tfMixID.addFocusListener(this);
		c.weightx = 1;
		layout2.setConstraints(m_tfMixID, c);
		m_panelAdvanced.add(m_tfMixID);

		// Generate Mix ID JButton
		m_bttnGenMixID = new JButton("Generate");
		m_bttnGenMixID.addActionListener(this);
		c.weightx = 1;
		layout2.setConstraints(m_bttnGenMixID, c);
		m_panelAdvanced.add(m_bttnGenMixID);

		// User ID JCheckBox
		m_checkboxUserID = new JCheckBox("Set User ID on Execution");
		m_checkboxUserID.addItemListener(this);
		c.gridy++;
		c.weightx = 1;
		layout2.setConstraints(m_checkboxUserID, c);
		m_panelAdvanced.add(m_checkboxUserID);

		// User ID JTextField
		m_tfID = new JTextField(20);
		m_tfID.setName("General/UserID");
		m_tfID.addFocusListener(this);
		c.weightx = 2;
		layout2.setConstraints(m_tfID, c);
		m_panelAdvanced.add(m_tfID);
		m_tfID.setEnabled(false);

		// File Descriptors JCheckBox
		m_checkboxNrOfFileDes = new JCheckBox("Set Number of File Descriptors");
		m_checkboxNrOfFileDes.addItemListener(this);
		c.weightx = 1;
		c.gridy++;
		layout2.setConstraints(m_checkboxNrOfFileDes, c);
		m_panelAdvanced.add(m_checkboxNrOfFileDes);

		// File Descriptors JTextField
		m_tfNumOfFiles = new JTextField(20);
		m_tfNumOfFiles.setName("General/NrOfFileDescriptors");
		m_tfNumOfFiles.addFocusListener(this);
		c.weightx = 2;
		layout2.setConstraints(m_tfNumOfFiles, c);
		m_panelAdvanced.add(m_tfNumOfFiles);

		// Daemon JCheckBox
		m_checkboxDaemon = new JCheckBox("Run as Daemon");
		m_checkboxDaemon.setName("General/Daemon");
		m_checkboxDaemon.addItemListener(this);
		c.gridy++;
		c.weightx = 1;
		layout2.setConstraints(m_checkboxDaemon, c);
		m_panelAdvanced.add(m_checkboxDaemon);

		// Logging JCheckBox
		m_checkboxLogging = new JCheckBox("Enable Logging");
		m_checkboxLogging.addItemListener(this);
		c.gridy++;
		c.weightx = 1;
		layout2.setConstraints(m_checkboxLogging, c);
		m_panelAdvanced.add(m_checkboxLogging);

		// Console Logging JCheckBox
		m_rbConsole = new JRadioButton("Log to Console");
		m_rbConsole.setName("General/Logging/Console");
		m_rbConsole.setModel(new ToggleButtonModel());
		m_rbConsole.setSelected(true);
		m_rbConsole.addItemListener(this);
		c.gridx = 1;
		c.weightx = 1;
		layout2.setConstraints(m_rbConsole, c);
		m_panelAdvanced.add(m_rbConsole);

		// Log to Directory JRadioButton
		m_rbFile = new JRadioButton("Log to Directory:");
		m_rbFile.setModel(new ToggleButtonModel());
		m_rbFile.setName("General/Logging/File");
		m_rbFile.addItemListener(this);
		c.weightx = 1;
		c.gridy++;
		layout2.setConstraints(m_rbFile, c);
		m_panelAdvanced.add(m_rbFile);

		// Log to Directory JTextField
		m_tfFileName = new JTextField(20);
		m_tfFileName.setName("General/Logging/File");
		m_tfFileName.addFocusListener(this);
		c.gridx = -1;
		c.weightx = 1;
		c.gridwidth = 2;
		layout2.setConstraints(m_tfFileName, c);
		m_panelAdvanced.add(m_tfFileName);

		// Encrypt Log JLabel
		m_labelEnrypt = new JLabel("Encrypt with:");
		m_labelEnrypt.setEnabled(false);
		c.insets.left += 20;
		c.gridx = 1;
		c.gridy++;
		c.weightx = 1;
		c.gridwidth = 1;
		layout2.setConstraints(m_labelEnrypt, c);
		m_panelAdvanced.add(m_labelEnrypt);

		// Encrypt Log Key JTextField
		m_tfLogEncryptKeyName = new JTextField(15);
		m_tfLogEncryptKeyName.setName("General/Logging/EncryptedLog/" +
									  "KeyInfo/X509Data/X509Certificate");
		m_tfLogEncryptKeyName.addFocusListener(this);
		c.insets.left -= 20;
		c.weightx = 1;
		c.gridx = -1;
		layout2.setConstraints(m_tfLogEncryptKeyName, c);
		m_panelAdvanced.add(m_tfLogEncryptKeyName);

		// Encrypt Log Key JButton
		m_bttnImportEncKey = new JButton("Import...");
		m_bttnImportEncKey.setEnabled(false);
		m_bttnImportEncKey.addActionListener(this);
		c.weightx = 1;
		layout2.setConstraints(m_bttnImportEncKey, c);
		m_panelAdvanced.add(m_bttnImportEncKey);

		// Compress Log JCheckBox
		m_compressLog = new JCheckBox("Compress Log Files");
		m_compressLog.addItemListener(this);
		m_compressLog.setName("General/Logging/File/compress");
		c.insets.left += 20;
		c.gridx = 1;
		c.gridy++;
		c.weightx = 1;
		layout2.setConstraints(m_compressLog, c);
		m_panelAdvanced.add(m_compressLog);

		// Syslog JRadioButton
		m_rbSyslog = new JRadioButton("Log to Syslog");
		m_rbSyslog.setName("General/Logging/Syslog");
		m_rbSyslog.setModel(new ToggleButtonModel());
		m_rbSyslog.addItemListener(this);
		c.insets.left -= 20;
		c.gridx = 1;
		c.gridy++;
		c.weightx = 1;
		layout2.setConstraints(m_rbSyslog, c);
		m_panelAdvanced.add(m_rbSyslog);

		p2.add(m_panelAdvanced);

		m_panelStrut = new JPanel();
		m_panelStrut.setLayout(new FlowLayout(FlowLayout.LEFT));
		JButton b = new JButton("Advanced ...");
		b.setActionCommand("advanced");
		b.addActionListener(this);
		m_panelStrut.add(b);

		p2.add(m_panelStrut);

		m_panelStrut.setVisible(false);

		gbc.gridy++;
		l.setConstraints(p2, gbc);
		add(p2);

		m_loggingButtonGroup = new ButtonGroup();
		m_loggingButtonGroup.add(m_rbConsole);
		m_loggingButtonGroup.add(m_rbFile);
		m_loggingButtonGroup.add(m_rbSyslog);

		// fire a dummy event to myself to initially enable/disable controls
		setAutoSaveEnabled(false);
		itemStateChanged(new ItemEvent(m_checkboxDaemon, 0, (Object)null, 0));
		setAutoSaveEnabled(true);
	}

	/** Re-implemented from <CODE>java.awt.Component</CODE>, this method decides whether
	 * to show or hide advanced settings, depending on whether the Mix configuration
	 * tool has been started as a wizard or as an application.
	 */
	public void addNotify()
	{
		super.addNotify();
		Container parent = getParent();
		if (parent instanceof ConfigWizardPanel)
		{
			setAdvancedVisible(false);
			if (getConfiguration().getAttribute("General/MixID") == null)
			{
				getConfiguration().setAttribute("General/MixID", genMixID());
				load(this.m_tfMixID);
			}
		}
		else
		{
			setAdvancedVisible(true);
		}
	}

	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == m_bttnImportEncKey)
		{
			importEncKeyForLog();
		}
		// event source is "generate" button
		else if (ae.getSource() == this.m_bttnGenMixID)
		{
			this.m_tfMixID.setText(genMixID());
			save(this.m_tfMixID);
		}
		else if (ae.getActionCommand().equals("advanced"))
		{
			setAdvancedVisible(true);
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

		b = m_tfNumOfFiles.getText().equals("");
		this.m_checkboxNrOfFileDes.setSelected(!b);

		b = this.m_tfID.getText().equals("");
		this.m_checkboxUserID.setSelected(!b);

		b = this.m_tfFileName.getText().equals("");
		this.m_rbFile.setSelected(!b);

		b = false;

		s = getConfiguration().getAttribute("General/Logging/File");
		b = b || (s != null);

		s = getConfiguration().getAttribute("General/Logging/Console");
		b = b || new Boolean(s).booleanValue();

		s = getConfiguration().getAttribute("General/Logging/Syslog");
		b = b || new Boolean(s).booleanValue();

		m_checkboxLogging.setSelected(b);

		// turn on auto saving again
		setAutoSaveEnabled(true);

		enableComponents();
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
			MixConfig.handleException(e);
			System.out.println("Prev Cert not set: " + e.getMessage());
			setEncKeyForLog(null);
		}
	}

	public Vector check()
	{
		Vector errors = new Vector();
		MixConfiguration mixConf = getConfiguration();
		String s;
		int mixType;

		s = mixConf.getAttribute("General/MixName");
		if (s == null || s.equals(""))
		{
			errors.addElement("Mix Name not entered in General Panel.");

		}

		/*
		 // cascade name configuration moved to CascadePanel
		 try
		 {
		  mixType = Integer.valueOf(mixConf.getAttribute("General/MixType")).intValue();
		  s = mixConf.getAttribute("General/CascadeName");
		  if (mixType == MixConfiguration.MIXTYPE_FIRST &&
		   (s == null || s.equals("")))
		  {
		   errors.addElement("Cascade Name not entered in General Panel.");
		  }
		 }
		 catch (NumberFormatException nfe)
		 {
		  errors.addElement("Invalid Mix type in configuration.");
		 }
		 */

		s = mixConf.getAttribute("General/MixID");
		if (s == null || s.equals(""))
		{
			errors.addElement("Mix ID field is blank in General Panel.");
		}
		else if (!mixConf.isMixIDValid())
		{
			errors.addElement(
				"Mix ID should start with an 'm' and contain only " +
				"the following characters: " +
				"'A'-'Z', 'a'-'z', '0'-'9', '.', '_', '-'");
		}

		s = mixConf.getAttribute("General/UserID");
		if (s != null && s.equals(""))
		{
			errors.addElement("User ID not entered in General Panel.");

		}
		s = mixConf.getAttribute("General/NrOfFileDescriptors");
		if (s != null && !isNumber(s))
		{
			errors.addElement(
				"Number of File Descriptors is not a number in General Panel.");

		}
		s = mixConf.getAttribute("General/Logging/File");
		if (s != null && s.equals(""))
		{
			errors.addElement("No directory for logging entered in General Panel.");

		}
		return errors;
	}

	protected void enableComponents()
	{
		boolean cascadeLength = m_checkboxCascadeLength.isSelected();
		boolean log = m_checkboxLogging.isSelected();
		// FIXME: m_rbFile.isSelected() always returns false here, even if m_rbFile is selected.
		// Find out why.
		boolean file = m_rbFile.isSelected();
		// Workaround: Assume m_rbFile is selected when no radio button returns selected == true
		file = file || (!file && !m_rbSyslog.isSelected() && !m_rbConsole.isSelected());
		boolean daemon = m_checkboxDaemon.isSelected();
		int selectedMixType = m_comboboxMixType.getSelectedIndex();

		// enable/disable some controls
		m_rbConsole.setEnabled(log && !daemon);
		m_rbFile.setEnabled(log);
		m_rbSyslog.setEnabled(log);
		m_tfFileName.setEnabled(log && file);
		m_compressLog.setEnabled(log && file);
		m_bttnImportEncKey.setEnabled(log && file);
		m_labelEnrypt.setEnabled(log && file);
		m_tfLogEncryptKeyName.setEnabled(log && file);
		m_tfCascadeLength.setEnabled(cascadeLength);

		m_tfID.setEnabled(m_checkboxUserID.isSelected());
		m_tfNumOfFiles.setEnabled(m_checkboxNrOfFileDes.isSelected());

		// if mix is run as daemon, we can't log to console so set log output to file.
		if (log && !file)
		{
			// A bug in JDK 1.1.8 causes an infinite event loop here, therefore
			// event casting must be disabled
			m_rbFile.removeItemListener(this);
			m_rbFile.setSelected(m_rbConsole.isSelected() && !m_rbConsole.isEnabled());
			m_rbFile.addItemListener(this);
		}
	}

	protected void save(JTextField a)
	{
		if (a == this.m_tfLogEncryptKeyName)
		{
			if (a.isEnabled())
			{
				getConfiguration().setAttribute(a.getName(), this.m_certLogEncKey);
			}
			else
			{
				getConfiguration().removeAttribute(a.getName());
			}
		}
		else
		{
			super.save(a);
		}
	}

	protected void save(JCheckBox a)
	{
		if (a == this.m_checkboxLogging || a == this.m_compressLog)
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
			boolean log = this.m_checkboxLogging.isSelected();

			if (!log)
			{
				mixConf.removeAttribute(m_rbSyslog.getName());
				mixConf.removeAttribute(m_rbConsole.getName());
				mixConf.removeAttribute(m_tfFileName.getName());
				mixConf.removeAttribute("General/Logging/EncryptedLog");
				return;
			}

			mixConf.setAttribute("General/Logging/Console", m_rbConsole.isSelected());
			mixConf.setAttribute("General/Logging/Syslog", m_rbSyslog.isSelected());

			if (m_rbFile.isSelected())
			{
				String fn = m_tfFileName.getText();
				if (fn.equals(""))
				{
					fn = null;
				}

				if (this.m_certLogEncKey != null)
				{
					mixConf.setAttribute("General/Logging/File", (String)null);
					mixConf.setAttribute("General/Logging/EncryptedLog/File", fn);
					mixConf.setAttribute("General/Logging/EncryptedLog/" +
										 "KeyInfo/X509Data/X509Certificate",
										 m_certLogEncKey);
				}
				else
				{
					// this kludge is necessary as compatibility with older version
					// of the XML file requires string representations of boolean
					// values to be capitalized
					String s = new Boolean(m_compressLog.isSelected()).toString();
					s = Character.toUpperCase(s.charAt(0)) + s.substring(1);

					Attr at = mixConf.getDocument().createAttribute("compressed");
					at.setNodeValue(s);

					mixConf.setAttribute("General/Logging/File", fn, at, false);
				}
			}
		}
	}

	protected void load(JTextField a)
	{
		if (a == m_tfLogEncryptKeyName)
		{
			String value = getConfiguration().getAttribute(a.getName());
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

	/**
	 * Generates a random valid Mix ID. The Mix ID contains only lower case characters, digits
	 * or '.', '_' and '-'.
	 * @return a new randomly calculated valid Mix ID
	 */
	private String genMixID()
	{
		MixConfiguration mixConf = getConfiguration();
		String r_Str = "m";
		String oMixid = mixConf.getAttribute("General/MixID");
		Object o = mixConf.getAttribute("Certificates/OwnCertificate/X509PKCS12");

		if (oMixid != null && oMixid.length() != 0)
		{
			int r = JOptionPane.showConfirmDialog(this,
												  "It is generally not a good idea to change a Mix ID.\n" +
												  "You should proceed only if you know what you're doing." +
												  ( (o == null) ?
				"" :
				"\nA new Mix ID may also invalidate your certificate."),
												  "Change of Mix ID.",
												  JOptionPane.OK_CANCEL_OPTION,
												  JOptionPane.WARNING_MESSAGE);
			if (r != JOptionPane.OK_OPTION)
			{
				return oMixid;
			}
		}

		for (int i = 0; i < 10; i++)
		{
			int r = (int) (Math.random() * idChars.length());
			r_Str += idChars.substring(r, r + 1);
		}
		return r_Str;
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

	/** If the Mix configuration tool is started as a wizard, certain settings are only
	 * shown if the user clicks on the &quot;Advanced ...&quot; button. This method is
	 * shows or hides the advanced settings.
	 * @param visible specifies whether the advanced settings are to be shown (<CODE>true</CODE>) or hidden (<CODE>false</CODE>)
	 */
	private void setAdvancedVisible(boolean visible)
	{
		m_panelStrut.setVisible(!visible);
		m_panelAdvanced.setVisible(visible);
	}
}
