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
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToggleButton.ToggleButtonModel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import anon.util.Base64;
import mixconfig.networkpanel.IPTextField;
import mixconfig.networkpanel.IncomingConnectionTableModel;
import mixconfig.networkpanel.OutgoingConnectionTableModel;
import javax.swing.JTabbedPane;

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
public abstract class MixConfigPanel extends JPanel implements ItemListener, FocusListener
{
	/**
	 * Indicates whether changes in configuration controls should automatically
	 * be written to the MixConfiguration object
	 */
	private boolean m_autoSave = true;

	/** The Mix configuration currently being edited. */
	private MixConfiguration m_mixConf = null;

	/** Constructs a new instance of <CODE>MixConfiPanel</CODE> */
	protected MixConfigPanel()
	{
		super();
		// this causes a NullPointerException as components of subclasses are not
		// yet constructed
		// setConfiguration(MixConfig.getMixConfiguration());
	}

	/** Checks the panel for inconsistencies and returns a <CODE>java.util.Vector</CODE>
	 * object containing possible warnings and error messages
	 * @return Possible error and warning messages
	 */
	public abstract Vector check();

	public void itemStateChanged(ItemEvent ie)
	{
		try
		{
			Object source = ie.getSource();

			enableComponents();

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
		}
		catch (Exception uee)
		{
			MixConfig.handleException(uee);
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
				save( (JTextField) e.getSource());
			}
		}
		catch (Exception ex)
		{
			MixConfig.handleException(ex);
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
			String s = ( (IPTextField) a).getText().trim();
			if (!a.isEnabled())
			{
				s = null;
			}
			m_mixConf.setAttribute(a.getName(), s);
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
	protected void save(JTable a)
	{
		Object o = a.getModel();
		if (o instanceof IncomingConnectionTableModel)
		{
			m_mixConf.setAttribute(a.getName(), (IncomingConnectionTableModel) o);
		}
		else if (o instanceof OutgoingConnectionTableModel)
		{
			m_mixConf.setAttribute(a.getName(), (OutgoingConnectionTableModel) o);
		}
		else if (o instanceof CascadePanel.MixListTableModel)
		{
			m_mixConf.setAttribute(a.getName(), (CascadePanel.MixListTableModel) o);
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
		byte[] b = a.getCert();
		if (!a.isEnabled() || b == null)
		{
			m_mixConf.removeAttribute(name);
		}
		else
		{
			if (name.endsWith("OwnCertificate"))
			{
				m_mixConf.setAttribute(name + "/X509PKCS12", b);
				b = a.getPubCert();
			}

			m_mixConf.setAttribute(name + "/X509Certificate", b);
		}
	}

	/** Saves the values of a text field to the configuration object. The value is only saved
         * if the component is currently enabled, otherwise, the element with the same XML path
         * as the component's name is removed from the XML structure.
         * @param a A text field
         */
	protected void save(JTextField a)
	{
		String s = a.getText().trim();
		if (!a.isEnabled())
		{
			m_mixConf.removeAttribute(a.getName());
		}
		else
		{
			m_mixConf.setAttribute(a.getName(), s);
		}
	}

	/** Saves the values of a checkbox to the configuration object. If the component
         * is currently disabled, a value of <code>false</code> is saved no matter what
         * the selected state of the checkbox is.
         * @param a A checkbox
         */
	protected void save(JCheckBox a)
	{
		m_mixConf.setAttribute(a.getName(), a.isSelected() && a.isEnabled());
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
			m_mixConf.removeAttribute(a.getName());
		}
		else
		{
			m_mixConf.setAttribute(a.getName(), s);
		}
	}

	/** Saves the values of a radio button to the configuration object. This method does
	 * not save the value directly, but tries to retrieve the radio button's group and
	 * invokes the appropriate save method.
	 * @param a A radio button
	 * @throws UnsupportedEncodingException String values are encoded with <CODE>javax.net.URLEncoder.encode(String, String)</CODE>;
	 * the encoding is UTF-8. If this encoding is not supported, an exception is
	 * thrown.
	 */
	protected void save(JRadioButton a) throws UnsupportedEncodingException
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

	/** Enables used and disables unused components. Some configuration values are
	 * mutually exclusive; the panels must ensure that of two components corresponding
	 * to contradictory attributes, only one at a time is enabled.
	 */
	protected abstract void enableComponents();

	/** Loads the value with the same name as the specified text field from the configuration object
	 * and sets its value accordingly.
	 * @param a A text field
	 */
	protected void load(JTextField a)
	{
		a.setText(m_mixConf.getAttribute(a.getName()));
	}

	/** Loads the value with the same name as the specified checkbox from the configuration object
	 * and sets its value accordingly.
	 * @param a A check box
	 */
	protected void load(JCheckBox a)
	{
		Boolean b = new Boolean(m_mixConf.getAttribute(a.getName()));
		a.setSelected(b.booleanValue());
	}

	/** Loads the value with the same name as the specified combo box from the configuration object
	 * and sets its value accordingly. If the value in the configuration is not an <code>int<code>,
	 * a value of 0 (zero) is assumed by default.
	 * @param a A combo box
	 */
	protected void load(JComboBox a)
	{
		String s = m_mixConf.getAttribute(a.getName());
		int i;
		try
		{
			i = Integer.valueOf(s).intValue();
		}
		catch (NumberFormatException nfe)
		{
			i = 0;
		}
		a.setSelectedIndex(i);
	}

	/** Loads the value with the same name as the specified combo box from the configuration object
	 * and sets its value accordingly.
	 * @param a A radio button
	 */
	protected void load(JRadioButton a)
	{
		Boolean b = new Boolean(m_mixConf.getAttribute(a.getName()));
		a.setSelected(b.booleanValue());
	}

	/** Loads the value with the same name as the specified table from the configuration object
	 * and sets its value accordingly.
	 * @param a A table
	 */
	protected void load(JTable a)
	{
		NodeList nl;
		Object o = a.getModel();
		boolean prevAutoSave = this.m_autoSave;
		this.setAutoSaveEnabled(false);
		if (o instanceof IncomingConnectionTableModel)
		{
			IncomingConnectionTableModel in = (IncomingConnectionTableModel) o;
			nl = m_mixConf.getDocument().getElementsByTagName("ListenerInterfaces");
			if (nl.getLength() == 0)
			{
				return;
			}
			in.readFromElement( (Element) nl.item(0));
		}
		else if (o instanceof OutgoingConnectionTableModel)
		{
			OutgoingConnectionTableModel out = (OutgoingConnectionTableModel) o;
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
		this.setAutoSaveEnabled(prevAutoSave);
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
		if (a instanceof IPTextField && a.getName() != null)
		{
			( (IPTextField) a).setText(m_mixConf.getAttribute(a.getName()));
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
			}
		}
	}

	/** Loads the value with the same name as the specified certificate panel from the configuration object
	 * and sets its value accordingly.
	 * @param a A certificate panel
	 * @throws IOException If an error occurs while writing the values to the configuration object
	 */
	protected void load(CertPanel a) throws IOException
	{
		String name = a.getName();
		if (name.endsWith("OwnCertificate"))
		{
			name = name + "/X509PKCS12";
		}
		else
		{
			name = name + "/X509Certificate";
		}

		String cert = m_mixConf.getAttribute(name);
		byte b[] = null;

		if (cert != null && !cert.equals(""))
		{
			b = Base64.decode(cert);
		}
		a.setCert(b);
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
