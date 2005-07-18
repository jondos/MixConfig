package mixconfig.networkpanel;

import java.util.Vector;
import java.util.Enumeration;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import mixconfig.IntegerDocument;
import java.awt.event.FocusListener;
import java.net.InetAddress;
import java.net.*;

final public class IPTextField extends JPanel implements FocusListener
{
	private JTextField[] iptext;
	private Vector m_focusListeners;

	public IPTextField()
	{
		super();
		super.addFocusListener(this);

		initIPTextField("");

		for (int i = 0; i < iptext.length; i++)
		{
			iptext[i].addFocusListener(this);
		}
	}


	private void initIPTextField(String IPStr)
	{
		final ActionListener nextfocusaction = new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				( (Component) evt.getSource()).transferFocus();
			}
		};

		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		iptext = new JTextField[4];

		GridBagConstraints ic = new GridBagConstraints();
		ic.anchor = GridBagConstraints.WEST;
		ic.fill = GridBagConstraints.HORIZONTAL;
		ic.insets = new Insets(1, 1, 1, 1);
		ic.gridx = 0;
		ic.gridy = 0;

		int pos = 0;
		for (int i = 0; i < 4; i++)
		{
			int npos = IPStr.indexOf('.', pos);
			String str;
			if (npos < 0)
			{
				str = "";
			}
			else
			{
				str = IPStr.substring(pos, npos - 1);
				pos = npos + 1;
			}
			if (i > 0)
			{
				JLabel punkt = new JLabel(".");
				ic.weightx = 0;
				layout.setConstraints(punkt, ic);
				add(punkt);
				ic.gridx++;
			}
			iptext[i] = new JTextField(3);
			iptext[i].setMinimumSize(iptext[i].getPreferredSize());
			iptext[i].setDocument(new IntegerDocument(255, iptext[i]));
			iptext[i].setText(str);
			ic.weightx = 1;
			layout.setConstraints(iptext[i], ic);
			add(iptext[i]);
			iptext[i].addActionListener(nextfocusaction);
			ic.gridx++;
		}
	}

	public void addFocusListener(FocusListener a_fl)
	{
		if (m_focusListeners == null)
		{
			m_focusListeners = new Vector();
		}

		if (a_fl != null && !m_focusListeners.contains(a_fl))
		{
			m_focusListeners.addElement(a_fl);
		}
	}

	public void removeFocusListener(FocusListener a_listener)
	{
		if (m_focusListeners != null)
		{
			m_focusListeners.removeElement(a_listener);
		}
	}

	public String getText()
	{
		String str = "";
		for (int i = 0; i < 4; i++)
		{
			if (iptext[i].getText().length() == 0)
			{
				str += "0";
			}
			else
			{
				str += iptext[i].getText();
			}
			if (i < 3)
			{
				str += ".";
			}
		}
		return str;
	}

	public void setText(String str)
	{
		try
		{
			// make sure str is not null; throw a NullPointerException otherwise
			str.length();

			InetAddress ip = InetAddress.getByName(str);
			byte b[] = ip.getAddress();
			for (int i = 0; i < 4; i++)
			{
				// byte is always signed in Java, IP adresses aren't
				if (b[i] >= 0)
				{
					iptext[i].setText(Byte.toString(b[i]));
				}
				else
				{
					iptext[i].setText(Integer.toString(b[i] + 256));
				}
			}
			return;
		}
		catch (UnknownHostException ex)
		{
		}
		catch (NullPointerException npe)
		{
		}
		for (int i = 0; i < 4; i++)
		{
			iptext[i].setText("");
		}
	}

	public void setEnabled(boolean enabled)
	{
		for(int i=0;i<iptext.length;i++)
		{
			if(iptext[i] != null)
			{
				iptext[i].setEnabled(enabled);
			}
		}
	}

	public boolean isEmpty()
	{
		for (int i = 0; i < 4; i++)
		{
			if (iptext[i].getText().length() != 0)
			{
				return false;
			}
		}
		return true;
	}

	public boolean isCorrect()
	{
		for (int i = 0; i < 4; i++)
		{
			if (iptext[i].getText().length() == 0)
			{
				return false;
			}
		}
		return true;
	}


	public void focusGained(FocusEvent event)
	{
		Enumeration listeners = m_focusListeners.elements();
		event = new FocusEvent(this, event.getID());
		while(listeners.hasMoreElements())
		{
			((FocusListener)listeners.nextElement()).focusGained(event);
		}
	}

	public void focusLost(FocusEvent event)
	{
		if (isCorrect() || isEmpty())
		{
			Enumeration listeners = m_focusListeners.elements();
			event = new FocusEvent(this, event.getID());
			while (listeners.hasMoreElements())
			{
				( (FocusListener) listeners.nextElement()).focusLost(event);
			}
		}
	}

}
