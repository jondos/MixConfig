package mixconfig.panels;

import gui.DateListener;
import gui.DatePanel;
import gui.MixConfigTextField;
import gui.TitledGridBagPanel;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
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
import mixconfig.infoservice.InfoServiceData;
import mixconfig.infoservice.InfoServiceTableModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This panel is used by mix operators to enter customized 
 * information to the standard terms and conditions.
 * 
 * @author renner
 */
public class TermsAndConditionsPanel extends MixConfigPanel implements ActionListener, DateListener, ChangeListener {
	
	/** Additional operator info */
	public static final String XMLPATH_OPERATOR_STREET = OwnCertificatesPanel.XMLPATH_OPERATOR + "/Street";
	public static final String XMLPATH_OPERATOR_POSTCODE = OwnCertificatesPanel.XMLPATH_OPERATOR + "/PostalCode";
	public static final String XMLPATH_OPERATOR_CITY = OwnCertificatesPanel.XMLPATH_OPERATOR + "/City";
	public static final String XMLPATH_OPERATOR_FAX = OwnCertificatesPanel.XMLPATH_OPERATOR + "/Fax";
	public static final String XMLPATH_OPERATOR_VAT = OwnCertificatesPanel.XMLPATH_OPERATOR + "/VAT";
	public static final String XMLPATH_OPERATOR_VENUE = OwnCertificatesPanel.XMLPATH_OPERATOR + "/Venue";
	
	/** Terms and conditions paths */
	public static final String XMLPATH_TERMS_LIST = "TermsAndConditionsList";
	public static final String XMLPATH_TERMS = XMLPATH_TERMS_LIST + "/TermsAndConditions";
	public static final String XMLPATH_TERMS_URLPP = XMLPATH_TERMS + "/PrivacyPolicyUrl";
	public static final String XMLPATH_TERMS_URLLO = XMLPATH_TERMS + "/LegalOpinionsUrl";
	public static final String XMLPATH_TERMS_URLOA = XMLPATH_TERMS + "/OperationalAgreementUrl";
	
	/** ComboBox and Model */
	private DefaultComboBoxModel m_modelReferenceIDs = new DefaultComboBoxModel();
	private JComboBox m_cbReferenceIDs = new JComboBox(m_modelReferenceIDs);
	private JButton m_btnUpdate;
	
	/** Update currently running */
	private boolean m_bUpdate = false;
	
	/** Date panel + button */
	private DatePanel m_pnlDate;
	private JButton m_btnToday;
	
	/** TextFields */
	private JTextField m_tfStreet;
	private JTextField m_tfPost;
	private JTextField m_tfCity;
	private JTextField m_tfVAT;
	private JTextField m_tfFax;
	private JTextField m_tfVenue;
	private JTextField m_tfUrlPP;
	private JTextField m_tfUrlLO;
	private JTextField m_tfUrlOA;
		
	/** The DateFormat that we use here */
	DateFormat m_dateFormatter = new SimpleDateFormat("yyyyMMdd");
	
	/** Constructor */
	public TermsAndConditionsPanel()
	{
		// Initial stuff
		super("Terms");
		GridBagConstraints constraints = getInitialConstraints();
		
		// Listen for ConfigurationEvents
		MixConfig.getMixConfiguration().addChangeListener(this);
		
		// Reference terms & date -------------------------
		
		TitledGridBagPanel panelTAC = new TitledGridBagPanel("Terms and Conditions");
		constraints.gridwidth = 2;
		add(panelTAC, constraints);

		// Do not call updateSerials() in the constructor
		m_cbReferenceIDs.setName(XMLPATH_TERMS);
		m_cbReferenceIDs.addItemListener(this);
		if (m_modelReferenceIDs.getSize() == 0) m_modelReferenceIDs.addElement("Please update ..");
		// Add the update button
		m_btnUpdate = new JButton("Update");
		m_btnUpdate.setActionCommand(m_btnUpdate.getText());
		m_btnUpdate.addActionListener(this);
		panelTAC.addRow(new JLabel("Reference ID"), m_cbReferenceIDs, m_btnUpdate);

		// The current date
		Date now = new Date(System.currentTimeMillis());
		m_pnlDate = new DatePanel(now);
		m_pnlDate.addDateListener(this);
		
		// Add the 'Today' button
		m_btnToday = new JButton("Today");
		m_btnToday.setActionCommand(m_btnToday.getText());
		m_btnToday.addActionListener(this);
		panelTAC.addRow(new JLabel("Current Date"), m_pnlDate, m_btnToday);
						
		// Operator Location ------------------------------
		
		TitledGridBagPanel panelOperatorLeft = new TitledGridBagPanel("Operator Address");
		constraints.gridy++;
		constraints.gridwidth = 1;
		add(panelOperatorLeft, constraints);

		// Street
		m_tfStreet = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tfStreet.setName(XMLPATH_OPERATOR_STREET);
		m_tfStreet.addFocusListener(this);
		panelOperatorLeft.addRow(new JLabel("Street"), m_tfStreet, null);

		// Postal code
		m_tfPost = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tfPost.setName(XMLPATH_OPERATOR_POSTCODE);
		m_tfPost.addFocusListener(this);
		panelOperatorLeft.addRow(new JLabel("Postal Code"), m_tfPost, null);

		// City
		m_tfCity = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tfCity.setName(XMLPATH_OPERATOR_CITY);
		m_tfCity.addFocusListener(this);
		panelOperatorLeft.addRow(new JLabel("City"), m_tfCity, null);
		
		// Operator information ---------------------------
		
		TitledGridBagPanel panelOperatorRight = new TitledGridBagPanel("Additional Information");
		constraints.gridx++;
		constraints.fill = GridBagConstraints.BOTH;
		add(panelOperatorRight, constraints);
		// Reset fill to HORIZONTAL
		constraints.fill = GridBagConstraints.HORIZONTAL;
		
		// Fax
		m_tfFax = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tfFax.setName(XMLPATH_OPERATOR_FAX);
		m_tfFax.addFocusListener(this);
		panelOperatorRight.addRow(new JLabel("Fax"), m_tfFax, null);
		
		// VAT
		m_tfVAT = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tfVAT.setName(XMLPATH_OPERATOR_VAT);
		m_tfVAT.addFocusListener(this);
		panelOperatorRight.addRow(new JLabel("VAT"), m_tfVAT, null);
		
		// Venue
		m_tfVenue = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tfVenue.setName(XMLPATH_OPERATOR_VENUE);
		m_tfVenue.addFocusListener(this);
		panelOperatorRight.addRow(new JLabel("Venue"), m_tfVenue, null);
						
		// URLs -------------------------------------------
		
		TitledGridBagPanel panelURLs = new TitledGridBagPanel("URLs");
		constraints.gridx--;
		constraints.gridy++;
		constraints.gridwidth = 2;
		add(panelURLs, constraints);
		
		// URL privacy policy
		m_tfUrlPP = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tfUrlPP.setName(XMLPATH_TERMS_URLPP);
		m_tfUrlPP.addFocusListener(this);
		panelURLs.addRow(new JLabel("Privacy Policy"), m_tfUrlPP, null);
		
		// URL legal opinions
		m_tfUrlLO = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tfUrlLO.setName(XMLPATH_TERMS_URLLO);
		m_tfUrlLO.addFocusListener(this);
		panelURLs.addRow(new JLabel("Legal Opinions"), m_tfUrlLO, null);
		
		// URL operational agreement
		m_tfUrlOA = new MixConfigTextField(MAX_COLUMN_LENGTH);
		m_tfUrlOA.setName(XMLPATH_TERMS_URLOA);
		m_tfUrlOA.addFocusListener(this);
		panelURLs.addRow(new JLabel("Operational Agreement"), m_tfUrlOA, null);
		
		// Keep the panels in place -----------------------
		
		constraints.gridy++;
		constraints.weighty = 1;
		this.add(new JLabel(), constraints);
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
			// If an update is currently running, ignore such events
			if (!this.m_bUpdate && a_event.getStateChange() == ItemEvent.SELECTED) 
			{ 
				// Call this class's method to save
				//LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Saving the ReferenceID");
				this.save(m_cbReferenceIDs);
			}
			//else LogHolder.log(LogLevel.DEBUG, LogType.MISC, "ComboBox-Action IGNORED!");
		}
		else super.itemStateChanged(a_event);
	}
	
	/** Implement ActionListener */
	public void actionPerformed(ActionEvent ae) 
	{
		if (ae.getActionCommand().equals("Update"))
		{
			// Update the vector of reference IDs
			this.updateReferenceIDs(m_modelReferenceIDs);
			// Get the attribute 'referenceId'
			String refID = getConfiguration().getAttributeValue(m_cbReferenceIDs.getName(), "referenceId");
			if (refID != null && m_modelReferenceIDs.getIndexOf(refID) != -1)
			{	
				// refID is in the model ..
				LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Model contains refID");
				m_cbReferenceIDs.setSelectedItem(refID);
			}
			else
			{
				// Set the ComboBox to the first item
				m_cbReferenceIDs.setSelectedItem(m_modelReferenceIDs.getElementAt(0));
				// XXX: Why do we need to save the ComboBox by hand here?
				this.save(m_cbReferenceIDs);
			}
		}
		else if (ae.getActionCommand().equals("Today"))
		{
			// Reset the DatePanel to today
			Date now = new Date(System.currentTimeMillis());
			m_pnlDate.setDate(now);
		}
		else LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Unknown action command " + ae.getActionCommand());
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
			//LogHolder.log(LogLevel.DEBUG, LogType.MISC, "The modified XML-path is " + sPath);
			if (sPath.equals("Certificates/OperatorOwnCertificate"))
			{
				// Act accordingly
				LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Act accordingly " + sPath);
			}
		}	
	}
	
	/** Implement abstract methods from MixConfigPanel */
	public Vector<String> check() 
	{
		// TODO: Implement some checks
		return new Vector<String>();
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
	
	// -------------------- PROTECTED METHODS --------------------
	
	public void load() throws IOException
	{
		super.load();
		// Initially save the date if it is not yet there
		if (getConfiguration().getAttributeValue(XMLPATH_TERMS_LIST, "date") == null)
		{
			this.saveDate();	
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
				String sDate = getConfiguration().getAttributeValue(XMLPATH_TERMS_LIST, "date");
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
	
	/**
	 * Overwrite load() for JComboBoxes
	 */
	protected void load(JComboBox a_comboBox)
	{
		if (a_comboBox == m_cbReferenceIDs)
		{
			String value = getConfiguration().getAttributeValue(m_cbReferenceIDs.getName(), "referenceId");
			if (value != null)
			{
				// The attribute 'referenceId' was found, so modify the ComboBox
				this.setReferenceID(value);			
			}
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
		if (a_comboBox == m_cbReferenceIDs)
		{
			Object item = m_cbReferenceIDs.getSelectedItem();
			if (item == null || getConfiguration() == null)
			{
				LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Not saving, configuration or selected item is null");
			}
			// Only set the attribute under certain conditions
			else if (!item.equals("None available"))
			{
				getConfiguration().setAttribute(m_cbReferenceIDs.getName(), "referenceId", item.toString());		
			}
			else 
			{
				// TODO: Think about what to do here, maybe remove the attribute?
				LogHolder.log(LogLevel.DEBUG, LogType.MISC, "TODO: Selected item is '" + item + "'");
			}
		}
		else super.save(a_comboBox);
	}
	
	protected void enableComponents() 
	{
		// TODO Implement this
	}
	
	// -------------------- PRIVATE METHODS ----------------------
	
	/**
	 * Set the Reference ID ComboBox to a given value
	 */
	private void setReferenceID(String value)
	{
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Setting ComboBox (ReferenceID) to " + value);
		// Clear the vector and add the given value
		m_modelReferenceIDs.removeAllElements();
		m_modelReferenceIDs.addElement(value);
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
	
	/**
	 * Update the internal (static) model containing terms and condition frameworks. 
	 * Therefore, establish a connection to a known InfoService and request reference
	 * IDs of all currently known terms and conditions frameworks.
	 */
	private void updateReferenceIDs(DefaultComboBoxModel refIDs)
	{
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Getting available 'Terms and Conditions Frameworks'");
		// This is used to suppress certain events
		this.m_bUpdate = true;
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
		refIDs.removeAllElements();
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
			    URL requestURL = new URL("http", host, port, "/tcframeworkserials");
				HttpURLConnection conn = (HttpURLConnection) requestURL.openConnection();
				// Create a serials document from the InputStream
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document serialsDoc = db.parse(conn.getInputStream());
				// Disconnect
				conn.disconnect();
				// Get the single nodes
				NodeList nl = serialsDoc.getDocumentElement().getElementsByTagName("TermsAndConditionsFramework");
				if (nl.getLength() == 0)
				{
					LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Failed ...");
				}
				else if (nl.getLength() > 0)
				{
					success = true;
					// Insert serials into the vector
					for (int i = 0; i < nl.getLength(); i++)
					{
						Node att = nl.item(i).getAttributes().getNamedItem("id"); 
						if (att != null)
						{
							String value = att.getNodeValue();
							LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Adding " + value);
							refIDs.addElement(value);						
						}
						else 
						{
							LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Attribute is " + att);
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
		    }
		    // Try the next InfoService
		    counter++;
		}
		// In case none is available, add dummy ..
		if (refIDs.getSize() == 0) refIDs.addElement("None available");
		this.m_bUpdate = false;
	}
	
	/**
	 * Save the date to the "Date" element as well as to the "serial" attribute
	 */
	private void saveDate()
	{
		// Get the date from the panel as Calendar object
		Calendar cal = m_pnlDate.getDate();		
		// Use DateFormat to store the date to the configuration
		String sDate = m_dateFormatter.format(cal.getTime());
		LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Saving the date: " + sDate);
		getConfiguration().setAttribute(XMLPATH_TERMS_LIST, "date", sDate);
	}
}