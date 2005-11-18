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

package gui;

import java.util.Calendar;
import java.util.Date;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import anon.crypto.Validity;
import gui.JAPJIntField;

public class ValidityDialog extends JAPDialog
{
	private DateTextField m_dateFrom, m_dateTo;

	public ValidityDialog(JAPDialog a_parent, String a_title)
	{
		super(a_parent, a_title);
		init();
	}

	public ValidityDialog(Component parent, String title)
	{
		super(parent, title);
		init();
	}

	private void init()
	{
		setResizable(false);
		createValidityDialog();
	}

	public Validity getValidity()
	{
		if (m_dateFrom == null || m_dateTo == null)
		{
			return null;
		}
		return new Validity(m_dateFrom.getDate(), m_dateTo.getDate());
	}

	private void createValidityDialog()
	{
		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);

		// Constraints for the labels
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JLabel label;

		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){m_dateFrom = null; m_dateTo = null; dispose();}});

		label = new JLabel("Valid from:");
		gbc.gridx = 0;
		gbc.weightx = 0;
		layout.setConstraints(label, gbc);
		getContentPane().add(label);
		gbc.gridx = 1;
		gbc.weightx = 5;
		Date now = new Date(System.currentTimeMillis());
		m_dateFrom = new DateTextField(now);
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
		m_dateTo = new DateTextField(cal2.getTime());
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
					Calendar cal = m_dateFrom.getDate();
					cal.add(Calendar.YEAR, 1);
					m_dateTo.setDate(cal.getTime());
				}
			}
		});
		layout.setConstraints(y1Button, gbc);
		getContentPane().add(y1Button);
		gbc.gridy++;

		GridBagLayout keylayout = new GridBagLayout();
		JPanel keys = new JPanel(keylayout);
		GridBagConstraints kc = new GridBagConstraints();
		kc.weightx = 1;
		kc.gridx = 0;
		kc.gridy = 0;
		kc.gridwidth = 1;
		kc.fill = GridBagConstraints.NONE;
		kc.insets = new Insets(1, 1, 1, 1);
		JButton key = new JButton("OK");
		key.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				try
				{
					getValidity();
					dispose();
				}
				catch (NumberFormatException a_e)
				{
					// there should be an error dialog here saying that one or more date fields are empty
				}
			}
		});
		keylayout.setConstraints(key, kc);
		keys.add(key);
		kc.gridx++;
		key = new JButton("Cancel");
		key.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				m_dateFrom = null;
				m_dateTo = null;
				dispose();
			}
		});
		keylayout.setConstraints(key, kc);
		keys.add(key);

		gbc.gridx = 0;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		layout.setConstraints(keys, gbc);
		getContentPane().add(keys);

		pack();
	}



	private class DateTextField extends JPanel implements ItemListener
	{
		private JAPJIntField day, year;
		private JComboBox month;

		public void itemStateChanged(ItemEvent a_event)
		{
			day.updateBounds();
		}

		class DayBounds implements JAPJIntField.IntFieldBounds
		{
			private final int daysPerMonth[] =
				{
				31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
			public boolean isZeroAllowed()
			{
				return false;
			}

			public int getMaximum()
			{
				int max = (month == null) ? 0 : (month.getSelectedIndex());
				if (max == 1)
				{
					int y = Integer.parseInt(year.getText());
					if ( (y % 4) == 0 && ( (y % 100) != 0 || (y % 400) == 0))
					{
						max = 29;
					}
					else
					{
						max = 28;
					}
				}
				else
				{
					max = daysPerMonth[max];
				}
				return max;
			}
		}


		private void initDateTextField(Date date)
		{
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			JLabel label;

			GridBagLayout layout = new GridBagLayout();
			setLayout(layout);

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.gridx = 0;
			gbc.gridy = 0;

			day = new JAPJIntField(new DayBounds(), true);
			day.setMinimumSize(day.getPreferredSize());
			day.setInt(cal.get(Calendar.DAY_OF_MONTH));
			gbc.weightx = 1;
			gbc.insets.right = 1;
			layout.setConstraints(day, gbc);
			add(day);
			gbc.gridx++;
			label = new JLabel(".");
			gbc.weightx = 0;
			gbc.insets.right = 5;
			gbc.insets.left = 1;
			layout.setConstraints(label, gbc);
			add(label);
			gbc.gridx++;
			gbc.insets.left = 5;

			month =
				new JComboBox(
				new String[]
				{
				"January",
				"February",
				"March",
				"April",
				"May",
				"June",
				"July",
				"August",
				"September",
				"Oktober",
				"November",
				"December"});
			month.setKeySelectionManager(new JComboBox.KeySelectionManager()
			{
				public int selectionForKey(
					char key,
					javax.swing.ComboBoxModel cbm)
				{
					int nr = key - '0';
					if (nr < 0 || nr > 9)
					{
						return -1;
					}

					if (nr < 3 && month.getSelectedIndex() == 0)
					{
						nr = 10 + nr;

					}
					return nr - 1;
				}
			});
			month.addItemListener(this);
			month.setSelectedIndex(cal.get(Calendar.MONTH));
			gbc.weightx = 1;
			layout.setConstraints(month, gbc);
			add(month);
			gbc.gridx++;

			year = new JAPJIntField(3000, true);
			year.setInt(cal.get(Calendar.YEAR));
			gbc.weightx = 1;
			layout.setConstraints(year, gbc);
			add(year);
			gbc.gridx++;
		}

		public DateTextField(Date date)
		{
			super();
			initDateTextField(date);
		}

		public void setDate(Date date)
		{
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			day.setInt(cal.get(Calendar.DAY_OF_MONTH));
			month.setSelectedIndex(cal.get(Calendar.MONTH));
			year.setInt(cal.get(Calendar.YEAR));
		}

		public Calendar getDate() throws NumberFormatException
		{
			Calendar cal = Calendar.getInstance();
			cal.set(year.getInt(), month.getSelectedIndex(), day.getInt());
			return cal;
		}
	}
}