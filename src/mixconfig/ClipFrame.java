package mixconfig;

import java.awt.Dialog;
import java.awt.TextArea;
import java.awt.Button;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.JOptionPane;

class ClipFrame extends Dialog implements ActionListener
{
  private TextArea m_TextArea;

  public ClipFrame(String title,boolean open)
  {
    super(TheApplet.getMainWindow(),title,true);
    m_TextArea = new TextArea(30,80);
    m_TextArea.setText("");
    add(m_TextArea,BorderLayout.CENTER);

    if(open == true)
    {
      Button b = new Button("Open");
      b.addActionListener(this);
      b.setActionCommand("open");
      add(b,BorderLayout.SOUTH);
    }

    addWindowListener(new WindowAdapter()
    {
           public void windowClosing(WindowEvent e)
          {
            dispose();
          }
    });

    pack();
  }

  public void setText(String data)
  {
    m_TextArea.setText(data);
  }

  public String getText()
  {
    return m_TextArea.getText();
  }

  public void actionPerformed(ActionEvent ae)
    {
      if(ae.getActionCommand().equals("open"))
      {
          if(m_TextArea.getText().equals(""))
          {
            JOptionPane.showMessageDialog(TheApplet.getMainWindow(),"The Text Area is empty!",
                            "Error!",JOptionPane.ERROR_MESSAGE);

          }
          else
          {
            dispose();
          }
      }
    }


}