package mixconfig.networkpanel;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import mixconfig.ConfigFrame;

class OutgoingDialog extends ConnectionDialog
{
	private ButtonGroup proxytype;

	protected String getType()
	{
		return (proxytype == null) ? "NextMix" : "Proxy";
	}

	protected ConnectionData getData()
	{
		ConnectionData data = super.getData();
		if (proxytype != null)
		{
			data.setFlags(proxytype.getSelection().getActionCommand().equals("HTTP")
						  ? ConnectionData.HTTP_PROXY : ConnectionData.SOCKS_PROXY);
		}
		return data;
	}

	protected void addType(final ConnectionData data, GridBagLayout layout, GridBagConstraints lc,
						   GridBagConstraints rc)
	{
		JLabel label = new JLabel("Proxy Type");
		layout.setConstraints(label, lc);
		getContentPane().add(label);
		lc.gridy++;

		int ptype;
		if (data == null)
		{
			ptype = ConnectionData.HTTP_PROXY;
		}
		else
		{
			ptype = data.getFlags() & ConnectionData.PROXY_MASK;

		}
		rc.anchor = GridBagConstraints.CENTER;
		rc.gridwidth = 3;
		proxytype = new ButtonGroup();
		JRadioButton t = new JRadioButton("HTTP", ptype != ConnectionData.SOCKS_PROXY);
		t.setActionCommand("HTTP");
		layout.setConstraints(t, rc);
		getContentPane().add(t);
		proxytype.add(t);
		if (firstone == null)
		{
			firstone = t;

		}
		rc.gridx += 4;
		t = new JRadioButton("Socks", ptype == ConnectionData.SOCKS_PROXY);
		t.setActionCommand("Socks");
		layout.setConstraints(t, rc);
		getContentPane().add(t);
		proxytype.add(t);
		rc.gridy++;
		rc.gridx -= 4;
	}

	private void createDialog(final ConnectionData data, final OutgoingConnectionTableModel where)
	{
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

		if (ConfigFrame.m_GeneralPanel.getMixType().equals("LastMix"))
		{
			addType(data, layout, lc, rc);
		}
		else
		{
			proxytype = null;
		}
		addTransport(data, layout, lc, rc);
		addName(data, layout, lc, rc);
		addIP(data, layout, lc, rc);
		addPort(data, layout, lc, rc);
		addKeys(data, where, layout, lc, rc);

		pack();
		firstone.requestFocus();
	}

	OutgoingDialog(Frame parent, String title, final OutgoingConnectionTableModel where)
	{
		super(parent, title);
		createDialog(null, where);
		this.setLocationRelativeTo(parent);
	}

	OutgoingDialog(Frame parent, String title, final OutgoingConnectionTableModel where, ConnectionData data)
	{
		super(parent, title);
		createDialog(data, where);
		this.setLocationRelativeTo(parent);
	}
}