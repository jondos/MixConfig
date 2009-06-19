package mixconfig.panels;

import gui.JAPHelpContext;
import gui.TitledGridBagPanel;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import mixconfig.tools.EmailComposer;

public class WizardFinishPanel extends MixConfigPanel implements ActionListener {

	private String options[] =
	{
		"- Click the 'Finish' button to save the configuration and quit this wizard.",
		"- Click the 'Back' button if you want to make changes before saving.",
		"- Click the 'Cancel' button to quit without saving."
    };
	
	public WizardFinishPanel()
	{
		super("Finish");
			
		// Setup constraints
		GridBagConstraints c = getInitialConstraints();
		c.weightx = 0;		
		c.anchor = GridBagConstraints.CENTER;
		this.add(new JLabel("Congratulations, your mix configuration is now complete!"), c);
			
		// Create a panel containing the message
		TitledGridBagPanel panelMessage = new TitledGridBagPanel();
		panelMessage.removeInsets();		
		// Add the strings one in each row
		for (int i = 0; i < options.length; i++)
		{
			panelMessage.addRow(new JLabel(options[i]), null);
		}
		c.gridy++;
		this.add(panelMessage, c);
		
		// Add a button to compose email
		JButton buttonEmail = new JButton("Click here to send certificates to JonDos");
		buttonEmail.setActionCommand("email");
		buttonEmail.addActionListener(this);		
		// Set constraints and add
		c.fill = GridBagConstraints.NONE;
		c.gridy++;
		this.add(buttonEmail, c);
	}
	
	@Override
	public Vector<String> check() {
		return null;
	}

	@Override
	protected void enableComponents() 
	{
		// TODO Auto-generated method stub
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("email"))
		{
			try
			{
				LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Composing email ..");
				EmailComposer.composeEmail();
			}
			catch (URISyntaxException use)
			{
				LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Error while composing email: " + use.getMessage());
			}
			catch (IOException ioe)
			{
				LogHolder.log(LogLevel.DEBUG, LogType.MISC, "Error while composing email: " + ioe.getMessage());
			}
		}
	}

	public String getHelpContext() 
	{
		return JAPHelpContext.INDEX;
	}

	public Container getHelpExtractionDisplayContext() 
	{
		return null;
	}
}
