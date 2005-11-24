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
package mixconfig;

import java.util.Vector;

import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.*;
import java.util.Enumeration;
import jcui.common.TextFormatUtil;
import gui.CountryMapper;
import gui.JAPMessages;
import java.util.Date;
import anon.crypto.AbstractX509Extension;
import anon.crypto.IMyPublicKey;
import anon.crypto.JAPCertificate;
import anon.crypto.X509DistinguishedName;
import anon.crypto.Validity;
import anon.crypto.X509Extensions;
import anon.crypto.AbstractX509Extension;
import anon.crypto.X509UnknownExtension;
import org.bouncycastle.asn1.x509.X509Name;
import javax.swing.JLabel;
import java.awt.GridLayout;

import gui.JAPDialog;

/**
 *
 * <p>CertDetails Dialog </p>
 * <p>Beschreibung: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Organisation: </p>
 * @author Kuno G. Gruen
 * @version 0.5
 */
public class CertDetailsDialog extends JAPDialog
{
	private static final String MSG_CERTVALID = CertDetailsDialog.class.getName() + "_CERT_VALID";
	private static final String MSG_CERTNOTVALID = CertDetailsDialog.class.getName() + "_CERT_NOTVALID";

	private int maxKeyLen = 0;

	public CertDetailsDialog(Component a_parent, JAPCertificate a_cert)
	{
		super(a_parent, "Certificate Details");
		JPanel jp_root = new JPanel();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		Vector lengthVector = new Vector();

	// Distinguished Name
		X509DistinguishedName dName = a_cert.getDistinguishedName();
		Vector dNameKeys = idsToNames(dName.getAttributeIdentifiers());
		Vector dNameVals = dName.getAttributes();
		lengthVector.addElement(dNameKeys);

	// Issuer
		X509DistinguishedName issuer = new X509DistinguishedName(a_cert.getIssuer());
		Vector issuerKeys = idsToNames(issuer.getAttributeIdentifiers());
		Vector issuerVals = issuer.getAttributes();
		lengthVector.addElement(issuerKeys);

	// Extension
		X509Extensions extensionsVect = a_cert.getExtensions();
		Vector extKeys = new Vector(extensionsVect.getSize());
		Vector extVals = new Vector(extensionsVect.getSize());

		for (int i = 0; i < extensionsVect.getSize(); i++)
		{
			if (extensionsVect.getExtension(i) instanceof X509UnknownExtension)
			{
				extKeys.addElement("Unknown Extension");
				AbstractX509Extension val = (AbstractX509Extension) extensionsVect.getExtension(i);
				StringBuffer tmpValBuf = new StringBuffer();

				if (val.getValues() != null && ! (val.getValues().isEmpty()))
				{
					for (int j = 0; j < val.getValues().size(); j++)
					{
						tmpValBuf.append(val.getValues().elementAt(j).toString());
						tmpValBuf.append(" - ");
					}
					extVals.addElement(tmpValBuf.toString());
				}
			}

			else
			{
				AbstractX509Extension key = extensionsVect.getExtension(i);
				extKeys.addElement(key.getName());
				AbstractX509Extension val = (AbstractX509Extension) extensionsVect.getExtension(i);
				StringBuffer tmpValBuf = new StringBuffer();

				if (val.getValues() != null && ! (val.getValues().isEmpty()))
				{
					for (int j = 0; j < val.getValues().size(); j++)
					{
						Object o = val.getValues().elementAt(j);
						if (o != val.getValues().lastElement())
						{
							tmpValBuf.append(val.getValues().elementAt(j).toString());
							tmpValBuf.append(" - ");
						}
						else
						{
							tmpValBuf.append(val.getValues().elementAt(j).toString());
						}
					}
					extVals.addElement(tmpValBuf.toString());
				}
			}
		}
		lengthVector.addElement(extKeys);

	// Validity
		Validity validity = a_cert.getValidity();
		Vector validityKeys = new Vector();
		validityKeys.addElement(new String("Is valid"));
		validityKeys.addElement(new String("Valid from"));
		validityKeys.addElement(new String("Valid until"));
		//lengthVector.addElement(validityKeys);
		Vector validityValues = new Vector();

		if (validity.isValid(new Date()))
		{
			validityValues.addElement(JAPMessages.getString(MSG_CERTVALID));
		}
		else
		{
			validityValues.addElement(JAPMessages.getString(MSG_CERTNOTVALID));
		}
		validityValues.addElement(validity.getValidFrom().toString());
		validityValues.addElement(validity.getValidTo().toString());

	// Fingerprints
	   Vector fpKeys = new Vector();
	   fpKeys.addElement(new String("SHA1 Fingerprint"));
	   fpKeys.addElement(new String("MD5 Fingerprint"));
		   //lengthVector.addElement(fpKeys);
	   Vector fpValues = new Vector();
	   fpValues.addElement(a_cert.getSHA1Fingerprint());
	   fpValues.addElement(a_cert.getMD5Fingerprint());

	// Key Algorithm and Key length
		Vector keyKeys = new Vector();
		keyKeys.addElement(new String("Key Algorithm"));
		keyKeys.addElement(new String("Key Length"));
		// lengthVector.addElement(keyKeys);
		Vector keyValues = new Vector();
		keyValues.addElement(new String(a_cert.getPublicKey().getAlgorithm()));
		int kLength = ( (IMyPublicKey) a_cert.getPublicKey()).getKeyLength();
		keyValues.addElement(new Integer(kLength).toString());


		// calculateAbsoluteMaxLen(lengthVector);

		// Construction of the Branches
		// needs to be done after all
		// Key-Vectors have been added to lengthVector
		// and after calculateAbsoluteMaxLen() has been done

		root.add(constructBranch(dNameKeys, dNameVals, "DistinguishedName"));
		root.add(constructBranch(issuerKeys, issuerVals, "Issuer"));
		root.add(constructBranch(extKeys, extVals, "Extensions"));
		root.add(constructBranch(validityKeys, validityValues, "Valditiy"));
		root.add(constructBranch(fpKeys, fpValues, "Fingerprints"));
		root.add(constructBranch(keyKeys, keyValues, "Key Information"));




		JTree tree = new JTree(root);
		tree.setCellRenderer(new MyCellRenderer());
		tree.setRootVisible(false);
		this.getContentPane().add(tree);
		this.setSize(550, 600);
	}

/**
	 * Constructs a Branch of a JTree from two the Vectors keys and values
	 * @param keyVect Vector with the Keys
	 * @param valVect Vector with the corresponding Values
	 * @param title String which is the title of the branch
	 * @return DefaultMutableTreeNode
	 */
	private DefaultMutableTreeNode constructBranch(Vector keyVect, Vector valVect, String title)
	{
		DefaultMutableTreeNode res = new DefaultMutableTreeNode(title);
		DetailsNode resNode;
		Enumeration keys = keyVect.elements();
		Enumeration values = valVect.elements();

		if ( (keys != null && values != null) && (keys.hasMoreElements() && values.hasMoreElements()) )
		{

			while (keys.hasMoreElements())
			{
				String keyTmp = keys.nextElement().toString();
				String valTmp = values.nextElement().toString();

				resNode = new DetailsNode(keyTmp, valTmp, maxKeyLen);
				res.add(resNode);
			}
		}
			return res;
	}

/**
	 * Returns the maximum length of Strings in a Vector
	 * @param a_vector Vector with Strings
	 * @return int which is the maximal length
	 */
	private int getMaxLength(Vector a_vector)
	{
		int tmpMaxLen = 0;
		if (a_vector != null && a_vector.size() > 0)
		{
			for (int i = 0; i < a_vector.size(); i++)
			{
				tmpMaxLen = a_vector.elementAt(i).toString().length();
				if (tmpMaxLen > maxKeyLen)
				{
					maxKeyLen = tmpMaxLen;
				}
			}
		}
		return tmpMaxLen;
	}

/**
	 * Sets the maximum String lengths from all Elements from a Vector
	 * yet are implemented as allowed Elements: Vector
	 * @param a_vector Vector
	 *
	 * @todo implement also handling for Validity objects
	 */
	private void calculateAbsoluteMaxLen(Vector a_meta_vector)
	{
		for (int i = 0; i < a_meta_vector.size(); i++)
		{
			Object o = a_meta_vector.elementAt(i);

			if (o instanceof Vector)
			{
				int tmpMaxValLen = getMaxLength( (Vector) o);
				if (tmpMaxValLen > maxKeyLen)
				{
					maxKeyLen = tmpMaxValLen;
				}
			}
		}
	}

/**
	 * Translates a Vector of numerical identifiers into human readable names
	 * see also anon.crypto.X509DistinguishedName.getAttributeNameFromAttributeIdentifier
	 * @param a_vector Vector with numerical identifiers
	 * @return a Vector with human readable Strings
	 */
	private Vector idsToNames(Vector a_vector)
	{
		Vector res = new Vector(a_vector.size());

		if (a_vector != null && a_vector.size() > 0)
		{

			for (int i = 0; i < a_vector.size(); i++)
			{
				res.addElement(anon.crypto.X509DistinguishedName.getAttributeNameFromAttributeIdentifier( (String)
					a_vector.elementAt(i)));
			}
		}
		return res;
	}

/**
	 * Encapsulates a key / value pair of Strings
	 *
	 * @author Kuno G. Gruen
	 * @version 1.0
	 * date: 21.11.05
	 */
	private class DetailsNode extends DefaultMutableTreeNode
	{
		private String key;
		private String value;
		private int keyLength;

		public DetailsNode(String a_key, String a_value, int a_length)
		{
			key = a_key;
			value = a_value;
			keyLength = a_length;
		}

		public String getKey()
		{
			return this.key;
		}

		public String getValue()
		{
			return this.value;
		}

		public String toString()
		{
			StringBuffer sb = new StringBuffer();
			sb.append(key);
			sb.append(": ");
			sb.append(value);
			return sb.toString();
		}

		/**
		 * Bloats a String to a given length
		 * The String is filled with " "
		 * @param a_string String
		 * @param a_length int
		 * @return String
		 */
		private String normaliseString(String a_string, int a_length)
		{
			StringBuffer buf = new StringBuffer();
			if (a_string != null && (a_string.trim().length()) != 0)
			{
				a_string = a_string.trim();
				int counter = a_length - a_string.length();
				buf = new StringBuffer(a_string);
				for (int i = 0; i <= counter; i++)
				{
					buf.append("_");
				}
			}
			return buf.toString();
		}

	}

	private class MyCellRenderer implements TreeCellRenderer
	{
		public Component getTreeCellRendererComponent(
			JTree tree,
			Object value,
			boolean selected,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus)
		{
			JPanel panel = new JPanel();

			if (value instanceof DefaultMutableTreeNode)
			{
				panel.add(new JLabel(value.toString()));
			}

			if (value instanceof DetailsNode)
			{
			//	panel.add(new JLabel(((DetailsNode) value).getValue()));
			}

			return panel;


		}

	}

}

