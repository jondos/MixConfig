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
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import logging.LogType;
import gui.JAPHelpContext;
import gui.dialog.JAPDialog;
import gui.GUIUtils;
import javax.swing.JTextField;

/** The <CODE>CascadePanel</CODE> is a panel that lets the user edit settings concerning
 * an entire mix cascade. It should only be made visible when the mix that is being
 * configured is to become the last mix in its cascade.<br>
 * The panel shows two tables: A list of available mixes, and a list of mixes
 * that are currently part of the cascade.<br>
 * The user may use the two arrow buttons between the tables to add and remove
 * entries from the cascade. The buttons to the left of the second table are there
 * to re-arrange the mixes within the cascade.<br>
 * Above the tables, there is a text field where the name of the cascade must be
 * entered, and a button that makes the panel connect to the InfoService host (if
 * configured) to fetch the list of available mixes (a mix is marked available by
 * the InfoService if it has itself registered as on-line, but has not yet been
 * assigned to any cascade).
 */
public class CascadePanel extends MixConfigPanel implements ActionListener, ListSelectionListener,
	ChangeListener
{
	private static final String IMG_ARROW_DOWN = CascadePanel.class.getName() + "_downarrow.gif";
	private static final String IMG_ARROW_UP = CascadePanel.class.getName() + "_uparrow.gif";

	/** A table holding the list of available mixes. */
	private JTable m_availableMixTable;
	/** A table holding the list of configured mixes in the cascade. */
	private JTable m_configuredMixTable;

	/** A button to move a mix closer to the beginning of the cascade */
	private JButton m_moveMixUpButton;

	/** A button to move a mix closer to the end of the cascade */
	private JButton m_moveMixDownButton;

	/** A button to add a mix to the cascade */
	private JButton m_addMixButton;

	/** A button to remove a mix from the cascade */
	private JButton m_remMixButton;

	/** A button that makes the panel fetch a list of available mixes from the
	 * InfoService host
	 */
	private JButton m_recvMixListButton;

	/** A text field for the name of the cascade */
	private JTextField m_tfCascadeName;

	/** Constructs a new instance of <CODE>CascadePanel</CODE> */
	public CascadePanel()
	{
		super("Cascade");
		this.setBorder(new EmptyBorder(10, 10, 10, 10));

		GridBagConstraints constraints = new GridBagConstraints();
		this.setLayout(new GridBagLayout());
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.weightx = 0;
		constraints.weighty = 0;

		constraints.gridx = 0;
		constraints.gridy = 0;

		// Cascade Name JTextField; this field is disabled by selecting a middle mix type
		m_tfCascadeName = new JTextField(20);
		m_tfCascadeName.setName(GeneralPanel.XMLPATH_GENERAL_CASCADENAME);
		m_tfCascadeName.addFocusListener(this);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add(new JLabel("Cascade name"), constraints);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		this.add(m_tfCascadeName, constraints);
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy++;

		m_recvMixListButton = new JButton("Update");
		m_recvMixListButton.addActionListener(this);

		m_availableMixTable = new JTable(new MixListTableModel());

		JScrollPane scrollPane1 = new JScrollPane(m_availableMixTable,
												  ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
												  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		constraints.gridy++;
		this.add(new JLabel("Available mixes:"), constraints);
		constraints.gridx++;
		this.add(m_recvMixListButton, constraints);

		m_availableMixTable.getSelectionModel().addListSelectionListener(this);
		constraints.gridy++;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weighty = 0.5;
		constraints.gridx = 0;
		constraints.gridwidth = 3;
		this.add(scrollPane1, constraints);
		constraints.fill = GridBagConstraints.NONE;
		constraints.weighty = 0;

		ImageIcon downarrow = GUIUtils.loadImageIcon(IMG_ARROW_DOWN);

		m_addMixButton = new JButton(downarrow);
		m_addMixButton.addActionListener(this);
		m_addMixButton.setToolTipText("Use the buttons to move available mixes into your cascade.");

		ImageIcon uparrow = GUIUtils.loadImageIcon(IMG_ARROW_UP);

		m_remMixButton = new JButton(uparrow);
		m_remMixButton.addActionListener(this);
		m_remMixButton.setToolTipText("Use the buttons to move available mixes into your cascade.");

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(m_addMixButton);
		buttonPanel.add(m_remMixButton);
		constraints.gridy++;
		constraints.gridwidth = 3;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.gridx = 0;
		this.add(buttonPanel, constraints);


		m_configuredMixTable = new JTable(new MixListTableModel());

		JScrollPane scrollPane2 = new JScrollPane(m_configuredMixTable,
												  ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
												  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		m_configuredMixTable.getSelectionModel().addListSelectionListener(this);
		m_configuredMixTable.setName("MixCascade");

		constraints.gridx = 0;
		constraints.gridy++;
		constraints.anchor = GridBagConstraints.NORTHWEST;

		this.add(new JLabel("Current cascade (top entry = first mix):"), constraints);

		constraints.gridy++;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weighty = 0.5;
		constraints.gridwidth = 3;
		this.add(scrollPane2, constraints);
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.NONE;

		m_moveMixUpButton = new JButton(GUIUtils.loadImageIcon("arrowUp.gif"));
		m_moveMixUpButton.addActionListener(this);
		m_moveMixUpButton.setToolTipText("Move selected mix up in cascade");
		m_moveMixDownButton = new JButton(GUIUtils.loadImageIcon("arrowDown.gif"));
		m_moveMixDownButton.addActionListener(this);
		m_moveMixDownButton.setToolTipText("Move selected mix down in cascade");
		JPanel buttonPanel2 = new JPanel();
		buttonPanel2.setLayout(new FlowLayout());
		buttonPanel2.add(m_moveMixUpButton);
		buttonPanel2.add(m_moveMixDownButton);
		constraints.gridx = 2;
		constraints.gridy++;
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.anchor = GridBagConstraints.NORTHEAST;
		this.add(buttonPanel2, constraints);
		enableComponents();
	}

	public Vector check()
	{
		Vector errors = new Vector();
		MixListTableModel confMix = (MixListTableModel)this.m_configuredMixTable.getModel();
		if (getConfiguration().getMixType() != MixConfiguration.MIXTYPE_LAST)
		{
			return errors;
		}

		String s = getConfiguration().getValue(GeneralPanel.XMLPATH_GENERAL_CASCADENAME);
		if (s == null || s.equals(""))
		{
			errors.addElement("Cascade Name not entered.");
		}

		if (confMix.getRowCount() > 1)
		{
			String mtype = confMix.getValueAt(0, 3).toString();
			if (!mtype.equals("FirstMix"))
			{
				errors.addElement(new String(
								"The first mix in the cascade does not want to be first mix. " +
								"Please re-arrange the cascade in Cascade Panel."));
			}

			mtype = confMix.getValueAt(confMix.getRowCount() - 1, 3).toString();
			if (!mtype.equals("LastMix"))
			{
				errors.addElement(new String(
								"The last mix in the cascade does not want to be last mix. " +
								"Please re-arrange the cascade in Cascade Panel."));
			}

			for (int i = confMix.getRowCount() - 2; i > 0; i--)
			{
				mtype = confMix.getValueAt(i, 3).toString();
				if (!mtype.equals("MiddleMix"))
				{
					errors.addElement(new String(
									   "One of the mixes in the cascade does not want to be middle mix. " +
									   "Please re-arrange the cascade."));
					break;
				}
			}

			for (int i = 0; i < confMix.getRowCount() - 2; i++)
			{
				Object cascadeLength = confMix.getValueAt(i, 4);
				if (cascadeLength != null && cascadeLength.toString().trim().length() > 0)
				{
					if (new Integer(cascadeLength.toString()).intValue() > confMix.getRowCount())
					{
						errors.addElement(new String(
							"One or more of the mixes in the cascade require(s) a greater " +
							"cascade size. Please add more mixes or remove that mix."));
	                    break;
					}
				}
			}
		}
		else
		{
			errors.addElement(new String("Too few mixes in cascade. Please add at " +
										 "least two mixes in Cascade Panel."));
		}

		return errors;
	}

	public void actionPerformed(ActionEvent e)
	{
		try
		{
			if (e.getSource() == this.m_addMixButton)
			{
				moveMix(this.m_availableMixTable, this.m_configuredMixTable);
			}
			else if (e.getSource() == this.m_recvMixListButton)
			{
				// clear the table first (this is necessary)
				m_availableMixTable.setModel(new MixListTableModel());
				m_availableMixTable.setModel(new MixListTableModel(recvMixList()));
			}
			else if (e.getSource() == this.m_remMixButton)
			{
				moveMix(this.m_configuredMixTable, this.m_availableMixTable);
			}
			else if (e.getSource() == this.m_moveMixUpButton)
			{
				int i = this.m_configuredMixTable.getSelectedRow();
				( (MixListTableModel)this.m_configuredMixTable.getModel()).moveRow(i, i - 1);
			}
			else if (e.getSource() == this.m_moveMixDownButton)
			{
				int i = this.m_configuredMixTable.getSelectedRow();
				( (MixListTableModel)this.m_configuredMixTable.getModel()).moveRow(i, i + 1);
			}
			save(m_configuredMixTable);
			enableComponents();
		}
		catch (ConnectException ce)
		{
			if (ce.getMessage().trim().equalsIgnoreCase("Connection refused"))
			{
				MixConfig.info("InfoService not available", new String[]
							   {
							   "The InfoService is not responding.",
							   "You may continue configuration now and retry later"});
			}
		}
		catch (Exception ex)
		{

			JAPDialog.showErrorDialog(MixConfig.getMainWindow(), null, LogType.GUI, ex);
		}
	}

	public void valueChanged(ListSelectionEvent e)
	{
		enableComponents();
	}

	public String getHelpContext()
	{
		return JAPHelpContext.INDEX;
	}

	/** Moves the currently selected entry from one table to another.
	 * @param src The source table from which the entry is to be moved away
	 * @param dest The destination table
	 */
	private static void moveMix(JTable src, JTable dest)
	{
		MixListTableModel smltm = (MixListTableModel) src.getModel();
		MixListTableModel dmltm = (MixListTableModel) dest.getModel();

		Object mix[] = smltm.getRow(src.getSelectedRow());
		smltm.removeRow(src.getSelectedRow());
		dmltm.addRow(mix);
	}

	protected void enableComponents()
	{
		String hostName = null, hostPort = null;
		int c = m_configuredMixTable.getRowCount() - 1;
		int s = m_configuredMixTable.getSelectedRow();


		if (getConfiguration() != null)
		{
			hostName = getConfiguration().getValue("Network/InfoService/Host");
			if (hostName == null || hostName.equals(""))
			{
				hostName = getConfiguration().getValue("Network/InfoService/IP");
			}

			hostPort = getConfiguration().getValue("Network/InfoService/Port");
		}

		m_recvMixListButton.setEnabled(hostName != null && hostPort != null);
		m_addMixButton.setEnabled(m_availableMixTable.getSelectedRowCount() == 1);
		m_remMixButton.setEnabled(m_configuredMixTable.getSelectedRowCount() == 1 && s < c);
		m_moveMixUpButton.setEnabled(s > 0 && s < c);
		m_moveMixDownButton.setEnabled(s >= 0 && s < c - 1);
	}

	/** Fetches the list of available mixes from the InfoService host and returns it as
	 * an XML/DOM element.
	 * @throws MalformedURLException If the URL pointing to the InfoService is invalid
	 * @throws ParserConfigurationException If an error occurs during parsing the answer from the InfoService
	 * @throws IOException If an error occurs during communication
	 * @throws SAXException If an error occurs during parsing the answer from the InfoService
	 * @return An XML/DOM element containing the list of mixes
	 */
	private Element recvMixList() throws MalformedURLException, ParserConfigurationException, IOException,
		SAXException
	{
		String hostName = getConfiguration().getValue("Network/InfoService/Host");
		if (hostName == null || hostName.equals(""))
		{
			hostName = getConfiguration().getValue("Network/InfoService/IP");
		}

		String hostPort = getConfiguration().getValue("Network/InfoService/Port");

		URL infoService = new URL("http", hostName, Integer.valueOf(hostPort).intValue(),
								  "/availablemixes");
		HttpURLConnection conn = (HttpURLConnection) infoService.openConnection();

		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document mixListDoc = db.parse(conn.getInputStream());
		conn.disconnect();

		NodeList nl = mixListDoc.getDocumentElement().getElementsByTagName("Mix");
		if (nl.getLength() == 0)
		{
			MixConfig.info("No mixes available", new String[]
						   {
						   "The InfoService returned an empty list.",
						   "You may continue configuration now",
						   "and retry later when enough mixes have",
						   "registered themselves with the InfoService."});
		}
		return mixListDoc.getDocumentElement();
	}

	/** This inner class represents the data model for the two tables and holds the mix
	 * list entries.
	 */
	public class MixListTableModel extends AbstractTableModel
	{
		/** The names of the columns in the model */
		private final String columnName[] =
			{"Mix ID", "Mix name", "Location", "Type"};
			//"Mix ID", "Mix name", "Location", "Type", "Desired cascade length"};

		/** The list of mix entries */
		Vector mixList = new Vector();

		/** Constructs a new empty instance of <CODE>MixListTableModel</CODE> */
		public MixListTableModel()
		{
			super();
		}

		/** Constructs a new instance of <code>MixListTableModel</code>.
		 * @param a_mixList The list of mixes to be contained in the table model as an XML/DOM element
		 */
		public MixListTableModel(Element a_mixList)
		{
			NodeList nl = a_mixList.getElementsByTagName("Mix");
			for (int i = 0; i < nl.getLength(); i++)
			{
				String strMixType = getElementData((Element)(nl.item(i)), "MixType");
				//Do not show LastMixes
				if (!strMixType.equalsIgnoreCase("LastMix"))
				{
					addRow( (Element) nl.item(i));
				}
			}
		}

		public int getColumnCount()
		{
			//return 5;
			return 4;
		}

		public int getRowCount()
		{
			if (mixList == null)
			{
				return 0;
			}
			return mixList.size();
		}

		public Class getColumnClass(int columnIndex)
		{
			return new String().getClass();
		}

		public String getColumnName(int columnIndex)
		{
			return columnName[columnIndex];
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			if (columnIndex > getColumnCount() - 1 || rowIndex > getRowCount() - 1)
			{
				return null;
			}
			Object mle[] = (Object[]) mixList.elementAt(rowIndex);
			return mle[columnIndex];
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex)
		{
			Object mle[] = (Object[]) mixList.elementAt(rowIndex);
			mle[columnIndex] = value;
			fireTableCellUpdated(rowIndex, columnIndex);
		}

		public boolean isCellEditable(int row, int col)
		{
			return false;
		}

		/** Gets the entry at the specified index
		 * @param rowIndex The index of the row to be returned
		 * @return The row at the specified index as an array of <code>Object</code>
		 */
		public Object[] getRow(int rowIndex)
		{
			return (Object[]) mixList.elementAt(rowIndex);
		}

		/** Adds an entry to the table model. The array representing the entry must have 5
		 * fields: Mix ID, mix name, location (City), type (FirstMix, MiddleMix, LastMix),
		 * and desired cascade length (a <code>String</code> reprentation of an int value, or
		 * <code>null</code>).
		 * @param a_mix An array of <B>Object</B> holding the mix data.
		 */
		public void addRow(Object a_mix[])
		{
			// check if that mix is already anywhere in the two tables;
			// if so, don't add it
			MixListTableModel m = (MixListTableModel) m_availableMixTable.getModel();
			for (int j = 0; j < 2; j++)
			{
				for (int i = 0; i < m.getRowCount(); i++)
				{
					if (m.getValueAt(i, 0) != null && m.getValueAt(i, 0).equals(a_mix[0]))
					{
						return;
					}
				}
				m = (MixListTableModel) m_configuredMixTable.getModel();
			}

			if (a_mix[3].toString().equalsIgnoreCase("FirstMix"))
			{
				mixList.insertElementAt(a_mix, 0);
				fireTableRowsInserted(0, 0);
			}
			else if (a_mix[3].toString().equalsIgnoreCase("LastMix"))
			{
				mixList.addElement(a_mix);
				fireTableRowsInserted(mixList.size() -1, mixList.size() -1);
			}
			else
			{
				int i;
				String strMixType;
				for (i = 0; i < mixList.size(); i++)
				{
					strMixType = ((Object[])mixList.elementAt(i))[3].toString();

					if (strMixType.equalsIgnoreCase(
									   MixConfiguration.getMixTypeAsString(MixConfiguration.MIXTYPE_FIRST)))
					{
						continue;
					}
					if (strMixType.equalsIgnoreCase(MixConfiguration.getMixTypeAsString(
									   MixConfiguration.MIXTYPE_MIDDLE)) ||
						strMixType.equalsIgnoreCase(MixConfiguration.getMixTypeAsString(
											  MixConfiguration.MIXTYPE_LAST)))
					{
						break;
					}
				}
				mixList.insertElementAt(a_mix, i);
				fireTableRowsInserted(i, i);
			}
		}

		/** Adds an entry to the table model. The <code>Element</code> representing the entry must have
		 * one attribute named "id" containing the mix ID, and one or more of the following child
		 * nodes:<br>
		 * <ul>
		 * <li><B>Name</B> (containing a text node with the mix's name)</li>
		 * <li><B>MixType</B> (containing a text node with the mix's type)</li>
		 * <li><B>Location</B> (containing a child node named <B>City</B> with a text node with the mix's type)</li>
		 * <li><B>MinCascadeLength</B> (containing a text node with an integer value)</li>
		 * </ul>
		 * If one of the nodes is missing, the corresponding field in the table entry is
		 * left blank. Additional other nodes will be ignored.
		 * @param a_mix The DOM element containing the mix data
		 */
		public void addRow(Element a_mix)
		{
			NodeList nl;
			Object mix[] = new Object[getColumnCount()];

			mix[0] = a_mix.getAttribute("id");

			mix[1] = getElementData(a_mix, "Name");

			nl = a_mix.getElementsByTagName("Location");
			Element m = (Element) nl.item(0);
			mix[2] = getElementData(m, "City");

			mix[3] = getElementData(a_mix, "MixType");

			//mix[4] = getElementData(a_mix, "MinCascadeLength");

			addRow(mix);
		}

		/** Removes the specified object from the list of entries.
		 * @param a_mle An array of <CODE>Object</CODE> representing a mix list entry
		 */
		public void removeRow(Object a_mle)
		{
			int i = mixList.indexOf(a_mle);
			mixList.removeElement(a_mle);
			fireTableRowsDeleted(i, i);
		}

		/** Removes the object at the specified index from the list of entries.
		 * @param rowIndex The index of the entry to be removed
		 */
		public void removeRow(int rowIndex)
		{
			mixList.removeElementAt(rowIndex);
			fireTableRowsDeleted(rowIndex, rowIndex);
		}

		/** Moves a row from one index to another. The other rows are shifted accordingly.
		 * @param src The index of the row to be moved
		 * @param dest The destination index
		 */
		public void moveRow(int src, int dest)
		{
			Object o = mixList.elementAt(src);
			mixList.removeElementAt(src);
			mixList.insertElementAt(o, dest);
			m_configuredMixTable.getSelectionModel().setSelectionInterval(dest, dest);
			if (src < dest)
			{
				fireTableChanged(new TableModelEvent(this, src, dest));
			}
			else
			{
				fireTableChanged(new TableModelEvent(this, dest, src));
			}
		}

		/** Converts the data in this model to a XML/DOM structure.
		 * @param a_doc The document for which the resulting element will be created
		 * @return An XML/DOM <CODE>DocumentFragment</CODE> object containing the mix list entries
		 * as elements (<CODE>&lt;Mix id=&quot;...&quot;&gt;...&lt;/Mix&gt</CODE>)
		 */
		public Node toXmlElement(Document a_doc)
		{
			Object mle[];
			Element f, g, h;
			DocumentFragment e;
			Enumeration list;

			e = a_doc.createDocumentFragment();

			list = mixList.elements();
			while (list.hasMoreElements())
			{
				mle = (Object[]) list.nextElement();
				String mixId = getConfiguration().getValue("General/MixID");
				if (mle[0] == null || mle[0].equals(mixId))
				{
					continue;
				}
				f = a_doc.createElement("Mix");
				e.appendChild(f);
				if (mle[0] != null)
				{
					f.setAttribute("id", mle[0].toString());
				}
				if (mle[1] != null)
				{
					g = a_doc.createElement("Name");
					g.appendChild(a_doc.createTextNode(mle[1].toString()));
					f.appendChild(g);
				}
				if (mle[2] != null)
				{
					g = a_doc.createElement("Location");
					f.appendChild(g);
					h = a_doc.createElement("City");
					g.appendChild(h);
					h.appendChild(a_doc.createTextNode(mle[2].toString()));
				}
				if (mle[3] != null)
				{
					g = a_doc.createElement("MixType");
					f.appendChild(g);
					g.appendChild(a_doc.createTextNode(mle[3].toString()));
				}
				if (mle[4] != null)
				{
					g = a_doc.createElement("MinCascadeLength");
					f.appendChild(g);
					g.appendChild(a_doc.createTextNode(mle[4].toString()));
				}
			}
			return e;
		}

		/** Searches the specified element for a child node with the specified name, and
		 * returns the first text node under that element (if found; <CODE>null</CODE>
		 * otherwise).
		 * @param a_parent The parent element to be searched
		 * @param a_elementName The name of the child element whose text node is to be returned
		 * @return The element's text node as a <CODE>String</CODE>, or <CODE>null</CODE> if there
		 * is no such node
		 */
		private String getElementData(Element a_parent, String a_elementName)
		{
			NodeList nl = a_parent.getElementsByTagName(a_elementName);
			if (nl.getLength() == 0)
			{
				return null;
			}

			Node e = (Element) nl.item(0);

			e = e.getFirstChild();

			while (e != null)
			{
				if (e instanceof Text)
				{
					return ( (Text) e).getData().trim();
				}
				e = e.getNextSibling();
			}
			return null;
		}
	}

	/**
	 * @todo This implementation is only needed because of a bug in the dynamic configuration
	 * of the mix.
	 * @param a_textField JTextField
	 */
	protected void load(JTextField a_textField)
	{
		super.load(a_textField);

		if (a_textField == m_tfCascadeName && a_textField.getText() != null &&
			a_textField.getText().equals(GeneralPanel.PSEUDO_CASCADE_NAME))
		{
			a_textField.setText("");
		}
	}

	protected void load(JTable a_table)
	{
		if (a_table == m_configuredMixTable)
		{
			Object myself[] = new Object[5];
			myself[0] = getConfiguration().getValue("General/MixID");
			myself[1] = getConfiguration().getValue(GeneralPanel.XMLPATH_GENERAL_MIXNAME);
			myself[2] = getConfiguration().getValue(OwnCertificatesPanel.XMLPATH_LOCATION_CITY);
			myself[3] = getConfiguration().getValue(GeneralPanel.XMLPATH_GENERAL_MIXTYPE);
			if (myself[3] != null)
			{
				Integer t = new Integer(myself[3].toString());
				myself[3] = MixConfiguration.getMixTypeAsString(t.intValue());
			}
			myself[4] = new String();

			// clear the tables first (this is necessary)
			m_configuredMixTable.setModel(new MixListTableModel());
			m_availableMixTable.setModel(new MixListTableModel());

			NodeList n = getConfiguration().getDocument().getElementsByTagName("MixCascade");
			if (n.getLength() > 0)
			{
				m_configuredMixTable.setModel(new MixListTableModel( (Element) n.item(0)));
			}

			( (MixListTableModel) m_configuredMixTable.getModel()).addRow(myself);
		}

	}

	public void setConfiguration(MixConfiguration a_mixConf) throws IOException
	{
		// make sure we are listening to change events only once
		super.setConfiguration(a_mixConf);
		a_mixConf.removeChangeListener(this);
		a_mixConf.addChangeListener(this);
		setEnabled(getConfiguration().getMixType() == MixConfiguration.MIXTYPE_LAST &&
				   getConfiguration().isAutoConfigurationAllowed());
	}

	public void stateChanged(ChangeEvent a_event)
	{

		if (a_event instanceof ConfigurationEvent)
		{
			int mixType = getConfiguration().getMixType();

			ConfigurationEvent ce = (ConfigurationEvent) a_event;
			int col = -1;
			if (ce.getChangedAttribute().endsWith("MixID"))
			{
				col = 0;
			}
			else if (ce.getChangedAttribute().endsWith("MixName"))
			{
				col = 1;
			}
			else if (ce.getChangedAttribute().endsWith("City"))
			{
				col = 2;
			}
			else if (ce.getChangedAttribute().equals("Network/InfoService/AllowAutoConfiguration"))
			{
				setEnabled(mixType == MixConfiguration.MIXTYPE_LAST &&
						   getConfiguration().isAutoConfigurationAllowed());
			}
			else if (ce.getChangedAttribute().equals(GeneralPanel.XMLPATH_GENERAL_MIXTYPE))
			{
				col = 3;
				setEnabled(mixType == MixConfiguration.MIXTYPE_LAST &&
						   getConfiguration().isAutoConfigurationAllowed());
			}
			else if (ce.getChangedAttribute().endsWith("MinCascadeLength"))
			{
				col = 4;
			}
			else if (ce.getChangedAttribute().equals(GeneralPanel.XMLPATH_GENERAL_CASCADENAME))
			{
				String value = getConfiguration().getValue(GeneralPanel.XMLPATH_GENERAL_CASCADENAME);
				if (value == null || !value.equals(m_tfCascadeName.getText()))
				{
					load(m_tfCascadeName);
				}
			}

			if (col >= 0 && m_configuredMixTable.getRowCount() > 0)
			{

				String v;
				if (ce.getNewValue() == null)
				{ // this could be a problem...
					v = "";
				}
				else
				{
					v = ce.getNewValue().toString();
				}

				if (col == 3)
				{
					Integer t = new Integer(v);
					v = MixConfiguration.getMixTypeAsString(t.intValue());
				}

				m_configuredMixTable.setValueAt(v, m_configuredMixTable.getRowCount() - 1, col);
			}

			enableComponents();
		}
	}
}
