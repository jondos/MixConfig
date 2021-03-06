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

import gui.IPTextField;
import gui.JAPHelpContext;
import gui.dialog.JAPDialog;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import mixconfig.MixConfig;
import mixconfig.MixConfiguration;
import mixconfig.infoservice.InfoServiceData;
import mixconfig.infoservice.InfoServiceTableModel;
import mixconfig.network.IncomingConnectionTableModel;
import mixconfig.network.OutgoingConnectionTableModel;
import mixconfig.network.ProxyTableModel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import anon.crypto.ICertificate;
import anon.crypto.PKCS12;
import anon.pay.xml.XMLPriceCertificate;
import anon.util.Base64;
import anon.util.ClassUtil;

/** This is the abstract superclass of all configuration panels. It saves
 * the data entered by the user to the underlying configuration object, and updates
 * the panels if the configuration changes.<br>
 * Loading data from the <code>MixConfiguration</code> object into the panel controls
 * happens via the <code>load(..)</code> methods. They should be called only when a new empty
 * configuration is created, or a configuration is loaded from a file. The only public
 * <code>load</code> method is the one that iterates over all controls in the dialog
 * and sets their values to the corresponding data from the configuration object (the
 * corresponding XML keys are found via the <code>getName()</code> method in
 * <code>java.awt.Component</code>).<br>
 * Saving the controls' values to the configuration object is done via the <code>save()</code>
 * methods. They should be called whenever the user changes the value of a control to keep
 * the panels and the configuration object synchronized. To make this easier, this class
 * implements the <code>ItemListener</code> and <code>FocusListener</code> interfaces.
 * Controls that correspond to a configuration key should add this class to their listeners
 * list; the event handler methods in this class will then take care of the data synchronisation
 * between AWT/Swing components and the configuration object.
 * @author ronin &lt;ronin2@web.de&gt;
 * @todo Find another way to synchronize non-ItemSelectables. The FocusListener is not a safe way.
 */
public abstract class MixConfigPanel extends JPanel implements ItemListener, FocusListener,
		JAPHelpContext.IHelpContext
{
	public static final int MAX_COLUMN_LENGTH = 20;
	public static final int MAX_COMBO_BOX_LENGTH = 27;
	public static final int MAX_COORDINATE_FIELD_LENGTH = 7;

	public static final String MSG_WARNING_NO_MIX_CERT = MixConfigPanel.class.getName() + "_warningNoMixCert";
	public static final String MSG_WARNING_NO_OPERATOR_CERT = MixConfigPanel.class.getName() + "_warningOperatorCert";
	public static final String MSG_ERROR_BLANK_FIELD = MixConfigPanel.class.getName() + "_errorBlankField";

	/**
	 * Indicates whether changes in configuration controls should automatically
	 * be written to the MixConfiguration object
	 */
	private boolean m_autoSave = true;

	/** The Mix configuration currently being edited. */
	private MixConfiguration m_mixConf = null;

	private Insets m_insets;

	/** 
	 * Construct a new instance of {@link MixConfigPanel}
	 * @param a_name the initial name of the panel; must be a non-blank String longer than zero
	 */
	protected MixConfigPanel(String a_name)
	{
		super(new GridBagLayout());
		setName(a_name);
		// Create the insets
		m_insets = new Insets(5, 5, 5, 5);

		// this causes a NullPointerException as components of subclasses are not
		// yet constructed
		// setConfiguration(MixConfig.getMixConfiguration());
	}

	/** Enables used and disables unused components. Some configuration values are
	 * mutually exclusive; the panels must ensure that of two components corresponding
	 * to contradictory attributes, only one at a time is enabled.
	 */
	protected abstract void enableComponents();

	/** Check the panel for inconsistencies and return a <CODE>java.util.Vector</CODE>
	 * object containing possible warnings and error messages
	 * @return Possible error and warning messages
	 */
	public abstract Vector<String> check();
	
	/**
	 * Return the name of the panel assuming all panel classes are named correctly
	 * @return the name of the panel
	 */
	public String getPanelName()
	{
		String name = ClassUtil.getShortClassName(getClass());

		if (name.toLowerCase().endsWith("panel"))
		{
			name = name.substring(0, name.length() - 5);
		}

		return name;
	}

	/**
	 * Create an instance of {@link GridBagConstraints}, set initial values and return it
	 * @return
	 */
	protected GridBagConstraints getInitialConstraints()
	{
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = getDefaultInsets();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;				
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 0;
		return constraints;
	}
	
	/**
	 * Return the default insets for subpanels.
	 * @return Insets
	 */
	public Insets getDefaultInsets()
	{
		return m_insets;
	}

	/**
	 * (Re)sets the name of this panel.
	 * @param a_name the new name of the panel; must be a non-blank String longer than zero
	 */
	public final void setName(String a_name)
	{
		if (a_name != null && a_name.trim().length() > 0)
		{
			super.setName(a_name);
		}
		else
		{
			throw new IllegalArgumentException("Panel name must be a valid string!");

		}
	}

	public void itemStateChanged(ItemEvent ie)
	{
		try
		{
			Object source = ie.getSource();

			if (m_autoSave && ( (Component) source).getName() != null)
			{
				if (source instanceof JTextField)
				{
					// this actually can't happen as a JTextField is no ItemSelectable
					save( (JTextField) source);
				}
				else if (source instanceof JCheckBox)
				{
					save( (JCheckBox) source);
				}
				else if (source instanceof JComboBox)
				{
					save( (JComboBox) source);
				}
				else if (source instanceof JRadioButton)
				{
					if (ie.getStateChange() == ItemEvent.SELECTED)
					{
						save( (JRadioButton) source);
					}
				}
			}
			enableComponents();
			validate();
		}
		catch (Exception uee)
		{
			JAPDialog.showErrorDialog(MixConfig.getMainWindow(), null, uee);
		}
	}

	public void focusGained(FocusEvent e)
	{}

	public void focusLost(FocusEvent e)
	{
		try
		{
			if (e.getSource() instanceof JTextField)
			{
				// Save the value of a TextField NOT only if it is not empty
				save((JTextField)e.getSource());
			}
			else if (e.getSource() instanceof IPTextField)
			{
				save((IPTextField)e.getSource());
			}
		}
		catch (Exception ex)
		{
			// TODO: Don't show a dialog here.. maybe only some logging?
			JAPDialog.showErrorDialog(MixConfig.getMainWindow(), null, ex);
		}
	}

	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		Container p = this;
		do
		{
			p = p.getParent();
			if (p instanceof JTabbedPane)
			{
				JTabbedPane jtb = (JTabbedPane) p;
				for (int k = 0; k < jtb.getTabCount(); k++)
				{
					if (jtb.getComponentAt(k) == this)
					{
						jtb.setEnabledAt(k, enabled);
					}
				}
			}
		}
		while (p != null);
	}

	/** Informs the panel about a new Mix configuration and makes it load the attribute
	 * values from the configuration object into the text fields, combo boxes etc.
	 * @param a_mixConf The new configuration
	 * @throws IOException If an error occurs while transferring the data
	 */
	public void setConfiguration(MixConfiguration a_mixConf) throws IOException
	{
		this.m_mixConf = a_mixConf;
		load();
	}

	/** Returns the configuration object currently edited by this panel.
	 * @return The current configuration
	 */
	public MixConfiguration getConfiguration()
	{
		return m_mixConf;
	}

	/** Determines whether values changed in the panel should be automatically saved to
	 * the configuration object. Normally, all values are written to the configuration
	 * as soon as they are entered in the panel. However, it is sometimes necessary to disable
	 * this in order to prevent infinite loops.<br>
	 * For example, if the state of a combo box is changed, it casts an
	 * <CODE>java.awt.event.ItemEvent</CODE>. The containing panel catches the event
	 * through its implementation of <CODE>java.awt.event.ItemListener</CODE> and saves
	 * the changed value to the MixConfiguration object. This triggers a
	 * <CODE>javax.swing.event.ChangeEvent</CODE>. If the panel catches this event and
	 * sets the combo box value accordingly to keep the configuration and the panel
	 * synchronized, this will trigger an ItemEvent again, leading to an infinite
	 * ping-pong of events.
	 * @param a_autoSave <CODE>true</CODE> to enable auto saving, <CODE>false</CODE> to disable it
	 */
	protected void setAutoSaveEnabled(boolean a_autoSave)
	{
		m_autoSave = a_autoSave;
	}

	/** Returns whether auto save is enabled
	 * @return <CODE>true</CODE> if auto saving is enabled, <CODE>false</CODE> otherwise
	 */
	protected boolean isAutoSaveEnabled()
	{
		return m_autoSave;
	}

	/** Loads the attribute values from the configuration object into the panel's
	 * controls. This method iterates through all components of the panel and reads their
	 * names using the <CODE>getName()</CODE> method. If the name is not
	 * <CODE>null</CODE>, it retrieves the configuration attribute with the same name
	 * and sets the value of the component (text field, combo box etc.) according to
	 * the attribute value.
	 * @throws IOException If loading an attribute from the configuration fails
	 */
	public void load() throws IOException
	{
		// disable automatic saving of values to the MixConfiguration
		// to prevent infinite event loops
		setAutoSaveEnabled(false);

		load(this);

		// turn on auto saving again
		setAutoSaveEnabled(true);
	}

	/** Saves the attribute values from the panel's controls (text fields, combo boxes
         * etc.) to the configuration object. This method iterates through all components of the panel and reads their
         * names using the <CODE>getName()</CODE> method. If the name is not
         * <CODE>null</CODE>, it retrieves the configuration attribute with the same name
         * and sets the attribute value to the value of the component.
         * @throws IOException If saving a value to the configuration fails
         */
	public void save() throws IOException
	{
		save(this);
	}

	/**
	 * Saves the value of the IP address in the IPTextField.
	 * @param a_ipTextField an IPTextField
	 */
	protected void save(IPTextField a_ipTextField)
	{
		String s =a_ipTextField.getText().trim();

		if (!a_ipTextField.isEnabled() || !a_ipTextField.isCorrect())
		{
			s = null;
		}
		m_mixConf.setValue(a_ipTextField.getName(), s);
	}

	/** Saves the value of all components in the specified container. The method
	 * iterates over all of the container's components and invokes the appropriate save
	 * method for them. If the container is an instance of <CODE>mixconfig.networkpanel.IPTextField</CODE>, the value of
	 * the IPTextField is saved instead.
	 * @param a A container
	 * @throws IOException If an error occurs while writing the values to the configuration object
	 */
	protected void save(Container a) throws IOException
	{
		if (a instanceof IPTextField)
		{
			save((IPTextField)a);
			return;
		}

		Component c[] = a.getComponents();
		for (int i = 0; i < c.length; i++)
		{
			if (c[i].getName() == null || c[i].getName().equals(""))
			{
				if (c[i] instanceof Container)
				{
					save( (Container) c[i]);
				}
				continue;
			}

			if (c[i] instanceof JTable)
			{
				save( (JTable) c[i]);
			}
			if (c[i] instanceof CertPanel)
			{
				save( (CertPanel) c[i]);
			}
			else if (c[i] instanceof JTextField)
			{
				save( (JTextField) c[i]);
			}
			else if (c[i] instanceof JComboBox)
			{
				save( (JComboBox) c[i]);
			}
			else if (c[i] instanceof JCheckBox)
			{
				save( (JCheckBox) c[i]);
			}
			else if (c[i] instanceof JRadioButton)
			{
				save( (JRadioButton) c[i]);
			}
			else if (c[i] instanceof IPTextField)
			{
				save( (IPTextField) c[i]);
			}
			else if (c[i] instanceof Container)
			{
				save( (Container) c[i]);
			}
		}
	}

	/** Saves the values of a table to the configuration object. The value is only saved
	 * if the component is currently enabled, otherwise, a <CODE>null</CODE> value is saved.
	 * @param a A table
	 */
	protected void save(JTable table)
	{
		Object model = table.getModel();
		if (model instanceof InfoServiceTableModel)
		{
			m_mixConf.setValue("Network", (InfoServiceTableModel) model);
		}
		else if (model instanceof IncomingConnectionTableModel)
		{
			m_mixConf.setValue("Network", (IncomingConnectionTableModel) model);
		}
		else if (model instanceof OutgoingConnectionTableModel)
		{
			m_mixConf.setValue(table.getName(), (OutgoingConnectionTableModel) model);
		}
		else if (model instanceof ProxyTableModel)
		{
			m_mixConf.setValue(table.getName(), (ProxyTableModel) model);
		}
		else if (model instanceof CascadePanel.MixListTableModel)
		{
			m_mixConf.setValue(table.getName(), (CascadePanel.MixListTableModel) model);
		}
	}

	/** Saves the values of a certificate panel to the configuration object. The value is only saved
	 * if the component is currently enabled, otherwise, the element with the same XML path
	 * as the component's name is removed from the XML structure.
	 * @param a A certificate panel
	 * @throws IOException If an error occurs while writing the values to the configuration object
	 */
	protected void save(CertPanel a) throws IOException
	{
		String name = a.getName();
		ICertificate certificate = a.getCert();
		if (!a.isEnabled() || certificate == null)
		{
			//LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Panel enabled? " + a.isEnabled() + ", certificate == null? " + (certificate == null));
			m_mixConf.removeNode(name);
		}
		else
		{
			if (name.endsWith("OwnCertificate"))
			{
				m_mixConf.setValue(name + "/X509PKCS12", ((PKCS12)certificate).toByteArray(a.getPrivateCertPassword()));
			}
			m_mixConf.setValue(name + "/X509Certificate", certificate.getX509Certificate().toByteArray());
		}
	}
		
	/**
	 * Save a price certificate from PriceCertPanel to the configuration
	 * @param a_panel
	 * @throws IOException
	 */
	protected void save(PriceCertPanel a_panel) throws IOException
	{
		String name = a_panel.getName();
		XMLPriceCertificate cert = a_panel.getCert();
		if (cert == null)
		{
			m_mixConf.removeNode(name);
		}
		else
		{
			m_mixConf.setValue(name, cert);
		}
	}

	/** 
	 * Save the values of a text field to the configuration object. The value is only saved
     * if the component is currently enabled, otherwise, the element with the same XML path
     * as the component's name is removed from the XML structure.
     * @param a A text field
     */
	protected void save(JTextField a_textfield)
	{
		// Trim the text before saving
		String s = a_textfield.getText().trim();
		if (!a_textfield.isEnabled())
		{
			// Remove the node
			m_mixConf.removeNode(a_textfield.getName());
		}
		else
		{
			save(a_textfield.getName(), s);
		}
	}

	/**
	 * Saves the value to the specified XML path in the configuration object.
	 * If the value is null or empty the path specified is deleted.
	 * @param a_xmlPath an xml path
	 * @param a_value a value
	 */
	protected void save(String a_xmlPath, String a_value)
	{
		// String variable to store the value
		String sValue = null;
		// Remove the node if a_value is null or empty string
	    if ((a_value == null || a_value.trim().length() == 0) && 
	    	!m_mixConf.hasAttributes(a_xmlPath))
		{
			m_mixConf.removeNode(a_xmlPath);
		}
	    // Do nothing if nothing has changed
	    else if (((sValue = m_mixConf.getValue(a_xmlPath)) != null) && sValue.equals(a_value))
	    {
	    	//LogHolder.log(LogLevel.DEBUG, LogType.MISC, "NOT changed: " + a_xmlPath);
	    	return;
	    }	    
	    // Otherwise save the value
	    else
		{
			m_mixConf.setValue(a_xmlPath, a_value);
		}
	}

	/** Saves the values of a checkbox to the configuration object. If the component
         * is currently disabled, a value of <code>false</code> is saved no matter what
         * the selected state of the checkbox is.
         * @param a A checkbox
         */
	protected void save(JCheckBox a)
	{
		m_mixConf.setValue(a.getName(), a.isSelected() && a.isEnabled());
	}

	/** Saves the values of a combo box to the configuration object. The value is only saved
         * if the component is currently enabled, otherwise, the element with the same XML path
         * as the component's name is removed from the XML structure.
         * @param a A combo box
         */
	protected void save(JComboBox a)
	{
		int s = a.getSelectedIndex();
		if (!a.isEnabled())
		{
			m_mixConf.removeNode(a.getName());
		}
		else
		{
			if (a.getName().equals(GeneralPanel.XMLPATH_GENERAL_MIXTYPE))
			{
				s = (int)Math.pow(2, s);
			}
			m_mixConf.setValue(a.getName(), s);
		}
	}

	/** Saves the values of a radio button to the configuration object. This method does
	 * not save the value directly, but tries to retrieve the radio button's group and
	 * invokes the appropriate save method.
	 * @param a A radio button
	 * the encoding is UTF-8. If this encoding is not supported, an exception is
	 * thrown.
	 */
	protected void save(JRadioButton a)
	{
		try
		{
			ToggleButtonModel bm = (ToggleButtonModel) a.getModel();
			ButtonGroup group = bm.getGroup();
			save(group);
		}
		catch (ClassCastException ButtonModel_is_no_ToggleButtonModel)
		{
			/* IGNORE */
		}
	}

	/** Saves the value of the currently selected button in the specified button group
         * to the configuration object, and removes the values of the unselected buttons in
         * the group. If the component is currently disabled, a <CODE>null</CODE> value is saved.
         * @param a A button group
         */
	protected void save(ButtonGroup a)
	{
		/* this one does nothing; override it if needed */
	}

	/** Loads the value of all components in the specified container. The method
	 * iterates over all of the container's components and invokes the appropriate load
	 * method for them. If the container is an instance of <CODE>mixconfig.networkpanel.IPTextField</CODE>,
	 * the value is loaded only into the IPTextField instead.
	 * @param a A container
	 * @throws IOException If an error occurs while writing the values to the configuration object
	 */
	protected void load(Container a) throws IOException
	{
		if (a instanceof IPTextField)
		{
			load((IPTextField) a);
			return;
		}

		Component c[] = a.getComponents();
		for (int i = 0; i < c.length; i++)
		{
			if (c[i] instanceof Container)
			{
				load( (Container) c[i]);
			}

			if (c[i].getName() != null)
			{
				if (c[i] instanceof JTable)
				{
					load( (JTable) c[i]);
				}
				else if (c[i] instanceof CertPanel)
				{
					load( (CertPanel) c[i]);
				}
				else if (c[i] instanceof PriceCertPanel)
				{
					load( (PriceCertPanel) c[i]);
				}
				else if (c[i] instanceof JTextField)
				{
					load( (JTextField) c[i]);
				}
				else if (c[i] instanceof JCheckBox)
				{
					load( (JCheckBox) c[i]);
				}
				else if (c[i] instanceof JComboBox)
				{
					load( (JComboBox) c[i]);
				}
				else if (c[i] instanceof JRadioButton)
				{
					load( (JRadioButton) c[i]);
				}
				else if (c[i] instanceof IPTextField)
				{
					load( (IPTextField) c[i]);
				}

			}
		}
	}	
	
	/** Loads the value with the same name as the specified text field from the configuration object
	 * and sets its value accordingly.
	 * @param a A text field
	 */
	protected void load(JTextField a)
	{
		a.setText(m_mixConf.getValue(a.getName()));
	}

	/** Loads the value with the same name as the specified checkbox from the configuration object
	 * and sets its value accordingly.
	 * @param a A check box
	 */
	protected void load(JCheckBox a)
	{
		Boolean b = new Boolean(m_mixConf.getValue(a.getName()));
		a.setSelected(b.booleanValue());
	}

	/** Loads the value with the same name as the specified combo box from the configuration object
	 * and sets its value accordingly. If the value in the configuration is not an <code>int<code>,
	 * a value of 0 (zero) is assumed by default.
	 * @param a A combo box
	 */
	protected void load(JComboBox a)
	{
		String s = m_mixConf.getValue(a.getName());
		int i;
		try
		{
			i = Integer.valueOf(s).intValue();
		}
		catch (NumberFormatException nfe)
		{
			i = 0;
		}
		if (a.getName().indexOf(GeneralPanel.XMLPATH_GENERAL_MIXTYPE) >= 0)
		{
			i = (int)(Math.log(i) / Math.log(2));
		}
		a.setSelectedIndex(i);
	}

	/** Loads the value with the same name as the specified combo box from the configuration object
	 * and sets its value accordingly.
	 * @param a A radio button
	 */
	protected void load(JRadioButton a)
	{
		Boolean b = new Boolean(m_mixConf.getValue(a.getName()));
		a.setSelected(b.booleanValue());
	}

	/** 
	 * Load the value with the same name as the specified table from the 
	 * configuration object and set its value accordingly.
	 * 
	 * @param a table
	 */
	protected void load(JTable table)
	{
		NodeList nl;
		Object model = table.getModel();
		boolean prevAutoSave = this.m_autoSave;
		this.setAutoSaveEnabled(false);
		// Check for InfoServiceTableModel
		if (model instanceof InfoServiceTableModel) 
		{
			// It is an InfoServiceTableModel
			InfoServiceTableModel tableModel = (InfoServiceTableModel) model;
			// Check for element 'InfoServices'
			nl = m_mixConf.getDocument().getElementsByTagName("InfoServices");
			// 1. If there is exactly one occurrence
			if (nl.getLength() == 1) 
			{
				tableModel.readFromElement((Element)nl.item(0));
			} 
			// 2. There is no such element ..
			else if (nl.getLength() == 0)
			{
				// Check for an old configuration to convert it
				nl = m_mixConf.getDocument().getElementsByTagName("InfoService");
				// For all 'InfoService'-elements:
				for (int i=0; i < nl.getLength(); i++) {
					// Check if the parent node is 'Network'
					Node parent = nl.item(i).getParentNode();
					if (!parent.getNodeName().equals("InfoServices"))
					{
						LogHolder.log(LogLevel.NOTICE, LogType.NET, "Found old InfoService element --> converting it ..");
						// This is it, try to determine host and port
						String sHost="";
						int iPort = 0;
						// Get the first child
						Node child = nl.item(i).getFirstChild();
						while (child != null)
						{
							// Is it the Host?
							if (child.getNodeName().equals("Host"))
							{
								sHost = child.getTextContent();
								LogHolder.log(LogLevel.DEBUG, LogType.NET, "The hostname is " + sHost);
							}
							// Is it the Port?
							else if (child.getNodeName().equals("Port"))
							{
								iPort = new Integer(child.getTextContent());
								LogHolder.log(LogLevel.DEBUG, LogType.NET, "The port is " + iPort);
							}
							child = child.getNextSibling();
						}
						tableModel.clear();
						tableModel.addData(new InfoServiceData(MixConfiguration.XML_PATH_INFO_SERVICE, sHost, iPort));
					}
				}
			}
			// 3. More than 1 ore less than 0 elements: return?
			else return;
			// In any case: remove old elements:
			nl = m_mixConf.getDocument().getElementsByTagName("InfoService");
			// For all 'InfoService'-elements:
			for (int i=0; i < nl.getLength(); i++) {
				// Check if the parent node is wrong
				Node parent = nl.item(i).getParentNode();
				if (!parent.getNodeName().equals("InfoServices"))
				{
					parent.removeChild(nl.item(i));
					LogHolder.log(LogLevel.NOTICE, LogType.NET, "Removed deprecated InfoService node");
					// .. And set changes to the document
					m_mixConf.setValue("Network", tableModel);
				}
			}
		}
		// ListenerInterfaces of this Mix
		else if (model instanceof IncomingConnectionTableModel)
		{
			IncomingConnectionTableModel in = (IncomingConnectionTableModel) model;
			// Find the right 'ListenerInterfaces' element here
			nl = m_mixConf.getDocument().getElementsByTagName("ListenerInterfaces");
			if (nl.getLength() == 0) return;
			for (int i=0; i<nl.getLength(); i++)
			{
				Node parent = nl.item(i).getParentNode();
				if (parent.getNodeName().equals("Network"))
				{
					parent = parent.getParentNode();
					if (parent.getNodeName().equals("MixConfiguration"))
					{
						in.readFromElement((Element)nl.item(i));
					}
				}
			}
		}
		// TODO: Have a look at this part!
		else if (model instanceof OutgoingConnectionTableModel)
		{
			OutgoingConnectionTableModel out = (OutgoingConnectionTableModel) model;
			nl = m_mixConf.getDocument().getElementsByTagName("Network");
			if (nl.getLength() != 0)
			{
				Node n = nl.item(0).getFirstChild();
				while (n != null)
				{
					if (n.getNodeName().equals("NextMix") ||
						n.getNodeName().equals("Proxies"))
					{
						out.readFromElement( (Element) n);
					}
					n = n.getNextSibling();
				}
			}
		}
		else if (model instanceof ProxyTableModel)
		{
			ProxyTableModel tableModel = (ProxyTableModel)model;
			nl = m_mixConf.getDocument().getElementsByTagName("Network");
			if (nl.getLength() != 0)
			{
				Node n = nl.item(0).getFirstChild();
				while (n != null)
				{
					if (n.getNodeName().equals("Proxies"))
					{
						tableModel.readFromElement((Element)n);
					}
					n = n.getNextSibling();
				}
			}
		}
		// Reset AutoSave
		this.setAutoSaveEnabled(prevAutoSave);
	}

	protected void load(IPTextField a_ipTextField)
	{
		if (a_ipTextField.getName() != null)
		{
			( (IPTextField) a_ipTextField).setText(m_mixConf.getValue(a_ipTextField.getName()));
			return;
		}
	}

	/** Loads the value with the same name as the specified certificate panel from the 
	 * configuration object and sets its value accordingly.
	 * @param a A certificate panel
	 * @throws IOException If an error occurs while writing the values to the configuration object
	 */
	protected void load(CertPanel a_panel) throws IOException
	{
		String name = a_panel.getName();
		if (name.endsWith("OwnCertificate"))
		{
			name = name + "/X509PKCS12";
		}
		else
		{
			name = name + "/X509Certificate";
		}

		String cert = m_mixConf.getValue(name);
		byte b[] = null;

		if (cert != null && !cert.equals(""))
		{
			b = Base64.decode(cert);
		}
		// Deactivate auto-signing of OwnCertificate with OperatorCertificate while loading from file
		CertPanel.setAutoSign(false);
		a_panel.setCert(b);
		// Activate auto-signing again
		CertPanel.setAutoSign(true);
	}
	
	/**
	 * Load a price certificate from a configuration into a PriceCertPanel
	 */
	protected void load(PriceCertPanel a_panel) throws IOException
	{
		// Find the node <PriceCertificate> in the configuration
		NodeList nl = m_mixConf.getDocument().getElementsByTagName("PriceCertificate");
		if (nl.getLength() >= 1)
		// TODO: Remove duplicates Elements?
		{
			Node priceCertNode = nl.item(0);
		
			// Create a price certificate, if the node exists
			XMLPriceCertificate cert = null;
			if (priceCertNode != null && priceCertNode instanceof Element)
			{
				Element priceCertElement = (Element)priceCertNode;
				try
				{
					cert = new XMLPriceCertificate(priceCertElement);
				}
				catch (Exception ex)
				{
					LogHolder.log(LogLevel.EXCEPTION, LogType.PAY, "Exception while loading price certificate", ex);
				}
			}
			a_panel.setCert(cert);
		}
		else
		{
			XMLPriceCertificate c = null; 
			a_panel.setCert(c);
		}
	}

	/** This subclass of <code>javax.swing.JToggleButton.ToggleButtonModel</code> is required
	 * to provide the <code>getGroup()</code> method.
	 * The real <code>javax.swing.JToggleButton.ToggleButtonModel.getGroup()</code> method
	 * is only available in Swing version >= 1.3, and <code>MixConfigPanel</code>
	 * is meant to be compatible with all Swing versions.
	 */
	protected class ToggleButtonModel extends javax.swing.JToggleButton.ToggleButtonModel
	{
		/** Returns the button group belonging to this button model
		 * @return The button group of this model
		 */
		public ButtonGroup getGroup()
		{
			return super.group;
		}
	}

	/**
	 * Checks whether the specified String value is parseable into an int value.
	 * @param str A string
	 * @return boolean <code>true</code> if the specified string can be parsed
	 * into an int value, <code>false</code> otherwise
	 */
	protected boolean isNumber(String str)
	{
		try
		{
			Integer.parseInt(str, 10);
			return true;
		}
		catch (NumberFormatException ev)
		{
			return false;
		}
	}

	/** Returns the component with the specified name.
	 * @param a_name The name of the desired component
	 * @return The component with the specified name, or <CODE>null</CODE> if there is
	 * no component with this name in this container
	 */
	protected Component getComponentByName(String a_name)
	{
		Component c[] = this.getComponents();
		for (int i = 0; i < c.length; i++)
		{
			if (c[i].getName().equals(a_name))
			{
				return c[i];
			}
		}
		return null;
	}
}