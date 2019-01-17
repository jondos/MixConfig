/*
 Copyright (c) 2000-2005, The JAP-Team
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
package mixconfig.network;

import mixconfig.panels.MixOnCDPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import anon.util.IXMLEncodable;
import anon.util.JAPMessages;
import anon.util.XMLUtil;

/**
 * Instances of this class represent previous or next mixes in a cascade or HTTP proxies
 * @author renner
 */
final public class ConnectionData implements IXMLEncodable, Cloneable
{
	public static final int TRANSPORT_BIT_MASK = 3; // Bit mask
	public static final int TCP = 0;
	public static final int UNIX = 1;
	public static final int UDP = 2;
	public static final int RAW = 0;
	public static final int SSL = 4;
	public static final int RAW_TCP = RAW | TCP;
	public static final int RAW_UNIX = RAW | UNIX;
	public static final int RAW_UDP = RAW | UDP;
	public static final int SSL_TCP = SSL | TCP;
	public static final int SSL_UNIX = SSL | UNIX;

	public static final int PROXY_MASK = 7;

	public static final int NO_PROXY = 0;
	public static final int HTTP_PROXY = 1;
	public static final int SOCKS_PROXY = 2;
	public static final int VPN_PROXY = 4;

	private String m_strXMLNodeName; // Name of the XML element
	private int m_iTransport;
	private String m_strHostname;
	private int m_iPort;
	// Only for proxies: hostname or IP (TODO: replace by a list)
	private String m_strVisibleAddress;
	private int m_iFlags;
	private boolean m_bIsVirtual = false;
	private boolean m_bIsHidden = false;
	
	/**
	 * Incredible enchainment of constructors ;-)
	 */
	public ConnectionData(String a_strXMLNodeName, int a_iTransport, String a_strHostname)
	{
		this(a_strXMLNodeName, a_iTransport, a_strHostname, 0);
	}
	
	public ConnectionData(String a_strXMLNodeName, int a_iTransport, String a_strHostname, int a_iFlags)
	{
		this(a_strXMLNodeName, a_iTransport, a_strHostname, 0, a_iFlags, false, false);
	}

	public ConnectionData(String a_strXMLNodeName, int a_iTransport, String a_strHostname, int a_iPort, 
		      int a_iFlags, boolean virtual, boolean hidden)
	{
		this(a_strXMLNodeName, a_iTransport, a_strHostname, a_iPort, null, a_iFlags, virtual, hidden);
	}
	
	public ConnectionData(String a_strXMLNodeName, int a_iTransport, String a_strHostname, int a_iPort, 
		      String a_strVisibleAddress, int a_iFlags, boolean virtual, boolean hidden)
	{
		setType(a_strXMLNodeName);
		setTransport(a_iTransport);
		setHostname(a_strHostname);
		setPort(a_iPort);
		setVisibleAddress(a_strVisibleAddress);
		setFlags(a_iFlags);
		setIsVirtual(virtual);
		setIsHidden(hidden);
	}
	
	public Object clone()
	{
		return new ConnectionData(m_strXMLNodeName, m_iTransport, m_strHostname,
								  m_iPort, m_iFlags, m_bIsVirtual, m_bIsHidden);
	}

	void setFlags(int f)
	{
		m_iFlags = f;
	}

	public int getFlags()
	{
		return m_iFlags;
	}

	void setType(String t)
	{
		m_strXMLNodeName = t;
	}

	String getType()
	{
		return m_strXMLNodeName;
	}

	void setTransport(int t)
	{
		m_iTransport = t;
	}

	public int getTransport()
	{
		return m_iTransport;
	}

	public void setHostname(String n)
	{
		m_strHostname = n;
	}

	public String getHostname()
	{
		return m_strHostname;
	}

	public void setVisibleAddress(String n)
	{
		m_strVisibleAddress = n;
	}

	public String getVisibleAddress()
	{
		return m_strVisibleAddress;
	}
	
	void setPort(int p)
	{
		m_iPort = p;
	}

	public int getPort()
	{
		return m_iPort;
	}

	void setIsHidden(boolean b)
	{
		m_bIsHidden = b;
	}

	public boolean isHidden()
	{
		return m_bIsHidden;
	}

	void setIsVirtual(boolean b)
	{
		m_bIsVirtual = b;
	}

	public boolean isVirtual()
	{
		return m_bIsVirtual;
	}

	public String getVisibilityString()
	{
		if (m_bIsVirtual)
		{
			return new String("Virtual");
		}
		else if(m_bIsHidden)
		{
			return new String("Hidden");
		}
		else
		{
			return new String("");
		}
	}

	/**
	 * Create the XML document
	 */
	public Element toXmlElement(Document docOwner)
	{
		Element elemRoot = docOwner.createElement(m_strXMLNodeName);
		boolean isProxy = (m_iFlags & PROXY_MASK) != NO_PROXY; // Proxy?
		Element data; // Dummy element
		if (isHidden())
		{
			elemRoot.setAttribute("hidden", "True");
		}
		if (isVirtual())
		{
			elemRoot.setAttribute("virtual", "True");
		}
		if (isProxy)
		{
			data = docOwner.createElement("ProxyType");
			int proxytype=m_iFlags & PROXY_MASK;
			String strProxyType="";
			if(proxytype==HTTP_PROXY)
				strProxyType="HTTP";
			if(proxytype==SOCKS_PROXY)
				strProxyType="SOCKS";
			if(proxytype==VPN_PROXY)
				strProxyType="VPN";
			data.appendChild(docOwner.createTextNode( strProxyType));
			elemRoot.appendChild(data);
		}
		data = docOwner.createElement("NetworkProtocol");
		data.appendChild(docOwner.createTextNode(getTransportAsString()));
		elemRoot.appendChild(data);
		// TCP or UDP?
		if ((m_iTransport & UNIX) == 0)
		{
			// Create elements for host ...
			data = docOwner.createElement("Host");
			if (!m_strHostname.equalsIgnoreCase(JAPMessages.getString(MixOnCDPanel.MSG_CONFIGURED_BY_MIXONCD)))
			{
				data.appendChild(docOwner.createTextNode(m_strHostname));
			}
			elemRoot.appendChild(data);
			// ... and port
			data = docOwner.createElement("Port");
			data.appendChild(docOwner.createTextNode(String.valueOf(m_iPort)));
			elemRoot.appendChild(data);
		}
		else // (UNIX)
		{
			data = docOwner.createElement("File");
			if (!m_strHostname.equalsIgnoreCase(JAPMessages.getString(MixOnCDPanel.MSG_CONFIGURED_BY_MIXONCD)))
			{
				data.appendChild(docOwner.createTextNode(m_strHostname));
			}
			elemRoot.appendChild(data);
		}
		// If this is a proxy, create VisibleAddress
		if (isProxy)
		{
			Element visibleAddresses = docOwner.createElement("VisibleAddresses");
			// TODO: For all visible addresses do ...
			data = docOwner.createElement("VisibleAddress");
			if(m_strVisibleAddress!=null)
				data.appendChild(docOwner.createTextNode(m_strVisibleAddress));
			visibleAddresses.appendChild(data);
			// And add
			elemRoot.appendChild(visibleAddresses);
		}
		return elemRoot;
	}

	private String getTransportAsString()
		{
			return getTransportAsString(m_iTransport);
		}
	
	public static String getTransportAsString(int iTransport)
		{
			String strTransport= ((iTransport & SSL) == 0) ? "RAW/" : "SSL/";
			switch(iTransport&TRANSPORT_BIT_MASK)
			{
				case UNIX:
					return strTransport+"UNIX";
				case TCP:
					return strTransport+"TCP";
				case UDP:
					return strTransport+"UDP";
			}
			return null;
		}

	/**
	 * Instantiate ConnectionData from XML element
	 * @param tag
	 * @param elemRoot
	 * @return
	 */
	static ConnectionData createFromElement(String tag, Element elemRoot)
	{
		if (!elemRoot.getTagName().equals(tag))
		{
			return null;
		}
		// Transport, ProxyType and Port
		int trans, ptype, port = 0;
		String host, data;
		// XXX Not referenced: String ip = null;
		// Get the attributes first
		boolean hidden = false, virtual = false;
		String tmp = elemRoot.getAttribute("hidden");
		if (tmp != null && tmp.equals("True"))
		{
			hidden = true;
		}
		tmp = elemRoot.getAttribute("virtual");
		if (tmp != null && tmp.equals("True"))
		{
			virtual = true;

		}
		// Read the ProxyType
		ptype = NO_PROXY;
		data = getChildElementValue(elemRoot, "ProxyType");
		if (data == null)
		{
			ptype = NO_PROXY;
		}
		else if (data.equalsIgnoreCase("HTTP"))
		{
			ptype = HTTP_PROXY;
		}
		else if (data.equalsIgnoreCase("SOCKS"))
		{
			ptype = SOCKS_PROXY;
		}
		else if (data.equalsIgnoreCase("VPN"))
		{
			ptype = VPN_PROXY;
		}
		// NetworkProtocol
		data = getChildElementValue(elemRoot, "NetworkProtocol");
		if (data != null)
		{
			if (data.equalsIgnoreCase("RAW/UNIX"))
			{
				trans = RAW_UNIX;
			}
			else if (data.equalsIgnoreCase("RAW/TCP"))
			{
				trans = RAW_TCP;
			}
			else if (data.equalsIgnoreCase("RAW/UDP"))
			{
				trans = RAW_UDP;
			}
			else if (data.equalsIgnoreCase("SSL/UNIX"))
			{
				trans = SSL_UNIX;
			}
			else if (data.equalsIgnoreCase("SSL/TCP"))
			{
				trans = SSL_TCP;
			}
			else
			{
				trans = 0;
			}
		}
		else if (getChildElementValue(elemRoot, "File") != null)
		{
			trans = RAW_UNIX;
		}
		else
		{
			trans = RAW_TCP;
		}
		if ( (trans & UNIX) == 0)
		{
			host = getChildElementValue(elemRoot, "Host");
			if (host == null)
			{
				host = "";
			}
			//ip = getChildElementValue(elemRoot, "IP");
			data = getChildElementValue(elemRoot, "Port");
			if (data == null)
			{
				port = 0;
			}
			else
			{
				port = Integer.parseInt(data);
			}
			// Proxies have VisibleAddresses
			if (ptype == NO_PROXY)
			{
				return new ConnectionData(tag, trans, host, port, ptype, virtual, hidden);	
			}
			else // Proxy, pass VisibleAddresses
			{
				Node visibleAddresses = XMLUtil.getFirstChildByName(elemRoot, "VisibleAddresses");
				// TODO: Parse all VisibleAddress elements
				String address = getChildElementValue((Element)visibleAddresses, "VisibleAddress");
				return new ConnectionData(tag, trans, host, port, address, ptype, virtual, hidden);
			}
		}
		else
		{
			host = getChildElementValue(elemRoot, "File");
			if (host == null)
			{
				host = "";
			}
			return new ConnectionData(tag, trans, host, ptype);
		}
	}

	/**
	 * Return the value of a XML element
	 * @param elemParent
	 * @param childName
	 * @return
	 */
	static private String getChildElementValue(Element elemParent, String childName)
	{
		Node nodeChild = XMLUtil.getFirstChildByName(elemParent, childName);
		return XMLUtil.parseValue(nodeChild, null);
	}
}

