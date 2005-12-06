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
import gui.GUIUtils;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.ListModel;
import javax.swing.ListCellRenderer;
import javax.swing.DefaultListModel;

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
public class CertDetailsDialog extends JAPDialog //implements ListModel
{

	private static final String ICON_ARROW_RIGHT = "arrow46.gif";
	private static final String ICON_ARROW_DOWN = "arrowDown.gif";
	private static final String MSG_CERTVALID = CertDetailsDialog.class.getName() + "_CERT_VALID";
	private static final String MSG_CERTNOTVALID = CertDetailsDialog.class.getName() + "_CERT_NOTVALID";
	private static final String MSG_X509Attribute_ST = CertDetailsDialog.class.getName() + "_X509Attribute_ST";
	private static final String MSG_X509Attribute_L = CertDetailsDialog.class.getName() + "_X509Attribute_L";
	private static final String MSG_X509Attribute_C = CertDetailsDialog.class.getName() + "_X509Attribute_C";
	private static final String MSG_X509Attribute_CN = CertDetailsDialog.class.getName() + "_X509Attribute_CN";
	private static final String MSG_X509Attribute_O = CertDetailsDialog.class.getName() + "_X509Attribute_O";
	private static final String MSG_X509Attribute_OU = CertDetailsDialog.class.getName() + "_X509Attribute_OU";
	private static final String MSG_X509Attribute_EMAIL = CertDetailsDialog.class.getName() + "_X509Attribute_EMAIL";
	private static final String MSG_X509Attribute_EMAILADDRESS = CertDetailsDialog.class.getName() + "_X509Attribute_EMAIL";
	private static final String MSG_X509Attribute_SURNAME = CertDetailsDialog.class.getName() + "_X509Attribute_SURNAME";
	private static final String MSG_X509Attribute_GIVENNAME = CertDetailsDialog.class.getName() + "_X509Attribute_GIVENNAME";
	private static final String UNKNOWN_EXTENSION = "Unknown extension";
	private int m_maxKeyLen = 0;

	private Vector m_distinguishedNameKeys;
	private Vector m_distinguishedNameValues;
	private Vector m_issuerKeys;
	private Vector m_issuerValues;
	private Vector m_extensionsKeys;
	private Vector m_extensionsValues;



	/**
	 * Constructs a JTree in which attributes of a Certificate are displayed
	 *
	 * @param a_parent Component in which the Tree is included
	 * @param a_cert JAPCertificate which is the Certificate to be displayed
	 */
	public CertDetailsDialog(Component a_parent, JAPCertificate a_cert)
	{
		super(a_parent, "Certificate Details");
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		Vector lengthVector = new Vector();
		Vector identifiers;

	// Distinguished Name
		X509DistinguishedName dName = a_cert.getDistinguishedName();
		identifiers = dName.getAttributeIdentifiers();
		m_distinguishedNameKeys = idsToNames(identifiers);
		m_distinguishedNameValues = dName.getAttributes();
		replaceCountryCodeByCountryName(m_distinguishedNameValues, identifiers);
		lengthVector.addElement(m_distinguishedNameKeys);

	// Issuer
		X509DistinguishedName issuer = new X509DistinguishedName(a_cert.getIssuer());
		identifiers = issuer.getAttributeIdentifiers();
		m_issuerKeys = idsToNames(identifiers);
		m_issuerValues = issuer.getAttributes();
		replaceCountryCodeByCountryName(m_issuerValues, identifiers);
		lengthVector.addElement(m_issuerKeys);

	// Extension
	// Extensions have keys and values
	// each key can have more than one values
	// that's why this complex construction is needed
		X509Extensions extensionsVect = a_cert.getExtensions();
		m_extensionsKeys = new Vector(extensionsVect.getSize());
		m_extensionsValues = new Vector(extensionsVect.getSize());

		for (int i = 0; i < extensionsVect.getSize(); i++)
		{
			if (extensionsVect.getExtension(i) instanceof X509UnknownExtension)
			{
				if (extensionsVect.getExtension(i).isCritical())
				{
					m_extensionsKeys.addElement(UNKNOWN_EXTENSION + " *");
				}

				else
				{
					m_extensionsKeys.addElement(UNKNOWN_EXTENSION);
				}
			}

			else
			{
				m_extensionsKeys.addElement(extensionsVect.getExtension(i).toString());
				if (!(extensionsVect.getExtensions().isEmpty()))
				{
					AbstractX509Extension val = (AbstractX509Extension) extensionsVect.getExtension(i);
					if (! (val.getValues().isEmpty()))
					{
						m_extensionsValues.addElement(val.getValues().firstElement());
						//val.getValues().removeElementAt(0);

						for (int j = 1; j < val.getValues().size(); j++)
						{
							m_extensionsKeys.addElement(new String(" "));
							m_extensionsValues.addElement(val.getValues().elementAt(j));
						}
					}
				}
			}
		}
		lengthVector.addElement(m_extensionsKeys);

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


		m_maxKeyLen = calculateAbsoluteMaxLen(lengthVector);


		/* Important
			Construction of the Branches needs to be done after:
			 - Vectors with Keys have been added to lengthVector
			 - the method calculateAbsoluteMaxLen() has run
		 */

		root.add(constructBranch(m_distinguishedNameKeys, m_distinguishedNameValues, "DistinguishedName"));
		root.add(constructBranch(m_issuerKeys, m_issuerValues, "Issuer"));
		root.add(constructBranch(m_extensionsKeys, m_extensionsValues, "Extensions"));
		root.add(constructBranch(validityKeys, validityValues, "Validity"));
		root.add(constructBranch(fpKeys, fpValues, "Fingerprints"));
		root.add(constructBranch(keyKeys, keyValues, "Key Information"));

		JTree tree = new JTree(root);
		tree.setCellRenderer(new DetailsTreeCellRenderer());
		tree.setRootVisible(false);
		tree.expandRow(0);
		//tree.expandRow(1);
		//tree.expandRow(2);
		tree.setBackground(a_parent.getBackground());
		//	tree.set
		JScrollPane sp = new JScrollPane(
			tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.getContentPane().add(sp);
		this.pack();
	}




/**
	 * Constructs a Branch of a JTree from the two Vectors keys and values
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
		Object obj;
		int res = 0;

		for (int i = 0; i < a_meta_vector.size(); i++)
		{
			obj = a_meta_vector.elementAt(i);

			if (obj instanceof Vector)
			{
				int tmpMaxLen = getVectorsMaxKeyLength( (Vector) obj);
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
				else if (abbrev.equals(X509DistinguishedName.LABEL_LOCALITY))
				{
					str = JAPMessages.getString(MSG_X509Attribute_L);
				}
				else if (abbrev.equals(X509DistinguishedName.LABEL_COUNTRY))
				{
					str = JAPMessages.getString(MSG_X509Attribute_C);
				}
				else if (abbrev.equals(X509DistinguishedName.LABEL_COMMON_NAME))
				{
					str = JAPMessages.getString(MSG_X509Attribute_CN);
				}
				else if (abbrev.equals(X509DistinguishedName.LABEL_ORGANISATION))
				{
					str = JAPMessages.getString(MSG_X509Attribute_O);
				}
				else if (abbrev.equals(X509DistinguishedName.LABEL_ORGANISATIONAL_UNIT))
				{
					str = JAPMessages.getString(MSG_X509Attribute_OU);
				}
				else if (abbrev.equals(X509DistinguishedName.LABEL_EMAIL))
				{
					str = JAPMessages.getString(MSG_X509Attribute_EMAIL);
				}
				else if (abbrev.equals(X509DistinguishedName.LABEL_EMAIL_ADDRESS))
				{
					str = JAPMessages.getString(MSG_X509Attribute_EMAILADDRESS);
				}
				else if (abbrev.equals(X509DistinguishedName.LABEL_SURNAME))
				{
					str = JAPMessages.getString(MSG_X509Attribute_SURNAME);
				}
				else if (abbrev.equals(X509DistinguishedName.LABEL_GIVENNAME))
				{
					str = JAPMessages.getString(MSG_X509Attribute_GIVENNAME);
				}
				else
				{
					str = abbrev;
				}
				if (!str.equals(abbrev))
				{
					str += " (" + abbrev + ")";
				}
				res.addElement(str);
			}
		}
		return res;
	}

	/**
	 * Looks for the C identifier and replaces the corresponding attribute by a country name if possible.
	 * @param a_attributes a Vector with distinguished name attributes
	 * @param a_identifiers a Vector with identifiers corresponding with the dn attributes
	 */
	private void replaceCountryCodeByCountryName(Vector a_attributes, Vector a_identifiers)
	{
		for (int i = 0; i < a_attributes.size(); i++)
		{
			if (a_identifiers.elementAt(i).equals(X509DistinguishedName.IDENTIFIER_C))
			{
				try
				{
					a_attributes.setElementAt(
						new CountryMapper(a_attributes.elementAt(i).toString()).toString(), i);
				}
				catch (IllegalArgumentException a_e)
				{
					// invalid country code
				}
			}
		}
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

	private class DetailsTreeCellRenderer implements TreeCellRenderer
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
					lbl_title.setFont(new java.awt.Font(
					   lbl_title.getFont().getName(),java.awt.Font.BOLD, 14));
					lbl_title.setText(node.getValue());
					res_panel.add(lbl_title);
				}

				else
				{
					String key = ( (DetailsNode) value).getKey();

					JLabel lbl_key = new JLabel(key);
					lbl_key.setPreferredSize(
					   new java.awt.Dimension(m_maxKeyLen * 8, lbl_key.getFont().getSize() + 4));
					res_panel.add(lbl_key);

					String val = ( (DetailsNode) value).getValue();
					JLabel lbl_val = new JLabel(val);
					lbl_val.setFont(new java.awt.Font(lbl_val.getFont().getName(), java.awt.Font.PLAIN,
													  lbl_val.getFont().getSize()));
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

