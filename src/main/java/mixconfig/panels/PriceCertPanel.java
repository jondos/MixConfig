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

package mixconfig.panels;

import gui.ClipFrame;
import gui.GUIUtils;
import gui.dialog.DialogContentPane;
import gui.dialog.DialogContentPaneOptions;
import gui.dialog.JAPDialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import mixconfig.ConfigurationEvent;
import mixconfig.MixConfig;
import mixconfig.MixConfiguration;
import anon.pay.xml.XMLPriceCertificate;
import anon.util.JAPMessages;

/**
 * JPanel-representation of a {@link XMLPriceCertificate}:
 *
 * Load and Store a price certificate as part of a {@link MixConfiguration}
 *
 * @author Elmar Schraml
 * @author Johannes Renner
 */
public class PriceCertPanel extends JPanel implements ActionListener, ChangeListener
{
	// XML-paths
	public static final String XMLPATH_PRICECERT = PaymentPanel.XMLPATH_ACCOUNTING+"/"+XMLPriceCertificate.XML_ELEMENT_NAME;
	
	// Messages
	private static final String MSG_CHOOSE_IMPORT_METHOD = PriceCertPanel.class.getName()+"_chooseImportMethod";
	private static final String MSG_CONFIRM_DELETION = PriceCertPanel.class.getName() + "_confirmDeletion";
	
	// The price certificate
	private XMLPriceCertificate m_cert;
	
	// List of ChangeListeners
	private Vector<ChangeListener> m_changeListeners = new Vector<ChangeListener>();
	
	// GUI components: Buttons
	private JButton m_bttnImport;
	private JButton m_bttnUpdate;
	private JButton m_bttnRemove;
	
	// Labels
	private JLabel m_rate;
	
	/**
	 * Constructor
	 * 
	 * @param name
	 * @param toolTip
	 * @param priceCert
	 */
	public PriceCertPanel(String name, String toolTip, XMLPriceCertificate priceCert)
	{
		// Set the certificate
		m_cert = priceCert;
		
		// Listen for ConfigurationEvents
		MixConfig.getMixConfiguration().addChangeListener(this);
		
		// Construct the components
		setBorder(new TitledBorder(name));
		setToolTipText(toolTip);
		
		// Create the buttons
		m_bttnImport = new JButton("Import");
		m_bttnImport.addActionListener(this);
		m_bttnUpdate = new JButton("Update");
		m_bttnUpdate.addActionListener(this);
		m_bttnRemove = new JButton("Remove");
		m_bttnRemove.addActionListener(this);
		
		// Layout and initial constraints
		this.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5,5,5,5);
		constraints.anchor = GridBagConstraints.NORTHWEST;		
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = constraints.gridy = 0;
		constraints.weightx = 0;
		constraints.weighty = 1;
		
		// Button row
		this.add(m_bttnImport, constraints);
		constraints.gridx++;
		this.add(m_bttnUpdate, constraints);
		constraints.gridx++;
		this.add(m_bttnRemove, constraints);
		
		// From now on: center everything
		constraints.anchor = GridBagConstraints.CENTER;
		
		// Spacer row (gridy=1)
		constraints.gridx = 1;
		constraints.gridy = 1;
		this.add(new JLabel(), constraints); //spacer		
		
		// Price indication row (gridy=2)
		JLabel rateLabel = new JLabel("Price per MB: ");
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		this.add(rateLabel, constraints);

		m_rate = new JLabel("");
		constraints.gridx = 2;
		constraints.gridy = 2;
		constraints.gridwidth = 2;
		this.add(m_rate, constraints);
		
		// Spacer
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 2;
		constraints.gridy = 3;
		constraints.gridwidth = 2;
		constraints.weightx = 1;
		//constraints.weighty = 0;
		this.add(new JLabel(), constraints);
		
		this.showPrice(m_cert);
		this.setVisible(true);
	}

	/**
	 * Actions here:
	 */
	public void actionPerformed(ActionEvent a_e)
	{
		if (a_e.getSource() == m_bttnImport)
		{
			// Try to import a certificate
			try 
			{
				if (importCert())
				{
					LogHolder.log(LogLevel.INFO, LogType.PAY, "Successfully imported price certificate");
					this.fireStateChanged();
				}
			}
			catch (IOException ioe)
			{
				LogHolder.log(LogLevel.ERR, LogType.PAY, "Error while importing certificate: " + ioe.getMessage());	
			}
		}
		else if (a_e.getSource() == m_bttnUpdate)
		{
			// TODO: Show dialog?
			LogHolder.log(LogLevel.DEBUG, LogType.PAY, "TODO: Show a PriceCertDialog, update the certificate!");
			
			//String mixId = MixConfig.getMixConfiguration().getValue(MixConfig.XMLPATH_SUBJECTKEYIDENTIFIER);
			//PriceCertDialog thePriceCertDialog = new PriceCertDialog(this,"Price Certificates",mixId );
			//actually setting the PriceCert to the current mixconfig is handled by the dialog
		}
		else if (a_e.getSource() == m_bttnRemove)
		{
			if (JAPDialog.showConfirmDialog(this, JAPMessages.getString(MSG_CONFIRM_DELETION),
					JAPDialog.OPTION_TYPE_OK_CANCEL,
					JAPDialog.MESSAGE_TYPE_QUESTION) == JAPDialog.RETURN_VALUE_OK)
			{
				// Remove price certificate from configuration by removing it from the panel
				this.removeCert();
			}
		}
		enableButtons();
	}

	/** 
	 * Import an X.509 public certificate. The user is prompted to give the name of a
	 * file from which to import. If that fails, the user is prompted to paste the
	 * certificate from the system clipboard.
	 * 
	 * @return True if certificate was imported successful; False otherwise
	 * @throws IOException If an error occurs while reading the certificate.
	 */
	private boolean importCert() throws IOException
	{
		byte[] cert = null;
		JAPDialog dialog = new JAPDialog(this, "Import Price Certificate");
		ChooseImportMethodPane pane = new ChooseImportMethodPane(dialog, JAPMessages.getString(MSG_CHOOSE_IMPORT_METHOD));
		pane.updateDialog();
		dialog.pack();
		dialog.setResizable(false);
		dialog.setVisible(true);
		if (pane.getButtonValue() != DialogContentPane.RETURN_VALUE_OK)
		{
			return false;
		}
		if (pane.isMethodFile())
		{
			// Certificate can be imported from an XML-file
			cert = MixConfig.openFile(this, MixConfig.FILTER_XML);

			if (cert == null)
			{
				return false;
			}
		}
		else
		{
			cert = GUIUtils.getTextFromClipboard(this).getBytes();
		}
		this.setCert(cert);
		return (m_cert != null);
	}
		
	/*
	public void updateCert()
	{
		//let user enter new price to be submitted to BI
		//ask for confirmation, since changing the price will void the price cert and require the PI to sign the new one
		String warningMessage = getString(MSG_CONFIRM_CHANGE);
		int retVal = JAPDialog.showConfirmDialog(this,warningMessage,JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
		if (retVal == JOptionPane.CANCEL_OPTION)
		{
			//do nothing = keep old price
		} else if (retVal == JOptionPane.OK_OPTION )
		{
			//ask user for new price
			double newRate = 0.0;
			String askUser = getString(MSG_ENTER_PRICE);
			String title = getString(MSG_NEW_PRICE_TITLE);
			enterANumber: {
				//String newRateString = JOptionPane.showInputDialog(this,askUser,title,JOptionPane.QUESTION_MESSAGE);
				String newRateString = JOptionPane.showInputDialog(askUser);
				//abort if user clicked cancel
				if (newRateString == null)
				{
					return;
				}
				try
				{
					newRate = Double.parseDouble(newRateString);
				}
				catch (NumberFormatException e) //no double entered
				{
					JAPDialog.showMessageDialog(this, MSG_NO_DOUBLE);
					break enterANumber;
				}
				if (newRate == 0.0 || newRate < 0)
				{
					break enterANumber;
				}
			}
			//build new price cert (like current one, but with != rate), and submit to PI
			String mix = MixConfig.getMixConfiguration().getValue(MixConfig.XMLPATH_SUBJECTKEYIDENTIFIER);
			String biId = MixConfig.getMixConfiguration().getValue(MixConfig.XMLPATH_PIID);
			XMLPriceCertificate newPriceCert = new XMLPriceCertificate(mix,newRate,null,biId); //not yet signed, so signatureTime is null
			XMLErrorMessage theAnswer = null;
			try
			{
				PIConnection thePI = MenuHandler.getPIConn();
				thePI.connect();
				theAnswer = thePI.submitPriceCert(newPriceCert);
				thePI.disconnect();
				if ( theAnswer.getErrorCode() == XMLErrorMessage.ERR_OK)
				{
					JAPDialog.showMessageDialog(this,MSG_NEW_PRICE);
				}
				else
				{
					throw new Exception("pi returned error");
				}
			} catch (Exception e)
			{
				JAPDialog.showMessageDialog(this,"Could not send new price to payment instance");
				LogHolder.log(LogLevel.DEBUG, LogType.PAY, "could not submit new price cert to payment instance");
			}
		}
	}*/

	/**
	 * Remove certificate from this panel
	 */
	public void removeCert()
	{
		this.m_cert = null;
		this.clearPrice();
		this.fireStateChanged();
	}

	/**
	 * Return the price certificate that is currently maintained by this {@link JPanel}
	 * @return
	 */
	public XMLPriceCertificate getCert()
	{
		return m_cert;
	}

	/**
	 * Create and set the price certificate from a given byte array
	 * @param a_cert
	 */
	public void setCert(byte[] a_cert)
	{
		try
		{
			// Set the certificate
			m_cert = new XMLPriceCertificate(a_cert);
			this.showPrice(m_cert);
		}
		catch (Exception e)
		{
			LogHolder.log(LogLevel.ERR, LogType.PAY, "Error while importing price certificate: " + e.getMessage());
		}
	}
	
	/**
	 * Set a given {@link XMLPriceCertificate} to this panel
	 * @param a_cert
	 */
	public void setCert(XMLPriceCertificate a_cert)
	{
	  m_cert = a_cert;
	  this.showPrice(m_cert);
	}

	private void showPrice(XMLPriceCertificate a_cert)
	{
		if (a_cert == null)
		{
			this.clearPrice();
		} 
		else
		{
			String rateText = this.formatEuroCentValue(a_cert.getRate());
			m_rate.setForeground(Color.BLACK);
			m_rate.setText(rateText);
			m_rate.setToolTipText("Price per Megabyte");
		}
	}
	
	/**
	 * Return a string representation of the price per megabyte
	 * 
	 * @param centvalue
	 * @return
	 */
	private String formatEuroCentValue(double centvalue)
	{
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		return nf.format(centvalue) + " Eurocent";
	}

	private void clearPrice()
	{
		m_rate.setForeground(Color.RED);
		m_rate.setText("No Certificate!");
		m_rate.setToolTipText("No price certificate available");
	}
	
	private void showError(String sMessage, String sToolTip)
	{
		m_rate.setForeground(Color.RED);
		m_rate.setText(sMessage);
		m_rate.setToolTipText(sToolTip);
	}
		
	public void setEnabled(boolean enabled)
	{
		// Get the components
		Component components[] = getComponents();
		TitledBorder border;
		// Iterate
		for (int i = 0; i < components.length; i++)
		{
			if (! (components[i] instanceof JButton))
			{
				components[i].setEnabled(enabled);
			}
		}
		if (getBorder() instanceof TitledBorder)
		{
			border = new TitledBorder( ( (TitledBorder) getBorder()).getTitle());
			if (!enabled)
			{
				border.setTitleColor(Color.gray);
			}
			setBorder(border);
		}
		super.setEnabled(enabled);
		enableButtons();
	}
	
	/** Enables/disables the buttons according to this object's state.
	 * The buttons for creating and importing certificates are enabled if the stored
	 * certificate is <CODE>null</CODE>. Otherwise, the buttons for exporting and removing
	 * the certificate and for changing the password are enabled.
	 */
	private void enableButtons()
	{
		boolean cert = (m_cert != null);
		boolean enabled = isEnabled();

		if (m_bttnImport != null)
		{
			m_bttnImport.setEnabled(enabled);
		}
		if (m_bttnUpdate != null)
		{
			// TODO: Implement update functionality
			m_bttnUpdate.setEnabled(false);
		}
		if (m_bttnRemove != null)
		{
			m_bttnRemove.setEnabled(enabled && cert);
		}
	}
	
	/**
	 * Check if Mix-Cert and JPI-Cert go well with the Price-Cert
	 */
	private void checkIDs()
	{
		// Get both of the values
		String sMixID = MixConfig.getMixConfiguration().getValue(GeneralPanel.XMLPATH_GENERAL_MIXID);
		String sPaymentInstanceID = MixConfig.getMixConfiguration().getAttributeValue(
				PaymentPanel.XMLPATH_PAYMENT_INSTANCE, PaymentPanel.XML_ATTRIBUTE_ID);
		// Initially these are true
		boolean bMixIDFits = true;
		boolean bPIIDFits = true;
		// Message strings
		String sError = "";
		String sErrorToolTip = "";
		// Check MixID
		if (sMixID != null)
		{
			if (this.m_cert.getSubjectKeyIdentifier().equals(sMixID))
			{
				LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Successfully checked MixID vs. price certificate");
			}
			else
			{
				bMixIDFits = false;
				LogHolder.log(LogLevel.ERR, LogType.MISC, "The MixID does not fit the SKI of the price certificate!");
				// Construct error message and tooltip
				sError += "Incompatible Mix-Cert";
				sErrorToolTip += "The MixID does not fit the SubjectKeyIdentifier of this price certificate";
			}
		}
		// Check PI-ID
		if (sPaymentInstanceID != null)
		{
			if (this.m_cert.getBiID().equals(sPaymentInstanceID))
			{
				LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Successfully checked PI-ID vs. price certificate");
			}
			else
			{
				System.out.println(this.m_cert.getBiID());
				System.out.println(sPaymentInstanceID);
				bPIIDFits = false;
				LogHolder.log(LogLevel.ERR, LogType.MISC, "The ID of the PI does not fit the PI-ID in the price certificate!");
				// Construct error message and tooltip
				if (!bMixIDFits)
				{
					sError = "Incompatible Mix- and JPI-Cert";
					sErrorToolTip += "<br/> AND <br/>";
				}
				else
				{
					sError += "Incompatible JPI-Cert";
					sErrorToolTip += "The ID of the JPI does not fit the PI-ID in the price certificate";				
				}
			}
		}
		// Evaluate
		if (bMixIDFits && bPIIDFits)
		{
			// All good
			showPrice(m_cert);
		}
		else showError(sError, sErrorToolTip+"!");
	}
	
	/**
	 * Listen to ChangeEvents
	 */
	public void stateChanged(ChangeEvent event) 
	{
		// In case of modifications of 'Accounting/PriceCertificate', 
		// 'Accounting/PaymentInstance/Certificate' or
		// 'General/MixID' OR 'Certificates/OwnCertificate/'
		// ... do the following:
		// Compare the SKI of PriceCertificate with MixID/SKI of OwnMixCertificate
		// Compare the PI-ID of PriceCertificate with SKI of PaymentInstance Certificate
		if (this.m_cert != null)
		{
			if (event instanceof ConfigurationEvent)
			{
				ConfigurationEvent configEvent = (ConfigurationEvent)event;
				String sPath = configEvent.getModifiedXMLPath();
				//LogHolder.log(LogLevel.DEBUG, LogType.MISC, "The modified XML-path is "+sAtt);
				if (sPath.equals(XMLPATH_PRICECERT) || sPath.equals(GeneralPanel.XMLPATH_GENERAL_MIXID) || 
						sPath.equals(PaymentPanel.XMLPATH_PAYMENT_INSTANCE))
				{
					checkIDs();
				}
			}	
		}
	}
	
	public void addChangeListener(ChangeListener a_listener)
	{
		m_changeListeners.addElement(a_listener);
	}

	private void fireStateChanged()
	{
		ChangeEvent event = new ChangeEvent(this);
		for (int i = 0; i < m_changeListeners.size(); i++)
		{
			((ChangeListener)m_changeListeners.elementAt(i)).stateChanged(event);
		}
	}
	
	private class ChooseImportMethodPane extends DialogContentPane implements
		DialogContentPane.IWizardSuitable
	{
		private JRadioButton m_btnFile;
		private JRadioButton m_btnClip;

		public ChooseImportMethodPane(JAPDialog a_dialog, String a_strText)
		{
			super(a_dialog, a_strText, new DialogContentPaneOptions(DialogContentPane.OPTION_TYPE_OK_CANCEL));
			GridBagConstraints constr = new GridBagConstraints();
			ButtonGroup group = new ButtonGroup();

			m_btnFile = new JRadioButton("File");
			m_btnClip = new JRadioButton("Clipboard");
			group.add(m_btnFile);
			group.add(m_btnClip);
			m_btnFile.setSelected(true);
			constr.gridx = 0;
			constr.gridy = 0;
			constr.anchor = GridBagConstraints.WEST;
			constr.fill = GridBagConstraints.HORIZONTAL;
			constr.weightx = 0;
			getContentPane().setLayout(new GridBagLayout());
			getContentPane().add(m_btnFile, constr);
			constr.gridy++;
			getContentPane().add(m_btnClip, constr);
		}
	
		public boolean isMethodFile()
		{
			return m_btnFile.isSelected();
		}
	}
}