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
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import anon.util.Base64;

import mixconfig.MixConfig;

import gui.dialog.JAPDialog;

public class CrimeLogProcessing extends JAPDialog implements Runnable
	{
		private JTextField m_tfLogFile;
		private JTextField m_tfOutputDir;
		private Pattern regexpLogLine;
		private JRadioButton m_radioFirstMix;
		private JRadioButton m_radioMiddleMix;
		private JRadioButton m_radioLastMix;
		private JProgressBar m_Progress;
		private DateFormat m_dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss",Locale.GERMANY);
		private DateFormat m_dateFormatLog = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss",Locale.GERMANY);
		private Hashtable<String, String> m_htDateChannels;
		private CrimeLogProcessing m_crimelogprocessingDlg;
		private int m_processedBytes;
		private JButton m_bttnDoIt;
		private Thread m_threadDoIt;
		private volatile boolean m_bRun;
		
		public CrimeLogProcessing(Frame parent)
			{
				super(parent, "Proccessing Tool for Law Enforcement Data", true);
				m_crimelogprocessingDlg=this;
				// CAMsg::printMsg(LOG_CRIT,"Crime detection: User surveillance, previous mix channel (opened at: %u): %u - Downstream Payload (Base64 encoded): %s\n");
				// CAMsg::printMsg(LOG_CRIT,"Crime detection: User surveillance, previous mix channel (opened at: %u): %u - Upstream Payload (Base64 encoded): %s\n");
				/*
				 * regexpLogLine = Pattern .compile(
				 * "Crime detection: User surveillance, previous mix channel \\(opened at: (\\d+)\\): (\\d+) - ([UpDown]+)stream Payload \\(Base64 encoded\\): (.+)$"
				 * , Pattern.CASE_INSENSITIVE);
				 */// CAMsg::printMsg(LOG_CRIT,"[2013/01/10-13:06:23, critical]  Crime detection: User surveillance, previous mix channel: %u - Downstream Payload (Base64 encoded): %s\n");
				// CAMsg::printMsg(LOG_CRIT,"[2013/01/10-13:06:23, critical] Crime detection: User surveillance, previous mix channel: %u): %u - Upstream Payload (Base64 encoded): %s\n");
				regexpLogLine = Pattern
						.compile(
								"\\[(.+), .*\\] Crime detection: User surveillance, previous mix channel: (\\d+) - ([UpDown]+)stream Payload \\(Base64 encoded\\): (.+)$",
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

				label = new JLabel("Verarbeitung:");
				constraintsPanel.gridx = 0;
				constraintsPanel.gridy = 4;
				constraintsPanel.anchor = GridBagConstraints.WEST;
				constraintsPanel.weightx = 0;
				constraintsPanel.insets = new Insets(10, 10, 10, 10);
				getContentPane().add(label, constraintsPanel);

				m_Progress = new JProgressBar();
				constraintsPanel.gridx = 1;
				constraintsPanel.gridwidth = 2;
				constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
				constraintsPanel.gridy = 4;
				constraintsPanel.anchor = GridBagConstraints.WEST;
				constraintsPanel.weightx = 1;
				constraintsPanel.insets = new Insets(10, 10, 10, 10);
				getContentPane().add(m_Progress, constraintsPanel);
				

				panelButtons = new JPanel(new GridLayout(1, 2, 20, 0));
				constraintsPanel.gridy = 5;
				constraintsPanel.gridx = 0;
				constraintsPanel.weighty = 0;
				constraintsPanel.gridwidth = GridBagConstraints.REMAINDER;
				constraintsPanel.fill = GridBagConstraints.NONE;
				constraintsPanel.anchor = GridBagConstraints.SOUTHEAST;
				getContentPane().add(panelButtons, constraintsPanel);

				m_bttnDoIt = new JButton("Ausf\u00FChren");
				panelButtons.add(m_bttnDoIt);
				m_bttnDoIt.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent arg0)
							{
								m_bttnDoIt.setEnabled(false);
								doIt();
							}

					});
				bttn = new JButton("Beenden");
				panelButtons.add(bttn);
				bttn.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent arg0)
							{
								m_bRun=false;
								if(m_threadDoIt!=null)
									try
										{
											m_threadDoIt.join();
										}
									catch (InterruptedException e)
										{
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
								dispose();
							}

					});
			}

		private void doIt()
			{
				m_bRun=true;
				m_threadDoIt=new Thread(this,"Thread process law enforcement data");
				m_threadDoIt.setDaemon(true);
				m_threadDoIt.start();
				
			}
		
		public void run()
			{
				m_htDateChannels = new Hashtable<String, String>();
				LineNumberReader reader = null;
				boolean ret = false;
				try
					{
						File fileLog = new File(m_tfLogFile.getText());
						final long filesize=fileLog.length();
						SwingUtilities.invokeAndWait(new Runnable(){public void run(){m_Progress.setMaximum((int) filesize);}});
						reader = new LineNumberReader(new FileReader(fileLog));
						File outputDir = new File(m_tfOutputDir.getText());
						String currentLine = null;
						m_processedBytes=0;
						while ((currentLine = reader.readLine()) != null&&m_bRun)
							{
								if (!processLine(currentLine, outputDir))
									{
										ret = false;
										break;
									}
								m_processedBytes+=currentLine.length();
								SwingUtilities.invokeLater(new Runnable(){public void run(){m_Progress.setValue(m_processedBytes);}});
															
							}
						ret = true;
					}
				catch (Throwable t)
					{
						ret = false;
					}
				if (reader != null)
					try
						{
							reader.close();
						}
					catch (Throwable e)
						{
						}
				
				if (!ret)
					{
						JAPDialog
								.showErrorDialog(
										m_crimelogprocessingDlg,
										"Ein Fehler ist aufgetreten.\nBitte \u00FCberpr\u00FCfen Sie, ob die Log-Datei existiert und lesbar ist. Pr\u00FCfen Sie auch, ob das Ausgabeverzeichnis existiert und schreibbar ist.");
					}
				else if(m_bRun) //Execution was not canceled
					{
						JAPDialog
						.showMessageDialog(
								m_crimelogprocessingDlg,
								"Verarbeitung erfolgreich beendet!");
					}
			}

		// CAMsg::printMsg(LOG_CRIT,"Crime detection: User surveillance, previous mix channel (opened at: %u): %u - Downstream Payload (Base64 encoded): %s\n");
		// CAMsg::printMsg(LOG_CRIT,"Crime detection: User surveillance, previous mix channel (opened at: %u): %u - Upstream Payload (Base64 encoded): %s\n");
		/*
		 * private boolean processLine(String currentLine, File outputDir) { if
		 * (!currentLine
		 * .contains("Crime detection: User surveillance, previous mix channel"))
		 * return true; boolean bUpstream = false; String strChannel = null; String
		 * strDate = null; String strPayloadBase64 = null; Matcher matcher =
		 * regexpLogLine.matcher(currentLine); if (matcher.find()) { strDate =
		 * matcher.group(1); strChannel = matcher.group(2); bUpstream =
		 * ("Up".equals(matcher.group(3))); strPayloadBase64 = matcher.group(4); }
		 * else return true; Date date=new Date(Long.parseLong(strDate)*1000);
		 * String strFilenameForChannel = m_dateFormat.format(date)+ " -- " +
		 * strChannel; if (bUpstream) { strFilenameForChannel += ".sent"; } else {
		 * strFilenameForChannel += ".received"; } try { FileOutputStream fileOut =
		 * new FileOutputStream(outputDir + "/" + strFilenameForChannel, true);
		 * byte[] payload = Base64.decode(strPayloadBase64); fileOut.write(payload);
		 * fileOut.close(); } catch (Throwable t) { return false; } return true; }
		 */

		// CAMsg::printMsg(LOG_CRIT,"Crime detection: User surveillance, previous mix channel:  %u - Downstream Payload (Base64 encoded): %s\n");
		// CAMsg::printMsg(LOG_CRIT,"Crime detection: User surveillance, previous mix channel: %u - Upstream Payload (Base64 encoded): %s\n");
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
				// Date date=new Date(Long.parseLong(strDate)*1000);
				String strTmp = m_htDateChannels.get(strChannel);
				if (strTmp == null)
					{
						try
							{
								strTmp = m_dateFormat.format(m_dateFormatLog.parse(strDate));
							}
						catch (Throwable t)
							{
								return false;
							}
						m_htDateChannels.put(strChannel, strTmp);
					}
				else
					{// check if channel is still the same, i.e. opening time <1h from
						// current time...
						try
							{
								Date dateCurrent = m_dateFormatLog.parse(strDate);
								Date dateOpen = m_dateFormat.parse(strTmp);
								if((dateCurrent.getTime()-dateOpen.getTime())>3600000)
										System.out.println("Problem with channel opening times for channel: "+strChannel+"Open time: "+strTmp+" Current Record Time: "+strDate);
							}
						catch (Throwable t)
							{
								System.out.println("Something is wrong");
							}
					}
				String strFilenameForChannel = /* m_dateFormat.format(date) */strTmp + " -- " + strChannel;
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
