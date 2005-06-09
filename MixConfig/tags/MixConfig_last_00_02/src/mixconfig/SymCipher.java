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

import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.params.KeyParameter;

final class SymCipher
{
	AESFastEngine aesEngine;
	byte[] m_iv = null;

	public SymCipher()
	{
		aesEngine = new AESFastEngine();
		m_iv = new byte[16];
		for (int i = 0; i < 16; i++)
		{
			m_iv[i] = 0;
		}
	}

	public int setKey(byte[] key)
	{
		return setKey(key, 0);
	}

	public int setKey(byte[] key, int offset)
	{
		try
		{
			aesEngine.init(true, new KeyParameter(key, offset, 16));
			for (int i = 0; i < 16; i++)
			{
				m_iv[i] = 0;
			}
			return 0;
		}
		catch (Exception e)
		{
			return -1;
		}
	}

	public int setIV(byte[] iv, int offset)
	{
		try
		{
			for (int i = 0; i < 16; i++)
			{
				m_iv[i] = iv[i + offset];
			}
			return 0;
		}
		catch (Exception e)
		{
			return -1;
		}
	}

	public int crypt(byte[] from, int ifrom, byte[] to, int ito, int len)
	{
		len = ifrom + len;
		while (ifrom < len - 15)
		{
			aesEngine.processBlock(m_iv, 0, m_iv, 0);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[0]);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[1]);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[2]);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[3]);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[4]);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[5]);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[6]);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[7]);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[8]);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[9]);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[10]);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[11]);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[12]);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[13]);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[14]);
			to[ito++] = (byte) (from[ifrom++] ^ m_iv[15]);
		}
		if (ifrom < len)
		{
			aesEngine.processBlock(m_iv, 0, m_iv, 0);
			len -= ifrom;
			for (int k = 0; k < len; k++)
			{
				to[ito++] = (byte) (from[ifrom++] ^ m_iv[k]);
			}
		}
		return 0;
	}

}
