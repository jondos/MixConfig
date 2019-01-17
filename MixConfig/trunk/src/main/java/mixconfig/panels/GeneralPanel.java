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

import gui.MixConfigTextField;
import gui.TitledGridBagPanel;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import mixconfig.ConfigurationEvent;
import mixconfig.MixConfig;
import mixconfig.MixConfiguration;
import mixconfig.infoservice.InfoServiceData;
import mixconfig.infoservice.InfoServiceDialog;
import mixconfig.infoservice.InfoServiceTableModel;
import mixconfig.network.ConnectionData;
import mixconfig.network.IncomingConnectionTableModel;
import mixconfig.network.IncomingDialog;
import anon.infoservice.Database;
import anon.infoservice.InfoServiceDBEntry;
import anon.infoservice.InfoServiceHolder;
import anon.infoservice.ListenerInterface;
import anon.infoservice.MixInfo;
import anon.util.JAPMessages;
import anon.util.Util;

public class GeneralPanel extends MixConfigPanel implements ActionListener, TableModelListener, ChangeListener
{
	// Paths
	public static final String XMLPATH_GENERAL = "General";
	public static final String XMLPATH_GENERAL_MIXTYPE = XMLPATH_GENERAL + "/MixType";
	public static final String XMLPATH_GENERAL_CASCADENAME = XMLPATH_GENERAL + "/CascadeName";
	public static final String XMLPATH_GENERAL_MIN_CASCADELENGTH = XMLPATH_GENERAL + "/MinCascadeLength";
	public static final String XMLPATH_GENERAL_MIXNAME = XMLPATH_GENERAL + "/MixName";
	public static final String XMLPATH_GENERAL_OPERATORNAME = XMLPATH_GENERAL + "/OperatorName";
	public static final String XMLPATH_GENERAL_MIXID = XMLPATH_GENERAL + "/MixID";
	public static final String XMLPATH_AUTOCONFIGURATION = "Network/InfoService/AllowAutoConfiguration";

	// Attributes
	public static final String XML_ATTRIBUTE_FALLBACK = "fallback";
	public static final String XML_ATTRIBUTE_PAYMENT = "payment";
	
	// Messages
	private static final String MSG_MIX_TYPE = "Mix Type";
	private static final String MSG_MIX_NAME = "Mix Name";
	private static final String MSG_CASCADE_NAME = "Cascade Name";
	private static final String MSG_FIRST_MIX = GeneralPanel.class.getName() + "_firstMix";
	private static final String MSG_MIDDLE_MIX = GeneralPanel.class.getName() + "_middleMix";
	private static final String MSG_LAST_MIX = GeneralPanel.class.getName() + "_lastMix";
	private static final String MSG_CONFIGURATION_STATIC = GeneralPanel.class.getName() + "_configStatic";
	private static final String MSG_CONFIGURATION_DYNAMIC = GeneralPanel.class.getName() + "_configDynamic";
	private static final String MSG_ALLOW_DYNAMIC_FALLBACK = GeneralPanel.class.getName() + "_allowDynamicFallback";
	private static final String MSG_ENABLE_PAYMENT = GeneralPanel.class.getName() + "_enablePayment";
	private static final String MSG_EXPERIMENTAL_FEATURE = GeneralPanel.class.getName() + "_experimentalFeature";
	private static final String MSG_TOO_MANY_INTERFACES = GeneralPanel.class.getName() + "_tooManyInterfaces";
	
	private static final String ACTION_UPDATE_IS = "UpdateInfoServices";
	
	public static final String PSEUDO_CASCADE_NAME = "******Dynamic cascade bug*******";

	// ComboBoxes
	private JComboBox m_combxMixType, m_combxConfiguration, m_combxCascadeLength;
	private JCheckBox m_cbxDynamicFallback;
	// TextFields
	//private JTextField m_tfMixName;
	private JTextField m_tfCascadeName;
	
	private JLabel m_cascadeNameLabel, m_lblCascadeLength;
	private TitledGridBagPanel m_panelGeneralSettings;
		
	private JLabel m_lblMixType;
	protected int m_mixTypeRow;
	
	// TODO: Remove these
	//private JPanel m_mixTypePanel;
	//private JCheckBox m_cbxFirstMix, m_cbxMiddleMix, m_cbxLastMix;
	
	// JCheckBox to (de-)activate the payment panel
	private JCheckBox m_cbxPayment;
	
	private JCheckBox m_cbxIgnoreOwnName;
	
	// InfoService stuff
	private JPanel m_infoServicePanel;
	private JTable m_infoServiceTable;
	private InfoServiceTableModel m_infoServiceModel;
	
	// ListenerInterfaces
	private JPanel m_listenerPanel;
	private JTable m_listenerTable;
	private IncomingConnectionTableModel m_listenerModel;

	/**
	 * Construct the panel for general Mix settings.
	 */
	public GeneralPanel()
	{
		super("General");
		// Get the initial constraints
		GridBagConstraints c = getInitialConstraints();
		
		// Create the 'General Settings'-panel
		m_panelGeneralSettings = new TitledGridBagPanel("General Settings");
		add(m_panelGeneralSettings, c);

		// Mix Configuration
		m_combxConfiguration = new JComboBox();
		m_combxConfiguration.setName(XMLPATH_AUTOCONFIGURATION);
		m_combxConfiguration.addItem(JAPMessages.getString(MSG_CONFIGURATION_STATIC));
		//m_combxConfiguration.addItem(JAPMessages.getString(MSG_CONFIGURATION_DYNAMIC));
		m_combxConfiguration.addItemListener(this);
		//m_combxConfiguration.setToolTipText(JAPMessages.getString(MSG_EXPERIMENTAL_FEATURE));

		m_cbxDynamicFallback = new JCheckBox(JAPMessages.getString(MSG_ALLOW_DYNAMIC_FALLBACK));
		m_cbxDynamicFallback.setToolTipText(JAPMessages.getString(MSG_EXPERIMENTAL_FEATURE));
		m_cbxDynamicFallback.setName(XMLPATH_AUTOCONFIGURATION + "/" + XML_ATTRIBUTE_FALLBACK );
		m_cbxDynamicFallback.addItemListener(this);
		// XXX: Temporarily disabled
		m_cbxDynamicFallback.setEnabled(false);
		
		m_panelGeneralSettings.addRow(new JLabel("Mix Configuration"), m_combxConfiguration,
									  m_cbxDynamicFallback, null, GridBagConstraints.HORIZONTAL);

		// Mix Type Label
		m_lblMixType = new JLabel(MSG_MIX_TYPE);
		// Mix Type ComboBox
		m_combxMixType = new JComboBox();
		m_combxMixType.setName(XMLPATH_GENERAL_MIXTYPE);
		m_combxMixType.addItem(JAPMessages.getString(MSG_FIRST_MIX));
		m_combxMixType.addItem(JAPMessages.getString(MSG_MIDDLE_MIX));
		m_combxMixType.addItem(JAPMessages.getString(MSG_LAST_MIX));
		m_combxMixType.addItemListener(this);
		// CheckBox for enabling/disabling payment panel
		m_cbxPayment = new JCheckBox(JAPMessages.getString(MSG_ENABLE_PAYMENT));
		m_cbxPayment.setToolTipText(JAPMessages.getString(MSG_ENABLE_PAYMENT));
		m_cbxPayment.setName(XMLPATH_GENERAL_MIXTYPE + "/" + XML_ATTRIBUTE_PAYMENT);
		m_cbxPayment.addItemListener(this);
		// Add the Mix Type row
		m_mixTypeRow = m_panelGeneralSettings.addRow(m_lblMixType, m_combxMixType, 
				m_cbxPayment, null, GridBagConstraints.HORIZONTAL);
		
		/*
		// FIXME: This could actually all be removed, right?
		m_mixTypePanel = new JPanel(new GridBagLayout());
		GridBagConstraints mixTypeConstraints = new GridBagConstraints();
		m_cbxFirstMix = new JCheckBox(JAPMessages.getString(MSG_FIRST_MIX));
		m_cbxFirstMix.setName(XMLPATH_GENERAL_MIXTYPE);
		m_cbxFirstMix.addItemListener(this);
		m_cbxMiddleMix = new JCheckBox(JAPMessages.getString(MSG_MIDDLE_MIX));
		m_cbxMiddleMix.setName(XMLPATH_GENERAL_MIXTYPE);
		m_cbxMiddleMix.addItemListener(this);
		m_cbxLastMix = new JCheckBox(JAPMessages.getString(MSG_LAST_MIX));
		m_cbxLastMix.setName(XMLPATH_GENERAL_MIXTYPE);
		m_cbxLastMix.addItemListener(this);
		m_mixTypePanel.add(m_cbxFirstMix, mixTypeConstraints);
		m_mixTypePanel.add(m_cbxMiddleMix, mixTypeConstraints);
		m_mixTypePanel.add(m_cbxLastMix, mixTypeConstraints);
		*/
		//m_panelGeneralSettings.replaceRow(m_lblMixType, m_mixTypePanel, m_mixTypeRow,
		//							  GridBagConstraints.HORIZONTAL);

		// Mix Name JTextField
		/*
		m_tfMixName = new MixConfigTextField(20);
		m_tfMixName.setText("");
		m_tfMixName.setName(XMLPATH_GENERAL_MIXNAME);
		m_tfMixName.addFocusListener(this);
		m_panelGeneralSettings.addRow(new JLabel(MSG_MIX_NAME), m_tfMixName,
									  GridBagConstraints.HORIZONTAL);
*/
		// Cascade Name JTextField; this field is disabled by selecting a middle mix type
		m_tfCascadeName = new MixConfigTextField(20);
		m_tfCascadeName.setName(XMLPATH_GENERAL_CASCADENAME);
		m_tfCascadeName.addFocusListener(this);
		
		// TODO: Remove cascade length
		m_combxCascadeLength = new JComboBox();
		m_combxCascadeLength.setVisible(false); /** @todo  not needed any more; remove... */
		m_combxCascadeLength.setName(XMLPATH_GENERAL_MIN_CASCADELENGTH);
		for (int i = 2; i <= 5; i++)
		{
			m_combxCascadeLength.addItem(new Integer(i));
		}
		m_combxCascadeLength.addItemListener(this);
		m_lblCascadeLength = new JLabel("Minimum cascade length");
		m_lblCascadeLength.setVisible(false); /** @todo  not needed any more; remove... */

		
		m_cbxIgnoreOwnName = new JCheckBox("Ignore own short name");
		m_cbxIgnoreOwnName.setName(GeneralPanel.XMLPATH_GENERAL_MIXNAME);
		m_cbxIgnoreOwnName.addItemListener(this);
		
		m_cascadeNameLabel = new JLabel(MSG_CASCADE_NAME);
		m_panelGeneralSettings.addRow(m_cascadeNameLabel, m_tfCascadeName, m_cbxIgnoreOwnName);
									 // m_lblCascadeLength, m_combxCascadeLength);

		// Initialize both of the tables here
		initInfoServicesTable(c);		
		initListenerInterfacesTable(c);
		
		// Keep the panels in place (on top)
		c.fill = GridBagConstraints.BOTH;
		c.gridy = 3;
		c.weighty = 1;
		this.add(new JLabel(), c);
	}

	// -------------------- PRIVATE METHODS --------------------
	
	/**
	 * Initialize a table for visualization of InfoServices
	 * @param c GridBagConstraints
	 */
	private void initInfoServicesTable(GridBagConstraints c) 
	{
		// Setup the panel
		m_infoServicePanel = new JPanel(new GridBagLayout());
		m_infoServicePanel.setBorder(new TitledBorder("Info Services"));
		m_infoServicePanel.setToolTipText("Configuration of Info Services");
		c.gridy = 1;
		add(m_infoServicePanel, c);
		// Basic cell renderers for both columns
		final TableCellRenderer hostRenderer = new DefaultTableCellRenderer()
		{
			protected void setValue(Object v)
			{
				super.setValue(v);
				setHorizontalAlignment(CENTER);
			}
		};
		final TableCellRenderer portRenderer = new DefaultTableCellRenderer()
		{
			protected void setValue(Object v)
			{
				int t = ((Integer)v).intValue();
				if (t <= 0) super.setValue("");
				else
				{
					setHorizontalAlignment(CENTER);
					super.setValue(v.toString());
				}
			}
		};
		// Create the table model
		m_infoServiceModel = new InfoServiceTableModel();
		m_infoServiceModel.addTableModelListener(this);
		m_infoServiceTable = new JTable(m_infoServiceModel)
		{
			public TableCellRenderer getCellRenderer(int row, int column)
			{
				switch (column)
				{
					case InfoServiceTableModel.HOST:
						return hostRenderer;
					case InfoServiceTableModel.PORT:
						return portRenderer;
					default:
						return super.getCellRenderer(row, column);
				}
			}
		};
		// Add the table to a ScrollPane
		JScrollPane scrollPane = new JScrollPane(m_infoServiceTable,
				  ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // Set the table name (Isn't this a Hack: The name is used as XML-path later on!)
		m_infoServiceTable.setName("Network");
		m_infoServiceTable.getTableHeader().setReorderingAllowed(false);
		m_infoServiceTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		m_infoServiceTable.setPreferredScrollableViewportSize(new Dimension(500, 50));
		// Add a mouse-listener for double clicks
		m_infoServiceTable.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					new InfoServiceDialog(MixConfig.getMainWindow(), "Modify Info Service", m_infoServiceModel, 
							m_infoServiceModel.getData(m_infoServiceTable.getSelectedRow())).setVisible(true);
				}
			}
		});
		// XXX: How to set this?
		int[] columnSizes = {300, 10};
		// Add columns
		for (int i=0; i<columnSizes.length; i++)
		{
			TableColumn column = m_infoServiceTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(columnSizes[i]);
		}
		// Now add the buttons
		for (int i=0; i<3; i++)
		{
			final JButton button;
			switch (i)
			{
			    // The 'Add'-button
				case 0:
					button = new JButton("Add");
					button.setActionCommand("AddInfoService");
					button.addActionListener(this);
					break;
			    // The 'Delete'-button
				case 1:
					button = new JButton("Delete");
					m_infoServiceTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
					{
						public void valueChanged(ListSelectionEvent e)
						{
							if (e.getValueIsAdjusting()) return;
							button.setEnabled(!((ListSelectionModel) e.getSource()).isSelectionEmpty());
						}
					});
					button.setEnabled(false);
					button.setActionCommand("DeleteInfoService");
					button.addActionListener(this);
					break;
				case 2:
					button = new JButton("Update");
					button.setActionCommand(ACTION_UPDATE_IS);
					button.addActionListener(this);
					break;
				default:
					throw (new RuntimeException("Unknown Button should be created."));
			}
			GridBagConstraints ibd = new GridBagConstraints();
			ibd.anchor = GridBagConstraints.NORTHWEST;
			ibd.insets = new Insets(5, 5, 5, 5);
			ibd.gridx = 1;
			ibd.gridy = i;
			ibd.fill = GridBagConstraints.HORIZONTAL;
			m_infoServicePanel.add(button, ibd);
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
		m_infoServicePanel.add(scrollPane, d);
	}
	
	/**
	 * Init the table for ListenerInterfaces
	 * @param c GridBagConstraints
	 */
	private void initListenerInterfacesTable(GridBagConstraints c) 
	{
		// Create the panel
		m_listenerPanel = new JPanel(new GridBagLayout());
		m_listenerPanel.setBorder(new TitledBorder("Listener Interfaces"));
		m_listenerPanel.setToolTipText("Interfaces the Mix should use for incoming connections (ListenerInterfaces)");
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(m_listenerPanel, c);
		// CellRenderers
		final TableCellRenderer centralRenderer = new DefaultTableCellRenderer()
		{
			protected void setValue(Object v)
			{
				super.setValue(v);
				setHorizontalAlignment(CENTER);
			}
		};
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
				super.setValue(ConnectionData.getTransportAsString(t));
				setHorizontalAlignment(CENTER);
			}
		};
		// Create the model and the table
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
						return portRenderer;
					default:
						//return super.getCellRenderer(row, column);
						return centralRenderer;
				}
			}
		};
		// Create a ScrollPane
		JScrollPane scrollPane = new JScrollPane(m_listenerTable,
												  ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
												  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// Setup the table
		m_listenerTable.setName("Network");
		m_listenerTable.getTableHeader().setReorderingAllowed(false);
		m_listenerTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		m_listenerTable.setPreferredScrollableViewportSize(new Dimension(500, 50));
		m_listenerTable.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					new IncomingDialog(MixConfig.getMainWindow(), "Change", m_listenerModel,
									   m_listenerModel.getData(m_listenerTable.getSelectedRow()),
									   getConfiguration().isMixOnCDEnabled()).setVisible(true);
				}
			}
		});
		// Setup columns
		int[] columnSizes = {15, 15, 60, 195, 40};
		for (int Index = 0; Index < columnSizes.length; Index++)
		{
			TableColumn column = m_listenerTable.getColumnModel().getColumn(Index);
			column.setPreferredWidth(columnSizes[Index]);
		}
		// Create the buttons
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
			ibd.fill = GridBagConstraints.HORIZONTAL;
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
		m_listenerPanel.add(scrollPane, d);
	}
	
	private boolean isFirstDynamicMix()
	{
		return (getConfiguration().getMixType() == MixConfiguration.MIXTYPE_FIRST &&
				getConfiguration().isAutoConfigurationAllowed() &&
				!getConfiguration().isFallbackEnabled());
	}
	
	/** 
	 * Set the information in "Listener Interfaces" according to MixOnCD 
	 */	
	private void setMixOnCDInfo(boolean a_configuredByMixOnCD)
	{
		if (a_configuredByMixOnCD)
		{
			for (int i = 0; i < m_listenerModel.getRowCount(); i++)
			{
				ConnectionData olddata = m_listenerModel.getData(i);
				if (olddata.getTransport() != ConnectionData.UNIX && !olddata.isVirtual())
				{
					ConnectionData newdata = (ConnectionData)olddata.clone();
					newdata.setHostname(JAPMessages.getString(MixOnCDPanel.MSG_CONFIGURED_BY_MIXONCD));
					m_listenerModel.changeData(newdata, olddata);
				}
			}
		}
		else
		{
			for (int i = 0; i < m_listenerModel.getRowCount(); i++)
			{
				ConnectionData olddata = m_listenerModel.getData(i);
				if (olddata.getHostname().equalsIgnoreCase(
								JAPMessages.getString(MixOnCDPanel.MSG_CONFIGURED_BY_MIXONCD)))
				{
					/*
					 * Bugfix: If the model changes here, a newly loaded configuration has no
					 * hostname if MixOnCD has been activated before loading this configuration.
					 */
					olddata.setHostname("");
				}
			}
		}
	}
	
	// -------------------- PROTECTED METHODS --------------------
	
	protected void enableComponents()
	{
		boolean bEnableCascadeName;

		//if (m_combxConfiguration.getSelectedItem().equals(JAPMessages.getString(MSG_CONFIGURATION_DYNAMIC)) &&
		//	m_cbxDynamicFallback.isSelected())
		//{
		//	m_cbxDynamicFallback.setSelected(false);
		//}
		//m_cbxDynamicFallback.setEnabled(
		//		  m_combxConfiguration.getSelectedItem().equals(
		//				  JAPMessages.getString(MSG_CONFIGURATION_STATIC)));
		
		// XXX: Currently always disabled
		m_cbxDynamicFallback.setEnabled(false);
		
		m_lblCascadeLength.setEnabled(getConfiguration().isAutoConfigurationAllowed() &&
			getConfiguration().getMixType() != MixConfiguration.MIXTYPE_LAST);
	    m_combxCascadeLength.setEnabled(getConfiguration().isAutoConfigurationAllowed() &&
			getConfiguration().getMixType() != MixConfiguration.MIXTYPE_LAST);

		bEnableCascadeName = (getConfiguration().getMixType() == MixConfiguration.MIXTYPE_FIRST &&
							  !isFirstDynamicMix());// ||
/*			(getConfiguration().getMixType() == MixConfiguration.MIXTYPE_LAST &&
			 getConfiguration().isAutoConfigurationAllowed());
*/
		m_tfCascadeName.setEnabled(bEnableCascadeName);
		m_cascadeNameLabel.setEnabled(bEnableCascadeName);

		//handle the bug in the dynamic mix
		if (isFirstDynamicMix())
		{
			save(m_tfCascadeName);
		}
	}
	
	/**
	 * @FIXME This is only needed because of a bug in the dynamic configuration
	 * of the mix.
	 * @param a_textField JTextField
	 */
	protected void load(JTextField a_textField)
	{
		super.load(a_textField);
		if (a_textField == m_tfCascadeName && a_textField.getText() != null &&
			a_textField.getText().equals(PSEUDO_CASCADE_NAME))
		{
			a_textField.setText("");
		}
	}
	
	protected void load(JCheckBox a_cbx)
	{
		// Payment checkbox
		if (a_cbx == m_cbxPayment)
		{
			// Try to get the attribute's value
			String sPayment = getConfiguration().getAttributeValue(XMLPATH_GENERAL_MIXTYPE, XML_ATTRIBUTE_PAYMENT);
			if (sPayment != null)
			{
				if (sPayment.equalsIgnoreCase("true"))
				{
					m_cbxPayment.setSelected(true);
				}
				else m_cbxPayment.setSelected(false);
			}
		}
		else if (a_cbx == m_cbxIgnoreOwnName)
		{
			String str = getConfiguration().getAttributeValue(a_cbx.getName(),  
					MixInfo.XML_ATTRIBUTE_NAME_FOR_CASCADE);
			if (str != null && (str.equals(MixInfo.NAME_TYPE_MIX) || str.equals(MixInfo.NAME_TYPE_OPERATOR)))
			{
				m_cbxIgnoreOwnName.setSelected(false);
			}
			else
			{
				m_cbxIgnoreOwnName.setSelected(true);
			}
		}
		else
		{
			super.load(a_cbx);
		}
	}
	
	protected void load(JComboBox a_combx)
	{
		if (a_combx == m_combxConfiguration)
		{
			if (!getConfiguration().isAutoConfigurationAllowed() ||
				getConfiguration().isFallbackEnabled())
			{
				m_cbxDynamicFallback.setSelected(getConfiguration().isFallbackEnabled());
				m_combxConfiguration.setSelectedItem(JAPMessages.getString(MSG_CONFIGURATION_STATIC));
			}
			else
			{
				m_combxConfiguration.setSelectedItem(JAPMessages.getString(MSG_CONFIGURATION_DYNAMIC));
			}
		}
		else if (a_combx == m_combxCascadeLength)
		{
			int index;

			if (getConfiguration().isAutoConfigurationAllowed() && getConfiguration().getMixType() !=
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
	
	/**
	 * @FIXME This is needed only because of a bug in the dynamic configuration
	 * of the mix.
	 * @param a_textField JTextField
	 */
	protected void save(JTextField a_textField)
	{
		if (a_textField == m_tfCascadeName && isFirstDynamicMix())
		{
			super.save(a_textField.getName(), PSEUDO_CASCADE_NAME);
		}
		else
		{
			super.save(a_textField);
		}
	}
	
	protected void save(JCheckBox a_cbx)
	{
		if (a_cbx == m_cbxDynamicFallback)
		{
			// FIXME: Do we need to call both of these?
			getConfiguration().setAttribute(XMLPATH_AUTOCONFIGURATION, XML_ATTRIBUTE_FALLBACK, m_cbxDynamicFallback.isSelected());
			getConfiguration().setValue(XMLPATH_AUTOCONFIGURATION, m_cbxDynamicFallback.isSelected());
		}
		else if (a_cbx == m_cbxPayment)
		{
			getConfiguration().setAttribute(XMLPATH_GENERAL_MIXTYPE, XML_ATTRIBUTE_PAYMENT, m_cbxPayment.isSelected());
		}
		else if (a_cbx == m_cbxIgnoreOwnName)
		{
			if (a_cbx.isSelected())
			{
				getConfiguration().removeAttribute(a_cbx.getName(), MixInfo.XML_ATTRIBUTE_NAME_FOR_CASCADE);
			}
			else
			{
				String str = getConfiguration().getAttributeValue(a_cbx.getName(), 
						MixInfo.XML_ATTRIBUTE_NAME_FOR_CASCADE);
				if (str != null && (str.equals(MixInfo.NAME_TYPE_MIX) || str.equals(MixInfo.NAME_TYPE_OPERATOR)))
				{
					// do nothing
				}
				else
				{
					// set default
					getConfiguration().setAttribute(a_cbx.getName(), MixInfo.XML_ATTRIBUTE_NAME_FOR_CASCADE, 
							MixInfo.NAME_TYPE_MIX);
				}
			}
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
			if (a_combx.getSelectedItem().equals(JAPMessages.getString(MSG_CONFIGURATION_STATIC)) &&
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
	
	// -------------------- PUBLIC METHODS --------------------
	
	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		super.setConfiguration(a_conf);
		a_conf.addChangeListener(this);
	}

	public void load() throws IOException
	{
		super.load();
		if (getConfiguration() != null)
		{
			setMixOnCDInfo(getConfiguration().isMixOnCDEnabled());
		}
	}
	
	// Trigger writing of table changes to the document
	public void tableChanged(TableModelEvent e)
	{
		enableComponents();
		if (this.isAutoSaveEnabled())
		{
			if (e.getSource() == m_infoServiceTable.getModel())
			{
				save(m_infoServiceTable);
			}
			if (e.getSource() == m_listenerTable.getModel())
			{
				save(m_listenerTable);
			}
		}
	}
	
	public void stateChanged(ChangeEvent a_event)
	{
		if (!(a_event instanceof ConfigurationEvent))
		{
			return;
		}
		ConfigurationEvent ce = (ConfigurationEvent) a_event;
		if (ce.getModifiedXMLPath().indexOf(MixOnCDPanel.XMLPATH_MIXONCD) >= 0)
		{
			setMixOnCDInfo(getConfiguration().isMixOnCDEnabled());
		}
		else if (ce.getModifiedXMLPath().equals(GeneralPanel.XMLPATH_GENERAL_CASCADENAME))
		{
			String value = getConfiguration().getValue(GeneralPanel.XMLPATH_GENERAL_CASCADENAME);
			if (value == null || !value.equals(m_tfCascadeName.getText()))
			{
				load(m_tfCascadeName);
			}
		}
	}

	public void actionPerformed(ActionEvent ae)
	{
		// 1. Actions on InfoServices:
		if (ae.getActionCommand().equals("AddInfoService"))
		{
			// Open the dialog for a new InfoService
			new InfoServiceDialog(MixConfig.getMainWindow(), "Add Info Service", m_infoServiceModel).setVisible(true);
		}
		else if (ae.getActionCommand().equals(ACTION_UPDATE_IS))
		{
			InfoServiceDBEntry isEntry;
			InfoServiceData isData;
			Hashtable hashIS = InfoServiceHolder.getInstance().getInfoServices();
			Enumeration enumIS;
			if (hashIS != null && hashIS.size() > 0)
			{
				enumIS = hashIS.elements();
				//Database.getInstance(InfoServiceDBEntry.class).removeAll();
				m_infoServiceModel.clear();
				while (enumIS.hasMoreElements())
				{
					isEntry = (InfoServiceDBEntry)enumIS.nextElement();
					
					Vector vecListeners = isEntry.getListenerInterfaces();
					ListenerInterface[] nList = new ListenerInterface[vecListeners.size()];
					for (int i = 0; i < vecListeners.size(); i++)
					{
						nList[i] = (ListenerInterface)vecListeners.elementAt(i);
					}
					
					isData = new InfoServiceData(MixConfiguration.XML_PATH_INFO_SERVICE, 
							nList);
					m_infoServiceModel.addData(isData);
				}
			}
		}
		else if (ae.getActionCommand().equals("DeleteInfoService"))
		{
			// Delete the selected row from the model
			m_infoServiceModel.deleteData(m_infoServiceTable.getSelectedRow());
		}
		// 2. Actions on ListenerInterfaces:
		else if (ae.getActionCommand().equals("AddIncoming"))
		{
			boolean bEditExisting = false;
			if (m_listenerModel.getRowCount() == 1)
			{
				if (m_listenerModel.getData(0).getHostname().equals(""))
				{
					bEditExisting = true;
				}
			}
			if (bEditExisting)
			{
				new IncomingDialog(MixConfig.getMainWindow(),
								   "Change", m_listenerModel,
								   m_listenerModel.getData(0),
								   getConfiguration().isMixOnCDEnabled()).setVisible(true);
			}
			else
			{
				new IncomingDialog(MixConfig.getMainWindow(), "Add", m_listenerModel,
								   getConfiguration().isMixOnCDEnabled()).setVisible(true);
			}
		}
		else if (ae.getActionCommand().equals("DeleteIncoming"))
		{
			m_listenerModel.deleteData(m_listenerTable.getSelectedRow());
		}
	}

	public Vector<String> check()
	{
		//LogHolder.log(LogLevel.DEBUG, LogType.MISC, "General Panel is being checked for validity!");
		Vector<String> errors = new Vector<String>();
		MixConfiguration mixConf = getConfiguration();
		String s;
		int mixType;

		try
		{
			mixType = getConfiguration().getMixType();
			s = mixConf.getValue(XMLPATH_GENERAL_CASCADENAME);
		/*	if ( ((mixType == MixConfiguration.MIXTYPE_FIRST && !isFirstDynamicMix()) ||
				  (mixType == MixConfiguration.MIXTYPE_LAST &&
				   getConfiguration().isAutoConfigurationAllowed())) &&
				(s == null || s.equals("")))*/
			if ((mixType == MixConfiguration.MIXTYPE_FIRST && !isFirstDynamicMix())  &&
				  (s == null || s.equals("")))
			{
				errors.addElement("Cascade Name not entered.");
			}
		}
		catch (NumberFormatException nfe)
		{
			errors.addElement("Invalid Mix type in configuration.");
		}
		
		// Check InfoServices by validating table data
		if (m_infoServiceModel.getRowCount() > 0)
		{
			for (int i = 0; i < m_infoServiceModel.getRowCount(); i++)
			{
				// Get every single one
				InfoServiceData infoService = m_infoServiceModel.getData(i);
				int noIfaces = infoService.getNumberOfListeners();
				// Check its ListenerInterfaces
				for (int j = 0; j < noIfaces; j++)
				{
					ListenerInterface iface = infoService.getListenerInterface(j);
					// Check the host
					if (!ListenerInterface.isValidHostname(iface.getHost()))
					{
						errors.addElement("Invalid host on" + " InfoService " + (i + 1) + " (interface " + (j + 1) + ").");
						// AND the port
						if (!ListenerInterface.isValidPort(iface.getPort()))
						{
							errors.addElement("Invalid host AND port on" + " InfoService " + (i + 1) + " (interface " + (j + 1) + ").");
						}	
						break;
					}
					// And the port
					if (!ListenerInterface.isValidPort(iface.getPort()))
					{
						errors.addElement("Invalid port on" + " InfoService " + (i + 1) + " (interface " + (j + 1) + ").");
						break;
					}
					LogHolder.log(LogLevel.DEBUG, LogType.NET, "Interface '"+iface.getHost()+":"+iface.getPort()
							+"' found to be valid");
				}
			}	
		}
		else
		{
			errors.addElement("At least one info service needs to be specified.");
		}

		// XXX: Previous code validated XML:
		/*
		s = getConfiguration().getValue("Network/InfoService/Host");
		if (s == null || s.equals(""))
		{
			errors.addElement("InfoService host name is incorrect.");
		}
		s = getConfiguration().getValue("Network/InfoService/Port");
		if (s == null || s.equals(""))
		{
			errors.addElement(
				"The Port field for the Info Service should not be blank.");
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
			errors.addElement("The port of the Info Service is invalid.");
		}
        */
		
		// Check ListenerInterfaces
		for (int i = 0; i < m_listenerModel.getRowCount(); i++)
		{
			if (m_listenerModel.getData(i).getHostname().equals(""))
			{
				errors.addElement("No hostname specified for listener interface " + (i + 1) + ".");
				break;
			}
		}

		// Check if every virtual interface has a corresponding hidden interface and vice versa
		Vector<Integer> hiddenPorts = new Vector<Integer>();
		Vector<Integer> virtualPorts = new Vector<Integer>();

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
		// Check if any virtual or hidden interfaces are conflicting with a default interface
		Vector<Integer> defaultPorts = new Vector<Integer>();
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
		// Check if there are two default interfaces with the same port
		Vector<Integer> failedDefaultPorts = new Vector<Integer>();
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

	
		// Check if every listener interface has a port if it is not UNIX transport
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
				errors.addElement("Autoconfiguration is not possible, if all " +
								  "listener interfaces are set to " +
								  "hidden or virtual. Add at least one non-hidden, " +
								  "non-virtual interface to enable autoconfiguration."
					);
			}
		}
		return errors;
	}

	public String getHelpContext()
	{
		return getClass().getName();
	}

	public Container getHelpExtractionDisplayContext() 
	{
		return null;
	}
}

