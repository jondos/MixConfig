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
package anon.crypto;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Locale;

import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.DERObjectIdentifier;

/**
 * Objects of this class represent an X509 distinguished name. The name consists of attributes,
 * that are pairs of identifiers and values. Each attribute may have exactly one value.
 * For each identifier, there also exists a string representation.
 * @author Rolf Wendolsky
 */
public final class X509DistinguishedName
{
	/** Common name : X509 identifier for the string "CN" */
	public static final String CN_IDENTIFIER = X509Name.CN.getId();
	/** Country code : X509 identifier for the string "C" */
	public static final String C_IDENTIFIER = X509Name.C.getId();
	/** State or province : X509 identifier for the string "CN" */
	public static final String ST_IDENTIFIER = X509Name.ST.getId();
	/** locality name : X509 identifier for the string "L" */
	public static final String L_IDENTIFIER = X509Name.L.getId();
	/** Organisation : X509 identifier for the string "O" */
	public static final String O_IDENTIFIER = X509Name.O.getId();
	/** Organisational Unit : X509 identifier for the string "OU" */
	public static final String OU_IDENTIFIER = X509Name.OU.getId();
	/** E-Mail : X509 identifier for the string "E" */
	public static final String E_IDENTIFIER = X509Name.E.getId();
	/** E-Mail 2: X509 identifier for the string "EmailAddress" */
	public static final String EmailAddress_IDENTIFIER = X509Name.EmailAddress.getId();
	/** Surname : Identifier for the string "SURNAME" */
	public static final String SURNAME_IDENTIFIER = X509Name.SURNAME.getId();
	/** Given name : Identifier for the string "GIVENNAME" */
	public static final String GIVENNAME_IDENTIFIER = X509Name.GIVENNAME.getId();

	private X509Name m_bcX509Name;

	/**
	 * Constructs a distinguished name from a String of the form "C=DE, ST=Bavaria, ..."
	 * @param a_x509distinguishedName String
	 */
	public X509DistinguishedName(String a_x509distinguishedName)
	{
		m_bcX509Name = new X509Name(a_x509distinguishedName);
	}

	/**
	 * Constructs a distinguished name from a Hashtable of attributes. The Hashtable contains
	 * the attribute identifiers as keys and the attributes as values. Do not use the
	 * string representation of the identifiers!
	 * @param a_attributes a Hashtable of attributes
	 * @throws IllegalCharacterException if an illegal character is contained in the DN
	 */
	public X509DistinguishedName(Hashtable a_attributes)
		throws IllegalCharacterException
	{
		if (a_attributes == null)
		{
			throw new IllegalArgumentException("Attributes must not be null!");
		}

		Enumeration oids = a_attributes.keys();
		Hashtable attributes = new Hashtable();
		String currentAttribute;
		Object oid;

		while (oids.hasMoreElements())
		{
			oid = oids.nextElement();

			if (a_attributes.get(oid) == null)
			{
				continue;
			}
			currentAttribute = a_attributes.get(oid).toString();
			if (currentAttribute.trim().length() == 0)
			{
				continue;
			}

			if (!(oid instanceof DERObjectIdentifier))
			{
				// compatibility to BouncyCastle
				oid = new DERObjectIdentifier(oid.toString());
			}


			// test if this is a valid attribute
			/*
			if (currentAttribute.indexOf("=") >= 0)
			{
				throw new IllegalCharacterException((DERObjectIdentifier)oid, '=');
			}
			else */
			if (currentAttribute.indexOf(",") >= 0)
			{
				throw new IllegalCharacterException((DERObjectIdentifier)oid, ',');
			}

			attributes.put(oid, currentAttribute);

		}

		if (attributes.size() == 0)
		{
			throw new IllegalArgumentException("Attributes are empty!");
		}
		m_bcX509Name = new X509Name(attributes);
	}

	/**
	 * Constructs a distinguished name from a BouncyCastle X509Name object. For internal use only.
	 * @param a_bcX509Name a BouncyCastle X509Name object.
	 */
	X509DistinguishedName(X509Name a_bcX509Name)
	{
		m_bcX509Name = a_bcX509Name;
	}

	/**
	 * Returns the X509 attribute corresponding to a given attribute identifier.
	 * @param a_identifier an X509 attribute identifier
	 * @return the X509 attribute corresponding to a given attribute identifier
	 */
	public static String getAttributeNameFromAttributeIdentifier(String a_identifier)
	{
		if (a_identifier == null)
		{
			return null;
		}

		if (a_identifier.equals(CN_IDENTIFIER))
		{
			return "CN";
		}
		else if (a_identifier.equals(C_IDENTIFIER))
		{
			return "C";
		}
		else if (a_identifier.equals(ST_IDENTIFIER))
		{
			return "ST";
		}
		else if (a_identifier.equals(L_IDENTIFIER))
		{
			return "L";
		}
		else if (a_identifier.equals(O_IDENTIFIER))
		{
			return "O";
		}
		else if (a_identifier.equals(OU_IDENTIFIER))
		{
			return "OU";
		}
		else if (a_identifier.equals(E_IDENTIFIER))
		{
			return "E";
		}

		return a_identifier;
	}

	/**
	 * Returns a user-displayable list of all countries in a given list of country codes.
	 * You can get the country codes by calling Locale.getISOCountries().
	 * @param a_locale a Locale; this should be the currently used Locale
	 * @param a_countryCodes country codes in uppercase two-letter ISO-3166 code
	 * @return a Vector with user-displayable countries
	 * @see java.util.Locale
	 */
	public static Vector getDisplayCountries(Locale a_locale, String[] a_countryCodes)
	{
		Vector countries = new Vector();

		for (int i = 0; i < a_countryCodes.length; i++)
		{
			countries.addElement(new Locale(
						 a_locale.getLanguage(), a_countryCodes[i]).getDisplayCountry());
		}

		return countries;
	}

	/**
	 * Returns the common name.
	 * @return the common name or null if not set
	 */
	public String getCommonName()
	{
		return getAttribute(CN_IDENTIFIER);
	}

	/**
	 * Returns the surname.
	 * @return the surname or null if not set
	 */
	public String getSurname()
	{
		return getAttribute(SURNAME_IDENTIFIER);
	}

	/**
	 * Returns the given name.
	 * @return the given name or null if not set
	 */
	public String getGivenName()
	{
		return getAttribute(GIVENNAME_IDENTIFIER);
	}

	/**
	 * Returns the country code;
	 * @return the country code or null if not set
	 */
	public String getCountryCode()
	{
		return getAttribute(C_IDENTIFIER);
	}

	/**
	 * Returns the state or province.
	 * @return the state or province or null if not set
	 */
	public String getStateOrProvince()
	{
		return getAttribute(ST_IDENTIFIER);
	}

	/**
	 * Returns the locality name.
	 * @return the locality name or null if not set
	 */
	public String getLocalityName()
	{
		return getAttribute(L_IDENTIFIER);
	}

	/**
	 * Returns the organisation.
	 * @return the organisation or null if not set
	 */
	public String getOrganisation()
	{
		return getAttribute(O_IDENTIFIER);
	}

	/**
	 * Returns the organisational unit.
	 * @return the organisational unit
	 */
	public String getOrganisationalUnit()
	{
		return getAttribute(OU_IDENTIFIER);
	}

	/**
	 * Returns the email address specified by the identifier "E".
	 * @return the email address or null if not set
	 */
	public String getE_EmailAddress()
	{
		return getAttribute(E_IDENTIFIER);
	}

	/**
	 * Returns the email address specified by the identifier "EmailAddress".
	 * @return the email address or null if not set
	 */
	public String getEmailAddress()
	{
		return getAttribute(EmailAddress_IDENTIFIER);
	}

	/**
	 * Returns the attribute value corresponding to a given identifier.
	 * @param a_identifier an attribute identifier
	 * @return String the attribute value corresponding to a given identifier or null if the
	 *                attribute is not set in this X509 name
	 */
	public String getAttribute(String a_identifier)
	{
		int index = m_bcX509Name.getOIDs().indexOf(new DERObjectIdentifier(a_identifier));

		if (index < 0)
		{
			return null;
		}

		return (String)m_bcX509Name.getValues().elementAt(index);

	}

	/**
	 * Returns the identifiers of attributes in this X509 name.
	 * @return the identifiers of attributes in this X509 name
	 */
	public Vector getAttributeIdentifiers()
	{
		Vector oids = new Vector();
		Enumeration bcOids = m_bcX509Name.getOIDs().elements();
		while (bcOids.hasMoreElements())
		{
			oids.addElement(((DERObjectIdentifier)bcOids.nextElement()).getId());
		}

		return oids;
	}

	/**
	 * Returns the values of attributes in this X509 name.
	 * @return the values of attributes in this X509 name
	 */
	public Vector getAttributes()
	{
		return m_bcX509Name.getValues();
	}

	/**
	 * Returns the distinguished name as Hashtable (identifier-attribute).
	 * @return the distinguished name as Hashtable (identifier-attribute)
	 */
	public Hashtable getDistinguishedName()
	{
		Hashtable name = new Hashtable();
		Vector identifiers = getAttributeIdentifiers();
		Vector attributes = getAttributes();
		for (int i = 0; i < identifiers.size(); i++)
		{
			name.put(identifiers.elementAt(i), attributes.elementAt(i));
		}

		return name;
	}

	/**
	 * Writes the distinguished name as a single String. Identifiers and values are
	 * separated by "=", the whole attributes are comma-separated.
	 * @return String
	 */
	public String toString()
	{
		Vector identifiers = getAttributeIdentifiers();
		Vector attributes = getAttributes();
		String name = "";

		for (int i = 0; i < identifiers.size(); i++)
		{
 			name += getAttributeNameFromAttributeIdentifier((String)identifiers.elementAt(i)) +
				"=" +
				attributes.elementAt(i);
			if (i + 1 < identifiers.size())
			{
				name += ", ";
			}
		}

		return name;
	}

	/**
	 * Represents an error that is thrown when an illegal character is used in a distinguished name
	 * attribute.
	 * @author Rolf Wendolsky
	 */
	public class IllegalCharacterException extends IllegalArgumentException
	{
		private char m_character;
		private String m_attribute;

		private IllegalCharacterException(DERObjectIdentifier a_identifier, char a_character)
		{
			super("'" + a_character + "' characters are not allowed!");

			m_attribute = getAttributeNameFromAttributeIdentifier(a_identifier.getId());
			m_character = a_character;
		}

		/**
		 * Returns the illegal character.
		 * @return the illegal character
		 */
		public char getCharacter()
		{
			return m_character;
		}

		/**
		 * Returns the attribute in that the illegal character was found.
		 * @return the attribute in that the illegal character was found
		 */
		public String getAttribute()
		{
			return m_attribute;
		}
	}

	/**
	 * Returns the BouncyCastle X509Name representation of this object. For internal use only.
	 * @return the BouncyCastle X509Name representation of this object
	 */
	X509Name getX509Name()
	{
		return m_bcX509Name;
	}
}