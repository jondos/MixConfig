package mixconfig.infoservice;

import java.util.Vector;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import anon.infoservice.ListenerInterface;
import anon.util.IXMLEncodable;
import anon.util.XMLParseException;

/**
 * Holds information about a single 'InfoService', i.e. a list of ListenerInterfaces
 * @author renner
 */
public final class InfoServiceData implements IXMLEncodable, Cloneable {
	
	// Array of interfaces
	private ListenerInterface[] m_interfaces;
	
	// The name of the XML Element (should be 'InfoService')
	private String m_strXMLNodeName;
	
	public InfoServiceData(String strXMLNodeName)
	{
		m_strXMLNodeName = strXMLNodeName;
		m_interfaces = new ListenerInterface[0];
	}
	
	public InfoServiceData(String strXMLNodeName, ListenerInterface[] list) 
	{
		m_strXMLNodeName = strXMLNodeName;
		m_interfaces = list;
	}
	
	/**
	 * Initialize an InfoService with a single ListenerInterface
	 * @param host
	 * @param port
	 */
	public InfoServiceData(String strXMLNodeName, String host, int port)
	{
		m_strXMLNodeName = strXMLNodeName;
		m_interfaces = new ListenerInterface[0];
		ListenerInterface iface = new ListenerInterface(host, port, ListenerInterface.PROTOCOL_TYPE_HTTP);
		addListenerInterface(iface);
	}
	
	/*
	 * Return the number of ListenerInterfaces of this InfoService
	 */
	public int getNumberOfListeners() 
	{
		return m_interfaces.length;
	}
	
	/**
	 * Add a new ListenerInterface to this InfoService
	 * @param iface
	 */
	public void addListenerInterface(ListenerInterface iface)
	{
		ListenerInterface[] nList = new ListenerInterface[m_interfaces.length + 1];
		for (int i = 0; i < m_interfaces.length; i++)
		{
			nList[i] = m_interfaces[i];
		}
		nList[m_interfaces.length] = iface;
		m_interfaces = nList;
	}
	
	public void removeListenerInterface(int index)
	{
		if (index >= 0 && index < m_interfaces.length)
		{
			ListenerInterface[] ifaces = new ListenerInterface[m_interfaces.length-1];
			for (int i = 0; i<index; i++)
			{
				ifaces[i] = m_interfaces[i];
			}
			for (int i = index+1; i<m_interfaces.length; i++)
			{
				ifaces[i-1] = m_interfaces[i];
			}
			m_interfaces = ifaces;
		}
	}
	
	public ListenerInterface getListenerInterface(int index)
	{		
		if (index<0 || index >= m_interfaces.length)
		{
			return null;
		}
		return m_interfaces[index];
	}
	
	public Vector getListenerInterfaces()
	{
		Vector listenerInterfaces = new Vector();
		ListenerInterface[] listeners = m_interfaces;
		for (int i = 0; i < listeners.length; i++)
		{
			listenerInterfaces.addElement(listeners[i]);
		}
		
		return listenerInterfaces;
	}
	
	public Element toXmlElement(Document doc) {
		Element infoServiceRoot = doc.createElement(m_strXMLNodeName);
		// Iterate over ListenerInterfaces and add them as children
		Element ifacesRoot = doc.createElement("ListenerInterfaces");
		infoServiceRoot.appendChild(ifacesRoot);
		for (int i=0; i<m_interfaces.length; i++)
		{
			LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Appending "+m_interfaces[i]);
			ifacesRoot.appendChild(m_interfaces[i].toXmlElement(doc));
		}
		return infoServiceRoot;
	}

	/**
	 * Return an InfoServiceData-object, casted from a DOM InfoService-Element
	 * 
	 * @param tag
	 * @param elemRoot
	 * @return 
	 * @throws XMLParseException
	 */
	public static InfoServiceData createFromElement(String tag, Element elemRoot) throws XMLParseException {		
		if (elemRoot.getTagName().equals(tag))
		{	
			Node child = elemRoot.getFirstChild();
			// Jump over any text-nodes to search for 'ListenerInterfaces'
			while (child!=null && child.getNodeType()!=Node.ELEMENT_NODE)
			{
				child = child.getNextSibling();
			}
			if (child.getNodeName().equals(ListenerInterface.XML_ELEMENT_CONTAINER_NAME))
			{
				child = child.getFirstChild();
				ListenerInterface[] ifaceList = new ListenerInterface[0];
				while (child != null)
				{
					if (child.getNodeType() == Node.ELEMENT_NODE)
					{
						ListenerInterface iface = new ListenerInterface((Element)child);
						LogHolder.log(LogLevel.DEBUG, LogType.NET, "Found InfoService --> "+iface.getHost()+":"+iface.getPort());		
						ListenerInterface[] nList = new ListenerInterface[ifaceList.length + 1];
						for (int i = 0; i < ifaceList.length; i++)
						{
							nList[i] = ifaceList[i];
						}
						nList[ifaceList.length] = iface;
						ifaceList = nList;
					}
					child = child.getNextSibling();
				}
				return new InfoServiceData(tag, ifaceList);
			} 
			else
			{
				LogHolder.log(LogLevel.WARNING, LogType.NET, "Could not find 'ListenerInterfaces'!");
				return new InfoServiceData(tag);
			}
		} else throw new XMLParseException(tag, "This is not the right element: "+elemRoot+" vs. "+tag);
	}
}