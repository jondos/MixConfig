/*
 Copyright (c) 2000, The JAP-Team
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

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;


/**
 * The PaymentPanel is one page in the MixConfig TabbedPane and allows the user to specify 
 * the data which is needed for the mix to successfully use payment, namely the JPI Host/Port, 
 * and the Postgresql Database Host/Port/DBName/Username.
 *
 * @author Bastian Voigt
 * @version 0.1
 */
class PaymentPanel 
extends JPanel
//implements ActionListener
{
	protected JCheckBox m_chkPaymentEnabled;
	protected JTextField m_textJPIHost;
	protected JTextField m_textJPIPort;
	protected JTextField m_textDatabaseHost;
	protected JTextField m_textDatabasePort;
	protected JTextField m_textDatabaseDBName;
	protected JTextField m_textDatabaseUsername;	
	private JLabel[] m_labels = new JLabel[6];


	public PaymentPanel()
	{
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		
		GridBagConstraints d = new GridBagConstraints();
		d.anchor = GridBagConstraints.NORTHWEST;
		d.insets = new Insets(5, 5, 5, 5);
		d.fill = GridBagConstraints.HORIZONTAL;
		d.gridx = 0;
		d.gridy = 0;
		d.weightx = 0;
 		d.weighty = 1;
		
		// MISC Panel
		JPanel miscPanel = new JPanel(new FlowLayout());
		miscPanel.setBorder(new TitledBorder("Payment misc"));
		miscPanel.setToolTipText("Please select whether you want to enable payment");

		m_chkPaymentEnabled = new JCheckBox("Enable Payment");
		m_chkPaymentEnabled.setSelected(false);
		m_chkPaymentEnabled.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e) {
					boolean enabled = m_chkPaymentEnabled.isSelected();
					for(int i=0;i<6;i++) m_labels[i].setEnabled(enabled);
					m_textJPIHost.setEnabled(enabled);
					m_textJPIPort.setEnabled(enabled);
					m_textDatabaseHost.setEnabled(enabled);
					m_textDatabasePort.setEnabled(enabled);
					m_textDatabaseDBName.setEnabled(enabled);
					m_textDatabaseUsername.setEnabled(enabled);
				}
			});	
		miscPanel.add(m_chkPaymentEnabled);		
		layout.setConstraints(miscPanel, c);
		this.add(miscPanel);
		

				
		// JPI Panel
		GridBagLayout jpiLayout = new GridBagLayout();
		JPanel jpiPanel = new JPanel(jpiLayout);
		jpiPanel.setBorder(new TitledBorder("JPI (Java Payment Instance)"));
		jpiPanel.setToolTipText("Please enter the Hostname or IP Address and the port number of the JPI<br> "+
														"that your mix should use.");
														
		m_labels[0] = new JLabel("JPI Hostname:");
		jpiLayout.setConstraints(m_labels[0], d);
		jpiPanel.add(m_labels[0]);
		
		m_textJPIHost = new JTextField();
		m_textJPIHost.setEnabled(false);
		d.gridx=1; d.weightx=1;
		jpiLayout.setConstraints(m_textJPIHost, d);
		jpiPanel.add(m_textJPIHost);

		m_labels[1] = new JLabel("JPI Portnumber:");
		d.gridy++; d.gridx=0; d.weightx=0;
		jpiLayout.setConstraints(m_labels[1], d);
		jpiPanel.add(m_labels[1]);
		
		m_textJPIPort = new JTextField();
		m_textJPIPort.setEnabled(false);
		d.gridx=1; d.weightx=1;
		jpiLayout.setConstraints(m_textJPIPort, d);
		jpiPanel.add(m_textJPIPort);
										
		c.gridy++;						
		layout.setConstraints(jpiPanel, c);
		this.add(jpiPanel);
		

				
		// DATABASE Panel
		GridBagLayout databaseLayout = new GridBagLayout();
		JPanel databasePanel = new JPanel(databaseLayout);
		databasePanel.setBorder(new TitledBorder("PostgreSQL Database for the accounting instance"));
		databasePanel.setToolTipText("The accounting instance inside the First Mix needs a PostgreSQL<br> "+
																	"database to store some internal accounting data. Before you start<br> "+
																	"the First Mix with payment enabled, setup a Postgresql DB and enter<br> "+
																	"its connection data here.");
																	
		m_labels[2] = new JLabel("Database Hostname:");
		d.gridx=0; d.gridy=0; d.weightx=0;
		databaseLayout.setConstraints(m_labels[2], d);
		databasePanel.add(m_labels[2]);

		m_textDatabaseHost = new JTextField();
		m_textDatabaseHost.setEnabled(false);
		d.gridx=1; d.weightx=1;
		databaseLayout.setConstraints(m_textDatabaseHost, d);
		databasePanel.add(m_textDatabaseHost);		
																			
		m_labels[3] = new JLabel("Database Portnumber:");
		d.gridy++; d.gridx=0; d.weightx=0;
		databaseLayout.setConstraints(m_labels[3], d);
		databasePanel.add(m_labels[3]);

		m_textDatabasePort = new JTextField();
		m_textDatabasePort.setEnabled(false);
		d.gridx=1; d.weightx=1;
		databaseLayout.setConstraints(m_textDatabasePort, d);
		databasePanel.add(m_textDatabasePort);				
		
		m_labels[4] = new JLabel("Database DBName:");
		d.gridy++; d.gridx=0; d.weightx=0;
		databaseLayout.setConstraints(m_labels[4], d);
		databasePanel.add(m_labels[4]);
		
		m_textDatabaseDBName = new JTextField();
		m_textDatabaseDBName.setEnabled(false);
		d.gridx=1; d.weightx=1;
		databaseLayout.setConstraints(m_textDatabaseDBName, d);
		databasePanel.add(m_textDatabaseDBName);

		m_labels[5] = new JLabel("Database Username:");
		d.gridy++; d.gridx=0; d.weightx=0;
		databaseLayout.setConstraints(m_labels[5], d);
		databasePanel.add(m_labels[5]);
		
		m_textDatabaseUsername = new JTextField();
		m_textDatabaseUsername.setEnabled(false);
		d.gridx=1; d.weightx=1;
		databaseLayout.setConstraints(m_textDatabaseUsername, d);
		databasePanel.add(m_textDatabaseUsername);
																			
		c.gridy++;
		layout.setConstraints(databasePanel, c);
		this.add(databasePanel);
		
		for(int i=0;i<6;i++) m_labels[i].setEnabled(false);
	}
	
	
/*	public void actionPerformed(ActionEvent e)
	{
		
	}*/
}