package mixconfig;
import java.util.*;
import java.net.*;
import java.awt.*;
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
import javax.swing.border.TitledBorder;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

class MapBox extends JDialog implements ActionListener,ChangeListener
{
    public static final int BOLD = 3;
    public static String m_IconString,m_urlString;
    public static JLabel map;
    public static JSlider s;
    public static JPanel p;

    public MapBox(MyFrame parent,String title,int level)
    {
     super(parent,title,false);
     GridBagLayout layout = new GridBagLayout();
     GridBagConstraints c =new GridBagConstraints();
     getContentPane().setLayout(layout);
     c.anchor=GridBagConstraints.NORTHWEST;
     c.insets=new Insets(10,10,10,10);
     setSize(650,650);

     try
     {
       URL icon = new URL(m_IconString);
       ImageIcon MapIcon = new ImageIcon(icon);
       map = new JLabel(MapIcon);
       c.gridx = 0;
       c.gridy = 0;
       c.gridwidth = 2;
       layout.setConstraints(map,c);
       getContentPane().add(map);
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }

     p = new JPanel();
     p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
     p.setBorder(new TitledBorder("Zoom"));
     s = new JSlider(JSlider.VERTICAL, 1, 9, level);
     s.setPaintTicks(true);
     s.setMajorTickSpacing(1);
     s.setPaintLabels( true );
     s.addChangeListener(this);
     Dimension d = new Dimension(15,460);
     p.add(Box.createRigidArea(d));
     p.add(s);
     c.gridx = 2;
     layout.setConstraints(p,c);
     getContentPane().add(p);

     Font font = new Font("PROCESSED BY:",BOLD,20);
     JLabel site = new JLabel("PROCESSED BY:");
     site.setFont(font);
     c.gridx = 0;
     c.gridy = 1;
     c.gridwidth = 1;
     c.anchor = GridBagConstraints.CENTER;
     layout.setConstraints(site,c);
     getContentPane().add(site);


     try
     {
       String logo = "http://art.mapquest.com/mqsite_english/logo";
       URL MapLogo = new URL(logo);
       ImageIcon maplogo = new ImageIcon(MapLogo);
       JLabel logolabel = new JLabel(maplogo);
       c.gridx = 1;
       c.gridy = 1;
       layout.setConstraints(logolabel,c);
       getContentPane().add(logolabel);
     }
     catch(Exception e)
     {
        e.printStackTrace();
     }

     JButton b = new JButton("Close");
     c.gridx = 2;
     c.gridy = 1;
     layout.setConstraints(b,c);
     b.addActionListener(this);
     getContentPane().add(b);
    }

    public void actionPerformed(ActionEvent ae)
    {
      dispose();
      DescriptionPanel.map.setText("Show Map");
      DescriptionPanel.map.setActionCommand("Map");
    }

    public static void update()
    {
      try
      {
         URL icon = new URL(m_IconString);
         ImageIcon MapIcon = new ImageIcon(icon);
         map.setIcon(MapIcon);
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }

   public void stateChanged(ChangeEvent e)
   {
      JSlider s1 = (JSlider)e.getSource();
      if (!s1.getValueIsAdjusting())
      {
         System.out.println(s1.getValue());
         int i = s1.getValue();

         try
         {
            URL url = new URL(m_urlString);
            BufferedInputStream bis = new BufferedInputStream(url.openStream(),500);
            char array[] = {'z','o','o','m','='};
            String newsite = "http://mapquest.com/maps/map.adp?zoom=";
            newsite += i;
            newsite = getnext(array,5,bis,newsite,1);
            m_urlString = newsite;

            URL Image = new URL(newsite);
            BufferedInputStream bisImage = new BufferedInputStream(Image.openStream(),500);
            char array2[] = {'q','m','a','p','g','e','n','d'};
            String newImage = "http://mq-mapgend.websys.aol.com:80/mqmapgend";
            newImage = getnext(array2,8,bisImage,newImage,0);
            m_IconString = newImage;

            update();
         }
         catch(Exception exp)
         {
            exp.printStackTrace();
         }
      }
   }


   private String getnext(char array[],int n,BufferedInputStream bis,String newsite,int on)
   {
       int j = 0;
       char ch;

       try
       {
          while(true)
          {
              if((char)bis.read() == array[0])
              {
                 for(j = 1; j < n; j++)
                    if((char)bis.read() != array[j])
                      break;
              }
              if(j == n)
                break;
          }

          if(on == 1)
             ch = (char)bis.read();

          ch = (char)bis.read();
          while(ch != '"')
          {
            newsite += ch;
            ch = (char)bis.read();
          }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return newsite;
    }

}
