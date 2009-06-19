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

import java.util.Vector;

import anon.crypto.X509DistinguishedName;
import anon.crypto.X509Extensions;

/** This interface contains methods that provide information needed for generating
 * new certificates. Classes that use <CODE>CertPanel</CODE> must implement this
 * interface and use the {@link #setCertCreationValidator} to set themselves as the
 * validator for the certificate generation.
 */
public interface ICertCreationValidator
{
	/** Indicates whether the prerequisites for generating the certificate are met.
	 * For example, if a certificate for the own Mix is to be created, the Mix id must
	 * be valid as it is incorporated in the certificate's subject name.
	 * @return <CODE>true</CODE> if the prerequisites are met, <CODE>false</CODE> otherwise
	 */
	boolean isValid();

	/** Returns the signer name for the new certificate.
	 * @return The signer name
	 */
	X509DistinguishedName getSigName();

	/**
	 * Returns the X509 extensions that are added to the new certificate.
	 * @return the X509 extensions that are added to the new certificate
	 */
	X509Extensions getExtensions();

	/** Returns a message to be shown in the &quot;new password&quot; dialog for the PKCS12 certificate.
	 * @return A password info message
	 */
	String getPasswordInfoMessage();

	/** Returns a message to be shown when the prerequisites of generating a new
	 * certificate are not met.
	 * @return A warning about the prerequisites
	 */
	Vector<String> getInvalidityMessages();
}
