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

import java.util.Vector;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import anon.crypto.JAPCertificate;
import gui.JAPHelp;
import javax.swing.event.ChangeEvent;
import logging.LogType;

/**
 * This class is an abstract superclass for NextMixProxyPanel and PreviousMixPanel
 * @author Tobias Bayer
 */
public abstract class OtherMixPanel extends MixConfigPanel implements ChangeListener
{
	public static final String MIX_TYPE_PREVIOUS = "Previous Mix";
	public static final String MIX_TYPE_NEXT = "Next Mix";

	private String m_type;

	private CertPanel m_otherCert;
	private CertPanel m_otherOpCert;

	private JPanel m_operatorPanel;
	private JPanel m_locationPanel;
	private JTextField m_opOrgField;
	private JTextField m_opOrgUnitField;
	private JTextField m_opCountryField;
	private JTextField m_opUrlField;
	private JTextField m_opEmailField;
	private JTextField m_locCityField;
	private JTextField m_locCountryField;
	private JTextField m_locStateField;
	private JTextField m_locLongField;
	private JTextField m_locLatField;
	private JButton m_mapButton;

	private GridBagLayout m_layout = new GridBagLayout();
	private GridBagConstraints m_gbc = new GridBagConstraints();

	public OtherMixPanel(String a_mixType)
	{
		super(a_mixType);
		m_type = a_mixType;

		setLayout(m_layout);

		m_gbc.anchor = GridBagConstraints.NORTHWEST;
		m_gbc.insets = new Insets(5, 5, 5, 5);
		m_gbc.fill = GridBagConstraints.NONE;
		m_gbc.weightx = 0;
		m_gbc.weighty = 0;

		m_otherCert = new CertPanel(a_mixType + " Certificate",
									"Hint: You will get the public test " +
									"certificate from the operator of the " +
									"other mix", (JAPCertificate)null);
		if (a_mixType == MIX_TYPE_PREVIOUS)
		{
			m_otherCert.setName("Certificates/PrevMixCertificate");
		}
		else if (a_mixType == MIX_TYPE_NEXT)
		{
			m_otherCert.setName("Certificates/NextMixCertificate");
		}
		m_otherCert.setCertificateView(new MixCertificateView());
		m_otherCert.addChangeListener(this);
		m_gbc.gridx = 0;
		m_gbc.gridy = 0;
		this.add(m_otherCert, m_gbc);

		m_otherOpCert = new CertPanel(a_mixType + " Operator Certificate",
									  "Hint: You will get the public test " +
									  "certificate from the operator of the " +
									  "other mix", (JAPCertificate)null);
		m_otherOpCert.setEnabled(false); // not used and therefore disabled at the moment
		/** @todo Specify correct name for operator cert*/
		if (a_mixType == MIX_TYPE_PREVIOUS)
		{
			m_otherOpCert.setName("Certificates/PrevOperatorCertificate");
		}
		else if (a_mixType == MIX_TYPE_NEXT)
		{
			m_otherOpCert.setName("Certificates/NextOperatorCertificate");

		}
		m_otherOpCert.addChangeListener(this);
		m_gbc.gridx++;
		m_gbc.fill = m_gbc.NONE;
		this.add(m_otherOpCert, m_gbc);

		m_locationPanel = this.createLocationPanel();
		m_gbc.gridx = 0;
		m_gbc.gridy++;
		this.add(m_locationPanel, m_gbc);
		m_otherCert.setPreferredSize(new Dimension( (int) m_locationPanel.getPreferredSize().width,
			(int) m_otherCert.getPreferredSize().height));

		m_operatorPanel = this.createOperatorPanel();
		m_gbc.gridx++;
		this.add(m_operatorPanel, m_gbc);
		m_otherOpCert.setPreferredSize(new Dimension( (int) m_operatorPanel.getPreferredSize().width,
			(int) m_otherOpCert.getPreferredSize().height));
	}

	protected JPanel getLocationPanel()
	{
		return m_locationPanel;
	}

	public JPanel getOperatorPanel()
	{
		return m_operatorPanel;
	}

	private JPanel createLocationPanel()
	{
		TitledGridBagPanel panel = new TitledGridBagPanel("Location");
		m_locCityField = new JTextField(MAX_COLUMN_LENGTH);
		m_locCityField.setEditable(false);
		m_locCountryField = new JTextField(MAX_COLUMN_LENGTH);
		m_locCountryField.setEditable(false);
		m_locStateField = new JTextField(MAX_COLUMN_LENGTH);
		m_locStateField.setEditable(false);
		m_locLongField = new JTextField(MAX_COORDINATE_FIELD_LENGTH);
		m_locLongField.setEditable(false);
		m_locLatField = new JTextField(MAX_COORDINATE_FIELD_LENGTH);
		m_locLatField.setEditable(false);
		m_mapButton = new JButton("Show on map");
		m_mapButton.setActionCommand("Map");
		m_mapButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					final MapBox mapBox = new MapBox(MixConfig.getMainWindow(),
						m_locLatField.getText(), m_locLongField.getText(),
						5);
					mapBox.setVisible(true);

				}
				catch (Exception ex)
				{
					MixConfig.handleError(ex, "Could not show map", LogType.GUI);
				}
			}
		}
		);

		panel.addRow(new JLabel("City"), m_locCityField, null);
		panel.addRow(new JLabel("State"), m_locStateField, null);
		panel.addRow(new JLabel("Country"), m_locCountryField, null);
		panel.addRow(new JLabel("Longitude"), m_locLongField, m_mapButton);
		panel.addRow(new JLabel("Longitude"), m_locLatField, null);

		return panel;
	}

	private JPanel createOperatorPanel()
	{
		m_opOrgField = new JTextField(MAX_COLUMN_LENGTH);
		m_opOrgField.setEditable(false);
		m_opOrgUnitField = new JTextField(MAX_COLUMN_LENGTH);
		m_opOrgUnitField.setEditable(false);
		m_opCountryField = new JTextField(MAX_COLUMN_LENGTH);
		m_opCountryField.setEditable(false);
		m_opUrlField = new JTextField(MAX_COLUMN_LENGTH);
		m_opUrlField.setEditable(false);
		m_opEmailField = new JTextField(MAX_COLUMN_LENGTH);
		m_opEmailField.setEditable(false);

		TitledGridBagPanel panel = new TitledGridBagPanel("Operator");

		panel.addRow(new JLabel("Organisation"), m_opOrgField);
		panel.addRow(new JLabel("Orga. Unit"), m_opOrgUnitField);
		panel.addRow(new JLabel("Country"), m_opCountryField);
		panel.addRow(new JLabel("URL"), m_opUrlField);
		panel.addRow(new JLabel("E-Mail"), m_opEmailField);

		return panel;
	}

	protected void enableCert(boolean a_enabled)
	{
		m_otherCert.setEnabled(a_enabled);
	}

	protected GridBagLayout getGridBagLayout()
	{
		return m_layout;
	}

	protected GridBagConstraints getGridBagConstraints()
	{
		return m_gbc;
	}

	protected CertPanel getMixCertPanel()
	{
		return m_otherCert;
	}

	protected CertPanel getMixOperatorCertPanel()
	{
		return m_otherOpCert;
	}

	/**
	 *
	 * @return Possible error and warning messages
	 * @todo Implement this mixconfig.MixConfigPanel method
	 */
	public Vector check()
	{
		Vector errors = new Vector();

		if (!m_otherCert.isEnabled() && m_otherCert.getCert() != null)
		{
			errors.addElement(
				m_type + " Certificate is present, but there is no corresponding mix.");
		}
		else if (!getConfiguration().isAutoConfigurationAllowed() &&
				 m_otherCert.isEnabled() && m_otherCert.getCert() == null)
		{
			errors.addElement(
				m_type + " Certificate is missing in " + getPanelName() + " panel.");
		}

		return errors;
	}

	public void paint(Graphics g)
	{
		super.paint(g);
		JAPHelp.getInstance().getContextObj().setContext("index");
	}

	/**
	 * Enables used and disables unused components.
	 *
	 * @todo Implement this mixconfig.MixConfigPanel method
	 */
	protected void enableComponents()
	{
	}

	/**
	 * Retrieves infomration about location from the certificate and puts it
	 * into the right text fields
	 */
	private void loadLocationInfo()
	{
		MixCertificateView certView = (MixCertificateView)m_otherCert.getCertificateView();
		m_locCityField.setText(certView.getLocalityName());
		m_locCountryField.setText(certView.getCountry());
		m_locStateField.setText(certView.getStateOrProvince());
		m_locLongField.setText(certView.getLongitude());
		m_locLatField.setText(certView.getLatitude());
	}

	public void stateChanged(ChangeEvent a_e)
	{
		if (a_e.getSource() == m_otherCert)
		{
			loadLocationInfo();
		}
	}
}
