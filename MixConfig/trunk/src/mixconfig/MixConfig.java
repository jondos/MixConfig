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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.BERInputStream;
import org.bouncycastle.asn1.DERInputStream;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.SignedData;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import anon.util.*;

/** \mainpage
 This is a tool which one can use for creating a configuration file for a Mix. This configuration file
 contains an XML struct with the foolowing elements:
 \verbinclude "./MixConfiguration.xml"
 */
public class MixConfig extends JApplet
{
	private static ConfigFrame m_ConfigFrame;
	private static Frame m_MainWindow;
	private static File m_fileCurrentDir;
	public final static int SAVE_DIALOG = 1;
	public final static int OPEN_DIALOG = 2;
	public final static int FILTER_ALL = 0;
	public final static int FILTER_CER = 1;
	public final static int FILTER_XML = 2;
	public final static int FILTER_PFX = 4;
	public final static int FILTER_B64_CER = 8;
	public final static String VERSION = "00.02.026"; //NEVER change the layout of this line!!

	public static void main(String[] args)
	{
		JFrame MainWindow = new JFrame("Mix Configuration Tool");
		m_MainWindow = MainWindow;
		ImageIcon icon = loadImage("icon.gif");
		if (icon != null)
		{
			m_MainWindow.setIconImage(icon.getImage());
		}
		m_ConfigFrame = new ConfigFrame(MainWindow);

		MainWindow.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});

		MainWindow.setJMenuBar(m_ConfigFrame.getMenuBar());
		MainWindow.setContentPane(m_ConfigFrame);
		MainWindow.pack();
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension size = MainWindow.getSize();
		MainWindow.setLocation( (d.width - size.width) / 2, (d.height - size.height) / 2);
		MainWindow.show();
	}

	public void init() // For the applet.
	{

		// Let's search for the parent frame:
		java.awt.Component comp = this;
		while ( (comp = comp.getParent()) != null &&
			   ! (comp instanceof java.awt.Frame))
		{
			;
		}
		m_MainWindow = (Frame) comp;
		m_ConfigFrame = new ConfigFrame(null);
		//myFrame.pack();//setBounds(10,10,600,650);
		setJMenuBar(m_ConfigFrame.getMenuBar());
		setContentPane(m_ConfigFrame);
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
			fd2.showSaveDialog(m_ConfigFrame);
		}
		else
		{
			fd2.showOpenDialog(m_ConfigFrame);
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
			catch (Exception e)
			{
				System.out.println("Error reading: " + file);
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
