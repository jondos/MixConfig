package mixconfig;

import java.util.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.BorderLayout;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;

import java.net.URLEncoder;
import java.net.URLDecoder;

import javax.swing.border.EmptyBorder;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;

import javax.swing.border.TitledBorder;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


//  Creating a Frame.........

 class MyFrame extends JPanel implements ActionListener
{
  private static final String TITLE="Mix Configuration Tool";
  private String m_aktFileName;
  private JFrame m_Parent;
  private JMenuBar m_MenuBar;
  //public static boolean New;
  private JMenuItem saveMenuItem,saveclipItem;

  protected static GeneralPanel m_GeneralPanel;
  private static NetworkPanel m_NetworkPanel;
  private static CertificatesPanel m_CertificatesPanel;
  private static DescriptionPanel m_DescriptionPanel;

  public MyFrame(JFrame parent)
  {
    m_Parent=parent;

    m_MenuBar = new JMenuBar();
    //setJMenuBar(mb);
    JMenu fileMenu = new JMenu("File");
    m_MenuBar.add(fileMenu);
    JMenu helpMenu = new JMenu("Help");
    m_MenuBar.add(helpMenu);

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

    JMenuItem aboutMenuItem = new JMenuItem("About...");
    helpMenu.add(aboutMenuItem);
    aboutMenuItem.setActionCommand("About");
    aboutMenuItem.addActionListener(this);
    m_aktFileName=null;

    JTabbedPane jtp = new JTabbedPane();
    m_GeneralPanel=new GeneralPanel();
    m_NetworkPanel = new NetworkPanel();
    m_CertificatesPanel = new CertificatesPanel();
    m_DescriptionPanel = new DescriptionPanel();

    jtp.addTab("General",m_GeneralPanel);
    jtp.addTab("Network",m_NetworkPanel);
    jtp.addTab("Certificates",m_CertificatesPanel);
    jtp.addTab("Description",m_DescriptionPanel);
    jtp.setBorder(new EmptyBorder(10,10,10,10));
    setLayout(new BorderLayout());
    add(jtp,BorderLayout.CENTER);
  }

  public JMenuBar getMenuBar()
    {
      return m_MenuBar;
    }

    public void actionPerformed(ActionEvent evt)
    {
      if(evt.getActionCommand().equals("New"))
      {
        int ret=JOptionPane.showConfirmDialog(TheApplet.getMainWindow(),
                                              "You will lose unsaved information. Do you want to continue?",
                                              "Caution!",JOptionPane.OK_CANCEL_OPTION);
        if(ret==JOptionPane.OK_OPTION)
           reset();
      }
      else if(evt.getActionCommand().equals("Exit"))
      {
	      //dispose();
	      System.exit(0);
      }
      else if(evt.getActionCommand().equals("Save"))
      {
        if(m_aktFileName!= null && check())
          save(m_aktFileName);
      }
      else if(evt.getActionCommand().equals("SaveAs"))
      {
     //   if(check())
        {
        try{
           File file = TheApplet.showFileDialog(TheApplet.SAVE_DIALOG,TheApplet.FILTER_XML).getSelectedFile();
           if(file!=null)
           {
               save(file.getCanonicalPath());
               saveMenuItem.setText("Save ["+file.getName()+"] ");
               saveMenuItem.setEnabled(true);
               m_aktFileName=file.getCanonicalPath();
              setTitle(TITLE+" - "+m_aktFileName);
           }}
           catch(Exception e){};
         }
      }

      else if(evt.getActionCommand().equals("OpenClip"))
      {
        ClipFrame Open = new ClipFrame("Paste a file to be opened in the area provided.",true);
        open_internal(Open.getText().getBytes());
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

      else if(evt.getActionCommand().equals("Open"))
      {
	      File file=TheApplet.showFileDialog(TheApplet.OPEN_DIALOG,TheApplet.FILTER_XML).getSelectedFile();
        if(file!=null)
          {
            try
              {
                open(file.getCanonicalPath());
                saveMenuItem.setText("Save ["+file.getName()+"] ");
                saveMenuItem.setEnabled(true);
                m_aktFileName=file.getCanonicalPath();
                setTitle(TITLE+" - "+m_aktFileName);
              }
            catch(Exception e)
              {
                e.printStackTrace();
              }
          }
      }
       else if(evt.getActionCommand().equals("About"))
        {
          JOptionPane.showMessageDialog(TheApplet.getMainWindow(),
                                        "Mix Configuration Tool\nVersion: "+TheApplet.VERSION,
                                        "About",JOptionPane.INFORMATION_MESSAGE,TheApplet.loadImage("icon.gif"));
        }
    }

    private void setTitle(String s)
      {
        if(m_Parent!=null)
          m_Parent.setTitle(s);
      }
    private void reset()
  {
     setTitle(TITLE);
     saveMenuItem.setText("Save [none]");
     saveMenuItem.setEnabled(false);
     m_aktFileName=null;
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

  private void open(String filename)
    {
      try
        {
          File f=new File(filename);
          int len=(int)f.length();
          byte[] buff=new byte[len];
          FileInputStream fin=new FileInputStream(f);
          fin.read(buff);
          fin.close();
          open_internal(buff);
        }
      catch(Exception e)
        {
          e.printStackTrace();
        }
    }

   private void open_internal(byte[] config)
    {
      try
	{
	  DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
	  DocumentBuilder docBuilder=factory.newDocumentBuilder();
	  Document doc=docBuilder.parse(new ByteArrayInputStream(config));

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
	  if(MixID!=null)
      MixID=URLDecoder.decode(MixID);
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
          Node netChild = elemNetwork.getFirstChild();
          while(netChild!=null)
          {
              if(netChild.getNodeType()==netChild.ELEMENT_NODE)
                  m_NetworkPanel.getIncomingModel().readFromElement((Element)netChild);
              netChild = netChild.getNextSibling();
          }
	Element elemOutgoing = getChild(elemNetwork,"Outgoing");
	int i = 0;
	int j = 1;
	Element elemKind;
        String main,transport,host,IP,port,kind,file;
        Element elemMain , elemTransport,elemHost,elemIP,elemPort;
	Element elemInterface = getChild(elemOutgoing,"ListenerInterface"+Integer.toString(i+1));

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

	Element elemInfoServer = getChild(elemNetwork,"InfoService");
	elemHost = getChild(elemInfoServer,"Host");
	host = getElementValue(elemHost,null);
	m_NetworkPanel.setInfoHost(host);
	elemIP = getChild(elemInfoServer,"IP");
	if(elemIP != null)
	{
	  IP = getElementValue(elemIP,null);
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
      System.out.println("Loading own Priv-Cert");
      m_CertificatesPanel.setOwnPrivCert(Base64.decode(name));
    }
  else
    m_CertificatesPanel.setOwnPrivCert(null);
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

    private void save(String fileName)
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
	  Text text3=doc.createTextNode(URLEncoder.encode(mixID));
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
          if(m_NetworkPanel.getIncomingModel()!=null)
              elemNetwork.appendChild(m_NetworkPanel.getIncomingModel().createAsElement(doc));
/*
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
*/
	  Element elemInfoSer = doc.createElement("InfoService");
	  elemNetwork.appendChild(elemInfoSer);
	  String Host = m_NetworkPanel.getHost();
	  Element elemHost = doc.createElement("Host");
	  elemInfoSer.appendChild(elemHost);
	  Text text9 = doc.createTextNode(Host);
	  elemHost.appendChild(text9);

	  String IP_Text = m_NetworkPanel.getIP();
    if(!IP_Text.equals(""))
	  {
	    Element elemIP = doc.createElement("IP");
	    elemInfoSer.appendChild(elemIP);
	    text9 = doc.createTextNode(IP_Text);
	    elemIP.appendChild(text9);
	  }

	  String Port = m_NetworkPanel.getPort();
	  Element elemPort = doc.createElement("Port");
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
    Text text;
    if(buff!=null)
    {
	   text = doc.createTextNode(Base64.encodeBytes(buff,true));
	  elem.appendChild(text);
    }
	  elem = doc.createElement("X509Certificate");
	  elemOwn.appendChild(elem);
    buff = m_CertificatesPanel.getOwnPubCert();
    if(buff!=null)
    {
          text = doc.createTextNode(Base64.encodeBytes(buff,true));
	  elem.appendChild(text);
    }
    buff = m_CertificatesPanel.getPrevPubCert();
	  if(buff!=null)
      {
        Element elemPrevious = doc.createElement("PrevMixCertificate");
	      elemCertificate.appendChild(elemPrevious);
	      elem = doc.createElement("X509Certificate");
        elemPrevious.appendChild(elem);
        text = doc.createTextNode(Base64.encodeBytes(buff,true));
	      elem.appendChild(text);
      }
    buff = m_CertificatesPanel.getNextPubCert();
	  if(buff!=null)
      {
        Element elemNext = doc.createElement("NextMixCertificate");
        elemCertificate.appendChild(elemNext);
        elem = doc.createElement("X509Certificate");
        elemNext.appendChild(elem);
        buff = m_CertificatesPanel.getNextPubCert();
        text = doc.createTextNode(Base64.encodeBytes(buff));
        elem.appendChild(text);
      }
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
    try{trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");}catch(Exception e){e.printStackTrace();};
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

  private boolean check()
    {
      if(m_GeneralPanel.getMixName().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.getMainWindow(),"Please enter a Mix Name.",
                            "Error in General Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }
      if(m_GeneralPanel.getMixID().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.getMainWindow(),"Mix ID field is blank.",
                            "Error in General Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }

      if(m_CertificatesPanel.getOwnPrivCert()==null||m_CertificatesPanel.getOwnPubCert()==null)
        {
          JOptionPane.showMessageDialog(TheApplet.getMainWindow(),"Own Mix Certificate is missing.",
                            "Error in Certificates Panel!",JOptionPane.ERROR_MESSAGE);
          return false;
        }
      if(m_CertificatesPanel.getPrevPubCert()==null)
      {
          JOptionPane.showMessageDialog(TheApplet.getMainWindow(),"Previous Mix Certificate is missing.",
                            "Error in Certificates Panel!",JOptionPane.ERROR_MESSAGE);
	        return false;
      }
      if(m_CertificatesPanel.getNextPubCert()==null)
      {
         JOptionPane.showMessageDialog(TheApplet.getMainWindow(),"Next Mix Certificate is missing.",
                            "Error in Certificates Panel!",JOptionPane.ERROR_MESSAGE);
	        return false;
      }

      if(m_DescriptionPanel.getCity().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.getMainWindow(),"The city field cannot be left blank!",
                            "Error in Description Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }
      if(m_DescriptionPanel.getState().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.getMainWindow(),"The state field cannot be left blank!",
                            "Error in Description Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }
      if(m_DescriptionPanel.getLatitude().equals("") && !m_DescriptionPanel.getLongitude().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.getMainWindow(),"Fill in the Latitude.",
                            "Error in Description Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }
      if(!m_DescriptionPanel.getLatitude().equals("") && m_DescriptionPanel.getLongitude().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.getMainWindow(),"Fill in the Longitude.",
                            "Error in Description Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }

      if(m_NetworkPanel.getHost().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.getMainWindow(),"The HOST field should not be blank.",
                            "Error in Network Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }
      if(m_NetworkPanel.getPort().equals(""))
      {
        JOptionPane.showMessageDialog(TheApplet.getMainWindow(),"The PORT field should not be blank.",
                            "Error in Network Panel!",JOptionPane.ERROR_MESSAGE);
	      return false;
      }

        return true;
    }

}


public class TheApplet extends JApplet
{
  private static MyFrame myFrame;
  private static JFrame m_MainWindow;
  public final static int SAVE_DIALOG=1;
  public final static int OPEN_DIALOG=2;
  public final static int FILTER_CER=1;
  public final static int FILTER_XML=2;
  public final static int FILTER_PFX=4;
  public final static String VERSION="00.01.012";

  public static void main(String[] args)
  {
    Security.addProvider(new BouncyCastleProvider());
    m_MainWindow = new JFrame("Mix Configuration Tool");
    ImageIcon icon=loadImage("icon.gif");
    if(icon!=null)
      m_MainWindow.setIconImage(icon.getImage());
    myFrame = new MyFrame(m_MainWindow);

    m_MainWindow.addWindowListener(new WindowAdapter()
    {
       public void windowClosing(WindowEvent e)
       {
           System.exit(0);
       }
    });

    m_MainWindow.setJMenuBar(myFrame.getMenuBar());
    m_MainWindow.setContentPane(myFrame);
    m_MainWindow.pack();
    Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
    Dimension size=m_MainWindow.getSize();
    m_MainWindow.setLocation((d.width-size.width)/2,(d.height-size.height)/2);
    m_MainWindow.show();
  }

   public void init()
  {
    //Security.addProvider(new BouncyCastleProvider());
    m_MainWindow=null;
    myFrame = new MyFrame(null);
    //myFrame.pack();//setBounds(10,10,600,650);
    setJMenuBar(myFrame.getMenuBar());
    setContentPane(myFrame);

  }

  public static JFrame getMainWindow()
    {
      return m_MainWindow;
    }

   public static ImageIcon loadImage(String name)
    {
        return new ImageIcon(TheApplet.class.getResource(name));
	   }

     public static JFileChooser showFileDialog(int type, int filter_type)
      {
        JFileChooser fd2= new JFileChooser();
        fd2.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if((filter_type&FILTER_CER)!=0)
          fd2.addChoosableFileFilter(new SimpleFileFilter(FILTER_CER));
        if((filter_type&FILTER_XML)!=0)
          fd2.addChoosableFileFilter(new SimpleFileFilter(FILTER_XML));
        if((filter_type&FILTER_PFX)!=0)
          fd2.addChoosableFileFilter(new SimpleFileFilter(FILTER_PFX));
        fd2.setFileHidingEnabled(false);
        if(type==SAVE_DIALOG)
          fd2.showSaveDialog(myFrame);
        else
          fd2.showOpenDialog(myFrame);
        return fd2;
      }

}




