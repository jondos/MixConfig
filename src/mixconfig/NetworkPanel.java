/**
 * TODO:
 * - Automatically suggest a serial number
 * - Do the same for outgoing connections
 **/
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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

class IncomingData
{
    private int serial;
    private boolean main;
    private int transport;
    private String name;
    private int[] ipaddr;
    private int port;

    public static final int TCP = 0;
    public static final int UNIX = 1;
    public static final int RAW = 0;
    public static final int SSL = 2;
    public static final int RAW_TCP = RAW|TCP;
    public static final int RAW_UNIX = RAW|UNIX;
    public static final int SSL_TCP = SSL|TCP;
    public static final int SSL_UNIX = SSL|UNIX;

    void setSerialNr(int s)
    {
        serial = s;
    }

    int getSerialNr()
    {
        return serial;
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

    IncomingData(int s, boolean m, int t, String n, int[] addr, int p)
    {
        setSerialNr(s);
        setIsMain(m);
        setTransport(t);
        setName(n);
        setIPAddr(addr);
        setPort(p);
    }

    IncomingData(int s, boolean m, int t, String n)
    {
        setSerialNr(s);
        setIsMain(m);
        setTransport(t);
        setName(n);
        ipaddr = null;
        setPort(0);
    }

    IncomingData(int s, boolean m, int t, String n, String addr, int p)
    {
        setSerialNr(s);
        setIsMain(m);
        setTransport(t);
        setName(n);
        setIPAddr(addr);
        setPort(p);
    }

    IncomingData deepClone()
    {
        return new IncomingData(serial, main, transport, name, ipaddr, port);
    }

    org.w3c.dom.Element createAsElement(org.w3c.dom.Document doc)
    {
        org.w3c.dom.Element iface = doc.createElement("ListenerInterface");
        iface.setAttribute("main",main?"True":"False");
        iface.setAttribute("nr", Integer.toString(serial));
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

    static IncomingData createFromElement(org.w3c.dom.Element iface)
    {

        if(!iface.getTagName().equals("ListenerInterface"))
            return null;

        int ser;
        boolean m;
        int trans;
        String n, ip=null, data;
        int p=0;

        m = iface.getAttribute("main").equalsIgnoreCase("True");
        try {ser = Integer.parseInt(iface.getAttribute("nr"));}
        catch(NumberFormatException e) {ser = 0;}
        data = elementData(iface, "Type");
        if(data.equalsIgnoreCase("RAW/UNIX"))
            trans = RAW_UNIX;
        else if(data.equalsIgnoreCase("RAW/TCP"))
            trans = RAW_TCP;
        else if(data.equalsIgnoreCase("SSL/UNIX"))
            trans = SSL_UNIX;
        else if(data.equalsIgnoreCase("SSL/TCP"))
            trans = SSL_TCP;
        else
            return null;
        if((trans&UNIX)==0)
        {
            n = elementData(iface, "Host");
            ip = elementData(iface, "IP");
            data = elementData(iface, "Port");
            if(data==null)
                return null;
            p = Integer.parseInt(data);
        }
        else
        {
            n = elementData(iface, "File");
        }
        if(n==null)
            return null;
        return new IncomingData(ser, m, trans, n, ip, p);
    }
}

class IncomingDialog extends JDialog
{
    /**
     *  A document that accepts only non-negatives.
     */
    protected class IntegerDocument extends PlainDocument
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

        public void insertString(int offset, String str, AttributeSet attr)
                throws BadLocationException
        {
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

    private JTextField serial, nametext, iptext[];
    private JCheckBox main;
    private ButtonGroup ssl, stype;
    private JLabel namelabel, iplabel[];

    private IncomingData getData()
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

            return new IncomingData((serial.getText().length()==0)?0:Integer.parseInt(serial.getText(),10),
                                    main.isSelected(),
                                    ssl.getSelection().getActionCommand().equals("SSL")?IncomingData.SSL_TCP:IncomingData.RAW_TCP,
                                    nametext.getText(),
                                    ips,
                                    (iptext[4].getText().length()==0)?0:Integer.parseInt(iptext[4].getText(),10));
        }
        else
            return new IncomingData(Integer.parseInt(serial.getText(),10),
                                    main.isSelected(),
                                    ssl.getSelection().getActionCommand().equals("SSL")?IncomingData.SSL_UNIX:IncomingData.RAW_UNIX,
                                    nametext.getText());
    }

    private void createDialog(final IncomingData data, final IncomingModel where)
    {
        setSize(500,350);

        GridBagLayout layout=new GridBagLayout();
        getContentPane().setLayout(layout);

        GridBagConstraints lc=new GridBagConstraints();
        lc.anchor =GridBagConstraints.WEST;
        lc.insets = new Insets(5,5,5,5);
        lc.gridx = 0;
        lc.gridy = 0;
        lc.weightx = 1;
        JLabel label = new JLabel("Serial Number");
        layout.setConstraints(label,lc);
        getContentPane().add(label);


        GridBagConstraints rc=new GridBagConstraints();
        rc.anchor = GridBagConstraints.WEST;
        rc.insets = new Insets(5,5,5,5);
        rc.gridx = 1;
        rc.gridy = 0;
        rc.weightx = 0;
        rc.gridwidth = 7;
        serial = new JTextField(14);
        serial.setMinimumSize(serial.getPreferredSize());
        serial.setDocument(new IntegerDocument());
        if(data!=null)
            serial.setText(String.valueOf(data.getSerialNr()));
        layout.setConstraints(serial,rc);
        getContentPane().add(serial);

        ActionListener nextfocusaction = new ActionListener()
            {
                public void actionPerformed(ActionEvent evt)
                {
                    ((Component)evt.getSource()).transferFocus();
                }
            };
        serial.addActionListener(nextfocusaction);

        rc.gridx = 8;
        rc.gridwidth = 1;
        if(data==null)
            main = new JCheckBox("Main");
        else
            main = new JCheckBox("Main",data.getIsMain());
        layout.setConstraints(main,rc);
        getContentPane().add(main);

        lc.gridy++;
        label = new JLabel("Transport");
        layout.setConstraints(label, lc);
        getContentPane().add(label);

        int ttype;
        if(data==null)
            ttype = IncomingData.RAW_TCP;
        else
            ttype = data.getTransport();
        rc.gridy++;
        rc.gridx = 1;
        rc.gridwidth = 3;
        ssl = new ButtonGroup();
        JRadioButton t = new JRadioButton("Raw",(ttype & IncomingData.SSL)==0);
        t.setActionCommand("Raw");
        layout.setConstraints(t,rc);
        getContentPane().add(t);
        ssl.add(t);
        rc.gridy++;
        t = new JRadioButton("SSL",(ttype & IncomingData.SSL)!=0);
        t.setActionCommand("SSL");
        layout.setConstraints(t,rc);
        getContentPane().add(t);
        ssl.add(t);

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
        rc.gridy--;
        rc.gridx+=4;
        stype = new ButtonGroup();
        t = new JRadioButton("TCP",(ttype & IncomingData.UNIX)==0);
        t.setActionCommand("TCP");
        t.addActionListener(tcpunixswitcher);
        layout.setConstraints(t,rc);
        getContentPane().add(t);
        stype.add(t);
        rc.gridy++;
        t = new JRadioButton("Unix",(ttype & IncomingData.UNIX)!=0);
        t.setActionCommand("Unix");
        t.addActionListener(tcpunixswitcher);
        layout.setConstraints(t,rc);
        getContentPane().add(t);
        stype.add(t);

        boolean isHost = ((ttype&IncomingData.UNIX)==0);
        lc.gridy+=2;
        namelabel = new JLabel(isHost?"Host Name":"File Name");
        layout.setConstraints(namelabel, lc);
        getContentPane().add(namelabel);

        rc.gridx=1;
        rc.gridy++;
        rc.gridwidth = 7;
        if(data==null)
            nametext = new JTextField(14);
        else
            nametext = new JTextField(String.valueOf(data.getName()),14);
        nametext.setMinimumSize(nametext.getPreferredSize());
        layout.setConstraints(nametext,rc);
        getContentPane().add(nametext);
        nametext.addActionListener(nextfocusaction);

        iplabel = new JLabel[5];
        iptext = new JTextField[5];
        int[] ips;
        if(data!=null)
            ips = data.getIPAddr();
        else
            ips = null;
        lc.gridy++;
        GridBagConstraints ic=new GridBagConstraints();
        ic.anchor = GridBagConstraints.WEST;
        ic.insets = new Insets(1,5,1,1);
        ic.gridx = 0;
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
        iplabel[4] = new JLabel("Port");
        layout.setConstraints(iplabel[4], lc);
        getContentPane().add(iplabel[4]);
        iplabel[4].setEnabled(isHost);

        rc.gridx=1;
        rc.gridy+=2;
        rc.gridwidth = 7;
        iptext[4] = new JTextField(5);
        iptext[4].setMinimumSize(iptext[4].getPreferredSize());
        iptext[4].setDocument(new IntegerDocument(65535));
        if(data!=null)
            iptext[4].setText(String.valueOf(data.getPort()));
        layout.setConstraints(iptext[4],rc);
        getContentPane().add(iptext[4]);
        iptext[4].addActionListener(nextfocusaction);
        iptext[4].setEnabled(isHost);

        GridBagLayout keylayout = new GridBagLayout();
        JPanel keys = new JPanel(keylayout);
        rc.weightx=1;
        rc.gridx=0;
        rc.gridy=0;
        rc.gridwidth=1;
        rc.fill=GridBagConstraints.HORIZONTAL;
        rc.insets = new Insets(1,1,1,1);
        JButton key;
        if(data==null)
        {
            key = new JButton("Add");
            key.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ev)
                    {
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
                        where.changeData(getData(),data);
                        dispose();
                    }
                });
        }
        key.setActionCommand("Ok");
        keylayout.setConstraints(key,rc);
        keys.add(key);
        rc.gridx++;
        key = new JButton("Cancel");
        key.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ev)
                {
                    dispose();
                }
            });
        keylayout.setConstraints(key,rc);
        keys.add(key);
        lc.gridy++;
        lc.gridwidth=9;
        lc.fill=GridBagConstraints.HORIZONTAL;
        layout.setConstraints(keys,lc);
        getContentPane().add(keys);
        pack();
        serial.requestFocus();
    }

    IncomingDialog(JFrame parent, String title, final IncomingModel where)
    {
       super(parent,title,false);
       createDialog(null,where);
       this.setLocationRelativeTo(parent);
    }

    IncomingDialog(JFrame parent, String title, final IncomingModel where, IncomingData data)
    {
       super(parent,title,false);
       createDialog(data,where);
       this.setLocationRelativeTo(parent);
    }
}

class IncomingModel extends AbstractTableModel
{
    private static final String[] columnNames =
            {"Serial No.", "Main", "Transport", "Host / FileName",
             "IP Address", "Port" };

    public static final int SERIAL_NR = 0;
    public static final int IS_MAIN = 1;
    public static final int TRANSPORT = 2;
    public static final int NAME = 3;
    public static final int IP_ADDR = 4;
    public static final int PORT = 5;

    private IncomingData[] rows = new IncomingData[0];

    public Object getValueAt(int row, int column)
    {
        if(row>=rows.length || row<0)
            return null;
        switch(column)
        {
            case SERIAL_NR: return new Integer(rows[row].getSerialNr());
            case IS_MAIN: return new Boolean(rows[row].getIsMain());
            case TRANSPORT: return new Integer(rows[row].getTransport());
            case NAME: return rows[row].getName();
            case IP_ADDR: return rows[row].getIPAddr();
            case PORT: return new Integer(rows[row].getPort());
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

    public int getRowCount()
    {
        return rows.length;
    }

    public boolean isCellEditable(int row, int col)
    {
        return false;
    }

    public String getColumnName(int col)
    {
        return columnNames[col];
    }

    // Und nun selbst ausgedachte Funktionen:
    void addData(IncomingData data)
    {
        boolean newMain = data.getIsMain();
        IncomingData[] nrows = new IncomingData[rows.length+1];
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

    IncomingData getData(int index)
    {
        if(index<0 || index>=rows.length)
            return null;
        return rows[index];
    }

    void changeData(IncomingData data, IncomingData olddata)
    {
        for(int i=0;i<rows.length;i++)
            if(rows[i]==olddata)
            {
                rows[i]=data;
                if(data.getIsMain() && !olddata.getIsMain())
                {
                    for(int j=0;j<rows.length;j++)
                        if(j!=i && rows[j].getIsMain())
                        {
                            rows[j].setIsMain(false);
                            fireTableRowsUpdated(j,j);
                        }
                }
                else if(!data.getIsMain() && olddata.getIsMain())
                {
                    if(i==0 && rows.length>1)
                    {
                        rows[1].setIsMain(true);
                        fireTableRowsUpdated(1,1);
                    }
                    else
                    {
                        rows[0].setIsMain(true);
                        fireTableRowsUpdated(0,0);
                    }
                }
                fireTableRowsUpdated(i,i);
                return;
            }
        addData(data);
    }

    void deleteData(int index)
    {
        if(index>=0 && index<rows.length)
        {
            IncomingData[] nrows = new IncomingData[rows.length-1];
            boolean wasMain = rows[index].getIsMain();
            for(int i=0;i<index;i++)
                nrows[i] = rows[i];
            for(int i=index+1;i<rows.length;i++)
                nrows[i-1] = rows[i];
            rows = nrows;
            fireTableRowsDeleted(index,index);
            if(wasMain && rows.length>0)
            {
                rows[0].setIsMain(true);
                fireTableRowsUpdated(0,0);
            }
        }
    }
    org.w3c.dom.Element createAsElement(org.w3c.dom.Document doc)
    {
        org.w3c.dom.Element ifaces = doc.createElement("ListenerInterfaces");
        for(int i=0;i<rows.length;i++)
            ifaces.appendChild(rows[i].createAsElement(doc));
        return ifaces;
    }

    void readFromElement(org.w3c.dom.Element iface)
    {
        if(iface.getTagName().equals("ListenerInterfaces"))
        {
            rows = new IncomingData[0];
            org.w3c.dom.Node child = iface.getFirstChild();
            while(child!=null)
            {
                if(child.getNodeType() == child.ELEMENT_NODE)
                {
                    IncomingData data = IncomingData.createFromElement((org.w3c.dom.Element)child);
                    if(data!=null)
                        addData(data);
                }
                child = child.getNextSibling();
            }
        }
    }
}

class NetworkPanel extends JPanel
{
   JPanel panel1,panel2,panel3;
   JTable table1,table2;
   JTextField Host_Text,IP_Text,Port_Text;
   IncomingModel imodel;

   public IncomingModel getIncomingModel()
   {
       return imodel;
   }

   public String getTable1(int x,int y)
   {
     Object o=table1.getValueAt(x,y);
     if(o==null)
      return "";
     return o.toString();
   }

   public String getTable2(int x,int y)
   {
    Object o=table2.getValueAt(x,y);
     if(o==null)
      return "";
     return o.toString();
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

   public void setTable1(String text,int x,int y)
   {
     table1.setValueAt(text,x,y);
   }

   public void setTable2(String text,int x,int y)
   {
     table2.setValueAt(text,x,y);
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

    int[] columnSizes1 = {70, 50, 75, 110, 85, 65};
    // table1 = new JTable(data1, columnNames1);

    imodel = new IncomingModel();
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
            }
        }
    };
    final TableCellRenderer PortRenderer = new DefaultTableCellRenderer()
    {
        protected void setValue(Object v)
        {
            int t = ((Integer)v).intValue();
            if(t<0)
                super.setValue("");
            else
            {
                super.setHorizontalAlignment(RIGHT);
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
                    (((t&IncomingData.SSL)==0)?"Raw, ":"SSL, ")+
                    (((t&IncomingData.UNIX)==0)?"TCP":"Unix"));
        }
    };
    table1 = new JTable(imodel)
    {
        public TableCellRenderer getCellRenderer(int row, int column)
        {
            if(column == 2)
                return transportRenderer;
            else if(column == 4)
                return IPRenderer;
            else if(column == 5)
                return PortRenderer;
            else
                return super.getCellRenderer(row, column);
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

    c.gridx = 0;
    c.gridy = 1;
    panel2 = new JPanel(Out_Layout);
    panel2.setBorder(new TitledBorder("Outgoing"));
    layout.setConstraints(panel2,c);
    add(panel2);

    Object[][] data2 = {
            {"  1","","","","","",""},
	    {"","","","","","",""},
	    {"","","","","","",""},
	    {"","","","","","",""},
	    {"","","","","","",""},
	    {"","","","","","",""},
	    {"","","","","","",""},
	    {"","","","","","",""},
	    {"","","","","","",""},
	    {"","","","","","",""}};

        String[] columnNames2 = {"Serial No.",
	                         "Main",
				 "Kind",
                                 "Transport",
                                 "Host / FileName",
                                 "IP Address",
                                 "Port"};

      table2 = new JTable(data2, columnNames2);

      int v2 = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
      int h2 = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
      JScrollPane scrollPane2 = new JScrollPane(table2,v2,h2);

      TableColumn transport2 = table2.getColumnModel().getColumn(3);
      JComboBox comboBox2 = new JComboBox();
      comboBox2.addItem("TCP");
      comboBox2.addItem("UNIX");
      transport2.setCellEditor(new DefaultCellEditor(comboBox2));

      TableColumn kind = table2.getColumnModel().getColumn(2);
      JComboBox comboBox3 = new JComboBox();
      comboBox3.addItem("MIX");
      comboBox3.addItem("HTTP Proxy");
      comboBox3.addItem("SOCKS Proxy");
      kind.setCellEditor(new DefaultCellEditor(comboBox3));

      TableColumn main2 = table2.getColumnModel().getColumn(1);
      JCheckBox checkBox2 = new JCheckBox();
      main2.setCellEditor(new DefaultCellEditor(checkBox2));
      table2.setPreferredScrollableViewportSize(new Dimension(450,90));

    table2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    int vColIndex = 0;
    TableColumn col = table2.getColumnModel().getColumn(vColIndex);
    int width = 60;
    col.setMinWidth(width);
    col.setMaxWidth(width);
    col.setPreferredWidth(width);
    vColIndex++;
    col = table2.getColumnModel().getColumn(vColIndex);
    width = 50;
    col.setMinWidth(width);
    col.setMaxWidth(width);
    col.setPreferredWidth(width);
    vColIndex++;
    col = table2.getColumnModel().getColumn(vColIndex);
    width = 85;
    col.setMinWidth(width);
    col.setMaxWidth(width);
    col.setPreferredWidth(width);
    vColIndex++;
    col = table2.getColumnModel().getColumn(vColIndex);
    width = 75;
    col.setMinWidth(width);
    col.setMaxWidth(width);
    col.setPreferredWidth(width);
    vColIndex++;
    col = table2.getColumnModel().getColumn(vColIndex);
    width = 110;
    col.setMinWidth(width);
    col.setMaxWidth(width);
    col.setPreferredWidth(width);
    vColIndex++;
    col = table2.getColumnModel().getColumn(vColIndex);
    width = 70;
    col.setMinWidth(width);
    col.setMaxWidth(width);
    col.setPreferredWidth(width);
    vColIndex++;
    col = table2.getColumnModel().getColumn(vColIndex);
    width = 60;
    col.setMinWidth(width);
    col.setMaxWidth(width);
    col.setPreferredWidth(width);


    GridBagConstraints e=new GridBagConstraints();
    e.anchor=GridBagConstraints.CENTER;
    e.insets=new Insets(10,10,10,10);
    e.gridx = 0;
    e.gridy = 0;
    e.weightx = 1;
    e.weighty = 1;
    e.fill = GridBagConstraints.BOTH;
    Out_Layout.setConstraints(scrollPane2,e);
    panel2.add(scrollPane2);

    c.gridx = 0;
    c.gridy = 2;
    c.weighty=0;
    panel3 = new JPanel(Info_Layout);
    panel3.setBorder(new TitledBorder("Information Server"));
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
    IP_Text = new JTextField(38);
    IP_Text.setText("");
    f.gridx = 1;
    f.weightx = 1;
    Info_Layout.setConstraints(IP_Text,f);
    panel3.add(IP_Text);

    JLabel port = new JLabel("Port");
    f.gridy = 2;
    f.gridx = 0;
    f.weightx = 0;
    Info_Layout.setConstraints(port,f);
    panel3.add(port);
    Port_Text = new JTextField(38);
    Port_Text.setText("");
    f.gridx = 1;
    Info_Layout.setConstraints(Port_Text,f);
    panel3.add(Port_Text);

  /*  table1.setEnabled(false);
    panel1.setEnabled(false);
    scrollPane1.setEnabled(false);
    table2.setEnabled(false);
    panel2.setEnabled(false);
    scrollPane2.setEnabled(false);
  */}
}
