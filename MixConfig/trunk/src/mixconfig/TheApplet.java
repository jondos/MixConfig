package mixconfig;

import java.util.*;
import java.net.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.*;
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
//import org.bouncycastle.jce.X509V3CertificateGenerator;
import org.bouncycastle.jce.provider.*;
//import org.bouncycastle.asn1.x509.X509Name;
//import org.bouncycastle.crypto.BlockCipher;


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
    JMenuItem openMenuItem = new JMenuItem("Open...");
    JMenuItem openclipItem = new JMenuItem("Open Using Clip Board");
    saveMenuItem = new JMenuItem("Save [none]");
    saveclipItem = new JMenuItem("Save Using Clip Board");
    JMenuItem saveAsMenuItem = new JMenuItem("Save as...");

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
        int ret=JOptionPane.showConfirmDialog(TheApplet.myFrame,
                                              "You will lose unsaved information. Do you want to continue?",
                                              "Caution!",JOptionPane.OK_CANCEL_OPTION);
        if(ret==JOptionPane.OK_OPTION)
           reset();
      }
      else if(evt.getActionCommand().equals("Exit"))
      {
	      dispose();
	      System.exit(0);
      }
      else if(evt.getActionCommand().equals("Save"))
      {
        if(filename != "" && check())
          save(filename);
      }
      else if(evt.getActionCommand().equals("SaveAs"))
      {
     //   if(check())
        {
        try{
           File file = TheApplet.showFileDialog(TheApplet.SAVE_DIALOG,TheApplet.FILTER_XML);
           if(file!=null)
           {
               save(file.getCanonicalPath());
               saveMenuItem.setText("Save ["+file.getName()+"] ");
               saveMenuItem.setEnabled(true);
           }}
           catch(Exception e){};
         }
      }

      else if(evt.getActionCommand().equals("OpenClip"))
      {
        ClipFrame Open = new ClipFrame("Paste a file to be opened in the area provided.",true);
      }

      else if(evt.getActionCommand().equals("SaveClip"))
      {
        try
        {
          if(check())
          {
            ClipFrame Save = new ClipFrame("Copy and Save this file in a new Location.",false);
            Save.setText(new String(save_internal()));
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }

      if(evt.getActionCommand().equals("Open"))
      {
	      File file=TheApplet.showFileDialog(TheApplet.OPEN_DIALOG,TheApplet.FILTER_XML);
        if(file!=null)
          {
            try
              {
                open(file.getCanonicalPath());
                saveMenuItem.setText("Save ["+file.getName()+"] ");
                saveMenuItem.setEnabled(true);
              }
            catch(Exception e)
              {
                e.printStackTrace();
              }
          }
      }
    }

    public static void reset()
  {
     saveMenuItem.setText("Save [none]");
     saveMenuItem.setEnabled(false);
     m_GeneralPanel.clear();

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

     m_CertificatesPanel.clear();

     m_DescriptionPanel.setCity("");
     m_DescriptionPanel.setLati("");
     m_DescriptionPanel.setLongi("");
     m_DescriptionPanel.setState("");
   }

   private Element getChild(Node node,String name)
    {
      if(node==null)
        return null;
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

    private String getElementValue(Element elem,String def)
      {
        if(elem==null)
          return def;
        Node n=elem.getFirstChild();
        if(n==null||n.getNodeType()!=n.TEXT_NODE)
          return def;
        return n.getNodeValue();
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
	  String MixType=getElementValue(elemType,null);
	  m_GeneralPanel.setMixType(MixType);

	  Element elemName=getChild(elemGeneral,"MixName");
	  String MixName=getElementValue(elemName,null);
	  m_GeneralPanel.setMixName(MixName);

	  Element elemMixID=getChild(elemGeneral,"MixID");
    m_GeneralPanel.setAuto(false);
    String MixID = getElementValue(elemMixID,null);
	  m_GeneralPanel.setMixID(MixID);

	  Element elemUserID = getChild(elemGeneral,"UserID");
	  if(elemUserID != null)
	  {
	    m_GeneralPanel.setUserID(getElementValue(elemUserID,null));
	  }

	  Element elemFileDes = getChild(elemGeneral,"NrOfFileDescriptors");
	  if(elemFileDes != null)
	  {
	    m_GeneralPanel.setFileDes(getElementValue(elemFileDes,null));
	  }

          Element elemDaemon = getChild(elemGeneral,"Daemon");
	  String daemon = getElementValue(elemDaemon,"False");
    m_GeneralPanel.setDaemon(daemon);

	  Element elemEnableLog = getChild(elemGeneral,"Logging");
	  if(elemEnableLog != null)
	  {
      boolean bLogFile=false;
      boolean bLogConsole=false;
      boolean bLogSyslog=false;
      String file=null;
      Element elemFile = getChild(elemEnableLog,"File");
      if(elemFile != null)
        {
          file = getElementValue(elemFile,null);
          bLogFile=true;
        }
      Element elemSyslog = getChild(elemEnableLog,"SysLog");
      if(elemSyslog != null)
        {
          bLogSyslog=getElementValue(elemSyslog,"False").equalsIgnoreCase("true");
        }
      Element elemConsole = getChild(elemEnableLog,"Console");
      if(elemConsole != null)
        {
          bLogConsole=getElementValue(elemSyslog,"False").equalsIgnoreCase("true");
        }
      m_GeneralPanel.setLogging(bLogConsole,bLogSyslog,bLogFile,file);
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
	  main = getElementValue(elemMain,"False");
	  m_NetworkPanel.setTable1(main,i,j);
	  j++;
	  elemTransport = getChild(elemInterface,"Transport");
	  transport = getElementValue(elemTransport,"");
	  m_NetworkPanel.setTable1(transport,i,j);

	  if(transport.equals("RAW/TCP") || transport.equals("SSL/TCP"))
	  {
	    j++;
	    elemHost = getChild(elemInterface,"Host");
	    host = getElementValue(elemHost,null);
	    m_NetworkPanel.setTable1(host,i,j);
	    j++;
	    elemIP = getChild(elemInterface,"IP");
	    if(elemIP != null)
	    {
	      IP = getElementValue(elemIP,null);
	      m_NetworkPanel.setTable1(IP,i,j);
	    }
	    j++;
	    elemPort = getChild(elemInterface,"Port");
	    if(elemPort!=null)
        {
          port = getElementValue(elemPort,"");
	        m_NetworkPanel.setTable1(port,i,j);
        }
	  }
	  else
	  {
	    j++;
	    Element elemFile = getChild(elemInterface,"File");
	    file = getElementValue(elemFile,null);
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
	  main = getElementValue(elemMain,"False");
	  m_NetworkPanel.setTable2(main,i,j);
	  j++;

	  elemKind = getChild(elemInterface,"Kind");
	  kind = getElementValue(elemKind,null);
	  m_NetworkPanel.setTable2(kind,i,j);
	  j++;

	  elemTransport = getChild(elemInterface,"Transport");
	  transport = getElementValue(elemTransport,"");
	  m_NetworkPanel.setTable2(transport,i,j);

	  if(transport.equals("TCP"))
	  {
	    j++;
	    elemHost = getChild(elemInterface,"Host");
	    host = getElementValue(elemHost,"");
	    m_NetworkPanel.setTable2(host,i,j);
	    j++;
	    elemIP = getChild(elemInterface,"IP");
	    if(elemIP != null)
	    {
	      IP = getElementValue(elemIP,"");
	      m_NetworkPanel.setTable2(IP,i,j);
	    }
	    j++;
	    elemPort = getChild(elemInterface,"Port");
	    port = getElementValue(elemPort,"");
	    m_NetworkPanel.setTable2(port,i,j);
	  }
	  else
	  {
	    j++;
	    Element elemFile = getChild(elemInterface,"File");
	    file = getElementValue(elemFile,null);
	    m_NetworkPanel.setTable2(file,i,j);
	  }
	  i++;
	  j = 1;
	 elemInterface = getChild(elemOutgoing,"ListenerInterface"+Integer.toString(i+1));
        }

	Element elemInfoServer = getChild(elemNetwork,"InformationServer");
	elemHost = getChild(elemInfoServer,"Host");
	host = getElementValue(elemHost,null);
	m_NetworkPanel.setInfoHost(host);
	elemIP = getChild(elemInfoServer,"IP");
	if(elemIP != null)
	{
	  IP = elemIP.getFirstChild().getNodeValue();
	  m_NetworkPanel.setInfoIP(IP);
	}
	elemPort = getChild(elemInfoServer,"Port");
	port = getElementValue(elemPort,null);
	m_NetworkPanel.setInfoPort(port);

	Element elemCertificates = getChild(root,"Certificates");
	Element elemOwnCert = getChild(elemCertificates,"OwnCertificate");
	Element elem = getChild(elemOwnCert,"X509PKCS12");
	String name = getElementValue(elem,null);
  if(name!=null)
    {
      PasswordBox pb=new PasswordBox(TheApplet.myFrame,"Enter the password",PasswordBox.ENTER_PASSWORD);
      pb.show();
      m_CertificatesPanel.setOwnPrivCert(Base64.decode(name),pb.getPassword());
    }
  else
    m_CertificatesPanel.setOwnPrivCert(null,null);
	Element elemPrevCert = getChild(elemCertificates,"PrevMixCertificate");
	elem = getChild(elemPrevCert,"X509Certificate");
	name = getElementValue(elem,null);
	if(name==null)
    m_CertificatesPanel.setPrevPubCert(null);
  else
    m_CertificatesPanel.setPrevPubCert(Base64.decode(name));

	Element elemNextCert = getChild(elemCertificates,"NextMixCertificate");
	elem = getChild(elemPrevCert,"X509Certificate");
	name = getElementValue(elem,null);
	if(name==null)
    m_CertificatesPanel.setNextPubCert(null);
  else
    m_CertificatesPanel.setNextPubCert(Base64.decode(name));

	Element elemDescription = getChild(root,"Description");
	Element elemLocation=getChild(elemDescription,"Location");
  Element elemCity = getChild(elemLocation,"City");
	String city = getElementValue(elemCity,null);
	m_DescriptionPanel.setCity(city);
  Element elemState = getChild(elemLocation,"State");
	String state = getElementValue(elemState,null);
	m_DescriptionPanel.setState(state);

  Element elemGeo=getChild(getChild(elemLocation,"Position"),"Geo");
	Element elemLongi = getChild(elemGeo,"Longitude");
  String longi = getElementValue(elemLongi,null);
  m_DescriptionPanel.setLongi(longi);

	Element elemLati = getChild(elemGeo,"Latitude");
  String lati = getElementValue(elemLati,null);
  m_DescriptionPanel.setLati(lati);
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


	  String mixID=m_GeneralPanel.getMixID();
    Element elemMixID=doc.createElement("MixID");
    elemGeneral.appendChild(elemMixID);
	  Text text3=doc.createTextNode(mixID);
	  elemMixID.appendChild(text3);

	  String elemUser_ID = m_GeneralPanel.getUserID();
	  if(elemUser_ID!=null&&!elemUser_ID.equals(""))
	  {
	    Element elemUserID = doc.createElement("UserID");
	    elemGeneral.appendChild(elemUserID);
	    Text text4 = doc.createTextNode(elemUser_ID);
	    elemUserID.appendChild(text4);
	  }

	  String elemFileDes = m_GeneralPanel.getFileDes();
	  if(elemFileDes!=null&&!elemFileDes.equals(""))
	  {
	    Element elemfiledes = doc.createElement("NrOfFileDescriptors");
	    elemGeneral.appendChild(elemfiledes);
	    Text text4 = doc.createTextNode(elemFileDes);
	    elemfiledes.appendChild(text4);
	  }

	  Element elemDaemon = doc.createElement("Daemon");
	  elemGeneral.appendChild(elemDaemon);
	  Text text5 = doc.createTextNode(m_GeneralPanel.getDaemon());
	  elemDaemon.appendChild(text5);

    if(m_GeneralPanel.isLoggingEnabled())
	  {
	    Element elemLogging = doc.createElement("Logging");
	    elemGeneral.appendChild(elemLogging);
	    String LogInfo = m_GeneralPanel.getEnabled();
	    if(LogInfo.equals("LogtoConsole"))
	    {
	      Element elemLog = doc.createElement("Console");
	      elemLogging.appendChild(elemLog);
	      Text text6 = doc.createTextNode("True");
	      elemLog.appendChild(text6);
	    }
	    if(LogInfo.equals("LogtoSyslog"))
	    {
	      Element elemLog = doc.createElement("SysLog");
	      elemLogging.appendChild(elemLog);
	    }
	    if(LogInfo.equals("Logtofile"))
	    {
	      String filename = m_GeneralPanel.getFileName();
	      Element elemLog = doc.createElement("File");
	      elemLogging.appendChild(elemLog);
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

	  Element elem = doc.createElement("X509PKCS12");
	  elemOwn.appendChild(elem);
    byte[] buff = m_CertificatesPanel.getOwnPrivCert();
	   Text text = doc.createTextNode(Base64.encodeBytes(buff,true));
	  elem.appendChild(text);
	  elem = doc.createElement("X509Certificate");
	  elemOwn.appendChild(elem);
    buff = m_CertificatesPanel.getOwnPubCert();
    text = doc.createTextNode(Base64.encodeBytes(buff,true));
	  elem.appendChild(text);

	  Element elemPrevious = doc.createElement("PrevMixCertificate");
	  elemCertificate.appendChild(elemPrevious);
	  elem = doc.createElement("X509Certificate");
    elemPrevious.appendChild(elem);
    buff = m_CertificatesPanel.getPrevPubCert();
    text = doc.createTextNode(Base64.encodeBytes(buff,true));
	  elem.appendChild(text);

    Element elemNext = doc.createElement("NextMixCertificate");
	  elemCertificate.appendChild(elemNext);
	  elem = doc.createElement("X509Certificate");
    elemNext.appendChild(elem);
    buff = m_CertificatesPanel.getNextPubCert();
    text = doc.createTextNode(Base64.encodeBytes(buff));
	  elem.appendChild(text);

	  Element elemDescription = doc.createElement("Description");
	  root.appendChild(elemDescription);
	  Element elemLocation=doc.createElement("Location");
    elemDescription.appendChild(elemLocation);
    Element elemCity = doc.createElement("City");
	  elemLocation.appendChild(elemCity);
	  String city = m_DescriptionPanel.getCity();
	  Text text11 = doc.createTextNode(city);
	  elemCity.appendChild(text11);

	  String State = m_DescriptionPanel.getState();
	  text11 = doc.createTextNode(State);
	  Element elemState = doc.createElement("State");
	  elemLocation.appendChild(elemState);
	  elemState.appendChild(text11);

	  Element elemPosition=doc.createElement("Position");
    elemLocation.appendChild(elemPosition);
    Element elemGeo=doc.createElement("Geo");
    elemPosition.appendChild(elemGeo);
    String Position = m_DescriptionPanel.getLongitude();
	  Element elemLong = doc.createElement("Longitude");
	  text11 = doc.createTextNode(Position);
	  elemGeo.appendChild(elemLong);
	  elemLong.appendChild(text11);
	  Position = m_DescriptionPanel.getLatitude();
	  Element elemLati = doc.createElement("Latitude");
	  text11 = doc.createTextNode(Position);
	  elemLati.appendChild(text11);
	  elemGeo.appendChild(elemLati);

	  //Writing to File...
	  ByteArrayOutputStream fout=new ByteArrayOutputStream();
	  Transformer trans=TransformerFactory.newInstance().newTransformer();
    trans.setOutputProperty("indent","yes");
	  trans.getOutputProperties().list(System.out);
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
      if(m_GeneralPanel.getMixName().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.myFrame,"Please enter a Mix Name.",
                            "Error in General Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }
      if(m_GeneralPanel.getMixID().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.myFrame,"Mix ID field is blank.",
                            "Error in General Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }

      if(m_CertificatesPanel.getOwnPrivCert()==null||m_CertificatesPanel.getOwnPubCert()==null)
        {
          JOptionPane.showMessageDialog(TheApplet.myFrame,"Own Mix Certificate is missing.",
                            "Error in Certificates Panel!",JOptionPane.ERROR_MESSAGE);
          return false;
        }
      if(m_CertificatesPanel.getPrevPubCert()==null)
      {
          JOptionPane.showMessageDialog(TheApplet.myFrame,"Previous Mix Certificate is missing.",
                            "Error in Certificates Panel!",JOptionPane.ERROR_MESSAGE);
	        return false;
      }
      if(m_CertificatesPanel.getNextPubCert()==null)
      {
         JOptionPane.showMessageDialog(TheApplet.myFrame,"Next Mix Certificate is missing.",
                            "Error in Certificates Panel!",JOptionPane.ERROR_MESSAGE);
	        return false;
      }

      if(m_DescriptionPanel.getCity().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.myFrame,"The city field cannot be left blank!",
                            "Error in Description Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }
      if(m_DescriptionPanel.getState().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.myFrame,"The state field cannot be left blank!",
                            "Error in Description Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }
      if(m_DescriptionPanel.getLatitude().equals("") && !m_DescriptionPanel.getLongitude().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.myFrame,"Fill in the Latitude.",
                            "Error in Description Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }
      if(!m_DescriptionPanel.getLatitude().equals("") && m_DescriptionPanel.getLongitude().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.myFrame,"Fill in the Longitude.",
                            "Error in Description Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }

      if(m_NetworkPanel.getHost().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.myFrame,"The HOST field should not be blank.",
                            "Error in Network Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }
      if(m_NetworkPanel.getPort().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.myFrame,"The PORT field should not be blank.",
                            "Error in Network Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }

        return true;
    }

}


public class TheApplet extends JApplet
{
  public static MyFrame myFrame;
  public final static int SAVE_DIALOG=1;
  public final static int OPEN_DIALOG=2;
  public final static int FILTER_CER=1;
  public final static int FILTER_XML=2;

  public static void main(String[] args)
  {
    Security.addProvider(new BouncyCastleProvider());
    JFrame window = new JFrame("Mix Configuration Tool");
    myFrame = new MyFrame();
    //myFrame.setBounds(10,10,600,650);

    window.addWindowListener(new WindowAdapter()
    {
       public void windowClosing(WindowEvent e)
       {
           System.exit(0);
       }
    });

    window.setJMenuBar(myFrame.getJMenuBar());
    window.setContentPane(myFrame.getContentPane());
    window.pack();
    Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
    Dimension size=window.getSize();
    window.setLocation((d.width-size.width)/2,(d.height-size.height)/2);
    window.show();
  }

   public void init()
  {
    //Security.addProvider(new BouncyCastleProvider());

    myFrame = new MyFrame();
    myFrame.pack();//setBounds(10,10,600,650);
    setJMenuBar(myFrame.getJMenuBar());
    setContentPane(myFrame.getContentPane());

  }


     public static File showFileDialog(int type, int filter_type)
      {
        JFileChooser fd2= new JFileChooser();
        fd2.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fd2.addChoosableFileFilter(new SimpleFileFilter(filter_type));
        fd2.setFileHidingEnabled(false);
        if(type==SAVE_DIALOG)
          fd2.showSaveDialog(myFrame);
        else
          fd2.showOpenDialog(myFrame);
        return fd2.getSelectedFile();
      }

}




