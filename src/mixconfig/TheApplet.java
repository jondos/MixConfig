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
               saveMenuItem.setText("Save ["+filename+"] ");
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

 if(m_CertificatesPanel.getOwnPrivCert()==null||m_CertificatesPanel.getOwnPubCert()==null)
       {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Certificates Panel !!");
	dialog.setlabel("Own Mix Certificate is missing!");
	dialog.setVisible(true);
	return false;
      }
      if(m_CertificatesPanel.getPrevPubCert()==null)
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Certificates Panel !!");
	dialog.setlabel("Previous Mix Certificate is missing!");
	dialog.setVisible(true);
	return false;
      }
      if(m_CertificatesPanel.getNextPubCert()==null)
      {
        dialog = new DialogBox(TheApplet.myFrame,"ERROR in Certificates Panel !!");
	dialog.setlabel("Next Mix Certificate is missing!");
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
    Security.addProvider(new BouncyCastleProvider());
    JFrame window = new JFrame("MixConfiguration Tool");
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
    window.show();
  }

   public void init()
  {
    Security.addProvider(new BouncyCastleProvider());

    myFrame = new MyFrame();
    myFrame.setBounds(10,10,600,650);
    setJMenuBar(myFrame.getJMenuBar());
    setContentPane(myFrame.getContentPane());

  }

}




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