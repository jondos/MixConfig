package mixconfig.networkpanel;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import mixconfig.IntegerDocument;

final public class IPTextField extends JPanel
{
	private JTextField[] iptext;

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

	public IPTextField()
	{
		super();
		initIPTextField("");
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
		int pos = 0;
		for (int i = 0; i < 4; i++)
		{
			int npos = str.indexOf('.', pos);
			if (npos < 0)
			{
				iptext[i].setText("");
			}
			else
			{
				iptext[i].setText(str.substring(pos, npos - 1));
				pos = npos + 1;
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
}
