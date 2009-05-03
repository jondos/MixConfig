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
import anon.crypto.X509Extensions;
import anon.crypto.X509DistinguishedName;
import anon.crypto.X509SubjectAlternativeName;
import anon.crypto.AbstractX509AlternativeName;
import anon.util.CountryMapper;

/**
 * Stores the information that should be present in an operator certificate.
 * @author Rolf Wendolsky
 */
public class OperatorCertificateView implements ICertificateView
{
	private CountryMapper m_CountryMapper;
	private String m_strOrganisation;
	private String m_strOrgaUnit;
	private String m_strEMail;
	private String m_strURL;
	private String m_strCommonName;

	public OperatorCertificateView()
	{
		update(null);
	}

	public void update(ICertificate a_certificate)
	{
		if (a_certificate == null)
		{
			m_CountryMapper = new CountryMapper();
			m_strOrganisation = "";
			m_strOrgaUnit = "";
			m_strEMail = "";
			m_strURL = "";
			m_strCommonName = "";
			return;
		}

		Vector vecTags, vecValues;
		X509Extensions extensions;
		JAPCertificate certificate = a_certificate.getX509Certificate();
		X509DistinguishedName dn = certificate.getSubject();

		try
		{
			m_CountryMapper = new CountryMapper(dn.getCountryCode());
		}
		catch (IllegalArgumentException a_e)
		{
		}

		m_strOrganisation = formatDNField(dn.getOrganisation());
		m_strCommonName = formatDNField(dn.getCommonName());
		m_strOrgaUnit = formatDNField(dn.getOrganisationalUnit());

		extensions = a_certificate.getX509Certificate().getExtensions();
		m_strURL = "";
		m_strEMail = "";

		// try alternatives to get the e-mail address
		if (m_strEMail.length() == 0)
		{
			m_strEMail = formatDNField(dn.getE_EmailAddress());
			if (m_strEMail.length() == 0)
			{
				m_strEMail = formatDNField(dn.getEmailAddress());
			}
		}

		for (int i = 0; i < extensions.getSize(); i++)
		{
			if (extensions.getExtension(i) instanceof X509SubjectAlternativeName)
			{
				vecTags = ( (X509SubjectAlternativeName) extensions.getExtension(i)).getTags();
				vecValues = extensions.getExtension(i).getValues();
				for (int j = 0; j < vecTags.size() && j < vecValues.size() &&
					 (m_strEMail.length() == 0 || m_strURL.length() == 0); j++)
				{
					if (m_strEMail.length() == 0 &&
						vecTags.elementAt(j).equals(AbstractX509AlternativeName.TAG_EMAIL))
					{
						m_strEMail = formatDNField(vecValues.elementAt(j).toString());
					}
					if (m_strURL.length() == 0 &&
						vecTags.elementAt(j).equals(AbstractX509AlternativeName.TAG_URL))
					{
						m_strURL = formatDNField(vecValues.elementAt(j).toString());
					}
				}
			}
		}
	}

	public String getOrganisation()
	{
		return m_strOrganisation;
	}

	public String getOrganisationalUnit()
	{
		return m_strOrgaUnit;
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

	public String getEMail()
	{
		return m_strEMail;
	}

	public String getURL()
	{
		return m_strURL;
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
