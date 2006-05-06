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

import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.Vector;
import anon.crypto.DSAKeyPair;
import anon.crypto.RSAKeyPair;
import anon.crypto.AsymmetricCryptoKeyPair;
import anon.crypto.PKCS12;
import anon.crypto.Validity;
import anon.crypto.X509DistinguishedName;
import anon.crypto.X509Extensions;
import anon.crypto.X509SubjectKeyIdentifier;
import logging.LogType;
import gui.dialog.JAPDialog;
import gui.dialog.WorkerContentPane;
import gui.dialog.ValidityContentPane;
import gui.dialog.DialogContentPane;


/** A subclass of <CODE>SwingWorker</CODE> that starts a new thread that generates the new
 * certificate in the background.
 */
public class CertificateGenerator implements Runnable
{
	private ValidityContentPane m_validityContentPane;

	/** The the certificate's validity. */
	private Validity m_validity;

	/** The signer name for the certificate */
	private X509DistinguishedName m_name;

	private X509Extensions m_extensions;

	private boolean m_bDSA;

	/** The newly generated certificate. */
	private PKCS12 m_cert;

	/** Constructs a new instance of <CODE>KeyPairGenerator</CODE>
	 * @param a_name The signer name
	 * @param a_extensions the extensions for the certificate (optional, may be null)
	 * @param a_validity the certificate's validity
	 * @param a_bDSA if true, DSA ist used; otherwise an RSA certificate is created
	 */
	public CertificateGenerator(X509DistinguishedName a_name, X509Extensions a_extensions,
								Validity a_validity, boolean a_bDSA)
	{
		m_name = a_name;
		m_extensions = a_extensions;
		m_validity = a_validity;
		m_bDSA = a_bDSA;
	}

	private CertificateGenerator(ValidityContentPane a_validityContentPane,  X509DistinguishedName a_name,
								 X509Extensions a_extensions, boolean a_bDSA)
	{
		m_validityContentPane = a_validityContentPane;
		m_name = a_name;
		m_extensions = a_extensions;
		m_bDSA = a_bDSA;
	}

	/** Retrieves the newly generated certificate.
	 * @return The new certificate
	 */
	public PKCS12 getCertificate()
	{
		return m_cert;
	}

	/** Generates the new certificate. This method is used internally and should not
	 * be called directly.
	 */
	public void run()
	{
		AsymmetricCryptoKeyPair keyPair;
		Vector extensions = new Vector();
		X509SubjectKeyIdentifier ski;
		if (m_validity == null)
		{
			m_validity = m_validityContentPane.getValidity();
		}

		try
		{
			if (m_bDSA)
			{
				keyPair = DSAKeyPair.getInstance(new SecureRandom(), 1024, 80);
			}
			else
			{
				keyPair = RSAKeyPair.getInstance(new SecureRandom(), 1024, 80);
			}

			/**
			 * Add the SubjectPublicKeyIdentifier extension to the certificate.
			 */
			ski = new X509SubjectKeyIdentifier(keyPair.getPublic());
			if (m_extensions != null && m_extensions.getSize() > 0)
			{
				extensions = m_extensions.getExtensions();
			}
			extensions.addElement(ski);


			/**
			 * @todo Remove this hack and rewrite mix/infoservice/jap code appropriately.
			 * If the common name is not set it is assumed that a mix certificate
			 * is created. Hack: The common name is replaced by an XML structure containing
			 * a hash of the public key.
			 */
			if (m_name.getCommonName() == null || m_name.getCommonName().trim().length() == 0)
			{
				Hashtable name;
				name = m_name.getDistinguishedName();
				name.put(X509DistinguishedName.IDENTIFIER_CN,
						 "<Mix id=" + ski.getValueWithoutColon() + "/>");
				m_name = new X509DistinguishedName(name);
			}

			m_cert = new PKCS12(m_name, keyPair, m_validity, new X509Extensions(extensions));
		}
		catch (Exception e)
		{
			if (!Thread.currentThread().isInterrupted())
			{
				JAPDialog.showErrorDialog(MixConfig.getMainWindow(), "Threading error!", LogType.THREAD, e);
			}
			m_cert = null;
		}
	}

	public static CertificateWorker createWorker(JAPDialog a_parentDialog,
												 DialogContentPane a_previousContentPane,
												 X509DistinguishedName a_name,
												 X509Extensions a_extensions,
												 boolean a_bDSA)
	{
		ValidityContentPane validityContentPane = null;
		DialogContentPane contentPane;

		contentPane = a_previousContentPane;
		while (contentPane != null && validityContentPane == null)
		{
			if (contentPane instanceof ValidityContentPane)
			{
				validityContentPane = (ValidityContentPane)contentPane;
			}
			contentPane = contentPane.getPreviousContentPane();
		}
		if (validityContentPane == null)
		{
			throw new IllegalArgumentException(
						 CertificateGenerator.class.getName() + " needs one " +
						 ValidityContentPane.class.getName() + "as previous content pane!");
		}

		CertificateWorker worker =
			new CertificateWorker(a_parentDialog, validityContentPane,
								  a_previousContentPane,
								  new CertificateGenerator(validityContentPane, a_name,
			a_extensions, a_bDSA));

		return worker;
	}

	public static class CertificateWorker extends WorkerContentPane
	{
		private ValidityContentPane m_validityContentPane;
		private CertificateGenerator m_generator;

		private CertificateWorker(JAPDialog a_parentDialog, ValidityContentPane a_validityContentPane,
								 DialogContentPane a_previousContentPane,
								 CertificateGenerator a_generator)
		 {
			 super(a_parentDialog, "Generating key pair.", a_previousContentPane, a_generator);
			 getButtonCancel().setEnabled(false);
			 m_validityContentPane = a_validityContentPane;
			 m_generator = a_generator;
		}

		public CertificateGenerator getCertificateGenerator()
		{
			return m_generator;
		}

		public CheckError[] checkDialog()
		{
			// this error should never happen
			if (m_validityContentPane.getValidity() == null)
			{
				return new CheckError[]
					{
					new CheckError("No validity given!", LogType.GUI)
				};
			}

			return null;
		}
	}
}
