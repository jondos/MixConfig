/*
 Copyright (c) 2000, The JAP-Team
 All rights reserved.
 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation and/or
 other materials provided with the distribution.

 - Neither the name of the University of Technology Dresden, Germany nor the names of its contributors
 may be used to endorse or promote products derived from this software without specific
 prior written permission.


 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS
 OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS
 BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 */
package mixconfig;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;
import javax.xml.parsers.ParserConfigurationException;

import java.awt.BorderLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.xml.sax.SAXException;
import mixconfig.networkpanel.NetworkPanel;

/**
 * The Frame of the MixConfig Application.
 */
public class ConfigFrame extends JPanel implements ActionListener
{
	private JFrame m_Parent;
	private JMenuBar m_MenuBar;

	//public static boolean New;
	private JMenuItem saveMenuItem, saveclipItem;

	private static GeneralPanel m_GeneralPanel;
	private static NetworkPanel m_NetworkPanel;
	private static CertificatesPanel m_CertificatesPanel;
	private static DescriptionPanel m_DescriptionPanel;

	// added by Bastian Voigt (Why static??)
	protected static PaymentPanel m_PaymentPanel;

	public ConfigFrame(JFrame parent) throws IOException
	{
		m_Parent = parent;

		m_MenuBar = new JMenuBar();
		//setJMenuBar(mb);
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		m_MenuBar.add(fileMenu);
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('T');
		m_MenuBar.add(toolsMenu);
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
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
		if (parent == null)
		{ // an applet
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

		JMenuItem toolCertSigMenuItem = new JMenuItem(
			"Sign a public Key Certificate ...");
		toolsMenu.add(toolCertSigMenuItem);
		toolCertSigMenuItem.setActionCommand("toolCertSigMenuItem");
		toolCertSigMenuItem.addActionListener(this);
		JMenuItem toolPGPMenuItem = new JMenuItem(
			"Convert PGP to X.509 ...");
		toolsMenu.add(toolPGPMenuItem);
		toolPGPMenuItem.setActionCommand("toolPGPMenuItem");
		toolPGPMenuItem.addActionListener(this);

		JMenuItem toolEncLogMenuItem = new JMenuItem(
			"Display encrypted Mix log ...");
		toolsMenu.add(toolEncLogMenuItem);
		toolEncLogMenuItem.setActionCommand("toolEncLogMenuItem");
		toolEncLogMenuItem.addActionListener(this);

		JMenuItem aboutMenuItem = new JMenuItem("About...");
		helpMenu.add(aboutMenuItem);
		aboutMenuItem.setActionCommand("About");
		aboutMenuItem.addActionListener(this);

		JTabbedPane jtp = new JTabbedPane();
		m_GeneralPanel = new GeneralPanel();
		m_NetworkPanel = new NetworkPanel();
		m_CertificatesPanel = new CertificatesPanel();
		m_DescriptionPanel = new DescriptionPanel(parent == null);

		//added by Bastian Voigt
		m_PaymentPanel = new PaymentPanel();

		jtp.addTab("General", m_GeneralPanel);
		jtp.addTab("Network", m_NetworkPanel);
		jtp.addTab("Certificates", m_CertificatesPanel);
		jtp.addTab("Description", m_DescriptionPanel);
		jtp.setBorder(new EmptyBorder(10, 10, 10, 10));

		//added by Bastian Voigt
		jtp.addTab("Payment", m_PaymentPanel);

		setLayout(new BorderLayout());
		add(jtp, BorderLayout.CENTER);

		setConfiguration(MixConfig.getMixConfiguration());
	}

	public JMenuBar getMenuBar()
	{
		return m_MenuBar;
	}

	public void actionPerformed(ActionEvent evt)
	{
		try
		{
			MixConfiguration mixConf = MixConfig.getMixConfiguration();

			if (evt.getActionCommand().equals("New"))
			{
				boolean ret = MixConfig.ask("Notice",
											"You will lose unsaved information. " +
											"Do you want to continue?");
				if (ret)
				{
					reset();
				}
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
				{
					MixConfig.info("Errors occurred", msg);
				}
				else
				{
					MixConfig.info("Check", "No errors.");
				}
			}
			else if (evt.getActionCommand().equals("Save"))
			{
				if (MixConfig.getCurrentFileName() != null)
				{
					mixConf.save(new FileWriter(MixConfig.getCurrentFileName()));
				}
			}
			else if (evt.getActionCommand().equals("SaveAs"))
			{
				File file =
					MixConfig.showFileDialog(MixConfig.SAVE_DIALOG,
											 MixConfig.FILTER_XML).getSelectedFile();
				if (file != null)
				{
					String fname = file.getName();
					if (!fname.toLowerCase().endsWith(".xml"))
					{
						file = new File(file.getParent(), fname + ".xml");

					}
					mixConf.save(new FileWriter(file.getCanonicalPath()));
					saveMenuItem.setText("Save [" + file.getName() + "] ");
					saveMenuItem.setEnabled(true);
					MixConfig.setCurrentFileName(file.getCanonicalPath());
				}
			}
			else if (evt.getActionCommand().equals("OpenClip"))
			{
				Clipboard cb = getClipboard();
				String xmlString;

				Transferable data = cb.getContents(this);
				if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor))
				{
					xmlString = (String) data.getTransferData(DataFlavor.stringFlavor);
				}
				else
				{
					ClipFrame cf =
						new ClipFrame("Paste a file to be opened in the area provided.", true);
					cf.show();
					xmlString = cf.getText();
				}
				setConfiguration(new MixConfiguration(new StringReader(xmlString)));
			}
			else if (evt.getActionCommand().equals("SaveClip"))
			{
				StringWriter sw = new StringWriter();
				MixConfig.getMixConfiguration().save(sw);
				String xmlString = sw.toString();

				try
				{
					Clipboard cb = getClipboard();
					cb.setContents(new StringSelection(xmlString),
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
					e.printStackTrace();
				}

				// There are some problems with the access of the
				// clipboard, so after the try to copy it, we
				// still offer the ClipFrame.
				ClipFrame cf =
					new ClipFrame("Copy and Save this file in a new Location.", false);
				cf.setText(xmlString);
				cf.show();
			}
			else if (evt.getActionCommand().equals("Open"))
			{
				File file =
					MixConfig
					.showFileDialog(MixConfig.OPEN_DIALOG, MixConfig.FILTER_XML)
					.getSelectedFile();
				if (file != null)
				{
					FileReader fr = new FileReader(file);
					setConfiguration(new MixConfiguration(fr));
					saveMenuItem.setText("Save [" + file.getName() + "] ");
					saveMenuItem.setEnabled(true);
					MixConfig.setCurrentFileName(file.getCanonicalPath());
				}
			}
			else if (evt.getActionCommand().equals("toolCertSigMenuItem"))
			{
				new SigCertTool(MixConfig.getMainWindow());
			}
			else if (evt.getActionCommand().equals("toolEncLogMenuItem"))
			{
				new EncryptedLogTool(MixConfig.getMainWindow());
			}
			else if (evt.getActionCommand().equals("toolPGPMenuItem"))
			{
				new PGPtoX509Tool(MixConfig.getMainWindow());
			}
			else if (evt.getActionCommand().equals("About"))
			{
				MixConfig.about();
			}
		}
		catch (Exception e)
		{
			MixConfig.handleException(e);
		}
	}

	private void reset() throws SAXException, IOException, ParserConfigurationException
	{
		saveMenuItem.setText("Save [none]");
		saveMenuItem.setEnabled(false);
		MixConfig.setCurrentFileName(null);
	}

	private Clipboard getClipboard()
	{
		Clipboard r_cb = null;
		try
		{
			Method getSystemSelection = getToolkit().getClass()
				.getMethod("getSystemSelection", new Class[0]);
			r_cb = (Clipboard) getSystemSelection.invoke(getToolkit(), new Object[0]);
		}
		catch (NoSuchMethodException nsme)
		{
			// JDK < 1.4 does not support getSystemSelection
		}
		catch (IllegalAccessException iae)
		{
			// this should not happen
		}
		catch (InvocationTargetException ite)
		{
			// this should not happen
		}

		// alternate way of retrieving the clipboard
		if (r_cb == null)
		{
			r_cb = getToolkit().getSystemClipboard();
		}
		return r_cb;
	}

        /** Notifies the configuration panels about a possibly new underlying configuration
         * object and makes them load the config data from the configuration object into
         * their controls.
         * @param m The configuration object to be set
         * @throws IOException If an error occurs while loading data from the config object into a panel
         */
	private void setConfiguration(MixConfiguration m) throws IOException
	{
		if (m == null)
		{
			m = MixConfig.getMixConfiguration();
		}
		else
		{
			MixConfig.setMixConfiguration(m);
		}

		m_GeneralPanel.setConfiguration(m);
		m_NetworkPanel.setConfiguration(m);
		m_CertificatesPanel.setConfiguration(m);
		m_DescriptionPanel.setConfiguration(m);
		m_PaymentPanel.setConfiguration(m);

		/*		m.addChangeListener(m_GeneralPanel);
		  m.addChangeListener(m_NetworkPanel);
		  m.addChangeListener(m_CertificatesPanel);
		  m.addChangeListener(m_DescriptionPanel);
		  m.addChangeListener(m_PaymentPanel);*/
	}

        /** Makes all of the configuration panels check their data for inconsistencies and
         * returns possible error and warning messages as an array of <CODE>String</CODE>s.
         * @return An array of <CODE>String</CODE> containing possible error messages
         */
	protected String[] check()
	{
		Vector errors[] = new Vector[4];
		errors[0] = m_GeneralPanel.check();
		errors[1] = m_NetworkPanel.check();
		errors[2] = m_CertificatesPanel.check();
		errors[3] = m_DescriptionPanel.check();

		int size = 0;
		for (int i = 0; i < errors.length; i++)
		{
			size += errors[i].size();
		}

		String[] asString = new String[size];

		int k = 0;

		for (int j = 0; j < errors.length; j++)
		{
			for (int i = 0; i < errors[j].size(); i++)
			{
				asString[k++] = (String) errors[j].elementAt(i);
			}
		}
		// Vector.toArray() must not be used; it is not compatible
		// with JDK 1.1.8
		// return (String[]) errors.toArray(new String[] {});

		return asString;
	}

	/**
	 * This method is a dummy that does nothing but to instantiate some Xerces
	 * DOM classes. This forces JBuilder to include them into the JAR file on
	 * compilation.
	 * @deprecated This method is an ugly but useful hack.
	 */
	private void forceXercesIntoJar()
	{
		new org.apache.xerces.jaxp.DocumentBuilderFactoryImpl();
		new org.apache.xerces.parsers.XML11Configuration();
		new org.apache.xerces.impl.dv.dtd.DTDDVFactoryImpl();
	}
}
