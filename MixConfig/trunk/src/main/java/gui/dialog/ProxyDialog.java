package gui.dialog;

import gui.TitledGridBagPanel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.Proxy.Type;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import logging.LogType;

public class ProxyDialog extends JAPDialog implements ActionListener
{

	private TitledGridBagPanel panel = new TitledGridBagPanel();
	
	private JLabel hostLabel = new JLabel("Host");
	private JLabel portLabel = new JLabel("Port");
	
	private JTextField hostTextField = new JTextField(20);
	private JTextField portTextField = new JTextField(20);
	
	private JButton cancelButton = new JButton("Cancel");
	private JButton disableButton = new JButton("Disable");
	private JButton okButton = new JButton("OK");

	private Proxy returnProxy = null;
	
	public ProxyDialog(Component component)
	{
		this(component, null);
	}

	public ProxyDialog(Component component, Proxy presetProxy) 
	{
		super(component, "Proxy Configuration");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		panel.addRow(hostLabel, hostTextField);
		panel.addRow(portLabel, portTextField);
		JPanel buttonPanel = new JPanel();
		
		if( (presetProxy != null) && 
			(presetProxy.address() != null) && 
			(presetProxy.address() instanceof InetSocketAddress))
		{
			hostTextField.setText(((InetSocketAddress) presetProxy.address()).getHostName());
			portTextField.setText(""+((InetSocketAddress) presetProxy.address()).getPort());
		}
		
		buttonPanel.add(cancelButton);
		buttonPanel.add(disableButton);
		buttonPanel.add(okButton);
		panel.addRow(buttonPanel);
		
		okButton.addActionListener(this);
		disableButton.addActionListener(this);
		cancelButton.addActionListener(this);
		getContentPane().add(panel);
		
		pack();
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == disableButton)
		{
			returnProxy = Proxy.NO_PROXY;
			dispose();
		}
		if(e.getSource() == cancelButton)
		{
			returnProxy = null;
			dispose();
		}
		if(e.getSource() == okButton)
		{
			String hostText = hostTextField.getText();
			String portText = portTextField.getText();
			if((hostText == null) || hostText.equals(""))
			{
				JAPDialog.showErrorDialog(this, "No host specified.", LogType.MISC);
			}
			else if((portText == null) || portText.equals(""))
			{
				JAPDialog.showErrorDialog(this, "No port specified.", LogType.MISC);
			}
			else
			{
				try
		    	{
		    		SocketAddress proxyAddress = new InetSocketAddress(
		    				hostText, Integer.parseInt(portText));
		    		
		    		returnProxy = new Proxy(Proxy.Type.HTTP, proxyAddress);
		    		dispose();
		    	}
		    	catch(NumberFormatException nfe)
		    	{
		    		JAPDialog.showErrorDialog(this,
		    				"Bad proxy port: "+portText+". Port must be a number between 0 and 65535",
		    				LogType.MISC);
		    	}
		    	catch(IllegalArgumentException ila)
		    	{
		    		JAPDialog.showErrorDialog(this, ila.getMessage(), LogType.MISC);
		    	}
			}
		}
	}

	public static Proxy showProxyDialog(Component component, Proxy presetProxy)
	{
		ProxyDialog d = new ProxyDialog(component, presetProxy);
		d.setVisible(true);
		return d.returnProxy;
	}
}
