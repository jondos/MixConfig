/*
 Copyright (c) 2000 - 2004, The JAP-Team
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
package anon.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

/**
 * This class is an abstract implementation of a private key.
 * @author Rolf Wendolsky
 */
public abstract class AbstractPublicKey implements IMyPublicKey
{
	protected AbstractPublicKey()
	{
	}

	/**
	 * Creates a public key from a PrivateKeyInfo. Every public key class should implement
	 * this constructor.
	 * @param a_keyInfo a SubjectPublicKeyInfo
	 */
	public AbstractPublicKey(SubjectPublicKeyInfo a_keyInfo)
	{
	}

	/**
	 * Returns the key in a byte encoded form that is defined to be the result of the method
	 * <Code> getAsSubjectPublicKeyInfo() </Code>.
	 * @return the key in a byte encoded form
	 * @see getAsSubjectPublicKeyInfo()
	 */
	public final byte[] getEncoded()
	{
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		DEROutputStream dOut = new DEROutputStream(bOut);
		try
		{
			dOut.writeObject(getAsSubjectPublicKeyInfo());
			dOut.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException("IOException while encoding public key");
		}
		return bOut.toByteArray();
	}

	/**
	 * @return the public key`s hash code
	 * @see java.lang.Object#hashCode()
	 */
	public abstract int hashCode();

	/**
	 * This method returns if two public keys have the same public key parameters.
	 * @param a_publicKey an other public key
	 * @return true if the keys have the same public key parameters; false otherwise
	 */
	public abstract boolean equals(Object a_publicKey);

}
