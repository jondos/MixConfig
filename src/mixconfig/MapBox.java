/*
Copyright (c) 2000, The JAP-Team
All rights reserved.
Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

	- Redistributions of source code must retain the above copyright notice,
		this list of conditions and the following disclaimer.

	- Redistributions in binary form must reproduce the above copyright notice,
		this list of conditions and the following disclaimer in the documentation and/or
		other materials provided with the distribution.

	- Neither the name of the University of Technology Dresden, Germany nor the names of its contributors
		may be used to endorse or promote products derived from this software without specific
		prior written permission.


THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS
OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
*/
package mixconfig;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import anon.crypto.*;


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
		private String m_urlString;
		private JLabel map;
		private JSlider s;

		public MapBox(Frame parent,String lat,String lon,int level) throws Exception
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
					//String logo = "http://art.mapquest.com/mqsite_english/logo";
					//URL MapLogo = new URL(logo);
					ImageIcon maplogo = MixConfig.loadImage("mapquest-logo.gif");
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
							URL tmpUrl = new URL("http://www.mapquest.com/maps/map.adp?zoom="+i+"&size=big"+m_urlString);
							BufferedInputStream bisImage = new BufferedInputStream(tmpUrl.openStream(),500);
							char array2[] = {'q','m','a','p','g','e','n','d'};
							String newImage = "http://mq-mapgend.websys.aol.com:80/mqmapgend";
							newImage = getnext(array2,8,bisImage,newImage,0);
							URL icon = new URL(newImage);
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
				BufferedInputStream bissmall;
				String Title = "";
				try
					{
						String site = "http://www.mapquest.com/maps/map.adp?latlongtype=decimal&latitude="+lati+"&longitude="+longi;
						Title = "The location shown on the Map is :  Latitude = "+lati+"  Longitude = "+ longi;
						setTitle(Title);
						URL urlsmall = new URL(site);
						bissmall = new BufferedInputStream(urlsmall.openStream(),500);

						int j = 0;
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
						String add_url = "";
						char address;
						address = (char)bissmall.read();
						while(address != '"')
						{
							 add_url += address;
							 address = (char)bissmall.read();
						}
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
