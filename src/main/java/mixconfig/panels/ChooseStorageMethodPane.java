package mixconfig.panels;

import gui.dialog.DialogContentPane;
import gui.dialog.DialogContentPaneOptions;
import gui.dialog.JAPDialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

public  class ChooseStorageMethodPane extends DialogContentPane implements
	DialogContentPane.IWizardSuitable
{
	public static final String MSG_CONFIRM_OVERWRITE = ChooseStorageMethodPane.class.getName() + "_confirmOverwriting";
	
	private JRadioButton m_btnFile;
	private JRadioButton m_btnClip;
	
	public ChooseStorageMethodPane(JAPDialog a_dialog, String a_strText)
	{
		super(a_dialog, a_strText,
			  new DialogContentPaneOptions(DialogContentPane.OPTION_TYPE_OK_CANCEL));
		GridBagConstraints constr = new GridBagConstraints();
		ButtonGroup group = new ButtonGroup();
		m_btnFile = new JRadioButton("File");
		m_btnClip = new JRadioButton("Clipboard");
		group.add(m_btnFile);
		group.add(m_btnClip);
		m_btnFile.setSelected(true);
		constr.gridx = 0;
		constr.gridy = 0;
		constr.anchor = GridBagConstraints.WEST;
		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.weightx = 0;
		getContentPane().setLayout(new GridBagLayout());
		getContentPane().add(m_btnFile, constr);
		constr.gridy++;
		getContentPane().add(m_btnClip, constr);
	}
	public boolean isMethodFile()
	{
		return m_btnFile.isSelected();
	}
}