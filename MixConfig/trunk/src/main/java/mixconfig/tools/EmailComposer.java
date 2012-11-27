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
package mixconfig.tools;

import gui.GUIUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import mixconfig.MixConfig;
import mixconfig.MixConfiguration;
import mixconfig.network.ConnectionData;
import mixconfig.network.IncomingConnectionTableModel;
import mixconfig.panels.AdvancedPanel;
import mixconfig.panels.CertPanel;
import mixconfig.panels.GeneralPanel;
import mixconfig.panels.OwnCertificatesPanel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import anon.platform.AbstractOS;
import anon.platform.WindowsOS;
import anon.crypto.PKCS10CertificationRequest;
import anon.crypto.PKCS12;
import anon.util.Base64;

/**
 * This class offers static methods to compose an email containing certain 
 * information that is collected from the configuration XML
 * 
 * @author renner
 */
public class EmailComposer {

	/** Messages */
    
	//private static final String MSG_EMAIL_ERROR = EmailComposer.class.getName() + "_composingEmailUnsupported";
    //private static final String MSG_MISSING_CERT_ERROR = EmailComposer.class.getName() + "_missingCert";
    //private static final String MSG_MISSING_MIXNAME_ERROR = EmailComposer.class.getName() + "_missingMixName";
    
	// TODO: Check for missing certificates etc. and return if necessary
	public static void composeEmail() throws IOException, URISyntaxException
	{
		// Get the configuration
		MixConfiguration mixConf = MixConfig.getMixConfiguration();
		
		// Determine mix name and operator
	    String sMixName = mixConf.getValue(GeneralPanel.XMLPATH_GENERAL_MIXNAME);
		String sOperator = mixConf.getValue(OwnCertificatesPanel.XMLPATH_OPERATOR_ORGANISATION);
		// Get the monitoring host and port
		String sMonitoringHost = mixConf.getValue(AdvancedPanel.XMLPATH_MONITORING_HOST);
		String sMonitoringPort = mixConf.getValue(AdvancedPanel.XMLPATH_MONITORING_PORT);
		
		// Initialize the URL, use StringBuffer
		StringBuffer sbURL = new StringBuffer("mailto:certification@jondos.de?");
		sbURL.append("subject=Certificates for '");
		sbURL.append(sMixName);
		sbURL.append("'&");
		
		// BEGIN of the body
		sbURL.append("body=Operator Organization: ");
		sbURL.append(sOperator); 
		sbURL.append("\nMix Name: ");
		sbURL.append(sMixName);
		sbURL.append("\n\n");
		
		// Listener Interface(s)
		sbURL.append("Listener Interfaces:\n");
		// Append one line for each interface
		IncomingConnectionTableModel dummy = EmailComposer.getIncomingInterfaces();
		ConnectionData data = null;
		for (int i=0; i<dummy.getRowCount(); i++)
		{
			data = dummy.getData(i);
			sbURL.append("- ");
			sbURL.append(data.getHostname());
			sbURL.append(":");
			sbURL.append(data.getPort());
			sbURL.append("\n");
		}
		sbURL.append("\n");
		
		// Server Monitoring
		sbURL.append("Server Monitoring:\n- ");
		sbURL.append(sMonitoringHost);
		sbURL.append(":");
		sbURL.append(sMonitoringPort); 
		sbURL.append("\n\n");
		
		// Use a separate StringBuffer for the certificates
		StringBuffer sbCerts = new StringBuffer();
		
		// Instantiate the operator certificate from the configuration
		String sPKCS12 = mixConf.getValue("Certificates/OperatorOwnCertificate/X509PKCS12");		
		if (sPKCS12 != null && !sPKCS12.equals(""))
		{
			// Decode to bytes
			byte[] b = Base64.decode(sPKCS12);				
			PKCS12 privateCertificate = null;
							
			// -----BEGIN HACK-----
			// Get the operator certificate password somehow
			Vector<CertPanel> panels = CertPanel.getCertPanels();
			Vector<String> passwords = new Vector<String>();
			// Collect passwords for all existing operator certificates
			for (int i=0; i<panels.size(); i++)
			{
				CertPanel p = panels.get(i);
				//LogHolder.log(LogLevel.DEBUG, LogType.CRYPTO, "CertName is " + p.getCertName());
			    if (p.getCertName().equals("Operator Certificate"))
			    {
			    	//LogHolder.log(LogLevel.DEBUG, LogType.CRYPTO, "Found password: " + new String(p.getPrivateCertPassword()));
			    	passwords.add(new String(p.getPrivateCertPassword()));
			    }
			}
			// Try the passwords that were found
			for (int i=0; i<passwords.size(); i++)
			{
				privateCertificate = PKCS12.getInstance(b, passwords.get(i));
				if (privateCertificate != null) break;
				//else LogHolder.log(LogLevel.DEBUG, LogType.CRYPTO, "Unsuccessful try");
			}
			// -----END HACK-----
			
			// Create the request from the private certificate
			if (privateCertificate != null)
			{
				PKCS10CertificationRequest request = new PKCS10CertificationRequest(privateCertificate);				
				// Add the P10 certification request
				sbCerts.append("Certificate Request (P10):\n\n");				
				sbCerts.append(new String(request.toByteArray(true)));
				sbCerts.append("\n");	
			}
		}
	    // Directly take the public parts of the certificates from XML
		String opCertX509 = mixConf.getValue("Certificates/OperatorOwnCertificate/X509Certificate");
		if (opCertX509 != null && !opCertX509.equals(""))
		{				
			// Add the PEM of the operator certificate
			sbCerts.append("Operator Certificate (PEM):\n\n-----BEGIN CERTIFICATE-----\n");
			sbCerts.append(opCertX509); 
			sbCerts.append("\n-----END CERTIFICATE-----\n\n");
		}
		String mixCertX509 = mixConf.getValue("Certificates/OwnCertificate/X509Certificate");
		if (mixCertX509 != null && !mixCertX509.equals(""))
		{
			// Add the PEM of the mix certificate
			sbCerts.append("Mix Certificate (PEM):\n\n-----BEGIN CERTIFICATE-----\n");
			sbCerts.append(mixCertX509); 
			sbCerts.append("\n-----END CERTIFICATE-----\n\n");
		}
		
		// Check if this is MS Windows
		AbstractOS os = AbstractOS.getInstance();
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Detected OS is " + os.getClass().getSimpleName());
		if (os instanceof WindowsOS)
		{
			// Save certificates into clipboard
			GUIUtils.saveTextToClipboard(sbCerts.toString(), MixConfig.getMainWindow());
			sbURL.append("CERTIFICATES ARE CURRENTLY IN THE CLIPBOARD,\n");
			sbURL.append("PLEASE PASTE HERE!\n\n");
		}
		else
		{
			// Append certificates to URL
			sbURL.append(sbCerts);
		}
		// Replace line breaks and whitespace, convert StringBuffer to String here
		String sURL = encodeURL(sbURL.toString());
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Length of URL string is " + sURL.length());
		
		// Finally open the URL
		AbstractOS.getInstance().openURL(new URL(sURL));
		// Java 6 could use this:
		//Desktop.getDesktop().mail(new URI(sURL));
	}
	
	/**
	 * This method somehow duplicates parts of the method load(JTable table) in MixConfigPanel
	 * @return a TableModel containing ListenerInterfaces
	 */
	private static IncomingConnectionTableModel getIncomingInterfaces()
	{
		IncomingConnectionTableModel model = new IncomingConnectionTableModel();
		// Find the right 'ListenerInterfaces' element here
		NodeList nl = MixConfig.getMixConfiguration().getDocument().getElementsByTagName("ListenerInterfaces");
		//if (nl.getLength() == 0) {Do something}
		for (int i=0; i<nl.getLength(); i++)
		{
			Node parent = nl.item(i).getParentNode();
			if (parent.getNodeName().equals("Network"))
			{
				parent = parent.getParentNode();
				if (parent.getNodeName().equals("MixConfiguration"))
				{
					model.readFromElement((Element)nl.item(i));
				}
			}
		}		
		return model;
	}
	
	private static String encodeURL(String sURL)
	{
		sURL = sURL.replace(" ", "%20");
		sURL = sURL.replace("\n", "%0D%0A");
		return sURL;
	}
}
