package mixconfig;

import java.util.*;
import java.net.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.Font.*;
import java.io.*;
import javax.swing.*;
import java.applet.*;
import java.lang.Object;
import java.math.*;
import java.net.URLEncoder;
import javax.swing.BorderFactory;
import javax.swing.table.*;
import javax.swing.event.*;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JFrame;

import javax.swing.border.TitledBorder;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.security.*;
import org.bouncycastle.util.encoders.*;
import org.bouncycastle.jce.X509V3CertificateGenerator;
import org.bouncycastle.jce.provider.*;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.crypto.BlockCipher;


//  Creating a Frame.........

 class MyFrame extends JFrame implements ActionListener
{
  public static String filename;
  public static boolean New;
  public static JMenuItem saveMenuItem,saveclipItem;

  public static GeneralPanel m_GeneralPanel;
  public static NetworkPanel m_NetworkPanel;
  public static CertificatesPanel m_CertificatesPanel;
  public static DescriptionPanel m_DescriptionPanel;

  public MyFrame()
  {
    super("Mix Configuration");

    JMenuBar mb = new JMenuBar();
    setJMenuBar(mb);
    JMenu fileMenu = new JMenu("File");
    mb.add(fileMenu);

    JMenuItem newMenuItem = new JMenuItem("New");
    JMenuItem exitMenuItem = new JMenuItem("Exit");
    JMenuItem openMenuItem = new JMenuItem("Open");
    JMenuItem openclipItem = new JMenuItem("Open Using Clip Board");
    saveMenuItem = new JMenuItem("Save [none]");
    saveclipItem = new JMenuItem("Save Using Clip Board");
    JMenuItem saveAsMenuItem = new JMenuItem("Save as");

    newMenuItem.addActionListener(this);
    exitMenuItem.addActionListener(this);
    openMenuItem.addActionListener(this);
    openclipItem.addActionListener(this);
    saveMenuItem.addActionListener(this);
    saveclipItem.addActionListener(this);
    saveAsMenuItem.addActionListener(this);

    newMenuItem.setActionCommand("New");
    exitMenuItem.setActionCommand("Exit");
    openMenuItem.setActionCommand("Open");
    saveclipItem.setActionCommand("SaveClip");
    openclipItem.setActionCommand("OpenClip");
    saveMenuItem.setActionCommand("Save");
    saveAsMenuItem.setActionCommand("SaveAs");
    saveMenuItem.setEnabled(false);

    fileMenu.add(newMenuItem);
    fileMenu.addSeparator();
    fileMenu.add(openMenuItem);
    fileMenu.add(openclipItem);
    fileMenu.addSeparator();
    fileMenu.add(saveMenuItem);
    fileMenu.add(saveAsMenuItem);
    fileMenu.add(saveclipItem);
    fileMenu.addSeparator();
    fileMenu.add(exitMenuItem);

    addWindowListener(new WindowAdapter()
    {
       public void windowClosing(WindowEvent e)
       {
           System.exit(0);
       }
    });

    filename = "";

    JTabbedPane jtp = new JTabbedPane();
    m_GeneralPanel=new GeneralPanel();
    m_NetworkPanel = new NetworkPanel();
    m_CertificatesPanel = new CertificatesPanel();
    m_DescriptionPanel = new DescriptionPanel();

    jtp.addTab("General",m_GeneralPanel);
    jtp.addTab("Network",m_NetworkPanel);
    jtp.addTab("Certificates",m_CertificatesPanel);
    jtp.addTab("Description",m_DescriptionPanel);
    getContentPane().add(jtp);
  }

    public void actionPerformed(ActionEvent evt)
    {
      if(evt.getActionCommand().equals("New"))
      {
        NewDialogBox dialog = new NewDialogBox(TheApplet.myFrame,"Caution!!");
	dialog.setlabel("You will lose unsaved information. Do you want to continue?");
	dialog.setVisible(true);
        if(New == true)
           reset();
      }
      if(evt.getActionCommand().equals("Exit"))
      {
	  dispose();
	  System.exit(0);
      }
      if(evt.getActionCommand().equals("Save"))
      {
        if(filename != "" && check() == true)
          save(filename);
      }
      if(evt.getActionCommand().equals("SaveAs"))
      {
        if(check() == true)
        {
           FileDialog fd2 = new FileDialog(this,"Save File",FileDialog.SAVE);
           fd2.show();
           String dir = fd2.getDirectory();
           String mySaveFile = fd2.getDirectory()+fd2.getFile();
           if(!(mySaveFile.equals("nullnull")))
           {
               save(mySaveFile);
               filename = fd2.getFile();
               saveMenuItem.setText("save ["+filename+"] ");
               saveMenuItem.setEnabled(true);
           }
         }
      }

      if(evt.getActionCommand().equals("OpenClip"))
      {
        ClipFrame Open = new ClipFrame("Paste a file to be opened in the area provided.",true);
      }

      if(evt.getActionCommand().equals("SaveClip"))
      {
        try
        {
          if(check() == true)
          {
            ClipFrame Save = new ClipFrame("Copy and Save this file in a new Location.",false);
           // Save.Area.append(new String(save_internal()));
            byte[] array = save_internal();
            int j = array.length;
            String str = "";
            int i = 0;
            char ch,ar;
            ch = (char)array[i];
            i++;
            str = "";
            while(i < j)
            {
              ar = (char)array[i];
              i++;
              if(ch == '>' && ar == '<')
              {
                Save.Area.append(str+">\n");
                str = "";
                ch = ar;
              }
              else
              {
                str += ch;
                ch = ar;
              }
            }
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }

      if(evt.getActionCommand().equals("Open"))
      {
	   FileDialog fd = new FileDialog(this,"Open File",FileDialog.LOAD);
	   fd.show();
	   String myFile = fd.getDirectory()+fd.getFile();
           if(!(myFile.equals("nullnull")))
           {
	     open(myFile);
             filename = fd.getFile();
             saveMenuItem.setText("save ["+filename+"] ");
             saveMenuItem.setEnabled(true);
           }
      }
    }

    public static void reset()
  {
     saveMenuItem.setText("save [none]");
     saveMenuItem.setEnabled(false);
     m_GeneralPanel.Box1.setSelectedIndex(0);
     m_GeneralPanel.setMixName("");
     m_GeneralPanel.Auto.setSelected(false);
     m_GeneralPanel.MixID.setText("");
     m_GeneralPanel.UserID.setSelected(false);
     m_GeneralPanel.number.setSelected(false);
     m_GeneralPanel.Daemon.setSelected(false);
     m_GeneralPanel.EnableLog.setSelected(false);
     m_GeneralPanel.FileName.setText("");

     int i,j;
     for(i = 0; i < 10; i++)
     {
       for(j = 0; j < 6; j++)
       {
         m_NetworkPanel.setTable1("",i,j);
         m_NetworkPanel.setTable2("",i,j);
       }
       m_NetworkPanel.setTable2("",i,6);
     }
     m_NetworkPanel.Host_Text.setText("");
     m_NetworkPanel.IP_Text.setText("");
     m_NetworkPanel.Port_Text.setText("");

     m_CertificatesPanel.from_text1.setText("");
     m_CertificatesPanel.to_text1.setText("");
     m_CertificatesPanel.text1.setText("");
     m_CertificatesPanel.from_text2.setText("");
     m_CertificatesPanel.to_text2.setText("");
     m_CertificatesPanel.text2.setText("");
     m_CertificatesPanel.from_text3.setText("");
     m_CertificatesPanel.to_text3.setText("");
     m_CertificatesPanel.text3.setText("");

     m_DescriptionPanel.setCity("");
     m_DescriptionPanel.setLati("");
     m_DescriptionPanel.setLongi("");
     m_DescriptionPanel.setState("");
   }

   private Element getChild(Node node,String name)
    {
      Node tmp=node.getFirstChild();
      while(tmp!=null)
	{
	  if(tmp.getNodeName().equals(name))
	    {
	      if(tmp.getNodeType()==Node.ELEMENT_NODE)
	        return (Element)tmp;
	    }
	  tmp=tmp.getNextSibling();
	}
      return null;
    }

   public void open(String fileName)
    {
      try
	{
	  DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
	  DocumentBuilder docBuilder=factory.newDocumentBuilder();
	  FileInputStream fin=new FileInputStream(fileName);
	  Document doc=docBuilder.parse(fin);
	  fin.close();

	  Element root=doc.getDocumentElement();
	  Element elemGeneral=getChild(root,"General");
	  Element elemType=getChild(elemGeneral,"MixType");
	  String MixType=elemType.getFirstChild().getNodeValue();
	  m_GeneralPanel.setType(MixType);

	  Element elemName=getChild(elemGeneral,"MixName");
	  String MixName=elemName.getFirstChild().getNodeValue();
	  m_GeneralPanel.setMixName(MixName);

	  Element elemAuto = getChild(elemGeneral,"AutomaticallyGenerated");
	  Element elemMixID;
	  if(elemAuto != null)
	  {
	    m_GeneralPanel.setAuto();
	    elemMixID = getChild(elemAuto,"MixID");
	  }
	  else
	    elemMixID = getChild(elemGeneral,"MixID");
	  String MixID = elemMixID.getFirstChild().getNodeValue();
	  m_GeneralPanel.setMixID(MixID);

	  Element elemUserID = getChild(elemGeneral,"UserID");
	  if(elemUserID != null)
	  {
	    m_GeneralPanel.UserID.setSelected(true);
	    String UserID = elemUserID.getFirstChild().getNodeValue();
	    m_GeneralPanel.setUserID(UserID);
	  }

	  Element elemFileDes = getChild(elemGeneral,"NumberofFileDescriptors");
	  if(elemFileDes != null)
	  {
	    m_GeneralPanel.number.setSelected(true);
	    String FileDes = elemFileDes.getFirstChild().getNodeValue();
	    m_GeneralPanel.setFileDes(FileDes);
	  }

          Element elemDaemon = getChild(elemGeneral,"Daemon");
	  String Daemon = elemDaemon.getFirstChild().getNodeValue();
	  if(Daemon.equals("True"))
	     m_GeneralPanel.Daemon.setSelected(true);

	  Element elemEnableLog = getChild(elemGeneral,"EnableLogging");
	  if(elemEnableLog != null)
	  {
	    m_GeneralPanel.EnableLog.setSelected(true);
	    if(Daemon.equals("False"))
	      m_GeneralPanel.Console.setEnabled(true);
	    m_GeneralPanel.File.setEnabled(true);
	    m_GeneralPanel.Syslog.setEnabled(true);
	  }

	  Element elemFile = getChild(elemEnableLog,"File");
	  if(elemFile != null)
	  {
	    String File = elemFile.getFirstChild().getNodeValue();
	    m_GeneralPanel.FileName.setEnabled(true);
	    m_GeneralPanel.FileName.setText(File);
	    m_GeneralPanel.File.setSelected(true);
	  }
	  else
	  {
	    Element elemSyslog = getChild(elemEnableLog,"SysLog");
	    if(elemSyslog != null)
	      m_GeneralPanel.Syslog.setSelected(true);
	    else
	      m_GeneralPanel.Console.setSelected(true);
	  }

          Element elemNetwork = getChild(root,"Network");
          Element elemIncomming = getChild(elemNetwork,"Incomming");
	  int i = 0,j = 1;
	  Element elemMain , elemTransport,elemHost,elemIP,elemPort;
	  String main,transport,host,IP,port,kind,file;
	  Element elemInterface = getChild(elemIncomming,"ListenerInterface"+Integer.toString(i+1));

	while(elemInterface != null)
	{
	  m_NetworkPanel.setTable1(Integer.toString(i+1),i,0);
	  elemMain = getChild(elemInterface,"Main");
	  main = elemMain.getFirstChild().getNodeValue();
	  m_NetworkPanel.setTable1(main,i,j);
	  j++;
	  elemTransport = getChild(elemInterface,"Transport");
	  transport = elemTransport.getFirstChild().getNodeValue();
	  m_NetworkPanel.setTable1(transport,i,j);

	  if(transport.equals("RAW/TCP") || transport.equals("SSL/TCP"))
	  {
	    j++;
	    elemHost = getChild(elemInterface,"Host");
	    host = elemHost.getFirstChild().getNodeValue();
	    m_NetworkPanel.setTable1(host,i,j);
	    j++;
	    elemIP = getChild(elemInterface,"IP");
	    if(elemIP != null)
	    {
	      IP = elemIP.getFirstChild().getNodeValue();
	      m_NetworkPanel.setTable1(IP,i,j);
	    }
	    j++;
	    elemPort = getChild(elemInterface,"Port");
	    port = elemPort.getFirstChild().getNodeValue();
	    m_NetworkPanel.setTable1(port,i,j);
	  }
	  else
	  {
	    j++;
	    elemFile = getChild(elemInterface,"File");
	    file = elemFile.getFirstChild().getNodeValue();
	    m_NetworkPanel.setTable1(file,i,j);
	  }
	  i++;
	  j = 1;
	 elemInterface = getChild(elemIncomming,"ListenerInterface"+Integer.toString(i+1));
        }

	Element elemOutgoing = getChild(elemNetwork,"Outgoing");
	i = 0;
	j = 1;
	Element elemKind;
	elemInterface = getChild(elemOutgoing,"ListenerInterface"+Integer.toString(i+1));

	while(elemInterface != null)
	{
	  m_NetworkPanel.setTable2(Integer.toString(i+1),i,0);
	  elemMain = getChild(elemInterface,"Main");
	  main = elemMain.getFirstChild().getNodeValue();
	  m_NetworkPanel.setTable2(main,i,j);
	  j++;

	  elemKind = getChild(elemInterface,"Kind");
	  kind = elemKind.getFirstChild().getNodeValue();
	  m_NetworkPanel.setTable2(kind,i,j);
	  j++;

	  elemTransport = getChild(elemInterface,"Transport");
	  transport = elemTransport.getFirstChild().getNodeValue();
	  m_NetworkPanel.setTable2(transport,i,j);

	  if(transport.equals("TCP"))
	  {
	    j++;
	    elemHost = getChild(elemInterface,"Host");
	    host = elemHost.getFirstChild().getNodeValue();
	    m_NetworkPanel.setTable2(host,i,j);
	    j++;
	    elemIP = getChild(elemInterface,"IP");
	    if(elemIP != null)
	    {
	      IP = elemIP.getFirstChild().getNodeValue();
	      m_NetworkPanel.setTable2(IP,i,j);
	    }
	    j++;
	    elemPort = getChild(elemInterface,"Port");
	    port = elemPort.getFirstChild().getNodeValue();
	    m_NetworkPanel.setTable2(port,i,j);
	  }
	  else
	  {
	    j++;
	    elemFile = getChild(elemInterface,"File");
	    file = elemFile.getFirstChild().getNodeValue();
	    m_NetworkPanel.setTable2(file,i,j);
	  }
	  i++;
	  j = 1;
	 elemInterface = getChild(elemOutgoing,"ListenerInterface"+Integer.toString(i+1));
        }

	Element elemInfoServer = getChild(elemNetwork,"InformationServer");
	elemHost = getChild(elemInfoServer,"Host");
	host = elemHost.getFirstChild().getNodeValue();
	m_NetworkPanel.setInfoHost(host);
	elemIP = getChild(elemInfoServer,"IP");
	if(elemIP != null)
	{
	  IP = elemIP.getFirstChild().getNodeValue();
	  m_NetworkPanel.setInfoIP(IP);
	}
	elemPort = getChild(elemInfoServer,"Port");
	port = elemPort.getFirstChild().getNodeValue();
	m_NetworkPanel.setInfoPort(port);

	Element elemCertificates = getChild(root,"Certificates");
	Element elemOwnCert = getChild(elemCertificates,"OwnCertificate");
	elemName = getChild(elemOwnCert,"Name");
	String name = elemName.getFirstChild().getNodeValue();
	m_CertificatesPanel.setName1(name);
	Element elemValidFrom = getChild(elemOwnCert,"ValidFrom");
	String from = elemValidFrom.getFirstChild().getNodeValue();
	m_CertificatesPanel.setFrom1(from);
	Element elemValidTo = getChild(elemOwnCert,"ValidTill");
	String validto = elemValidTo.getFirstChild().getNodeValue();
	m_CertificatesPanel.setTo1(validto);

	Element elemPrevCert = getChild(elemCertificates,"PreviousCertificate");
	elemName = getChild(elemPrevCert,"Name");
	name = elemName.getFirstChild().getNodeValue();
	m_CertificatesPanel.setName2(name);
	elemValidFrom = getChild(elemPrevCert,"ValidFrom");
	from = elemValidFrom.getFirstChild().getNodeValue();
	m_CertificatesPanel.setFrom2(from);
	elemValidTo = getChild(elemPrevCert,"ValidTill");
	validto = elemValidTo.getFirstChild().getNodeValue();
	m_CertificatesPanel.setTo2(validto);

	Element elemNextCert = getChild(elemCertificates,"NextCertificate");
	elemName = getChild(elemNextCert,"Name");
	name = elemName.getFirstChild().getNodeValue();
	m_CertificatesPanel.setName3(name);
	elemValidFrom = getChild(elemNextCert,"ValidFrom");
	from = elemValidFrom.getFirstChild().getNodeValue();
	m_CertificatesPanel.setFrom3(from);
	elemValidTo = getChild(elemNextCert,"ValidTill");
	validto = elemValidTo.getFirstChild().getNodeValue();
	m_CertificatesPanel.setTo3(validto);

	Element elemDescription = getChild(root,"Description");
	Element elemCity = getChild(elemDescription,"City");
	String city = elemCity.getFirstChild().getNodeValue();
	m_DescriptionPanel.setCity(city);
        Element elemState = getChild(elemDescription,"State");
	String state = elemState.getFirstChild().getNodeValue();
	m_DescriptionPanel.setState(state);

	Element elemLongi = getChild(elemDescription,"Longitude");
	if(elemLongi != null)
	{
	  String longi = elemLongi.getFirstChild().getNodeValue();
	  m_DescriptionPanel.setLongi(longi);
	}
	Element elemLati = getChild(elemDescription,"Latitude");
	if(elemLati != null)
	{
	  String lati = elemLati.getFirstChild().getNodeValue();
	  m_DescriptionPanel.setLati(lati);
	}


	}
      catch(Exception e)
	{
	  System.out.println("Open() - There was an error:");
	  e.printStackTrace();
	}
    }

    public void save(String fileName)
      {
        try{
          FileOutputStream fout=new FileOutputStream(fileName);
          fout.write(save_internal());
          fout.close();
        }
        catch(Exception e)
        {
        e.printStackTrace();
        }
      }

    private byte[] save_internal()
    {
      try
	{
	  DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
	  DocumentBuilder docBuilder=factory.newDocumentBuilder();
	  Document doc=docBuilder.newDocument();

          DocumentBuilderFactory factory2=DocumentBuilderFactory.newInstance();
	  DocumentBuilder docBuilder2=factory2.newDocumentBuilder();
	  Document doc2=docBuilder2.newDocument();

	  Element root=doc.createElement("MixConfiguration");
	  doc.appendChild(root);
	  Element elemGeneral=doc.createElement("General");
	  root.appendChild(elemGeneral);

	  String elemmixtype=m_GeneralPanel.getMixType();
	  Element MixType=doc.createElement("MixType");
	  elemGeneral.appendChild(MixType);
	  Text text1=doc.createTextNode(elemmixtype);
	  MixType.appendChild(text1);

	  String elemmixname=m_GeneralPanel.getMixName();
          Element elemMixName=doc.createElement("MixName");
	  elemGeneral.appendChild(elemMixName);
	  Text text2=doc.createTextNode(elemmixname);
	  elemMixName.appendChild(text2);

	  String MixAuto = m_GeneralPanel.getMixAuto();
	  Element elemMixAuto = doc.createElement("AutomaticallyGenerated");
	  if(MixAuto.equals("True"))
	    elemGeneral.appendChild(elemMixAuto);

	  String mixID=m_GeneralPanel.getMixID();
          Element elemMixID=doc.createElement("MixID");
	  if(MixAuto.equals("True"))
	    elemMixAuto.appendChild(elemMixID);
	  else
	    elemGeneral.appendChild(elemMixID);
	  Text text3=doc.createTextNode(mixID);
	  elemMixID.appendChild(text3);

	  String elemUser_ID = m_GeneralPanel.User_ID;
	  if(!elemUser_ID.equals(""))
	  {
	    elemUser_ID = m_GeneralPanel.getUserID();
	    Element elemUserID = doc.createElement("UserID");
	    elemGeneral.appendChild(elemUserID);
	    Text text4 = doc.createTextNode(elemUser_ID);
	    elemUserID.appendChild(text4);
	  }

	  String elemFileDes = m_GeneralPanel.fileDes;
	  if(!elemFileDes.equals(""))
	  {
	    elemFileDes = m_GeneralPanel.getFileDes();
	    Element elemfiledes = doc.createElement("NumberofFileDescriptors");
	    elemGeneral.appendChild(elemfiledes);
	    Text text4 = doc.createTextNode(elemFileDes);
	    elemfiledes.appendChild(text4);
	  }

	  String elemDaemon = m_GeneralPanel.is_daemon;
	  if(elemDaemon.equals(""))
	    elemDaemon = "False";
	  else
	    elemDaemon = "True";
	  Element DaemonElem = doc.createElement("Daemon");
	  elemGeneral.appendChild(DaemonElem);
	  Text text5 = doc.createTextNode(elemDaemon);
	  DaemonElem.appendChild(text5);

          String elemLogging = m_GeneralPanel.logging;
	  if(elemLogging.equals("True"))
	  {
	    Element EnableLog = doc.createElement("EnableLogging");
	    elemGeneral.appendChild(EnableLog);
	    String LogInfo = m_GeneralPanel.getEnabled();
	    if(LogInfo.equals("LogtoConsole"))
	    {
	      Element elemLog = doc.createElement("Console");
	      EnableLog.appendChild(elemLog);
	      Text text6 = doc.createTextNode("True");
	      elemLog.appendChild(text6);
	    }
	    if(LogInfo.equals("LogtoSyslog"))
	    {
	      Element elemLog = doc.createElement("SysLog");
	      EnableLog.appendChild(elemLog);
	    }
	    if(LogInfo.equals("Logtofile"))
	    {
	      String filename = m_GeneralPanel.getFileName();
	      Element elemLog = doc.createElement("File");
	      EnableLog.appendChild(elemLog);
	      Text text6 = doc.createTextNode(filename);
	      elemLog.appendChild(text6);
	    }
	  }

	  Element elemNetwork=doc.createElement("Network");
	  root.appendChild(elemNetwork);
	  Element elemIn = doc.createElement("Incomming");
	  elemNetwork.appendChild(elemIn);
	  int i = 0, j = 0;
	  Text text7;
	  Element elemInterface,elemtransport,elemHost,elemIP,elemPort,elemFile,elemMain;
	  String Incomming = m_NetworkPanel.getTable1(i,j);
	  while(!Incomming.equals(""))
	  {
	    j++;
	    elemInterface = doc.createElement("ListenerInterface"+Integer.toString(i+1));
	    elemIn.appendChild(elemInterface);
	    Incomming = m_NetworkPanel.getTable1(i,j);
	    elemMain = doc.createElement("Main");
	    elemInterface.appendChild(elemMain);
	    text7 = doc.createTextNode(Incomming);
	    elemMain.appendChild(text7);

	    j++;
	    Incomming = m_NetworkPanel.getTable1(i,j);
	    elemtransport = doc.createElement("Transport");
	    elemInterface.appendChild(elemtransport);
	    text7 = doc.createTextNode(Incomming);
	    elemtransport.appendChild(text7);
	    if(Incomming.equals("RAW/TCP") || Incomming.equals("SSL/TCP"))
	    {
	      j++;
	      Incomming = m_NetworkPanel.getTable1(i,j);
	      elemHost = doc.createElement("Host");
	      elemInterface.appendChild(elemHost);
	      text7 = doc.createTextNode(Incomming);
	      elemHost.appendChild(text7);
	      j++;
	      Incomming = m_NetworkPanel.getTable1(i,j);
	      if(!Incomming.equals(""))
	      {
	        elemIP = doc.createElement("IP");
	        elemInterface.appendChild(elemIP);
	        text7 = doc.createTextNode(Incomming);
	        elemIP.appendChild(text7);
	      }
	      j++;
	      Incomming = m_NetworkPanel.getTable1(i,j);
	      elemPort = doc.createElement("Port");
	      elemInterface.appendChild(elemPort);
	      text7 = doc.createTextNode(Incomming);
	      elemPort.appendChild(text7);
	    }
	    else
	    {
	      j = 3;
	      Incomming = m_NetworkPanel.getTable1(i,j);
	      elemFile = doc.createElement("File");
	      elemInterface.appendChild(elemFile);
	      text7 = doc.createTextNode(Incomming);
	      elemFile.appendChild(text7);
	    }
	    i++;
	    j = 0;
	    Incomming = m_NetworkPanel.getTable1(i,j);
	  }

	  Element elemOut = doc.createElement("Outgoing");
	  elemNetwork.appendChild(elemOut);
	  i = 0;
	  j = 0;
	  Text text8;
	  Element elemKind;
	  String Outgoing = m_NetworkPanel.getTable2(i,j);
          while(!Outgoing.equals(""))
	  {
	    j++;
	    elemInterface = doc.createElement("ListenerInterface"+Integer.toString(i+1));
	    elemOut.appendChild(elemInterface);
	    Outgoing = m_NetworkPanel.getTable2(i,j);
	    elemMain = doc.createElement("Main");
	    elemInterface.appendChild(elemMain);
	    text8 = doc.createTextNode(Outgoing);
	    elemMain.appendChild(text8);

	    j++;
	    Outgoing = m_NetworkPanel.getTable2(i,j);
	    elemKind = doc.createElement("Kind");
	    elemInterface.appendChild(elemKind);
	    text8 = doc.createTextNode(Outgoing);
	    elemKind.appendChild(text8);

	    j++;
	    Outgoing = m_NetworkPanel.getTable2(i,j);
	    elemtransport = doc.createElement("Transport");
	    elemInterface.appendChild(elemtransport);
	    text8 = doc.createTextNode(Outgoing);
	    elemtransport.appendChild(text8);
	    if(Outgoing.equals("TCP"))
	    {
	      j++;
	      Outgoing = m_NetworkPanel.getTable2(i,j);
	      elemHost = doc.createElement("Host");
	      elemInterface.appendChild(elemHost);
	      text8 = doc.createTextNode(Outgoing);
	      elemHost.appendChild(text8);
	      j++;
	      Outgoing = m_NetworkPanel.getTable2(i,j);
	      if(!Outgoing.equals(""))
	      {
	        elemIP = doc.createElement("IP");
	        elemInterface.appendChild(elemIP);
	        text8 = doc.createTextNode(Outgoing);
	        elemIP.appendChild(text8);
	      }
	      j++;
	      Outgoing = m_NetworkPanel.getTable2(i,j);
	      elemPort = doc.createElement("Port");
	      elemInterface.appendChild(elemPort);
	      text8 = doc.createTextNode(Outgoing);
	      elemPort.appendChild(text8);
	    }
	    else
	    {
	      j = 4;
	      Outgoing = m_NetworkPanel.getTable2(i,j);
	      elemFile = doc.createElement("File");
	      elemInterface.appendChild(elemFile);
	      text8 = doc.createTextNode(Outgoing);
	      elemFile.appendChild(text8);
	    }
	    i++;
	    j = 0;
	    Outgoing = m_NetworkPanel.getTable2(i,j);
	  }

	  Element elemInfoSer = doc.createElement("InformationServer");
	  elemNetwork.appendChild(elemInfoSer);
	  String Host = m_NetworkPanel.getHost();
	  elemHost = doc.createElement("Host");
	  elemInfoSer.appendChild(elemHost);
	  Text text9 = doc.createTextNode(Host);
	  elemHost.appendChild(text9);

	  String IP_Text = m_NetworkPanel.getIP();
          if(!IP_Text.equals(""))
	  {
	    elemIP = doc.createElement("IP");
	    elemInfoSer.appendChild(elemIP);
	    text9 = doc.createTextNode(IP_Text);
	    elemIP.appendChild(text9);
	  }

	  String Port = m_NetworkPanel.getPort();
	  elemPort = doc.createElement("Port");
	  elemInfoSer.appendChild(elemPort);
	  text9 = doc.createTextNode(Port);
	  elemPort.appendChild(text9);

 	  Element elemCertificate = doc.createElement("Certificates");
	  root.appendChild(elemCertificate);
	  Element elemOwn = doc.createElement("OwnCertificate");
	  elemCertificate.appendChild(elemOwn);

	  Element elemName = doc.createElement("Name");
	  elemOwn.appendChild(elemName);
          String Name = m_CertificatesPanel.getOwnName();
	  Text text10 = doc.createTextNode(Name);
	  elemName.appendChild(text10);

	  Element elemFrom = doc.createElement("ValidFrom");
	  elemOwn.appendChild(elemFrom);
          String From = m_CertificatesPanel.getOwnFrom();
	  text10 = doc.createTextNode(From);
	  elemFrom.appendChild(text10);

	  Element elemTo = doc.createElement("ValidTill");
	  elemOwn.appendChild(elemTo);
          String To = m_CertificatesPanel.getOwnTo();
	  text10 = doc.createTextNode(To);
	  elemTo.appendChild(text10);

	  Element elemPrevious = doc.createElement("PreviousCertificate");
	  elemCertificate.appendChild(elemPrevious);

	  elemName = doc.createElement("Name");
	  elemPrevious.appendChild(elemName);
          Name = m_CertificatesPanel.getPreviousName();
	  text10 = doc.createTextNode(Name);
	  elemName.appendChild(text10);

	  elemFrom = doc.createElement("ValidFrom");
	  elemPrevious.appendChild(elemFrom);
          From = m_CertificatesPanel.getPreviousFrom();
	  text10 = doc.createTextNode(From);
	  elemFrom.appendChild(text10);

	  elemTo = doc.createElement("ValidTill");
	  elemPrevious.appendChild(elemTo);
          To = m_CertificatesPanel.getPreviousTo();
	  text10 = doc.createTextNode(To);
	  elemTo.appendChild(text10);

          //for the certificate

          Element root2 = doc2.createElement("PreviousCertificate");
          doc2.appendChild(root2);
          elemName = doc2.createElement("Name");
	  root2.appendChild(elemName);
          Name = m_CertificatesPanel.getPreviousName();
	  text10 = doc2.createTextNode(Name);
	  elemName.appendChild(text10);

	  elemFrom = doc2.createElement("ValidFrom");
	  root2.appendChild(elemFrom);
          From = m_CertificatesPanel.getPreviousFrom();
	  text10 = doc2.createTextNode(From);
	  elemFrom.appendChild(text10);

	  elemTo = doc2.createElement("ValidTill");
	  root2.appendChild(elemTo);
          To = m_CertificatesPanel.getPreviousTo();
	  text10 = doc2.createTextNode(To);
	  elemTo.appendChild(text10);

	  Element elemNext = doc.createElement("NextCertificate");
	  elemCertificate.appendChild(elemNext);

	  elemName = doc.createElement("Name");
	  elemNext.appendChild(elemName);
          Name = m_CertificatesPanel.getNextName();
	  text10 = doc.createTextNode(Name);
	  elemName.appendChild(text10);

	  elemFrom = doc.createElement("ValidFrom");
	  elemNext.appendChild(elemFrom);
          From = m_CertificatesPanel.getNextFrom();
	  text10 = doc.createTextNode(From);
	  elemFrom.appendChild(text10);

	  elemTo = doc.createElement("ValidTill");
	  elemNext.appendChild(elemTo);
          To = m_CertificatesPanel.getNextTo();
	  text10 = doc.createTextNode(To);
	  elemTo.appendChild(text10);

	  Element elemDescription = doc.createElement("Description");
	  root.appendChild(elemDescription);
	  Element elemCity = doc.createElement("City");
	  elemDescription.appendChild(elemCity);
	  String city = m_DescriptionPanel.getCity();
	  Text text11 = doc.createTextNode(city);
	  elemCity.appendChild(text11);

	  String State = m_DescriptionPanel.getState();
	  text11 = doc.createTextNode(State);
	  Element elemState = doc.createElement("State");
	  elemDescription.appendChild(elemState);
	  elemState.appendChild(text11);

	  String Position = m_DescriptionPanel.getLongitude();
	  if(!Position.equals(""))
	  {
	    Element elemLong = doc.createElement("Longitude");
	    text11 = doc.createTextNode(Position);
	    elemDescription.appendChild(elemLong);
	    elemLong.appendChild(text11);
	    Position = m_DescriptionPanel.getLatitude();
	    Element elemLati = doc.createElement("Latitude");
	    text11 = doc.createTextNode(Position);
	    elemDescription.appendChild(elemLati);
	    elemLati.appendChild(text11);
	  }

	  //Writing to File...
	  ByteArrayOutputStream fout=new ByteArrayOutputStream();
	  Transformer trans=TransformerFactory.newInstance().newTransformer();
	  DOMSource src=new DOMSource(doc);
	  StreamResult target=new StreamResult(fout);
	  trans.transform(src,target);
	  fout.close();
          return fout.toByteArray();
	}
      catch(Exception e)
	{
	  System.out.println("Save() - There was an error:");
	  e.printStackTrace();
          return null;
	}
    }

    // To check for Errors or Missing Fields while writing a file.....

  public static boolean check()
    {
      DialogBox dialog;

      if(m_GeneralPanel.getMixName().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in General Panel !!");
	dialog.setlabel("You have to enter the Mix Name !!");
	dialog.setVisible(true);
	return false;
      }
      if(m_GeneralPanel.getMixID().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in General Panel !!");
	dialog.setlabel("Mix ID field is blank!");
	dialog.setVisible(true);
	return false;
      }

      if(m_CertificatesPanel.getOwnName().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Certificates Panel !!");
	dialog.setlabel("Own Mix Certificate Name is missing!");
	dialog.setVisible(true);
	return false;
      }
      if(m_CertificatesPanel.getOwnFrom().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Certificates Panel !!");
	dialog.setlabel("Confirm Own Certificates Validity.");
	dialog.setVisible(true);
	return false;
      }
      if(m_CertificatesPanel.getOwnTo().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Certificates Panel !!");
	dialog.setlabel("Confirm Own Certificates Validity");
	dialog.setVisible(true);
	return false;
      }
      if(m_CertificatesPanel.getPreviousName().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Certificates Panel !!");
	dialog.setlabel("Previous Mix Certificate Name is missing!");
	dialog.setVisible(true);
	return false;
      }
      if(m_CertificatesPanel.getPreviousFrom().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Certificates Panel !!");
	dialog.setlabel("Confirm Previous Certificates Validity.");
	dialog.setVisible(true);
	return false;
      }
      if(m_CertificatesPanel.getPreviousTo().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Certificates Panel !!");
	dialog.setlabel("Confirm Previous Certificates Validity");
	dialog.setVisible(true);
	return false;
      }
      if(m_CertificatesPanel.getNextName().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Certificates Panel !!");
	dialog.setlabel("Next Mix Certificate Name is missing!");
	dialog.setVisible(true);
	return false;
      }
      if(m_CertificatesPanel.getNextFrom().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Certificates Panel !!");
	dialog.setlabel("Confirm Next Certificates Validity.");
	dialog.setVisible(true);
	return false;
      }
      if(m_CertificatesPanel.getNextTo().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Certificates Panel !!");
	dialog.setlabel("Confirm Next Certificates Validity");
	dialog.setVisible(true);
	return false;
      }

      if(m_DescriptionPanel.getCity().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Description Panel !!");
	dialog.setlabel("The city field cannot be left blank!");
	dialog.setVisible(true);
	return false;
      }
      if(m_DescriptionPanel.getState().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Description Panel !!");
	dialog.setlabel("The state field cannot be left blank!");
	dialog.setVisible(true);
	return false;
      }
      if(m_DescriptionPanel.getLatitude().equals("") && !m_DescriptionPanel.getLongitude().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Description Panel !!");
	dialog.setlabel("Fill in the Latitude!!");
	dialog.setVisible(true);
	return false;
      }
      if(!m_DescriptionPanel.getLatitude().equals("") && m_DescriptionPanel.getLongitude().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Description Panel !!");
	dialog.setlabel("Fill in the Longitude!!");
	dialog.setVisible(true);
	return false;
      }

      if(m_NetworkPanel.getHost().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Network Panel !!");
	dialog.setlabel("The HOST field should not be blank!");
	dialog.setVisible(true);
	return false;
      }
      if(m_NetworkPanel.getPort().equals(""))
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Network Panel !!");
	dialog.setlabel("The PORT field should not be blank!");
	dialog.setVisible(true);
	return false;
      }

        return true;
    }

}


public class TheApplet extends JApplet
{
  public static MyFrame myFrame;

  public static void main(String[] args)
  {
    JFrame window = new JFrame();
    myFrame = new MyFrame();
    myFrame.setBounds(10,10,600,650);

    window.addWindowListener(new WindowAdapter()
    {
       public void windowClosing(WindowEvent e)
       {
           System.exit(0);
       }
    });

    window.setJMenuBar(myFrame.getJMenuBar());
    window.getContentPane().add(myFrame.getContentPane());
    window.pack();
    window.show();
  }

   public void init()
  {
    myFrame = new MyFrame();
    myFrame.setBounds(10,10,600,650);
    setJMenuBar(myFrame.getJMenuBar());
    setContentPane(myFrame.getContentPane());
  }

}


class GeneralPanel extends JPanel implements ItemListener,ActionListener
{
  public static JComboBox Box1;
  public static JTextField MixName,MixID,FileName,ID_Text,num_file;
  public static JCheckBox Auto,Daemon,EnableLog,UserID,number;
  public static String text,User_ID = "",fileDes = "",is_daemon="",logging = "",selection = "";
  public static JRadioButton Console,File,Syslog;
  public static ButtonGroup bg;

  public String getMixType()
  {
    return (String)Box1.getSelectedItem();
  }

  public String getMixName()
  {
    return MixName.getText();
  }

  public String getMixAuto()
  {
    if(Auto.isSelected() == true)
      return "True";
    else
      return "False";
  }

  public String getMixID()
  {
    return MixID.getText();
  }

  public String getUserID()
  {
    return ID_Text.getText();
  }

  public String getFileDes()
  {
    return num_file.getText();
  }

  public String getFileName()
  {
    return FileName.getText();
  }

  public String getEnabled()
  {
    if(Syslog.isSelected() == true)
      return "LogtoSyslog";
    if(File.isSelected() == true)
      return "Logtofile";
    if(Console.isSelected() == true)
      return "LogtoConsole";
    return "null";
  }


  public static void setType(String type)
  {
    int j = 0;
    for(int i = 1; i < Box1.getItemCount() && j == 0; i++)
    {
      if(Box1.getItemAt(i).toString().equals(type))
      {
        Box1.setSelectedIndex(i);
        j++;
      }
    }
    return;
  }

  public static void setMixName(String name)
  {
    MixName.setText(name);
  }

  public static void setAuto()
  {
    Auto.setSelected(true);
  }

  public static void setMixID(String mixid)
  {
    MixID.setText(mixid);
  }

  public static void setUserID(String userid)
  {
    ID_Text.setText(userid);
  }

  public static void setFileDes(String filedes)
  {
    num_file.setText(filedes);
  }

  public GeneralPanel()
  {
    GridBagLayout layout=new GridBagLayout();
    setLayout(layout);
    GridBagConstraints c=new GridBagConstraints();
    c.anchor=GridBagConstraints.NORTHWEST;
    c.insets=new Insets(10,10,10,10);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weighty = 1;

    JLabel j1 = new JLabel("Mix Type");
    c.gridx=0;
    c.gridy=0;
    c.gridwidth = 1;
    layout.setConstraints(j1,c);
    add(j1);
    Box1 = new JComboBox();
    Box1.addItem("First Mix");
    Box1.addItem("Middle Mix");
    Box1.addItem("Last Mix");
    c.weightx=1;
    c.gridx=1;
    c.gridy=0;
    c.gridwidth = 3;
    layout.setConstraints(Box1,c);
    add(Box1);

    JLabel j2 = new JLabel("Mix Name");
    c.gridx=0;
    c.gridy=1;
    c.gridwidth = 1;
    c.weightx = 0;
    layout.setConstraints(j2,c);
    add(j2);
    MixName = new JTextField(20);
    MixName.setText("");
    c.gridx=1;
    c.gridy=1;
    c.gridwidth = 3;
    c.weightx = 1;
    layout.setConstraints(MixName,c);
    add(MixName);

    JLabel j3 = new JLabel("Mix ID");
    c.gridx=0;
    c.gridy=2;
    c.gridwidth = 1;
    c.weightx = 0;
    layout.setConstraints(j3,c);
    add(j3);
    Auto = new JCheckBox("Automatically Generated");
    c.gridx=1;
    c.gridy=2;
    c.gridwidth = 3;
    c.weightx = 1;
    layout.setConstraints(Auto,c);
    Auto.addItemListener(this);
    add(Auto);

    MixID = new JTextField(20);
    MixID.setText("");
    c.gridx=1;
    c.gridy=3;
    c.gridwidth = 3;
    layout.setConstraints(MixID,c);
    add(MixID);

    UserID = new JCheckBox("Set User ID on Execution");
    c.gridy = 5;
    c.gridx = 0;
    c.gridwidth = 1;
    c.weightx = 0;
    UserID.addItemListener(this);
    layout.setConstraints(UserID,c);
    add(UserID);
    ID_Text = new JTextField(20);
    c.gridx = 1;
    c.weightx = 1;
    c.gridwidth = 3;
    layout.setConstraints(ID_Text,c);
    add(ID_Text);
    ID_Text.setEnabled(false);

    number = new JCheckBox("Set Number of File Descriptors");
    c.weightx = 0;
    c.gridy = 6;
    c.gridx = 0;
    c.gridwidth = 1;
    number.addItemListener(this);
    layout.setConstraints(number,c);
    add(number);
    num_file = new JTextField(20);
    c.gridx = 1;
    c.gridwidth = 3;
    c.weightx = 1;
    layout.setConstraints(num_file,c);
    add(num_file);
    num_file.setEnabled(false);

    Daemon = new JCheckBox("Run as Daemon?");
    c.gridx = 0;
    c.gridy = 7;
    Daemon.addItemListener(this);
    layout.setConstraints(Daemon,c);
    add(Daemon);

    EnableLog = new JCheckBox("Enable Logging?");
    c.gridx = 0;
    c.gridy = 8;
    EnableLog.addItemListener(this);
    layout.setConstraints(EnableLog,c);
    add(EnableLog);

    Console = new JRadioButton("Log to Console");
    c.gridx = 1;
    c.gridy = 9;
    c.weightx = 1;
    Console.setActionCommand("LogtoConsole");
    Console.addActionListener(this);
    layout.setConstraints(Console,c);
    add(Console);
    Console.setEnabled(false);

    File = new JRadioButton("Log to file");
    c.gridy = 10;
    c.weightx = 0;
    File.addActionListener(this);
    File.setActionCommand("Logtofile");
    layout.setConstraints(File,c);
    add(File);
    File.setEnabled(false);

    FileName = new JTextField(20);
    FileName.setText("");
    c.gridx = 1;
    c.gridy = 11;
    c.weightx = 1;
    layout.setConstraints(FileName,c);
    add(FileName);
    FileName.setEnabled(false);

    Syslog = new JRadioButton("Log to Syslog");
    c.gridx = 1;
    c.gridy = 12;
    Syslog.addActionListener(this);
    Syslog.setActionCommand("LogtoSyslog");
    layout.setConstraints(Syslog,c);
    add(Syslog);
    Syslog.setEnabled(false);

    bg = new ButtonGroup();
    bg.add(Console);
    bg.add(File);
    bg.add(Syslog);
  }

  public void actionPerformed(ActionEvent ae)
  {
    selection = ae.getActionCommand();
    if(Console.isSelected() == true || Syslog.isSelected() == true)
    {
      FileName.setEnabled(false);
      FileName.setText("");
    }
    if(File.isSelected() == true)
      FileName.setEnabled(true);
  }

  public void itemStateChanged(ItemEvent ie)
  {
    if(Auto.isSelected() == true)
      MixID.setText("Auto is Selected");
    if(Auto.isSelected() == false)
      MixID.setText("");


    if(EnableLog.isSelected() == true)
    {
      if(Daemon.isSelected() == false)
        Console.setEnabled(true);
      File.setEnabled(true);
      Syslog.setEnabled(true);
      logging = "True";
    }
    else
    {
      Console.setEnabled(false);
      File.setEnabled(false);
      Syslog.setEnabled(false);
      FileName.setEnabled(false);
      logging = "";
    }

    if(Daemon.isSelected() == true)
    {
      Console.setEnabled(false);
      is_daemon = "True";
    }
    if(Daemon.isSelected() == false)
      is_daemon = "";

    if(Daemon.isSelected() == true && Console.isSelected() == true)
    {
       File.setSelected(true);
       FileName.setEnabled(true);
    }

    if(Console.isSelected() == true || Syslog.isSelected() == true)
    {
      FileName.setEnabled(false);
    }

    if(UserID.isSelected() == true)
    {
      ID_Text.setEnabled(true);
      User_ID = "True";
    }
    else
    {
      ID_Text.setText("");
      ID_Text.setEnabled(false);
      User_ID = "";
    }

    if(number.isSelected() == true)
    {
      num_file.setEnabled(true);
      fileDes = "True";
    }
    else
    {
      num_file.setText("");
      num_file.setEnabled(false);
      fileDes = "";
    }
  }
}



class NetworkPanel extends JPanel
{
   JPanel panel1,panel2,panel3;
   JTable table1,table2;
   JTextField Host_Text,IP_Text,Port_Text;

   public String getTable1(int x,int y)
   {
     return table1.getValueAt(x,y).toString();
   }

   public String getTable2(int x,int y)
   {
     return table2.getValueAt(x,y).toString();
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
    panel1.setBorder(BorderFactory.createTitledBorder("Incomming"));
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
    panel2.setBorder(BorderFactory.createTitledBorder("Outgoing"));
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
    panel3.setBorder(BorderFactory.createTitledBorder("Information Server"));
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
  }
}


class CertificatesPanel extends JPanel implements ActionListener
{
   JPanel panel1,panel2,panel3;
   JTextField text1,from_text1,to_text1;
   JTextField text2,from_text2,to_text2;
   JTextField text3,from_text3,to_text3;
   JButton import1,import2,create;

   public String getOwnName()
   {
     return text1.getText();
   }
   public String getOwnFrom()
   {
     return from_text1.getText();
   }
   public String getOwnTo()
   {
     return to_text1.getText();
   }

   public String getPreviousName()
   {
     return text2.getText();
   }
   public String getPreviousFrom()
   {
     return from_text2.getText();
   }
   public String getPreviousTo()
   {
     return to_text2.getText();
   }

   public String getNextName()
   {
     return text3.getText();
   }
   public String getNextFrom()
   {
     return from_text3.getText();
   }
   public String getNextTo()
   {
     return to_text3.getText();
   }

   public void setName1(String name)
   {
     text1.setText(name);
   }
   public void setFrom1(String from)
   {
     from_text1.setText(from);
   }
   public void setTo1(String to)
   {
     to_text1.setText(to);
   }

   public void setName2(String name)
   {
     text2.setText(name);
   }
   public void setFrom2(String from)
   {
     from_text2.setText(from);
   }
   public void setTo2(String to)
   {
     to_text2.setText(to);
   }

   public void setName3(String name)
   {
     text3.setText(name);
   }
   public void setFrom3(String from)
   {
     from_text3.setText(from);
   }
   public void setTo3(String to)
   {
     to_text3.setText(to);
   }

   public CertificatesPanel()
  {
    GridBagLayout layout=new GridBagLayout();
    setLayout(layout);
    GridBagLayout Own=new GridBagLayout();
    GridBagLayout Previous=new GridBagLayout();
    GridBagLayout Next=new GridBagLayout();

    GridBagConstraints c=new GridBagConstraints();
    c.anchor=GridBagConstraints.NORTHWEST;
    c.insets=new Insets(10,10,10,10);
    c.fill = GridBagConstraints.HORIZONTAL;

    panel1 = new JPanel(Own);
    GridBagConstraints d=new GridBagConstraints();
    d.anchor=GridBagConstraints.NORTHWEST;
    d.insets=new Insets(5,5,5,5);
    panel1.setBorder(BorderFactory.createTitledBorder("Own Mix Certificate"));
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1;
    c.weighty = 1;
    layout.setConstraints(panel1,c);
    add(panel1);

    create = new JButton("Create a New One");
    d.gridx = 1;
    d.gridy = 0;
    d.gridwidth = 1;
    d.fill = GridBagConstraints.HORIZONTAL;
    create.addActionListener(this);
    create.setActionCommand("Create");
    Own.setConstraints(create,d);
    panel1.add(create);
    JButton passwd = new JButton("Change Password");
    d.gridx = 3;
    passwd.addActionListener(this);
    passwd.setActionCommand("passwd");
    Own.setConstraints(passwd,d);
    panel1.add(passwd);

    d.gridx = 0;
    d.gridy = 1;
    d.fill = GridBagConstraints.HORIZONTAL;
    JLabel name1 = new JLabel("Name");
    Own.setConstraints(name1,d);
    panel1.add(name1);
    text1 = new JTextField(20);
    d.gridx = 1;
    d.gridwidth = 3;
    d.weightx = 1;
    Own.setConstraints(text1,d);
    panel1.add(text1);

    JLabel from1 = new JLabel("Valid From");
    d.gridx = 0;
    d.gridy = 2;
    d.gridwidth = 1;
    d.weightx = 0;
    Own.setConstraints(from1,d);
    panel1.add(from1);
    from_text1 = new JTextField(20);
    d.gridx = 1;
    d.gridwidth = 3;
    d.weightx = 1;
    Own.setConstraints(from_text1,d);
    panel1.add(from_text1);

    JLabel to1 = new JLabel("Valid To");
    d.gridx = 0;
    d.gridy = 3;
    d.gridwidth = 1;
    d.weightx = 0;
    Own.setConstraints(to1,d);
    panel1.add(to1);
    to_text1 = new JTextField(20);
    d.gridx = 1;
    d.gridwidth = 3;
    d.weightx = 1;
    Own.setConstraints(to_text1,d);
    panel1.add(to_text1);

    c.gridx = 0;
    c.gridy = 1;
    panel2 = new JPanel(Previous);
    GridBagConstraints e=new GridBagConstraints();
    e.anchor=GridBagConstraints.NORTHWEST;
    e.insets=new Insets(5,5,5,5);
    e.fill = GridBagConstraints.HORIZONTAL;
  /*  panel2.setBorder(BorderFactory.createCompoundBorder(
		BorderFactory.createTitledBorder("Previous Mix Certificate"),
		BorderFactory.createEmptyBorder(0,0,0,0))); */
    panel2.setBorder(BorderFactory.createTitledBorder("Previous Mix Certificate"));
    layout.setConstraints(panel2,c);
    add(panel2);

    import1 = new JButton("Import...");
    e.gridx = 1;
    e.gridy = 0;
    import1.addActionListener(this);
    import1.setActionCommand("Import1");
    Previous.setConstraints(import1,e);
    panel2.add(import1);

    JLabel name2 = new JLabel("Name");
    e.gridx = 0;
    e.gridy = 1;
    Previous.setConstraints(name2,e);
    panel2.add(name2);
    text2 = new JTextField(26);
    e.gridx = 1;
    e.gridwidth = 3;
    e.weightx = 1;
    Previous.setConstraints(text2,e);
    panel2.add(text2);

    JLabel from2 = new JLabel("Valid From");
    e.gridx = 0;
    e.gridy = 2;
    e.gridwidth = 1;
    e.weightx = 0;
    Previous.setConstraints(from2,e);
    panel2.add(from2);
    from_text2 = new JTextField(26);
    e.gridx = 1;
    e.gridwidth = 4;
    e.weightx = 1;
    Previous.setConstraints(from_text2,e);
    panel2.add(from_text2);

    JLabel to2 = new JLabel("Valid To");
    e.gridx = 0;
    e.gridy = 3;
    e.gridwidth = 1;
    e.weightx = 0;
    Previous.setConstraints(to2,e);
    panel2.add(to2);
    to_text2 = new JTextField(26);
    e.gridx = 1;
    e.gridwidth = 4;
    e.weightx = 1;
    Previous.setConstraints(to_text2,e);
    panel2.add(to_text2);

    c.gridy = 2;
    panel3 = new JPanel(Next);
    GridBagConstraints f=new GridBagConstraints();
    f.anchor=GridBagConstraints.NORTHWEST;
    f.insets=new Insets(5,5,5,5);
    f.fill = GridBagConstraints.HORIZONTAL;
    panel3.setBorder(BorderFactory.createCompoundBorder(
		BorderFactory.createTitledBorder("Next Mix Certificate"),
		BorderFactory.createEmptyBorder(0,0,0,0)));
    layout.setConstraints(panel3,c);
    add(panel3);

    import2 = new JButton("Import...");
    f.gridx = 1;
    f.gridy = 0;
    import2.addActionListener(this);
    import2.setActionCommand("Import2");
    Next.setConstraints(import2,f);
    panel3.add(import2);

    JLabel name3 = new JLabel("Name");
    f.gridx = 0;
    f.gridy = 1;
    f.weightx = 0;
    Next.setConstraints(name3,f);
    panel3.add(name3);
    text3 = new JTextField(26);
    f.gridx = 1;
    f.gridwidth = 5;
    f.weightx = 1;
    Next.setConstraints(text3,f);
    panel3.add(text3);

    JLabel from3 = new JLabel("Valid From");
    f.gridx = 0;
    f.gridy = 2;
    f.gridwidth = 1;
    f.weightx = 0;
    Next.setConstraints(from3,f);
    panel3.add(from3);
    from_text3 = new JTextField(26);
    f.gridx = 1;
    f.gridwidth = 5;
    f.weightx = 1;
    Next.setConstraints(from_text3,f);
    panel3.add(from_text3);

    JLabel to3 = new JLabel("Valid To");
    f.gridx = 0;
    f.gridy = 3;
    f.gridwidth = 1;
    f.weightx = 0;
    Next.setConstraints(to3,f);
    panel3.add(to3);
    to_text3 = new JTextField(26);
    f.gridx = 1;
    f.gridwidth = 5;
    f.weightx = 1;
    Next.setConstraints(to_text3,f);
    panel3.add(to_text3);
  }

  public void actionPerformed(ActionEvent ae)
  {
    PasswdBox dialog;

    if(ae.getActionCommand().equals("Create"))
    {
      Security.addProvider(new BouncyCastleProvider());

      String mixid = MyFrame.m_GeneralPanel.MixID.getText();
      mixid=URLEncoder.encode(mixid);
      X509V3CertificateGenerator gen=new X509V3CertificateGenerator();
      gen.setSignatureAlgorithm("DSAWITHSHA1");
      gen.setNotBefore(new Date(System.currentTimeMillis() - 50000));
      gen.setNotAfter(new Date(System.currentTimeMillis() + 50000));

      gen.setIssuerDN(new X509Name("CN=<Mix id=\""+mixid+"\"/>"));
      gen.setSubjectDN(new X509Name("CN=<Mix id=\""+mixid+"\"/>"));
      gen.setSerialNumber(new BigInteger("1"));

      try
      {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator kpg=KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        KeyPair kp=kpg.generateKeyPair();
        gen.setPublicKey(kp.getPublic());
        X509Certificate cert=gen.generateX509Certificate(kp.getPrivate());

        //PKCS12 generation
        KeyStore kstore = KeyStore.getInstance("PKCS12","BC");
        kstore.load(null, null);

        Certificate[] chain=new  Certificate[1];
        chain[0] = cert;
        kstore.setKeyEntry("<Mix id=\""+mixid+"\"/>",(Key)kp.getPrivate(),(char[])null, chain);
        ByteArrayOutputStream out=new ByteArrayOutputStream();

        String passwd = "geetugarg";
        //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        //passwd = br.readLine();
        kstore.store(out, passwd.toCharArray());

        byte[] b = out.toByteArray();
        FileOutputStream fout = new FileOutputStream("garg.crt");
        fout.write(b);
        fout.close();

      }
      catch(Exception e)
      {
        System.out.println("Error in Key generation and storage!!");
        e.printStackTrace();
      }
    }

    if(ae.getActionCommand().equals("passwd"))
    {
      dialog = new PasswdBox(TheApplet.myFrame,"Change Password");
      dialog.setVisible(true);
    }

    if(ae.getActionCommand().equals("Import1"))
    {
       FileDialog fd = new FileDialog(TheApplet.myFrame,"Open File",FileDialog.LOAD);
       fd.show();
       String myFile = fd.getDirectory()+fd.getFile();
     if(fd.getFile() != null)
     {
       try
       {
         FileInputStream fin = new FileInputStream(myFile);
         CertificateFactory cf = CertificateFactory.getInstance("X.509");
         X509Certificate cert1 = (X509Certificate)cf.generateCertificate(fin);
         text2.setText(cert1.getSubjectDN().getName());
         from_text2.setText(cert1.getNotBefore().toString());
         to_text2.setText(cert1.getNotAfter().toString());

         byte[] bytes = cert1.getEncoded();
         Base64 base = new Base64();
         byte[] encoded = base.encode(bytes);
         String PrevMix = encoded.toString();
      }
      catch(Exception e)
	{
	  System.out.println("Error in getting certificate");
	  e.printStackTrace();
	}
      }

    }
    if(ae.getActionCommand().equals("Import2"))
    {
       FileDialog fd = new FileDialog(TheApplet.myFrame,"Open File",FileDialog.LOAD);
       fd.show();
       String myFile = fd.getDirectory()+fd.getFile();

      if(fd.getFile() != null)
      {
       try
       {
         FileInputStream fin = new FileInputStream("self.crt");
         CertificateFactory cf = CertificateFactory.getInstance("X.509");
         X509Certificate cert1 = (X509Certificate)cf.generateCertificate(fin);
         text3.setText(cert1.getSubjectDN().getName());
         from_text3.setText(cert1.getNotBefore().toString());
         to_text3.setText(cert1.getNotAfter().toString());

         byte[] bytes = cert1.getEncoded();
         Base64 base = new Base64();
         byte[] encoded = base.encode(bytes);
         String PrevMix = encoded.toString();
      }
      catch(Exception e)
	{
	  System.out.println("Error in getting certificate");
	  e.printStackTrace();
	}
      }
    }
  }
}


class DescriptionPanel extends JPanel implements ActionListener
{
  JPanel panel1;
  public static JButton map;
  JTextField text1,text2,longi,lati;
  public static MapBox box;

  public String getCity()
  {
    return text1.getText();
  }

  public String getState()
  {
    return text2.getText();
  }

  public String getLongitude()
  {
    return longi.getText();
  }

  public String getLatitude()
  {
    return lati.getText();
  }

  public void setCity(String city)
  {
    text1.setText(city);
  }
  public void setState(String state)
  {
    text2.setText(state);
  }
  public void setLati(String latitude)
  {
   lati.setText(latitude);
  }
  public void setLongi(String longitude)
  {
    longi.setText(longitude);
  }

  public DescriptionPanel()
  {
    GridBagLayout layout=new GridBagLayout();
    setLayout(layout);
    GridBagConstraints c=new GridBagConstraints();
    c.anchor=GridBagConstraints.NORTHWEST;
    c.insets=new Insets(10,10,10,10);

    GridBagLayout forpanel = new GridBagLayout();
    GridBagConstraints d = new GridBagConstraints();
    d.anchor=GridBagConstraints.NORTHWEST;
    d.insets=new Insets(10,10,10,10);

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1;
    c.weighty = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    panel1 = new JPanel(forpanel);
    panel1.setBorder(BorderFactory.createCompoundBorder(
		BorderFactory.createTitledBorder("Location"),
		BorderFactory.createEmptyBorder(0,0,0,0)));
    layout.setConstraints(panel1,c);
    add(panel1);

    d.gridx = 0;
    d.gridy = 0;
    d.fill = GridBagConstraints.HORIZONTAL;
    JLabel city = new JLabel("City");
    forpanel.setConstraints(city,d);
    panel1.add(city);
    text1 = new JTextField(20);
    d.gridx = 1;
    d.weightx = 1;
    d.gridwidth = 3;
    forpanel.setConstraints(text1,d);
    panel1.add(text1);

    d.gridx = 0;
    d.gridy = 2;
    d.weightx = 0;
    d.gridwidth = 1;
    JLabel state = new JLabel("State");
    forpanel.setConstraints(state,d);
    panel1.add(state);
    text2 = new JTextField(20);
    d.gridx = 1;
    d.gridwidth = 3;
    d.weightx = 1;
    forpanel.setConstraints(text2,d);
    panel1.add(text2);

    d.gridx = 0;
    d.gridy = 3;
    d.weightx = 1;
    d.gridwidth = 3;
    JLabel pos = new JLabel("Geographical Position");
    forpanel.setConstraints(pos,d);
    panel1.add(pos);
    map = new JButton("Show Map");
    map.addActionListener(this);
    map.setActionCommand("Map");
    d.gridx = 3;
    d.gridwidth = 1;
    d.weightx = 1;
    forpanel.setConstraints(map,d);
    panel1.add(map);
    JLabel longitude = new JLabel("Longitude");
    d.gridy = 4;
    d.gridx = 0;
    d.weightx = 0;
    forpanel.setConstraints(longitude,d);
    panel1.add(longitude);
    longi = new JTextField(20);
    d.gridx = 1;
    d.gridwidth = 3;
    d.weightx = 1;
    forpanel.setConstraints(longi,d);
    panel1.add(longi);
    JLabel latitude = new JLabel("Latitude");
    d.gridy = 5;
    d.gridx = 0;
    d.weightx = 0;
    d.gridwidth = 1;
    forpanel.setConstraints(latitude,d);
    panel1.add(latitude);
    lati = new JTextField(20);
    d.gridx = 1;
    d.weightx = 1;
    d.gridwidth = 3;
    forpanel.setConstraints(lati,d);
    panel1.add(lati);
  }

 public void actionPerformed(ActionEvent ae)
    {
      if(ae.getActionCommand().equals("Map"))
      {
          String Title = getMapIcon();
          box = new MapBox(TheApplet.myFrame,Title,5);
          box.setVisible(true);
          map.setText("Update");
          map.setActionCommand("Update");
      }

      if(ae.getActionCommand().equals("Update"))
      {
        String Title = getMapIcon();
        box.setTitle(Title);
        try
        {
          URL icon = new URL(MapBox.m_IconString);
          ImageIcon MapIcon = new ImageIcon(icon);
          box.map.setIcon(MapIcon);
          box.s.setValue(5);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
    }

    public String getMapIcon()
    {
       BufferedInputStream bissmall,bisbig;
       String Title = "";
        try
        {
          String site = "http://www.mapquest.com/maps/map.adp?latlongtype=decimal&latitude="+lati.getText()+"&longitude="+longi.getText();
          Title = "The location shown on the Map is :  Latitude = "+lati.getText()+"  Longitude = "+ longi.getText();
          URL urlsmall = new URL(site);
          bissmall = new BufferedInputStream(urlsmall.openStream(),500);

          int i = 0,j = 0;
          char small[] = {'i','z','e','=','b','i','g'};

          while(true)
          {
            if((char)bissmall.read() == 's')
            {
               for(j = 0; j < 7; j++)
                 if((char)bissmall.read() != small[j])
                   break;
            }
            if(j == 7)
              break;
          }
          String add_url = "http://www.mapquest.com/maps/map.adp?size=big";
          char address;
          address = (char)bissmall.read();
          while(address != '"')
          {
             add_url += address;
             address = (char)bissmall.read();
          }

          URL urlbig = new URL(add_url);
          bisbig = new BufferedInputStream(urlbig.openStream(),500);
          char big[] = {'q','m','a','p','g','e','n','d'};
          while(true)
          {
            if((char)bisbig.read() == 'm')
            {
               for(j = 0; j < 8; j++)
                 if((char)bisbig.read() != big[j])
                   break;
            }
            if(j == 8)
              break;
          }
          String big_url = "http://mq-mapgend.websys.aol.com:80/";
          address = (char)bisbig.read();
          while(address != '"')
          {
             big_url += address;
             address = (char)bisbig.read();
          }
          MapBox.m_IconString = big_url;
          MapBox.m_urlString = add_url;
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        return Title;
    }

}



class DialogBox extends Dialog implements ActionListener
{
   Label subject;
   DialogBox(MyFrame parent,String title)
   {
     super(parent,title,true);
     GridBagLayout layout=new GridBagLayout();
     setLayout(layout);
     GridBagConstraints c=new GridBagConstraints();
     c.anchor=GridBagConstraints.CENTER;
     c.insets=new Insets(10,10,10,10);
     setSize(300,100);

     subject = new Label(" ");
     c.gridx=0;
     c.gridy=0;
     c.weightx = 1;
     layout.setConstraints(subject,c);
     add(subject);

     Button b = new Button("OK");
     c.gridx = 0;
     c.gridy = 2;
     c.gridwidth = 2;
     layout.setConstraints(b,c);
     add(b);
     b.addActionListener(this);
    }

    public void setlabel(String name)
    {
      subject.setText(name);
    }

    public void actionPerformed(ActionEvent ae)
    {
      dispose();
    }
  }


 class NewDialogBox extends Dialog implements ActionListener
{
   Label subject;
   NewDialogBox(MyFrame parent,String title)
   {
     super(parent,title,true);
     GridBagLayout layout=new GridBagLayout();
     setLayout(layout);
     GridBagConstraints c=new GridBagConstraints();
     c.anchor=GridBagConstraints.CENTER;
     c.insets=new Insets(10,10,10,10);
     setSize(450,100);

     subject = new Label(" ");
     c.gridx=0;
     c.gridy=0;
     layout.setConstraints(subject,c);
     add(subject);

     Button ok = new Button("Yes");
     c.gridx = 0;
     c.gridy = 1;
     c.anchor = GridBagConstraints.WEST;
     layout.setConstraints(ok,c);
     add(ok);
     ok.addActionListener(this);

     Button cancel = new Button("Cancel");
     c.gridx = 1;
     layout.setConstraints(cancel,c);
     add(cancel);
     cancel.addActionListener(this);
    }

    public void setlabel(String name)
    {
      subject.setText(name);
    }

    public void actionPerformed(ActionEvent ae)
    {
      if(ae.getActionCommand().equals("Yes"))
        MyFrame.New = true;
      else
        MyFrame.New = false;
      dispose();
    }
  }


class PasswdBox extends Dialog implements ActionListener
 {
   JPasswordField text1,text2,text3;

   PasswdBox(MyFrame parent,String title)
   {
     super(parent,title,false);
     GridBagLayout layout=new GridBagLayout();
     setLayout(layout);
     GridBagConstraints c=new GridBagConstraints();
     c.anchor=GridBagConstraints.CENTER;
     c.insets=new Insets(10,10,10,10);
     setSize(400,200);

      JLabel old = new JLabel("Enter Old Password");
      c.gridx = 0;
      c.gridy = 0;
      layout.setConstraints(old,c);
      add(old);
      text1 = new JPasswordField(20);
      text1.setEchoChar('*');
      c.gridx = 3;
      c.weightx = 1;
      layout.setConstraints(text1,c);
      add(text1);

      JLabel new1 = new JLabel("Enter New Password");
      c.gridx = 0;
      c.gridy = 1;
      c.weightx = 0;
      layout.setConstraints(new1,c);
      add(new1);
      text2 = new JPasswordField(20);
      text2.setEchoChar('*');
      c.gridx = 3;
      c.weightx = 1;
      layout.setConstraints(text2,c);
      add(text2);

      JLabel new2 = new JLabel("Confirm Password");
      c.gridx = 0;
      c.gridy = 2;
      c.weightx = 0;
      layout.setConstraints(new2,c);
      add(new2);
      text3 = new JPasswordField(20);
      text3.setEchoChar('*');
      c.gridx = 3;
      c.weightx = 1;
      layout.setConstraints(text3,c);
      add(text3);

       Button b = new Button("OK");
       c.gridx = 3;
       c.gridy = 3;
       layout.setConstraints(b,c);
       add(b);
       b.addActionListener(this);
    }

    public void actionPerformed(ActionEvent ae)
    {
      dispose();
    }
  }


/*
class BrowserControl
{
    // The default system browser under windows.
    private static final String WIN_PATH = "rundll32";
    // The flag to display a url.
    private static final String WIN_FLAG = "url.dll,FileProtocolHandler";

  public static void displayURL(String url)
    {
        String cmd = null;
        try
        {
                //cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
                cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
                Process p = Runtime.getRuntime().exec(cmd);
                String process = p.getInputStream().toString();
                BufferedInputStream bis = new BufferedInputStream(p.getInputStream());
                int number = bis.available();
                System.out.println(process + "    " + number);

        }
        catch(IOExcepntion x)
        {
            // couldn't exec browser
            System.err.println("Could not invoke browser, command=" + cmd);
            System.err.println("Caught: " + x);
        }
    }
}*/