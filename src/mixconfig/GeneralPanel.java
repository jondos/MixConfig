package mixconfig;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.ButtonGroup;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

class GeneralPanel extends JPanel implements ItemListener,ActionListener
{
  private JComboBox m_comboboxMixType;
  private JTextField MixName,MixID,FileName,ID_Text,num_file;
  private JCheckBox Auto,m_checkboxDaemon,m_checkboxLogging,m_checkboxUserID,m_checkboxNrOfFileDes;
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
    layout.setConstraints(j1,c);
    add(j1);
    m_comboboxMixType = new JComboBox();
    m_comboboxMixType.addItem("First Mix");
    m_comboboxMixType.addItem("Middle Mix");
    m_comboboxMixType.addItem("Last Mix");
    c.weightx=1;
    c.gridx=1;
    c.gridy=0;
    c.gridwidth = 3;
    layout.setConstraints(m_comboboxMixType,c);
    add(m_comboboxMixType);

    JLabel j2 = new JLabel("Mix Name");
    c.gridx=0;
    c.gridy=1;
    c.gridwidth = 1;
    c.weightx = 0;
    layout.setConstraints(j2,c);
    add(j2);
    MixName = new JTextField(20);
    MixName.setText("");
    c.gridx=1;
    c.gridy=1;
    c.gridwidth = 3;
    c.weightx = 1;
    layout.setConstraints(MixName,c);
    add(MixName);

    JLabel j3 = new JLabel("Mix ID");
    c.gridx=0;
    c.gridy=2;
    c.gridwidth = 1;
    c.weightx = 0;
    layout.setConstraints(j3,c);
    add(j3);
    Auto = new JCheckBox("Automatically Generated");
    c.gridx=1;
    c.gridy=2;
    c.gridwidth = 3;
    c.weightx = 1;
    layout.setConstraints(Auto,c);
    Auto.addItemListener(this);
    add(Auto);

    MixID = new JTextField(20);
    MixID.setText("");
    c.gridx=1;
    c.gridy=3;
    c.gridwidth = 3;
    layout.setConstraints(MixID,c);
    add(MixID);

    m_checkboxUserID = new JCheckBox("Set User ID on Execution");
    c.gridy = 5;
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
    c.gridy = 6;
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

    m_checkboxDaemon = new JCheckBox("Run as Daemon?");
    c.gridx = 0;
    c.gridy = 7;
    m_checkboxDaemon.addItemListener(this);
    layout.setConstraints(m_checkboxDaemon,c);
    add(m_checkboxDaemon);

    m_checkboxLogging = new JCheckBox("Enable Logging?");
    c.gridx = 0;
    c.gridy = 8;
    m_checkboxLogging.addItemListener(this);
    layout.setConstraints(m_checkboxLogging,c);
    add(m_checkboxLogging);

    Console = new JRadioButton("Log to Console");
    c.gridx = 1;
    c.gridy = 9;
    c.weightx = 1;
    Console.setActionCommand("LogtoConsole");
    Console.addActionListener(this);
    layout.setConstraints(Console,c);
    add(Console);
    Console.setEnabled(false);

    File = new JRadioButton("Log to file");
    c.gridy = 10;
    c.weightx = 0;
    File.addActionListener(this);
    File.setActionCommand("Logtofile");
    layout.setConstraints(File,c);
    add(File);
    File.setEnabled(false);

    FileName = new JTextField(20);
    FileName.setText("");
    c.gridx = 1;
    c.gridy = 11;
    c.weightx = 1;
    layout.setConstraints(FileName,c);
    add(FileName);
    FileName.setEnabled(false);

    Syslog = new JRadioButton("Log to Syslog");
    c.gridx = 1;
    c.gridy = 12;
    Syslog.addActionListener(this);
    Syslog.setActionCommand("LogtoSyslog");
    layout.setConstraints(Syslog,c);
    add(Syslog);
    Syslog.setEnabled(false);

    bg = new ButtonGroup();
    bg.add(Console);
    bg.add(File);
    bg.add(Syslog);
  }

  public void clear()
    {
       setMixType(null);
       setMixName(null);
       setUserID(null);
       setLogging(false,false,false,null);
       setFileDes(null);
       setDaemon(null);
       setMixID(null);
       Auto.setSelected(false);
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

  public String getMixName()
    {
      return MixName.getText();
    }

  public String getMixAuto()
  {
    if(Auto.isSelected() == true)
      return "True";
    else
      return "False";
  }

  public String getMixID()
  {
    return MixID.getText();
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
      return "Logtofile";
    if(Console.isSelected() == true)
      return "LogtoConsole";
    return "null";
  }

  public boolean isLoggingEnabled()
    {
      return m_checkboxLogging.isSelected();
    }

  public void setLogging(boolean bLogConsole,boolean bLogSyslog,boolean bLogFile, String file)
    {
      if(bLogConsole||bLogSyslog||bLogFile)
        {
          m_checkboxLogging.setSelected(true);
          Console.setSelected(bLogConsole);
          File.setSelected(bLogFile);
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

  public void setMixName(String name)
  {
    MixName.setText(name);
  }

  public void setAuto(boolean b)
  {
    Auto.setSelected(b);
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
    //String selection = ae.getActionCommand();
    if(Console.isSelected() == true || Syslog.isSelected() == true)
    {
      FileName.setEnabled(false);
      FileName.setText("");
    }
    if(File.isSelected() == true)
      FileName.setEnabled(true);
  }

  public void itemStateChanged(ItemEvent ie)
  {
    if(Auto.isSelected() == true)
      MixID.setText("Auto is Selected");
    //if(Auto.isSelected() == false)
    //  MixID.setText("");


    if(m_checkboxLogging.isSelected())
    {
      if(m_checkboxDaemon.isSelected())
        Console.setEnabled(true);
      File.setEnabled(true);
      Syslog.setEnabled(true);
      //logging = "True";
    }
    else
    {
      Console.setEnabled(false);
      File.setEnabled(false);
      Syslog.setEnabled(false);
      FileName.setEnabled(false);
      //logging = "";
    }

    if(m_checkboxDaemon.isSelected() )
    {
      Console.setEnabled(false);
      //is_daemon = "True";
    }
   // if(m_checkboxDaemon.isSelected() == false)
    //  is_daemon = "";

    if(m_checkboxDaemon.isSelected()  && Console.isSelected() )
    {
       File.setSelected(true);
       FileName.setEnabled(true);
    }

    if(Console.isSelected() || Syslog.isSelected() )
    {
      FileName.setEnabled(false);
    }

    if(m_checkboxUserID.isSelected())
    {
      ID_Text.setEnabled(true);
      //User_ID = "True";
    }
    else
    {
      ID_Text.setText("");
      ID_Text.setEnabled(false);
      //User_ID = "";
    }

    if(m_checkboxNrOfFileDes.isSelected())
    {
      num_file.setEnabled(true);
      //fileDes = "True";
    }
    else
    {
      num_file.setText("");
      num_file.setEnabled(false);
      //fileDes = "";
    }
  }
}

