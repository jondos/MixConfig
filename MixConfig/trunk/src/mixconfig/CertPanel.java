/*
 Copyright (c) 2000 - 2005, The JAP-Team
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import anon.crypto.ICertificate;
import anon.crypto.JAPCertificate;
import anon.crypto.PKCS12;
import anon.util.IMiscPasswordReader;
import gui.ClipFrame;
import gui.GUIUtils;
import gui.PasswordBox;
import gui.ValidityDialog;
import logging.LogType;
import mixconfig.wizard.CannotContinueException;

/** This class provides a control to set and display PKCS12 and X.509 certificates.
 * It contains text fields showing issuer name, validity dates etc.<br>
 * If the displayed certificate is PKCS12, it shows buttons for creating, importing, exporting, and deleting
 * certificates, and for changing the certificate's password.<br>
 * If the displayed certificate is an X.509 public certificate, only the buttons for importing, exporting, and deleting
 * are shown.<br>
 * @author ronin &lt;ronin2@web.de&gt;
 */
public class CertPanel extends JPanel implements ActionListener, ChangeListener
{
	/** A <code>String</code> that contains a single char with value 0 */
	private static final String STRING_ZERO = new String(new char[]
		{0});

	// the trusted CSs against those all certificates are tested
	private static final Hashtable TRUSTED_CERTIFICATES =
		JAPCertificate.getInstance("certificates/acceptedCAs/", true);

	private static final String CERT_VALID = "../cert.gif";
	private static final String CERT_INVALID = "../certinvalid.gif";
	private static final String CERT_VALID_INACTIVE = "../certinactive.gif";
	private static final String CERT_INVALID_INACTIVE = "../certinvalidinactive.gif";
	private static final String CERT_DISABLED = "../certdisabled.gif";

	// holds a Vector with all instanciated CertPanels
	private static Vector ms_certpanels = new Vector();

	/** The 'create new certificate' button */
	private JButton m_bttnCreateCert;

	/** The 'import certificate' button */
	private JButton m_bttnImportCert;

	/** The 'export certificate' button */
	private JButton m_bttnExportCert;

	/** The 'change certificate's password' button */
	private JButton m_bttnChangePasswd;

	/** The 'remove certificate' button */
	private JButton m_bttnRemoveCert;

	/** The certificate graphic */
	private JLabel m_certLabel;

	/** A label for the validity start date */
	private JLabel m_textCertValidity;

	/** A text field for the validity end date */
	/*private JTextField m_textCertValidTo;*/

	private JTextPane m_sha1Part1Label;
	private JTextPane m_sha1Part2Label;

	/** Indicates whether the certificate object is PKCS12 (<CODE>true</CODE>) or X.509 (<CODE>false</CODE>) */
	private boolean m_certIsPKCS12 = false;

	/** The certificate */
	private ICertificate m_cert;

	/** The password for the private certificate (<CODE>null</CODE> if the certificate
	 * is an X.509 public certificate)
	 */
	private String m_privCertPasswd;

	/** The validator for newly generated certificates.
	 * @see mixconfig.CertPanel.CertCreationValidator
	 */
	private ICertCreationValidator m_validator;

	/**
	 * Optional component, may be null; stores the current view of the stored certificate.
	 */
	private ICertificateView m_certView;

	/** The list of objects that listen to <CODE>ChangeEvent</CODE>s from this object */
	private Vector m_changeListeners = new Vector();

	/**
	 * Constructs a new instance of <CODE>CertPanel</CODE> with the specified name, the
	 * specified tool tip and the specified X.509 certificate.
	 * @param a_name A name that will be displayed above the panel.
	 * @param a_toolTip A text that will be displayed as a tool tip when the user moves the
	 * mouse over the panel.
	 * @param a_certificate the certificate (X.509)
	 */
	public CertPanel(String a_name, String a_toolTip, JAPCertificate a_certificate)
	{
		this(a_name, a_toolTip, (ICertificate) a_certificate, false);
	}

	/**
	 * Constructs a new instance of <CODE>CertPanel</CODE> with the specified name, the
	 * specified tool tip and the specified X.509 certificate.
	 * @param a_name A name that will be displayed above the panel.
	 * @param a_toolTip A text that will be displayed as a tool tip when the user moves the
	 * mouse over the panel.
	 * @param a_certificate the certificate (PKCS12)
	 */
	public CertPanel(String a_name, String a_toolTip, PKCS12 a_certificate)
	{
		this(a_name, a_toolTip, (ICertificate) a_certificate, true);
	}

	/**
	 * Constructs a new instance of <CODE>CertPanel</CODE> with the specified name, the
	 * specified tool tip and the specified certificate.
	 * @param a_name A name that will be displayed above the panel.
	 * @param a_toolTip A text that will be displayed as a tool tip when the user moves the
	 * mouse over the panel.
	 * @param a_certificate the certificate (PKCS12 or X.509)
	 * @param a_pkcs12 true if the certificate is PKCS12; false otherwise
	 */
	private CertPanel(String a_name, String a_toolTip, ICertificate a_certificate,
					  boolean a_pkcs12)
	{
		ms_certpanels.addElement(this);

		m_certIsPKCS12 = a_pkcs12;
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.insets = new Insets(5, 5, 5, 5);

		setBorder(new TitledBorder(a_name));
		if (a_toolTip != null)
		{
			setToolTipText(a_toolTip);
		}

		// --- Buttons
		m_bttnImportCert = new JButton("Import");

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;

		m_bttnImportCert.addActionListener(this);
		layout.setConstraints(m_bttnImportCert, constraints);
		add(m_bttnImportCert);

		m_bttnExportCert = new JButton("Export");

		constraints.gridx = 1;
		constraints.gridy = 0;

		m_bttnExportCert.addActionListener(this);
		layout.setConstraints(m_bttnExportCert, constraints);
		add(m_bttnExportCert);

		m_bttnRemoveCert = new JButton("Remove");

		constraints.gridx = 2;
		constraints.gridy = 0;

		m_bttnRemoveCert.addActionListener(this);
		m_bttnRemoveCert.setEnabled(false);
		layout.setConstraints(m_bttnRemoveCert, constraints);
		add(m_bttnRemoveCert);

		if (m_certIsPKCS12)
		{
			m_bttnCreateCert = new JButton("Create");
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			m_bttnCreateCert.addActionListener(this);
			layout.setConstraints(m_bttnCreateCert, constraints);
			add(m_bttnCreateCert);


			m_bttnChangePasswd = new JButton("Change Password");
			constraints.gridx = 1;
			constraints.gridy = 1;
			constraints.gridwidth = 2;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			m_bttnChangePasswd.addActionListener(this);
			layout.setConstraints(m_bttnChangePasswd, constraints);
			add(m_bttnChangePasswd);
			constraints.fill = GridBagConstraints.NONE;
		}

		m_certLabel = new JLabel(MixConfig.loadImageIcon(CERT_DISABLED));
		m_certLabel.setBorder(null);
		m_certLabel.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent a_event)
			{
				if (m_certLabel.isEnabled())
				{
					CertDetailsDialog dialog = new CertDetailsDialog(getParent(), m_cert.getX509Certificate());
					dialog.setVisible(true);
				}
			}

			public void mouseEntered(MouseEvent a_event)
			{
				if (m_certLabel.isEnabled())
				{
					updateCertificateIcon(true);
				}
			}

			public void mouseExited(MouseEvent a_event)
			{
				if (m_certLabel.isEnabled())
				{
					updateCertificateIcon(false);
				}
			}

		});
		constraints.gridy = 2;
		constraints.gridheight = 3;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		add(m_certLabel, constraints);

		constraints.gridheight = 1;
		constraints.gridwidth = 3;
		constraints.gridx++;
		constraints.insets = new Insets(0, 5, 0, 0);

		m_sha1Part1Label = GUIUtils.createSelectableAndResizeableLabel(this);
		m_sha1Part1Label.setFont(new Font(m_sha1Part1Label.getFont().getName(),Font.BOLD, 10));
		m_sha1Part1Label.setPreferredSize( (new JLabel("00:00:00:00:00:00:00:00:00:00")).getPreferredSize());

		add(m_sha1Part1Label, constraints);

		m_sha1Part2Label = GUIUtils.createSelectableAndResizeableLabel(this);
		m_sha1Part2Label.setFont(new Font(m_sha1Part2Label.getFont().getName(),Font.BOLD, 10));
		m_sha1Part2Label.setPreferredSize( (new JLabel("00:00:00:00:00:00:00:00:00:00")).getPreferredSize());

		m_sha1Part1Label.setToolTipText("SHA-1 Fingerprint");
		m_sha1Part2Label.setToolTipText("SHA-1 Fingerprint");

		constraints.gridy++;
		constraints.insets = new Insets(0, 5, 5, 0);
		add(m_sha1Part2Label, constraints);

		m_textCertValidity = new JLabel();
		m_textCertValidity.setToolTipText("Validity");
		m_textCertValidity.setPreferredSize(new JLabel("00.00.0000 - 00.00.0000").getPreferredSize());

		constraints.gridx = 1;
		constraints.gridy++;
		constraints.gridwidth = 5;
		constraints.weightx = 1;
		layout.setConstraints(m_textCertValidity, constraints);
		add(m_textCertValidity, constraints);
		constraints.weightx = 0;


		enableButtons();

		if (a_certificate != null)
		{
			setCert(a_certificate.toByteArray());
		}
	}

	/** Sets the validator for the creation of new certificates.
	 * @param a_cg the new validator
	 * @see #CertCreationValidator
	 */
	public void setCertCreationValidator(ICertCreationValidator a_cg)
	{
		this.m_validator = a_cg;
	}

	public void setEnabled(boolean enabled)
	{
		Component components[] = getComponents();
		TitledBorder border;

		for (int i = 0; i < components.length; i++)
		{
			if (! (components[i] instanceof JButton))
			{
				components[i].setEnabled(enabled);
			}
		}

		if (getBorder() instanceof TitledBorder)
		{
			border = new TitledBorder( ( (TitledBorder) getBorder()).getTitle());
			if (!enabled)
			{
				border.setTitleColor(Color.gray);
			}
			setBorder(border);
		}

		super.setEnabled(enabled);

		enableButtons();
	}

	public void actionPerformed(ActionEvent a_event)
	{
		Object source = a_event.getSource();
		try
		{
			if (source == this.m_bttnChangePasswd)
			{
				changePasswd();
			}
			else if (source == this.m_bttnCreateCert)
			{
				generateNewCert();
			}
			else if (source == this.m_bttnExportCert)
			{
				exportCert();
			}
			else if (source == this.m_bttnRemoveCert)
			{
				removeCert();
			}
			else if (source == this.m_bttnImportCert)
			{
				if (this.m_certIsPKCS12)
				{
					importPrivCert();
				}
				else
				{
					importPubCert();
				}
			}
			enableButtons();
		}
		catch (Exception ex)
		{
			MixConfig.handleError(ex, null, LogType.GUI);
		}
	}

	public void stateChanged(ChangeEvent a_event)
	{
		try
		{
			if (a_event.getSource() instanceof CertificateGenerator)
			{
				CertificateGenerator generator = (CertificateGenerator) a_event.getSource();
				if (generator.getCertificate() == null)
				{
					throw new RuntimeException("Certificate creation failed for some unknown reason!");
				}
				setCertificate(generator.getCertificate(), generator.getPassword());
			}
		}
		catch (Exception ex)
		{
			MixConfig.handleError(ex, "Certificate creation failed", LogType.MISC);
		}
	}

	public char[] getPrivateCertPassword()
	{
		if (m_privCertPasswd == null)
		{
			return new char[0];
		}
		return m_privCertPasswd.toCharArray();
	}

	/**
	 * Returns the certificate.
	 * @return The certificate
	 */
	public ICertificate getCert()
	{
		return m_cert;
	}

	public void setCert(JAPCertificate a_certificate)
	{
		setCert(a_certificate.toByteArray());
	}

	/**
	 * Returns the current certificate view.
	 * @return the current certificate view or null if no view is registered
	 */
	public ICertificateView getCertificateView()
	{
		return m_certView;
	}

	/**
	 * Sets a view for the certificate stored in this panel.
	 * @param a_certificateView a certificate view
	 */
	public void setCertificateView(ICertificateView a_certificateView)
	{
		m_certView = a_certificateView;
	}

	/** Set the certificate. The method decides according to {@link #isPKCS12()} whether to set the
	 * PKCS12 or X.509 certificate.
	 * @param cert A certificate, which must be of the appropriate type for this object.
	 * @throws IOException If an error occurs while converting the certificate
	 * @throws IllegalArgumentException If the certificate is not of the required type
	 */
	public void setCert(byte[] cert) throws IllegalArgumentException
	{
		JAPCertificate x509cert = JAPCertificate.getInstance(cert);

		if (cert == null)
		{
			m_cert = null;
			clearCertInfo();
		}
		else if (m_certIsPKCS12)
		{
			if (x509cert != null)
			{
				( (PKCS12) m_cert).setX509Certificate(x509cert);
			}
			else
			{
				PasswordBox pb = new PasswordBox(MixConfig.getMainWindow(), "Enter the password",
												 PasswordBox.ENTER_PASSWORD, null);
				CertPanelPasswordReader pwReader = new CertPanelPasswordReader(pb);
				PKCS12 privateCertificate = PKCS12.getInstance(cert, pwReader);
				setCertificate(privateCertificate, pwReader.getPassword());
			}
		}
		else if (x509cert != null)
		{
			setCertInfo(x509cert);
			m_cert = x509cert;
		}
		else
		{
			throw new IllegalArgumentException("Unknown certificate type!");
		}
		enableButtons();
		fireStateChanged();
	}

	/** Adds a <CODE>ChangeListener</CODE> to this object's listeners list.
	 * @param a_cl A new <CODE>ChangeListener</CODE>
	 */
	public void addChangeListener(ChangeListener a_cl)
	{
		m_changeListeners.addElement(a_cl);
	}

	/** Sends a <CODE>ChangeEvent</CODE> to all of this object's <CODE>ChangeListener</CODE>s */
	private void fireStateChanged()
	{
		if (m_certView != null)
		{
			m_certView.update(getCert());
		}

		ChangeEvent event = new ChangeEvent(this);
		for (int i = 0; i < m_changeListeners.size(); i++)
		{
			( (ChangeListener) m_changeListeners.elementAt(i)).stateChanged(event);
		}
	}

	/** Sets the PKCS12 certificate and the password.
	 * @param pkcs12 The new PKCS12 certificate.
	 * @param strPrivCertPasswd The new password for the certificate.
	 */
	private void setCertificate(PKCS12 pkcs12, char[] strPrivCertPasswd)
	{
		if (pkcs12 == null)
		{
			return;
		}

		JAPCertificate x509cs = pkcs12.getX509Certificate();
		setCertInfo(x509cs);
		m_cert = pkcs12;
		if (strPrivCertPasswd != null)
		{
			m_privCertPasswd = new String(strPrivCertPasswd);
		}
		else
		{
			m_privCertPasswd = new String();
		}
		if (m_privCertPasswd.equals(STRING_ZERO)) //change a passwd which has len=1 and char[0]=0
		{
			//to a passwd with len=0 [because both seam to be 'empty'
			//passwds but are not equal!]
			m_privCertPasswd = new String();
		}
		enableButtons();
		fireStateChanged();
	}

	private void updateCertificateIcon(boolean a_bActive)
	{
		if (m_cert != null)
		{
			boolean bValid = m_cert.getX509Certificate().verify(TRUSTED_CERTIFICATES.elements());

			if (a_bActive)
			{
				if (bValid)
				{
					m_certLabel.setIcon(MixConfig.loadImageIcon(CERT_VALID));
				}
				else
				{
					m_certLabel.setIcon(MixConfig.loadImageIcon(CERT_INVALID));
				}
			}
			else
			{
				if (bValid)
				{
					m_certLabel.setIcon(MixConfig.loadImageIcon(CERT_VALID_INACTIVE));
				}
				else
				{
					m_certLabel.setIcon(MixConfig.loadImageIcon(CERT_INVALID_INACTIVE));
				}
			}

			if (bValid)
			{
				m_certLabel.setToolTipText("");
			}
			else
			{
				m_certLabel.setToolTipText(
							"The certificate cannot be verified! " +
							"It is not signed by a trusted certificate authority.");
			}
		}
		else
		{
			m_certLabel.setIcon(MixConfig.loadImageIcon(CERT_DISABLED));
			m_certLabel.setToolTipText("");
		}
	}

	/** Enables/disables the buttons according to this object's state.
	 * The buttons for creating and importing certificates are enabled if the stored
	 * certificate is <CODE>null</CODE>. Otherwise, the buttons for exporting and removing
	 * the certificate and for changing the password are enabled.
	 */
	private void enableButtons()
	{
		boolean cert = (m_cert != null);
		boolean enabled = isEnabled();

		if (m_bttnCreateCert != null)
		{
			m_bttnCreateCert.setEnabled((m_validator != null) && (enabled && !cert));
		}
		if (m_bttnImportCert != null)
		{
			m_bttnImportCert.setEnabled(enabled);
		}
		if (m_bttnExportCert != null)
		{
			m_bttnExportCert.setEnabled(enabled && cert);
		}
		if (m_bttnRemoveCert != null)
		{
			m_bttnRemoveCert.setEnabled(enabled && cert);
		}
		if (m_bttnChangePasswd != null)
		{
			m_bttnChangePasswd.setEnabled(enabled && cert);
		}
		if (m_certLabel != null)
		{
			m_certLabel.setEnabled(enabled && cert);
		}


		updateCertificateIcon(false);


		if (!cert || m_cert.getX509Certificate().getValidity().isValid(
				  Calendar.getInstance().getTime()))
		{
			m_textCertValidity.setForeground(new JLabel().getForeground());
			m_textCertValidity.setToolTipText("");
		}
		else
		{
			m_textCertValidity.setForeground(Color.red);
			m_textCertValidity.setToolTipText("The certificate has expired.");
		}

	}

	/** Imports an X.509 public certificate. The user is prompted to give the name of a
	 * file from which to import. If that fails, the user is prompted to paste a
	 * certificate from the system clipboard.
	 * @throws IOException If an error occurs while reading the certificate.
	 */
	private void importPubCert() throws IOException
	{
		byte[] cert = null;
		try
		{
			cert = MixConfig.openFile(MixConfig.FILTER_CER | MixConfig.FILTER_B64_CER);
		}
		catch (RuntimeException a_ex)
		{
			ClipFrame open = new ClipFrame(this, "Paste a certificate to be imported in " +
										   "the area provided.", true);
			open.setVisible(true);
			cert = open.getText().getBytes();
		}
		if (cert == null)
		{
			return;
		}

		setCert(cert);
	}

	/** Imports a PKCS12 certificate. The user is prompted to give the name of a
	 * file from which to import. If that fails, the user is prompted to paste a
	 * certificate from the system clipboard.
	 * @throws IOException If an error occurs while reading the certificate.
	 */
	private void importPrivCert() throws IOException
	{
		byte[] buff;
		PKCS12 pkcs12;
		try
		{
			pkcs12 = (PKCS12) m_cert;
			int filter = MixConfig.FILTER_PFX;
			if (m_cert != null)
			{
				filter |= (MixConfig.FILTER_B64_CER | MixConfig.FILTER_CER);
			}
			buff = MixConfig.openFile(filter);
		}
		catch (Exception e)
		{
			/** @todo Find out which exception exactly is thrown, and catch only that */
			JOptionPane.showMessageDialog(
				this,
				"Import of a private key with certificate\n" +
				"is not supported when running as an applet.",
				"Not supported!",
				JOptionPane.ERROR_MESSAGE);
			m_bttnImportCert.setEnabled(false);
			return;
		}

		if (buff == null)
		{
			return;
		}

		//if own key is already set, then maybe only an other certificate for this key is imported...
		boolean bIsPubCert = false;
		if (pkcs12 != null)
		{
			JAPCertificate cert1 = JAPCertificate.getInstance(buff);
			if (cert1 != null)
			{
				bIsPubCert = true;
				if (pkcs12.setX509Certificate(cert1))
				{
					setCertificate(pkcs12, m_privCertPasswd.toCharArray());
				}
				else
				{
					JOptionPane.showMessageDialog(
						this,
						"This public key certificate does not\n" +
						"belong to your private key!",
						"Wrong certificate!",
						JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		if (!bIsPubCert)
		{
			setCert(buff);
		}
	}

	/** Prompts the user to enter a new password for the PKCS12 certificate. */
	private void changePasswd()
	{
		String strMessage = null;
		if (m_validator != null)
		{
			strMessage = m_validator.getPasswordInfoMessage();
		}

		PasswordBox dialog =
			new PasswordBox(MixConfig.getMainWindow(), "Change password",
							PasswordBox.CHANGE_PASSWORD, strMessage);
		GUIUtils.positionWindow(dialog, MixConfig.getMainWindow());

		while (true)
		{
			dialog.setVisible(true);
			char[] passwd = dialog.getPassword();
			char[] oldpasswd = dialog.getOldPassword();

			if (passwd == null)
			{
				break;
			}
			try
			{
				if (!m_privCertPasswd.equals(new String(oldpasswd)))
				{
					throw new SecurityException("Wrong password.");
				}
				m_privCertPasswd = new String(passwd);
				break;
			}
			catch (SecurityException a_ex)
			{
				javax.swing.JOptionPane.showMessageDialog(
					this,
					a_ex.getMessage(),
					"Password Error",
					javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		}
		fireStateChanged();
	}

	/** Generates a new certificate. For this purpose, a new thread is started that
	 * works in the background.
	 * @throws NullPointerException If this object's certificate generation validator is <CODE>null</CODE>.
	 * @see #CertCreationValidator
	 */
	private void generateNewCert() throws NullPointerException
	{
		Dimension windowSize;
		Point windowLocation;
		char[] passwd;

		if (!m_validator.isValid())
		{
			throw new CannotContinueException(
				 m_validator.getInvalidityMessages());
		}

		ValidityDialog vdialog = new ValidityDialog(this, "Validity");
		vdialog.setVisible(true);
		windowSize = vdialog.getSize();
		windowLocation = vdialog.getLocation();

		if (vdialog.getValidity() == null)
		{
			return;
		}


	    //Dialog for password
		PasswordBox dialog = new PasswordBox(MixConfig.getMainWindow(), "New Password",
											 PasswordBox.NEW_PASSWORD, m_validator.getPasswordInfoMessage());
		dialog.setSize(windowSize);
		dialog.setLocation(windowLocation);
		dialog.setVisible(true);
		passwd = dialog.getPassword();
		if (passwd == null)
		{
			return;
		}

	    //Dialog which shows the progress
	    CertificateGenerator worker = new CertificateGenerator(
			  m_validator.getSigName(), m_validator.getExtensions(),
			  vdialog.getValidity(), passwd, dialog);

		worker.addChangeListener(this);
		worker.start();
		worker.showBussyWin();

	}

	/** Exports the certificate to a file.
	 */
	private void exportCert()
	{
		if (m_cert == null)
		{
			throw new IllegalArgumentException("No certificate to export.");
		}

		try
		{
			int filter = MixConfig.FILTER_CER | MixConfig.FILTER_B64_CER;
			if (m_cert instanceof PKCS12)
			{
				filter = filter | MixConfig.FILTER_PFX;
			}

			JFileChooser fd = MixConfig.showFileDialog(MixConfig.SAVE_DIALOG, filter);
			FileFilter ff = fd.getFileFilter();

			int type;
			if (ff instanceof SimpleFileFilter)
			{
				type = ( (SimpleFileFilter) ff).getFilterType();
			}
			else
			{
				type = MixConfig.FILTER_B64_CER;
			}

			File file = fd.getSelectedFile();
			if (file != null)
			{
				String fname = file.getName();

				if (fname.indexOf('.') < 0)
				{
					String extensions[] =
						{
						".pfx", ".der.cer", ".b64.cer"};
					int ext = 0;
					// we can't use the MixConfig constants as array indices
					// because we can't rely that their values don't change
					// in future versions
					switch (type)
					{
						case MixConfig.FILTER_PFX:
							ext = 0;
							break;
						case MixConfig.FILTER_CER:
							ext = 1;
							break;
						case MixConfig.FILTER_B64_CER:
							ext = 2;
							break;
						default:
					}
					file = new File(file.getParent(), fname + extensions[ext]);
				}

				FileOutputStream fout = new FileOutputStream(file);
				switch (type)
				{
					case MixConfig.FILTER_PFX:
						fout.write( ( (PKCS12) m_cert).toByteArray(getPrivateCertPassword()));
						break;
					case MixConfig.FILTER_CER:
						fout.write(m_cert.getX509Certificate().toByteArray());
						break;
					case MixConfig.FILTER_B64_CER:
						fout.write(m_cert.getX509Certificate().toByteArray(true));
						break;
					default:
				}
				fout.close();
			}
			return;
		}
		catch (IOException a_e)
		{
			MixConfig.handleError(a_e, null, LogType.MISC);
			ClipFrame save =
				new ClipFrame(this,
					"I/O error while saving, try clipboard. " +
					"Copy and Save this file in a new Location.",
					false);
			save.setText(new String(m_cert.getX509Certificate().toByteArray(true)));
			save.setVisible(true);
		}
	}

	/** Fills the text fields in the panel with the data from the specified certificate.
	 * @param a_x509cs an X.509 certificate structure containing the required data about subject,
	 * issuer, validity etc.
	 */
	private void setCertInfo(JAPCertificate a_x509cs)
	{
		//m_textCertCN.setText(a_x509cs.getSubject().toString());
		//m_textCertIssuer.setText(a_x509cs.getIssuer().toString());
		Calendar cal = Calendar.getInstance();
		cal.setTime(a_x509cs.getStartDate().getDate());
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH) + 1;
		int year = cal.get(Calendar.YEAR);
		String startDate = day + "." + month + "." + year;
		cal.setTime(a_x509cs.getEndDate().getDate());
		day = cal.get(Calendar.DAY_OF_MONTH);
		month = cal.get(Calendar.MONTH) + 1;
		year = cal.get(Calendar.YEAR);
		String endDate = day + "." + month + "." + year;
		m_textCertValidity.setText(startDate + " - " + endDate);

		String fp = a_x509cs.getSHA1Fingerprint();
		fp = fp.replace(':', ' ');
		String fp1 = fp.substring(0, fp.length() / 2);
		String fp2 = fp.substring(fp.length() / 2 + 1, fp.length());
		m_sha1Part1Label.setText(fp1);
		m_sha1Part2Label.setText(fp2);
	}

	/** Clears the text fields that display info about the current certificate. */
	private void clearCertInfo()
	{
		//m_textCertCN.setText("");
		//m_textCertIssuer.setText("");
		m_textCertValidity.setText("");
		m_sha1Part1Label.setText("");
		m_sha1Part2Label.setText("");
	}

	/** Removes the certficate from the panel */
	public void removeCert()
	{
		this.m_cert = null;
		clearCertInfo();
		fireStateChanged();
	}

	private class CertPanelPasswordReader implements IMiscPasswordReader
	{
		private char[] m_password;
		private Vector m_certPanels;
		private boolean m_triedOwnPassword;
		private IMiscPasswordReader m_passwordReader;

		public CertPanelPasswordReader(IMiscPasswordReader a_passwordReader)
		{
			m_certPanels = (Vector) ms_certpanels.clone();
			m_triedOwnPassword = false;
			m_passwordReader = a_passwordReader;
		}

		public String readPassword(Object message)
		{
			if (!m_triedOwnPassword && getPrivateCertPassword() != null)
			{
				m_triedOwnPassword = true;
				m_password = getPrivateCertPassword();
			}
			else
			{
				m_password = null;
				while (m_certPanels.size() > 0)
				{
					m_password = ( (CertPanel) (m_certPanels.elementAt(0))).getPrivateCertPassword();
					m_certPanels.removeElementAt(0);
					if (m_password != null)
					{
						break;
					}
				}

			}

			if (m_password == null)
			{
				m_password = m_passwordReader.readPassword("").toCharArray();
			}

			return new String(m_password);
		}

		public char[] getPassword()
		{
			return m_password;
		}
	}
}
