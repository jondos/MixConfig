package mixconfig;


import java.util.Vector;
import javax.swing.JTable;
import javax.swing.Box;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.table.AbstractTableModel;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import javax.swing.JLabel;
import javax.swing.event.TableModelEvent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Enumeration;
import org.w3c.dom.DocumentFragment;
import java.net.ConnectException;
import javax.swing.border.TitledBorder;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

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
    /** A table holding the list of available mixes. */
	JTable m_availableMixTable;
        /** A table holding the list of configured mixes in the cascade. */
	JTable m_configuredMixTable;

        /** A button to move a mix closer to the beginning of the cascade */
        JButton m_moveMixUpButton;

        /** A button to move a mix closer to the end of the cascade */
        JButton m_moveMixDownButton;

        /** A button to add a mix to the cascade */
        JButton m_addMixButton;

        /** A button to remove a mix from the cascade */
        JButton m_remMixButton;

        /** A button that makes the panel fetch a list of available mixes from the
         * InfoService host
         */
	JButton
		m_recvMixListButton;
        /** A text field for the name of the cascade */
	JTextField m_cascadeName;

        /** Constructs a new instance of <CODE>CascadePanel</CODE> */
	public CascadePanel()
	{
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		BoxLayout vb = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(vb);

		JPanel c = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel jl = new JLabel("Name of new cascade: ");
		jl.setPreferredSize(new Dimension(200, 30));
		m_cascadeName = new JTextField();
		m_cascadeName.setName("General/CascadeName");
		m_cascadeName.setPreferredSize(new Dimension(200, 30));
		m_cascadeName.addFocusListener(this);
		c.add(jl);
		c.add(m_cascadeName);
		add(c);

		add(Box.createVerticalStrut(10));

		JLabel infoLabel = new JLabel("Use the buttons to move available mixes into your cascade.");
		Box b = Box.createHorizontalBox();
		b.add(infoLabel);
		b.add(Box.createHorizontalGlue());
		add(b);

		add(Box.createVerticalStrut(10));

		Box updateBox = Box.createHorizontalBox();
		m_recvMixListButton = new JButton("Fetch list of available mixes from InfoService");
		m_recvMixListButton.addActionListener(this);
		updateBox.add(this.m_recvMixListButton);
		updateBox.add(Box.createHorizontalGlue());
		add(updateBox);

		add(Box.createVerticalStrut(10));

		Box tableBox = Box.createVerticalBox();

		m_availableMixTable = new JTable(new MixListTableModel());

		JScrollPane scrollPane1 = new JScrollPane(m_availableMixTable,
												  ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
												  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		scrollPane1.setBorder(new TitledBorder("Available mixes"));

		tableBox.add(scrollPane1);

		tableBox.add(Box.createVerticalStrut(5));

		m_availableMixTable.getSelectionModel().addListSelectionListener(this);

		Box buttonBox = Box.createHorizontalBox();

		ImageIcon downarrow = MixConfig.loadImage("downarrow.gif");

		m_addMixButton = new JButton(downarrow);
		m_addMixButton.addActionListener(this);

		ImageIcon uparrow = MixConfig.loadImage("uparrow.gif");

		m_remMixButton = new JButton(uparrow);
		m_remMixButton.addActionListener(this);

		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(m_addMixButton);
		buttonBox.add(Box.createHorizontalStrut(10));
		buttonBox.add(m_remMixButton);
		buttonBox.add(Box.createHorizontalGlue());

		tableBox.add(buttonBox);
		tableBox.add(Box.createVerticalStrut(5));

		m_configuredMixTable = new JTable(new MixListTableModel());

		Box confBox = Box.createHorizontalBox();
		JScrollPane scrollPane2 = new JScrollPane(m_configuredMixTable,
												  ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
												  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		m_configuredMixTable.getSelectionModel().addListSelectionListener(this);
		m_configuredMixTable.setName("MixCascade");

		scrollPane2.setBorder(new TitledBorder("Current cascade (top entry = first mix)"));

		confBox.add(Box.createHorizontalGlue());
		confBox.add(scrollPane2);

		tableBox.add(confBox);

		tableBox.add(Box.createHorizontalStrut(5));

		Box buttonBox2 = Box.createVerticalBox();

		m_moveMixUpButton = new JButton(uparrow);
		m_moveMixUpButton.addActionListener(this);

		m_moveMixDownButton = new JButton(downarrow);
		m_moveMixDownButton.addActionListener(this);

		buttonBox2.add(Box.createVerticalGlue());
		buttonBox2.add(m_moveMixUpButton);
		buttonBox2.add(Box.createVerticalStrut(10));
		buttonBox2.add(m_moveMixDownButton);
		buttonBox2.add(Box.createVerticalGlue());

		confBox.add(buttonBox2);
		//confBox.add(Box.createHorizontalStrut(5));

		add(tableBox);

		enableComponents();
	}

	public Vector check()
	{
		Vector errors = new Vector();
		MixListTableModel confMix = (MixListTableModel)this.m_configuredMixTable.getModel();
		Integer t = new Integer(getConfiguration().getAttribute("General/MixType"));
		if (t.intValue() != MixConfiguration.MIXTYPE_LAST)
		{
			return errors;
		}

		String cname = getConfiguration().getAttribute("General/CascadeName");
		if (cname == null || cname.equals(""))
		{
			errors.addElement("Please enter a name for the new cascade in Cascade Panel.");

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
				}
			}

			for (int i = 0; i < confMix.getRowCount() - 2; i++)
			{
				Integer cl = new Integer(confMix.getValueAt(i,4).toString());
				if(cl.intValue() > confMix.getRowCount())
					errors.addElement(new String(
						"One or more of the mixes in the cascade require(s) a greater " +
						"cascade size. Please add more mixes or remove that mix."));
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

			MixConfig.handleException(ex);
		}
	}

	public void valueChanged(ListSelectionEvent e)
	{
		enableComponents();
	}

        /** Moves the currently selected entry from one table to another.
         * @param src The source table from which the entry is to be moved away
         * @param dest The destination table
         */
	private void moveMix(JTable src, JTable dest)
	{
		MixListTableModel smltm = (MixListTableModel) src.getModel();
		MixListTableModel dmltm = (MixListTableModel) dest.getModel();

		int r = src.getSelectedRow();
		Object mix[] = smltm.getRow(r);
		smltm.removeRow(r);
		dmltm.addRow(mix);
	}

	protected void enableComponents()
	{
		String hostName = null, hostPort = null;
		int c = m_configuredMixTable.getRowCount() - 1;
		int s = m_configuredMixTable.getSelectedRow();
		int sc = m_configuredMixTable.getSelectedRowCount();

		if (getConfiguration() != null)
		{
			hostName = getConfiguration().getAttribute("Network/InfoService/Host");
			if (hostName == null || hostName.equals(""))
			{
				hostName = getConfiguration().getAttribute("Network/InfoService/IP");
			}

			hostPort = getConfiguration().getAttribute("Network/InfoService/Port");
		}

		m_recvMixListButton.setEnabled(hostName != null && hostPort != null);
		m_addMixButton.setEnabled(m_availableMixTable.getSelectedRowCount() == 1);
		m_remMixButton.setEnabled(sc == 1 && s < c);
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
		String hostName = getConfiguration().getAttribute("Network/InfoService/Host");
		if (hostName == null || hostName.equals(""))
		{
			hostName = getConfiguration().getAttribute("Network/InfoService/IP");
		}

		String hostPort = getConfiguration().getAttribute("Network/InfoService/Port");

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
			{
			"Mix ID", "Mix name", "Location", "Type", "Desired cascade length"};

                        /** The list of mix entries */
		Vector mixList = new Vector();

                /** Constructs a new empty instance of <CODE>MixListTableModel</CODE> */
		public MixListTableModel()
		{
		}

                /** Constructs a new instance of <code>MixListTableModel</code>.
                 * @param a_mixList The list of mixes to be contained in the table model as an XML/DOM element
                 */
		public MixListTableModel(Element a_mixList)
		{
			NodeList nl = a_mixList.getElementsByTagName("Mix");
			for (int i = 0; i < nl.getLength(); i++)
			{
				addRow( (Element) nl.item(i));
			}
		}

		public int getColumnCount()
		{
			return 5;
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

			int i = Math.max(mixList.size(), 1);
			if (mixList.size() > 0)
			{
				mixList.insertElementAt(a_mix, mixList.size() - 1);
			}
			else
			{
				mixList.addElement(a_mix);
			}
			fireTableRowsInserted(mixList.size(), mixList.size());
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

			mix[4] = getElementData(a_mix, "MinCascadeLength");

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
                 * @param d The document for which the resulting element will be created
                 * @return An XML/DOM <CODE>DocumentFragment</CODE> object containing the mix list entries
                 * as elements (<CODE>&lt;Mix id=&quot;...&quot;&gt;...&lt;/Mix&gt</CODE>)
                 */
		public Node createAsElement(Document d)
		{
			Object mle[];
			Element f, g, h;
			DocumentFragment e;
			Enumeration list;

			e = d.createDocumentFragment();

			list = mixList.elements();
			while (list.hasMoreElements())
			{
				mle = (Object[]) list.nextElement();
				String mixId = getConfiguration().getAttribute("General/MixID");
				if (mle[0] == null || mle[0].equals(mixId))
				{
					continue;
				}
				f = d.createElement("Mix");
				e.appendChild(f);
				if (mle[0] != null)
				{
					f.setAttribute("id", mle[0].toString());
				}
				if (mle[1] != null)
				{
					g = d.createElement("Name");
					g.appendChild(d.createTextNode(mle[1].toString()));
					f.appendChild(g);
				}
				if (mle[2] != null)
				{
					g = d.createElement("Location");
					f.appendChild(g);
					h = d.createElement("City");
					g.appendChild(h);
					h.appendChild(d.createTextNode(mle[2].toString()));
				}
				if (mle[3] != null)
				{
					g = d.createElement("MixType");
					f.appendChild(g);
					g.appendChild(d.createTextNode(mle[3].toString()));
				}
				if (mle[4] != null)
				{
					g = d.createElement("MinCascadeLength");
					f.appendChild(g);
					g.appendChild(d.createTextNode(mle[4].toString()));
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

	protected void load(JTable a_table)
	{
		if (a_table == m_configuredMixTable)
		{
			Object myself[] = new Object[5];
			myself[0] = getConfiguration().getAttribute("General/MixID");
			myself[1] = getConfiguration().getAttribute("General/MixName");
			myself[2] = getConfiguration().getAttribute("Description/Location/City");
			myself[3] = getConfiguration().getAttribute("General/MixType");
			if(myself[3] != null)
			{
				Integer t = new Integer(myself[3].toString());
				myself[3] = MixConfiguration.MIXTYPE_NAME[t.intValue()];
			}
			myself[4] = new String();

			// clear the tables first (this is necessary)
			m_configuredMixTable.setModel(new MixListTableModel());
			m_availableMixTable.setModel(new MixListTableModel());

			Document d = this.getConfiguration().getDocument();
			NodeList n = d.getElementsByTagName("MixCascade");
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
		setEnabled(new Integer(a_mixConf.getAttribute("General/MixType")).intValue() ==
				   MixConfiguration.MIXTYPE_LAST);
	}

	public void stateChanged(ChangeEvent e)
	{
		if (e instanceof ConfigurationEvent)
		{
			ConfigurationEvent ce = (ConfigurationEvent) e;
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
			else if (ce.getChangedAttribute().endsWith("MixType"))
			{
				col = 3;
				setEnabled(new Integer(ce.getNewValue().toString()).intValue() ==
					MixConfiguration.MIXTYPE_LAST);
			}
			else if (ce.getChangedAttribute().endsWith("MinCascadeLength"))
			{
				col = 4;
			}

			if (col >= 0 && m_configuredMixTable.getRowCount() > 0)
			{
				String v = ce.getNewValue().toString();
				if(col == 3)
				{
					Integer t = new Integer(v);
					v = MixConfiguration.MIXTYPE_NAME[t.intValue()];
				}

				m_configuredMixTable.setValueAt(v,
												m_configuredMixTable.getRowCount() - 1,
												col);
			}

			enableComponents();
		}
	}
}
