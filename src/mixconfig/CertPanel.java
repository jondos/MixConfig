package mixconfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.bouncycastle.asn1.DERInputStream;
import anon.crypto.DSAKeyPair;
import anon.crypto.JAPCertificate;
import anon.crypto.PKCS12;
import anon.crypto.ICertificate;

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

	/** The 'create new certificate' button */
	private JButton m_bttnCreateCert;

	/** The 'import certificate' button */
	private JButton m_bttnImportPub;

	/** The 'export certificate' button */
	private JButton m_bttnExportPub;

	/** The 'change certificate's password' button */
	private JButton m_bttnChangePasswd;

	/** The 'remove certificate' button */
	private JButton m_bttnRemoveCert;

	/** A text field for the subject name */
	private JTextField m_textCertCN;

	/** A text field for the issuer name */
	private JTextField m_textCertIssuer;

	/** A text field for the validity start date */
	private JTextField m_textCertValidFrom;

	/** A text field for the validity end date */
	private JTextField m_textCertValidTo;

	/** Indicates whether the certificate object is PKCS12 (<CODE>true</CODE>) or X.509 (<CODE>false</CODE>) */
	private boolean m_certIsPKCS12;

	/** The certificate */
	private ICertificate m_cert;

	/** The password for the private certificate (<CODE>null</CODE> if the certificate
	 * is an X.509 public certificate)
	 */
	private String m_privCertPasswd;

	/** The validator for newly generated certificates.
	 * @see mixconfig.CertPanel.CertCreationValidator
	 */
	private CertCreationValidator m_validator;

	/** The list of objects that listen to <CODE>ChangeEvent</CODE>s from this object */
	private Vector m_changeListeners = new Vector();

	/** Constructs a new instance of <CODE>CertPanel</CODE> with the specified name, the
	 * specified tool tip and the specified PKCS12 certificate.
	 * @param a_name A name that will be displayed above the panel.
	 * @param a_toolTip A text that will be displayed as a tool tip when the user moves the mouse over
	 * the panel.
	 * @param a_cert The PKCS12 private certificate
	 */
	public CertPanel(String a_name, String a_toolTip, PKCS12 a_cert)
	{
		this(a_name, a_toolTip, true);
		if (a_cert != null)
		{
			this.setPrivCert(a_cert);
		}
	}

	/** Constructs a new instance of <CODE>CertPanel</CODE> with the specified name, the
	 * specified tool tip and the specified X.509 public certificate.
	 * @param a_name A name that will be displayed above the panel.
	 * @param a_toolTip A text that will be displayed as a tool tip when the user moves the mouse over
	 * the panel.
	 * @param a_cert The X.509 public certificate
	 * @throws IOException If an error occurs while extracting the certificte from the byte array
	 * <code>a_cert</code>
	 */
	public CertPanel(String a_name, String a_toolTip, byte[] a_cert) throws IOException
	{
		this(a_name, a_toolTip, false);
		if (a_cert != null)
		{
			this.setPubCert(a_cert);
		}
	}

	/** Constructs a new instance of <CODE>CertPanel</CODE>. Wrapped by the public constructors
	 * @param a_name A name that will be displayed above the panel.
	 * @param a_toolTip A text that will be displayed as a tool tip when the user moves the mouse over
	 * the panel.
	 * @param a_private <CODE>true</CODE> indicates that the certificate is expected to be PKCS12,
	 * <code>false</code> otherwise.
	 */
	private CertPanel(String a_name, String a_toolTip, boolean a_private)
	{
		m_certIsPKCS12 = a_private;
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints d = new GridBagConstraints();
		d.anchor = GridBagConstraints.NORTHWEST;
		d.insets = new Insets(5, 5, 5, 5);

		setBorder(new TitledBorder(a_name));
		if (a_toolTip != null)
		{
			setToolTipText(a_toolTip);
		}

		// --- Buttons

		if (a_private)
		{
			m_bttnCreateCert = new JButton("Create a New One");
			d.gridx = 1;
			d.gridy = 0;
			d.gridwidth = 1;
			d.fill = GridBagConstraints.NONE;
			m_bttnCreateCert.addActionListener(this);
			m_bttnCreateCert.setActionCommand("Create");
			layout.setConstraints(m_bttnCreateCert, d);
			add(m_bttnCreateCert);
		}

		m_bttnImportPub = new JButton("Import...");
		if (a_private)
		{
			d.gridx = 2;
		}
		else
		{
			d.gridx = 1;
		}
		d.gridy = 0;
		if (a_private)
		{
			d.gridwidth = 1;
		}
		else
		{
			d.fill = GridBagConstraints.NONE;
		}
		m_bttnImportPub.addActionListener(this);
		m_bttnImportPub.setActionCommand("ImportOwnCert");
		layout.setConstraints(m_bttnImportPub, d);
		add(m_bttnImportPub);

		m_bttnExportPub = new JButton("Export...");
		if (a_private)
		{
			d.gridx = 3;
			d.gridy = 0;
			d.gridwidth = 1;
		}
		else
		{
			d.gridx = 2;
		}
		m_bttnExportPub.addActionListener(this);
		m_bttnExportPub.setActionCommand("ExportOwnPubCert");
		layout.setConstraints(m_bttnExportPub, d);
		add(m_bttnExportPub);

		if (a_private)
		{
			m_bttnChangePasswd = new JButton("Change Password");
			d.gridx = 4;
			m_bttnChangePasswd.addActionListener(this);
			m_bttnChangePasswd.setActionCommand("passwd");
			layout.setConstraints(m_bttnChangePasswd, d);
			add(m_bttnChangePasswd);
		}

		m_bttnRemoveCert = new JButton("Remove");
		if (a_private)
		{
			d.gridx = 5;
		}
		else
		{
			d.gridx = 3;
		}
		m_bttnRemoveCert.addActionListener(this);
		m_bttnRemoveCert.setActionCommand("RemoveOwnCert");
		m_bttnRemoveCert.setEnabled(false);
		layout.setConstraints(m_bttnRemoveCert, d);
		add(m_bttnRemoveCert);

		// ------ Text fields

		JLabel name1 = new JLabel("Subject Name");
		d.gridx = 0;
		d.gridy = 1;
		d.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(name1, d);
		add(name1);

		m_textCertCN = new JTextField();
		m_textCertCN.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		layout.setConstraints(m_textCertCN, d);
		add(m_textCertCN);

		JLabel name9 = new JLabel("Issuer Name");
		d.gridx = 0;
		d.gridy = 2;
		d.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(name9, d);
		add(name9);

		m_textCertIssuer = new JTextField();
		m_textCertIssuer.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		layout.setConstraints(m_textCertIssuer, d);
		add(m_textCertIssuer);

		JLabel from1 = new JLabel("Valid From");
		d.gridx = 0;
		d.gridy = 3;
		d.gridwidth = 1;
		d.weightx = 0;
		layout.setConstraints(from1, d);
		add(from1);

		m_textCertValidFrom = new JTextField();
		m_textCertValidFrom.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		layout.setConstraints(m_textCertValidFrom, d);
		add(m_textCertValidFrom);

		JLabel to1 = new JLabel("Valid To");
		d.gridx = 0;
		d.gridy = 4;
		d.gridwidth = 1;
		d.weightx = 0;
		layout.setConstraints(to1, d);
		add(to1);

		m_textCertValidTo = new JTextField();
		m_textCertValidTo.setEditable(false);
		d.gridx = 1;
		d.gridwidth = 5;
		d.weightx = 1;
		layout.setConstraints(m_textCertValidTo, d);
		add(m_textCertValidTo);

		enableButtons();
	}

	/** Sets the validator for the creation of new certificates.
	 * @param a_cg the new validator
	 * @see #CertCreationValidator
	 */
	public void setCertCreationValidator(CertCreationValidator a_cg)
	{
		this.m_validator = a_cg;
	}

	/** Returns whether the certificate is PKCS12 or X.509
	 * @return <code>true</code> if the certificate is PKCS12, <CODE>false</code> if X.509
	 */
	public boolean isPKCS12()
	{
		return this.m_certIsPKCS12;
	}

	public void setEnabled(boolean enabled)
	{
		Component c[] = getComponents();

		for (int i = 0; i < c.length; i++)
		{
			if (! (c[i] instanceof JButton))
			{
				c[i].setEnabled(enabled);
			}
		}

		super.setEnabled(enabled);

		enableButtons();
	}

	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		try
		{
			if (source == this.m_bttnChangePasswd)
			{
				changePasswd();
			}
			else if (source == this.m_bttnCreateCert)
			{
				this.generateNewCert();
			}
			else if (source == this.m_bttnExportPub)
			{
				this.exportCert();
			}
			else if (source == this.m_bttnRemoveCert)
			{
				this.m_cert = null;
				clearCertInfo();
				fireStateChanged();
			}
			else if (source == this.m_bttnImportPub)
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
			MixConfig.handleException(ex);
		}
	}

	public void stateChanged(ChangeEvent e)
	{
		try
		{
			if (e.getSource() instanceof KeyPairGenerator)
			{
				setPrivCert( ( (KeyPairGenerator) e.getSource()).getCertificate(),
							( (KeyPairGenerator) e.getSource()).getPassword());

			}
		}
		catch (Exception ex)
		{
			MixConfig.handleException(ex);
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

	/** Returns the certificate.
	 * @return The certificate
	 */
	public ICertificate getCert()
	{
		return m_cert;
	}

	/** Set the certificate. The method decides according to {@link #isPKCS12()} whether to set the
	 * PKCS12 or X.509 certificate.
	 * @param b A certificate, which must be of the appropriate type for this object.
	 * @throws IOException If an error occurs while converting the certificate
	 * @throws IllegalArgumentException If the certificate is not of the required type
	 */
	public void setCert(byte[] b) throws IOException, IllegalArgumentException
	{
		if (this.m_certIsPKCS12)
		{
			setPrivCert(b);
		}
		else
		{
			setPubCert(b);
		}
	}

	/** Sets the PKCS12 certificate.
	 * @param pkcs12 The new PKCS12 certificate.
	 */
	public void setPrivCert(PKCS12 pkcs12)
	{
		setPrivCert(pkcs12, null);
	}

	/** Sets the PKCS12 certificate.
	 * @param cert The new PKCS12 certificate.
	 * @throws IllegalArgumentException If the certificate is not of the required type.
	 */
	public void setPrivCert(byte[] cert) throws IllegalArgumentException
	{
		if (!this.m_certIsPKCS12)
		{
			throw new IllegalArgumentException("This panel does not support " +
											   "PKCS12 certificate structures.");
		}

		if (cert == null)
		{
			this.m_cert = null;
			this.clearCertInfo();
		}
		else
		{
			//Ok the problem is that an empty string (size =0) is different from an empty string (first char=0)
			// so we try both...
			char[] passwd = new char[0];
			try
			{
				setPrivCert(cert, passwd);
				return;
			}
			catch (Exception e)
			{
				/* TODO: Catch only the exception thrown in case of wrong password */
				//e.printStackTrace();
			}
			passwd = new char[]
				{
				0};
			while (passwd != null)
			{
				try
				{
					setPrivCert(cert, passwd);
					break;
				}
				catch (Exception e)
				{
					/* TODO: Catch only the exception thrown in case of wrong password */
					//e.printStackTrace();
				}
				PasswordBox pb =
					new PasswordBox(
					MixConfig.getMainWindow(),
					"Enter the password",
					PasswordBox.ENTER_PASSWORD, null);
				pb.show();
				passwd = pb.getPassword();
			}
		}
		enableButtons();
		fireStateChanged();
	}

	/** Sets the public certificate. If the stored certificate is PKCS12, the new
	 * certificate is set as the PKCS12 certificate's public part.
	 * @param cert The new public certificate.
	 * @throws IOException If an error occurs while converting the certificate
	 */
	public void setPubCert(byte[] cert) throws IOException
	{
		if (cert == null)
		{
			this.m_cert = null;
			this.clearCertInfo();
		}
		else
		{
			if (this.m_certIsPKCS12)
			{
				( (PKCS12) m_cert).setX509Certificate(JAPCertificate.getInstance(cert));

			}
			else
			{
				JAPCertificate x509cs = JAPCertificate.getInstance(cert);
				setCertInfo(x509cs);

				m_cert = x509cs;
			}
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
	protected void fireStateChanged()
	{
		ChangeEvent e = new ChangeEvent(this);
		for (int i = 0; i < m_changeListeners.size(); i++)
		{
			( (ChangeListener) m_changeListeners.elementAt(i)).stateChanged(e);
		}
	}

	/** Sets the PKCS12 certificate and the password.
	 * @param cert The new PKCS12 certificate.
	 * @param passwd The new password for the certificate.
	 * @throws InvalidKeyException If an error occurs during key generation
	 * @throws IOException If an error occurs while converting the certificate
	 */
	private void setPrivCert(byte[] cert, char[] passwd) throws InvalidKeyException, IOException
	{
		if (cert[0] != (DERInputStream.SEQUENCE | DERInputStream.CONSTRUCTED))
		{
			throw new RuntimeException("Not a PKCS 12 stream.");
		}
		PKCS12 pkcs12 = PKCS12.getInstance(cert, passwd);
		setPrivCert(pkcs12, new String(passwd));
	}

	/** Sets the PKCS12 certificate and the password.
	 * @param pkcs12 The new PKCS12 certificate.
	 * @param strPrivCertPasswd The new password for the certificate.
	 */
	private void setPrivCert(PKCS12 pkcs12, String strPrivCertPasswd)
	{
		JAPCertificate x509cs = pkcs12.getX509Certificate();
		setCertInfo(x509cs);
		m_cert = pkcs12;
		m_privCertPasswd = strPrivCertPasswd;
		if (m_privCertPasswd.equals(STRING_ZERO)) //change a passwd which has len=1 and char[0]=0
		{
			//to a passwd with len=0 [because both seam to be 'empty'
			//passwds but are not equal!]
			m_privCertPasswd = new String();
		}
		enableButtons();
		fireStateChanged();
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
			m_bttnCreateCert.setEnabled(enabled && !cert);
		}
		if (m_bttnImportPub != null)
		{
			m_bttnImportPub.setEnabled(enabled);
		}
		if (m_bttnExportPub != null)
		{
			m_bttnExportPub.setEnabled(enabled && cert);
		}
		if (m_bttnRemoveCert != null)
		{
			m_bttnRemoveCert.setEnabled(enabled && cert);
		}
		if (m_bttnChangePasswd != null)
		{
			m_bttnChangePasswd.setEnabled(enabled && cert);
		}
	}

	/** Imports an X.509 public certificate. The user is prompted to give the name of a
	 * file from which to import. If that fails, the user is prompted to paste a
	 * certificate from the system clipboard.
	 * @throws IOException If an error occurs while reading the certificate.
	 */
	private void importPubCert() throws IOException
	{
		byte[] cert;
		try
		{
			cert = MixConfig.openFile(MixConfig.FILTER_CER | MixConfig.FILTER_B64_CER);
		}
		catch (RuntimeException e)
		{
			ClipFrame Open = new ClipFrame("Paste a certificate to be imported in " +
										   "the area provided.", true);
			Open.show();
			cert = Open.getText().getBytes();
		}
		setPubCert(cert);
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
			m_bttnImportPub.setEnabled(false);
			return;
		}

		if (buff == null)
		{
			throw new IOException("Could not read certificate.");
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
					setPrivCert(pkcs12, m_privCertPasswd);
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
			setPrivCert(buff);
		}
	}

	/** Prompts the user to enter a new password for the PKCS12 certificate. */
	private void changePasswd()
	{
		PasswordBox dialog =
			new PasswordBox(
			MixConfig.getMainWindow(),
			"Change Password",
			PasswordBox.CHANGE_PASSWORD, null);
		while (true)
		{
			dialog.show();
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
			catch (SecurityException e)
			{
				javax.swing.JOptionPane.showMessageDialog(
					this,
					e.getMessage(),
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
		if (!m_validator.isValid())
		{
			throw new RuntimeException(m_validator.getInvalidityMessage());
		}

		final ValidityDialog vdialog =
			new ValidityDialog(MixConfig.getMainWindow(), "Validity");

		vdialog.show();

		if (vdialog.from == null)
		{
			return;
		}

		PasswordBox dialog = new PasswordBox(MixConfig.getMainWindow(),
											 "New Password",
											 PasswordBox.NEW_PASSWORD,
											 m_validator.getPasswordInfoMessage());
		dialog.show();
		final char[] passwd = dialog.getPassword();
		if (passwd == null)
		{
			return;
		}

		KeyPairGenerator worker = new KeyPairGenerator(
			m_validator.getSigName(),
			vdialog.from.getDate(),
			vdialog.to.getDate(),
			passwd);

		worker.addChangeListener(this);
		worker.start();
	}

	/** Exports the certificate to a file.
	 * @throws IOException If an error occurs while writing the file.
	 */
	private void exportCert() throws IOException
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
					}
					file = new File(file.getParent(), fname + extensions[ext]);
				}

				FileOutputStream fout = new FileOutputStream(file);
				switch (type)
				{
					case MixConfig.FILTER_PFX:
						fout.write(((PKCS12)m_cert).toByteArray(getPrivateCertPassword()));
						break;
					case MixConfig.FILTER_CER:
						fout.write(m_cert.getX509Certificate().toByteArray());
						break;
					case MixConfig.FILTER_B64_CER:
						fout.write(m_cert.getX509Certificate().toByteArray(true));
						break;
				}
				fout.close();
			}
			return;
		}
		catch (IOException e)
		{
			MixConfig.handleException(e);
			ClipFrame Save =
				new ClipFrame(
				"I/O error while saving, try clipboard. " +
				"Copy and Save this file in a new Location.",
				false);
			Save.setText(new String(m_cert.getX509Certificate().toByteArray(true)));
			Save.show();
		}
	}

	/** Fills the text fields in the panel with the data from the specified certificate.
	 * @param a_x509cs an X.509 certificate structure containing the required data about subject,
	 * issuer, validity etc.
	 */
	private void setCertInfo(JAPCertificate a_x509cs)
	{
		m_textCertCN.setText(a_x509cs.getSubject().toString());
		m_textCertIssuer.setText(a_x509cs.getIssuer().toString());
		m_textCertValidFrom.setText(a_x509cs.getStartDate().getDate().toString());
		m_textCertValidTo.setText(a_x509cs.getEndDate().getDate().toString());
	}

	/** Clears the text fields that display info about the current certificate. */
	private void clearCertInfo()
	{
		m_textCertCN.setText("");
		m_textCertIssuer.setText("");
		m_textCertValidFrom.setText("");
		m_textCertValidTo.setText("");
	}

	/** This interface contains methods that provide information needed for generating
	 * new certificates. Classes that use <CODE>CertPanel</CODE> must implement this
	 * interface and use the {@link #setCertCreationValidator} to set themselves as the
	 * validator for the certificate generation.
	 */
	public static interface CertCreationValidator
	{
		/** Indicates whether the prerequisites for generating the certificate are met.
		 * For example, if a certificate for the own Mix is to be created, the Mix id must
		 * be valid as it is incorporated in the certificate's subject name.
		 * @return <CODE>true</CODE> if the prerequisites are met, <CODE>false</CODE> otherwise
		 */
		public abstract boolean isValid();

		/** Returns the signer name for the new certificate.
		 * @return The signer name
		 */
		public abstract String getSigName();

		/** Returns a message to be shown in the &quot;new password&quot; dialog for the PKCS12 certificate.
		 * @return A password info message
		 */
		public abstract String getPasswordInfoMessage();

		/** Returns a message to be shown when the prerequisites of generating a new
		 * certificate are not met.
		 * @return A warning about the prerequisites
		 */
		public abstract String getInvalidityMessage();
	}

	/** A subclass of <CODE>SwingWorker</CODE> that starts a new thread that generates the new
	 * certificate in the background.
	 */
	protected class KeyPairGenerator extends SwingWorker
	{
		/** The start date of the certificate's validity. */
		private Date m_startDate;

		/** The expiry date of the certificate. */
		private Date m_endDate;

		/** The password for the certificate to be generated. */
		private char[] m_passwd;

		/** A dialog to be shown as long as the certificate generation thread is busy. */
		private BusyWindow m_notification;

		/** The signer name for the certificate */
		private String m_name;

		/** A list of <CODE>ChangeListener</CODE>s that receive <CODE>ChangeEvent</CODE>s
		 * from this object.
		 */
		private Vector m_changeListeners = new Vector();

		/** The newly generated certificate. */
		private byte[] m_cert;

		/** Constructs a new instance of <CODE>KeyPairGenerator</CODE>
		 * @param a_name The signer name
		 * @param a_start The start date of the certificate's validity
		 * @param a_end The certificate's expiry date
		 * @param a_passwd The password for the certificate
		 */
		public KeyPairGenerator(String a_name, Date a_start, Date a_end, char[] a_passwd)
		{
			m_name = a_name;
			m_startDate = a_start;
			m_endDate = a_end;
			m_passwd = a_passwd;
			m_notification = new BusyWindow(MixConfig.getMainWindow(),
											"Generating Key Pair.");
			m_notification.setSwingWorker(KeyPairGenerator.this);
		}

		/** Adds the specified <CODE>ChangeListener</CODE> to this object's listeners list.
		 * @param a_cl A new <CODE>ChangeListener</CODE>
		 */
		public void addChangeListener(ChangeListener a_cl)
		{
			m_changeListeners.addElement(a_cl);
		}

		/** Retrieves the newly generated certificate.
		 * @return The new certificate
		 */
		public byte[] getCertificate()
		{
			return m_cert;
		}

		/** Retrieves the password of the newly generated certificate.
		 * @return The new password
		 */
		public char[] getPassword()
		{
			return m_passwd;
		}

		/** Generates the new certificate. This method is used internally and should not
		 * be called directly.
		 * @return The generated certificate.
		 */
		public Object construct()
		{
			DSAKeyPair keyPair;
			Calendar startDate = Calendar.getInstance();
			Calendar endDate = Calendar.getInstance();

			try
			{
				keyPair = DSAKeyPair.getInstance(new SecureRandom(), 1024, 80);
				startDate.setTime(m_startDate);
				endDate.setTime(m_endDate);

				return new PKCS12(m_name, keyPair, startDate, endDate).toByteArray(m_passwd);
			}
			catch (Exception e)
			{
				if (!Thread.interrupted())
				{
					MixConfig.handleException(e);
				}
			}
			return null;
		}

		/** Called internally when the certificate generation thread finishes. This method
		 * should not be called directly.
		 */
		public void finished()
		{
			m_cert = (byte[]) get();
			if (m_cert != null)
			{
				fireStateChanged();
			}
			m_notification.dispose();
		}

		/** Sends a <CODE>ChangeEvent</CODE> to all <CODE>ChangeListener</CODE>s of this
		 * object. This method is called when the process of certificate generation is
		 * aborted or complete.
		 */
		protected void fireStateChanged()
		{
			Object cl;
			Enumeration e = m_changeListeners.elements();
			for (cl = e.nextElement(); e.hasMoreElements(); cl = e.nextElement())
			{
				;
			}
			{
				( (ChangeListener) cl).stateChanged(new ChangeEvent(this));
			}
		}
	}
}
