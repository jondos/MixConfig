package mixconfig.networkpanel;

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
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import mixconfig.IntegerDocument;

abstract class ConnectionDialog extends JDialog
{
	private JTextField nametext, iptext[];
	private ButtonGroup ssl, m_bttngrpType;
	private JCheckBox m_checkboxVirtual, m_checkboxHidden;
	private JLabel namelabel, iplabel[];
	protected javax.swing.JComponent firstone;
	abstract protected String getType();

	protected ConnectionData getData()
	{
		boolean bHidden = false;
		boolean bVirtual = false;
		if (m_checkboxVirtual != null && m_checkboxVirtual.isSelected())
		{
			bVirtual = true;
		}
		if (m_checkboxHidden != null && m_checkboxHidden.isSelected())
		{
			bHidden = true;
		}
		if (m_bttngrpType.getSelection().getActionCommand().equals("TCP"))
		{
			int[] ips = new int[4];
			for (int i = 0; i < 4; i++)
			{
				if (iptext[i].getText().length() == 0)
				{
					ips = null;
					break;
				}
				else
				{
					ips[i] = Integer.parseInt(iptext[i].getText(), 10);
				}
			}

			return new ConnectionData(getType(),
									  ssl.getSelection().getActionCommand().equals("SSL") ?
									  ConnectionData.SSL_TCP : ConnectionData.RAW_TCP,
									  nametext.getText(),
									  ips,
									  (iptext[4].getText().length() == 0) ? 0 :
									  Integer.parseInt(iptext[4].getText(), 10),
									  0,
									  bVirtual,
									  bHidden);
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
		if (firstone == null)
		{
			firstone = t;
		}
		t.setActionCommand("TCP");
		ActionListener tcpunixswitcher = new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				boolean is_tcp = ev.getActionCommand().equals("TCP");
				namelabel.setText(is_tcp ? "Host Name" : "File Name");
				for (int i = 0; i < 5; i++)
				{
					iplabel[i].setEnabled(is_tcp);
					iptext[i].setEnabled(is_tcp);
				}
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
						   GridBagConstraints rc)
	{
		boolean isHost = m_bttngrpType.getSelection().getActionCommand().equals("TCP");
		namelabel = new JLabel(isHost ? "Host Name" : "File Name");
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
		if (firstone == null)
		{
			firstone = nametext;
		}
	}

	protected void addIP(final ConnectionData data, GridBagLayout layout, GridBagConstraints lc,
						 GridBagConstraints rc)
	{
		boolean isHost = m_bttngrpType.getSelection().getActionCommand().equals("TCP");
		iplabel = new JLabel[5];
		iptext = new JTextField[5];
		int[] ips;
		if (data != null)
		{
			ips = data.getIPAddr();
		}
		else
		{
			ips = null;
		}
		GridBagConstraints ic = new GridBagConstraints();
		ic.anchor = GridBagConstraints.WEST;
		ic.insets = new Insets(1, 5, 1, 1);
		ic.gridx = lc.gridx;
		ic.gridy = lc.gridy;
		ic.weightx = 0;
		for (int i = 0; i < 4; i++)
		{
			iplabel[i] = new JLabel( (i == 0) ? "IP Address" : ".");
			layout.setConstraints(iplabel[i], (i == 0) ? lc : ic);
			getContentPane().add(iplabel[i]);
			iplabel[i].setEnabled(isHost);
			ic.gridx++;

			iptext[i] = new JTextField(3);
			iptext[i].setMinimumSize(iptext[i].getPreferredSize());
			iptext[i].setDocument(new IntegerDocument(255, iptext[i]));
			if (ips != null)
			{
				iptext[i].setText(Integer.toString(ips[i], 10));
			}
			layout.setConstraints(iptext[i], ic);
			getContentPane().add(iptext[i]);
			iptext[i].addActionListener(nextfocusaction);
			iptext[i].setEnabled(isHost);
			ic.gridx++;
			if (i == 0)
			{
				ic.insets.left = 1;
			}
		}
		lc.gridy++;
		rc.gridy++;
		if (firstone == null)
		{
			firstone = iptext[0];
		}
	}

	protected void addPort(final ConnectionData data, GridBagLayout layout, GridBagConstraints lc,
						   GridBagConstraints rc)
	{
		boolean isHost = m_bttngrpType.getSelection().getActionCommand().equals("TCP");
		iplabel[4] = new JLabel("Port");
		layout.setConstraints(iplabel[4], lc);
		getContentPane().add(iplabel[4]);
		iplabel[4].setEnabled(isHost);
		lc.gridy++;

		rc.gridwidth = 7;
		iptext[4] = new JTextField(5);
		iptext[4].setMinimumSize(iptext[4].getPreferredSize());
		iptext[4].setDocument(new IntegerDocument(65535));
		if (isHost && data != null)
		{
			iptext[4].setText(String.valueOf(data.getPort()));
		}
		layout.setConstraints(iptext[4], rc);
		getContentPane().add(iptext[4]);
		iptext[4].addActionListener(nextfocusaction);
		iptext[4].setEnabled(isHost);
		rc.gridy++;
		if (firstone == null)
		{
			firstone = iptext[4];
		}
	}

	protected void addOptions(final ConnectionData data, GridBagLayout layout, GridBagConstraints lc,
							  GridBagConstraints rc)
	{
		JPanel p = new JPanel(new GridLayout(1, 2));
		p.setToolTipText(
			"This are two additional options, which are useful if you are for instance behind a NAT gateway.");
		p.setBorder(new TitledBorder("Additional Options "));
		lc.gridwidth = 8;
		lc.gridx = 0;
		lc.anchor = lc.NORTHEAST;
		lc.fill = rc.HORIZONTAL;
		lc.weightx = 1.0;
		layout.setConstraints(p, lc);
		getContentPane().add(p);
		lc.gridy++;
		rc.gridy++;
		m_checkboxVirtual = new JCheckBox("Virtual");
		m_checkboxVirtual.setToolTipText("Virutal - the Mix will not bind or listen on this interface, but the information is transferred to the InfoService");
		m_checkboxHidden = new JCheckBox("Hidden");
		m_checkboxHidden.setToolTipText(
			"Hidden - information about this interface is not propagate to the InfoService.");
		if (data != null)
		{
			m_checkboxVirtual.setSelected(data.isVirtual());
			m_checkboxHidden.setSelected(data.isHidden());
		}
		p.add(m_checkboxVirtual);
		p.add(m_checkboxHidden);
	}

	protected void addKeys(final ConnectionData data, final ConnectionTableModel where, GridBagLayout layout,
						   GridBagConstraints lc, GridBagConstraints rc)
	{
		GridBagLayout keylayout = new GridBagLayout();
		JPanel keys = new JPanel(keylayout);
		GridBagConstraints kc = new GridBagConstraints();
		final JDialog parent = this;
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
					if (m_bttngrpType.getSelection().getActionCommand().equals("TCP"))
					{
						for (int i = 0; i < 4; i++)
						{
							if (iptext[i].getText().length() == 0)
							{
								if (i == 0)
								{
									break;
								}
								javax.swing.JOptionPane.showMessageDialog(parent,
									"IP Address is not complete.",
									"Error", javax.swing.JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
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
					if (m_bttngrpType.getSelection().getActionCommand().equals("TCP"))
					{
						for (int i = 0; i < 4; i++)
						{
							if (iptext[i].getText().length() == 0)
							{
								if (i == 0)
								{
									break;
								}
								javax.swing.JOptionPane.showMessageDialog(parent,
									"IP Address is not complete.",
									"Error", javax.swing.JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
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
		if (firstone == null)
		{
			firstone = key;
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
		super(parent, title, false);
	}
}
