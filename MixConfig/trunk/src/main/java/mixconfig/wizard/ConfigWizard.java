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
package mixconfig.wizard;

import gui.dialog.JAPDialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import logging.LogType;
import mixconfig.ChoicePanel;
import mixconfig.Menu;
import mixconfig.MixConfig;
import mixconfig.MixConfiguration;

/** A class that displays the Mix configuration panels as a wizard. To the left of
 * the panel, a logo is displayed; at the bottom, there are three navigation
 * buttons (back, next, and cancel). The center of the panel displays the wizard's
 * pages, where only one page is visible at a time.<br>
 * Upon click on &quot;next&quot;, a plausibility check is performed on the
 * page and a warning about any inconsistent input is shown. If there are no
 * warnings, the wizard continues by displaying the next page. If the end is
 * reached, the &quot;next&quot; button changes into a &quot;finish&quot; button.
 * When this is clicked, a file dialog is shown to let the user save the new
 * configuration.
 * @author ronin &lt;ronin2@web.de&gt;
 */
public class ConfigWizard extends WizardLayout implements ActionListener, ChangeListener
{

	/** A container laid out with a <CODE>CardLayout</CODE> that contains the wizard's
	 * pages
	 */
	private ConfigWizardPanel m_wizPanel;

	/** Constructs a new instance of <CODE>ConfigWizard</CODE>
	 * @throws IOException If an I/O error occurs while saving the configuration
	 */
	public ConfigWizard() throws IOException
	{
		m_wizPanel = new ConfigWizardPanel();
		m_wizPanel.addChangeListener(this);

		add(m_wizPanel, BorderLayout.CENTER);

		getButtonForward().addActionListener(this);
		getButtonBack().addActionListener(this);
		getButtonCancel().addActionListener(this);

		stateChanged(null);
	}

	public void stateChanged(ChangeEvent e)
	{
		//getButtonBack().setEnabled( (m_wizPanel.getState() & ConfigWizardPanel.STATE_BEGIN) == 0);
	}

	public void actionPerformed(ActionEvent e)
	{
		try
		{
			getButtonForward().setText("Next ->");

			try
			{
				if (e.getSource() == getButtonForward())
				{
					m_wizPanel.checkAndForward();
				}
				else if (e.getSource() == getButtonBack())
				{
					if ( (m_wizPanel.getState() & ConfigWizardPanel.STATE_BEGIN) == 0)
					{
						m_wizPanel.back();
					}
					else
					{
						Menu menu = ( (ChoicePanel)this.getParent()).getMenu();
						menu.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
							Menu.CMD_NEW_FROM_CANCEL));
					}
				}
				else if (e.getSource() == getButtonCancel())
				{
					Menu menu = ( (ChoicePanel)this.getParent()).getMenu();
					menu.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
						Menu.CMD_NEW_FROM_CANCEL));

				}
				int i = m_wizPanel.getState();
				if ( (i & ConfigWizardPanel.STATE_READY_TO_FINISH) != 0)
				{
					getButtonForward().setText("Finish");
				}
			}
			catch (CannotContinueException cce)
			{
				int i = m_wizPanel.getState();
				if ( (i & ConfigWizardPanel.STATE_STOP) > 0)
				{
					// TODO: if cce was thrown due to end of wizard, save config
					String[] msg = cce.getMessages();
					if (msg != null && msg.length > 0)
					{
						if (JAPDialog.showYesNoDialog(this, msg[0] + " Continue anyway?", "Errors"))
						{
							m_wizPanel.doForward();
						}
						//MixConfig.info("Errors", msg);
					}
				}
				else if ( (i & ConfigWizardPanel.STATE_END) > 0)
				{
					getButtonForward().setText("Finish");
					File file;
					if ((file = MixConfig.getMixConfiguration().saveToFile()) != null)
					{
						JAPDialog.showMessageDialog(MixConfig.getMainWindow(),
												  "Configuration saved as " + file, "Configuration saved");
						m_wizPanel.finish();
						( (ChoicePanel)getParent()).getMenu().reset(false);
						stateChanged(new ChangeEvent(this));
					}
				}
			}
		}
		catch (Exception ex)
		{
			JAPDialog.showErrorDialog(MixConfig.getMainWindow(), null, ex);
		}

		//Set the title with steps
		((ChoicePanel)this.getParent()).setMessageTitle();

	}

	/**
	 * Calls the load-Methode of each Panel
	 * This is necessary if you change the view (expert|wizard)
	 */
	public void load()
	{
		try
		{
			m_wizPanel.load();
		}
		catch (Exception io)
		{
			JAPDialog.showErrorDialog(MixConfig.getMainWindow(),
									   "Switching View to Wizard: Error on loading the MixConfiguration",
									   io);
		}
	}

	/**
	 * Calls the setConfiguration method of ConfigWizardPanel
	 * This is necessary if you change the view (expert|wizard)
	 * @param mixconfig a mix configuration
	 */
	public void setConfiguration(MixConfiguration mixconfig)
	{
		try
		{
			m_wizPanel.setConfiguration(mixconfig);
		}
		catch (Exception io)
		{
			JAPDialog.showErrorDialog(MixConfig.getMainWindow(),
									   "Switching View to Wizard: Error on loading the MixConfiguration",
									   io);
		}
	}

	/**
	 * Calls the reset method of ConfigWizardPanel
	 * If you choose "new" from the MenuBar, this is necessary to change to the first leaf.
	 */
	public void reset()
	{
		m_wizPanel.reset();
	}

	/**
	 * Change button Label to "Next ->"
	 * This is necessary if you coose "new" in the menubar, when the button lable says "finish"
	 */
	public void changeButtonLabelToNext()
	{
		getButtonForward().setText("Next ->");
	}

	public String getHelpContext()
	{
		return m_wizPanel.getHelpContext();
	}

	public int getPageCount()
	{
		return m_wizPanel.getPageCount();
	}

	public int getCurrentPageNr()
	{
		return m_wizPanel.getCurrentPageNr();
	}

	/** Decision of which class an instance is of
	 * Necessary if you change from Wizard -> Expert view
	 */
	public Class getCurrentPageClass()
	{
			return m_wizPanel.getCurrentPage().getClass();
	}

	public Container getHelpExtractionDisplayContext() 
	{
		return null;
	}
}
