package mixconfig;

import java.awt.Choice;
import java.awt.Dialog;
import java.awt.TextArea;
import java.awt.Button;
import java.awt.BorderLayout;
import java.awt.event.*;

import javax.swing.JOptionPane;

class ClipFrame extends Dialog implements ActionListener, ItemListener
{
  private TextArea m_TextArea;
  private Choice chooser;
  private ClipChoice[] choices;

  static public class ClipChoice
  {
      public String name;
      public String text;
      
      public ClipChoice(String n, String t)
      {
          name = n;
          text = t;
      }
  }
  
  private void init(boolean open, ClipChoice[] c)
  {
    choices = c;
    if(choices==null)
        chooser = null;
    else
    {
       chooser = new Choice();
       for(int i=0;i<choices.length;i++)
           chooser.add(choices[i].name);
       add(chooser, BorderLayout.NORTH);
       chooser.addItemListener(this);
    }
   
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

  public ClipFrame(String title,boolean open, ClipChoice[] choices)
  {
    super(TheApplet.getMainWindow(),title,true);
    init(open, choices);
  }

  public ClipFrame(String title,boolean open)
  {
      super(TheApplet.getMainWindow(),title,true);
      init(open, null);
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

    public void itemStateChanged(ItemEvent e)
    {
        setText(choices[chooser.getSelectedIndex()].text);
    }

}