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

import java.io.IOException;
import java.util.Vector;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import gui.JAPHelp;
import gui.JAPMessages;
import mixconfig.networkpanel.ConnectionData;
import mixconfig.networkpanel.IncomingConnectionTableModel;
import mixconfig.networkpanel.IncomingDialog;

public class GeneralPanel extends MixConfigPanel implements ActionListener, TableModelListener,
	ChangeListener
{
	private static final String FIRST_MIX = "First mix";
	private static final String MIDDLE_MIX = "Middle mix";
	private static final String LAST_MIX = "Last mix";

	private static final String CONFIGURATION_STATIC = "Static (fixed neighbour mixes)";
	private static final String CONFIGURATION_DYNAMIC = "Dynamic (configured by InfoService)";

	public static final String XMLPATH_AUTOCONFIGURATION =
		"Network/InfoService/AllowAutoConfiguration";
	public static final String XML_ATTRIBUTE_FALLBACK = "fallback";

	private JComboBox m_comboboxMixType, m_combxConfiguration, m_combxCascadeLength;
	private JCheckBox m_cbxDynamicFallback;
	private JTextField m_tfMixName;
	private JTextField m_txtISHost, m_txtISPort;
	private JLabel m_cascadeNameLabel, m_lblCascadeLength;

	private TitledGridBagPanel m_panelGeneralSettings;
	private TitledGridBagPanel m_panelInfoService;

	/** A text field for the name of the cascade */
	private JTextField m_tfCascadeName;

	private JPanel m_listenerPanel;
	private JTable m_listenerTable;
	private IncomingConnectionTableModel m_listenerModel;

	/**
	 * Constructs a panel with controls for general Mix settings.
	 */
	public GeneralPanel()
	{
		super("General");
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = getDefaultInsets();
		//c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		m_panelGeneralSettings = new TitledGridBagPanel("General Settings");
		add(m_panelGeneralSettings, c);

		// Mix Type JComboBox
		m_comboboxMixType = new JComboBox();
		m_comboboxMixType.setName("General/MixType");
		m_comboboxMixType.addItem(FIRST_MIX);
		m_comboboxMixType.addItem(MIDDLE_MIX);
		m_comboboxMixType.addItem(LAST_MIX);
		m_comboboxMixType.addItemListener(this);
		m_panelGeneralSettings.addRow(new JLabel("Mix type"), m_comboboxMixType,
									  GridBagConstraints.HORIZONTAL);

		// Mix Configuration
		m_combxConfiguration = new JComboBox();
		m_combxConfiguration.setName(XMLPATH_AUTOCONFIGURATION);
		m_combxConfiguration.addItem(CONFIGURATION_STATIC);
		m_combxConfiguration.addItem(CONFIGURATION_DYNAMIC);
		m_combxConfiguration.addItemListener(this);

		m_cbxDynamicFallback = new JCheckBox("Allow dynamic fallback");
		m_cbxDynamicFallback.setName(XMLPATH_AUTOCONFIGURATION + "/" + XML_ATTRIBUTE_FALLBACK );
		m_cbxDynamicFallback.addItemListener(this);

		m_panelGeneralSettings.addRow(new JLabel("Mix configuration"), m_combxConfiguration,
									  m_cbxDynamicFallback, null, GridBagConstraints.HORIZONTAL);




		// Mix Name JTextField
		m_tfMixName = new JTextField(20);
		m_tfMixName.setText("");
		m_tfMixName.setName("General/MixName");
		m_tfMixName.addFocusListener(this);
		m_panelGeneralSettings.addRow(new JLabel("Mix name"), m_tfMixName,
									  GridBagConstraints.HORIZONTAL);



		// Cascade Name JTextField; this field is disabled by selecting a middle mix type
		m_tfCascadeName = new JTextField(20);
		m_tfCascadeName.setName("General/CascadeName");
		m_tfCascadeName.addFocusListener(this);
		// Cascade length
		m_combxCascadeLength = new JComboBox();
		m_combxCascadeLength.setName("General/MinCascadeLength");
		for (int i = 2; i <= 5; i++)
		{
			m_combxCascadeLength.addItem(new Integer(i));
		}
		m_combxCascadeLength.addItemListener(this);
		m_lblCascadeLength = new JLabel("Minimum cascade length");


		m_cascadeNameLabel = new JLabel("Cascade name");
		m_panelGeneralSettings.addRow(m_cascadeNameLabel, m_tfCascadeName,
									  m_lblCascadeLength, m_combxCascadeLength,
									  GridBagConstraints.HORIZONTAL);

		m_panelInfoService = new TitledGridBagPanel("InfoService");
		c.gridy = 1;
		c.fill = c.HORIZONTAL;
		add(m_panelInfoService, c);

		m_txtISHost = new JTextField(20);
		m_txtISHost.setText("");
		m_txtISHost.setName("Network/InfoService/Host");
		m_txtISHost.addFocusListener(this);

		m_txtISPort = new JTextField(5);
		m_txtISPort.setName("Network/InfoService/Port");
		m_txtISPort.setText("");
		m_txtISPort.setDocument(new IntegerDocument(65535));
		m_txtISPort.setMinimumSize(m_txtISPort.getPreferredSize());
		m_txtISPort.addFocusListener(this);

		JLabel isHostLabel = new JLabel("Host name");
		isHostLabel.setPreferredSize(m_cascadeNameLabel.getPreferredSize());
		m_panelInfoService.addRow(isHostLabel, m_txtISHost, new JLabel("Port"), m_txtISPort);

		m_listenerPanel = new JPanel(new GridBagLayout());
		m_listenerPanel.setBorder(new TitledBorder("Listener Interfaces"));
		m_listenerPanel.setToolTipText(
			"Interfaces the Mix should use for incoming connections (ListenerInterfaces)");
		c.gridy = 2;
		c.fill = c.HORIZONTAL;
		add(m_listenerPanel, c);

		int[] columnSizes1 =
			{
			15, 15, 60, 195, 40};

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

		m_listenerModel = new IncomingConnectionTableModel();
		m_listenerModel.addTableModelListener(this);

		m_listenerTable = new JTable(m_listenerModel)
		{
			public TableCellRenderer getCellRenderer(int row, int column)
			{
				switch (column)
				{
					case IncomingConnectionTableModel.TRANSPORT:
						return transportRenderer;

					case IncomingConnectionTableModel.PORT:
						return PortRenderer;

					default:
						return super.getCellRenderer(row, column);
				}
			}
		};

		JScrollPane scrollPane1 = new JScrollPane(m_listenerTable,
												  ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
												  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		m_listenerTable.setName("Network");

		// Man kann nur eine Zeile selektieren
		m_listenerTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		m_listenerTable.setPreferredScrollableViewportSize(new Dimension(500, 50));
		m_listenerTable.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					new IncomingDialog(MixConfig.getMainWindow(),
									   "Change", m_listenerModel,
									   m_listenerModel.getData(m_listenerTable.getSelectedRow()),
									   getConfiguration().isMixOnCDEnabled()).show();
				}
			}
		});
		for (int Index = 0; Index < columnSizes1.length; Index++)
		{
			TableColumn column = m_listenerTable.getColumnModel().getColumn(Index);
			column.setPreferredWidth(columnSizes1[Index]);
		}

		for (int Nr = 0; Nr < 2; Nr++)
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
					final JButton db = new JButton("Delete");
					InButton = db;
					m_listenerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
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
			ibd.insets = new Insets(5, 5, 5, 5);
			ibd.gridx = 1;
			ibd.gridy = Nr;
			ibd.fill = ibd.HORIZONTAL;
			m_listenerPanel.add(InButton, ibd);
		}

		GridBagConstraints d = new GridBagConstraints();
		d.anchor = GridBagConstraints.CENTER;
		d.insets = new Insets(5, 5, 5, 5);
		d.gridx = 0;
		d.gridy = 0;
		d.weightx = 1;
		d.weighty = 1;
		d.gridheight = 3;
		d.fill = GridBagConstraints.BOTH;
		m_listenerPanel.add(scrollPane1, d);

		//Keep the panels in place
		JLabel dummyLabel1 = new JLabel("");
		c.gridy = 3;
		c.weighty = 1;
		c.fill = c.VERTICAL;
		this.add(dummyLabel1, c);

		JLabel dummyLabel2 = new JLabel("");
		c.gridx = 1;
		c.weightx = 1;
		c.fill = c.HORIZONTAL;
		this.add(dummyLabel2, c);


		enableComponents();
	}

	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		super.setConfiguration(a_conf);
		a_conf.addChangeListener(this);
	}

	public void stateChanged(ChangeEvent a_event)
	{
		if (a_event instanceof ConfigurationEvent)
		{
			if (((ConfigurationEvent)a_event).getChangedAttribute().indexOf(
						 MixOnCDPanel.XMLPATH_MIXONCD) >= 0)
			{
				setMixOnCDInfo(getConfiguration().isMixOnCDEnabled());
			}
		}
	}

	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getActionCommand().equals("AddIncoming"))
		{
			new IncomingDialog(MixConfig.getMainWindow(), "Add", m_listenerModel,
							   getConfiguration().isMixOnCDEnabled()).show();
		}
		else if (ae.getActionCommand().equals("DeleteIncoming"))
		{
			m_listenerModel.deleteData(m_listenerTable.getSelectedRow());
		}
	}

	public Vector check()
	{
		Vector errors = new Vector();
		MixConfiguration mixConf = getConfiguration();
		String s;
		int mixType;

		s = mixConf.getValue("General/MixName");
		if (s == null || s.equals(""))
		{
			errors.addElement("Mix Name not entered in General Panel.");
		}

		try
		{
			mixType = getConfiguration().getMixType();
			s = mixConf.getValue("General/CascadeName");
			if ( (mixType == MixConfiguration.MIXTYPE_FIRST ||
				  (mixType == MixConfiguration.MIXTYPE_LAST &&
				   isAutoConfigurationAllowed())) &&
				(s == null || s.equals("")))
			{
				errors.addElement("Cascade Name not entered in General Panel.");
			}
		}
		catch (NumberFormatException nfe)
		{
			errors.addElement("Invalid Mix type in configuration.");
		}

		s = mixConf.getValue("General/UserID");
		if (s != null && s.equals(""))
		{
			errors.addElement("User ID not entered in General Panel.");

		}
		s = mixConf.getValue("General/NrOfFileDescriptors");
		if (s != null && !isNumber(s))
		{
			errors.addElement(
				"Number of File Descriptors is not a number in General Panel.");

		}
		s = mixConf.getValue("General/Logging/File");
		if (s != null && s.equals(""))
		{
			errors.addElement("No directory for logging entered in General Panel.");

		}

		s = getConfiguration().getValue("Network/InfoService/Host");
		if (s == null || s.equals(""))
		{
			errors.addElement("InfoService host name is incorrect in General Panel.");
		}

		s = getConfiguration().getValue("Network/InfoService/Port");
		if (s == null || s.equals(""))
		{
			errors.addElement(
				"The Port field for the Info Service should not be blank in General Panel.");
		}
		boolean bPortInvalid = false;
		try
		{
			bPortInvalid = (Integer.parseInt(s) < 1);
		}
		catch (NumberFormatException a_e)
		{
			bPortInvalid = true;
		}
		if (bPortInvalid)
		{
			errors.addElement(
				"The InfoService port is invalid in General Panel.");
		}

		for (int i = 0; i < m_listenerModel.getRowCount(); i++)
		{
			if (m_listenerModel.getData(i).getName().equals(""))
			{
				errors.addElement("No hostname specified for listener interface " + (i + 1) + ".");
				break;
			}
		}

		//Check if every virtual interface has a corrsponding hidden interface and vice versa
		Vector hiddenPorts = new Vector();
		Vector virtualPorts = new Vector();

		for (int i = 0; i < m_listenerModel.getRowCount(); i++)
		{
			if (m_listenerModel.getData(i).isVirtual())
			{
				virtualPorts.addElement(new Integer(m_listenerModel.getData(i).getPort()));
			}
			if (m_listenerModel.getData(i).isHidden())
			{
				hiddenPorts.addElement(new Integer(m_listenerModel.getData(i).getPort()));
			}
		}
		for (int i = 0; i < virtualPorts.size(); i++)
		{
			if (!hiddenPorts.contains(virtualPorts.elementAt(i)))
			{
				errors.addElement("No corresponding hidden interface for port "
								  + ( (Integer) virtualPorts.elementAt(i)).intValue() + ".");
			}
		}
		for (int i = 0; i < hiddenPorts.size(); i++)
		{
			if (!virtualPorts.contains(hiddenPorts.elementAt(i)))
			{
				errors.addElement("No corresponding virtual interface for port "
								  + ( (Integer) hiddenPorts.elementAt(i)).intValue() + ".");
			}
		}

		//Check if any virtual or hidden interfaces are conflicting with a default interface
		Vector defaultPorts = new Vector();
		for (int i = 0; i < m_listenerModel.getRowCount(); i++)
		{
			if (!m_listenerModel.getData(i).isVirtual() && !m_listenerModel.getData(i).isHidden())
			{
				defaultPorts.addElement(new Integer(m_listenerModel.getData(i).getPort()));
			}
		}

		for (int i = 0; i < virtualPorts.size(); i++)
		{
			if (defaultPorts.contains(virtualPorts.elementAt(i)))
			{
				errors.addElement("A virtual interface conflicts with a default interface on port "
								  + ( (Integer) virtualPorts.elementAt(i)).intValue() + ".");
			}
		}

		for (int i = 0; i < hiddenPorts.size(); i++)
		{
			if (defaultPorts.contains(hiddenPorts.elementAt(i)))
			{
				errors.addElement("A hidden interface is conflicting with a default interface on port "
								  + ( (Integer) hiddenPorts.elementAt(i)).intValue() + ".");
			}
		}

		//Check if there are two default interfaces with the same port
		Vector failedDefaultPorts = new Vector();
		for (int i = 0; i < defaultPorts.size(); i++)
		{
			Integer port = (Integer) defaultPorts.elementAt(i);
			for (int j = 0; j < defaultPorts.size(); j++)
			{
				if (i != j && ( (Integer) defaultPorts.elementAt(j)).equals(port)
					&& !failedDefaultPorts.contains(port))
				{
					errors.addElement("There is more than one default interface for port " +
									  port.intValue() + ".");
					failedDefaultPorts.addElement(port);
				}
			}
		}

	//Check if a middle or last mix has only one valid interface
	mixType = Integer.valueOf(mixConf.getValue("General/MixType")).intValue();
	if (mixType == MixConfiguration.MIXTYPE_LAST || mixType == MixConfiguration.MIXTYPE_MIDDLE)
	{
		if (m_listenerModel.getRowCount() > 2)
		{
			errors.addElement("Too many interfaces for middle or last mix");
		}
		else if(defaultPorts.size() > 1)
		{
			errors.addElement("Too many interfaces for middle or last mix");
		}
		else if(hiddenPorts.size() > 1 && virtualPorts.size() > 1)
		{
			errors.addElement("Too many interfaces for middle or last mix");
		}
	}

		//Check if every listener interface has a port if it is not UNIX transport
		for (int i = 0; i < m_listenerModel.getRowCount(); i++)
		{
			int port = m_listenerModel.getData(i).getPort();

			if (port == 0 && m_listenerModel.getData(i).getTransport() != ConnectionData.UNIX)
			{
				errors.addElement("No port specified for listener interface " + (i + 1) + ".");
				break;
			}
		}
		if (m_listenerModel.getRowCount() == 0)
		{
			errors.addElement("No listener interfaces specified.");
		}

		if (m_listenerModel.getRowCount() > 0)
		{
			boolean ok = false;

			for (int i = 0; i < m_listenerModel.getRowCount(); i++)
			{
				if (!m_listenerModel.getData(i).isVirtual())
				{
					ok = true;
				}
			}
			if (!ok)
			{
				errors.addElement("No non-virtual listener interfaces specified.");
			}
		}

		boolean autoconf = (m_listenerModel.getRowCount() == 0);

		// disable autoconf if all listener interfaces are hidden and/or virtual
		boolean hidden = false, virtual = false;
		if (!autoconf)
		{
			for (int i = 0; i < m_listenerModel.getRowCount(); i++)
			{
				virtual = m_listenerModel.getData(i).isVirtual();
				hidden = m_listenerModel.getData(i).isHidden();

				if (!hidden && !virtual)
				{
					autoconf = true;
					break;
				}
			}
			if (getConfiguration().isAutoConfigurationAllowed() && !autoconf)
			{
				errors.addElement("Autoconfiguration is not possible if all " +
								  "listener interfaces are set to " +
								  "hidden or virtual. Add at least one non-hidden, " +
								  "non-virtual interface to enable autoconfiguration."
					);
			}
		}

		return errors;
	}

	protected void load(JComboBox a_combx)
	{
		if (a_combx == m_combxConfiguration)
		{
			if (!getConfiguration().isAutoConfigurationAllowed() ||
				getConfiguration().isFallbackEnabled())
			{
				m_cbxDynamicFallback.setSelected(getConfiguration().isFallbackEnabled());
				m_combxConfiguration.setSelectedItem(CONFIGURATION_STATIC);
			}
			else
			{
				m_combxConfiguration.setSelectedItem(CONFIGURATION_DYNAMIC);
			}
		}
		else if (a_combx == m_combxCascadeLength)
		{
			int index;

			if (isAutoConfigurationAllowed() && getConfiguration().getMixType() !=
				MixConfiguration.MIXTYPE_LAST)
			{

				try
				{
					index = Integer.parseInt(getConfiguration().getValue(
						m_combxCascadeLength.getName())) - 2;
					if (index >= m_combxCascadeLength.getItemCount())
					{
						index = m_combxCascadeLength.getItemCount() - 1;
					}
					else if (index < 0)
					{
						index = 0;
					}
				}
				catch (Exception a_e)
				{
					index = 0;
				}

				m_combxCascadeLength.setSelectedIndex(index);
			}
			else
			{
				m_combxCascadeLength.setSelectedIndex(0);
			}
		}
		else
		{
			super.load(a_combx);
		}
	}

	protected void save(JCheckBox a_cbx)
	{
		if (a_cbx == m_cbxDynamicFallback)
		{
			getConfiguration().setAttribute(XMLPATH_AUTOCONFIGURATION, XML_ATTRIBUTE_FALLBACK,
												m_cbxDynamicFallback.isSelected());
			getConfiguration().setValue(XMLPATH_AUTOCONFIGURATION, m_cbxDynamicFallback.isSelected());
		}
		else
		{
			super.save(a_cbx);
		}
	}

	protected void save(JComboBox a_combx)
	{
		if (a_combx == m_combxConfiguration)
		{
			if (a_combx.getSelectedItem().equals(CONFIGURATION_STATIC) &&
				!m_cbxDynamicFallback.isSelected())
			{
				getConfiguration().removeNode(m_combxConfiguration.getName());
			}
			else
			{
				getConfiguration().setValue(m_combxConfiguration.getName(), true);
			}
		}
		else if (a_combx == m_combxCascadeLength)
		{
			if (a_combx.getSelectedIndex() == 0)
			{
				getConfiguration().removeNode(a_combx.getName());
			}
			else
			{
				getConfiguration().setValue(a_combx.getName(),
											((Integer)a_combx.getSelectedItem()).intValue());
			}
		}
		else
		{
			super.save(a_combx);
		}
	}

	protected void enableComponents()
	{
		boolean bEnableCascadeName;

		if (m_combxConfiguration.getSelectedItem().equals(CONFIGURATION_DYNAMIC) &&
			m_cbxDynamicFallback.isSelected())
		{
			m_cbxDynamicFallback.setSelected(false);
		}
		m_cbxDynamicFallback.setEnabled(
				  m_combxConfiguration.getSelectedItem().equals(CONFIGURATION_STATIC));


		m_lblCascadeLength.setEnabled(isAutoConfigurationAllowed());
		m_combxCascadeLength.setEnabled(isAutoConfigurationAllowed());

		bEnableCascadeName = m_comboboxMixType.getSelectedItem() == FIRST_MIX ||
			(m_comboboxMixType.getSelectedItem() == LAST_MIX && isAutoConfigurationAllowed());

		m_tfCascadeName.setEnabled(bEnableCascadeName);
		m_cascadeNameLabel.setEnabled(bEnableCascadeName);
	}

	/**
	 * Registers for the correct help context every time the panel is painted.
	 * @param g Graphics
	 */
	public void paint(Graphics g)
	{
		super.paint(g);
		JAPHelp.getInstance().getContextObj().setContext("index");
	}

	public void tableChanged(TableModelEvent e)
	{
		enableComponents();
		if (this.isAutoSaveEnabled())
		{
			if (e.getSource() == m_listenerTable.getModel())
			{
				save(m_listenerTable);
			}
		}
	}

	public void load() throws IOException
	{
		super.load();
		if (getConfiguration() != null)
		{
			setMixOnCDInfo(getConfiguration().isMixOnCDEnabled());
		}
	}

	private boolean isAutoConfigurationAllowed()
	{
		return m_combxConfiguration.getSelectedItem().equals(CONFIGURATION_DYNAMIC) ||
			(m_combxConfiguration.getSelectedItem().equals(CONFIGURATION_STATIC) &&
			 m_cbxDynamicFallback.isSelected());
	}

	private boolean isFallbackEnabled()
	{
		return isAutoConfigurationAllowed() && getConfiguration().isFallbackEnabled();
	}

	/** Set the information in the "Listener interfaces" according to MixOnCD */
	private void setMixOnCDInfo(boolean a_configuredByMixOnCD)
	{
		if (a_configuredByMixOnCD)
		{
			for (int i = 0; i < m_listenerModel.getRowCount(); i++)
			{
				ConnectionData olddata = (ConnectionData) m_listenerModel.getData(i);
				if (olddata.getTransport() != olddata.UNIX && !olddata.isVirtual())
				{
					ConnectionData newdata = olddata.deepClone();
					newdata.setName(JAPMessages.getString("configuredByMixOnCD"));
					m_listenerModel.changeData(newdata, olddata);
				}
			}
		}
		else
		{
			for (int i = 0; i < m_listenerModel.getRowCount(); i++)
			{
				ConnectionData olddata = (ConnectionData) m_listenerModel.getData(i);
				if (olddata.getName().equalsIgnoreCase(JAPMessages.getString("configuredByMixOnCD")))
				{
					ConnectionData newdata = olddata.deepClone();
					newdata.setName(JAPMessages.getString(""));
					m_listenerModel.changeData(newdata, olddata);
				}
			}

		}
	}
}

