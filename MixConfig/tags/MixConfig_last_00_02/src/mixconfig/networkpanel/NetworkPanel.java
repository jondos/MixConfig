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
package mixconfig.networkpanel;

import java.util.Vector;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import mixconfig.IntegerDocument;
import mixconfig.MixConfig;
import mixconfig.MixConfigPanel;
import mixconfig.MixConfiguration;
import java.awt.event.FocusEvent;
import java.awt.Container;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import mixconfig.ConfigurationEvent;
import java.io.IOException;
import javax.swing.JCheckBox;
import java.awt.AWTEvent;
import mixconfig.wizard.ConfigWizardPanel;

/** A panel that provides settings for configuring the Mix's network access:<br>
 * Listeners for incoming connections, and network adresses of outgoing
 * connections (next Nix in the cascade or proxies).
 */
public final class NetworkPanel extends MixConfigPanel implements TableModelListener, ActionListener,
	ChangeListener
{
	private JPanel panel1, panel2, panel3;
	private JTable table1, table2;
	private JTextField Host_Text, Port_Text;
	private IPTextField IP_Text;
	private IncomingConnectionTableModel imodel;
	private OutgoingConnectionTableModel omodel;
	private JButton m_bttnAddOutgoing;
	private JCheckBox m_chbAutoConfig;

	/** Constructs a new instance of <CODE>NetworkPanel</CODE> */
	public NetworkPanel()
	{
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagLayout In_Layout = new GridBagLayout();
		GridBagLayout Out_Layout = new GridBagLayout();
		GridBagLayout Info_Layout = new GridBagLayout();

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		panel1 = new JPanel(In_Layout);
		panel1.setBorder(new TitledBorder("Listener Interfaces"));
		panel1.setToolTipText("Interfaces the Mix should use for incoming connections (ListenerInterfaces)");
		layout.setConstraints(panel1, c);
		add(panel1);

		int[] columnSizes1 =
			{
			15, 15, 20, 60, 195, 110, 40};
		// table1 = new JTable(data1, columnNames1);

		final TableCellRenderer IPRenderer = new DefaultTableCellRenderer()
		{
			protected void setValue(Object v)
			{
				if (v == null)
				{
					super.setValue("");
				}
				else
				{
					int[] ips = (int[]) v;
					super.setValue(ips[0] + "." + ips[1] + "." + ips[2] + "." + ips[3]);
					setHorizontalAlignment(CENTER);
				}
			}
		};
		final TableCellRenderer PortRenderer = new DefaultTableCellRenderer()
		{
			protected void setValue(Object v)
			{
				int t = ( (Integer) v).intValue();
				if (t <= 0)
				{
					super.setValue("");
				}
				else
				{
					setHorizontalAlignment(CENTER);
					super.setValue(v.toString());
				}
			}
		};
		final TableCellRenderer transportRenderer = new DefaultTableCellRenderer()
		{
			protected void setValue(Object v)
			{
				int t = ( (Integer) v).intValue();
				super.setValue(
					( ( (t & ConnectionData.SSL) == 0) ? "Raw/" : "SSL/") +
					( ( (t & ConnectionData.UNIX) == 0) ? "TCP" : "Unix"));
				setHorizontalAlignment(CENTER);
			}
		};
		final TableCellRenderer centeringRenderer = new DefaultTableCellRenderer()
		{
			protected void setValue(Object v)
			{
				super.setValue(v);
				setHorizontalAlignment(CENTER);
			}
		};
		/*
		   final TableCellRenderer emptyRenderer = new DefaultTableCellRenderer()
		   {
		  protected void setValue(Object v)
		  {
		 super.setValue("");
		  }
		   };
		 */

		imodel = new IncomingConnectionTableModel();
		imodel.addTableModelListener(this);

		table1 = new JTable(imodel)
		{
			public TableCellRenderer getCellRenderer(int row, int column)
			{
				switch (column)
				{
					case IncomingConnectionTableModel.TRANSPORT:
						return transportRenderer;
					case IncomingConnectionTableModel.IP_ADDR:
						return IPRenderer;
					case IncomingConnectionTableModel.PORT:
						return PortRenderer;
					default:
						return super.getCellRenderer(row, column);
				}
			}
		};

		JScrollPane scrollPane1 = new JScrollPane(table1,
												  ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
												  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		table1.setName("Network");

		// Man kann nur eine Zeile selektieren
		table1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		table1.setPreferredScrollableViewportSize(new Dimension(450, 90));

		// table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for (int Index = 0; Index < columnSizes1.length; Index++)
		{
			TableColumn column = table1.getColumnModel().getColumn(Index);
			column.setPreferredWidth(columnSizes1[Index]);
			// Die Spalten kann der Nutzer ruhig anpassen, wenn eine
			// Spalte zu klein ist (z.B. aufgrund anderer Schriftarten)
			// column.setMinWidth(columnSizes1[Index]);
			// column.setMaxWidth(columnSizes1[Index]);
		}

		GridBagConstraints d = new GridBagConstraints();
		d.anchor = GridBagConstraints.CENTER;
		d.insets = new Insets(10, 10, 10, 10);
		d.gridx = 0;
		d.gridy = 0;
		d.weightx = 1;
		d.weighty = 1;
		d.gridheight = 3;
		d.fill = GridBagConstraints.BOTH;
		In_Layout.setConstraints(scrollPane1, d);
		panel1.add(scrollPane1);

		for (int Nr = 0; Nr < 3; Nr++)
		{
			JButton InButton;
			switch (Nr)
			{
				case 0:
					InButton = new JButton("Add");
					InButton.setActionCommand("AddIncoming");
					InButton.addActionListener(this);
					break;
				case 1:
					final JButton cb = new JButton("Change");
					InButton = cb;
					table1.getSelectionModel().addListSelectionListener(new ListSelectionListener()
					{
						public void valueChanged(ListSelectionEvent e)
						{
							if (e.getValueIsAdjusting())
							{
								return;
							}
							cb.setEnabled(! ( (ListSelectionModel) e.getSource()).isSelectionEmpty());
						}
					});
					cb.setEnabled(false);
					cb.setActionCommand("ChangeIncoming");
					cb.addActionListener(this);
					break;
				case 2:
					final JButton db = new JButton("Delete");
					InButton = db;
					table1.getSelectionModel().addListSelectionListener(new ListSelectionListener()
					{
						public void valueChanged(ListSelectionEvent e)
						{
							if (e.getValueIsAdjusting())
							{
								return;
							}
							db.setEnabled(! ( (ListSelectionModel) e.getSource()).isSelectionEmpty());
						}

					});
					db.setEnabled(false);
					db.setActionCommand("DeleteIncoming");
					db.addActionListener(this);
					break;
				default:
					throw (new RuntimeException("Unknown Button should be created."));
			}
			GridBagConstraints ibd = new GridBagConstraints();
			ibd.anchor = GridBagConstraints.NORTHWEST;
			ibd.insets = new Insets(10, 10, 10, 10);
			ibd.gridx = 1;
			ibd.gridy = Nr;
			ibd.weightx = 0.1;
			ibd.weighty = 0.2;
			ibd.fill = GridBagConstraints.HORIZONTAL;
			In_Layout.setConstraints(InButton, ibd);
			panel1.add(InButton);
		}

		// Now the outgoing connections
		c.gridx = 0;
		c.gridy = 1;
		panel2 = new JPanel(Out_Layout);
		panel2.setBorder(new TitledBorder("Outgoing"));
		panel2.setToolTipText("Connection(s) to next Mix or Proxies.");
		layout.setConstraints(panel2, c);
		add(panel2);

		int[] columnSizes2 =
			{
			15, 70, 60, 125, 110, 40};

		omodel = new OutgoingConnectionTableModel();
		omodel.addTableModelListener(this);

		table2 = new JTable(omodel)
		{
			public TableCellRenderer getCellRenderer(int row, int column)
			{
				switch (column)
				{
					case OutgoingConnectionTableModel.TYPE:
						return centeringRenderer;
					case OutgoingConnectionTableModel.TRANSPORT:
						return transportRenderer;
					case OutgoingConnectionTableModel.IP_ADDR:
						return IPRenderer;
					case OutgoingConnectionTableModel.PORT:
						return PortRenderer;
					default:
						return super.getCellRenderer(row, column);
				}
			}
		};

		JScrollPane scrollPane2 = new JScrollPane(table2,
												  ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
												  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		table2.setName("Network");

		// Man kann nur eine Zeile selektieren
		table2.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		table2.setPreferredScrollableViewportSize(new Dimension(450, 90));

		for (int Index = 0; Index < columnSizes2.length; Index++)
		{
			TableColumn column = table2.getColumnModel().getColumn(Index);
			column.setPreferredWidth(columnSizes2[Index]);
			// Die Spalten kann der Nutzer ruhig anpassen, wenn eine
			// Spalte zu klein ist (z.B. aufgrund anderer Schriftarten)
			// column.setMinWidth(columnSizes1[Index]);
			// column.setMaxWidth(columnSizes1[Index]);
		}

		d.anchor = GridBagConstraints.CENTER;
		d.insets = new Insets(10, 10, 10, 10);
		d.gridx = 0;
		d.gridy = 0;
		d.weightx = 1;
		d.weighty = 1;
		d.gridheight = 3;
		d.fill = GridBagConstraints.BOTH;
		Out_Layout.setConstraints(scrollPane2, d);
		panel2.add(scrollPane2);

		for (int Nr = 0; Nr < 3; Nr++)
		{
			JButton OutButton;
			switch (Nr)
			{
				case 0:
					final JButton ob = new JButton("Add");
					OutButton = ob;
					omodel.addTableModelListener(new TableModelListener()
					{
						public void tableChanged(TableModelEvent e)
						{
							String s = getConfiguration().getAttribute("General/MixType");
							int i = Integer.valueOf(s).intValue();
							ob.setEnabled(i == MixConfiguration.MIXTYPE_LAST ||
										  omodel.getRowCount() == 0);
						}
					});
					OutButton.setActionCommand("AddOutgoing");
					OutButton.addActionListener(this);
					m_bttnAddOutgoing = OutButton;
					break;
				case 1:
					final JButton cb = new JButton("Change");
					OutButton = cb;
					table2.getSelectionModel().addListSelectionListener(new ListSelectionListener()
					{
						public void valueChanged(ListSelectionEvent e)
						{
							cb.setEnabled(! ( (ListSelectionModel) e.getSource()).isSelectionEmpty());
						}
					});
					cb.setEnabled(false);
					cb.setActionCommand("ChangeOutgoing");
					cb.addActionListener(this);
					break;
				case 2:
					final JButton db = new JButton("Delete");
					OutButton = db;
					table2.getSelectionModel().addListSelectionListener(new ListSelectionListener()
					{
						public void valueChanged(ListSelectionEvent e)
						{
							db.setEnabled(! ( (ListSelectionModel) e.getSource()).isSelectionEmpty());
						}

					});
					db.setEnabled(false);
					db.setActionCommand("DeleteOutgoing");
					db.addActionListener(this);
					break;
				default:
					throw (new RuntimeException("Unknown Button should be created."));
			}
			GridBagConstraints ibd = new GridBagConstraints();
			ibd.anchor = GridBagConstraints.NORTHWEST;
			ibd.insets = new Insets(10, 10, 10, 10);
			ibd.gridx = 1;
			ibd.gridy = Nr;
			ibd.weightx = 0.1;
			ibd.weighty = 0.2;
			ibd.fill = GridBagConstraints.HORIZONTAL;
			Out_Layout.setConstraints(OutButton, ibd);
			panel2.add(OutButton);
		}

		c.gridx = 0;
		c.gridy = 2;
		c.weighty = 0;
		panel3 = new JPanel(Info_Layout);
		panel3.setBorder(new TitledBorder("Info Service"));
		layout.setConstraints(panel3, c);
		add(panel3);

		GridBagConstraints f = new GridBagConstraints();
		f.anchor = GridBagConstraints.NORTHWEST;
		f.insets = new Insets(10, 10, 10, 10);
		f.fill = GridBagConstraints.HORIZONTAL;

		JLabel host = new JLabel("Host");
		f.gridx = 0;
		f.gridy = 0;
		Info_Layout.setConstraints(host, f);
		panel3.add(host);

		Host_Text = new JTextField(38);
		Host_Text.setText("");
		Host_Text.setName("Network/InfoService/Host");
		Host_Text.addFocusListener(this);
		f.gridx = 1;
		f.weightx = 1;
		Info_Layout.setConstraints(Host_Text, f);
		panel3.add(Host_Text);

		JLabel IP = new JLabel("IP");
		f.gridy = 1;
		f.gridx = 0;
		f.weightx = 0;
		Info_Layout.setConstraints(IP, f);
		panel3.add(IP);

		IP_Text = new IPTextField();
		IP_Text.setText("");
		IP_Text.setName("Network/InfoService/IP");
		IP_Text.addFocusListener(this);
		f.gridx = 1;
		f.weightx = 0;
		f.fill = GridBagConstraints.NONE;
		Info_Layout.setConstraints(IP_Text, f);
		panel3.add(IP_Text);

		JLabel port = new JLabel("Port");
		f.gridy = 2;
		f.gridx = 0;
		f.weightx = 0;
		Info_Layout.setConstraints(port, f);
		panel3.add(port);

		Port_Text = new JTextField(5);
		Port_Text.setName("Network/InfoService/Port");
		Port_Text.setText("");
		Port_Text.setDocument(new IntegerDocument(65535));
		Port_Text.setMinimumSize(Port_Text.getPreferredSize());
		Port_Text.addFocusListener(this);
		f.gridx = 1;
		Info_Layout.setConstraints(Port_Text, f);
		panel3.add(Port_Text);

		m_chbAutoConfig = new JCheckBox(
			"Allow InfoService to update configuration automatically (may change previous/next mix)");
		m_chbAutoConfig.setName("Network/InfoService/AllowAutoConfiguration");
		m_chbAutoConfig.addItemListener(this);
		f.gridx = 0;
		f.gridwidth = 2;
		f.gridy++;
		Info_Layout.setConstraints(m_chbAutoConfig, f);
		panel3.add(m_chbAutoConfig);
	}

	public void tableChanged(TableModelEvent e)
	{
		enableComponents();
		if (this.isAutoSaveEnabled())
		{
			if (e.getSource() == table1.getModel())
			{
				save(table1);
			}
			if (e.getSource() == table2.getModel())
			{
				save(table2);
			}
		}
	}

	public void focusLost(FocusEvent e)
	{
		try
		{
			if (e.getSource() instanceof JTextField)
			{
				Container c = ( (JTextField) e.getSource()).getParent();
				if (c == IP_Text)
				{
					if (IP_Text.isCorrect())
					{
						getConfiguration().setAttribute(IP_Text.getName(),
							IP_Text.getText());
					}
				}
				else
				{
					super.focusLost(e);
				}
			}
			else
			{
				super.focusLost(e);
			}
		}
		catch (Exception ex)
		{
			MixConfig.handleException(ex);
		}
		enableComponents();
	}

	public Vector check()
	{
		Vector errors = new Vector();
		String s = MixConfig.getMixConfiguration().getAttribute("General/MixType");
		int mixType = Integer.valueOf(s).intValue();

		if (imodel.getRowCount() == 0)
		{
			errors.addElement("No Incoming Connection given in Network Panel.");
		}
		else
		{
			int rows = imodel.getRowCount();
			for (int i = 0; i < rows; i++)
			{
				ConnectionData data = imodel.getData(i);
				if (data.isHidden() && data.isVirtual())
				{
					errors.addElement("Incoming connection no. " + (i + 1) +
									  " is 'virtual' and 'hidden'. This is not possible.");

				}
				if ( (data.getTransport() & ConnectionData.TRANSPORT) ==
					ConnectionData.TCP)
				{
					if (data.getPort() == 0)
					{
						errors.addElement("Incoming connection no. " + (i + 1) +
										  " has no port set.");
					}
					else
					if (data.getName() == null || data.getName().length() == 0)
					{
						errors.addElement("Incoming connection no. " + (i + 1) +
										  " has no filename set.");
					}
				}
			}
		}

		if (omodel.getRowCount() == 0)
		{
			if (!this.m_chbAutoConfig.isSelected())
			{
				errors.addElement("If auto-configuration is disabled, you must " +
								  "specify outgoing connections in Network Panel.");
			}
		}
		else
		{
			int rows = omodel.getRowCount();
			for (int i = 0; i < rows; i++)
			{
				ConnectionData data = omodel.getData(i);
				if ( (data.getTransport() & ConnectionData.TRANSPORT) ==
					ConnectionData.TCP)
				{
					if (data.getName() == null || data.getName().length() == 0)
					{
						errors.addElement("Outgoing connection no. " + (i + 1) +
										  " has no host name set.");

					}
					if (data.getPort() == 0)
					{
						errors.addElement("Outgoing connection no. " + (i + 1) +
										  " has no port set.");
					}
				}
				else
				if (data.getName() == null || data.getName().length() == 0)
				{
					errors.addElement("Outgoing connection no. " + (i + 1) +
									  " has no filename set.");
				}
			}
			if (mixType != MixConfiguration.MIXTYPE_LAST &&
				omodel.getRowCount() > 1)
			{
				errors.addElement("Too many Outgoing Connections in Network Panel.");
			}
		}

		s = getConfiguration().getAttribute("Network/InfoService/Host");
		if (s == null || s.equals(""))
		{
			s = getConfiguration().getAttribute("Network/InfoService/IP");
			if (s == null || s.equals("") || !IP_Text.isCorrect())
			{
				errors.addElement("Neither InfoService host name nor IP address " +
								  "are correct in Network Panel.");
			}
		}
		s = getConfiguration().getAttribute("Network/InfoService/Port");
		if (s == null || s.equals(""))
		{
			errors.addElement(
				"The Port field for the Info Service should not be blank in Network Panel.");

		}
		return errors;
	}

	protected void enableComponents()
	{
		if (getConfiguration() != null)
		{
			Integer t = new Integer(getConfiguration().getAttribute("General/MixType"));
			setEnabled(! (getParent() instanceof ConfigWizardPanel &&
						  t.intValue() != MixConfiguration.MIXTYPE_LAST));
		}

		boolean autoconf = (imodel.getRowCount() == 0);

		// disable autoconf if all listener interfaces are hidden and/or virtual
		boolean hidden = false, virtual = false;
		if (!autoconf)
		{
			for (int i = 0; i < imodel.getRowCount(); i++)
			{
				virtual = ( (Boolean) imodel.getValueAt(i, 1)).booleanValue();
				hidden = ( (Boolean) imodel.getValueAt(i, 2)).booleanValue();

				if (!hidden && !virtual)
				{
					autoconf = true;
					break;
				}
			}
			if (m_chbAutoConfig.isEnabled() && !autoconf)
			{
				MixConfig.info("Autoconfiguration disabled",
							   new String[]
							   {
							   "Autoconfiguration is not possible if all",
							   "listener interfaces are set to",
							   "hidden or virtual. Add at least one non-hidden,",
							   "non-virtual interface to enable autoconfiguration."
				}
					);
			}
		}

		// only enable autoconf if either host or IP of InfoService is given and port is given
		autoconf = autoconf &&
			( ( (Host_Text.getText() != null && !Host_Text.getText().equals(""))
			   || IP_Text.isCorrect()) &&
			 (Port_Text.getText() != null && !Port_Text.getText().equals("")));

		m_chbAutoConfig.setEnabled(autoconf);
		if (!autoconf)
		{
			// Disabling events is necessary, as on JDK < 1.3, the AWT will run into
			// an infinite event loop here otherwise
			m_chbAutoConfig.removeItemListener(this);
			m_chbAutoConfig.setSelected(false);
			if (getConfiguration() != null)
			{
				save(m_chbAutoConfig);
			}
			m_chbAutoConfig.addItemListener(this);
		}
	}

	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		// first enable all components to make MixConfigPanel load their data
		enableComponents();

		super.setConfiguration(a_conf);

		// make sure this panel is contained only once in the config's listeners list
		a_conf.removeChangeListener(this);
		a_conf.addChangeListener(this);

		enableComponents();
	}

	public void stateChanged(ChangeEvent e)
	{
		try
		{
			if (e instanceof ConfigurationEvent)
			{
				ConfigurationEvent c = (ConfigurationEvent) e;
				if (c.getChangedAttribute().equals("General/MixType"))
				{
					int i = Integer.valueOf( (String) c.getNewValue()).intValue();
					m_bttnAddOutgoing.setEnabled(i == MixConfiguration.MIXTYPE_LAST ||
												 omodel.getRowCount() == 0);
					enableComponents();
				}
			}
		}
		catch (Exception ex)
		{
			MixConfig.handleException(ex);
		}
	}

	public void actionPerformed(ActionEvent a)
	{
		String titles[] =
			{
			" Next Mix", " Next Mix", " Proxy"
		};
		MixConfiguration mixConf = getConfiguration();
		int mixType = Integer.valueOf(mixConf.getAttribute("General/MixType")).intValue();

		if (a.getActionCommand().equals("AddIncoming"))
		{
			new IncomingDialog(MixConfig.getMainWindow(), "Add", imodel).show();
		}
		else if (a.getActionCommand().equals("ChangeIncoming"))
		{
			new IncomingDialog(MixConfig.getMainWindow(),
							   "Change", imodel,
							   imodel.getData(table1.getSelectedRow())).show();
		}
		else if (a.getActionCommand().equals("DeleteIncoming"))
		{
			imodel.deleteData(table1.getSelectedRow());
		}
		else if (a.getActionCommand().equals("AddOutgoing"))
		{
			/*			String type;
			   if(mixType != MixConfiguration.MIXTYPE_LAST)
			 */
			new OutgoingDialog(MixConfig.getMainWindow(),
							   "Add" + titles[mixType],
							   omodel).show();
		}
		else if (a.getActionCommand().equals("ChangeOutgoing"))
		{
			new OutgoingDialog(MixConfig.getMainWindow(),
							   "Change" + titles[mixType],
							   omodel,
							   omodel.getData(table2.getSelectedRow())).show();
		}
		else if (a.getActionCommand().equals("DeleteOutgoing"))
		{
			omodel.deleteData(table2.getSelectedRow());
		}
	}
}
