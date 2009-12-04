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

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import mixconfig.MixConfiguration;
import mixconfig.tools.dataretention.DataRetentionEncryptionCertCreationValidator;
import mixconfig.tools.dataretention.DataRetentionSmartCard;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import anon.crypto.ICertificate;
import anon.crypto.MyRSAPublicKey;
import anon.crypto.PKCS12;

public class DataRetentionPanel extends JPanel implements ActionListener, FocusListener
{
	private static final String ACTIONCOMMAND_IMPORTDATARETENTIONKEY="impordataretentionkey";
	
	private MixConfigPanel m_parentPanel = null;
	
	//Data retention
	private JButton m_bttnImportDataRetentionKey;
	private JTextField m_tfDataRetentionKey;
	private JPanel m_panelDataRetentionKeyStoreCards;
	private JTextField m_tfDataRetentionLogDir;
	private JCheckBox m_cbDoDataRetention;
	
	public DataRetentionPanel(MixConfigPanel parentPanel)
	{
		//Data Retention Panel
		setLayout(new GridBagLayout());
		setBorder(new TitledBorder("Data Retention"));
		
		m_parentPanel = parentPanel;
		//Enable / Disable
		m_cbDoDataRetention=new JCheckBox("Enable Data Retention Logs");
		GridBagConstraints c=new GridBagConstraints();
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridwidth=2;
		c.weightx=1;
		add(m_cbDoDataRetention,c);
		
		//Type of key storage
		JPanel panelKeyStore=new JPanel(new GridBagLayout());
		panelKeyStore.setBorder(new TitledBorder("Encryption Key Storage"));
		c.gridy=0;
		c.gridx=3;
		c.gridwidth=1;
		c.gridheight=2;
		c.weightx=0;
		add(panelKeyStore,c);
		
		JRadioButton radiobuttonSmartCard=new JRadioButton("Store on SmartCard");
		JRadioButton radiobuttonDisk=new JRadioButton("Store on Disk (USB-Stick etc.)");
		ButtonGroup bttngrpDataRetentionKeyStore=new ButtonGroup();
		bttngrpDataRetentionKeyStore.add(radiobuttonDisk);
		bttngrpDataRetentionKeyStore.add(radiobuttonSmartCard);
		radiobuttonDisk.setSelected(true);
		radiobuttonSmartCard.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				((CardLayout)m_panelDataRetentionKeyStoreCards.getLayout()).show(m_panelDataRetentionKeyStoreCards,"smartcard");
			}
			
		});
		radiobuttonDisk.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				((CardLayout)m_panelDataRetentionKeyStoreCards.getLayout()).show(m_panelDataRetentionKeyStoreCards,"disk");
			}
			
		});

		JPanel panelDataRetentionKeyStoreSelection=new JPanel();
		panelDataRetentionKeyStoreSelection.add(radiobuttonDisk);
		panelDataRetentionKeyStoreSelection.add(radiobuttonSmartCard);
		
		GridBagConstraints constraintsPanelKeyStore=new GridBagConstraints();
		constraintsPanelKeyStore.gridy=0;
		panelKeyStore.add(panelDataRetentionKeyStoreSelection,constraintsPanelKeyStore);
		
		//switch Panel for key on smartcard or disk
		m_panelDataRetentionKeyStoreCards=new JPanel(new CardLayout());
		constraintsPanelKeyStore.gridy=1;
		constraintsPanelKeyStore.gridx=0;
		constraintsPanelKeyStore.fill=GridBagConstraints.BOTH;
		constraintsPanelKeyStore.weightx=1;
		constraintsPanelKeyStore.weighty=1;
		panelKeyStore.add(m_panelDataRetentionKeyStoreCards,constraintsPanelKeyStore);
		
		//Use key from disk
		JPanel p=new JPanel(new GridBagLayout());
		GridBagConstraints cp=new GridBagConstraints();
		final CertPanel certp=new CertPanel("Log encryption key (USB stick or similar external storage)","This key is used to encrypt the data retention logs",(PKCS12)null,CertPanel.CERT_ALGORITHM_RSA,0,2048);
		certp.setCertCreationValidator(new DataRetentionEncryptionCertCreationValidator());
		certp.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent arg0) {
				MixConfiguration c= m_parentPanel.getConfiguration();
				Document doc=c.getDocument();
				ICertificate cert=certp.getCert();
				if(cert==null)
				{
					c.removeNode("DataRetention/PublicEncryptionKey");
				}
				else
				{
				MyRSAPublicKey key=(MyRSAPublicKey)(cert.getPublicKey());
				Element elem=key.toXmlElement(doc);
				Element elemPEK = doc.createElement("PublicEncryptionKey");
				c.setValue("DataRetention", elemPEK);
				}
			}
			
		});
		cp.gridy=0;
		cp.weightx=1.0;
		cp.weighty=1.0;
		cp.fill=GridBagConstraints.BOTH;
		p.add(certp,cp);
		
		m_panelDataRetentionKeyStoreCards.add(p, "disk");

		//Import public key from smart card
		p=new JPanel(new GridBagLayout());
		m_bttnImportDataRetentionKey=new JButton("Import public key from Smart Card...");
		m_bttnImportDataRetentionKey.setActionCommand(ACTIONCOMMAND_IMPORTDATARETENTIONKEY);
		m_bttnImportDataRetentionKey.addActionListener(this);
		cp.gridy=0;
		p.add(m_bttnImportDataRetentionKey,cp);

		m_tfDataRetentionKey=new JTextField();
		cp.gridy=1;
		cp.weightx=1.0;
		cp.fill=GridBagConstraints.HORIZONTAL;
		p.add(m_tfDataRetentionKey,cp);
		
		m_panelDataRetentionKeyStoreCards.add(p, "smartcard");


		JLabel l=new JLabel("Log directory:");
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.weightx=0;
		c.gridy=1;
		c.gridx=0;
		c.gridwidth=1;
		c.gridheight=1;
		add(l,c);
		
		m_tfDataRetentionLogDir=new JTextField();
		m_tfDataRetentionLogDir.setName("DataRetention/LogDir");
		m_tfDataRetentionLogDir.addFocusListener(this);
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.weightx=1;
		c.gridy=1;
		c.gridx=1;
		c.gridwidth=1;
		c.gridheight=1;
		c.insets=new Insets(0,10,0,10);
		add(m_tfDataRetentionLogDir,c);
	}

	public void focusGained(FocusEvent a_event)
	{
		/*if (a_event.getSource() == m_tfFileName)
		{
			MixConfiguration c = m_parentPanel.getConfiguration();
			if (c.isMixOnCDEnabled() && m_tfFileName.getText().equals(""))
			{
				m_tfFileName.setText("/usbstick");
			}
		}
		else
		{
			m_parentPanel.focusGained(a_event);
		}
		*/
	}

	public void focusLost(FocusEvent a_event)
	{	
		//Data retention
		if(a_event.getSource()==m_tfDataRetentionLogDir)
		{
			m_parentPanel.save(m_tfDataRetentionLogDir);
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(ACTIONCOMMAND_IMPORTDATARETENTIONKEY))
			importDataRetentionKey();
	}
	
	private void importDataRetentionKey()
	{
		try
		{
			DataRetentionSmartCard smartcard=new DataRetentionSmartCard();
			smartcard.connectToSmartCard();
			MyRSAPublicKey key=smartcard.retrievePublicKey();
			m_tfDataRetentionKey.setText(key.toString());
			MixConfiguration c=m_parentPanel.getConfiguration();
			Document doc=c.getDocument();
			Element elem=key.toXmlElement(doc);
			Element elemPEK = doc.createElement("PublicEncryptionKey");
			elemPEK.appendChild(elem);
			c.setValue("DataRetention", elemPEK);
		}
		catch(Exception e)
		{
			LogHolder.log(LogLevel.ERR,LogType.MISC,"Error accesing smart card "+ e.getMessage());
			//e.printStackTrace();
		}
		
	}
}
