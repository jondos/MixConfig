package mixconfig.networkpanel;

import org.w3c.dom.Node;
final public class ConnectionData
{
    private int transport;
    private String name;
    private int[] ipaddr;
    private int port;
    private String type; // Name of the XML element
    private int flags;

                public static final int TRANSPORT = 1; // Bit mask
    public static final int TCP = 0;
    public static final int UNIX = 1;
    public static final int RAW = 0;
    public static final int SSL = 2;
    public static final int RAW_TCP = RAW|TCP;
    public static final int RAW_UNIX = RAW|UNIX;
    public static final int SSL_TCP = SSL|TCP;
    public static final int SSL_UNIX = SSL|UNIX;

    public static final int PROXY_MASK = 3;

    public static final int NO_PROXY = 0;
    public static final int HTTP_PROXY = 1;
    public static final int SOCKS_PROXY = 2;

    void setFlags(int f)
    {
        flags = f;
    }

    int getFlags()
    {
        return flags;
    }

    void setType(String t)
    {
        type = t;
    }

    String getType()
    {
        return type;
    }

    void setTransport(int t)
    {
        transport = t;
    }

    public int getTransport()
    {
        return transport;
    }

    void setName(String n)
    {
        name = n;
    }

    public String getName()
    {
        return name;
    }

    void setIPAddr(int[] addr)
    {
        if(addr==null)
            ipaddr = null;
        else
            ipaddr = (int[])addr.clone();
    }

    void setIPAddr(String addr)
    {
        if(addr==null)
        {
            ipaddr = null;
            return;
        }
        int idx = 0;
        int[] myaddr = new int[4];

        for(int i=0;i<4;i++)
        {
            int newidx = (i==3)?addr.length():addr.indexOf('.',idx);
            myaddr[i] = Integer.parseInt(addr.substring(idx,newidx),10);
            idx = newidx+1;
        }
        ipaddr = myaddr;
    }

    int[] getIPAddr()
    {
        return ipaddr;
//        return (ipaddr==null)?null:((int[])ipaddr.clone());
    }

    void setPort(int p)
    {
        port = p;
    }

    public int getPort()
    {
        return port;
    }

    ConnectionData(String x, int t, String n, int[] addr, int p)
    {
        setType(x);
        setTransport(t);
        setName(n);
        setIPAddr(addr);
        setPort(p);
        setFlags(0);
    }

    ConnectionData(String x, int t, String n)
    {
        setType(x);
        setTransport(t);
        setName(n);
        ipaddr = null;
        setPort(0);
        setFlags(0);
    }

    ConnectionData(String x, int t, String n, String addr, int p)
    {
        setType(x);
        setTransport(t);
        setName(n);
        setIPAddr(addr);
        setPort(p);
        setFlags(0);
    }

    ConnectionData(String x, int t, String n, int[] addr, int p, int f)
    {
        setType(x);
        setTransport(t);
        setName(n);
        setIPAddr(addr);
        setPort(p);
        setFlags(f);
    }

    ConnectionData(String x, int t, String n, int f)
    {
        setType(x);
        setTransport(t);
        setName(n);
        ipaddr = null;
        setPort(0);
        setFlags(f);
    }

    ConnectionData(String x, int t, String n, String addr, int p, int f)
    {
        setType(x);
        setTransport(t);
        setName(n);
        setIPAddr(addr);
        setPort(p);
        setFlags(f);
    }

    ConnectionData deepClone()
    {
        return new ConnectionData(type, transport, name, ipaddr, port, flags);
    }

    org.w3c.dom.Element createAsElement(org.w3c.dom.Document doc)
    {
        org.w3c.dom.Element iface = doc.createElement(type);
        org.w3c.dom.Element data;
        if((flags&PROXY_MASK)!=NO_PROXY)
        {
            data = doc.createElement("ProxyType");
            data.appendChild(doc.createTextNode(((flags&PROXY_MASK)==HTTP_PROXY)?"HTTP":"SOCKS"));
            iface.appendChild(data);
        }
        data = doc.createElement("NetworkProtocol");
        data.appendChild(doc.createTextNode(
                (((transport&SSL)==0)?"RAW/":"SSL/")+
                (((transport&UNIX)==0)?"TCP":"UNIX")));
        iface.appendChild(data);
        if((transport&UNIX)==0) // TCP?
        {
            data = doc.createElement("Host");
            data.appendChild(doc.createTextNode(name));
            iface.appendChild(data);
            if(ipaddr!=null)
            {
                data = doc.createElement("IP");
                data.appendChild(doc.createTextNode(
                        ipaddr[0]+"."+ipaddr[1]+"."+ipaddr[2]+"."+ipaddr[3]));
                iface.appendChild(data);
            }
            data = doc.createElement("Port");
            data.appendChild(doc.createTextNode(String.valueOf(port)));
            iface.appendChild(data);
        }
        else // UNIX?
        {
            data = doc.createElement("File");
            data.appendChild(doc.createTextNode(name));
            iface.appendChild(data);
        }
        return iface;
    }

    static private String elementData(org.w3c.dom.Element iface, String name)
    {
        org.w3c.dom.Node node;
        String data = null;
        org.w3c.dom.NodeList nlist = iface.getElementsByTagName(name);
        if(nlist.getLength()==0 || (node=nlist.item(0)).getNodeType()!=org.w3c.dom.Node.ELEMENT_NODE)
            return null;
        node = ((org.w3c.dom.Element)node).getFirstChild();
        while(node!=null)
        {
            if(node.getNodeType()==Node.TEXT_NODE)
            {
                data = ((org.w3c.dom.Text)node).getData();
                break;
            }
        }
        return data;
    }

    static ConnectionData createFromElement(String t, org.w3c.dom.Element iface)
    {
      try{
        if(!iface.getTagName().equals(t))
            return null;

        int trans, ptype;
        String n, ip=null, data;
        int p=0;

  data = elementData(iface, "ProxyType");
  if(data == null)
      ptype = NO_PROXY;
  else if(data.equalsIgnoreCase("HTTP"))
      ptype = HTTP_PROXY;
  else
      ptype = SOCKS_PROXY;
        data = elementData(iface, "NetworkProtocol");
        if(data.equalsIgnoreCase("RAW/UNIX"))
            trans = RAW_UNIX;
        else if(data.equalsIgnoreCase("RAW/TCP"))
            trans = RAW_TCP;
        else if(data.equalsIgnoreCase("SSL/UNIX"))
            trans = SSL_UNIX;
        else if(data.equalsIgnoreCase("SSL/TCP"))
            trans = SSL_TCP;
        else if(elementData(iface, "File")!=null)
            trans = RAW_UNIX;
        else
            trans = RAW_TCP;
        if((trans&UNIX)==0)
        {
            n = elementData(iface, "Host");
            if(n==null)
                n = "";
            ip = elementData(iface, "IP");
            data = elementData(iface, "Port");
            if(data==null)
                p = 0;
            else
                p = Integer.parseInt(data);
            return new ConnectionData(t, trans, n, ip, p, ptype);
        }
        else
        {
            n = elementData(iface, "File");
            if(n==null)
                n = "";
            return new ConnectionData(t, trans, n, ptype);
        }
      }
      catch(Exception e)
      {
        System.out.println("Network interface not set");
        return null;
      }
        }
}
