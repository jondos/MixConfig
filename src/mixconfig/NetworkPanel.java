package mixconfig;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableColumn;
import javax.swing.DefaultCellEditor;
import javax.swing.border.TitledBorder;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

class NetworkPanel extends JPanel
{
   JPanel panel1,panel2,panel3;
   JTable table1,table2;
   JTextField Host_Text,IP_Text,Port_Text;

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
    panel1.setBorder(new TitledBorder("Incomming"));
    layout.setConstraints(panel1,c);
    add(panel1);

    Object[][] data1 = {
            {"  1","","","","",""},
	    {"","","","","",""},
	    {"","","","","",""},
	    {"","","","","",""},
	    {"","","","","",""},
	    {"","","","","",""},
	    {"","","","","",""},
	    {"","","","","",""},
	    {"","","","","",""},
	    {"","","","","",""}};

        String[] columnNames1 = {"Serial No.",
	                        "Main",
                                "Transport",
                                "Host / FileName",
                                "IP Address",
                                "Port"};
      table1 = new JTable(data1, columnNames1);

      int v1 = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
      int h1 = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
      JScrollPane scrollPane1 = new JScrollPane(table1,v1,h1);

      TableColumn transport1 = table1.getColumnModel().getColumn(2);
      JComboBox comboBox1 = new JComboBox();
      comboBox1.addItem("RAW/TCP");
      comboBox1.addItem("RAW/UNIX");
      comboBox1.addItem("SSL/TCP");
      comboBox1.addItem("SSL/UNIX");
      transport1.setCellEditor(new DefaultCellEditor(comboBox1));

      TableColumn main1 = table1.getColumnModel().getColumn(1);
      JCheckBox checkBox1 = new JCheckBox("enter");
      checkBox1.setHorizontalAlignment(JLabel.CENTER);
      main1.setCellEditor(new DefaultCellEditor(checkBox1));
      table1.setPreferredScrollableViewportSize(new Dimension(450,90));

    table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    int Index = 0;
    TableColumn column = table1.getColumnModel().getColumn(Index);
    int wide = 70;
    column.setMinWidth(wide);
    column.setMaxWidth(wide);
    column.setPreferredWidth(wide);
    Index++;
    column = table1.getColumnModel().getColumn(Index);
    wide = 50;
    column.setMinWidth(wide);
    column.setMaxWidth(wide);
    column.setPreferredWidth(wide);
    Index++;
    column = table1.getColumnModel().getColumn(Index);
    wide = 75;
    column.setMinWidth(wide);
    column.setMaxWidth(wide);
    column.setPreferredWidth(wide);
    Index++;
    column = table1.getColumnModel().getColumn(Index);
    wide = 110;
    column.setMinWidth(wide);
    column.setMaxWidth(wide);
    column.setPreferredWidth(wide);
    Index++;
    column = table1.getColumnModel().getColumn(Index);
    wide = 85;
    column.setMinWidth(wide);
    column.setMaxWidth(wide);
    column.setPreferredWidth(wide);
    Index++;
    column = table1.getColumnModel().getColumn(Index);
    wide = 65;
    column.setMinWidth(wide);
    column.setMaxWidth(wide);
    column.setPreferredWidth(wide);


    GridBagConstraints d=new GridBagConstraints();
    d.anchor=GridBagConstraints.CENTER;
    d.insets=new Insets(10,10,10,10);
    d.gridx = 0;
    d.gridy = 0;
    d.weightx = 1;
    d.weighty = 1;
    d.fill = GridBagConstraints.BOTH;
    In_Layout.setConstraints(scrollPane1,d);
    panel1.add(scrollPane1);

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

    table1.setEnabled(false);
    panel1.setEnabled(false);
    scrollPane1.setEnabled(false);
    table2.setEnabled(false);
    panel2.setEnabled(false);
    scrollPane2.setEnabled(false);
  }
}
