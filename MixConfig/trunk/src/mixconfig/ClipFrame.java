package mixconfig;

import java.util.*;
import java.net.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.Font.*;
import java.io.*;
import javax.swing.*;
import java.applet.*;
import java.lang.Object;
import java.math.*;
import java.net.URLEncoder;
import javax.swing.BorderFactory;
import javax.swing.table.*;
import javax.swing.event.*;

class ClipFrame extends Frame implements ActionListener
{
  public static TextArea Area;

  public ClipFrame(String title,boolean open)
  {
    Frame ClipBox = new Frame(title);
    Area = new TextArea(20,100);
    Area.setText("");

    if(open == true)
    {
      MenuBar mb = new MenuBar();
      ClipBox.setMenuBar(mb);
      Menu fileMenu = new Menu("File");
      mb.add(fileMenu);
      MenuItem OpenItem = new MenuItem("Open");
      OpenItem.addActionListener(this);
      OpenItem.setActionCommand("open");
      fileMenu.add(OpenItem);
    }

    ClipBox.add(Area);
    ClipBox.addWindowListener(new WindowAdapter()
    {
           public void windowClosing(WindowEvent e)
          {
            Frame frame = (Frame)e.getSource();
            frame.dispose();
          }
    });

    ClipBox.pack();
    ClipBox.setVisible(true);
  }

  public static void SetText(String data)
  {
    Area.setText(data);
  }

  public void actionPerformed(ActionEvent ae)
    {
      if(ae.getActionCommand().equals("open"))
      {
          if(Area.getText().equals(""))
          {
            DialogBox dialog = new DialogBox(TheApplet.myFrame,"ERROR !!");
            dialog.setlabel("The Text Area is empty!!");
            dialog.setVisible(true);
          }
          else
          {
            OpenFile();
          }
      }
    }

  public void OpenFile()
  {
 /*   String data = Area.getText();
    int i = 0,j = 0,index,k = 1;
    String find2 = "";

    // General Panel........

    String find = "<MixType>";
    index = search(data,find);
    MyFrame.m_GeneralPanel.setType(getString(data,index));
    find = "<MixName>";
    index = search(data,find);
    MyFrame.m_GeneralPanel.setMixName(getString(data,index));
    find = "<AutomaticallyGenerated>";
    index = search(data,find);
    if(index != 0)
      MyFrame.m_GeneralPanel.setAuto();
    find = "<MixID>";
    index = search(data,find);
    MyFrame.m_GeneralPanel.setMixID(getString(data,index));
    find = "<UserID>";
    index = search(data,find);
    if(index != 0)
    {
      MyFrame.m_GeneralPanel.UserID.setSelected(true);
      MyFrame.m_GeneralPanel.setUserID(getString(data,index));
    }
    find = "<NumberofFileDescriptors>";
    index = search(data,find);
    if(index != 0)
    {
      MyFrame.m_GeneralPanel.number.setSelected(true);
      MyFrame.m_GeneralPanel.setFileDes(getString(data,index));
    }
    find = "<Daemon>";
    index = search(data,find);
    if(index != 0)
      MyFrame.m_GeneralPanel.Daemon.setSelected(true);
    find = "<EnableLogging>";
    index = search(data,find);
    if(index != 0)
      MyFrame.m_GeneralPanel.EnableLog.setSelected(true);
    find = "<Console>";
    index = search(data,find);
    if(index != 0)
      MyFrame.m_GeneralPanel.Console.setSelected(true);
    find = "<File>";
    index = search(data,find);
    if(index != 0)
    {
      MyFrame.m_GeneralPanel.File.setSelected(true);
      MyFrame.m_GeneralPanel.FileName.setText(getString(data,index));
      MyFrame.m_GeneralPanel.FileName.setEnabled(true);
    }
    find = "<SysLog>";
    index = search(data,find);
    if(index != 0)
      MyFrame.m_GeneralPanel.Syslog.setSelected(true);

      // Network panel........

    find = "<ListenerInterface" + k + ">";
    index = search(data,find);
    while(index != 0)
    {
      MyFrame.m_NetworkPanel.setTable1(Integer.toString(k),k-1,0);
      find = "<Main>";
      index = search(data,find);
      MyFrame.m_NetworkPanel.setTable1(getString(data,index),k-1,1);
      find = "<Transport>";
      index = search(data,find);
      MyFrame.m_NetworkPanel.setTable1(getString(data,index),k-1,2);
      if(getString(data,index).equals("RAW/UNIX") || getString(data,index).equals("SSl/UNIX"))
      {
        find = "<File>";
        index = search(data,find);
        MyFrame.m_NetworkPanel.setTable1(getString(data,index),k-1,3);
      }
      else
      {
        find = "<Host>";
        index = search(data,find);
        MyFrame.m_NetworkPanel.setTable1(getString(data,index),k-1,3);
        find = "<IP>";
        index = search(data,find);
        if(index != 0)
          MyFrame.m_NetworkPanel.setTable1(getString(data,index),k-1,4);
        find = "<Port>";
        index = search(data,find);
        MyFrame.m_NetworkPanel.setTable1(getString(data,index),k-1,5);
      }

      find = "</ListenerInterface" + k + ">";
      index = search(data,find);
      data = data.substring(index);
      k++;
      find = "<ListenerInterface" + k + ">";
      index = search(data,find);
    }

    k = 1;
    find = "<ListenerInterface" + k + ">";
    index = search(data,find);
    while(index != 0)
    {
      MyFrame.m_NetworkPanel.setTable2(Integer.toString(k),k-1,0);
      find = "<Main>";
      index = search(data,find);
      MyFrame.m_NetworkPanel.setTable2(getString(data,index),k-1,1);
      find = "<Kind>";
      index = search(data,find);
      MyFrame.m_NetworkPanel.setTable2(getString(data,index),k-1,2);
      find = "<Transport>";
      index = search(data,find);
      MyFrame.m_NetworkPanel.setTable2(getString(data,index),k-1,3);
      if(getString(data,index).equals("RAW/UNIX") || getString(data,index).equals("SSl/UNIX"))
      {
        find = "<File>";
        index = search(data,find);
        MyFrame.m_NetworkPanel.setTable2(getString(data,index),k-1,4);
      }
      else
      {
        find = "<Host>";
        index = search(data,find);
        MyFrame.m_NetworkPanel.setTable2(getString(data,index),k-1,4);
        find = "<IP>";
        index = search(data,find);
        if(index != 0)
          MyFrame.m_NetworkPanel.setTable2(getString(data,index),k-1,5);
        find = "<Port>";
        index = search(data,find);
        MyFrame.m_NetworkPanel.setTable2(getString(data,index),k-1,6);
      }

      find = "</ListenerInterface" + k + ">";
      index = search(data,find);
      data = data.substring(index);
      k++;
      find = "<ListenerInterface" + k + ">";
      index = search(data,find);
    }
     find = "<Host>";
     index = search(data,find);
     MyFrame.m_NetworkPanel.setInfoHost(getString(data,index));
     find = "<IP>";
     index = search(data,find);
     if(index != 0)
       MyFrame.m_NetworkPanel.setInfoIP(getString(data,index));
     find = "<Port>";
     index = search(data,find);
     if(index != 0)
       MyFrame.m_NetworkPanel.setInfoPort(getString(data,index));

     // Certificates Panel........

     find = "<Name>";
     index = search(data,find);
     MyFrame.m_CertificatesPanel.setName1(getString(data,index));
     find = "<ValidFrom>";
     index = search(data,find);
     MyFrame.m_CertificatesPanel.setFrom1(getString(data,index));
     find = "<ValidTill>";
     index = search(data,find);
     MyFrame.m_CertificatesPanel.setTo1(getString(data,index));

     data = data.substring(index);
     find = "<Name>";
     index = search(data,find);
     MyFrame.m_CertificatesPanel.setName2(getString(data,index));
     find = "<ValidFrom>";
     index = search(data,find);
     MyFrame.m_CertificatesPanel.setFrom2(getString(data,index));
     find = "<ValidTill>";
     index = search(data,find);
     MyFrame.m_CertificatesPanel.setTo2(getString(data,index));

     data = data.substring(index);
     find = "<Name>";
     index = search(data,find);
     MyFrame.m_CertificatesPanel.setName3(getString(data,index));
     find = "<ValidFrom>";
     index = search(data,find);
     MyFrame.m_CertificatesPanel.setFrom3(getString(data,index));
     find = "<ValidTill>";
     index = search(data,find);
     MyFrame.m_CertificatesPanel.setTo3(getString(data,index));

     //Description Panel...........

     find = "<City>";
     index = search(data,find);
     MyFrame.m_DescriptionPanel.setCity(getString(data,index));
     find = "<State>";
     index = search(data,find);
     MyFrame.m_DescriptionPanel.setState(getString(data,index));
     find = "<Longitude>";
     index = search(data,find);
     MyFrame.m_DescriptionPanel.setLongi(getString(data,index));
     find = "<Latitude>";
     index = search(data,find);
     MyFrame.m_DescriptionPanel.setLati(getString(data,index));
  */
  }

  private String getString(String data, int index)
  {
    String text = "";

    while(data.charAt(index) != '<')
    {
      text += data.charAt(index);
      index++;
    }

    return text;
  }

  public int search(String data,String find)
  {
     int i = 0,j = 0;

     while(i < data.length())
     {
        j = 0;
        if(data.charAt(i) == find.charAt(j))
        {
           i++;
           j++;
           while(true)
           {
             if(j < find.length() && (data.charAt(i) == find.charAt(j)))
             {
               i++;
               j++;
             }
             else
               break;
           }
           if(j == find.length())
              return i;
        }
        i++;
     }
    return 0;
  }

}