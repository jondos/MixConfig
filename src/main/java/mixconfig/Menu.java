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

import gui.GUIUtils;
import gui.JAPHelpContext;
import gui.dialog.CADialog;
import gui.dialog.JAPDialog;
import gui.dialog.ProxyDialog;
import gui.help.JAPHelp;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Proxy;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRootPane;
import javax.swing.event.ChangeEvent;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import mixconfig.tools.CertificationTool;
import mixconfig.tools.DataRetentionLogDecrypt;
import mixconfig.tools.EmailComposer;
import mixconfig.tools.EncryptedLogTool;
import mixconfig.tools.TemplateSigningTool;
import mixconfig.wizard.ConfigWizard;
import anon.util.JAPMessages;
import anon.util.XMLParseException;

public class Menu implements ActionListener, JAPHelpContext.IHelpContext
{

	public static final String CMD_OPEN_FILE = "Open";
	public static final String CMD_OPEN_FILE_WIZARD = "OpenWizard";
	public static final String CMD_RESET = "Reset";
	public static final String CMD_NEW_FROM_CANCEL = "New_from_Cancel";

	private static final String WIZARD = "wizard";
	private static final String EXPERT = "expert";
	private static final String START = "start";

	private static final String MSG_NO_VALID_CLIPDOC = Menu.class.getName() + "_noValidClipdoc";
	private static final String MSG_COULD_NOT_PARSE = Menu.class.getName() + "_couldNotParse";
	private static final String MSG_REALLY_CONTINUE = Menu.class.getName() + "_reallyContinue";
	private static final String MSG_FILE = Menu.class.getName() + "_file";
	private static final String MSG_TOOLS = Menu.class.getName() + "_tools";
	private static final String MSG_VIEW = Menu.class.getName() + "_view";
	private static final String MSG_EMAIL = Menu.class.getName() + "_email";
	private static final String MSG_IGNORE_INCONSISTENCIES = Menu.class.getName() + "_ignoreInconsistencies";
	
	private JFrame m_mainWin;
	private JMenuBar m_MenuBar;

	private ConfigFrame m_configFrame_Panel;
	private ConfigWizard m_configWiz_Panel;

	private JMenuItem m_defaultSize;
	private JCheckBoxMenuItem m_changeViewToWizMenuItem;
	private JCheckBoxMenuItem m_changeViewToExpertMenuItem;
	private JMenuItem m_saveMenuItem;
	private JMenuItem m_saveAsMenuItem;
	private JMenuItem m_saveclipItem;
	private JMenuItem m_checkItem;
	private JMenuItem m_proxyItem;
	private JMenuItem m_newMenuItem;
	private JMenuItem m_openMenuItem;
	private JMenuItem m_openclipItem;
	protected JMenuItem m_helpTopics;

	private JMenu m_toolsMenu;
	private JMenu m_fileMenu;

	public Menu(JFrame mainWin, JRootPane rootPane, ConfigWizard configWiz_Panel, ConfigFrame configFrame_Panel)
	{
		m_mainWin = mainWin;

		m_configFrame_Panel = configFrame_Panel;
		m_configWiz_Panel = configWiz_Panel;

		m_MenuBar = new JMenuBar();
		if (m_mainWin != null)
		{
			m_mainWin.setJMenuBar(m_MenuBar);
		}
		else if (rootPane != null)
		{
			rootPane.setJMenuBar(m_MenuBar);
		}
		//the main menu
		m_fileMenu = new JMenu(JAPMessages.getString(MSG_FILE));
		m_fileMenu.setMnemonic(JAPMessages.getString(MSG_FILE).charAt(0));
		m_MenuBar.add(m_fileMenu);
		m_toolsMenu = new JMenu(JAPMessages.getString(MSG_TOOLS));
		m_toolsMenu.setMnemonic(JAPMessages.getString(MSG_TOOLS).charAt(0));
		m_MenuBar.add(m_toolsMenu);
		JMenu viewMenu = new JMenu(JAPMessages.getString(MSG_VIEW));
		viewMenu.setMnemonic(JAPMessages.getString(MSG_VIEW).charAt(0));
		m_MenuBar.add(viewMenu);
		JMenu helpMenu = new JMenu(JAPMessages.getString(JAPHelp.MSG_HELP_BUTTON));
		helpMenu.setMnemonic(JAPMessages.getString(JAPHelp.MSG_HELP_BUTTON).charAt(0));
		m_MenuBar.add(helpMenu);

		// items for "File"
		m_newMenuItem = new JMenuItem("New");
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		m_openMenuItem = new JMenuItem("Open ...");
		m_openclipItem = new JMenuItem("Open from clipboard");

		m_checkItem = new JMenuItem("Check");
		m_checkItem.setEnabled(false);

		m_proxyItem = new JMenuItem("Proxy");
		m_proxyItem.setEnabled(true);
		
		m_saveMenuItem = new JMenuItem();
		String curFileName = MixConfig.getCurrentFileName();
		if (curFileName == null)
		{
			curFileName = "none";
			m_saveMenuItem.setEnabled(false);
		}
		m_saveMenuItem.setText("Save [" + curFileName + "] ");
		m_saveclipItem = new JMenuItem("Save to clipboard");
		m_saveclipItem.setEnabled(false);

		m_saveAsMenuItem = new JMenuItem("Save as ...");
		m_saveAsMenuItem.setEnabled(false);

		m_newMenuItem.addActionListener(this);
		exitMenuItem.addActionListener(this);
		m_openMenuItem.addActionListener(this);
		m_openclipItem.addActionListener(this);
		m_checkItem.addActionListener(this);
		m_proxyItem.addActionListener(this);
		m_saveMenuItem.addActionListener(this);
		m_saveclipItem.addActionListener(this);
		m_saveAsMenuItem.addActionListener(this);

		m_newMenuItem.setActionCommand("New");
		exitMenuItem.setActionCommand("Exit");
		m_openMenuItem.setActionCommand(CMD_OPEN_FILE);
		m_saveclipItem.setActionCommand("SaveClip");
		m_openclipItem.setActionCommand("OpenClip");
		m_checkItem.setActionCommand("Check");
		m_proxyItem.setActionCommand("Proxy");
		m_saveMenuItem.setActionCommand("Save");
		m_saveAsMenuItem.setActionCommand("SaveAs");
		if (m_mainWin == null)
		{ // an applet
			exitMenuItem.setEnabled(false);
			m_openMenuItem.setEnabled(false);
			m_saveAsMenuItem.setEnabled(false);
		}

		m_fileMenu.add(m_newMenuItem);
		m_fileMenu.addSeparator();
		m_fileMenu.add(m_openMenuItem);
		m_fileMenu.add(m_openclipItem);
		m_fileMenu.addSeparator();
		m_fileMenu.add(m_checkItem);
		m_fileMenu.add(m_proxyItem);
		m_fileMenu.add(m_saveMenuItem);
		m_fileMenu.add(m_saveAsMenuItem);
		m_fileMenu.add(m_saveclipItem);
		m_fileMenu.addSeparator();
		m_fileMenu.add(exitMenuItem);

		// Items for "Tools"
		JMenuItem toolEmailMenuItem = new JMenuItem(JAPMessages.getString(MSG_EMAIL));
		m_toolsMenu.add(toolEmailMenuItem);
		//toolEmailMenuItem.setEnabled(false);
		toolEmailMenuItem.setActionCommand("toolEmailMenuItem");
		toolEmailMenuItem.addActionListener(this);
		
		JMenuItem toolCertSigMenuItem = new JMenuItem("Sign X.509 certificate ...");
		m_toolsMenu.add(toolCertSigMenuItem);
		toolCertSigMenuItem.setActionCommand("toolCertSigMenuItem");
		toolCertSigMenuItem.addActionListener(this);

		//JMenuItem toolPGPMenuItem = new JMenuItem("Convert PGP to X.509 ...");
		//m_toolsMenu.add(toolPGPMenuItem);
		//toolPGPMenuItem.setActionCommand("toolPGPMenuItem");
		//toolPGPMenuItem.addActionListener(this);

		JMenuItem toolEncLogMenuItem = new JMenuItem("Display encrypted mix log ...");
		m_toolsMenu.add(toolEncLogMenuItem);
		toolEncLogMenuItem.setActionCommand("toolEncLogMenuItem");
		toolEncLogMenuItem.addActionListener(this);

		JMenuItem toolDataRetentionLogMenuItem = new JMenuItem("Process retained Data ...");
		m_toolsMenu.add(toolDataRetentionLogMenuItem);
		toolDataRetentionLogMenuItem.setActionCommand("toolDataRetentionLogMenuItem");
		toolDataRetentionLogMenuItem.addActionListener(this);

		JMenuItem toolCertViewMenuItem = new JMenuItem("View accepted CAs ...");
		m_toolsMenu.add(toolCertViewMenuItem);
			toolCertViewMenuItem.setActionCommand("toolCertViewMenuItem");
		toolCertViewMenuItem.addActionListener(this);
		
		JMenuItem toolTemplateSignerMenuItem = new JMenuItem("Sign T&C templates");
		m_toolsMenu.add(toolTemplateSignerMenuItem);
		toolTemplateSignerMenuItem.setActionCommand("toolTemplateSignerMenuItem");
		toolTemplateSignerMenuItem.addActionListener(this);
		
		// Items for "View"
		m_changeViewToWizMenuItem = new JCheckBoxMenuItem("Wizard", false);
		viewMenu.add(m_changeViewToWizMenuItem);
		m_changeViewToWizMenuItem.setActionCommand("ChangeViewToWiz");
		m_changeViewToWizMenuItem.addActionListener(this);

		m_changeViewToExpertMenuItem = new JCheckBoxMenuItem("Expert", false);
		viewMenu.add(m_changeViewToExpertMenuItem);
		m_changeViewToExpertMenuItem.setActionCommand("ChangeViewToExpert");
		m_changeViewToExpertMenuItem.addActionListener(this);

		m_defaultSize = new JMenuItem("Default size");
		m_defaultSize.addActionListener(this);
		viewMenu.add(m_defaultSize);

		// Items for "Help"
		JMenuItem aboutMenuItem = new JMenuItem("About...");
		helpMenu.add(JAPHelp.createHelpMenuItem(this));
		helpMenu.add(aboutMenuItem);
		aboutMenuItem.setActionCommand("About");
		aboutMenuItem.addActionListener(this);

	}

	public String getHelpContext()
	{
		return JAPHelpContext.INDEX;
	}

	public void exit()
	{
		int exit = JAPDialog.RETURN_VALUE_OK;

		if (!MixConfig.getMainWindow().isEnabled())
		{
			return;
		}

		if (!MixConfig.getMixConfiguration().isSavedToFile())
		{
			exit = JAPDialog.showConfirmDialog(MixConfig.getMainWindow(),
											   JAPMessages.getString(MSG_REALLY_CONTINUE),
											   JAPDialog.OPTION_TYPE_OK_CANCEL,
											   JAPDialog.MESSAGE_TYPE_QUESTION);
		}
		if (JAPDialog.RETURN_VALUE_OK == exit)
		{
			System.exit(0);
		}
	}

	public void reset(boolean a_bStartNewConfiguration) throws XMLParseException, IOException
	{
		boolean bReset = false;
		int ok;

		ChoicePanel cp = (ChoicePanel) m_configWiz_Panel.getParent();
		//if you choose "new", when the start-screen is in top, then start the expert-mode
		if (cp.getActiveCard().equals(START))
		{
			cp.setExpertVisible();
			m_changeViewToWizMenuItem.setEnabled(true);
			m_changeViewToExpertMenuItem.setEnabled(false);
		}
		else
		{
			bReset = MixConfig.getMixConfiguration().isSavedToFile();
			//if the start screen is not on top -> show the warning message
			if (!bReset)
			{
				ok = JAPDialog.showConfirmDialog(MixConfig.getMainWindow(),
												 JAPMessages.getString(MSG_REALLY_CONTINUE),
												 JAPDialog.OPTION_TYPE_OK_CANCEL,
												 JAPDialog.MESSAGE_TYPE_QUESTION);
				bReset = JAPDialog.RETURN_VALUE_OK == ok;
			}
		}
		if (bReset)
		{
			reset();
			if (!a_bStartNewConfiguration)
			{
				cp.setStartScreenVisible(); //set correct button labels
			}
			m_configWiz_Panel.changeButtonLabelToNext();
			m_configWiz_Panel.stateChanged(new ChangeEvent(this));
		}
	}

	public void actionPerformed(ActionEvent evt)
	{
		try
		{
			// Get the mix configuration object
			MixConfiguration mixConf = MixConfig.getMixConfiguration();

			if (evt.getActionCommand().equals(CMD_RESET))
			{
				reset();
			}
			else if (evt.getSource() == m_defaultSize)
			{
				((ChoicePanel)m_configWiz_Panel.getParent()).setDefaultSize();
			}
			else if (evt.getActionCommand().equals("New") || evt.getActionCommand().equals(CMD_NEW_FROM_CANCEL))
			{
				reset(!evt.getActionCommand().equals(CMD_NEW_FROM_CANCEL));
			}
			else if (evt.getActionCommand().equals("Exit"))
			{
				exit();
			}
			else if (evt.getActionCommand().equals("Check"))
			{
				String[] msg = m_configFrame_Panel.check();
				if (msg != null && msg.length > 0)
				{
					MixConfig.info("Inconsistencies found", msg, false);
				}
				else
				{
					JAPDialog.showMessageDialog(MixConfig.getMainWindow(), "Configuration is valid.", "Check");
				}
				// XXX: Additionally perform the NEW reload-check here
				mixConf.performReloadCheck();
			}
			else if (evt.getActionCommand().equals("Proxy"))
			{
				Proxy proxy = ProxyDialog.showProxyDialog(MixConfig.getMainWindow());
				if(proxy != null)
				{
					MixConfig.configureProxy(proxy);
				}
			}
			else if (evt.getActionCommand().equals("Save"))
			{
				if (ignoreInconsistenciesForSaving() && MixConfig.getCurrentFileName() != null && 
						mixConf.performReloadCheck())
				{
					//Force UTF-8 as standard output charset. 
					OutputStreamWriter writer = 
						new OutputStreamWriter(new FileOutputStream(MixConfig.getCurrentFileName()), "UTF-8");
					mixConf.save(writer);
					writer.close();
					//mixConf.save(new FileWriter(MixConfig.getCurrentFileName()));
				}
			}
			else if (evt.getActionCommand().equals("SaveAs"))
			{
				if (ignoreInconsistenciesForSaving() && mixConf.performReloadCheck())
				{
					JFileChooser fileChooser =
						MixConfig.showFileDialog(MixConfig.getMainWindow(), MixConfig.SAVE_DIALOG,
								     MixConfig.FILTER_XML);
					if (fileChooser == null)
					{
						return;
					}
					File file = fileChooser.getSelectedFile();
					if (file != null)
					{
						String fname = file.getName();
						if (!fname.toLowerCase().endsWith(".xml"))
						{
							file = new File(file.getParent(), fname + ".xml");
						}
						mixConf.save(new FileWriter(file.getCanonicalPath()));
						m_saveMenuItem.setText("Save [" + file.getName() + "] ");
						m_saveMenuItem.setEnabled(true);
						MixConfig.setCurrentFilename(file.getCanonicalPath());
					}
				}
			}
			else if (evt.getActionCommand().equals("SaveClip"))
			{
				if (ignoreInconsistenciesForSaving() && mixConf.performReloadCheck())
				{
					StringWriter sw = new StringWriter();
					MixConfig.getMixConfiguration().save(sw);
					String xmlString = sw.toString();
					GUIUtils.saveTextToClipboard(xmlString, MixConfig.getMainWindow());
					//mixConf.setSavedToFile();
				}
			}
			else if (evt.getActionCommand().equals("OpenClip"))
			{
				// Read XML from the clipboard into a string
				String xmlString = GUIUtils.getTextFromClipboard(m_MenuBar);
				StringReader sr = new StringReader(xmlString);
				try
				{
					mixConf.setMixConfiguration(sr);
					m_configFrame_Panel.setConfiguration(mixConf);
					m_configWiz_Panel.setConfiguration(mixConf);

					//if you choose "open using clipboard", when the start-screen is in top, then start the expert-mode
					ChoicePanel cp = (ChoicePanel) m_configWiz_Panel.getParent();
					if (cp.getActiveCard().equals(START))
					{
						cp.setExpertVisible();
						m_changeViewToWizMenuItem.setEnabled(true);
						m_changeViewToExpertMenuItem.setEnabled(false);
					}
					m_configFrame_Panel.reset(); //show the first leaf
					m_configWiz_Panel.reset(); //show the first leaf
				}
				catch (XMLParseException a_e)
				{
					JAPDialog.showErrorDialog(MixConfig.getMainWindow(),
							     JAPMessages.getString(MSG_NO_VALID_CLIPDOC),
								 JAPMessages.getString(MSG_COULD_NOT_PARSE), LogType.GUI, a_e);
				}
			}
			else if (evt.getActionCommand().equals(CMD_OPEN_FILE) ||
					 evt.getActionCommand().equals(CMD_OPEN_FILE_WIZARD))
			{
				File file = null;
				JFileChooser chooser = MixConfig.showFileDialog(MixConfig.getMainWindow(), 
						                            MixConfig.OPEN_DIALOG, MixConfig.FILTER_XML);
				if (chooser != null)
				{
					file = chooser.getSelectedFile();
				}
				if (file != null && mixConf.setMixConfiguration(new FileReader(file)))
				{
					m_configFrame_Panel.setConfiguration(mixConf);
					m_configWiz_Panel.setConfiguration(mixConf);

					m_saveMenuItem.setText("Save [" + file.getName() + "] ");
					m_saveMenuItem.setEnabled(true);
					MixConfig.setCurrentFilename(file.getCanonicalPath());

					// If "open" is chosen, when the start-screen is in top, then start expert-mode
					ChoicePanel cp = (ChoicePanel) m_configWiz_Panel.getParent();
					if (evt.getActionCommand().equals(CMD_OPEN_FILE))
					{
						cp.setExpertVisible();
					}
					else
					{
						cp.setWizardVisible();
					}
					m_configFrame_Panel.reset(); //show the first leaf
					m_configWiz_Panel.reset(); //show the first leaf
					mixConf.setSavedToFile();
				}
			}
			else if (evt.getActionCommand().equals("toolEmailMenuItem"))
			{
				// FIXME: How to call a non-static method in OwnCertificatesPanel from here?
				EmailComposer.composeEmail();
			}			
			else if (evt.getActionCommand().equals("toolCertSigMenuItem"))
			{
				new CertificationTool(MixConfig.getMainWindow());
			}
			else if (evt.getActionCommand().equals("toolEncLogMenuItem"))
			{
				new EncryptedLogTool(MixConfig.getMainWindow());
			}
			else if (evt.getActionCommand().equals("toolDataRetentionLogMenuItem"))
			{
				new DataRetentionLogDecrypt(MixConfig.getMainWindow());
			}
			else if (evt.getActionCommand().equals("toolCertViewMenuItem"))
			{
				new CADialog(MixConfig.getMainWindow());
			}
			else if (evt.getActionCommand().equals("toolTemplateSignerMenuItem"))
			{
				new TemplateSigningTool(MixConfig.getMainWindow());
			}
			else if (evt.getActionCommand().equals("About"))
			{
				MixConfig.about();
			}
			else if (evt.getActionCommand().equals("ChangeViewToWiz"))
			{
				LogHolder.log(LogLevel.DEBUG, LogType.GUI, "Switching to Wizard View");
				ChoicePanel cp = (ChoicePanel) m_configWiz_Panel.getParent();
				boolean bSavedToFile = mixConf.isSavedToFile();
				cp.setWizardVisible();
				m_configWiz_Panel.load();
				if (bSavedToFile)
				{
					mixConf.setSavedToFile();
				}
			}
			else if (evt.getActionCommand().equals("ChangeViewToExpert"))
			{
				LogHolder.log(LogLevel.DEBUG, LogType.GUI, "Switching to Expert View");
				ChoicePanel cp = (ChoicePanel) m_configFrame_Panel.getParent();
				boolean bSavedToFile = mixConf.isSavedToFile();
				cp.setExpertVisible();
				m_configFrame_Panel.load();
				m_configFrame_Panel.setActivePanel(m_configWiz_Panel.getCurrentPageClass());
				if (bSavedToFile)
				{
					mixConf.setSavedToFile();
				}
			}
		}
		catch (Exception e)
		{
			JAPDialog.showErrorDialog(MixConfig.getMainWindow(),
					     JAPMessages.getString(JAPDialog.MSG_ERROR_UNKNOWN), LogType.GUI, e);
		}
		// Set MessageTitle
		((ChoicePanel)m_configWiz_Panel.getParent()).setMessageTitle();
	}

	/** Clears all data in the panels and restarts with a new configuration object.
	 * @throws IOException If a communication error occurs
	 * @throws XMLParseException If an XML error occurs
	 */
	private void reset() throws XMLParseException, IOException
	{
		m_saveMenuItem.setText("Save [none]");
		m_saveMenuItem.setEnabled(false);
		MixConfig.setCurrentFilename(null);
		// Create a new empty instance of MixConfiguration
		MixConfiguration mixconfig = new MixConfiguration();
		m_configFrame_Panel.setConfiguration(mixconfig);
		m_configWiz_Panel.setConfiguration(mixconfig);
		m_configFrame_Panel.reset(); //show the first leaf
		m_configWiz_Panel.reset(); //show the first leaf
		mixconfig.setSavedToFile();

		//if you choose "new", when the start-screen is in top, then start the wizard-mode
		ChoicePanel cp = (ChoicePanel) m_configWiz_Panel.getParent();
		if (cp.getActiveCard().equals(START))
		{
			cp.setWizardVisible();
		}
	}

	protected void checkUnuseableMenuItem()
	{
		ChoicePanel cp = (ChoicePanel) m_configWiz_Panel.getParent();
		// Inactivate MenuItems in "view"
		if (cp.getActiveCard().equals(WIZARD))
		{
			m_changeViewToWizMenuItem.setEnabled(false);
			m_changeViewToWizMenuItem.setState(true);
			m_changeViewToExpertMenuItem.setEnabled(true);
			m_changeViewToExpertMenuItem.setState(false);
			m_toolsMenu.setEnabled(false);
			m_newMenuItem.setEnabled(false);
			m_openMenuItem.setEnabled(false);
			m_openclipItem.setEnabled(false);

			m_saveAsMenuItem.setEnabled(true);
			m_saveclipItem.setEnabled(true);
			m_checkItem.setEnabled(true);
		}
		else if (cp.getActiveCard().equals(EXPERT))
		{
			m_changeViewToWizMenuItem.setEnabled(true);
			m_changeViewToWizMenuItem.setState(false);
			m_changeViewToExpertMenuItem.setEnabled(false);
			m_changeViewToExpertMenuItem.setState(true);
			m_toolsMenu.setEnabled(true);
			m_newMenuItem.setEnabled(true);
			m_openMenuItem.setEnabled(true);
			m_openclipItem.setEnabled(true);

			m_saveAsMenuItem.setEnabled(true);
			m_saveclipItem.setEnabled(true);
			m_checkItem.setEnabled(true);
		}
		else if (cp.getActiveCard().equals(START))
		{
			m_changeViewToWizMenuItem.setEnabled(true);
			m_changeViewToWizMenuItem.setState(false);
			m_changeViewToExpertMenuItem.setEnabled(true);
			m_changeViewToExpertMenuItem.setState(false);
			m_toolsMenu.setEnabled(true);
			m_newMenuItem.setEnabled(true);
			m_openMenuItem.setEnabled(true);
			m_openclipItem.setEnabled(true);

			m_saveAsMenuItem.setEnabled(false);
			m_saveclipItem.setEnabled(false);
			m_checkItem.setEnabled(false);
		}
	}

	/**
	 * If there are inconsistencies in the current configuration, ask the user if
	 * he wants to ignore them for saving.
	 * @throws IOException
	 * @return true if there are no inconsistencies or inconsistencies should be ignored
	 *         false otherwise
	 */
	private boolean ignoreInconsistenciesForSaving() throws IOException
	{
		// Return value
		boolean bIgnore = true;
		String[] msg = m_configFrame_Panel.check();
		
		if (msg.length > 0)
		{
			bIgnore = JAPDialog.showYesNoDialog(MixConfig.getMainWindow(), 
					               JAPMessages.getString(MSG_IGNORE_INCONSISTENCIES), 
					               "Inconsistencies found");
			if (!bIgnore)
			{
				MixConfig.info("Inconsistencies found", msg, false);
			}
		}
		return bIgnore;
	}

	public Container getHelpExtractionDisplayContext() 
	{
		return null;
	}
}
