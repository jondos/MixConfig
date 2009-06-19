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
package mixconfig.panels;

import gui.CertDetailsDialog;
import gui.ClipFrame;
import gui.GUIUtils;
import gui.dialog.DialogContentPane;
import gui.dialog.DialogContentPaneOptions;
import gui.dialog.FinishedContentPane;
import gui.dialog.JAPDialog;
import gui.dialog.PasswordContentPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import mixconfig.ICertCreationValidator;
import mixconfig.ICertificateView;
import mixconfig.MixConfig;
import mixconfig.SimpleFileFilter;
import mixconfig.tools.CertificateGenerator;
import mixconfig.wizard.CannotContinueException;
import anon.crypto.CertificateInfoStructure;
import anon.crypto.CertificateStore;
import anon.crypto.ICertificate;
import anon.crypto.IMyPublicKey;
import anon.crypto.JAPCertificate;
import anon.crypto.MyDSAPublicKey;
import anon.crypto.MyRSAPublicKey;
import anon.crypto.PKCS10CertificationRequest;
import anon.crypto.PKCS12;
import anon.crypto.SignatureVerifier;
import anon.util.IMiscPasswordReader;
import anon.util.JAPMessages;

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
	public static final int CERT_ALGORITHM_RSA = 1;
	public static final int CERT_ALGORITHM_DSA = 2;
	public static final int CERT_ALGORITHM_BOTH = 3;

	/** A <code>String</code> that contains a single char with value 0 */
	private static final String STRING_ZERO = new String(new char[]{0});
	
	// The trusted CSs against those all certificates are tested
	private static CertificateStore m_trustedCertificates;
	
	// The type of this certificate (see JAPCertificate)
	private int m_certType = 0;
	
	// Paths to certificates
	private static final String CERTPATH = "certificates/";
	public static final String CERTPATH_MIX = CERTPATH + "acceptedMixCAs/";
	public static final String CERTPATH_PAYMENT = CERTPATH + "acceptedPaymentInstances/";
	public static final String CERTPATH_TERMS = CERTPATH + "acceptedTaCTemplates/";
	public static final String CERTPATH_INFOSERVICES = CERTPATH + "acceptedInfoServiceCAs/";
	
	static {
		// Add default certificates to the certificate store
		addDefaultCertificates(CERTPATH_MIX, JAPCertificate.CERTIFICATE_TYPE_MIX); /** TODO should be root mix instead */
		addDefaultCertificates(CERTPATH_MIX, JAPCertificate.CERTIFICATE_TYPE_ROOT_MIX); // TODO use this instead of above line!
		addDefaultCertificates(CERTPATH_PAYMENT, JAPCertificate.CERTIFICATE_TYPE_PAYMENT);
		addDefaultCertificates(CERTPATH_TERMS, JAPCertificate.CERTIFICATE_TYPE_TERMS_AND_CONDITIONS);
		addDefaultCertificates(CERTPATH_INFOSERVICES, JAPCertificate.CERTIFICATE_TYPE_ROOT_INFOSERVICE);
		// needed for applet version (no dynamic loading is possible)
		addDefaultCertificates(CERTPATH_MIX + "gpf_jondonym_ca.cer", JAPCertificate.CERTIFICATE_TYPE_MIX);
		addDefaultCertificates(CERTPATH_MIX + "japmixroot.cer", JAPCertificate.CERTIFICATE_TYPE_MIX);
		addDefaultCertificates(CERTPATH_MIX + "Operator_CA.cer", JAPCertificate.CERTIFICATE_TYPE_MIX);
		addDefaultCertificates(CERTPATH_PAYMENT + "Payment_Instance.cer", JAPCertificate.CERTIFICATE_TYPE_PAYMENT);	
		addDefaultCertificates(CERTPATH_TERMS + "Terms_and_Conditions.b64.cer", JAPCertificate.CERTIFICATE_TYPE_TERMS_AND_CONDITIONS);
		
		// TODO use these instead of above lines!
		addDefaultCertificates(CERTPATH_MIX + "gpf_jondonym_ca.cer", JAPCertificate.CERTIFICATE_TYPE_ROOT_MIX);
		addDefaultCertificates(CERTPATH_MIX + "japmixroot.cer", JAPCertificate.CERTIFICATE_TYPE_ROOT_MIX);
		addDefaultCertificates(CERTPATH_MIX + "Operator_CA.cer", JAPCertificate.CERTIFICATE_TYPE_ROOT_MIX);
		
		addDefaultCertificates(CERTPATH_INFOSERVICES + "InfoService_CA.cer", JAPCertificate.CERTIFICATE_TYPE_ROOT_INFOSERVICE);
		addDefaultCertificates(CERTPATH_INFOSERVICES + "japinfoserviceroot.cer", JAPCertificate.CERTIFICATE_TYPE_ROOT_INFOSERVICE);
		
		
	}

	private static final String CERT_VALID = "cert.gif";
	private static final String CERT_INVALID = "certinvalid.gif";
	private static final String CERT_VALID_INACTIVE = "certinactive.gif";
	private static final String CERT_INVALID_INACTIVE = "certinvalidinactive.gif";
	private static final String CERT_DISABLED = "certdisabled.gif";

	private static final String MSG_MANDATORY_ALGO = CertPanel.class.getName() + "_mandatoryAlgorithm";
	private static final String MSG_CERT_TYPE_UNKNOWN = CertPanel.class.getName() + "_certTypeUnknown";
	private static final String MSG_CONFIRM_OVERWRITE = CertPanel.class.getName() + "_confirmOverwriting";
	private static final String MSG_CONFIRM_DELETION = CertPanel.class.getName() + "_confirmDeletion";
	private static final String MSG_CHOOSE_LOAD_METHOD = CertPanel.class.getName() + "_chooseLoadMethod";
	private static final String MSG_CHOOSE_SAVE_METHOD = CertPanel.class.getName() + "_chooseSaveMethod";
	private static final String MSG_CHOOSE_CERT_TYPE = CertPanel.class.getName() + "_chooseCertType";
	private static final String MSG_NO_PRIVATE_CERT = CertPanel.class.getName() + "_noPrivateCert";

	// Holds a Vector with all instanciated CertPanels
	private static Vector<CertPanel> ms_certpanels = new Vector<CertPanel>();

	private static boolean m_autoSign = false;

	private String m_strChangedCertNotVerifyable;
	private JAPDialog.ILinkedInformation m_linkedInformation;

	/** The 'create new certificate' button */
	private JButton m_bttnCreateCert;

	/** The 'import certificate' button */
	private JButton m_bttnImportCert;

	/** The 'export certificate' button */
	private JButton m_bttnExportCert;

	/** The 'sign certificate' button */
	private JButton m_bttnSignCert;
	
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

	/** TextPane for showing the SubjectKeyIdentifier */
	private JTextPane m_lblSubjectKeyIdentifier;

	/** Indicates whether the certificate object is PKCS12 (<CODE>true</CODE>) or X.509 (<CODE>false</CODE>) */
	private boolean m_bCertIsPKCS12 = false;

	private int m_certAlgorithm;

	private boolean m_bCreateDSACerts;
	private int m_nKeySize=1024;
	private boolean m_bCertificateSaved = true;

	/** The certificate */
	private ICertificate m_cert;

	/** An additional certificate for the verification */
	private ICertificate m_additionalVerifier;

	/** The password for the private certificate (<CODE>null</CODE> if the certificate
	 * is an X.509 public certificate) */
	private String m_privCertPasswd;

	/** The name of this cert that is shown in the titled border */
	private String m_name;
	
	/** The validator for newly generated certificates.
	 * @see mixconfig.CertPanel.CertCreationValidator
	 */
	private ICertCreationValidator m_validator;

	/**
	 * Optional component, may be null; stores the current view of the stored certificate.
	 */
	private ICertificateView m_certView;

	/** The list of objects that listen to <CODE>ChangeEvent</CODE>s from this object */
	private Vector<ChangeListener> m_changeListeners = new Vector<ChangeListener>();

	/**
	 * Constructs a new instance of <CODE>CertPanel</CODE> with the specified name, the
	 * specified tool tip and the specified X.509 certificate.
	 * @param a_name A name that will be displayed above the panel.
	 * @param a_toolTip A text that will be displayed as a tool tip when the user moves the
	 * mouse over the panel.
	 * @param a_certificate the certificate (X.509)
	 * @param a_certAlgorithm the certificate algorithm that is supported by this panel
	 * @param a_certType the certificate type that is used for verification
	 */
	public CertPanel(String a_name, String a_toolTip, JAPCertificate a_certificate, 
			int a_certAlgorithm, int a_certType)
	{
		this(a_name, a_toolTip, (ICertificate) a_certificate, false, a_certAlgorithm, a_certType,1024);
	}

	/**
	 * Constructs a new instance of <CODE>CertPanel</CODE> with the specified name, the
	 * specified tool tip and the specified PKCS12 certificate.
	 * @param a_name A name that will be displayed above the panel.
	 * @param a_toolTip A text that will be displayed as a tool tip when the user moves the
	 * mouse over the panel.
	 * @param a_certificate the certificate (PKCS12)
	 * @param a_certAlgorithm the certificate algorithm that is supported by this panel
	 */
	public CertPanel(String a_name, String a_toolTip, PKCS12 a_certificate, 
			int a_certAlgorithm, int a_certType)
	{
		this(a_name, a_toolTip, (ICertificate) a_certificate, true, a_certAlgorithm, a_certType,1024);
	}
	
	/**
	 * Constructs a new instance of <CODE>CertPanel</CODE> with the specified name, the
	 * specified tool tip and the specified PKCS12 certificate. The keysize is used, if a new certificate is created.
	 * @param a_name A name that will be displayed above the panel.
	 * @param a_toolTip A text that will be displayed as a tool tip when the user moves the
	 * mouse over the panel.
	 * @param a_certificate the certificate (PKCS12)
	 * @param a_certAlgorithm the certificate algorithm that is supported by this panel
	 * @param a_certType ?
	 * @param a_keysize keysize (in bits) used if a new key is created
	 */
	public CertPanel(String a_name, String a_toolTip, PKCS12 a_certificate, 
			int a_certAlgorithm, int a_certType, int keysize)
	{
		this(a_name, a_toolTip, (ICertificate) a_certificate, true, a_certAlgorithm, a_certType,keysize);
	}

	/**
	 * Constructs a new instance of <CODE>CertPanel</CODE> with the specified name, the
	 * specified tool tip and the specified certificate.
	 * @param a_name A name that will be displayed above the panel.
	 * @param a_toolTip A text that will be displayed as a tool tip when the user moves the
	 * mouse over the panel.
	 * @param a_certificate the certificate (PKCS12 or X.509)
	 * @param a_pkcs12 true if the certificate is PKCS12; false otherwise
	 * @param a_certAlgorithm the certificate algorithm that is supported by this panel
	 */
	private CertPanel(String a_name, String a_toolTip, ICertificate a_certificate, boolean a_pkcs12,
					  int a_certAlgorithm, int a_certType,int keysize)
	{
		// Add this CertPanel to the vector
		ms_certpanels.addElement(this);
		m_bCreateDSACerts = true;
        // Set the algorithm
		if (a_certAlgorithm == CERT_ALGORITHM_DSA)
		{
			m_certAlgorithm = CERT_ALGORITHM_DSA;
		}
		else if (a_certAlgorithm == CERT_ALGORITHM_RSA)
		{
			m_certAlgorithm = CERT_ALGORITHM_RSA;
			m_bCreateDSACerts = false;
		}
		else
		{
			m_certAlgorithm = CERT_ALGORITHM_BOTH;
		}
		// Is this a private certificate
		m_bCertIsPKCS12 = a_pkcs12;
		// Set the certificate type for validating
		m_certType = a_certType;
		// Set the name
		m_name = a_name;
		//Set the keySize
		m_nKeySize=keysize;
		
		//LogHolder.log(LogLevel.DEBUG, LogType.CRYPTO, "Creating a CertPanel with CertType " + m_certType);
		
		// Create GUI
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.insets = new Insets(5, 5, 5, 5);

		setBorder(new TitledBorder(m_name));
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

		if (m_bCertIsPKCS12)
		{
			m_bttnCreateCert = new JButton("Create");
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			m_bttnCreateCert.addActionListener(this);
			layout.setConstraints(m_bttnCreateCert, constraints);
			add(m_bttnCreateCert);
			
			// XXX: HACK: Add the 'Sign'-button only if this is the 'Own Mix Certificate'-panel
			if (a_name.equals("Own Mix Certificate"))
			{
				m_bttnSignCert = new JButton("Sign");
				constraints.gridx = 1;
				m_bttnSignCert.addActionListener(this);
				add(m_bttnSignCert, constraints);

				m_bttnChangePasswd = new JButton("Password");
				constraints.gridx = 2;
				m_bttnChangePasswd.addActionListener(this);
				add(m_bttnChangePasswd, constraints);
				constraints.fill = GridBagConstraints.NONE;				
			}
			else
			{
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
		}

		m_certLabel = new JLabel(GUIUtils.loadImageIcon(CERT_DISABLED));
		m_certLabel.setBorder(null);
		m_certLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		m_certLabel.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent a_event)
			{
				if (m_certLabel.isEnabled())
				{
					CertDetailsDialog dialog = new CertDetailsDialog(getParent(), m_cert.getX509Certificate(),
						isCertificateVerifyable(), null);
					dialog.pack();
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

			//public void mouseExited(MouseEvent a_event)
			//{
			//	if (m_certLabel.isEnabled())
			//	{
			//		updateCertificateIcon(false);
			//	}
			//}

		});
		constraints.gridy = 2;
		constraints.gridheight = 3;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		add(m_certLabel, constraints);

		constraints.gridheight = 2;
		constraints.gridwidth = 3;
		constraints.gridx++;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(0, 5, 0, 0);

		m_lblSubjectKeyIdentifier = GUIUtils.createSelectableAndResizeableLabel(this);
		m_lblSubjectKeyIdentifier.setFont(new Font(m_lblSubjectKeyIdentifier.getFont().getName(),Font.BOLD, 10));
		m_lblSubjectKeyIdentifier.setText("00 00 00 00 00 00 00 00 00 00 00 ");
		m_lblSubjectKeyIdentifier.setPreferredSize(new Dimension(m_lblSubjectKeyIdentifier.getPreferredSize().width,
									   (int)(2.5 * m_lblSubjectKeyIdentifier.getFontMetrics(
		m_lblSubjectKeyIdentifier.getFont()).getHeight())));
	    m_lblSubjectKeyIdentifier.setText("");
		m_lblSubjectKeyIdentifier.setToolTipText("SubjectKeyIdentifier");
		add(m_lblSubjectKeyIdentifier, constraints);

		constraints.insets = new Insets(0, 5, 5, 0);

		// add dummy label
		constraints.gridx = 1;
		constraints.gridy++;
		constraints.gridheight = 1;
		constraints.gridwidth = 5;
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.VERTICAL;
		add(new JLabel(), constraints);
		
		// add validity label
		constraints.weighty = 0;
		constraints.gridy++;
		m_textCertValidity = new JLabel();
		m_textCertValidity.setToolTipText("Validity");
		m_textCertValidity.setPreferredSize(new JLabel("00.00.0000 - 00.00.0000").getPreferredSize());
		add(m_textCertValidity, constraints);
		constraints.weightx = 0;

		enableButtons();

		if (a_certificate != null)
		{
			setCert(a_certificate.toByteArray());
		}
	}

	/**
	 * Read all certificates from a specified path
	 * 
	 * @param sPath
	 * @param iType
	 */
	private static void addDefaultCertificates(String sPath, int iType)
	{
		// Create CertificateStore if it is not yet there
		if (m_trustedCertificates == null) 
		{
			m_trustedCertificates = new CertificateStore();
		}
	 	// Load certificates into a CertificateStore
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Loading certificates of type '"+iType+"' from '"+sPath+"'");		
		Enumeration certificates = JAPCertificate.getInstance(sPath, true).elements();
		JAPCertificate cert = null;
		while (certificates.hasMoreElements())
		{
			cert = (JAPCertificate) certificates.nextElement();
			m_trustedCertificates.addCertificateWithoutVerification(cert, iType, true, true);
			SignatureVerifier.getInstance().getVerificationCertificateStore().addCertificateWithoutVerification(cert, iType, true, true);
		}
		
		// No certificates found
		if (cert == null)
		{
			LogHolder.log(LogLevel.WARNING, LogType.MISC, "No certificates of type '"+iType+"'");
		}
	}
	
	/** Set the validator for the creation of new certificates
	 * @param a_cg the new validator
	 * @see #CertCreationValidator
	 */
	public void setCertCreationValidator(ICertCreationValidator a_cg)
	{
		this.m_validator = a_cg;
		enableButtons();
	}

	public boolean isCertificateSaved()
	{
		return getCert() == null || !m_bCertIsPKCS12 || m_bCertificateSaved;
	}

	/**
	 * Return TRUE if the certificate can be verified against the trusted certificates
	 * @return boolean
	 */
	public boolean isCertificateVerifyable()
	{
		LogHolder.log(LogLevel.DEBUG, LogType.CRYPTO, "Verifying certificate: " +
	       this.m_cert.getX509Certificate().getSubjectKeyIdentifier());
		// Create a vector of JAPCertificates with type m_certType from the CertificateStore
		Enumeration certInfoEnum = m_trustedCertificates.getAvailableCertificatesByType(m_certType).elements();
		Vector<JAPCertificate> certs = new Vector<JAPCertificate>();
		while (certInfoEnum.hasMoreElements())
		{
			certs.add(((CertificateInfoStructure)certInfoEnum.nextElement()).getCertificate());
			//LogHolder.log(LogLevel.DEBUG, LogType.CRYPTO, "Added certificate of type " + m_certType);
		}
		// Check if there is an additional verifier
		if(m_additionalVerifier != null)
		{
			if(m_cert.getX509Certificate().verify(m_additionalVerifier.getPublicKey()))
			{
				// If the additional verifier verified the cert, try to verify the additional verifier
				//return m_additionalVerifier.getX509Certificate().verify(TRUSTED_CERTIFICATES.elements());
				return m_additionalVerifier.getX509Certificate().verify(certs.elements());
			}
		}
		//return m_cert.getX509Certificate().verify(TRUSTED_CERTIFICATES.elements());		
		return m_cert.getX509Certificate().verify(certs.elements());
	}

	/**
	 * Sets the message that is displayed when the user creates or imports an certificate that cannot be
	 * verified against one of the trusted certificates.
	 * @param a_strChangedCertNotVerifyable String
	 * @param a_linkedInformation an additional information, for example a help context, that is linked
	 * in the displayed message
	 */
	public void setChangedCertNotVerifyableMessage(String a_strChangedCertNotVerifyable,
		JAPDialog.ILinkedInformation a_linkedInformation)
	{
		m_strChangedCertNotVerifyable = a_strChangedCertNotVerifyable;
		m_linkedInformation = a_linkedInformation;
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

	public static boolean isAutoSign()
	{
		return m_autoSign;
	}

	public static void setAutoSign(boolean b_autoSign)
	{
		m_autoSign = b_autoSign;
	}

	public void actionPerformed(ActionEvent a_event)
	{
		Object source = a_event.getSource();
		try
		{
			boolean bCertChanged = false;

			if (source == this.m_bttnChangePasswd)
			{
				changePasswd();
			}
			else if (source == this.m_bttnCreateCert)
			{
				bCertChanged = generateNewCert();
			}
			else if (source == this.m_bttnExportCert)
			{
				exportCert();
			}
			else if (source == this.m_bttnSignCert)
			{
				signCert();
			}			
			else if (source == this.m_bttnRemoveCert)
			{
				if (JAPDialog.showConfirmDialog(this, JAPMessages.getString(MSG_CONFIRM_DELETION),
					JAPDialog.OPTION_TYPE_OK_CANCEL, JAPDialog.MESSAGE_TYPE_QUESTION) == JAPDialog.RETURN_VALUE_OK)
				{
					removeCert();
				}
			}
			else if (source == this.m_bttnImportCert)
			{

				if (m_bCertIsPKCS12)
				{
					bCertChanged = importPrivCert();
				}
				else
				{
					bCertChanged = importPubCert();
				}
			}
			enableButtons();
            // Show a warning if certificate changed and it is not verifyable
			if (bCertChanged && !isCertificateVerifyable() && m_strChangedCertNotVerifyable != null)
			{
				JAPDialog.showMessageDialog(this, m_strChangedCertNotVerifyable,
				             GUIUtils.loadImageIcon(CERT_INVALID), m_linkedInformation);
			}
		}
		catch (Exception ex)
		{
			JAPDialog.showErrorDialog(this, LogType.GUI, ex);
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
	 * @return true if the certificate has been changed; false otherwise
	 * @throws IOException If an error occurs while converting the certificate
	 * @throws IllegalArgumentException If the certificate is not of the required type
	 */
	public boolean setCert(byte[] cert) throws IllegalArgumentException
	{
		JAPCertificate x509cert = JAPCertificate.getInstance(cert);
		boolean bChanged;

		if (cert == null)
		{
			bChanged = m_cert != null;
			m_cert = null;
			clearCertInfo();
		}
		else if (m_bCertIsPKCS12)
		{
			if (x509cert != null)
			{
				if (m_cert == null)
				{
					throw new IllegalArgumentException(JAPMessages.getString(MSG_NO_PRIVATE_CERT));
				}

				bChanged = ( (PKCS12) m_cert).setX509Certificate(x509cert);
			}
			else
			{
				JAPDialog dialog =
					new JAPDialog(GUIUtils.getParentWindow(this), "Enter the certificate password", true);
				dialog.setResizable(false);
				dialog.setDefaultCloseOperation(JAPDialog.HIDE_ON_CLOSE);
				PasswordContentPane pb = new PasswordContentPane(dialog,
					PasswordContentPane.PASSWORD_ENTER, "Please enter your certificate password.");
				pb.setDefaultButtonOperation(PasswordContentPane.ON_CLICK_HIDE_DIALOG);
				pb.updateDialog();
				dialog.pack();
				CertPanelPasswordReader pwReader = new CertPanelPasswordReader(pb);
				PKCS12 privateCertificate = null;

				while (privateCertificate == null)
				{
					privateCertificate = PKCS12.getInstance(cert, pwReader);
					if (privateCertificate == null && !pb.hasValidValue())
					{
						if (pb.getButtonValue() == JAPDialog.RETURN_VALUE_UNINITIALIZED)
						{
							JAPDialog.showErrorDialog(GUIUtils.getParentWindow(this),
								"Your private certificate was not loaded for some unknown reason! " +
								"It might be damaged.",
								LogType.CRYPTO);
							break;
						}
						else
						{
							int returnValue =
								JAPDialog.showConfirmDialog(GUIUtils.getParentWindow(this),
								"Are you sure you want to cancel? " +
								"Your private certificate will not be loaded!",
								"Certificate not loaded",
								JAPDialog.OPTION_TYPE_OK_CANCEL, JAPDialog.MESSAGE_TYPE_WARNING);
							if (returnValue == JAPDialog.RETURN_VALUE_OK)
							{
								break;
							}
						}
					}
				}
				dialog.dispose();
				bChanged = setCertificate(privateCertificate, pwReader.getPassword());
			}
		}
		else if (x509cert != null)
		{
			//System.out.println(((anon.crypto.X509SubjectKeyIdentifier)x509cert.getExtensions().getExtension(anon.crypto.X509SubjectKeyIdentifier.IDENTIFIER)).getValue());
			checkPublicKeyAlgorithm(x509cert.getPublicKey());
			setCertInfo(x509cert);
			m_cert = x509cert;
			bChanged = true;
		}
		else
		{
			throw new IllegalArgumentException(JAPMessages.getString(MSG_CERT_TYPE_UNKNOWN));
		}
		enableButtons();
		//deactivate auto-signing of OwnCertificate with OperatorCertificate while loading from file
		CertPanel.setAutoSign(false);
		this.fireStateChanged();
		//activate auto-signing again
		CertPanel.setAutoSign(true);

		return bChanged;
	}

	/** Sets the addional cert for verification */
	public void setAdditionalVerifier(ICertificate a_cert)
	{
		m_additionalVerifier = a_cert;
	}

	/** Removes the certficate from the panel */
	public void removeCert()
	{
		this.m_cert = null;
		this.clearCertInfo();
		this.fireStateChanged();
	}

	/** Adds a <CODE>ChangeListener</CODE> to this object's listeners list.
	 * @param a_cl A new <CODE>ChangeListener</CODE>
	 */
	public void addChangeListener(ChangeListener a_cl)
	{
		m_changeListeners.addElement(a_cl);
	}

	/**
	 * Checks if this panel accepts a public key of this type and throws an IllegalArgumentException if not.
	 * @param a_publicKey a public key
	 * @throws IllegalArgumentException if this panel does not accept a public key of the given type
	 */
	private void checkPublicKeyAlgorithm(IMyPublicKey a_publicKey)
		throws IllegalArgumentException
	{
		if (m_certAlgorithm == CERT_ALGORITHM_BOTH ||
			(m_certAlgorithm == CERT_ALGORITHM_DSA && a_publicKey instanceof MyDSAPublicKey) ||
			(m_certAlgorithm == CERT_ALGORITHM_RSA && a_publicKey instanceof MyRSAPublicKey))
		{
			return;
		}

		if (m_certAlgorithm == CERT_ALGORITHM_DSA)
		{
			throw new IllegalArgumentException(JAPMessages.getString(MSG_MANDATORY_ALGO, "DSA"));
		}
		else
		{
			throw new IllegalArgumentException(JAPMessages.getString(MSG_MANDATORY_ALGO, "RSA"));
		}
	}

	/** Sends a <CODE>ChangeEvent</CODE> to all of this object's <CODE>ChangeListener</CODE>s */
	private void fireStateChanged()
	{
		if (m_certView != null)
		{
			m_certView.update(getCert());
		}
		validate();
		ChangeEvent event = new ChangeEvent(this);
		for (int i = 0; i < m_changeListeners.size(); i++)
		{
			( (ChangeListener) m_changeListeners.elementAt(i)).stateChanged(event);
		}
	}

	/** Sets the PKCS12 certificate and the password.
	 * @param pkcs12 The new PKCS12 certificate.
	 * @param strPrivCertPasswd The new password for the certificate.
	 * @return true if the certificate has changed; false otherwise
	 * @throws IllegalArgumentException if this panel does not accept a public key of the given type
	 */
	private boolean setCertificate(PKCS12 pkcs12, char[] strPrivCertPasswd)
		throws IllegalArgumentException
	{
		if (pkcs12 == null)
		{
			return false;
		}

		JAPCertificate x509cs = pkcs12.getX509Certificate();

		checkPublicKeyAlgorithm(x509cs.getPublicKey());

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
			//to a passwd with len=0 [because both seem to be 'empty' passwds but are not equal!]
			m_privCertPasswd = new String();
		}
		m_bCertificateSaved = true;
		enableButtons();
		fireStateChanged();
		return true;
	}

	public void updateCertificateIcon(boolean a_bActive)
	{
		if (m_cert != null)
		{
			boolean bValid = isCertificateVerifyable();

			if (a_bActive)
			{
				if (bValid)
				{
					m_certLabel.setIcon(GUIUtils.loadImageIcon(CERT_VALID));
				}
				else
				{
					m_certLabel.setIcon(GUIUtils.loadImageIcon(CERT_INVALID));
				}
			}
			else
			{
				if (bValid)
				{
					m_certLabel.setIcon(GUIUtils.loadImageIcon(CERT_VALID_INACTIVE));
				}
				else
				{
					m_certLabel.setIcon(GUIUtils.loadImageIcon(CERT_INVALID_INACTIVE));
				}
			}

			if (bValid)
			{
				m_certLabel.setToolTipText("");
			}
			else
			{
				m_certLabel.setToolTipText("Click to see the details...");
			}
		}
		else
		{
			m_certLabel.setIcon(GUIUtils.loadImageIcon(CERT_DISABLED));
			m_certLabel.setToolTipText("");
		}
	}

	/** 
	 * Enables/disable the buttons according to this object's state.
	 * The buttons for creating and importing certificates are enabled if the stored
	 * certificate is <CODE>null</CODE>. Otherwise, the buttons for exporting and 
	 * removing the certificate and for changing the password are enabled.
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
		if (m_bttnSignCert != null)
		{
			boolean opCert = false;
			// Check again if the parent is of the right class
			if (this.getParent() instanceof OwnCertificatesPanel)
			{
				opCert = ((OwnCertificatesPanel)this.getParent()).hasOperatorCert();
			}
			m_bttnSignCert.setEnabled(enabled && cert && opCert);
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
	 * @return true if the certificate has changed; false otherwise
	 * @throws IOException If an error occurs while reading the certificate.
	 */
	private boolean importPubCert() throws IOException
	{
		byte[] cert = null;
		JAPDialog dialog = new JAPDialog(this, "Import certificate");
		ChooseCertStorageMethodPane pane =
			new ChooseCertStorageMethodPane(dialog, JAPMessages.getString(MSG_CHOOSE_LOAD_METHOD));
		pane.updateDialog();
		dialog.pack();
		dialog.setResizable(false);
		dialog.setVisible(true);
		if (pane.getButtonValue() != DialogContentPane.RETURN_VALUE_OK)
		{
			return false;
		}
		if (pane.isMethodFile())
		{
			try
			{
				//cert = MixConfig.openFile(this, MixConfig.FILTER_CER | MixConfig.FILTER_B64_CER);
				cert = MixConfig.openFile(this, MixConfig.FILTER_CER);
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
				return false;
			}
		}
		else
		{
			cert = GUIUtils.getTextFromClipboard(this).getBytes();
		}
		return setCert(cert);
	}

	/** Imports a PKCS12 certificate. The user is prompted to give the name of a
	 * file from which to import. If that fails, the user is prompted to paste a
	 * certificate from the system clipboard.
	 * @return true if an certificate has been imported; false otherwise
	 * @throws IOException If an error occurs while reading the certificate.
	 */
	private boolean importPrivCert() throws IOException
	{
		byte[] buff;
		PKCS12 pkcs12;

		pkcs12 = (PKCS12) m_cert;
		JAPDialog dialog = new JAPDialog(this, "Import certificate");
		ChooseCertStorageMethodPane pane =
			new ChooseCertStorageMethodPane(dialog, JAPMessages.getString(MSG_CHOOSE_LOAD_METHOD));
		pane.updateDialog();
		dialog.pack();
		dialog.setResizable(false);
		dialog.setVisible(true);
		if (pane.getButtonValue() != DialogContentPane.RETURN_VALUE_OK)
		{
			return false;
		}
		if (pane.isMethodFile())
		{
			int filter = MixConfig.FILTER_PFX;
			if (m_cert != null)
			{
				//filter |= (MixConfig.FILTER_B64_CER | MixConfig.FILTER_CER);
				filter |= (MixConfig.FILTER_CER);
			}
			try
			{
				buff = MixConfig.openFile(this, filter);
			}
			catch (SecurityException e)
			{
				/*
					 JAPDialog.showErrorDialog(
				 this,
				 "Import of a private key with certificate is not supported when running as an applet.",
				 "Not supported!",
				 LogType.GUI);
					 m_bttnImportCert.setEnabled(false);
					return false;*/
				ClipFrame open = new ClipFrame(this, "Paste a certificate to be imported in " +
											   "the area provided.", true);
				open.setVisible(true);
				buff = open.getText().getBytes();

			}
			if (buff == null)
			{
				return false;
			}
		}
		else
		{
			buff = GUIUtils.getTextFromClipboard(this).getBytes();
		}

		//if own key is already set, then maybe only an other certificate for this key is imported...
		if (pkcs12 != null)
		{
			JAPCertificate cert1 = JAPCertificate.getInstance(buff);
			if (cert1 != null)
			{
				if (pkcs12.setX509Certificate(cert1))
				{
					setCertificate(pkcs12, m_privCertPasswd.toCharArray());
					return true;
				}
				else
				{
					JAPDialog.showErrorDialog(
						this, "This public key certificate does not belong to your private key!",
						"Wrong certificate!", LogType.GUI);
					return false;
				}
			}
		}

		return setCert(buff);
	}

	/** Prompts the user to enter a new password for the PKCS12 certificate. */
	private void changePasswd()
	{
		String strMessage = null;
		if (m_validator != null)
		{
			strMessage = m_validator.getPasswordInfoMessage();
		}

		JAPDialog dialog = new JAPDialog(this, "Change password", true);
		dialog.setResizable(false);
		PasswordContentPane pb =
			new PasswordContentPane(dialog, PasswordContentPane.PASSWORD_CHANGE, strMessage)
			{
				public char[] getComparedPassword()
				{
					return m_privCertPasswd.toCharArray();
				}
			};

		pb.updateDialog();
		dialog.pack();
		dialog.setVisible(true);

		if (pb.hasValidValue())
		{
			m_privCertPasswd = new String(pb.getPassword());
			//deactivate auto-signing of OwnCertificate with OperatorCertificate while loading from file
			CertPanel.setAutoSign(false);
			fireStateChanged();
			//activate auto-signing again
			CertPanel.setAutoSign(true);
		}
	}

	/** Generates a new certificate. For this purpose, a new thread is started that
	 * works in the background.
	 * @return true if a new certificate has been created; false otherwise
	 * @throws NullPointerException If this object's certificate generation validator is <CODE>null</CODE>.
	 * @throws CannotContinueException if this object's certificate generation validator is not valid or null
	 * @see #CertCreationValidator
	 */
	private boolean generateNewCert() throws NullPointerException, CannotContinueException
	{
		if (!m_validator.isValid())
		{
			throw new CannotContinueException(m_validator.getInvalidityMessages());
		}
        // Create the dialog
		final JAPDialog dialog = new JAPDialog(this, "Create new certificate", true);
		dialog.setDefaultCloseOperation(JAPDialog.DO_NOTHING_ON_CLOSE);

		// By default, validity is chosen to one year ..
		//ValidityContentPane validityContentPane = new ValidityContentPane(dialog);		
		
		// Add a PasswordContentPane to the dialog
		PasswordContentPane passwordContentPane = new PasswordContentPane(dialog, //validityContentPane,
			PasswordContentPane.PASSWORD_NEW, m_validator.getPasswordInfoMessage());
		
		// Create the worker for certificate generation
		final CertificateGenerator.CertificateWorker worker = CertificateGenerator.createWorker(dialog, 
				passwordContentPane, m_validator.getSigName(), m_validator.getExtensions(), m_bCreateDSACerts,m_nKeySize);
		
		final FinishedContentPane finishedContentPane = new CertPanelFinishedContentPane(dialog, worker);
		
		dialog.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent a_event)
			{
				if (!worker.isVisible() &&
					(!finishedContentPane.isVisible() || finishedContentPane.checkCancel() == null))
				{
					dialog.dispose();
				}
			}
		});

		DialogContentPane.updateDialogOptimalSized(passwordContentPane);
		dialog.setResizable(false);
		dialog.setVisible(true);

		if (finishedContentPane.getButtonValue() != DialogContentPane.RETURN_VALUE_OK ||
			worker.getCertificateGenerator().getCertificate() == null)
		{
			return false;
		}
		setCertificate(worker.getCertificateGenerator().getCertificate(), passwordContentPane.getPassword());
		m_bCertificateSaved = false;

		return true;
	}

	/** Exports the certificate to a file.
	 */
	private void exportCert()
	{
		if (m_cert == null)
		{
			throw new IllegalArgumentException("No certificate to export.");
		}

		JFileChooser fd;
		FileFilter ff;
		File file = null;
		int type;

		JAPDialog dialog = new JAPDialog(this, "Export certificate");
		ChooseCertStorageMethodPane pane =
			new ChooseCertStorageMethodPane(dialog, JAPMessages.getString(MSG_CHOOSE_SAVE_METHOD));
		FileFilterSelectionPane typePane =
			new FileFilterSelectionPane(dialog, JAPMessages.getString(MSG_CHOOSE_CERT_TYPE), pane);

		DialogContentPane.updateDialogOptimalSized(pane);
		dialog.setResizable(false);
		dialog.setVisible(true);
		if (typePane.getButtonValue() != DialogContentPane.RETURN_VALUE_OK)
		{
			return;
		}

		if (pane.isMethodFile())
		{
			int filter;
			if (typePane.getSelectionType() == FileFilterSelectionPane.X509)
			{
				filter = MixConfig.FILTER_CER | MixConfig.FILTER_B64_CER;
			}
			else if (typePane.getSelectionType() == FileFilterSelectionPane.PKCS10)
			{
				filter = MixConfig.FILTER_P10 | MixConfig.FILTER_B64_P10;
			}
			else
			{
				filter = MixConfig.FILTER_PFX | MixConfig.FILTER_PFX;
			}

			do
			{
				fd = MixConfig.showFileDialog(this, MixConfig.SAVE_DIALOG, filter);
				if (fd == null)
				{
					type = 0;
					file = null;
					continue;
				}
				ff = fd.getFileFilter();
				if (ff instanceof SimpleFileFilter)
				{
					type = ( (SimpleFileFilter) ff).getFilterType();
				}
				else
				{
					if (typePane.getSelectionType() == FileFilterSelectionPane.X509)
					{
						type = MixConfig.FILTER_B64_CER;
					}
					else if (typePane.getSelectionType() == FileFilterSelectionPane.PKCS10)
					{
						type = MixConfig.FILTER_B64_P10;
					}
					else
					{
						type = MixConfig.FILTER_PFX;
					}
				}
				file = fd.getSelectedFile();
				if (file != null)
				{
					String fname = file.getName();
					if (fname.indexOf('.') < 0)
					{
						String extensions[] =
							{
							PKCS12.FILE_EXTENSION, ".b64" + PKCS12.FILE_EXTENSION, ".der.cer", ".b64.cer",
							PKCS10CertificationRequest.FILE_EXTENSION,
							".b64" + PKCS10CertificationRequest.FILE_EXTENSION};
						int ext = 0;
						// we can't use the MixConfig constants as array indices
						// because we can't rely that their values don't change
						// in future versions
						switch (type)
						{
							case MixConfig.FILTER_PFX:
								ext = 0;
								break;
							case MixConfig.FILTER_B64_PFX:
								ext = 1;
								break;
							case MixConfig.FILTER_CER:
								ext = 2;
								break;
							case MixConfig.FILTER_B64_CER:
								ext = 3;
								break;
							case MixConfig.FILTER_P10:
								ext = 4;
								break;
							case MixConfig.FILTER_B64_P10:
								ext = 5;
								break;
							default:
						}
						file = new File(file.getParent(), fname + extensions[ext]);
					}
				}
			}
			while (file != null && file.exists() &&
				   (JAPDialog.showConfirmDialog(
					   this, JAPMessages.getString(MSG_CONFIRM_OVERWRITE),
					   JAPDialog.OPTION_TYPE_OK_CANCEL,
					   JAPDialog.MESSAGE_TYPE_QUESTION) != JAPDialog.RETURN_VALUE_OK));
		}
		else
		{
			if (typePane.getSelectionType() == FileFilterSelectionPane.X509)
			{
				type = MixConfig.FILTER_B64_CER;
			}
			else if (typePane.getSelectionType() == FileFilterSelectionPane.PKCS10)
			{
				type = MixConfig.FILTER_B64_P10;
			}
			else
			{
				type = MixConfig.FILTER_B64_PFX;
			}
		}
		byte[] output = null;
		switch (type)
		{
			case MixConfig.FILTER_PFX:
				output = ( (PKCS12) m_cert).toByteArray(getPrivateCertPassword());
				m_bCertificateSaved = true;
				break;
			case MixConfig.FILTER_B64_PFX:
				output = ( (PKCS12) m_cert).toByteArray(getPrivateCertPassword(), true);
				m_bCertificateSaved = true;
				break;
			case MixConfig.FILTER_CER:
				output = m_cert.getX509Certificate().toByteArray();
				break;
			case MixConfig.FILTER_B64_CER:
				output = m_cert.getX509Certificate().toByteArray(true);
				break;
			case MixConfig.FILTER_P10:
				output = new PKCS10CertificationRequest( (PKCS12) m_cert).toByteArray(false);
			case MixConfig.FILTER_B64_P10:
				output = new PKCS10CertificationRequest( (PKCS12) m_cert).toByteArray(true);
				break;
			default:
		}

		try
		{
			if (file != null)
			{
				FileOutputStream fout = new FileOutputStream(file);
				fout.write(output);
				fout.close();
			}
			else if (!pane.isMethodFile())
			{
				GUIUtils.saveTextToClipboard(new String(output), this);
			}
			return;
		}
		catch (IOException a_e)
		{
			JAPDialog.showErrorDialog(this, "Could not export certificate", LogType.MISC, a_e);
			ClipFrame save =
				new ClipFrame(this,
							  "I/O error while saving, try clipboard. " +
							  "Copy and Save this file in a new Location.",
							  false);
			save.setText(new String(output));
			save.setVisible(true);
		}
	}

	/**
	 * Method to sign a certificate (only for OwnCertificatesPanels!)
	 */
	private void signCert()
	{
		Component parent = this.getParent();
		if (this.getParent() instanceof OwnCertificatesPanel)
		{
			((OwnCertificatesPanel)parent).signMixCertificate(true);
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
		cal.setTime(a_x509cs.getValidity().getValidFrom());
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH) + 1;
		int year = cal.get(Calendar.YEAR);
		String startDate = day + "." + month + "." + year;
		cal.setTime(a_x509cs.getValidity().getValidTo());
		day = cal.get(Calendar.DAY_OF_MONTH);
		month = cal.get(Calendar.MONTH) + 1;
		year = cal.get(Calendar.YEAR);
		String endDate = day + "." + month + "." + year;
		m_textCertValidity.setText(startDate + " - " + endDate);

		// Try to show the SubjectKeyIdentifier, use SHA-1 Fingerprint only as fallback
		String ski = a_x509cs.getSubjectKeyIdentifier();		
		if (ski == null)
		{
			ski = a_x509cs.getSHA1Fingerprint().replace(':', ' ');
			m_lblSubjectKeyIdentifier.setToolTipText("SHA-1 Fingerprint");
		}
		try
		{
			m_lblSubjectKeyIdentifier.setText(ski);
			// This is a bugfix for old JDKs
			m_lblSubjectKeyIdentifier.setVisible(false);
			m_lblSubjectKeyIdentifier.setVisible(true);
		}
		catch (Throwable a_e)
		{
			LogHolder.log(LogLevel.DEBUG, LogType.GUI, a_e);
			/*
			fp = fp.substring(0, fp.length() / 2) + "\n" + fp.substring((fp.length() / 2) + 1, fp.length());
			m_lblSHA1HashFallback.setText(fp);
			m_lblSHA1Hash.setVisible(false);
			m_lblSHA1HashFallback.setVisible(true);*/
		}
	}

	/** Clears the text fields that display info about the current certificate. */
	private void clearCertInfo()
	{
		m_textCertValidity.setText("");
		m_lblSubjectKeyIdentifier.setText("");
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

	private class ChooseCertStorageMethodPane extends DialogContentPane implements
		DialogContentPane.IWizardSuitable
	{
		private JRadioButton m_btnFile;
		private JRadioButton m_btnClip;

		public ChooseCertStorageMethodPane(JAPDialog a_dialog, String a_strText)
		{
			super(a_dialog, a_strText,
				  new DialogContentPaneOptions(DialogContentPane.OPTION_TYPE_OK_CANCEL));
			GridBagConstraints constr = new GridBagConstraints();
			ButtonGroup group = new ButtonGroup();

			m_btnFile = new JRadioButton("File");
			m_btnClip = new JRadioButton("Clipboard");
			group.add(m_btnFile);
			group.add(m_btnClip);
			m_btnFile.setSelected(true);
			constr.gridx = 0;
			constr.gridy = 0;
			constr.anchor = GridBagConstraints.WEST;
			constr.fill = GridBagConstraints.HORIZONTAL;
			constr.weightx = 0;
			getContentPane().setLayout(new GridBagLayout());
			getContentPane().add(m_btnFile, constr);
			constr.gridy++;
			getContentPane().add(m_btnClip, constr);
		}
		public boolean isMethodFile()
		{
			return m_btnFile.isSelected();
		}
	}

	private class FileFilterSelectionPane extends DialogContentPane implements
		DialogContentPane.IWizardSuitable
	{
		public static final int X509 = 0;
		public static final int PKCS10 = 1;
		public static final int PKCS12 = 2;

		private JRadioButton m_btnPublicCert;
		private JRadioButton m_btnCertRequest;
		private JRadioButton m_btnPrivateCert;

		private ChooseCertStorageMethodPane m_previousPane;

		public FileFilterSelectionPane(JAPDialog a_dialog, String a_strText,
			ChooseCertStorageMethodPane a_previousPane)
		{
			super(a_dialog, a_strText,
				  new DialogContentPaneOptions(DialogContentPane.OPTION_TYPE_OK_CANCEL, a_previousPane));
			m_previousPane = a_previousPane;

			GridBagConstraints constr = new GridBagConstraints();
			ButtonGroup group = new ButtonGroup();

			m_btnPublicCert = new JRadioButton("X509 public certificate");
			m_btnCertRequest = new JRadioButton("PKCS10 certification request");
			m_btnPrivateCert = new JRadioButton("PKCS12 private certificate");
			group.add(m_btnPublicCert);
			group.add(m_btnCertRequest);
			group.add(m_btnPrivateCert);
			m_btnPublicCert.setSelected(true);
			constr.gridx = 0;
			constr.gridy = 0;
			constr.anchor = GridBagConstraints.WEST;
			constr.fill = GridBagConstraints.HORIZONTAL;
			constr.weightx = 0;
			getContentPane().setLayout(new GridBagLayout());
			getContentPane().add(m_btnPublicCert, constr);
			constr.gridy++;
			getContentPane().add(m_btnCertRequest, constr);
			constr.gridy++;
			getContentPane().add(m_btnPrivateCert, constr);
		}

		public CheckError[] checkUpdate()
		{
			if (m_cert instanceof PKCS12)
			{
				m_btnCertRequest.setEnabled(true);
				//m_btnPrivateCert.setEnabled(m_previousPane.isMethodFile());
				m_btnPrivateCert.setEnabled(true);
				if (m_btnPrivateCert.isSelected() && !m_previousPane.isMethodFile())
				{
					m_btnPublicCert.setSelected(true);
				}
			}
			else
			{
				m_btnPublicCert.setSelected(true);
				m_btnCertRequest.setEnabled(false);
				m_btnPrivateCert.setEnabled(false);
			}
			return super.checkUpdate();
		}

		public int getSelectionType()
		{
			if (m_btnPublicCert.isSelected())
			{
				return X509;
			}
			else if (m_btnCertRequest.isSelected())
			{
				return PKCS10;
			}
			else
			{
				return PKCS12;
			}
		}
	}

	private class CertPanelFinishedContentPane extends FinishedContentPane
	{
		public CertPanelFinishedContentPane(JAPDialog a_parentDialog, DialogContentPane a_previousContentPane)
		{
			super(a_parentDialog, "You have successfully created the certificate!", a_previousContentPane);
		}

		public CheckError[] checkCancel()
		{
			return showConfirmDialog();
		}

		public CheckError[] checkNo()
		{
			return showConfirmDialog();
		}

		private CheckError[] showConfirmDialog()
		{
			int returnValue =
				JAPDialog.showConfirmDialog( (JAPDialog) getDialog(),
											"This will delete your newly created certificate. " +
											"Do you really want to continue?",
											JAPDialog.OPTION_TYPE_OK_CANCEL, JAPDialog.MESSAGE_TYPE_WARNING);
		   if (returnValue != RETURN_VALUE_OK)
		   {
			   return new CheckError[]{new CheckError()};
		   }

		   return null;
	   }
	}

	public void stateChanged(ChangeEvent arg0) 
	{
		enableButtons();
	}
	
	/**
	 * Return the title that is shown in the border
	 */
	public String getCertName()
	{
		return m_name;
	}
	
	/**
	 * Return the vector containing all CertPanels
	 */
	public static Vector<CertPanel> getCertPanels()
	{
		return ms_certpanels;
	}
}
