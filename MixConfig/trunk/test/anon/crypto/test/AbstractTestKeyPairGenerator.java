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
package anon.crypto.test;

import java.security.SecureRandom;

import anon.crypto.AsymmetricCryptoKeyPair;

/**
 * Instances of this class create random asymmetric cipher key pairs. It is for testing
 * purposes only.
 * @author Rolf Wendolsky
 */
public abstract class AbstractTestKeyPairGenerator
{
	private SecureRandom m_random;

	/**
	 * Creates a new instance.
	 * @param a_random a random number generator
	 */
	public AbstractTestKeyPairGenerator(SecureRandom a_random)
	{
		m_random = a_random;
	}

	/**
	 * Creates a random asymmetric cipher key pair.
	 * @return a random asymmetric cipher key pair
	 */
	public abstract AsymmetricCryptoKeyPair createKeyPair();

	/**
	 * Returns the random number generator.
	 * @return the random number generator
	 */
	protected SecureRandom getRandom()
	{
		return m_random;
	}
}
