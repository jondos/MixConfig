package mixconfig;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.DefaultCellEditor;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JSeparator;

class ConnectionData
{
    private boolean main;
    private int transport;
    private String name;
    private int[] ipaddr;
    private int port;
    private String type; // Name of the XML element
    private int flags;

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

    void setIsMain(boolean m)
    {
        main = m;
    }

    boolean getIsMain()
    {
        return main;
    }

    void setTransport(int t)
    {
        transport = t;
    }

    int getTransport()
    {
        return transport;
    }

    void setName(String n)
    {
        name = n;
    }

    String getName()
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

    int getPort()
    {
        return port;
    }

    ConnectionData(String x, boolean m, int t, String n, int[] addr, int p)
    {
        setType(x);
        setIsMain(m);
        setTransport(t);
        setName(n);
        setIPAddr(addr);
        setPort(p);
        setFlags(0);
    }

    ConnectionData(String x, boolean m, int t, String n)
    {
        setType(x);
        setIsMain(m);
        setTransport(t);
        setName(n);
        ipaddr = null;
        setPort(0);
        setFlags(0);
    }

    ConnectionData(String x, boolean m, int t, String n, String addr, int p)
    {
        setType(x);
        setIsMain(m);
        setTransport(t);
        setName(n);
        setIPAddr(addr);
        setPort(p);
        setFlags(0);
    }

    ConnectionData(String x, boolean m, int t, String n, int[] addr, int p, int f)
    {
        setType(x);
        setIsMain(m);
        setTransport(t);
        setName(n);
        setIPAddr(addr);
        setPort(p);
        setFlags(f);
    }

    ConnectionData(String x, boolean m, int t, String n, int f)
    {
        setType(x);
        setIsMain(m);
        setTransport(t);
        setName(n);
        ipaddr = null;
        setPort(0);
        setFlags(f);
    }

    ConnectionData(String x, boolean m, int t, String n, String addr, int p, int f)
    {
        setType(x);
        setIsMain(m);
        setTransport(t);
        setName(n);
        setIPAddr(addr);
        setPort(p);
        setFlags(f);
    }

    ConnectionData deepClone()
    {
        return new ConnectionData(type, main, transport, name, ipaddr, port, flags);
    }

    org.w3c.dom.Element createAsElement(org.w3c.dom.Document doc)
    {
        org.w3c.dom.Element iface = doc.createElement(type);
        iface.setAttribute("main",main?"True":"False");
        org.w3c.dom.Element data = doc.createElement("Type");
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
        if((flags&PROXY_MASK)!=NO_PROXY)
        {
            data = doc.createElement("ProxyType");
            data.appendChild(doc.createTextNode(((flags&PROXY_MASK)==HTTP_PROXY)?"HTTP":"SOCKS"));
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
            if(node.getNodeType()==node.TEXT_NODE)
            {
                data = ((org.w3c.dom.Text)node).getData();
                break;
            }
        }
        return data;
    }

    static ConnectionData createFromElement(String t, org.w3c.dom.Element iface)
    {
        if(!iface.getTagName().equals(t))
            return null;

        boolean m;
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
        m = iface.getAttribute("main").equalsIgnoreCase("True");
        data = elementData(iface, "Type");
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
            return new ConnectionData(t, m, trans, n, ip, p, ptype);
        }
        else
        {
            n = elementData(iface, "File");
            if(n==null)
                n = "";
            return new ConnectionData(t, m, trans, n, ptype);
        }
    }
}

/**
 *  A document that accepts only non-negatives.
 */
class IntegerDocument extends PlainDocument
{
    int max;
    Component which;

    IntegerDocument(int maxval, Component comp)
    {
        super();
        max = maxval;
        which = comp;
    }

    IntegerDocument(int maxval)
    {
        super();
        max = maxval;
        which = null;
    }

    IntegerDocument()
    {
        super();
        max = 0;
        which = null;
    }

    IntegerDocument(Component comp)
    {
        super();
        max = 0;
        which = comp;
    }

    public void insertString(int offset, String str, AttributeSet attr)
            throws BadLocationException
    {
        if(str==null)
            return;

        String p1 = getText(0,offset);
        String p2 = getText(offset, getLength()-offset);
        String res = "";

        for(int i=0;i<str.length();i++)
            if(!Character.isDigit(str.charAt(i)))
                java.awt.Toolkit.getDefaultToolkit().beep();
            else
            {
                String sstr = str.substring(i,i+1);
                int val = Integer.parseInt(p1+res+sstr+p2,10);
                if(max>0 && val>max)
                    java.awt.Toolkit.getDefaultToolkit().beep();
                else
                    res+=sstr;
            }
        super.insertString(offset,res,attr);
        if(which!=null && max>0 && getLength()>0 && 10*Integer.parseInt(getText(0,getLength()),10)>max)
            which.transferFocus();
    }
}

class IPTextField extends JPanel
{
    private JTextField[] iptext;

    private void initIPTextField(String IPStr)
    {
        final ActionListener nextfocusaction = new ActionListener()
            {
                public void actionPerformed(ActionEvent evt)
                {
                    ((Component)evt.getSource()).transferFocus();
                }
            };

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        iptext = new JTextField[4];

        GridBagConstraints ic=new GridBagConstraints();
        ic.anchor = GridBagConstraints.WEST;
        ic.fill = GridBagConstraints.HORIZONTAL;
        ic.insets = new Insets(1,1,1,1);
        ic.gridx = 0;
        ic.gridy = 0;

        int pos = 0;
        for(int i=0;i<4;i++)
        {
            int npos = IPStr.indexOf('.',pos);
            String str;
            if(npos<0)
                str = "";
            else
            {
                str = IPStr.substring(pos,npos-1);
                pos = npos+1;
            }
            if(i>0)
            {
                JLabel punkt = new JLabel(".");
                ic.weightx = 0;
                layout.setConstraints(punkt, ic);
                add(punkt);
                ic.gridx++;
            }
            iptext[i] = new JTextField(3);
            iptext[i].setMinimumSize(iptext[i].getPreferredSize());
            iptext[i].setDocument(new IntegerDocument(255,iptext[i]));
            iptext[i].setText(str);
            ic.weightx = 1;
            layout.setConstraints(iptext[i],ic);
            add(iptext[i]);
            iptext[i].addActionListener(nextfocusaction);
            ic.gridx++;
        }
    }

    public IPTextField()
    {
        super();
        initIPTextField("");
    }

    public String getText()
    {
        String str = "";
        for(int i=0;i<4;i++)
        {
            if(iptext[i].getText().length()==0)
                str+="0";
            else
                str+=iptext[i].getText();
            if(i<3)
                str+=".";
        }
        return str;
    }

    public void setText(String str)
    {
        int pos = 0;
        for(int i=0;i<4;i++)
        {
            int npos = str.indexOf('.',pos);
            if(npos<0)
                iptext[i].setText("");
            else
            {
                iptext[i].setText(str.substring(pos,npos-1));
                pos = npos+1;
            }
        }
    }

    public boolean isEmpty()
    {
        for(int i=0;i<4;i++)
            if(iptext[i].getText().length()!=0)
                return false;
        return true;
    }

    public boolean isCorrect()
    {
        for(int i=0;i<4;i++)
            if(iptext[i].getText().length()==0)
                return false;
        return true;
    }
}

abstract class ConnectionDialog extends JDialog
{
    private JTextField nametext, iptext[];
    private JCheckBox main;
    private ButtonGroup ssl, stype;
    private JLabel namelabel, iplabel[];
    protected javax.swing.JComponent firstone;
    abstract protected String getType();

    protected ConnectionData getData()
    {
        if(stype.getSelection().getActionCommand().equals("TCP"))
        {
            int[] ips = new int[4];
            for(int i=0;i<4;i++)
            {
                if(iptext[i].getText().length()==0)
                {
                    ips = null;
                    break;
                }
                else
                    ips[i] = Integer.parseInt(iptext[i].getText(),10);
            }

            return new ConnectionData(getType(), (main==null)?false:main.isSelected(),
                                    ssl.getSelection().getActionCommand().equals("SSL")?ConnectionData.SSL_TCP:ConnectionData.RAW_TCP,
                                    nametext.getText(),
                                    ips,
                                    (iptext[4].getText().length()==0)?0:Integer.parseInt(iptext[4].getText(),10));
        }
        else
            return new ConnectionData(getType(), (main==null)?false:main.isSelected(),
                                    ssl.getSelection().getActionCommand().equals("SSL")?ConnectionData.SSL_UNIX:ConnectionData.RAW_UNIX,
                                    nametext.getText());
    }

    protected void addMain(final ConnectionData data, GridBagLayout layout, GridBagConstraints lc, GridBagConstraints rc)
    {
        rc.gridx--;
        rc.gridwidth = 8;
        if(data==null)
            main = new JCheckBox("Main Connection");
        else
            main = new JCheckBox("Main Connection",data.getIsMain());
        layout.setConstraints(main,rc);
        getContentPane().add(main);
        lc.gridy++;
        rc.gridy++;
        rc.gridx++;
        if(firstone==null)
            firstone = main;
    }

    protected void addTransport(final ConnectionData data, GridBagLayout layout, GridBagConstraints lc, GridBagConstraints rc)
    {
        JLabel label = new JLabel("Transport");
        layout.setConstraints(label, lc);
        getContentPane().add(label);
        lc.gridy+=2;

        int ttype;
        if(data==null)
            ttype = ConnectionData.RAW_TCP;
        else
            ttype = data.getTransport();
        rc.anchor = GridBagConstraints.CENTER;
        rc.gridwidth = 3;
        stype = new ButtonGroup();
        JRadioButton t = new JRadioButton("TCP",(ttype & ConnectionData.UNIX)==0);
        if(firstone==null)
            firstone = t;
        t.setActionCommand("TCP");
        ActionListener tcpunixswitcher = new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    boolean is_tcp = ev.getActionCommand().equals("TCP");
                    namelabel.setText(is_tcp?"Host Name":"File Name");
                    for(int i=0;i<5;i++)
                    {
                        iplabel[i].setEnabled(is_tcp);
                        iptext[i].setEnabled(is_tcp);
                    }
                }
            };

        t.addActionListener(tcpunixswitcher);
        layout.setConstraints(t,rc);
        getContentPane().add(t);
        stype.add(t);
        rc.gridy++;
        t = new JRadioButton("Unix",(ttype & ConnectionData.UNIX)!=0);
        t.setActionCommand("Unix");
        t.addActionListener(tcpunixswitcher);
        layout.setConstraints(t,rc);
        getContentPane().add(t);
        stype.add(t);

        rc.gridy--;
        rc.gridx+=3;
        rc.gridwidth=1;
        rc.gridheight=2;
        JSeparator vertLine = new JSeparator(JSeparator.VERTICAL);
        rc.fill=GridBagConstraints.VERTICAL;
        layout.setConstraints(vertLine,rc);
        rc.fill=GridBagConstraints.NONE;
        getContentPane().add(vertLine);

        rc.gridx++;
        rc.gridwidth=3;
        rc.gridheight=1;
        ssl = new ButtonGroup();
        t = new JRadioButton("Raw",(ttype & ConnectionData.SSL)==0);
        t.setActionCommand("Raw");
        layout.setConstraints(t,rc);
        getContentPane().add(t);
        ssl.add(t);
        rc.gridy++;
        t = new JRadioButton("SSL",(ttype & ConnectionData.SSL)!=0);
        t.setActionCommand("SSL");
        layout.setConstraints(t,rc);
        getContentPane().add(t);
        ssl.add(t);
        rc.gridy++;
        rc.gridx-=4;
    }

    protected final ActionListener nextfocusaction = new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                ((Component)evt.getSource()).transferFocus();
            }
        };

    protected void addName(final ConnectionData data, GridBagLayout layout, GridBagConstraints lc, GridBagConstraints rc)
    {
        boolean isHost = stype.getSelection().getActionCommand().equals("TCP");
        namelabel = new JLabel(isHost?"Host Name":"File Name");
        layout.setConstraints(namelabel, lc);
        getContentPane().add(namelabel);
        lc.gridy++;

        rc.anchor = GridBagConstraints.WEST;
        rc.gridwidth = 7;
        if(data==null)
            nametext = new JTextField(14);
        else
            nametext = new JTextField(String.valueOf(data.getName()),14);
        nametext.setMinimumSize(nametext.getPreferredSize());
        layout.setConstraints(nametext,rc);
        getContentPane().add(nametext);
        nametext.addActionListener(nextfocusaction);
        rc.gridy++;
        if(firstone==null)
            firstone = nametext;
    }

    protected void addIP(final ConnectionData data, GridBagLayout layout, GridBagConstraints lc, GridBagConstraints rc)
    {
        boolean isHost = stype.getSelection().getActionCommand().equals("TCP");
        iplabel = new JLabel[5];
        iptext = new JTextField[5];
        int[] ips;
        if(data!=null)
            ips = data.getIPAddr();
        else
            ips = null;
        GridBagConstraints ic=new GridBagConstraints();
        ic.anchor = GridBagConstraints.WEST;
        ic.insets = new Insets(1,5,1,1);
        ic.gridx = lc.gridx;
        ic.gridy = lc.gridy;
        ic.weightx = 0;
        for(int i=0;i<4;i++)
        {
            iplabel[i] = new JLabel((i==0)?"IP Address":".");
            layout.setConstraints(iplabel[i], (i==0)?lc:ic);
            getContentPane().add(iplabel[i]);
            iplabel[i].setEnabled(isHost);
            ic.gridx++;

            iptext[i] = new JTextField(3);
            iptext[i].setMinimumSize(iptext[i].getPreferredSize());
            iptext[i].setDocument(new IntegerDocument(255,iptext[i]));
            if(ips!=null)
                iptext[i].setText(Integer.toString(ips[i],10));
            layout.setConstraints(iptext[i],ic);
            getContentPane().add(iptext[i]);
            iptext[i].addActionListener(nextfocusaction);
            iptext[i].setEnabled(isHost);
            ic.gridx++;
            if(i==0)
                ic.insets.left=1;
        }
        lc.gridy++;
        rc.gridy++;
        if(firstone==null)
            firstone = iptext[0];
    }

    protected void addPort(final ConnectionData data, GridBagLayout layout, GridBagConstraints lc, GridBagConstraints rc)
    {
        boolean isHost = stype.getSelection().getActionCommand().equals("TCP");
        iplabel[4] = new JLabel("Port");
        layout.setConstraints(iplabel[4], lc);
        getContentPane().add(iplabel[4]);
        iplabel[4].setEnabled(isHost);
        lc.gridy++;

        rc.gridwidth = 7;
        iptext[4] = new JTextField(5);
        iptext[4].setMinimumSize(iptext[4].getPreferredSize());
        iptext[4].setDocument(new IntegerDocument(65535));
        if(isHost && data!=null)
            iptext[4].setText(String.valueOf(data.getPort()));
        layout.setConstraints(iptext[4],rc);
        getContentPane().add(iptext[4]);
        iptext[4].addActionListener(nextfocusaction);
        iptext[4].setEnabled(isHost);
        rc.gridy++;
        if(firstone==null)
            firstone = iptext[4];
    }

    protected void addKeys(final ConnectionData data, final ConnectionModel where, GridBagLayout layout, GridBagConstraints lc, GridBagConstraints rc)
    {
        GridBagLayout keylayout = new GridBagLayout();
        JPanel keys = new JPanel(keylayout);
        GridBagConstraints kc = new GridBagConstraints();
        final JDialog parent = this;
        kc.weightx=1;
        kc.gridx=0;
        kc.gridy=0;
        kc.gridwidth=1;
        kc.fill=GridBagConstraints.HORIZONTAL;
        kc.insets = new Insets(1,1,1,1);
        JButton key;
        if(data==null)
        {
            key = new JButton("Add");
            key.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ev)
                    {
                        if(stype.getSelection().getActionCommand().equals("TCP"))
                            for(int i=0;i<4;i++)
                                if(iptext[i].getText().length()==0)
                                {
                                    if(i==0)
                                        break;
                                    javax.swing.JOptionPane.showMessageDialog(parent,
                                            "IP Address is not complete.",
                                            "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                        where.addData(getData());
                        dispose();
                    }
                });
        }
        else
        {
            key = new JButton("Change");
            key.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ev)
                    {
                        if(stype.getSelection().getActionCommand().equals("TCP"))
                            for(int i=0;i<4;i++)
                                if(iptext[i].getText().length()==0)
                                {
                                    if(i==0)
                                        break;
                                    javax.swing.JOptionPane.showMessageDialog(parent,
                                            "IP Address is not complete.",
                                            "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                        where.changeData(getData(),data);
                        dispose();
                    }
                });
        }
        key.setActionCommand("Ok");
        keylayout.setConstraints(key,kc);
        keys.add(key);
        kc.gridx++;
        key = new JButton("Cancel");
        key.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    dispose();
                }
            });
        keylayout.setConstraints(key,kc);
        keys.add(key);
        if(firstone==null)
            firstone = key;
        lc.gridwidth=8;
        lc.fill=GridBagConstraints.HORIZONTAL;
        layout.setConstraints(keys,lc);
        getContentPane().add(keys);
        lc.gridy++;
    }

    ConnectionDialog(Frame parent, String title)
    {
        super(parent, title, false);
    }
}

class IncomingDialog extends ConnectionDialog
{
    protected String getType()
    {
        return "ListenerInterface";
    }

    private void createDialog(final ConnectionData data, final IncomingModel where)
    {
        setSize(500,350);

        GridBagLayout layout=new GridBagLayout();
        getContentPane().setLayout(layout);

        // Constraints for the labels
        GridBagConstraints lc=new GridBagConstraints();
        lc.anchor =GridBagConstraints.WEST;
        lc.insets = new Insets(5,5,5,5);
        lc.gridx = 0;
        lc.gridy = 0;
        lc.weightx = 1;

        // Constraints for all the other things...
        GridBagConstraints rc=new GridBagConstraints();
        rc.anchor = GridBagConstraints.WEST;
        rc.insets = new Insets(5,5,5,5);
        rc.gridx = 1;
        rc.gridy = 0;
        rc.weightx = 0;

//        addMain(data, layout, lc, rc);
        addTransport(data, layout, lc, rc);
        addName(data, layout, lc, rc);
        addIP(data, layout, lc, rc);
        addPort(data, layout, lc, rc);
        addKeys(data, where, layout, lc, rc);

        pack();
        firstone.requestFocus();
    }

    IncomingDialog(Frame parent, String title, final IncomingModel where)
    {
       super(parent,title);
       createDialog(null,where);
       this.setLocationRelativeTo(parent);
    }

    IncomingDialog(Frame parent, String title, final IncomingModel where, ConnectionData data)
    {
       super(parent,title);
       createDialog(data,where);
       this.setLocationRelativeTo(parent);
    }
}

class OutgoingDialog extends ConnectionDialog
{
    private ButtonGroup proxytype;

    protected String getType()
    {
        return (proxytype==null)?"NextMix":"Proxy";
    }

    protected ConnectionData getData()
    {
        ConnectionData data = super.getData();
        if(proxytype!=null)
            data.setFlags(proxytype.getSelection().getActionCommand().equals("HTTP")
                          ?ConnectionData.HTTP_PROXY:ConnectionData.SOCKS_PROXY);
        return data;
    }

    protected void addType(final ConnectionData data, GridBagLayout layout, GridBagConstraints lc, GridBagConstraints rc)
    {
        JLabel label = new JLabel("Proxy Type");
        layout.setConstraints(label, lc);
        getContentPane().add(label);
        lc.gridy++;

        int ptype;
        if(data==null)
            ptype = data.HTTP_PROXY;
        else
            ptype = data.getFlags() & data.PROXY_MASK;

        rc.anchor = GridBagConstraints.CENTER;
        rc.gridwidth = 3;
        proxytype = new ButtonGroup();
        JRadioButton t = new JRadioButton("HTTP",ptype!=data.SOCKS_PROXY);
        t.setActionCommand("HTTP");
        layout.setConstraints(t,rc);
        getContentPane().add(t);
        proxytype.add(t);
        if(firstone==null)
            firstone = t;

        rc.gridx+=4;
        t = new JRadioButton("Socks",ptype==data.SOCKS_PROXY);
        t.setActionCommand("Socks");
        layout.setConstraints(t,rc);
        getContentPane().add(t);
        proxytype.add(t);
        rc.gridy++;
        rc.gridx-=4;
    }

    private void createDialog(final ConnectionData data, final OutgoingModel where)
    {
        setSize(500,350);

        GridBagLayout layout=new GridBagLayout();
        getContentPane().setLayout(layout);

        // Constraints for the labels
        GridBagConstraints lc=new GridBagConstraints();
        lc.anchor =GridBagConstraints.WEST;
        lc.insets = new Insets(5,5,5,5);
        lc.gridx = 0;
        lc.gridy = 0;
        lc.weightx = 1;

        // Constraints for all the other things...
        GridBagConstraints rc=new GridBagConstraints();
        rc.anchor = GridBagConstraints.WEST;
        rc.insets = new Insets(5,5,5,5);
        rc.gridx = 1;
        rc.gridy = 0;
        rc.weightx = 0;

        if(MyFrame.m_GeneralPanel.getMixType().equals("LastMix"))
            addType(data, layout, lc, rc);
        else
            proxytype = null;
        addTransport(data, layout, lc, rc);
        addName(data, layout, lc, rc);
        addIP(data, layout, lc, rc);
        addPort(data, layout, lc, rc);
        addKeys(data, where, layout, lc, rc);

        pack();
        firstone.requestFocus();
    }

    OutgoingDialog(Frame parent, String title, final OutgoingModel where)
    {
       super(parent,title);
       createDialog(null,where);
       this.setLocationRelativeTo(parent);
    }

    OutgoingDialog(Frame parent, String title, final OutgoingModel where, ConnectionData data)
    {
       super(parent,title);
       createDialog(data,where);
       this.setLocationRelativeTo(parent);
    }
}

abstract class ConnectionModel extends AbstractTableModel
{
    private ConnectionData[] rows = new ConnectionData[0];

    public int getRowCount()
    {
        return rows.length;
    }

    public boolean hasMain(int row)
    {
        return true;
    }

    public boolean isCellEditable(int row, int col)
    {
        return (col==1) && hasMain(row);
    }

    public void setMain(boolean m, int row)
    {
        int i;

        ConnectionData data = getData(row);
        if(data==null || data.getIsMain()==m)
            return;
        data.setIsMain(m);
        fireTableRowsUpdated(row, row);
        if(m)
        {
            for(i=0;i<rows.length;i++)
                if(i!=row && hasMain(i) && rows[i].getIsMain())
                {
                    rows[i].setIsMain(false);
                    fireTableRowsUpdated(i, i);
                }
        }
        else
            for(i=0;i<rows.length;i++)
                if(i!=row && hasMain(i))
                {
                    if(!rows[i].getIsMain())
                    {
                        rows[i].setIsMain(true);
                        fireTableRowsUpdated(i,i);
                    }
                    return;
                }
    }

    public void setValueAt(Object ob, int row, int col)
    {
        if(col!=1 || !(ob instanceof Boolean))
            return;
        setMain(((Boolean)ob).booleanValue(), row);
    }

    void addData(ConnectionData data)
    {
        boolean newMain = data.getIsMain();
        ConnectionData[] nrows = new ConnectionData[rows.length+1];
        for(int i=0;i<rows.length;i++)
        {
            nrows[i] = rows[i];
            if(newMain && nrows[i].getIsMain())
            {
                nrows[i].setIsMain(false);
                fireTableRowsUpdated(i,i);
            }
        }
        nrows[rows.length] = data;
        rows = nrows;
        if(nrows.length==1)
            data.setIsMain(true);
        fireTableRowsInserted(rows.length-1,rows.length-1);
    }

    ConnectionData getData(int index)
    {
        if(index<0 || index>=rows.length)
            return null;
        return rows[index];
    }

    void changeData(ConnectionData data, ConnectionData olddata)
    {
        for(int i=0;i<rows.length;i++)
            if(rows[i]==olddata)
            {
                rows[i]=data;
                data.setIsMain(olddata.getIsMain());
                fireTableRowsUpdated(i,i);
                return;
            }
        addData(data);
    }

    void deleteData(int index)
    {
        if(index>=0 && index<rows.length)
        {
            ConnectionData[] nrows = new ConnectionData[rows.length-1];
            setMain(false, index);
            for(int i=0;i<index;i++)
                nrows[i] = rows[i];
            for(int i=index+1;i<rows.length;i++)
                nrows[i-1] = rows[i];
            rows = nrows;
            fireTableRowsDeleted(index,index);
        }
    }

    void clear()
    {
        int old = rows.length;
        rows = new ConnectionData[0];
        fireTableRowsDeleted(1,old);
    }
}

class IncomingModel extends ConnectionModel
{
    private static final String[] columnNames =
            {"No.", "Main", "Transport", "Host / FileName",
             "IP Address", "Port" };

    public static final int SERIAL_NR = 0;
    public static final int IS_MAIN = 1;
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
            case IS_MAIN: return new Boolean(data.getIsMain());
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
            case IS_MAIN: return Boolean.class;
            case NAME: return String.class;
            case PORT: return Integer.class;
            // Transport und IP-Addresse muessen wir in der Tabelle
            // gesondert behandeln.
        }
        return Object.class;
    }

    public int getColumnCount()
    {
        return 6;
    }

    public String getColumnName(int col)
    {
        return columnNames[col];
    }

    org.w3c.dom.Element createAsElement(org.w3c.dom.Document doc)
    {
        org.w3c.dom.Element ifaces = doc.createElement("ListenerInterfaces");
        for(int i=0;i<getRowCount();i++)
            ifaces.appendChild(getData(i).createAsElement(doc));
        return ifaces;
    }

    void readFromElement(org.w3c.dom.Element iface)
    {
        if(iface.getTagName().equals("ListenerInterfaces"))
        {
            for(int i=getRowCount()-1;i>=0;i--)
                deleteData(i);
            org.w3c.dom.Node child = iface.getFirstChild();
            while(child!=null)
            {
                if(child.getNodeType() == child.ELEMENT_NODE)
                {
                    ConnectionData data = ConnectionData.createFromElement(
                            "ListenerInterface", (org.w3c.dom.Element)child);
                    if(data!=null)
                        addData(data);
                }
                child = child.getNextSibling();
            }
        }
    }
}

class OutgoingModel extends ConnectionModel
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

    public boolean hasMain(int row)
    {
        return false;
      // return (getData(row)!=null && getData(row).getType().equals("Proxy"));
    }

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
                    switch(data.getFlags()&data.PROXY_MASK)
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

    org.w3c.dom.Element createProxiesAsElement(org.w3c.dom.Document doc)
    {
        org.w3c.dom.Element proxies = doc.createElement("Proxies");
        for(int i=0;i<getRowCount();i++)
            if(getData(i).getType().equals("Proxy"))
            {
                org.w3c.dom.Element proxy = getData(i).createAsElement(doc);
                proxy.removeAttribute("main");
                proxies.appendChild(proxy);
            }
        return proxies;
    }

    org.w3c.dom.Element createMixAsElement(org.w3c.dom.Document doc)
    {
        for(int i=0;i<getRowCount();i++)
            if(getData(i).getType().equals("NextMix"))
            {
                org.w3c.dom.Element mix = getData(i).createAsElement(doc);
                mix.removeAttribute("main");
                return mix;
            }
        return null;
    }

    void readFromElement(org.w3c.dom.Element out)
    {
        if(out.getTagName().equals("Proxies"))
        {
            for(int i=getRowCount()-1;i>=0;i--)
                if(getData(i).getType().equals("Proxy"))
                    deleteData(i);
            org.w3c.dom.Node child = out.getFirstChild();
            while(child!=null)
            {
                if(child.getNodeType() == child.ELEMENT_NODE)
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

class NetworkPanel extends JPanel
{
   JPanel panel1,panel2,panel3;
   JTable table1,table2;
   JTextField Host_Text,Port_Text;
   IPTextField IP_Text;
   IncomingModel imodel;
   OutgoingModel omodel;

   public IncomingModel getIncomingModel()
   {
       return imodel;
   }

   public OutgoingModel getOutgoingModel()
   {
       return omodel;
   }

   public String getHost()
   {
     return Host_Text.getText();
   }

   public String getIP()
   {
       return IP_Text.getText();
   }

   public String getPort()
   {
     return Port_Text.getText();
   }

   public void setInfoHost(String info)
   {
       Host_Text.setText(info);
   }

   public void setInfoIP(String info)
   {
       IP_Text.setText(info);
   }

   public void setInfoPort(String info)
   {
     Port_Text.setText(info);
   }

  public NetworkPanel()
  {
    GridBagLayout layout=new GridBagLayout();
    setLayout(layout);

    GridBagLayout In_Layout = new GridBagLayout();
    GridBagLayout Out_Layout = new GridBagLayout();
    GridBagLayout Info_Layout = new GridBagLayout();

    GridBagConstraints c=new GridBagConstraints();
    c.anchor=GridBagConstraints.NORTHWEST;
    c.insets=new Insets(10,10,10,10);
    c.fill=GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1;
    c.weighty = 1;
    panel1 = new JPanel(In_Layout);
    panel1.setBorder(new TitledBorder("Incoming"));
    layout.setConstraints(panel1,c);
    add(panel1);

    int[] columnSizes1 = {15, 25, 60, 170, 110, 40};
    // table1 = new JTable(data1, columnNames1);

    final TableCellRenderer IPRenderer = new DefaultTableCellRenderer()
    {
        protected void setValue(Object v)
        {
            if(v==null)
                super.setValue("");
            else
            {
                int[] ips = (int[])v;
                super.setValue(ips[0]+"."+ips[1]+"."+ips[2]+"."+ips[3]);
                setHorizontalAlignment(CENTER);
            }
        }
    };
    final TableCellRenderer PortRenderer = new DefaultTableCellRenderer()
    {
        protected void setValue(Object v)
        {
            int t = ((Integer)v).intValue();
            if(t<=0)
                super.setValue("");
            else
            {
                setHorizontalAlignment(CENTER);
                super.setValue(v.toString());
            }
        }
    };
    final TableCellRenderer transportRenderer = new DefaultTableCellRenderer()
    {
        protected void setValue(Object v)
        {
            int t = ((Integer)v).intValue();
            super.setValue(
                    (((t&ConnectionData.SSL)==0)?"Raw/":"SSL/")+
                    (((t&ConnectionData.UNIX)==0)?"TCP":"Unix"));
            setHorizontalAlignment(CENTER);
        }
    };
    final TableCellRenderer centeringRenderer = new DefaultTableCellRenderer()
    {
        protected void setValue(Object v)
        {
            super.setValue(v);
            setHorizontalAlignment(CENTER);
        }
    };
    final TableCellRenderer emptyRenderer = new DefaultTableCellRenderer()
    {
        protected void setValue(Object v)
        {
            super.setValue("");
        }
    };

    imodel = new IncomingModel();
    imodel.addTableModelListener(new TableModelListener()
        {
            public void tableChanged(TableModelEvent e)
            {
                MyFrame.m_GeneralPanel.updateMixId();
            }
        });

    table1 = new JTable(imodel)
    {
        public TableCellRenderer getCellRenderer(int row, int column)
        {
            switch(column)
            {
                case IncomingModel.TRANSPORT:
                    return transportRenderer;
                case IncomingModel.IP_ADDR:
                    return IPRenderer;
                case IncomingModel.PORT:
                    return PortRenderer;
                default:
                    return super.getCellRenderer(row, column);
            }
        }
    };

    JScrollPane scrollPane1 = new JScrollPane(table1,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // Man kann nur eine Zeile selektieren
    table1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    table1.setPreferredScrollableViewportSize(new Dimension(450,90));

    // table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    for(int Index=0; Index<columnSizes1.length;Index++)
    {
        TableColumn column = table1.getColumnModel().getColumn(Index);
        column.setPreferredWidth(columnSizes1[Index]);
        // Die Spalten kann der Nutzer ruhig anpassen, wenn eine
        // Spalte zu klein ist (z.B. aufgrund anderer Schriftarten)
        // column.setMinWidth(columnSizes1[Index]);
        // column.setMaxWidth(columnSizes1[Index]);
    }

    GridBagConstraints d=new GridBagConstraints();
    d.anchor=GridBagConstraints.CENTER;
    d.insets=new Insets(10,10,10,10);
    d.gridx = 0;
    d.gridy = 0;
    d.weightx = 1;
    d.weighty = 1;
    d.gridheight = 3;
    d.fill = GridBagConstraints.BOTH;
    In_Layout.setConstraints(scrollPane1,d);
    panel1.add(scrollPane1);

    for(int Nr=0;Nr<3;Nr++)
    {
        JButton InButton;
        switch (Nr)
        {
            case 0: InButton = new JButton("Add");
                InButton.setActionCommand("Add");
                InButton.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent a)
                        {
                            if(a.getActionCommand().equals("Add"))
                            {
                                IncomingDialog dialog = new IncomingDialog(TheApplet.getMainWindow(),"Add",imodel);
                                dialog.show();
                            }
                        }
                    });
                break;
            case 1: final JButton cb = new JButton("Change");
                InButton = cb;
                table1.getSelectionModel().addListSelectionListener(new ListSelectionListener()
                    {
                        public void valueChanged(ListSelectionEvent e)
                        {
                            if(e.getValueIsAdjusting())
                                return;
                            cb.setEnabled(!((ListSelectionModel) e.getSource()).isSelectionEmpty());
                        }
                    });
                cb.setEnabled(false);
                cb.setActionCommand("Change");
                cb.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent a)
                        {
                            if(a.getActionCommand().equals("Change"))
                            {
                                IncomingDialog dialog = new IncomingDialog(TheApplet.getMainWindow(),"Change",imodel,
                                        ((IncomingModel)table1.getModel()).getData(table1.getSelectedRow()));
                                dialog.show();
                            }
                        }
                    });
                break;
            case 2: final JButton db = new JButton("Delete");
                InButton = db;
                table1.getSelectionModel().addListSelectionListener(new ListSelectionListener()
                    {
                        public void valueChanged(ListSelectionEvent e)
                        {
                            if(e.getValueIsAdjusting())
                                return;
                            db.setEnabled(!((ListSelectionModel) e.getSource()).isSelectionEmpty());
                        }

                    });
                db.setEnabled(false);
                db.setActionCommand("Delete");
                db.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent a)
                        {
                            if(a.getActionCommand().equals("Delete"))
                            {
                                ((IncomingModel)table1.getModel()).deleteData(table1.getSelectedRow());
                            }
                        }
                    });
                break;
            default:
                throw(new RuntimeException("Unknown Button should be created."));
        }
        GridBagConstraints ibd = new GridBagConstraints();
        ibd.anchor=GridBagConstraints.NORTHWEST;
        ibd.insets = new Insets(10,10,10,10);
        ibd.gridx = 1;
        ibd.gridy = Nr;
        ibd.weightx = 0.1;
        ibd.weighty = 0.2;
        ibd.fill = GridBagConstraints.HORIZONTAL;
        In_Layout.setConstraints(InButton, ibd);
        panel1.add(InButton);
    }

    // Now the outgoing connections
    c.gridx = 0;
    c.gridy = 1;
    panel2 = new JPanel(Out_Layout);
    panel2.setBorder(new TitledBorder("Outgoing"));
    layout.setConstraints(panel2,c);
    add(panel2);

    int[] columnSizes2 = {15, 70, 60, 125, 110, 40};
    omodel = new OutgoingModel();
    table2 = new JTable(omodel)
    {
        public TableCellRenderer getCellRenderer(int row, int column)
        {
            switch(column)
            {
                case OutgoingModel.TYPE:
                    return centeringRenderer;
                case OutgoingModel.TRANSPORT:
                    return transportRenderer;
                case OutgoingModel.IP_ADDR:
                    return IPRenderer;
                case OutgoingModel.PORT:
                    return PortRenderer;
                default:
                    return super.getCellRenderer(row, column);
            }
        }
    };

    JScrollPane scrollPane2 = new JScrollPane(table2,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // Man kann nur eine Zeile selektieren
    table2.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    table2.setPreferredScrollableViewportSize(new Dimension(450,90));

    for(int Index=0; Index<columnSizes2.length;Index++)
    {
        TableColumn column = table2.getColumnModel().getColumn(Index);
        column.setPreferredWidth(columnSizes2[Index]);
        // Die Spalten kann der Nutzer ruhig anpassen, wenn eine
        // Spalte zu klein ist (z.B. aufgrund anderer Schriftarten)
        // column.setMinWidth(columnSizes1[Index]);
        // column.setMaxWidth(columnSizes1[Index]);
    }

    d.anchor=GridBagConstraints.CENTER;
    d.insets=new Insets(10,10,10,10);
    d.gridx = 0;
    d.gridy = 0;
    d.weightx = 1;
    d.weighty = 1;
    d.gridheight = 3;
    d.fill = GridBagConstraints.BOTH;
    Out_Layout.setConstraints(scrollPane2,d);
    panel2.add(scrollPane2);

    for(int Nr=0;Nr<3;Nr++)
    {
        JButton OutButton;
        switch (Nr)
        {
            case 0: final JButton ob = new JButton("Add");
                OutButton = ob;
                omodel.addTableModelListener(new TableModelListener()
                    {
                        public void tableChanged(TableModelEvent e)
                        {
                            if(!MyFrame.m_GeneralPanel.getMixType().equals("LastMix"))
                                ob.setEnabled(omodel.getRowCount()==0);
                            else
                                ob.setEnabled(true);
                        }
                    });
                OutButton.setActionCommand("Add");
                OutButton.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent a)
                        {
                            if(a.getActionCommand().equals("Add"))
                            {
                                OutgoingDialog dialog = new OutgoingDialog(TheApplet.getMainWindow(),
                                        (MyFrame.m_GeneralPanel.getMixType().equals("LastMix"))?"Add Proxy":"Add Next Mix",
                                        omodel);
                                dialog.show();
                            }
                        }
                    });
                break;
            case 1: final JButton cb = new JButton("Change");
                OutButton = cb;
                table2.getSelectionModel().addListSelectionListener(new ListSelectionListener()
                    {
                        public void valueChanged(ListSelectionEvent e)
                        {
                            cb.setEnabled(!((ListSelectionModel) e.getSource()).isSelectionEmpty());
                        }
                    });
                cb.setEnabled(false);
                cb.setActionCommand("Change");
                cb.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent a)
                        {
                            if(a.getActionCommand().equals("Change"))
                            {
                                OutgoingDialog dialog = new OutgoingDialog(TheApplet.getMainWindow(),
                                        (MyFrame.m_GeneralPanel.getMixType().equals("LastMix"))?"Change Proxy":"Change Next Mix",
                                        omodel,
                                        ((OutgoingModel)table2.getModel()).getData(table2.getSelectedRow()));
                                dialog.show();
                            }
                        }
                    });
                break;
            case 2: final JButton db = new JButton("Delete");
                OutButton = db;
                table2.getSelectionModel().addListSelectionListener(new ListSelectionListener()
                    {
                        public void valueChanged(ListSelectionEvent e)
                        {
                            db.setEnabled(!((ListSelectionModel) e.getSource()).isSelectionEmpty());
                        }

                    });
                db.setEnabled(false);
                db.setActionCommand("Delete");
                db.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent a)
                        {
                            if(a.getActionCommand().equals("Delete"))
                            {
                                ((OutgoingModel)table2.getModel()).deleteData(table2.getSelectedRow());
                            }
                        }
                    });
                break;
            default:
                throw(new RuntimeException("Unknown Button should be created."));
        }
        GridBagConstraints ibd = new GridBagConstraints();
        ibd.anchor=GridBagConstraints.NORTHWEST;
        ibd.insets = new Insets(10,10,10,10);
        ibd.gridx = 1;
        ibd.gridy = Nr;
        ibd.weightx = 0.1;
        ibd.weighty = 0.2;
        ibd.fill = GridBagConstraints.HORIZONTAL;
        Out_Layout.setConstraints(OutButton, ibd);
        panel2.add(OutButton);
    }

    c.gridx = 0;
    c.gridy = 2;
    c.weighty=0;
    panel3 = new JPanel(Info_Layout);
    panel3.setBorder(new TitledBorder("Info Service"));
    layout.setConstraints(panel3,c);
    add(panel3);

    GridBagConstraints f=new GridBagConstraints();
    f.anchor=GridBagConstraints.NORTHWEST;
    f.insets=new Insets(10,10,10,10);
    f.fill = GridBagConstraints.HORIZONTAL;

    JLabel host = new JLabel("Host");
    f.gridx = 0;
    f.gridy = 0;
    Info_Layout.setConstraints(host,f);
    panel3.add(host);
    Host_Text = new JTextField(38);
    Host_Text.setText("");
    f.gridx = 1;
    f.weightx = 1;
    Info_Layout.setConstraints(Host_Text,f);
    panel3.add(Host_Text);

    JLabel IP = new JLabel("IP");
    f.gridy = 1;
    f.gridx = 0;
    f.weightx = 0;
    Info_Layout.setConstraints(IP,f);
    panel3.add(IP);
    IP_Text = new IPTextField();
    IP_Text.setText("");
    f.gridx = 1;
    f.weightx = 0;
    f.fill = f.NONE;
    Info_Layout.setConstraints(IP_Text,f);
    panel3.add(IP_Text);

    JLabel port = new JLabel("Port");
    f.gridy = 2;
    f.gridx = 0;
    f.weightx = 0;
    Info_Layout.setConstraints(port,f);
    panel3.add(port);
    Port_Text = new JTextField(5);
    Port_Text.setText("");
    Port_Text.setDocument(new IntegerDocument(65535));
    f.gridx = 1;
    Info_Layout.setConstraints(Port_Text,f);
    panel3.add(Port_Text);
    }

    public void clear()
    {
        imodel.clear();
        omodel.clear();
        Host_Text.setText("");
        setInfoIP("");
        Port_Text.setText("");
    }
}
