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
package mixconfig.panels;

import gui.dialog.JAPDialog;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Vector;

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

import logging.LogType;
import mixconfig.ConfigurationEvent;
import mixconfig.MixConfig;
import mixconfig.MixConfiguration;
import mixconfig.network.ConnectionData;
import mixconfig.network.ConnectionTableModel;
import mixconfig.network.OutgoingConnectionTableModel;
import mixconfig.network.OutgoingDialog;
import mixconfig.network.ProxyTableModel;

/** 
 * A panel that provides settings for configuring the Mix's network access:<br>
 * Listeners for incoming connections, and network adresses of outgoing
 * connections (next Mix in a cascade or HTTP proxies).
 */
public final class NextMixProxyPanel extends OtherMixPanel implements TableModelListener, 
	ActionListener, ChangeListener, ComponentListener
{
	// Table
	private JPanel m_panel;
	private JTable m_table;
	
	// Either OutgoingConnectionTableModel or ProxyTableModel
	private ConnectionTableModel m_model;
	
	// GUI Elements
	private JButton m_bttnAdd;
	// XXX (Not needed) private JButton m_bttnDelete;
	private TitledBorder m_border;

	/**
	 * Constructor
	 */
	public NextMixProxyPanel()
	{
		super(OtherMixPanel.MIX_TYPE_NEXT);
		this.addComponentListener(this);
		GridBagLayout Out_Layout = super.getGridBagLayout();
		GridBagConstraints c = super.getGridBagConstraints();

		final TableCellRenderer portRenderer = new DefaultTableCellRenderer()
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

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridheight = 3;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		// Now the outgoing connections
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 4;
		m_panel = new JPanel(Out_Layout);
		m_border = new TitledBorder("Outgoing");
		m_panel.setBorder(m_border);
		m_panel.setToolTipText("Connection(s) to next Mix or Proxies.");
		add(m_panel, c);
		//add layout dummy
		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 1;
		c.weighty = 1;
		add(new JLabel(" "), c);
		c.gridwidth = 1;
		c.weightx = 0;
		c.weighty = 0;

		// Initially this is not a last mix
		m_model = new OutgoingConnectionTableModel();
		m_model.addTableModelListener(this);

		// Create the table
		m_table = new JTable(m_model)
		{
			public TableCellRenderer getCellRenderer(int row, int column)
			{
				switch (column)
				{
					case ConnectionTableModel.TYPE:
						return centeringRenderer;
					case ConnectionTableModel.TRANSPORT:
						return transportRenderer;
					case ConnectionTableModel.PORT:
						return portRenderer;
					default:
						return centeringRenderer;
				}
			}
		};

		JScrollPane scrollPane2 = new JScrollPane(m_table,
												  ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
												  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// Set other preferences
		m_table.setName("Network");
		// Do not allow reordering
		m_table.getTableHeader().setReorderingAllowed( false );
		m_table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		m_table.setPreferredScrollableViewportSize(new Dimension(500, 40));
		m_table.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					String title;
					if ((getConfiguration().getMixType() & MixConfiguration.MIXTYPE_LAST) > 0)
					{
						title = "Proxy";
					}
					else
					{
						title = "Next Mix";
					}
					new OutgoingDialog(MixConfig.getMainWindow(),
									   "Change" + title, m_model,
									   m_model.getData(m_table.getSelectedRow())).setVisible(true);
				}
			}
		});
		// Set the column sizes
		this.setColumnSizes();

		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = getDefaultInsets();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridheight = 3;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		m_panel.add(scrollPane2, constraints);

		// Add the buttons
		for (int i = 0; i < 2; i++)
		{
			final JButton button;
			switch (i)
			{
				case 0:
					button = new JButton("Add");
					//button = ob;
					
					// TODO: Remove this!
					/*m_model.addTableModelListener(new TableModelListener()
					{
						public void tableChanged(TableModelEvent e)
						{
							button.setEnabled(getConfiguration().getMixType() == MixConfiguration.MIXTYPE_LAST ||
										  m_model.getRowCount() == 0);
						}
					});*/
					
					button.setActionCommand("Add");
					button.addActionListener(this);
					m_bttnAdd = button;
					break;

				case 1:
					button = new JButton("Delete");
					//button = db;
					m_table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
					{
						public void valueChanged(ListSelectionEvent e)
						{
							button.setEnabled(! ( (ListSelectionModel) e.getSource()).isSelectionEmpty());
						}
					});
					button.setEnabled(false);
					button.setActionCommand("Delete");
					button.addActionListener(this);
					//m_bttnDelete = button;
					break;
				default:
					throw (new RuntimeException("Creating unknown button"));
			}
			GridBagConstraints ibd = new GridBagConstraints();
			ibd.anchor = GridBagConstraints.NORTHWEST;
			ibd.insets = getDefaultInsets();
			ibd.gridx = 1;
			ibd.gridy = i;
			ibd.weightx = 0.1;
			ibd.weighty = 0.2;
			ibd.fill = GridBagConstraints.HORIZONTAL;
			Out_Layout.setConstraints(button, ibd);
			m_panel.add(button);
		}

		// Keep the panels in place
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.weighty = 1;
		add(new JLabel(), constraints);
	}

	public void tableChanged(TableModelEvent e)
	{
		enableComponents();
		if (this.isAutoSaveEnabled())
		{
			if (e.getSource() == m_table.getModel())
			{
				save(m_table);
			}
		}
	}

	public void focusLost(FocusEvent e)
	{
		try
		{
			if (e.getSource() instanceof JTextField)
			{
				super.focusLost(e);
			}
		}
		catch (Exception ex)
		{
			JAPDialog.showErrorDialog(MixConfig.getMainWindow(), null, ex);
		}
		enableComponents();
	}

	/**
	 * Check this panel's validity
	 */
	public Vector<String> check()
	{
		Vector<String> errors = super.check();
		int mixType = MixConfig.getMixConfiguration().getMixType();

		if (m_model.getRowCount() == 0)
		{
			if (mixType == MixConfiguration.MIXTYPE_LAST ||
				!getConfiguration().isAutoConfigurationAllowed())
			{
				errors.addElement("You must specify outgoing connections in " + getName() + " panel.");
			}
		}
		else
		{
			int rows = m_model.getRowCount();
			for (int i = 0; i < rows; i++)
			{
				ConnectionData data = m_model.getData(i);
				if ((data.getTransport() & ConnectionData.TRANSPORT) == ConnectionData.TCP)
				{
					if (data.getHostname() == null || data.getHostname().length() == 0)
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
				if (data.getHostname() == null || data.getHostname().length() == 0)
				{
					errors.addElement("Outgoing connection no. " + (i + 1) +
									  " has no filename set.");
				}
			}
			// Only last mixes can specify multiple proxies
			if (mixType != MixConfiguration.MIXTYPE_LAST && m_model.getRowCount() > 1)
			{
				errors.addElement("Too many outgoing connections in panel NextMix.");
			}
		}
		return errors;
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
				m_border.setTitle("Proxy");
			}
			else
			{
				m_border.setTitle("Next Mix");
			}

			bEnableCerts = mixType != MixConfiguration.MIXTYPE_LAST &&
										 (!getConfiguration().isAutoConfigurationAllowed() ||
										  getConfiguration().isFallbackEnabled());
			getMixCertPanel().setEnabled(bEnableCerts);
			getLocationPanel().setEnabled(bEnableCerts);
			getOperatorPanel().setEnabled(bEnableCerts);
			getMixOperatorCertPanel().setEnabled(bEnableCerts);

			getMixCertPanel().setVisible(bEnableCerts);
			getLocationPanel().setVisible(bEnableCerts);
			getOperatorPanel().setVisible(bEnableCerts);
			getMixOperatorCertPanel().setVisible(bEnableCerts);

			bEnableOutgoing = mixType == MixConfiguration.MIXTYPE_LAST ||
				!getConfiguration().isAutoConfigurationAllowed()
				|| getConfiguration().isFallbackEnabled();

			m_bttnAdd.setEnabled(
						 mixType == MixConfiguration.MIXTYPE_LAST ||
						 m_model.getRowCount() == 0);

			/** @todo do not save outgoing if it is disabled; edit MixConfigPanel... */
			m_table.setEnabled(bEnableOutgoing);
			setEnabled(bEnableOutgoing);
		}
		catch (NullPointerException npe)
		{
			// do nothing if config is not yet loaded
		}
	}

	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		// First enable all components to make MixConfigPanel load their data
		enableComponents();
		
		// Reset data in outgoing?
		//resetTableModel();
		
		// Set the configuration
		super.setConfiguration(a_conf);
		a_conf.addChangeListener(this);
		// Is this panel enabled?
		setEnabled(getConfiguration().getMixType() != MixConfiguration.MIXTYPE_LAST);
		// Enable single components
		enableComponents();
	}

	public void load() throws IOException
	{
		resetTableModel();
		super.load();
	}

	public void stateChanged(ChangeEvent e)
	{
		try
		{
			if (e instanceof ConfigurationEvent)
			{
				ConfigurationEvent c = (ConfigurationEvent) e;
				// The mix type has changed
				if (c.getModifiedXMLPath().equals(GeneralPanel.XMLPATH_GENERAL_MIXTYPE))
				{
					int flags;
					for (int i = 0; i >= 0 && i < m_model.getRowCount(); i++)
					{
						flags = m_model.getData(i).getFlags();
						if (getConfiguration().getMixType() == MixConfiguration.MIXTYPE_LAST)
						{
							if (flags == ConnectionData.NO_PROXY)
							{
								m_model.deleteData(i);
								i--;
							}
						}
						else
						{
							if (flags > ConnectionData.NO_PROXY)
							{
								m_model.deleteData(i);
								i--;
							}
						}
					}
					enableComponents();
					resetTableModel();
				}
				else if (c.getModifiedXMLPath().indexOf(GeneralPanel.XMLPATH_AUTOCONFIGURATION) >= 0)
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
			JAPDialog.showErrorDialog(MixConfig.getMainWindow(), null, ex);
		}
	}

	public void actionPerformed(ActionEvent a)
	{
		// XXX Set the title, but why here ??? TODO: Move ...
		String title;
		if ((getConfiguration().getMixType() & MixConfiguration.MIXTYPE_LAST) > 0)
		{
			title = "Proxy";
		}
		else title = "Next Mix";

		// Handle actions
		if (a.getActionCommand().equals("Add"))
		{
			new OutgoingDialog(MixConfig.getMainWindow(), "Add " + title, m_model).setVisible(true);
		}
		else if (a.getActionCommand().equals("Delete"))
		{
			m_model.deleteData(m_table.getSelectedRow());
		}
	}

	/**
	 * Reset the table model
	 */
	private void resetTableModel()
	{
		m_model = createTableModel();
		m_table.setModel(m_model);
		m_model.addTableModelListener(this);
		//LogHolder.log(LogLevel.DEBUG, LogType.NET, "No. of Listeners: "+m_model.getTableModelListeners().length);
		this.setColumnSizes();
	}

	/**
	 * Return a table model considering the mix type
	 * @return
	 */
	private ConnectionTableModel createTableModel()
	{
		ConnectionTableModel ret;
		// Check the MixType and act accordingly
		int mixType = getConfiguration().getMixType();
		if (mixType == MixConfiguration.MIXTYPE_LAST)
		{
			ret = new ProxyTableModel();
		}
		else
		{
			ret = new OutgoingConnectionTableModel();
		}
		return ret;
	}
	
	/**
	 * Set columnSizes to m_table
	 * @param columnSizes
	 */
	private void setColumnSizes()
	{
		// Check the TableModel and act accordingly
		if (m_model instanceof ProxyTableModel)
		{
			int[] columnSizes = {15, 35, 55, 100, 40, 100};
			adjustColumnSizes(columnSizes);
		}
		else
		{
			int[] columnSizes = {15, 35, 55, 195, 40};
			adjustColumnSizes(columnSizes);
		}
	}
	
	/**
	 * Do the actual adjustments
	 * @param columnSizes
	 */
	private void adjustColumnSizes(int[] columnSizes)
	{
		for (int i = 0; i < columnSizes.length; i++)
		{
			TableColumn column = m_table.getColumnModel().getColumn(i);
			column.setPreferredWidth(columnSizes[i]);
		}
	}
	
	/**
	 * XXX Do we need these?
	 */
	public void componentResized(ComponentEvent e)
	{
	}

	public void componentMoved(ComponentEvent e)
	{
	}

	public void componentShown(ComponentEvent e)
	{
		MixConfiguration m = getConfiguration();
		if (m.isMixOnCDEnabled() && m.getMixType() == MixConfiguration.MIXTYPE_LAST)
		{
			if (m_table.getRowCount() == 0)
			{
				ConnectionData c = new ConnectionData("Proxy", ConnectionData.RAW_TCP, "localhost", 
						8080, ConnectionData.HTTP_PROXY, false, false);
				m_model.addData(c);
			}
		}
	}

	public void componentHidden(ComponentEvent e)
	{
	}

	public Container getHelpExtractionDisplayContext() 
	{
		return null;
	}
}
