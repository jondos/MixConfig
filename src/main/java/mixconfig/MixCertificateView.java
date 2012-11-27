/*
 Copyright (c) 2000-2005, The JAP-Team
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

import java.util.Vector;
import anon.crypto.JAPCertificate;
import anon.crypto.ICertificate;
import anon.util.CountryMapper;
import anon.util.Util;
import anon.crypto.X509BasicConstraints;
import anon.crypto.MyX509Extensions;
import anon.crypto.X509DistinguishedName;
import anon.crypto.X509SubjectAlternativeName;
import anon.crypto.AbstractX509Extension;

/**
 * Stores the information that should be present in a mix certificate.
 * @author Rolf Wendolsky
 */
public class MixCertificateView implements ICertificateView
{
	private boolean m_bCA;
	private CountryMapper m_CountryMapper;
	private String m_strLocalityName;
	private String m_strStateOrProvince;
	private String m_strLongitude;
	private String m_strLatitude;
	private String m_strCommonName;

	public MixCertificateView()
	{
		update(null);
	}

	public void update(ICertificate a_certificate)
	{
		if (a_certificate == null)
		{
			m_bCA = false;
			m_CountryMapper = new CountryMapper();
			m_strLocalityName = "";
			m_strStateOrProvince = "";
			m_strLongitude = "";
			m_strLatitude = "";
			m_strCommonName = "";
			return;
		}
		JAPCertificate certificate = a_certificate.getX509Certificate();
		X509DistinguishedName dn;
		MyX509Extensions extensions;
		X509SubjectAlternativeName alternativeName;
		Vector coordinates;
		X509BasicConstraints extConstraints;

		dn = certificate.getSubject();
		extensions = certificate.getExtensions();
		try
		{
			m_CountryMapper = new CountryMapper(dn.getCountryCode());
		}
		catch (IllegalArgumentException a_e)
		{
		}

		extConstraints = (X509BasicConstraints)
			certificate.getExtensions().getExtension(X509BasicConstraints.IDENTIFIER);
		if (extConstraints != null && extConstraints.isCA())
			
		{
			m_bCA = true;
		}
		else
		{
			m_bCA = false;
		}
			
		m_strLocalityName = formatDNField(dn.getLocalityName());
		m_strStateOrProvince = formatDNField(dn.getStateOrProvince());
		m_strCommonName = formatDNField(dn.getCommonName());
		
		AbstractX509Extension extension =
			extensions.getExtension(X509SubjectAlternativeName.IDENTIFIER);
		if (extension != null && extension instanceof X509SubjectAlternativeName)
		{
			alternativeName = (X509SubjectAlternativeName) extension;
			if (alternativeName.getTags().size() == 2 &&
				alternativeName.getValues().size() == 2)
			{
				coordinates = alternativeName.getTags();
				if (coordinates.elementAt(0).equals(
					X509SubjectAlternativeName.TAG_OTHER) &&
					coordinates.elementAt(1).equals(
						X509SubjectAlternativeName.TAG_OTHER))
				{
					coordinates = alternativeName.getValues();
					try
					{
						m_strLongitude = coordinates.elementAt(0).toString();
						Util.parseDouble(m_strLongitude);
						m_strLongitude = m_strLongitude.trim();
					}
					catch (NumberFormatException a_e)
					{
						m_strLongitude = "";
					}
					try
					{
						m_strLatitude = coordinates.elementAt(1).toString();
						Util.parseDouble(m_strLatitude);
						m_strLatitude = m_strLatitude.trim();
					}
					catch (NumberFormatException a_e)
					{
						m_strLatitude = "";
					}
				}
			}

		}
	}

	public String getLocalityName()
	{
		return m_strLocalityName;
	}

	public String getStateOrProvince()
	{
		return m_strStateOrProvince;
	}

	public CountryMapper getCountryMapper()
	{
		return m_CountryMapper;
	}
	
	public String getCommonName()
	{
		return m_strCommonName;
	}

	public String getCountry()
	{
		if (m_CountryMapper.getISOCode().length() == 0)
		{
			return "";
		}

		return 	m_CountryMapper.toString();
	}

	public String getLongitude()
	{
		return m_strLongitude;
	}

	public String getLatitude()
	{
		return m_strLatitude;
	}

	public boolean isCA()
	{
		return m_bCA;
	}

	private String formatDNField(String a_strDNField)
	{
		if (a_strDNField == null)
		{
			a_strDNField = "";
		}
		return a_strDNField.trim();
	}

}
