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

/**
 *  A document that accepts only floating points.
 */
class FloatDocument extends PlainDocument
{
		private float max, min;

		// minval should be < 0, maxval > 0
		FloatDocument(float minval, float maxval)
		{
						super();
						max = maxval;
						min = minval;
		}

		// Only positive floating points
		FloatDocument(float maxval)
		{
						super();
						max = maxval;
						min = 0;
		}

		FloatDocument()
		{
						super();
						max = 0;
						min = 0;
		}

		public void insertString(int offset, String str, AttributeSet attr)
										throws BadLocationException
		{
						if(str==null)
										return;

						String p1 = getText(0,offset);
						String p2 = getText(offset, getLength()-offset);
						String res = "";

						boolean hasPoint = p1.indexOf('.')>=0 || p2.indexOf(',')>=0;

						for(int i=0;i<str.length();i++)
										if(str.charAt(i)=='.' || str.charAt(i)==',')
										{
												if(hasPoint)
														java.awt.Toolkit.getDefaultToolkit().beep();
												else
												{
														res+=".";
														hasPoint = true;
												}
										}
										else if(min<0 && str.charAt(i)=='-')
										{
												if(p1.length()==0 && res.length()==0)
														res="-";
												else
														java.awt.Toolkit.getDefaultToolkit().beep();
										}
										else if(!Character.isDigit(str.charAt(i)))
														java.awt.Toolkit.getDefaultToolkit().beep();
										else
										{
														String sstr = str.substring(i,i+1);
														float val = (new Float(p1+res+sstr+p2)).floatValue();
														if((max>0 && val>max) || (min<0 && val<min))
																		java.awt.Toolkit.getDefaultToolkit().beep();
														else
																		res+=sstr;
										}
						super.insertString(offset,res,attr);
		}
}
