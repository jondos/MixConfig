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

import gui.ButtonConstants;
import gui.GUIUtils;
import gui.MixConfigTextField;
import gui.TermsAndConditionsDialog;
import gui.TitledGridBagPanel;
import gui.dialog.DialogContentPane;
import gui.dialog.JAPDialog;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.JTextComponent;

import logging.LogType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import anon.terms.TCComponent;
import anon.terms.TCComposite;
import anon.terms.TermsAndConditions.Translation;
import anon.terms.template.Paragraph;
import anon.terms.template.Section;
import anon.terms.template.TermsAndConditionsTemplate;
import anon.util.JAPMessages;

/**
 * The content management dialog for the terms and conditions 
 * @author simon
 */
public class TermsAndConditionsContentDialog extends JAPDialog 
	implements ItemListener, ActionListener, FocusListener
{
	static final String MSG_STATUS_PREFIX = TermsAndConditionsContentDialog.class.getName() + "_status";
	static final String MSG_NAME = TermsAndConditionsContentDialog.class.getName() + "_name";
	static final String MSG_ID = TermsAndConditionsContentDialog.class.getName() + "_id";
	static final String MSG_ID_DESCRIPTION = TermsAndConditionsContentDialog.class.getName() + "_idDescription";
	
	static final String MSG_BUTTON_FROM_TEMPLATE =  TermsAndConditionsContentDialog.class.getName() + "_btnFromTemplate";
	static final String MSG_BUTTON_CAPITALIZE =  TermsAndConditionsContentDialog.class.getName() + "_btnCapitalize";
	static final String MSG_BUTTON_BOLD =  TermsAndConditionsContentDialog.class.getName() + "_btnBold";
	static final String MSG_BUTTON_URL_TAG =  TermsAndConditionsContentDialog.class.getName() + "_btnUrlTag";
	
	static final String URL_TAG = "<Url></Url>";
	
	private TermsAndConditionsPanel parentPanel;
	
	//View components
	private JButton sectionAddButton;
	private JButton sectionDeleteButton;
	private JButton sectionResetButton;
	
	private JButton paragraphAddButton;
	private JButton paragraphDeleteButton;
	private JButton paragraphResetButton;
	
	//edit customized content buttons
	private JButton fromTemplateButton;
	private JButton capitalizeButton;
	private JButton boldButton;
	private JButton urlTagButton;
	
	private JButton previewButton;
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
	
	private int lastCaretPosition = 0;
	
	private TermsAndConditionsContentDialog(TermsAndConditionsPanel parentPanel, TermsAndConditionsTemplate template,
			Translation translation) 
	{
		super(parentPanel, JAPMessages.getString(TermsAndConditionsPanel.MSG_CONTENT), true);
		this.parentPanel = parentPanel;
		setDefaultCloseOperation(JAPDialog.DISPOSE_ON_CLOSE);
		initializeComponents();
		placeComponents();
		
		templateSections = template.getSections();
		translationSections = translation.getSections();
		loadSectionList();
	}

	private void initializeComponents()
	{
		//create components
		sectionAddButton = GUIUtils.createButton(ButtonConstants.ADD);
		sectionDeleteButton = GUIUtils.createButton(ButtonConstants.DELETE);
		sectionResetButton = new JButton(JAPMessages.getString(TermsAndConditionsPanel.MSG_RESET));
		
		paragraphAddButton = GUIUtils.createButton(ButtonConstants.ADD);
		paragraphDeleteButton = GUIUtils.createButton(ButtonConstants.DELETE);
		paragraphResetButton = new JButton(JAPMessages.getString(TermsAndConditionsPanel.MSG_RESET));
	
		JPanel contentActionButtonPanel = new JPanel();
		fromTemplateButton = new JButton(JAPMessages.getString(MSG_BUTTON_FROM_TEMPLATE));
		capitalizeButton = new JButton(JAPMessages.getString(MSG_BUTTON_CAPITALIZE));
		boldButton = new JButton(JAPMessages.getString(MSG_BUTTON_BOLD));
		urlTagButton = new JButton(JAPMessages.getString(MSG_BUTTON_URL_TAG));
		
		contentActionButtonPanel.add(fromTemplateButton);
		contentActionButtonPanel.add(capitalizeButton);
		contentActionButtonPanel.add(boldButton);
		contentActionButtonPanel.add(urlTagButton);
		
		previewButton =  new JButton(JAPMessages.getString(TermsAndConditionsPanel.MSG_PREVIEW));
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
					sectionNameTemplate, templateText, null);
		customizedContentPanel = createContentPanel(TermsAndConditionsPanel.MSG_TRANSLATION_LABEL, 
					sectionNameTranslation, translationText, contentActionButtonPanel);
		
		//initialize listeners
		sectionChoice.addItemListener(this);
		paragraphChoice.addItemListener(this);
		
		sectionAddButton.addActionListener(this);
		sectionDeleteButton.addActionListener(this);
		sectionResetButton.addActionListener(this);
		paragraphAddButton.addActionListener(this);
		paragraphDeleteButton.addActionListener(this);
		paragraphResetButton.addActionListener(this);
		fromTemplateButton.addActionListener(this);
		capitalizeButton.addActionListener(this);
		boldButton.addActionListener(this);
		urlTagButton.addActionListener(this);
		previewButton.addActionListener(this);
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		
		sectionNameTranslation.addFocusListener(this);
		translationText.addFocusListener(this);
		
		sectionNameTemplate.setEditable(false);
		templateText.setEditable(false);
		
		templateText.setLineWrap(true);
		translationText.setLineWrap(true);
		ListCellRenderer tcComponentRenderer = new TCComponentListItemRenderer();
		//initialize renderers
		sectionChoice.setRenderer(tcComponentRenderer);
		paragraphChoice.setRenderer(tcComponentRenderer);
	}
	
	private void placeComponents()
	{
		Container contentPane = getContentPane();
		GridBagLayout gridbag = new GridBagLayout();
		
		GridBagConstraints c = new GridBagConstraints();
		contentPane.setLayout(gridbag);
		
		JPanel buttonPanel = new JPanel();
		
		buttonPanel.add(previewButton);
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
	
	/**
	 * Reloads the section-comboBox without specifying a particular selected
	 * section.
	 */
	public void loadSectionList()
	{
		loadSectionList(null);
	}
	
	/**
	 * Reloads the section-comboBox. The section with the id of the specified section will 
	 * be the selected item, if such an item exists in the section-list.
	 */
	public void loadSectionList(TCComponent selected)
	{
		sectionChoice.removeAllItems();
		TCComponent[] fromTemplate = (templateSections != null) ? templateSections.getTCComponents() : null;
		TCComponent[] fromTrans = (translationSections != null) ? translationSections.getTCComponents() : null;
		
		Collection<TCComponentListItem> items = TCComponentListItem.createList(fromTemplate, fromTrans);
		loadComboBoxItems(sectionChoice, items, selected);
	}
	
	/**
	 * Reloads the paragraph-comboBox of the selected section without specifying a particular 
	 * selected paragraph.
	 */
	public void loadParagraphList()
	{
		loadParagraphList(null);
	}
	
	/**
	 * Reloads the paragraph-comboBox of the selected section. The paragraph with the id of the specified paragraph will 
	 * be the selected item, if such an item exists in the paragraph-list.
	 */
	public void loadParagraphList(TCComponent selected)
	{
		paragraphChoice.removeAllItems();
		Collection<TCComponentListItem> items = 
			TCComponentListItem.createList(
					getParagraphsOfSelectedTemplateSection(), 
					getParagraphsOfSelectedTranslationSection());
		loadComboBoxItems(paragraphChoice, items, selected);
	}
	
	/**
	 * Used by the list-item-loading-functions to fill the content of the comboBoxes
	 * @param comboBox the comboBox to fill in the items
	 * @param items collection of items to be filled in the comboBox
	 * @param selected component that will be selected if a 
	 * customized- or template-component with the same id exists in the collection of list-items, 
	 * if null, this parameter will be ignored.
	 */
	private void loadComboBoxItems(JComboBox comboBox, 
			Collection<TCComponentListItem> items, TCComponent selected)
	{
		TCComponentListItem selectedItem = null;
		TCComponent currentComponent = null;
		for (TCComponentListItem item : items) 
		{
			if(	selected != null ) 
			{
				currentComponent = item.getTranslationComponent();
				if (currentComponent == null) 
				{
					currentComponent = item.getTemplateComponent();
				}	
				if(currentComponent.getId() == selected.getId())
				{
					selectedItem = item;
				}
			}
			comboBox.addItem(item);
		}
		if(selectedItem != null)
		{
			comboBox.setSelectedItem(selectedItem);
		}
	}
	
	/**
	 * ItemListener implementations
	 */
	public void itemStateChanged(ItemEvent e) 
	{
		if( e.getStateChange() == ItemEvent.SELECTED)
		{
			if(e.getSource() == sectionChoice)
			{
				loadParagraphList();
			}

			//set the content of the text-fields and -areas.
			Section templateSect = getSelectedTemplateSection();
			Section transSect = getSelectedTranslationSection();
			
			Paragraph templatePar = getSelectedTemplateParagraph();
			Paragraph transPar = getSelectedTranslationParagraph();
			
			templateText.setText((templatePar != null) ? templatePar.toString() : "");
			translationText.setText((transPar != null) ? transPar.toString() : "");
			
			sectionNameTemplate.setText(
					(templateSect != null) && templateSect.hasContent() && (templateSect.getContent() != null) ? 
							templateSect.getContent().toString().trim() : "");
			sectionNameTranslation.setText(
					(transSect != null) && (transSect.hasContent()) && (transSect.getContent() != null) ? 
							transSect.getContent().toString().trim() : "");
			lastCaretPosition = 0;
			enableComponents();
		}
	}

	/**
	 * ActionListener implementations
	 */
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == sectionAddButton)
		{
			actionAddSection();
		}
		else if(e.getSource() == sectionDeleteButton)
		{
			actionDeleteSection();
		}
		else if(e.getSource() == sectionResetButton)
		{
			actionResetSection();
		}
		else if(e.getSource() == paragraphAddButton)
		{
			actionAddParagraph();
		}
		else if(e.getSource() == paragraphDeleteButton)
		{
			actionDeleteParagraph();
		}
		else if(e.getSource() == paragraphResetButton)
		{
			actionResetParagraph();
		}
		else if(e.getSource() == fromTemplateButton)
		{
			actionContentFromTemplate();
		}
		else if(e.getSource() == capitalizeButton)
		{
			actionCapitalizeContent();
		}
		else if(e.getSource() == boldButton)
		{
			actionSetContentBold();
		}
		else if(e.getSource() == urlTagButton)
		{
			actionAddUrlTag();
		}
		else if(e.getSource() == previewButton)
		{
			actionPreview();
		}
		else if(e.getSource() == okButton)
		{
			dispose();
			return;
		}
		else if(e.getSource() == cancelButton)
		{
			translationSections = null;
			dispose();
			return;
		}
		enableComponents();
	}
	
	/**
	 * @return the template-section of the selected section-list item
	 * or null if it is not defined by the selected list item.
	 */
	private Section getSelectedTemplateSection()
	{
		TCComponentListItem item = getSelectedListItem(sectionChoice);
		return (Section) ((item != null) ? item.getTemplateComponent() : null);
	}
	
	/**
	 * @return the customized translation-section of the selected section-list item
	 * or null if it is not defined by the selected list item.
	 */
	private Section getSelectedTranslationSection()
	{
		TCComponentListItem item = getSelectedListItem(sectionChoice);
		return (Section) ((item != null) ? item.getTranslationComponent() : null);
	}
	
	/**
	 * @return an array with the paragraphs of the selected template-section
	 * or null if no template-section is defined by the selected section-item.
	 */
	private TCComponent[] getParagraphsOfSelectedTemplateSection()
	{
		Section selectedTemplateSection = 
				getSelectedTemplateSection();
		return (selectedTemplateSection != null) ? 
				selectedTemplateSection.getTCComponents() : null;
	}
	
	/**
	 * @return an array with the paragraphs of the selected translation-section
	 * or null if no translation-section is defined by the selected section-item.
	 */
	private TCComponent[] getParagraphsOfSelectedTranslationSection()
	{
		Section selectedTranslationSection = 
				getSelectedTranslationSection();
		return (selectedTranslationSection != null) ? 
				selectedTranslationSection.getTCComponents() : null;
	}
	
	/**
	 * @return the template-paragraph of the selected paragraph-list item
	 * or null if it is not defined by the selected list item.
	 */
	private Paragraph getSelectedTemplateParagraph()
	{
		TCComponentListItem item = getSelectedListItem(paragraphChoice);
		return (Paragraph) ((item != null) ? item.getTemplateComponent() : null);
	}
	
	/**
	 * @return the customized translation-paragraph of the selected paragraph-list item
	 * or null if it is not defined by the selected list item.
	 */
	private Paragraph getSelectedTranslationParagraph()
	{
		TCComponentListItem item = getSelectedListItem(paragraphChoice);
		return (Paragraph) ((item != null) ? item.getTranslationComponent() : null);
	}
	
	/**
	 * convenience function to get a customized section as a container for a modified
	 * or added paragraph. The returned section will also be part of the model. If no customized 
	 * section with the id of the selected section exists it will be created and added to the model 
	 * before it will be returned.
	 * @param withSectionName if true the section-name from the template will be set for the customized
	 * section if it is created, otherwise a newly created customized section will have no name.
	 * This parameter has no effect if a customized section already exists.
	 * @return customized section that acts as a container for the modified or added paragraphs.
	 */
	private Section getCustomizedSection(boolean withSectionName)
	{
		TCComponentListItem item = getSelectedListItem(sectionChoice);
		Section customizedSection = (Section) item.getTranslationComponent(); 
		if(customizedSection == null)
		{
			Section templateSection = (Section) item.getTemplateComponent();
			//templateSection must not be null
			customizedSection = 
				new Section(templateSection.getId(), withSectionName ? templateSection.getContent() : null);
			translationSections.addTCComponent(customizedSection);
			item.setTranslationComponent(customizedSection);
		}
		return customizedSection;
	}
	
	/**
	 * @return the same as getCustomizedSection(false);
	 */
	private Section getCustomizedSection()
	{
		return getCustomizedSection(false);
	}
	
	/**
	 * returns a customized paragraph that can be edited.
	 * if it does not exist in the model it will be created and added to the model 
	 * before it will be returned. This method will also create a new customized section
	 * if the customized paragraph has no container yet.
	 * @param withSectionName refers to the creation of customized section as container.
	 * @return an editable customized paragraph.
	 */
	private Paragraph getCustomizedParagraph(boolean withSectionName)
	{
		TCComponentListItem item = getSelectedListItem(paragraphChoice);
		Paragraph customizedParagraph = (Paragraph) item.getTranslationComponent();
		if(customizedParagraph == null)
		{
			Paragraph templateParagraph = (Paragraph) item.getTemplateComponent();
			customizedParagraph = new Paragraph(templateParagraph.getId());
			Section customizedSection = getCustomizedSection(withSectionName);
			customizedSection.addTCComponent(customizedParagraph);
		}
		return customizedParagraph;
	}
	
	/**
	 * @return the same as getCustomizedParagraph(false);
	 */
	private Paragraph getCustomizedParagraph()
	{
		return getCustomizedParagraph(false);
	}
	
	/**
	 * @param comboBox
	 * @return the selected list item of the specified combobox
	 */
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
			JTextField sectionNameTF, JTextArea paragraphTextArea, JPanel buttonPanel)
	{
		TitledGridBagPanel contentPanel = 
			new TitledGridBagPanel(
					JAPMessages.getString(TermsAndConditionsPanel.MSG_CONTENT)+"/"+
					JAPMessages.getString(contentSourceNameKey));
		contentPanel.addRow(new Component[]{
				GUIUtils.createLabel(TermsAndConditionsPanel.MSG_SECTION), 
				sectionNameTF}, 
				new int[]{1, GridBagConstraints.REMAINDER});
		contentPanel.addRow(new Component[]{
				GUIUtils.createLabel(TermsAndConditionsPanel.MSG_PARAGRAPH), 
				new JScrollPane(paragraphTextArea, 
						JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)}, 
				new int[]{1, GridBagConstraints.REMAINDER});
		if(buttonPanel != null)
		{
			contentPanel.addRow(buttonPanel);
		}
		return contentPanel;
	}
	
	/****** user actions: *****/
	
	private void actionAddSection()
	{
		Section newSection = AddComponentDialog.showAddSectionDialog(this.getContentPane());
		if(newSection != null)
		{
			TCComponent existingSection = translationSections.getTCComponent(newSection.getId());
			if((existingSection != null) && existingSection.hasContent())
			{	
				JAPDialog.showErrorDialog(getParentComponent(), "Section '"+existingSection.getContent()+"' with id "+
						existingSection.getId()+" already exists", LogType.MISC);
			}
			else
			{
				translationSections.addTCComponent(newSection);
				loadSectionList(newSection);
			}
		}
	}
	
	private void actionAddParagraph()
	{
		Paragraph newParagraph = AddComponentDialog.showAddParagraphDialog(this.getContentPane());
		if(newParagraph != null)
		{
			Section translationSection = getCustomizedSection();
			TCComponent existingParagraph = translationSection.getTCComponent(newParagraph.getId());
			if( (existingParagraph != null) && existingParagraph.hasContent())
			{
				JAPDialog.showErrorDialog(getParentComponent(), "Paragraph with id "+
						existingParagraph.getId()+" already exists", LogType.MISC);
			}
			else
			{
				Section templateSection = getSelectedTemplateSection();
				if(templateSection != null)
				{
					//check if a paragraph with the given id is specified by the template
					existingParagraph = templateSection.getTCComponent(newParagraph.getId());
					if(existingParagraph != null)
					{
						//if this is the case: copy the template content for modification.
						newParagraph.setContent(existingParagraph.getContent());
					}
				}
				translationSection.addTCComponent(newParagraph);
				loadParagraphList(newParagraph);
			}
		}
	}
	
	private void actionDeleteSection()
	{
		TCComponentListItem item = getSelectedListItem(sectionChoice);
		deleteSelectedComponent(item, translationSections);
		loadSectionList(item.getTemplateComponent());
	}
	
	private void actionDeleteParagraph()
	{
		TCComponentListItem item = getSelectedListItem(paragraphChoice);
		Section container = getSelectedTranslationSection();
		boolean containerHadContent =  (container != null) && (container.hasContent());
		//Might look crazy but we need to create a customized section for nesting a paragraph item,
		//which overwrites the template content with null-content.
		if(container == null) 
		{
			container = getCustomizedSection();
		}
		deleteSelectedComponent(item, container);
		//We must consider this special case that the last customized paragragh of
		//a nameless section was deleted. This means the customized section definition 
		//must be removed with the last paragraph, otherwise the empty section
		//is misinterpreted as a null-overwrite-section for the template.
		if(containerHadContent && !container.hasContent())
		{
			translationSections.removeTCComponent(container.getId());
			loadSectionList(getSelectedTemplateSection());
		}
		else
		{
			//if a template component still exists: re-select it
			loadParagraphList(item.getTemplateComponent());
		}
	}
	
	private void actionContentFromTemplate()
	{
		Paragraph templateParagraph = getSelectedTemplateParagraph();
		Paragraph customizedParagraph = getSelectedTranslationParagraph();
		if( templateParagraph != null )
		{
			if( customizedParagraph == null )
			{
				customizedParagraph = getCustomizedParagraph();
			}
			if(templateParagraph.hasContent())
			{
				customizedParagraph.setContent(templateParagraph.getContent());
			}
			loadParagraphList(customizedParagraph);
		}
	}
	
	private void actionCapitalizeContent()
	{
		Paragraph p = getSelectedTranslationParagraph();
		if(p != null)
		{
			capitalizeTextContent(p);
			//view changes directly is simple in this case: no need to reload 
			//the whole paragraph list
			translationText.setText(p.toString());
		}
	}
	
	private void actionSetContentBold()
	{
		Paragraph p = getSelectedTranslationParagraph();
		if(p != null)
		{
			p.setContentBold();
			//view changes directly is simple in this case: no need to reload 
			//the whole paragraph list
			translationText.setText(p.toString());
		}
	}
	
	private void actionAddUrlTag()
	{
		String text = translationText.getText();
		if( (text == null) || text.equals(""))
		{
			editParagraph(URL_TAG);		
		}
		else
		{
			StringBuffer buffer = new StringBuffer(text);
			buffer.insert(lastCaretPosition, URL_TAG);
			editParagraph(buffer.toString());
		}
		
	}
	
	//TODO: If the Java 1.1 source restriction of the Anonlib classes is 
	//revoked move this method the class anon.terms.template.Paragraph
	private void capitalizeTextContent(Paragraph p)
	{
		NodeList contentNodes = (NodeList) p.getContent();
		for (int i = 0; i < contentNodes.getLength(); i++) 
		{
			capitalizeTextContentRecursion(contentNodes.item(i));
		}
	}
	
	//TODO: As this is a recursive helper routine for the above function
	//move it also to class anon.terms.template.Paragraph
	private void capitalizeTextContentRecursion(Node node)
	{
		if(node != null)
		{
			if( node.getNodeType() == Node.TEXT_NODE )
			{
				node.setTextContent(node.getTextContent().toUpperCase());
			}
			else if( node.getNodeType() == Node.ELEMENT_NODE )
			{
				NodeList nl = ((Element) node).getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) 
				{
					capitalizeTextContentRecursion(nl.item(i));
				}
			}
			else if( node.getNodeType() == Node.DOCUMENT_NODE )
			{
				capitalizeTextContentRecursion(((Document) node).getDocumentElement());
			}
		}
	}
	
	/**
	 * Reusable function to remove Sections or Paragraphs from the model
	 * @param item the list-item representing the component to be removed
	 * @param if a customized component has to be removed, this is the container 
	 * from where to remove it.
	 * @param returns the component that was deleted or the null-content object which
	 * represents the disabling of a corresponding template component  
	 */
	private void deleteSelectedComponent(TCComponentListItem item, TCComposite container)
	{
		if( (item != null) && 
			(item.getStatus() != null) && 
			!item.getStatus().equals(TCComponentListItem.STATUS_DELETED) &&
			(container != null) )
		{
			TCComponent deleteComponent =  item.getTranslationComponent();
			if(deleteComponent != null)
			{
				//simply delete the customized section
				container.removeTCComponent(deleteComponent.getId());
			}
			else
			{
				//add element that overwrites the corresponding template element with 
				//no content, with the effect that the corresponding template component will not be shown.
				try 
				{
					TCComponent templateComponent = item.getTemplateComponent();
					deleteComponent = templateComponent.getClass().newInstance();
					deleteComponent.setId(templateComponent.getId());
					deleteComponent.setContent(null);
					container.addTCComponent(deleteComponent);
				} 
				catch (InstantiationException e) {} 
				catch (IllegalAccessException e) {}
			}
		}
	}
	
	private void actionResetSection()
	{
		TCComponentListItem sectionItem = getSelectedListItem(sectionChoice);
		resetCustomizedComponent(sectionItem, translationSections);
		loadSectionList(sectionItem.getTemplateComponent());
	}
	
	private void actionResetParagraph()
	{ 
		TCComponentListItem paragraphItem = getSelectedListItem(paragraphChoice);
		Section container = getSelectedTranslationSection();
		boolean containerHadContent = (container != null) && (container.hasContent());
		resetCustomizedComponent(paragraphItem, container);
		
		//same special case handling as in actionDeleteParagraph
		if(containerHadContent && !container.hasContent())
		{
			translationSections.removeTCComponent(container.getId());
			loadSectionList(getSelectedTemplateSection());
		}
		else
		{
			//if a template component still exists: re-select it
			loadParagraphList(paragraphItem.getTemplateComponent());
		}
	}
	
	private void actionPreview()
	{
		Translation previewTranslation = parentPanel.getPreviewTranslation();
		if(previewTranslation != null)
		{
			previewTranslation.setSections(translationSections);
			TermsAndConditionsDialog.previewTranslation(getContentPane(), previewTranslation);
		}
	}
	
	//resulting effect: the template content will be displayed again without changes
	//if component was resetted, it is returned. otherwise null will be returned.
	private void resetCustomizedComponent(TCComponentListItem item, TCComposite container)
	{
		if((item != null) && (container != null) )
		{
			String status = item.getStatus();
			if(status != null)
			{
				TCComponent translationComponent = item.getTranslationComponent();
				if(translationComponent != null)
				{
					container.removeTCComponent(translationComponent.getId());
				}
			}
		}
	}
	
	/** FocusListener implementation */
	public void focusGained(FocusEvent e) 
	{
		lastCaretPosition = translationText.getCaretPosition();
	}

	public void focusLost(FocusEvent e) 
	{
		JTextComponent source = (JTextComponent) e.getSource();
		String content = (source.getText() != null) ? source.getText() : "";
		if( source == translationText )
		{
			editParagraph(content);
		}
		else if (source == sectionNameTranslation)
		{
			editSection(content);
		}
		enableComponents();
	}
	
	/**
	 * Edits the name of the model of the selected section.
	 * @param content the new name for the selected section
	 */
	private void editSection(String content)
	{
		Section translationSection = getSelectedTranslationSection();
		
		if(translationSection != null)
		{
			if((content != null) && !(content.equals("")))
			{
				translationSection.setContent(content);
			}
			else
			{
				if(translationSection.getTCComponentCount() == 0)
				{
					actionResetSection();
				}
			}
		}
		else if((content != null) && !(content.equals("")))
		{
			translationSection = getCustomizedSection(true);
			translationSection.setContent(content);
			loadSectionList(translationSection);
		}
	}
	
	/**
	 * Edits the model of the selected Paragraph.
	 * @param content after the operation the model of the selected Paragraph will store this content.
	 */
	private void editParagraph(String content)
	{
		if( (getSelectedTranslationParagraph() != null) &&
			( (content == null) || content.equals("") ))
		{
			actionResetParagraph();
		}
		else
		{
			if( (content != null) && !content.equals(""))
			{
				int caretSave = lastCaretPosition;
				Paragraph customizedParagraph = getCustomizedParagraph();
				customizedParagraph.setContent(content);
				loadParagraphList(customizedParagraph);
				lastCaretPosition = caretSave;
			}
		}
	}
	
	/**
	 * to be invoked for enabling or disabling view components
	 */
	protected void enableComponents()
	{
		sectionChoice.setEnabled(isSectionSelectable());
		sectionDeleteButton.setEnabled(isSectionDeletable());
		sectionResetButton.setEnabled(isSectionResettable());
		
		paragraphChoice.setEnabled(isParagraphSelectable());
		paragraphAddButton.setEnabled(isParagraphAddable());
		paragraphDeleteButton.setEnabled(isParagraphDeletable());
		paragraphResetButton.setEnabled(isParagraphResettable());
		
		fromTemplateButton.setEnabled(isContentFromTemplateAvailable());
		capitalizeButton.setEnabled(isContentCapitalizable());
		boldButton.setEnabled(isContentSetToBoldPossible());
		
		//Although this check is done, a preview must always be possible when this dialog is displayed.
		previewButton.setEnabled(parentPanel.isPreviewPossible());
		
		translationText.setEnabled(isParagraphEditable());
		sectionNameTranslation.setEnabled(isSectionNameEditable());
	}
	
	/** methods to check if view components should be enabled or disabled **/
	private boolean isSectionSelectable()
	{
		return (getSelectedListItem(sectionChoice) != null);
	}
	
	private boolean isSectionResettable()
	{
		return isSectionSelectable() && (getSelectedTranslationSection() != null);
	}
	
	private boolean isSectionDeletable()
	{
		return isSectionSelectable() && isItemDeletable(getSelectedListItem(sectionChoice));
	}
	
	private boolean isParagraphSelectable()
	{
		return isSectionDeletable() && (getSelectedListItem(paragraphChoice) != null);
	}
	
	private boolean isParagraphDeletable()
	{
		return isParagraphSelectable() && isItemDeletable(getSelectedListItem(paragraphChoice));
	}
	
	private boolean isParagraphResettable()
	{
		return isParagraphSelectable() && (getSelectedTranslationParagraph() != null);
	}
	
	private boolean isParagraphEditable()
	{
		return isParagraphDeletable();
	}
	
	private boolean isContentCapitalizable()
	{
		Paragraph customizedPar = getSelectedTranslationParagraph();
		return (customizedPar != null) && customizedPar.hasContent();
	}
	
	private boolean isContentSetToBoldPossible()
	{
		Paragraph customizedPar = getSelectedTranslationParagraph();
		return (customizedPar != null) && customizedPar.hasContent();
	}
	
	private boolean isContentFromTemplateAvailable()
	{
		Paragraph templatePar = getSelectedTemplateParagraph();
		return (templatePar != null) && templatePar.hasContent();
	}
	
	/*
	 * sometimes it is not possible to add a paragraph, (i.e if a template section is overwritten with null-content)
	 */
	private boolean isParagraphAddable()
	{
		return isSectionDeletable();
	}
	
	private boolean isSectionNameEditable()
	{
		return isSectionDeletable();
	}
	
	private boolean isItemDeletable(TCComponentListItem item)
	{
		return (item != null) && (item.getStatus() != null) && 
				!(item.getStatus().equals(TCComponentListItem.STATUS_DELETED));
	}
	
	/** 
	 * used to synchronously show a T&C content management dialog and return the customized sections as result.
	 * @param parentPanel the parentComponent of the content management dialog
	 * @param template the base template from which the content shall be customized 
	 * @param translation the translation from which to take the customized content.
	 * @return the sections containing the customized content.
	 */
	public static TCComposite showContentDialog(TermsAndConditionsPanel parentPanel, TermsAndConditionsTemplate template,
			Translation translation)
	{
		TermsAndConditionsContentDialog dialog = 
			new TermsAndConditionsContentDialog(parentPanel, template, translation);
		dialog.pack();
		dialog.setVisible(true);
		return dialog.translationSections;
	}
	
	private static class TCComponentListItem
	{
		public final static String STATUS_EDITED = "Edited";
		public final static String STATUS_DELETED = "Deleted";
		public final static String STATUS_NEW = "New";
		
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
					//throw new IllegalStateException("template and translation component are both null !?!");
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
		
		private String getStatusDisplayString()
		{
			String status = getStatus();
			return ( (status != null) && !status.equals("")) ? JAPMessages.getString(MSG_STATUS_PREFIX+status) : status;
		}
		
		private String getTypeString()
		{
			if( (templateComponent == null) && 
				(translationComponent == null) )
			{
				return null;
			}
			Class<? extends TCComponent> tcComponentClass = (Class<? extends TCComponent>) 
				((templateComponent != null) ? templateComponent.getClass() : translationComponent.getClass());
			return JAPMessages.getString(TermsAndConditionsPanel.class.getName()+"_"+tcComponentClass.getSimpleName().toLowerCase());
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
			String statusDisplayString = getStatusDisplayString();
			if((statusDisplayString != null) && !statusDisplayString.equals(""))
			{
				statusDisplayString = "("+statusDisplayString+")";
			}
			if( (typeString != null) && (idString != null) && (statusDisplayString != null) )
			{
				return typeString+" "+idString+" "+statusDisplayString;
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
	
	/**
	 * Class for showing special dialog that allwos user to create and add new 
	 * customized sections or paragraphs
	 */
	private static class AddComponentDialog extends JAPDialog 
		implements KeyListener, ActionListener
	{
		JButton ok = new JButton(JAPMessages.getString(DialogContentPane.MSG_OK));
		JButton cancel = new JButton(JAPMessages.getString(DialogContentPane.MSG_CANCEL));
		JTextField[] inputFields = null;
		InputVerifier verifier = null;
		
		private AddComponentDialog(Component parent, String componentKey, 
				JTextField[] inputFields, JLabel[] labels, JLabel helpTextLabel, InputVerifier verifier)
		{
			super(parent, JAPMessages.getString(componentKey), true);
			this.inputFields = inputFields;
			this.verifier = verifier;
			
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
			for (c.gridy = 0; c.gridy < this.inputFields.length; c.gridy++) 
			{
				if(verifier != null)
				{
					this.inputFields[c.gridy].addKeyListener(this);
				}
				getContentPane().add(this.inputFields[c.gridy], c);
			}
		
			ok.addActionListener(this);
			cancel.addActionListener(this);
			
			JPanel panel = new JPanel();
			panel.add(ok);
			panel.add(cancel);
			
			c.gridx = 0;
			c.gridy = Math.max(inputFields.length, labels.length);
			if(helpTextLabel != null)
			{
				getContentPane().add(helpTextLabel, c);
				c.gridy++;
			}
			getContentPane().add(panel, c);
			ok.setEnabled((verifier != null) ? verifier.verfifyInput() : true);
		}
		
		/**
		 * ActionListener implementation
		 */
		public void actionPerformed(ActionEvent e) 
		{
			if(e.getSource() == cancel)
			{
				for (int i = 0; i < inputFields.length; i++) 
				{
					inputFields[i].setText("");
				}
			}
			setVisible(false);
		}
		
		/**
		 * KeyListener implementation for live field validation
		 */
		public void keyReleased(KeyEvent e)
		{
			ok.setEnabled((verifier != null) ? verifier.verfifyInput() : true);
		}
		public void keyTyped(KeyEvent e) {}
		public void keyPressed(KeyEvent e) {}
		
		
		/** 
		 * @param input Input to check.
		 * @return true if the input is a valid ID, i.e. a valid
		 * floating point number,false otherwise
		 */
		public static boolean verifyValidID(String input)
		{
			if(input != null) 
			{
				try
				{
					Double.parseDouble(input);
					return true;
				}
				catch (NumberFormatException e) {}
			}
			return false;
		}
		
		/**
		 * @param input Input to check
		 * @return true if the entered input is a valid name, i.e. 
		 * not null and not empty, false otherwise
		 */
		public static boolean verifyValidName(String input)
		{
			return (input != null) && !input.equals("");
		}
		
		/**
		 * Display a a dialog for creating a new customized section
		 * @param parent parent component for displaying the dialog
		 * @return the created customized section or null if 
		 * user canceled operation. 
		 */
		public static Section showAddSectionDialog(Component parent)
		{
			JLabel idLabel = GUIUtils.createLabel(MSG_ID);
			JLabel nameLabel = GUIUtils.createLabel(MSG_NAME);
			
			final JTextField idField = new JTextField(10);
			final JTextField nameField = new JTextField(10);
			
			InputVerifier verifier = new InputVerifier()
			{
				public boolean verfifyInput() 
				{
					return verifyValidID(idField.getText()) && 
							verifyValidName(nameField.getText());
				}
			};
			
			AddComponentDialog addSectionDialog = 
				new AddComponentDialog(parent, TermsAndConditionsPanel.MSG_SECTION,
						new JTextField[]{idField, nameField}, new JLabel[]{idLabel, nameLabel},
						GUIUtils.createMultiLineLabel(MSG_ID_DESCRIPTION, 300), verifier);
			
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
				//Not possible since fields are validated live
			}
			addSectionDialog.dispose();
			return sect;
		}
		
		/**
		 * Display a a dialog for creating a new customized paragraph
		 * @param parent parent component for displaying the dialog
		 * @return the created customized paragraph or null if 
		 * user canceled operation. 
		 */
		public static Paragraph showAddParagraphDialog(Component parent)
		{
			JLabel idLabel = GUIUtils.createLabel(MSG_ID);
			final JTextField idField = new JTextField(10);
			
			InputVerifier verifier = new InputVerifier()
			{
				public boolean verfifyInput() 
				{
					return verifyValidID(idField.getText());
				}
			};
			
			AddComponentDialog addParagraphDialog = 
				new AddComponentDialog(parent, TermsAndConditionsPanel.MSG_SECTION,
						new JTextField[]{idField}, new JLabel[]{idLabel},
						GUIUtils.createMultiLineLabel(MSG_ID_DESCRIPTION, 300), verifier);
			
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
				//Not possible since fields are validated live
			}
			addParagraphDialog.dispose();
			return par;
		}
		
		/** just a helper for verifying textfield content */ 
		private interface InputVerifier
		{
			boolean verfifyInput();
		}
	}
	
	
	public static class TCComponentListItemRenderer extends BasicComboBoxRenderer
	{
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if( (value instanceof TCComponentListItem) && (value != null) &&
				(c instanceof JLabel) && (c != null) )
			{
				TCComponentListItem item = (TCComponentListItem) value;
				JLabel itemLabel = (JLabel) c;
				
				if( (item.getStatus() != null) && 
					(item.getStatus().equals(TCComponentListItem.STATUS_DELETED)))
				{
					//display deleted items cursive
					Font f = itemLabel.getFont();
					itemLabel.setFont(f.deriveFont(Font.ITALIC));
				}
			}
			return c;
		}
		
	}
}
