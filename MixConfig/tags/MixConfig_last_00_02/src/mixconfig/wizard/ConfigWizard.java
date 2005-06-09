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
package mixconfig.wizard;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.xml.sax.SAXException;
import anon.util.ResourceLoader;
import mixconfig.MixConfig;
import mixconfig.MixConfiguration;
import gui.ImageIconLoader;


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
	private static final String PATH_LOG = new String("mixconfig/anonLogo.jpg");

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
         * @throws ParserConfigurationException If an XML error occurs while the wizard is active
         * @throws SAXException If an XML error occurs while the wizard is active
	 * @throws IOException If an I/O error occurs while saving the configuration
	 */
	public ConfigWizard() throws IOException, ParserConfigurationException, SAXException
	{
		// DEBUG
//		MixConfig.setMixConfiguration(new MixConfiguration(new java.io.FileReader(
//			"/home/ronin/mixtest-payment-wiz.xml")));

		setLayout(new BorderLayout());

		JLabel imageCanvas = new JLabel(ImageIconLoader.loadImageIcon(PATH_LOG, true));
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

		Dimension d = new Dimension(900, 700);
		setSize(d);
		setPreferredSize(d);

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

		if (fileName != null)
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
			MixConfig.info("Configuration saved", "Configuration saved as " + file);
		}

		return file != null;
	}
}
