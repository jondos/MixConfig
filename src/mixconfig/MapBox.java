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
    private String m_IconString,m_urlString;
    private JLabel map;
    private JSlider s;
    private JPanel p;

    public MapBox(JFrame parent,String lat,String lon,int level) throws Exception
    {
      super(parent,"",false);
      setBackground(Color.white);
      Double.valueOf(lat);
      Double.valueOf(lon);

      GridBagLayout layout = new GridBagLayout();
      GridBagConstraints c =new GridBagConstraints();
      getContentPane().setLayout(layout);
      getContentPane().setBackground(Color.white);
      c.anchor=GridBagConstraints.NORTHWEST;
      c.insets=new Insets(10,10,10,10);
      map = new JLabel();
      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 2;
      c.gridheight=2;
      layout.setConstraints(map,c);
      getContentPane().add(map);

      c.gridx=2;
      c.gridy=0;
      c.gridwidth=1;
      c.gridheight=1;
      c.insets=new Insets(20,10,5,10);
      JLabel l=new JLabel("Zoom");
      layout.setConstraints(l,c);
      getContentPane().add(l);

      s = new JSlider(JSlider.VERTICAL, 1, 9, level);
      s.setBackground(Color.white);
      s.setPaintTicks(true);
      s.setMajorTickSpacing(1);
      s.setMinorTickSpacing(1);
      s.setSnapToTicks(true);
      s.setPaintLabels( true );
      s.setRequestFocusEnabled(false);
      s.addChangeListener(this);
      c.insets=new Insets(5,10,20,10);
      c.gridx = 2;
      c.gridy=1;
      c.fill=GridBagConstraints.VERTICAL;
      layout.setConstraints(s,c);
      getContentPane().add(s);

      Font font = new Font("Dialog",Font.BOLD,20);
      JLabel site = new JLabel("PROCESSED BY:");
      site.setFont(font);
      c.insets=new Insets(10,10,10,10);
      c.gridx = 0;
      c.gridy = 2;
      c.gridwidth = 1;
      c.anchor = GridBagConstraints.CENTER;
      c.fill=GridBagConstraints.NONE;
      layout.setConstraints(site,c);
      getContentPane().add(site);

      try
        {
          String logo = "http://art.mapquest.com/mqsite_english/logo";
          URL MapLogo = new URL(logo);
          ImageIcon maplogo = new ImageIcon(MapLogo);
          JLabel logolabel = new JLabel(maplogo);
          c.gridx = 1;
          c.gridy = 2;
          layout.setConstraints(logolabel,c);
          getContentPane().add(logolabel);
        }
      catch(Exception e)
        {
          e.printStackTrace();
        }

      JButton b = new JButton("Close");
      c.gridx = 2;
      c.gridy = 2;
      layout.setConstraints(b,c);
      b.addActionListener(this);
      getContentPane().add(b);
      setPosition(lat,lon);
      pack();
      addWindowListener(new WindowAdapter()
        {
          public void windowClosing(WindowEvent e)
            {
              close();
            }
        });
    }
    private void close()
    {
      dispose();
      DescriptionPanel.map.setText("Show on Map");
      DescriptionPanel.map.setActionCommand("Map");
    }

    public void actionPerformed(ActionEvent ae)
      {
        close();
      }

/*    public static void update()
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
*/
   public void stateChanged(ChangeEvent e)
   {
      JSlider s1 = (JSlider)e.getSource();
      if (!s1.getValueIsAdjusting())
        setZoomLevel(s1.getValue());
   }

    private void setZoomLevel(int i)
      {
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
              URL icon = new URL(m_IconString);
              ImageIcon MapIcon = new ImageIcon(icon);
              map.setIcon(MapIcon);
            }
         catch(Exception exp)
          {
            exp.printStackTrace();
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

    public void setPosition(String lati,String longi) throws Exception
      {
        double lat=Double.valueOf(lati).doubleValue();
        double lon=Double.valueOf(longi).doubleValue();

        BufferedInputStream bissmall,bisbig;
        String Title = "";
        try
          {
            String site = "http://www.mapquest.com/maps/map.adp?latlongtype=decimal&latitude="+lati+"&longitude="+longi;
            Title = "The location shown on the Map is :  Latitude = "+lati+"  Longitude = "+ longi;
            setTitle(Title);
            URL urlsmall = new URL(site);
            bissmall = new BufferedInputStream(urlsmall.openStream(),500);

            int i = 0,j = 0;
            char small[] = {'i','z','e','=','b','i','g'};

            while(true)
            {
              if((char)bissmall.read() == 's')
              {
                 for(j = 0; j < 7; j++)
                   if((char)bissmall.read() != small[j])
                     break;
              }
              if(j == 7)
                break;
            }
            String add_url = "http://www.mapquest.com/maps/map.adp?size=big";
            char address;
            address = (char)bissmall.read();
            while(address != '"')
            {
               add_url += address;
               address = (char)bissmall.read();
            }

            URL urlbig = new URL(add_url);
            bisbig = new BufferedInputStream(urlbig.openStream(),500);
            char big[] = {'q','m','a','p','g','e','n','d'};
            while(true)
            {
              if((char)bisbig.read() == 'm')
              {
                 for(j = 0; j < 8; j++)
                   if((char)bisbig.read() != big[j])
                     break;
              }
              if(j == 8)
                break;
            }
            String big_url = "http://mq-mapgend.websys.aol.com:80/";
            address = (char)bisbig.read();
            while(address != '"')
            {
               big_url += address;
               address = (char)bisbig.read();
            }
            m_IconString = big_url;
            m_urlString = add_url;
            setZoomLevel(s.getValue());
          }
          catch(Exception e)
          {
            map.setText("Error getting the map: "+e.getMessage());
            e.printStackTrace();
          }
      }

}
