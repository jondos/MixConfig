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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

class DescriptionPanel extends JPanel implements ActionListener
{
	private JPanel panel1, panel2;
	public static JButton map;
	private JTextField text1, text2, text3, longi, lati, operatororg, operatorurl;
	private MapBox box;

	public DescriptionPanel(boolean isApplet)
	{
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 10, 10, 10);

		GridBagLayout forpanel = new GridBagLayout();
		GridBagConstraints d = new GridBagConstraints();
		d.anchor = GridBagConstraints.NORTHWEST;
		d.insets = new Insets(10, 10, 10, 10);

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel1 = new JPanel(forpanel);
		panel1.setBorder(new TitledBorder("Location"));
		layout.setConstraints(panel1, c);
		add(panel1);

		d.gridx = 0;
		d.gridy = 0;
		d.fill = GridBagConstraints.HORIZONTAL;
		JLabel city = new JLabel("City");
		forpanel.setConstraints(city, d);
		panel1.add(city);
		text1 = new JTextField(20);
		d.gridx = 1;
		d.weightx = 1;
		d.gridwidth = 3;
		forpanel.setConstraints(text1, d);
		panel1.add(text1);

		d.gridx = 0;
		d.gridy = 2;
		d.weightx = 0;
		d.gridwidth = 1;
		JLabel state = new JLabel("State");
		forpanel.setConstraints(state, d);
		panel1.add(state);
		text3 = new JTextField(20);
		d.gridx = 1;
		d.gridwidth = 3;
		d.weightx = 1;
		forpanel.setConstraints(text3, d);
		panel1.add(text3);

		d.gridx = 0;
		d.gridy = 3;
		d.weightx = 0;
		d.gridwidth = 1;
		JLabel country = new JLabel("Country");
		forpanel.setConstraints(country, d);
		panel1.add(country);
		text2 = new JTextField(20);
		d.gridx = 1;
		d.gridwidth = 3;
		d.weightx = 1;
		forpanel.setConstraints(text2, d);
		panel1.add(text2);

		d.gridx = 0;
		d.gridy = 4;
		d.weightx = 1;
		d.gridwidth = 3;
		JLabel pos = new JLabel("Geographical Position");
		pos.setToolTipText(
			"Example: University of Technology Dresden, CS Department: Longitude: 13.761, Latitude: 51.053");
		forpanel.setConstraints(pos, d);
		panel1.add(pos);
		map = new JButton("Show on Map");
		map.setToolTipText("This will probably only work on Windows Systems. Blame Sun!");
		map.addActionListener(this);
		map.setActionCommand("Map");
		map.setEnabled(!isApplet);
		d.gridx = 3;
		d.gridwidth = 1;
		d.weightx = 1;
		forpanel.setConstraints(map, d);
		panel1.add(map);
		JLabel longitude = new JLabel("Longitude");
		d.gridy = 5;
		d.gridx = 0;
		d.weightx = 0;
		forpanel.setConstraints(longitude, d);
		panel1.add(longitude);
		longi = new JTextField(20);
		longi.setDocument(new FloatDocument( -180, 180));
		longi.setToolTipText("Longitude in degrees east from Greenwich. ( -180.0 to 180.0)");
		d.gridx = 1;
		d.gridwidth = 3;
		d.weightx = 1;
		forpanel.setConstraints(longi, d);
		panel1.add(longi);
		JLabel latitude = new JLabel("Latitude");
		d.gridy = 6;
		d.gridx = 0;
		d.weightx = 0;
		d.gridwidth = 1;
		forpanel.setConstraints(latitude, d);
		panel1.add(latitude);
		lati = new JTextField(20);
		lati.setDocument(new FloatDocument( -90, 90));
		lati.setToolTipText("Latitude in degrees. (-90.0: South Pole, 0: Equator, 90.0: North Pole)");
		d.gridx = 1;
		d.weightx = 1;
		d.gridwidth = 3;
		forpanel.setConstraints(lati, d);
		panel1.add(lati);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel2 = new JPanel(forpanel);
		panel2.setBorder(new TitledBorder("Operator"));
		layout.setConstraints(panel2, c);
		add(panel2);

		d.gridx = 0;
		d.gridy = 0;
		d.weightx = 0;
		d.gridwidth = 1;
		d.fill = GridBagConstraints.HORIZONTAL;
		JLabel op_org = new JLabel("Organisation");
		forpanel.setConstraints(op_org, d);
		panel2.add(op_org);

		operatororg = new JTextField(60);
		operatororg.setToolTipText(
			"This should contain the operating organisation's or a person's name for private persons.");
		d.gridx = 1;
		d.weightx = 1;
		d.gridwidth = 3;
		forpanel.setConstraints(operatororg, d);
		panel2.add(operatororg);

		d.gridx = 0;
		d.gridy = 2;
		d.weightx = 0;
		d.gridwidth = 1;

		JLabel op_url = new JLabel("URL");
		forpanel.setConstraints(op_url, d);
		panel2.add(op_url);

		operatorurl = new JTextField(20);
		operatorurl.setToolTipText("This should contain a URL that will lead to more information about the operator including contact information.");
		d.gridx = 1;
		d.gridwidth = 3;
		d.weightx = 1;
		forpanel.setConstraints(operatorurl, d);
		panel2.add(operatorurl);

	}

	public String getCity()
	{
		return text1.getText();
	}

	public String getCountry()
	{
		return text2.getText();
	}

	public String getState()
	{
		return text3.getText();
	}

	public String getLongitude()
	{
		return longi.getText();
	}

	public String getLatitude()
	{
		return lati.getText();
	}

	public String getOperatorOrg()
	{
		return operatororg.getText();
	}

	public String getOperatorURL()
	{
		return operatorurl.getText();
	}

	public void setCity(String city)
	{
		text1.setText(city);
	}

	public void setCountry(String country)
	{
		text2.setText(country);
	}

	public void setState(String state)
	{
		text3.setText(state);
	}

	public void setLati(String latitude)
	{
		lati.setText(latitude);
	}

	public void setLongi(String longitude)
	{
		longi.setText(longitude);
	}

	public void setOperatorOrg(String org)
	{
		operatororg.setText(org);
	}

	public void setOperatorURL(String url)
	{
		operatorurl.setText(url);
	}

	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getActionCommand().equals("Map"))
		{
			try
			{
				box = new MapBox(MixConfig.getMainWindow(), getLatitude(), getLongitude(), 5);
				box.show();
				map.setText("Update Map");
				map.setActionCommand("Update");
			}
			catch (Exception e)
			{
				//e.printStackTrace();
			}
		}
		else if (ae.getActionCommand().equals("Update"))
		{
			try
			{
				box.setPosition(getLatitude(), getLongitude());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
