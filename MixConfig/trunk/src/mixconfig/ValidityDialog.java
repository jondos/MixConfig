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

import java.util.Calendar;
import java.util.Date;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;


class ValidityDialog extends JDialog
{
	public DateTextField from, to;

	protected void createValidityDialog()
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

		label = new JLabel("Valid from:");
		gbc.gridx = 0;
		gbc.weightx = 0;
		layout.setConstraints(label, gbc);
		getContentPane().add(label);
		gbc.gridx = 1;
		gbc.weightx = 5;
		Date now = new Date(System.currentTimeMillis());
		from = new DateTextField(now);
		layout.setConstraints(from, gbc);
		getContentPane().add(from);
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
					from.setDate(new Date(System.currentTimeMillis()));
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

		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		cal.add(Calendar.YEAR, 1);
		to = new DateTextField(cal.getTime());
		layout.setConstraints(to, gbc);
		getContentPane().add(to);
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
					Calendar cal = Calendar.getInstance();
					cal.setTime(from.getDate());
					cal.add(Calendar.YEAR, 1);
					to.setDate(cal.getTime());
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
		kc.fill = GridBagConstraints.HORIZONTAL;
		kc.insets = new Insets(1, 1, 1, 1);
		JButton key = new JButton("OK");
		key.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				dispose();
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
				from = null;
				to = null;
				dispose();
			}
		});
		keylayout.setConstraints(key, kc);
		keys.add(key);

		gbc.gridx = 0;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(keys, gbc);
		getContentPane().add(keys);

		pack();
	}

	ValidityDialog(Frame parent, String title)
	{
		super(parent, title, true);
		createValidityDialog();
		setLocationRelativeTo(parent);
	}
	
	class DateTextField extends JPanel
	{
		private JTextField hour, min, sec, day, year;
		private JComboBox month;

		class DayDocument extends PlainDocument
		{
			Component which;

			private final int daysPerMonth[] =
				{
				31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

			DayDocument(Component comp)
			{
				super();
				which = comp;
			}

			DayDocument()
			{
				super();
				which = null;
			}

			public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException
			{
				String p1 = getText(0, offset);
				String p2 = getText(offset, getLength() - offset);
				String res = "";

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
				for (int i = 0; i < str.length(); i++)
				{
					if (!Character.isDigit(str.charAt(i)))
					{
						java.awt.Toolkit.getDefaultToolkit().beep();
					}
					else
					{
						String sstr = str.substring(i, i + 1);
						int val = Integer.parseInt(p1 + res + sstr + p2, 10);
						if (max > 0 && val > max)
						{
							java.awt.Toolkit.getDefaultToolkit().beep();
						}
						else
						{
							res += sstr;
						}
					}
				}
				super.insertString(offset, res, attr);
				if (which != null
					&& max > 0
					&& getLength() > 0
					&& 10 * Integer.parseInt(getText(0, getLength()), 10) > max)
				{
					which.transferFocus();
				}
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

			day = new JTextField(2);
			day.setMinimumSize(day.getPreferredSize());
			day.setDocument(new DayDocument(day));
			day.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
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
			month.setSelectedIndex(cal.get(Calendar.MONTH));
			gbc.weightx = 1;
			layout.setConstraints(month, gbc);
			add(month);
			gbc.gridx++;

			year = new JTextField(4);
			year.setMinimumSize(year.getPreferredSize());
			year.setDocument(new IntegerDocument(year));
			year.setText(Integer.toString(cal.get(Calendar.YEAR)));
			gbc.weightx = 1;
			layout.setConstraints(year, gbc);
			add(year);
			gbc.gridx++;

			hour = new JTextField(2);
			hour.setMinimumSize(day.getPreferredSize());
			hour.setDocument(new IntegerDocument(23, hour));
			hour.setText(Integer.toString(cal.get(Calendar.HOUR)));
			gbc.weightx = 1;
			gbc.insets.right = 1;
			layout.setConstraints(hour, gbc);
			add(hour);
			gbc.gridx++;
			label = new JLabel(":");
			gbc.weightx = 0;
			gbc.insets.left = 1;
			layout.setConstraints(label, gbc);
			add(label);
			gbc.gridx++;
			min = new JTextField(2);
			min.setMinimumSize(day.getPreferredSize());
			min.setDocument(new IntegerDocument(59, min));
			min.setText(Integer.toString(cal.get(Calendar.MINUTE)));
			gbc.weightx = 1;
			layout.setConstraints(min, gbc);
			add(min);
			gbc.gridx++;
			label = new JLabel(":");
			gbc.weightx = 0;
			layout.setConstraints(label, gbc);
			add(label);
			gbc.gridx++;
			sec = new JTextField(2);
			sec.setMinimumSize(day.getPreferredSize());
			sec.setDocument(new IntegerDocument(59, sec));
			sec.setText(Integer.toString(cal.get(Calendar.SECOND)));
			gbc.weightx = 1;
			layout.setConstraints(sec, gbc);
			add(sec);
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
			day.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
			month.setSelectedIndex(cal.get(Calendar.MONTH));
			year.setText(Integer.toString(cal.get(Calendar.YEAR)));
			hour.setText(Integer.toString(cal.get(Calendar.HOUR)));
			min.setText(Integer.toString(cal.get(Calendar.MINUTE)));
			sec.setText(Integer.toString(cal.get(Calendar.SECOND)));
		}

		public Date getDate()
		{
			Calendar cal = Calendar.getInstance();
			cal.set(
				Integer.parseInt(year.getText()),
				month.getSelectedIndex(),
				Integer.parseInt(day.getText()),
				Integer.parseInt(hour.getText()),
				Integer.parseInt(min.getText()),
				Integer.parseInt(sec.getText()));
			return cal.getTime();
		}
	}	
}
