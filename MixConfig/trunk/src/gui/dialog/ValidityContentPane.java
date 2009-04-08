/*
 Copyright (c) 2000-2005, The JAP-Team
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
package gui.dialog;

import gui.DatePanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;

import logging.LogType;
import anon.crypto.Validity;

public class ValidityContentPane extends DialogContentPane implements
	DialogContentPane.IWizardSuitable
{
	private DatePanel m_dateFrom, m_dateTo;

	public ValidityContentPane(JAPDialog a_parent)
	{
		this(a_parent, null);
	}

	public ValidityContentPane(JAPDialog a_parent, DialogContentPane a_previousContentPane)
	{
		super(a_parent,
			  new Layout("Please choose a validity", MESSAGE_TYPE_QUESTION),
			  new DialogContentPaneOptions(OPTION_TYPE_OK_CANCEL, a_previousContentPane));
		setDefaultButtonOperation(BUTTON_OPERATION_WIZARD);

		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);

		// Constraints for the labels
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JLabel label;

		label = new JLabel("Valid from:");
		gbc.gridx = 0;
		gbc.weightx = 0;
		layout.setConstraints(label, gbc);
		getContentPane().add(label);
		gbc.gridx = 1;
		gbc.weightx = 5;
		Date now = new Date(System.currentTimeMillis());
		m_dateFrom = new DatePanel(now);
		layout.setConstraints(m_dateFrom, gbc);
		getContentPane().add(m_dateFrom);
		gbc.gridx = 2;
		gbc.weightx = 1;
		JButton nowButton = new JButton("Now");
		nowButton.setActionCommand("Now");
		nowButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				if (ev.getActionCommand().equals("Now"))
				{
					m_dateFrom.setDate(new Date(System.currentTimeMillis()));
				}
			}
		});
		layout.setConstraints(nowButton, gbc);
		getContentPane().add(nowButton);
		gbc.gridy++;

		label = new JLabel("Valid to:");
		gbc.gridx = 0;
		gbc.weightx = 0;
		layout.setConstraints(label, gbc);
		getContentPane().add(label);
		gbc.gridx = 1;
		gbc.weightx = 5;

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(now);
		cal2.add(Calendar.YEAR, 1);
		m_dateTo = new DatePanel(cal2.getTime());
		layout.setConstraints(m_dateTo, gbc);
		getContentPane().add(m_dateTo);
		gbc.gridx = 2;
		gbc.weightx = 1;
		JButton y1Button = new JButton("1 Year");
		y1Button.setActionCommand("1 Year");
		y1Button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				if (ev.getActionCommand().equals("1 Year"))
				{
					Calendar cal = m_dateFrom.getCalendar();
					cal.add(Calendar.YEAR, 1);
					m_dateTo.setDate(cal.getTime());
				}
			}
		});
		layout.setConstraints(y1Button, gbc);
		getContentPane().add(y1Button);
/*		gbc.gridy++;
		gbc.weighty = 10;
		gbc.weightx = 10;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.VERTICAL;
		getContentPane().add(new JLabel(), gbc);
	*/
	}

	public CheckError[] checkYesOK()
	{
		try
		{
			getValidity();
		}
		catch (NumberFormatException a_e)
		{
			return new CheckError[]{new CheckError("One or more date fields are empty", LogType.GUI)};
		}
		return null;
	}

	public Validity getValidity()
	{
		if (RETURN_VALUE_CLOSED == getButtonValue() || RETURN_VALUE_CANCEL == getButtonValue() ||
			m_dateFrom == null || m_dateTo == null)
		{
			return null;
		}
		return new Validity(m_dateFrom.getCalendar(), m_dateTo.getCalendar());
	}
}
