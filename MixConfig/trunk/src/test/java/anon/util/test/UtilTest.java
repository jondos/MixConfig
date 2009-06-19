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
package anon.util.test;

import junitx.framework.extension.XtendedPrivateTestCase;

import anon.util.Util;

/**
 * Tests for the class Util.
 *
 * @author Rolf Wendolsky, Kuno G. Gruen
 */
public class UtilTest extends XtendedPrivateTestCase
{
	public UtilTest(String a_strName)
	{
		super(a_strName);
	}

	/**
	 * Tests if the parseFloat method works correctly.
	 */
	public void testParseFloat()
	{

		assertEquals(4.00, Util.parseFloat("4.00"), 0.001);
		assertEquals( -1.99, Util.parseFloat("-1.99"), 0.001);
		assertEquals(180.00, Util.parseFloat("180.00"), 0.001);
		assertEquals(23.03, Util.parseFloat("+23.03"), 0.001);
		assertEquals(.00, Util.parseFloat(".0"), 0.001);
		assertEquals(3.0, Util.parseFloat("3."), 0.001);

		try
		{
			Util.parseFloat("1.-00");
			fail();
		}
		catch (NumberFormatException a_e)
		{
		}

		try
		{
			Util.parseFloat("just a string");
			this.fail("A string should not be accepted!");
		}
		catch (Exception e)
		{
		}
		try
		{
			Util.parseFloat("2.0.");
			fail();
		}
		catch (NumberFormatException a_e)
		{
		}
		try
		{
			Util.parseFloat("4..00");
			fail();
		}
		catch (NumberFormatException a_e)
		{
		}
		try
		{
			Util.parseFloat(".");
			fail();
		}
		catch (NumberFormatException a_e)
		{
		}
	}

}
