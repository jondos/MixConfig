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
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import anon.util.Base64;
import mixconfig.networkpanel.IncomingConnectionTableModel;
import mixconfig.networkpanel.OutgoingConnectionTableModel;
import java.net.URLDecoder;
import org.w3c.dom.NamedNodeMap;

/** This class provides unified access to the Mix configuration. The configuration
 * is stored as a DOM document.
 * @author ronin &lt;ronin2@web.de&gt;
 */
public class MixConfiguration
{
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
	private static final String MIXTYPE_NAME[] =
		{
		"FirstMix", "MiddleMix", "LastMix"};

	/** The configuration as a DOM document */
	private Document m_configuration = null;

	/** A list of <CODE>ChangeListener</CODE>s receiving events from this object
	 * whenever the value of an attribute changes
	 */
	private Vector m_changeListeners = new Vector();

	/** Constructs a new instance of <CODE>MixConfiguration</CODE>. The configuration
	 * contains only the root element and empty elements on the second level. The leaf
	 * elements are created as soon as the corresponding attributes are set.
	 * @throws ParserConfigurationException If an error occurs while parsing the <CODE>String</CODE> that contains the new
	 * configuration
	 * @throws IOException If an error occurs while reading from the <CODE>String</CODE>
	 * @throws SAXException If parsing the configuration string causes an error
	 */
	public MixConfiguration() throws ParserConfigurationException, IOException, SAXException
	{
		String mixConfigXML =
			"<?xml version=\"1.0\"?>" +
			"<MixConfiguration version=\"0.4\">" +
			"   <General><MixType>FirstMix</MixType></General>" +
			"   <Network/>" +
			"   <Certificates/>" +
			"   <Description/>" +
			"   <Accounting/>" +
			"</MixConfiguration>";

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		m_configuration = docBuilder.parse(new InputSource(new StringReader(mixConfigXML)));
	}

	/** Constructs a new instance of <CODE>MixConfiguration</CODE>. The configuration is
	 * read from the specified <CODE>java.io.Reader</CODE>
	 * @param r A <CODE>Reader</CODE> providing the configuration
	 * @throws ParserConfigurationException If an error occurs while parsing the input from the reader
	 * @throws IOException If an error occurs while reading the configuration
	 * @throws SAXException If parsing the configuration causes an error
	 */
	public MixConfiguration(Reader r) throws ParserConfigurationException, IOException, SAXException
	{
		m_configuration = open(new InputSource(r));
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
		//Writing to File...
		OutputFormat format = new OutputFormat(m_configuration, "UTF-8", true);
		// format.setPreserveSpace(true);
		format.setLineWidth(0); //avoid line wrapping
		XMLSerializer serial = new XMLSerializer(a_writer, format);
		serial.serialize(m_configuration);
	}

	/** Returns the value of the attribute with the specified name. The name must be of
	 * the form &quot;RootElement/ChildElement/ChildElement/... etc.&quot;, similar to
	 * the XPath syntax, but XPath functions and relative paths are not allowed.
	 * @return The attribute value as a <CODE>String</CODE>, or <CODE>null</CODE> if an element
	 * with this path does not exist.
	 * @param a_xmlPath The path to the DOM element
	 */
	public String getAttribute(String a_xmlPath)
	{
		Node n = getAttributeNode(a_xmlPath, false);
		boolean urlDecode = false;

		if (n instanceof Element)
		{
			// no need to check for a null return value; Element.getAttribute(String)
			// always returns a String object (though it may be empty)
			urlDecode = a_xmlPath.endsWith("MixID");
				//! ( (Element) n).getAttribute("xml:space").equals("preserve");
		}
		else if (n instanceof Attr)
		{
			return ( (Attr) n).getValue();
		}

		if (n == null)
		{
			return null;
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

		if (urlDecode)
		{
			v = URLDecoder.decode(v);

			// convert Mix type name back to an integer
			// and return it as a string
		}
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
	 * @throws UnsupportedEncodingException The value will be stored in URL-encoded form, encoded as UTF-8. If this encoding
	 * is not supported, an exception is throws.
	 */
	public void setAttribute(String a_xmlPath, String value) throws UnsupportedEncodingException
	{
		setAttribute(a_xmlPath, value, null, a_xmlPath.endsWith("MixID"));
	}

	/** Sets the value of the attribute with the specified name. The name must be of
	 * the form &quot;RootElement/ChildElement/ChildElement/... etc.&quot;, similar to
	 * the XPath syntax, but XPath functions and relative paths are not allowed.
	 * @param a_xmlPath The path to the DOM element
	 * @param a_value The new value for the attribute as a <CODE>String</CODE>
	 * @param a_attribute A DOM attribute to be added to the DOM element where the attribute is stored
	 * @param urlEncode Determines if the stored value is to be URL encoded (<CODE>true</CODE>) or not (<CODE>false</CODE>)
	 * @throws UnsupportedEncodingException If the encoding used for URL encoding (UTF-8) is not supported.
	 */
	public void setAttribute(String a_xmlPath, String a_value, Attr a_attribute, boolean urlEncode) throws
		UnsupportedEncodingException
	{
		Node n = getAttributeNode(a_xmlPath, true);

		// clear the elements child nodes
		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++)
		{
			n.removeChild(nl.item(i));
		}

		if (urlEncode)
		{
			a_value = URLEncoder.encode(a_value);
			/* Does not work on JDK < 1.4
			 try
			 {
			 a_value = URLEncoder.encode(a_value, "UTF-8");
			 }
			 catch(UnsupportedEncodingException uee)
			 {
			 // if encoding UTF is not supported, leave the string the way it is
			 // (this should not happen actually)
			 }
			 */
		}
		if (a_value != null)
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
	 * @throws UnsupportedEncodingException The value will be stored in URL-encoded form, encoded as UTF-8. If this encoding
	 * is not supported, an exception is throws.
	 */
	public void setAttribute(String a_xmlPath, boolean a_value) throws UnsupportedEncodingException
	{
		if (a_value)
		{
			setAttribute(a_xmlPath, "True");
		}
		else
		{
			setAttribute(a_xmlPath, "False");
		}
	}

	/** Sets the value of the attribute with the specified name. The name must be of
	 * the form &quot;RootElement/ChildElement/ChildElement/... etc.&quot;, similar to
	 * the XPath syntax, but XPath functions and relative paths are not allowed.
	 * @param a_xmlPath The path to the DOM element
	 * @param a_value The new value for the attribute. The <CODE>int</CODE> will be converted to a
	 * <CODE>String</CODE> before being saved.
	 * @throws UnsupportedEncodingException The value will be stored in URL-encoded form, encoded as UTF-8. If this encoding
	 * is not supported, an exception is throws.
	 */
	public void setAttribute(String a_xmlPath, int a_value) throws UnsupportedEncodingException
	{
		if (a_xmlPath.indexOf("MixType") >= 0)
		{
			setAttribute(a_xmlPath, this.MIXTYPE_NAME[a_value]);
		}
		else
		{
			setAttribute(a_xmlPath, Integer.toString(a_value));
		}
	}

	/** Sets the value of the attribute with the specified name. The name must be of
	 * the form &quot;RootElement/ChildElement/ChildElement/... etc.&quot;, similar to
	 * the XPath syntax, but XPath functions and relative paths are not allowed.
	 * @param a_xmlPath The path to the DOM element
	 * @param a_value The new value for the attribute. The <CODE>byte[]</CODE> will be Base64-encoded before being saved.
	 * @throws UnsupportedEncodingException If an error occurs while encoding the value with Base64
	 */
	public void setAttribute(String a_xmlPath, byte[] a_value) throws UnsupportedEncodingException
	{
		Attr attr = this.m_configuration.createAttribute("xml:space");
		attr.setNodeValue("preserve");
		String s = null;
		if (a_value != null)
		{
			s = Base64.encode(a_value, true);
		}
		setAttribute(a_xmlPath, s, attr, false);
	}

	/** Converts the specified table model to a DOM tree and integrates it below the
	 * existing element with the specified name.
	 * @param a_xmlPath The path to the DOM parent element of the connection element to be set
	 * @param a_inConnModel An incoming connection table model
	 */
	public void setAttribute(String a_xmlPath, IncomingConnectionTableModel a_inConnModel)
	{
		Element f = a_inConnModel.createAsElement(m_configuration);

		Node n = getAttributeNode(a_xmlPath, true);
		NodeList nl = n.getChildNodes();

		for (int i = 0; i < nl.getLength(); i++)
		{
			Node o = nl.item(i);
			if (o.getNodeName().equals(f.getNodeName()))
			{
				n.removeChild(o);
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
	public void setAttribute(String a_xmlPath, OutgoingConnectionTableModel a_outConnModel)
	{
		Element a;
		Element f = a_outConnModel.createMixAsElement(m_configuration);
		Element g = a_outConnModel.createProxiesAsElement(m_configuration);

		this.removeAttribute("Network/NextMix");
		this.removeAttribute("Network/Proxies");

		Node n = getAttributeNode(a_xmlPath, true);
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

	/** Removes the attribute with the specified name from the configuration.
	 * @param a_xmlPath The path to the DOM element
	 */
	public void removeAttribute(String a_xmlPath)
	{
		Node n = getAttributeNode(a_xmlPath, false);
		if (n != null)
		{
			n.getParentNode().removeChild(n);
			this.fireStateChanged(a_xmlPath, null);
		}
	}

	/**
	 * Checks whether the entered Mix ID is valid. A Mix ID is valid if it contains
	 * no other characters than lower case letters (a-z), digits (0-9), dots (.),
	 * dashes (-) or underscores (_).
	 * @return <code>true</code> if the Mix ID is valid, <code>false</code>
	 * otherwise
	 */
	public boolean isMixIDValid()
	{
		String mixID = getAttribute("General/MixID");
		if (mixID == null || mixID.equals(""))
		{
			return false;
		}
		else
		{
			String idChars = "abcdefghijklmnopqrstuvwxyz0123456789.-_";
			mixID = mixID.toLowerCase();
			if (mixID.charAt(0) != 'm')
			{
				return false;
			}
			for (int i = 0; i < mixID.length(); i++)
			{
				if (idChars.indexOf(mixID.charAt(i)) < 0)
				{
					return false;
				}
			}
		}
		return true;
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

	/** Gets the DOM node with the specified name. The name must be of
	 * the form &quot;RootElement/ChildElement/ChildElement/... etc.&quot;, similar to
	 * the XPath syntax, but XPath functions and relative paths are not allowed.
	 * @param a_xmlPath The path to the DOM element
	 * @param create if <CODE>true</CODE>, the node is create if it does not exist
	 * @return The node with the specified path, or <CODE>null</CODE> there is no such node and
	 * <CODE>create</CODE> was set to <CODE>false</CODE>
	 */
	private Node getAttributeNode(String a_xmlPath, boolean create)
	{
		Node n;
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
				if (n instanceof Element)
				{
					Node m = n.getAttributes().getNamedItem(headPart);
					if (m != null)
					{
						return m;
					}
				}

				n = null;
			}

			// DEBUG
			/*
			 if (n == null)
			 {
			 System.out.println("No element named " + headPart + " found.");
			 }
			 */

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

			nl = n.getChildNodes();
			p = n;
		}
		while (headPart != tailPart); // indicates that traversal is complete

		return n;
	}

	/** Opens the specified input source (normally a <CODE>Reader</CODE> corresponding
	 * to an XML file) and reads the DOM tree from it.
	 * @param r the input source
	 * @throws ParserConfigurationException If an error occurs while parsing the input
	 * @throws SAXException If parsing the configuration causes an error
	 * @throws IOException If an error occurs while reading the configuration
	 * @return A DOM document containing the configuration
	 */
	private Document open(InputSource r) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		Document doc = docBuilder.parse(r);

		Element root = doc.getDocumentElement();
		if (!root.getNodeName().equals("MixConfiguration"))
		{
			throw new IllegalArgumentException("Cannot parse config file: Root element '" +
											   root.getNodeName() + "' unknown.");
		}

		String ver = root.getAttribute("version");
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

		int version[] = new int[]
			{
			0, 0, 0};
		try
		{
			int begin, end = -1;
			for (int i = 0; i < 3; i++)
			{
				begin = end + 1;
				end = ver.indexOf('.');
				String versionPart = ver.substring(0, end);
				version[i] = Integer.valueOf(versionPart).intValue();
			}
		}
		catch (StringIndexOutOfBoundsException sioobe)
		{
		}
		catch (NumberFormatException nfe)
		{
		}

		if (version[0] > 0 || version[1] > 4)
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
		return doc;
	}
}
