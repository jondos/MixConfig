package mixconfig.networkpanel;

import org.w3c.dom.Node;
import anon.crypto.*;

final public class OutgoingConnectionTableModel extends ConnectionTableModel
{
    private static final String[] columnNames =
            {"No.", "Type", "Transport", "Host / FileName",
             "IP Address", "Port" };

    public static final int SERIAL_NR = 0;
    public static final int TYPE = 1;
    public static final int TRANSPORT = 2;
    public static final int NAME = 3;
    public static final int IP_ADDR = 4;
    public static final int PORT = 5;

    public Object getValueAt(int row, int column)
    {
        ConnectionData data = getData(row);
        if(data==null)
            return null;
        switch(column)
        {
            case SERIAL_NR: return new Integer(row+1);
            case TYPE:
                if(data.getType().equals("Proxy"))
                {
                    switch(data.getFlags()&ConnectionData.PROXY_MASK)
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
                    return "Mix";
            case TRANSPORT: return new Integer(data.getTransport());
            case NAME: return data.getName();
            case IP_ADDR: return data.getIPAddr();
            case PORT: return new Integer(data.getPort());
        }
        return null;
    }

    public Class getColumnClass(int column)
    {
        switch(column)
        {
            case SERIAL_NR: return Integer.class;
            case NAME: return String.class;
            case PORT: return Integer.class;
            // Type, Transport und IP-Addresse muessen wir in der Tabelle
            // gesondert behandeln.
        }
        return Object.class;
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public String getColumnName(int col)
    {
        return columnNames[col];
    }

    public org.w3c.dom.Element createProxiesAsElement(org.w3c.dom.Document doc)
    {
        org.w3c.dom.Element proxies = doc.createElement("Proxies");
        for(int i=0;i<getRowCount();i++)
            if(getData(i).getType().equals("Proxy"))
            {
                org.w3c.dom.Element proxy = getData(i).createAsElement(doc);
                proxies.appendChild(proxy);
            }
                                if(proxies.hasChildNodes())
            return proxies;
                                else
                                    return null;
    }

    public org.w3c.dom.Element createMixAsElement(org.w3c.dom.Document doc)
    {
        for(int i=0;i<getRowCount();i++)
            if(getData(i).getType().equals("NextMix"))
            {
                org.w3c.dom.Element mix = getData(i).createAsElement(doc);
                return mix;
            }
        return null;
    }

    public void readFromElement(org.w3c.dom.Element out)
    {
        if(out.getTagName().equals("Proxies"))
        {
            for(int i=getRowCount()-1;i>=0;i--)
                if(getData(i).getType().equals("Proxy"))
                    deleteData(i);
            org.w3c.dom.Node child = out.getFirstChild();
            while(child!=null)
            {
                if(child.getNodeType() == Node.ELEMENT_NODE)
                {
                    ConnectionData data = ConnectionData.createFromElement(
                            "Proxy", (org.w3c.dom.Element)child);
                    if(data!=null)
                        addData(data);
                }
                child = child.getNextSibling();
            }
        }
        else if(out.getTagName().equals("NextMix"))
        {
            for(int i=getRowCount()-1;i>=0;i--)
                if(getData(i).getType().equals("NextMix"))
                    deleteData(i);
            ConnectionData data = ConnectionData.createFromElement("NextMix", out);
            if(data!=null)
                addData(data);
        }
    }
}
