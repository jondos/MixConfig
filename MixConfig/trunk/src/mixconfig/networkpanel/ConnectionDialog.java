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
package mixconfig.networkpanel;

import java.util.Vector;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import anon.infoservice.ListenerInterface;
import gui.JAPMessages;
import gui.JAPJIntField;
import javax.swing.JComponent;
import mixconfig.MixOnCDPanel;

abstract class ConnectionDialog extends JDialog
{
	private JTextField nametext;
	private JAPJIntField iptext;
	private ButtonGroup ssl, m_bttngrpType;
	private JComboBox m_type;
	private JLabel namelabel, iplabel;
	private JComponent m_firstone;
	private boolean m_bMixOnCD;

	abstract protected String getType();

	protected ConnectionData getData()
	{
		boolean bHidden = false;
		boolean bVirtual = false;
		if (m_type != null && m_type.getSelectedIndex() == 1)
		{
			bVirtual = true;
		}
		if (m_type != null && m_type.getSelectedIndex() == 2)
		{
			bHidden = true;
		}
		if (m_bttngrpType.getSelection().getActionCommand().equals("TCP"))
		{
			if (!m_bMixOnCD)
			{
				return new ConnectionData(getType(),
										  ssl.getSelection().getActionCommand().equals("SSL") ?
										  ConnectionData.SSL_TCP : ConnectionData.RAW_TCP,
										  nametext.getText(),
										  (iptext.getText().length() == 0) ? 0 :
										  Integer.parseInt(iptext.getText()),
										  0,
										  bVirtual,
										  bHidden);
			}
			else
			{
				return new ConnectionData(getType(),
										  ssl.getSelection().getActionCommand().equals("SSL") ?
										  ConnectionData.SSL_TCP : ConnectionData.RAW_TCP,
										  JAPMessages.getString(MixOnCDPanel.MSG_CONFIGURED_BY_MIXONCD),
										  (iptext.getText().length() == 0) ? 0 :
										  Integer.parseInt(iptext.getText()),
										  0,
										  bVirtual,
										  bHidden);

			}
		}
		else
		{
			return new ConnectionData(getType(),
									  ssl.getSelection().getActionCommand().equals("SSL") ?
									  ConnectionData.SSL_UNIX : ConnectionData.RAW_UNIX,
									  nametext.getText());
		}
	}

	protected void addTransport(final ConnectionData data, GridBagLayout layout, GridBagConstraints lc,
								GridBagConstraints rc)
	{
		JLabel label = new JLabel("Transport");
		layout.setConstraints(label, lc);
		getContentPane().add(label);
		lc.gridy += 2;

		int ttype;
		if (data == null)
		{
			ttype = ConnectionData.RAW_TCP;
		}
		else
		{
			ttype = data.getTransport();
		}
		rc.anchor = GridBagConstraints.CENTER;
		rc.gridwidth = 3;
		m_bttngrpType = new ButtonGroup();
		JRadioButton t = new JRadioButton("TCP", (ttype & ConnectionData.UNIX) == 0);
		if (m_firstone == null)
		{
			m_firstone = t;
		}
		t.setActionCommand("TCP");
		ActionListener tcpunixswitcher = new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				boolean is_tcp = ev.getActionCommand().equals("TCP");
				namelabel.setText(is_tcp ? "Host name" : "File name");
				iplabel.setEnabled(is_tcp);
				iptext.setEnabled(is_tcp);
			}
		};

		t.addActionListener(tcpunixswitcher);
		layout.setConstraints(t, rc);
		getContentPane().add(t);
		m_bttngrpType.add(t);
		rc.gridy++;
		t = new JRadioButton("Unix", (ttype & ConnectionData.UNIX) != 0);
		t.setActionCommand("Unix");
		t.addActionListener(tcpunixswitcher);
		layout.setConstraints(t, rc);
		getContentPane().add(t);
		m_bttngrpType.add(t);

		rc.gridy--;
		rc.gridx += 3;
		rc.gridwidth = 1;
		rc.gridheight = 2;
		JSeparator vertLine = new JSeparator(JSeparator.VERTICAL);
		rc.fill = GridBagConstraints.VERTICAL;
		layout.setConstraints(vertLine, rc);
		rc.fill = GridBagConstraints.NONE;
		getContentPane().add(vertLine);

		rc.gridx++;
		rc.gridwidth = 3;
		rc.gridheight = 1;
		ssl = new ButtonGroup();
		t = new JRadioButton("Raw", (ttype & ConnectionData.SSL) == 0);
		t.setActionCommand("Raw");
		layout.setConstraints(t, rc);
		getContentPane().add(t);
		ssl.add(t);
		rc.gridy++;
		t = new JRadioButton("SSL", (ttype & ConnectionData.SSL) != 0);
		t.setActionCommand("SSL");
		t.setEnabled(false);
		layout.setConstraints(t, rc);
		getContentPane().add(t);
		ssl.add(t);
		rc.gridy++;
		rc.gridx -= 4;
	}

	protected final ActionListener nextfocusaction = new ActionListener()
	{
		public void actionPerformed(ActionEvent evt)
		{
			( (Component) evt.getSource()).transferFocus();
		}
	};

	protected void addName(final ConnectionData data, GridBagLayout layout, GridBagConstraints lc,
						   GridBagConstraints rc, boolean a_enabled)
	{
		boolean isHost = m_bttngrpType.getSelection().getActionCommand().equals("TCP");
		namelabel = new JLabel(isHost ? "Host name" : "File name");
		layout.setConstraints(namelabel, lc);
		getContentPane().add(namelabel);
		lc.gridy++;

		rc.anchor = GridBagConstraints.WEST;
		rc.gridwidth = 7;
		if (data == null)
		{
			nametext = new JTextField(14);
		}
		else
		{
			nametext = new JTextField(String.valueOf(data.getName()), 14);
		}
		nametext.setMinimumSize(nametext.getPreferredSize());
		layout.setConstraints(nametext, rc);
		getContentPane().add(nametext);
		nametext.addActionListener(nextfocusaction);
		rc.gridy++;
		if (m_firstone == null)
		{
			m_firstone = nametext;
		}
		nametext.setEnabled(a_enabled);
		namelabel.setEnabled(a_enabled);

	}

	protected void addPort(final ConnectionData data, GridBagLayout layout, GridBagConstraints lc,
						   GridBagConstraints rc)
	{
		boolean isHost = m_bttngrpType.getSelection().getActionCommand().equals("TCP");
		iplabel = new JLabel("Port");
		layout.setConstraints(iplabel, lc);
		getContentPane().add(iplabel);
		iplabel.setEnabled(isHost);
		lc.gridy++;

		rc.gridwidth = 7;
		iptext = new JAPJIntField(ListenerInterface.PORT_MAX_VALUE, true);
		iptext.setMinimumSize(iptext.getPreferredSize());
		if (isHost && data != null)
		{
			iptext.setInt(data.getPort());
		}
		if (isHost && data == null)
		{
			iptext.setInt(6544);
		}

		layout.setConstraints(iptext, rc);
		getContentPane().add(iptext);
		iptext.addActionListener(nextfocusaction);
		iptext.setEnabled(isHost);
		rc.gridy++;
		if (m_firstone == null)
		{
			m_firstone = iptext;
		}
	}

	protected void addOptions(final ConnectionData data, GridBagLayout layout, GridBagConstraints lc,
							  GridBagConstraints rc, boolean a_enabled)
	{
		JPanel p = new JPanel(new GridLayout(1, 2));
		p.setToolTipText(
			"These are two additional options which are useful if you are behind a NAT gateway  for instance.");
		p.setBorder(new TitledBorder("Visibility"));
		lc.gridwidth = 8;
		lc.gridx = 0;
		lc.anchor = lc.NORTHEAST;
		lc.fill = rc.HORIZONTAL;
		lc.weightx = 1.0;
		layout.setConstraints(p, lc);
		getContentPane().add(p);
		lc.gridy++;
		rc.gridy++;
		Vector items = new Vector();
		JLabel virtual = new JLabel("Virtual");
		virtual.setToolTipText("Virtual - the Mix will not bind or listen on this interface, but the information is transferred to the InfoService");
		items.addElement("Default");
		items.addElement("Virtual");
		items.addElement("Hidden");
		m_type = new JComboBox(items);
		m_type.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (m_type.getSelectedIndex() == 0)
				{
					m_type.setToolTipText("");
				}
				else if (m_type.getSelectedIndex() == 1)
				{
					m_type.setToolTipText("Virtual - the Mix will not bind or listen on this interface, but the information is transferred to the InfoService");
				}
				else if (m_type.getSelectedIndex() == 2)
				{
					m_type.setToolTipText(
						"Hidden - information about this interface is not propagated to the InfoService.");
				}
			}
		});

		if (data != null)
		{
			if (data.isVirtual())
			{
				m_type.setSelectedIndex(1);
			}
			else if (data.isHidden())
			{
				m_type.setSelectedIndex(2);
			}
			else
			{
				m_type.setSelectedIndex(0);
			}
		}
		m_type.setEnabled(a_enabled);
		p.add(m_type);
	}

	protected void addKeys(final ConnectionData data, final ConnectionTableModel where, GridBagLayout layout,
						   GridBagConstraints lc, GridBagConstraints rc)
	{
		GridBagLayout keylayout = new GridBagLayout();
		JPanel keys = new JPanel(keylayout);
		GridBagConstraints kc = new GridBagConstraints();
		kc.weightx = 1;
		kc.gridx = 0;
		kc.gridy = 0;
		kc.gridwidth = 1;
		kc.fill = GridBagConstraints.HORIZONTAL;
		kc.insets = new Insets(1, 1, 1, 1);
		JButton key;
		if (data == null)
		{
			key = new JButton("Add");
			key.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ev)
				{
					where.addData(getData());
					dispose();
				}
			});
		}
		else
		{
			key = new JButton("Change");
			key.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ev)
				{
					where.changeData(getData(), data);
					dispose();
				}
			});
		}
		key.setActionCommand("Ok");
		keylayout.setConstraints(key, kc);
		keys.add(key);
		kc.gridx++;
		key = new JButton("Cancel");
		key.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				dispose();
			}
		});
		keylayout.setConstraints(key, kc);
		keys.add(key);
		if (m_firstone == null)
		{
			m_firstone = key;
		}
		lc.gridwidth = 8;
		lc.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(keys, lc);
		getContentPane().add(keys);
		lc.gridy++;
		rc.gridy++;
	}

	ConnectionDialog(Frame parent, String title)
	{
		super(parent, title, true);
		this.setResizable(false);
	}

	protected void setMixOnCDEnabled(boolean a_bEnabled)
	{
		m_bMixOnCD = a_bEnabled;
	}

	protected JComponent getFirstone()
	{
		return m_firstone;
	}

	protected void setFirstone(JComponent a_firstone)
	{
		m_firstone = a_firstone;
	}
}
