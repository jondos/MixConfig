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

/* Hint: This file may be only a copy of the original file which is always in the JAP source tree!
 * If you change something - do not forget to add the changes also to the JAP source tree!
 */
package anon.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.Reader;
import java.lang.reflect.Method;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * This class provides an easy interface to XML methods.
 */
public class XMLUtil
{
	private final static String DEFAULT_FORMAT_SPACE = "    ";
	private final static String XML_STR_BOOLEAN_TRUE = "true";
	private final static String XML_STR_BOOLEAN_FALSE = "false";
	private static DocumentBuilder ms_DocumentBuilder;

	/**
	 * Throws an XMLParseException if the given XML node is null.
	 * @param a_node an XML node
	 * @throws XMLParseException if the given XML node is null
	 */
	public static void assertNotNull(Node a_node)
		throws XMLParseException
	{
		if (a_node == null)
		{
			throw new XMLParseException(XMLParseException.NODE_NULL_TAG);
		}
	}


	/**
	 * Throws an XMLParseException if the given XML node has not the expected name or if it is null.
	 * If the given node is an XML document, the document element is returned. Otherwise, the given
	 * node is returned.
	 * @param a_node an XML node
	 * @param a_strExpectedName the node`s expected name
	 * @return If the given node is an XML document, the document element is returned. Otherwise,
	 * the given node is returned.
	 * @throws XMLParseException if the given node has not the expected name or if it is null
	 */
	public static Node assertNodeName(Node a_node, String a_strExpectedName)
		throws XMLParseException
	{
		if (a_node == null)
		{
			throw new XMLParseException(XMLParseException.NODE_NULL_TAG,
										"Expected node '" + a_strExpectedName
										+ "' is NULL!");
		}

		a_node = getDocumentElement(a_node);

		if (!a_node.getNodeName().equals(a_strExpectedName))
		{
			String nodeName;

			if (a_node.getOwnerDocument().getDocumentElement() == a_node ||
				a_node.getOwnerDocument() == a_node)
			{
				nodeName = XMLParseException.ROOT_TAG;
			}
			else
			{
				nodeName = a_node.getNodeName();
			}

			throw new XMLParseException(nodeName,
										"Node '" + a_node.getNodeName()
										+ "' has not the expected name: '"
										+ a_strExpectedName + "'");
		}

		return a_node;
	}


	/**
	 * If the current node is of the type XML document, this method returns the document
	 * element. Otherwise, the node is returned unchanged.
	 * @param a_node an XML node
	 * @return if the current node is of the type XML document, this method returns the document
	 * element; otherwise, the node is returned unchanged
	 */
	public static Node getDocumentElement(Node a_node)
	{
		if (a_node instanceof Document)
		{
			a_node = ( (Document) a_node).getDocumentElement();
		}

		return a_node;
	}

	/**
	 *
	 * @param n Node
	 * @param defaultValue int
	 * @return int
	 * @deprecated use parseValue(); scheduled for removal on 04/12/12
	 */
	public static int parseNodeInt(Node n, int defaultValue)
	{
		return parseValue(n, defaultValue);
	}

	/**
	 * Returns the value of the specified XML node as int.
	 * @param a_node an XML node
	 * @param a_defaultValue the default value
	 * @return the value of the specified node as boolean if the element`s value is of
	 *         type int; otherwise, the default value is returned
	 */
	public static int parseValue(Node a_node, int a_defaultValue)
	{
		int i = a_defaultValue;
		String s = parseValue(a_node, null);
		if (s != null)
		{
			try
			{
				i = Integer.parseInt(s);
			}
			catch (Exception e)
			{
			}
		}
		return i;
	}

	/**
	 * Returns the value of the specified XML node as long.
	 * @param a_node an XML node
	 * @param a_defaultValue the default value
	 * @return the value of the specified node as boolean if the element`s value is of
	 *         type long; otherwise, the default value is returned
	 */
	public static long parseValue(Node a_node, long a_defaultValue)
	{
		long i = a_defaultValue;
		String s = parseValue(a_node, null);
		if (s != null)
		{
			try
			{
				i = Long.parseLong(s);
			}
			catch (Exception e)
			{
			}
		}
		return i;
	}

	/**
	 *
	 * @param n Node
	 * @param defaultValue long
	 * @return long
	 * @deprecated use parseValue(); scheduled for removal on 04/12/12
	 */
	public static long parseNodeLong(Node n, long defaultValue)
	{
		return parseValue(n, defaultValue);
	}

	/**
	 * Returns the value of the specified attribute of an XML element as String.
	 * @param a_node an XML node
	 * @param a_attribute an attribute`s name
	 * @param a_default the default value
	 * @return the value of the specified attribute as String if the element has this attribute;
	 *         otherwise, the default value is returned
	 */
	public static String parseAttribute(Node a_node, String a_attribute, String a_default)
	{
		try
		{
			if (a_node instanceof Document)
			{
				a_node = ((Document)a_node).getDocumentElement();
			}

			Attr at = ((Element)a_node).getAttributeNode(a_attribute);
			return at.getValue().trim();
		}
		catch (Exception a_e)
		{
			return a_default;
		}
	}

	/**
	 * Returns the value of the specified attribute of an XML element as boolean.
	 * @param a_node an XML node
	 * @param a_attribute an attribute`s name
	 * @param a_default the default value
	 * @return the value of the specified attribute as boolean if the element has this attribute;
	 *         otherwise, the default value is returned
	 */
	public static boolean parseAttribute(Node a_node, String a_attribute, boolean a_default)
	{
		boolean b = a_default;

		try
		{
			String tmpStr = parseAttribute(a_node, a_attribute, null);
			if (tmpStr.equalsIgnoreCase("true"))
			{
				b = true;
			}
			else if (tmpStr.equalsIgnoreCase("false"))
			{
				b = false;
			}
		}
		catch (Exception ex)
		{
		}

		return b;
	}

	/**
	 * Returns the value of the specified attribute of an XML element as int.
	 * @param a_node an XML node
	 * @param a_attribute an attribute`s name
	 * @param a_default the default value
	 * @return the value of the specified attribute as int if the element has this attribute;
	 *         otherwise, the default value is returned
	 */
	public static int parseAttribute(Node a_node, String a_attribute, int a_default)
	{
		int i = a_default;

		try
		{
			i = Integer.parseInt(parseAttribute(a_node, a_attribute, null));
		}
		catch (Exception ex)
		{
		}

		return i;
	}

	/**
	 *
	 * @param e Element
	 * @param attr String
	 * @param defaultValue boolean
	 * @return boolean
	 * @deprecated use parseAttribute(); scheduled for removal on 04/12/3
	 */
	public static boolean parseElementAttrBoolean(Element e, String attr, boolean defaultValue)
	{
		return parseAttribute(e, attr, defaultValue);
	}

	/**
	 *
	 * @param e Element
	 * @param attr String
	 * @param defaultValue int
	 * @return int
	 * @deprecated use parseAttribute(); scheduled for removal on 04/12/3
	 */
	public static int parseElementAttrInt(Element e, String attr, int defaultValue)
	{
		return parseAttribute(e, attr, defaultValue);
	}

	/**
	 * Returns the value of the specified XML node as boolean.
	 * @param a_node an XML node
	 * @param a_defaultValue the default value
	 * @return the value of the specified node as boolean if the element`s value is of
	 *         type boolean; otherwise, the default value is returned
	 */
	public static boolean parseValue(Node a_node, boolean a_defaultValue)
	{
		boolean b = a_defaultValue;
		try
		{
			String tmpStr = parseValue(a_node, null);
			if (tmpStr == null)
			{
				return b;
			}
			if (tmpStr.equalsIgnoreCase(XML_STR_BOOLEAN_TRUE))
			{
				b = true;
			}
			else if (tmpStr.equalsIgnoreCase(XML_STR_BOOLEAN_FALSE))
			{
				b = false;
			}
		}
		catch (Exception e)
		{
		}
		return b;

	}

	/**
	 * Gets the content of an Element or Text Node. The "content" of an Element Node is
	 * the text between the opening and closing Element Tag. The content of an attribute node
	 * is the value of the attribute.
	 * @param a_node text node, element node or attribute node
	 * @param a_defaultValue value returned, if an error occured
	 * @return the "content" of the node or the default value, if the node has no value or an error
	 *         occured
	 */
	public static String parseValue(Node a_node, String a_defaultValue)
	{
		String s = a_defaultValue;
		if (a_node != null)
		{
			try
			{
				if (a_node.getNodeType() == Node.ELEMENT_NODE)
				{
					a_node = a_node.getFirstChild();
				}
				if (a_node.getNodeType() == Node.TEXT_NODE)
				{
					s = "";
					while (a_node != null &&
						   (a_node.getNodeType() == Node.ENTITY_REFERENCE_NODE ||
							a_node.getNodeType() == Node.TEXT_NODE))
					{ ///@todo parsing of Documents which contains quoted chars are wrong under JAXP 1.0
						if (a_node.getNodeType() == Node.ENTITY_REFERENCE_NODE)
						{
							s += a_node.getFirstChild().getNodeValue();
						}
						else
						{
							s += a_node.getNodeValue();
						}
						a_node = a_node.getNextSibling();
					}
				}
				else
				{
					s = a_node.getNodeValue();
				}
			}
			catch (Exception e)
			{
				return a_defaultValue;
			}
		}
		return s;
	}

	/**
	 *
	 * @param n Node
	 * @param defaultValue boolean
	 * @return boolean
	 * @deprecated use parseValue(); scheduled for removal on 04/12/12
	 */
	public static boolean parseNodeBoolean(Node n, boolean defaultValue)
	{
		return parseValue(n, defaultValue);
	}

	/**
	 * Gets the content of an Element or Text Node. The "content" of an Element Node is
	 * the text between the opening and closing Element Tag. The content of an attribute node
	 * is the value of the attribute. For all other nodes null is returned.
	 * @param n text node, element node or attribute node
	 * @param defaultValue value returned, if an error occured
	 * @return null if this node has no "content"
	 * @return defaultValue if an error occured
	 * @return "content" of the node
	 * @deprecated use parseValue(); scheduled for removal on 04/12/12
	 */
	public static String parseNodeString(Node n, String defaultValue)
	{
		return parseValue(n, defaultValue);
	}

	/**
	 * Returns the child node of the given node with the given name.
	 * @param a_node the node from that the search starts
	 * @param a_childname the childnode we are looking for
	 * @return the child node of the given node with the given name or null if it was not found
	 */
	public static Node getFirstChildByName(Node a_node, String a_childname)
	{
		try
		{
			Node child = a_node.getFirstChild();
			while (child != null)
			{
				if (child.getNodeName().equals(a_childname))
				{
					return child;
				}
				child = child.getNextSibling();
			}
		}
		catch (Exception e)
		{
		}
		return null;
	}

	/**
	 * Returns the child node of the given node with the given name. If the node is not found
	 * in the direct children of the parent node, then all child nodes will be searched and then
	 * their child nodes and so on until either the requested child has been found or all nodes
	 * in the XML structure have been traversed.
	 * @param a_node the node from that the search starts
	 * @param a_childname the childnode we are looking for
	 * @return the child node of the given node with the given name or null if it was not found
	 */
	public static Node getFirstChildByNameUsingDeepSearch(Node a_node, String a_childname)
	{
		Node result = null;

		try
		{
			a_node = a_node.getFirstChild();

			while (a_node != null)
			{
				result = getFirstChildByNameUsingDeepSearchInternal(a_node, a_childname);
				if (result != null)
				{
					break;
				}
				a_node = a_node.getNextSibling();
			}
		}
		catch (Exception a_e)
		{
		}
		return result;
	}

	public static Node getLastChildByName(Node n, String name)
	{
		try
		{
			Node child = n.getLastChild();
			while (child != null)
			{
				if (child.getNodeName().equals(name))
				{
					return child;
				}
				child = child.getPreviousSibling();
			}
		}
		catch (Exception e)
		{
		}
		return null;
	}

	/**
	 * Inserts a String value into an XML node.
	 * @param a_node an XML node
	 * @param a_value a String
	 * @deprecated use setValue(Node, String); this method has been declared deprecated as its name
	 * contained an argument type; scheduled for removal on 04/12/3
	 */
	public static void setNodeValue(Node a_node, String a_value)
	{
		setValue(a_node, a_value);
	}

	/**
	 * Inserts a String value into an XML node.
	 * @param a_node an XML node
	 * @param a_value a String
	 */
	public static void setValue(Node a_node, String a_value)
	{
		a_node.appendChild(a_node.getOwnerDocument().createTextNode(a_value));
	}

	/**
	 * Inserts an int value into an XML node.
	 * @param a_node an XML node
	 * @param a_value an int value
	 */
	public static void setValue(Node a_node, int a_value)
	{
		a_node.appendChild(a_node.getOwnerDocument().createTextNode(a_value + ""));
	}

	/**
	 * Inserts a long value into an XML node.
	 * @param a_node an XML node
	 * @param a_value a long value
	 */
	public static void setValue(Node a_node, long a_value)
	{
		a_node.appendChild(a_node.getOwnerDocument().createTextNode(a_value + ""));
	}

	/**
	 * Inserts a boolean value into an XML node.
	 * @param a_node an XML node
	 * @param a_bValue a boolean value
	 */
	public static void setValue(Node a_node, boolean a_bValue)
	{
		setValue(a_node, a_bValue ? XML_STR_BOOLEAN_TRUE : XML_STR_BOOLEAN_FALSE);
	}

	/**
	 * Inserts a boolean value into an XML node.
	 * @param node an XML node
	 * @param b a boolean value
	 * @deprecated use setValue(Node, boolean); this method has been declared deprecated as its name
	 * contained an argument type; scheduled for removal on 04/12/3
	 */
	public static void setNodeBoolean(Node node, boolean b)
	{
		setValue(node, b);
	}

	/**
	 * Creates and sets an attribute with a String value to an XML element.
	 * @param a_element an XML Element
	 * @param a_attribute an attribute name
	 * @param a_value a String value for the attribute
	 */
	public static void setAttribute(Element a_element, String a_attribute, String a_value)
	{
		a_element.setAttribute(a_attribute, a_value);
	}

	/**
	 * Creates and sets an attribute with a boolean value to an XML element.
	 * @param a_element an XML Element
	 * @param a_attribute an attribute name
	 * @param a_value a boolean value for the attribute
	 */
	public static void setAttribute(Element a_element, String a_attribute, boolean a_value)
	{
		setAttribute(a_element, a_attribute, a_value ? XML_STR_BOOLEAN_TRUE : XML_STR_BOOLEAN_FALSE);
	}

	/**
	 * Creates and sets an attribute with an int value to an XML element.
	 * @param a_element an XML Element
	 * @param a_attribute an attribute name
	 * @param a_value an int value for the attribute
	 */
	public static void setAttribute(Element a_element, String a_attribute, int a_value)
	{
		setAttribute(a_element, a_attribute, a_value + "");
	}

	/**
	 * Creates a new Document.
	 * @return a new Document
	 */
	public static Document createDocument()
	{
		try
		{
			if (ms_DocumentBuilder == null)
			{
				ms_DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			}
			return ms_DocumentBuilder.newDocument();
		}
		catch (ParserConfigurationException a_e)
		{
			return null;
		}
	}

	/**
	 * Returns a copy of the source node with the given document as owner document
	 * This method is needed as nodes cannot be appended to foreign documents by default,
	 * but only to the document by which they have been created.
	 * @param a_doc the new owner document of the copied source node
	 * @param a_source the source XML node
	 * @param a_bDeep true if the source node should be copied with all children, the chlidren`s
	 * children and so on; false, if only the direct children of the source node should be copied
	 * @author Apache Xerces-J
	 * @throws Exception if an error occurs
	 * @return a copy of the source node with the given document as owner document
	 */
	public static Node importNode(Document a_doc, Node a_source, boolean a_bDeep) throws Exception
	{

		Node newnode = null;

		// Sigh. This doesn't work; too many nodes have private data that
		// would have to be manually tweaked. May be able to add local
		// shortcuts to each nodetype. Consider ?????
		// if(source instanceof NodeImpl &&
		//	!(source instanceof DocumentImpl))
		// {
		//  // Can't clone DocumentImpl since it invokes us...
		//	newnode=(NodeImpl)source.cloneNode(false);
		//	newnode.ownerDocument=this;
		//}
		//else
		int type = a_source.getNodeType();
		switch (type)
		{

			case Node.ELEMENT_NODE:
			{
				Element newelement = a_doc.createElement(a_source.getNodeName());
				NamedNodeMap srcattr = a_source.getAttributes();
				if (srcattr != null)
				{
					for (int i = 0; i < srcattr.getLength(); i++)
					{
						newelement.setAttributeNode(
							(Attr) importNode(a_doc, srcattr.item(i), true));
					}
				}
				newnode = newelement;
				break;
			}

			case Node.ATTRIBUTE_NODE:
			{
				newnode = a_doc.createAttribute(a_source.getNodeName());
				newnode.setNodeValue(a_source.getNodeValue());
				// Kids carry value
				break;
			}

			case Node.TEXT_NODE:
			{
				Node tmpParent = a_source.getParentNode();
				if (tmpParent != null && tmpParent.getNodeType() != Node.ATTRIBUTE_NODE)
			{
				newnode = a_doc.createTextNode(a_source.getNodeValue());
				}
				break;
			}

			case Node.CDATA_SECTION_NODE:
			{
				newnode = a_doc.createCDATASection(a_source.getNodeValue());
				break;
			}

			case Node.ENTITY_REFERENCE_NODE:
			{
				newnode = a_doc.createEntityReference(a_source.getNodeName());
				a_bDeep = false; // ????? Right Thing?
				// Value implied by doctype, so we should not copy it
				// -- instead, refer to local doctype, if any.
				break;
			}

			case Node.ENTITY_NODE:
			{
				/*Entity srcentity = (Entity) source;
				  Entity newentity = doc.createEntity(source.getNodeName());
				  newentity.setPublicId(srcentity.getPublicId());
				  newentity.setSystemId(srcentity.getSystemId());
				  newentity.setNotationName(srcentity.getNotationName());
				  // Kids carry additional value
				  newnode = newentity;*/
				throw new Exception("HIERARCHY_REQUEST_ERR");
				//break;
			}

			case Node.PROCESSING_INSTRUCTION_NODE:
			{
				newnode = a_doc.createProcessingInstruction(a_source.getNodeName(),
					a_source.getNodeValue());
				break;
			}

			case Node.COMMENT_NODE:
			{
				newnode = a_doc.createComment(a_source.getNodeValue());
				break;
			}

			case Node.DOCUMENT_TYPE_NODE:
			{
				/*	DocumentType doctype = (DocumentType) source;
				 DocumentType newdoctype =
				  doc.createDocumentType(
				  doctype.getNodeName(),
				  doctype.getPublicID(), doctype.getSystemID());
				 // Values are on NamedNodeMaps
				 NamedNodeMap smap = ( (DocumentType) source).getEntities();
				 NamedNodeMap tmap = newdoctype.getEntities();
				 if (smap != null)
				 {
				  for (int i = 0; i < smap.getLength(); i++)
				  {
				   tmap.setNamedItem( (EntityImpl) importNode(smap.item(i), true));
				  }
				 }
				 smap = ( (DocumentType) source).getNotations();
				 tmap = newdoctype.getNotations();
				 if (smap != null)
				 {
				  for (int i = 0; i < smap.getLength(); i++)
				  {
				   tmap.setNamedItem( (NotationImpl) importNode(smap.item(i), true));
				  }
				 }
				 // NOTE: At this time, the DOM definition of DocumentType
				 // doesn't cover Elements and their Attributes. domimpl's
				 // extentions in that area will not be preserved, even if
				 // copying from domimpl to domimpl. We could special-case
				 // that here. Arguably we should. Consider. ?????
				 newnode = newdoctype;
				 break;*/
				throw new Exception("HIERARCHY_REQUEST_ERR");

			}

			case Node.DOCUMENT_FRAGMENT_NODE:
			{
				newnode = a_doc.createDocumentFragment();
				// No name, kids carry value
				break;
			}

			case Node.NOTATION_NODE:
			{
				/*
				   Notation srcnotation = (Notation) source;
				   Notation newnotation = (Notation) doc.createNotation(source.getNodeName());
				   newnotation.setPublicId(srcnotation.getPublicId());
				   newnotation.setSystemId(srcnotation.getSystemId());
				   // Kids carry additional value
				   newnode = newnotation;
				   // No name, no value
				   break;*/
				throw new Exception("HIERARCHY_REQUEST_ERR");

			}

			case Node.DOCUMENT_NODE: // Document can't be child of Document
			default:
			{ // Unknown node type
				throw new Exception("HIERARCHY_REQUEST_ERR");
			}
		}

		// If deep, replicate and attach the kids.
		if (a_bDeep)
		{
			for (Node srckid = a_source.getFirstChild();
				 srckid != null;
				 srckid = srckid.getNextSibling())
			{
				if (newnode != null)
				{
					Node n = importNode(a_doc, srckid, true);

					if (n != null)
					{
						newnode.appendChild(n);
					}
				}
			}
		}

		return newnode;

	}


	/**
	 * Creates a byte array from the abstract tree of the node.
	 * @param a_inputNode The node (incl. the whole tree) which is flattened to a byte array.
	 * @return the node as a byte array (incl. the whole tree).
	 */
	public static byte[] toByteArray(Node a_inputNode)
	{
		return toString(a_inputNode).getBytes();
	}

	/**
	 * Writes an XML-Node to a String. If the node is a Document then the <XML> header is included.
	 * Since writing was not standardized until JAXP 1.1 different Methods are tried
	 * @param node an XML Node
	 * @return an XML Node in a String representation
	 */
	public static String toString(Node node)
	{
		ByteArrayOutputStream out = null;
		try
		{
			out = new ByteArrayOutputStream();
		}
		catch (Throwable t3)
		{
			return null;
		}
		try //For JAXP 1.0.1 Reference Implementation (shipped with JAP)
		{
			Class c = Class.forName("com.sun.xml.tree.ParentNode");
			if (c.isInstance(node))
			{
				Document doc = null;
				if (node instanceof Document)
				{
					doc = (Document) node;
				}
				else
				{
					doc = node.getOwnerDocument();
				}
				Writer w = new OutputStreamWriter(out, "UTF8");
				//What we do here is acutally:
				//com.sun.xml.tree.XmlWriteContext context=
				//						((com.sun.xml.tree.XmlDocument)doc).createWriteContext(w,2);
				//((com.sun.xml.tree.ElementNode)node).writeXml(context);
				//We do that this way to avoid the need of JAXP1.0 for compilation!
				Class classXmlDocument = Class.forName("com.sun.xml.tree.XmlDocument");
				Class[] paramClasses = new Class[2];
				paramClasses[0] = Writer.class;
				paramClasses[1] = int.class;
				Method methodCreateWriteContext = classXmlDocument.getMethod("createWriteContext",
					paramClasses);
				Object params[] = new Object[2];
				params[0] = w;
				params[1] = new Integer(2);
				Object context = methodCreateWriteContext.invoke(doc, params);
				paramClasses = new Class[1];
				paramClasses[0] = Class.forName("com.sun.xml.tree.XmlWriteContext");
				Method methodWriteXml = node.getClass().getMethod("writeXml", paramClasses);
				params = new Object[1];
				params[0] = context;
				methodWriteXml.invoke(node, params);
				w.flush();
				return out.toString("UTF8");
			}
		}
		catch (Throwable t1)
		{
		}
		try
		{ //For JAXP 1.1 (for Instance Apache Crimson/Xalan shipped with Java 1.4)
			//This seams to be realy stupid and compliecated...
			//But if the do a simple t.transform(), a NoClassDefError is thrown, if
			//the new JAXP1.1 is not present, even if we NOT call saveXMLDocument, but
			//calling any other method within JAPUtil.
			//Dont no why --> maybe this has something to to with Just in Time compiling ?
			Object t =
				javax.xml.transform.TransformerFactory.newInstance().newTransformer();
			javax.xml.transform.Result r = new javax.xml.transform.stream.StreamResult(out);
			javax.xml.transform.Source s = new javax.xml.transform.dom.DOMSource(node);

			//this is to simply invoke t.transform(s,r)
			Class c = t.getClass();
			Method m = null;
			Method[] ms = c.getMethods();
			for (int i = 0; i < ms.length; i++)
			{
				if (ms[i].getName().equals("transform"))
				{
					m = ms[i];
					Class[] params = m.getParameterTypes();
					if (params.length == 2)
					{
						break;
					}
				}
			}
			Object[] p = new Object[2];
			p[0] = s;
			p[1] = r;
			m.invoke(t, p);
			return out.toString();
		}
		catch (Throwable t2)
		{
			return null;
		}
	}

	//Quotes a string according to XML (&,<,>)
	public static String quoteXML(String text)
	{
		String s = text;
		if (s.indexOf('&') >= 0 || s.indexOf('<') >= 0 || s.indexOf('>') >= 0)
		{
			StringBuffer sb = new StringBuffer(text);
			int i = 0;
			while (i < sb.length())
			{
				char c = sb.charAt(i);
				if (c == '&')
				{
					sb.insert(i, "amp;");
					i += 4;
				}
				else if (c == '<')
				{
					sb.setCharAt(i, '&');
					sb.insert(i + 1, "lt;");
					i += 3;
				}
				else if (c == '>')
				{
					sb.setCharAt(i, '&');
					sb.insert(i + 1, "gt;");
					i += 3;
				}
				i++;
			}
			return sb.toString();

		}
		return s;
	}

	/**
	 * Removes all comments and empty lines from a node. Does nothing if the node
	 * is a comment node.
	 * @param a_node a node
	 */
	public static void removeComments(Node a_node)
	{
		if (a_node.getNodeType() != Document.COMMENT_NODE)
		{
			removeCommentsInternal(a_node, a_node);
		}
	}

	/**
	 * Reformats an XML document into a human readable format.
	 * @param a_doc an xml document
	 */
	public static void formatHumanReadable(Document a_doc)
	{
		formatHumanReadable(a_doc.getDocumentElement(), 0);
	}

	/**
	 * Reformats an XML element into a human readable format.
	 * @param a_element an xml element
	 */
	public static void formatHumanReadable(Element a_element)
	{

		formatHumanReadable(a_element, 0);
	}

	/**
	 * Reads an XML document from an input source.
	 * @param a_inputSource an input source
	 * @return the XML document that was read from the input source
	 * @throws IOException if an I/O error occurs
	 * @throws XMLParseException if the input stream could not be parsed correctly
	 */
	public static Document readXMLDocument(InputSource a_inputSource)
		throws IOException, XMLParseException
	{
		Document doc = null;

		try
		{
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(a_inputSource);
			removeComments(doc);
		}
		catch (IOException a_e)
			{
			throw a_e;
			}
		catch (Exception a_e)
			{
				throw new XMLParseException(
						 XMLParseException.ROOT_TAG, "Could not parse XML document: " +
					a_e.getMessage());
			}


		return doc;
	}

	/**
	 * Reads an XML document from an input stream.
	 * @param a_inputStream an input stream
	 * @return the XML document that was read from the input stream
	 * @throws IOException if an I/O error occurs
	 * @throws XMLParseException if the input stream could not be parsed correctly
	 */
	public static Document readXMLDocument(InputStream a_inputStream)
		throws IOException, XMLParseException
	{
		return readXMLDocument(new InputSource(a_inputStream));
	}


	/**
	 * Reads an XML document from a Reader.
	 * @param a_reader a Reader.
	 * @return the XML document that was read from the Reader
	 * @throws IOException if an I/O error occurs
	 * @throws XMLParseException if the input stream could not be parsed correctly
	 */
	public static Document readXMLDocument(Reader a_reader)
		throws IOException, XMLParseException
	{
		return readXMLDocument(new InputSource(a_reader));
	}

	/**
	 * Reads an XML document from a file.
	 * @param a_file a file
	 * @return the XML document that was read from the file
	 * @throws IOException if an I/O error occurs
	 * @throws XMLParseException if the file could not be parsed correctly
	 */
	public static Document readXMLDocument(File a_file) throws IOException, XMLParseException
	{
		return readXMLDocument(new FileInputStream(a_file));
	}

	/**
	 * Writes an XML document to an output stream.
	 * @param a_doc an XML document
	 * @param a_outputStream an output stream
	 * @throws IOException if an I/O error occurs
	 */
	public static void write(Document a_doc, OutputStream a_outputStream) throws IOException
	{
		XMLUtil.formatHumanReadable(a_doc);
		a_outputStream.write(XMLUtil.toString(a_doc).getBytes());
		a_outputStream.flush();
	}

	/**
	 * Writes an XML document to a Writer.
	 * @param a_doc an XML document
	 * @param a_writer a Writer
	 * @throws IOException if an I/O error occurs
	 */
	public static void write(Document a_doc, Writer a_writer) throws IOException
	{
		XMLUtil.formatHumanReadable(a_doc);
		a_writer.write(toString(a_doc));
		a_writer.flush();
	}

	/**
	 * Writes an XML document to a file.
	 * @param a_doc an XML document
	 * @param a_file a file
	 * @throws IOException if an I/O error occurs
	 */
	public static void write(Document a_doc, File a_file) throws IOException
	{
		FileOutputStream out = new FileOutputStream(a_file);
		write(a_doc, out);
		out.close();
	}

	/**
	 * Transforms a String into an XML document. The String must be
	 * a valid XML document in String representation.
	 * @param a_xmlDocument a valid XML document in String representation
	 * @return an XML document
	 * @exception XMLParseException if the given String is no valid XML document
	 */
	public static Document toXMLDocument(String a_xmlDocument) throws XMLParseException
	{
		return toXMLDocument(a_xmlDocument.getBytes());
	}

	/**
	 * Transforms a byte array into an XML document. The byte array must be
	 * a valid XML document in byte representation.
	 * @param a_xmlDocument a valid XML document in byte representation
	 * @return an XML document
	 * @exception XMLParseException if the given byte array is no valid XML document
	 */
	public static Document toXMLDocument(byte[] a_xmlDocument) throws XMLParseException
	{
		ByteArrayInputStream in = new ByteArrayInputStream(a_xmlDocument);
		Document doc;
		try
		{
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
		}
		catch (Exception a_e)
		{
			throw new XMLParseException(XMLParseException.ROOT_TAG,
										"Could not transform bytes into an XML document.");
		}

		return doc;
	}

	/**
	 * Transforms an IXMLEncodable object into an XML document.
	 * @param a_xmlEncodable an IXMLEncodable
	 * @return an XML document
	 */
	public static Document toXMLDocument(IXMLEncodable a_xmlEncodable)
	{
		Document doc;
		Element element;

		element = toXMLElement(a_xmlEncodable);
		doc = element.getOwnerDocument();
		doc.appendChild(element);

		return doc;
	}

	/**
	 * Transforms an IXMLEncodable object into an XML element.
	 * @param a_xmlEncodable an IXMLEncodable
	 * @return an XML element
	 */
	public static Element toXMLElement(IXMLEncodable a_xmlEncodable)
	{
		Document doc = null;
		Element element;

		try
		{
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch (ParserConfigurationException a_e)
		{
			return null;
		}

		element = a_xmlEncodable.toXmlElement(doc);

		return element;
	}

	/**
	 * Returns a node that is equal to the given name, starting from the given node
	 * and, if it is not the node we are looking for, recursing to all its children.
	 * @param node the node from that the search starts
	 * @param name the node we are looking for
	 * @return Node the node with the given name or null if it was not found
	 */
	private static Node getFirstChildByNameUsingDeepSearchInternal(Node node, String name)
	{
		try
		{
			if (node.getNodeName().equals(name)) // found!
			{
				return node;
			}
			if (node.hasChildNodes()) // not found, but the Node has children ...
			{
				NodeList childNodes = node.getChildNodes();
				for (int i = 0; i < childNodes.getLength(); i++)
				{
					Node tmp_result = getFirstChildByNameUsingDeepSearchInternal(childNodes.item(i), name);
					if (tmp_result != null)
					{
						return tmp_result;
					}
				}
			}
			else // Node has no children and it is not the Node we are looking for
			{
				return null;
			}
		}
		catch (Exception e)
		{
		}
		return null;
	}

	/**
	 * Reformats an element into a human readable format. This is a recursive function.
	 * @param a_element an xml element
	 * @param a_level the level of this element
	 * @return the number of nodes added (0 or 1)
	 */
	private static int formatHumanReadable(Node a_element, int a_level)
	{
		Text newLine;
		Node node;
		int added = 0;
		String space;

		// call the function recursive for all child nodes
		if (a_element.hasChildNodes())
		{
			NodeList childNodes = a_element.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++)
			{
				i += formatHumanReadable(childNodes.item(i), a_level + 1);
			}
		}

		// if this is empty text, remove it!
		if (a_element.getNodeType() == Document.TEXT_NODE &&
			a_element.getNodeValue().trim().length() == 0)
		{
			if (a_element.getNextSibling() == null)
			{
				// this is a last node; append a new line and space
				space = new String();
				for (int i = 0; i < a_level - 1; i++)
				{
					space += DEFAULT_FORMAT_SPACE;
				}
				newLine = a_element.getOwnerDocument().createTextNode(space);
				a_element.getParentNode().appendChild(newLine);

				added = 0;
			}
			else
			{
				added = -1;
			}

			a_element.getParentNode().removeChild(a_element);
			return added;

		}

		// do this, if this is not the root element and no text node
		if ( (a_element.getOwnerDocument().getDocumentElement() != a_element) &&
			(a_element.getNodeType() != Document.TEXT_NODE))
		{
			// insert a new line before this element, if this is the first element
			if (a_element == a_element.getParentNode().getFirstChild())
			{
				newLine = a_element.getOwnerDocument().createTextNode("\n");
				a_element.getParentNode().insertBefore(newLine, a_element);
				added++; // count one more
			}

			// insert space before the element according to the layer
			space = new String();
			for (int i = 0; i < a_level; i++)
			{
				space += DEFAULT_FORMAT_SPACE;
			}
			newLine = a_element.getOwnerDocument().createTextNode(space);
			a_element.getParentNode().insertBefore(newLine, a_element);
			added++; // count one more

			// insert a new line after the current element
			node = a_element.getNextSibling();
			if (node != null)
			{
				// add a new line before the next node
				newLine = a_element.getOwnerDocument().createTextNode("\n");
				a_element.getParentNode().insertBefore(newLine, node);
			}
			else
			{
				// this is the last node; append a new line and space
				space = space.substring(0, space.length() - DEFAULT_FORMAT_SPACE.length());
				newLine = a_element.getOwnerDocument().createTextNode("\n" + space);
				a_element.getParentNode().appendChild(newLine);
			}
			added++; // count one more
		}

		return added;
	}

	/**
	 * Removes all comments, empty lines and new lines from a node.
	 * This is a recursive function.
	 * @param a_node a node
	 * @param a_parentNode the node`s parent node
	 * @return the number of children removed (0 or 1)
	 */
	private static int removeCommentsInternal(Node a_node, Node a_parentNode)
	{
		if (a_node.getNodeType() == Document.COMMENT_NODE)
		{
			a_parentNode.removeChild(a_node);
			return 1;
		}

		if (a_node.getNodeType() == Document.TEXT_NODE)
		{
			if (a_node.getNodeValue().trim().length() == 0)
			{
				a_parentNode.removeChild(a_node);
				return 1;
			}
		}

		if (a_node.hasChildNodes())
		{
			NodeList childNodes = a_node.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++)
			{
				i -= removeCommentsInternal(childNodes.item(i), a_node);
			}
		}
		return 0;
	}
}
