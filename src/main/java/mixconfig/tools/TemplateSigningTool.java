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
package mixconfig.tools;

import gui.TitledGridBagPanel;
import gui.dialog.JAPDialog;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.w3c.dom.Document;

import mixconfig.MixConfig;
import mixconfig.panels.CertPanel;
import anon.crypto.ICertificate;
import anon.crypto.JAPCertificate;
import anon.crypto.PKCS12;
import anon.crypto.SignatureCreator;
import anon.crypto.SignatureVerifier;
import anon.infoservice.Database;
import anon.terms.template.TermsAndConditionsTemplate;
import anon.util.XMLParseException;
import anon.util.XMLUtil;

public class TemplateSigningTool extends JAPDialog implements ActionListener, ChangeListener
{
	private CertPanel certPanel = null;
	private TitledGridBagPanel templatePanel = null;
	private JComboBox templatesBox = null;
	
	private JButton loadTemplateButton = null;
	private JButton signButton = null;
	private JButton closeButton = null;
	
	private Frame parent = null;
	
	private PKCS12 signingKey = null;
	
	private SignatureCreator signer = SignatureCreator.getInstance();
	
	public TemplateSigningTool(Frame parent)
	{
		super(parent, "Sign the templates");
		this.parent = parent;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		certPanel = new CertPanel("Signing certificate (PKCS #12)",
				   					"Hint: Private Certificate to sign a Public Certificate",
				   					(PKCS12)null, CertPanel.CERT_ALGORITHM_BOTH, 
				   					JAPCertificate.CERTIFICATE_TYPE_ROOT);
		
		certPanel.addChangeListener(this);
		loadTemplateButton = new JButton("Load");
		loadTemplateButton.addActionListener(this);
		signButton = new JButton("Sign");
		closeButton = new JButton("Close");
		signButton.setEnabled(false);
		signButton.addActionListener(this);
		closeButton.addActionListener(this);
		
		JPanel loadTemplateTemplate = new JPanel();
		loadTemplateTemplate.add(loadTemplateButton);
		
		templatesBox = new JComboBox();
		templatePanel = new TitledGridBagPanel("Available templates");
		templatePanel.addRow(new JLabel("Template"), templatesBox);
		templatePanel.addRow(loadTemplateTemplate);
		
		for (TermsAndConditionsTemplate template : getAllTemplates()) 
		{
			templatesBox.addItem(template.getId());
		}
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(closeButton);
		buttonPanel.add(signButton);
		
		Container contentPane = getContentPane();
		
		GridBagLayout dialogLayout = new GridBagLayout();
		GridBagConstraints dialogConstraints = new GridBagConstraints();
		
		contentPane.setLayout(dialogLayout);
		
		dialogConstraints.anchor = GridBagConstraints.WEST;
		dialogConstraints.gridx = 0;
		dialogConstraints.gridy = 0;
		dialogConstraints.insets = new Insets(5,5,5,5);
		dialogConstraints.fill = GridBagConstraints.BOTH;
		dialogConstraints.gridheight = 1;
		dialogConstraints.gridy = GridBagConstraints.REMAINDER;
	
		contentPane.add(certPanel, dialogConstraints);
		dialogConstraints.gridy++;
		contentPane.add(templatePanel, dialogConstraints);
		dialogConstraints.gridy++;
		contentPane.add(buttonPanel, dialogConstraints);
		
		pack();
		setVisible(true);
	}

	
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == closeButton)
		{
			dispose();
		}
		else if(e.getSource() == signButton)
		{
			TermsAndConditionsTemplate selectedTemplate = getSelectedTemplate();
			if( (selectedTemplate != null) && (signingKey != null) )
			{
				Document docToSign = selectedTemplate.getDocument();
				signer.signXml(SignatureVerifier.DOCUMENT_CLASS_TERMS, docToSign);
				selectedTemplate.setSignedDocument(docToSign);
				exportFile(selectedTemplate);
			}
		}
		else if (e.getSource() == loadTemplateButton)
		{
			loadTemplateFromFile();
		}
	}
	
	public void stateChanged(ChangeEvent e) 
	{
		ICertificate cert = certPanel.getCert();
		signingKey = (PKCS12) (cert instanceof PKCS12 ? cert : null);
		boolean signingKeyAvaliable = signingKey != null;
		signButton.setEnabled(signingKeyAvaliable);
		if(signingKeyAvaliable)
		{
			signer.setSigningKey(SignatureVerifier.DOCUMENT_CLASS_TERMS, signingKey);
		}
	}
	
	private void loadTemplateFromFile()
	{
		//Load template from file
		JFileChooser fc = MixConfig.showFileDialog(parent, MixConfig.OPEN_MULTIPLE_DIALOG, MixConfig.FILTER_XML);
		
		if (fc != null)
		{
				TermsAndConditionsTemplate currentTemplate = null;
				File[] fs = fc.getSelectedFiles();
				for (File file : fs) 
				{
					try 
					{
						currentTemplate = new TermsAndConditionsTemplate(file);
						Database.getInstance(TermsAndConditionsTemplate.class).update(currentTemplate);
						removeOldItem(currentTemplate.getId());
						templatesBox.addItem(currentTemplate.getId());
					} 
					catch (XMLParseException e) 
					{
						JAPDialog.showErrorDialog(this, "Cannot load template", e);
					} 
					catch (IOException e) 
					{
						JAPDialog.showErrorDialog(this, "Cannot load template", e);
					}
				}
			
		 }
	}
	
	private void exportFile(TermsAndConditionsTemplate template)
	{
		JFileChooser fc = new JFileChooser();
		File suggestedFile = new File(fc.getCurrentDirectory()+File.separator+template.getId()+".xml");
		fc.setSelectedFile(suggestedFile);
		
		int clicked = fc.showSaveDialog(parent);
		switch ( clicked )
		{
			case JFileChooser.APPROVE_OPTION:
			{
				File selectedFile = fc.getSelectedFile();
				boolean confirmed = true;
				if(selectedFile.exists())
				{
					confirmed = 
						JAPDialog.showConfirmDialog(this, "File already exists. Overwrite?",
							JAPDialog.OPTION_TYPE_YES_NO, 
							JAPDialog.MESSAGE_TYPE_QUESTION) == JAPDialog.RETURN_VALUE_YES;
				}
				if(confirmed)
				{
					try 
					{		
						OutputStreamWriter exportWriter = 
							new OutputStreamWriter(new FileOutputStream(selectedFile), "UTF-8");
						
						XMLUtil.write(template.getDocument(), exportWriter);
						exportWriter.flush();
						exportWriter.close();
					} 
					catch (IOException e) 
					{
						JAPDialog.showErrorDialog(this, "IOException occured.", e);
					} 
				}
				break;
			}
			case JFileChooser.CANCEL_OPTION:
			{
				break;
			}
			case JFileChooser.ERROR_OPTION:
			{
				break;
			}
		}
	}
	
	private TermsAndConditionsTemplate getSelectedTemplate()
	{
		Object item = templatesBox.getSelectedItem();
		return (TermsAndConditionsTemplate) ((item != null) ?
				Database.getInstance(TermsAndConditionsTemplate.class).getEntryById(item.toString()) : null);
	}

	private Iterable<TermsAndConditionsTemplate> getAllTemplates()
	{
		return Database.getInstance(TermsAndConditionsTemplate.class).getEntryList();
	}
	
	private void removeOldItem(String id)
	{
		Object currentItem = null;
		for (int i = 0; i < templatesBox.getItemCount(); i++) 
		{
			currentItem = templatesBox.getItemAt(i);
			if( (currentItem != null) && 
				(currentItem instanceof String) &&
				((String) currentItem).equals(id) )
			{
				templatesBox.removeItemAt(i);
				return;
			}
		}
	}
}
