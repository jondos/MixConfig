package mixconfig.networkpanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import mixconfig.MixConfig;

final public class ConnectionData
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
	private int[] m_arIPAddr;
	private int m_iPort;
	private String m_strXMLNodeName; // Name of the XML element
	private int m_iFlags;
	private boolean m_bIsVirtual = false;
	private boolean m_bIsHidden = false;

	ConnectionData(String x, int t, String n, int[] addr, int p)
	{
		setType(x);
		setTransport(t);
		setName(n);
		setIPAddr(addr);
		setPort(p);
		setFlags(0);
		setIsVirtual(false);
		setIsHidden(false);
	}

	ConnectionData(String x, int t, String n, String addr, int p, int f, boolean virtual, boolean hidden)
	{
		setType(x);
		setTransport(t);
		setName(n);
		setIPAddr(addr);
		setPort(p);
		setFlags(f);
		setIsVirtual(virtual);
		setIsHidden(hidden);
	}

	ConnectionData(String x, int t, String n, int[] addr, int p, int f, boolean virtual, boolean hidden)
	{
		setType(x);
		setTransport(t);
		setName(n);
		setIPAddr(addr);
		setPort(p);
		setFlags(f);
		setIsVirtual(virtual);
		setIsHidden(hidden);
	}

	ConnectionData(String x, int t, String n, int f)
	{
		setType(x);
		setTransport(t);
		setName(n);
		m_arIPAddr = null;
		setPort(0);
		setFlags(f);
		setIsVirtual(false);
		setIsHidden(false);
	}

	ConnectionData(String x, int t, String n)
	{
		setType(x);
		setTransport(t);
		setName(n);
		m_arIPAddr = null;
		setPort(0);
		setFlags(0);
		setIsVirtual(false);
		setIsHidden(false);
	}

	ConnectionData deepClone()
	{
		return new ConnectionData(m_strXMLNodeName, m_iTransport, m_strHostname, m_arIPAddr, m_iPort,
								  m_iFlags, m_bIsVirtual, m_bIsHidden);
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

	void setName(String n)
	{
		m_strHostname = n;
	}

	public String getName()
	{
		return m_strHostname;
	}

	void setIPAddr(int[] addr)
	{
		if (addr == null)
		{
			m_arIPAddr = null;
		}
		else
		{
			m_arIPAddr = (int[]) addr.clone();
		}
	}

	void setIPAddr(String addr)
	{
		if (addr == null)
		{
			m_arIPAddr = null;
			return;
		}
		int idx = 0;
		int[] myaddr = new int[4];

		for (int i = 0; i < 4; i++)
		{
			int newidx = (i == 3) ? addr.length() : addr.indexOf('.', idx);
			myaddr[i] = Integer.parseInt(addr.substring(idx, newidx), 10);
			idx = newidx + 1;
		}
		m_arIPAddr = myaddr;
	}

	int[] getIPAddr()
	{
		return m_arIPAddr;
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

	Element createAsElement(Document docOwner)
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
			data.appendChild(docOwner.createTextNode(m_strHostname));
			elemRoot.appendChild(data);
			if (m_arIPAddr != null)
			{
				data = docOwner.createElement("IP");
				data.appendChild(docOwner.createTextNode(
					m_arIPAddr[0] + "." + m_arIPAddr[1] + "." + m_arIPAddr[2] + "." + m_arIPAddr[3]));
				elemRoot.appendChild(data);
			}
			data = docOwner.createElement("Port");
			data.appendChild(docOwner.createTextNode(String.valueOf(m_iPort)));
			elemRoot.appendChild(data);
		}
		else // UNIX?
		{
			data = docOwner.createElement("File");
			data.appendChild(docOwner.createTextNode(m_strHostname));
			elemRoot.appendChild(data);
		}
		return elemRoot;
	}

	static private String elementData(Element iface, String name)
	{
		Node node;
		String data = null;
		NodeList nlist = iface.getElementsByTagName(name);
		if (nlist.getLength() == 0 || (node = nlist.item(0)).getNodeType() != org.w3c.dom.Node.ELEMENT_NODE)
		{
			return null;
		}
		node = ( (Element) node).getFirstChild();
// What is this while loop for? I comment it out because it causes infinite loops
// if node is not a text node
//		while (node != null)
//		{
			if (node.getNodeType() == Node.TEXT_NODE)
			{
				data = ( (org.w3c.dom.Text) node).getData();
//				break;
			}
//		}
		return data;
	}

	static ConnectionData createFromElement(String t, Element elemRoot)
	{
		try
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
			data = elementData(elemRoot, "ProxyType");
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
			data = elementData(elemRoot, "NetworkProtocol");
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
			else if (elementData(elemRoot, "File") != null)
			{
				trans = RAW_UNIX;
			}
			else
			{
				trans = RAW_TCP;
			}
			if ( (trans & UNIX) == 0)
			{
				n = elementData(elemRoot, "Host");
				if (n == null)
				{
					n = "";
				}
				ip = elementData(elemRoot, "IP");
				data = elementData(elemRoot, "Port");
				if (data == null)
				{
					p = 0;
				}
				else
				{
					p = Integer.parseInt(data);
				}
				return new ConnectionData(t, trans, n, ip, p, ptype, virtual, hidden);
			}
			else
			{
				n = elementData(elemRoot, "File");
				if (n == null)
				{
					n = "";
				}
				return new ConnectionData(t, trans, n, ptype);
			}
		}
		catch (Exception e)
		{
			MixConfig.handleException(e);
			//System.out.println("Network interface not set");
			return null;
		}
	}
}
