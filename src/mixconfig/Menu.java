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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.event.ChangeEvent;
import anon.util.XMLParseException;
import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import gui.JAPHelp;
import gui.GUIUtils;
import mixconfig.wizard.ConfigWizard;

public class Menu implements ActionListener
{

	public static final String CMD_OPEN_FILE = "Open";
	public static final String CMD_OPEN_FILE_WIZARD = "OpenWizard";
	public static final String CMD_RESET = "Reset";


	private static final String WIZARD = "wizard";
	private static final String EXPERT = "expert";
	private static final String START  = "start";

	private JFrame m_mainWin;
	private JMenuBar m_MenuBar;

	private ConfigFrame  m_configFrame_Panel;
	private ConfigWizard m_configWiz_Panel;

	private JMenuItem m_defaultSize;
	private JCheckBoxMenuItem m_changeViewToWizMenuItem;
	private JCheckBoxMenuItem m_changeViewToExpertMenuItem;
	private JMenuItem m_saveMenuItem;
	private JMenuItem m_saveAsMenuItem;
	private JMenuItem m_saveclipItem;
	private JMenuItem m_checkItem;
	private JMenuItem m_newMenuItem;
	private JMenuItem m_openMenuItem;
	private JMenuItem m_openclipItem;
	private JMenuItem m_helpTopics;

	private JMenu m_toolsMenu;
	private JMenu m_fileMenu;

	public Menu(JFrame mainWin, ConfigWizard configWiz_Panel, ConfigFrame configFrame_Panel)
	{
		m_mainWin = mainWin;

		m_configFrame_Panel = configFrame_Panel;
		m_configWiz_Panel   = configWiz_Panel;

		m_MenuBar = new JMenuBar();
		m_mainWin.setJMenuBar(m_MenuBar);

		//the main menu
	    m_fileMenu = new JMenu("File");
		m_fileMenu.setMnemonic('F');
		m_MenuBar.add(m_fileMenu);
		m_toolsMenu = new JMenu("Tools");
		m_toolsMenu.setMnemonic('T');
		m_MenuBar.add(m_toolsMenu);
		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic('V');
		m_MenuBar.add(viewMenu);
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		m_MenuBar.add(helpMenu);

		//items for "file"
		m_newMenuItem = new JMenuItem("New");
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		m_openMenuItem = new JMenuItem("Open...");
		m_openclipItem = new JMenuItem("Open from clipboard");

     	m_checkItem = new JMenuItem("Check");
		m_checkItem.setEnabled(false);

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

	    m_saveAsMenuItem = new JMenuItem("Save as...");
		m_saveAsMenuItem.setEnabled(false);

		m_newMenuItem.addActionListener(this);
		exitMenuItem.addActionListener(this);
		m_openMenuItem.addActionListener(this);
		m_openclipItem.addActionListener(this);
		m_checkItem.addActionListener(this);
		m_saveMenuItem.addActionListener(this);
		m_saveclipItem.addActionListener(this);
		m_saveAsMenuItem.addActionListener(this);

		m_newMenuItem.setActionCommand("New");
		exitMenuItem.setActionCommand("Exit");
		m_openMenuItem.setActionCommand(CMD_OPEN_FILE);
		m_saveclipItem.setActionCommand("SaveClip");
		m_openclipItem.setActionCommand("OpenClip");
		m_checkItem.setActionCommand("Check");
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
		m_fileMenu.add(m_saveMenuItem);
		m_fileMenu.add(m_saveAsMenuItem);
		m_fileMenu.add(m_saveclipItem);
		m_fileMenu.addSeparator();
		m_fileMenu.add(exitMenuItem);

		//items for "tools"
		JMenuItem toolCertSigMenuItem = new JMenuItem(
			"Sign X.509 certificate ...");
		m_toolsMenu.add(toolCertSigMenuItem);
		toolCertSigMenuItem.setActionCommand("toolCertSigMenuItem");
		toolCertSigMenuItem.addActionListener(this);
		JMenuItem toolPGPMenuItem = new JMenuItem(
			"Convert PGP to X.509 ...");
		m_toolsMenu.add(toolPGPMenuItem);
		toolPGPMenuItem.setActionCommand("toolPGPMenuItem");
		toolPGPMenuItem.addActionListener(this);

		JMenuItem toolEncLogMenuItem = new JMenuItem(
			"Display encrypted mix log ...");
		m_toolsMenu.add(toolEncLogMenuItem);
		toolEncLogMenuItem.setActionCommand("toolEncLogMenuItem");
		toolEncLogMenuItem.addActionListener(this);

		//items for "view"
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


		//items for "help"
		JMenuItem aboutMenuItem = new JMenuItem("About...");
		m_helpTopics = new JMenuItem("Help topics");
		m_helpTopics.addActionListener(this);
		helpMenu.add(m_helpTopics);
		helpMenu.add(aboutMenuItem);
		aboutMenuItem.setActionCommand("About");
		aboutMenuItem.addActionListener(this);

	}

	public void actionPerformed(ActionEvent evt)
	{
		try
		{
			MixConfiguration mixConf = MixConfig.getMixConfiguration();

			if (evt.getActionCommand().equals(CMD_RESET))
			{
				reset();
			}
			else if (evt.getSource() == m_helpTopics)
			{
				JAPHelp.getInstance().getContextObj().setContext("index");
				GUIUtils.positionWindow(JAPHelp.getInstance(), MixConfig.getMainWindow());
				JAPHelp.getInstance().show();
			}
			else if (evt.getSource() == m_defaultSize)
			{
				((ChoicePanel) m_configWiz_Panel.getParent()).setDefaultSize();
			}
			else if (evt.getActionCommand().equals("New") || evt.getActionCommand().equals("New_from_Cancel"))
			{
				boolean warning = false;
				ChoicePanel cp = (ChoicePanel) m_configWiz_Panel.getParent();
				//if the start screen is on top -> don't show the warning message
				if (!cp.getActiveCard().equals(START))
				{
					warning = MixConfig.ask("Notice", "You will lose unsaved information. " +
											"Do you really want to cancel?");
				}
                //if you choose "new", when the start-screen is in top, then start the expert-mode
				if (cp.getActiveCard().equals(START))
				{
					cp.setExpertVisible();
					m_changeViewToWizMenuItem.setEnabled(true);
					m_changeViewToExpertMenuItem.setEnabled(false);
				}
                if (warning)
				{
					reset();
					if (evt.getActionCommand().equals("New_from_Cancel")) cp.setStartScreenVisible(); //set korrect button lables
					m_configWiz_Panel.changeButtonLabelToNext();
					m_configWiz_Panel.stateChanged(new ChangeEvent(this));
				}

			}
			else if (evt.getActionCommand().equals("Exit"))
			{
               //dispose();
				System.exit(0);
			}
			else if (evt.getActionCommand().equals("Check"))
			{
				String[] msg = m_configFrame_Panel.check();
				if (msg != null && msg.length > 0)
				{
					MixConfig.info("Inconsistencies found", msg);
				}
				else
				{
					MixConfig.info("Check", "Configuration is valid.");
				}
			}
			else if (evt.getActionCommand().equals("Save"))
			{
				if (ignoreInconsistenciesForSaving() && MixConfig.getCurrentFileName() != null)
				{
					mixConf.save(new FileWriter(MixConfig.getCurrentFileName()));
				}
			}
			else if (evt.getActionCommand().equals("SaveAs"))
			{
				if (ignoreInconsistenciesForSaving())
				{
					File file =
						MixConfig.showFileDialog(MixConfig.SAVE_DIALOG,
												 MixConfig.FILTER_XML).getSelectedFile();
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
				if (ignoreInconsistenciesForSaving())
				{
					StringWriter sw = new StringWriter();
					MixConfig.getMixConfiguration().save(sw);
					String xmlString = sw.toString();

					try
					{
						Clipboard cb = getClipboard();
						cb.setContents(new StringSelection(xmlString),
									   new ClipboardOwner()
						{
							public void lostOwnership(Clipboard cb, Transferable co)
							{
								// Don't care.
							}
						});
						/*
						 JOptionPane.showMessageDialog(TheApplet.getMainWindow(),
						 "Configuration saved into clipboard.", "Save", JOptionPane.INFORMATION_MESSAGE);
						 return;
						 */
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					// There are some problems with the access of the
					// clipboard, so after the try to copy it, we
					// still offer the ClipFrame.
					ClipFrame cf =
						new ClipFrame("Copy and Save this file in a new Location.", false);
					cf.setText(xmlString);
					cf.show();
				}
			}
			else if (evt.getActionCommand().equals("OpenClip"))
			{
				Clipboard cb = getClipboard();
				String xmlString;

				Transferable data = cb.getContents(this);
				if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor))
				{
					xmlString = (String) data.getTransferData(DataFlavor.stringFlavor);
				}
				else
				{
					ClipFrame cf = new ClipFrame("Paste a file to be opened in the area provided.", true);
					cf.show();
					xmlString = cf.getText();
				}
				//m_configFrame_Panel.setConfiguration(new MixConfiguration(new StringReader(xmlString)));
				StringReader sr = new StringReader(xmlString);
				MixConfiguration mixconfig = MixConfig.getMixConfiguration();
				mixconfig.setMixConfiguration(sr);
				m_configFrame_Panel.setConfiguration(mixconfig);
				m_configWiz_Panel.setConfiguration(mixconfig);

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
			else if (evt.getActionCommand().equals(CMD_OPEN_FILE) ||
					 evt.getActionCommand().equals(CMD_OPEN_FILE_WIZARD))
			{
				File file = MixConfig.showFileDialog(MixConfig.OPEN_DIALOG, MixConfig.FILTER_XML)
					.getSelectedFile();
				MixConfiguration mixconfig = MixConfig.getMixConfiguration();

				if (file != null && mixconfig.setMixConfiguration(new FileReader(file)))
				{
					m_configFrame_Panel.setConfiguration(mixconfig);
					m_configWiz_Panel.setConfiguration(mixconfig);

					m_saveMenuItem.setText("Save [" + file.getName() + "] ");
					m_saveMenuItem.setEnabled(true);
					MixConfig.setCurrentFilename(file.getCanonicalPath());

					//if you choose "open", when the start-screen is in top, then start the expert-mode
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
				}
			}
			else if (evt.getActionCommand().equals("toolCertSigMenuItem"))
			{
				new SigCertTool(MixConfig.getMainWindow());
			}
			else if (evt.getActionCommand().equals("toolEncLogMenuItem"))
			{
				new EncryptedLogTool(MixConfig.getMainWindow());
			}
			else if (evt.getActionCommand().equals("toolPGPMenuItem"))
			{
				new PGPtoX509Tool(MixConfig.getMainWindow());
			}
			else if (evt.getActionCommand().equals("About"))
			{
				MixConfig.about();
			}
			else if (evt.getActionCommand().equals("ChangeViewToWiz"))
			{
				LogHolder.log(LogLevel.DEBUG, LogType.GUI, "Set Wizard Visible");
				ChoicePanel cp = (ChoicePanel) m_configWiz_Panel.getParent();
				cp.setWizardVisible();
				m_configWiz_Panel.load();
			}
			else if (evt.getActionCommand().equals("ChangeViewToExpert"))
			{
				LogHolder.log(LogLevel.DEBUG, LogType.GUI, "Set Expert Visible");
				ChoicePanel cp = (ChoicePanel)m_configFrame_Panel.getParent();
				cp.setExpertVisible();
				m_configFrame_Panel.load();
			}

		}
		catch (Exception e)
		{
			MixConfig.handleError(e, null, LogType.GUI);
		}

	    //set MessageTitle
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
		//create a new empty MixConfiguration Instace
	    MixConfiguration mixconfig = new MixConfiguration();
	    m_configFrame_Panel.setConfiguration(mixconfig);
		m_configWiz_Panel.setConfiguration(mixconfig);
		m_configFrame_Panel.reset(); //show the first leaf
		m_configWiz_Panel.reset(); //show the first leaf

       //if you choose "new", when the start-screen is in top, then start the wizard-mode
	   ChoicePanel cp = (ChoicePanel) m_configWiz_Panel.getParent();
	   if (cp.getActiveCard().equals(START))
	   {
		   cp.setWizardVisible();
	   }

	}


	private Clipboard getClipboard()
	{
		Clipboard r_cb = null;
		try
		{
			Method getSystemSelection = m_mainWin.getToolkit().getClass()
				.getMethod("getSystemSelection", new Class[0]);
			r_cb = (Clipboard) getSystemSelection.invoke(m_mainWin.getToolkit(), new Object[0]);
		}
		catch (NoSuchMethodException nsme)
		{
			// JDK < 1.4 does not support getSystemSelection
		}
		catch (IllegalAccessException iae)
		{
			// this should not happen
		}
		catch (InvocationTargetException ite)
		{
			// this should not happen
		}

		// alternate way of retrieving the clipboard
		if (r_cb == null)
		{
			r_cb = m_mainWin.getToolkit().getSystemClipboard();
		}
		return r_cb;
	}

	protected void checkUnuseableMenuItem()
	{
		ChoicePanel cp = (ChoicePanel) m_configWiz_Panel.getParent();
        //grey the unusable Menuitem in "view"
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
	 * If there are inconsistencies in the current configuration the user is asked if
	 * he wants to ignore them for saving.
	 * @throws IOException
	 * @return true if there are no inconsistencies or all inconsistencies are ignored;
	 *         fasle otherwise
	 */
	private boolean ignoreInconsistenciesForSaving() throws IOException
	{
		boolean bIgnore = true;

		if (m_configFrame_Panel.check().length > 0)
		{
			bIgnore = MixConfig.ask("Inconsistencies found",
								  "The configuration is not consistent! " +
								  "The mix will not run properly if you do not " +
								  "correct the remaining errors." +
								  "Please choose '" + "" + "' for details. \n" +
								  "Do you really want to save this configuration?");
		}

		return bIgnore;
	}

}
