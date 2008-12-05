package mixconfig.network;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ProxyTableModel extends ConnectionTableModel {

	private static final String[] columnNames = {"No.", "Type", "Transport", "Host", "Port", "Visible Address"};
		
	/**
	 * Return the number of columns
	 */
	public int getColumnCount() {
		return columnNames.length;
	}
	
	/**
	 * Return the name of a given column
	 */
	public String getColumnName(int col)
	{
		return columnNames[col];
	}

	public Object getValueAt(int row, int column)
	{
		ConnectionData data = getData(row);
		if (data == null)
		{
			return null;
		}
		switch (column)
		{
			case NR:
				return new Integer(row + 1);
			case TYPE:
				// TODO: Remove the check
				if (data.getType().equals("Proxy"))
				{
					switch (data.getFlags() & ConnectionData.PROXY_MASK)
					{
						case ConnectionData.NO_PROXY:
							return "Proxy";
						case ConnectionData.HTTP_PROXY:
							return "HTTP Proxy";
						case ConnectionData.SOCKS_PROXY:
							return "Socks Proxy";
					}
				}
				else
				{
					return "Mix";
				}
			case TRANSPORT:
				return new Integer(data.getTransport());
			case HOST:
				return data.getHostname();
			case VISIBLE:
				return data.getVisibleAddress();
			case PORT:
				return new Integer(data.getPort());
		}
		return null;
	}
	
	/**
	 * Create XML element from table model
	 * @param doc
	 * @return
	 */
	public Element createProxiesAsElement(Document doc)
	{
		// Create the element
		Element proxies = doc.createElement("Proxies");
		for (int i = 0; i < getRowCount(); i++)
		{
			if (getData(i).getType().equals("Proxy"))
			{
				Element proxy = getData(i).toXmlElement(doc);
				proxies.appendChild(proxy);
			}
		}
		if (proxies.hasChildNodes())
		{
			return proxies;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Read data from XML
	 * @param out
	 */
	public void readFromElement(Element element)
	{
		if (element.getTagName().equals("Proxies"))
		{
			for (int i = getRowCount() - 1; i >= 0; i--)
			{
				if (getData(i).getType().equals("Proxy"))
				{
					deleteData(i);
				}
			}
			Node child = element.getFirstChild();
			while (child != null)
			{
				if (child.getNodeType() == Node.ELEMENT_NODE)
				{
					ConnectionData data = ConnectionData.createFromElement("Proxy", (Element)child);
					if (data != null)
					{
						addData(data);
					}
				}
				child = child.getNextSibling();
			}
		}
	}
}
