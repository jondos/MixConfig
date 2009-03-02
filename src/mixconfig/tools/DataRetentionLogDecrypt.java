package mixconfig.tools;

import gui.JAPHelpContext;
import gui.JAPJIntField;
import gui.dialog.JAPDialog;
import gui.dialog.PasswordContentPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.EventObject;

import javax.swing.ButtonGroup;
import javax.swing.CellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import anon.crypto.MyRSAPrivateKey;
import anon.crypto.PKCS12;

import mixconfig.MixConfig;
import mixconfig.panels.CertPanel;
import mixconfig.tools.dataretention.DataRetentionLogFile;
import mixconfig.tools.dataretention.DataRetentionLogFileEntry;
import mixconfig.tools.dataretention.DataRetentionLogFileHeader;
import mixconfig.tools.dataretention.DataRetentionSmartCard;
import mixconfig.tools.dataretention.JTreeTable;
import mixconfig.tools.dataretention.LogFilesModel;

public class DataRetentionLogDecrypt extends JAPDialog

{
	JAPJIntField m_tfDay,m_tfYear,m_tfMonth,m_tfHour,m_tfSecond,m_tfMinute;
	private String m_privateKeyFile;
	private JLabel m_labelPrivateKeyStorage;
	private boolean m_bUseSmartCardforPrivateKey;
	private JTextField m_tfLogDir;
	
public DataRetentionLogDecrypt(Frame parent)
{
	super(parent, "Proccessing Tool for retained Data", true);
	initComponents();
	pack();
	setVisible(true, false);
}

private void initComponents() {
	GridBagConstraints constraintsContentPane=new GridBagConstraints();
	GridBagConstraints constraintsPanel=new GridBagConstraints();

	getContentPane().setLayout(new GridBagLayout());
	
	JPanel panelLogfile = new JPanel(new GridBagLayout());
	panelLogfile.setBorder(new TitledBorder("Allgemeine Angaben"));
	constraintsContentPane.gridx=0;
	constraintsContentPane.gridy=0;
	constraintsContentPane.anchor=GridBagConstraints.WEST;
	constraintsContentPane.weightx=1.0;
	constraintsContentPane.fill=GridBagConstraints.HORIZONTAL;
	constraintsContentPane.insets=new Insets(10,10,10,10);
	getContentPane().add(panelLogfile, constraintsContentPane);
	
	JLabel label=new JLabel("Verzeichnis der Logdateien:");
	constraintsPanel.gridx=0;
	constraintsPanel.gridy=0;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	panelLogfile.add(label,constraintsPanel);
	
	label=new JLabel("Schluesselspeicher:");
	constraintsPanel.gridx=0;
	constraintsPanel.gridy=1;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	panelLogfile.add(label,constraintsPanel);

	m_tfLogDir=new JTextField(30);
	constraintsPanel.gridx=1;
	constraintsPanel.gridy=0;
	constraintsPanel.weightx=1.0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelLogfile.add(m_tfLogDir,constraintsPanel);
	
	m_labelPrivateKeyStorage=new JLabel();
	Font f=m_labelPrivateKeyStorage.getFont();
	m_labelPrivateKeyStorage.setFont(f.deriveFont(Font.BOLD));
	m_labelPrivateKeyStorage.setForeground(Color.blue);
	setPrivateKeyFile(false,m_privateKeyFile);
	constraintsPanel.gridx=1;
	constraintsPanel.gridy=1;
	constraintsPanel.weightx=1.0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	panelLogfile.add(m_labelPrivateKeyStorage,constraintsPanel);
	
	JButton bttn=new JButton("Select...");
	bttn.addActionListener(new ActionListener()
	{
		public void actionPerformed(ActionEvent arg0) {
			doSelectLogDir();
		}

	
	});
	constraintsPanel.gridx=2;
	constraintsPanel.gridy=0;
	constraintsPanel.weightx=0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelLogfile.add(bttn,constraintsPanel);

	bttn=new JButton("Select...");
	bttn.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent arg0) 
		{
			doSelectSecretKey();// TODO Auto-generated method stub
			
		}
		
	});
	constraintsPanel.gridx=2;
	constraintsPanel.gridy=1;
	constraintsPanel.weightx=0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelLogfile.add(bttn,constraintsPanel);

	
	
	JPanel panelRequest = new JPanel(new GridBagLayout());
	panelRequest.setBorder(new TitledBorder("Anfrage"));
	constraintsContentPane.gridy=1;
	getContentPane().add(panelRequest, constraintsContentPane);
	
	label=new JLabel("Datum:");
	constraintsPanel.gridx=0;
	constraintsPanel.gridy=0;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	panelRequest.add(label,constraintsPanel);
	
	label=new JLabel("Uhrzeit:");
	constraintsPanel.gridx=0;
	constraintsPanel.gridy=1;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	panelRequest.add(label,constraintsPanel);

	m_tfDay=new JAPJIntField(31);
	constraintsPanel.gridx=1;
	constraintsPanel.gridy=0;
	constraintsPanel.weightx=0;
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelRequest.add(m_tfDay,constraintsPanel);

	m_tfMonth=new JAPJIntField(12);
	constraintsPanel.gridx=3;
	constraintsPanel.gridy=0;
	constraintsPanel.weightx=0;
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelRequest.add(m_tfMonth,constraintsPanel);

	m_tfYear=new JAPJIntField(2050);
	constraintsPanel.gridx=5;
	constraintsPanel.gridy=0;
	constraintsPanel.weightx=0;
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelRequest.add(m_tfYear,constraintsPanel);

	m_tfHour=new JAPJIntField(23);
	constraintsPanel.gridx=1;
	constraintsPanel.gridy=1;
	constraintsPanel.weightx=0;
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelRequest.add(m_tfHour,constraintsPanel);
	
	m_tfMinute=new JAPJIntField(59);
	constraintsPanel.gridx=3;
	constraintsPanel.gridy=1;
	constraintsPanel.weightx=0;
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelRequest.add(m_tfMinute,constraintsPanel);
	
	m_tfSecond=new JAPJIntField(59);
	constraintsPanel.gridx=5;
	constraintsPanel.gridy=1;
	constraintsPanel.weightx=0;
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelRequest.add(m_tfSecond,constraintsPanel);
	
	label=new JLabel(".");
	constraintsPanel.gridx=2;
	constraintsPanel.gridy=0;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	constraintsPanel.insets=new Insets(0,0,0,0);
	panelRequest.add(label,constraintsPanel);

	label=new JLabel(".");
	constraintsPanel.gridx=4;
	constraintsPanel.gridy=0;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	panelRequest.add(label,constraintsPanel);

	label=new JLabel(":");
	constraintsPanel.gridx=2;
	constraintsPanel.gridy=1;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	panelRequest.add(label,constraintsPanel);

	label=new JLabel(":");
	constraintsPanel.gridx=4;
	constraintsPanel.gridy=1;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	panelRequest.add(label,constraintsPanel);
	
	JComboBox cb=new JComboBox();
	cb.setEditable(false);
	cb.addItem("UTC");
	constraintsPanel.gridx=6;
	constraintsPanel.gridy=1;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	panelRequest.add(cb,constraintsPanel);
	
	label=new JLabel();
	constraintsPanel.gridx=7;
	constraintsPanel.gridy=1;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=1.0;
	panelRequest.add(label,constraintsPanel);

	bttn=new JButton("Verify Log Files...");
	bttn.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent arg0) 
		{
			doVerifyLogFiles();// TODO Auto-generated method stub
			
		}
		
	});
	constraintsPanel.gridx=8;
	constraintsPanel.gridy=2;
	constraintsPanel.weightx=0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	constraintsPanel.ipadx=10;
	constraintsPanel.ipady=10;
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelRequest.add(bttn,constraintsPanel);

	bttn=new JButton("Search Records...");
	bttn.setFont(bttn.getFont().deriveFont(Font.BOLD));
	bttn.addActionListener(new ActionListener()
	{
		public void actionPerformed(ActionEvent arg0) 
		{
			// TODO Auto-generated method stub
			try {
				doLogDecrpyt();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	
	});
	constraintsPanel.gridx=9;
	constraintsPanel.gridy=2;
	constraintsPanel.weightx=0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	constraintsPanel.ipadx=10;
	constraintsPanel.ipady=10;
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelRequest.add(bttn,constraintsPanel);
	
	
	JPanel panelResult = new JPanel(new GridBagLayout());
	panelResult.setBorder(new TitledBorder("Ergebnis"));
	constraintsContentPane.gridy=2;
	constraintsContentPane.weighty=1.0;
	constraintsContentPane.fill=GridBagConstraints.BOTH;
	getContentPane().add(panelResult, constraintsContentPane);
	
	JTreeTable treetable=new JTreeTable(new LogFilesModel());
	constraintsPanel.gridx=0;
	constraintsPanel.gridy=0;
	constraintsPanel.weightx=1.0;
	constraintsPanel.weighty=1.0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	constraintsPanel.fill=GridBagConstraints.BOTH;
	panelResult.add(new JScrollPane(treetable),constraintsPanel);
	
	
	
	JPanel panelButtons = new JPanel(new GridLayout(1,2,20,0));
	constraintsContentPane.gridy=3;
	constraintsContentPane.weighty=0;
	constraintsContentPane.fill=GridBagConstraints.NONE;
	constraintsContentPane.anchor=GridBagConstraints.SOUTHEAST;
	getContentPane().add(panelButtons, constraintsContentPane);

	bttn=new JButton("Ergebnis speichern ...");
	panelButtons.add(bttn);

	bttn=new JButton("Beenden");
	panelButtons.add(bttn);

}


private Object doSetSecretPrivateKey()
{
		final MyRSAPrivateKey privKey;
		final DataRetentionSmartCard smartcard;
		if(!m_bUseSmartCardforPrivateKey)
		{
			smartcard=null;
			PKCS12 pkcs12=null;
			File filePrivKey=null;
			try{
				filePrivKey=new File(m_privateKeyFile);
				if(!filePrivKey.isFile()||!filePrivKey.canRead())
				{
					doErrorPrivKey(this);
				return null;
				}	
			}catch(Exception e)
			{
				doErrorPrivKey(this);
				return null;
			}	
			try {
				for(;;){
					JAPDialog dlg=new JAPDialog(this,"Password for private key");
					PasswordContentPane passwd=new PasswordContentPane(dlg,PasswordContentPane.PASSWORD_ENTER,"Password");
					passwd.updateDialog();
					dlg.pack();
				pkcs12 = PKCS12.getInstance(new FileInputStream(filePrivKey),passwd);
				if(pkcs12!=null||passwd.getButtonValue()==PasswordContentPane.RETURN_VALUE_CANCEL)
					break;
				dlg.dispose();
				} 
				if(pkcs12==null)
					return null;
				privKey=(MyRSAPrivateKey)pkcs12.getPrivateKey();
				} catch (Exception e1) {
				// TODO Auto-generated catch block
				doErrorPrivKey(this);
				return null;
			}
			return privKey;	
		}		
		else
		{
			privKey=null;
			smartcard=new DataRetentionSmartCard();
			boolean bFoundCard=false;
			try {
				bFoundCard=smartcard.connectToSmartCard();
				smartcard.retrievePublicKey();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(!bFoundCard)
			{
				doErrorSmartCard(this);
				return null;
			}
			boolean bUserAuth=false;
			try {
				for(;;){
					JAPDialog dlg=new JAPDialog(this,"Password for SamrtCard");
					PasswordContentPane passwd=new PasswordContentPane(dlg,PasswordContentPane.PASSWORD_ENTER,"Password");
					passwd.updateDialog();
					dlg.pack();
					dlg.setVisible(true);
					dlg.dispose();
					if(passwd.getButtonValue()==PasswordContentPane.RETURN_VALUE_CANCEL)
						return null;
					byte[] pin=new String(passwd.getPassword()).getBytes();
					try{
					bUserAuth=smartcard.authenticateUser(pin);
					}
					catch(Exception e)
					{
						bUserAuth=false;
					}
					if(bUserAuth)
						break;
				} 
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			}
			return smartcard;
		}//if bSmartCard

}

private void doVerifyLogFiles() {
	final MyRSAPrivateKey privKey;
	final DataRetentionSmartCard smartcard;
	
		Object oPrivKeyOrSmartCard=doSetSecretPrivateKey();
		if(oPrivKeyOrSmartCard==null)
			return;
		if(oPrivKeyOrSmartCard instanceof DataRetentionSmartCard)
		{
			privKey=null;
			smartcard=(DataRetentionSmartCard)oPrivKeyOrSmartCard;
		}
		else
		{
			smartcard=null;
			privKey=(MyRSAPrivateKey)oPrivKeyOrSmartCard;
		}
		final JAPDialog dlgVerify=new JAPDialog(this,"Verify Log Files");
		Container contentPane=dlgVerify.getContentPane();

		GridBagConstraints constraintsContentPane=new GridBagConstraints();

		contentPane.setLayout(new GridBagLayout());
	
		JPanel panel=new JPanel(new GridBagLayout());
		panel.setBorder(new TitledBorder("Verify"));
		JLabel label=new JLabel("Verify File:");
		constraintsContentPane.insets=new Insets(10,10,10,10);
		constraintsContentPane.anchor=GridBagConstraints.NORTHWEST;
		constraintsContentPane.weighty=0.0;
		panel.add(label,constraintsContentPane);
		final JLabel labelCurrentFile=new JLabel();
		constraintsContentPane.gridx=1;
		constraintsContentPane.fill=GridBagConstraints.HORIZONTAL;
		constraintsContentPane.weightx=1.0;
		panel.add(labelCurrentFile,constraintsContentPane);

		label=new JLabel("Status:");
		constraintsContentPane.gridx=0;
		constraintsContentPane.gridy=1;
		constraintsContentPane.weightx=0.0;
		panel.add(label,constraintsContentPane);
		final JLabel labelCurrentFileStatus=new JLabel();
		constraintsContentPane.gridx=1;
		constraintsContentPane.weightx=1.0;
		panel.add(labelCurrentFileStatus,constraintsContentPane);
		
		final JProgressBar pbFiles=new JProgressBar();
		pbFiles.setMinimum(0);
		constraintsContentPane.gridx=0;
		constraintsContentPane.gridy=2;
		constraintsContentPane.gridwidth=2;
		constraintsContentPane.weightx=1.0;
		constraintsContentPane.weighty=1.0;
		panel.add(pbFiles,constraintsContentPane);

		constraintsContentPane.gridy=0;		
		constraintsContentPane.weightx=1.0;
		constraintsContentPane.weighty=0.0;
		constraintsContentPane.fill=GridBagConstraints.BOTH;
		constraintsContentPane.fill=GridBagConstraints.HORIZONTAL;
		contentPane.add(panel,constraintsContentPane);

		panel=new JPanel();
		panel.setBorder(new TitledBorder("Results"));
		JTable tableVerify=new JTable();
		final DefaultTableModel model=new DefaultTableModel();
		model.addColumn("File");
		model.addColumn("Header");
		model.addColumn("Log Entries");
		model.addColumn("Footer");
	//	model.setRowCount(20);
		tableVerify.setModel(model);
/*		constraintsContentPane.gridx=0;
		constraintsContentPane.gridy=2;
		constraintsContentPane.gridwidth=2;
		constraintsContentPane.weightx=1.0;
		constraintsContentPane.weighty=1.0;
		constraintsContentPane.fill=GridBagConstraints.BOTH;
	*/	JScrollPane sp=new JScrollPane(tableVerify);
		panel.add(sp/*,constraintsContentPane*/);

		constraintsContentPane.gridy=1;
		constraintsContentPane.fill=GridBagConstraints.BOTH;
		constraintsContentPane.weighty=1.0;
		contentPane.add(panel,constraintsContentPane);

		final JButton bttnClose=new JButton("Cancel");
		constraintsContentPane.gridy=2;
		constraintsContentPane.fill=GridBagConstraints.NONE;
		constraintsContentPane.weighty=0.0;
		constraintsContentPane.weightx=0.0;
		constraintsContentPane.anchor=GridBagConstraints.SOUTHEAST;
		contentPane.add(bttnClose,constraintsContentPane);
		
		class CRunnableDoVerify implements Runnable{

			volatile boolean bRun;
			public void stopIt()
			{
				bRun=false;	
			}
			public void run() {
				bRun=true;
				File logFiles[]=null;
				// TODO Auto-generated method stub
				try{
				File dir=new File(m_tfLogDir.getText());
				if(!dir.isDirectory()||!dir.canRead())
					{
					doErrorLogDir();
					return;
					}
				logFiles=dir.listFiles();
				}
				catch(Exception e)
				{
					doErrorLogDir();
					return;					
				}
				pbFiles.setMaximum(logFiles.length);
				for(File logFile:logFiles)
				{
					if(!bRun)
						break;
					if(logFile.isFile())
					{
						boolean bSkipNext=false;
						labelCurrentFile.setText(logFile.getName());
						String[] rowEntries=new String[model.getColumnCount()];
						rowEntries[0]=logFile.getName();
						model.addRow(rowEntries);

						labelCurrentFileStatus.setText("Parse Header");
						DataRetentionLogFile anonLogFile=null;
						try
						{
						anonLogFile=new DataRetentionLogFile(logFile);
						}catch(Exception e)
						{
							labelCurrentFileStatus.setText("Error while parsing Header: "+e.getMessage());
							model.setValueAt("failed", model.getRowCount()-1, 1);
							bSkipNext=true;
						}
						if(!bSkipNext)
						{
							byte[] encKey=anonLogFile.getEncryptedKey(0);
							byte[] symKey=null;
							labelCurrentFileStatus.setText("Try to decrypt symmetric key");
							if(m_bUseSmartCardforPrivateKey)
								try
							{
								symKey=smartcard.decrpytSymmetricKey(encKey);
							}catch(Exception e)
							{
								labelCurrentFileStatus.setText("Error while getting symmetric Key: "+e.getMessage());
								bSkipNext=true;
							}
							else
							{
								try
								{
									symKey=DataRetentionLogFileHeader.decryptSymKey(encKey, privKey);
								}catch(Exception e)
								{
									labelCurrentFileStatus.setText("Error while getting symmetric Key: "+e.getMessage());
									bSkipNext=true;
								}
							}
							if(symKey==null)
							{
								labelCurrentFileStatus.setText("Error while getting symmetric Key: Sym key is NULL");
								bSkipNext=true;
							}
							else
							{
								anonLogFile.setDecryptionKey(symKey);
							}
						}
						if(!bSkipNext)
						{
						labelCurrentFileStatus.setText("Try to verify log file header");
						try
						{
						anonLogFile.verifyHeader();
						}catch(Exception e)
						{
							labelCurrentFileStatus.setText("Couldt not verify header: "+e.getMessage());
							model.setValueAt("failed", model.getRowCount()-1, 1);
						}
						model.setValueAt("ok", model.getRowCount()-1, 1);
						labelCurrentFileStatus.setText("Try to verify log file footer");
						try
						{
						anonLogFile.verifyFooter();
						}catch(Exception e)
						{
							labelCurrentFileStatus.setText("Couldt not verify footer: "+e.getMessage());
							model.setValueAt("failed", model.getRowCount()-1, 3);
						}
						model.setValueAt("ok", model.getRowCount()-1, 3);
						int lines=anonLogFile.getNrOfLogLines();
						labelCurrentFileStatus.setText("Try to verify log line 1/"+lines);
						int goodLines=0,badLines=0;
						try
						{
						anonLogFile.verifyFirstLogLine();
						goodLines++;
						}catch(Exception e)
						{
							labelCurrentFileStatus.setText("Try to verify log line 1/"+lines+" (1 Error, last error message: "+e.getMessage()+")");
							badLines++;
						}
						for(int i=2;i<=lines&&bRun;i++)
						{
							labelCurrentFileStatus.setText("Try to verify log line "+i+"/"+lines);
							try
							{
								anonLogFile.verifyNextLogLine();
								goodLines++;
							}catch(Exception e)
							{
								labelCurrentFileStatus.setText("Try to verify log line "+i+"/"+lines+" (1 Error, last error message: "+e.getMessage()+")");
								badLines++;
							}
						}//for each log line
						model.setValueAt(goodLines+" ok / "+badLines+" failed" , model.getRowCount()-1, 2);

					}// skip next
				}//if is files
//next_files:

				try {
					Thread.sleep(100);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pbFiles.setValue(pbFiles.getValue()+1);
				
			} //for
			bttnClose.setText("Close");	
		} //run()
	};//runable
	
	final CRunnableDoVerify runnableDoVerify=new CRunnableDoVerify();

	final Thread threadDoVerify=new Thread(runnableDoVerify);

	bttnClose.addActionListener(new ActionListener()
	{
			public void actionPerformed(ActionEvent e)
			{
				// TODO Auto-generated method stub
				runnableDoVerify.stopIt();
				try {
					threadDoVerify.join();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				dlgVerify.dispose();
			}
			
		});
		
		
		threadDoVerify.setDaemon(true);
		threadDoVerify.start();
		dlgVerify.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		dlgVerify.pack();
		dlgVerify.setVisible(true);
		
}

private void doErrorPrivKey(JAPDialog parent) {
	// TODO Auto-generated method stub
	JAPDialog.showErrorDialog(parent, "The given private key could not be read. Plaese check if the provided location is correct.", 0);
}

private void doErrorSmartCard(JAPDialog parent) {
	// TODO Auto-generated method stub
	JAPDialog.showErrorDialog(parent, "Could not find any SmartCard reader which contains a valid SmartCard. Plaese check your readers and the inserted SmartCard",0);
}

private void doErrorLogDir() {
	// TODO Auto-generated method stub
	JAPDialog.showErrorDialog(this, "The given log dir could not be read. Plaese check if the provided location is correct.", 0);
}

private void doSelectLogDir() {
	JFileChooser fileChosser=MixConfig.showFileDialog(getOwner(), MixConfig.CHOOSE_DIR_DIALOG,MixConfig.FILTER_ALL);
	File f=fileChosser.getSelectedFile();
	if(f!=null)
		m_tfLogDir.setText(f.getAbsolutePath());
	// TODO Auto-generated method stub
	
}

protected void doLogDecrpyt() throws Exception {
	// TODO Auto-generated method stub
	final MyRSAPrivateKey privKey;
	final DataRetentionSmartCard smartcard;
	
		Object oPrivKeyOrSmartCard=doSetSecretPrivateKey();
		if(oPrivKeyOrSmartCard==null)
			return;
		if(oPrivKeyOrSmartCard instanceof DataRetentionSmartCard)
		{
			privKey=null;
			smartcard=(DataRetentionSmartCard)oPrivKeyOrSmartCard;
		}
		else
		{
			smartcard=null;
			privKey=(MyRSAPrivateKey)oPrivKeyOrSmartCard;
		}

		File logFiles[]=null;
		try{
		File dir=new File(m_tfLogDir.getText());
		if(!dir.isDirectory()||!dir.canRead())
			{
			doErrorLogDir();
			return;
			}
		logFiles=dir.listFiles();
		}
		catch(Exception e)
		{
			doErrorLogDir();
			return;					
		}
		for(File logFile:logFiles)
		{
			if(logFile.isFile())
			{
				DataRetentionLogFile anonLogFile=new DataRetentionLogFile(logFile);
				byte[] encKey=anonLogFile.getEncryptedKey(0);
				byte[] symKey=null;
				if(m_bUseSmartCardforPrivateKey)
					try
				{
					symKey=smartcard.decrpytSymmetricKey(encKey);
				}catch(Exception e)
				{
				}
				else
				{
					try
					{
						symKey=DataRetentionLogFileHeader.decryptSymKey(encKey, privKey);
					}catch(Exception e)
					{
					}
				}
				anonLogFile.setDecryptionKey(symKey);
				Date d=new Date(m_tfYear.getInt()-1900,m_tfMonth.getInt()-1,m_tfDay.getInt(),m_tfHour.getInt(),m_tfMinute.getInt(),m_tfSecond.getInt());
				DataRetentionLogFileEntry[] entriesFound=anonLogFile.search(d.getTime()/1000, 10);
				if(entriesFound!=null)
				{
				for(int i=0;i<entriesFound.length;i++)
				{
					System.out.println((i+1)+". entry: "+entriesFound[i].toString());				
				}
				}
			}
		}
	
}

private void doSelectSecretKey()
{
	final JAPDialog dlgKeySelect=new JAPDialog(this,"Select Secret Key Storage");
	Container contentPane=dlgKeySelect.getContentPane();

	GridBagConstraints constraintsContentPane=new GridBagConstraints();

	contentPane.setLayout(new GridBagLayout());
	
	JRadioButton radiobttnStoreDisk=new JRadioButton("Stored on disk / USB-Stick etc.");
	final JRadioButton radiobttnStoreSmartCard=new JRadioButton("Stored on SmartCard");
	ButtonGroup bttngrpStore=new ButtonGroup();
	bttngrpStore.add(radiobttnStoreDisk);
	bttngrpStore.add(radiobttnStoreSmartCard);
	radiobttnStoreDisk.setSelected(true);
	
	constraintsContentPane.gridx=0;
	constraintsContentPane.gridy=0;
	constraintsContentPane.gridwidth=3;
	constraintsContentPane.insets=new Insets(10,10,10,10);
	constraintsContentPane.anchor=GridBagConstraints.NORTHWEST;
	contentPane.add(radiobttnStoreDisk,constraintsContentPane);
	
	JLabel label=new JLabel("Secret key file:");
	constraintsContentPane.gridy=1;
	constraintsContentPane.gridwidth=1;
	constraintsContentPane.insets=new Insets(0,30,10,10);
	constraintsContentPane.anchor=GridBagConstraints.WEST;
	contentPane.add(label,constraintsContentPane);

	final JTextField tfSecretKeyFile=new JTextField(30);
	tfSecretKeyFile.setText(m_privateKeyFile);
	constraintsContentPane.gridx=1;
	constraintsContentPane.weightx=1.0;
	constraintsContentPane.fill=GridBagConstraints.HORIZONTAL;
	constraintsContentPane.insets=new Insets(0,0,10,10);
	contentPane.add(tfSecretKeyFile,constraintsContentPane);
	
	JButton bttnSelect=new JButton("Select...");
	bttnSelect.addActionListener(new ActionListener()
	{
		public void actionPerformed(ActionEvent arg0) 
		{
			JFileChooser fileChosser = MixConfig.showFileDialog(dlgKeySelect.getOwner(), MixConfig.OPEN_DIALOG, MixConfig.FILTER_PFX);
			File f=fileChosser.getSelectedFile();
			if(f!=null)
				tfSecretKeyFile.setText(f.getAbsolutePath());
		}
		
	});
	constraintsContentPane.gridx=2;
	constraintsContentPane.weightx=0.0;
	constraintsContentPane.insets=new Insets(0,10,10,10);
	contentPane.add(bttnSelect,constraintsContentPane);
	
	constraintsContentPane.gridx=0;
	constraintsContentPane.gridy=2;
	constraintsContentPane.gridwidth=3;
	constraintsContentPane.insets=new Insets(10,10,10,10);
	constraintsContentPane.anchor=GridBagConstraints.NORTHWEST;
	contentPane.add(radiobttnStoreSmartCard,constraintsContentPane);
	JButton bttnOk=new JButton("Ok");
	bttnOk.addActionListener(new ActionListener(){

		public void actionPerformed(ActionEvent arg0) {
			setPrivateKeyFile(radiobttnStoreSmartCard.isSelected(),tfSecretKeyFile.getText());
			dlgKeySelect.dispose();
		}
		
	});
	constraintsContentPane.fill=GridBagConstraints.NONE;
	constraintsContentPane.gridx=0;
	constraintsContentPane.gridy=3;
	constraintsContentPane.gridwidth=3;
	constraintsContentPane.insets=new Insets(10,10,10,10);
	constraintsContentPane.anchor=GridBagConstraints.NORTHEAST;
	contentPane.add(bttnOk,constraintsContentPane);
	
	dlgKeySelect.pack();
	dlgKeySelect.setVisible(true);
}

private void setPrivateKeyFile(boolean bSmartCard,String text) {
	// TODO Auto-generated method stub
	m_bUseSmartCardforPrivateKey=bSmartCard;
	if(bSmartCard)
	{
		m_labelPrivateKeyStorage.setText("SmartCard");
		return;
	}
	if(text==null||text.length()==0)
		{
			m_labelPrivateKeyStorage.setText("Unspecified");
			m_privateKeyFile=null;
		}
	else
		{
			m_labelPrivateKeyStorage.setText(text);
			m_privateKeyFile=text;
		}
}



}
