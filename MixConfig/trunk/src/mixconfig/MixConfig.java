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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.BERInputStream;
import org.bouncycastle.asn1.DERInputStream;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.SignedData;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import anon.util.Base64;
import mixconfig.wizard.ConfigWizard;
import java.io.FileReader;

/** \mainpage
 This is a tool which one can use for creating a configuration file for a Mix. This configuration file
 contains an XML struct with the foolowing elements:
 \verbinclude "./MixConfiguration.xml"
 */
public class MixConfig extends JApplet
{
	public final static int SAVE_DIALOG = 1;
	public final static int OPEN_DIALOG = 2;
	public final static int FILTER_ALL = 0;
	public final static int FILTER_CER = 1;
	public final static int FILTER_XML = 2;
	public final static int FILTER_PFX = 4;
	public final static int FILTER_B64_CER = 8;
	public final static String VERSION = "00.02.032"; //NEVER change the layout of this line!!

	private static final String m_configFilePath = ".";
	private static final String TITLE = "Mix Configuration Tool";

        /** The configuration object edited by this <CODE>MixConfig</CODE> application. */
	private static MixConfiguration m_mixConfiguration;
        /** Indicates whether to show the wizard interface or the normal interface */
	private static boolean showWizard = false;
	private static JPanel m_mainPanel;
	private static Frame m_MainWindow;
	private static File m_fileCurrentDir;
	private static String m_currentFileName;

	public static void main(String[] argv)
	{
		try
		{
			JFrame MainWindow = new JFrame();
			m_MainWindow = MainWindow;

			setCurrentFileName(null);

			boolean force_no_wizard = false;
			boolean force_wizard = false;
			File f = null;
			for (int i = 0; i < argv.length; i++)
			{
				if (argv[i].equals("--no-wizard"))
				{
					force_no_wizard = true;
				}
				if (argv[i].equals("--wizard"))
				{
					force_wizard = true;
				}
				if (argv[i].equals("--help"))
				{
					usage();
					System.exit(0);
				}
				else
				{
					setCurrentFileName(argv[i]);
				}
			}

			if (m_currentFileName != null)
			{
				f = new File(m_currentFileName);

				if (f != null && f.exists())
				{
					m_mixConfiguration = new MixConfiguration(new FileReader(f));
				}
				else
				{
					String message[] =
						{
						"The configuration file ",
						m_currentFileName,
						"does not exist.",
						"Would you like to start the wizard interface to create it?"
					};
					m_mixConfiguration = new MixConfiguration();
					if (force_no_wizard)
					{
						showWizard = false;
					}
					else if (force_wizard)
					{
						showWizard = true;
					}
					else
					{
						showWizard = ask("Create new configuration", message);
					}
				}
			}
			else
			{
				m_mixConfiguration = new MixConfiguration();
				if (force_no_wizard)
				{
					showWizard = false;
				}
				else if (force_wizard)
				{
					showWizard = true;
				}
				else
				{
					String message[] =
						{
						"Would you like to start the wizard interface to create a new " +
						"configuration?"
					};

					showWizard = ask("Create new configuration", message);
				}
			}

			ImageIcon icon = loadImage("icon.gif");
			if (icon != null)
			{
				m_MainWindow.setIconImage(icon.getImage());
			}

			if (showWizard)
			{
				m_mainPanel = new ConfigWizard();
			}
			else
			{
				m_mainPanel = new ConfigFrame(MainWindow);
			}

			MainWindow.addWindowListener(new WindowAdapter()
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

			if (!showWizard)
			{
				MainWindow.setJMenuBar( ( (ConfigFrame) m_mainPanel).getMenuBar());
			}

			MainWindow.setContentPane(m_mainPanel);
			MainWindow.pack();
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension size = MainWindow.getSize();
			MainWindow.setLocation( (d.width - size.width) / 2, (d.height - size.height) / 2);
			MainWindow.show();
		}
		catch (Exception e)
		{
			MixConfig.handleException(e);
			System.exit(1);
		}
	}

	/**
	 * Displays a short help message about available command line options for this class.
	 */
	public static void usage()
	{
		String message[] =
			{
			"Usage: java -cp <classpath> mixconfig.MixConfig [options] [configfilename]",
			"where options is one of",
			"--wizard     Always start with wizard interface, don't ask",
			"--no-wizard  Never start with wizard interface, don't ask",
			"--help       Show this message and exit",
			"",
			"If no option is given, and no file name is given or the specified file",
			"does not exist, the user is asked whether to use the wizard interface",
			"or the standard interface."
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
			if (showWizard)
			{
				m_mainPanel = new ConfigWizard();
			}
			else
			{
				m_mainPanel = new ConfigFrame(null);

				//myFrame.pack();//setBounds(10,10,600,650);
			}
			setJMenuBar( ( (ConfigFrame) m_mainPanel).getMenuBar());
			setContentPane(m_mainPanel);
		}
		catch (Exception e)
		{
			MixConfig.handleException(e);
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

	/**
	 * Sets the path name of the currently edited config file. The title of the application
	 * window is changed accordingly.
	 * @param a_currentFileName the new file name of the currently edited configuration.
	 */
	public static void setCurrentFileName(String a_currentFileName)
	{
		m_currentFileName = a_currentFileName;

		StringBuffer s = new StringBuffer(TITLE);
		if (m_currentFileName != null)
		{
			s.append(" - ");
			s.append(m_currentFileName);
		}
		getMainWindow().setTitle(s.toString());
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
	public static boolean ask(String a_title, Object a_message)
	{
		int i = JOptionPane.showConfirmDialog(getMainWindow(),
											  a_message,
											  a_title,
											  JOptionPane.YES_NO_OPTION);
		return (i == JOptionPane.YES_OPTION);
	}

	/** Displays an info message dialog
	 * @param a_title The title of the message dialog
	 * @param a_message The message to be displayed
	 */
	public static void info(String a_title, Object a_message)
	{
		JOptionPane.showMessageDialog(getMainWindow(), a_message, a_title,
									  JOptionPane.INFORMATION_MESSAGE);
	}

	public static void about()
	{
		JOptionPane.showMessageDialog(
			getMainWindow(),
			"Mix Configuration Tool\nVersion: " + VERSION,
			"About",
			JOptionPane.INFORMATION_MESSAGE,
			loadImage("icon.gif"));
	}

	/** Displays a dialog showing the specified throwable and its stack trace.
	 * @param t A <CODE>Throwable</CODE> object to be displayed
	 */
	public static void handleException(Throwable t)
	{
		try
		{
			Box b = Box.createVerticalBox();
			b.add(new JLabel(t.getMessage(), JLabel.LEFT));
			b.add(Box.createVerticalStrut(10));

			if (t.getMessage() == null || t.getMessage().indexOf("exists") < 0)
			{
				StringWriter s = new StringWriter();
				t.printStackTrace(new PrintWriter(s));
				JTextArea tx = new JTextArea(s.toString());
				s.close();
				b.add(new JScrollPane(tx));
			}

			JOptionPane.showMessageDialog(getMainWindow(), b,
										  t.getClass().getName(),
										  JOptionPane.ERROR_MESSAGE);
			t.printStackTrace(System.err);
		}
		catch (Exception e)
		{
			System.err.println(e.getClass().getName() +
							   " occurred while displaying error dialog:");
			e.printStackTrace(System.err);
		}
	}

	public static Frame getMainWindow()
	{
		return m_MainWindow;
	}

	public static ImageIcon loadImage(String name)
	{
		return new ImageIcon(MixConfig.class.getResource(name));
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
			fd2.showSaveDialog(m_mainPanel);
		}
		else
		{
			fd2.showOpenDialog(m_mainPanel);
		}
		m_fileCurrentDir = fd2.getCurrentDirectory();
		return fd2;
	}

	public static byte[] openFile(int type)
	{
		File file =
			showFileDialog(MixConfig.OPEN_DIALOG, type)
			.getSelectedFile();

		if (file != null)
		{
			try
			{
				byte[] buff = new byte[ (int) file.length()];
				FileInputStream fin = new FileInputStream(file);
				fin.read(buff);
				fin.close();
				return buff;
			}
			catch (IOException e)
			{
				MixConfig.handleException(e);
				return null;
			}
		}
		return null;
	}

	public static X509CertificateStructure readCertificate(byte[] cert) throws IOException
	{
		ByteArrayInputStream bin = null;

		if (cert[0] != (DERInputStream.SEQUENCE | DERInputStream.CONSTRUCTED))
		{
			// Probably a Base64 encoded certificate
			BufferedReader in =
				new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cert)));
			StringBuffer sbuf = new StringBuffer();
			String line;

			while ( (line = in.readLine()) != null)
			{
				if (line.equals("-----BEGIN CERTIFICATE-----")
					|| line.equals("-----BEGIN X509 CERTIFICATE-----"))
				{
					break;
				}
			}

			while ( (line = in.readLine()) != null)
			{
				if (line.equals("-----END CERTIFICATE-----")
					|| line.equals("-----END X509 CERTIFICATE-----"))
				{
					break;
				}
				sbuf.append(line);
			}
			bin = new ByteArrayInputStream(Base64.decode(sbuf.toString()));
		}

		if (bin == null && cert[1] == 0x80)
		{
			// a BER encoded certificate
			BERInputStream in =
				new BERInputStream(new ByteArrayInputStream(cert));
			ASN1Sequence seq = (ASN1Sequence) in.readObject();
			DERObjectIdentifier oid = (DERObjectIdentifier) seq.getObjectAt(0);
			if (oid.equals(PKCSObjectIdentifiers.signedData))
			{
				return new X509CertificateStructure(
					(ASN1Sequence)new SignedData(
					(ASN1Sequence) ( (DERTaggedObject) seq
									.getObjectAt(1))
					.getObject())
					.getCertificates()
					.getObjectAt(0));
			}
		}
		else
		{
			if (bin == null)
			{
				bin = new ByteArrayInputStream(cert);
				// DERInputStream
			}
			DERInputStream in = new DERInputStream(bin);
			ASN1Sequence seq = (ASN1Sequence) in.readObject();
			if (seq.size() > 1
				&& seq.getObjectAt(1) instanceof DERObjectIdentifier
				&& seq.getObjectAt(0).equals(PKCSObjectIdentifiers.signedData))
			{
				return X509CertificateStructure.getInstance(
					new SignedData(
					ASN1Sequence.getInstance(
					(ASN1TaggedObject) seq.getObjectAt(1),
					true))
					.getCertificates()
					.getObjectAt(0));
			}
			return X509CertificateStructure.getInstance(seq);
		}
		throw (new IOException("Couldn't read certificate."));
	}
}
