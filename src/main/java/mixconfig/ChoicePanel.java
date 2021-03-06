/*
 Copyright (c) 2000 - 2004, The JAP-Team
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

import gui.dialog.JAPDialog;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import logging.LogType;
import mixconfig.panels.StartScreenPanel;
import mixconfig.wizard.ConfigWizard;

/**
 * Shows the StartScreen when you run the "Mix Configuration Tool" in a CardLayout.
 * There are two other Cards: a Wizard-Panel and a No-Wizzard-Panel.
 * Depending on the Choice you have made, one of this two Panels will be shown.
 * * @author wolfgang
 */

public class ChoicePanel extends JPanel
{
	// Make the default size available to other classes, esp. MixConfig to set the size of m_mainWindow
	// XXX: Rather move this constant to the class MixConfig?
	public static final Dimension DEFAULT_SIZE = new Dimension(785, 600);

	public static final String CARD_WIZ = "card_mainPanel_wiz";
	public static final String CARD_EXPERT = "card_mainPanel_expert";
	public static final String CARD_CHOICE = "card_choicePanel";

	public static final String WIZARD = "wizard";
	public static final String EXPERT = "expert";
	public static final String START = "start";
	private String activeCard = START;

	/** Panel, which contains the Start-Screen where you make your choice.
	 * The Panel is splitted into two Parts
	 * - the first part shows a Logo
	 * - the second part shows the choice items
	 */
	private static StartScreenPanel m_startScreenPanel;
	/** Panel, which contains the Wizard-View */
	private static ConfigWizard m_mainPanel_wiz;
	/** Panel, which contains the No-Wizard-View */
	private static ConfigFrame m_mainPanel_expert;
	/** second half of the choice Panel, it shows the choice items */

	private Menu m_menu;
	private JFrame m_Parent;
	private CardLayout m_cardLayout;

	public ChoicePanel(JFrame parent, JRootPane rootPane)
	{
		this(parent, rootPane, CARD_CHOICE);
	}
	
	public ChoicePanel(JFrame parent, JRootPane rootPane, String startPanel)
	{
		//choose the start panel
		String panelToStart = startPanel;
		if(panelToStart.equals(CARD_WIZ))
		{
			activeCard = WIZARD;
		}
		else if (panelToStart.equals(CARD_EXPERT))
		{
			activeCard = EXPERT;
		}
		//default is CARD_CHOICE
		else 
		{
			panelToStart = CARD_CHOICE;
			activeCard = START;
		}
		//set size
		setDefaultSize();

		m_Parent = parent;
		if (m_Parent != null)
		{
			m_Parent.setResizable(true);
		}
		
		// Add a listener for catching resize events
		this.addComponentListener(new java.awt.event.ComponentAdapter() {
	        public void componentResized(ComponentEvent event) 
			{
			  ChoicePanel component = (ChoicePanel)event.getComponent();
			  if (component.getWidth() < ChoicePanel.DEFAULT_SIZE.width ||
			     component.getHeight() < ChoicePanel.DEFAULT_SIZE.height)
			  {
				// Reset the size to default
			    component.setDefaultSize();
			  }
			}
		});

		m_cardLayout = new CardLayout();
		this.setLayout(m_cardLayout);

		try
		{
			/* Create the Panel where you can choose:
			   - Create new Configuration
			   - Load existing Configuration
			   - Configure Cascade
			 */


			//add Panels to the 3 Cards

			m_startScreenPanel = new StartScreenPanel(this);
			m_mainPanel_wiz = new ConfigWizard();
			m_mainPanel_expert = new ConfigFrame(m_Parent);
			this.add(m_startScreenPanel, CARD_CHOICE);
			this.add(m_mainPanel_wiz, CARD_WIZ);
			this.add(m_mainPanel_expert, CARD_EXPERT);

			//show the StartScreen
			m_cardLayout.show(this, panelToStart);

			//show the Menu
			m_menu = new Menu(parent, rootPane, m_mainPanel_wiz, m_mainPanel_expert);

		}
		catch (Exception e)
		{
			JAPDialog.showErrorDialog(MixConfig.getMainWindow(), null, e);
		}
	}

	/**
	 * There are two relevant Cards in Choice-Panel:
	 * 1.) CARD_WIZ
	 * 2.) CARD_EXPERT
	 * @return the card which is on TOP
	 */
	protected String getActiveCard()
	{
		return activeCard;
	}

	/**
	 * Set the Wizard-Card visible
	 */
	public void setWizardVisible()
	{
		m_cardLayout.show(this, CARD_WIZ);
		activeCard = WIZARD;
		m_menu.checkUnuseableMenuItem();
		this.setMessageTitle();
	}

	/**
	 * Set the Expert-Card visible
	 */
	protected void setExpertVisible()
	{
		m_cardLayout.show(this, CARD_EXPERT);
		activeCard = EXPERT;
		m_menu.checkUnuseableMenuItem();
		this.setMessageTitle();
	}

	/**
	 * Set the Start Screen visible
	 */
	public void setStartScreenVisible()
	{
		m_cardLayout.show(this, CARD_CHOICE);
		activeCard = START;
		m_menu.checkUnuseableMenuItem();
		this.setMessageTitle();
	}

	/**
	 * @return a reference to the mneu
	 */
	public Menu getMenu()
	{
		return m_menu;
	}

	public void setDefaultSize()
	{
		setSize(DEFAULT_SIZE);
		setMinimumSize(DEFAULT_SIZE);
		setPreferredSize(DEFAULT_SIZE);

		if (m_Parent != null)
		{
			Point location = m_Parent.getLocation();
			m_Parent.pack();
			m_Parent.setLocation(location);
		}
	}

	/**
	 * Set the message title in the window
	 * Automatically set the correct step numbers
	 */
	public void setMessageTitle()
	{
		try
		{
			JFrame parentFrame = (JFrame) m_mainPanel_wiz.getRootPane().getParent();
			//String title = parentFrame.getTitle();
			int max = m_mainPanel_wiz.getPageCount();
			int current = m_mainPanel_wiz.getCurrentPageNr() + 1;

			if (this.getActiveCard().equals(WIZARD))
			{
				String newTitle = "(" + String.valueOf(current) + "/" + String.valueOf(max) + ")";
				newTitle = "Mix Configuration Tool Wizard " + newTitle;
				parentFrame.setTitle(newTitle);
			}
			else if (this.getActiveCard().equals(EXPERT))
			{
				parentFrame.setTitle("Mix Configuration Tool");
			}
			else if (this.getActiveCard().equals(START))
			{
				parentFrame.setTitle("Mix Configuration Tool");
			}

			//Set file name, if it is given
			if (MixConfig.getCurrentFileName() != null)
			{
				String currentTitle = parentFrame.getTitle();
				parentFrame.setTitle(currentTitle + " - " + MixConfig.getCurrentFileName());
			}
		}
		catch (Throwable t)
		{
		}
	}
}
