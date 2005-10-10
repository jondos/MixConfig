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

import java.awt.Component;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import anon.crypto.DSAKeyPair;
import anon.crypto.PKCS12;
import anon.crypto.Validity;
import anon.crypto.X509DistinguishedName;
import anon.crypto.X509Extensions;
import anon.crypto.X509SubjectKeyIdentifier;
import logging.LogType;


/** A subclass of <CODE>SwingWorker</CODE> that starts a new thread that generates the new
 * certificate in the background.
 */
public class CertificateGenerator extends SwingWorker
{
	/** The the certificate's validity. */
	private Validity m_validity;

	/** The password for the certificate to be generated. */
	private char[] m_passwd;

	/** The overgiven Parent */
	private Component m_parent;

	/** A dialog to be shown as long as the certificate generation thread is busy. */
	private BusyWindow m_notification;

	/** The signer name for the certificate */
	private X509DistinguishedName m_name;

	private X509Extensions m_extensions;

	/** A list of <CODE>ChangeListener</CODE>s that receive <CODE>ChangeEvent</CODE>s
	 * from this object.
	 */
	private Vector m_changeListeners = new Vector();

	/** The newly generated certificate. */
	private PKCS12 m_cert;

	/** Constructs a new instance of <CODE>KeyPairGenerator</CODE>
	 * @param a_name The signer name
	 * @param a_extensions the extensions for the certificate (optional, may be null)
	 * @param a_validity the certificate's validity
	 * @param a_passwd The password for the certificate
	 * @param a_bVisible if a BusyWindow is shown or not
	 */
	public CertificateGenerator(X509DistinguishedName a_name, X509Extensions a_extensions,
								Validity a_validity, char[] a_passwd, Component a_parent)
	{
		m_name = a_name;
		m_extensions = a_extensions;
		m_validity = a_validity;
		m_passwd = a_passwd;
		m_parent = a_parent;
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
	public PKCS12 getCertificate()
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
		Vector extensions = new Vector();
		X509SubjectKeyIdentifier ski;

		try
		{
			keyPair = DSAKeyPair.getInstance(new SecureRandom(), 1024, 80);

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
				name.put(X509DistinguishedName.CN_IDENTIFIER,
						 "<Mix id=" + ski.getValueWithoutColon() + "/>");
				m_name = new X509DistinguishedName(name);
			}

			return new PKCS12(m_name, keyPair, m_validity, new X509Extensions(extensions));
		}
		catch (Exception e)
		{
			if (!Thread.interrupted())
			{
				MixConfig.handleError(e, null, LogType.THREAD);
			}
		}
		return null;
	}

	/** Called internally when the certificate generation thread finishes. This method
	 * should not be called directly.
	 */
	public void finished()
	{
		m_cert = (PKCS12) get();
		if (m_cert != null)
		{
			fireStateChanged();
		}
		if (m_notification != null)
		{
			m_notification.dispose();
		}
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

	protected void showBussyWin() {
		if (m_parent != null)
		{
			m_notification = new BusyWindow(MixConfig.getMainWindow(), "Generating Key Pair.");
			m_notification.setSwingWorker(this);
			m_notification.setSize(m_parent.getSize());
			m_notification.setLocation(m_parent.getLocation());
			m_notification.setVisible(true);
		}

	}
}
