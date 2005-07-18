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
import gui.GUIUtils;
import jcui.common.TextFormatUtil;
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
	public final static String VERSION = "00.03.000"; //NEVER change the layout of this line!!

	private static final String IMAGE_LOAD_PATH = "images/mixconfig/";
	private static final String MAIN_ICON_PATH = JAPMessages.getString("main_icon");
	private static final int OPTION_PANE_WIDTH = 70;

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
					handleError(null, JAPMessages.getString("config_file_not_found",
						new File(m_currentFileName).toString()),
								LogType.MISC);
				}

				m_mixConfiguration = new MixConfiguration();
			}

			m_startPanel = new ChoicePanel((JFrame)m_MainWindow);
			((JFrame)m_MainWindow).setContentPane(m_startPanel);
			m_MainWindow.pack();
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension size = m_MainWindow.getSize();
			m_MainWindow.setLocation( (d.width - size.width) / 2, (d.height - size.height) / 2);
			LogHolder.log(LogLevel.DEBUG, LogType.GUI, JAPMessages.getString("show_gui"));
			m_MainWindow.show();

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
					System.exit(0);
				}
			});
		}
		catch (Exception e)
		{
			MixConfig.handleException(e, JAPMessages.getString("could_not_initialise"), LogType.MISC);
		}

		//set Message Title
		m_startPanel.setMessageTitle();
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
			JAPHelp.init(m_MainWindow);

			m_mainPanel = new ConfigFrame(null);
			//myFrame.pack();//setBounds(10,10,600,650);

			setContentPane(m_mainPanel);
		}
		catch (Exception e)
		{
			MixConfig.handleException(e, JAPMessages.getString("could_not_initialise"), LogType.MISC);
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
		int i = JOptionPane.showConfirmDialog(getMainWindow(),
											  TextFormatUtil.wrapWordsOfTextLine(
											 a_message, OPTION_PANE_WIDTH),
											  a_title,
											  JOptionPane.YES_NO_OPTION);
		return (i == JOptionPane.YES_OPTION);
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
			info(a_title, "");

		}
		else if (a_message.length == 1)
		{
			info(a_title, a_message[0]);
		}
		else
		{
			for (int i = 0; i < a_message.length; i++)
			{
				message += TextFormatUtil.formatDescription(a_message[i], "* ", 2, OPTION_PANE_WIDTH);
			}

			JOptionPane.showMessageDialog(getMainWindow(), message,
										  a_title, JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/** Displays an info message dialog
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 */
	public static void info(String a_title, String a_message)
	{
		JOptionPane.showMessageDialog(getMainWindow(),
									  TextFormatUtil.wrapWordsOfTextLine(
									   a_message, OPTION_PANE_WIDTH),
									  a_title, JOptionPane.INFORMATION_MESSAGE);
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

	/**
	 * Displays a dialog showing a critical error message to the user and logs the error message
	 * to the currently used Log. As this method handles critical errors only,
	 * the application will stop when the dialog is closed.
	 * @param a_e a Throwable that has been caught (may be null)
	 * @param a_message a message that is shown to the user (may be null)
	 * @param a_logType the log type for this error
	 * @see logging.LogHolder
	 * @see logging.LogType
	 * @see logging.Log
	 */
	public static void handleException(Throwable a_e, String a_message, int a_logType)
	{
		a_message = retrieveErrorMessage(a_e, a_message);
		LogHolder.log(LogLevel.EXCEPTION, a_logType, a_message, true);
		LogHolder.log(LogLevel.EXCEPTION, a_logType, a_e);

		showErrorMessage(a_message, JAPMessages.getString("exception"));

		System.exit(1);
	}

	/**
	 * Displays a dialog showing an error message to the user and logs the error message
	 * to the currently used Log.
	 * @param a_e a Throwable that has been caught (may be null)
	 * @param a_message a message that is shown to the user (may be null)
	 * @param a_logType the log type for this error
	 * @see logging.LogHolder
	 * @see logging.LogType
	 * @see logging.Log
	 */
	public static void handleError(Throwable a_e, String a_message, int a_logType)
	{
		a_message = retrieveErrorMessage(a_e, a_message);
		LogHolder.log(LogLevel.ERR, a_logType, a_message, true);
		if (a_e != null)
		{
			// the exception is only shown in debug mode
			LogHolder.log(LogLevel.DEBUG, a_logType, a_e);
		}

		showErrorMessage(a_message, JAPMessages.getString("error"));
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

	public static JFileChooser showFileDialog(int type, int filter_type)
	{
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
			fd2.showSaveDialog(getMainWindow());
		}
		else
		{
			fd2.showOpenDialog(getMainWindow());
		}
		m_fileCurrentDir = fd2.getCurrentDirectory();
		return fd2;
	}

	public static byte[] openFile(int type)
	{
		File file = showFileDialog(MixConfig.OPEN_DIALOG, type).getSelectedFile();

		if (file != null)
		{
			try
			{
				return ResourceLoader.getStreamAsBytes(new FileInputStream(file));
			}
			catch (IOException e)
			{
				MixConfig.handleError(e, JAPMessages.getString("error_open_file", file.toString()),
									  LogType.MISC);
			}
		}
		return null;
	}



	/**
	 * Retrieves an error message from a Throwable and a message String that may be shown to the
	 * user.
	 * @param a_e a Throwable (may be null)
	 * @param a_message an error message (may be null)
	 * @return the retrieved error message
	 */
	private static String retrieveErrorMessage(Throwable a_e, String a_message)
	{
		if (a_message == null)
		{
			if (a_e == null || a_e.getMessage() == null)
			{
				a_message = JAPMessages.getString("unknown_error");
			}
			else
			{
				a_message = a_e.getMessage();
			}
		}

		return a_message;
	}

	/**
	 * Shows an error message to the user.
	 * @param a_message a message
	 * @param a_title a title
	 */
	private static void showErrorMessage(String a_message, String a_title)
	{
		try
		{
			JOptionPane.showMessageDialog(getMainWindow(),
										  TextFormatUtil.wrapWordsOfTextLine(
											  a_message, OPTION_PANE_WIDTH),
										  a_title,
										  JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception e)
		{
			LogHolder.log(LogLevel.EXCEPTION, LogType.GUI,
						  JAPMessages.getString("could_not_display_error"));
			LogHolder.log(LogLevel.EXCEPTION, LogType.GUI, e);
		}
	}

	private static void createMixOnCDConfiguration() throws Exception
	{

		MixConfiguration configuration = new MixConfiguration();

		// create mix certificate
		X509DistinguishedName dn = new X509DistinguishedName("CN=");
		CertificateGenerator certificateGenerator =
			new CertificateGenerator(dn, null, new Validity(Calendar.getInstance(), 1), new char[0], null);
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
