package mixconfig;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

class DescriptionPanel extends JPanel implements ActionListener
  {
    private JPanel panel1;
    public static JButton map;
    private JTextField text1,text2,longi,lati;
    private MapBox box;

    public DescriptionPanel()
    {
      GridBagLayout layout=new GridBagLayout();
      setLayout(layout);
      GridBagConstraints c=new GridBagConstraints();
      c.anchor=GridBagConstraints.NORTHWEST;
      c.insets=new Insets(10,10,10,10);

      GridBagLayout forpanel = new GridBagLayout();
      GridBagConstraints d = new GridBagConstraints();
      d.anchor=GridBagConstraints.NORTHWEST;
      d.insets=new Insets(10,10,10,10);

      c.gridx = 0;
      c.gridy = 0;
      c.weightx = 1;
      c.weighty = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      panel1 = new JPanel(forpanel);
      panel1.setBorder(new TitledBorder("Location"));
      layout.setConstraints(panel1,c);
      add(panel1);

      d.gridx = 0;
      d.gridy = 0;
      d.fill = GridBagConstraints.HORIZONTAL;
      JLabel city = new JLabel("City");
      forpanel.setConstraints(city,d);
      panel1.add(city);
      text1 = new JTextField(20);
      d.gridx = 1;
      d.weightx = 1;
      d.gridwidth = 3;
      forpanel.setConstraints(text1,d);
      panel1.add(text1);

      d.gridx = 0;
      d.gridy = 2;
      d.weightx = 0;
      d.gridwidth = 1;
      JLabel state = new JLabel("State");
      forpanel.setConstraints(state,d);
      panel1.add(state);
      text2 = new JTextField(20);
      d.gridx = 1;
      d.gridwidth = 3;
      d.weightx = 1;
      forpanel.setConstraints(text2,d);
      panel1.add(text2);

      d.gridx = 0;
      d.gridy = 3;
      d.weightx = 1;
      d.gridwidth = 3;
      JLabel pos = new JLabel("Geographical Position");
      forpanel.setConstraints(pos,d);
      panel1.add(pos);
      map = new JButton("Show on Map");
      map.addActionListener(this);
      map.setActionCommand("Map");
      d.gridx = 3;
      d.gridwidth = 1;
      d.weightx = 1;
      forpanel.setConstraints(map,d);
      panel1.add(map);
      JLabel longitude = new JLabel("Longitude");
      d.gridy = 4;
      d.gridx = 0;
      d.weightx = 0;
      forpanel.setConstraints(longitude,d);
      panel1.add(longitude);
      longi = new JTextField(20);
      d.gridx = 1;
      d.gridwidth = 3;
      d.weightx = 1;
      forpanel.setConstraints(longi,d);
      panel1.add(longi);
      JLabel latitude = new JLabel("Latitude");
      d.gridy = 5;
      d.gridx = 0;
      d.weightx = 0;
      d.gridwidth = 1;
      forpanel.setConstraints(latitude,d);
      panel1.add(latitude);
      lati = new JTextField(20);
      d.gridx = 1;
      d.weightx = 1;
      d.gridwidth = 3;
      forpanel.setConstraints(lati,d);
      panel1.add(lati);
    }


    public String getCity()
    {
      return text1.getText();
    }

    public String getState()
    {
      return text2.getText();
    }

    public String getLongitude()
    {
      return longi.getText();
    }

    public String getLatitude()
    {
      return lati.getText();
    }

    public void setCity(String city)
    {
      text1.setText(city);
    }
    public void setState(String state)
    {
      text2.setText(state);
    }
    public void setLati(String latitude)
    {
     lati.setText(latitude);
    }
    public void setLongi(String longitude)
    {
      longi.setText(longitude);
    }


   public void actionPerformed(ActionEvent ae)
      {
        if(ae.getActionCommand().equals("Map"))
          {
            try
              {
                box = new MapBox(TheApplet.myFrame,getLatitude(),getLongitude(),5);
                box.show();
                map.setText("Update Map");
                map.setActionCommand("Update");
              }
            catch(Exception e)
              {
                //e.printStackTrace();
              }
          }
        else if(ae.getActionCommand().equals("Update"))
          {
            try
              {
                box.setPosition(getLatitude(),getLongitude());
              }
            catch(Exception e)
              {
                e.printStackTrace();
              }
          }
      }
  }
