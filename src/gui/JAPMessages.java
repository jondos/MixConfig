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

package gui;

import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.text.MessageFormat;
import java.awt.Frame;

import anon.util.Util;
import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;

/**
 * Use this class to display GUI texts in the user's language. The texts Strings are loaded from
 * properties files, so-called Resource Bundles. The default resource bundle is english and its
 * name ends with '_en.properties'. Resource bundle files in other languages must have language specific
 * endings as given by ISO 639. The default resource bundle must always be present and must contain all
 * language strings. The other bundles may contain a subset of these strings.
 * @see http://www.w3.org/WAI/ER/IG/ert/iso639.htm
 */
public final class JAPMessages
{
	private static ResourceBundle ms_resourceBundle = null;
	private static ResourceBundle ms_defaultResourceBundle = null;
	private static Locale ms_locale;

	private JAPMessages()
	{
	}

	/**
	 * Initialises the resource bundle with the System default Locale. The initialisation may be repeated
	 * with a new Locale.
	 * @param a_resourceBundleFilename a file name for the resource bundle; the language code for the
	 * locale will be added programmatically (e.g. _en, _de, ...).
	 */
	public static void init(String a_resourceBundleFilename)
	{
		// Load Texts for Messages and Windows
		init(null, a_resourceBundleFilename);
	}

	/**
	 * Initialises the resource bundle with the specified Locale. The initialisation may be repeated
	 * with a new Locale.
	 * @param locale a Locale
	 * @param a_resourceBundleFilename a file name for the resource bundle; the language code for the
	 * locale will be added programmatically (e.g. _en, _de, ...).
	 */
	public static void init(Locale locale, String a_resourceBundleFilename)
	{
		try
		{
			ms_defaultResourceBundle = PropertyResourceBundle.getBundle(a_resourceBundleFilename,
				Locale.ENGLISH);
		}
		catch (Exception a_e)
		{
			JAPAWTMsgBox.MsgBox(new Frame(),
								"File not found: " + a_resourceBundleFilename + "_en" +
								".properties\nYour package of JAP may be corrupted.\n" +
								"Try again to download or install the package.",
								"Error");
			System.exit(1);
		}

		try
		{
			ms_resourceBundle = PropertyResourceBundle.getBundle(a_resourceBundleFilename, locale);
		}
		catch (Exception a_e)
		{
			try
			{
				if (locale == null || !locale.equals(Locale.getDefault()))
				{
					locale = Locale.getDefault();
					ms_resourceBundle = PropertyResourceBundle.getBundle(a_resourceBundleFilename, locale);
				}
				else
				{
					throw a_e;
				}
			}
			catch (Exception e)
			{
				ms_resourceBundle = null;
			}
		}
		if (ms_resourceBundle == null || locale.getLanguage().equals(Locale.ENGLISH))
		{
			locale = Locale.ENGLISH;
			ms_resourceBundle = ms_defaultResourceBundle;
		}

		ms_locale = locale;
	}

	/**
	 * Returns the Locale that is used by this class to get the messages.
	 * @return the Locale that is used by this class to get the messages
	 */
	public static Locale getLocale()
	{
		return ms_locale;
	}

	/**
	 * Gets the localised String for a given key.
	 * @param a_key a key for the localised String
	 * @return the localised String
	 */
	public static String getString(String a_key)
	{
		String string = null;

		try
		{
			string = ms_resourceBundle.getString(a_key);
			if (string == null || string.trim().length() == 0)
			{
				throw new MissingResourceException("Resource is empty",
					PropertyResourceBundle.class.getName(), a_key);
			}

		}
		catch (Exception e)
		{
			try
			{
				if (ms_resourceBundle != ms_defaultResourceBundle)
				{
					string = ms_defaultResourceBundle.getString(a_key);
					LogHolder.log(LogLevel.DEBUG, LogType.GUI,
								  "Could not load messsage string '" + a_key + "' for the locale '" +
								  ms_locale.getLanguage() + "'. Using default resource bundle.", true);
				}
			}
			catch (Exception a_e)
			{
				string = null;
			}

			if (string == null || string.trim().length() == 0)
			{
				LogHolder.log(LogLevel.INFO, LogType.GUI, "Could not load messsage string: " + a_key, true);
				return a_key;
			}
		}
		return string;
	}

	/**
	 * Gets the localised String for a given key. If the String contains formatting patterns,
	 * these patterns are replaced by the corresponding arguments given in an object array.
	 * For a detailed description of the formatting options please see class
	 * <code> java.text.MessageFormat </code>.
	 * @param a_key a key for the localised String
	 * @param a_arguments an object array that contains the objects that replace
	 * @return the localised String with inserted arguments
	 * @see java.text.MessageFormat
	 */
	public static String getString(String a_key, Object[] a_arguments)
	{
		return MessageFormat.format(getString(a_key), a_arguments);
	}

	/**
	 * Gets the localised String for a given key. If the String contains a formatting pattern,
	 * this pattern is replaced by the given argument object. Note that this method allows only
	 * one argument.
	 * For a detailed description of the formatting options please see class
	 * <code> java.text.MessageFormat </code>.
	 * @param a_key a key for the localised String
	 * @param a_argument a object that is inserted into the message String
	 * @return the localised String with inserted arguments
	 * @see java.text.MessageFormat
	 */
	public static String getString(String a_key, Object a_argument)
	{
		return getString(a_key, Util.toArray(a_argument));
	}
}
