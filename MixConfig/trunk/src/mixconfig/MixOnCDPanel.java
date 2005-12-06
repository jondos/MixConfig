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
package mixconfig;

import java.util.Vector;
import java.io.IOException;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.FocusEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import java.security.NoSuchAlgorithmException;

import anon.crypto.MD5Crypt;
import anon.crypto.DESCrypt;
import mixconfig.networkpanel.IPTextField;
import gui.JAPHelp;
import java.awt.Graphics;

/**
 * This panel stores the MixOnCD configuration.
 * @author Rolf Wendolsky
 */
public class MixOnCDPanel extends MixConfigPanel implements ActionListener
{
	public static final String XMLPATH_MIXONCD = "MixOnCD";
	public static final String XMLPATH_MIXONCD_NETWORK = XMLPATH_MIXONCD + "/Network";
	public static final String XMLPATH_MIXONCD_LOGIN_PASSWORD = XMLPATH_MIXONCD + "/Login/Password";
	public static final String XMLATTRIBUTE_DHCP = "dhcp";
	public static final String XMLVALUE_NETWORKINTERFACE = "NetworkInterface";

	private static final int VISIBLE_PASSWORD_LENGTH = 15;

	private JCheckBox m_cbxMixOnCD;
	private JCheckBox m_cbDHCP;
	private JTextField m_txtHostname;
	private JTextField m_txtNetworkInterface;
	private IPTextField m_txtIP;
	private IPTextField m_txtSubnetMask;
	private IPTextField m_txtDefaultGateway;
	private IPTextField[] m_txtDNSServers;
	private JLabel m_lblDNSServersHeadline;
	private JLabel[] m_lblDNSServersOptional;
	private JLabel[] m_lblDNSServersMandatory;

	private JLabel[] m_lblsPW;
	private JButton[] m_btnsRemovePW;
	private JPasswordField[] m_pwds;
	private JTextField[] m_txtHashedPasswords;

	private TitledGridBagPanel m_panelLocalNetworkSettings;
	private TitledGridBagPanel m_panelPasswords;

	private String[] m_users = {"root", "mix"};
	private String[] m_passwordHashes;


	public MixOnCDPanel()
	{
		super("MixOnCD");

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.insets = getDefaultInsets();
		constraints.fill = GridBagConstraints.NONE;

		m_cbxMixOnCD = new JCheckBox("Use bootable MixOnCD to create and install the mix");
		m_cbxMixOnCD.setName(MixOnCDPanel.XMLPATH_MIXONCD_NETWORK + "/" +
							 MixOnCDPanel.XMLATTRIBUTE_DHCP);
		m_cbxMixOnCD.addActionListener(this);
		m_cbxMixOnCD.addItemListener(this);
		constraints.gridwidth = 2;
		add(m_cbxMixOnCD, constraints);
		constraints.gridwidth = 1;

		/*
		 * network settings
		 */
		constraints.gridy = 1;
		m_panelLocalNetworkSettings = new TitledGridBagPanel("Local Network Settings");
		add(m_panelLocalNetworkSettings, constraints);


		// DHCP JCheckBox
		m_cbDHCP = new JCheckBox("Automatically configured by DHCP");
		m_cbDHCP.setName(XMLPATH_MIXONCD_NETWORK + "/" + XMLATTRIBUTE_DHCP);
		m_cbDHCP.addItemListener(this);
		m_panelLocalNetworkSettings.addRow(m_cbDHCP, (Component)null);

		m_txtIP = new IPTextField();
		m_txtIP.setName(XMLPATH_MIXONCD_NETWORK + "/IP");
		m_txtIP.addFocusListener(this);
		m_panelLocalNetworkSettings.addRow(new JLabel("IP"), m_txtIP);

		m_txtSubnetMask = new IPTextField();
		m_txtSubnetMask.setName(XMLPATH_MIXONCD_NETWORK + "/SubnetMask");
		m_txtSubnetMask.addFocusListener(this);
		m_panelLocalNetworkSettings.addRow(new JLabel("Subnet mask"), m_txtSubnetMask);


		m_txtDefaultGateway = new IPTextField();
		m_txtDefaultGateway.setName(XMLPATH_MIXONCD_NETWORK + "/DefaultGateway");
		m_txtDefaultGateway.addFocusListener(this);
		m_panelLocalNetworkSettings.addRow(new JLabel("Default Gateway"), m_txtDefaultGateway);

		m_txtNetworkInterface = new JTextField(4);
		m_txtNetworkInterface.setName(XMLPATH_MIXONCD_NETWORK + "/" + XMLVALUE_NETWORKINTERFACE);
		m_txtNetworkInterface.addFocusListener(this);
		m_txtNetworkInterface.setToolTipText(
				  "The network interface, for example eth0, eth1. (Optional)");
		m_panelLocalNetworkSettings.addRow(new JLabel("Interface (optional)"), m_txtNetworkInterface);

		m_txtHostname = new JTextField(15);
		m_txtHostname.setName(XMLPATH_MIXONCD_NETWORK + "/Hostname");
		m_txtHostname.addFocusListener(this);
		m_txtHostname.setToolTipText("Host name of the mix server. (Optional)");
		m_panelLocalNetworkSettings.addRow(new JLabel("Hostname (optional)"), m_txtHostname);



		m_panelLocalNetworkSettings.addRow(new JLabel(), (Component)null);
		m_lblDNSServersHeadline = new JLabel("DNS Servers");
		m_panelLocalNetworkSettings.addRow(m_lblDNSServersHeadline, (Component)null);

		m_txtDNSServers = new IPTextField[2];
		m_lblDNSServersOptional = new JLabel[m_txtDNSServers.length];
		m_lblDNSServersMandatory = new JLabel[m_txtDNSServers.length];
		for (int i = 0; i < m_txtDNSServers.length; i++)
		{
			m_txtDNSServers[i] = new IPTextField();
			m_txtDNSServers[i].setName(XMLPATH_MIXONCD_NETWORK + "/DNSServers" + "/IP");
			m_txtDNSServers[i].addFocusListener(this);
			m_lblDNSServersOptional[i] = new JLabel("DNS " + (i+1) +" (optional)");
			m_lblDNSServersMandatory[i] = new JLabel("DNS " + (i+1));
			m_panelLocalNetworkSettings.addRow(m_lblDNSServersOptional[i], m_txtDNSServers[i]);
		}


		/*
		 * user passwords
		 */
		constraints.gridx = 1;
		constraints.weightx = 1;
		constraints.weighty = 1;
		m_panelPasswords = new TitledGridBagPanel("User Passwords");
		add(m_panelPasswords, constraints);


		m_pwds = new JPasswordField[m_users.length];
		m_lblsPW = new JLabel[m_users.length];
		m_btnsRemovePW = new JButton[m_users.length];
		m_passwordHashes = new String[m_users.length];
		m_txtHashedPasswords = new JTextField[m_users.length];
		for (int i = 0; i < m_users.length; i++)
		{
			m_txtHashedPasswords[i] = new JTextField(VISIBLE_PASSWORD_LENGTH); // dummy label
			m_txtHashedPasswords[i].setEditable(false);

			m_pwds[i] = new JPasswordField(VISIBLE_PASSWORD_LENGTH);

			m_pwds[i].setName(XMLPATH_MIXONCD_LOGIN_PASSWORD + "/" + m_users[i]);
			m_pwds[i].addFocusListener(this);

			m_btnsRemovePW[i] = new JButton("Clear");
			m_btnsRemovePW[i].addActionListener(this);
			m_btnsRemovePW[i].addFocusListener(this);
			m_lblsPW[i] = new JLabel(m_users[i]);
			m_panelPasswords.addRow(m_lblsPW[i], m_btnsRemovePW[i], m_pwds[i]);
		}
	}


	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		// first enable all components to make MixConfigPanel load their data
		enableComponents();

		super.setConfiguration(a_conf);

		setActivated(getConfiguration().isMixOnCDEnabled());

		enableComponents();
	}


	public Vector check()
	{
		Vector errors = new Vector();
		boolean bValidDNS = false;

		if (m_cbxMixOnCD.isSelected() && !checkDHCPEnabled())
		{
			if (!m_txtIP.isCorrect())
			{
				errors.addElement("Invalid IP entered in MixOnCD panel.");
			}
			if (!m_txtSubnetMask.isCorrect())
			{
				errors.addElement("Invalid subnet mask entered in MixOnCD panel.");
			}
			if (!m_txtDefaultGateway.isCorrect())
			{
				errors.addElement("Invalid default gateway entered in MixOnCD panel.");
			}

			for (int i = 0; i < m_txtDNSServers.length; i++)
			{
				if (m_txtDNSServers[i].isCorrect())
				{
					bValidDNS = true;
				}
			}
			if (!bValidDNS)
			{
				errors.addElement("No valid DNS server is specified in MixOnCD panel.");
			}
		}

		return errors;
	}

	/**
	 * Saves the value of the IP address in the IPTextField.
	 * @param a_ipTextField an IPTextField
	 */
	protected void save(IPTextField a_ipTextField)
	{
		for (int i = 0; i < m_txtDNSServers.length; i++)
		{
			if (a_ipTextField == m_txtDNSServers[i])
			{
				String[] values = new String[m_txtDNSServers.length];

				for (int j = 0; j < values.length; j++)
				{
					if (m_txtDNSServers[j].isEnabled() && m_txtDNSServers[j].isCorrect())
					{
						values[j] = m_txtDNSServers[j].getText().trim();
					}
				}
				getConfiguration().setValues(a_ipTextField.getName(), values);
				return;
			}
		}

		super.save(a_ipTextField);
	}


	protected void load(JCheckBox a_checkbox)
	{
		if (a_checkbox == m_cbxMixOnCD)
		{
			m_cbxMixOnCD.setSelected(getConfiguration().isMixOnCDEnabled());
			return;
		}

		super.load(a_checkbox);
	}


	protected void save(JCheckBox a_checkBox)
	{
		if (a_checkBox == m_cbDHCP)
		{
			getConfiguration().setAttribute(XMLPATH_MIXONCD_NETWORK, XMLATTRIBUTE_DHCP,
											m_cbDHCP.isSelected());
		}
		else  if (a_checkBox == m_cbxMixOnCD)
		{
			if (m_cbxMixOnCD.isSelected())
			{
				if (!getConfiguration().isMixOnCDEnabled())
				{
					getConfiguration().setAttribute(MixOnCDPanel.XMLPATH_MIXONCD_NETWORK,
						MixOnCDPanel.XMLATTRIBUTE_DHCP, true);
				}
			}
			else
			{
				getConfiguration().removeNode(MixOnCDPanel.XMLPATH_MIXONCD);
			}
			enableComponents();
		}
		else
		{
			super.save(a_checkBox);
		}
	}

	protected void load(IPTextField a_ipTextField)
	{
		for (int i = 0; i < m_txtDNSServers.length; i++)
		{
			if (a_ipTextField == m_txtDNSServers[i])
			{
				String[] values = getConfiguration().getValues(a_ipTextField.getName());

				for (int j = 0; j < m_txtDNSServers.length; j++)
				{
					if (j < values.length && values[j] != null && values[j].trim().length() > 0)
					{
						m_txtDNSServers[j].setText(values[j]);
					}
					else
					{
						m_txtDNSServers[j].setText(null);
					}
				}

				return;
			}
		}

		super.load(a_ipTextField);
	}

	protected void enableComponents()
	{
		m_panelLocalNetworkSettings.setEnabled(m_cbxMixOnCD.isSelected());
		m_panelPasswords.setEnabled(m_cbxMixOnCD.isSelected());

		if (m_cbxMixOnCD.isSelected())
		{

			boolean bNoDHCP = !m_cbDHCP.isSelected();

			Component[] components = m_panelLocalNetworkSettings.getComponents();

			for (int i = 0; i < components.length; i++)
			{
				if (components[i] != m_cbDHCP)
				{
					components[i].setEnabled(bNoDHCP);
				}
			}

			for (int i = 0; i < m_txtDNSServers.length; i++)
			{
				m_txtDNSServers[i].setEnabled(true);
				m_lblDNSServersOptional[i].setEnabled(true);
				m_lblDNSServersMandatory[i].setEnabled(true);
				m_lblDNSServersHeadline.setEnabled(true);

				if (bNoDHCP)
				{
					m_lblDNSServersMandatory[i].setVisible(true);
					m_lblDNSServersOptional[i].setVisible(false);
					m_panelLocalNetworkSettings.replaceRow(m_lblDNSServersMandatory[i],
						m_txtDNSServers[i], 8 + i);
				}
				else
				{
					m_lblDNSServersMandatory[i].setVisible(false);
					m_lblDNSServersOptional[i].setVisible(true);
					m_panelLocalNetworkSettings.replaceRow(m_lblDNSServersOptional[i],
						m_txtDNSServers[i], 8 + i);
				}
			}
			m_panelLocalNetworkSettings.revalidate();
		}

	}

	public void actionPerformed(ActionEvent a_event)
	{
		for (int i = 0; i < m_btnsRemovePW.length; i++)
		{
			if (a_event.getSource() == m_btnsRemovePW[i])
			{
				m_passwordHashes[i] = null;
				savePasswords();
				loadPasswords();
				m_pwds[i].requestFocus();
			}
		}

		if (a_event.getSource() == m_cbxMixOnCD &&  m_cbxMixOnCD.isSelected())
		{
			MixConfig.info("MixOnCD download hint",
						   "You will have to download the MixOnCD iso-image " +
						   "from http://anon.inf.tu-dresden.de/develop/MixOnCD.iso.bz2 " +
						   "to create a bootable mix CD.");
		}
	}

	/**
	 * Registers for the correct help context every time the panel is painted.
	 * @param g Graphics
	 */
	public void paint(Graphics g)
	{
		super.paint(g);
		JAPHelp.getInstance().getContextObj().setContext("livecd");
	}

	public void focusGained(FocusEvent a_event)
	{
		for (int index = 0; index < m_btnsRemovePW.length; index++)
		{
			if (a_event.getSource() == m_btnsRemovePW[index])
			{
				if (!m_btnsRemovePW[index].isEnabled())
				{
					m_btnsRemovePW[index].transferFocus();
				}
			}
		}
		super.focusGained(a_event);
	}

	public void focusLost(FocusEvent a_event)
	{
		for (int i = 0; i < m_users.length; i++)
		{
			if (a_event.getSource() == m_pwds[i])
			{
				char[] password = m_pwds[i].getPassword();

				if (password == null || password.length == 0)
				{
					m_passwordHashes[i] = null;
				}
				else
				{
					try
					{
						m_passwordHashes[i] = new MD5Crypt().crypt(new String(password));
					}
					catch (NoSuchAlgorithmException a_e)
					{
						m_passwordHashes[i] = new DESCrypt().crypt(new String(password));
					}
				}

				savePasswords();
				loadPasswords();

				return;
			}
		}

		super.focusLost(a_event);
	}

	public void itemStateChanged(ItemEvent a_event)
	{
		super.itemStateChanged(a_event);

		if (a_event.getSource() == m_cbxMixOnCD)
		{
			m_panelLocalNetworkSettings.setEnabled(m_cbxMixOnCD.isSelected());
			m_panelPasswords.setEnabled(m_cbxMixOnCD.isSelected());

			if (m_cbxMixOnCD.isSelected())
			{
				boolean bDHCPEnabled = checkDHCPEnabled();
				if (m_cbDHCP.isSelected() != bDHCPEnabled)
				{
					m_cbDHCP.setSelected(bDHCPEnabled);
				}
			}
			else
			{
				getConfiguration().removeNode(XMLPATH_MIXONCD);
			}
		}
		else if (a_event.getSource() == m_cbDHCP && !m_cbDHCP.isSelected())
		{
			save(m_txtHostname);
			save(m_txtNetworkInterface);
			save(m_txtIP);
			save(m_txtSubnetMask);
			save(m_txtDefaultGateway);
			for (int i = 0; i < m_txtDNSServers.length; i++)
			{
				save(m_txtDNSServers[i]);
			}
		}
	}

	public void load() throws IOException
	{
		loadPasswords();
		super.load();
	}

	public void save() throws IOException
	{
		super.save();
		savePasswords();
	}

	private void setActivated(boolean a_bActivate)
	{
		if (m_cbxMixOnCD.isSelected() != a_bActivate)
		{
			m_cbxMixOnCD.setSelected(a_bActivate);

		}
	}

	private boolean checkDHCPEnabled()
	{
		String strEnabled = getConfiguration().getValue(MixOnCDPanel.XMLPATH_MIXONCD_NETWORK + "/" +
			MixOnCDPanel.XMLATTRIBUTE_DHCP);

		if (strEnabled == null)
		{
			getConfiguration().setAttribute(MixOnCDPanel.XMLPATH_MIXONCD_NETWORK,
											MixOnCDPanel.XMLATTRIBUTE_DHCP, true);
			strEnabled = "true";
		}

		return Boolean.valueOf(strEnabled).booleanValue();
	}


	private void savePasswords()
	{
		getConfiguration().setValues(XMLPATH_MIXONCD_LOGIN_PASSWORD, m_passwordHashes, "user", m_users);
	}


	private void loadPasswords()
	{
		String[] password;

		for (int i = 0; i < m_users.length; i++)
		{
			password = getConfiguration().getValues(XMLPATH_MIXONCD_LOGIN_PASSWORD, "user", m_users[i]);

			if (password.length == 0 || password[0] == null || password[0].trim().length() == 0)
			{
				m_passwordHashes[i] = null;
				m_btnsRemovePW[i].setEnabled(false);
				m_btnsRemovePW[i].invalidate();
				m_pwds[i].setText(null);
				m_pwds[i].setVisible(true);
				m_pwds[i].setEnabled(true);
				m_pwds[i].invalidate();
				m_txtHashedPasswords[i].setVisible(false);
				m_txtHashedPasswords[i].invalidate();
				m_panelPasswords.replaceRow(m_lblsPW[i],  m_btnsRemovePW[i], m_pwds[i], i);

			}
			else
			{
				m_passwordHashes[i] = password[0];
				m_btnsRemovePW[i].setEnabled(true);
				m_btnsRemovePW[i].invalidate();
				m_pwds[i].setVisible(false);
				m_pwds[i].invalidate();
				m_txtHashedPasswords[i] = new JTextField(password[0], VISIBLE_PASSWORD_LENGTH);
				m_txtHashedPasswords[i].setEditable(false);
				m_txtHashedPasswords[i].setCaretPosition(0);

				m_txtHashedPasswords[i].invalidate();
				m_panelPasswords.replaceRow(m_lblsPW[i], m_btnsRemovePW[i], m_txtHashedPasswords[i], i);
			}
			m_panelPasswords.revalidate();
		}
	}
}