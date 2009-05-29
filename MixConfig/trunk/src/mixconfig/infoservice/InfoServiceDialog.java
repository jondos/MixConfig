/*
 Copyright (c) 2008, JonDos GmbH
 All rights reserved.
 */
package mixconfig.infoservice;

import gui.JAPJIntField;
import gui.MixConfigTextField;
import gui.dialog.JAPDialog;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mixconfig.MixConfiguration;

import anon.infoservice.ListenerInterface;

/**
 * Dialog for adding new, respectively modifying existing Info Service configurations
 * TODO: Add more components to enable adding of additional ListenerInterfaces
 * @author renner
 */
public class InfoServiceDialog extends JAPDialog
{
	// TextFields for host and port
	private MixConfigTextField m_tfHost;
	private JAPJIntField m_tfPort;
	
	// Labels for host and port
	private JLabel m_lblHost, m_lblPort;
	
	/**
	 * Constructor for a "New"-dialog
	 * @param parent
	 * @param title
	 * @param where
	 */
	public InfoServiceDialog(Frame parent, String title, final InfoServiceTableModel model)
	{
		super(parent, title);
		createDialog(null, model);
	}
	
	/**
	 * Constructor for a "Modify"-dialog
	 * @param parent
	 * @param title
	 * @param where
	 * @param data
	 */
	public InfoServiceDialog(Frame parent, String title, final InfoServiceTableModel model, InfoServiceData data)
	{
		super(parent, title);
		createDialog(data, model);
	}

	protected String getType()
	{
		return MixConfiguration.XML_PATH_INFO_SERVICE;
	}
	
	/**
	 * Create a data-object from the TextFields and return it
	 * @return
	 */
	protected InfoServiceData getData()
	{
		// XXX: Trim the hostname here?
		return new InfoServiceData(getType(), m_tfHost.getText().trim(), m_tfPort.getInt());
	}

	private void createDialog(final InfoServiceData data, final InfoServiceTableModel where)
	{
		// XXX: Good size?
		setSize(500, 350);

		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);

		// Constraints for the labels
		GridBagConstraints lc = new GridBagConstraints();
		lc.anchor = GridBagConstraints.WEST;
		lc.insets = new Insets(5, 5, 5, 5);
		lc.gridx = 0;
		lc.gridy = 0;
		lc.weightx = 1;

		// Constraints for all the other things...
		GridBagConstraints rc = new GridBagConstraints();
		rc.anchor = GridBagConstraints.WEST;
		rc.insets = new Insets(5, 5, 5, 5);
		rc.gridx = 1;
		rc.gridy = 0;
		rc.weightx = 0;

		// Add the single components
		addHost(data, layout, lc, rc);
		addPort(data, layout, lc, rc);
		addKeys(data, where, layout, lc, rc);
		
		//addTransport(data, layout, lc, rc);
		//addOptions(data, layout, lc, rc, !a_bMixOnCD);
		
		setResizable(false);
		pack();
	}
	
	// XXX Do we really need this ??
	protected final ActionListener nextFocusAction = new ActionListener()
	{
		public void actionPerformed(ActionEvent evt)
		{
			((Component)evt.getSource()).transferFocus();
		}
	};
	
	protected void addHost(final InfoServiceData data, GridBagLayout layout, GridBagConstraints lc,
			   GridBagConstraints rc)
	{
		// The left side:
		m_lblHost = new JLabel("Host");
		layout.setConstraints(m_lblHost, lc);
		getContentPane().add(m_lblHost);
		lc.gridy++;
		// The right side:
		rc.anchor = GridBagConstraints.WEST;
		rc.gridwidth = 7;
		if (data == null)
		{
			m_tfHost = new MixConfigTextField(20);
		}
		else
		{
			m_tfHost = new MixConfigTextField(data.getListenerInterface(0).getHost(), 20);
		}
		m_tfHost.setMinimumSize(m_tfHost.getPreferredSize());
		layout.setConstraints(m_tfHost, rc);
		getContentPane().add(m_tfHost);
		m_tfHost.addActionListener(nextFocusAction);
		rc.gridy++;
		//if (m_firstone == null)
		//{
		//m_firstone = m_tfHost;
	    //}
		//m_tfHost.setEnabled(a_enabled);
		//m_hostLabel.setEnabled(a_enabled);
	}

	protected void addPort(final InfoServiceData data, GridBagLayout layout, GridBagConstraints lc,
			   GridBagConstraints rc)
	{
		// The left side:
		m_lblPort = new JLabel("Port");
		layout.setConstraints(m_lblPort, lc);
		getContentPane().add(m_lblPort);
		lc.gridy++;
		// The right side:
		rc.gridwidth = 7;
		m_tfPort = new JAPJIntField(ListenerInterface.PORT_MAX_VALUE, true);
		m_tfPort.setMinimumSize(m_tfPort.getPreferredSize());
		if (data != null)
		{
			m_tfPort.setInt(data.getListenerInterface(0).getPort());
		}
		else
		{
			m_tfPort.setInt(80);
		}
		layout.setConstraints(m_tfPort, rc);
		getContentPane().add(m_tfPort);
		m_tfPort.addActionListener(nextFocusAction);
		rc.gridy++;
		//if (m_firstone == null)
		//{
		//	m_firstone = tfPort;
		//}
	}
	
	protected void addKeys(final InfoServiceData data, final InfoServiceTableModel model, GridBagLayout layout,
			   GridBagConstraints lc, GridBagConstraints rc)
	{
		GridBagLayout keyLayout = new GridBagLayout();
		JPanel keys = new JPanel(keyLayout);
		GridBagConstraints kc = new GridBagConstraints();
		kc.weightx = 1;
		kc.gridx = 0;
		kc.gridy = 0;
		kc.gridwidth = 1;
		kc.fill = GridBagConstraints.HORIZONTAL;
		kc.insets = new Insets(1, 1, 1, 1);
		JButton key;
		if (data == null)
		{
			key = new JButton("Add");
			key.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ev)
				{
					if (!m_tfHost.getText().equals("") && !m_tfPort.getText().equals(""))
					{
						model.addData(getData());
						dispose();	
					}
				}
			});
		}
		else
		{
			key = new JButton("Apply");
			key.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ev)
				{
					if (!m_tfHost.getText().equals("") && !m_tfPort.getText().equals(""))
					{
						model.changeData(getData(), data);
						dispose();	
					}
				}
			});
		}
		key.setActionCommand("Ok");
		keyLayout.setConstraints(key, kc);
		keys.add(key);
		kc.gridx++;
		// In any case add the Cancel-button
		key = new JButton("Cancel");
		key.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				dispose();
			}
		});
		keyLayout.setConstraints(key, kc);
		keys.add(key);
		//if (m_firstone == null)
		//{
		//	m_firstone = key;
		//}
		lc.gridwidth = 8;
		lc.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(keys, lc);
		getContentPane().add(keys);
		lc.gridy++;
		rc.gridy++;
	}
}
