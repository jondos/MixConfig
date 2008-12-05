package mixconfig.infoservice;

import javax.swing.table.AbstractTableModel;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import anon.util.XMLParseException;

/**
 * @author renner
 */
public class InfoServiceTableModel extends AbstractTableModel 
{
	// The column-names
	private static final String[] columnNames = {"Host", "Port"};
	public static final int HOST = 0;
	public static final int PORT = 1;

	// The list of InfoServices
	private InfoServiceData[] m_rows = new InfoServiceData[0];

	public int getColumnCount() 
	{
		return columnNames.length;
	}

	public String getColumnName(int col)
	{
		return columnNames[col];
	}
	
	public int getRowCount() 
	{
		return m_rows.length;
	}
	
	public boolean isCellEditable(int row, int col) 
	{
		return false;
	}
	
	public void addData(InfoServiceData data)
	{
		InfoServiceData[] nrows = new InfoServiceData[m_rows.length + 1];
		for (int i = 0; i < m_rows.length; i++)
		{
			nrows[i] = m_rows[i];
		}
		nrows[m_rows.length] = data;
		m_rows = nrows;
		fireTableRowsInserted(m_rows.length-1, m_rows.length-1);
	}
	
	public InfoServiceData getData(int index) 
	{
		if (index < 0 || index >= m_rows.length)
		{
			return null;
		}
		return m_rows[index];
	}
	
	public void changeData(InfoServiceData data, InfoServiceData olddata)
	{
		for (int i = 0; i < m_rows.length; i++)
		{
			if (m_rows[i] == olddata)
			{
				m_rows[i] = data;
				fireTableRowsUpdated(i, i);
				return;
			}
		}
		addData(data);
	}
	
	public void deleteData(int index)
	{
		if (index >= 0 && index < m_rows.length)
		{
			InfoServiceData[] nrows = new InfoServiceData[m_rows.length - 1];
			for (int i = 0; i < index; i++)
			{
				nrows[i] = m_rows[i];
			}
			for (int i = index + 1; i < m_rows.length; i++)
			{
				nrows[i - 1] = m_rows[i];
			}
			m_rows = nrows;
			fireTableRowsDeleted(index, index);
		}		
	}
	
	public Object getValueAt(int row, int column) 
	{
		InfoServiceData data = getData(row);
		if (data == null) return null;
		switch (column)
		{
			case HOST:
				return data.getListenerInterface(0).getHost();
			case PORT:
				return new Integer(data.getListenerInterface(0).getPort());
		}
		return null;
	}
	
	/**
	 * Clear this TableModel
	 */
	public void clear()
	{
		int old = m_rows.length;
		m_rows = new InfoServiceData[0];
		fireTableRowsDeleted(1, old);
	}
	
	/**
	 * Create the 'InfoServices'-Element and initiate addition of the single entries
	 * @param doc
	 * @return services
	 */
	public Element createAsElement(Document doc)
	{
		Element services = doc.createElement("InfoServices");
		for (int i = 0; i < getRowCount(); i++)
		{
			services.appendChild(getData(i).toXmlElement(doc));
		}
		return services;
	}
	
	/**
	 * Read InfoServices from DOM Element 'InfoServices'
	 * @param infoService Element
	 */
	public void readFromElement(Element infoServices)
	{
		if (infoServices.getTagName().equals("InfoServices"))
		{
			for (int i=getRowCount()-1; i>=0; i--)
			{
				deleteData(i);
			}
			Node child = infoServices.getFirstChild();
			while (child != null)
			{
				if (child.getNodeType() == Node.ELEMENT_NODE)
				{
					InfoServiceData data;
					try {
						data = InfoServiceData.createFromElement("InfoService", (Element) child);
						if (data != null) addData(data);
					} catch (XMLParseException e) {
						LogHolder.log(LogLevel.WARNING, LogType.NET, e.getMessage());
						//e.printStackTrace();
					}
				}
				child = child.getNextSibling();
			}
		}
	}
}