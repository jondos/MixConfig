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
package mixconfig.panels;

import gui.MapBox;
import gui.MixConfigTextField;
import gui.TitledGridBagPanel;
import gui.dialog.JAPDialog;
import gui.dialog.LinkedEmailComposer;
import gui.dialog.ValidityContentPane;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import mixconfig.ConfigurationEvent;
import mixconfig.FloatDocument;
import mixconfig.ICertCreationValidator;
import mixconfig.MixCertificateView;
import mixconfig.MixConfig;
import mixconfig.MixConfiguration;
import mixconfig.OperatorCertificateView;
import mixconfig.panels.MixConfigPanel.ToggleButtonModel;
import anon.crypto.AbstractX509Extension;
import anon.crypto.JAPCertificate;
import anon.crypto.PKCS12;
import anon.crypto.Validity;
import anon.crypto.X509AuthorityKeyIdentifier;
import anon.crypto.X509BasicConstraints;
import anon.crypto.X509DistinguishedName;
import anon.crypto.X509Extensions;
import anon.crypto.X509KeyUsage;
import anon.crypto.X509SubjectAlternativeName;
import anon.crypto.X509SubjectKeyIdentifier;
import anon.infoservice.MixInfo;
import anon.util.CountryMapper;
import anon.util.JAPMessages;
import anon.util.Util;

public class OwnCertificatesPanel extends MixConfigPanel implements ActionListener, ChangeListener
{
	/** XML paths*/
	public static final String XMLPATH_LOCATION = "Description/Location";
	public static final String XMLPATH_OPERATOR = "Description/Operator";
	public static final String XMLPATH_LOCATION_CITY = XMLPATH_LOCATION + "/City";
	public static final String XMLPATH_LOCATION_COUNTRY = XMLPATH_LOCATION + "/Country";
	public static final String XMLPATH_LOCATION_STATE = XMLPATH_LOCATION + "/State";
	public static final String XMLPATH_LOCATION_LONGITUDE = XMLPATH_LOCATION + "/Position/Geo/Longitude";
	public static final String XMLPATH_LOCATION_LATITUDE = XMLPATH_LOCATION + "/Position/Geo/Latitude";
	public static final String XMLPATH_OPERATOR_ORGANISATION = XMLPATH_OPERATOR + "/Organisation";
	public static final String XMLPATH_OPERATOR_URL = XMLPATH_OPERATOR + "/URL";
	public static final String XMLPATH_OPERATOR_EMAIL = XMLPATH_OPERATOR + "/EMail";
	public static final String XMLPATH_OPERATOR_COUNTRY = XMLPATH_OPERATOR + "/Country";
	public static final String XMLPATH_OPERATOR_CITY = XMLPATH_OPERATOR + "/City";
	public static final String XMLPATH_OPERATOR_ORGA_UNIT = XMLPATH_OPERATOR + "/OrganisationalUnit";

	/** Messages */
	public static final String MSG_INVALID_POSITION = OwnCertificatesPanel.class.getName() + "_invalidPosition";
	public static final String MSG_INVALID_EMAIL = OwnCertificatesPanel.class.getName() + "_invalidEmail";
	public static final String MSG_INVALID_URL = OwnCertificatesPanel.class.getName() + "_invalidUrl";
	private static final String MSG_CERT_NOT_VERIFYABLE = OwnCertificatesPanel.class.getName() + "_certNotVerifyable";
    
	//private static final String MSG_AUTO_SIGN = OwnCertificatesPanel.class.getName() + "_autoSign";
    //private static final String MSG_CONFIRMATION_TITLE = CertificationTool.class.getName() + "_confirmationTitle";
	//private static final String MSG_CONFIRMATION = CertificationTool.class.getName() + "_confirmationMessage";
	
	/** CertPanels */
	private CertPanel m_ownCert;
	private CertPanel m_operatorCert;
	
	/** GUI components */
	private JButton m_buttonMapBox;
	private JTextField m_txtCity, m_txtState, m_txtLongitude, m_txtLatitude;
	private JTextField m_txtOperatorOrg, m_txtOperatorOrgaUnit, m_txtOperatorUrl, m_txtOperatorEmail;
	private JComboBox m_cboxCountry, m_cbxOperatorCountry;
	private MapBox box;
	
	private JTextField m_txtMixName, m_txtOperatorName;
	private JRadioButton m_radioMixName, m_radioOperatorName;
	private ButtonGroup m_groupCascadeName;
	
	@SuppressWarnings("unchecked")
	public OwnCertificatesPanel(boolean isApplet)
	{
		super("Certificates");
		
		// Get the initial constraints
		GridBagConstraints constraints = getInitialConstraints();

		// Own Mix Certificate
		m_ownCert = new CertPanel("Own Mix Certificate", "Hint: You have to send your public " +
								  "certificate to the operators of adjacent mixes", (PKCS12)null, 
								  CertPanel.CERT_ALGORITHM_DSA, JAPCertificate.CERTIFICATE_TYPE_MIX);
		m_ownCert.setName("Certificates/OwnCertificate");
		//m_ownCert.setChangedCertNotVerifyableMessage(JAPMessages.getString(MSG_CERT_NOT_VERIFYABLE),
				     //new LinkedEmailComposer("Compose Email ..."));									 
				     //XXX: new JAPDialog.LinkedHelpContext(this));
		m_ownCert.setCertCreationValidator(new OwnCertCreationValidator());
		m_ownCert.setCertificateView(new MixCertificateView());
		m_ownCert.addChangeListener(this);
		add(m_ownCert, constraints);

		// Operator Certificate
		m_operatorCert = new CertPanel("Operator Certificate", "Hint: You have to send your public " +
									   "certificate to the operators of adjacent mixes", (PKCS12)null, 
									   CertPanel.CERT_ALGORITHM_DSA, JAPCertificate.CERTIFICATE_TYPE_MIX);
		m_operatorCert.setName("Certificates/OperatorOwnCertificate");
		//m_operatorCert.setChangedCertNotVerifyableMessage(JAPMessages.getString(MSG_CERT_NOT_VERIFYABLE),
		                  //new LinkedEmailComposer("Compose Email ..."));	
		                  //XXX: new JAPDialog.LinkedHelpContext(this));
		m_operatorCert.setCertCreationValidator(new OperatorCertCreationValidator());
		m_operatorCert.setCertificateView(new OperatorCertificateView());
		m_operatorCert.addChangeListener(this);
		// XXX New: m_ownCert is listening for changes on m_operatorCert
		m_operatorCert.addChangeListener(m_ownCert);
		constraints.gridx = 1;
		add(m_operatorCert, constraints);

		// Mix name Panel
		JPanel panelMixName = new JPanel(new GridBagLayout());
		constraints.gridx = 0;
		constraints.gridy = 1;
		add(panelMixName, constraints);
		m_txtMixName = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_txtMixName.setName(GeneralPanel.XMLPATH_GENERAL_MIXNAME);
		m_txtMixName.addFocusListener(this);
		m_radioMixName = new JRadioButton("show in cascade");
		m_radioMixName.setModel(new ToggleButtonModel());
		m_radioMixName.setName(GeneralPanel.XMLPATH_GENERAL_MIXNAME + "/" + 
				MixInfo.XML_ATTRIBUTE_NAME_FOR_CASCADE);
		m_radioMixName.addItemListener(this);
		GridBagConstraints tmpConstraints = new GridBagConstraints();
		tmpConstraints.gridx = 0;
		tmpConstraints.gridy = 0;
		tmpConstraints.insets = new Insets(5, 5, 5, 5);
		panelMixName.add(new JLabel("Mix short name"), tmpConstraints);
		tmpConstraints.gridx++;
		tmpConstraints.weightx = 1.0;
		tmpConstraints.fill = GridBagConstraints.HORIZONTAL;
		panelMixName.add(m_txtMixName, tmpConstraints);
		tmpConstraints.gridx++;
		tmpConstraints.weightx = 0.0;
		tmpConstraints.insets = new Insets(5, 5, 5, 0);
		panelMixName.add(m_radioMixName, tmpConstraints);		
		
		// Operator name Panel
		JPanel panelOperatorName = new JPanel(new GridBagLayout());
		constraints.gridx = 1;
		constraints.gridy = 1;
		add(panelOperatorName, constraints);
		m_txtOperatorName = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_txtOperatorName.setName(GeneralPanel.XMLPATH_GENERAL_OPERATORNAME);
		m_txtOperatorName.addFocusListener(this);
		m_radioOperatorName = new JRadioButton("show in cascade");
		m_radioOperatorName.setModel(new ToggleButtonModel());
		m_radioOperatorName.setName(GeneralPanel.XMLPATH_GENERAL_MIXNAME + "/" + 
				MixInfo.XML_ATTRIBUTE_NAME_FOR_CASCADE);
		m_radioOperatorName.addItemListener(this);
		tmpConstraints.gridx = 0;
		tmpConstraints.gridy = 0;
		tmpConstraints.insets = new Insets(5, 5, 5, 5);
		panelOperatorName.add(new JLabel("Operator short name"), tmpConstraints);
		tmpConstraints.gridx++;
		tmpConstraints.weightx = 1.0;
		tmpConstraints.fill = GridBagConstraints.HORIZONTAL;
		panelOperatorName.add(m_txtOperatorName, tmpConstraints);
		tmpConstraints.gridx++;
		tmpConstraints.weightx = 0.0;
		tmpConstraints.insets = new Insets(5, 5, 5, 0);
		panelOperatorName.add(m_radioOperatorName, tmpConstraints);
		
		m_groupCascadeName = new ButtonGroup();
		m_groupCascadeName.add(m_radioMixName);
		m_groupCascadeName.add(m_radioOperatorName);
		
		
		// Mix Location Panel
		TitledGridBagPanel panelLocation = new TitledGridBagPanel("Mix Location");
		constraints.gridx = 0;
		constraints.gridy = 2;
		add(panelLocation, constraints);

		m_txtCity = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_txtCity.setName(XMLPATH_LOCATION_CITY);
		m_txtCity.addFocusListener(this);
		panelLocation.addRow(new JLabel("City"), m_txtCity, null);

		m_txtState = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_txtState.setName(XMLPATH_LOCATION_STATE);
		m_txtState.addFocusListener(this);
		panelLocation.addRow(new JLabel("State"), m_txtState, null);

		Vector ctrVec = CountryMapper.getLocalisedCountries(MAX_COMBO_BOX_LENGTH, Locale.ENGLISH);
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
		m_txtLongitude.setToolTipText("Longitude in degrees east of Greenwich. (-180.0000 to 180.0000)");

		m_txtLatitude = new JTextField(MAX_COORDINATE_FIELD_LENGTH);
		m_txtLatitude.setName(XMLPATH_LOCATION_LATITUDE);
		m_txtLatitude.addFocusListener(this);
		m_txtLatitude.setDocument(new FloatDocument("-90.0000", "90.0000"));
		m_txtLatitude.setToolTipText(
			"Latitude in degrees. (-90.0000: South Pole, 0: Equator, 90.0000: North Pole)");

		m_buttonMapBox = new JButton("Show on Map");
		m_buttonMapBox.setToolTipText("Opens a window showing a map (using maps.google.com) " +
						   "of the area around the specified coordinates.");
		m_buttonMapBox.addActionListener(this);
		m_buttonMapBox.setActionCommand("Map");
		m_buttonMapBox.setEnabled(!isApplet);

		panelLocation.addRow(new JLabel("Longitude"), m_txtLongitude, m_buttonMapBox);
		panelLocation.addRow(new JLabel("Latitude"), m_txtLatitude, null);

		// Operator Panel
		TitledGridBagPanel panelOperator = new TitledGridBagPanel("Operator");
		// Set the coordinates
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 1;
		constraints.gridy = 2;
		// Add
		add(panelOperator, constraints);

		m_txtOperatorOrg = new MixConfigTextField();
		m_txtOperatorOrg.setName(XMLPATH_OPERATOR_ORGANISATION);
		m_txtOperatorOrg.addFocusListener(this);
		m_txtOperatorOrg.setToolTipText(
			"This should contain the operating organisation's or a person's name for private persons.");
		panelOperator.addRow(new JLabel("Organisation"), m_txtOperatorOrg);

		m_txtOperatorOrgaUnit = new MixConfigTextField();
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

		m_txtOperatorUrl = new MixConfigTextField();
		m_txtOperatorUrl.setName(XMLPATH_OPERATOR_URL);
		m_txtOperatorUrl.addFocusListener(this);
		m_txtOperatorUrl.setToolTipText("This should contain a URL that will lead to more information about the operator including contact information.");
		panelOperator.addRow(new JLabel("URL"), m_txtOperatorUrl);

		m_txtOperatorEmail = new MixConfigTextField();
		m_txtOperatorEmail.setName(XMLPATH_OPERATOR_EMAIL);
		m_txtOperatorEmail.addFocusListener(this);
		m_txtOperatorEmail.setToolTipText(
			"An E-Mail address to which a confirmation message will be sent once the cascade is established.");
		panelOperator.addRow(new JLabel("E-Mail"), m_txtOperatorEmail);

		// Keep the panels in place using a dummy
		constraints.gridy = 3;
		constraints.weighty = 1;
		this.add(new JLabel(), constraints);
	}

	// -------------------- PUBLIC METHODS -----------------------
	
	public void actionPerformed(ActionEvent ae)
	{
		try
		{
			if (ae.getActionCommand().equals("Map"))
			{
				String lat = getConfiguration().getValue(XMLPATH_LOCATION_LATITUDE);
				String lon = getConfiguration().getValue(XMLPATH_LOCATION_LONGITUDE);
				// Check lat and long for null
				if (!(lat == null || lon == null))
				{
					box = new MapBox(MixConfig.getMainWindow(), lat, lon, 8);
					box.setVisible(true);	
				}
				else
				{
					LogHolder.log(LogLevel.WARNING, LogType.GUI, "Please specify latitude AND longitude!");
				}
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

	public Vector<String> check()
	{
		Vector<String> errors, tempErrors;
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
		if (m_ownCert.getCert() == null)
		{
			errors.addElement("Own mix certificate is missing in " + this.getName() + " panel.");
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
				save((CertPanel)e.getSource());
				if(e.getSource() == m_ownCert)
				{
					if (m_ownCert.getCert() != null)
					{
						MixCertificateView certView = (MixCertificateView) m_ownCert.getCertificateView();
						if (certView.getCommonName().length() > 0 && 
							!certView.getCommonName().startsWith("<Mix id="))
						{
							m_txtMixName.setText(certView.getCommonName());
							save(m_txtMixName);
						}
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
						// Sign own certificate with operator certificate
						if((m_operatorCert.getCert() != null) && CertPanel.isAutoSign() &&
						   !m_ownCert.getCert().getX509Certificate().verify(
								   m_operatorCert.getCert().getX509Certificate()))
						{
							//if(JAPDialog.showYesNoDialog(this, JAPMessages.getString(MSG_AUTO_SIGN))) {..}
							signMixCertificate(false);
						}
					}
					updateMixID();
					if (((MixCertificateView) m_ownCert.getCertificateView()).isCA())
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
						
						if (certView.getCommonName().length() > 0)
						{
							m_txtOperatorName.setText(certView.getCommonName());
							save(m_txtOperatorName);
						}
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
						// Sign Own Mix Certificate with Operator Certificate?
						if((m_ownCert.getCert() != null) && CertPanel.isAutoSign() &&
							!m_ownCert.getCert().getX509Certificate().verify(
							 m_operatorCert.getCert().getX509Certificate()))
						{
							//if(JAPDialog.showYesNoDialog(this, JAPMessages.getString(MSG_AUTO_SIGN))) {..}
							signMixCertificate(false);
						}
					}
				}
				enableComponents();
			}
			else if (e instanceof ConfigurationEvent)
			{
				ConfigurationEvent c = (ConfigurationEvent) e;
				if (c.getModifiedXMLPath().startsWith((GeneralPanel.XMLPATH_GENERAL_MIXNAME)))
				{					
					load();
				}
			}
		}
		catch (Exception ex)
		{
			JAPDialog.showErrorDialog(MixConfig.getMainWindow(), null, LogType.GUI, ex);
		}
	}

	/**
	 * @return True if there is an operator certificate set, else return False
	 */
	public boolean hasOperatorCert()
	{
		return (m_operatorCert.getCert() != null);
	}
	
	/**
	 * Sign the 'Own Mix Certificate' with the 'Operator Certificate'
	 * while optionally showing a dialog for choosing a new validity
	 */
	public void signMixCertificate(boolean chooseValidity)
	{
		// Show a dialog depending on chooseValidity
		Validity validity;
		if (chooseValidity)
		{
			JAPDialog dialog = new JAPDialog(this, "Set a new certificate validity", true);
			ValidityContentPane contentPane = new ValidityContentPane(dialog);
			contentPane.updateDialog();
			dialog.pack();
			dialog.setResizable(false);
			dialog.setVisible(true);
			validity = contentPane.getValidity();
		} 
		else
		{
			// Set the validity to one year from now on
			Calendar cal = Calendar.getInstance();
			validity = new Validity(cal, 1);
			LogHolder.log(LogLevel.DEBUG, LogType.CRYPTO, "Setting default validity: " + 
		       validity.getValidFrom() + " -- " + validity.getValidTo());
		}
		LogHolder.log(LogLevel.DEBUG, LogType.CRYPTO, "Signing the mix certificate ..");
		// Get the cert
		PKCS12 operatorCert = (PKCS12)m_operatorCert.getCert();
		Vector<AbstractX509Extension> vecExtensions = new Vector(); 
		X509Extensions extensions;
		vecExtensions.addElement(new X509SubjectKeyIdentifier(
				((PKCS12) m_ownCert.getCert()).getPublicKey()));
		vecExtensions.addElement(new X509AuthorityKeyIdentifier(operatorCert.getPublicKey()));
		vecExtensions.addElement(new X509KeyUsage(
				X509KeyUsage.DIGITAL_SIGNATURE | X509KeyUsage.NON_REPUDIATION));
		vecExtensions.addElement(new X509BasicConstraints(false));
		extensions = new X509Extensions(vecExtensions);
		
		// Sign ..
		((PKCS12) m_ownCert.getCert()).sign(operatorCert, validity, 
				extensions, new BigInteger("0"));
		// Set the new certificate
		m_ownCert.setCert(m_ownCert.getCert().getX509Certificate());
		
		// Show this message for debugging
		//JAPDialog.showMessageDialog(this, JAPMessages.getString(MSG_CONFIRMATION), 
		//		JAPMessages.getString(MSG_CONFIRMATION_TITLE));
		
		if (!m_ownCert.isCertificateVerifyable())
		{
			// Show an additional Link to email composition
			JAPDialog.showMessageDialog(this, JAPMessages.getString(MSG_CERT_NOT_VERIFYABLE), 
					new LinkedEmailComposer("Compose email to JonDos ...", this));
		}
	}
	
	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		// First enable all components to make MixConfigPanel load their data
		enableComponents();

		super.setConfiguration(a_conf);

		// make sure this panel is contained only once in the config's listeners list
		a_conf.removeChangeListener(this);
		a_conf.addChangeListener(this);

		updateMixID();

		enableComponents();
	}

	public Container getHelpExtractionDisplayContext() 
	{
		return null;
	}
	
	// -------------------- PROTECTED METHODS --------------------
	
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
	
	public void save(JRadioButton a_cbx)
	{
		MixConfiguration c = getConfiguration();
		if (a_cbx == m_radioMixName && m_radioMixName.isSelected() && m_radioMixName.isEnabled() && isAutoSaveEnabled())
		{
			c.setAttribute(GeneralPanel.XMLPATH_GENERAL_MIXNAME, MixInfo.XML_ATTRIBUTE_NAME_FOR_CASCADE, 
					MixInfo.NAME_TYPE_MIX);
		}
		else if (a_cbx == m_radioOperatorName && m_radioOperatorName.isSelected() && m_radioOperatorName.isEnabled()
				&& isAutoSaveEnabled())
		{
			c.setAttribute(GeneralPanel.XMLPATH_GENERAL_MIXNAME, MixInfo.XML_ATTRIBUTE_NAME_FOR_CASCADE, 
					MixInfo.NAME_TYPE_OPERATOR);
		}
		else
		{
			super.save(a_cbx);
		}
	}
	
	protected void load(JRadioButton a)
	{
		if (a == m_radioMixName || a == m_radioOperatorName)
		{
			String s = getConfiguration().getValue(a.getName());
			if (s == null)
			{
				a.setEnabled(false);
			}
			else if (s.equals(MixInfo.NAME_TYPE_MIX))
			{
				m_radioMixName.setSelected(true);
				m_radioMixName.setEnabled(true);
				m_radioOperatorName.setEnabled(true);
			}
			else if (s.equals(MixInfo.NAME_TYPE_OPERATOR))
			{
				m_radioOperatorName.setSelected(true);
				m_radioMixName.setEnabled(true);
				m_radioOperatorName.setEnabled(true);
			}
			else
			{
				a.setEnabled(false);
			}
		}
		else
		{
			super.load(a);
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
			catch (IllegalArgumentException iae)
			{
				LogHolder.log(LogLevel.EXCEPTION, LogType.MISC, "Error: " + iae.getMessage());
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

	// -------------------- PRIVATE METHODS ----------------------
	
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
		
		if (certView.getCommonName().length() > 0 &&
			certView.getCommonName().equals(m_txtOperatorName.getText().trim()))
		{
			m_txtOperatorName.setEditable(false);
		}
		else
		{
			m_txtOperatorName.setEditable(true);
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
		if (m_ownCert.getCert() != null && certView.getCommonName().equals(m_txtMixName.getText().trim()) &&
			certView.getCommonName().length() > 0)
		{
			m_txtMixName.setEditable(false);
		}
		else
		{
			m_txtMixName.setEditable(true);
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


	private void updateMixID()
	{
		if (m_ownCert == null || m_ownCert.getCert() == null)
		{
			save("General/MixID", null);
		}
		else
		{
			save("General/MixID", 
					( (PKCS12) m_ownCert.getCert()).getX509Certificate().
					getSubjectKeyIdentifierConcatenated());
		}
	}

	private boolean checkCertificateField(String a_xmlPath, Vector<String> a_vecInvalidityMsg, Object[] msgArgs)
	{
		String strValue = getConfiguration().getValue(a_xmlPath);

		if (strValue == null || strValue.trim().equals(""))
		{
			a_vecInvalidityMsg.addElement(JAPMessages.getString(MSG_ERROR_BLANK_FIELD, msgArgs));
			return false;
		}
		return true;
	}
	
	// -------------------- PRIVATE CLASSES ----------------------
	
	private class OwnCertCreationValidator implements ICertCreationValidator
	{
		private Vector<String> m_invalidity = new Vector<String>();

		public boolean isValid()
		{
			boolean valid = true;
			String strLongitude;
			String strLatitude;
			MixConfiguration conf = getConfiguration();
			String[] message = new String[2];
			message[1] = getName();

			m_invalidity = new Vector<String>();

			message[0] = "Mix short name";
			valid = valid && checkCertificateField(GeneralPanel.XMLPATH_GENERAL_MIXNAME, m_invalidity, message);
			
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
			Hashtable<String,String> attributes = new Hashtable<String,String>();
			attributes.put(X509DistinguishedName.IDENTIFIER_CN, m_txtMixName.getText());
			attributes.put(X509DistinguishedName.IDENTIFIER_ST, m_txtState.getText());
			attributes.put(X509DistinguishedName.IDENTIFIER_L, m_txtCity.getText());
			attributes.put(X509DistinguishedName.IDENTIFIER_C,((CountryMapper)m_cboxCountry.getSelectedItem()).getISOCode());

			return new X509DistinguishedName(attributes);
		}

		public X509Extensions getExtensions()
		{
			Vector<String> coordinates = new Vector<String>();
			Vector<Integer> tags = new Vector<Integer>();
			Vector<AbstractX509Extension> vecExtensions = new Vector<AbstractX509Extension>();
			
			String strLongitude = getConfiguration().getValue(XMLPATH_LOCATION_LONGITUDE);
			String strLatitude = getConfiguration().getValue(XMLPATH_LOCATION_LATITUDE);

			if (strLongitude != null && strLongitude.length() > 0 &&
				strLatitude != null && strLatitude.length() > 0)
			{
				coordinates.addElement(strLongitude);
				coordinates.addElement(strLatitude);
				tags.addElement(X509SubjectAlternativeName.TAG_OTHER);
				tags.addElement(X509SubjectAlternativeName.TAG_OTHER);
				vecExtensions.addElement(
						new X509SubjectAlternativeName(coordinates, tags));
			}
			
			vecExtensions.addElement(new X509KeyUsage(
					X509KeyUsage.DIGITAL_SIGNATURE | X509KeyUsage.NON_REPUDIATION));

			return new X509Extensions(vecExtensions);
		}

		public String getPasswordInfoMessage()
		{
			return "This password has to be entered every time the Mix " +
				"server starts.\nSo if you want to start it automatically " +
				"you shouldn't enter a password.";
		}

		public Vector<String> getInvalidityMessages()
		{
			return m_invalidity;
		}
	}

	private class OperatorCertCreationValidator implements ICertCreationValidator
	{
		private Vector<String> m_invalidity = new Vector<String>();

		/**
		 * @todo check if it is really a valid url
		 * @return boolean
		 */
		public boolean isValid()
		{
			String strValue;
			String[] message = new String[2];
			message[1] = getName();
			m_invalidity = new Vector<String>();

			message[0] = "Operator short name";
			checkCertificateField(GeneralPanel.XMLPATH_GENERAL_OPERATORNAME, m_invalidity, message);
			
			// Organization
			message[0] = "organisation";
			checkCertificateField(XMLPATH_OPERATOR_ORGANISATION, m_invalidity, message);
			
			// Country
			message[0] = "country";
			checkCertificateField(XMLPATH_OPERATOR_COUNTRY, m_invalidity, message);
			
			// URL
			strValue = getConfiguration().getValue(XMLPATH_OPERATOR_URL);
			if (strValue != null && strValue.trim().length() > 0)
			{
				if (new StringTokenizer(strValue).countTokens() > 1)
				{
					m_invalidity.addElement(JAPMessages.getString(MSG_INVALID_URL, getName()));
				}
			}
			
			// Email
			message[0] = "email";			
			if (!X509SubjectAlternativeName.isValidEMail(getConfiguration().getValue(
				 XMLPATH_OPERATOR_EMAIL)))
			{
				m_invalidity.addElement(JAPMessages.getString(MSG_INVALID_EMAIL, getName()));
			}

			return m_invalidity.size() == 0;
		}

		public X509DistinguishedName getSigName()
		{
			Hashtable<String, String> attributes = new Hashtable<String, String>();
			attributes.put(X509DistinguishedName.IDENTIFIER_CN, m_txtOperatorName.getText());
			attributes.put(X509DistinguishedName.IDENTIFIER_OU, m_txtOperatorOrgaUnit.getText());
			attributes.put(X509DistinguishedName.IDENTIFIER_O, m_txtOperatorOrg.getText());
			attributes.put(X509DistinguishedName.IDENTIFIER_E, m_txtOperatorEmail.getText());
			attributes.put(X509DistinguishedName.IDENTIFIER_C,
						   ( (CountryMapper) m_cbxOperatorCountry.getSelectedItem()).getISOCode());
			return new X509DistinguishedName(attributes);
		}

		public X509Extensions getExtensions()
		{
			Vector<AbstractX509Extension> vecExtensions = new Vector<AbstractX509Extension>();
			Vector<String> vecValues = new Vector<String>();
			Vector<Integer> vecTags = new Vector<Integer>();
			String strUrl = getConfiguration().getValue(XMLPATH_OPERATOR_URL);

			if (strUrl != null && strUrl.trim().length() > 0)
			{
				vecValues.addElement(strUrl);
				vecTags.addElement(X509SubjectAlternativeName.TAG_URL);
			}
			vecValues.addElement(getConfiguration().getValue(XMLPATH_OPERATOR_EMAIL));
			vecTags.addElement(X509SubjectAlternativeName.TAG_EMAIL);

			vecExtensions.addElement(new X509SubjectAlternativeName(vecValues, vecTags));
			vecExtensions.addElement(new X509BasicConstraints(0));
			vecExtensions.addElement(new X509KeyUsage(
					X509KeyUsage.DIGITAL_SIGNATURE | X509KeyUsage.NON_REPUDIATION |
					X509KeyUsage.KEY_CERT_SIGN));
			return new X509Extensions(vecExtensions);
		}

		public String getPasswordInfoMessage()
		{
			return "Your operator certificate should be protected by a strong password.";
		}

		public Vector<String> getInvalidityMessages()
		{
			return m_invalidity;
		}
	}
}