/*
Copyright (c) 2008 The JAP-Team, JonDos GmbH

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
       this list of conditions and the following disclaimer in the documentation and/or
       other materials provided with the distribution.
    * Neither the name of the University of Technology Dresden, Germany, nor the name of
       the JonDos GmbH, nor the names of their contributors may be used to endorse or
       promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package mixconfig.panels;

import gui.ButtonConstants;
import gui.DateListener;
import gui.DatePanel;
import gui.GUIUtils;
import gui.MixConfigTextField;
import gui.TermsAndConditionsDialog;
import gui.TitledGridBagPanel;
import gui.dialog.DialogContentPane;
import gui.dialog.JAPDialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SignatureException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.InflaterInputStream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import mixconfig.ConfigurationEvent;
import mixconfig.MixConfig;
import mixconfig.MixConfiguration;
import mixconfig.infoservice.InfoServiceData;
import mixconfig.infoservice.InfoServiceTableModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import anon.crypto.JAPCertificate;
import anon.infoservice.Database;
import anon.infoservice.OperatorAddress;
import anon.infoservice.ServiceOperator;
import anon.terms.TCComposite;
import anon.terms.TermsAndConditions;

import anon.terms.TermsAndConditions.Translation;
import anon.terms.template.TermsAndConditionsTemplate;
import anon.util.Base64;
import anon.util.IXMLEncodable;
import anon.util.JAPMessages;
import anon.util.LanguageMapper;
import anon.util.XMLParseException;
import anon.util.XMLUtil;

/**
 * This panel is used by mix operators to enter customized 
 * information to the standard terms and conditions.
 * 
 * @author renner, simon
 */
public class TermsAndConditionsPanel extends MixConfigPanel implements ActionListener, DateListener, ChangeListener, MouseListener {
	
	private static final String MSG_TC_CAPTION = TermsAndConditionsPanel.class.getName() + "_caption";
	private static final String MSG_TITLE_OP_GENERAL = TermsAndConditionsPanel.class.getName() + "_titleopGeneral";
	private static final String MSG_TITLE_OP_WITH_LANG = TermsAndConditionsPanel.class.getName() + "_titleopWithLang";
	private static final String MSG_TITLE_URLS = TermsAndConditionsPanel.class.getName() + "_titleurls";
	private static final String MSG_ADD_TRANSLATIONS_DIALOG = TermsAndConditionsPanel.class.getName() + "_addTranslationsDialog";
	private static final String MSG_TEMPLATE_MISMATCH = TermsAndConditionsPanel.class.getName() + "_templateMismatch";
	private static final String MSG_INFOSERVICE = TermsAndConditionsPanel.class.getName() + "_infoservice";
	static final String MSG_PREVIEW = TermsAndConditionsPanel.class.getName() + "_preview";
	private static final String MSG_URL = TermsAndConditionsPanel.class.getName() + "_url";
	private static final String MSG_PATH = TermsAndConditionsPanel.class.getName() + "_path";
	private static final String MSG_FILE = TermsAndConditionsPanel.class.getName() + "_file";
	//static final String MSG_LOAD = TermsAndConditionsPanel.class.getName() + "_load";
	static final String MSG_RESET = TermsAndConditionsPanel.class.getName() + "_reset";
	private static final String MSG_EXPORT = TermsAndConditionsPanel.class.getName() + "_export";
	private static final String MSG_DEFAULT_TRANSLATION = TermsAndConditionsPanel.class.getName() + "_defaultTranslation";
	private static final String MSG_AVAIL_LANG_LABEL = TermsAndConditionsPanel.class.getName() + "_availableLanguagesLabel";
	private static final String MSG_VALID_LABEL = TermsAndConditionsPanel.class.getName() + "_validLabel";
	static final String MSG_TEMPLATE_LABEL = TermsAndConditionsPanel.class.getName() + "_templateLabel";
	private static final String MSG_LANGUAGE_LABEL = TermsAndConditionsPanel.class.getName() + "_languageLabel";
	static final String MSG_TRANSLATION_LABEL = TermsAndConditionsPanel.class.getName() + "_translationLabel";
	private static final String MSG_STREET_LABEL = TermsAndConditionsPanel.class.getName() + "_streetLabel";
	private static final String MSG_ZIP_LABEL = TermsAndConditionsPanel.class.getName() + "_zipLabel";
	private static final String MSG_CITY_LABEL = TermsAndConditionsPanel.class.getName() + "_cityLabel";
	private static final String MSG_FAX_LABEL = TermsAndConditionsPanel.class.getName() + "_faxLabel";
	private static final String MSG_ADDITIONAL_INFO_LABEL = TermsAndConditionsPanel.class.getName() + "_additionalInfoLabel";
	private static final String MSG_VAT_LABEL = TermsAndConditionsPanel.class.getName() + "_vatLabel";
	private static final String MSG_VENUE_LABEL = TermsAndConditionsPanel.class.getName() + "_venueLabel";
	private static final String MSG_PRIVACY_POLICY_LABEL = TermsAndConditionsPanel.class.getName() + "_privacyPolicyURLLabel";
	private static final String MSG_LEGAL_OPINIONS_LABEL = TermsAndConditionsPanel.class.getName() + "_legalOpinionsURLLabel";
	private static final String MSG_OP_AGREEMENT_LABEL = TermsAndConditionsPanel.class.getName() + "_operationalAgreementURLLabel";
	private static final String MSG_PHOLDER_NONE_AVAIL = TermsAndConditionsPanel.class.getName() + "_pholderNoneAvail";
	private static final String MSG_PHOLDER_ADD_TRANSLATION = TermsAndConditionsPanel.class.getName() + "_pholderAddTranslation";
	private static final String MSG_PHOLDER_UPDATE = TermsAndConditionsPanel.class.getName() + "_pholderUpdate";
	private static final String MSG_FILE_EXISTS = TermsAndConditionsPanel.class.getName() + "_fileExists";
	private static final String MSG_SAVE_FILE_ERROR = TermsAndConditionsPanel.class.getName() + "_saveFileError";
	private static final String MSG_LOAD_FILE_ERROR = TermsAndConditionsPanel.class.getName() + "_loadFileError";
	private static final String ACTION_CMD_ADD_TRANSLATION = "addTranslation";
	static final String MSG_SECTION = TermsAndConditionsPanel.class.getName() + "_section";
	static final String MSG_PARAGRAPH = TermsAndConditionsPanel.class.getName() + "_paragraph";
	static final String MSG_CONTENT = TermsAndConditionsPanel.class.getName() + "_content";
	private static final String MSG_EDIT_CONTENT = TermsAndConditionsPanel.class.getName() + "_editContent";
	private static final String MSG_IMPORT_WHOLE = TermsAndConditionsPanel.class.getName() + "_importWhole";
	private static final String MSG_TEMPLATE_NOT_LOADED = TermsAndConditionsPanel.class.getName() + "_templateNotLoaded";
	private static final String MSG_TEMPLATE_NOT_LOADED_TITLE = TermsAndConditionsPanel.class.getName() + "_templateNotLoadedTitle";
	private static final String MSG_TEMPLATE_NOT_DEFINED = TermsAndConditionsPanel.class.getName() + "_templateNotDefined";
	private static final String MSG_TEMPLATE_NOT_DEFINED_TITLE = TermsAndConditionsPanel.class.getName() + "_templateNotDefinedTitle";
	private static final String MSG_WARNING_TEMPLATE_LANGUAGE = TermsAndConditionsPanel.class.getName() + "_warningTemplateLanguage";
	private static final String MSG_RECOMMEND_MIX_PATH = TermsAndConditionsPanel.class.getName() + "_recommendMixPath";
	
	
	public static final String XML_ELEMENT_TEMPLATES = "Templates"; 
	public static final String XML_ELEMENT_TEMPLATE = "Template";
	public static final String XML_ELEMENT_TC_OPTIONS = "TermsAndConditionsOptions";
	public static final String XML_ELEMENT_TC_TRANSLATION_IMPORTS = "TCTranslationImports";
	
	/** Terms and conditions paths */
	public static final String XMLPATH_TERMS_OPTIONS = XML_ELEMENT_TC_OPTIONS;
	
	public final static String PRIVACY_POLICY_TAG ="PrivacyPolicyUrl";
	public final static String LEGAL_OPINIONS_TAG ="LegalOpinionsUrl";
	public final static String OPERATIONAL_AGREEMENT_TAG ="OperationalAgreementUrl";
	
	public final static String TEMPLATE_EXPORT_ENCODING = "UTF-8";
	
	/** ComboBox and Model */
	private DefaultComboBoxModel m_modelReferenceIDs = new DefaultComboBoxModel();
	private JComboBox m_cbReferenceIDs = new JComboBox(m_modelReferenceIDs);
	private JComboBox m_cbTranslations = new JComboBox();
	private boolean m_bIgnoreTemplateWarningMessages = false;
	
	private JButton m_btnPreview;
	private JButton m_btnAddTranslation;
	private JButton m_btnDefaultTranslation;
	private JButton m_btnDeleteTranslation;
	private JLabel m_lblWarningWrongTemplate;
	private JLabel m_lblNoticeTemplatePath;
	//private JButton m_btnExportTranslation;
	
	private JButton m_btnInfoServiceTemplate;
	private JButton m_btnFileTemplate;
	private JButton m_btnExportTemplate;
	private JButton m_btnDeleteTemplate;
	
	private JButton m_btnImportWhole;
	private JButton m_btnContent;
	
	/** Date panel + button */
	private DatePanel m_pnlDate;
	
	private JTextField m_tf_templateURL;
	
	TitledGridBagPanel panelOperatorLeft;
	/** TextFields */
	private JTextField m_tf_general_additionalInfo;
	private JTextField m_tf_general_Street;
	private JTextField m_tf_general_Post;
	private JTextField m_tf_general_City;
	private JTextField m_tf_general_VAT;
	private JTextField m_tf_general_Fax;
	private JTextField m_tf_general_Venue;
	
	TitledGridBagPanel panelOperatorRight;
	/** TextFields */
	private JTextField m_tf_lang_additionalInfo;
	private JTextField m_tf_lang_Street;
	private JTextField m_tf_lang_Post;
	private JTextField m_tf_lang_City;
	private JTextField m_tf_lang_Fax;
	private JTextField m_tf_lang_Venue;
	
	private JTextField m_tfUrlPP;
	private JTextField m_tfUrlLO;
	private JTextField m_tfUrlOA;
	
	private JTextField m_tf_templateReferenceId;
	
	private PropertyToComponentMapping<JTextField>[] generalAddressMappings;
	private PropertyToComponentMapping<JTextField>[] translationAddressMappings;
	private PropertyToComponentMapping<JTextField>[] translationUrlMappings;
	
	private TermsAndConditions operatorTCs = null;
	private OperatorAddress generalAddress = null;
	
	private String selectedLanguage = "";
	private File lastOpened = null;

	private static Vector<LanguageMapper> LANGUAGE_CHOICE = 
		LanguageMapper.getLocalisedLanguages();
	
	/** The DateFormat that we use here */
	DateFormat m_dateFormatter = new SimpleDateFormat(TermsAndConditions.DATE_FORMAT);
	
	private boolean savingEnabled = true;
	
	private static TermsAndConditionsPanel panelSingleton = null;
	
	public static TermsAndConditionsPanel get()
	{
		if(panelSingleton == null)
		{
			panelSingleton = new TermsAndConditionsPanel();
		}
		return panelSingleton;
	}
	
	/** Constructor */
	private TermsAndConditionsPanel()
	{
		// Initial stuff
		super(JAPMessages.getString(MSG_TC_CAPTION));
		GridBagConstraints constraints = getInitialConstraints();
	
		
		// Reference terms & date -------------------------
		
		TitledGridBagPanel panelTranslation = new TitledGridBagPanel(JAPMessages.getString(MSG_TRANSLATION_LABEL));
		TitledGridBagPanel panelTemplate = new TitledGridBagPanel(JAPMessages.getString(MSG_TEMPLATE_LABEL));
		
		//reduce the insets to get all the components in there.
		constraints.insets = new Insets(3,3,3,3);
		constraints.gridwidth = 1;
		constraints.gridx++;
		add(panelTranslation, constraints);
		constraints.gridx--;
		add(panelTemplate, constraints);
		
		// Do not call updateSerials() in the constructor
		//m_cbReferenceIDs.setName(XMLPATH_TERMS);
		//m_cbTranslations.setName(XMLPATH_TERMS_TRANSLATION);
		
		m_tf_templateReferenceId = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_templateReferenceId.addFocusListener(this);
		
		//m_tf_templateURL = new MixConfigTextField(20);
		m_tf_templateURL = new MixConfigTextField();
		m_tf_templateURL.addFocusListener(this);
		// Add the update button
		m_btnInfoServiceTemplate = new JButton(JAPMessages.getString(MSG_INFOSERVICE));
		m_btnInfoServiceTemplate.addActionListener(this);
		
		//Button for previewing the T&Cs with the current settings
		m_btnPreview = new JButton(JAPMessages.getString(MSG_PREVIEW));
		m_btnPreview.addActionListener(this);
		
		//Button for editing translations
		m_btnAddTranslation = GUIUtils.createButton(ButtonConstants.ADD);//new JButton(JAPMessages.getString(MSG_ADD));
		m_btnAddTranslation.addActionListener(this);
		m_btnDefaultTranslation = new JButton(JAPMessages.getString(MSG_DEFAULT_TRANSLATION));
		m_btnDefaultTranslation.addActionListener(this);
		m_btnDeleteTranslation = GUIUtils.createButton(ButtonConstants.DELETE);
		m_btnDeleteTranslation.addActionListener(this);
		
		//m_btnExportTranslation = new JButton(JAPMessages.getString(MSG_EXPORT));
		//m_btnExportTranslation.addActionListener(this);
		//Button for loading a template locally
		m_btnFileTemplate = new JButton(JAPMessages.getString(MSG_FILE));
		m_btnFileTemplate.addActionListener(this);
		
		m_btnExportTemplate = new JButton(JAPMessages.getString(MSG_EXPORT));
		m_btnExportTemplate.addActionListener(this);
		m_btnDeleteTemplate = GUIUtils.createButton(ButtonConstants.DELETE);
		m_btnDeleteTemplate.addActionListener(this);
		m_btnContent = new JButton(JAPMessages.getString(MSG_EDIT_CONTENT));
		m_btnContent.addActionListener(this);
		
		m_btnImportWhole = new JButton(JAPMessages.getString(MSG_IMPORT_WHOLE));
		m_btnImportWhole.addActionListener(this);
		
		JPanel translationButtonPanel = new JPanel();
		translationButtonPanel.add(m_btnAddTranslation);
		translationButtonPanel.add(m_btnDefaultTranslation);
		translationButtonPanel.add(m_btnPreview);
	
		JPanel templateButtonPanel = new JPanel();
		templateButtonPanel.add(m_btnInfoServiceTemplate);
		templateButtonPanel.add(m_btnFileTemplate);
		templateButtonPanel.add(m_btnExportTemplate);
		
		JPanel templateURLPanel = new JPanel(new GridBagLayout());
		GridBagConstraints contr = new GridBagConstraints();
		contr.fill = GridBagConstraints.HORIZONTAL;
		contr.gridx = 0;
		contr.gridy = 0;
		contr.insets = new Insets(0,0,0,5);
		templateURLPanel.add(GUIUtils.createLabel(MSG_URL, MSG_PATH), contr);
		contr.gridx++;
		contr.insets = new Insets(0,0,0,0);
		contr.weightx = 1.0;
		templateURLPanel.add(m_tf_templateURL, contr);
		contr.gridx++;
		contr.weightx = 0.0;
		contr.insets = new Insets(0,5,0,0);
		m_lblNoticeTemplatePath = new JLabel(GUIUtils.loadImageIcon("info.png"));
		m_lblNoticeTemplatePath.addMouseListener(this);
		m_lblNoticeTemplatePath.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		templateURLPanel.add(m_lblNoticeTemplatePath, contr);
		
		// The current date
		Date now = new Date(System.currentTimeMillis());
		m_pnlDate = new DatePanel(now);
		m_pnlDate.addDateListener(this);
		
		panelTranslation.addRow(GUIUtils.createLabel(MSG_LANGUAGE_LABEL), m_cbTranslations, m_btnDeleteTranslation);
		panelTranslation.addRow(new Component[]{GUIUtils.createLabel(MSG_VALID_LABEL), m_pnlDate}, new int[]{1,2}); //, m_btnToday);
		panelTranslation.addRow(new Component[]{translationButtonPanel}, new int[]{3});		
		
		m_lblWarningWrongTemplate = new JLabel(GUIUtils.loadImageIcon("warning.gif"));
		m_lblWarningWrongTemplate.addMouseListener(this);
		m_lblWarningWrongTemplate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panelTemplate.addRow(m_cbReferenceIDs,m_lblWarningWrongTemplate, m_btnDeleteTemplate);
		panelTemplate.addRow(new Component[]{templateURLPanel}, new int[2]);
		panelTemplate.addRow(new Component[]{templateButtonPanel}, new int[2]);
		
		// Operator Location ------------------------------
		
		panelOperatorLeft = new TitledGridBagPanel(JAPMessages.getString(MSG_TITLE_OP_GENERAL));
		constraints.gridy++;
		constraints.gridwidth = 1;
		add(panelOperatorLeft, constraints);
		
		// Street
		m_tf_general_Street = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_general_Street.addFocusListener(this);
		panelOperatorLeft.addRow(GUIUtils.createLabel(MSG_STREET_LABEL), m_tf_general_Street, null);

		// Postal code
		m_tf_general_Post = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_general_Post.addFocusListener(this);

		// City
		m_tf_general_City = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_general_City.addFocusListener(this);
		panelOperatorLeft.addRow(GUIUtils.createLabel(MSG_ZIP_LABEL, MSG_CITY_LABEL), m_tf_general_Post, m_tf_general_City);
		
		// additional info
		m_tf_general_additionalInfo = new MixConfigTextField(MAX_COLUMN_LENGTH);	
		m_tf_general_additionalInfo.addFocusListener(this);
		panelOperatorLeft.addRow(GUIUtils.createLabel(MSG_ADDITIONAL_INFO_LABEL), m_tf_general_additionalInfo, null);
		
		// Fax
		m_tf_general_Fax = new MixConfigTextField(MAX_COLUMN_LENGTH);	
		m_tf_general_Fax.addFocusListener(this);
		panelOperatorLeft.addRow(GUIUtils.createLabel(MSG_FAX_LABEL), m_tf_general_Fax, null);
		
		// Venue
		m_tf_general_Venue = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_general_Venue.addFocusListener(this);
		panelOperatorLeft.addRow(GUIUtils.createLabel(MSG_VENUE_LABEL), m_tf_general_Venue, null);
		
		// VAT
		m_tf_general_VAT = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_general_VAT.addFocusListener(this);
		panelOperatorLeft.addRow(GUIUtils.createLabel(MSG_VAT_LABEL), m_tf_general_VAT, null);
		
		// Operator information ---------------------------
		
		panelOperatorRight = new TitledGridBagPanel(JAPMessages.getString(MSG_TITLE_OP_WITH_LANG)+" "+selectedLanguage);
		constraints.gridx++;
		constraints.fill = GridBagConstraints.BOTH;
		add(panelOperatorRight, constraints);
		// Reset fill to HORIZONTAL
		constraints.fill = GridBagConstraints.HORIZONTAL;
		
		// Street
		m_tf_lang_Street = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_lang_Street.addFocusListener(this);
		panelOperatorRight.addRow(GUIUtils.createLabel(MSG_STREET_LABEL), m_tf_lang_Street, null);

		// Postal code
		m_tf_lang_Post = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_lang_Post.addFocusListener(this);

		// City
		m_tf_lang_City = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_lang_City.addFocusListener(this);
		panelOperatorRight.addRow(GUIUtils.createLabel(MSG_ZIP_LABEL, MSG_CITY_LABEL), m_tf_lang_Post, m_tf_lang_City);
		
		m_tf_lang_additionalInfo = new MixConfigTextField(MAX_COLUMN_LENGTH);	
		m_tf_lang_additionalInfo.addFocusListener(this);
		panelOperatorRight.addRow(GUIUtils.createLabel(MSG_ADDITIONAL_INFO_LABEL), m_tf_lang_additionalInfo, null);
		
		// Fax
		m_tf_lang_Fax = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_lang_Fax.addFocusListener(this);
		panelOperatorRight.addRow(GUIUtils.createLabel(MSG_FAX_LABEL), m_tf_lang_Fax, null);
		
		// Venue
		m_tf_lang_Venue = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_lang_Venue.addFocusListener(this);
		panelOperatorRight.addRow(GUIUtils.createLabel(MSG_VENUE_LABEL), m_tf_lang_Venue, null);	
		
		panelOperatorRight.addRow(new Component[]{m_btnContent /*, m_btnImportWhole */ }, 
				new int[]{1, GridBagConstraints.REMAINDER});
		// Translations -------------------------------------------
		
		
		TitledGridBagPanel panelURLs = new TitledGridBagPanel(JAPMessages.getString(MSG_TITLE_URLS));
		
		constraints.gridx--;
		constraints.gridy++;
		constraints.gridwidth = 2;
		add(panelURLs, constraints);
		
		// URL privacy policy
		m_tfUrlPP = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tfUrlPP.addFocusListener(this);
		panelURLs.addRow(GUIUtils.createLabel(MSG_PRIVACY_POLICY_LABEL), m_tfUrlPP, null);
		
		// URL legal opinions
		m_tfUrlLO = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tfUrlLO.addFocusListener(this);
		panelURLs.addRow(GUIUtils.createLabel(MSG_LEGAL_OPINIONS_LABEL), m_tfUrlLO, null);
		
		// URL operational agreement
		m_tfUrlOA = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tfUrlOA.addFocusListener(this);
		panelURLs.addRow(GUIUtils.createLabel(MSG_OP_AGREEMENT_LABEL), m_tfUrlOA, null);
		
		panelURLs.setVisible(false);
		
		constraints.gridx=0;
		constraints.gridy++;
		constraints.gridwidth = 2;
		
		JPanel universalButtonPanel = new JPanel();
		universalButtonPanel.add(m_btnImportWhole);
		add(universalButtonPanel, constraints);
		
		// Keep the panels in place -----------------------
		
		constraints.gridy++;
		constraints.weighty = 1;
		this.add(new JLabel(), constraints);
		
		PropertyToComponentMapping<JTextField> privacyPolicyMapping = null;
		PropertyToComponentMapping<JTextField> operationalAgreementsMapping = null;
		PropertyToComponentMapping<JTextField> legalOpinionsMapping = null;
		
		BeanInfo info;
		try 
		{
			info = Introspector.getBeanInfo(Translation.class);
			PropertyDescriptor[] pds = info.getPropertyDescriptors();
		
			for (int i = 0; i < pds.length; i++)
			{
				if(pds[i].getName().equals(Translation.PROPERTY_NAME_PRIVACY_POLICY))
				{
					privacyPolicyMapping = 
						new PropertyToComponentMapping<JTextField>(pds[i], m_tfUrlPP);
				}
				else if(pds[i].getName().equals(Translation.PROPERTY_NAME_OPERATIONAL_AGREEMENT))
				{
					operationalAgreementsMapping = 
						new PropertyToComponentMapping<JTextField>(pds[i], m_tfUrlOA);
				}
				else if(pds[i].getName().equals(Translation.PROPERTY_NAME_LEGAL_OPINIONS))
				{
					legalOpinionsMapping = 
						new PropertyToComponentMapping<JTextField>(pds[i], m_tfUrlLO);
				}
			}
		} 
		catch (IntrospectionException e)
		{
			//if this happens, it is a bug
			e.printStackTrace();
		}
	
		/* these mappings between property descriptors and compenents
		 * allows to use reflection for loading and saving the translation properties 
		 */
		translationUrlMappings = new PropertyToComponentMapping[]
        {
			//url mappings
			privacyPolicyMapping,
			operationalAgreementsMapping,
			legalOpinionsMapping,
        };
		
		generalAddressMappings = new PropertyToComponentMapping[]
   		{ 
   			//OperatorAddress property mappings
   			new PropertyToComponentMapping<JTextField>
   				(OperatorAddressIntrospection.getDescriptor(OperatorAddress.PROPERTY_NAME_CITY), m_tf_general_City),
   			new PropertyToComponentMapping<JTextField>
   				(OperatorAddressIntrospection.getDescriptor(OperatorAddress.PROPERTY_NAME_STREET), m_tf_general_Street),
   			new PropertyToComponentMapping<JTextField>
   				(OperatorAddressIntrospection.getDescriptor(OperatorAddress.PROPERTY_NAME_POSTALCODE), m_tf_general_Post),
   			new PropertyToComponentMapping<JTextField>
   				(OperatorAddressIntrospection.getDescriptor(OperatorAddress.PROPERTY_NAME_FAX), m_tf_general_Fax),
   			new PropertyToComponentMapping<JTextField>
   				(OperatorAddressIntrospection.getDescriptor(OperatorAddress.PROPERTY_NAME_ADDITIONALINFO), m_tf_general_additionalInfo),
   			new PropertyToComponentMapping<JTextField>
   				(OperatorAddressIntrospection.getDescriptor(OperatorAddress.PROPERTY_NAME_VAT), m_tf_general_VAT),
   			new PropertyToComponentMapping<JTextField>
   				(OperatorAddressIntrospection.getDescriptor(OperatorAddress.PROPERTY_NAME_VENUE), m_tf_general_Venue)
   		};
		
		translationAddressMappings = new PropertyToComponentMapping[]
		{ 
			//OperatorAddress property mappings
			new PropertyToComponentMapping<JTextField>
				(OperatorAddressIntrospection.getDescriptor(OperatorAddress.PROPERTY_NAME_CITY), m_tf_lang_City),
			new PropertyToComponentMapping<JTextField>
				(OperatorAddressIntrospection.getDescriptor(OperatorAddress.PROPERTY_NAME_STREET), m_tf_lang_Street),
			new PropertyToComponentMapping<JTextField>
				(OperatorAddressIntrospection.getDescriptor(OperatorAddress.PROPERTY_NAME_POSTALCODE), m_tf_lang_Post),
			new PropertyToComponentMapping<JTextField>
				(OperatorAddressIntrospection.getDescriptor(OperatorAddress.PROPERTY_NAME_FAX), m_tf_lang_Fax),
			new PropertyToComponentMapping<JTextField>
				(OperatorAddressIntrospection.getDescriptor(OperatorAddress.PROPERTY_NAME_ADDITIONALINFO), m_tf_lang_additionalInfo),
			/*new PropertyToComponentMapping<JTextField>
				(OperatorAddress.getDescriptor(OperatorAddress.PROPERTY_NAME_VAT), m_tf_lang_VAT),*/
			new PropertyToComponentMapping<JTextField>
				(OperatorAddressIntrospection.getDescriptor(OperatorAddress.PROPERTY_NAME_VENUE), m_tf_lang_Venue)
		};
		
		m_cbReferenceIDs.addItemListener(this);
		m_cbTranslations.addItemListener(this);
	}
	
	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		// Enable all components first to load the data
		enableComponents();
		// Set the configuration
		super.setConfiguration(a_conf);
		// Make sure this panel is contained only once in the config's listeners list
		a_conf.removeChangeListener(this);
		a_conf.addChangeListener(this);
		enableComponents();
	}
	
	// -------------------- PUBLIC METHODS -----------------------
	
	/** Implement DateListener */
	public void dateChanged()
	{
		saveAndLog();
	}
	
	
	
	/** Implement ItemListener */
	public void itemStateChanged(ItemEvent a_event)
	{
		if( (a_event.getStateChange() == ItemEvent.SELECTED) &&
			(a_event.getItem() instanceof ComboBoxPlaceHolder)	)
		{
			//ignore events fired by selection of placeholders.
			LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Placeholder selected: ignoring event.");
			m_lblWarningWrongTemplate.setVisible(false);
			return;
		}
		// Catch events from the ReferenceIDs ComboBox
		if (a_event.getSource() == m_cbReferenceIDs)
		{
			if(a_event.getStateChange() == ItemEvent.SELECTED)
			{
				TemplateReferenceID refId = getSelectedTemplateReferenceID();
				Translation currentTrans = null;
				Translation otherTrans = null;
				
				if (!m_bIgnoreTemplateWarningMessages && !m_bInitialisingTranslations)
				{
					if (currentTrans != null)
					{
						currentTrans.setTemplateReferenceId((refId != null) ? refId.getReferenceID() : null);
					}
				
					if( (refId != null) && refId.isComplete() )
					{
						Enumeration<Translation> allTranslations =
							operatorTCs.getAllTranslations();
						Vector<TemplateReferenceID> matchingTemplates = null;
						while (allTranslations.hasMoreElements()) 
						{
							otherTrans = allTranslations.nextElement();
							if (otherTrans != currentTrans)
							{
								matchingTemplates = 
									getMatchingTemplateReferenceIDs(refId.getType(), otherTrans.getLocale(), refId.getDate());
								if(matchingTemplates.size() > 0)
								{
									otherTrans.setTemplateReferenceId(matchingTemplates.firstElement().getReferenceID());
								}
							}
						}
					}
				}

				saveAndLog();
				
				if(refId != null && refId.getSource() == null)
				{
					LogHolder.log(LogLevel.WARNING, LogType.MISC, "RefId '"+refId.getReferenceID()+"' has no source specified.");
				}

				m_tf_templateURL.setText( (refId != null) ? 
						((refId.getSource() != null) ? refId.getSource().toString() : "") : "");
			}
		}
		else if (a_event.getSource() == m_cbTranslations && !m_bInitialisingTranslations)
		{
			Translation selectedTranslation = getSelectedTranslation();
			TemplateReferenceID refId;
			if( a_event.getStateChange() == ItemEvent.SELECTED)
			{
				if(selectedTranslation != null)
				{
					loadTranslation(selectedTranslation);
					
					selectedLanguage = selectedTranslation.toString();
					//if the template of the selected language was not found:
					//try to set it to the currently selected template
					
					m_bIgnoreTemplateWarningMessages = true;
					if(!selectTemplateReferenceID(selectedTranslation.getTemplateReferenceId()) )
					{
						refId = getSelectedTemplateReferenceID();
						if(refId != null)
						{
							LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Set refId of translation "+selectedTranslation+" to "+refId.getReferenceID());
							selectedTranslation.setTemplateReferenceId(refId.getReferenceID());
						}
						else
						{
							LogHolder.log(LogLevel.WARNING, LogType.MISC, "Template '"+selectedTranslation.getTemplateReferenceId()+"' was not found");
						}
					}					
					m_bIgnoreTemplateWarningMessages = false;
					
					((TitledBorder)panelOperatorRight.getBorder()).setTitle(
							JAPMessages.getString(MSG_TITLE_OP_WITH_LANG)+" "+selectedLanguage);
					repaint();
				}
			}
		}
		else super.itemStateChanged(a_event);
		//check if a transformation for TC export or preview is possible
		//and set button accordingly.
		
		if(a_event.getStateChange() == ItemEvent.SELECTED)
		{
			enableComponents();
		}
	}
	
	/**
	 * Listen to ChangeEvents
	 */
	public void stateChanged(ChangeEvent event) 
	{
		if (event instanceof ConfigurationEvent)
		{
			ConfigurationEvent configEvent = (ConfigurationEvent)event;
			String sPath = configEvent.getModifiedXMLPath();
			if (sPath.startsWith("Certificates/OperatorOwnCertificate"))
			{
				if (getServiceOperator() != null)
				{
					if (!isEnabled())
					{
						setEnabled(true);
						try 
						{
							load();
						} 
						catch (IOException e) 
						{
							LogHolder.log(LogLevel.WARNING, LogType.MISC, "T&C panel could not be loaded.");
						}
					}
				}
				else
				{
					setEnabled(false);
				}
			}
		}	
	}
	
	public void mouseClicked(MouseEvent a_event)
	{
		if (a_event.getSource() == m_lblWarningWrongTemplate)
		{
			JAPDialog.showWarningDialog(this, JAPMessages.getString(MSG_WARNING_TEMPLATE_LANGUAGE));
		}
		else if (a_event.getSource() == m_lblNoticeTemplatePath)
		{
			JAPDialog.showMessageDialog(this, JAPMessages.getString(MSG_RECOMMEND_MIX_PATH));
		}
	}
	
	public void mouseEntered(MouseEvent a_event)
	{
		
	}
	
	public void mouseExited(MouseEvent a_event)
	{
		
	}
	
	public void mousePressed(MouseEvent a_event)
	{
		
	}
	
	public void mouseReleased(MouseEvent a_event)
	{
		
	}
	
	/** Implement ActionListener */
	public void actionPerformed(ActionEvent ae) 
	{
		if ( ae.getSource() == m_btnInfoServiceTemplate )
		{
			// Update the vector of reference IDs
			updateFromInfoService();
			// Get the attribute 'referenceId'
			//String refID = getConfiguration().getAttributeValue(m_cbReferenceIDs.getName(), "referenceId");
			Translation selectedTranslation = getSelectedTranslation();
			if ( (selectedTranslation != null) && 
					selectTemplateReferenceID(selectedTranslation.getTemplateReferenceId()) )
			{	
				LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Model contains refID");
			}
			else
			{
				// Set the ComboBox to the first item
				m_cbReferenceIDs.setSelectedItem(m_modelReferenceIDs.getElementAt(0));
			}
		}
		else if( ae.getSource() == m_btnAddTranslation )
		{
			actionAddTranslation();
		}
		else if( ae.getSource() == m_btnDeleteTranslation )
		{
			actionDeleteTranslation();
		}
		else if( ae.getSource() == m_btnDefaultTranslation )
		{
			actionDefaultTranslation();
		}
		else if (ae.getSource() == m_btnPreview )
		{
			actionPreviewTranslation();
		}
		else if (ae.getSource() == m_btnFileTemplate )
		{
			actionLoadTemplateFromFile();
		}
		else if (ae.getSource() == m_btnExportTemplate )
		{
			actionExportTemplate();
		}
		else if (ae.getSource() == m_btnDeleteTemplate )
		{
			actionDeleteTemplate();
		}
		else if (ae.getSource() == m_btnContent )
		{
			showContentDialog();
		}
		else if (ae.getSource() == m_btnImportWhole )
		{
			actionImportWhole();
		}
		saveAndLog();
		enableComponents();
	}
	
	public void focusGained(FocusEvent e){}
	
	public void focusLost(FocusEvent e)
	{
		if(e.getSource() == m_tf_templateURL)
		{
			actionSetTemplatePath();
		}
		saveAndLog();
		enableComponents();
	}
	
	/** Implement abstract methods from MixConfigPanel */
	public synchronized Vector<String> check() 
	{
		
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Check invoked.");
		Vector<String> messages = new Vector<String>();
		if(operatorTCs == null)
		{
			messages.add("T&C container is null. This must not happen and seems to be a bug.");
			return messages;
		}
		
		if (!operatorTCs.hasTranslations())
		{
			if (XMLUtil.getFirstChildByName(getConfiguration().getDocument().getDocumentElement(), 
					XML_ELEMENT_TC_OPTIONS) != null )
			{
				messages.add("Warning: no translations defined. " +
					"Please specify at least one default translation. The mix won't start without.");
			}
		}
		else if(!operatorTCs.hasDefaultTranslation())
		{
			messages.add("Warning: no default translation defined. " +
					"The mix won't start unless a default translation is defined.");
		}
		
		Enumeration<Translation> enumTranslations = operatorTCs.getAllTranslations();
		Translation translation;
		TemplateReferenceID templateRefID;
		Hashtable<String, TemplateReferenceID> hashRefIDs = 
			new Hashtable<String, TemplateReferenceID>();
		TermsAndConditionsTemplate template;
		String commonType = null;
		boolean bInvalidTemplatePath = false;
		boolean bLanguageWarning = false;
		boolean bTypeWarning = false;
		Vector<TemplateReferenceID> allRefIds = getAllTemplateReferenceIds();
		
		for (TemplateReferenceID templateReferenceID : allRefIds) 
		{
			hashRefIDs.put(templateReferenceID.getReferenceID(), templateReferenceID);
		}
		
		while (enumTranslations.hasMoreElements())
		{
			translation = enumTranslations.nextElement();
			templateRefID = hashRefIDs.get(translation.getTemplateReferenceId());
			if (templateRefID != null)
			{
				if(operatorTCs.getDate().before(templateRefID.getDate()))
				{
					messages.add("Error: The valid date of your terms  is earlier than the date of the corresponding template '"+
							templateRefID+"'.");
				}
				template = (TermsAndConditionsTemplate)
					Database.getInstance(TermsAndConditionsTemplate.class).getEntryById(templateRefID.getReferenceID());
				
				if (!checkTemplatePath(templateRefID))
				{
					bInvalidTemplatePath = true;
				}
			}
			else
			{
				template = null;
				bInvalidTemplatePath = true;
			}
			if (template != null)
			{
				if (!template.getLanguage().equals(translation.getLocale()))
				{
					bLanguageWarning = true;
				}
				if (commonType != null && !template.getType().equals(commonType))
				{
					bTypeWarning = true;
				}
				else
				{
					commonType = template.getType();
				}
			}
		}
		if (bInvalidTemplatePath)
		{
			messages.add("You should set the template path(s) for your terms and conditions to a file path on your mix server. Do not forget to export your template(s) to this path.");
		}
		
		if (bLanguageWarning)
		{
			messages.add(JAPMessages.getString(MSG_WARNING_TEMPLATE_LANGUAGE));
		}
		
		if (bTypeWarning)
		{
			messages.add("Your terms and condition translations use different template types. Warning: This may lead to juristically different terms for different languages!");
		}
		
		if (operatorTCs.getDate() == null)
		{
			messages.add("T&C container has no valid date set. This must not happen and seems to be a bug.");
		}
		
		Enumeration<Translation>
			allTrans = operatorTCs.getAllTranslations();
		Translation currentTrans = null;
		//Method currentTransPropertyGetter = null;
		//Object currentValue = null;
		String currentTemplateRefId = null;
		while (allTrans.hasMoreElements())
		{
			 currentTrans = allTrans.nextElement();
			 currentTemplateRefId = currentTrans.getTemplateReferenceId();
			 if( (currentTemplateRefId == null) || 
				(currentTemplateRefId.equals("")) )
			 {
				 messages.add("Translation "+currentTrans+" has not specified a template and cannot be rendered.");
			 }
			 
			 /*
			 for (PropertyToComponentMapping<JTextField> transMapping : translationUrlMappings) 
			 {
				 try 
				 {
					 currentTransPropertyGetter =
						 transMapping.getPropertyDescriptor().getReadMethod();
				
					 currentValue = currentTransPropertyGetter.invoke(currentTrans);
					 if((currentValue == null) || 
						(currentValue.toString().equals("")))
					 {
						 messages.add("Translation "+currentTrans+" has not set the relevant property '"+
								 transMapping.getPropertyDescriptor().getName()+"'");
					 }
				} 
				//all impossible
				catch (IllegalArgumentException e) {} 
				catch (IllegalAccessException e) {} 
				catch (InvocationTargetException e) {}
			 }*/
		}
		for (String message : messages) 
		{
			LogHolder.log(LogLevel.WARNING, LogType.MISC, "Found Inconsistency: "+message);
		}
		return messages;
	}
	
	/** Implement IHelpContext */
	public String getHelpContext() 
	{
		return TermsAndConditionsPanel.class.getName();
	}

	/** Implement IHelpContext */
	public Container getHelpExtractionDisplayContext() 
	{
		return null;
	}
	
	public void load() throws IOException
	{
		load((Document) null);
	}
	
	private void load(Document configDoc) throws IOException
	{
		savingEnabled = false;
		try 
		{
			m_lblWarningWrongTemplate.setVisible(false);
			//m_lblNoticeTemplatePath.setVisible(false);
			
			initTermsAndConditionsSettings(configDoc);
			//general address setting must be loaded immediately after initTermsAndConditionsSettings
			initGeneralAddress();
			initTemplates(configDoc);
			initTranslations();
			loadDate();
			// Initially save the date if it is not yet there
			
			//enableComponents();
		} 
		catch (NotLoadedException e) 
		{
			LogHolder.log(LogLevel.WARNING, LogType.MISC, "T&C panel could not be loaded.");
		}
		savingEnabled = true;
	}
	
	public void save() throws IOException
	{
		if(!savingEnabled)
		{
			return;
		}
		//better create the whole TermsAndConditions DOM subtree
		//and replace it with the old one it to the MixConfiguration.
		Document configDoc = getConfiguration().getDocument();
		Element tcOptionsRoot = configDoc.createElement(XML_ELEMENT_TC_OPTIONS);
		
		if(operatorTCs != null)
		{
			//save the valid date ...
			saveDate();
			// ... and the translation values ...
			Translation selectedTranslation = getSelectedTranslation();
			if(selectedTranslation != null)
			{
				saveTranslation(selectedTranslation);
				
			}
			
			// ... the start to create the XML output
			Element operatorTCsRoot = operatorTCs.createXMLOutput(configDoc);
			if(operatorTCsRoot == null)
			{
				operatorTCsRoot = operatorTCs.createTCRoot(configDoc);
			}
			if(generalAddress != null)
			{
				//update the general address settings.
				saveGeneralAddress();
				Enumeration<Element> generalAddressElements = generalAddress.getAddressAsNodeList(configDoc);
				if(generalAddressElements.hasMoreElements())
				{
					Element tcTransImports = configDoc.createElement(XML_ELEMENT_TC_TRANSLATION_IMPORTS);
					Element tcTransImportsAddress = configDoc.createElement(ServiceOperator.XML_ELEMENT_NAME);
					tcTransImports.appendChild(tcTransImportsAddress);
					operatorTCsRoot.appendChild(tcTransImports);
					do 
					{
						tcTransImportsAddress.appendChild(generalAddressElements.nextElement());
					}
					while(generalAddressElements.hasMoreElements());
				}
			}
			if(operatorTCsRoot.hasChildNodes())
			{
				tcOptionsRoot.appendChild(operatorTCsRoot);
			}
		}
		Element templatesRoot = getSpecifiedTemplatesElement(configDoc);
		if(templatesRoot != null)
		{
			tcOptionsRoot.appendChild(templatesRoot);
		}
		
		Element mixConfRoot = configDoc.getDocumentElement();
		NodeList nl = mixConfRoot.getElementsByTagName(XML_ELEMENT_TC_OPTIONS);
		//if there are already children with the same name: replace the first one.
		if(nl.getLength() > 0)
		{
			if(tcOptionsRoot.hasChildNodes())
			{
				mixConfRoot.replaceChild(tcOptionsRoot, nl.item(0));
			}
			else
			{
				mixConfRoot.removeChild(nl.item(0));
			}
		}
		else if(tcOptionsRoot.hasChildNodes())
		{
			mixConfRoot.appendChild(tcOptionsRoot);
		}
		
		
		//no component specific saving necessary.
		//save(this);
	}
	
	// -------------------- PROTECTED METHODS --------------------
	
	/**
	 * TODO: Add general support for DatePanels to the load() method in MixConfigPanel.java?
	 * @param a_container
	 */
	protected void load(Container a_container) throws IOException
	{
		//super.load(a_container);	
	}
	
	/**
	 * Overwrite load() for JComboBoxes
	 */
	protected void load(JComboBox a_comboBox)
	{
		if (a_comboBox == m_cbReferenceIDs)
		{
			//will be explicitly done by initTemplates
		}
		else if (a_comboBox == m_cbTranslations)
		{
			//will be explicitly done by initTranslations
		}
		else super.load(a_comboBox);
	}
	
	/**
	 * TODO: Add support for DatePanels to the save() method in MixConfigPanel?
	 * 
	 * @param a_container
	 */
	protected void save(Container a_container) throws IOException
	{
		super.save(a_container);
	}
	
	
	/**
	 * Overwrite save() for JComboBoxes
	 */
	protected void save(JComboBox a_comboBox) 
	{
		//do nothing here: saving is not component dependent
		//only prevent the super class from handling this case. This would destroy the config.
		if ( !(a_comboBox == m_cbReferenceIDs) && 
			 !(a_comboBox == m_cbTranslations) )
		{
			super.save(a_comboBox);
		}
	}
	
	protected void enableComponents() 
	{
		//Buttons
		m_btnDeleteTranslation.setEnabled(isDeleteTranslationPossible());
		m_btnDefaultTranslation.setEnabled(isDefaultLanguagePossible());
		//cannot be shown because only becomes visible when on the preview dialog
		
		//m_btnExportTranslation.setEnabled(isPreviewTranslationPossible());
		m_btnDeleteTemplate.setEnabled(isDeleteTemplatePossible());
		m_btnExportTemplate.setEnabled(isExportTemplatePossible());
	
		//other components
		panelOperatorRight.setEnabled(isATranslationLoaded());
		//this enable check must take place after checking the right oprator panel, 
		//otherwise the button can be falsely reactivated.
		m_btnContent.setEnabled(isContentManagementPossible());
		m_btnPreview.setEnabled(isPreviewPossible());
		
		m_tf_templateURL.setEnabled(isDeleteTemplatePossible());
		if(m_cbReferenceIDs.getItemCount() == 0)
		{
			m_cbReferenceIDs.addItem(
					ComboBoxPlaceHolder.createPlaceHolder(MSG_PHOLDER_UPDATE));
			m_tf_templateURL.setText("");
		}
		
		if(m_cbTranslations.getItemCount() == 0)
		{
			m_cbTranslations.addItem(
					ComboBoxPlaceHolder.createPlaceHolder(MSG_PHOLDER_ADD_TRANSLATION));
		}
		
		m_btnAddTranslation.setEnabled(isAddTranslationPossible());
		
		TemplateReferenceID refId = getSelectedTemplateReferenceID();
		m_lblNoticeTemplatePath.setVisible(!checkTemplatePath(refId));
		m_lblWarningWrongTemplate.setVisible(isWrongTemplateSet());
		
		repaint();
	}
	
	
	// -------------------- PRIVATE METHODS ----------------------
	
	//---------- dialog display methods ------------
	
	private DialogResult<LanguageMapper> showAddTranslationsDialog(String selected)
	{	
		final JAPDialog translationsDialog = 
			new JAPDialog(MixConfig.getMainWindow(), JAPMessages.getString(MSG_ADD_TRANSLATIONS_DIALOG), true);
		final JComboBox languageChoiceBox = new JComboBox();
		final DialogResult<LanguageMapper> result = new DialogResult<LanguageMapper>();
		
		translationsDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		translationsDialog.getContentPane().setLayout(new BorderLayout());
		
		JPanel langPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		
		langPanel.setLayout(new FlowLayout());
		buttonPanel.setLayout(new FlowLayout());
		
		JLabel langLabel = GUIUtils.createLabel(MSG_AVAIL_LANG_LABEL);
		
		final JButton addButton = GUIUtils.createButton(ButtonConstants.ADD);
		addButton.setActionCommand(ACTION_CMD_ADD_TRANSLATION);
		final JButton cancelButton = 
			new JButton(JAPMessages.getString(DialogContentPane.MSG_CANCEL));
		
		ItemListener itemListener = new ItemListener()
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					Object selectedItem = languageChoiceBox.getSelectedItem();
					if( (selectedItem != null) && (selectedItem instanceof LanguageMapper) )
					{
						String selectedLang = ((LanguageMapper) selectedItem).getISOCode().toLowerCase();
						addButton.setEnabled(!operatorTCs.hasTranslation(selectedLang));
					}
					else
					{
						addButton.setEnabled(false);
					}
				}
			}
		};
		
		ActionListener buttonListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				result.reset();
				if( ( (e.getSource() != cancelButton) ) &&
					  (languageChoiceBox.getSelectedItem() instanceof LanguageMapper) )
				{
					result.setCommand(e.getActionCommand());
					result.setResult(
							((LanguageMapper)languageChoiceBox.getSelectedItem()) );
				}
				translationsDialog.setVisible(false);
			}	
		};
		addButton.addActionListener(buttonListener);
		cancelButton.addActionListener(buttonListener);
		
		languageChoiceBox.addItemListener(itemListener);
		
		for (LanguageMapper langMapper : LANGUAGE_CHOICE) 
		{
			languageChoiceBox.addItem(langMapper);
			if (selected != null && langMapper.getISOCode().equals(selected))
			{
				languageChoiceBox.setSelectedItem(langMapper);
			}
		}
		
		
		langPanel.add(langLabel);
		langPanel.add(languageChoiceBox);
		
		buttonPanel.add(cancelButton);
		buttonPanel.add(addButton);
		translationsDialog.getContentPane().add(langPanel, BorderLayout.CENTER);
		translationsDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		translationsDialog.pack();
		translationsDialog.setVisible(true);
		return ((result.getCommand() != null) && result.getResult() != null) ? result : null;
	}
	
	private boolean checkTemplatePath(TemplateReferenceID templateRefID)
	{
		if (templateRefID == null)
		{
			return true;
		}
		
		if (templateRefID.getSource() == null)
		{
			return false;
		}
		
		URI templateURI;
		try
		{
			templateURI = new URI(templateRefID.getSource().toString());
		} 
		catch (URISyntaxException e)
		{
			LogHolder.log(LogLevel.EXCEPTION, LogType.FILE, e);
			return false;
		}
			
		if (!templateRefID.getSource().getProtocol().equals("file") || new File(templateURI).exists())
		{
			return false;
		}
		
		return true;
	}
	
	private void showContentDialog()
	{
		TermsAndConditionsTemplate template = getSelectedTemplate();
		Translation translation = getSelectedTranslation();
		TCComposite translationSections =
			TermsAndConditionsContentDialog.showContentDialog(this, template, translation);
		if(translationSections != null)
		{
			translation.setSections(translationSections);
		}
	}
	
	private TermsAndConditionsTemplate getSelectedTemplate()
	{
		TemplateReferenceID refId = getSelectedTemplateReferenceID();
		TermsAndConditionsTemplate template = (TermsAndConditionsTemplate) 
			(refId != null ? Database.getInstance(TermsAndConditionsTemplate.class).getEntryById(refId.getReferenceID()) : null);
		return template;
	}
	
	// -- action methods - invoked if a corresponding action event occurs --
	private void actionAddTranslation()
	{
		String newLangCode = null;
	
		TemplateReferenceID selectedRefId = getSelectedTemplateReferenceID();
		if((selectedRefId == null) || !selectedRefId.isComplete())
		{
			//Should not be possible. The button that invokes this action is not enabled then.
			return;
		}
		
		//Ask for InfoService template update if the selected template is not loaded.
		if(getSelectedTemplate() == null)
		{
			if( JAPDialog.showYesNoDialog(this, 
					JAPMessages.getString(MSG_TEMPLATE_NOT_LOADED, selectedRefId),
					JAPMessages.getString(MSG_TEMPLATE_NOT_LOADED_TITLE)) )
			{
				updateFromInfoService();
			}
		}
		
		//look for a language for which we havn't defined a translation yet but 
		//corresponding templates are available (at least their reference IDs)
		Vector<TemplateReferenceID> templateRefIDs = getAllTemplateReferenceIds();
		for (TemplateReferenceID templateReferenceID : templateRefIDs)
		{
			if(!operatorTCs.hasTranslation(templateReferenceID.getLangCode()))
			{
				newLangCode = templateReferenceID.getLangCode();
				break;
			}
		}
		
		DialogResult<LanguageMapper> result = showAddTranslationsDialog(newLangCode);
		if( (result != null) && 
			(result.getResult() != null) &&
			(result.getCommand() != null) )
		{
			LanguageMapper langMapper = result.getResult();
			String cmd = result.getCommand();
			Translation t = null;
			
			if(cmd.equals(ACTION_CMD_ADD_TRANSLATION))
			{
				String langCode = langMapper.getISOCode().toLowerCase();
				removeFirstPlaceHolder(m_cbTranslations);
				if(operatorTCs.hasTranslation(langCode))
				{
					LogHolder.log(LogLevel.WARNING, LogType.MISC, "Translation "+langMapper+" already exists");
				}
				else
				{
					LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Add translation "+langMapper);
					t = operatorTCs.initializeEmptyTranslation(langCode);
					
					//Perhaps there is a template which matches the new translation better than the selected one
					//(at least the language should match)
					boolean refIdSet = false;
					//perform three searches to find an appropriate match
					for(int i = 0; i < 3; i++)
					{
						templateRefIDs = 
							getMatchingTemplateReferenceIDs(
									(i > 1) ? selectedRefId.getType() : null, //after the second search: don't care about the type ...
									langCode, //... only care about the language
									(i > 0) ? selectedRefId.getDate() : null); //after first search: don't care about the date
					
						if(templateRefIDs.size() > 0)
						{
							String matchingRefId = templateRefIDs.firstElement().getReferenceID();
							TermsAndConditionsTemplate matchingTemplate = (TermsAndConditionsTemplate)
								Database.getInstance(TermsAndConditionsTemplate.class).getEntryById(matchingRefId);
							//Ask again for InfoService template update, if the best matching template isn't already loaded.
							if(matchingTemplate == null)
							{
								if( JAPDialog.showYesNoDialog(this, 
										JAPMessages.getString(MSG_TEMPLATE_NOT_LOADED, matchingRefId),
										JAPMessages.getString(MSG_TEMPLATE_NOT_LOADED_TITLE)) )
								{
									updateFromInfoService();
								}
							}
							t.setTemplateReferenceId(matchingRefId);
							refIdSet = true;
							break;
						}
					}
					if(!refIdSet)
					{
						Locale loc = new Locale(langCode);
						//Not even a template with the same language is available. This is a reason for warning.
						JAPDialog.showWarningDialog(this, 
								JAPMessages.getString(MSG_TEMPLATE_NOT_DEFINED, loc.getDisplayLanguage()),
								JAPMessages.getString(MSG_TEMPLATE_NOT_DEFINED_TITLE));
					}
					
					updateObject(m_cbTranslations, t, true);
					//set as default translation if there isn't already one defined.
					if (!operatorTCs.hasDefaultTranslation() || langCode.equals("en"))
					{
						t.setDefaultTranslation(true);
					}
				}
			}
		}
	}
	
	private void actionPreviewTranslation()
	{
		Translation previewTranslation = getPreviewTranslation(); 
		if(previewTranslation != null)
		{
			TermsAndConditionsDialog.previewTranslation(this, previewTranslation);
		}
	}
	
	private void actionLoadTemplateFromFile()
	{
		//Load template from file
		JFileChooser fc = (lastOpened != null) ? new JFileChooser(lastOpened) : new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(true);
		int clicked = fc.showDialog(this, JAPMessages.getString(ButtonConstants.LOAD));
		switch ( clicked )
		{
			case JFileChooser.APPROVE_OPTION:
			{
				boolean somethingAdded = false;
				TermsAndConditionsTemplate currentTemplate = null;
				File[] fs = fc.getSelectedFiles();
				for (File file : fs) 
				{
					try 
					{
						currentTemplate = new TermsAndConditionsTemplate(file);
						
						if (!currentTemplate.isVerified())
						{
							if (!JAPDialog.showYesNoDialog(this, "The template " + currentTemplate.getId() + 
									" does not have a valid signature! It will not be accepted by the users. Load it nevertheless?"))
							{
								continue;
							}
						}
						Database.getInstance(TermsAndConditionsTemplate.class).update(currentTemplate);
						if(!somethingAdded)
						{
							somethingAdded = true;
							removeFirstPlaceHolder(m_cbReferenceIDs);
						}
						if(!isTemplateReferenceIdInComboBox(currentTemplate.getId()))
						{
							updateObject(m_cbReferenceIDs, 
									new TemplateReferenceID(currentTemplate.getId(), file.toURI().toURL()), false );
						}
						lastOpened = file.getParentFile();
					} 
					catch (XMLParseException e) 
					{
						JAPDialog.showErrorDialog(this, 
								JAPMessages.getString(MSG_LOAD_FILE_ERROR), 
								LogType.MISC, e);
					} 
					catch (IOException e) 
					{
						JAPDialog.showErrorDialog(this, 
								JAPMessages.getString(MSG_LOAD_FILE_ERROR), 
								LogType.MISC, e);
					}
				}
				break;
			}
			case JFileChooser.CANCEL_OPTION:
			{
				break;
			}
			case JFileChooser.ERROR_OPTION:
			{
				break;
			}
		}
	}
	
	private void actionImportWhole()
	{
		//Load template from file
		JFileChooser fc = (lastOpened != null) ? new JFileChooser(lastOpened) : new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		int clicked = fc.showDialog(this, JAPMessages.getString(ButtonConstants.LOAD));
		switch ( clicked )
		{
			case JFileChooser.APPROVE_OPTION:
			{
				File file = fc.getSelectedFile();
				try 
				{
					InputStreamReader reader = 
						new InputStreamReader(new FileInputStream(file), "UTF-8");
					
					Document doc = XMLUtil.readXMLDocument(reader);
					if((doc == null) || (doc.getDocumentElement() == null) )
						throw new XMLParseException("No document found");
					
					NodeList nl = doc.getElementsByTagName(XML_ELEMENT_TC_OPTIONS);
					if( nl.getLength() < 1)
						throw new XMLParseException("No T&C Options element found");
					
					Element tcOptionsRoot = (Element) nl.item(0);
					nl = tcOptionsRoot.getElementsByTagName(TermsAndConditions.XML_ELEMENT_NAME);
					if( nl.getLength() < 1)
						throw new XMLParseException("No T&C element found");
					load(doc);
				} 
				catch (XMLParseException e) 
				{
					JAPDialog.showErrorDialog(this, 
							JAPMessages.getString(MSG_LOAD_FILE_ERROR), 
							LogType.MISC, e);
				} 
				catch (IOException e) 
				{
					JAPDialog.showErrorDialog(this, 
							JAPMessages.getString(MSG_LOAD_FILE_ERROR), 
							LogType.MISC, e);
				}
				break;
			}
			case JFileChooser.CANCEL_OPTION:
			{
				break;
			}
			case JFileChooser.ERROR_OPTION:
			{
				break;
			}
		}
	}
	
	private void actionExportTemplate()
	{
		TemplateReferenceID refId = getSelectedTemplateReferenceID();
		TermsAndConditionsTemplate template = (TermsAndConditionsTemplate) 
			(refId != null ? Database.getInstance(TermsAndConditionsTemplate.class).getEntryById(refId.getReferenceID()) : null);
		if(template != null)
		{
			String suggestedFileName = refId.getReferenceID()+".xml";
			JFileChooser fc = (lastOpened != null) ? new JFileChooser(lastOpened) : new JFileChooser();
			File suggestedFile = new File(fc.getCurrentDirectory()+File.separator+suggestedFileName);
			fc.setSelectedFile(suggestedFile);
			int clicked = fc.showSaveDialog(this);
			switch ( clicked )
			{
				case JFileChooser.APPROVE_OPTION:
				{
					File selectedFile = fc.getSelectedFile();
					boolean confirmed = true;
					if(selectedFile.exists())
					{
						confirmed = 
							JAPDialog.showConfirmDialog(this, 
								JAPMessages.getString(MSG_FILE_EXISTS, selectedFile.getName()),
								JAPDialog.OPTION_TYPE_YES_NO, 
								JAPDialog.MESSAGE_TYPE_QUESTION) == JAPDialog.RETURN_VALUE_YES;
					}
					if(confirmed)
					{
						try 
						{
							//If no encoding is explicitly set: use UTF-8 encoding
							String exportEncoding = template.getDocument().getTextContent();
							if(exportEncoding == null)
							{
								exportEncoding = TEMPLATE_EXPORT_ENCODING;
							}
							OutputStreamWriter exportWriter = 
								new OutputStreamWriter(new FileOutputStream(selectedFile), exportEncoding);
							XMLUtil.write(template.getDocument(), exportWriter);
							exportWriter.flush();
							exportWriter.close();
						} 
						catch (IOException e) 
						{
							JAPDialog.showErrorDialog(this, 
									JAPMessages.getString(MSG_SAVE_FILE_ERROR, selectedFile.getName()), 
									LogType.MISC, e);
						}
					}
					break;
				}
				case JFileChooser.CANCEL_OPTION:
				{
					break;
				}
				case JFileChooser.ERROR_OPTION:
				{
					break;
				}
			}
		}
	}
	
	private void actionDeleteTemplate()
	{
		TemplateReferenceID refId = getSelectedTemplateReferenceID();
		if(refId  != null)
		{
			m_cbReferenceIDs.removeItem(refId);
			Database.getInstance(TermsAndConditionsTemplate.class).remove(refId.getReferenceID());
		}
	}
	
	private void actionSetTemplatePath()
	{
		TemplateReferenceID refId = getSelectedTemplateReferenceID();
		if( (refId  != null) &&
			(m_tf_templateURL.getText() != null) &&
			!m_tf_templateURL.getText().equals(""))
		{
			TermsAndConditionsTemplate template = null;
			try 
			{
				URL newTemplateUrl = new URL(m_tf_templateURL.getText());
				
				try 
				{
					template = getTemplateFromURL(newTemplateUrl);
					if(template != null)
					{
						//if the template loaded from  the specified URL does not match the corresponding referenceId
						if(!template.getId().equals(refId.getReferenceID()))
						{
							JAPDialog.showErrorDialog(this, 
									JAPMessages.getString(MSG_TEMPLATE_MISMATCH, 
											new Object[]{template.getId(), refId.getReferenceID()}) ,LogType.MISC);
							m_tf_templateURL.setText(refId.getSource().toString());
							return;
						}
						Database.getInstance(TermsAndConditionsTemplate.class).update(template);
					}
				}
				//ignore: the template does not need to refer to a valid location
				catch (IOException e) {} 
				catch (XMLParseException e) 
				{
					LogHolder.log(LogLevel.WARNING, LogType.MISC, "The template at '"+refId.getSource()+"' cannot be parsed.");
				}
				refId.setSource(newTemplateUrl);
			} 
			catch (MalformedURLException e) 
			{
				m_tf_templateURL.setText(refId.getSource().toString());
			}
		}
	}
	
	private void actionDeleteTranslation()
	{
		Translation selectedTranslation = getSelectedTranslation();
		if(selectedTranslation != null)
		{
			LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Remove translation: "+selectedTranslation);
			operatorTCs.removeTranslation(selectedTranslation.getLocale());
			clearTranslationFields();
			m_cbTranslations.removeItem(selectedTranslation);
		}
	}
	
	private void actionDefaultTranslation()
	{
		Translation oldDefault = operatorTCs.getDefaultTranslation();
		Translation newDefault = getSelectedTranslation();
		if( (newDefault != null) && 
			(newDefault != oldDefault) )
		{
			if(oldDefault != null)
			{
				oldDefault.setDefaultTranslation(false);
			}
			LogHolder.log(LogLevel.DEBUG, LogType.MISC, "set translation: "+newDefault+" to default");
			newDefault.setDefaultTranslation(true);
		}
	}
	
	// --- private load and save functions ---
	
	private void loadDate()
	{
		// Try to parse the date from the configuration
		Date date = (operatorTCs != null) ? operatorTCs.getDate() : new Date(System.currentTimeMillis());
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Loading date " + date);
		//a null-pointer for date must be a bug.
		m_pnlDate.setDate(date);
	}
	
	private void loadTranslation(Translation translation)
	{
		if(translation != null)
		{
			OperatorAddress addr = translation.getOperatorAddress();
			Method currentGetter = null;
			Object currentValue = null;
			
			for (int i = 0; i < translationUrlMappings.length; i++) 
			{
				currentGetter = translationUrlMappings[i].getPropertyDescriptor().getReadMethod();
				
				try 
				{
					currentValue = currentGetter.invoke(translation, (Object[]) null);
				} 
				catch (Exception e) 
				{
					LogHolder.log(LogLevel.EXCEPTION, LogType.MISC, e);
				} 
				
				if (translationUrlMappings[i].getComponent() != null)
				{
					translationUrlMappings[i].getComponent().setText(
							(currentValue != null) ? currentValue.toString() : "");
				}
			}
			
			for (int i = 0; i < translationAddressMappings.length; i++) 
			{
				currentGetter = translationAddressMappings[i].getPropertyDescriptor().getReadMethod();
				try 
				{
					currentValue = (addr != null) ? currentGetter.invoke(addr, (Object[]) null) : null;
				} 
				catch (Exception e) 
				{
					LogHolder.log(LogLevel.EXCEPTION, LogType.MISC, e);
				} 
				
				translationAddressMappings[i].getComponent().setText(
						(currentValue != null) ? currentValue.toString() : "");
			}
		}
	}
	
	private ServiceOperator getServiceOperator()
	{
		if(getConfiguration() == null)
		{
			//Actually this case must never happen. Since this method is 'abused' by
			//the stateChanged we have to handle this condition.
			return null;
		}
		String opCertFromConfig = getConfiguration().getValue("Certificates/OperatorOwnCertificate/X509Certificate");
		JAPCertificate operatorCertificate = (opCertFromConfig != null) ? 
				JAPCertificate.getInstance(Base64.decode(opCertFromConfig)) : null;
		return (operatorCertificate != null) ? new ServiceOperator(operatorCertificate) : null;
	}
	
	private void initTermsAndConditionsSettings(Document configDoc) throws IOException, NotLoadedException
	{
		Document rootConfigDoc = (configDoc != null) ? configDoc : getConfiguration().getDocument();
		NodeList nl = rootConfigDoc.getElementsByTagName(TermsAndConditions.XML_ELEMENT_NAME);
		//String opCertFromConfig = getConfiguration().getValue("Certificates/OperatorOwnCertificate/X509Certificate");
		ServiceOperator operator = getServiceOperator();
		
		/* Why? This prevents new configurations from being created or loaded correctly!!
		if(configDoc == null)
		{
			if((operator != null) && 
			   (operatorTCs != null) && 
			   (operatorTCs.getOperator() != null) &&
			   operatorTCs.getOperator().equals(operator) )
		   {
				LogHolder.log(LogLevel.INFO, LogType.MISC, "Don't reload operator T&Cs.");
				return;
		   }
		}*/
		
		operatorTCs = null;
		if(operator == null)
		{
			LogHolder.log(LogLevel.ERR, LogType.MISC, "No operator is specified. "+
					"Cannot initiate any terms and conditions without operator settings.");
			setEnabled(false);
			throw new NotLoadedException();
		}
		else
		{
			try 
			{
				if(nl.getLength() > 0)
				{
					Element root = (Element) nl.item(0);
					operatorTCs = new TermsAndConditions(root, operator, false);
					
					//... and now descend into the translation imports to get the general address informations
					String [] parentNames = 
						new String[]{XML_ELEMENT_TC_TRANSLATION_IMPORTS, 
									ServiceOperator.XML_ELEMENT_NAME};
					for(int i = 0; (i < parentNames.length) && (root != null); i++)
					{
						nl = root.getElementsByTagName(parentNames[i]);
						root = (Element) (nl.getLength() > 0 ? nl.item(0) : null);
					}
					generalAddress = (root != null) ? new OperatorAddress(root) : new OperatorAddress();
				}
			} catch (SignatureException se) {
				LogHolder.log(LogLevel.ERR, LogType.MISC, se);
			} catch (XMLParseException xpe) {
				LogHolder.log(LogLevel.ERR, LogType.MISC, xpe);
			} catch (ParseException pe) {
				LogHolder.log(LogLevel.ERR, LogType.MISC, pe);
			} finally {
				if (operatorTCs == null)
				{
					LogHolder.log(LogLevel.INFO, LogType.MISC, "creating new Terms And Conditions container.");
					try 
					{
						Date now = new Date(System.currentTimeMillis());
						operatorTCs = new TermsAndConditions(operator, now);
						generalAddress = new OperatorAddress();
					} catch (ParseException e) {
						throw new IOException("Wrong format of the newly created date. This should never happen!");
					}
				}

				setEnabled(true);
			}
		}
	}
	
	private void initGeneralAddress()
	{
		Method currentGetter = null;
		Object currentValue = null;
		for (int i = 0; i < generalAddressMappings.length; i++) 
		{
			currentGetter = generalAddressMappings[i].getPropertyDescriptor().getReadMethod();
			try {
				currentValue = currentGetter.invoke(generalAddress, (Object[]) null);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			generalAddressMappings[i].getComponent().setText(
					(currentValue != null) ? currentValue.toString() : "");
		}
	}
	
	private void initTemplates(Document configDoc)
	{
		Document rootConfigDoc = (configDoc != null) ? configDoc : getConfiguration().getDocument();
			
		if( (rootConfigDoc == null) || 
			(rootConfigDoc.getDocumentElement() == null) )
		{
			return;
		}
		
		NodeList nl = rootConfigDoc.getElementsByTagName(XML_ELEMENT_TEMPLATE);
		Element currentElement = null;
		String currentTemplateURLValue = null;
		if(nl.getLength() == 0)
		{
			return;
		}
		TemplateReferenceID[] allRefIds = new TemplateReferenceID[nl.getLength()];
		
		m_bInitialisingTranslations = true;
		//m_cbReferenceIDs.removeAllItems();
		
		for (int i = 0; i < nl.getLength(); i++)
		{
			currentElement = (Element) nl.item(i);
			currentTemplateURLValue = currentElement.getTextContent();
			TermsAndConditionsTemplate template = null;
			URL currentTemplateURL = null;
			try 
			{
				if(!currentElement.hasAttribute(IXMLEncodable.XML_ATTR_ID) ||
						currentTemplateURLValue == null	)
				{
					LogHolder.log(LogLevel.ERR, LogType.MISC, "Will not load template, because it is not specified correctly.");
					allRefIds[i] = null;
					continue;
				}

				currentTemplateURL = new URL(currentTemplateURLValue);
				template = getTemplateFromURL(currentTemplateURL);
			} 
			catch (Exception e) 
			{
				LogHolder.log(LogLevel.ERR, LogType.MISC, "Could not load template, reason: "+e.getMessage());
			} 
			finally
			{
				TemplateReferenceID refIdItem = null;
				if(template != null)
				{
					Database.getInstance(TermsAndConditionsTemplate.class).update(template);
					//if(!isTemplateReferenceIdInComboBox(template.getId()))
					{
						refIdItem = new TemplateReferenceID(template.getId(), currentTemplateURL);
					}
				}
				else
				{
					refIdItem = new TemplateReferenceID(
							currentElement.getAttribute(IXMLEncodable.XML_ATTR_ID),
							currentTemplateURL);
				}
				if(refIdItem == null || !isTemplateReferenceIdInComboBox(refIdItem.getReferenceID()))
				{
					allRefIds[i] = refIdItem;
				}
			}
		}
		
		//System.out.println(allRefIds.length);
		
		for (TemplateReferenceID refId : allRefIds) 
		{
			if(refId != null) updateObject(m_cbReferenceIDs, refId, false);
		}
		/*
		System.out.println(m_cbReferenceIDs.getItemCount() + ":" + allRefIds.length);
		if (m_cbReferenceIDs.getItemCount() == 0)
		{
			System.out.println(XMLUtil.toString(rootConfigDoc));
		}
		new Exception().printStackTrace();*/
		m_bInitialisingTranslations = false;
	}
	
	private boolean m_bInitialisingTranslations = false;
	
	private void initTranslations()
	{
		m_cbTranslations.removeAllItems();
		clearTranslationFields();
		if(operatorTCs != null)
		{
			m_bInitialisingTranslations = true;
			Enumeration<Translation> supportedLanguages = operatorTCs.getAllTranslations();
			Translation currentTrans = null;	
			while(supportedLanguages.hasMoreElements())
			{
				currentTrans = supportedLanguages.nextElement();
				updateObject(m_cbTranslations, currentTrans, false);
				if(currentTrans.isDefaultTranslation())
				{
					m_cbTranslations.setSelectedItem(currentTrans);
					loadTranslation(currentTrans);
				}
			}
			Translation selectedTranslation = getSelectedTranslation();
			selectedLanguage = (selectedTranslation != null) ? selectedTranslation.getLocale() : null;
			if(selectedLanguage != null)
			{
				((TitledBorder)panelOperatorRight.getBorder()).setTitle(
					JAPMessages.getString(MSG_TITLE_OP_WITH_LANG)+" "+selectedLanguage);
			}
			m_bInitialisingTranslations = false;
			
			itemStateChanged(new ItemEvent(m_cbTranslations, ItemEvent.ITEM_STATE_CHANGED, 
					m_cbTranslations.getSelectedItem(), ItemEvent.SELECTED));
		}
	}
	
	/* save and log the Exception */
	private synchronized void saveAndLog()
	{
		try 
		{
			save();
		} catch (IOException e) {
			LogHolder.log(LogLevel.WARNING, LogType.MISC, "Could not save the T&C configuration, reason: "+
					e.getMessage());
		}
	}
	
	/**
	 * Read InfoServices from the configuration and return a TableModel containing the data
	 * @return
	 */
	private InfoServiceTableModel getInfoServices()
	{
		// Create a InfoServiceTableModel 
		InfoServiceTableModel tableModel = new InfoServiceTableModel();
		NodeList infoServices = getConfiguration().getDocument().getElementsByTagName("InfoServices");
		// There should be exactly one occurrence of 'InfoServices'
		if (infoServices.getLength() == 1) 
		{
			tableModel.readFromElement((Element)infoServices.item(0));
		}
		return tableModel;
	}
	
	/** loads a TC template from the corresponding URL saves it in the IS Database
	 * puts it's ReferenceID in the corresponding combobox and returns the template if it could be loaded.
	 * @param templateURL
	 * @return
	 * @throws IOException 
	 * @throws XMLParseException 
	 */
	private TermsAndConditionsTemplate getTemplateFromURL(URL templateURL) throws IOException, XMLParseException
	{
		InputStream inputStream = null;
		URLConnection currentConn = templateURL.openConnection();
		
		try
		{
			inputStream = 
				((currentConn.getContentEncoding() != null) &&
				 currentConn.getContentEncoding().contains("deflate")) ?
				new InflaterInputStream(currentConn.getInputStream()) : currentConn.getInputStream();
		}
		catch(NullPointerException npe)
		{
			//a NullPointerException can occur here in case the URL 
			//is incomplete: should better be thrown as an IOException
			throw new IOException(npe);
		}
		Document templateDoc = XMLUtil.readXMLDocument(inputStream);
		Element docElement = templateDoc.getDocumentElement();
		if(currentConn instanceof HttpURLConnection )
		{
			((HttpURLConnection)currentConn).disconnect();
		}
		TermsAndConditionsTemplate template = 
			(docElement != null) ? new TermsAndConditionsTemplate(docElement) : null;
		
		return template;
	}
	
	/**
	 * Update the internal (static) model containing terms and condition templates. 
	 * Therefore, establish a connection to a known InfoService and request reference
	 * IDs of all currently known terms and conditions frameworks.
	 */
	public void updateFromInfoService()
	{
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Getting available 'Terms and Conditions Templates'");
		// This is used to suppress certain events
		//this.loadingTemplate = true;
		// Get all info services
		InfoServiceTableModel infoServices = this.getInfoServices();
		// Variables needed for the 'while'
		int infoServicesNumber = infoServices.getRowCount();
		int counter = 0;
		boolean success = false;
		// Variables for host/port
		String host;
		int port;
		// Clear the vector
		//refIDs.removeAllElements();
		
		// Iterate
		while (!success && counter<infoServicesNumber)
		{
			// Get the info service data
			InfoServiceData isData = infoServices.getData(counter);
			host = isData.getListenerInterface(0).getHost();		
		    port = isData.getListenerInterface(0).getPort();
		    LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Asking " + host + ":" + port);
		    try {
				// Create the request URL an connect
			    URL requestURL = new URL("http", host, port, 
			    		TermsAndConditionsTemplate.INFOSERVICE_CONTAINER_PATH);
		
			    HttpURLConnection conn = (HttpURLConnection) requestURL.openConnection();
			    	
				// Create a serials document from the InputStream
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				
				Document serialsDoc = db.parse(conn.getInputStream());
				// Disconnect
				conn.disconnect();
				// Get the single nodes
				NodeList nl = serialsDoc.getDocumentElement().getElementsByTagName(
						TermsAndConditionsTemplate.XML_ELEMENT_NAME);
				String currentTemplateRefID = null;
				URL currentTemplateURL = null;
				
				if (nl.getLength() == 0)
				{
					LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Failed ...");
				}
				else if (nl.getLength() > 0)
				{
					removeFirstPlaceHolder(m_cbReferenceIDs);
					success = true;
					TermsAndConditionsTemplate currentTemplate = null;
					// Insert serials into the vector
					for (int i = 0; i < nl.getLength(); i++)
					{
						currentTemplateRefID = ((Element)nl.item(i)).getAttribute(IXMLEncodable.XML_ATTR_ID);
						currentTemplateURL = new URL("http", host, port, 
								TermsAndConditionsTemplate.INFOSERVICE_PATH+currentTemplateRefID);
						currentTemplate = getTemplateFromURL(currentTemplateURL);
						if(currentTemplate != null)
						{
							Database.getInstance(TermsAndConditionsTemplate.class).update(currentTemplate);
							if(!isTemplateReferenceIdInComboBox(currentTemplate.getId()))
							{
								TemplateReferenceID refIdItem = 
									new TemplateReferenceID(currentTemplate.getId(), currentTemplateURL);
								updateObject(m_cbReferenceIDs, refIdItem, false);
							}
						}
					}
				}				
		    } catch (MalformedURLException mue) {
		    	LogHolder.log(LogLevel.EXCEPTION, LogType.NET, "Error: " + mue.getMessage());
		    } catch (IOException ioe) {
		    	LogHolder.log(LogLevel.EXCEPTION, LogType.NET, "Error: " + ioe.getMessage());
		    } catch (SAXException se) {
		    	LogHolder.log(LogLevel.EXCEPTION, LogType.MISC, "Error: " + se.getMessage());
		    } catch (ParserConfigurationException pce) {
		    	LogHolder.log(LogLevel.EXCEPTION, LogType.MISC, "Error: " + pce.getMessage());
		    } catch (IllegalArgumentException iae) {
		    	LogHolder.log(LogLevel.EXCEPTION, LogType.MISC, "Error: " + iae.getMessage());
			} catch (XMLParseException xpe) {
				LogHolder.log(LogLevel.EXCEPTION, LogType.MISC, "Error: " + xpe.getMessage());
			}
		    // Try the next InfoService
		    counter++;
		}
	}
	
	/**
	 * Save the date to the "Date" element as well as to the "serial" attribute
	 */
	private void saveDate()
	{
		if(operatorTCs != null)
		{
			// Get the date from the panel as Calendar object
			Calendar cal = m_pnlDate.getCalendar();		
			// Use DateFormat to store the date to the configuration
			String sDate = m_dateFormatter.format(cal.getTime());
			LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Saving the date: " + sDate);
			operatorTCs.setDate(m_pnlDate.getDate());
		}
	}
	
	private Element getSpecifiedTemplatesElement(Document ownerDoc)
	{
		Element templatesRoot = null;
		if( (m_cbReferenceIDs.getItemCount() > 0) && 
				!(m_cbReferenceIDs.getSelectedItem() instanceof ComboBoxPlaceHolder) )
		{
			//Save the URLS from where the templates are loaded.
			templatesRoot = ownerDoc.createElement(XML_ELEMENT_TEMPLATES);
			Element currentTemplateElement = null;
			Object currentItem = null;
			TemplateReferenceID currentRefId = null;
			
			for (int i = 0; i < m_cbReferenceIDs.getItemCount(); i++) 
			{
				currentItem = m_cbReferenceIDs.getItemAt(i);
				if( (currentItem != null) &&
					(currentItem instanceof TemplateReferenceID) )
				{
					currentRefId = (TemplateReferenceID) m_cbReferenceIDs.getItemAt(i);
					currentTemplateElement = currentRefId.toXMLElement(ownerDoc);
					currentTemplateElement.setAttribute(IXMLEncodable.XML_ATTR_ID, currentRefId.getReferenceID());
					templatesRoot.appendChild(currentTemplateElement);
				}
			}
		}
		return templatesRoot;
	}
	
	private void saveTranslation(Translation translation)
	{
		if(translation != null)
		{
			OperatorAddress addr = translation.getOperatorAddress();
			Method currentSetter = null;
			String currentValue = null;
			
			for (int i = 0; i < translationUrlMappings.length; i++) 
			{
				currentSetter = translationUrlMappings[i].getPropertyDescriptor().getWriteMethod();
				if (translationUrlMappings[i].getComponent() != null)
				{
					currentValue = translationUrlMappings[i].getComponent().getText();
					
					try {
						currentSetter.invoke(translation, new Object[]{currentValue});
					} catch (Exception e) {
						LogHolder.log(LogLevel.EXCEPTION, LogType.GUI, e);
					} 
				}
			}
			
			if(addr == null)
			{
				addr = new OperatorAddress();
				translation.setOperatorAddress(addr);
			}
			
			for (int i = 0; i < translationAddressMappings.length; i++) 
			{
				currentSetter = translationAddressMappings[i].getPropertyDescriptor().getWriteMethod();
				currentValue = translationAddressMappings[i].getComponent().getText();
				
				try {
					currentSetter.invoke(addr, new Object[]{currentValue});
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void saveGeneralAddress()
	{
		Method currentSetter = null;
		Object currentValue = null;
		for (int i = 0; i < generalAddressMappings.length; i++) 
		{
			currentSetter = generalAddressMappings[i].getPropertyDescriptor().getWriteMethod();
			currentValue = generalAddressMappings[i].getComponent().getText();
			try {
				currentSetter.invoke(generalAddress, new Object[]{currentValue});
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	
	// clear the text translation dependent fields
	private void clearTranslationFields()
	{
		for (int i = 0; i < translationUrlMappings.length; i++) 
		{
			translationUrlMappings[i].getComponent().setText("");
		}
		
		for (int i = 0; i < translationAddressMappings.length; i++) 
		{	
			translationAddressMappings[i].getComponent().setText("");
		}
	}
	
	//--- private helper and convenience functions ---
	
	private boolean selectTemplateReferenceID(String templateReferenceID)
	{//new Exception().printStackTrace();
		TemplateReferenceID currentTemplateRefId = null;
		for(int i=0; i < m_cbReferenceIDs.getItemCount(); i++)
		{
			if(m_cbReferenceIDs.getItemAt(i) instanceof TemplateReferenceID)
			{
				currentTemplateRefId =
					(TemplateReferenceID) m_cbReferenceIDs.getItemAt(i);
				if( (currentTemplateRefId.getReferenceID() != null) && 
					currentTemplateRefId.getReferenceID().equals(templateReferenceID))
				{
					if(m_cbReferenceIDs.getSelectedIndex() != i)
					{
						m_cbReferenceIDs.setSelectedIndex(i);
					}
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isATranslationLoaded()
	{
		return  (m_cbTranslations.getItemCount() > 1) ||
				((m_cbTranslations.getItemCount() == 1) && 
				 (m_cbTranslations.getItemAt(0) instanceof Translation));
	}
	
	private boolean isTemplateReferenceIdInComboBox(String templateReferenceID)
	{
		TemplateReferenceID currentTemplateRefId = null;
		for(int i=0; i < m_cbReferenceIDs.getItemCount(); i++)
		{
			if(m_cbReferenceIDs.getItemAt(i) instanceof TemplateReferenceID)
			{
				currentTemplateRefId =
					(TemplateReferenceID) m_cbReferenceIDs.getItemAt(i);
				if( (currentTemplateRefId.getReferenceID() != null) && 
					currentTemplateRefId.getReferenceID().equals(templateReferenceID))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isAddTranslationPossible()
	{
		TemplateReferenceID selectedRefId = getSelectedTemplateReferenceID();
		return (selectedRefId != null) && selectedRefId.isComplete();
	}
	
	Translation getPreviewTranslation()
	{
		Document rootConfigDoc = getConfiguration().getDocument();
		NodeList nl = rootConfigDoc.getElementsByTagName(XML_ELEMENT_TC_TRANSLATION_IMPORTS);
		Translation currentTranslation = getSelectedTranslation();
		if(currentTranslation == null || operatorTCs == null)
		{
			return null;
		}
		//create a translation object with the values of the current
		//selected translation completed with the the general import values. 
		return (nl.getLength() > 0) ? 
				duplicateWithImports(operatorTCs, currentTranslation, (Element) nl.item(0)) : currentTranslation;
	}
	
	private boolean isDefaultLanguagePossible()
	{
		Translation currentTrans = getSelectedTranslation();
		return (currentTrans != null) ? !currentTrans.isDefaultTranslation() : false;
	}
	
	private boolean isDeleteTranslationPossible()
	{
		return (getSelectedTranslation() != null);
	}
	
	private boolean isWrongTemplateSet()
	{
		TemplateReferenceID refId = getSelectedTemplateReferenceID();
		Translation currentTrans = getSelectedTranslation();
		
		return (currentTrans != null) && 
			(currentTrans.getLocale() != null) && 
			(refId != null) &&
			(refId.getLangCode() != null) &&
			!currentTrans.getLocale().equals(refId.getLangCode());
	}
	
	boolean isPreviewPossible()
	{
		return isTransformationPossible();
	}
	
	private boolean isContentManagementPossible()
	{
		return isTransformationPossible();
	}
	
	//requires that a translation is defined and selected which
	//referes to a loaded template.
	private boolean isTransformationPossible()
	{
		TermsAndConditionsTemplate template = null;
		Translation currentTranslation = getSelectedTranslation();
		String templateRefID = null;
		if( currentTranslation != null)
		{
			templateRefID = currentTranslation.getTemplateReferenceId();
			if((templateRefID != null) &&
				!templateRefID.equals("") )
			{
				template = (TermsAndConditionsTemplate)
								Database.getInstance(TermsAndConditionsTemplate.class).getEntryById(templateRefID);
				return template != null;
			}
		}
		return false;
	}
	
	private boolean isExportTemplatePossible()
	{
		TemplateReferenceID refId = getSelectedTemplateReferenceID();
		TermsAndConditionsTemplate template = (TermsAndConditionsTemplate) 
			(refId != null ? Database.getInstance(TermsAndConditionsTemplate.class).getEntryById(refId.getReferenceID()) : null);
		return (template != null);
	}
	
	private boolean isDeleteTemplatePossible()
	{
		return (getSelectedTemplateReferenceID() != null);
	}
	
	private Translation getSelectedTranslation()
	{
			Object selected = m_cbTranslations.getSelectedItem();
			return (Translation) ((selected instanceof Translation) ? selected : null);
	}
	
	private TemplateReferenceID getSelectedTemplateReferenceID()
	{
			Object selected = m_cbReferenceIDs.getSelectedItem();
			return (TemplateReferenceID) ((selected instanceof TemplateReferenceID) ? selected : null);
	}
	
	/*private Vector<TemplateReferenceID> getTemplateReferenceIDsByType(String type)
	{
		return getMatchingTemplateReferenceIDs(type, null, null);
	}
	
	private Vector<TemplateReferenceID> getTemplateReferenceIDsByLangCode(String langCode)
	{
		return getMatchingTemplateReferenceIDs(null, langCode, null);
	}*/
	
	private Vector<TemplateReferenceID> getAllTemplateReferenceIds()
	{
		return getMatchingTemplateReferenceIDs(null, null, null);
	}
	
	private Vector<TemplateReferenceID> getMatchingTemplateReferenceIDs(String type, String langCode, Date date)
	{
		Vector<TemplateReferenceID> returnItems = new Vector<TemplateReferenceID>();
		Object currentItem = null;
		TemplateReferenceID refId = null;
		boolean typeMatches = false;
		boolean langCodeMatches = false;
		boolean dateMatches = false;
		for (int i = 0; i < m_cbReferenceIDs.getItemCount(); i++) 
		{
			currentItem = m_cbReferenceIDs.getItemAt(i);
			if( (currentItem != null) &&
				(currentItem instanceof TemplateReferenceID) )
			{
				refId = (TemplateReferenceID) currentItem;
				//null parameter means: don't care if it's matching or not.
				typeMatches = (type != null) ? (refId.getType() != null ? refId.getType().equals(type) : false) : true;
				langCodeMatches = (langCode != null) ? (refId.getLangCode() != null ? refId.getLangCode().equals(langCode) : false) : true;
				dateMatches = (date != null) ? (refId.getDate() != null ? refId.getDate().equals(date) : false) : true;
				if( typeMatches && langCodeMatches && dateMatches)
				{
					returnItems.add(refId);
				}
			}	
		}
		return returnItems;
	}
	
	//---------- private static helper and convenience functions ---------------

	private static void updateObject(JComboBox comboBox, Object o, boolean selectForced)
	{
		boolean selectedRemoved = false;
		if(comboBox.getModel() instanceof MutableComboBoxModel)
		{
			MutableComboBoxModel cbModel =
				(MutableComboBoxModel) comboBox.getModel();
			Object currentItem = null;
			Object selectedItem = cbModel.getSelectedItem();
			
			removeFirstPlaceHolder(comboBox);	
			for(int i = 0; i < cbModel.getSize(); i++)
			{
				currentItem = cbModel.getElementAt(i);
				if( cbModel.getElementAt(i).equals(o) )
				{
					cbModel.removeElementAt(i);
					selectedRemoved = (selectedItem == currentItem);
					break;
				}
			}
		}
		comboBox.addItem(o);
		if(selectForced || selectedRemoved) comboBox.setSelectedItem(o);
	}
	
	/**
	 * only removes the first placeHolder
	 * (serves the usual case to have only one placeholder if a combobox is empty)
	 * @param comboBox
	 */
	private static void removeFirstPlaceHolder(JComboBox comboBox)
	{
		if(comboBox.getModel() instanceof MutableComboBoxModel)
		{
			MutableComboBoxModel cbModel =
				(MutableComboBoxModel) comboBox.getModel();
			for(int i = 0; i < cbModel.getSize(); i++)
			{
				if(cbModel.getElementAt(i) instanceof ComboBoxPlaceHolder)
				{
					cbModel.removeElementAt(i);
					return;
				}
			}
		}
	}
	
	private static Translation duplicateWithImports(TermsAndConditions tcsContainer, Translation translation,
			Element xmlImports)
	{
		try 
		{
			Translation importTrans = 
				tcsContainer.new Translation(xmlImports, false);
			
			//create a deep copy of this translation
			//Translation
			Object currentValue = null;
			Method currentGetter = null;
			Method currentSetter = null;
			
			//even though it may be a waste of memory, it is necessary to clone the sections in case the
			//translation copy will modify the sections.
			//importTrans.sections = (TCComposite) this.sections.clone();
			
			PropertyDescriptor translationPDs[] =
				Introspector.getBeanInfo(translation.getClass()).getPropertyDescriptors();
			for (int i = 0; i < translationPDs.length; i++) 
			{
				if( !translationPDs[i].getName().equals("operatorAddress") &&
					!translationPDs[i].getName().equals("defaultTranslation") &&
					translationPDs[i].getWriteMethod() != null)
				{
					currentGetter = translationPDs[i].getReadMethod();
					currentSetter = translationPDs[i].getWriteMethod();
					System.out.println("invoking getter: "+currentGetter);
					currentValue = currentGetter.invoke(translation, null);
				
					if( (currentValue != null) && !currentValue.toString().equals(""))
					{
						currentSetter.invoke(
								importTrans, 
								new Object[]{currentValue});
					}
				}
			}
			
			if(importTrans.getOperator() == null)
			{
				importTrans.setOperatorAddress(translation.getOperatorAddress());
			}
			else if(translation.getOperatorAddress() != null)
			{
				//now we update every address field on the newTrans address object
				//which is defined in the address object of the current translation
				PropertyDescriptor addressPDs[] =
					Introspector.getBeanInfo(OperatorAddress.class).getPropertyDescriptors();
				for (int i = 0; i < addressPDs.length; i++) 
				{
					if( addressPDs[i].getWriteMethod() != null )
					{
						currentGetter = addressPDs[i].getReadMethod();
						currentSetter = addressPDs[i].getWriteMethod();
						currentValue = currentGetter.invoke(translation.getOperatorAddress(), null);
						//update the field in the copied object.
						if( (currentValue != null) && !currentValue.toString().equals(""))
						{
							currentSetter.invoke(
									importTrans.getOperatorAddress(), 
									new Object[]{currentValue});
						}
					}
				}
			}
			return importTrans;
		} catch (XMLParseException e) {
			e.printStackTrace();
		} catch (IntrospectionException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Acts as an 'empty' element displaying a message
	 * if a ComboBox does not contain any regular 
	 * elements.
	 */
	private static class ComboBoxPlaceHolder
	{
		private String message;

		private ComboBoxPlaceHolder()
		{	
			this.message = null;
		}
		
		private ComboBoxPlaceHolder(String message)
		{	
			this.message = message;
		}
		
		public String getMessage() 
		{
			return message;
		}

		public void setMessage(String message) 
		{
			this.message = message;
		}
		
		public String toString()
		{
			return getMessage();
		}
		
		public boolean equals(Object o)
		{
			return (o instanceof ComboBoxPlaceHolder) ? message.equals(((ComboBoxPlaceHolder)o).message) : false;
		}
		
		public static ComboBoxPlaceHolder createPlaceHolder(String messageKey)
		{
			return new ComboBoxPlaceHolder(JAPMessages.getString(messageKey));
		}
	}
	
	/**
	 * Class representing a reference to a Template
	 * A Template reference consists of the 
	 * corresponding template ID and the URL
	 * from where it was loaded.
	 */
	private class TemplateReferenceID
	{
		//private String referenceID;
		private URL source;
		private String type;
		private Date date;
		private String langCode;
		
		private TemplateReferenceID()
		{	
			this.type = null;
			this.date = null;
			this.langCode = null;
			this.source = null;
		}
		
		private TemplateReferenceID(String name, LanguageMapper langMapper, Date date, String sourceURL) throws MalformedURLException
		{
			this(name, langMapper, date, new URL(sourceURL));
		}
		
		private TemplateReferenceID(String type, LanguageMapper langMapper, Date date, URL source)
		{
			this(type, langMapper.getISOCode().toLowerCase(), date, source);
		}
		
		private TemplateReferenceID(String type, String langCode, Date date, URL source)
		{
			//this(name+"_"+isoLangCode+"_"+m_dateFormatter.format(date), source);
			this.type = type;
			this.date = date;
			this.langCode = langCode;
			this.source = source;
		}
		
		private TemplateReferenceID(String referenceID, URL source)
		{	
			setReferenceID(referenceID);
			this.source = source;
		}
		
		public String getReferenceID() 
		{
			return isComplete() ? type+"_"+langCode+"_"+m_dateFormatter.format(date) : null;
		}

		private void setReferenceID(String referenceId)
		{
			StringTokenizer refIdTokenizer = new StringTokenizer(referenceId,"_");
			
			if(refIdTokenizer.countTokens() != 3)
			{
				throw new IllegalArgumentException("Not a valid template reference id: "+referenceId);
			}
			
			String tempType = refIdTokenizer.nextToken();
			String tempLangCode = refIdTokenizer.nextToken();
			Date tempDate = null;
			
			try 
			{
				tempDate = new SimpleDateFormat(TermsAndConditions.DATE_FORMAT).parse(refIdTokenizer.nextToken());
			} 
			catch (ParseException e) 
			{
				throw new IllegalArgumentException("refernce id "+referenceId+" contains invalid date.");
			} 
			
			setType(tempType);
			setLangCode(tempLangCode);
			setDate(tempDate);
		}
		
		public URL getSource() 
		{
			return source;
		}

		public void setSource(URL source) 
		{
			this.source = source;
		}

		public boolean equals(Object otherRefid)
		{
			if(otherRefid == null) return false;
			if(!(otherRefid instanceof TemplateReferenceID))
			{
				return false;
			}
			return ((TemplateReferenceID)otherRefid).getReferenceID().equals(getReferenceID());
		}
		
		public String toString()
		{
			return getReferenceID();
		}
		
		public Element toXMLElement(Document doc)
		{
			Element templateElement = doc.createElement(XML_ELEMENT_TEMPLATE);
			templateElement.setTextContent(source.toString());
			return templateElement;
		}

		public String getType() 
		{
			return type;
		}

		public void setType(String type) 
		{
			this.type = type;
		}

		public Date getDate() 
		{
			return date;
		}

		public void setDate(Date date) 
		{
			this.date = date;
		}

		public String getLangCode() 
		{
			return langCode;
		}

		public void setLangCode(String langCode) 
		{
			this.langCode = langCode;
		}
		
		public boolean isComplete()
		{
			return (type != null) && (langCode != null) && (date != null); 
		}
	}
	
	private class PropertyToComponentMapping<ComponentType extends Component>
	{
		private PropertyDescriptor propertyDescriptor = null;
		private ComponentType component = null;
		
		public PropertyToComponentMapping(PropertyDescriptor propertyDescriptor, ComponentType component)
		{
			this.propertyDescriptor = propertyDescriptor;
			this.component = component;
		}
		
		public PropertyDescriptor getPropertyDescriptor() 
		{
			return propertyDescriptor;
		}

		public void setPropertyDescriptor(PropertyDescriptor propertyDescriptor) 
		{
			this.propertyDescriptor = propertyDescriptor;
		}

		public ComponentType getComponent() 
		{
			return component;
		}
		
		public void setComponent(ComponentType component) 
		{
			this.component = component;
		}
	}
	
	private class DialogResult<ResultType>
	{
		private ResultType result = null;
		private String command = null;
		
		public DialogResult(){}
		
		public void reset()
		{
			result = null;
			command = null;
		}

		public DialogResult(String command, ResultType result) 
		{
			this.result = result;
			this.command = command;
		}
		
		public ResultType getResult() {
			return result;
		}

		public void setResult(ResultType locale) {
			this.result = locale;
		}

		public String getCommand() {
			return command;
		}

		public void setCommand(String command) {
			this.command = command;
		}
	}
	
	class NotLoadedException extends Exception
	{}
	
	static class OperatorAddressIntrospection 
	{
		private static Hashtable propertyDescriptors = new Hashtable();
		
		static 
		{
			try 
			{
				BeanInfo info = Introspector.getBeanInfo(OperatorAddress.class);
				PropertyDescriptor[] pds = info.getPropertyDescriptors();
				for (int i = 0; i < pds.length; i++)
				{
					propertyDescriptors.put(pds[i].getName(), pds[i]);
				}
			} 
			catch (IntrospectionException e) 
			{
			} 
		}
		
		public static PropertyDescriptor getDescriptor(String propertyName)
		{
			return (PropertyDescriptor) propertyDescriptors.get(propertyName);
		}
	}
}