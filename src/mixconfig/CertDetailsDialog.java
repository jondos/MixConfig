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
import gui.CountryMapper;
import gui.JAPMessages;
import java.util.Date;
import java.util.StringTokenizer;
import anon.crypto.AbstractX509Extension;
import anon.crypto.IMyPublicKey;
import anon.crypto.JAPCertificate;
import anon.crypto.X509DistinguishedName;
import anon.crypto.Validity;
import anon.crypto.X509Extensions;
import anon.crypto.AbstractX509Extension;
import anon.crypto.X509UnknownExtension;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
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

	private static final String MSG_X509Attribute_ST = CertDetailsDialog.class.getName() + "_X509Attribute_ST";
	private static final String MSG_X509Attribute_L = CertDetailsDialog.class.getName() + "_X509Attribute_L";
	private static final String MSG_X509Attribute_C = CertDetailsDialog.class.getName() + "_X509Attribute_C";
	private static final String MSG_X509Attribute_CN = CertDetailsDialog.class.getName() + "_X509Attribute_CN";
	private static final String DELIMITER = ", ";
	private int maxKeyLen = 0;

	public CertDetailsDialog(Component a_parent, JAPCertificate a_cert)
	{
		super(a_parent, "Certificate Details");
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
				if (extensionsVect.getExtension(i).isCritical())
				{
					extKeys.addElement("Unknown Extension*");
				}

				else
				{
					extKeys.addElement("Unknown Extension");
				}

				AbstractX509Extension val = (AbstractX509Extension) extensionsVect.getExtension(i);
				StringBuffer tmpValBuf = new StringBuffer();

				if (val.getValues() != null && ! (val.getValues().isEmpty()))
				{
					for (int j = 0; j < val.getValues().size(); j++)
					{
						tmpValBuf.append(" ");
						//tmpValBuf.append(val.getValues().elementAt(j).toString());
						//tmpValBuf.append(DELIMITER);
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
							tmpValBuf.append(DELIMITER);
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
		validityKeys.addElement(new String("Certificate is"));
		validityKeys.addElement(new String("Valid from"));
		validityKeys.addElement(new String("Valid until"));
		lengthVector.addElement(validityKeys);
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
	   lengthVector.addElement(fpKeys);
	   Vector fpValues = new Vector();
	   fpValues.addElement(a_cert.getSHA1Fingerprint());
	   fpValues.addElement(a_cert.getMD5Fingerprint());

	// Key Algorithm and Key length
		Vector keyKeys = new Vector();
		keyKeys.addElement(new String("Key Algorithm"));
		keyKeys.addElement(new String("Key Length"));
		lengthVector.addElement(keyKeys);
		Vector keyValues = new Vector();
		keyValues.addElement(new String(a_cert.getPublicKey().getAlgorithm()));
		int kLength = ( (IMyPublicKey) a_cert.getPublicKey()).getKeyLength();
		keyValues.addElement(new Integer(kLength).toString());


		maxKeyLen = calculateAbsoluteMaxLen(lengthVector);


		/* Important
			Construction of the Branches needs to be done after:
			 - Vectors with Keys have been added to lengthVector
			 - the method calculateAbsoluteMaxLen() has run
		 */

		root.add(constructBranch(dNameKeys, dNameVals, "DistinguishedName"));
		root.add(constructBranch(issuerKeys, issuerVals, "Issuer"));
		root.add(constructBranch(extKeys, extVals, "Extensions"));
		root.add(constructBranch(validityKeys, validityValues, "Validity"));
		root.add(constructBranch(fpKeys, fpValues, "Fingerprints"));
		root.add(constructBranch(keyKeys, keyValues, "Key Information"));

		JTree tree = new JTree(root);
		tree.setCellRenderer(new MyCellRenderer());
		tree.setRootVisible(false);
		tree.expandRow(0);
		//tree.expandRow(1);
		//tree.expandRow(2);
		tree.setBackground(a_parent.getBackground());
		JScrollPane sp = new JScrollPane(
			tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.getContentPane().add(sp);
		this.pack();
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
		DetailsNode res = new DetailsNode(title);
		DetailsNode resNode;
		String resKey, resValue;
		Enumeration keys = keyVect.elements();
		Enumeration values = valVect.elements();

		if ( (keys != null && values != null) && (keys.hasMoreElements() && values.hasMoreElements()) )
		{

			while (keys.hasMoreElements())
			{
				resKey = keys.nextElement().toString();
				if (values.hasMoreElements())
				{
					resValue = values.nextElement().toString();
				}
				else
				{
					resValue = "missing Data";
				}
				resNode = new DetailsNode(resKey, resValue);
				res.add(resNode);
			}
		}
			return res;
	}

	/**
	 * Returns the maximum length of Strings in a Vector
	 * @param a_vector Vector with DetailNodes
	 * @return int which is the maximal length
	 */
	private int getVectorsMaxKeyLength(Vector a_vector)
	{
		int maxLen = 0;
		if (a_vector != null && a_vector.size() > 0)
		{
			for (int i = 0; i < a_vector.size(); i++)
			{
				if (a_vector.elementAt(i) instanceof String)
				{
					String str = (String) a_vector.elementAt(i);
					int tmpLen = str.length();
					if (tmpLen > maxLen)
					{
						maxLen = tmpLen;
					}
				}
			}
		}
	return maxLen;
	}

	/**
	 * Returns the maximum key length of ... /todo/
	 * yet are implemented as allowed Elements: Vector
	 * @param a_meta_vector Vector
	 *
	 * @todo implement also handling for Validity objects
	 */
	private int calculateAbsoluteMaxLen(Vector a_meta_vector)
	{
		int res = 0;

		for (int i = 0; i < a_meta_vector.size(); i++)
		{
			Object o = a_meta_vector.elementAt(i);

			if (o instanceof Vector)
			{
				int tmpMaxLen = getVectorsMaxKeyLength( (Vector) o);
				if (tmpMaxLen > res)
				{
					res = tmpMaxLen;
				}
			}
		}
	return res;
	}

/**
	 * Translates a Vector of numerical identifiers into human readable names
	 *
	 * see also: anon.crypto.X509DistinguishedName.getAttributeNameFromAttributeIdentifier()
	 * @param a_vector Vector with numerical identifiers
	 * @return a Vector with human readable Strings
	 */
	private Vector idsToNames(Vector a_vector)
	{
		Vector res = new Vector(a_vector.size());
		String str = " ";

		if (a_vector != null && a_vector.size() > 0)
		{
			for (int i = 0; i < a_vector.size(); i++)
			{

				String abbrev = (anon.crypto.X509DistinguishedName.
								 getAttributeNameFromAttributeIdentifier( (String) a_vector.elementAt(i)));

				if (abbrev.equals(X509DistinguishedName.LABEL_STATE_OR_PROVINCE))
				{
					str = JAPMessages.getString(MSG_X509Attribute_ST);
				}

				if (abbrev.equals(X509DistinguishedName.LABEL_LOCALITY))
				{
					str = JAPMessages.getString(MSG_X509Attribute_L);
				}

				if (abbrev.equals(X509DistinguishedName.LABEL_COUNTRY))
				{
					str = JAPMessages.getString(MSG_X509Attribute_C);
				}

				if (abbrev.equals(X509DistinguishedName.LABEL_COMMON_NAME))
				{
					str = JAPMessages.getString(MSG_X509Attribute_CN);
				}
				res.addElement(str);
			}
		}
		return res;
	}

	/**
	 * Class DetialsNode
	 * encapsulates a key / value pair of Strings
	 *
	 * @author Kuno G. Gruen
	 * @version 1.0
	 * date: 21.11.05
	 */
	private class DetailsNode extends DefaultMutableTreeNode
	{
		private String key;
		private String value;
		private boolean isTitle = false;

		/**
		 * For key / value pairs only
		 *
		 * @param a_key String
		 * @param a_value String
		 */
		public DetailsNode(String a_key, String a_value)
		{
			key = a_key;
			value = a_value;
		}

		/**
		 *  For titles only
		 *
		 *  these will be stored as the value
		 * @param a_title String
		 */
		public DetailsNode(String a_title)
		{
			key = null;
			value = a_title;
			isTitle = true;
		}

		public String getKey()
		{
			return this.key;
		}

		public String getValue()
		{
			return this.value;
		}

		public int getKeyLength()
		{
			return key.length();
		}

		public String toString()
		{
			return new String();
		}

		public boolean isTitle()
		{
			return isTitle;
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
			JPanel res_panel = new JPanel();

			if (value instanceof DefaultMutableTreeNode)
			{
				res_panel.add(new JLabel(value.toString()));
			}

			if (value instanceof DetailsNode)
			{
				DetailsNode node = (DetailsNode) value;

				if (node.isTitle())
				{
					JLabel lbl_title = new JLabel();
					lbl_title.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,5,5));
					lbl_title.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
					lbl_title.setText(node.getValue());
					res_panel.add(lbl_title);
				}

				else
				{
					String key = ( (DetailsNode) value).getKey();

					JLabel lbl_key = new JLabel(key);
					lbl_key.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
					lbl_key.setPreferredSize(new java.awt.Dimension(maxKeyLen * 8, lbl_key.getFont().getSize() + 4));
					res_panel.add(lbl_key);

					String val = ( (DetailsNode) value).getValue();
					JLabel lbl_val = new JLabel(val);

					lbl_val.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,5,5));
					res_panel.add(lbl_val);
				}
			}

			else
			{
				res_panel.add(new JLabel("error"));
			}
			return res_panel;

		}

	}

}

