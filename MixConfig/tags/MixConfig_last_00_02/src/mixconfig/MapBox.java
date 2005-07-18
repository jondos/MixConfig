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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DocumentParser;

/** This class provides a dialog showing a map section loaded from MapQuest(R)
 * according to the specified latitude and longitude.
 */

class MapBox extends JDialog implements ChangeListener
{
	public static final int BOLD = 3;
        /** The URL pointing to the real map image */
	private String m_urlString;
        /** The label containing the map in form of an <CODE>ImageIcon</CODE> */
	private JLabel map;
	private JSlider s;
	private JButton b;
        /** The longitude of the center of the map */
	private String m_longitude;
        /** The latitude of the center of the map */
	private String m_latitude;

        /** Constructs a new <CODE>MapBox</CODE> instance.
         * @param parent The parent of the dialog window
         * @param lat The latitude of the point to show on the map
         * @param lon The longitude of the point to show on the map
         * @param level The zoom level to be set (0 - 9)
         * @throws Exception If an error occurs
         */
	public MapBox(Frame parent, String lat, String lon, int level) throws Exception
	{
		super(parent, "", false);
		setBackground(Color.white);
		m_longitude = lon;
		m_latitude = lat;

// will not work on JDK < 1.2
//		this.setLocation(MixConfig.getMainWindow().getX() + 15,
//						 MixConfig.getMainWindow().getY() + 15);

		this.setLocation(MixConfig.getMainWindow().getLocation().x + 15,
						 MixConfig.getMainWindow().getLocation().y + 15);

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		getContentPane().setLayout(layout);
		getContentPane().setBackground(Color.white);
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 10, 10, 10);
		map = new JLabel();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 2;
		layout.setConstraints(map, c);
		getContentPane().add(map);

		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.insets = new Insets(20, 10, 5, 10);
		JLabel l = new JLabel("Zoom");
		layout.setConstraints(l, c);
		getContentPane().add(l);

		s = new JSlider(JSlider.VERTICAL, 1, 9, level);
		s.setBackground(Color.white);
		s.setPaintTicks(true);
		s.setMajorTickSpacing(1);
		s.setMinorTickSpacing(1);
		s.setSnapToTicks(true);
		s.setPaintLabels(true);
		s.setRequestFocusEnabled(false);
		s.addChangeListener(this);
		c.insets = new Insets(5, 10, 20, 10);
		c.gridx = 2;
		c.gridy = 1;
		c.fill = GridBagConstraints.VERTICAL;
		layout.setConstraints(s, c);
		getContentPane().add(s);

		Font font = new Font("Dialog", Font.BOLD, 20);
		JLabel site = new JLabel("PROCESSED BY:");
		site.setFont(font);
		c.insets = new Insets(10, 10, 10, 10);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		layout.setConstraints(site, c);
		getContentPane().add(site);

		try
		{
			//String logo = "http://art.mapquest.com/mqsite_english/logo";
			//URL MapLogo = new URL(logo);
			ImageIcon maplogo = MixConfig.loadImage("mapquest-logo.gif");
			JLabel logolabel = new JLabel(maplogo);
			c.gridx = 1;
			c.gridy = 2;
			layout.setConstraints(logolabel, c);
			getContentPane().add(logolabel);
		}
		catch (Exception e)
		{
			// FIXME: Dumping exception instead of throwing is a Bad Thing
			e.printStackTrace();
		}

		b = new JButton("Close");
		c.gridx = 2;
		c.gridy = 2;
		layout.setConstraints(b, c);
		getContentPane().add(b);
		refresh();
		pack();
	}

        /** Sets the coordinates to be displayed on the map.
         * @param a_latitude The latitude
         * @param a_longitude The longitude
         * @throws IOException If an error occurs while reading the map from www.mapquest.com
         */
	public void setGeo(String a_latitude, String a_longitude) throws IOException
	{
		m_longitude = a_longitude;
		m_latitude = a_latitude;
		refresh();
	}

	public void addActionListener(ActionListener a_al)
	{
		b.addActionListener(a_al);
		b.setActionCommand("CloseMapBox");
	}

	public void stateChanged(ChangeEvent e)
	{
		try
		{
			JSlider s1 = (JSlider) e.getSource();
			if (!s1.getValueIsAdjusting())
			{
				refresh();
			}
		}
		catch (IOException ioe)
		{
			MixConfig.handleException(ioe);
		}
	}

        /** Contacts <a href="http://www.mapquest.com">MapQuest</a> to re-load the map.
         * To retrieve the map image, it is necessary to submit a HTTP Get request to the
         * web site, parse the returned HTML page and search for the image.<br>
         * As of June 2004, the HTML page contains the following tag:<br>
         * <br>
         * &lt;INPUT TYPE=&quot;IMAGE&quot; NAME=&quot;mqmap&quot;
         * SRC=&quot;&lt;MAP-URL&gt;&quot;&gt;<br>
         * <br>
         * The SRC= attribute contains the real URL of the map image.
         * @throws IOException If an error occurs while retrieving the web site
         */
	private void refresh() throws IOException
	{
		BufferedReader bissmall;
		String Title = "";

		map.setIcon(null);
		map.setText("Please wait ...");
		map.repaint();

		String site = "http://www.mapquest.com/maps/map.adp?latlongtype=" +
			"decimal&latitude=" + m_latitude + "&longitude=" + m_longitude +
			"&zoom=" + s.getValue();

		Title = "The location shown on the Map is :  Latitude = " +
			m_latitude + "  Longitude = " + m_longitude;

		setTitle(Title);
		URL urlsmall = new URL(site);
		bissmall = new BufferedReader(new InputStreamReader(urlsmall.openStream()), 1024);
		MapQuestSiteParser mqsp = new MapQuestSiteParser();
		DocumentParser dp = new DocumentParser(DTD.getDTD("-//W3C//DTD HTML 4.01 Transitional//EN"));

		m_urlString = null;

		dp.parse(bissmall, mqsp, true);

		if (m_urlString == null)
		{
			throw new IOException("Image reference not found on site " + site);
		}

		map.setText("");
		map.setIcon(new ImageIcon(new URL(m_urlString)));
	}

        /** A subclass of <CODE>javax.swing.text.html.HTMLEditorKit.ParserCallback</CODE>
         * that parses the HTML page downloaded from www.mapquest.com and searches for the
         * URL of the map image.
         */
	private class MapQuestSiteParser extends ParserCallback
	{
		public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos)
		{
			handleStartTag(t, a, pos);
		}

		public void handleStartTag(Tag t, MutableAttributeSet a, int pos)
		{
			String type = null, name = null, src = null;
			if (t == Tag.INPUT)
			{
				try
				{
					type = a.getAttribute(Attribute.TYPE).toString();
					name = a.getAttribute(Attribute.NAME).toString();
					if (type.equals("image") && name.equals("mqmap"))
					{
						src = a.getAttribute(Attribute.SRC).toString();
						MapBox.this.m_urlString = src;
					}
				}
				catch (NullPointerException npe)
				{
					/* IGNORE */
				}
			}
		}
	}
}