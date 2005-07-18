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

import java.util.Vector;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import anon.crypto.JAPCertificate;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import logging.LogType;
import java.io.IOException;

/** This panel displays information about the previous mix if the current mix is
 * a middle or last mix.
 *
 * @author Tobias Bayer
 */
public class PreviousMixPanel extends MixConfigPanel implements ChangeListener
{
	private CertPanel m_prevCert;
	private CertPanel m_prevOpCert;

	private JPanel m_operatorPanel;
	private JPanel m_locationPanel;
	private JLabel m_opOrgLabel;
	private JLabel m_opUrlLabel;
	private JLabel m_opEmailLabel;
	private JLabel m_locCityLabel;
	private JLabel m_locCountryLabel;
	private JLabel m_locStateLabel;
	private JLabel m_locLongLabel;
	private JLabel m_locLatLabel;
	private JButton m_mapButton;

	public PreviousMixPanel()
	{
		super("Previous Mix");
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;

		m_prevCert = new CertPanel("Previous Mix Certificate",
								   "Hint: You will get the public test " +
								   "certificate from the operator of the " +
								   "previous mix", (JAPCertificate)null);
		m_prevCert.setName("Certificates/PrevMixCertificate");
		m_prevCert.addChangeListener(this);
		c.gridx = 0;
		c.gridy = 0;
		c.fill = c.HORIZONTAL;
		this.add(m_prevCert, c);

		m_prevOpCert = new CertPanel("Previous Mix Operator Certificate",
									 "Hint: You will get the public test " +
									 "certificate from the operator of the " +
									 "previous mix", (JAPCertificate)null);
		m_prevOpCert.setEnabled(false); // not used and disabled at the moment
		/** @todo Specify correct name for operator cert*/
		m_prevOpCert.setName("Certificates/PrevOperatorCertificate");
		m_prevOpCert.addChangeListener(this);
		c.gridx++;
		c.fill = c.HORIZONTAL;
		c.weightx = 1;
		this.add(m_prevOpCert, c);

		m_locationPanel = this.createLocationPanel();
		c.gridx = 0;
		c.gridy++;
		this.add(m_locationPanel, c);

		m_operatorPanel = this.createOperatorPanel();
		c.gridx++;
		c.weighty = 1;
		this.add(m_operatorPanel, c);
	}

	private JPanel createLocationPanel()
	{
		JPanel panel = new JPanel();
		m_locCityLabel = new JLabel("N/A");
		m_locCountryLabel = new JLabel("N/A");
		m_locStateLabel = new JLabel("N/A");
		m_locLongLabel = new JLabel("N/A");
		m_locLatLabel = new JLabel("N/A");
		m_mapButton = new JButton("Show on map");
		m_mapButton.setActionCommand("Map");
		m_mapButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					final MapBox mapBox = new MapBox(MixConfig.getMainWindow(),
						m_locLatLabel.getText(), m_locLongLabel.getText(),
						5);
					mapBox.show();
					m_mapButton.setText("Update Map");
					m_mapButton.setActionCommand("Update");

					mapBox.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e2)
						{
							if (e2.getActionCommand().equals("CloseMapBox"))
							{
								mapBox.dispose();
								m_mapButton.setText("Show on Map");
								m_mapButton.setActionCommand("Map");

							}
							else if (e2.getActionCommand().equals("Update"))
							{
								try
								{
									mapBox.setGeo(m_locLatLabel.getText(), m_locLongLabel.getText());
								}
								catch (Exception ex2)
								{
								}
							}
						}
					}
					);
				}
				catch (Exception ex)
				{

				}
			}
		}
		);

		panel.setLayout(new GridBagLayout());
		panel.setBorder(new TitledBorder("Location"));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = c.NORTHWEST;
		c.fill = c.NONE;
		c.insets = new Insets(5, 5, 5, 5);
		c.gridx = 0;
		c.gridy = 0;
		panel.add(new JLabel("City:"), c);
		c.gridx++;
		panel.add(m_locCityLabel, c);
		c.gridx = 0;
		c.gridy++;
		panel.add(new JLabel("Country:"), c);
		c.gridx++;
		panel.add(m_locCountryLabel, c);
		c.gridx = 0;
		c.gridy++;
		panel.add(new JLabel("State:"), c);
		c.gridx++;
		panel.add(m_locStateLabel, c);
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = c.NORTHEAST;
		c.weightx = 1;
		c.gridheight = 3;
		JPanel geoPanel = new JPanel();
		geoPanel.setLayout(new GridBagLayout());
		GridBagConstraints d = new GridBagConstraints();
		d.insets = new Insets(0, 5, 5, 5);
		d.anchor = d.NORTHWEST;
		d.fill = d.NONE;
		d.gridx = 0;
		d.gridy = 0;
		geoPanel.add(new JLabel("Longitude:"), d);
		d.gridx++;
		geoPanel.add(m_locLongLabel, d);
		d.insets = new Insets(5, 5, 5, 5);
		d.gridx = 0;
		d.gridy++;
		geoPanel.add(new JLabel("Latitude:"), d);
		d.gridx++;
		geoPanel.add(m_locLatLabel, d);
		d.gridy++;
		d.gridx = 0;
		d.gridwidth = 2;
		d.weighty = 1;
		geoPanel.add(m_mapButton, d);
		panel.add(geoPanel, c);

		return panel;
	}

	private JPanel createOperatorPanel()
	{
		m_opOrgLabel = new JLabel("N/A");
		m_opUrlLabel = new JLabel("N/A");
		m_opEmailLabel = new JLabel("N/A");

		TitledGridBagPanel panel = new TitledGridBagPanel("Operator");

		panel.addRow(new JLabel("Organization:"), m_opOrgLabel);
		panel.addRow(new JLabel("URL:"), m_opUrlLabel);
		panel.addRow(new JLabel("E-Mail:"), m_opEmailLabel);

		return panel;
	}

	public Vector check()
	{
		Vector errors = new Vector();

		if (!m_prevCert.isEnabled() && m_prevCert.getCert() != null)
		{
			errors.addElement(
						 "Previous Mix Certificate is present, but there is no previous mix.");
		}
		else if (!getConfiguration().isAutoConfigurationAllowed() &&
				 m_prevCert.isEnabled() && m_prevCert.getCert() == null)
		{
			errors.addElement(
						 "Previous Mix Certificate is missing in Certificates Panel.");
		}

		return errors;
	}

	protected void enableComponents()
	{
	}

	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		// first enable all components to make MixConfigPanel load their data
		enableComponents();

		super.setConfiguration(a_conf);

		// make sure this panel is contained only once in the config's listeners list
		a_conf.removeChangeListener(this);
		a_conf.addChangeListener(this);

		int mixType = Integer.valueOf(getConfiguration().getValue("General/MixType")).
			intValue();
		setEnabled (mixType != MixConfiguration.MIXTYPE_FIRST &&
					(!getConfiguration().isAutoConfigurationAllowed()
					 || getConfiguration().isFallbackEnabled()));

		enableComponents();
	}

	public void stateChanged(ChangeEvent e)
	{
		try
		{
			if (e instanceof ConfigurationEvent)
			{
				ConfigurationEvent c = (ConfigurationEvent) e;
				if (c.getChangedAttribute().equals("General/MixType"))
				{
					int mixType = Integer.valueOf(getConfiguration().getValue("General/MixType")).
						intValue();

					m_prevCert.setEnabled(mixType != MixConfiguration.MIXTYPE_FIRST &&
										  (!getConfiguration().isAutoConfigurationAllowed() ||
										   getConfiguration().isFallbackEnabled()));


					setEnabled (mixType != MixConfiguration.MIXTYPE_FIRST);
				}
				else if (c.getChangedAttribute().indexOf(GeneralPanel.XMLPATH_AUTOCONFIGURATION) >= 0)
				{
					enableComponents();
				}

			}

			else if (e.getSource() instanceof CertPanel)
			{
				save( (CertPanel) e.getSource());
			}
		}
		catch (Exception ex)
		{
			MixConfig.handleError(ex, null, LogType.GUI);
		}

	}
}
