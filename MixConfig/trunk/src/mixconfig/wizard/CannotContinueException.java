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

package mixconfig.wizard;

import java.util.Vector;

/**
 * An exception class thrown when navigation in a wizard is not possible in the desired direction.
 *
 * @author ronin &lt;ronin2@web.de&gt;
 */
public class CannotContinueException extends IndexOutOfBoundsException {

    /** An array of messages describing the reason for this exception. This is used to
     * display plausibility warnings for the Mix configuration panels when they are
     * shown in the configuration wizard.
     */
	private String[] m_messages = null;

	/** Creates a new instance of <CODE>CannotContinueException</CODE> */
	public CannotContinueException() {  }

	/** Creates a new instance of <CODE>CannotContinueException</CODE>
	 * @param message a message describing the cause of the exception
	 */
	public CannotContinueException(String message) { super(message); }

	/** Creates a new instance of <CODE>CannotContinueException</CODE>
	 * @param a_messages an array of messages describing the cause of the exception
	 */
	public CannotContinueException(Object a_messages[])
	{
		m_messages = new String[a_messages.length];
		for (int i = 0; i < m_messages.length; i++)
		{
			m_messages[i] = a_messages[i].toString();
		}
	}

	/** Creates a new instance of <CODE>CannotContinueException</CODE>
	 * @param a_messages a Vector of messages describing the cause of the exception
	 */
	public CannotContinueException(Vector a_messages)
	{
		m_messages = new String[a_messages.size()];
		for (int i = 0; i < m_messages.length; i++)
		{
			m_messages[i] = a_messages.elementAt(i).toString();
		}
	}

	/** Creates a new instance of <CODE>CannotContinueException</CODE>
	 * @param cause the <CODE>Throwable</CODE> object that caused the exception
	 */
	// This does not work in JDK < 1.4
	//public CannotContinueException(Throwable cause) { initCause(cause); }

	/**
	 * Returns an array of error messages.
	 * @return An array of <code>String</code> objects containing messages
	 * descriptive of this exception
	 */
	public String[] getMessages()
	{
		if(m_messages == null)
		{
			return new String[0];
		}
		return m_messages;
	}

	/**
	 * Returns all error messages in a whole String, separated by line separators.
	 * @return all error messages in a whole String, separated by line separators
	 */
	public String getMessage()
	{
		String message = "";
		String[] messages = getMessages();


		for (int i = 0; i < messages.length; i++)
		{
			message += messages[i] + "\n";
		}

		return message.trim();
	}
}
