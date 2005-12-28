package mixconfig.wizard;

import mixconfig.MixConfigPanel;
import java.util.Vector;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JTextField;
import mixconfig.networkpanel.IPTextField;
import mixconfig.IntegerDocument;
import javax.swing.JComboBox;
import java.awt.event.ItemEvent;
import mixconfig.MixConfig;
import java.awt.event.FocusEvent;
import java.awt.Container;
import mixconfig.MixConfiguration;
import java.io.IOException;
import javax.swing.event.ChangeEvent;
import mixconfig.ConfigurationEvent;
import javax.swing.event.ChangeListener;

/** This panel is a simplified form of
 * <CODE>mixconfig.networkpanel.NetworkPanel</CODE>, as the name suggests.<br>
 * It lets the user specify only one non-virtual, non-hidden listener interface for
 * the mix, and the network address of the InfoService host.<br>
 * No outgoing connections may be specified, as they are retrieved from the
 * InfoService via auto configuration at the mix's run-time.
 * @author ronin &lt;ronin2@web.de&gt;
 */
public class SimpleNetworkPanel extends MixConfigPanel implements ChangeListener
{

	/** A text field containing the InfoService host name */
	private JTextField m_tfISHost;

	/** A text field containing the InfoService port */
	private JTextField m_tfISPort;

	/** A text field containing this mix's host name */
	private JTextField m_tfLHost;

	/** A text field containing this mix's port */
	private JTextField m_tfLPort;

	/** A text field containing this mix's IP address (may be left blank if host name is
	 * specified)
	 */
	private IPTextField m_tfLIP;

	/** A text field containing the InfoService's IP address (may be left blank if host name is
	 * specified)
	 */
	private IPTextField m_tfISIP;

	/** A combo box to set the network protocol type: TCP or Unix Domain Socket, SSL or
	 * normal
	 */
	private JComboBox m_cbProtocol;

	/** The label for the listener host name field */
	private JLabel inHost;

	/** The label for the listener IP address name field */
	private JLabel inIP;

	/** The label for the listener port field */
	private JLabel inPort;

	/** Constructs a new instance of <CODE>SimpleNetworkPanel</CODE> */
	public SimpleNetworkPanel()
	{
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = GridBagConstraints.HORIZONTAL;

		GridBagLayout Listener_Layout = new GridBagLayout();
		JPanel simpleListenerInput = new JPanel(Listener_Layout);
		simpleListenerInput.setBorder(new TitledBorder("Configure listening address"));
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0;
		layout.setConstraints(simpleListenerInput, c);
		add(simpleListenerInput);

		GridBagConstraints f = new GridBagConstraints();
		f.anchor = GridBagConstraints.NORTHWEST;
		f.insets = new Insets(10, 10, 10, 10);
		f.fill = GridBagConstraints.HORIZONTAL;

		JLabel protocol = new JLabel("Protocol");
		f.gridy = 0;
		f.gridx = 0;
		f.weightx = 0;
		Listener_Layout.setConstraints(protocol, f);
		simpleListenerInput.add(protocol);

		m_cbProtocol = new JComboBox();
		m_cbProtocol.setName("Network/ListenerInterfaces/ListenerInterface/NetworkProtocol");
		m_cbProtocol.addItem("TCP");
		m_cbProtocol.addItem("Unix");
		m_cbProtocol.addItem("TCP (encrypted)");
		m_cbProtocol.addItem("Unix (encrypted)");
		m_cbProtocol.addItemListener(this);
		f.gridx = 1;
		Listener_Layout.setConstraints(m_cbProtocol, f);
		simpleListenerInput.add(m_cbProtocol);

		inHost = new JLabel("Host");
		f.gridx = 0;
		f.gridy++;
		Listener_Layout.setConstraints(inHost, f);
		simpleListenerInput.add(inHost);

		m_tfLHost = new JTextField(38);
		m_tfLHost.setText("");
		m_tfLHost.setName("Network/ListenerInterfaces/ListenerInterface/Host");
		m_tfLHost.addFocusListener(this);
		f.gridx = 1;
		f.weightx = 1;
		Listener_Layout.setConstraints(m_tfLHost, f);
		simpleListenerInput.add(m_tfLHost);

		inIP = new JLabel("IP");
		f.gridy++;
		f.gridx = 0;
		f.weightx = 0;
		Listener_Layout.setConstraints(inIP, f);
		simpleListenerInput.add(inIP);

		m_tfLIP = new IPTextField();
		m_tfLIP.setText("");
		m_tfLIP.setName("Network/ListenerInterfaces/ListenerInterface/IP");
		m_tfLIP.addFocusListener(this);
		f.gridx = 1;
		f.weightx = 0;
		f.fill = GridBagConstraints.NONE;
		Listener_Layout.setConstraints(m_tfLIP, f);
		simpleListenerInput.add(m_tfLIP);

		inPort = new JLabel("Port");
		f.gridy++;
		f.gridx = 0;
		f.weightx = 0;
		Listener_Layout.setConstraints(inPort, f);
		simpleListenerInput.add(inPort);

		m_tfLPort = new JTextField(5);
		m_tfLPort.setName("Network/ListenerInterfaces/ListenerInterface/Port");
		m_tfLPort.setText("");
		m_tfLPort.setDocument(new IntegerDocument(65535));
		m_tfLPort.setMinimumSize(m_tfLPort.getPreferredSize());
		m_tfLPort.addFocusListener(this);
		f.gridx = 1;
		Listener_Layout.setConstraints(m_tfLPort, f);
		simpleListenerInput.add(m_tfLPort);

		GridBagLayout Info_Layout = new GridBagLayout();

		JPanel panel3 = new JPanel(Info_Layout);
		panel3.setBorder(new TitledBorder("Info Service"));
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 0;
		layout.setConstraints(panel3, c);
		add(panel3);

		f = new GridBagConstraints();
		f.anchor = GridBagConstraints.NORTHWEST;
		f.insets = new Insets(10, 10, 10, 10);
		f.fill = GridBagConstraints.HORIZONTAL;

		JLabel host = new JLabel("Host");
		f.gridx = 0;
		f.gridy = 0;
		Info_Layout.setConstraints(host, f);
		panel3.add(host);

		m_tfISHost = new JTextField(38);
		m_tfISHost.setText("");
		m_tfISHost.setName("Network/InfoService/Host");
		m_tfISHost.addFocusListener(this);
		f.gridx = 1;
		f.weightx = 1;
		Info_Layout.setConstraints(m_tfISHost, f);
		panel3.add(m_tfISHost);

		JLabel IP = new JLabel("IP");
		f.gridy = 1;
		f.gridx = 0;
		f.weightx = 0;
		Info_Layout.setConstraints(IP, f);
		panel3.add(IP);

		m_tfISIP = new IPTextField();
		m_tfISIP.setText("");
		m_tfISIP.setName("Network/InfoService/IP");
		m_tfISIP.addFocusListener(this);
		f.gridx = 1;
		f.weightx = 0;
		f.fill = GridBagConstraints.NONE;
		Info_Layout.setConstraints(m_tfISIP, f);
		panel3.add(m_tfISIP);

		JLabel port = new JLabel("Port");
		f.gridy = 2;
		f.gridx = 0;
		f.weightx = 0;
		Info_Layout.setConstraints(port, f);
		panel3.add(port);

		m_tfISPort = new JTextField(5);
		m_tfISPort.setName("Network/InfoService/Port");
		m_tfISPort.setText("");
		m_tfISPort.setDocument(new IntegerDocument(65535));
		m_tfISPort.setMinimumSize(m_tfISPort.getPreferredSize());
		m_tfISPort.addFocusListener(this);
		f.gridx = 1;
		Info_Layout.setConstraints(m_tfISPort, f);
		panel3.add(m_tfISPort);

	}

	public void itemStateChanged(ItemEvent i)
	{
		if (i.getSource() == m_cbProtocol)
		{
			if (m_cbProtocol.getSelectedIndex() == 1 || m_cbProtocol.getSelectedIndex() == 3)
			{
				inHost.setText("File");
				m_tfLHost.setName("Network/ListenerInterfaces/ListenerInterface/File");
			}
			else
			{
				inHost.setText("Host");
				m_tfLHost.setName("Network/ListenerInterfaces/ListenerInterface/Host");
			}
		}
		super.itemStateChanged(i);
	}

	public void focusLost(FocusEvent e)
	{
		try
		{
			if (e.getSource() instanceof JTextField)
			{
				Container c = ( (JTextField) e.getSource()).getParent();
				if (c instanceof IPTextField)
				{
					IPTextField ipt = (IPTextField) c;
					if (ipt.isCorrect())
					{
						getConfiguration().setAttribute(ipt.getName(), ipt.getText());
					}
				}
				else
				{
					super.focusLost(e);
				}
			}
			else
			{
				super.focusLost(e);
			}
		}
		catch (Exception ex)
		{
			MixConfig.handleException(ex);
		}
	}

	public Vector check()
	{
		Vector errors = new Vector();

		if (this.m_cbProtocol.getSelectedIndex() == 0 || this.m_cbProtocol.getSelectedIndex() == 2)
		{
			if ( (m_tfLHost.getText() == null || m_tfLHost.getText().equals("")) && !m_tfLIP.isCorrect())
			{
				errors.addElement(new String(
					"Listener host name and/or IP address is missing in Network Panel."));
			}
		}
		else
		{
			if (m_tfLHost.getText() == null || m_tfLHost.getText().equals(""))
			{
				errors.addElement(new String(
					"Unix domain socket file name is missing in Network Panel."));
			}
		}

		if ( (m_tfISHost.getText() == null || m_tfISHost.getText().equals("")) && !m_tfISIP.isCorrect())
		{
			errors.addElement(new String(
				"InfoService host name and/or IP address is missing in Network Panel."));
		}

		return errors;
	}

	public void stateChanged(ChangeEvent e)
	{
		if(e instanceof ConfigurationEvent)
		{
			if(((ConfigurationEvent) e).getChangedAttribute().endsWith("MixType"))
			{
				enableComponents();
			}
		}
	}

	protected void enableComponents()
	{
		if (getConfiguration() != null)
		{
			Integer t = new Integer(getConfiguration().getAttribute("General/MixType"));
			setEnabled(getParent() instanceof ConfigWizardPanel &&
					   t.intValue() != MixConfiguration.MIXTYPE_LAST);
		}

		boolean e = (m_cbProtocol.getSelectedIndex() == 1 || m_cbProtocol.getSelectedIndex() == 3);
		m_tfLIP.setEnabled(!e);
		m_tfLPort.setEditable(!e);
		inPort.setEnabled(!e);
		inIP.setEnabled(!e);
	}

	public void setConfiguration(MixConfiguration a_mixConf) throws IOException
	{
		super.setConfiguration(a_mixConf);
		if (a_mixConf != null)
		{
			a_mixConf.removeChangeListener(this);
			a_mixConf.addChangeListener(this);
			a_mixConf.setAttribute("Network/InfoService/AllowAutoConfiguration", true);
		}
	}

	protected void load(JComboBox a)
	{
		if (a == this.m_cbProtocol && getConfiguration() != null)
		{
			String vs[] =
				{
				"RAW/TCP", "RAW/UNIX", "SSL/TCP", "SSL/UNIX"};

			String v = getConfiguration().getAttribute(
				"Network/ListenerInterfaces/ListenerInterface/NetworkProtocol");

			m_cbProtocol.setSelectedIndex(0);

			for (int i = 0; i < vs.length; i++)
			{
				if (vs[i].equals(v))
				{
					m_cbProtocol.setSelectedIndex(i);
				}
			}
		}
	}

	protected void save(JComboBox a)
	{
		if (a == this.m_cbProtocol && getConfiguration() != null)
		{
			String vs[] =
				{
				"RAW/TCP", "RAW/UNIX", "SSL/TCP", "SSL/UNIX"};

			getConfiguration().setAttribute(
				"Network/ListenerInterfaces/ListenerInterface/NetworkProtocol",
				vs[m_cbProtocol.getSelectedIndex()]);

		}
	}

	protected void save(JTextField a)
	{
		if(a == this.m_tfLHost)
		{
			save(this.m_cbProtocol);
		}
		super.save(a);
	}
}