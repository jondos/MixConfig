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
package mixconfig.panels;

import gui.JAPHtmlMultiLineLabel;
import gui.TitledGridBagPanel;
import gui.dialog.JAPDialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import anon.util.JAPMessages;

import logging.LogType;
import mixconfig.ChoicePanel;
import mixconfig.Menu;
import mixconfig.MixConfig;
import mixconfig.wizard.WizardLayout;

public class StartScreenPanel extends WizardLayout implements ActionListener
{
	private static final String MSG_NEW = StartScreenPanel.class.getName() + "_createNew";
	private static final String MSG_LOAD = StartScreenPanel.class.getName() + "_load";
	private static final String MSG_CONFIGURE = StartScreenPanel.class.getName() + "_configureCascade";
	private static final String MSG_WELCOME = StartScreenPanel.class.getName() + "_welcome";
	private static final String MSG_EXIT = StartScreenPanel.class.getName() + "_exit";

	private JButton m_btnNew, m_btnLoad, m_btnConfigure;
	private ChoicePanel m_choicePanel;

	public StartScreenPanel(ChoicePanel choicePanel)
	{
		m_choicePanel = choicePanel;

		//Create Panel and add all the necessary Elements
		JPanel itemPanel = new JPanel(new BorderLayout());
		itemPanel.setBorder(new EtchedBorder());

		itemPanel.add(new JAPHtmlMultiLineLabel(JAPMessages.getString(MSG_WELCOME)), BorderLayout.NORTH);

		m_btnNew = new JButton(JAPMessages.getString(MSG_NEW));
		m_btnNew.addActionListener(this);

		m_btnLoad = new JButton(JAPMessages.getString(MSG_LOAD));
		m_btnLoad.addActionListener(this);

		m_btnConfigure = new JButton(JAPMessages.getString(MSG_CONFIGURE));
		m_btnConfigure.addActionListener(this);
		m_btnConfigure.setEnabled(false);

		//Back Button
		this.getButtonBack().setVisible(false);

		//Forward Button
		this.getButtonForward().setVisible(false);

		//Cancel Button
		this.getButtonCancel().setText(JAPMessages.getString(MSG_EXIT));
		this.getButtonCancel().addActionListener(this);
		this.getButtonCancel().setEnabled(true);

		// center panel with buttons
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		JPanel panelCenter = new JPanel(layout);
		itemPanel.add(panelCenter, BorderLayout.CENTER);

		TitledGridBagPanel panelButtons = new TitledGridBagPanel();
		panelButtons.addRow(m_btnNew, null, GridBagConstraints.HORIZONTAL);
		panelButtons.addRow(m_btnLoad, null, GridBagConstraints.HORIZONTAL);
		panelButtons.addRow(m_btnConfigure, null, GridBagConstraints.HORIZONTAL);

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
			if (ae.getSource() == m_btnNew)
			{
				m_choicePanel.setWizardVisible();
			}
			else if (ae.getSource() == m_btnLoad)
			{
				m_choicePanel.getMenu().actionPerformed(
								new ActionEvent(
					this, ActionEvent.ACTION_PERFORMED, Menu.CMD_OPEN_FILE_WIZARD));
			}
			else if (ae.getSource() == m_btnConfigure)
			{
				/*** NOT AKTIV ***/
                //m_Parent.setJMenuBar( ( (ConfigFrame) m_mainPanel_expert).getMenuBar());
				//( (ConfigFrame) m_mainPanel_expert).actionPerformed(new ActionEvent(this,
				//      	ActionEvent.ACTION_PERFORMED, ConfigFrame.CMD_OPEN_FILE));
				//m_cardLayout.show(this, CARD_EXPERT);
				//m_Parent.pack();
			}
			else if (ae.getSource() == getButtonCancel())
			{
				MixConfig.getMainWindow().dispose();
			}
		}
		catch (Exception e)
		{
			JAPDialog.showErrorDialog(MixConfig.getMainWindow(), null, LogType.GUI, e);
		}

	}

	public String getHelpContext()
	{
		return getClass().getName();
	}
	
	public Container getHelpExtractionDisplayContext() {
		// TODO Auto-generated method stub
		return null;
	}

}
