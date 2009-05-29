/*
Copyright (c) 2008 The JAP-Team, JonDos GmbH

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
       this list of conditions and the following disclaimer in the documentation and/or
       other materials provided with the distribution.
    * Neither the name of the University of Technology Dresden, Germany, nor the name of
       the JonDos GmbH, nor the names of their contributors may be used to endorse or
       promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package mixconfig.panels;

import gui.GUIUtils;
import gui.TitledGridBagPanel;
import gui.dialog.JAPDialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.InetAddress;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;
import gui.ButtonConstants;

import anon.util.JAPMessages;
import anon.util.XMLUtil;

public class LogCrimePanel extends MixConfigPanel implements ActionListener, KeyListener
{

	private static final String MSG_CRIME_TITLE = LogCrimePanel.class.getName() + "_title";
	private static final String MSG_URL_PANEL_NAME = LogCrimePanel.class.getName() + "_urlPanelName";
	private static final String MSG_PAYLOAD_PANEL_NAME = LogCrimePanel.class.getName() + "_payloadPanelName";
	private static final String MSG_IP_PANEL_NAME = LogCrimePanel.class.getName() + "_ipPanelName";
	private static final String MSG_INVALID_REGEXP = LogCrimePanel.class.getName() + "_invalidRegExp";
	private static final String MSG_INVALID_IP = LogCrimePanel.class.getName() + "_invalidIP";
	
	private static final String XML_ELEMENT_CRIME_DETECTION = "CrimeDetection";
	private static final String XML_ELEMENT_REG_EXP_URL = "RegExpURL";
	private static final String XML_ELEMENT_REG_EXP_PAYLOAD = "RegExpPayload";
	private static final String XML_ELEMENT_SURVEILLANCE_IP = "SurveillanceIP";
	
	private static final int URL_PANEL = 0;
	private static final int PAYLOAD_PANEL = 1;
	private static final int IP_PANEL = 2;
	private static final int PANELS = 3;
	
	private final static int ADD = 0;
	private final static int REMOVE = 1;
	
	private final static String[] XML_ELEMENT_NAMES =
		new String[]{XML_ELEMENT_REG_EXP_URL, XML_ELEMENT_REG_EXP_PAYLOAD, XML_ELEMENT_SURVEILLANCE_IP};
	
	private static final String[] PANEL_NAME_KEYS = 
		new String[]{MSG_URL_PANEL_NAME, MSG_PAYLOAD_PANEL_NAME, MSG_IP_PANEL_NAME};
	
	private static final String[] PANEL_INPUT_ERROR_KEYS = 
		new String[]{MSG_INVALID_REGEXP, MSG_INVALID_REGEXP, MSG_INVALID_IP};
	
	private JButton[] addButtons;
	private JButton[] removeButtons;
	private JComboBox[] parameterBoxes;
	private JTextComponent[] inputComponents;
	private JPanel[] surveillanceParameterPanels;
	private DataRetentionPanel dataRetentionPanel;
	
	private InputChecker[] inputChecker;
	
	
	
	private static LogCrimePanel panelSingleton = null;
	
	public static LogCrimePanel get()
	{
		if(panelSingleton == null)
		{
			panelSingleton = new LogCrimePanel();
		}
		return panelSingleton;
	}
	
	protected LogCrimePanel() 
	{
		super(JAPMessages.getString(MSG_CRIME_TITLE));
		initComponents();
		placeComponents();
		enableComponents();
	}

	private void initComponents()
	{
		addButtons = new JButton[PANELS];
		removeButtons = new JButton[PANELS];
		parameterBoxes = new JComboBox[PANELS];
		inputComponents = new JTextComponent[PANELS];
		surveillanceParameterPanels = new JPanel[PANELS];
		inputChecker = new InputChecker[PANELS];
		
		for (int i = 0; i < PANELS; i++)
		{
			addButtons[i] = GUIUtils.createButton(ButtonConstants.ADD);
			addButtons[i].addActionListener(this);
			
			removeButtons[i] = GUIUtils.createButton(ButtonConstants.REMOVE);
			removeButtons[i].addActionListener(this);
			
			inputComponents[i] = new JTextField(50);
			inputComponents[i].addKeyListener(this);
			
			parameterBoxes[i] = new JComboBox();
			parameterBoxes[i].addItemListener(this);
			
			surveillanceParameterPanels[i] =
				createSurveillanceParameterPanel(PANEL_NAME_KEYS[i], parameterBoxes[i], removeButtons[i], 
						inputComponents[i], addButtons[i]);
			
			inputChecker[i] = (i == IP_PANEL) ? 
					new InputChecker()
					{ 
						public boolean checkInput(String input)
						{
							return checkIP(input);
						}
					} :
					new InputChecker()
					{ 
						public boolean checkInput(String input)
						{
							return checkRegExp(input);
						}
					};
		}
		
		dataRetentionPanel = new DataRetentionPanel(this);
		//dataRetentionPanel.setEnabled(false);
	}
	
	void placeComponents()
	{
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.weighty = 1;
		c.weightx = 1;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(3, 3, 3, 3);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.BOTH;
		//c.gridwidth = 1;
		for (c.gridx = 0; c.gridx < surveillanceParameterPanels.length; c.gridx++) 
		{
			add(surveillanceParameterPanels[c.gridx], c);
		}
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(dataRetentionPanel, c);
	}
	
	public Vector<String> check() 
	{
		return new Vector<String>();
	}

	
	protected void enableComponents() 
	{
		for(int i = 0; i < addButtons.length; i++)
		{
			addButtons[i].setEnabled(inputChecker[i].checkInput((inputComponents[i].getText())));
		}
		
		for(int i = 0; i < removeButtons.length; i++)
		{
			removeButtons[i].setEnabled(
					parameterBoxes[i].getSelectedItem() != null);
		}
	}


	public String getHelpContext() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Component getHelpExtractionDisplayContext() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	private static JPanel createSurveillanceParameterPanel(String nameKey,
			JComboBox parameters, JButton removeButton, JTextComponent inputComponent, JButton addButton)
	{
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.weighty = 0.1;
		c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(3, 3, 3, 3);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		JPanel surveillanceParameterPanel = new JPanel();
		surveillanceParameterPanel.setBorder(new TitledBorder(JAPMessages.getString(nameKey)));
		surveillanceParameterPanel.setLayout(new GridBagLayout());
		surveillanceParameterPanel.add(parameters, c);
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		c.gridy++;
		surveillanceParameterPanel.add(inputComponent, c);
		
		c.gridy++;
		c.weighty = 0.1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		surveillanceParameterPanel.add(addButton, c);
		c.gridx++;
		surveillanceParameterPanel.add(removeButton, c);
		
		return surveillanceParameterPanel;
	}

	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) 
	{
		enableComponents();
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		int actionType = 0;
		int panelType = -1;
		JButton[] currentArray = null;
		for (;actionType <= REMOVE; actionType++) 
		{
			currentArray = (actionType == ADD) ? addButtons : removeButtons;
			for (int i = 0; i < currentArray.length; i++) 
			{
				if(e.getSource() == currentArray[i])
				{
					if(actionType == ADD) addInput(i);
					else removeSelectedParameter(i);
					enableComponents();
					saveAfterAction();
					return;
				}
			}
		}
	}
	
	private void addInput(int panelIndex)
	{
		String input = inputComponents[panelIndex].getText();
		if(inputChecker[panelIndex].checkInput(input))
		{
			updateParameterBox(input.trim(), panelIndex);
		}
		else
		{
			JAPDialog.showErrorDialog(this, 
					JAPMessages.getString(
							PANEL_INPUT_ERROR_KEYS[panelIndex], input), LogType.MISC);
		}
		//inputComponents[boxIndex].setText("");
	}
	
	private void removeSelectedParameter(int panelIndex)
	{
		Object item = parameterBoxes[panelIndex].getSelectedItem();
		if(item != null)
		{
			parameterBoxes[panelIndex].removeItemAt(
					parameterBoxes[panelIndex].getSelectedIndex());
		}
	}
	
	private void updateParameterBox(String newItem, int boxIndex)
	{
		Object item = null;
		for (int i = 0; i < parameterBoxes[boxIndex].getItemCount(); i++) 
		{
			item = parameterBoxes[boxIndex].getItemAt(i);
			if((item != null) && (item instanceof String) &&
				item.equals(newItem))
			{
				parameterBoxes[boxIndex].setSelectedIndex(i);
				return;
			}
		}
		parameterBoxes[boxIndex].addItem(newItem);
		parameterBoxes[boxIndex].setSelectedItem(newItem);
	}
	
	private boolean checkRegExp(String regExp)
	{
		if((regExp == null) || regExp.equals(""))
		{
			return false;
		}
		
		try
		{
			Pattern p = Pattern.compile(regExp.trim());
		}
		catch(PatternSyntaxException e)
		{
			return false;
		}
		return true;
	}
	
	private boolean checkIP(String ip)
	{
		if(ip == null) return false;
		StringTokenizer tokenizer = new StringTokenizer(ip.trim(), ".");
		if(tokenizer.countTokens() != 4) return false;
		while(tokenizer.hasMoreTokens())
		{
			try
			{
				int i = Integer.parseInt(tokenizer.nextToken());
				if(i > 255 || i < 0) return false;
			}
			catch(NumberFormatException nfe)
			{
				return false;
			}
		}
		return true;
	}
	
	private interface InputChecker
	{
		public boolean checkInput(String input);
	}

	
	public void load() throws IOException 
	{
		super.load(dataRetentionPanel);
		if( (getConfiguration() != null) && 
			(getConfiguration().getDocument() != null) &&
			(getConfiguration().getDocument().getDocumentElement() != null) )
		{
			Element crimeDetectionRoot = (Element) XMLUtil.getFirstChildByName(
					getConfiguration().getDocument().getDocumentElement(), 
					XML_ELEMENT_CRIME_DETECTION);
				
			if(crimeDetectionRoot != null)
			{
				NodeList crimeDetectionElements = null;
				String textContent = null;
				for (int panelIndex = 0; panelIndex < PANELS; panelIndex++)
				{
					crimeDetectionElements =
						crimeDetectionRoot.getElementsByTagName(XML_ELEMENT_NAMES[panelIndex]);
					
					for (int i = 0; i < crimeDetectionElements.getLength(); i++) 
					{
						textContent = crimeDetectionElements.item(i).getTextContent();
						if( (textContent != null) && 
							!textContent.equals(""))
						{
							textContent = textContent.trim();
							if(inputChecker[panelIndex].checkInput(textContent))
							{
								updateParameterBox(textContent, panelIndex);
							}
							else
							{
								LogHolder.log(LogLevel.WARNING, LogType.MISC,
									"The parameter "+textContent+" is invalid and therefore not loaded.");
							}
						}
					}
				}
			}
		}
	}

	private void saveAfterAction()
	{
		try 
		{
			save();
		} 
		catch (IOException e) 
		{
			LogHolder.log(LogLevel.WARNING, LogType.MISC,
					"Saving crime detection options impossible due to an IOException.",e);
		}
	}
	
	public void save() throws IOException 
	{
		
		Document configDoc =
			(getConfiguration() != null) ? getConfiguration().getDocument() : null;
		
		if( (configDoc != null) && 
			(configDoc.getDocumentElement() != null) )
		{
			Element crimeDetectionRoot = 
				configDoc.createElement(XML_ELEMENT_CRIME_DETECTION);
			String textContent = null;
			Object selectedItem = null;
			
			for (int panelIndex = 0; panelIndex < PANELS; panelIndex++)
			{
				for (int i = 0; i < parameterBoxes[panelIndex].getItemCount(); i++) 
				{
					selectedItem = parameterBoxes[panelIndex].getItemAt(i);
					if((selectedItem != null) && 
						(selectedItem instanceof String) )
					{
						Element parameterElement = 
							configDoc.createElement(XML_ELEMENT_NAMES[panelIndex]);
						parameterElement.setTextContent(((String)selectedItem).trim());
						crimeDetectionRoot.appendChild(parameterElement);
					}
				}
			}
			
			NodeList nl = configDoc.getElementsByTagName(XML_ELEMENT_CRIME_DETECTION);
			
			Element oldCrimeDetectionRoot = (nl.getLength() > 0) ? (Element) nl.item(0) : null;
			if(crimeDetectionRoot.hasChildNodes())
			{
				if(oldCrimeDetectionRoot != null)
				{
					oldCrimeDetectionRoot.getParentNode().replaceChild(crimeDetectionRoot, oldCrimeDetectionRoot);
				}
				else
				{
					configDoc.getDocumentElement().appendChild(crimeDetectionRoot);
				}
			}
			else
			{
				if(oldCrimeDetectionRoot != null)
				{
					oldCrimeDetectionRoot.getParentNode().removeChild(oldCrimeDetectionRoot);
				}
			}
		}	
	}

	public void itemStateChanged(ItemEvent ie) 
	{
		if(ie.getStateChange() == ItemEvent.SELECTED)
		{
			enableComponents();
		}
	}

	public void focusGained(FocusEvent e) {}
	public void focusLost(FocusEvent e) {}
}
