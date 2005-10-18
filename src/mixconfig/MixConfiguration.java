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
import java.io.Reader;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import anon.util.Base64;
import anon.util.XMLUtil;
import anon.util.XMLParseException;
import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import mixconfig.networkpanel.IncomingConnectionTableModel;
import mixconfig.networkpanel.OutgoingConnectionTableModel;

/** This class provides unified access to the Mix configuration. The configuration
 * is stored as a DOM document.
 * @author ronin &lt;ronin2@web.de&gt;
 */
public class MixConfiguration
{
	/** Indicates that the Mix is ready to be either first or middle mix */
	//public static final int MIXTYPE_FIRST_OR_MIDDLE = 3;

	/** Indicates that the Mix is the last in a cascade */
	public static final int MIXTYPE_LAST = 2;

	/** Indicates that the Mix is within a cascade */
	public static final int MIXTYPE_MIDDLE = 1;

	/** Indicates that the Mix is the first in a cascade */
	public static final int MIXTYPE_FIRST = 0;

	/** Indicates that no logging should take place */
	public static final int LOG_NONE = 0;

	/** Indicates that logging output should be printed to the console */
	public static final int LOG_CONSOLE = 1;

	/** Indicates that logging output should be sent to the system log service */
	public static final int LOG_SYSLOG = 2;

	/** Indicates that logging output should be saved to a directory */
	public static final int LOG_DIRECTORY = 3;

	/** An array containing the Mix types as String values. The indices correspond to
	 * the MIXTYPE_xxx constants.
	 */
	public static final String MIXTYPE_NAME[] =
		{
		"FirstMix", "MiddleMix", "LastMix", "FirstOrMiddle"};

	/** The configuration file information version number. */
	private static final String VERSION = "0.5";

	private static final String XML_ATTRIBUTE_VERSION = "version";

	/** The configuration as a DOM document */
	private Document m_configuration = null;

	/** A list of <CODE>ChangeListener</CODE>s receiving events from this object
	 * whenever the value of an attribute changes
	 */
	private Vector m_changeListeners = new Vector();

	/** Constructs a new instance of <CODE>MixConfiguration</CODE>. The configuration
	 * contains only the root element and empty elements on the second level. The leaf
	 * elements are created as soon as the corresponding attributes are set.
	 * @throws XMLParseException If parsing the configuration string causes an error
	 */
	public MixConfiguration() throws XMLParseException
	{
		String mixConfigXML =
			"<?xml version=\"1.0\"?>" +
			"<MixConfiguration version=\"0.5\">" +
			"   <General><MixType>FirstMix</MixType></General>" +
			"   <Network/>" +
			"   <Certificates/>" +
			"   <Description/>" +
			"</MixConfiguration>";

		m_configuration = XMLUtil.toXMLDocument(mixConfigXML);

		//setValue("Network/InfoService/Host", "infoservice.inf.tu-dresden.de");
		//setValue("Network/InfoService/Port", "80");
		setValue("Network/InfoService/Host", "80.237.206.62");
		setValue("Network/InfoService/Port", "6543");
		/*
		setValue(MixOnCDPanel.XMLPATH_MIXONCD_NETWORK + "/" + MixOnCDPanel.XMLVALUE_NETWORKINTERFACE,
				 "eth0");*/
	}

	/** Constructs a new instance of <CODE>MixConfiguration</CODE>. The configuration is
	 * read from the specified <CODE>java.io.Reader</CODE>
	 * @param r A <CODE>Reader</CODE> providing the configuration
	 * @throws XMLParseException If an error occurs while parsing the input from the reader
	 * @throws IOException If an error occurs while reading the configuration
	 */
	public MixConfiguration(Reader r) throws XMLParseException, IOException
	{
		if (!setMixConfiguration(r))
		{
			throw new IOException("Loading of configuration has been canceled!");
		}
	}

	/** Set's a new <CODE>MixConfiguration</CODE>. Without creating a new Instance
	 * The configuration is read from the specified <CODE>java.io.Reader</CODE>
	 * @param r A <CODE>Reader</CODE> providing the configuration
	 * @return true if an XML configuration file has been loaded; false otherwise
	 * @throws XMLParseException If an error occurs while parsing the input from the reader
	 * @throws IOException If an error occurs while reading the configuration
	 */
	public boolean setMixConfiguration(Reader r) throws XMLParseException, IOException
	{
		Document configuration = open(new InputSource(r));

		if (configuration == null)
		{
			return false;
		}

		m_configuration = configuration;
		return true;
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

	/** Write the configuration to the specified <CODE>java.io.Writer</CODE>. Normally,
	 * the Writer is a FileWriter to which an XML file is to be written.
	 * @param a_writer A <CODE>java.io.Writer</CODE> to which to write the XML
	 * @throws IOException If an error occurs while writing
	 */
	public void save(Writer a_writer) throws IOException
	{
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Writing configuration...");
		XMLUtil.write(m_configuration, a_writer);
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
		Vector values = new Vector();
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
			strValues[i] = (String)values.elementAt(i);
		}

		return strValues;
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
	 * Returns if the mix may be configured by the info service.
	 * @return true if the mix may be configured by the info service; false otherwise
	 */
	public boolean isAutoConfigurationAllowed()
	{
		return	Boolean.valueOf(getValue(GeneralPanel.XMLPATH_AUTOCONFIGURATION)).booleanValue();
	}

	public boolean isFallbackEnabled()
	{
		return Boolean.valueOf(getValue(GeneralPanel.XMLPATH_AUTOCONFIGURATION + "/" +
										GeneralPanel.XML_ATTRIBUTE_FALLBACK)).booleanValue();
	}

	public int getMixType()
	{
		int mixtype;

		try
		{
			mixtype = Integer.valueOf(getValue("General/MixType")).intValue();

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

	/** Sets the value of the attribute with the specified name. The name must be of
	 * the form &quot;RootElement/ChildElement/ChildElement/... etc.&quot;, similar to
	 * the XPath syntax, but XPath functions and relative paths are not allowed.
	 * @param a_xmlPath The path to the DOM element
	 * @param value The new value for the attribute as a <CODE>String</CODE>
	 */
	public void setValue(String a_xmlPath, String value)
	{
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, a_xmlPath + ":" + value);

		//if (a_xmlPath != null) we should keep this for debgging
		{
			setValue(a_xmlPath, value, null, a_xmlPath.endsWith("MixID"));
		}
	}

	/** Sets the value of the attribute with the specified name. The name must be of
	 * the form &quot;RootElement/ChildElement/ChildElement/... etc.&quot;, similar to
	 * the XPath syntax, but XPath functions and relative paths are not allowed.
	 * @param a_xmlPath The path to the DOM element
	 * @param a_value The new value for the attribute as a <CODE>String</CODE>
	 * @param a_attribute A DOM attribute to be added to the DOM element where the attribute is stored
	 * @param urlEncode Determines if the stored value is to be URL encoded (<CODE>true</CODE>) or not (<CODE>false</CODE>)
	 */
	public void setValue(String a_xmlPath, String a_value, Attr a_attribute, boolean urlEncode)
	{
		Node n = getNode(a_xmlPath, true);

		// clear the elements child nodes
		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++)
		{
			n.removeChild(nl.item(i));
			i--;
		}

		if (urlEncode)
		{
			a_value = URLEncoder.encode(a_value);
		}
		if (a_value != null && a_value.trim().length() > 0)
		{
			n.appendChild(this.m_configuration.createTextNode(a_value));
		}

		if (n instanceof Element && a_attribute != null)
		{
			( (Element) n).setAttributeNode(a_attribute);
		}

		fireStateChanged(a_xmlPath, a_value);
	}

	/** Sets the value of the attribute with the specified name. The name must be of
	 * the form &quot;RootElement/ChildElement/ChildElement/... etc.&quot;, similar to
	 * the XPath syntax, but XPath functions and relative paths are not allowed.
	 * @param a_xmlPath The path to the DOM element
	 * @param a_value The new value for the attribute. The <CODE>boolean</CODE> will be converted to a
	 * <CODE>String</CODE> before being saved.
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

	/** Sets the value of the attribute with the specified name. The name must be of
	 * the form &quot;RootElement/ChildElement/ChildElement/... etc.&quot;, similar to
	 * the XPath syntax, but XPath functions and relative paths are not allowed.
	 * @param a_xmlPath The path to the DOM element
	 * @param a_value The new value for the attribute. The <CODE>int</CODE> will be converted to a
	 * <CODE>String</CODE> before being saved.
	 */
	public void setValue(String a_xmlPath, int a_value)
	{
		if (a_xmlPath.indexOf("MixType") >= 0)
		{
			setValue(a_xmlPath, this.MIXTYPE_NAME[a_value]);
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

		fireStateChanged(a_xmlPath, a_values);
	}

	/** Converts the specified table model to a DOM tree and integrates it below the
	 * existing element with the specified name.
	 * @param a_xmlPath The path to the DOM parent element of the connection element to be set
	 * @param a_inConnModel An incoming connection table model
	 */
	public void setValue(String a_xmlPath, IncomingConnectionTableModel a_inConnModel)
	{
		Element f = a_inConnModel.createAsElement(m_configuration);

		Node n = getNode(a_xmlPath, true);
		NodeList nl = n.getChildNodes();

		for (int i = 0; i < nl.getLength(); i++)
		{
			Node o = nl.item(i);
			if (o.getNodeName().equals(f.getNodeName()))
			{
				n.removeChild(o);
				i--;
			}
		}
		n.insertBefore(f, null);

		fireStateChanged(a_xmlPath + "/" + f.getNodeName(), a_inConnModel);
	}

	/** Converts the specified table model to a DOM tree and integrates it below the
	 * existing element with the specified name.
	 * @param a_xmlPath The path to the DOM parent element of the connection element to be set
	 * @param a_outConnModel An outgoing connection table model
	 */
	public void setValue(String a_xmlPath, OutgoingConnectionTableModel a_outConnModel)
	{
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
		}
	}

	public void setAttribute(String a_xmlPath, String a_attribute, boolean a_Boolean)
	{
		// this kludge is necessary as compatibility with older version
		// of the XML file requires string representations of boolean
		// values to be capitalized
		String s = new Boolean(a_Boolean).toString();
		s = Character.toUpperCase(s.charAt(0)) + s.substring(1);

		Attr at = getDocument().createAttribute(a_attribute);
		at.setNodeValue(s);

		setValue(a_xmlPath, null, at, false);
	}

	/** Removes the attribute with the specified name from the configuration.
	 * @param a_xmlPath The path to the DOM element
	 */
	public void removeNode(String a_xmlPath)
	{
		Node n = getNode(a_xmlPath, false);
		if (n != null)
		{
			n.getParentNode().removeChild(n);
			this.fireStateChanged(a_xmlPath, null);
		}
	}

	// ------------- protected methods --------------

	/** Sends a <CODE>ConfigurationEvent</CODE> with the specified attribute name and
	 * value to all <CODE>ChangeListener</CODE>s. This method is called whenever the value of an attribute
	 * changes.
	 * @param a_name The name of the changed attribute
	 * @param a_value The new value
	 */
	protected void fireStateChanged(String a_name, Object a_value)
	{
		if (a_name.indexOf("MixType") >= 0)
		{
			a_value = Integer.toString(getMixTypeAsInt(a_value.toString()));
		}
		ChangeEvent c = new ConfigurationEvent(this, a_name, a_value);
		for (int i = 0; i < m_changeListeners.size(); i++)
		{
			( (ChangeListener) m_changeListeners.elementAt(i)).stateChanged(c);
		}
	}

	/** Sends a <CODE>ChangeEvent</CODE> to all <CODE>ChangeListener</CODE>s. This method is called whenever the value of an attribute
	 * changes.
	 */
	protected void fireStateChanged()
	{
		ChangeEvent c = new ChangeEvent(this);
		for (int i = 0; i < m_changeListeners.size(); i++)
		{
			( (ChangeListener) m_changeListeners.elementAt(i)).stateChanged(c);
		}
	}

	// ------------- private methods ----------------


	/** Returns the MIXTYPE_xxx constant corresponding to the specified Mix type name.
	 * @param a_s A <CODE>String</CODE> representing a Mix type (first, middle or last)
	 * @return One of the MIXTYPE_xxx constants
	 */
	private int getMixTypeAsInt(String a_s)
	{
		for (int i = 0; i < this.MIXTYPE_NAME.length; i++)
		{
			if (a_s.equals(MIXTYPE_NAME[i]))
			{
				return i;
			}
		}
		return 0;
	}

	private Node getAttribute(Node n, String a_namedItem)
	{
		Node m;

		if (n instanceof Element)
		{
			m = n.getAttributes().getNamedItem(a_namedItem);
			return m;
		}

		return null;
	}

	/** Gets the DOM node with the specified name. The name must be of
	 * the form &quot;RootElement/ChildElement/ChildElement/... etc.&quot;, similar to
	 * the XPath syntax, but XPath functions and relative paths are not allowed.
	 * @param a_xmlPath The path to the DOM element
	 * @param create if <CODE>true</CODE>, the node is create if it does not exist
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
				n = getAttribute(n, headPart);
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
				m = getAttribute(n, tailPart);
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

	/** Opens the specified input source (normally a <CODE>Reader</CODE> corresponding
	 * to an XML file) and reads the DOM tree from it.
	 * @param r the input source
	 * @throws XMLParseException If an error occurs while parsing the input
	 * @throws IOException If an error occurs while reading the configuration
	 * @return A DOM document containing the configuration
	 */
	private Document open(InputSource r) throws XMLParseException, IOException
	{
		Document doc;
		Node root;

		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Reading configuration...");
		doc = XMLUtil.readXMLDocument(r);
		root = XMLUtil.assertNodeName(doc, "MixConfiguration");


		String ver = XMLUtil.parseAttribute(root, XML_ATTRIBUTE_VERSION, null);
		if (ver == null || ver.length() == 0)
		{
			if (!MixConfig.ask(
				"XML file version unknown",
				"This file does not contain any version information,\n" +
				"thus information may be lost.\nDo you want to continue?"))
			{
				return null;
			}
		}


		try
		{
			if (Float.valueOf(ver).floatValue() > Float.valueOf(VERSION).floatValue())
			{
				if (!MixConfig.ask(
								"XML file version mismatch",
								"The version of this file is newer than this utility,\n" +
								"thus information may not be read properly.\n" +
								"Do you want to continue?"))
				{
					return null;
				}

			}
		}
		catch (NumberFormatException a_e)
		{
			if (!MixConfig.ask(
				"Invalid XML file version",
				"This file contains an invalid version information,\n" +
				"thus information may not be read properly.\nDo you want to continue?"))
			{
				return null;
			}
		}

		XMLUtil.setAttribute((Element)root, XML_ATTRIBUTE_VERSION, VERSION);

		return doc;
	}

	/**
	 * Checks if there is a <Accounting> tag in the XML configuration structure
	 * @return boolean
	 */
	public boolean isPaymentPresent()
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


}
