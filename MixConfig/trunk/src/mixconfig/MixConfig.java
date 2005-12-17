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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.WindowConstants;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.w3c.dom.Document;
import anon.crypto.PKCS12;
import anon.crypto.Validity;
import anon.crypto.X509DistinguishedName;
import anon.crypto.X509SubjectKeyIdentifier;
import anon.util.ResourceLoader;
import anon.util.XMLUtil;
import gui.ImageIconLoader;
import gui.JAPHelp;
import gui.JAPMessages;
import gui.JAPDialog;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import logging.SystemErrLog;



public class MixConfig extends JApplet
{
	static {
		JAPMessages.init("MixConfigMessages");
		LogHolder.setLogInstance(new SystemErrLog(LogLevel.WARNING, LogType.ALL));
	}

	public final static int SAVE_DIALOG = 1;
	public final static int OPEN_DIALOG = 2;
	public final static int FILTER_ALL = 0;
	public final static int FILTER_CER = 1;
	public final static int FILTER_XML = 2;
	public final static int FILTER_PFX = 4;
	public final static int FILTER_B64_CER = 8;
	public final static String VERSION = "00.04.019"; //NEVER change the layout of this line!!

	public static final String MSG_WARNING = "MixConfig_warning";

	private static final String IMAGE_LOAD_PATH = "images/mixconfig/";
	private static final String MAIN_ICON_PATH = JAPMessages.getString("main_icon");

        /** The configuration object edited by this <CODE>MixConfig</CODE> application. */
	private static MixConfiguration m_mixConfiguration;
	private static JPanel m_mainPanel;
	private static ChoicePanel m_startPanel;
	private static Frame m_MainWindow;
	private static File m_fileCurrentDir = new File(System.getProperty("user.dir"));
	private static String m_currentFileName;

	public static void main(String[] argv)
	{
		File f = null;

		try
		{
	        for (int i = 0; i < argv.length; i++)
			{
				if (argv[i].equals("--help"))
				{
					usage();
					System.exit(0);
				}
				else if (argv[i].equals("--createConf"))
				{
					createMixOnCDConfiguration();
					System.exit(0);
				}
				else if (argv[i].equals("--logdetail"))
				{
					try
					{
						i++;
						LogHolder.setDetailLevel(Integer.parseInt(argv[i]));
					}
					catch (Exception a_e)
					{
						usage();
						System.exit(1);
					}
				}
				else if (argv[i].equals("--loglevel"))
				{
					try
					{
						i++;
						int j;
						for (j = 0; j < LogLevel.STR_Levels.length; j++)
						{
							if (argv[i].trim().equalsIgnoreCase(LogLevel.STR_Levels[j].trim()))
							{
								LogHolder.setLogInstance(new SystemErrLog(j, LogType.ALL));
								break;
							}
						}

						if (j >= LogLevel.STR_Levels.length)
						{
							throw new Exception();
						}

					}
					catch (Exception a_e)
					{
						a_e.printStackTrace();
						usage();
						System.exit(1);
					}
				}
				else
				{
					setCurrentFilename(argv[i]);
				}
			}

			m_MainWindow = new JFrame();
			((JFrame)m_MainWindow).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			m_MainWindow.setResizable(false);
			JAPHelp.init(m_MainWindow);


			if (m_currentFileName != null && (f = new File(m_currentFileName)).exists())
			{
				LogHolder.log(LogLevel.DEBUG, LogType.MISC,
							  JAPMessages.getString("loading_config_file"));
				m_mixConfiguration = new MixConfiguration(new FileReader(f));
			}
			else //no existig file is given
			{
				if (m_currentFileName != null)
				{
					JAPDialog.showErrorMessage(getMainWindow(), null,
											   JAPMessages.getString("config_file_not_found",
						new File(m_currentFileName).toString()),
											   LogType.MISC);
				}

				m_mixConfiguration = new MixConfiguration();
			}

			m_startPanel = new ChoicePanel((JFrame)m_MainWindow,null);
			((JFrame)m_MainWindow).setContentPane(m_startPanel);
			m_MainWindow.pack();
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension size = m_MainWindow.getSize();
			m_MainWindow.setLocation( (d.width - size.width) / 2, (d.height - size.height) / 2);
			LogHolder.log(LogLevel.DEBUG, LogType.GUI, JAPMessages.getString("show_gui"));
			m_MainWindow.setVisible(true);

			ImageIcon icon = loadImageIcon(MAIN_ICON_PATH);
			if (icon != null)
			{
				m_MainWindow.setIconImage(icon.getImage());
			}

			m_MainWindow.addWindowListener(new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					windowClosing(e);
				}

				public void windowClosing(WindowEvent e)
				{
					m_startPanel.getMenu().exit();
				}
			});
		}
		catch (Exception e)
		{
			JAPDialog.showErrorMessage(getMainWindow(),e, JAPMessages.getString("could_not_initialise"),
									   LogType.MISC);
			System.exit(1);
		}

		m_startPanel.setMessageTitle(); //set Message Title
		m_mixConfiguration.setSavedToFile(); // tell the GUI that this configuration has not changed
	}

	/**
	 * Displays a short help message about available command line options for this class.
	 */
	public static void usage()
	{
		String logLevels = "";

		for (int i= 0; i < LogLevel.STR_Levels.length; i++)
		{
			logLevels += LogLevel.STR_Levels[i].trim() + ",";
		}
		if (logLevels.length() > 0)
		{
			logLevels = logLevels.substring(0, logLevels.length() - 1);
		}

		String message[] =
			{
			"Usage: java -cp <classpath> mixconfig.MixConfig [options] [configfilename]",
			"where options is one of",
			"--help       Show this message and exit.",
			"--logDetail  Sets the detail level of log messages. " +
			"(lowest is " + LogHolder.DETAIL_LEVEL_LOWEST +  ", highest is " +
			LogHolder.DETAIL_LEVEL_HIGHEST  + ")",
			"--logLevel   Sets the level of the log messages. (" + logLevels + ")",
			"--createConf Creates a generic configuration for MixOnCD."
		};
		for (int i = 0; i < message.length; i++)
		{
			System.out.println(message[i]);
		}
		MixConfig.info("Usage", message);
	}

	public void init() // For the applet.
	{
		try
		{
			// Let's search for the parent frame:
			java.awt.Component comp = this;
			while ( (comp = comp.getParent()) != null &&
				   ! (comp instanceof java.awt.Frame))
			{
				;
			}
			m_MainWindow = (Frame) comp;
			m_mixConfiguration = new MixConfiguration();
			JAPHelp.init(m_MainWindow);

			m_mainPanel = new ConfigFrame(null);
			m_startPanel = new ChoicePanel(null,getRootPane());
			//myFrame.pack();//setBounds(10,10,600,650);

			setContentPane(m_startPanel);
		}
		catch (Exception e)
		{
			JAPDialog.showErrorMessage(getMainWindow(),e, JAPMessages.getString("could_not_initialise"),
									   LogType.MISC);
			System.exit(1);
		}
	}

	/** Gets the Mix configuration that is currently being edited
	 * @return The current Mix configuration
	 */
	public static MixConfiguration getMixConfiguration()
	{
		return m_mixConfiguration;
	}

	/** Sets the Mix configuration to be edited
	 * @param a_mixConfiguration The new Mix configuration
	 */
	public static void setMixConfiguration(MixConfiguration a_mixConfiguration)
	{
		m_mixConfiguration = a_mixConfiguration;
	}

	/** Sets the filname of a saved or loaded file to a variable.
	 * You can read it out, using getCurrentFileName()
	 * eg. for setting the MessageTitle
	 * @param filename a file name
	 */
	public static void setCurrentFilename(String filename)
	{
		m_currentFileName = filename;
	}

	/**
	 * Returns the path name of the currently edited config file.
	 * @return the file name of the currently edited configuration
	 */
	public static String getCurrentFileName()
	{
		return m_currentFileName;
	}

	/** Displays a confirm dialog.
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 * @return <CODE>true</CODE> if the user confirmed, <CODE>false</CODE> otherwise
	 */
	public static boolean ask(String a_title, String a_message)
	{
		return JAPDialog.showYesNoConfirmMessage(MixConfig.getMainWindow(), a_title, a_message);
	}

	/** Displays an info message dialog
	 * @param a_title The title of the message dialog
	 * @param a_message The messages to be displayed. Each message will be displayed in a single
	 * line.
	 */
	public static void info(String a_title, String[] a_message)
	{
		String message = "";

		if (a_message == null || a_message.length == 0)
		{
			JAPDialog.showInfoDialog(getMainWindow(), a_title, "");

		}
		else if (a_message.length == 1)
		{
			JAPDialog.showInfoDialog(getMainWindow(), a_title, a_message[0]);
		}
		else
		{
			message += "<UL>";
			for (int i = 0; i < a_message.length; i++)
			{
				if (a_message[i] == null || a_message[i].trim().length() == 0)
				{
					continue;
				}
				message +=  "<LI>" + a_message[i] + "</LI>";
			}
			message += "</UL>";
			JAPDialog.showInfoDialog(getMainWindow(), a_title, message);
		}
	}

	public static void about()
	{
		JOptionPane.showMessageDialog(
			getMainWindow(),
			"Mix Configuration Tool\nVersion: " + VERSION,
			"About",
			JOptionPane.INFORMATION_MESSAGE,
			loadImageIcon(MAIN_ICON_PATH));
	}


	public static Frame getMainWindow()
	{
		return m_MainWindow;
	}

	/**
	 * Loads an image icon from the MixConfig icon directory. This is
	 * a shortcut for IconDirectory + ImageName. If you need to load an
	 * image from the common image directory add "../" before the file name.
	 * @param a_name the name of the image icon to load
	 * @return the loaded image icon or null if the icon could not be loaded
	 */
	public static ImageIcon loadImageIcon(String a_name)
	{
		return ImageIconLoader.loadImageIcon(IMAGE_LOAD_PATH + a_name);
	}

	/**
	 *
	 * @param type int
	 * @param filter_type int
	 * @return the JFileChooser if an action was taken or null if the user clicked 'cancel'
	 */
	public static JFileChooser showFileDialog(int type, int filter_type)
	{
		int returnValue;
		SimpleFileFilter active = null;
		JFileChooser fd2 = new JFileChooser(m_fileCurrentDir);
		fd2.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if ( (filter_type & FILTER_CER) != 0)
		{
			fd2.addChoosableFileFilter(active = new SimpleFileFilter(FILTER_CER));
		}
		if ( (filter_type & FILTER_B64_CER) != 0)
		{
			fd2.addChoosableFileFilter(active = new SimpleFileFilter(FILTER_B64_CER));
		}
		if ( (filter_type & FILTER_XML) != 0)
		{
			fd2.addChoosableFileFilter(active = new SimpleFileFilter(FILTER_XML));
		}
		if ( (filter_type & FILTER_PFX) != 0)
		{
			fd2.addChoosableFileFilter(new SimpleFileFilter(FILTER_PFX));
		}
		if (active != null)
		{
			fd2.setFileFilter(active);
		}
		fd2.setFileHidingEnabled(false);
		if (type == SAVE_DIALOG)
		{
			returnValue = fd2.showSaveDialog(getMainWindow());
		}
		else
		{
			returnValue = fd2.showOpenDialog(getMainWindow());
		}
		if (returnValue == JFileChooser.CANCEL_OPTION)
		{
			return null;
		}

		m_fileCurrentDir = fd2.getCurrentDirectory();
		return fd2;
	}

	public static byte[] openFile(int type)
	{
		JFileChooser fileChooser = showFileDialog(MixConfig.OPEN_DIALOG, type);
		if (fileChooser == null)
		{
			return null;
		}

		File file = fileChooser.getSelectedFile();

		if (file != null)
		{
			try
			{
				return ResourceLoader.getStreamAsBytes(new FileInputStream(file));
			}
			catch (IOException e)
			{
				JAPDialog.showErrorMessage(getMainWindow(),e,
										   JAPMessages.getString("error_open_file", file.toString()),
										   LogType.MISC);
			}
		}
		return null;
	}

	private static void createMixOnCDConfiguration() throws Exception
	{

		MixConfiguration configuration = new MixConfiguration();

		// create mix certificate
		X509DistinguishedName dn = new X509DistinguishedName("CN=");
		CertificateGenerator certificateGenerator =
			new CertificateGenerator(dn, null, new Validity(Calendar.getInstance(), 1), new char[0], null,
			true);
		PKCS12 privateCert = (PKCS12)certificateGenerator.construct();
		configuration.setValue("Certificates/OwnCertificate/X509PKCS12", privateCert.toByteArray());
		configuration.setValue("Certificates/OwnCertificate/X509Certificate",
							   privateCert.getX509Certificate().toByteArray());

		// create other entries
		configuration.setValue("Network/ListenerInterfaces/ListenerInterface/Port", 6544);
		configuration.setValue("Network/ListenerInterfaces/ListenerInterface/NetworkProtocol",
								   "RAW/TCP");
		configuration.setValue("General/CascadeName", "Dynamic Cascade");
		configuration.setValue("General/MixName", "Dynamic Mix");
		configuration.setValue("General/MixID",
								   ((X509SubjectKeyIdentifier)privateCert.getExtensions().getExtension(
									X509SubjectKeyIdentifier.IDENTIFIER)).getValueWithoutColon());
		configuration.setValue("General/UserID", "mix");
		configuration.setValue("General/Logging/SysLog", true);
		configuration.setValue("Network/InfoService/AllowAutoConfiguration", true);

		Document document = configuration.getDocument();
		XMLUtil.formatHumanReadable(document);
		System.out.println(XMLUtil.toString(document));
	}
}
