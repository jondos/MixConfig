/*
Copyright (c) 2000, The JAP-Team
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
package mixconfig;

import java.io.ByteArrayOutputStream;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.x509.X509CertificateStructure;

final public class GeneralPanel extends JPanel implements ItemListener,ActionListener
{
	private JComboBox m_comboboxMixType;
	private JTextField m_tfMixName,m_tfCascadeName,m_tfMixID,m_tfFileName,m_tfID,m_tfNumOfFiles,m_tfLogEncryptKeyName;
	private JCheckBox m_checkboxDaemon,m_checkboxLogging,m_checkboxUserID,m_checkboxNrOfFileDes,m_compressLog;
	private JRadioButton m_rbConsole,m_rbFile,m_rbSyslog;
	private JLabel m_labelEnrypt;
	private ButtonGroup bg;
	private JButton m_bttnImportEncKey;
	private byte[]m_certLogEncKey=null;

	public GeneralPanel()
	{
		GridBagLayout layout=new GridBagLayout();
		setLayout(layout);
		GridBagConstraints c=new GridBagConstraints();
		c.anchor=GridBagConstraints.NORTHWEST;
		c.insets=new Insets(10,10,10,10);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 1;

		JLabel j1 = new JLabel("Mix Type");
		c.gridx=0;
		c.gridy=0;
		c.gridwidth = 1;
		c.weightx = 0;
		layout.setConstraints(j1,c);
		add(j1);
		m_comboboxMixType = new JComboBox();
		m_comboboxMixType.addItem("First Mix");
		m_comboboxMixType.addItem("Middle Mix");
		m_comboboxMixType.addItem("Last Mix");
		c.weightx=1;
		c.gridx=1;
		c.gridwidth = 3;
		layout.setConstraints(m_comboboxMixType,c);
		add(m_comboboxMixType);
		m_comboboxMixType.addItemListener(new ItemListener()
				{
						public void itemStateChanged(ItemEvent e)
						{
								int sel = m_comboboxMixType.getSelectedIndex();
								m_tfCascadeName.setEnabled(sel==0);
								ConfigFrame.m_CertificatesPanel.updateButtons(sel>0, sel<2);
						}
				});

		JLabel j1a = new JLabel("Cascade Name");
		c.gridx=0;
		c.gridy++;
		c.gridwidth = 1;
		c.weightx = 0;
		layout.setConstraints(j1a,c);
		add(j1a);
		m_tfCascadeName = new JTextField(20);
		m_tfCascadeName.setText("");
		c.gridx=1;
		c.gridwidth = 3;
		c.weightx = 1;
		layout.setConstraints(m_tfCascadeName,c);
		add(m_tfCascadeName);

		JLabel j2 = new JLabel("Mix Name");
		c.gridx=0;
		c.gridy++;
		c.gridwidth = 1;
		c.weightx = 0;
		layout.setConstraints(j2,c);
		add(j2);
		m_tfMixName = new JTextField(20);
		m_tfMixName.setText("");
		c.gridx=1;
		c.gridwidth = 3;
		c.weightx = 1;
		layout.setConstraints(m_tfMixName,c);
		add(m_tfMixName);

		JLabel j3 = new JLabel("Mix ID");
		c.gridx=0;
		c.gridy++;
		c.gridwidth = 1;
		c.weightx = 0;
		layout.setConstraints(j3,c);
		add(j3);

		m_tfMixID = new JTextField(20);
		m_tfMixID.setText("");
		c.gridx=1;
		c.gridwidth = 2;
		c.weightx = 1;
		layout.setConstraints(m_tfMixID,c);
		add(m_tfMixID);

		JButton genButton = new JButton("Generate");
		c.gridx = 3;
		c.gridwidth = 1;
		c.weightx = 0;
		layout.setConstraints(genButton, c);
		add(genButton);
		final Component myComponent = this;
		genButton.addActionListener(
				new ActionListener()
				{
						final static String idChars =
										"abcdefghijklmnopqrstuvwxyz0123456789.-_";
						public void actionPerformed(ActionEvent ev)
						{
								String oMixid=m_tfMixID.getText();

								if(oMixid!=null && oMixid.length()!=0)
								{
										if(JOptionPane.showConfirmDialog(myComponent,
												"It is generally not a good idea to change a Mix ID.\n" +
												"You should proceed only if you know what you're doing." +
												((ConfigFrame.m_CertificatesPanel.getOwnPrivCert()==null)?"":
												"\nA new Mix ID may also invalidate your certificate."),
												"Change of Mix ID.",
												JOptionPane.OK_CANCEL_OPTION,
												JOptionPane.WARNING_MESSAGE)!=JOptionPane.OK_OPTION)
														return;
								}

								String str = "m";
								for(int i=0;i<10;i++)
								{
										int r = (int)(Math.random()*idChars.length());
										str+= idChars.substring(r,r+1);
								}
								m_tfMixID.setText(str);
						}
				});

		m_checkboxUserID = new JCheckBox("Set User ID on Execution");
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		c.weightx = 0;
		m_checkboxUserID.addItemListener(this);
		layout.setConstraints(m_checkboxUserID,c);
		add(m_checkboxUserID);
		m_tfID = new JTextField(20);
		c.gridx = 1;
		c.weightx = 1;
		c.gridwidth = 3;
		layout.setConstraints(m_tfID,c);
		add(m_tfID);
		m_tfID.setEnabled(false);

		m_checkboxNrOfFileDes = new JCheckBox("Set Number of File Descriptors");
		c.weightx = 0;
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		m_checkboxNrOfFileDes.addItemListener(this);
		layout.setConstraints(m_checkboxNrOfFileDes,c);
		add(m_checkboxNrOfFileDes);
		m_tfNumOfFiles = new JTextField(20);
		c.gridx = 1;
		c.gridwidth = 3;
		c.weightx = 1;
		layout.setConstraints(m_tfNumOfFiles,c);
		add(m_tfNumOfFiles);
		m_tfNumOfFiles.setEnabled(false);

		m_checkboxDaemon = new JCheckBox("Run as Daemon");
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		c.weightx = 0;
		m_checkboxDaemon.addItemListener(this);
		layout.setConstraints(m_checkboxDaemon,c);
		add(m_checkboxDaemon);

		m_checkboxLogging = new JCheckBox("Enable Logging");
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		c.weightx = 0;
		m_checkboxLogging.addItemListener(this);
		layout.setConstraints(m_checkboxLogging,c);
		add(m_checkboxLogging);

		m_rbConsole = new JRadioButton("Log to Console");
		c.gridx = 1;
		c.weightx = 1;
		c.gridwidth = 3;
		m_rbConsole.setActionCommand("LogtoConsole");
		m_rbConsole.addActionListener(this);
		layout.setConstraints(m_rbConsole,c);
		add(m_rbConsole);
		m_rbConsole.setEnabled(false);

		m_rbFile = new JRadioButton("Log to Directory");
		c.gridwidth = 3;
		c.weightx = 1;
		c.gridy++;
		m_rbFile.addActionListener(this);
		m_rbFile.setActionCommand("Logtodir");
		layout.setConstraints(m_rbFile,c);
		add(m_rbFile);
		m_rbFile.setEnabled(false);

		m_tfFileName = new JTextField(20);
		m_tfFileName.setText("");
		c.insets.left+=20;
		c.gridx = 1;
		c.gridy++;
		c.gridwidth = 3;
		c.weightx = 1;
		layout.setConstraints(m_tfFileName,c);
		add(m_tfFileName);
		m_tfFileName.setEnabled(false);

		m_labelEnrypt = new JLabel("Encrypt with:");
		c.gridx = 1;
		c.gridy++;
		c.gridwidth = 1;
		c.weightx = 0;
	layout.setConstraints(m_labelEnrypt,c);
		add(m_labelEnrypt);
		m_labelEnrypt.setEnabled(false);

		m_tfLogEncryptKeyName=new JTextField(15);
		c.insets.left-=20;
		c.gridx = 2;
		c.gridwidth = 1;
		c.weightx = 1;
	layout.setConstraints(m_tfLogEncryptKeyName,c);
		add(m_tfLogEncryptKeyName);
		m_tfLogEncryptKeyName.setEnabled(false);

		m_bttnImportEncKey = new JButton("Import...");
		c.gridx = 3;
		c.gridwidth = 1;
		c.weightx = 0;
		layout.setConstraints(m_bttnImportEncKey, c);
		add(m_bttnImportEncKey);
m_bttnImportEncKey.setEnabled(false);
		m_bttnImportEncKey.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent ev) {
					importEncKeyForLog();}
				});

		m_compressLog = new JCheckBox("Compress Log Files");
		c.insets.left+=20;
		c.gridx = 1;
		c.gridy++;
		c.gridwidth = 3;
		c.weightx = 1;
	layout.setConstraints(m_compressLog,c);
		add(m_compressLog);
		m_compressLog.setEnabled(false);

		m_rbSyslog = new JRadioButton("Log to Syslog");
		c.insets.left-=20;
		c.gridx = 1;
		c.gridy++;
		m_rbSyslog.addActionListener(this);
		m_rbSyslog.setActionCommand("LogtoSyslog");
		layout.setConstraints(m_rbSyslog,c);
		add(m_rbSyslog);
		m_rbSyslog.setEnabled(false);

		bg = new ButtonGroup();
		bg.add(m_rbConsole);
		bg.add(m_rbFile);
		bg.add(m_rbSyslog);
		m_rbConsole.setSelected(true);
	}

	public void clear()
		{
			 setMixType(null);
			 setMixName(null);
			 setCascadeName(null);
			 setUserID(null);
			 setLogging(false,false,false,null,false);
			 setFileDes(null);
			 setDaemon(null);
			 setMixID(null);
			 setEncKeyForLog(null);
		}

	public String getMixType()
		{
			switch(m_comboboxMixType.getSelectedIndex())
				{
					case 2:
						return "LastMix";
					case 1:
						return "MiddleMix";
					default:
						return "FirstMix";
				}
		}

	public String getCascadeName()
	{
			return m_tfCascadeName.getText();
	}

	public String getMixName()
		{
			return m_tfMixName.getText();
		}

	public String getMixID()
	{
		return m_tfMixID.getText();
	}

	public boolean isMixIDValid()
	{
			String mixID = getMixID();
			if(mixID.equals(""))
					return false;
			else
			{
					final String idChars = "abcdefghijklmnopqrstuvwxyz0123456789.-_";
					mixID = mixID.toLowerCase();
					if(mixID.charAt(0)!='m')
							return false;
					for(int i=0;i<mixID.length();i++)
							if(idChars.indexOf(mixID.charAt(i))<0)
									return false;
			}
			return true;
	}

	public String getUserID()
	{
		if(m_checkboxUserID.isSelected())
			return m_tfID.getText().trim();
		return null;
	}

	public String getFileDes()
	{
		if(m_checkboxNrOfFileDes.isSelected())
			return m_tfNumOfFiles.getText();
		return null;
	}

	public String getFileName()
	{
		return m_tfFileName.getText();
	}

	public String getEnabled()
	{
		if(m_rbSyslog.isSelected() == true)
			return "LogtoSyslog";
		if(m_rbFile.isSelected() == true)
			return "Logtodir";
		if(m_rbConsole.isSelected() == true)
			return "LogtoConsole";
		return "null";
	}

	public boolean isLoggingEnabled()
		{
			return m_checkboxLogging.isSelected();
		}

	public boolean isLoggingCompressed()
	{
			return m_compressLog.isSelected();
	}

	public void setLogging(boolean bLogConsole,boolean bLogSyslog,boolean bLogFile, String file, boolean bcompLog)
		{
			if(bLogConsole||bLogSyslog||bLogFile)
				{
					m_checkboxLogging.setSelected(true);
					m_rbConsole.setSelected(bLogConsole);
					m_rbFile.setSelected(bLogFile);
					if(bLogFile)
					{
						m_tfFileName.setText(file);
						m_compressLog.setSelected(bcompLog);
					}
					m_rbSyslog.setSelected(bLogSyslog);

				}
			else
				{
					m_checkboxLogging.setSelected(false);
				}
		}

	public void setMixType(String type)
	{
		if(type==null)
			m_comboboxMixType.setSelectedIndex(0);
		else if(type.equalsIgnoreCase("LastMix"))
			m_comboboxMixType.setSelectedIndex(2);
		else if(type.equalsIgnoreCase("MiddleMix"))
			m_comboboxMixType.setSelectedIndex(1);
		else
			m_comboboxMixType.setSelectedIndex(0);
	}

	public void setCascadeName(String name)
	{
		m_tfCascadeName.setText(name);
	}

	public void setMixName(String name)
	{
		m_tfMixName.setText(name);
	}

	public void setDaemon(String s)
		{
			if(s==null)
				s="";
			m_checkboxDaemon.setSelected(s.equalsIgnoreCase("true"));
		}

	public String getDaemon()
		{
			return m_checkboxDaemon.isSelected()?"True":"False";
		}

	public void setMixID(String mixid)
	{
		m_tfMixID.setText(mixid);
	}

	public void setUserID(String userid)
	{
		m_tfID.setText(userid);
		m_checkboxUserID.setSelected(userid!=null);
	}

	public void setFileDes(String filedes)
	{
		m_tfNumOfFiles.setText(filedes);
		m_checkboxNrOfFileDes.setSelected(filedes!=null);
	}


	public void actionPerformed(ActionEvent ae)
	{
		m_tfFileName.setEnabled(m_rbFile.isSelected());
		m_compressLog.setEnabled(m_rbFile.isSelected());
		m_labelEnrypt.setEnabled(m_rbFile.isSelected());
		m_bttnImportEncKey.setEnabled(m_rbFile.isSelected());
			}

	public void itemStateChanged(ItemEvent ie)
	{
		if(m_checkboxLogging.isSelected())
			{
				if(m_checkboxDaemon.isSelected())
					{
						if( m_rbConsole.isSelected() )//switch automaticaly to File if console and dameon is selected
							m_rbFile.setSelected(true);
						m_rbConsole.setEnabled(false);
					}
				else
					m_rbConsole.setEnabled(true);
				m_rbFile.setEnabled(true);
				m_tfFileName.setEnabled(m_rbFile.isSelected());
				m_compressLog.setEnabled(m_rbFile.isSelected());
				m_rbSyslog.setEnabled(true);
			}
		else
			{
				m_rbConsole.setEnabled(false);
				m_rbFile.setEnabled(false);
				m_rbSyslog.setEnabled(false);
				m_tfFileName.setEnabled(false);
				m_compressLog.setEnabled(false);
				m_bttnImportEncKey.setEnabled(false);
				m_labelEnrypt.setEnabled(false);
		}


		m_tfID.setEnabled(m_checkboxUserID.isSelected());
		m_tfNumOfFiles.setEnabled(m_checkboxNrOfFileDes.isSelected());
	}

private void importEncKeyForLog()
{
	byte [] cert;
		try
		{
				cert = MixConfig.openFile(MixConfig.FILTER_CER|MixConfig.FILTER_B64_CER);
		}
		catch(Exception e)
		{

				ClipFrame Open =
						new ClipFrame(
								"Paste a certificate to be imported in the area provided.",
								true);
				Open.show();
				cert = Open.getText().getBytes();
		}
	setEncKeyForLog(cert);
}
public void setEncKeyForLog(byte[] cert)
	{
			try
			{
					if (cert != null)
					{
							X509CertificateStructure cert1 = MixConfig.readCertificate(cert);
							m_tfLogEncryptKeyName.setText(cert1.getSubject().toString());
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							new DEROutputStream(out).writeObject(cert1);
							m_certLogEncKey = out.toByteArray();
					}
					else
					{
						m_tfLogEncryptKeyName.setText(null);
						m_certLogEncKey = null;
}
			}
			catch (Exception e)
			{
					System.out.println("Prev Cert not set: " + e.getMessage());
					setEncKeyForLog(null);
			}
	}
		public byte[] getEncKeyForLog()
		{
			return m_certLogEncKey;
		}
}








