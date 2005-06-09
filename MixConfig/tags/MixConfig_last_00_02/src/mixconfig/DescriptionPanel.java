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

import java.util.Vector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class DescriptionPanel extends MixConfigPanel implements ActionListener
{
	private JPanel panel1, panel2;
	private JButton map;
	private JTextField text1, text2, text3, longi, lati, operatororg, operatorurl, operatoremail;
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

		panel1 = new JPanel(forpanel);
		panel1.setBorder(new TitledBorder("Location"));
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(panel1, c);
		add(panel1);

		JLabel city = new JLabel("City");
		d.gridx = 0;
		d.gridy = 0;
		d.fill = GridBagConstraints.HORIZONTAL;
		forpanel.setConstraints(city, d);
		panel1.add(city);

		text1 = new JTextField(20);
		text1.setName("Description/Location/City");
		text1.addFocusListener(this);
		d.gridx = 1;
		d.weightx = 1;
		d.gridwidth = 3;
		forpanel.setConstraints(text1, d);
		panel1.add(text1);

		JLabel state = new JLabel("State");
		d.gridx = 0;
		d.gridy = 2;
		d.weightx = 0;
		d.gridwidth = 1;
		forpanel.setConstraints(state, d);
		panel1.add(state);

		text3 = new JTextField(20);
		text3.setName("Description/Location/State");
		text3.addFocusListener(this);
		d.gridx = 1;
		d.gridwidth = 3;
		d.weightx = 1;
		forpanel.setConstraints(text3, d);
		panel1.add(text3);

		JLabel country = new JLabel("Country");
		d.gridx = 0;
		d.gridy = 3;
		d.weightx = 0;
		d.gridwidth = 1;
		forpanel.setConstraints(country, d);
		panel1.add(country);

		text2 = new JTextField(20);
		text2.setName("Description/Location/Country");
		text2.addFocusListener(this);
		d.gridx = 1;
		d.gridwidth = 3;
		d.weightx = 1;
		forpanel.setConstraints(text2, d);
		panel1.add(text2);

		JLabel pos = new JLabel("Geographical Position");
		pos.setName("Description/Location/Position");
		pos.addFocusListener(this);
		pos.setToolTipText(
			"Example: University of Technology Dresden, CS Department: Longitude: 13.761, Latitude: 51.053");
		d.gridx = 0;
		d.gridy = 4;
		d.weightx = 1;
		d.gridwidth = 3;
		forpanel.setConstraints(pos, d);
		panel1.add(pos);

		map = new JButton("Show on Map");
		map.setToolTipText("Opens a window with a map from www.MapQuest.com " +
						   "of the area around the specified coordinates.");
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
		longi.setName("Description/Location/Position/Geo/Longitude");
		longi.addFocusListener(this);
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
		lati.setName("Description/Location/Position/Geo/Latitude");
		lati.addFocusListener(this);
		lati.setDocument(new FloatDocument( -90, 90));
		lati.setToolTipText("Latitude in degrees. (-90.0: South Pole, 0: Equator, 90.0: North Pole)");
		d.gridx = 1;
		d.weightx = 1;
		d.gridwidth = 3;
		forpanel.setConstraints(lati, d);
		panel1.add(lati);

		panel2 = new JPanel(forpanel);
		panel2.setBorder(new TitledBorder("Operator"));
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(panel2, c);
		add(panel2);

		JLabel op_org = new JLabel("Organisation");
		d.gridx = 0;
		d.gridy = 0;
		d.weightx = 0;
		d.gridwidth = 1;
		d.fill = GridBagConstraints.HORIZONTAL;
		forpanel.setConstraints(op_org, d);
		panel2.add(op_org);

		operatororg = new JTextField(60);
		operatororg.setName("Description/Operator/Organisation");
		operatororg.addFocusListener(this);
		operatororg.setToolTipText(
			"This should contain the operating organisation's or a person's name for private persons.");
		d.gridx = 1;
		d.weightx = 1;
		d.gridwidth = 3;
		forpanel.setConstraints(operatororg, d);
		panel2.add(operatororg);

		JLabel op_url = new JLabel("URL");
		d.gridx = 0;
		d.gridy = 2;
		d.weightx = 0;
		d.gridwidth = 1;
		forpanel.setConstraints(op_url, d);
		panel2.add(op_url);

		operatorurl = new JTextField(20);
		operatorurl.setName("Description/Operator/URL");
		operatorurl.addFocusListener(this);
		operatorurl.setToolTipText("This should contain a URL that will lead to more information about the operator including contact information.");
		d.gridx = 1;
		d.gridwidth = 3;
		d.weightx = 1;
		forpanel.setConstraints(operatorurl, d);
		panel2.add(operatorurl);

		JLabel op_email = new JLabel("EMail");
		d.gridx = 0;
		d.gridy = 3;
		d.weightx = 0;
		d.gridwidth = 1;
		forpanel.setConstraints(op_email, d);
		panel2.add(op_email);

		operatoremail = new JTextField(20);
		operatoremail.setName("Description/Operator/EMail");
		operatoremail.addFocusListener(this);
		operatoremail.setToolTipText(
			"An E-Mail address to which a confirmation message will be sent once the cascade is established.");
		d.gridx = 1;
		d.gridwidth = 3;
		d.weightx = 1;
		forpanel.setConstraints(operatoremail, d);
		panel2.add(operatoremail);
	}

	public void actionPerformed(ActionEvent ae)
	{
		String lat = getConfiguration().getAttribute(
			"Description/Location/Position/Geo/Latitude");
		String lon = getConfiguration().getAttribute(
			"Description/Location/Position/Geo/Longitude");

		try
		{
			if (ae.getActionCommand().equals("Map"))
			{
				box = new MapBox(MixConfig.getMainWindow(), lat, lon, 5);
				box.addActionListener(this);
				box.show();
				map.setText("Update Map");
				map.setActionCommand("Update");
			}
			else if (ae.getActionCommand().equals("Update"))
			{
				box.setGeo(lat, lon);
			}
			else if (ae.getActionCommand().equals("CloseMapBox"))
			{
				box.dispose();
				map.setText("Show on Map");
				map.setActionCommand("Map");
			}
		}
		catch (Exception e)
		{
			MixConfig.handleException(e);
		}
	}

	public Vector check()
	{
		Vector errors = new Vector();
		MixConfiguration mixConf = getConfiguration();

		String names[] =
			{
			"Description/Location/City",
			"Description/Location/Country",
			"Description/Operator/Organisation",
			"Description/Operator/URL",
			"Description/Operator/EMail"
		};

		String messages[] =
			{
			"The city field may not be left blank in Description Panel.",
			"The country field may not be left blank in Description Panel.",
			"The Operator Organisation field may not be left blank in Description Panel.",
			"The Operator URL field may not be left blank in Description Panel.",
			"The Operator E-Mail field may not be left blank in Description Panel.",
		};

		for (int i = 0; i < names.length; i++)
		{
			String value = mixConf.getAttribute(names[i]);
			if (value == null || value.equals(""))
			{
				errors.addElement(messages[i]);
			}
		}

		return errors;
	}

	protected void enableComponents()
	{
	/* all text fields are always enabled */}
}
