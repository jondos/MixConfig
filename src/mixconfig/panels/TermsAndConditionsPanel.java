package mixconfig.panels;

import gui.DateListener;
import gui.DatePanel;
import gui.MixConfigTextField;
import gui.TermsAndConditionsDialog;
import gui.TitledGridBagPanel;
import gui.dialog.DialogContentPane;
import gui.dialog.JAPDialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SignatureException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
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
import javax.xml.transform.TransformerException;

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
import anon.infoservice.TermsAndConditions;
import anon.infoservice.TermsAndConditionsFramework;
import anon.infoservice.TermsAndConditionsTranslation;
import anon.util.Base64;
import anon.util.IXMLEncodable;
import anon.util.JAPMessages;
import anon.util.XMLParseException;
import anon.util.XMLUtil;

/**
 * This panel is used by mix operators to enter customized 
 * information to the standard terms and conditions.
 * 
 * @author renner
 */
public class TermsAndConditionsPanel extends MixConfigPanel implements ActionListener, DateListener, ChangeListener {
	
	private static final String MSG_TC_CAPTION = TermsAndConditionsPanel.class.getName() + "_caption";
	//private static final String MSG_TITLE_TC = TermsAndConditionsPanel.class.getName() + "_titletc";
	private static final String MSG_TITLE_OP_GENERAL = TermsAndConditionsPanel.class.getName() + "_titleopGeneral";
	private static final String MSG_TITLE_OP_WITH_LANG = TermsAndConditionsPanel.class.getName() + "_titleopWithLang";
	private static final String MSG_TITLE_URLS = TermsAndConditionsPanel.class.getName() + "_titleurls";
	private static final String MSG_ADD_TRANSLATIONS_DIALOG = TermsAndConditionsPanel.class.getName() + "_addTranslationsDialog";
	private static final String MSG_ADD_TEMPLATE_DIALOG = TermsAndConditionsPanel.class.getName() + "_addTemplateDialog";
	private static final String MSG_ADD_TEMPLATE_DIALOG_LABEL = TermsAndConditionsPanel.class.getName() + "_addTemplateDialogLabel";
	//private static final String MSG_EDIT_TEMPLATE_DIALOG_ERROR = TermsAndConditionsPanel.class.getName() + "_editTemplateDialogError";
	private static final String MSG_UPDATE = TermsAndConditionsPanel.class.getName() + "_update";
	private static final String MSG_PREVIEW = TermsAndConditionsPanel.class.getName() + "_preview";
	private static final String MSG_URL = TermsAndConditionsPanel.class.getName() + "_url";
	private static final String MSG_PATH = TermsAndConditionsPanel.class.getName() + "_path";
	private static final String MSG_LOAD = TermsAndConditionsPanel.class.getName() + "_load";
	//private static final String MSG_EDIT = TermsAndConditionsPanel.class.getName() + "_edit";
	private static final String MSG_ADD = TermsAndConditionsPanel.class.getName() + "_add";
	private static final String MSG_DELETE = TermsAndConditionsPanel.class.getName() + "_delete";
	private static final String MSG_EXPORT = TermsAndConditionsPanel.class.getName() + "_export";
	private static final String MSG_DEFAULT_TRANSLATION = TermsAndConditionsPanel.class.getName() + "_defaultTranslation";
	//private static final String MSG_TODAY = TermsAndConditionsPanel.class.getName() + "_today";
	private static final String MSG_AVAIL_LANG_LABEL = TermsAndConditionsPanel.class.getName() + "_availableLanguagesLabel";
	private static final String MSG_VALID_LABEL = TermsAndConditionsPanel.class.getName() + "_validLabel";
	private static final String MSG_TEMPLATE_LABEL = TermsAndConditionsPanel.class.getName() + "_templateLabel";
	private static final String MSG_LANGUAGE_LABEL = TermsAndConditionsPanel.class.getName() + "_languageLabel";
	private static final String MSG_TRANSLATION_LABEL = TermsAndConditionsPanel.class.getName() + "_translationLabel";
	private static final String MSG_STREET_LABEL = TermsAndConditionsPanel.class.getName() + "_streetLabel";
	private static final String MSG_ZIP_LABEL = TermsAndConditionsPanel.class.getName() + "_zipLabel";
	private static final String MSG_CITY_LABEL = TermsAndConditionsPanel.class.getName() + "_cityLabel";
	private static final String MSG_FAX_LABEL = TermsAndConditionsPanel.class.getName() + "_faxLabel";
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
	
	//private static final String ACTION_CMD_DELETE = "delete";
	private static final String ACTION_CMD_OK = "ok";
	private static final String ACTION_CMD_ADD_TRANSLATION = "addTranslation";
	//private static final String ACTION_CMD_DEFAULT_TRANSLATION = "setDefaultTranslation";
	
	public static final String XML_ELEMENT_TEMPLATES = "Templates"; 
	public static final String XML_ELEMENT_TEMPLATE = "Template";
	public static final String XML_ELEMENT_TC_OPTIONS = "TermsAndConditionsOptions";
	public static final String XML_ELEMENT_TC_TRANSLATION_IMPORTS = "TCTranslationImports";
	
	/** Terms and conditions paths */
	public static final String XMLPATH_TERMS_OPTIONS = XML_ELEMENT_TC_OPTIONS;
	public static final String XMLPATH_TERMS = XMLPATH_TERMS_OPTIONS + "/TermsAndConditions";
	public static final String XMLPATH_TERMS_TRANSLATION_IMPORTS = XMLPATH_TERMS + "/TCTranslationImports";
	public static final String XMLPATH_TERMS_TRANSLATION = XMLPATH_TERMS + "/TCTranslation";
	
	public static final String XMLPATH_TERMS_URLPP = XMLPATH_TERMS_TRANSLATION_IMPORTS + "/PrivacyPolicyUrl";
	public static final String XMLPATH_TERMS_URLLO = XMLPATH_TERMS_TRANSLATION_IMPORTS + "/LegalOpinionsUrl";
	public static final String XMLPATH_TERMS_URLOA = XMLPATH_TERMS_TRANSLATION_IMPORTS + "/OperationalAgreementUrl";
	
	/** general operator infos */
	public static final String XMLPATH_TERMS_GENERAL_STREET = 
		XMLPATH_TERMS_TRANSLATION_IMPORTS + "/Operator/"+OperatorAddress.NODE_NAME_STREET;
	public static final String XMLPATH_TERMS_GENERAL_POSTCODE = 
		XMLPATH_TERMS_TRANSLATION_IMPORTS + "/Operator/"+OperatorAddress.NODE_NAME_POSTALCODE;
	public static final String XMLPATH_TERMS_GENERAL_CITY = 
		XMLPATH_TERMS_TRANSLATION_IMPORTS + "/Operator/"+OperatorAddress.NODE_NAME_CITY;
	public static final String XMLPATH_TERMS_GENERAL_FAX =
		XMLPATH_TERMS_TRANSLATION_IMPORTS + "/Operator/"+OperatorAddress.NODE_NAME_FAX;
	public static final String XMLPATH_TERMS_GENERAL_VAT = 
		XMLPATH_TERMS_TRANSLATION_IMPORTS + "/Operator/"+OperatorAddress.NODE_NAME_VAT;
	public static final String XMLPATH_TERMS_GENERAL_VENUE = 
		XMLPATH_TERMS_TRANSLATION_IMPORTS + "/Operator/"+OperatorAddress.NODE_NAME_VENUE;
	
	public final static String PRIVACY_POLICY_TAG ="PrivacyPolicyUrl";
	public final static String LEGAL_OPINIONS_TAG ="LegalOpinionsUrl";
	public final static String OPERATIONAL_AGREEMENT_TAG ="OperationalAgreementUrl";
	
	/** ComboBox and Model */
	private DefaultComboBoxModel m_modelReferenceIDs = new DefaultComboBoxModel();
	private JComboBox m_cbReferenceIDs = new JComboBox(m_modelReferenceIDs);
	private JComboBox m_cbTranslations = new JComboBox();
	
	private JButton m_btnPreview;
	private JButton m_btnAddTranslation;
	private JButton m_btnDefaultTranslation;
	private JButton m_btnDeleteTranslation;
	private JButton m_btnExportTranslation;
	
	private JButton m_btnInfoServiceTemplate;
	private JButton m_btnFileTemplate;
	private JButton m_btnCustomTemplate;
	private JButton m_btnDeleteTemplate;
	
	/** Date panel + button */
	private DatePanel m_pnlDate;
	//private JButton m_btnToday;
	
	private JTextField m_tf_templateURL;
	
	TitledGridBagPanel panelOperatorLeft;
	/** TextFields */
	private JTextField m_tf_general_Street;
	private JTextField m_tf_general_Post;
	private JTextField m_tf_general_City;
	private JTextField m_tf_general_VAT;
	private JTextField m_tf_general_Fax;
	private JTextField m_tf_general_Venue;
	
	TitledGridBagPanel panelOperatorRight;
	/** TextFields */
	private JTextField m_tf_lang_Street;
	private JTextField m_tf_lang_Post;
	private JTextField m_tf_lang_City;
	private JTextField m_tf_lang_VAT;
	private JTextField m_tf_lang_Fax;
	private JTextField m_tf_lang_Venue;
	
	private JTextField m_tfUrlPP;
	private JTextField m_tfUrlLO;
	private JTextField m_tfUrlOA;
	
	private JTextField m_tf_templateReferenceId;
	
	private PropertyToComponentMapping<JTextField>[] tcTranslationMappings;
	private PropertyToComponentMapping<JTextField>[] tcAddressMappings;
	
	private TermsAndConditions operatorTCs = null;
	private String opCertX509 = null;
	private String selectedLanguage = "";
	private File lastOpened = null;

	private static final String[] LOCALE_CHOICE = 
		new String[]
		{ 
			Locale.GERMAN.getLanguage(),
			Locale.ENGLISH.getLanguage(),
			Locale.FRENCH.getLanguage(),
			Locale.ITALIAN.getLanguage(),
			"es"
		};
	
	/** The DateFormat that we use here */
	DateFormat m_dateFormatter = new SimpleDateFormat("yyyyMMdd");
	
	private volatile boolean loadingTemplate;
	
	private static TermsAndConditionsPanel panelSingleton = new TermsAndConditionsPanel();
	
	public static TermsAndConditionsPanel get()
	{
		return panelSingleton;
	}
	
	public static void loadOperator() throws IOException, NotLoadedException
	{
		panelSingleton.load();
	}
	
	/** Constructor */
	private TermsAndConditionsPanel()
	{
		// Initial stuff
		super(JAPMessages.getString(MSG_TC_CAPTION));
		GridBagConstraints constraints = getInitialConstraints();
		
		// Listen for ConfigurationEvents
		MixConfig.getMixConfiguration().addChangeListener(this);
		
		// Reference terms & date -------------------------
		
		TitledGridBagPanel panelTranslation = new TitledGridBagPanel(JAPMessages.getString(MSG_TRANSLATION_LABEL));
		TitledGridBagPanel panelTemplate = new TitledGridBagPanel(JAPMessages.getString(MSG_TEMPLATE_LABEL));
		
		//reduce the insets to get all the components in there.
		constraints.insets = new Insets(3,3,3,3);
		constraints.gridwidth = 1;
		add(panelTranslation, constraints);
		constraints.gridx++;
		add(panelTemplate, constraints);
		constraints.gridx--;
		
		// Do not call updateSerials() in the constructor
		m_cbReferenceIDs.setName(XMLPATH_TERMS);
		m_cbTranslations.setName(XMLPATH_TERMS_TRANSLATION);
		
		m_tf_templateReferenceId = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_templateReferenceId.addFocusListener(this);
		
		m_tf_templateURL = new MixConfigTextField(20);
		m_tf_templateURL.addFocusListener(this);
		// Add the update button
		m_btnInfoServiceTemplate = new JButton(JAPMessages.getString(MSG_UPDATE));
		m_btnInfoServiceTemplate.addActionListener(this);
		
		//Button for previewing the T&Cs with the surrent settings
		m_btnPreview = new JButton(JAPMessages.getString(MSG_PREVIEW));
		m_btnPreview.addActionListener(this);
		
		//Button for editing translations
		m_btnAddTranslation = new JButton(JAPMessages.getString(MSG_ADD));
		m_btnAddTranslation.addActionListener(this);
		m_btnDefaultTranslation = new JButton(JAPMessages.getString(MSG_DEFAULT_TRANSLATION));
		m_btnDefaultTranslation.addActionListener(this);
		m_btnDeleteTranslation = new JButton(JAPMessages.getString(MSG_DELETE));
		m_btnDeleteTranslation.addActionListener(this);
		
		m_btnExportTranslation = new JButton(JAPMessages.getString(MSG_EXPORT));
		m_btnExportTranslation.addActionListener(this);
		//Button for loading a template locally
		m_btnFileTemplate = new JButton(JAPMessages.getString(MSG_LOAD));
		m_btnFileTemplate.addActionListener(this);
		
		m_btnCustomTemplate = new JButton(JAPMessages.getString(MSG_ADD));
		m_btnCustomTemplate.addActionListener(this);
		m_btnDeleteTemplate = new JButton(JAPMessages.getString(MSG_DELETE));
		m_btnDeleteTemplate.addActionListener(this);
		
		JPanel translationButtonPanel = new JPanel();
		translationButtonPanel.add(m_btnAddTranslation);
		translationButtonPanel.add(m_btnDefaultTranslation);
		translationButtonPanel.add(m_btnPreview);
	
		JPanel templateButtonPanel = new JPanel();
		templateButtonPanel.add(m_btnInfoServiceTemplate);
		templateButtonPanel.add(m_btnFileTemplate);
		templateButtonPanel.add(m_btnCustomTemplate);
		
		JPanel templateURLPanel = new JPanel();
		templateURLPanel.add(createLabel(MSG_URL, MSG_PATH));
		templateURLPanel.add(m_tf_templateURL);
		
		// The current date
		Date now = new Date(System.currentTimeMillis());
		m_pnlDate = new DatePanel(now);
		m_pnlDate.addDateListener(this);
		
		// Add the 'Today' button
		//m_btnToday = new JButton(JAPMessages.getString(MSG_TODAY));
		//m_btnToday.addActionListener(this);
		
		panelTranslation.addRow(createLabel(MSG_LANGUAGE_LABEL), m_cbTranslations, m_btnDeleteTranslation);
		panelTranslation.addRow(new Component[]{createLabel(MSG_VALID_LABEL), m_pnlDate}, new int[]{1,2}); //, m_btnToday);
		panelTranslation.addRow(new Component[]{translationButtonPanel}, new int[]{3});		
		
		panelTemplate.addRow(m_cbReferenceIDs, m_btnDeleteTemplate);
		panelTemplate.addRow(new Component[]{templateURLPanel}, new int[2]);
		panelTemplate.addRow(new Component[]{templateButtonPanel}, new int[2]);
		
		// Operator Location ------------------------------
		
		panelOperatorLeft = new TitledGridBagPanel(JAPMessages.getString(MSG_TITLE_OP_GENERAL));
		constraints.gridy++;
		constraints.gridwidth = 1;
		add(panelOperatorLeft, constraints);

		// Street
		m_tf_general_Street = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_general_Street.setName(XMLPATH_TERMS_GENERAL_STREET);
		m_tf_general_Street.addFocusListener(this);
		panelOperatorLeft.addRow(createLabel(MSG_STREET_LABEL), m_tf_general_Street, null);

		// Postal code
		m_tf_general_Post = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_general_Post.setName(XMLPATH_TERMS_GENERAL_POSTCODE);
		m_tf_general_Post.addFocusListener(this);

		// City
		m_tf_general_City = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_general_City.setName(XMLPATH_TERMS_GENERAL_CITY);
		m_tf_general_City.addFocusListener(this);
		panelOperatorLeft.addRow(createLabel(MSG_ZIP_LABEL, MSG_CITY_LABEL), m_tf_general_Post, m_tf_general_City);
		
		// Fax
		m_tf_general_Fax = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_general_Fax.setName(XMLPATH_TERMS_GENERAL_FAX);
		m_tf_general_Fax.addFocusListener(this);
		panelOperatorLeft.addRow(createLabel(MSG_FAX_LABEL), m_tf_general_Fax, null);
		
		// VAT
		m_tf_general_VAT = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_general_VAT.setName(XMLPATH_TERMS_GENERAL_VAT);
		m_tf_general_VAT.addFocusListener(this);
		panelOperatorLeft.addRow(createLabel(MSG_VAT_LABEL), m_tf_general_VAT, null);
		
		// Venue
		m_tf_general_Venue = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_general_Venue.setName(XMLPATH_TERMS_GENERAL_VENUE);
		m_tf_general_Venue.addFocusListener(this);
		panelOperatorLeft.addRow(createLabel(MSG_VENUE_LABEL), m_tf_general_Venue, null);
		
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
		panelOperatorRight.addRow(createLabel(MSG_STREET_LABEL), m_tf_lang_Street, null);

		// Postal code
		m_tf_lang_Post = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_lang_Post.addFocusListener(this);

		// City
		m_tf_lang_City = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_lang_City.addFocusListener(this);
		panelOperatorRight.addRow(createLabel(MSG_ZIP_LABEL, MSG_CITY_LABEL), m_tf_lang_Post, m_tf_lang_City);
		
		// Fax
		m_tf_lang_Fax = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_lang_Fax.addFocusListener(this);
		panelOperatorRight.addRow(createLabel(MSG_FAX_LABEL), m_tf_lang_Fax, null);
		
		// VAT
		m_tf_lang_VAT = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_lang_VAT.addFocusListener(this);
		panelOperatorRight.addRow(createLabel(MSG_VAT_LABEL), m_tf_lang_VAT, null);
		
		// Venue
		m_tf_lang_Venue = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tf_lang_Venue.addFocusListener(this);
		panelOperatorRight.addRow(createLabel(MSG_VENUE_LABEL), m_tf_lang_Venue, null);	
						
		// Translations -------------------------------------------
		
		TitledGridBagPanel panelURLs = new TitledGridBagPanel(JAPMessages.getString(MSG_TITLE_URLS));
		
		constraints.gridx--;
		constraints.gridy++;
		constraints.gridwidth = 2;
		add(panelURLs, constraints);
		
		// URL privacy policy
		m_tfUrlPP = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tfUrlPP.addFocusListener(this);
		panelURLs.addRow(createLabel(MSG_PRIVACY_POLICY_LABEL), m_tfUrlPP, null);
		
		// URL legal opinions
		m_tfUrlLO = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tfUrlLO.addFocusListener(this);
		panelURLs.addRow(createLabel(MSG_LEGAL_OPINIONS_LABEL), m_tfUrlLO, null);
		
		// URL operational agreement
		m_tfUrlOA = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tfUrlOA.addFocusListener(this);
		panelURLs.addRow(createLabel(MSG_OP_AGREEMENT_LABEL), m_tfUrlOA, null);
		
		// Keep the panels in place -----------------------
		
		constraints.gridy++;
		constraints.weighty = 1;
		this.add(new JLabel(), constraints);
		
		PropertyToComponentMapping<JTextField> privacyPolicyMapping = null;
		PropertyToComponentMapping<JTextField> operationalAgreementsMapping = null;
		PropertyToComponentMapping<JTextField> legalOpinionsMapping = null;
		//PropertyToComponentMapping<JTextField> templateReferenceIdMapping = null;
		
		BeanInfo info;
		try 
		{
			info = Introspector.getBeanInfo(TermsAndConditionsTranslation.class);
			PropertyDescriptor[] pds = info.getPropertyDescriptors();
		
			for (int i = 0; i < pds.length; i++)
			{
				if(pds[i].getName().equals(TermsAndConditionsTranslation.PROPERTY_NAME_PRIVACY_POLICY))
				{
					privacyPolicyMapping = 
						new PropertyToComponentMapping<JTextField>(pds[i], m_tfUrlPP);
				}
				else if(pds[i].getName().equals(TermsAndConditionsTranslation.PROPERTY_NAME_OPERATIONAL_AGREEMENT))
				{
					operationalAgreementsMapping = 
						new PropertyToComponentMapping<JTextField>(pds[i], m_tfUrlOA);
				}
				else if(pds[i].getName().equals(TermsAndConditionsTranslation.PROPERTY_NAME_LEGAL_OPINIONS))
				{
					legalOpinionsMapping = 
						new PropertyToComponentMapping<JTextField>(pds[i], m_tfUrlLO);
				}
				/*else if(pds[i].getName().equals(TermsAndConditionsTranslation.PROPERTY_NAME_TEMPLATE_REFERENCE_ID))
				{
					templateReferenceIdMapping = 
						new PropertyToComponentMapping<JTextField>(pds[i], m_tf_templateReferenceId);
				}*/
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
		tcTranslationMappings = new PropertyToComponentMapping[]
        {
			//url mappings
			privacyPolicyMapping,
			operationalAgreementsMapping,
			legalOpinionsMapping,
        };
		                                                       
		tcAddressMappings = new PropertyToComponentMapping[]
		{ 
			//OperatorAddress property mappings
			new PropertyToComponentMapping<JTextField>
				(OperatorAddress.getDescriptor(OperatorAddress.PROPERTY_NAME_CITY), m_tf_lang_City),
			new PropertyToComponentMapping<JTextField>
				(OperatorAddress.getDescriptor(OperatorAddress.PROPERTY_NAME_STREET), m_tf_lang_Street),
			new PropertyToComponentMapping<JTextField>
				(OperatorAddress.getDescriptor(OperatorAddress.PROPERTY_NAME_POSTALCODE), m_tf_lang_Post),
			new PropertyToComponentMapping<JTextField>
				(OperatorAddress.getDescriptor(OperatorAddress.PROPERTY_NAME_FAX), m_tf_lang_Fax),
			new PropertyToComponentMapping<JTextField>
				(OperatorAddress.getDescriptor(OperatorAddress.PROPERTY_NAME_VAT), m_tf_lang_VAT),
			new PropertyToComponentMapping<JTextField>
				(OperatorAddress.getDescriptor(OperatorAddress.PROPERTY_NAME_VENUE), m_tf_lang_Venue)
		};
		
		m_cbReferenceIDs.addItemListener(this);
		m_cbTranslations.addItemListener(this);
		
		if (m_modelReferenceIDs.getSize() == 0) 
		{
			m_modelReferenceIDs.addElement(ComboBoxPlaceHolder.createPlaceHolder(MSG_PHOLDER_UPDATE));
			m_tf_templateURL.setEnabled(false);
		}
	}
		
	// -------------------- PUBLIC METHODS -----------------------
	
	/** Implement DateListener */
	public void dateChanged()
	{
		// Simply save the date
		this.saveDate();
	}
	
	/** Implement ItemListener */
	public void itemStateChanged(ItemEvent a_event)
	{
		// Catch events from the ReferenceIDs ComboBox
		if (a_event.getSource() == m_cbReferenceIDs)
		{
			if(a_event.getStateChange() == ItemEvent.SELECTED)
			{
				TemplateReferenceID refId = getSelectedTemplateReferenceID();
				// If a load process is currently running, ignore such events
				if (!this.loadingTemplate) 
				{ 
					TermsAndConditionsTranslation currentTrans = getSelectedTranslation();
					if(currentTrans != null)
					{
						currentTrans.setTemplateReferenceId((refId != null) ? refId.getReferenceID() : null);
						LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Set refId of translation "+currentTrans+
								" to "+currentTrans.getTemplateReferenceId());
					}
					boolean refIDselected = (refId != null);
					m_btnDeleteTemplate.setEnabled(refIDselected);
					m_tf_templateURL.setEnabled(refIDselected);
					saveAndLog();
				}
				m_tf_templateURL.setText( (refId != null) ? refId.getSource().toString() : ""); 
				setTransformButtonsEnabled(isTransformationPossible());
			}
		}
		else if (a_event.getSource() == m_cbTranslations)
		{
			TermsAndConditionsTranslation selectedTranslation = getSelectedTranslation();
			if( (a_event.getStateChange() == ItemEvent.SELECTED) &&
				(selectedTranslation != null) )
			{
				load(selectedTranslation);
				selectedLanguage = selectedTranslation.toString();
				//if the template of the selected language was not found:
				//try to set it to the currently selected template
				if(!selectTemplateReferenceID(selectedTranslation.getTemplateReferenceId()) )
				{
					TemplateReferenceID refId = getSelectedTemplateReferenceID();
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
				((TitledBorder)panelOperatorRight.getBorder()).setTitle(
						JAPMessages.getString(MSG_TITLE_OP_WITH_LANG)+" "+selectedLanguage);
				repaint();
			}
				
			if( m_cbTranslations.getItemCount() == 0)
			{
				m_cbTranslations.addItem(ComboBoxPlaceHolder.createPlaceHolder(MSG_PHOLDER_ADD_TRANSLATION));
			}
			panelOperatorRight.setEnabled(isATranslationLoaded());
			setTransformButtonsEnabled(isTransformationPossible());
		}
		else super.itemStateChanged(a_event);
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
			if (sPath.equals("Certificates/OperatorOwnCertificate"))
			{
				// Act accordingly
				LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Act accordingly " + sPath);
			}
		}	
	}
	
	/** Implement ActionListener */
	public void actionPerformed(ActionEvent ae) 
	{
		if ( ae.getSource() == m_btnInfoServiceTemplate )
		{
			// Update the vector of reference IDs
			this.updateReferenceIDs(m_modelReferenceIDs);
			// Get the attribute 'referenceId'
			//String refID = getConfiguration().getAttributeValue(m_cbReferenceIDs.getName(), "referenceId");
			TermsAndConditionsTranslation selectedTranslation = getSelectedTranslation();
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
		/*else if ( ae.getSource() == m_btnToday )
		{
			// Reset the DatePanel to today
			Date now = new Date(System.currentTimeMillis());
			m_pnlDate.setDate(now);
		}*/
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
		else if (ae.getSource() == m_btnCustomTemplate )
		{
			actionAddTemplate();
		}
		else if (ae.getSource() == m_btnDeleteTemplate )
		{
			actionDeleteTemplate();
		}
		/*else if (ae.getSource() == m_btnSetTemplateURL )
		{
			actionSetTemplatePath();
		}*/
		else if (ae.getSource() == m_btnExportTranslation )
		{
			actionExportTranslation();
		}
		saveAndLog();
	}
	
	private void actionAddTranslation()
	{
		TermsAndConditionsTranslation selectedTranslation = getSelectedTranslation();
		String selectedLangCode = (selectedTranslation != null) ? selectedTranslation.getLocale() : null;
		DialogResult<Locale> result = showAddTranslationsDialog(selectedLangCode);
		if( (result != null) && 
			(result.getResult() != null) &&
			(result.getCommand() != null) )
		{
			Locale locale = result.getResult();
			String cmd = result.getCommand();
			TermsAndConditionsTranslation t = null;
			if(cmd.equals(ACTION_CMD_ADD_TRANSLATION))
			{
				removeFirstPlaceHolder(m_cbTranslations);
				if(operatorTCs.hasTranslation(locale))
				{
					LogHolder.log(LogLevel.WARNING, LogType.MISC, "Warning translation "+locale.getDisplayLanguage()
							+" already exists");
				}
				else
				{
					LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Add translation "+locale.getDisplayLanguage());
					t = operatorTCs.initializeEmptyTranslation(locale);
					updateObject(m_cbTranslations, t, true);
					//set as default translation if there isn't already one defined.
					if(!operatorTCs.hasDefaultTranslation())
					{
						t.setDefaultTranslation(true);
					}
				}
			}
		}
	}
	
	private void actionPreviewTranslation()
	{
		Document rootConfigDoc = getConfiguration().getDocument();
		NodeList nl = rootConfigDoc.getElementsByTagName(XML_ELEMENT_TC_TRANSLATION_IMPORTS);
		TermsAndConditionsTranslation currentTranslation = getSelectedTranslation();
		//create a translation object with the values of the current
		//selected translation completed with the the general import values. 
		currentTranslation = (nl.getLength() > 0) ? 
				getSelectedTranslation().duplicateWithImports((Element) nl.item(0)) : getSelectedTranslation();
		TermsAndConditionsDialog.previewTranslation(MixConfig.getMainWindow(), currentTranslation, m_btnExportTranslation);
	}
	
	private void actionLoadTemplateFromFile()
	{
		//Load template from file
		JFileChooser fc = (lastOpened != null) ? new JFileChooser(lastOpened) : new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(true);
		int clicked = fc.showDialog(this, JAPMessages.getString(MSG_LOAD));
		switch ( clicked )
		{
			case JFileChooser.APPROVE_OPTION:
			{
				boolean somethingAdded = false;
				TermsAndConditionsFramework currentTemplate = null;
				File[] fs = fc.getSelectedFiles();
				for (File file : fs) 
				{
					try 
					{
						currentTemplate = new TermsAndConditionsFramework(file);
						Database.getInstance(TermsAndConditionsFramework.class).update(currentTemplate);
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
				setTransformButtonsEnabled(isTransformationPossible());
				
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
	
	private void actionAddTemplate()
	{
		DialogResult<TemplateReferenceID> dialogResult = showAddTemplateDialog();
		if(dialogResult != null && dialogResult.getResult() != null)
		{
			updateObject(m_cbReferenceIDs, dialogResult.getResult(), true);
		}
	}
	
	private void actionDeleteTemplate()
	{
		TemplateReferenceID refId = getSelectedTemplateReferenceID();
		if(refId  != null)
		{
			m_cbReferenceIDs.removeItem(refId);
			Database.getInstance(TermsAndConditionsFramework.class).remove(refId.getReferenceID());
			if(m_cbReferenceIDs.getItemCount() == 0)
			{
				m_cbReferenceIDs.addItem(ComboBoxPlaceHolder.createPlaceHolder(MSG_PHOLDER_UPDATE));
				m_tf_templateURL.setEnabled(false);
			}
		}
	}
	
	private void actionSetTemplatePath()
	{
		TemplateReferenceID refId = getSelectedTemplateReferenceID();
		if( (refId  != null) &&
			(m_tf_templateURL.getText() != null) &&
			!m_tf_templateURL.getText().equals(""))
		{
			
			TermsAndConditionsFramework template = null;
			try 
			{
				URL newTemplateUrl = new URL(m_tf_templateURL.getText());
				refId.setSource(newTemplateUrl);
				try {
					template =
						getTemplateFromURL(newTemplateUrl);
					if(template != null)
					{
						Database.getInstance(TermsAndConditionsFramework.class).update(template);
						setTransformButtonsEnabled(isTransformationPossible());
					}
				}
				//ignore: the template needs not to refer to a valid location
				catch (IOException e) {} 
				catch (XMLParseException e) {}
			} 
			catch (MalformedURLException e) 
			{
				m_tf_templateURL.setText(refId.getSource().toString());
			}
		}
	}
	
	private void actionDeleteTranslation()
	{
		TermsAndConditionsTranslation selectedTranslation = getSelectedTranslation();
		if(selectedTranslation != null)
		{
			LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Remove translation: "+selectedTranslation);
			operatorTCs.removeTranslation(selectedTranslation.getLocale());
			m_cbTranslations.removeItem(selectedTranslation);
			if( m_cbTranslations.getItemCount() == 0 )
			{
				m_cbTranslations.addItem(ComboBoxPlaceHolder.createPlaceHolder(MSG_PHOLDER_ADD_TRANSLATION));
			}
		}
	}
	
	private void actionDefaultTranslation()
	{
		TermsAndConditionsTranslation oldDefault = operatorTCs.getDefaultTranslation();
		TermsAndConditionsTranslation newDefault = getSelectedTranslation();
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
	
	private void actionExportTranslation()
	{
		//export template must never be null. in this case the button must be disabled
		//don't catch further null pointers, because they would be bugs and need to be fixed.
		TermsAndConditionsFramework exportTemplate = 
			(TermsAndConditionsFramework) Database.getInstance(TermsAndConditionsFramework.class).getEntryById(
				getSelectedTemplateReferenceID().getReferenceID());
		TermsAndConditionsTranslation exportTranslation = getSelectedTranslation();
		
		String suggestedFileName = 
			"Terms_"+operatorTCs.getOperator().getOrganization()+"_"+
				exportTranslation.getLocale()+".html";
		
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
					exportTemplate.importData(exportTranslation);
					try 
					{
						FileWriter exportFw = new FileWriter(selectedFile);
						exportTemplate.transform(exportFw);
						exportFw.flush();
						exportFw.close();
					} 
					catch (IOException e) 
					{
						JAPDialog.showErrorDialog(this, 
								JAPMessages.getString(MSG_SAVE_FILE_ERROR, selectedFile.getName()), 
								LogType.MISC, e);
					} 
					catch (TransformerException e) 
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
		if(!operatorTCs.hasTranslations())
		{
			messages.add("Warning: no translations defined. " +
					"Please specify at least one default translation. The mix won't start without.");
		}
		else if(!operatorTCs.hasDefaultTranslation())
		{
			messages.add("Warning: no default translation defined. " +
					"The mix won't start unless a default translation is defined.");
		}

		if(operatorTCs.getDate() == null)
		{
			messages.add("T&C container has no valid date set. This must not happen and seems to be a bug.");
		}
		
		Enumeration<TermsAndConditionsTranslation>
			allTrans = operatorTCs.getAllTranslations();
		TermsAndConditionsTranslation currentTrans = null;
		Method currentTransPropertyGetter = null;
		Object currentValue = null;
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
			 
			 for (PropertyToComponentMapping<JTextField> transMapping : tcTranslationMappings) 
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
			 }
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

	
	JLabel createLabel(String messageKey)
	{
		return createLabel(new String[]{messageKey});
	}
	
	JLabel createLabel(String messageKey1, String messageKey2)
	{
		return createLabel(new String[]{messageKey1, messageKey2});
	}
	
	JLabel createLabel(String[] messageKeys)
	{
		StringBuffer labelName = new StringBuffer("");
		for (int i = 0; i < messageKeys.length; i++) 
		{
			labelName.append(
					JAPMessages.getString(messageKeys[i])+
					((i < messageKeys.length - 1) ? "/" : ""));
		}
		return new JLabel(labelName.toString());
	}
	
	private void loadOparator() throws IOException, NotLoadedException
	{
		Document rootConfigDoc = getConfiguration().getDocument();
		NodeList nl = rootConfigDoc.getElementsByTagName(TermsAndConditions.XML_ELEMENT_NAME);
		String opCertFromConfig = getConfiguration().getValue("Certificates/OperatorOwnCertificate/X509Certificate");
		
		if((opCertFromConfig != null) && 
		   (opCertX509 != null) &&
		   (operatorTCs != null) && 
		   opCertFromConfig.equals(opCertX509) )
	   {
			LogHolder.log(LogLevel.INFO, LogType.MISC, "Don't reload operator T&Cs.");
			return;
	   }
		
		opCertX509 = opCertFromConfig;
		JAPCertificate operatorCertificate = (opCertX509 != null) ? JAPCertificate.getInstance(Base64.decode(opCertX509)) : null;
		if(operatorCertificate == null)
		{
			LogHolder.log(LogLevel.ERR, LogType.MISC, "No operator certificate can be found. "+
					"Cannot initiate any terms and conditions without operator certificate.");
			setEnabled(false);
			throw new NotLoadedException();
		}
		else
		{
			ServiceOperator operator = new ServiceOperator(operatorCertificate);
			try 
			{
				if(nl.getLength() > 0)
				{
					operatorTCs = new TermsAndConditions((Element) nl.item(0), operator);
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
						m_pnlDate.setDate(now);
					} catch (ParseException e) {
						throw new IOException("Wrong format of the newly created date. This should never happen!");
					}
				}
				setEnabled(true);
			}
		}
	}
	// -------------------- PROTECTED METHODS --------------------
	
	public void loadSpecifiedTemplates()
	{
		if( (getConfiguration() == null) || (getConfiguration().getDocument() == null) )
		{
			return;
		}
		
		NodeList nl = getConfiguration().getDocument().getElementsByTagName(XML_ELEMENT_TEMPLATE);
		Element currentElement = null;
		String currentTemplateURLValue = null;
		for (int i = 0; i < nl.getLength(); i++)
		{
			currentElement = (Element) nl.item(i);
			currentTemplateURLValue = currentElement.getTextContent();
			TermsAndConditionsFramework template = null;
			URL currentTemplateURL = null;
			try 
			{
				if(!currentElement.hasAttribute(IXMLEncodable.XML_ATTR_ID) ||
						currentTemplateURLValue == null	)
				{
					LogHolder.log(LogLevel.ERR, LogType.MISC, "Will not load template, because it is not specified correctly.");
					continue;
				}
				currentTemplateURL = new URL(currentTemplateURLValue);
				template = getTemplateFromURL(currentTemplateURL);
			} catch (MalformedURLException e) {
				LogHolder.log(LogLevel.ERR, LogType.MISC, "Could not load template, reason: "+e.getMessage());
			} catch (IOException e) {
				LogHolder.log(LogLevel.ERR, LogType.MISC, "Could not load template, reason: "+e.getMessage());
			} catch (XMLParseException e) {
				LogHolder.log(LogLevel.ERR, LogType.MISC, "Could not load template, reason: "+e.getMessage());
			} 
			finally
			{
				TemplateReferenceID refIdItem = null;
				if(template != null)
				{
					Database.getInstance(TermsAndConditionsFramework.class).update(template);
					if(!isTemplateReferenceIdInComboBox(template.getId()))
					{
						refIdItem = new TemplateReferenceID(template.getId(), currentTemplateURL);
						updateObject(m_cbReferenceIDs, refIdItem, false);
						setTransformButtonsEnabled(isTransformationPossible());
					}
				}
				else
				{
					refIdItem = new TemplateReferenceID(
							currentElement.getAttribute(IXMLEncodable.XML_ATTR_ID),
							currentTemplateURL);
					updateObject(m_cbReferenceIDs, refIdItem, false);
				}	
			}
		}
	}
	
	public synchronized void load() throws IOException
	{
		try 
		{
			loadSpecifiedTemplates();
			loadOparator();
			super.load();
			// Initially save the date if it is not yet there
			if (getConfiguration().getAttributeValue(XMLPATH_TERMS, "date") == null)
			{
				this.saveDate();	
			}
		} 
		catch (NotLoadedException e) 
		{
			LogHolder.log(LogLevel.WARNING, LogType.MISC, "T&C panel could not be loaded.");
		}
		
		catch(Exception e)
		{
			LogHolder.log(LogLevel.WARNING, LogType.MISC, "T&C panel could not be loaded.");
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO: Add general support for DatePanels to the load() method in MixConfigPanel.java?
	 * @param a_container
	 */
	protected void load(Container a_container) throws IOException
	{
		// Only the DatePanel is handled here
		if (a_container == m_pnlDate)
		{
			try {
				// Try to parse the date from the configuration
				String sDate = getConfiguration().getAttributeValue(XMLPATH_TERMS, "date");
				if (sDate != null)
				{
					//LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Loading date " + sDate);
					Date date = m_dateFormatter.parse(sDate);
					m_pnlDate.setDate(date);
				}
				else
				{
					// Do nothing?
				}
			} catch (ParseException pe) {
				LogHolder.log(LogLevel.EXCEPTION, LogType.MISC, "Error: " + pe.getMessage());
			}
		}
		else 
		{
			super.load(a_container);	
		}
	}
	
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
	
	private boolean selectTemplateReferenceID(String templateReferenceID)
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
		return (m_cbTranslations.getItemCount() > 1) ||
				((m_cbTranslations.getItemCount() == 1) && 
				 (m_cbTranslations.getItemAt(0) instanceof TermsAndConditionsTranslation));
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
	
	private boolean isTransformationPossible()
	{
		TermsAndConditionsFramework template = null;
		TermsAndConditionsTranslation currentTranslation = getSelectedTranslation();
		String templateRefID = null;
		if( currentTranslation != null)
		{
			templateRefID = currentTranslation.getTemplateReferenceId();
			if((templateRefID != null) &&
				!templateRefID.equals("") )
			{
				template = (TermsAndConditionsFramework)
								Database.getInstance(TermsAndConditionsFramework.class).getEntryById(templateRefID);
				return template != null;
			}
		}
		return false;
	}
	
	private void setTransformButtonsEnabled(boolean enabled)
	{
		m_btnExportTranslation.setEnabled(enabled);
		m_btnPreview.setEnabled(enabled);
	}
	
	private TermsAndConditionsTranslation getSelectedTranslation()
	{
			Object selected = m_cbTranslations.getSelectedItem();
			return (TermsAndConditionsTranslation) ((selected instanceof TermsAndConditionsTranslation) ? selected : null);
	}
	
	private TemplateReferenceID getSelectedTemplateReferenceID()
	{
			Object selected = m_cbReferenceIDs.getSelectedItem();
			return (TemplateReferenceID) ((selected instanceof TemplateReferenceID) ? selected : null);
	}
	
	/**
	 * Overwrite load() for JComboBoxes
	 */
	protected void load(JComboBox a_comboBox)
	{
		loadingTemplate = true;
		if (a_comboBox == m_cbReferenceIDs)
		{
			//will be explicitly done by loadSpecifiedTemplates
		}
		else if (a_comboBox == m_cbTranslations)
		{
			if(operatorTCs != null)
			{
				Enumeration<TermsAndConditionsTranslation> supportedLanguages = operatorTCs.getAllTranslations();
				TermsAndConditionsTranslation currentTrans = null;
				while(supportedLanguages.hasMoreElements())
				{
					currentTrans = supportedLanguages.nextElement();
					updateObject(m_cbTranslations, currentTrans, false);
					if(currentTrans.isDefaultTranslation())
					{
						m_cbTranslations.setSelectedItem(currentTrans);
						load(currentTrans);
					}
				}
				TermsAndConditionsTranslation selectedTranslation = getSelectedTranslation();
				selectedLanguage = (selectedTranslation != null) ? selectedLanguage.toString() : null;
				if(selectedLanguage != null)
				{
					((TitledBorder)panelOperatorRight.getBorder()).setTitle(
						JAPMessages.getString(MSG_TITLE_OP_WITH_LANG)+" "+selectedLanguage);
				}
			}
			
			if( m_cbTranslations.getItemCount() == 0)
			{
				m_cbTranslations.addItem(ComboBoxPlaceHolder.createPlaceHolder(MSG_PHOLDER_ADD_TRANSLATION));
			}
			panelOperatorRight.setEnabled(isATranslationLoaded());
			setTransformButtonsEnabled(isTransformationPossible());
			repaint();
		}
		else super.load(a_comboBox);
		loadingTemplate = false;
	}
	
	protected void load(TermsAndConditionsTranslation translation)
	{
		if(translation != null)
		{
			OperatorAddress addr = translation.getOperatorAddress();
			Method currentGetter = null;
			Object currentValue = null;
			
			for (int i = 0; i < tcTranslationMappings.length; i++) 
			{
				currentGetter = tcTranslationMappings[i].getPropertyDescriptor().getReadMethod();
				
				try {
					currentValue = currentGetter.invoke(translation, (Object[]) null);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				tcTranslationMappings[i].getComponent().setText(
						(currentValue != null) ? currentValue.toString() : "");
			}
			
			for (int i = 0; i < tcAddressMappings.length; i++) 
			{
				currentGetter = tcAddressMappings[i].getPropertyDescriptor().getReadMethod();
				try {
					currentValue = (addr != null) ? currentGetter.invoke(addr, (Object[]) null) : null;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				
				tcAddressMappings[i].getComponent().setText(
						(currentValue != null) ? currentValue.toString() : "");
			}
		}
	}
	
	DialogResult<TemplateReferenceID> showAddTemplateDialog()
	{
		final JAPDialog templateDialog = new JAPDialog(MixConfig.getMainWindow(), 
				JAPMessages.getString(MSG_ADD_TEMPLATE_DIALOG), true);
		Container contentPane = templateDialog.getContentPane();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		contentPane.setLayout(gridbag);
		
		final JLabel templateNameLabel = new JLabel("Name");
		final JTextField templateNameTextField = new MixConfigTextField(20);
		final JLabel templateUrlLabel = createLabel(MSG_URL, MSG_PATH);
		final JTextField templateUrlTextField = new MixConfigTextField(20);
		final JButton okButton = 
			new JButton(JAPMessages.getString(JAPMessages.getString(DialogContentPane.MSG_OK)));
		final JButton cancelButton = 
			new JButton(JAPMessages.getString(DialogContentPane.MSG_CANCEL));
		
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.ipadx = 2;
		constraints.ipady = 2;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(templateNameLabel, constraints);
		
		constraints.gridy = 1;
		gridbag.setConstraints(templateUrlLabel, constraints);
		
		constraints.gridy = 0;
		constraints.gridx = 1;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(templateNameTextField, constraints);
		
		constraints.gridy = 1;
		gridbag.setConstraints(templateUrlTextField, constraints);
		
		constraints.gridy = 2;
		constraints.gridx = GridBagConstraints.LINE_END - 1;
		constraints.gridwidth = 1;
		gridbag.setConstraints(cancelButton, constraints);
		
		constraints.gridx = GridBagConstraints.LINE_END;
		gridbag.setConstraints(okButton, constraints);
		templateDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		final DialogResult<TemplateReferenceID> result = new DialogResult<TemplateReferenceID>();
		
	
		KeyListener keyListener = new KeyListener()
		{
			public void keyPressed(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e)
			{
				String nameText = templateNameTextField.getText();
				String urlText = templateUrlTextField.getText();
				if( (nameText == null) ||
					(nameText.equals("")) ||
					(urlText == null) ||
					urlText.equals("") ||
					isTemplateReferenceIdInComboBox(nameText) )
				{
					okButton.setEnabled(false);
				}
				else
				{
					try 
					{
						new URL(templateUrlTextField.getText());
						okButton.setEnabled(true);
					} 
					catch (MalformedURLException e1) 
					{
						okButton.setEnabled(false);
					}
				}
			}
		};
		
		ActionListener actionlistener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(e.getSource() != cancelButton)
				{
					result.setCommand(e.getActionCommand());
				}
				if( (e.getSource() == okButton) &&
					(templateUrlTextField.getText() != null) &&
					!templateUrlTextField.getText().equals("") )
				{
					try 
					{
						result.setResult(
								new TemplateReferenceID(templateNameTextField.getText(),
								new URL(templateUrlTextField.getText())) );
					} 
					catch (MalformedURLException e1) 
					{
						return;
					}
				}
				templateDialog.dispose();
			}
		};
		
		okButton.setActionCommand(ACTION_CMD_OK);
		okButton.addActionListener(actionlistener);
		cancelButton.addActionListener(actionlistener);
		templateNameTextField.addKeyListener(keyListener);
		templateUrlTextField.addKeyListener(keyListener);
		templateUrlLabel.setText(JAPMessages.getString(MSG_ADD_TEMPLATE_DIALOG_LABEL));
		
		contentPane.add(templateNameLabel);
		contentPane.add(templateNameTextField);
		contentPane.add(templateUrlLabel);
		contentPane.add(templateUrlTextField);
		contentPane.add(cancelButton);
		contentPane.add(okButton);
	
		templateDialog.pack();
		templateDialog.setVisible(true);
		
		return (result.getCommand() != null) ? result : null;
	}
	
	DialogResult<Locale> showAddTranslationsDialog(String selected)
	{	
		
		final JAPDialog translationsDialog = 
			new JAPDialog(MixConfig.getMainWindow(), JAPMessages.getString(MSG_ADD_TRANSLATIONS_DIALOG), true);
		final JComboBox localeChoiceBox = new JComboBox();
		final DialogResult<Locale> result = new DialogResult<Locale>();
		
		translationsDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		translationsDialog.getContentPane().setLayout(new BorderLayout());
		
		JPanel langPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		
		langPanel.setLayout(new FlowLayout());
		buttonPanel.setLayout(new FlowLayout());
		
		JLabel langLabel = createLabel(MSG_AVAIL_LANG_LABEL);
		
		final JButton addButton = 
			new JButton(JAPMessages.getString(MSG_ADD));
		addButton.setActionCommand(ACTION_CMD_ADD_TRANSLATION);
		final JButton cancelButton = 
			new JButton(JAPMessages.getString(DialogContentPane.MSG_CANCEL));
		
		ItemListener itemListener = new ItemListener()
		{
			public void itemStateChanged(ItemEvent e) 
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					Object selectedItem = localeChoiceBox.getSelectedItem();
					if( (selectedItem != null) && (selectedItem instanceof String) )
					{
						String selectedLang = (String) selectedItem;
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
					  (localeChoiceBox.getSelectedItem() instanceof String) )
				{
					result.setCommand(e.getActionCommand());
					result.setResult(new Locale((String)localeChoiceBox.getSelectedItem()));
				}
				translationsDialog.setVisible(false);
			}	
		};
		addButton.addActionListener(buttonListener);
		cancelButton.addActionListener(buttonListener);
		
		localeChoiceBox.addItemListener(itemListener);
		
		for (String localeString : LOCALE_CHOICE) 
		{
			localeChoiceBox.addItem(localeString);
		}
		
		langPanel.add(langLabel);
		langPanel.add(localeChoiceBox);
		
		buttonPanel.add(cancelButton);
		buttonPanel.add(addButton);
		translationsDialog.getContentPane().add(langPanel, BorderLayout.CENTER);
		translationsDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		translationsDialog.pack();
		translationsDialog.setVisible(true);
		return ((result.getCommand() != null) && result.getResult() != null) ? result : null;
	}
	
	/* save and log the Excpetion */
	private synchronized void saveAndLog()
	{
		try 
		{
			TermsAndConditionsTranslation selectedTranslation = getSelectedTranslation();
			if(selectedTranslation != null)
			{
				save(selectedTranslation);
				save();
			}
		} catch (IOException e) {
			LogHolder.log(LogLevel.WARNING, LogType.MISC, "Could not save the T&C configuration, reason: "+
					e.getMessage());
		}
	}
	
	public void save() throws IOException
	{
		MixConfiguration config = getConfiguration();
		if(operatorTCs != null)
		{
			Element element = operatorTCs.createXMLOutput(config.getDocument());
			if(element == null)
			{
				element = operatorTCs.createTCRoot(config.getDocument());
			}
			config.setValue(XMLPATH_TERMS_OPTIONS, element);
		}
		save(this);
	}
	
	/**
	 * TODO: Add support for DatePanels to the save() method in MixConfigPanel?
	 * 
	 * @param a_container
	 */
	protected void save(Container a_container) throws IOException
	{
		// Only m_pnlDate is handled here quickly
		if (a_container == m_pnlDate)
		{
			this.saveDate();
		}
		else 
		{
			super.save(a_container);
		}
	}
	
	
	/**
	 * Overwrite save() for JComboBoxes
	 */
	protected void save(JComboBox a_comboBox) 
	{
		try
		{
			if (a_comboBox == m_cbReferenceIDs)
			{
				if( (m_cbReferenceIDs.getItemCount() > 0) && 
					!(m_cbReferenceIDs.getSelectedItem() instanceof ComboBoxPlaceHolder) )
				{
					//Save the URLS from where the templates are loaded.
					Document doc = getConfiguration().getDocument();
					Element templatesRoot = doc.createElement(XML_ELEMENT_TEMPLATES);
					Element currentTemplateElement = null;
					Object currentItem = null;
					TemplateReferenceID currentRefId = null;
					
					for (int i = 0; i < m_cbReferenceIDs.getItemCount(); i++) 
					{
						currentItem = m_cbReferenceIDs.getItemAt(i);
						if( (currentItem != null) &&
							(currentItem instanceof TemplateReferenceID) )
						{
							currentRefId = 
								(TemplateReferenceID) m_cbReferenceIDs.getItemAt(i);
							currentTemplateElement = currentRefId.toXMLElement(doc);
							currentTemplateElement.setAttribute(IXMLEncodable.XML_ATTR_ID, currentRefId.getReferenceID());
							templatesRoot.appendChild(currentTemplateElement);
						}
					}
					NodeList nl = doc.getElementsByTagName(XML_ELEMENT_TEMPLATES);
					if(nl.getLength() > 0)
					{
						nl.item(0).getParentNode().replaceChild(templatesRoot, nl.item(0));
					}
					else
					{
						NodeList nl2 = doc.getElementsByTagName(XML_ELEMENT_TC_OPTIONS);
						if(nl2.getLength() > 0)
						{
							nl2.item(0).appendChild(templatesRoot);
						}
					}
				}
			}
			else if (a_comboBox == m_cbTranslations)
			{
				//prevent the super class from handling this case. This would destroy the config.
			}
			else super.save(a_comboBox);
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	protected void enableComponents() 
	{
		// TODO Implement this
	}
	
	// -------------------- PRIVATE METHODS ----------------------
	
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
	
	/** loads a TC template from the correpsonding URL saves it in the IS Database
	 * puts it's ReferenceID in the corrresponding combobox and returns this 
	 * ReferenceID
	 * @param templateURL
	 * @return
	 * @throws IOException 
	 * @throws XMLParseException 
	 */
	private TermsAndConditionsFramework getTemplateFromURL(URL templateURL) throws IOException, XMLParseException
	{
		URLConnection currentConn = templateURL.openConnection();
		InputStream inputStream = null;
		
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
		if(currentConn instanceof HttpURLConnection )
		{
			((HttpURLConnection)currentConn).disconnect();
		}
		TermsAndConditionsFramework template = 
			new TermsAndConditionsFramework(templateDoc.getDocumentElement());
		
		return template;
	}
	
	/**
	 * Update the internal (static) model containing terms and condition frameworks. 
	 * Therefore, establish a connection to a known InfoService and request reference
	 * IDs of all currently known terms and conditions frameworks.
	 */
	private void updateReferenceIDs(DefaultComboBoxModel refIDs)
	{
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Getting available 'Terms and Conditions Frameworks'");
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
			    URL requestURL = new URL("http", host, port, "/tcframeworks");
				HttpURLConnection conn = (HttpURLConnection) requestURL.openConnection();
				// Create a serials document from the InputStream
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				
				Document serialsDoc = db.parse(conn.getInputStream());
				// Disconnect
				conn.disconnect();
				// Get the single nodes
				NodeList nl = serialsDoc.getDocumentElement().getElementsByTagName(
						TermsAndConditionsFramework.XML_ELEMENT_NAME);
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
					TermsAndConditionsFramework currentTemplate = null;
					// Insert serials into the vector
					for (int i = 0; i < nl.getLength(); i++)
					{
						currentTemplateRefID = ((Element)nl.item(i)).getAttribute(IXMLEncodable.XML_ATTR_ID);
						currentTemplateURL = new URL("http", host, port, "/tcframework/"+currentTemplateRefID);
						currentTemplate = getTemplateFromURL(currentTemplateURL);
						if(currentTemplate != null)
						{
							Database.getInstance(TermsAndConditionsFramework.class).update(currentTemplate);
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
		    	LogHolder.log(LogLevel.EXCEPTION, LogType.NET, "Error: " + ioe.getMessage(), ioe);
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
		// In case none is available, add dummy ..
		if (refIDs.getSize() == 0)
		{
			refIDs.addElement(
			ComboBoxPlaceHolder.createPlaceHolder(MSG_PHOLDER_NONE_AVAIL));
		}
		else
		{
			setTransformButtonsEnabled(isTransformationPossible());
		}
	}
	
	public void focusGained(FocusEvent e){}
	public void focusLost(FocusEvent e)
	{
		if(e.getSource() == m_tf_templateURL)
		{
			actionSetTemplatePath();
		}
		saveAndLog();
	}
	
	/**
	 * Save the date to the "Date" element as well as to the "serial" attribute
	 */
	private void saveDate()
	{
		// Get the date from the panel as Calendar object
		Calendar cal = m_pnlDate.getCalendar();		
		// Use DateFormat to store the date to the configuration
		String sDate = m_dateFormatter.format(cal.getTime());
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Saving the date: " + sDate);
		operatorTCs.setDate(m_pnlDate.getDate());
		//getConfiguration().setAttribute(XMLPATH_TERMS, "date", sDate);
	}
	
	private void save(TermsAndConditionsTranslation translation)
	{
		if(translation != null)
		{
			OperatorAddress addr = translation.getOperatorAddress();
			Method currentSetter = null;
			String currentValue = null;
			
			for (int i = 0; i < tcTranslationMappings.length; i++) 
			{
				currentSetter = tcTranslationMappings[i].getPropertyDescriptor().getWriteMethod();
				currentValue = tcTranslationMappings[i].getComponent().getText();
				
				try {
					currentSetter.invoke(translation, new Object[]{currentValue});
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			
			if(addr == null)
			{
				addr = new OperatorAddress();
				translation.setOperatorAddress(addr);
			}
			
			for (int i = 0; i < tcAddressMappings.length; i++) 
			{
				currentSetter = tcAddressMappings[i].getPropertyDescriptor().getWriteMethod();
				currentValue = tcAddressMappings[i].getComponent().getText();
				
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
	 * Class representing a refernce to a Template
	 * A Template refernce consistrs of the 
	 * corresponding template ID and the URL
	 * from where it was loaded.
	 */
	private class TemplateReferenceID
	{
		private String referenceID;
		private URL source;
		
		private TemplateReferenceID()
		{	
			this.referenceID = null;
			this.source = null;
		}
		
		private TemplateReferenceID(String referenceID, URL source)
		{	
			this.referenceID = referenceID;
			this.source = source;
		}
		
		public String getReferenceID() 
		{
			return referenceID;
		}

		public void setReferenceID(String referenceID) 
		{
			this.referenceID = referenceID;
		}

		public URL getSource() {
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
			return ((TemplateReferenceID)otherRefid).referenceID.equals(referenceID);
		}
		
		public String toString()
		{
			return this.referenceID;
		}
		
		public Element toXMLElement(Document doc)
		{
			Element templateElement = doc.createElement(XML_ELEMENT_TEMPLATE);
			templateElement.setTextContent(source.toString());
			return templateElement;
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
}