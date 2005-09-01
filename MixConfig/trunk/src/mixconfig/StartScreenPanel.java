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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.border.EtchedBorder;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import logging.LogType;
import mixconfig.wizard.WizardLayout;

public class StartScreenPanel  extends WizardLayout implements ActionListener
{
	//Buttonlables
	private static final String BLABEL_NEW    = "Create new configuration...";
	private static final String BLABEL_LOAD   = "Load/Resume existing configuration...";
	private static final String BLABEL_CONFIG = "Configure Cascade for existing configuration...";

	//Commands
	private static final String CMD_NEW = "new";
	private static final String CMD_LOAD = "load";
	private static final String CMD_CONFIG = "config";
	private static final String CMD_EXIT = "exit";

	//Start Text
	private static final String START_TXT = "Welcome to the mix configuration tool! With the help of this program, " +
						   "you can create the configuration file for your AN.ON mix. " +
						   "If you are not experienced in "+
						  "configuring a mix, our wizard will help you with one of the following " +
						  "options:";

	private ChoicePanel m_choicePanel;

	public StartScreenPanel(ChoicePanel choicePanel)
	{
		m_choicePanel = choicePanel;

		//Create Panel and add all the necessary Elements
		JPanel itemPanel = new JPanel(new BorderLayout());
		itemPanel.setBorder(new EtchedBorder());

		JTextPane start_text = new JTextPane();
		start_text.setBackground(getBackground());
		start_text.setEnabled(false);
		start_text.setEditable(false);
		start_text.setText("\n" + START_TXT);
		start_text.setDisabledTextColor(start_text.getCaretColor());
		itemPanel.add(start_text, BorderLayout.NORTH);

		JButton b_new = new JButton(BLABEL_NEW);
		b_new.setActionCommand(CMD_NEW);
		b_new.addActionListener(this);

		JButton b_load = new JButton(BLABEL_LOAD);
		b_load.setActionCommand(CMD_LOAD);
		b_load.addActionListener(this);

		JButton b_config = new JButton(BLABEL_CONFIG);
		b_config.setActionCommand(CMD_CONFIG);
		b_config.addActionListener(this);
		b_config.setEnabled(false);

		//Back Button
		this.getButtonBack().setVisible(false);

		//Forward Button
		this.getButtonForward().setVisible(false);

		//Cancel Button
		this.getButtonCancel().setActionCommand(CMD_EXIT);
		this.getButtonCancel().setText("Exit");
		this.getButtonCancel().addActionListener(this);
		this.getButtonCancel().setEnabled(true);


		// center panel with buttons
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		JPanel panelCenter = new JPanel(layout);
		itemPanel.add(panelCenter, BorderLayout.CENTER);

		TitledGridBagPanel panelButtons = new TitledGridBagPanel();
		panelButtons.addRow(b_new, null, GridBagConstraints.HORIZONTAL);
		panelButtons.addRow(b_load, null, GridBagConstraints.HORIZONTAL);
		panelButtons.addRow(b_config, null, GridBagConstraints.HORIZONTAL);

		constraints.anchor = GridBagConstraints.CENTER;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.NONE;
		panelCenter.add(panelButtons, constraints);


		add(itemPanel, BorderLayout.CENTER);
	}


	public void actionPerformed(ActionEvent ae)
	{
		try
		{
			if (ae.getActionCommand().equals(CMD_NEW))
			{
				m_choicePanel.setWizardVisible();
			}
			else if (ae.getActionCommand().equals(CMD_LOAD))
			{
				m_choicePanel.getMenu().actionPerformed(
								new ActionEvent(
					this, ActionEvent.ACTION_PERFORMED, Menu.CMD_OPEN_FILE_WIZARD));
			}
			else if (ae.getActionCommand().equals(CMD_CONFIG))
			{
				/*** NOT AKTIV ***/
                //m_Parent.setJMenuBar( ( (ConfigFrame) m_mainPanel_expert).getMenuBar());
				//( (ConfigFrame) m_mainPanel_expert).actionPerformed(new ActionEvent(this,
				//      	ActionEvent.ACTION_PERFORMED, ConfigFrame.CMD_OPEN_FILE));
				//m_cardLayout.show(this, CARD_EXPERT);
				//m_Parent.pack();
			}
			else if (ae.getActionCommand().equals(CMD_EXIT))
			{
				MixConfig.getMainWindow().dispose();
			}
		}
		catch (Exception e)
		{
			MixConfig.handleError(e, null, LogType.GUI);
		}

	}

}
