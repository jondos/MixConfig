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
package mixconfig.networkpanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import anon.util.XMLUtil;
import gui.JAPMessages;
import anon.util.IXMLEncodable;
import mixconfig.panels.MixOnCDPanel;

final public class ConnectionData implements IXMLEncodable, Cloneable
{
	public static final int TRANSPORT = 1; // Bit mask
	public static final int TCP = 0;
	public static final int UNIX = 1;
	public static final int RAW = 0;
	public static final int SSL = 2;
	public static final int RAW_TCP = RAW | TCP;
	public static final int RAW_UNIX = RAW | UNIX;
	public static final int SSL_TCP = SSL | TCP;
	public static final int SSL_UNIX = SSL | UNIX;

	public static final int PROXY_MASK = 3;

	public static final int NO_PROXY = 0;
	public static final int HTTP_PROXY = 1;
	public static final int SOCKS_PROXY = 2;

	private int m_iTransport;
	private String m_strHostname;
	private int m_iPort;
	private String m_strXMLNodeName; // Name of the XML element
	private int m_iFlags;
	private boolean m_bIsVirtual = false;
	private boolean m_bIsHidden = false;

	public ConnectionData(String a_strXMLNodeName, int a_iTransport,
						  String a_strHostname, int a_port)
	{
		setType(a_strXMLNodeName);
		setTransport(a_iTransport);
		setName(a_strHostname);
		setPort(a_port);
		setFlags(0);
		setIsVirtual(false);
		setIsHidden(false);
	}

	public ConnectionData(String a_strXMLNodeName, int a_iTransport,
						   String a_strHostname, int a_port, int f,
						   boolean virtual, boolean hidden)
	{
		setType(a_strXMLNodeName);
		setTransport(a_iTransport);
		setName(a_strHostname);
		setPort(a_port);
		setFlags(f);
		setIsVirtual(virtual);
		setIsHidden(hidden);
	}

	public ConnectionData(String a_strXMLNodeName, int a_iTransport, int f,
						  String a_strHostname)
	{
		setType(a_strXMLNodeName);
		setTransport(a_iTransport);
		setName(a_strHostname);
		setPort(0);
		setFlags(f);
		setIsVirtual(false);
		setIsHidden(false);
	}

	public ConnectionData(String a_strXMLNodeName, int t, String a_strHostname)
	{
		setType(a_strXMLNodeName);
		setTransport(t);
		setName(a_strHostname);
		setPort(0);
		setFlags(0);
		setIsVirtual(false);
		setIsHidden(false);
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

	int getFlags()
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

	public void setName(String n)
	{
		m_strHostname = n;
	}

	public String getName()
	{
		return m_strHostname;
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

	public Element toXmlElement(Document docOwner)
	{
		Element elemRoot = docOwner.createElement(m_strXMLNodeName);
		Element data;
		if (isHidden())
		{
			elemRoot.setAttribute("hidden", "True");
		}
		if (isVirtual())
		{
			elemRoot.setAttribute("virtual", "True");
		}
		if ( (m_iFlags & PROXY_MASK) != NO_PROXY)
		{
			data = docOwner.createElement("ProxyType");
			data.appendChild(docOwner.createTextNode( ( (m_iFlags & PROXY_MASK) == HTTP_PROXY) ? "HTTP" :
				"SOCKS"));
			elemRoot.appendChild(data);
		}
		data = docOwner.createElement("NetworkProtocol");
		data.appendChild(docOwner.createTextNode(
			( ( (m_iTransport & SSL) == 0) ? "RAW/" : "SSL/") +
			( ( (m_iTransport & UNIX) == 0) ? "TCP" : "UNIX")));
		elemRoot.appendChild(data);
		if ( (m_iTransport & UNIX) == 0) // TCP?
		{
			data = docOwner.createElement("Host");
			if (!m_strHostname.equalsIgnoreCase(
						 JAPMessages.getString(MixOnCDPanel.MSG_CONFIGURED_BY_MIXONCD)))
			{
				data.appendChild(docOwner.createTextNode(m_strHostname));
			}

			elemRoot.appendChild(data);
			data = docOwner.createElement("Port");
			data.appendChild(docOwner.createTextNode(String.valueOf(m_iPort)));
			elemRoot.appendChild(data);
		}
		else // UNIX?
		{
			data = docOwner.createElement("File");
			if (!m_strHostname.equalsIgnoreCase(JAPMessages.getString(
												MixOnCDPanel.MSG_CONFIGURED_BY_MIXONCD)))
			{
				data.appendChild(docOwner.createTextNode(m_strHostname));
			}
			elemRoot.appendChild(data);
		}
		return elemRoot;
	}

	static ConnectionData createFromElement(String t, Element elemRoot)
	{
		if (!elemRoot.getTagName().equals(t))
		{
			return null;
		}

		int trans, ptype;
		String n, ip = null, data;
		int p = 0;
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

		data = getChildElementValue(elemRoot, "ProxyType");
		if (data == null)
		{
			ptype = NO_PROXY;
		}
		else if (data.equalsIgnoreCase("HTTP"))
		{
			ptype = HTTP_PROXY;
		}
		else
		{
			ptype = SOCKS_PROXY;
		}
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
			n = getChildElementValue(elemRoot, "Host");
			if (n == null)
			{
				n = "";
			}
			ip = getChildElementValue(elemRoot, "IP");
			data = getChildElementValue(elemRoot, "Port");
			if (data == null)
			{
				p = 0;
			}
			else
			{
				p = Integer.parseInt(data);
			}
			return new ConnectionData(t, trans, n, p, ptype, virtual, hidden);
		}
		else
		{
			n = getChildElementValue(elemRoot, "File");
			if (n == null)
			{
				n = "";
			}
			return new ConnectionData(t, trans, ptype, n);
		}
	}

	static private String getChildElementValue(Element elemParent, String childName)
	{
		Node nodeChild = XMLUtil.getFirstChildByName(elemParent, childName);
		return XMLUtil.parseValue(nodeChild, null);
	}
}

