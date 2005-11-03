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

package gui;

import java.util.Locale;
import java.util.Vector;
import anon.util.Util;
import gui.*;

public class CountryMapper
{
	private static final String[] ms_ctrArr =
		// ISO 3166 on 25.10.05
		// http://www.iso.ch/iso/en/prods-services/iso3166ma/
		//           02iso-3166-code-lists/iso_3166-1_decoding_table.html

		{
		// Officially assigned code element
		"AD", "AE", "AF", "AG", "AI", "AL", "AM", "AN", "AO", "AQ", "AR", "AS",
		"AT", "AU", "AW", "AX", "AZ", "BA", "BB", "BD", "BE", "BF", "BG", "BH",
		"BI", "BJ", "BM", "BN", "BO", "BR", "BS", "BT", "BV", "BW",	"BY", "BZ",
		"CA", "CC", "CD", "CF", "CG", "CH", "CI", "CK", "CL", "CM", "CN", "CO",
		"CR", "CS", "CU", "CV", "CX", "CY", "CZ", "DE", "DJ", "DK", "DM", "DO",
		"DZ", "EC", "EE", "EG", "EH", "ER", "ES", "ET",	"FI", "FJ", "FK", "GA",
		"GB", "FM", "FO", "GP", "GQ", "GR", "GS", "GT", "GU", "GW", "GY", "GD",
		"GE", "GF", "GH", "GI", "GL", "GM", "GN", "HM", "HN", "HR", "HT", "HU",
		"IQ", "IR", "IS", "IT", "LR", "LS",	"LT", "LU", "LV", "HK", "ID", "IE",
		"IL", "IN", "IO", "KE", "KG", "KH", "KI", "JM", "JO", "JP", "KM", "KN",
		"KP", "KR", "KW", "KY", "KZ", "LA", "LB", "LC", "LI", "LK", "LY", "MA",
		"MC", "MD", "MG", "MH",	"MK", "ML", "MM", "MN", "MO", "MP", "MQ", "MR",
		"MS", "MT", "MU", "MV", "MW", "MX", "MY", "MZ", "NA", "NC", "NE", "NF",
		"NG", "NI", "NL", "NO", "NP", "NR", "NU", "NZ", "PA", "PE", "PF", "PG",
		"PH", "OM",	"PK", "PL", "PM", "PN", "RU", "RW", "SV", "PR", "PS", "PT",
		"QA", "RE", "PW", "PY", "SY", "SZ", "SA", "SB", "SC", "SD", "SE", "SG",
		"SH", "SI", "SJ", "SK", "SL", "SM", "SN", "SO", "RO", "SR", "ST", "TC",
		"TD", "TF", "TG", "TH", "TJ", "TK", "TL", "TM", "TN", "TO", "TR", "TT",
		"TV", "TW", "TZ", "UG", "UA", "UY", "UZ", "UM", "US", "VI", "VN", "ZM",
		"YT", "VU", "VA", "VC", "VE", "VG", "WF", "WS", "ZA", "YE",	"ZW",
		/*

		 // Exceptionally reserved code element
		"AC", "DG", "EA", "CP", "EU", "FX", "GG", "IC", "JE", "IM", "UK",

		 // User-assigned code element
		"TA", "QM", "QN", "QO", "QP", "QQ", "QR", "QS", "QT", "QU", "QV", "QW",
		"QX", "QY", "QZ", "XA", "XB", "XC", "XD", "XE", "XF", "XG", "XH", "XI",
		"XJ", "XK", "XL", "XM", "XN", "XO", "XP", "XQ", "XR", "XS", "XT", "XU",
		"XV", "XW", "XX", "XY", "XZ", "ZZ"

		 */
};

	private boolean m_bUseDefaultLocale;
	private String m_iso2;
	private Locale m_locale;

	/**
	 * Constructor with default Locale
	 * @see getLocalisedCountries()
	 * @param a_ISO2CountryCode String
	 * @throws IllegalArgumentException
	 */
	public CountryMapper(String a_ISO2CountryCode)
		throws IllegalArgumentException
	{
		if (a_ISO2CountryCode == null || a_ISO2CountryCode.trim().length() == 0)
		{
			a_ISO2CountryCode = "  ";
		}

		if (a_ISO2CountryCode.length() != 2)
		{
			throw new IllegalArgumentException(
					 "ISO Country code must have a length of two characters!");
		}

		m_iso2 = a_ISO2CountryCode.trim();
		m_bUseDefaultLocale = true;
		m_locale = Locale.getDefault();
	}

	/**
	 * Constructor with given Locale
	 * @see getLocalisedCountries(Locale)
	 * @param a_ISO2CountryCode String
	 * @param a_locale Locale
	 * @return CountryMapper constuctor
	 * @throws IllegalArgumentException
	 */
	public CountryMapper(String a_ISO2CountryCode, Locale a_locale)
		throws IllegalArgumentException
	{
		if (a_ISO2CountryCode == null || a_ISO2CountryCode.trim().length() == 0)
				{
					a_ISO2CountryCode = "  ";
		}
		if (a_ISO2CountryCode.length() != 2)
		{
			throw new IllegalArgumentException(
					 "ISO Country code must have a length of two characters!");
		}

		m_iso2 = a_ISO2CountryCode.trim();
		if (a_locale == null)
		{
			m_bUseDefaultLocale = true;
			m_locale = Locale.getDefault();
		}
		m_bUseDefaultLocale = false;
		m_locale = a_locale;
	}

	/**
	 * @return String[]
	 */
	static String[] getISOCountries()
	{
		return ms_ctrArr;
	}

	/**
	 * getter for the isoCountryCode
	 * @return String
	 */
	public String getISOCountryCode()
	{
		return m_iso2;
	}

	/**
	 * overwritten for use by JComboBox
	 * @return String
	 */
	public String toString()
	{
		if (m_iso2.length() == 0)
		{
			return JAPMessages.getString("CountryMapper_ChooseCountry");
		}

		if (m_bUseDefaultLocale)
		{
			String cName = new Locale(Locale.getDefault().getLanguage(), m_iso2).getDisplayCountry();
			if (cName.equals(m_iso2) || cName == null)
			{
				cName = JAPMessages.getString(m_iso2);
			}
			return cName;

		}
		return new Locale(m_locale.getLanguage(), m_iso2).getDisplayCountry();
	}

	/**
	 * Factory for a Vector of CountryMappers
	 * uses the default locale
	 * @return Vector
	 */
	public static Vector getLocalisedCountries()
	{
		Vector localisedCountries = new Vector();

		for (int i = 0; i < ms_ctrArr.length; i++)
		{
			localisedCountries.addElement(new CountryMapper(ms_ctrArr[i]));
		}
		return anon.util.Util.bubbleSort(localisedCountries);
	}
	/**
	 * Factory for a Vector of CountryMappers
	 * uses a given locale
	 * @return Vector
	 */

	public static Vector getLocalisedCountries(Locale a_loc)
	{
		Vector localisedCountries = new Vector();

		for (int i = 0; i < ms_ctrArr.length; i++)
		{
			localisedCountries.addElement(new CountryMapper(ms_ctrArr[i], a_loc));
		}
		return localisedCountries;
	}
}
