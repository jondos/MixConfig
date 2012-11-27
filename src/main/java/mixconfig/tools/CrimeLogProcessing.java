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
package mixconfig.tools;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.LineNumberReader;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import anon.util.Base64;

import mixconfig.MixConfig;

import gui.dialog.JAPDialog;

public class CrimeLogProcessing extends JAPDialog
	{
		private JTextField m_tfLogFile;
		private JTextField m_tfOutputDir;
		private Pattern regexpLogLine;
		private JRadioButton m_radioFirstMix;
		private JRadioButton m_radioMiddleMix;
		private JRadioButton m_radioLastMix;
		private DateFormat m_dateFormat=new SimpleDateFormat("yyyyMMdd-hhmmss");
				
		public CrimeLogProcessing(Frame parent)
			{
				super(parent, "Proccessing Tool for Law Enforcement Data", true);
				// CAMsg::printMsg(LOG_CRIT,"Crime detection: User surveillance, previous mix channel (opened at: %u): %u - Downstream Payload (Base64 encoded): %s\n");
				// CAMsg::printMsg(LOG_CRIT,"Crime detection: User surveillance, previous mix channel (opened at: %u): %u - Upstream Payload (Base64 encoded): %s\n");
				regexpLogLine = Pattern
						.compile(
								"Crime detection: User surveillance, previous mix channel \\(opened at: (\\d+)\\): (\\d+) - ([UpDown]+)stream Payload \\(Base64 encoded\\): (.+)$",
								Pattern.CASE_INSENSITIVE);
				// regexpLogLine=Pattern.compile("Crime detection: User surveillance, previous mix channel \\(opened at: (\\d+)\\): (\\d+) - ",Pattern.CASE_INSENSITIVE);
				initComponents();
				pack();
				setVisible(true, false);
			}

		private void initComponents()
			{
				GridBagConstraints constraintsPanel = new GridBagConstraints();

				getContentPane().setLayout(new GridBagLayout());

				JLabel label = new JLabel("Logdatei:");
				constraintsPanel.gridx = 0;
				constraintsPanel.gridy = 0;
				constraintsPanel.anchor = GridBagConstraints.WEST;
				constraintsPanel.weightx = 0;
				constraintsPanel.insets = new Insets(10, 10, 10, 10);
				getContentPane().add(label, constraintsPanel);

				m_tfLogFile = new JTextField(30);
				constraintsPanel.gridx = 1;
				constraintsPanel.gridy = 0;
				constraintsPanel.weightx = 1.0;
				constraintsPanel.insets = new Insets(10, 10, 10, 10);
				constraintsPanel.fill = GridBagConstraints.HORIZONTAL;
				getContentPane().add(m_tfLogFile, constraintsPanel);

				JButton bttn = new JButton("Select...");
				bttn.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent arg0)
							{
								doSelectLogFile();
							}

					});
				constraintsPanel.gridx = 2;
				constraintsPanel.gridy = 0;
				constraintsPanel.weightx = 0;
				constraintsPanel.insets = new Insets(10, 10, 10, 10);
				constraintsPanel.fill = GridBagConstraints.HORIZONTAL;
				getContentPane().add(bttn, constraintsPanel);

				label = new JLabel("Mix-Typ:");
				constraintsPanel.gridx = 0;
				constraintsPanel.gridy = 2;
				constraintsPanel.anchor = GridBagConstraints.WEST;
				constraintsPanel.weightx = 0;
				constraintsPanel.insets = new Insets(10, 10, 10, 10);
				getContentPane().add(label, constraintsPanel);

				JPanel panelButtons = new JPanel(new GridLayout(1, 3, 20, 0));
				constraintsPanel.gridx = 1;
				constraintsPanel.gridy = 2;
				constraintsPanel.anchor = GridBagConstraints.WEST;
				constraintsPanel.weightx = 0;
				constraintsPanel.insets = new Insets(10, 10, 10, 10);
				getContentPane().add(panelButtons, constraintsPanel);

				ButtonGroup radioGroup = new ButtonGroup();
				m_radioFirstMix = new JRadioButton("erster Mix");
				m_radioFirstMix.setEnabled(false);
				radioGroup.add(m_radioFirstMix);
				panelButtons.add(m_radioFirstMix);

				m_radioMiddleMix = new JRadioButton("mittler Mix");
				radioGroup.add(m_radioMiddleMix);
				m_radioMiddleMix.setEnabled(false);
				panelButtons.add(m_radioMiddleMix);

				m_radioLastMix = new JRadioButton("letzter Mix");
				radioGroup.add(m_radioLastMix);
				panelButtons.add(m_radioLastMix);
				m_radioLastMix.setSelected(true);

				label = new JLabel("Ausgabeverzeichnis:");
				constraintsPanel.gridx = 0;
				constraintsPanel.gridy = 3;
				constraintsPanel.anchor = GridBagConstraints.WEST;
				constraintsPanel.weightx = 0;
				constraintsPanel.insets = new Insets(10, 10, 10, 10);
				getContentPane().add(label, constraintsPanel);

				m_tfOutputDir = new JTextField(30);
				constraintsPanel.gridx = 1;
				constraintsPanel.gridy = 3;
				constraintsPanel.weightx = 1.0;
				constraintsPanel.insets = new Insets(10, 10, 10, 10);
				constraintsPanel.fill = GridBagConstraints.HORIZONTAL;
				getContentPane().add(m_tfOutputDir, constraintsPanel);

				bttn = new JButton("Select...");
				bttn.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent arg0)
							{
								doSelectOutputDir();
							}

					});
				constraintsPanel.gridx = 2;
				constraintsPanel.gridy = 3;
				constraintsPanel.weightx = 0;
				constraintsPanel.insets = new Insets(10, 10, 10, 10);
				constraintsPanel.fill = GridBagConstraints.HORIZONTAL;
				getContentPane().add(bttn, constraintsPanel);

				panelButtons = new JPanel(new GridLayout(1, 2, 20, 0));
				constraintsPanel.gridy = 4;
				constraintsPanel.gridx = 0;
				constraintsPanel.weighty = 0;
				constraintsPanel.gridwidth = GridBagConstraints.REMAINDER;
				constraintsPanel.fill = GridBagConstraints.NONE;
				constraintsPanel.anchor = GridBagConstraints.SOUTHEAST;
				getContentPane().add(panelButtons, constraintsPanel);

				final JAPDialog parentDlg = this;
				bttn = new JButton("Ausf\u00FChren");
				panelButtons.add(bttn);
				bttn.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent arg0)
							{
								if (!doIt())
									{
										JAPDialog
												.showErrorDialog(
														parentDlg,
														"Ein Fehler ist aufgetreten.\nBitte \u00FCberpr\u00FCfen Sie, ob die Log-Datei existiert und lesbar ist. Pr\u00FCfen Sie auch, ob das Ausgabeverzeichnis existiert und schreibbar ist.");
									}
								else
									dispose();
							}

					});
				bttn = new JButton("Beenden");
				panelButtons.add(bttn);
				bttn.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent arg0)
							{
								dispose();
							}

					});
			}

		private boolean doIt()
			{
				LineNumberReader reader = null;
				boolean ret = false;
				try
					{
						File fileLog = new File(m_tfLogFile.getText());
						reader = new LineNumberReader(new FileReader(fileLog));
						File outputDir = new File(m_tfOutputDir.getText());
						String currentLine = null;
						while ((currentLine = reader.readLine()) != null)
							{
								if (!processLine(currentLine, outputDir))
									{
										ret = false;
										break;
									}
							}
						ret=true;
					}
				catch (Throwable t)
					{
						ret=false;
					}
				if (reader != null)
					try
						{
							reader.close();
						}
					catch (Throwable e)
						{
						}
				return ret;
			}

		// CAMsg::printMsg(LOG_CRIT,"Crime detection: User surveillance, previous mix channel (opened at: %u): %u - Downstream Payload (Base64 encoded): %s\n");
		// CAMsg::printMsg(LOG_CRIT,"Crime detection: User surveillance, previous mix channel (opened at: %u): %u - Upstream Payload (Base64 encoded): %s\n");
		private boolean processLine(String currentLine, File outputDir)
			{
				if (!currentLine.contains("Crime detection: User surveillance, previous mix channel"))
					return true;
				boolean bUpstream = false;
				String strChannel = null;
				String strDate = null;
				String strPayloadBase64 = null;
				Matcher matcher = regexpLogLine.matcher(currentLine);
				if (matcher.find()/* &&matcher.groupCount()==4 */)
					{
						strDate = matcher.group(1);
						strChannel = matcher.group(2);
						bUpstream = ("Up".equals(matcher.group(3)));
						strPayloadBase64 = matcher.group(4);
					}
				else
					return true;
				Date date=new Date(Long.parseLong(strDate)*1000);
				String strFilenameForChannel = m_dateFormat.format(date)+ " -- " + strChannel;
				if (bUpstream)
					{
						strFilenameForChannel += ".sent";
					}
				else
					{
						strFilenameForChannel += ".received";
					}
				try
					{
						FileOutputStream fileOut = new FileOutputStream(outputDir + "/" + strFilenameForChannel, true);
						byte[] payload = Base64.decode(strPayloadBase64);
						fileOut.write(payload);
						fileOut.close();
					}
				catch (Throwable t)
					{
						return false;
					}
				return true;
			}

		private void doSelectLogFile()
			{
				JFileChooser fileChosser = MixConfig.showFileDialog(getOwner(), MixConfig.OPEN_DIALOG, MixConfig.FILTER_ALL);
				if (fileChosser != null)
					{
						File f = fileChosser.getSelectedFile();
						if (f != null)
							m_tfLogFile.setText(f.getAbsolutePath());
					}

			}

		private void doSelectOutputDir()
			{
				JFileChooser fileChosser = MixConfig.showFileDialog(getOwner(), MixConfig.CHOOSE_DIR_DIALOG,
						MixConfig.FILTER_ALL);
				if (fileChosser != null)
					{
						File f = fileChosser.getSelectedFile();
						if (f != null)
							m_tfOutputDir.setText(f.getAbsolutePath());
					}

			}
	}
