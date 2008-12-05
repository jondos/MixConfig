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

import gui.dialog.JAPDialog;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import logging.LogType;
import mixconfig.panels.AdvancedPanel;
import mixconfig.panels.GeneralPanel;
import mixconfig.panels.MixConfigPanel;
import mixconfig.panels.NextMixProxyPanel;
import mixconfig.panels.OwnCertificatesPanel;
import mixconfig.panels.PaymentPanel;
import mixconfig.panels.PreviousMixPanel;
import mixconfig.panels.TermsAndConditionsPanel;
import anon.util.XMLParseException;

/**
 * The Frame of the MixConfig Application.
 */
public class ConfigFrame extends JPanel
{
	private JTabbedPane m_tabbedPane;
	private MixConfigPanel[] m_panels;

	public ConfigFrame(JFrame parent) throws IOException
	{
		m_panels = new MixConfigPanel[7];
		
		// Currently not displayed:
		//m_panels[0] = new MixOnCDPanel();
		//m_panels[0] = new CascadePanel();
		
		m_panels[0] = new GeneralPanel();
		m_panels[1] = new AdvancedPanel();
		m_panels[2] = new PaymentPanel();
		m_panels[3] = new OwnCertificatesPanel(parent == null);
		m_panels[4] = new PreviousMixPanel();
		m_panels[5] = new NextMixProxyPanel();
		m_panels[6] = new TermsAndConditionsPanel();
		
		m_tabbedPane = new JTabbedPane();
		for (int i = 0; i < m_panels.length; i++)
		{
			m_tabbedPane.addTab(m_panels[i].getName(), m_panels[i]);
		}
		m_tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));

		setLayout(new BorderLayout());
		add(m_tabbedPane, BorderLayout.CENTER);

		setConfiguration(MixConfig.getMixConfiguration());
	}

	/**
	 * Call the load-method of each Panel
	 * This is necessary if you change the view (expert|wizard)
	 */
	protected void load()
	{
		try
		{
			for (int i = 0; i < m_panels.length; i++)
			{
				m_panels[i].load();
			}
		}
		catch (Exception io)
		{
			JAPDialog.showErrorDialog(MixConfig.getMainWindow(),
									   "Error while loading MixConfiguration", LogType.MISC, io);
		}
		if (!m_tabbedPane.getSelectedComponent().isEnabled())
		{
			reset();
		}
	}

	/** Decision which panel active at the moment
	*  Necessary if you change from Wizard -> Expert view
	*/
	protected void setActivePanel(Class a_panelClass)
	{
		if (a_panelClass == null)
		{
			return;
		}
		for (int i = 0; i < m_panels.length; i++)
		{
			if (m_panels[i].getClass().equals(a_panelClass))
			{
				m_tabbedPane.setSelectedComponent(m_panels[i]);
				return;
			}
		}
	}

	/** Clears all data in the panels and restarts with a new configuration object.
	 * @throws IOException If a communication error occurs
	 * @throws XMLParseException If an XML error occurs
	 */
	protected void reset()
	{
		m_tabbedPane.setSelectedComponent(m_panels[0]);
	}

	/** Notifies the configuration panels about a possibly new underlying configuration
	 * object and makes them load the config data from the configuration object into
	 * their controls.
	 * @param m The configuration object to be set
	 * @throws IOException If an error occurs while loading data from the config object into a panel
	 */
	protected void setConfiguration(MixConfiguration m) throws IOException
	{
		if (m == null)
		{
			m = MixConfig.getMixConfiguration();
		}
		else
		{
			MixConfig.setMixConfiguration(m);
		}

		for (int i = 0; i < m_panels.length; i++)
		{
			m_panels[i].setConfiguration(m);
		}
	}

	/** Makes all of the configuration panels check their data for inconsistencies and
	 * returns possible error and warning messages as an array of <CODE>String</CODE>s.
	 * @return An array of <CODE>String</CODE> containing possible error messages
	 */
	protected String[] check() throws IOException
	{
		Vector errors[] = new Vector[m_panels.length];

		setConfiguration(MixConfig.getMixConfiguration());

		for (int i = 0; i < m_panels.length; i++)
		{
				errors[i] = m_panels[i].check();
				for (int j = 0; j < errors[i].size(); j++)
				{
					errors[i].setElementAt(errors[i].elementAt(j) + " (" + m_panels[i].getName() + ")", j);
				}
		}

		int size = 0;
		for (int i = 0; i < errors.length; i++)
		{
			if (m_panels[i].isEnabled())
			{
				size += errors[i].size();
			}
		}

		String[] asString = new String[size];

		int k = 0;

		for (int j = 0; j < errors.length; j++)
		{
			if (m_panels[j].isEnabled())
			{
				for (int i = 0; i < errors[j].size(); i++)
				{
					asString[k++] = (String) errors[j].elementAt(i);
				}
			}
		}
		return asString;
	}
}
