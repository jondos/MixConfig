package mixconfig;

import javax.swing.JDialog;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

class PasswordBox extends JDialog implements ActionListener
 {
    private JPasswordField m_textOldPasswd,m_textNewPasswd,m_textConfirmPasswd;
    private char[] m_passwd=null;
    private char[] m_oldPasswd=null;
    private int m_Type;
    public final static int NEW_PASSWORD=1;
    public final static int ENTER_PASSWORD=2;
    public final static int CHANGE_PASSWORD=3;

   PasswordBox(JFrame parent,String title,int type)
   {
      super(parent,title,true);
      m_Type=type;
      GridBagLayout layout=new GridBagLayout();
      getContentPane().setLayout(layout);
      GridBagConstraints c=new GridBagConstraints();
      c.anchor=GridBagConstraints.WEST;
      c.insets=new Insets(10,10,10,10);
      c.gridx = 0;
      c.gridy = 0;

      if(type==CHANGE_PASSWORD)
        {
          JLabel old = new JLabel("Enter Old Password");
          layout.setConstraints(old,c);
          getContentPane().add(old);
          m_textOldPasswd = new JPasswordField(20);
          m_textOldPasswd.setEchoChar('*');
          c.gridx = 1;
          c.weightx = 1;
          c.fill=GridBagConstraints.HORIZONTAL;
          layout.setConstraints(m_textOldPasswd,c);
          getContentPane().add(m_textOldPasswd);
        }
      if(type==CHANGE_PASSWORD||type==NEW_PASSWORD)
        {
          JLabel new1 = new JLabel("Enter New Password");
          c.gridx = 0;
          c.gridy++;
          c.weightx = 0;
          c.fill=GridBagConstraints.NONE;
          layout.setConstraints(new1,c);
          getContentPane().add(new1);
          m_textNewPasswd = new JPasswordField(20);
          m_textNewPasswd.setEchoChar('*');
          c.fill=GridBagConstraints.HORIZONTAL;
          c.gridx = 1;
          c.weightx = 1;
          layout.setConstraints(m_textNewPasswd,c);
          getContentPane().add(m_textNewPasswd);
        }
      JLabel new2 = new JLabel("Confirm Password");
      c.fill=GridBagConstraints.NONE;
      c.gridx = 0;
      c.gridy ++;
      c.weightx = 0;
      layout.setConstraints(new2,c);
      getContentPane().add(new2);
      m_textConfirmPasswd = new JPasswordField(20);
      m_textConfirmPasswd.setEchoChar('*');
      c.gridx = 1;
      c.weightx = 1;
      c.fill=GridBagConstraints.HORIZONTAL;
      layout.setConstraints(m_textConfirmPasswd,c);
      getContentPane().add(m_textConfirmPasswd);

      JPanel p=new JPanel();
      JButton b = new JButton("OK");
      b.setActionCommand("OK");
/*      c.gridx = 0;
      c.gridwidth=1;
      c.anchor=GridBagConstraints.CENTER;
      layout.setConstraints(b,c);
      getContentPane().add(b);
  */
      p.add(b);
      b.addActionListener(this);
      b = new JButton("Cancel");
      b.setActionCommand("Cancel");
/*      c.gridx = 1;
      c.gridwidth=1;
      c.gridy ++;
      layout.setConstraints(b,c);
      getContentPane().add(b);
  */
      p.add(b);
      b.addActionListener(this);
      c.gridx = 0;
      c.gridwidth=2;
      c.gridy++;
      c.anchor=GridBagConstraints.CENTER;
      c.weightx=1;
      c.fill=GridBagConstraints.HORIZONTAL;
      layout.setConstraints(p,c);
      getContentPane().add(p);
      pack();
      setLocationRelativeTo(parent);
    }

    public void actionPerformed(ActionEvent ae)
      {
        if(ae.getActionCommand().equals("OK"))
          {
            if(m_Type==NEW_PASSWORD||m_Type==CHANGE_PASSWORD)
            {
              if(!Arrays.equals(
                      m_textConfirmPasswd.getPassword(),
                      m_textNewPasswd.getPassword()))
              {
                  javax.swing.JOptionPane.showMessageDialog(this,
                          "Passwords do not match.", "Password Error",
                          javax.swing.JOptionPane.ERROR_MESSAGE);
                  return;
              }
              m_passwd=m_textNewPasswd.getPassword();
            }
            else if(m_Type==ENTER_PASSWORD)
              m_passwd=m_textConfirmPasswd.getPassword();
            if(m_Type==CHANGE_PASSWORD)
              m_oldPasswd=m_textOldPasswd.getPassword();
          }
        else
          m_passwd=null;
        dispose();
      }

    public char[] getPassword()
      {
        return m_passwd;
      }

    public char[] getOldPassword()
      {
        return m_oldPasswd;
      }

  }
