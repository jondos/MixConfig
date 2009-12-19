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

import gui.dialog.JAPDialog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import mixconfig.infoservice.InfoServiceData;
import mixconfig.infoservice.InfoServiceTableModel;
import mixconfig.network.IncomingConnectionTableModel;
import mixconfig.network.OutgoingConnectionTableModel;
import mixconfig.network.ProxyTableModel;
import mixconfig.panels.CascadePanel;
import mixconfig.panels.CertPanel;
import mixconfig.panels.GeneralPanel;
import mixconfig.panels.MixOnCDPanel;
import mixconfig.panels.PaymentPanel;
import mixconfig.panels.PriceCertPanel;


import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import anon.infoservice.Database;
import anon.infoservice.InfoServiceDBEntry;
import anon.infoservice.MixInfo;
import anon.pay.xml.XMLPriceCertificate;
import anon.util.Base64;
import anon.util.ResourceLoader;
import anon.util.XMLParseException;
import anon.util.XMLUtil;

/** 
 * This class provides unified access to the Mix 
 * configuration that is stored as a DOM document.
 * 
 * @author ronin &lt;ronin2@web.de&gt;
 * @author Johannes Renner
 */
public class MixConfiguration
{
	/** Indicates that the Mix is the first in a cascade */
	public static final int MIXTYPE_FIRST = 1;

	/** Indicates that the Mix is within a cascade */
	public static final int MIXTYPE_MIDDLE = 2;

	/** Indicates that the Mix is the last in a cascade */
	public static final int MIXTYPE_LAST = 4;

	/** Indicates that no logging should take place */
	public static final int LOG_NONE = 0;

	/** Indicates that logging output should be printed to the console */
	public static final int LOG_CONSOLE = 1;

	/** Indicates that logging output should be sent to the system log service */
	public static final int LOG_SYSLOG = 2;

	/** Indicates that logging output should be saved to a directory */
	public static final int LOG_DIRECTORY = 3;
	
	public static final String XML_PATH_INFO_SERVICE = "InfoService";

	/** An array containing the Mix types as String values. The indices correspond to
	   the MIXTYPE_xxx constants */
	private static final String MIXTYPE_NAME[] = {"FirstMix", "MiddleMix", "LastMix"};

	/** The configuration file information version number. */
	private static final String VERSION = "0.61";

	private static final String XML_ATTRIBUTE_VERSION = "version";

	/** The configuration as a DOM document */
	private Document m_configuration = null;

	private boolean m_bSavedToFile = true;

	/** A list of <CODE>ChangeListener</CODE>s receiving events from this object
	 * whenever the value of an attribute changes */
	private Vector<ChangeListener> m_changeListeners = new Vector<ChangeListener>();

	// -------------------- CONSTRUCTORS --------------------
	
	/** Constructs a new instance of <CODE>MixConfiguration</CODE>. The configuration
	 * contains only the root element and empty elements on the second level. The leaf
	 * elements are created as soon as the corresponding attributes are set.
	 * @throws XMLParseException If parsing the configuration string causes an error */
	public MixConfiguration() throws XMLParseException
	{
		String mixConfigXML =
			"<?xml version=\"1.0\"?>" +
			"<MixConfiguration version=\"" + VERSION + "\">" +
			"</MixConfiguration>";

		m_configuration = XMLUtil.toXMLDocument(mixConfigXML);
		this.setDefaults();
		this.setSavedToFile();
	}

	/** Constructs a new instance of <CODE>MixConfiguration</CODE>. The configuration is
	 * read from the specified <CODE>java.io.Reader</CODE>
	 * @param r A <CODE>Reader</CODE> providing the configuration
	 * @throws XMLParseException If an error occurs while parsing the input from the reader
	 * @throws IOException If an error occurs while reading the configuration
	 */
	public MixConfiguration(Reader r) throws XMLParseException, IOException
	{
		if (!this.setMixConfiguration(r))
		{
			throw new IOException("Error while loading configuration!");
		}
		this.setSavedToFile();
	}

	// -------------------- PUBLIC METHODS --------------------
	
	/** Set's a new <CODE>MixConfiguration</CODE>. Without creating a new Instance
	 * The configuration is read from the specified <CODE>java.io.Reader</CODE>
	 * @param r A <CODE>Reader</CODE> providing the configuration
	 * @return true if an XML configuration file has been loaded; false otherwise
	 * @throws XMLParseException If an error occurs while parsing the input from the reader
	 * @throws IOException If an error occurs while reading the configuration
	 */
	public boolean setMixConfiguration(Reader r) throws XMLParseException, IOException
	{
		// Create the document from input source
		Document configuration = open(new InputSource(r));
		if (configuration == null)
		{
			return false;
		}
		else
		{
			// Set the configuration
			this.m_configuration = configuration;
			// Call the compatibility checker
			this.initCheck();
			this.setSavedToFile();
			return true;	
		}
	}
	
	public static String getMixTypeAsString(int a_mixType)
	{
		String strMixType = "";

		if ((a_mixType & MIXTYPE_FIRST) > 0)
		{
			strMixType += MIXTYPE_NAME[0];
		}
		if ((a_mixType & MIXTYPE_MIDDLE) > 0)
		{
			strMixType += MIXTYPE_NAME[1];
		}
		if ((a_mixType & MIXTYPE_LAST) > 0)
		{
			strMixType += MIXTYPE_NAME[2];
		}
		return strMixType;
	}

	/** Returns the DOM Document object underlying this configuration.
	 * @return The configuration as a DOM structure
	 */
	public Document getDocument()
	{
		return this.m_configuration;
	}

	/** Adds a <CODE>ChangeListener</CODE> to this object's listeners list
	 * @param a_changeListener A new <CODE>ChangeListener</CODE> to receive events every time an attribute
	 * changes
	 */
	public void addChangeListener(ChangeListener a_changeListener)
	{
		// make sure this listener is contained only once in the config's listeners list
		removeChangeListener(a_changeListener);
		m_changeListeners.addElement(a_changeListener);
	}

	/** Removes a <CODE>ChangeListener</CODE> to this object's listeners list
	 * @param a_changeListener A <CODE>ChangeListener</CODE> in this objects listeners list
	 */
	public void removeChangeListener(ChangeListener a_changeListener)
	{
		m_changeListeners.removeElement(a_changeListener);
	}

	/** 
	 * Write the configuration to the specified <CODE>java.io.Writer</CODE>. 
	 * Normally this is a FileWriter to which an XML file is to be written.
	 * @param a_writer A <CODE>java.io.Writer</CODE> to which to write the XML
	 * @throws IOException If an error occurs while writing
	 */
	public void save(Writer a_writer) throws IOException
	{
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Writing configuration ..");
		XMLUtil.write(m_configuration, a_writer);
		if (a_writer instanceof FileWriter) m_bSavedToFile = true;
	}

	/**
	 * Return if the configuration has been saved to a file.
	 * @return true if the configuration has been saved to a file; false otherwise
	 */
	public boolean isSavedToFile()
	{
		return m_bSavedToFile;
	}

	/**
	 * Tells the configuration object that it has been save to a file.
	 */
	public void setSavedToFile()
	{
		m_bSavedToFile = true;
	}

	/** Shows a file dialog and saves the configuration
	 * @throws IOException If an I/O error occurs while saving the configuration
	 * @return the file if the saving succeeded, null if it was aborted by the user
	 */
	public File saveToFile() throws IOException
	{
		String fileName = MixConfig.getCurrentFileName();
		File file;

		if (fileName != null)
		{
			file = new File(fileName);
		}
		else
		{
			JFileChooser fileChooser = MixConfig.showFileDialog(
						 MixConfig.getMainWindow(), MixConfig.SAVE_DIALOG, MixConfig.FILTER_XML);
			if (fileChooser == null)
			{
				file = null;
			}
			else
			{
				file = fileChooser.getSelectedFile();
			}
		}

		if (file != null)
		{
			String fname = file.getName();
			if (!fname.toLowerCase().endsWith(".xml"))
			{
				file = new File(file.getParent(), fname + ".xml");
			}
			save(new FileWriter(file.getCanonicalPath()));
		}

		return file;
	}

	/**
	 * Returns the values for all elements or attributes in the specified path.
	 * @param a_xmlPath a path to an element or an attribute inside an element
	 * @return the values for all elements or attributes in the specified path
	 */
	public String[] getValues(String a_xmlPath)
	{
		return getValues(a_xmlPath, null, null);
	}

	/**
	 * Returns the node values of elements with the specified attribute.
	 * @param a_xmlPath String
	 * @param a_attribute an attribute
	 * @param a_attributeValue the attribute's wanted value
	 * @return String
	 */
	public String[] getValues(String a_xmlPath, String a_attribute, String a_attributeValue)
	{
		Vector<String> values = new Vector<String>();
		String[] strValues;
		String temp;
		String strParentNode = a_xmlPath.substring(0, a_xmlPath.lastIndexOf("/"));
		String strNodeName = a_xmlPath.substring(a_xmlPath.lastIndexOf("/") + 1, a_xmlPath.length());

		Node n = getNode(strParentNode, false);

		if (n == null)
		{
			return new String[0];
		}

		NodeList nl = n.getChildNodes();

		for (int i = 0; i < nl.getLength(); i++)
		{
			if (nl.item(i).getNodeName().equals(strNodeName))
			{
				temp = XMLUtil.parseAttribute(nl.item(i), a_attribute, null);

				if (a_attribute == null || a_attribute.trim().length() == 0 ||
					a_attributeValue == temp ||
					(temp != null && temp.equals(a_attributeValue)))
				{
					temp = XMLUtil.parseValue(nl.item(i), null);
					if (temp != null && temp.trim().length() > 0)
					{
						values.addElement(temp);
					}
				}
			}
		}

		strValues = new String[values.size()];
		for (int i = 0; i < strValues.length; i++)
		{
			strValues[i] = (String) values.elementAt(i);
		}

		return strValues;
	}

	public boolean isRootPathVerificationEnabled()
	{
		try
		{
			return Boolean.valueOf(getValue(CertPanel.XMLPATH_CERTIFICATES_PATH_VERIFICATION)).booleanValue();
		}
		catch (Exception a_e)
		{
			return false;
		}
		
	}
	
	/**
	 * Returns if the MixOnCD configuration is used.
	 * @return true if the MixOnCD configuration is used; false otherwise
	 */
	public boolean isMixOnCDEnabled()
	{
		return (getValue(MixOnCDPanel.XMLPATH_MIXONCD_NETWORK + "/" +
						 MixOnCDPanel.XMLATTRIBUTE_DHCP) != null);
	}

	/**
	 * Return true, if the mix may be configured by an info service
	 * 
	 * @return true, if the mix may be configured by an info service; false otherwise
	 */
	public boolean isAutoConfigurationAllowed()
	{
		return Boolean.valueOf(getValue(GeneralPanel.XMLPATH_AUTOCONFIGURATION)).booleanValue();
	}

	public boolean isFallbackEnabled()
	{
		return Boolean.valueOf(getValue(GeneralPanel.XMLPATH_AUTOCONFIGURATION + "/" +
										GeneralPanel.XML_ATTRIBUTE_FALLBACK)).booleanValue();
	}

	/**
	 * Check if there is a tag <Accounting> in the XML
	 * TODO: Remove since it is not referenced?
	 * 
	 * @return boolean
	 */
	public boolean isAccountingNodePresent()
	{
		NodeList list = m_configuration.getElementsByTagName("Accounting");
		if (list.getLength() > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Add the node 'Accounting' and set default values
	 */
	public void addAccounting()
	{
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Adding 'Accounting' to the configuration");
		// Defaults for the PaymentPanel
		setValue(PaymentPanel.XMLPATH_SOFTLIMIT, "1200000");
		setValue(PaymentPanel.XMLPATH_HARDLIMIT, "500000");
		setValue(PaymentPanel.XMLPATH_PREPAIDINTERVAL, "3000000");
		setValue(PaymentPanel.XMLPATH_SETTLEINTERVAL, "20");		
		// Load default JPI certificate from file 'Payment_Instance.cer'		
		try
		{			
			byte[] cert = ResourceLoader.loadResource(PaymentPanel.FILESYSTEM_PATH_PI_CERT);
			setValue("Accounting/PaymentInstance/Certificate/X509Certificate", cert);
		}
		// One of either FileNotFoundException or IOException
		catch (Exception e)
		{
			LogHolder.log(LogLevel.ERR, LogType.PAY, "Error while loading default JPI-certificate: "+e.getMessage());
		}
		// JPI port and host
 		setValue(PaymentPanel.XMLPATH_PI_PORT, "3018");
		setValue(PaymentPanel.XMLPATH_PI_HOST, "pi.jondopay.de");
		// Database defaults
		setValue(PaymentPanel.XMLPATH_DATABASE_HOST, "localhost");
		setValue(PaymentPanel.XMLPATH_DATABASE_PORT, "5432");
		setValue(PaymentPanel.XMLPATH_DATABASE_NAME, "aidb");
		setValue(PaymentPanel.XMLPATH_DATABASE_USERNAME, "aiuser");
	}
	
	/**
	 * Return the MixType as an {@link Integer}
	 * 
	 * @return mixtype
	 */
	public int getMixType()
	{
		int mixtype;
		try
		{
			mixtype = Integer.valueOf(getValue(GeneralPanel.XMLPATH_GENERAL_MIXTYPE)).intValue();
			if (mixtype < MIXTYPE_FIRST)
			{
				mixtype = MIXTYPE_FIRST;
			}
			else if (mixtype > MIXTYPE_LAST)
			{
				mixtype = MIXTYPE_LAST;
			}
		}
		catch (NumberFormatException a_e)
		{
			mixtype = MIXTYPE_FIRST;
		}
		return mixtype;
	}

	/** Returns the value of the attribute with the specified name. The name must be of
	 * the form &quot;RootElement/ChildElement/ChildElement/... etc.&quot;, similar to
	 * the XPath syntax, but XPath functions and relative paths are not allowed.
	 * @return The attribute value as a <CODE>String</CODE>, or <CODE>null</CODE> if an element
	 * with this path does not exist.
	 * @param a_xmlPath The path to the DOM element
	 */
	public String getValue(String a_xmlPath)
	{
		Node n = getNode(a_xmlPath, false);

		if (n == null)
		{
			return null;
		}

		if (n instanceof Attr)
		{
			return ( (Attr) n).getValue();
		}

		NodeList nl = n.getChildNodes();

		if (nl.getLength() == 0)
		{
			return null;
		}

		StringBuffer value = new StringBuffer();
		for (int i = 0; i < nl.getLength(); i++)
		{
			n = nl.item(i);
			if (n instanceof Text)
			{
				value.append(' ');
				value.append(n.getNodeValue());
			}
		}

		String v = value.toString().trim();

		if (a_xmlPath.indexOf("MixType") >= 0)
		{
			v = Integer.toString(getMixTypeAsInt(v));
		}

		return v;
	}

	/** 
	 * Set a {@link String} node value
	 * 
	 * @param a_xmlPath The path to the DOM element
	 * @param a_value The new value for the attribute as a <CODE>String</CODE>
	 */
	public void setValue(String a_xmlPath, String a_value)
	{
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Setting value: " + a_xmlPath + ":" + a_value);

		//if (a_xmlPath != null) we should keep this for debugging
		{
			setValue(a_xmlPath, a_value, null, a_xmlPath.endsWith("MixID"));
		}
	}

	/** 
	 * Sets the value of the attribute with the specified name. The name must be of
	 * the form &quot;RootElement/ChildElement/ChildElement/... etc.&quot;, similar to
	 * the XPath syntax, but XPath functions and relative paths are not allowed.
	 * 
	 * @param a_xmlPath The path to the DOM element
	 * @param a_value The new node value for the element as a <CODE>String</CODE>
	 * @param a_attribute Optional DOM attribute to be added to the DOM element
	 * @param urlEncode Determines if the stored value is to be URL encoded (<CODE>true</CODE> or <CODE>false</CODE>)
	 */
	public void setValue(String a_xmlPath, String a_value, Attr a_attribute, boolean urlEncode)
	{
		// Get the desired node, create if it is non-existent
		Node n = getNode(a_xmlPath, true);
		// Clear this nodes children
		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++)
		{
			//LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Removing value from node '" + n.getNodeName() + "'");
			n.removeChild(nl.item(i));
			i--;
		}
		// Check urlEncode
		if (urlEncode)
		{
			try
			{
				a_value = URLEncoder.encode(a_value, "UTF-8");				
				LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Encoded in UTF-8: " + a_value);
			}
			catch (UnsupportedEncodingException uee)
			{
				LogHolder.log(LogLevel.EXCEPTION, LogType.MISC, "Error while encoding URL: " + uee.getMessage());
			}
		}
		// Set the actual value by creating a TextNode
		if (a_value != null && a_value.trim().length() > 0)
		{
			n.appendChild(this.m_configuration.createTextNode(a_value));
		}
		// Set an additional attribute
		if (n instanceof Element && a_attribute != null)
		{
			((Element)n).setAttributeNode(a_attribute);
		}
		this.fireStateChanged(a_xmlPath, a_value);
	}

	/** 
	 * Set a {@link Boolean} node value
	 * 
	 * @param a_xmlPath The path to the DOM element
	 * @param a_value The new (<CODE>boolean</CODE>) value for the node (to be converted to a <CODE>String</CODE>).
	 */
	public void setValue(String a_xmlPath, boolean a_value)
	{
		if (a_value)
		{
			setValue(a_xmlPath, "True");
		}
		else
		{
			setValue(a_xmlPath, "False");
		}
	}

	/** 
	 * Set an {@link Integer} node value
	 * 
	 * @param a_xmlPath The path to the DOM element
	 * @param a_value The new (<CODE>int</CODE>) value for the node (to be converted to a <CODE>String</CODE>).
	 */
	public void setValue(String a_xmlPath, int a_value)
	{
		// Check if we are setting the MixType
		if (a_xmlPath.indexOf(GeneralPanel.XMLPATH_GENERAL_MIXTYPE) >= 0)
		{
			setValue(a_xmlPath, getMixTypeAsString(a_value));
		}
		else
		{
			setValue(a_xmlPath, Integer.toString(a_value));
		}
	}

	/** Sets the value of the attribute with the specified name. The name must be of
	 * the form &quot;RootElement/ChildElement/ChildElement/... etc.&quot;, similar to
	 * the XPath syntax, but XPath functions and relative paths are not allowed.
	 * @param a_xmlPath The path to the DOM element
	 * @param a_value The new value for the attribute. The <CODE>byte[]</CODE> will be Base64-encoded before being saved.
	 */
	public void setValue(String a_xmlPath, byte[] a_value)
	{
		Attr attr = this.m_configuration.createAttribute("xml:space");
		attr.setNodeValue("preserve");
		String s = null;
		if (a_value != null)
		{
			s = Base64.encode(a_value, true);
		}
		setValue(a_xmlPath, s, attr, false);
	}

	/** Converts the specified table model to a DOM tree and integrates it below the
	 * existing element with the specified name.
	 * @param a_mixListModel An instance of <CODE>mixconfig.CascadePanel.MixListTableModel</CODE>
	 * @param a_xmlPath The path to the DOM parent element of the connection element to be set
	 */
	public void setValue(String a_xmlPath, CascadePanel.MixListTableModel a_mixListModel)
	{
		Node f = a_mixListModel.toXmlElement(m_configuration);

		Node n = getNode(a_xmlPath, true);

		while (n.hasChildNodes())
		{
			n.removeChild(n.getFirstChild());
		}

		n.insertBefore(f, null);

		fireStateChanged(a_xmlPath + "/" + f.getNodeName(), a_mixListModel);
	}

	/**
	 * In the case that there is more than one value for a specified path, this
	 * method creates as many nodes with the same name as values.
	 * @param a_xmlPath String
	 * @param a_values String[]
	 */
	public void setValues(String a_xmlPath, String[] a_values)
	{
		setValues(a_xmlPath, a_values, null, null);
	}

	/**
	 * In the case that there is more than one value for a specified path, this
	 * method creates as many nodes with the same name as values. For each value it is possible
	 * to add one optional attribute.
	 * 
	 * @param a_xmlPath String
	 * @param a_values String[]
	 * @param a_attribute String
	 * @param a_attrValues String[]
	 */
	public void setValues(String a_xmlPath, String[] a_values,
						  String a_attribute, String[] a_attrValues)
	{
		String strParentNode = a_xmlPath.substring(0, a_xmlPath.lastIndexOf("/"));
		String strNodeName = a_xmlPath.substring(a_xmlPath.lastIndexOf("/") + 1, a_xmlPath.length());

		Node n = getNode(strParentNode, true);
		Element valueNode;

		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++)
		{
			Node o = nl.item(i);
			if (o.getNodeName().equals(strNodeName))
			{
				n.removeChild(o);
				i--;
			}
		}

		for (int i = 0; i < a_values.length; i++)
		{
			if (a_values[i] == null || a_values[i].trim().length() == 0)
			{
				continue;
			}

			valueNode = m_configuration.createElement(strNodeName);
			XMLUtil.setValue(valueNode, a_values[i]);
			if (a_attribute != null && a_attrValues != null && a_attrValues.length > i)
			{
				XMLUtil.setAttribute(valueNode, a_attribute, a_attrValues[i]);
			}
			n.appendChild(valueNode);
		}
		this.fireStateChanged(a_xmlPath, a_values);
	}

	/**
	 * TODO: Refactor this one with the next one?
	 * @param a_xmlPath
	 * @param a_infoServiceModel
	 */
	public void setValue(String a_xmlPath, InfoServiceTableModel a_infoServiceModel)
	{
		Element elem = a_infoServiceModel.createAsElement(m_configuration);
		Node n = getNode(a_xmlPath, true);
		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++)
		{
			Node o = nl.item(i);
			if (o.getNodeName().equals(elem.getNodeName()))
			{
				n.removeChild(o);
				i--;
			}
		}
		Database.getInstance(InfoServiceDBEntry.class).removeAll();
		synchronized(a_infoServiceModel)
		{
			for (int i = 0; i < a_infoServiceModel.getRowCount(); i++)
			{
				Database.getInstance(InfoServiceDBEntry.class).update(
						new InfoServiceDBEntry(a_infoServiceModel.getData(i).getListenerInterfaces()));
				
			}
			
			// Insert 'InfoServices' as the first child of network
			n.insertBefore(elem, n.getFirstChild());
		}
		fireStateChanged(a_xmlPath + "/" + elem.getNodeName(), a_infoServiceModel);
	}
	
	/** 
	 * Converts the specified table model to a DOM tree and integrates it below the
	 * existing element with the specified name.
	 * 
	 * @param a_xmlPath The path to the DOM parent element of the connection element to be set
	 * @param a_inConnModel An incoming connection table model
	 */
	public void setValue(String a_xmlPath, IncomingConnectionTableModel a_inConnModel)
	{
		Element elem = a_inConnModel.createAsElement(m_configuration);
		Node n = getNode(a_xmlPath, true);
		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++)
		{
			Node o = nl.item(i);
			if (o.getNodeName().equals(elem.getNodeName()))
			{
				n.removeChild(o);
				i--;
			}
		}
		n.insertBefore(elem, null);
		fireStateChanged(a_xmlPath + "/" + elem.getNodeName(), a_inConnModel);
	}

	/** 
	 * Converts the specified table model to a DOM tree and integrates it below the
	 * existing element with the specified name.
	 * 
	 * @param a_xmlPath The path to the DOM parent element of the connection element to be set
	 * @param a_outConnModel An outgoing connection table model
	 */
	public void setValue(String a_xmlPath, OutgoingConnectionTableModel a_outConnModel)
	{
		// Create the element
		Element elementNextMix = a_outConnModel.createMixAsElement(m_configuration);
		// Remove old elements
		this.removeNode("Network/NextMix");
		this.removeNode("Network/Proxies");
		// Get the parent
		Node parent = getNode(a_xmlPath, true);
		NodeList nl = parent.getChildNodes();
		if (elementNextMix != null)
		{
			// XXX This might be unnecessary
			for (int i = 0; i < nl.getLength(); i++)
			{
				Node n = nl.item(i);
				if (n.getNodeName().equals(elementNextMix.getNodeName()))
				{
					parent.removeChild(n);
					i--;
				}
			}
			// Do the actual work
			parent.insertBefore(elementNextMix, null);
			fireStateChanged(a_xmlPath + "/" + elementNextMix.getNodeName(), a_outConnModel);
		}
		
		/*
		Element a;
		Element f = a_outConnModel.createMixAsElement(m_configuration);
		Element g = a_outConnModel.createProxiesAsElement(m_configuration);

		this.removeNode("Network/NextMix");
		this.removeNode("Network/Proxies");

		Node n = getNode(a_xmlPath, true);
		NodeList nl = n.getChildNodes();

		a = f;
		for (int z = 0; z < 2; z++)
		{
			if (a != null)
			{
				for (int i = 0; i < nl.getLength(); i++)
				{
					Node o = nl.item(i);
					if (o.getNodeName().equals(a.getNodeName()))
					{
						n.removeChild(o);
						i--;
					}
				}
				n.insertBefore(a, null);
			}

			a = g;
		}

		if (f != null)
		{
			fireStateChanged(a_xmlPath + "/" + f.getNodeName(), a_outConnModel);
		}
		if (g != null)
		{
			fireStateChanged(a_xmlPath + "/" + g.getNodeName(), a_outConnModel);
		}*/
	}
	
	public void setValue(String a_xmlPath, ProxyTableModel a_proxyTableModel)
	{
		// Create the element
		Element elementProxies = a_proxyTableModel.createProxiesAsElement(m_configuration);
		// Remove old elements
		this.removeNode("Network/NextMix");
		this.removeNode("Network/Proxies");
		// Get the parent
		Node parent = getNode(a_xmlPath, true);
		NodeList nl = parent.getChildNodes();
		if (elementProxies != null)
		{
			// XXX This might be unnecessary
			for (int i = 0; i < nl.getLength(); i++)
			{
				Node n = nl.item(i);
				if (n.getNodeName().equals(elementProxies.getNodeName()))
				{
					parent.removeChild(n);
					i--;
				}
			}
			// Do the actual work
			parent.insertBefore(elementProxies, null);
			fireStateChanged(a_xmlPath + "/" + elementProxies.getNodeName(), a_proxyTableModel);
		}
	}

	/**
	 * Save a price certificate to the configuration
	 * 
	 * @param a_xmlPath
	 * @param a_cert
	 */
	public void setValue(String a_xmlPath, XMLPriceCertificate a_cert)
	{
		// XXX: Is it necessary to remove the old node??
		this.removeNode(PriceCertPanel.XMLPATH_PRICECERT);
		// Create the new element to insert
		Element priceCertElement = a_cert.toXmlElement(m_configuration);
		// Find the node 'Accounting', create if it doesn't exist
		Node accountingNode = getNode(PaymentPanel.XMLPATH_ACCOUNTING, true);
		// Append the price certificate in 'Accounting'
		accountingNode.appendChild(priceCertElement);
		this.fireStateChanged(a_xmlPath, a_cert);
	}
	
	/**
	 * Save a XML Element to the configuration
	 * 
	 * @param a_xmlPath
	 * @param a_cert
	 */
	public void setValue(String a_xmlPath, Element a_elem)
	{
		// XXX: Is it necessary to remove the old node??
		this.removeNode(a_xmlPath + "/" + a_elem.getNodeName());
		// Find the node, create if it doesn't exist
		Node n = getNode(a_xmlPath + "/" + a_elem.getNodeName(), true);
		// Append the price certificate in 'Accounting'
		for (int i = 0; i < a_elem.getChildNodes().getLength(); i++)
		{
			n.appendChild(a_elem.getChildNodes().item(i));
		}
		this.fireStateChanged(a_xmlPath + "/" + a_elem.getNodeName(), a_elem);
	}

	/**
	 * Set a {@link Boolean} attribute that needs to be capitalized for whatever reason
	 * 
	 * @param a_xmlPath
	 * @param a_attribute
	 * @param a_Boolean
	 */
	public void setAttribute(String a_xmlPath, String a_attribute, boolean a_Boolean)
	{
		// This kludge is necessary as compatibility with older version of 
		// the XML file that require string representations of boolean
		// values to be capitalized
		String s = new Boolean(a_Boolean).toString();
		s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
		// Call the standard method
		this.setAttribute(a_xmlPath, a_attribute, s);
	}
	
	/**
	 * Set an attribute to an element specified by an XML path, create the element if not existent
	 * 
	 * @param a_xmlPath Path to a DOM node
	 * @param a_attribute Attribute name
	 * @param a_value Attribute value
	 */
	public void setAttribute(String a_xmlPath, String a_attribute, String a_value)
	{
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Setting attribute: " + a_attribute + "@" + a_xmlPath + ":" + a_value);
		// Create the attribute and set its value
		Attr attr = getDocument().createAttribute(a_attribute);
		attr.setNodeValue(a_value);
		// Create the element if necessary
		Node n = getNode(a_xmlPath, true);
		// This should always be the case:
		if (n instanceof Element)
		{
			// Set the attribute to the element
			((Element)n).setAttributeNode(attr);
			this.fireStateChanged(a_xmlPath, a_value);
		}
	}
		
	/**
	 * Check the existence of a specific attribute at a certain xmlPath
	 * 
	 * @param a_xmlPath
	 * @param a_attribute
	 * @return
	 */
	public boolean hasAttribute(String a_xmlPath, String a_attribute)
	{
		boolean ret = false;
		// Get the targeted node
		Node targetNode = getNode(a_xmlPath, false);
		if (targetNode != null)
		{
			NamedNodeMap attributes = targetNode.getAttributes();
			Node attributeNode = attributes.getNamedItem(a_attribute);
			if (attributeNode != null)
			{
			    ret = true;	
			}
		}
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Checking for attribute "+a_attribute+"@"+a_xmlPath+": "+ret);
		return ret;
	}
	
	/**
	 * Return the value of a certain attribute or null if the attribute does not exist
	 * 
	 * @param a_xmlPath
	 * @param a_attribute
	 * @return
	 */
	public String getAttributeValue(String a_xmlPath, String a_attribute)
	{
		String ret = null;
		// Get the targeted node
		Node targetNode = getNode(a_xmlPath, false);
		if (targetNode != null)
		{
			NamedNodeMap attributes = targetNode.getAttributes();
			Node attributeNode = attributes.getNamedItem(a_attribute);
			if (attributeNode != null)
			{
			    ret = attributeNode.getNodeValue();	
			}
		}
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Getting value of "+a_attribute+"@"+a_xmlPath+": "+ret);
		return ret;
	}
	
	public boolean hasAttributes(String a_xmlPath)
	{
		Node n = getNode(a_xmlPath, false);
		if (n != null)
		{
			if (n.getAttributes() != null && n.getAttributes().getLength() > 0)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Remove the node with the specified name from the configuration
	 * 
	 * @param a_xmlPath The path to the DOM element
	 */
	public void removeNode(String a_xmlPath)
	{
		Node n = getNode(a_xmlPath, false);
		if (n != null)
		{
			LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Removing node: "+a_xmlPath);
			n.getParentNode().removeChild(n);
			this.fireStateChanged(a_xmlPath, null);
		}
	}
	
	/**
	 * Remove an attribute from a DOM element if existent
	 * 
	 * @param a_xmlPath
	 * @param a_attribute
	 */
	public void removeAttribute(String a_xmlPath, String a_attribute)
	{
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Removing attribute: " + a_attribute + "@" + a_xmlPath);
		// Get the node if existent
		Node n = getNode(a_xmlPath, false);
		// Set the attribute to the element
		if (n != null && n instanceof Element)
		{
			((Element)n).removeAttribute(a_attribute);
			this.fireStateChanged(a_xmlPath, null);
		}
	}
		
	// -------------------- PROTECTED METHODS --------------------

	/** 
	 * Sends a <CODE>ConfigurationEvent</CODE> with the specified attribute name and
	 * value to all <CODE>ChangeListener</CODE>s. This method is called whenever the 
	 * value of a node or attribute changes.
	 * 
	 * @param a_name The name of the changed attribute
	 * @param a_value The new value
	 */
	protected void fireStateChanged(String a_name, Object a_value)
	{
		m_bSavedToFile = false;
		if (a_name.indexOf("MixType") >= 0)
		{
			a_value = Integer.toString(getMixTypeAsInt(a_value.toString()));
		}
		ChangeEvent c = new ConfigurationEvent(this, a_name, a_value);
		for (int i = 0; i < m_changeListeners.size(); i++)
		{
			((ChangeListener)m_changeListeners.elementAt(i)).stateChanged(c);
		}
	}
			
	// -------------------- PRIVATE METHODS --------------------

	/**
	 * Set an initial default configuration 
	 * TODO: Rather split this to the single panels?
	 */
	private void setDefaults()
	{
		// General defaults
		setValue(GeneralPanel.XMLPATH_GENERAL_MIXTYPE, "FirstMix");
		setAttribute(GeneralPanel.XMLPATH_GENERAL_MIXNAME, MixInfo.XML_ATTRIBUTE_NAME_FOR_CASCADE, 
				MixInfo.NAME_TYPE_MIX);
		// Set the payment attribute
		setAttribute(GeneralPanel.XMLPATH_GENERAL_MIXTYPE, GeneralPanel.XML_ATTRIBUTE_PAYMENT, true);
		
		setValue(CertPanel.XMLPATH_CERTIFICATES_PATH_VERIFICATION, true);
		CertPanel.resetRootCertificates(this);
		
		
		// Create a dummy TableModel for adding default InfoServices
		InfoServiceTableModel dummyTableModel = new InfoServiceTableModel();
		// XXX: Test-InfoService
		//dummyTableModel.addData(new InfoServiceData("InfoService", "87.230.20.187", 80));
		dummyTableModel.addData(new InfoServiceData(XML_PATH_INFO_SERVICE, "infoservice.inf.tu-dresden.de", 80));
		dummyTableModel.addData(new InfoServiceData(XML_PATH_INFO_SERVICE, "72.55.137.241", 80));
		dummyTableModel.addData(new InfoServiceData(XML_PATH_INFO_SERVICE, "87.230.56.74", 80));
		dummyTableModel.addData(new InfoServiceData(XML_PATH_INFO_SERVICE, "78.129.146.44", 80));
		setValue("Network", dummyTableModel);
		
		// Set a default ListenerInterface
		setValue("Network/ListenerInterfaces/ListenerInterface/NetworkProtocol", "RAW/TCP");
		setValue("Network/ListenerInterfaces/ListenerInterface/Port", "6544");
		//setValue("Network/ListenerInterfaces/ListenerInterface/Host", "localhost");		
		
		// Set defaults for ServerMonitoring
		setValue("Network/ServerMonitoring/Port", "8080");
		setValue("Network/ServerMonitoring/Host", "0.0.0.0");
		
		// Add nodes 'Accounting' and 'TermsAndConditionsOperatorData'
		this.addAccounting();
		
		// TODO: Remove MixOnCD element?
		/* setValue(MixOnCDPanel.XMLPATH_MIXONCD_NETWORK + "/" + MixOnCDPanel.XMLVALUE_NETWORKINTERFACE, "eth0"); */		
	}
	
	/**
	 * Check for the existence of certain elements, attributes and set values accordingly.
	 * Call this method when loading a configuration e.g. from file.
	 */
	private void initCheck()
	{
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Performing compatibility check");
		// Set the 'payment'-attribute in case it is not already there
		if (!this.hasAttribute(GeneralPanel.XMLPATH_GENERAL_MIXTYPE, GeneralPanel.XML_ATTRIBUTE_PAYMENT))
		{
			if (this.isAccountingNodePresent())
			{
				this.setAttribute(GeneralPanel.XMLPATH_GENERAL_MIXTYPE, GeneralPanel.XML_ATTRIBUTE_PAYMENT, true);	
			}
			else
			{
				this.setAttribute(GeneralPanel.XMLPATH_GENERAL_MIXTYPE, GeneralPanel.XML_ATTRIBUTE_PAYMENT, false);
			}
		}
	}
	
	/**
	 * FIXME: Rather make this a private method?
	 * 
	 * Perform a check by writing out the document to a string and reloading it again. 
	 * To be called before the XML document is saved.
	 * @return true if the check succeeds, false otherwise
	 */
	public boolean performReloadCheck()
	{	
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Performing 'save-and-reload' check ..");
		boolean ret = false;
        // Write the document to a string
		StringWriter sw = new StringWriter();
		try 
		{
			LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Writing document to string");
			XMLUtil.write(m_configuration, sw);
		} 
		catch (IOException e) 
		{
		    JAPDialog.showErrorDialog(MixConfig.getMainWindow(), e.getMessage(), LogType.MISC);
		}
		String xmlString = sw.toString();
		// Reload the document from the string
		StringReader sr = new StringReader(xmlString);
		try 
		{
			LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Reading document from string");
			Document doc = XMLUtil.readXMLDocument(sr);
			if (doc != null) 
		    {
				ret = true;
				// Reset to null for the garbage collection
				doc = null;
		    }
		} 
		// XMLParseException or IOException
		catch (XMLParseException e) 
		{
			LogHolder.log(LogLevel.WARNING, LogType.MISC, e.getMessage());
		    JAPDialog.showErrorDialog(MixConfig.getMainWindow(), 
		    		"This configuration contains invalid characters! Once saved it cannot be loaded " +
		    		"again by the mix software or this tool. Please re-check this configuration!!", 
		    		LogType.MISC);
		}
		catch (IOException ioe)
		{
			LogHolder.log(LogLevel.WARNING, LogType.MISC, ioe.getMessage());
		    JAPDialog.showErrorDialog(MixConfig.getMainWindow(), ioe.getMessage(), LogType.MISC);
		}
		// Log something
		if (ret == true) LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Reload-check successful :-)");
		else LogHolder.log(LogLevel.ERR, LogType.MISC, "Reload-check NOT successful :-(");
		return ret;
	}
	
	/** Returns the MIXTYPE_xxx constant corresponding to the specified Mix type name.
	 * @param a_s A <CODE>String</CODE> representing a Mix type (first, middle or last)
	 * @return One of the MIXTYPE_xxx constants
	 */
	private int getMixTypeAsInt(String a_s)
	{
		int mixType = 0;
		if (a_s != null)
		{
			for (int i = 0; i < MIXTYPE_NAME.length; i++)
			{
				if (a_s.indexOf(MIXTYPE_NAME[i]) >= 0)
				{
					mixType += Math.pow(2,i);
				}
			}
		}
		return mixType;
	}

	/**
	 * Return a node's attribute given by name
	 * @param n
	 * @param a_namedItem
	 * @return The named attribute node
	 */
	private Node getAttributeNode(Node n, String a_namedItem)
	{
		if (n instanceof Element)
		{
			Node m = n.getAttributes().getNamedItem(a_namedItem);
			return m;
		}
		else return null;
	}

	/** Gets the DOM node with the specified name. The name must be of
	 * the form &quot;RootElement/ChildElement/ChildElement/... etc.&quot;, similar to
	 * the XPath syntax, but XPath functions and relative paths are not allowed.
	 * @param a_xmlPath The path to the DOM element
	 * @param create if <CODE>true</CODE>, the node is created if it does not exist
	 * @return The node with the specified path, or <CODE>null</CODE> there is no such node and
	 * <CODE>create</CODE> was set to <CODE>false</CODE>
	 */
	private Node getNode(String a_xmlPath, boolean create)
	{
		Node m, n;
		String headPart;
		String tailPart = a_xmlPath;
		Node p = m_configuration.getDocumentElement();
		NodeList nl = p.getChildNodes();

		// DEBUG
		//System.out.println("Searching for node " + a_xmlPath + " ...");

		do
		{
			n = null;
			headPart = tailPart;

			int i = tailPart.indexOf('/');
			if (i > 0)
			{
				headPart = tailPart.substring(0, i);
				tailPart = tailPart.substring(i + 1);
			}

			// DEBUG
			//System.out.println(" Element " + p.getNodeName() + " has " + nl.getLength() +
			//				   " children. Searching child " + headPart + " ... ");

			for (int j = 0; j < nl.getLength(); j++)
			{
				n = nl.item(j);

				// DEBUG
				//System.out.println(" Found element " + n.getNodeName() + " ... ");

				// find node with that name
				if (n.getNodeName().equals(headPart))
				{
					break;
				}

				// no such node? try one of its attributes
				n = getAttributeNode(n, headPart);
				if (n != null)
				{
					return n;
				}
			}

			// DEBUG

			//if (n == null)
			//{
			//System.out.println("No element named " + headPart + " found.");
			//}

			// if no such node exists, create one
			if (n == null)
			{
				if (!create)
				{
					return null;
				}

				n = m_configuration.createElement(headPart);
				p.appendChild(n);
			}
			else
			{
				m = getAttributeNode(n, tailPart);
				if (m != null)
				{
					return m;
				}
			}

			nl = n.getChildNodes();
			p = n;

		}
		while (headPart != tailPart); // indicates that traversal is complete
		return n;
	}

	/** 
	 * Open the specified input source (normally a <CODE>Reader</CODE> corresponding
	 * to an XML file) and read the DOM tree from it.
	 * @param r the input source
	 * @throws XMLParseException If an error occurs while parsing the input
	 * @throws IOException If an error occurs while reading the configuration
	 * @return A DOM document containing the configuration
	 */
	private static Document open(InputSource r) throws XMLParseException, IOException
	{
		Document doc;
		Node root;

		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Reading configuration ..");
		doc = XMLUtil.readXMLDocument(r);
		root = XMLUtil.assertNodeName(doc, "MixConfiguration");

		String ver = XMLUtil.parseAttribute(root, XML_ATTRIBUTE_VERSION, null);
		if (ver == null || ver.length() == 0)
		{
			if (!JAPDialog.showYesNoDialog(MixConfig.getMainWindow(),
				"This file does not contain any version information,\n" +
				"thus information might be lost.\nDo you want to continue?",
				"XML file version unknown"))
			{
				return null;
			}
		}

		try
		{
			if (Float.valueOf(ver).floatValue() > Float.valueOf(VERSION).floatValue())
			{
				if (!JAPDialog.showYesNoDialog(MixConfig.getMainWindow(),
					"The version of this file is newer than this utility,\n" +
					"thus information may not be read properly.\n" +
					"Do you want to continue?",
					"XML file version mismatch"))
				{
					return null;
				}

			}
		}
		catch (NumberFormatException a_e)
		{
			if (!JAPDialog.showYesNoDialog(MixConfig.getMainWindow(),
				"This file contains an invalid version information,\n" +
				"thus information may not be read properly.\nDo you want to continue?",
				"Invalid XML file version"))
			{
				return null;
			}
		}
		XMLUtil.setAttribute((Element)root, XML_ATTRIBUTE_VERSION, VERSION);
		return doc;
	}	
}