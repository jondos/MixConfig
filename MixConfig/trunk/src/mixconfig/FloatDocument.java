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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import anon.util.Util;

/**
 *  A document that accepts only floating points.
 */
class FloatDocument extends PlainDocument
{
	private float max, min;
	private int aCDigits_max = 0;

	// minval should be < 0 maxval > 0
	// String s is seen as a format pattern
	FloatDocument(String minval, String maxval)
	{
		super();
		 // Float.parseFloat() not available in JDK 1.1.8 !!!
	    max =  Util.parseFloat(maxval);
		min =  Util.parseFloat(minval);

		aCDigits_max = getACDigits(maxval);
	}

	/**
	 * counts the digits after a comma
	 * @param a_float float
	 * @return int
	 */

	int getPCDigits(float a_float)
	 {
	  Float fObj = new Float(a_float);
	  String str = fObj.toString();
	  int idx = str.indexOf(".");

	  if (str.startsWith("-"))
	  {
	   String sstr = str.substring(1, idx);
	   return sstr.length();
	  }
	  else
	  {
	   String sstr = str.substring(0, idx);
	   return sstr.length();
	  }
	 }

	/**
	 * counts the digits in front of a comma in a float
	 * @param a_float float
	 * @return int
	 */
	int getACDigits(float a_float)
	{
		Float fObj = new Float(a_float);
		String str = fObj.toString();
		int idx = str.indexOf(".");
		String sstr = str.substring(idx, str.length() - 1);
		return sstr.length();
	}

	/**
	 * counts the digits in front of a comma in a string
	 * @param a_string String
	 * @return int
	 */

	int getACDigits(String a_string)
	{
		int idx = a_string.indexOf(".");
		String sstr = a_string.substring(idx, a_string.length() - 1);
		return sstr.length();
	}

	/**
	 *
	 * @param offset int
	 * @param str String
	 * @param attr AttributeSet
	 * @throws BadLocationException
	 */

	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException
	{
		if (str == null)
		{
			return;
		}

		String p1 = getText(0, offset);
		String p2 = getText(offset, getLength() - offset);
		String res = "";

		boolean hasPoint = p1.indexOf('.') >= 0 || p2.indexOf(',') >= 0;

		for (int i = 0; i < str.length(); i++)
		{
			if (str.charAt(i) == '.' || str.charAt(i) == ',')
			{
				if (hasPoint)
				{
					java.awt.Toolkit.getDefaultToolkit().beep();
				}
				else
				{
					res += ".";
					hasPoint = true;
				}
			}
			else if (min < 0 && str.charAt(i) == '-')
			{
				if (p1.length() == 0 && res.length() == 0)
				{
					res = "-";
				}
				else
				{
					java.awt.Toolkit.getDefaultToolkit().beep();
				}
			}
			else if (!Character.isDigit(str.charAt(i)))
			{
				java.awt.Toolkit.getDefaultToolkit().beep();
			}
			else
			{
				if (hasPoint == true)
				{
					String all = p1 + p2;
					int idx = all.indexOf(".");
					int pt = all.length() - idx;
					if (pt > aCDigits_max)
					{
						java.awt.Toolkit.getDefaultToolkit().beep();
					}
					else
					{
						String sstr = str.substring(i, i + 1);
						float val = (new Float(p1 + res + sstr + p2)).floatValue();

						if ( (max > 0 && val > max) || (min < 0 && val < min))
						{
							java.awt.Toolkit.getDefaultToolkit().beep();
						}
						else
						{
							res += sstr;
						}
					}
				}
				else
				{
					String sstr = str.substring(i, i + 1);
					float val = (new Float(p1 + res + sstr + p2)).floatValue();

					if ( (max > 0 && val > max) || (min < 0 && val < min))
					{
						java.awt.Toolkit.getDefaultToolkit().beep();
					}
					else
					{
						res += sstr;
					}
				}
			}
		}

		super.insertString(offset, res, attr);
	}
}
