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

import java.io.IOException;
import java.util.Vector;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import gui.JAPHelp;
import logging.LogType;
import mixconfig.CertPanel;
import mixconfig.ConfigurationEvent;
import mixconfig.GeneralPanel;
import mixconfig.MixConfig;
import mixconfig.MixConfiguration;
import mixconfig.OtherMixPanel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import gui.JAPDialog;

/** A panel that provides settings for configuring the Mix's network access:<br>
 * Listeners for incoming connections, and network adresses of outgoing
 * connections (next Nix in the cascade or proxies).
 */
public final class NextMixProxyPanel extends OtherMixPanel implements TableModelListener, ActionListener,
	ChangeListener, ComponentListener
{
	private JPanel panel2;
	private JTable table2;
	private OutgoingConnectionTableModel omodel;
	private JButton m_bttnAddOutgoing;

	/** Constructs a new instance of <CODE>NetworkPanel</CODE> */
	public NextMixProxyPanel()
	{
		super(OtherMixPanel.MIX_TYPE_NEXT);
		this.addComponentListener(this);
		GridBagLayout Out_Layout = super.getGridBagLayout();
		GridBagConstraints c = super.getGridBagConstraints();


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

		GridBagConstraints d = new GridBagConstraints();
		d.anchor = GridBagConstraints.CENTER;
		d.insets = new Insets(5, 5, 5, 5);
		d.gridx = 0;
		d.gridy = 1;
		d.weightx = 1;
		d.weighty = 1;
		d.gridheight = 3;
		d.fill = GridBagConstraints.HORIZONTAL;

		// Now the outgoing connections
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 4;
		panel2 = new JPanel(Out_Layout);
		panel2.setBorder(new TitledBorder("Outgoing"));
		panel2.setToolTipText("Connection(s) to next Mix or Proxies.");
		add(panel2, c);
		//add layout dummy
		c.gridy++;
		c.fill = c.NONE;
		c.weightx = 1;
		c.weighty = 1;
		add(new JLabel(" "), c);
		c.gridwidth = 1;
		c.weightx = 0;
		c.weighty = 0;

		int[] columnSizes2 =
			{
			15, 35, 55, 195, 40};

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
						/*case OutgoingConnectionTableModel.IP_ADDR:
						 return IPRenderer;*/
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
		table2.setPreferredScrollableViewportSize(new Dimension(500, 40));
		table2.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					String titles[] =
						{
						" Next Mix", " Next Mix", " Proxy"
					};
					MixConfiguration mixConf = getConfiguration();
					int mixType = Integer.valueOf(mixConf.getValue("General/MixType")).intValue();

					new OutgoingDialog(MixConfig.getMainWindow(),
									   "Change" + titles[mixType],
									   omodel,
									   omodel.getData(table2.getSelectedRow())).setVisible(true);
				}
			}
		});

		for (int Index = 0; Index < columnSizes2.length; Index++)
		{
			TableColumn column = table2.getColumnModel().getColumn(Index);
			column.setPreferredWidth(columnSizes2[Index]);
			// Die Spalten kann der Nutzer ruhig anpassen, wenn eine
			// Spalte zu klein ist (z.B. aufgrund anderer Schriftarten)
			//column.setMinWidth(columnSizes1[Index]);
			//column.setMaxWidth(columnSizes1[Index]);
		}

		d.anchor = GridBagConstraints.CENTER;
		d.insets = new Insets(5, 5, 5, 5);
		d.gridx = 0;
		d.gridy = 0;
		d.weightx = 1;
		d.weighty = 1;
		d.gridheight = 3;
		d.fill = GridBagConstraints.BOTH;
		Out_Layout.setConstraints(scrollPane2, d);
		panel2.add(scrollPane2);

		for (int Nr = 0; Nr < 2; Nr++)
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
							String s = getConfiguration().getValue("General/MixType");
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
			ibd.insets = new Insets(5, 5, 5, 5);
			ibd.gridx = 1;
			ibd.gridy = Nr;
			ibd.weightx = 0.1;
			ibd.weighty = 0.2;
			ibd.fill = GridBagConstraints.HORIZONTAL;
			Out_Layout.setConstraints(OutButton, ibd);
			panel2.add(OutButton);
		}
	}

	public void tableChanged(TableModelEvent e)
	{
		enableComponents();
		if (this.isAutoSaveEnabled())
		{
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
			JAPDialog.showErrorMessage(MixConfig.getMainWindow(), ex, null, LogType.GUI);
		}
		enableComponents();
	}

	public Vector check()
	{
		Vector errors = super.check();
		String s = MixConfig.getMixConfiguration().getValue("General/MixType");
		int mixType = Integer.valueOf(s).intValue();

		if (omodel.getRowCount() == 0)
		{
			if (!getConfiguration().isAutoConfigurationAllowed())
			{
				errors.addElement("You must specify outgoing connections in NextMix/Proxy panel.");
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

		return errors;
	}

	public void paint(Graphics g)
	{
		super.paint(g);
		JAPHelp.getInstance().getContextObj().setContext("index");
	}

	protected void enableComponents()
	{
		try
		{
			boolean bEnableOutgoing;
			boolean bEnableCerts;
			int mixType = getConfiguration().getMixType();
			if (mixType == MixConfiguration.MIXTYPE_LAST)
			{
				getMixCertPanel().removeCert();
			}

			bEnableCerts = mixType != MixConfiguration.MIXTYPE_LAST &&
										 (!getConfiguration().isAutoConfigurationAllowed() ||
										  getConfiguration().isFallbackEnabled());
			getMixCertPanel().setEnabled(bEnableCerts);
			getLocationPanel().setEnabled(bEnableCerts);
			getOperatorPanel().setEnabled(bEnableCerts);
			getMixOperatorCertPanel().setEnabled(bEnableCerts);

			bEnableOutgoing = mixType == MixConfiguration.MIXTYPE_LAST ||
				!getConfiguration().isAutoConfigurationAllowed()
				|| getConfiguration().isFallbackEnabled();

			/** @todo do not save outgoing if it is disabled; edit MixConfigPanel... */
			table2.setEnabled(bEnableOutgoing);
			setEnabled(bEnableOutgoing);
		}
		catch (NullPointerException npe)
		{
			// do nothing if config is not yet loaded
		}
	}

	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		// first enable all components to make MixConfigPanel load their data
		enableComponents();

		//reset data in outgoing
		resetOutgoingModel();

		super.setConfiguration(a_conf);

		a_conf.addChangeListener(this);

		setEnabled(getConfiguration().getMixType() != MixConfiguration.MIXTYPE_LAST);

		enableComponents();
	}

	public void load() throws IOException
	{
		resetOutgoingModel();
		super.load();
	}

	public void stateChanged(ChangeEvent e)
	{
		try
		{
			if (e instanceof ConfigurationEvent)
			{
				ConfigurationEvent c = (ConfigurationEvent) e;
				if (c.getChangedAttribute().equals(GeneralPanel.XMLPATH_GENERAL_MIXTYPE))
				{
					int i = Integer.valueOf( (String) c.getNewValue()).intValue();
					m_bttnAddOutgoing.setEnabled(i == MixConfiguration.MIXTYPE_LAST ||
												 omodel.getRowCount() == 0);
					enableComponents();
				}
				else if (c.getChangedAttribute().indexOf(GeneralPanel.XMLPATH_AUTOCONFIGURATION) >= 0)
				{
					enableComponents();
				}
			}
			else if (e.getSource() instanceof CertPanel)
			{
				save( (CertPanel) e.getSource());
			}
			super.stateChanged(e);

		}
		catch (Exception ex)
		{
			JAPDialog.showErrorMessage(MixConfig.getMainWindow(), ex, null, LogType.GUI);
		}
	}

	public void actionPerformed(ActionEvent a)
	{
		String titles[] =
			{
			" Next Mix", " Next Mix", " Proxy"
		};
		MixConfiguration mixConf = getConfiguration();
		int mixType = Integer.valueOf(mixConf.getValue(GeneralPanel.XMLPATH_GENERAL_MIXTYPE)).intValue();

		if (a.getActionCommand().equals("AddOutgoing"))
		{
			/*			String type;
			   if(mixType != MixConfiguration.MIXTYPE_LAST)
			 */
			new OutgoingDialog(MixConfig.getMainWindow(),
							   "Add" + titles[mixType],
							   omodel).setVisible(true);
		}
		else if (a.getActionCommand().equals("DeleteOutgoing"))
		{
			omodel.deleteData(table2.getSelectedRow());
		}
	}

	private void resetOutgoingModel()
	{
		omodel = new OutgoingConnectionTableModel();
		table2.setModel(omodel);
		omodel.addTableModelListener(this);
		int[] columnSizes2 =
			{
			15, 35, 55, 195, 40};

		for (int Index = 0; Index < columnSizes2.length; Index++)
		{
			TableColumn column = table2.getColumnModel().getColumn(Index);
			column.setPreferredWidth(columnSizes2[Index]);
		}

	}

	public void componentResized(ComponentEvent e)
	{
	}

	public void componentMoved(ComponentEvent e)
	{
	}

	public void componentShown(ComponentEvent e)
	{
		MixConfiguration m = getConfiguration();
		if (m.isMixOnCDEnabled() && m.getMixType() == m.MIXTYPE_LAST)
		{
			if (table2.getRowCount() == 0)
			{
				ConnectionData c = new ConnectionData("Proxy", ConnectionData.RAW_TCP, "localhost");
				c.setPort(8080);
				c.setFlags(ConnectionData.HTTP_PROXY);
				omodel.addData(c);
			}
		}
	}

	public void componentHidden(ComponentEvent e)
	{
	}
}
