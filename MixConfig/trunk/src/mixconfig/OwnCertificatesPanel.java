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
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Hashtable;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JComboBox;
import anon.crypto.PKCS12;
import anon.crypto.X509DistinguishedName;
import anon.crypto.X509Extensions;
import anon.crypto.X509SubjectAlternativeName;
import anon.util.Util;
import logging.LogType;
import gui.JAPHelp;
import gui.CountryMapper;
import gui.JAPMessages;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import gui.*;

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

	private CertPanel m_ownCert;
	private CertPanel m_operatorCert;
	private JButton map;
	private JTextField m_txtCity, m_txtState, m_txtLongitude, m_txtLatitude, m_txtOperarorOrgaUnit;
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
		c.weightx = 0;
		c.weighty = 0;
		c.insets = getDefaultInsets();

		m_ownCert = new CertPanel("Own Mix Certificate",
								  "Hint: You have to sent your public test " +
								  "certificate to the operators of your " +
								  "adjacent mixes", (PKCS12)null, CertPanel.CERT_ALGORITHM_DSA);
		m_ownCert.setName("Certificates/OwnCertificate");
		m_ownCert.setCertCreationValidator(new OwnCertCreationValidator());
		m_ownCert.setCertificateView(new MixCertificateView());
		m_ownCert.addChangeListener(this);
		add(m_ownCert, c);

		m_operatorCert = new CertPanel("Operator Certificate",
									   "Hint: You have to sent your public test " +
									   "certificate to the operators of your " +
									   "adjacent mixes",
									   (PKCS12)null, CertPanel.CERT_ALGORITHM_DSA);
		m_operatorCert.setName("Certificates/OperatorCertificate");
		m_operatorCert.setCertCreationValidator(new OwnCertCreationValidator());
		m_operatorCert.addChangeListener(this);
		c.gridx = 1;
		add(m_operatorCert, c);
		m_operatorCert.setEnabled(false); // disabled at the moment

		TitledGridBagPanel panelLocation = new TitledGridBagPanel("Mix Location");
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0;
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

		JTextField operatororg, operatorurl, operatoremail;
		TitledGridBagPanel panelOperator = new TitledGridBagPanel("Operator");
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0;
		c.weighty = 0;
		add(panelOperator, c);

		operatororg = new JTextField(MAX_COLUMN_LENGTH);
		operatororg.setName(XMLPATH_OPERATOR_ORGANISATION);
		operatororg.addFocusListener(this);
		operatororg.setToolTipText(
			"This should contain the operating organisation's or a person's name for private persons.");
		panelOperator.addRow(new JLabel("Organisation"), operatororg);

		m_txtOperarorOrgaUnit = new JTextField(MAX_COLUMN_LENGTH);
		m_txtOperarorOrgaUnit.setName(XMLPATH_OPERATOR_ORGA_UNIT);
		m_txtOperarorOrgaUnit.addFocusListener(this);
		m_txtOperarorOrgaUnit.setToolTipText(
			"The operator's organisational unit.");
		panelOperator.addRow(new JLabel("Orga. Unit"), m_txtOperarorOrgaUnit);

		m_cbxOperatorCountry = new JComboBox(ctrVec);
		m_cbxOperatorCountry.setName(XMLPATH_OPERATOR_COUNTRY);
		m_cbxOperatorCountry.addFocusListener(this);
		m_cbxOperatorCountry.addItemListener(this);
		m_cbxOperatorCountry.setEditable(false);
		panelOperator.addRow(new JLabel("Country"), m_cbxOperatorCountry);

		operatorurl = new JTextField(MAX_COLUMN_LENGTH);
		operatorurl.setName(XMLPATH_OPERATOR_URL);
		operatorurl.addFocusListener(this);
		operatorurl.setToolTipText("This should contain a URL that will lead to more information about the operator including contact information.");
		panelOperator.addRow(new JLabel("URL"), operatorurl);

		operatoremail = new JTextField(MAX_COLUMN_LENGTH);
		operatoremail.setName(XMLPATH_OPERATOR_EMAIL);
		operatoremail.addFocusListener(this);
		operatoremail.setToolTipText(
			"An E-Mail address to which a confirmation message will be sent once the cascade is established.");
		panelOperator.addRow(new JLabel("E-Mail"), operatoremail);

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
			MixConfig.handleError(e, null, LogType.GUI);
		}
	}

	public void paint(Graphics g)
	{
		super.paint(g);
		JAPHelp.getInstance().getContextObj().setContext("index");
	}

	public Vector check()
	{
		Vector errors;
		MixConfiguration mixConf = getConfiguration();
		OwnCertCreationValidator validator = new OwnCertCreationValidator();

		String names[] =
			{
			XMLPATH_OPERATOR_ORGANISATION,
			//	XMLPATH_OPERATOR_URL,
			XMLPATH_OPERATOR_EMAIL
		};

		String messages[][] =
			{
			{
			"Operator Organisation", getName()},
			//{"Operator URL", getName()},
			{
			"Operator E-Mail", getName()},
		};

		validator.isValid();
		errors = validator.getInvalidityMessages();

		for (int i = 0; i < names.length; i++)
		{
			String value = mixConf.getValue(names[i]);
			if (value == null || value.equals(""))
			{
				errors.addElement(JAPMessages.getString(
					"error_blank_field", messages[i]));
			}
		}

		if (this.m_ownCert.getCert() == null)
		{
			errors.addElement(
				"Own Mix Certificate is missing in " + getName() + " panel.");
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
				if (e.getSource() == m_ownCert)
				{
					if (m_ownCert.getCert() != null)
					{
						MixCertificateView certView = (MixCertificateView) m_ownCert.getCertificateView();
						if (certView.getCountry().length() > 0)
						{
							m_cboxCountry.setSelectedItem(certView.getCountryMapper());
						}
						if (certView.getLocalityName().length() > 0)
						{
							m_txtCity.setText(certView.getLocalityName());
						}
						if (certView.getStateOrProvince().length() > 0)
						{
							m_txtState.setText(certView.getStateOrProvince());
						}
						if (certView.getLongitude().length() > 0)
						{
							m_txtLongitude.setText(certView.getLongitude());
						}
						if (certView.getLatitude().length() > 0)
						{
							m_txtLatitude.setText(certView.getLatitude());
						}
						save(this);
					}
					updateDeprecatedMixID();
				}
				enableComponents();
			}
		}
		catch (Exception ex)
		{
			MixConfig.handleError(ex, null, LogType.GUI);
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
			String strValue;
			String strLongitude;
			String strLatitude;
			MixConfiguration conf = getConfiguration();
			String[] message = new String[2];
			message[1] = getName();

			m_invalidity = new Vector();

			strValue = conf.getValue(XMLPATH_LOCATION_CITY);
			if (strValue == null || strValue.trim().equals(""))
			{
				message[0] = "city";
				m_invalidity.addElement(
					JAPMessages.getString("error_blank_field", message));
				valid = false;
			}

			strValue = conf.getValue(XMLPATH_LOCATION_COUNTRY);
			if (strValue == null || strValue.trim().equals(""))
			{
				message[0] = "country";
				m_invalidity.addElement(
					JAPMessages.getString("error_blank_field", message));
				valid = false;
			}

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
					m_invalidity.addElement(JAPMessages.getString(
						"error_illegal_position", getName()));
					valid = false;
				}
			}

			return valid;
		}

		public X509DistinguishedName getSigName()
		{
			Hashtable attributes = new Hashtable();
			attributes.put(X509DistinguishedName.ST_IDENTIFIER,
						   m_txtState.getText());
			attributes.put(X509DistinguishedName.L_IDENTIFIER,
						   m_txtCity.getText());
			attributes.put(X509DistinguishedName.C_IDENTIFIER,
						   ( (CountryMapper) m_cboxCountry.getSelectedItem()).
						   getISOCountryCode());

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

	protected void save(JComboBox a_comboBox)
	{
		if (a_comboBox == m_cboxCountry || a_comboBox == m_cbxOperatorCountry)
		{
			getConfiguration().setValue(a_comboBox.getName(),
										( (CountryMapper) a_comboBox.
										 getSelectedItem()).getISOCountryCode());
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
		MixCertificateView certView = (MixCertificateView)m_ownCert.getCertificateView();

		if (certView.getCountry().length() > 0 &&
			((CountryMapper)m_cboxCountry.getSelectedItem()).equals(certView.getCountryMapper()))
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
}
