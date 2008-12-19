package mixconfig.tools;

import gui.JAPHelpContext;
import gui.dialog.JAPDialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.EventObject;

import javax.swing.CellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import mixconfig.panels.CertPanel;
import mixconfig.tools.dataretention.JTreeTable;
import mixconfig.tools.dataretention.LogFilesModel;

public class DataRetentionLogDecrypt extends JAPDialog

{

public DataRetentionLogDecrypt(Frame parent)
{
	super(parent, "Proccessing Tool for retained Data", true);
	initComponents();
	pack();
	setVisible(true, false);
}

private void initComponents() {
	GridBagConstraints constraintsContentPane=new GridBagConstraints();
	GridBagConstraints constraintsPanel=new GridBagConstraints();

	getContentPane().setLayout(new GridBagLayout());
	
	JPanel panelLogfile = new JPanel(new GridBagLayout());
	panelLogfile.setBorder(new TitledBorder("Allgemeine Angaben"));
	constraintsContentPane.gridx=0;
	constraintsContentPane.gridy=0;
	constraintsContentPane.anchor=GridBagConstraints.WEST;
	constraintsContentPane.weightx=1.0;
	constraintsContentPane.fill=GridBagConstraints.HORIZONTAL;
	constraintsContentPane.insets=new Insets(10,10,10,10);
	getContentPane().add(panelLogfile, constraintsContentPane);
	
	JLabel label=new JLabel("Verzeichnis der Logdateien:");
	constraintsPanel.gridx=0;
	constraintsPanel.gridy=0;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	panelLogfile.add(label,constraintsPanel);
	
	label=new JLabel("Schluesselspeicher:");
	constraintsPanel.gridx=0;
	constraintsPanel.gridy=1;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	panelLogfile.add(label,constraintsPanel);

	JTextField tf=new JTextField("/mnt/sdb1/logfiles");
	constraintsPanel.gridx=1;
	constraintsPanel.gridy=0;
	constraintsPanel.weightx=1.0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelLogfile.add(tf,constraintsPanel);
	
	label=new JLabel("Chipkarte in Kobil Lesegeraet");
	Font f=label.getFont();
	label.setFont(f.deriveFont(Font.BOLD));
	label.setForeground(Color.blue);
	constraintsPanel.gridx=1;
	constraintsPanel.gridy=1;
	constraintsPanel.weightx=1.0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	panelLogfile.add(label,constraintsPanel);
	
	JButton bttn=new JButton("Suchen ...");
	constraintsPanel.gridx=2;
	constraintsPanel.gridy=0;
	constraintsPanel.weightx=0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelLogfile.add(bttn,constraintsPanel);

	bttn=new JButton("Auswaehlen ...");
	constraintsPanel.gridx=2;
	constraintsPanel.gridy=1;
	constraintsPanel.weightx=0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelLogfile.add(bttn,constraintsPanel);

	
	
	JPanel panelRequest = new JPanel(new GridBagLayout());
	panelRequest.setBorder(new TitledBorder("Anfrage"));
	constraintsContentPane.gridy=1;
	getContentPane().add(panelRequest, constraintsContentPane);
	
	label=new JLabel("Datum:");
	constraintsPanel.gridx=0;
	constraintsPanel.gridy=0;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	panelRequest.add(label,constraintsPanel);
	
	label=new JLabel("Uhrzeit:");
	constraintsPanel.gridx=0;
	constraintsPanel.gridy=1;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	panelRequest.add(label,constraintsPanel);

	tf=new JTextField(2);
	constraintsPanel.gridx=1;
	constraintsPanel.gridy=0;
	constraintsPanel.weightx=0;
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelRequest.add(tf,constraintsPanel);

	tf=new JTextField(2);
	constraintsPanel.gridx=1;
	constraintsPanel.gridy=1;
	constraintsPanel.weightx=0;
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelRequest.add(tf,constraintsPanel);

	tf=new JTextField(2);
	constraintsPanel.gridx=3;
	constraintsPanel.gridy=0;
	constraintsPanel.weightx=0;
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelRequest.add(tf,constraintsPanel);

	tf=new JTextField(2);
	constraintsPanel.gridx=3;
	constraintsPanel.gridy=1;
	constraintsPanel.weightx=0;
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelRequest.add(tf,constraintsPanel);
	
	tf=new JTextField(4);
	constraintsPanel.gridx=5;
	constraintsPanel.gridy=0;
	constraintsPanel.weightx=0;
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelRequest.add(tf,constraintsPanel);
	
	tf=new JTextField(4);
	constraintsPanel.gridx=5;
	constraintsPanel.gridy=1;
	constraintsPanel.weightx=0;
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelRequest.add(tf,constraintsPanel);
	
	label=new JLabel(".");
	constraintsPanel.gridx=2;
	constraintsPanel.gridy=0;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	constraintsPanel.insets=new Insets(0,0,0,0);
	panelRequest.add(label,constraintsPanel);

	label=new JLabel(".");
	constraintsPanel.gridx=4;
	constraintsPanel.gridy=0;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	panelRequest.add(label,constraintsPanel);

	label=new JLabel(":");
	constraintsPanel.gridx=2;
	constraintsPanel.gridy=1;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	panelRequest.add(label,constraintsPanel);

	label=new JLabel(":");
	constraintsPanel.gridx=4;
	constraintsPanel.gridy=1;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	panelRequest.add(label,constraintsPanel);
	
	JComboBox cb=new JComboBox();
	cb.setEditable(false);
	cb.addItem("UTC");
	constraintsPanel.gridx=6;
	constraintsPanel.gridy=1;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	panelRequest.add(cb,constraintsPanel);
	
	label=new JLabel();
	constraintsPanel.gridx=7;
	constraintsPanel.gridy=1;
	constraintsPanel.anchor=GridBagConstraints.WEST;
	constraintsPanel.weightx=1.0;
	panelRequest.add(label,constraintsPanel);

	bttn=new JButton("Suche");
	bttn.setFont(bttn.getFont().deriveFont(Font.BOLD));
	constraintsPanel.gridx=8;
	constraintsPanel.gridy=2;
	constraintsPanel.weightx=0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	constraintsPanel.ipadx=10;
	constraintsPanel.ipady=10;
	constraintsPanel.fill=GridBagConstraints.HORIZONTAL;
	panelRequest.add(bttn,constraintsPanel);
	
	
	JPanel panelResult = new JPanel(new GridBagLayout());
	panelResult.setBorder(new TitledBorder("Ergebnis"));
	constraintsContentPane.gridy=2;
	constraintsContentPane.weighty=1.0;
	constraintsContentPane.fill=GridBagConstraints.BOTH;
	getContentPane().add(panelResult, constraintsContentPane);
	
	JTreeTable treetable=new JTreeTable(new LogFilesModel());
	constraintsPanel.gridx=0;
	constraintsPanel.gridy=0;
	constraintsPanel.weightx=1.0;
	constraintsPanel.weighty=1.0;
	constraintsPanel.insets=new Insets(10,10,10,10);
	constraintsPanel.fill=GridBagConstraints.BOTH;
	panelResult.add(new JScrollPane(treetable),constraintsPanel);
	
	
	
	JPanel panelButtons = new JPanel(new GridLayout(1,2,20,0));
	constraintsContentPane.gridy=3;
	constraintsContentPane.weighty=0;
	constraintsContentPane.fill=GridBagConstraints.NONE;
	constraintsContentPane.anchor=GridBagConstraints.SOUTHEAST;
	getContentPane().add(panelButtons, constraintsContentPane);

	bttn=new JButton("Ergebnis speichern ...");
	panelButtons.add(bttn);

	bttn=new JButton("Beenden");
	panelButtons.add(bttn);

}




}
