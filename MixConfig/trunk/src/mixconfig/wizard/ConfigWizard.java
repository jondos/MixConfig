package mixconfig.wizard;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.xml.sax.SAXException;
import mixconfig.MixConfig;
import mixconfig.MixConfiguration;
import java.io.FileInputStream;

/** A class that displays the Mix configuration panels as a wizard. To the left of
 * the panel, a logo is displayed; at the bottom, there are three navigation
 * buttons (back, next, and cancel). The center of the panel displays the wizard's
 * pages, where only one page is visible at a time.<br>
 * Upon click on &quot;next&quot;, a plausibility check is performed on the current
 * page and a warning about any inconsistent input is shown. If there are no
 * warnings, the wizard continues by displaying the next page. If the end is
 * reached, the &quot;next&quot; button changes into a &quot;finish&quot; button.
 * When this is clicked, a file dialog is shown to let the user save the new
 * configuration.
 * @author ronin &lt;ronin2@web.de&gt;
 */
public class ConfigWizard extends JPanel implements ActionListener, ChangeListener
{
	/** The path to the logo to display at the left side of the wizard */
	private static String confWizLogoPath = new String("anonLogo.jpg");

	/** A container laid out with a <CODE>CardLayout</CODE> that contains the wizard's
	 * pages
	 */
	private ConfigWizardPanel m_wizPanel;

	/** The forward navigation button */
	private JButton m_bttnForward;

	/** The back navigation button */
	private JButton m_bttnBack;

	/** The cancel navigation button */
	private JButton m_bttnClose;

	/** Constructs a new instance of <CODE>ConfigWizard</CODE>
	 * @throws IOException If an I/O error occurs while saving the configuration
	 */
	public ConfigWizard() throws IOException // -- DEBUG: , SAXException, ParserConfigurationException
	{
		// DEBUG
//		MixConfig.setMixConfiguration(new MixConfiguration(new java.io.FileReader(
//			"/home/ronin/mixtest-payment-wiz.xml")));

		setLayout(new BorderLayout());

		JLabel imageCanvas = null;


		ImageIcon confWizLogo = MixConfig.loadImage(confWizLogoPath);
		imageCanvas = new JLabel(confWizLogo);
		m_wizPanel = new ConfigWizardPanel();
		m_wizPanel.addChangeListener(this);

		m_bttnBack = new JButton("<- Back");
		m_bttnClose = new JButton("Cancel");
		m_bttnForward = new JButton("Next ->");

		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(m_bttnBack);
		buttonBox.add(m_bttnForward);
		buttonBox.add(m_bttnClose);

		add(imageCanvas, BorderLayout.WEST);
		add(m_wizPanel, BorderLayout.CENTER);
		add(buttonBox, BorderLayout.SOUTH);

		m_bttnForward.addActionListener(this);
		m_bttnBack.addActionListener(this);
		m_bttnClose.addActionListener(this);

		stateChanged(null);
	}

	public void stateChanged(ChangeEvent e)
	{
		m_bttnBack.setEnabled( (m_wizPanel.getState() & ConfigWizardPanel.STATE_BEGIN) == 0);
	}

	public void actionPerformed(ActionEvent e)
	{
		try
		{
			m_bttnForward.setText("Next ->");

			try
			{
				if (e.getSource() == m_bttnForward)
				{
					m_wizPanel.forward();
				}
				else if (e.getSource() == m_bttnBack)
				{
					m_wizPanel.back();
				}
				else if (e.getSource() == m_bttnClose)
				{
					MixConfig.getMainWindow().dispose();

				}
				int i = m_wizPanel.getState();
				if ( (i & ConfigWizardPanel.STATE_READY_TO_FINISH) != 0)
				{
					m_bttnForward.setText("Finish");
				}
			}
			catch (CannotContinueException cce)
			{
				int i = m_wizPanel.getState();
				if ( (i & ConfigWizardPanel.STATE_STOP) > 0)
				{
					// TODO: if cce was thrown due to end of wizard, save config
					String[] msg = cce.getMessages();
					if (msg != null && msg.length > 0)
					{
						JOptionPane.showMessageDialog(
							MixConfig.getMainWindow(),
							msg,
							"Errors",
							JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else if ( (i & ConfigWizardPanel.STATE_END) > 0)
				{
					m_bttnForward.setText("Finish");
					if (save())
					{
						// TODO: Start mix now
						System.exit(0);
					}
				}
			}
		}
		catch (Exception ex)
		{
			MixConfig.handleException(ex);
		}
	}

	/** Shows a file dialog and saves the configuration
	 * @throws IOException If an I/O error occurs while saving the configuration
	 * @return <CODE>true</CODE> if the saving succeeded, <CODE>false</CODE> if it was aborted by the user
	 */
	private boolean save() throws IOException
	{
		MixConfiguration mixConf = MixConfig.getMixConfiguration();

		String fileName = MixConfig.getCurrentFileName();
		File file;

		if(fileName != null)
		{
			file = new File(fileName);
		}
		else
		{
			file = MixConfig.showFileDialog(MixConfig.SAVE_DIALOG,
											MixConfig.FILTER_XML).getSelectedFile();
		}

		if (file != null)
		{
			String fname = file.getName();
			if (!fname.toLowerCase().endsWith(".xml"))
			{
				file = new File(file.getParent(), fname + ".xml");
			}
			mixConf.save(new FileWriter(file.getCanonicalPath()));
			MixConfig.info("Configuration saved", "Configuration saved as "+file);
		}

		return file != null;
	}
}
