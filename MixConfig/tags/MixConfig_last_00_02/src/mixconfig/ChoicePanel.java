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

import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import anon.util.ResourceLoader;
import mixconfig.wizard.ConfigWizard;
import gui.ImageIconLoader;

/**
 * Shows the StartScreen when you run the "Mix Configuration Tool" in a CardLayout.
* There are two other Cards: a Wizard-Panel and a No-Wizzard-Panel.
* Depending on the Choice you have made, one of this two Panels will be shown.
* * @author wolfgang
 */

public class ChoicePanel extends JPanel implements ActionListener
{
	private static final String LABEL_GO = "Go";
	private static final String CARD_WIZ = "card_mainPanel_wiz";
	private static final String CARD_NO_WIZ = "card_mainPanel_nowiz";
	private static final String CARD_CHOICE = "card_choicePanel";
	private static final String CMD_WIZ="wiz";
	private static final String CMD_NOWIZ="nowiz";
	private static final String CMD_LOAD="load";

	/** The path to the logo to display at top of the choice Panel */
	private static final String PATH_LOGO = new String("mixconfig/anonLogo_start.jpg");

	/** Panel, which contains the Start-Screen where you make your choice.
	 * The Panel is splitted into two Parts
	 * - the first part shows a Logo
	 * - the second part shows the choice items
	 */
	private static JPanel m_mainPanel_choice;
	/** Panel, which contains the Wizard-View */
	private static JPanel m_mainPanel_wiz;
	/** Panel, which contains the No-Wizard-View */
	private static JPanel m_mainPanel_nowiz;
	/** second half of the choice Panel, it shows the choice items */
	private static JPanel m_secondHalfPanel;

	private JFrame m_Parent;
	private CardLayout m_cardLayout;
	private GridLayout m_gridLayout;

	public ChoicePanel(JFrame parent)
	{
		m_Parent = parent;
		m_cardLayout = new CardLayout();
		this.setLayout(m_cardLayout);

	  try {
	    //Create the Panel where you can click Wizard or NoWizard
		m_gridLayout = new GridLayout(2,1);
		m_mainPanel_choice = new JPanel(m_gridLayout);

		//Create Panel with Image and add it to m_mainPanelChoice,
		//it will be in the 1. part of the GridLayout
		addStartImage();

		//Create 2. half Panel and Elements for the 2. half Panel
		m_secondHalfPanel = new JPanel(null);
		JLabel label_wiz   = new JLabel ("Start the Wizard:");
		JLabel label_nowiz = new JLabel ("Create expert configuration:");
		JLabel label_load  = new JLabel ("Load existing configuration");

		JButton b_wiz = new JButton(LABEL_GO);
		b_wiz.setActionCommand(CMD_WIZ);
		b_wiz.addActionListener(this);

		JButton b_nowiz = new JButton(LABEL_GO);
		b_nowiz.setActionCommand(CMD_NOWIZ);
		b_nowiz.addActionListener(this);

		JButton b_load = new JButton(LABEL_GO);
		b_load.setActionCommand(CMD_LOAD);
		b_load.addActionListener(this);


		//add items to panel
		label_wiz.setBounds(300,0,200,30);
		label_nowiz.setBounds(300,50,200,30);
		label_load.setBounds(300,100,200,30);
		b_wiz.setBounds(500,0,60,30);
		b_nowiz.setBounds(500,50,60,30);
		b_load.setBounds(500,100,60,30);


		m_secondHalfPanel.add(label_wiz);
		m_secondHalfPanel.add(b_wiz);
		m_secondHalfPanel.add(label_nowiz);
		m_secondHalfPanel.add(b_nowiz);
		m_secondHalfPanel.add(label_load);
		m_secondHalfPanel.add(b_load);
		//add secondHalfPanel to m_mainPanelChoice
		m_mainPanel_choice.add(m_secondHalfPanel);

		//addStartImage();


		//add Panels to the 3 Cards
		this.add(m_mainPanel_choice, CARD_CHOICE);

		m_mainPanel_wiz   = new ConfigWizard();
		m_mainPanel_nowiz = new ConfigFrame(m_Parent);
		this.add(m_mainPanel_wiz, CARD_WIZ);
		this.add(m_mainPanel_nowiz, CARD_NO_WIZ);
		//show the choicePanel
		m_cardLayout.show(this, CARD_CHOICE);
	  }
	  catch (Exception e)
	  {
	    	MixConfig.handleException(e);
		    System.exit(1);
	  }

	}

	/**
	* Reads the Image from "confWizLogoPath" and adds it to the "m_mainPanel_choice".
	* Call this Funktion before you add the m_secondHalfPanel, then it will be shown
	* in the first half of the Choice-Panel.
	*/
	private void addStartImage() {
		JLabel imageCanvas =
			imageCanvas = new JLabel(ImageIconLoader.loadImageIcon(PATH_LOGO, true));
		//add the picture to the Panel
		imageCanvas.setBounds(300,50,177,116);
		//this will be added into the first part of the GridLayout
		m_mainPanel_choice.add(imageCanvas);
	}


	public void actionPerformed(ActionEvent ae)
	{
	try {
		if (ae.getActionCommand().equals(CMD_WIZ))
		{
			m_cardLayout.show(this, CARD_WIZ);
		}
		else if (ae.getActionCommand().equals(CMD_NOWIZ))
		{
			m_Parent.setJMenuBar( ( (ConfigFrame) m_mainPanel_nowiz).getMenuBar());
			m_cardLayout.show(this, CARD_NO_WIZ);
			m_Parent.pack();
		}
		else if (ae.getActionCommand().equals(CMD_LOAD))
		{
			m_Parent.setJMenuBar( ( (ConfigFrame) m_mainPanel_nowiz).getMenuBar());
			((ConfigFrame)m_mainPanel_nowiz).actionPerformed(new ActionEvent(this,
				ActionEvent.ACTION_PERFORMED, ConfigFrame.CMD_OPEN_FILE));
			m_cardLayout.show(this, CARD_NO_WIZ);
			m_Parent.pack();
		} //end else if
	}
	catch (Exception e)
	{
		MixConfig.handleException(e);
	}


}

}
