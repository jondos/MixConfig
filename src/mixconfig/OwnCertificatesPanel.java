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

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import anon.crypto.PKCS12;
import anon.crypto.X509DistinguishedName;
import anon.crypto.X509Extensions;
import logging.LogType;
import gui.JAPHelp;
import java.awt.Graphics;

public class OwnCertificatesPanel extends MixConfigPanel implements ActionListener,
	ChangeListener
{
	/** A <CODE>CertPanel</CODE> for this Mix's certificate. */
	private CertPanel m_ownCert;

	private CertPanel m_operatorCert;

	private JButton map;
	private JTextField text1, text2, text3, longi, lati;
	private MapBox box;

	public OwnCertificatesPanel(boolean isApplet)
	{
		super("Own Certificates");

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 0;
		c.insets = getDefaultInsets();


		m_ownCert = new CertPanel("Own Mix Certificate",
								  "Hint: You have to sent your public test " +
								  "certificate to the operators of your " +
								  "adjacent mixes", (PKCS12)null);
		m_ownCert.setName("Certificates/OwnCertificate");
		m_ownCert.setCertCreationValidator(new OwnCertCreationValidator());
		m_ownCert.addChangeListener(this);
		add(m_ownCert, c);


		m_operatorCert = new CertPanel("Operator Certificate",
									   "Hint: You have to sent your public test " +
									   "certificate to the operators of your " +
									   "adjacent mixes",
									   (PKCS12)null);
		m_operatorCert.setName("Certificates/OperatorCertificate");
		m_operatorCert.setCertCreationValidator(new OwnCertCreationValidator());
		m_operatorCert.addChangeListener(this);
		c.gridx = 1;
		add(m_operatorCert, c);
		m_operatorCert.setEnabled(false); // disabled at the moment

		TitledGridBagPanel panelLocation = new TitledGridBagPanel("Location");
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(panelLocation, c);

		text1 = new JTextField(15);
		text1.setName("Description/Location/City");
		text1.addFocusListener(this);
		panelLocation.addRow(new JLabel("City"), text1, GridBagConstraints.HORIZONTAL);

		text2 = new JTextField(15);
		text2.setName("Description/Location/Country");
		text2.addFocusListener(this);
		panelLocation.addRow(new JLabel("Country"), text2, GridBagConstraints.HORIZONTAL);

		text3 = new JTextField(15);
		text3.setName("Description/Location/State");
		text3.addFocusListener(this);
		panelLocation.addRow(new JLabel("State"), text3, GridBagConstraints.HORIZONTAL);

	/**
		JLabel pos = new JLabel("Geographical Position");
		pos.setToolTipText(
				  "Example: University of Technology Dresden, CS Department: Longitude: 13.761, Latitude: 51.053");
		panelLocation.addRow(pos, null);**/

		longi = new JTextField(7);
		longi.setName("Description/Location/Position/Geo/Longitude");
		longi.addFocusListener(this);
		longi.setDocument(new FloatDocument( -180, 180));
		longi.setToolTipText("Longitude in degrees east from Greenwich. ( -180.0 to 180.0)");
		panelLocation.addRow(new JLabel("Longitude"), longi);

		lati = new JTextField(7);
		lati.setName("Description/Location/Position/Geo/Latitude");
		lati.addFocusListener(this);
		lati.setDocument(new FloatDocument( -90, 90));
		lati.setToolTipText("Latitude in degrees. (-90.0: South Pole, 0: Equator, 90.0: North Pole)");
		panelLocation.addRow(new JLabel("Latitude"), lati);

		map = new JButton("Show on Map");
		map.setToolTipText("Opens a window with a map from www.MapQuest.com " +
						   "of the area around the specified coordinates.");
		map.addActionListener(this);
		map.setActionCommand("Map");
		map.setEnabled(!isApplet);
		panelLocation.addRow(map, null);




		JTextField operatororg, operatorurl, operatoremail;
		TitledGridBagPanel panelOperator = new TitledGridBagPanel("Operator");
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0;
		c.weighty = 0;
		add(panelOperator, c);


		operatororg = new JTextField(20);
		operatororg.setName("Description/Operator/Organisation");
		operatororg.addFocusListener(this);
		operatororg.setToolTipText(
			"This should contain the operating organisation's or a person's name for private persons.");
		panelOperator.addRow(new JLabel("Organisation"), operatororg);

		operatorurl = new JTextField(20);
		operatorurl.setName("Description/Operator/URL");
		operatorurl.addFocusListener(this);
		operatorurl.setToolTipText("This should contain a URL that will lead to more information about the operator including contact information.");
		panelOperator.addRow(new JLabel("URL"), operatorurl);

		operatoremail = new JTextField(20);
		operatoremail.setName("Description/Operator/EMail");
		operatoremail.addFocusListener(this);
		operatoremail.setToolTipText(
			"An E-Mail address to which a confirmation message will be sent once the cascade is established.");
		panelOperator.addRow(new JLabel("E-Mail"), operatoremail);


        //Keep the panels in place
		JLabel dummyLabel1 = new JLabel("");
		c.gridx = 2;
		c.gridy = 2;
		c.weightx = 5;
		c.weighty = 5;
		c.fill = c.BOTH;
		this.add(dummyLabel1, c);

	}

	public void actionPerformed(ActionEvent ae)
	{
		String lat = getConfiguration().getValue(
			"Description/Location/Position/Geo/Latitude");
		String lon = getConfiguration().getValue(
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
			MixConfig.handleError(e, null, LogType.GUI);
		}
	}

	public void paint(Graphics g)
	{
		super.paint(g);
		JAPHelp.getInstance().getContextObj().setContext("index");
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
		//	"Description/Operator/URL",
			"Description/Operator/EMail"
		};

		String messages[] =
			{
			"The city field may not be left blank in " + getName() + " panel.",
			"The country field may not be left blank in " + getName() + " panel.",
			"The Operator Organisation field may not be left blank in " + getName() + " panel.",
		//	"The Operator URL field may not be left blank in " + getName() + " panel.",
			"The Operator E-Mail field may not be left blank in " + getName() + " panel.",
		};

		for (int i = 0; i < names.length; i++)
		{
			String value = mixConf.getValue(names[i]);
			if (value == null || value.equals(""))
			{
				errors.addElement(messages[i]);
			}
		}

		if (this.m_ownCert.getCert() == null)
		{
			errors.addElement(
				"Own Mix Certificate is missing in " + getName() + " panel.");
		}


		return errors;
	}

	public void stateChanged(ChangeEvent e)
	{
		try
		{
			if (e.getSource() instanceof CertPanel)
			{
				save( (CertPanel) e.getSource());
				updateDeprecatedMixID();
			}
		}
		catch (Exception ex)
		{
			MixConfig.handleError(ex, null, LogType.GUI);
		}
	}


	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		// first enable all components to make MixConfigPanel load their data
		enableComponents();

		super.setConfiguration(a_conf);

		// make sure this panel is contained only once in the config's listeners list
		a_conf.removeChangeListener(this);
		a_conf.addChangeListener(this);


		updateDeprecatedMixID();

		enableComponents();
	}





	private class OwnCertCreationValidator implements ICertCreationValidator
	{
		private String m_invalidity = "";

		public boolean isValid()
		{
			boolean valid = true;

			m_invalidity = "";
			if (text1.getText() == null || text1.getText().trim().equals(""))
			{
				m_invalidity += "City not valid!\n";
				valid = false;
			}

			if (text2.getText() == null || text2.getText().trim().equals(""))
			{
				m_invalidity += "Country not valid!\n";
				valid = false;
			}

			return valid;

		}

		/**
		 * @todo not implemented
		 * @return X509DistinguishedName
		 */
		public X509DistinguishedName getSigName()
		{
			return new X509DistinguishedName("CN=");
		}

		/**
		 * @todo not implemented
		 * @return X509Extensions
		 */
		public X509Extensions getExtensions()
		{
			return null;
		}


		public String getPasswordInfoMessage()
		{
			return "This password has to be entered every time the Mix " +
				"server starts.\nSo if you want to start it automatically " +
				"you shouldn't enter a password.";
		}

		public String getInvalidityMessage()
		{
			return m_invalidity;
	}
}

	protected void enableComponents()
	{
		/* all text fields are always enabled */
	}

	/**
	 * @todo remove hack if mix/infoservice/jap has been rewritten accordingly
	 * Hack: update the MixID each time the private mix certificate is saved
	 */
	private void updateDeprecatedMixID()
	{
		if (m_ownCert == null || m_ownCert.getCert() == null)
		{
			save("General/MixID", null);
		}
		else
		{
			String cn = ((PKCS12) m_ownCert.getCert()).getSubject().getCommonName();
			try
			{
				String temp = cn.substring( (cn.indexOf("id=") + 3), cn.indexOf("/>"));
				StringTokenizer tokenizer = new StringTokenizer(temp, "\"");
				cn = tokenizer.nextToken();
			}
			catch(Exception a_e)
			{
			}

			save("General/MixID", cn);
		}
	}
}
