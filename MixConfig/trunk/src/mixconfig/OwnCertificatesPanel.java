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

import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import anon.crypto.JAPCertificate;
import anon.crypto.PKCS12;
import anon.crypto.X509DistinguishedName;
import anon.crypto.X509Extensions;
import anon.crypto.X509SubjectAlternativeName;
import anon.util.Util;
import gui.CountryMapper;
import gui.JAPMessages;
import gui.MapBox;
import gui.TitledGridBagPanel;
import gui.dialog.JAPDialog;
import logging.LogType;

public class OwnCertificatesPanel extends MixConfigPanel implements ActionListener,
	ChangeListener
{
	public static final String XMLPATH_LOCATION = "Description/Location";
	public static final String XMLPATH_OPERATOR = "Description/Operator";
	public static final String XMLPATH_LOCATION_CITY = XMLPATH_LOCATION +
		"/City";
	public static final String XMLPATH_LOCATION_COUNTRY = XMLPATH_LOCATION +
		"/Country";
	public static final String XMLPATH_LOCATION_STATE = XMLPATH_LOCATION +
		"/State";
	public static final String XMLPATH_LOCATION_LONGITUDE = XMLPATH_LOCATION +
		"/Position/Geo/Longitude";
	public static final String XMLPATH_LOCATION_LATITUDE = XMLPATH_LOCATION +
		"/Position/Geo/Latitude";
	public static final String XMLPATH_OPERATOR_ORGANISATION = XMLPATH_OPERATOR +
		"/Organisation";
	public static final String XMLPATH_OPERATOR_URL = XMLPATH_OPERATOR +
		"/URL";
	public static final String XMLPATH_OPERATOR_EMAIL = XMLPATH_OPERATOR +
		"/EMail";
	public static final String XMLPATH_OPERATOR_COUNTRY = XMLPATH_OPERATOR +
		"/Country";
	public static final String XMLPATH_OPERATOR_CITY = XMLPATH_OPERATOR +
		"/City";
	public static final String XMLPATH_OPERATOR_ORGA_UNIT = XMLPATH_OPERATOR +
		"/OrganisationalUnit";

	public static final String MSG_INVALID_POSITION = OwnCertificatesPanel.class.getName() +
		"_invalidPosition";
	public static final String MSG_INVALID_EMAIL = OwnCertificatesPanel.class.getName() + "_invalidEmail";
	public static final String MSG_INVALID_URL = OwnCertificatesPanel.class.getName() + "_invalidUrl";
	private static final String MSG_CERT_NOT_VERIFYABLE =
		OwnCertificatesPanel.class.getName() + "_certNotVerifyable";

	private CertPanel m_ownCert;
	private CertPanel m_operatorCert;
	private JButton map;
	private JTextField m_txtCity, m_txtState, m_txtLongitude, m_txtLatitude, m_txtOperatorOrgaUnit;
	private JTextField m_txtOperatorOrg, m_txtOperatorUrl, m_txtOperatorEmail;
	private JComboBox m_cboxCountry, m_cbxOperatorCountry;
	private MapBox box;

	public OwnCertificatesPanel(boolean isApplet)
	{
		super("Own Certificates");

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.insets = getDefaultInsets();

		m_ownCert = new CertPanel("Own Mix Certificate",
								  "Hint: You have to send your public test " +
								  "certificate to the operators of your " +
								  "adjacent mixes", (PKCS12)null, CertPanel.CERT_ALGORITHM_DSA);
		m_ownCert.setName("Certificates/OwnCertificate");
		m_ownCert.setChangedCertNotVerifyableMessage(JAPMessages.getString(MSG_CERT_NOT_VERIFYABLE),
													 new JAPDialog.LinkedHelpContext(this));
		m_ownCert.setCertCreationValidator(new OwnCertCreationValidator());
		m_ownCert.setCertificateView(new MixCertificateView());
		m_ownCert.addChangeListener(this);
		add(m_ownCert, c);

		m_operatorCert = new CertPanel("Operator Certificate",
									   "Hint: You have to send your public test " +
									   "certificate to the operators of your " +
									   "adjacent mixes",
									   (PKCS12)null, CertPanel.CERT_ALGORITHM_DSA);
		m_operatorCert.setName("Certificates/OperatorOwnCertificate");
		m_operatorCert.setChangedCertNotVerifyableMessage(JAPMessages.getString(MSG_CERT_NOT_VERIFYABLE),
			new JAPDialog.LinkedHelpContext(this));
		m_operatorCert.setCertCreationValidator(new OperatorCertCreationValidator());
		m_operatorCert.setCertificateView(new OperatorCertificateView());
		m_operatorCert.addChangeListener(this);
		c.gridx = 1;
		add(m_operatorCert, c);
		//m_operatorCert.setEnabled(false); // disabled at the moment

		TitledGridBagPanel panelLocation = new TitledGridBagPanel("Mix Location");
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(panelLocation, c);

		m_txtCity = new JTextField(MAX_COLUMN_LENGTH);
		m_txtCity.setName(XMLPATH_LOCATION_CITY);
		m_txtCity.addFocusListener(this);
		panelLocation.addRow(new JLabel("City"), m_txtCity, null);

		m_txtState = new JTextField(MAX_COLUMN_LENGTH);
		m_txtState.setName(XMLPATH_LOCATION_STATE);
		m_txtState.addFocusListener(this);
		panelLocation.addRow(new JLabel("State"), m_txtState, null);

		Vector ctrVec = CountryMapper.getLocalisedCountries(MAX_COMBO_BOX_LENGTH);
		ctrVec.insertElementAt(new CountryMapper(MAX_COMBO_BOX_LENGTH), 0);
		m_cboxCountry = new JComboBox(ctrVec);
		m_cboxCountry.setName(XMLPATH_LOCATION_COUNTRY);
		m_cboxCountry.addFocusListener(this);
		m_cboxCountry.addItemListener(this);
		m_cboxCountry.setEditable(false);
		panelLocation.addRow(new JLabel("Country"), m_cboxCountry, null);

		/**
		 JLabel pos = new JLabel("Geographical Position");
		 pos.setToolTipText(
		 "Example: University of Technology Dresden, CS Department: Longitude: 13.761, Latitude: 51.053");
		 panelLocation.addRow(pos, null);**/

		m_txtLongitude = new JTextField(MAX_COORDINATE_FIELD_LENGTH);
		m_txtLongitude.setName(XMLPATH_LOCATION_LONGITUDE);
		m_txtLongitude.addFocusListener(this);
		m_txtLongitude.setDocument(new FloatDocument("-180.0000", "180.0000"));
		m_txtLongitude.setToolTipText("Longitude in degrees east of Greenwich. ( -180.0000 to 180.0000)");

		m_txtLatitude = new JTextField(MAX_COORDINATE_FIELD_LENGTH);
		m_txtLatitude.setName(XMLPATH_LOCATION_LATITUDE);
		m_txtLatitude.addFocusListener(this);
		m_txtLatitude.setDocument(new FloatDocument("-90.0000", "90.0000"));
		m_txtLatitude.setToolTipText(
			"Latitude in degrees. (-90.0000: South Pole, 0: Equator, 90.0000: North Pole)");

		map = new JButton("Show on Map");
		map.setToolTipText("Opens a window with a map from www.MapQuest.com " +
						   "of the area around the specified coordinates.");
		map.addActionListener(this);
		map.setActionCommand("Map");
		map.setEnabled(!isApplet);

		panelLocation.addRow(new JLabel("Longitude"), m_txtLongitude, map);
		panelLocation.addRow(new JLabel("Latitude"), m_txtLatitude, null);

		TitledGridBagPanel panelOperator = new TitledGridBagPanel("Operator");
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 0;
		add(panelOperator, c);

		m_txtOperatorOrg = new JTextField();
		m_txtOperatorOrg.setName(XMLPATH_OPERATOR_ORGANISATION);
		m_txtOperatorOrg.addFocusListener(this);
		m_txtOperatorOrg.setToolTipText(
			"This should contain the operating organisation's or a person's name for private persons.");
		panelOperator.addRow(new JLabel("Organisation"), m_txtOperatorOrg);

		m_txtOperatorOrgaUnit = new JTextField();
		m_txtOperatorOrgaUnit.setName(XMLPATH_OPERATOR_ORGA_UNIT);
		m_txtOperatorOrgaUnit.addFocusListener(this);
		m_txtOperatorOrgaUnit.setToolTipText(
			"The operator's organisational unit.");
		panelOperator.addRow(new JLabel("Orga. Unit"), m_txtOperatorOrgaUnit);

		m_cbxOperatorCountry = new JComboBox(ctrVec);
		m_cbxOperatorCountry.setName(XMLPATH_OPERATOR_COUNTRY);
		m_cbxOperatorCountry.addFocusListener(this);
		m_cbxOperatorCountry.addItemListener(this);
		m_cbxOperatorCountry.setEditable(false);
		panelOperator.addRow(new JLabel("Country"), m_cbxOperatorCountry);

		m_txtOperatorUrl = new JTextField();
		m_txtOperatorUrl.setName(XMLPATH_OPERATOR_URL);
		m_txtOperatorUrl.addFocusListener(this);
		m_txtOperatorUrl.setToolTipText("This should contain a URL that will lead to more information about the operator including contact information.");
		panelOperator.addRow(new JLabel("URL"), m_txtOperatorUrl);

		m_txtOperatorEmail = new JTextField();
		m_txtOperatorEmail.setName(XMLPATH_OPERATOR_EMAIL);
		m_txtOperatorEmail.addFocusListener(this);
		m_txtOperatorEmail.setToolTipText(
			"An E-Mail address to which a confirmation message will be sent once the cascade is established.");
		panelOperator.addRow(new JLabel("E-Mail"), m_txtOperatorEmail);

		//Keep the panels in place
		JLabel dummyLabel1 = new JLabel("");
		c.gridx = 2;
		c.gridy = 2;
		c.weightx = 5;
		c.weighty = 5;
		c.fill = c.BOTH;
		this.add(dummyLabel1, c);

	}

	public void actionPerformed(ActionEvent ae)
	{
		String lat = getConfiguration().getValue(XMLPATH_LOCATION_LATITUDE);
		String lon = getConfiguration().getValue(XMLPATH_LOCATION_LONGITUDE);

		try
		{
			if (ae.getActionCommand().equals("Map"))
			{
				box = new MapBox(MixConfig.getMainWindow(), lat, lon, 5);
				box.setVisible(true);
			}
		}
		catch (Exception e)
		{
			JAPDialog.showErrorDialog(MixConfig.getMainWindow(), null, LogType.GUI, e);
		}
	}

	public String getHelpContext()
	{
		return OwnCertificatesPanel.class.getName();
	}

	public Vector check()
	{
		Vector errors, tempErrors;
		ICertCreationValidator validator;


		validator = new OwnCertCreationValidator();
		validator.isValid();
		errors = validator.getInvalidityMessages();

		validator = new OperatorCertCreationValidator();
		validator.isValid();
		tempErrors = validator.getInvalidityMessages();
		if (tempErrors.size() != 0)
		{
			if (errors.size() == 0)
			{
				errors = tempErrors;
			}
			else
			{
				for (int i = 0; i < tempErrors.size(); i++)
				{
					errors.addElement(tempErrors.elementAt(i));
				}
			}
		}


		if (this.m_ownCert.getCert() == null)
		{
			errors.addElement("Own Mix Certificate is missing in " + getName() + " panel.");
		}

		return errors;
	}

	public void focusLost(FocusEvent a_event)
	{
		super.focusLost(a_event);
		if (a_event.getSource() instanceof JTextField)
		{
			enableComponents();
		}

	}

	public void stateChanged(ChangeEvent e)
	{
		try
		{
			if (e.getSource() instanceof CertPanel)
			{
				save( (CertPanel) e.getSource());
				if(e.getSource() == m_ownCert)
				{
					if (m_ownCert.getCert() != null)
					{
						MixCertificateView certView = (MixCertificateView) m_ownCert.getCertificateView();
						if (certView.getCountry().length() > 0)
						{
							m_cboxCountry.setSelectedItem(certView.getCountryMapper());
							save(m_cboxCountry);
						}
						if (certView.getLocalityName().length() > 0)
						{
							m_txtCity.setText(certView.getLocalityName());
							save(m_txtCity);
						}
						if (certView.getStateOrProvince().length() > 0)
						{
							m_txtState.setText(certView.getStateOrProvince());
							save(m_txtState);
						}
						if (certView.getLongitude().length() > 0)
						{
							m_txtLongitude.setText(certView.getLongitude());
							save(m_txtLongitude);
						}
						if (certView.getLatitude().length() > 0)
						{
							m_txtLatitude.setText(certView.getLatitude());
							save(m_txtLatitude);
						}
						//sign Own cert with Operator cert
						if((m_operatorCert.getCert() != null) && CertPanel.isAutoSign() &&
						   !m_ownCert.getCert().getX509Certificate().verify(
												 m_operatorCert.getCert().getX509Certificate()))
						{
							if(JAPDialog.showYesNoDialog(this,
								"Do you want to sign your Mix-Certificate with the Operator-Certificate?"))
							{
								PKCS12 operatorCert = (PKCS12)m_operatorCert.getCert();
								((PKCS12) m_ownCert.getCert()).sign(operatorCert);
								m_ownCert.setCert(m_ownCert.getCert().getX509Certificate());
							}
						}
					}
					updateDeprecatedMixID();
					if (! ((MixCertificateView) m_ownCert.getCertificateView()).isMixCertificate())
					{
						JAPDialog.showWarningDialog(MixConfig.getMainWindow(),
							JAPMessages.getString(MSG_WARNING_NO_MIX_CERT));
					}
				}
				else if (e.getSource() == m_operatorCert)
				{
					m_ownCert.setAdditionalVerifier(m_operatorCert.getCert());
					m_ownCert.updateCertificateIcon(false);
					if (m_operatorCert.getCert() != null)
					{
						OperatorCertificateView certView =
							(OperatorCertificateView) m_operatorCert.getCertificateView();
						if (certView.getCountry().length() > 0)
						{
							m_cbxOperatorCountry.setSelectedItem(certView.getCountryMapper());
							save(m_cbxOperatorCountry);
						}
						if (certView.getEMail().length() > 0)
						{
							m_txtOperatorEmail.setText(certView.getEMail());
							save(m_txtOperatorEmail);
						}
						if (certView.getOrganisation().length() > 0)
						{
							m_txtOperatorOrg.setText(certView.getOrganisation());
							save(m_txtOperatorOrg);
						}
						if (certView.getOrganisationalUnit().length() > 0)
						{
							m_txtOperatorOrgaUnit.setText(certView.getOrganisationalUnit());
							save(m_txtOperatorOrgaUnit);
						}
						if (certView.getURL().length() > 0)
						{
							m_txtOperatorUrl.setText(certView.getURL());
							save(m_txtOperatorUrl);
						}
						//sign Own Cert with Operator Cert
						if((m_ownCert.getCert() != null) && CertPanel.isAutoSign() &&
							!m_ownCert.getCert().getX509Certificate().verify(
							 m_operatorCert.getCert().getX509Certificate()))
						{
							if(JAPDialog.showYesNoDialog(this,
								"Do you want to sign your Mix-Certificate with the Operator-Certificate?"))
							{
								PKCS12 operatorCert = (PKCS12)m_operatorCert.getCert();
								((PKCS12) m_ownCert.getCert()).sign(operatorCert);
								m_ownCert.setCert(m_ownCert.getCert().getX509Certificate());
							}
						}
					}
				}
				enableComponents();
			}
		}
		catch (Exception ex)
		{
			JAPDialog.showErrorDialog(MixConfig.getMainWindow(), null, LogType.GUI, ex);
		}
	}

	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		// first enable all components to make MixConfigPanel load their data
		enableComponents();

		super.setConfiguration(a_conf);

		// make sure this panel is contained only once in the config's listeners list
		a_conf.removeChangeListener(this);
		a_conf.addChangeListener(this);

		updateDeprecatedMixID();

		enableComponents();
	}

	private class OwnCertCreationValidator implements ICertCreationValidator
	{
		private Vector m_invalidity = new Vector();

		public boolean isValid()
		{
			boolean valid = true;
			String strLongitude;
			String strLatitude;
			MixConfiguration conf = getConfiguration();
			String[] message = new String[2];
			message[1] = getName();

			m_invalidity = new Vector();

			message[0] = "city";
			valid = valid && checkCertificateField(XMLPATH_LOCATION_CITY, m_invalidity, message);
			message[0] = "country";
			valid = valid && checkCertificateField(XMLPATH_LOCATION_COUNTRY, m_invalidity, message);

			strLongitude = conf.getValue(XMLPATH_LOCATION_LONGITUDE);
			strLatitude = conf.getValue(XMLPATH_LOCATION_LATITUDE);
			if ( (strLongitude != null && strLongitude.length() > 0)
				|| (strLatitude != null && strLatitude.length() > 0))
			{
				try
				{
					Util.parseFloat(strLongitude);
					Util.parseFloat(strLatitude);
				}
				catch (NumberFormatException a_e)
				{
					m_invalidity.addElement(JAPMessages.getString(MSG_INVALID_POSITION, getName()));
					valid = false;
				}
			}

			return valid;
		}

		public X509DistinguishedName getSigName()
		{
			Hashtable attributes = new Hashtable();
			attributes.put(X509DistinguishedName.IDENTIFIER_ST,
						   m_txtState.getText());
			attributes.put(X509DistinguishedName.IDENTIFIER_L,
						   m_txtCity.getText());
			attributes.put(X509DistinguishedName.IDENTIFIER_C,
						   ( (CountryMapper) m_cboxCountry.getSelectedItem()).
						   getISOCode());

			return new X509DistinguishedName(attributes);
		}

		public X509Extensions getExtensions()
		{
			Vector coordinates = new Vector();
			Vector tags = new Vector();
			String strLongitude =
				getConfiguration().getValue(XMLPATH_LOCATION_LONGITUDE);
			String strLatitude =
				getConfiguration().getValue(XMLPATH_LOCATION_LATITUDE);

			if (strLongitude != null && strLongitude.length() > 0 &&
				strLatitude != null && strLatitude.length() > 0)
			{
				coordinates.addElement(strLongitude);
				coordinates.addElement(strLatitude);
				tags.addElement(X509SubjectAlternativeName.TAG_OTHER);
				tags.addElement(X509SubjectAlternativeName.TAG_OTHER);
				return new X509Extensions(
					new X509SubjectAlternativeName(coordinates, tags));
			}

			return null;
		}

		public String getPasswordInfoMessage()
		{
			return "This password has to be entered every time the Mix " +
				"server starts.\nSo if you want to start it automatically " +
				"you shouldn't enter a password.";
		}

		public Vector getInvalidityMessages()
		{
			return m_invalidity;
		}
	}

	private class OperatorCertCreationValidator implements ICertCreationValidator
	{
		private Vector m_invalidity = new Vector();

		/**
		 * @todo check if it is really a valid url
		 * @return boolean
		 */
		public boolean isValid()
		{
			String strValue;
			String[] message = new String[2];
			message[1] = getName();

			m_invalidity = new Vector();

			message[0] = "organisation";
			checkCertificateField(XMLPATH_OPERATOR_ORGANISATION, m_invalidity, message);
			message[0] = "email";

			if (!X509SubjectAlternativeName.isValidEMail(getConfiguration().getValue(
				 XMLPATH_OPERATOR_EMAIL)))
			{
				m_invalidity.addElement(JAPMessages.getString(MSG_INVALID_EMAIL, getName()));
			}

			/*
			message[0] = "country";
			checkCertificateField(XMLPATH_OPERATOR_COUNTRY, m_invalidity, message);*/

			strValue = getConfiguration().getValue(XMLPATH_OPERATOR_URL);
			if (strValue != null && strValue.trim().length() > 0)
			{
				if (new StringTokenizer(strValue).countTokens() > 1)
				{
					m_invalidity.addElement(JAPMessages.getString(MSG_INVALID_URL, getName()));
				}
			}

			return m_invalidity.size() == 0;
		}

		public X509DistinguishedName getSigName()
		{
			Hashtable attributes = new Hashtable();
			attributes.put(X509DistinguishedName.IDENTIFIER_CN, CN_ANON_OPERATOR_CERTIFICATE);
			attributes.put(X509DistinguishedName.IDENTIFIER_OU, m_txtOperatorOrgaUnit.getText());
			attributes.put(X509DistinguishedName.IDENTIFIER_O, m_txtOperatorOrg.getText());
			attributes.put(X509DistinguishedName.IDENTIFIER_E, m_txtOperatorEmail.getText());
			attributes.put(X509DistinguishedName.IDENTIFIER_C,
						   ( (CountryMapper) m_cbxOperatorCountry.getSelectedItem()).getISOCode());
			return new X509DistinguishedName(attributes);
		}


		public X509Extensions getExtensions()
		{
			Vector vecValues = new Vector();
			Vector vecTags = new Vector();
			String strUrl = getConfiguration().getValue(XMLPATH_OPERATOR_URL);

			if (strUrl != null && strUrl.trim().length() > 0)
			{
				vecValues.addElement(strUrl);
				vecTags.addElement(X509SubjectAlternativeName.TAG_URL);
			}
			vecValues.addElement(getConfiguration().getValue(XMLPATH_OPERATOR_EMAIL));
			vecTags.addElement(X509SubjectAlternativeName.TAG_EMAIL);

			return new X509Extensions(new X509SubjectAlternativeName(vecValues, vecTags));
		}

		public String getPasswordInfoMessage()
		{
			return "Your operator certificate should be protected by a good password.";
		}

		public Vector getInvalidityMessages()
		{
			return m_invalidity;
		}
	}

	protected void save(JComboBox a_comboBox)
	{
		if (a_comboBox == m_cboxCountry || a_comboBox == m_cbxOperatorCountry)
		{
			getConfiguration().setValue(a_comboBox.getName(),
										( (CountryMapper) a_comboBox.
										 getSelectedItem()).getISOCode());
		}
		else
		{
			super.save(a_comboBox);
		}
	}

	protected void load(JComboBox a_comboBox)
	{
		if (a_comboBox == m_cboxCountry || a_comboBox == m_cbxOperatorCountry)
		{
			int selectedIndex = 0;
			CountryMapper cmSelected = null;

			try
			{
				cmSelected = new CountryMapper(
					getConfiguration().getValue(a_comboBox.getName()));
			}
			catch (IllegalArgumentException a_e)
			{
			}

			if (cmSelected != null)
			{
				for (int i = 0; i < a_comboBox.getItemCount(); i++)
				{
					if (cmSelected.equals(a_comboBox.getItemAt(i)))
					{
						selectedIndex = i;
						break;
					}
				}
			}
			a_comboBox.setSelectedIndex(selectedIndex);
		}
		else
		{
			super.load(a_comboBox);
		}
	}

	protected void enableComponents()
	{
		enableMixCertificateFields();
		enableOperatorCertificateFields();
	}

	private void enableOperatorCertificateFields()
	{
		OperatorCertificateView certView = (OperatorCertificateView) m_operatorCert.getCertificateView();
		if (m_operatorCert.getCert() != null &&
			( (CountryMapper) m_cbxOperatorCountry.getSelectedItem()).equals(certView.getCountryMapper()))
		{
			m_cbxOperatorCountry.setEnabled(false);
		}
		else
		{
			m_cbxOperatorCountry.setEnabled(true);
		}
		if (certView.getEMail().length() > 0 &&
			certView.getEMail().equals(m_txtOperatorEmail.getText().trim()))
		{
			m_txtOperatorEmail.setEditable(false);
		}
		else
		{
			m_txtOperatorEmail.setEditable(true);
		}
		if (certView.getOrganisation().length() > 0 &&
			certView.getOrganisation().equals(m_txtOperatorOrg.getText().trim()))
		{
			m_txtOperatorOrg.setEditable(false);
		}
		else
		{
			m_txtOperatorOrg.setEditable(true);
		}

		if (m_operatorCert.getCert() != null &&
			certView.getOrganisationalUnit().equals(m_txtOperatorOrgaUnit.getText().trim()))
		{
			m_txtOperatorOrgaUnit.setEditable(false);
		}
		else
		{
			m_txtOperatorOrgaUnit.setEditable(true);
		}
		if (m_operatorCert.getCert() != null && certView.getURL().equals(m_txtOperatorUrl.getText()))
		{
			m_txtOperatorUrl.setEditable(false);
		}
		else
		{
			m_txtOperatorUrl.setEditable(true);
		}
	}

	private void enableMixCertificateFields()
	{
		MixCertificateView certView = (MixCertificateView) m_ownCert.getCertificateView();
		if (certView.getCountry().length() > 0 &&
			( (CountryMapper) m_cboxCountry.getSelectedItem()).equals(certView.getCountryMapper()))
		{
			m_cboxCountry.setEnabled(false);
		}
		else
		{
			m_cboxCountry.setEnabled(true);
		}
		if (certView.getLocalityName().length() > 0 &&
			certView.getLocalityName().equals(m_txtCity.getText().trim()))
		{
			m_txtCity.setEditable(false);
		}
		else
		{
			m_txtCity.setEditable(true);
		}
		if (m_ownCert.getCert() != null && certView.getStateOrProvince().equals(m_txtState.getText().trim()))
		{
			m_txtState.setEditable(false);
		}
		else
		{
			m_txtState.setEditable(true);
		}
		if (m_ownCert.getCert() != null && certView.getLongitude().equals(m_txtLongitude.getText()))
		{
			m_txtLongitude.setEditable(false);
		}
		else
		{
			m_txtLongitude.setEditable(true);
		}
		if (m_ownCert.getCert() != null && certView.getLatitude().equals(m_txtLatitude.getText()))
		{
			m_txtLatitude.setEditable(false);
		}
		else
		{
			m_txtLatitude.setEditable(true);
		}
	}

	/**
	 * @todo remove hack if mix/infoservice/jap has been rewritten accordingly
	 * Hack: update the MixID each time the private mix certificate is saved
	 */
	private void updateDeprecatedMixID()
	{
		if (m_ownCert == null || m_ownCert.getCert() == null)
		{
			save("General/MixID", null);
		}
		else
		{
			String cn = ( (PKCS12) m_ownCert.getCert()).getSubject().getCommonName();
			try
			{
				String temp = cn.substring( (cn.indexOf("id=") + 3), cn.indexOf("/>"));
				StringTokenizer tokenizer = new StringTokenizer(temp, "\"");
				cn = tokenizer.nextToken();
			}
			catch (Exception a_e)
			{
			}

			save("General/MixID", cn);
		}
	}


	private boolean checkCertificateField(String a_xmlPath, Vector a_vecInvalidityMsg, Object[] msgArgs)
	{
		String strValue = getConfiguration().getValue(a_xmlPath);

		if (strValue == null || strValue.trim().equals(""))
		{
			a_vecInvalidityMsg.addElement(JAPMessages.getString(MSG_ERROR_BLANK_FIELD, msgArgs));
			return false;
		}
		return true;
	}
}
