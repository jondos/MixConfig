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

import java.awt.Component;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import anon.crypto.*;

/**
 *  A document that accepts only non-negatives.
 */

public class IntegerDocument extends PlainDocument {
		int max;
		Component which;

		public IntegerDocument(int maxval, Component comp)
		{
						super();
						max = maxval;
						which = comp;
		}

		public IntegerDocument(int maxval)
		{
						super();
						max = maxval;
						which = null;
		}

		public IntegerDocument()
		{
						super();
						max = 0;
						which = null;
		}

		public IntegerDocument(Component comp)
		{
						super();
						max = 0;
						which = comp;
		}

		public void insertString(int offset, String str, AttributeSet attr)
										throws BadLocationException
		{
						if(str==null)
										return;

						String p1 = getText(0,offset);
						String p2 = getText(offset, getLength()-offset);
						String res = "";

						for(int i=0;i<str.length();i++)
										if(!Character.isDigit(str.charAt(i)))
														java.awt.Toolkit.getDefaultToolkit().beep();
										else
										{
														String sstr = str.substring(i,i+1);
														int val = Integer.parseInt(p1+res+sstr+p2,10);
														if(max>0 && val>max)
																		java.awt.Toolkit.getDefaultToolkit().beep();
														else
																		res+=sstr;
										}
						super.insertString(offset,res,attr);
						if(which!=null && max>0 && getLength()>0 && 10*Integer.parseInt(getText(0,getLength()),10)>max)
										which.transferFocus();
		}

}
