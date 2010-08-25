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

import java.io.File;

import anon.crypto.PKCS10CertificationRequest;

public class SimpleFileFilter extends javax.swing.filechooser.FileFilter
{
	private String m_strDesc;
	private String m_strExtension;
	private String m_strExtensionTwo;
	private int filterType;

	public int getFilterType()
	{
		return filterType;
	}

	public SimpleFileFilter(int filter_type)
	{
		filterType = filter_type;
		switch (filter_type)
		{
			case MixConfig.FILTER_CER:
				m_strDesc = "Public X.509 Certificate (*.cer, *.crt)";
				m_strExtension = ".cer";
				m_strExtensionTwo = ".crt";
				break;
			case MixConfig.FILTER_B64_CER:
				m_strDesc = "Public X.509 Certificate, Base64 (*.cer, *.crt)";
				m_strExtension = ".cer";
				m_strExtensionTwo = ".crt";
				break;
			case MixConfig.FILTER_XML:
				m_strDesc = "Mix Configuration (*.xml)";
				m_strExtension = ".xml";
				break;
			case MixConfig.FILTER_P10:
				m_strDesc = "PKCS10 Certification Request (*" +
					PKCS10CertificationRequest.FILE_EXTENSION + ")";
				m_strExtension = PKCS10CertificationRequest.FILE_EXTENSION;
				break;
			case MixConfig.FILTER_B64_P10:
				m_strDesc = "PKCS10 Certification Request, Base64 (*" +
					PKCS10CertificationRequest.FILE_EXTENSION + ")";
				m_strExtension = PKCS10CertificationRequest.FILE_EXTENSION;
				break;
			case MixConfig.FILTER_PFX:
				m_strDesc = "Private Key with Certificate (*.pfx)";
				m_strExtension = ".pfx";
				break;
			case MixConfig.FILTER_B64_PFX:
				m_strDesc = "Private Key with Certificate, Base64 (*.pfx)";
				m_strExtension = ".pfx";
				break;
			default:
				m_strDesc = "";
				m_strExtension = "";
		}
	};

	public boolean accept(File f)
	{
		return f.isDirectory() || f.getName().endsWith(m_strExtension) ||
		(m_strExtensionTwo != null && f.getName().endsWith(m_strExtensionTwo));
	}

	public String getDescription()
	{
		return m_strDesc;
	}
}
