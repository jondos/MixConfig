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

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.TreeMap;

import gui.MixConfigTextField;
import gui.TitledGridBagPanel;
import gui.dialog.DialogContentPane;
import gui.dialog.JAPDialog;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JLabel;

import logging.LogType;

import anon.terms.TCComponent;
import anon.terms.TCComposite;
import anon.terms.TermsAndConditionsTranslation;
import anon.terms.template.Paragraph;
import anon.terms.template.Section;
import anon.terms.template.TermsAndConditionsTemplate;
import anon.util.JAPMessages;

/**
 * Dialog for the TC content management
 * TODO: still under construction 
 * @author simon
 *
 */
public class TermsAndConditionsContentDialog extends JAPDialog 
	implements ItemListener, ActionListener, FocusListener
{
	//View
	private JButton sectionAddButton;
	private JButton sectionDeleteButton;
	private JButton sectionResetButton;
	
	private JButton paragraphAddButton;
	private JButton paragraphDeleteButton;
	private JButton paragraphResetButton;
	
	private JButton okButton;
	private JButton cancelButton;
	
	private JComboBox sectionChoice;
	private JComboBox paragraphChoice;
	
	private JTextField sectionNameTemplate;
	private JTextArea templateText;
	
	private JTextField sectionNameTranslation;
	private JTextArea translationText;
	
	private TitledGridBagPanel sectionPanel;
	private TitledGridBagPanel paragraphPanel;
	private TitledGridBagPanel templateContentPanel;
	private TitledGridBagPanel customizedContentPanel;
	
	//Model
	private TCComposite templateSections = null;
	private TCComposite translationSections = null;
	
	public TermsAndConditionsContentDialog(Component parent, TermsAndConditionsTemplate template,
			TermsAndConditionsTranslation translation) 
	{
		super(parent, JAPMessages.getString(TermsAndConditionsPanel.MSG_CONTENT), true);
		setDefaultCloseOperation(JAPDialog.DISPOSE_ON_CLOSE);
		//setResizable(false);
		initializeComponents();
		placeComponents();
		
		templateSections = template.getSections();
		translationSections = translation.getSections();
		loadSectionList();
	}

	private void initializeComponents()
	{
		//create components
		sectionAddButton = new JButton(JAPMessages.getString(TermsAndConditionsPanel.MSG_ADD));
		sectionDeleteButton = new JButton(JAPMessages.getString(TermsAndConditionsPanel.MSG_DELETE));
		sectionResetButton = new JButton(JAPMessages.getString(TermsAndConditionsPanel.MSG_RESET));
		
		paragraphAddButton = new JButton(JAPMessages.getString(TermsAndConditionsPanel.MSG_ADD));
		paragraphDeleteButton = new JButton(JAPMessages.getString(TermsAndConditionsPanel.MSG_DELETE));
		paragraphResetButton = new JButton(JAPMessages.getString(TermsAndConditionsPanel.MSG_RESET));
		
		okButton = new JButton(JAPMessages.getString(DialogContentPane.MSG_OK));
		cancelButton = new JButton(JAPMessages.getString(DialogContentPane.MSG_CANCEL));
		
		sectionChoice = new JComboBox();
		paragraphChoice = new JComboBox();
		
		sectionNameTemplate = new MixConfigTextField(20);
		templateText = new JTextArea(7,50);
		
		sectionNameTranslation = new MixConfigTextField(20);
		translationText = new JTextArea(7,50);
		
		sectionPanel = createTCComponentPanel(TermsAndConditionsPanel.MSG_SECTION, 
					sectionChoice, sectionAddButton, sectionDeleteButton, sectionResetButton);
		paragraphPanel = createTCComponentPanel(TermsAndConditionsPanel.MSG_PARAGRAPH, 
					paragraphChoice, paragraphAddButton, paragraphDeleteButton, paragraphResetButton);
		templateContentPanel = createContentPanel(TermsAndConditionsPanel.MSG_TEMPLATE_LABEL, 
					sectionNameTemplate, templateText);
		customizedContentPanel = createContentPanel(TermsAndConditionsPanel.MSG_TRANSLATION_LABEL, 
					sectionNameTranslation, translationText);
		
		//initialize listeners
		sectionChoice.addItemListener(this);
		paragraphChoice.addItemListener(this);
		
		sectionAddButton.addActionListener(this);
		sectionDeleteButton.addActionListener(this);
		sectionResetButton.addActionListener(this);
		paragraphAddButton.addActionListener(this);
		paragraphDeleteButton.addActionListener(this);
		paragraphResetButton.addActionListener(this);
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		sectionNameTranslation.addFocusListener(this);
		translationText.addFocusListener(this);
		
		sectionNameTemplate.setEditable(false);
		templateText.setEditable(false);
		
		templateText.setLineWrap(true);
		translationText.setLineWrap(true);
		
	}
	
	private void placeComponents()
	{
		Container contentPane = getContentPane();
		GridBagLayout gridbag = new GridBagLayout();
		
		GridBagConstraints c = new GridBagConstraints();
		contentPane.setLayout(gridbag);
		
		JPanel buttonPanel = new JPanel();
		
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		
		c.gridheight = 1;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(3, 3, 3, 3);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.BOTH;
		contentPane.add(sectionPanel, c);
		
		c.gridx = 1;
		contentPane.add(paragraphPanel, c);
		
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 1;
		contentPane.add(templateContentPanel, c);
		
		c.gridy = 2;
		contentPane.add(customizedContentPanel, c);
		
		c.gridy = 3;
		contentPane.add(buttonPanel, c);
	}
	
	public void loadSectionList()
	{
		loadSectionList(null);
	}
	
	public void loadSectionList(TCComponent selected)
	{
		sectionChoice.removeAllItems();
		TCComponent[] fromTemplate = (templateSections != null) ? templateSections.getTCComponents() : null;
		TCComponent[] fromTrans = (translationSections != null) ? translationSections.getTCComponents() : null;
		
		TCComponentListItem selectedItem = null;
		
		Collection<TCComponentListItem> items = TCComponentListItem.createList(fromTemplate, fromTrans);
		for (TCComponentListItem item : items) 
		{
			if(	(selected != null) && 
				(item.getTranslationComponent() != null) &&
				(item.getTranslationComponent().equals(selected)) )
			{
				selectedItem = item;
			}
			sectionChoice.addItem(item);
		}
		if(selectedItem != null)
		{
			sectionChoice.setSelectedItem(selectedItem);
		}
	}
	
	public void itemStateChanged(ItemEvent e) 
	{
		if( e.getStateChange() == ItemEvent.SELECTED)
		{
			if(e.getSource() == sectionChoice)
			{
				paragraphChoice.removeAllItems();
				Collection<TCComponentListItem> items = 
					TCComponentListItem.createList(
							getParagraphsOfSelectedTemplateSection(), 
							getParagraphsOfSelectedTranslationSection());
				for (TCComponentListItem item : items) 
				{
					paragraphChoice.addItem(item);
				}
			}
			/*else if(e.getSource() == paragraphChoice)
			{
				
			}*/
			Section templateSect = getSelectedTemplateSection();
			Section transSect = getSelectedTranslationSection();
			
			Paragraph templatePar = getSelectedTemplateParagraph();
			Paragraph transPar = getSelectedTranslationParagraph();
			
			templateText.setText((templatePar != null) ? templatePar.toString().trim() : "");
			translationText.setText((transPar != null) ? transPar.toString().trim() : "");
			
			sectionNameTemplate.setText(
					(templateSect != null) && templateSect.hasContent() && (templateSect.getContent() != null) ? 
							templateSect.getContent().toString().trim() : "");
			sectionNameTranslation.setText(
					(transSect != null) && (transSect.hasContent()) && (transSect.getContent() != null) ? 
							transSect.getContent().toString().trim() : "");
		}
	}

	//TODO implement actions
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == sectionAddButton)
		{
			actionAddSection();
		}
		else if(e.getSource() == sectionDeleteButton)
		{
			
		}
		else if(e.getSource() == sectionResetButton)
		{
			
		}
		else if(e.getSource() == paragraphAddButton)
		{
			//TODO: same as above
			Paragraph p = AddComponentDialog.showAddParagraphDialog(this.getContentPane());
		}
		else if(e.getSource() == paragraphDeleteButton)
		{
			
		}
		else if(e.getSource() == paragraphResetButton)
		{
			
		}
		else if(e.getSource() == okButton)
		{
			
		}
		else if(e.getSource() == cancelButton)
		{
			dispose();
		}
	}
	
	private Section getSelectedTemplateSection()
	{
		TCComponentListItem item = getSelectedListItem(sectionChoice);
		return (Section) ((item != null) ? item.getTemplateComponent() : null);
	}
	
	private TCComponent[] getParagraphsOfSelectedTemplateSection()
	{
		Section selectedTemplateSection = 
				getSelectedTemplateSection();
		return (selectedTemplateSection != null) ? 
				selectedTemplateSection.getTCComponents() : null;
	}
	
	private Section getSelectedTranslationSection()
	{
		TCComponentListItem item = getSelectedListItem(sectionChoice);
		return (Section) ((item != null) ? item.getTranslationComponent() : null);
	}
	
	private TCComponent[] getParagraphsOfSelectedTranslationSection()
	{
		Section selectedTranslationSection = 
				getSelectedTranslationSection();
		return (selectedTranslationSection != null) ? 
				selectedTranslationSection.getTCComponents() : null;
	}
	
	private Paragraph getSelectedTemplateParagraph()
	{
		TCComponentListItem item = getSelectedListItem(paragraphChoice);
		return (Paragraph) ((item != null) ? item.getTemplateComponent() : null);
	}
	
	private Paragraph getSelectedTranslationParagraph()
	{
		TCComponentListItem item = getSelectedListItem(paragraphChoice);
		return (Paragraph) ((item != null) ? item.getTranslationComponent() : null);
	}
	
	private static TCComponentListItem getSelectedListItem(JComboBox comboBox)
	{
		Object item = comboBox.getSelectedItem();
		return (TCComponentListItem) ((item instanceof TCComponentListItem) ? item : null);
	}
	
	private static TitledGridBagPanel 
		createTCComponentPanel(String nameKey, JComboBox choiceBox, JButton addButton, JButton deleteButton, JButton resetButton)
	{
		TitledGridBagPanel tcPanel = new TitledGridBagPanel(JAPMessages.getString(nameKey));
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(resetButton);
		tcPanel.addRow(new Component[]{choiceBox}, new int[]{GridBagConstraints.REMAINDER});
		tcPanel.addRow(new Component[]{buttonPanel}, new int[]{GridBagConstraints.REMAINDER});
		return tcPanel;
	}
	
	private static TitledGridBagPanel createContentPanel(String contentSourceNameKey,
			JTextField sectionNameTF, JTextArea paragraphTextArea)
	{
		TitledGridBagPanel contentPanel = 
			new TitledGridBagPanel(
					JAPMessages.getString(TermsAndConditionsPanel.MSG_CONTENT)+"/"+
					JAPMessages.getString(contentSourceNameKey));
		contentPanel.addRow(new Component[]{
				TermsAndConditionsPanel.createLabel(TermsAndConditionsPanel.MSG_SECTION), 
				sectionNameTF}, 
				new int[]{1, GridBagConstraints.REMAINDER});
		contentPanel.addRow(new Component[]{
				TermsAndConditionsPanel.createLabel(TermsAndConditionsPanel.MSG_PARAGRAPH), 
				new JScrollPane(paragraphTextArea, 
						JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)}, 
				new int[]{1, GridBagConstraints.REMAINDER});
		return contentPanel;
	}
	
	private static class TCComponentListItem
	{
		public final static String STATUS_EDITED = "(Edited)";
		public final static String STATUS_DELETED = "(Deleted)";
		public final static String STATUS_NEW = "(New)";
		
		double id = 0;
		TCComponent templateComponent = null;
		TCComponent translationComponent = null;
		
		public TCComponentListItem()
		{}

		public TCComponentListItem(TCComponent templateComponent,
				TCComponent translationComponentType) 
		{
			this.templateComponent = templateComponent;
			this.translationComponent = translationComponentType;
		}
		
		public TCComponent getTemplateComponent() 
		{
			return templateComponent;
		}

		public void setTemplateComponent(TCComponent templateComponent) 
		{
			this.templateComponent = templateComponent;
		}

		public TCComponent getTranslationComponent() 
		{
			return translationComponent;
		}

		public void setTranslationComponent(TCComponent translationComponent) 
		{
			this.translationComponent = translationComponent;
		}
		
		private String getStatus()
		{
			if( templateComponent == null ) 
			{
				if(translationComponent != null)
				{
					return STATUS_NEW;
				}
				else
				{
					//throw new IllegalStateException("template and tarnslation component are both null !?!");
					return null;
				}
			}
			else
			{
				if(translationComponent != null)
				{
					return translationComponent.hasContent() ? STATUS_EDITED : STATUS_DELETED;
				}
				else
				{
					return "";
				}
			}
		}
		
		private String getTypeString()
		{
			if( (templateComponent == null) && 
				(translationComponent == null) )
			{
				return null;
			}
			Class clazz = (templateComponent != null) ? templateComponent.getClass() : translationComponent.getClass();
			return JAPMessages.getString(TermsAndConditionsPanel.class.getName()+"_"+clazz.getSimpleName().toLowerCase());
		}
		
		private String getIdString()
		{
			if((templateComponent == null) && 
				(translationComponent == null) )
			{
				return null;
			}
			return ""+((templateComponent != null) ? templateComponent.getId() : translationComponent.getId());
		}
		
		public String toString()
		{
			String typeString = getTypeString();
			String idString = getIdString();
			String status = getStatus();
			if( (typeString != null) && (idString != null) && (status != null) )
			{
				return getTypeString()+" "+getIdString()+" "+getStatus();
			}
			return null;
		}
		
		private static Collection<TCComponentListItem> 
			createList(TCComponent[] fromTemplate, TCComponent[] fromTrans)
		{
			TreeMap<Double, TCComponentListItem> ht = 
				new TreeMap<Double, TCComponentListItem>();
			TCComponentListItem currentItem = null;
			if(fromTemplate != null)
			{
				for (int i = 0; i < fromTemplate.length; i++) 
				{
					ht.put(fromTemplate[i].getId(), new TCComponentListItem(fromTemplate[i], null));
				}
			}
			if(fromTrans != null)
			{
				for (int i = 0; i < fromTrans.length; i++) 
				{
					currentItem = ht.get(fromTrans[i].getId());
					if(currentItem == null) 
					{
						currentItem = new TCComponentListItem(null, fromTrans[i]);
						ht.put(fromTrans[i].getId(), currentItem);
					}
					else
					{
						currentItem.setTranslationComponent(fromTrans[i]);
					}
				}
			}
			return ht.values();
		}
	}

	//actions:
	private void actionAddSection()
	{
		Section s = AddComponentDialog.showAddSectionDialog(this.getContentPane());
		if(s != null)
		{
			Section idCheckSect = (Section) translationSections.getTCComponent(s.getId());
			if(idCheckSect != null)
			{
				JAPDialog.showErrorDialog(getParentComponent(), "Section '"+idCheckSect.getContent()+"' with id "+
						idCheckSect.getId()+" already exists", LogType.MISC);
			}
			else
			{
				translationSections.addTCComponent(s);
				loadSectionList(s);
			}
		}
		else
		{
			//something went wrong
			JAPDialog.showErrorDialog(getParentComponent(), "New section could not be added.", LogType.MISC);	
		}
	}
	
	
	
	public void focusGained(FocusEvent e) {}

	public void focusLost(FocusEvent e) 
	{
		
	}
	
	private static class AddComponentDialog extends JAPDialog
	{
		private AddComponentDialog(Component parent, String componentKey, 
				JTextField[] inputFields, JLabel[] labels)
		{
			super(parent, JAPMessages.getString(componentKey), true);
			setDefaultCloseOperation(HIDE_ON_CLOSE);
			getContentPane().setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridheight = 1;
			c.gridwidth = 1;
			c.insets = new Insets(3, 3, 3, 3);
			c.anchor = GridBagConstraints.WEST;
			c.fill = GridBagConstraints.BOTH;
			
			c.gridx = 0;
			for (c.gridy = 0; c.gridy < labels.length; c.gridy++) 
			{
				getContentPane().add(labels[c.gridy], c);
			}
			
			c.gridx = 1;
			c.gridwidth = GridBagConstraints.REMAINDER;
			for (c.gridy = 0; c.gridy < labels.length; c.gridy++) 
			{
				getContentPane().add(inputFields[c.gridy], c);
			}
			JPanel panel = new JPanel();
			JButton ok = new JButton(JAPMessages.getString(DialogContentPane.MSG_OK));
			final JButton cancel = new JButton(JAPMessages.getString(DialogContentPane.MSG_CANCEL));
			
			final JTextField[] tfs = inputFields;
			
			ActionListener simpleActionListener = new ActionListener()
			{
				public void actionPerformed(ActionEvent e) 
				{
					if(e.getSource() == cancel)
					{
						for (int i = 0; i < tfs.length; i++) 
						{
							tfs[i].setText("");
						}
					}
					setVisible(false);
				}
			};
			
			ok.addActionListener(simpleActionListener);
			cancel.addActionListener(simpleActionListener);
			
			panel.add(ok);
			panel.add(cancel);
			
			c.gridx = 0;
			c.gridy = Math.max(inputFields.length, labels.length);
			getContentPane().add(panel, c);
		}
		
		//TODO: a lot
		public static Section showAddSectionDialog(Component parent)
		{
			JTextField idField = new JTextField(10);
			JTextField nameField = new JTextField(10);
			
			JLabel idLabel = new JLabel("Id");
			JLabel nameLabel = new JLabel("Name");
			
			AddComponentDialog addSectionDialog = 
				new AddComponentDialog(parent, TermsAndConditionsPanel.MSG_SECTION,
						new JTextField[]{idField, nameField}, new JLabel[]{idLabel, nameLabel});
			
			addSectionDialog.pack();
			addSectionDialog.setVisible(true);

			Section sect = null;
			double id = -1.0;
			String name = "";
			try
			{
				id = (idField.getText() != null) ? Double.parseDouble(idField.getText()) : -1;
				name = (nameField.getText() != null) ? nameField.getText() : "";
				sect = (id >= 0) && !(name.equals("")) ? new Section(id, name) : null;
			}
			catch(NumberFormatException nfe)
			{
				//TODO show error Dialog
			}
			addSectionDialog.dispose();
			return sect;
		}
		
		public static Paragraph showAddParagraphDialog(Component parent)
		{
			JLabel idLabel = new JLabel("Id");
			JTextField idField = new JTextField(10);
			
			AddComponentDialog addParagraphDialog = 
				new AddComponentDialog(parent, TermsAndConditionsPanel.MSG_SECTION,
						new JTextField[]{idField}, new JLabel[]{idLabel});
			
			addParagraphDialog.pack();
			addParagraphDialog.setVisible(true);
			
			Paragraph par = null;
			double id = -1.0;

			try
			{
				id = (idField.getText() != null) ? Double.parseDouble(idField.getText()) : -1;
				par = (id >= 0) ? new Paragraph(id) : null;
			}
			catch(NumberFormatException nfe)
			{
				//TODO show error Dialog
			}
			addParagraphDialog.dispose();
			return par;
		}
	}
}
