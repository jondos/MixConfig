package mixconfig;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

class GeneralPanel extends JPanel implements ItemListener,ActionListener
{
  private JComboBox m_comboboxMixType;
  private JTextField MixName,CascadeName,MixID,FileName,ID_Text,num_file;
  private JCheckBox m_checkboxDaemon,m_checkboxLogging,m_checkboxUserID,m_checkboxNrOfFileDes,m_compressLog;
  private JRadioButton Console,File,Syslog;
  private ButtonGroup bg;

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
                CascadeName.setEnabled(m_comboboxMixType.getSelectedIndex()==0);
            }
        });

    JLabel j1a = new JLabel("Cascade Name");
    c.gridx=0;
    c.gridy++;
    c.gridwidth = 1;
    c.weightx = 0;
    layout.setConstraints(j1a,c);
    add(j1a);
    CascadeName = new JTextField(20);
    CascadeName.setText("");
    c.gridx=1;
    c.gridwidth = 3;
    c.weightx = 1;
    layout.setConstraints(CascadeName,c);
    add(CascadeName);

    JLabel j2 = new JLabel("Mix Name");
    c.gridx=0;
    c.gridy++;
    c.gridwidth = 1;
    c.weightx = 0;
    layout.setConstraints(j2,c);
    add(j2);
    MixName = new JTextField(20);
    MixName.setText("");
    c.gridx=1;
    c.gridwidth = 3;
    c.weightx = 1;
    layout.setConstraints(MixName,c);
    add(MixName);

    JLabel j3 = new JLabel("Mix ID");
    c.gridx=0;
    c.gridy++;
    c.gridwidth = 1;
    c.weightx = 0;
    layout.setConstraints(j3,c);
    add(j3);

    MixID = new JTextField(20);
    MixID.setText("");
    c.gridx=1;
    c.gridwidth = 2;
    c.weightx = 1;
    layout.setConstraints(MixID,c);
    add(MixID);

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
                String oMixid=MixID.getText();

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
                MixID.setText(str);
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
    ID_Text = new JTextField(20);
    c.gridx = 1;
    c.weightx = 1;
    c.gridwidth = 3;
    layout.setConstraints(ID_Text,c);
    add(ID_Text);
    ID_Text.setEnabled(false);

    m_checkboxNrOfFileDes = new JCheckBox("Set Number of File Descriptors");
    c.weightx = 0;
    c.gridy++;
    c.gridx = 0;
    c.gridwidth = 1;
    m_checkboxNrOfFileDes.addItemListener(this);
    layout.setConstraints(m_checkboxNrOfFileDes,c);
    add(m_checkboxNrOfFileDes);
    num_file = new JTextField(20);
    c.gridx = 1;
    c.gridwidth = 3;
    c.weightx = 1;
    layout.setConstraints(num_file,c);
    add(num_file);
    num_file.setEnabled(false);

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

    Console = new JRadioButton("Log to Console");
    c.gridx = 1;
    c.weightx = 1;
    c.gridwidth = 3;
    Console.setActionCommand("LogtoConsole");
    Console.addActionListener(this);
    layout.setConstraints(Console,c);
    add(Console);
    Console.setEnabled(false);

    File = new JRadioButton("Log to Directory");
    c.gridwidth = 3;
    c.weightx = 1;
    c.gridy++;
    File.addActionListener(this);
    File.setActionCommand("Logtodir");
    layout.setConstraints(File,c);
    add(File);
    File.setEnabled(false);

    FileName = new JTextField(20);
    FileName.setText("");
    c.gridx = 1;
    c.gridy++;
    c.gridwidth = 3;
    c.weightx = 1;
    layout.setConstraints(FileName,c);
    add(FileName);
    FileName.setEnabled(false);

    m_compressLog = new JCheckBox("Compress Log Files");
    c.gridx = 1;
    c.gridy++;
    c.gridwidth = 3;
    c.weightx = 1;
    layout.setConstraints(m_compressLog,c);
    add(m_compressLog);
    m_compressLog.setEnabled(false);

    Syslog = new JRadioButton("Log to Syslog");
    c.gridx = 1;
    c.gridy++;
    Syslog.addActionListener(this);
    Syslog.setActionCommand("LogtoSyslog");
    layout.setConstraints(Syslog,c);
    add(Syslog);
    Syslog.setEnabled(false);

    bg = new ButtonGroup();
    bg.add(Console);
    bg.add(File);
    bg.add(Syslog);
    Console.setSelected(true);
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
      return CascadeName.getText();
  }

  public String getMixName()
    {
      return MixName.getText();
    }

  public String getMixID()
  {
    return MixID.getText();
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
      return ID_Text.getText().trim();
    return null;
  }

  public String getFileDes()
  {
    if(m_checkboxNrOfFileDes.isSelected())
      return num_file.getText();
    return null;
  }

  public String getFileName()
  {
    return FileName.getText();
  }

  public String getEnabled()
  {
    if(Syslog.isSelected() == true)
      return "LogtoSyslog";
    if(File.isSelected() == true)
      return "Logtodir";
    if(Console.isSelected() == true)
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
          Console.setSelected(bLogConsole);
          File.setSelected(bLogFile);
          if(bLogFile)
          {
            FileName.setText(file);
            m_compressLog.setSelected(bcompLog);
          }
          Syslog.setSelected(bLogSyslog);

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
    CascadeName.setText(name);
  }

  public void setMixName(String name)
  {
    MixName.setText(name);
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
    MixID.setText(mixid);
  }

  public void setUserID(String userid)
  {
    ID_Text.setText(userid);
    m_checkboxUserID.setSelected(userid!=null);
  }

  public void setFileDes(String filedes)
  {
    num_file.setText(filedes);
    m_checkboxNrOfFileDes.setSelected(filedes!=null);
  }


  public void actionPerformed(ActionEvent ae)
  {
    FileName.setEnabled(File.isSelected());
    m_compressLog.setEnabled(File.isSelected());
  }

  public void itemStateChanged(ItemEvent ie)
  {
    if(m_checkboxLogging.isSelected())
      {
        if(m_checkboxDaemon.isSelected())
          {
            if( Console.isSelected() )//switch automaticaly to File if console and dameon is selected
              File.setSelected(true);
            Console.setEnabled(false);
          }
        else
          Console.setEnabled(true);
        File.setEnabled(true);
        FileName.setEnabled(File.isSelected());
        m_compressLog.setEnabled(File.isSelected());
        Syslog.setEnabled(true);
      }
    else
      {
        Console.setEnabled(false);
        File.setEnabled(false);
        Syslog.setEnabled(false);
        FileName.setEnabled(false);
        m_compressLog.setEnabled(false);
    }


    ID_Text.setEnabled(m_checkboxUserID.isSelected());
    num_file.setEnabled(m_checkboxNrOfFileDes.isSelected());
  }
}

