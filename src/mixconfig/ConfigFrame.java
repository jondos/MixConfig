package mixconfig;

import java.awt.BorderLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;


/**
 * The Frame of the MixConfig Application.
 */
public class ConfigFrame extends JPanel implements ActionListener
{
		private static final String TITLE = "Mix Configuration Tool";
		private String m_aktFileName;
		private JFrame m_Parent;
		private JMenuBar m_MenuBar;
		//public static boolean New;
		private JMenuItem saveMenuItem, saveclipItem;

		protected static GeneralPanel m_GeneralPanel;
		protected static NetworkPanel m_NetworkPanel;
		protected static CertificatesPanel m_CertificatesPanel;
		private static DescriptionPanel m_DescriptionPanel;

		public ConfigFrame(JFrame parent)
		{

				m_Parent = parent;

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
				JMenuItem checkItem = new JMenuItem("Check");
				saveMenuItem = new JMenuItem("Save [none]");
				saveclipItem = new JMenuItem("Save Using Clip Board");
				JMenuItem saveAsMenuItem = new JMenuItem("Save as...");

				newMenuItem.addActionListener(this);
				exitMenuItem.addActionListener(this);
				openMenuItem.addActionListener(this);
				openclipItem.addActionListener(this);
				checkItem.addActionListener(this);
				saveMenuItem.addActionListener(this);
				saveclipItem.addActionListener(this);
				saveAsMenuItem.addActionListener(this);

				newMenuItem.setActionCommand("New");
				exitMenuItem.setActionCommand("Exit");
				openMenuItem.setActionCommand("Open");
				saveclipItem.setActionCommand("SaveClip");
				openclipItem.setActionCommand("OpenClip");
				checkItem.setActionCommand("Check");
				saveMenuItem.setActionCommand("Save");
				saveAsMenuItem.setActionCommand("SaveAs");
				saveMenuItem.setEnabled(false);
                                if(parent==null) // an applet
                                {
                                    exitMenuItem.setEnabled(false);
                                    openMenuItem.setEnabled(false);
                                    saveAsMenuItem.setEnabled(false);
                                }

				fileMenu.add(newMenuItem);
				fileMenu.addSeparator();
				fileMenu.add(openMenuItem);
				fileMenu.add(openclipItem);
				fileMenu.addSeparator();
				fileMenu.add(checkItem);
				fileMenu.add(saveMenuItem);
				fileMenu.add(saveAsMenuItem);
				fileMenu.add(saveclipItem);
				fileMenu.addSeparator();
				fileMenu.add(exitMenuItem);

				JMenuItem aboutMenuItem = new JMenuItem("About...");
				helpMenu.add(aboutMenuItem);
				aboutMenuItem.setActionCommand("About");
				aboutMenuItem.addActionListener(this);
				m_aktFileName = null;

				JTabbedPane jtp = new JTabbedPane();
				m_GeneralPanel = new GeneralPanel();
				m_NetworkPanel = new NetworkPanel();
				m_CertificatesPanel = new CertificatesPanel();
				m_DescriptionPanel = new DescriptionPanel();

				jtp.addTab("General", m_GeneralPanel);
				jtp.addTab("Network", m_NetworkPanel);
				jtp.addTab("Certificates", m_CertificatesPanel);
				jtp.addTab("Description", m_DescriptionPanel);
				jtp.setBorder(new EmptyBorder(10, 10, 10, 10));
				setLayout(new BorderLayout());
				add(jtp, BorderLayout.CENTER);
		}

		public JMenuBar getMenuBar()
		{
				return m_MenuBar;
		}

		public void actionPerformed(ActionEvent evt)
		{
				if (evt.getActionCommand().equals("New"))
				{
						int ret =
								JOptionPane.showConfirmDialog(
										MixConfig.getMainWindow(),
										"You will lose unsaved information. Do you want to continue?",
										"Caution!",
										JOptionPane.OK_CANCEL_OPTION);
						if (ret == JOptionPane.OK_OPTION)
								reset();
				}
				else if (evt.getActionCommand().equals("Exit"))
				{
						//dispose();
						System.exit(0);
				}
				else if (evt.getActionCommand().equals("Check"))
				{
						String[] msg = check();
						if (msg != null && msg.length > 0)
								JOptionPane.showMessageDialog(
										MixConfig.getMainWindow(),
										msg,
										"Errors",
										JOptionPane.INFORMATION_MESSAGE);
						else
								JOptionPane.showMessageDialog(
										MixConfig.getMainWindow(),
										"No errors found.",
										"Check",
										JOptionPane.INFORMATION_MESSAGE);
				}
				else if (evt.getActionCommand().equals("Save"))
				{
						if (m_aktFileName != null)
								save(m_aktFileName);
				}
				else if (evt.getActionCommand().equals("SaveAs"))
				{
						try
						{
								File file =
										MixConfig
												.showFileDialog(
														MixConfig.SAVE_DIALOG,
														MixConfig.FILTER_XML)
												.getSelectedFile();
								if (file != null)
								{
                                                                                String fname = file.getName();
                                                                                if (!fname.toLowerCase().endsWith(".xml"))
                                                                                        file = new File(file.getParent(), fname + ".xml");
                                                                                                
										save(file.getCanonicalPath());
										saveMenuItem.setText("Save [" + file.getName() + "] ");
										saveMenuItem.setEnabled(true);
										m_aktFileName = file.getCanonicalPath();
										setTitle(TITLE + " - " + m_aktFileName);
								}
						}
						catch (Exception e)
						{
						};
				}
				else if (evt.getActionCommand().equals("OpenClip"))
				{
						try
						{
								Clipboard cb = null;
								try
								{
										cb =
												(Clipboard) getToolkit()
														.getClass()
														.getMethod("getSystemSelection", new Class[] {
										}).invoke(getToolkit(), new Object[] {
										});
								}
								catch (Exception ex)
								{
								}
								if (cb == null)
										cb = getToolkit().getSystemClipboard();
								Transferable data = cb.getContents(this);
								if (data != null
										&& data.isDataFlavorSupported(DataFlavor.stringFlavor))
								{
										String text =
												(String) data.getTransferData(DataFlavor.stringFlavor);
										open_internal(text.getBytes());
										return;
								}
						}
						catch (Exception e)
						{
						}
						ClipFrame Open =
								new ClipFrame(
										"Paste a file to be opened in the area provided.",
										true);
						Open.show();
						open_internal(Open.getText().getBytes());
				}
				else if (evt.getActionCommand().equals("SaveClip"))
				{
						try
						{
								Clipboard cb = null;
								try
								{
										cb =
												(Clipboard) getToolkit()
														.getClass()
														.getMethod("getSystemSelection", new Class[] {
										}).invoke(getToolkit(), new Object[] {
										});
								}
								catch (Exception ex)
								{
								}
								if (cb == null)
										cb = getToolkit().getSystemClipboard();
								cb
										.setContents(
												new StringSelection(new String(save_internal())),
												new ClipboardOwner()
								{
										public void lostOwnership(Clipboard cb, Transferable co)
										{
												// Don't care.
										}
								});
								/*
																JOptionPane.showMessageDialog(TheApplet.getMainWindow(),
														"Configuration saved into clipboard.", "Save", JOptionPane.INFORMATION_MESSAGE);
																return;
								*/
						}
						catch (Exception e)
						{
						}
						// There are some problems with the access of the
						// clipboard, so after the try to copy it, we
						// still offer the ClipFrame.
						try
						{
								ClipFrame Save =
										new ClipFrame(
												"Copy and Save this file in a new Location.",
												false);
								Save.setText(new String(save_internal()));
								Save.show();
						}
						catch (Exception e)
						{
								e.printStackTrace();
						}
				}
				else if (evt.getActionCommand().equals("Open"))
				{
						File file =
								MixConfig
										.showFileDialog(MixConfig.OPEN_DIALOG, MixConfig.FILTER_XML)
										.getSelectedFile();
						if (file != null)
						{
								try
								{
										open(file.getCanonicalPath());
										saveMenuItem.setText("Save [" + file.getName() + "] ");
										saveMenuItem.setEnabled(true);
										m_aktFileName = file.getCanonicalPath();
										setTitle(TITLE + " - " + m_aktFileName);
								}
								catch (Exception e)
								{
										e.printStackTrace();
								}
						}
				}
				else if (evt.getActionCommand().equals("About"))
				{
						JOptionPane.showMessageDialog(
								MixConfig.getMainWindow(),
								"Mix Configuration Tool\nVersion: " + MixConfig.VERSION,
								"About",
								JOptionPane.INFORMATION_MESSAGE,
								MixConfig.loadImage("icon.gif"));
				}
		}

		private void setTitle(String s)
		{
				if (m_Parent != null)
						m_Parent.setTitle(s);
		}

		private void reset()
		{
				setTitle(TITLE);
				saveMenuItem.setText("Save [none]");
				saveMenuItem.setEnabled(false);
				m_aktFileName = null;
				m_GeneralPanel.clear();
				m_NetworkPanel.clear();
				m_CertificatesPanel.clear();

				m_DescriptionPanel.setCity("");
				m_DescriptionPanel.setLati("");
				m_DescriptionPanel.setLongi("");
				m_DescriptionPanel.setState("");
		}

		private Element getChild(Node node, String name)
		{
				if (node == null)
						return null;
				Node tmp = node.getFirstChild();
				while (tmp != null)
				{
						if (tmp.getNodeName().equals(name))
						{
								if (tmp.getNodeType() == Node.ELEMENT_NODE)
										return (Element) tmp;
						}
						tmp = tmp.getNextSibling();
				}
				return null;
		}

		private String getElementValue(Element elem, String def)
		{
				if (elem == null)
						return def;
				Node n = elem.getFirstChild();
				if (n == null || n.getNodeType() != Node.TEXT_NODE)
						return def;
				return n.getNodeValue();
		}

		private void open(String filename)
		{
				try
				{
						File f = new File(filename);
						int len = (int) f.length();
						byte[] buff = new byte[len];
						FileInputStream fin = new FileInputStream(f);
						fin.read(buff);
						fin.close();
						open_internal(buff);
				}
				catch (Exception e)
				{
						e.printStackTrace();
				}
		}

		private void open_internal(byte[] config)
		{
                                reset();
				try
				{
						DocumentBuilderFactory factory =
								DocumentBuilderFactory.newInstance();
						DocumentBuilder docBuilder = factory.newDocumentBuilder();
						Document doc = docBuilder.parse(new ByteArrayInputStream(config));

						Element root = doc.getDocumentElement();
                                                if(!root.getNodeName().equals("MixConfiguration"))
                                                {
                                                    JOptionPane.showMessageDialog(
                                                            MixConfig.getMainWindow(),
                                                            "The root element '"+root.getNodeName()+"'\nin the XML file is unknown.",
                                                            "Unknown XML format.",
                                                            JOptionPane.ERROR_MESSAGE);
                                                    return;
                                                }
                                                String ver = root.getAttribute("version");
                                                if(ver==null || ver.length()==0)
                                                {
                                                    int ret =
                                                            JOptionPane.showConfirmDialog(
                                                                    MixConfig.getMainWindow(),
                                                                    "This file does not contain any version information,\nso information may be lost.\nDo you want to continue?",
                                                                    "Old XML file!",
                                                                    JOptionPane.OK_CANCEL_OPTION);
                                                    if (ret != JOptionPane.OK_OPTION)
                                                            return;
                                                }
                                                else
                                                {
                                                    int[] version = new int[] {0,0,0};
                                                    int p = 0;
                                                    for(int i=0;i<3;i++)
                                                    {
                                                        int q = ver.indexOf('.',p);
                                                        try
                                                        {
                                                            version[i] = Integer.parseInt((q<0)?ver.substring(p):ver.substring(p,q));
                                                        }
                                                        catch(NumberFormatException e)
                                                        {
                                                            break;
                                                        }
                                                        if(q<0)
                                                            break;
                                                        else
                                                            p = q+1;
                                                    }
                                                    
                                                    if(version[0]>0 || version[1]>1)
                                                    {
                                                        int ret =
                                                                JOptionPane.showConfirmDialog(
                                                                        MixConfig.getMainWindow(),
                                                                        "The version of this file is newer than this utility,\nso information may not be read properly.\nDo you want to continue?",
                                                                        "XML file too new!",
                                                                        JOptionPane.OK_CANCEL_OPTION);
                                                        if (ret != JOptionPane.OK_OPTION)
                                                                return;
                                                    }
                                                }
						Element elemGeneral = getChild(root, "General");
						Element elemType = getChild(elemGeneral, "MixType");
						String MixType = getElementValue(elemType, null);
						m_GeneralPanel.setMixType(MixType);

						Element elemName = getChild(elemGeneral, "MixName");
						String MixName = getElementValue(elemName, null);
						m_GeneralPanel.setMixName(MixName);

						Element elemCascade = getChild(elemGeneral, "CascadeName");
						String CascadeName = getElementValue(elemCascade, null);
						m_GeneralPanel.setCascadeName(CascadeName);

						Element elemMixID = getChild(elemGeneral, "MixID");
						String MixID = getElementValue(elemMixID, null);
						if (MixID != null)
								MixID = URLDecoder.decode(MixID);
						m_GeneralPanel.setMixID(MixID);

						Element elemUserID = getChild(elemGeneral, "UserID");
						if (elemUserID != null)
						{
								m_GeneralPanel.setUserID(getElementValue(elemUserID, null));
						}

						Element elemFileDes = getChild(elemGeneral, "NrOfFileDescriptors");
						if (elemFileDes != null)
						{
								m_GeneralPanel.setFileDes(getElementValue(elemFileDes, null));
						}

						Element elemDaemon = getChild(elemGeneral, "Daemon");
						String daemon = getElementValue(elemDaemon, "False");
						m_GeneralPanel.setDaemon(daemon);

						Element elemEnableLog = getChild(elemGeneral, "Logging");
						if (elemEnableLog != null)
						{
								boolean bLogFile = false;
								boolean bLogConsole = false;
								boolean bLogSyslog = false;
								String file = null;
								boolean bLogcompress = false;
								Element elemFile = getChild(elemEnableLog, "File");
								if (elemFile != null)
								{
										file = getElementValue(elemFile, null);
										bLogFile = true;
										String sCompr = elemFile.getAttribute("compressed");
										if (sCompr != null && sCompr.equalsIgnoreCase("true"))
												bLogcompress = false;
								}
								Element elemSyslog = getChild(elemEnableLog, "SysLog");
								if (elemSyslog != null)
								{
										bLogSyslog =
												getElementValue(elemSyslog, "False").equalsIgnoreCase(
														"true");
								}
								Element elemConsole = getChild(elemEnableLog, "Console");
								if (elemConsole != null)
								{
										bLogConsole =
												getElementValue(elemSyslog, "False").equalsIgnoreCase(
														"true");
								}
								m_GeneralPanel.setLogging(
										bLogConsole,
										bLogSyslog,
										bLogFile,
										file,
										bLogcompress);
						}

						Element elemNetwork = getChild(root, "Network");
						Node netChild = elemNetwork.getFirstChild();
						while (netChild != null)
						{
								if (netChild.getNodeType() == Node.ELEMENT_NODE)
								{
										m_NetworkPanel.getIncomingModel().readFromElement(
												(Element) netChild);
										m_NetworkPanel.getOutgoingModel().readFromElement(
												(Element) netChild);
								}
								netChild = netChild.getNextSibling();
						}
						Element elemInfoServer = getChild(elemNetwork, "InfoService");
						Element elemHost = getChild(elemInfoServer, "Host");
						String host = getElementValue(elemHost, null);
						m_NetworkPanel.setInfoHost(host);
						Element elemIP = getChild(elemInfoServer, "IP");
						if (elemIP != null)
						{
								String IP = getElementValue(elemIP, null);
								m_NetworkPanel.setInfoIP(IP);
						}
						Element elemPort = getChild(elemInfoServer, "Port");
						String port = getElementValue(elemPort, null);
						m_NetworkPanel.setInfoPort(port);

						Element elemCertificates = getChild(root, "Certificates");
						Element elemOwnCert = getChild(elemCertificates, "OwnCertificate");
						Element elem = getChild(elemOwnCert, "X509PKCS12");
						String name = getElementValue(elem, null);
						if (name != null)
						{
								System.out.println("Loading own Priv-Cert");
								m_CertificatesPanel.setOwnPrivCert(Base64.decode(name));
						}
						else
								m_CertificatesPanel.setOwnPrivCert(null);
						Element elemPrevCert =
								getChild(elemCertificates, "PrevMixCertificate");
						elem = getChild(elemPrevCert, "X509Certificate");
						name = getElementValue(elem, null);
						if (name == null)
								m_CertificatesPanel.setPrevPubCert(null);
						else
								m_CertificatesPanel.setPrevPubCert(Base64.decode(name));

						Element elemNextCert =
								getChild(elemCertificates, "NextMixCertificate");
						elem = getChild(elemNextCert, "X509Certificate");
						name = getElementValue(elem, null);
						if (name == null)
								m_CertificatesPanel.setNextPubCert(null);
						else
								m_CertificatesPanel.setNextPubCert(Base64.decode(name));

						Element elemDescription = getChild(root, "Description");
						Element elemLocation = getChild(elemDescription, "Location");
						Element elemCity = getChild(elemLocation, "City");
						String city = getElementValue(elemCity, null);
						m_DescriptionPanel.setCity(city);
						Element elemState = getChild(elemLocation, "State");
						String state = getElementValue(elemState, null);
						m_DescriptionPanel.setState(state);

						Element elemGeo =
								getChild(getChild(elemLocation, "Position"), "Geo");
						Element elemLongi = getChild(elemGeo, "Longitude");
						String longi = getElementValue(elemLongi, null);
						m_DescriptionPanel.setLongi(longi);

						Element elemLati = getChild(elemGeo, "Latitude");
						String lati = getElementValue(elemLati, null);
						m_DescriptionPanel.setLati(lati);
				}
				catch (Exception e)
				{
						System.out.println("Open() - There was an error:");
						e.printStackTrace();
				}
		}

		private void save(String fileName)
		{
				try
				{
						FileOutputStream fout = new FileOutputStream(fileName);
						fout.write(save_internal());
						fout.close();
				}
				catch (Exception e)
				{
						e.printStackTrace();
				}
		}

		private byte[] save_internal()
		{
				try
				{
						DocumentBuilderFactory factory =
								DocumentBuilderFactory.newInstance();
						DocumentBuilder docBuilder = factory.newDocumentBuilder();
						Document doc = docBuilder.newDocument();

						Element root = doc.createElement("MixConfiguration");
						root.setAttribute("version", "0.1");
						doc.appendChild(root);
						Element elemGeneral = doc.createElement("General");
						root.appendChild(elemGeneral);

						String elemmixtype = m_GeneralPanel.getMixType();
						Element MixType = doc.createElement("MixType");
						elemGeneral.appendChild(MixType);
						Text text1 = doc.createTextNode(elemmixtype);
						MixType.appendChild(text1);

						String elemcascadename = m_GeneralPanel.getCascadeName();
						Element elemCascadeName = doc.createElement("CascadeName");
						elemGeneral.appendChild(elemCascadeName);
						Text text1a = doc.createTextNode(elemcascadename);
						elemCascadeName.appendChild(text1a);

						String elemmixname = m_GeneralPanel.getMixName();
						Element elemMixName = doc.createElement("MixName");
						elemGeneral.appendChild(elemMixName);
						Text text2 = doc.createTextNode(elemmixname);
						elemMixName.appendChild(text2);

						String mixID = m_GeneralPanel.getMixID();
						Element elemMixID = doc.createElement("MixID");
						elemGeneral.appendChild(elemMixID);
						Text text3 = doc.createTextNode(URLEncoder.encode(mixID));
						elemMixID.appendChild(text3);

						String elemUser_ID = m_GeneralPanel.getUserID();
						if (elemUser_ID != null && !elemUser_ID.equals(""))
						{
								Element elemUserID = doc.createElement("UserID");
								elemGeneral.appendChild(elemUserID);
								Text text4 = doc.createTextNode(elemUser_ID);
								elemUserID.appendChild(text4);
						}

						String elemFileDes = m_GeneralPanel.getFileDes();
						if (elemFileDes != null && !elemFileDes.equals(""))
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

						if (m_GeneralPanel.isLoggingEnabled())
						{
								Element elemLogging = doc.createElement("Logging");
								elemGeneral.appendChild(elemLogging);
								String LogInfo = m_GeneralPanel.getEnabled();
								if (LogInfo.equals("LogtoConsole"))
								{
										Element elemLog = doc.createElement("Console");
										elemLogging.appendChild(elemLog);
										Text text6 = doc.createTextNode("True");
										elemLog.appendChild(text6);
								}
								if (LogInfo.equals("LogtoSyslog"))
								{
										Element elemLog = doc.createElement("SysLog");
										elemLogging.appendChild(elemLog);
								}
								if (LogInfo.equals("Logtodir"))
								{
										String filename = m_GeneralPanel.getFileName();
										Element elemLog = doc.createElement("File");
										elemLogging.appendChild(elemLog);
										elemLog.setAttribute(
												"compressed",
												m_GeneralPanel.isLoggingCompressed()
														? "true"
														: "false");
										Text text6 = doc.createTextNode(filename);
										elemLog.appendChild(text6);
								}
						}

						Element elemNetwork = doc.createElement("Network");
						root.appendChild(elemNetwork);
						if (m_NetworkPanel.getIncomingModel() != null)
								elemNetwork.appendChild(
										m_NetworkPanel.getIncomingModel().createAsElement(doc));
						if (m_NetworkPanel.getOutgoingModel() != null)
						{
								org.w3c.dom.Element mix =
										m_NetworkPanel.getOutgoingModel().createMixAsElement(doc);
								if (mix != null)
										elemNetwork.appendChild(mix);
								elemNetwork.appendChild(
										m_NetworkPanel.getOutgoingModel().createProxiesAsElement(
												doc));
						}
						Element elemInfoSer = doc.createElement("InfoService");
						elemNetwork.appendChild(elemInfoSer);
						String Host = m_NetworkPanel.getHost();
						Element elemHost = doc.createElement("Host");
						elemInfoSer.appendChild(elemHost);
						Text text9 = doc.createTextNode(Host);
						elemHost.appendChild(text9);

						String IP_Text = m_NetworkPanel.getIP();
						if (!IP_Text.equals("") && !IP_Text.equals("0.0.0.0"))
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
						if (buff != null)
						{
								text = doc.createTextNode(Base64.encodeBytes(buff, true));
								elem.appendChild(text);
						}
						elem = doc.createElement("X509Certificate");
						elemOwn.appendChild(elem);
						buff = m_CertificatesPanel.getOwnPubCert();
						if (buff != null)
						{
								text = doc.createTextNode(Base64.encodeBytes(buff, true));
								elem.appendChild(text);
						}
						buff = m_CertificatesPanel.getPrevPubCert();
						if (buff != null)
						{
								Element elemPrevious = doc.createElement("PrevMixCertificate");
								elemCertificate.appendChild(elemPrevious);
								elem = doc.createElement("X509Certificate");
								elemPrevious.appendChild(elem);
								text = doc.createTextNode(Base64.encodeBytes(buff, true));
								elem.appendChild(text);
						}
						buff = m_CertificatesPanel.getNextPubCert();
						if (buff != null)
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
						Element elemLocation = doc.createElement("Location");
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

						Element elemPosition = doc.createElement("Position");
						elemLocation.appendChild(elemPosition);
						Element elemGeo = doc.createElement("Geo");
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
						ByteArrayOutputStream fout = new ByteArrayOutputStream();

						OutputFormat format = new OutputFormat(doc,"UTF-8", true);
						format.setLineWidth(0);//avoid line wrapping
						XMLSerializer serial = new XMLSerializer(fout, format);
						serial.serialize(doc);
						fout.close();
						return fout.toByteArray();
				}
				catch (Exception e)
				{
						System.out.println("Save() - There was an error:");
						e.printStackTrace();
						return null;
				}
		}

		// To check for Errors or Missing Fields while writing a file.....

		private boolean isNumber(String str)
		{
				try
				{
						Integer.parseInt(str, 10);
						return true;
				}
				catch (NumberFormatException ev)
				{
						return false;
				}
		}
		private String[] check()
		{
				java.util.Vector errors = new java.util.Vector();

				if (m_GeneralPanel.getMixName().equals(""))
						errors.addElement("Mix Name not entered in General Panel.");
				if (m_GeneralPanel.getMixType().equals("FirstMix")
						&& m_GeneralPanel.getCascadeName().equals(""))
						errors.addElement("Cascade Name not entered in General Panel.");
				String mixID = m_GeneralPanel.getMixID();
				if (mixID.equals(""))
						errors.addElement("Mix ID field is blank in General Panel.");
				else
				{
						final String idChars = "abcdefghijklmnopqrstuvwxyz0123456789.-_";
						mixID = mixID.toLowerCase();
						if (mixID.charAt(0) != 'm')
								errors.addElement("Mix ID should start with a 'm'");
						for (int i = 0; i < mixID.length(); i++)
								if (idChars.indexOf(mixID.charAt(i)) < 0)
								{
										errors.addElement(
												"Mix ID should contain only letters, digits, dots, underscores and minuses.");
										break;
								}
				}
				if (m_GeneralPanel.getUserID() != null
						&& m_GeneralPanel.getUserID().equals(""))
						errors.addElement("User ID not entered in General Panel.");
				if (m_GeneralPanel.getFileDes() != null
						&& !isNumber(m_GeneralPanel.getFileDes()))
						errors.addElement(
								"Number of File Descriptors is not a number in General Panel.");
				if (m_GeneralPanel.getEnabled().equals("Logtodir")
						&& m_GeneralPanel.getFileName().equals(""))
						errors.addElement(
								"No directory for logging entered in General Panel.");

				if (m_NetworkPanel.getIncomingModel().getRowCount() == 0)
						errors.addElement("No Incoming Connection given in Network Panel.");
                                else
                                {
                                    int rows = m_NetworkPanel.getIncomingModel().getRowCount();
                                    for(int i=0;i<rows;i++)
                                    {
                                        ConnectionData data = m_NetworkPanel.getIncomingModel().getData(i);
                                        if((data.getTransport()&ConnectionData.TRANSPORT)==ConnectionData.TCP)
                                        {
                                            if(data.getPort()==0)
                                                errors.addElement("Incoming connection no. "+(i+1)+" has no port set.");
                                        }
                                        else
                                        {
                                            if(data.getName()==null || data.getName().length()==0)
                                                errors.addElement("Incoming connection no. "+(i+1)+" has no filename set.");
                                        }
                                    }
                                }
				if (m_NetworkPanel.getOutgoingModel().getRowCount() == 0)
						errors.addElement("No Outgoing Connection given in Network Panel.");
				else
                                {
                                    int rows = m_NetworkPanel.getOutgoingModel().getRowCount();
                                    for(int i=0;i<rows;i++)
                                    {
                                        ConnectionData data = m_NetworkPanel.getOutgoingModel().getData(i);
                                        if((data.getTransport()&ConnectionData.TRANSPORT)==ConnectionData.TCP)
                                        {
                                            if(data.getName()==null || data.getName().length()==0)
                                                errors.addElement("Outgoing connection no. "+(i+1)+" has no host name set.");
                                            if(data.getPort()==0)
                                                errors.addElement("Outgoing connection no. "+(i+1)+" has no port set.");
                                        }
                                        else
                                        {
                                            if(data.getName()==null || data.getName().length()==0)
                                                errors.addElement("Outgoing connection no. "+(i+1)+" has no filename set.");
                                        }
                                    }
                                    if (!m_GeneralPanel.getMixType().equals("LastMix"))
                                    {
						if (m_NetworkPanel.getOutgoingModel().getRowCount() > 1)
								errors.addElement(
										"Too many Outgoing Connections in Network Panel.");
                                    }
				}
				if (m_NetworkPanel.getHost().equals(""))
						errors.addElement(
								"The Host field for the Info Service should not be blank in Network Panel.");
				if (m_NetworkPanel.getPort().equals(""))
						errors.addElement(
								"The Port field for the Info Service should not be blank in Network Panel.");
				if (!m_NetworkPanel.IP_Text.isEmpty()
						&& !m_NetworkPanel.IP_Text.isCorrect())
						errors.addElement(
								"IP of Info Service is not correct in Network Panel.");

				if (m_CertificatesPanel.getOwnPrivCert() == null
						|| m_CertificatesPanel.getOwnPubCert() == null)
						errors.addElement(
								"Own Mix Certificate is missing in Certificates Panel.");
				if (m_GeneralPanel.getMixType().equals("FirstMix"))
				{
                                        if(m_CertificatesPanel.getPrevPubCert() != null)
                                                errors.addElement(
                                                        "Previous Mix Certificate is present, but there is no previous mix.");
                                }
                                else
                                {
                                        if(m_CertificatesPanel.getPrevPubCert() == null)                        
                                                errors.addElement(
                                                        "Previous Mix Certificate is missing in Certificates Panel.");
                                }
                                    
				if (m_GeneralPanel.getMixType().equals("LastMix"))
                                {
                                        if(m_CertificatesPanel.getNextPubCert() != null)
                                                errors.addElement(
                                                                 "Next Mix Certificate is present, but there is no next mix.");
                                }
                                else
                                {
                                        if(m_CertificatesPanel.getNextPubCert() == null)
						errors.addElement(
								"Next Mix Certificate is missing in Certificates Panel.");
                                }

				if (m_DescriptionPanel.getCity().equals(""))
						errors.addElement(
								"The city field cannot be left blank in Description Panel.");
				if (m_DescriptionPanel.getState().equals(""))
						errors.addElement(
								"The state field cannot be left blank in Description Panel.");
				if (m_DescriptionPanel.getLatitude().equals("")
						&& !m_DescriptionPanel.getLongitude().equals(""))
						errors.addElement("Latitude is missing in Description Panel.");
				if (!m_DescriptionPanel.getLatitude().equals("")
						&& m_DescriptionPanel.getLongitude().equals(""))
						errors.addElement("Longitude is missing in Description Panel.");

				String[] asString = new String[errors.size()];
				for (int i = 0; i < asString.length; i++)
						asString[i] = (String) errors.elementAt(i);
				// return (String[]) errors.toArray(new String[] {});
				return asString;
		}

//ugly but cool!! .-))
public void useslessdummy()
	{
	//The foolowin class are only imported,
//so what they are "automatically" included in the generated JAR file
 new org.apache.xerces.jaxp.DocumentBuilderFactoryImpl();
	new org.apache.xerces.parsers.XML11Configuration();
	new org.apache.xerces.impl.dv.dtd.DTDDVFactoryImpl();
	}
}