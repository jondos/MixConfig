package mixconfig.network;

import javax.swing.table.AbstractTableModel;

abstract public class ConnectionTableModel extends AbstractTableModel
{
	private ConnectionData[] rows = new ConnectionData[0];

	public static final int NR = 0;
	public static final int TYPE = 1;
	public static final int TRANSPORT = 2;
	public static final int HOST = 3;
	public static final int PORT = 4;	
	public static final int VISIBLE = 5;
	
	public int getRowCount()
	{
		return rows.length;
	}

	public boolean isCellEditable(int row, int col)
	{
		return false;
	}

	public void addData(ConnectionData data)
	{
		ConnectionData[] nrows = new ConnectionData[rows.length + 1];
		for (int i = 0; i < rows.length; i++)
		{
			nrows[i] = rows[i];
		}
		nrows[rows.length] = data;
		rows = nrows;
		fireTableRowsInserted(rows.length - 1, rows.length - 1);
	}

	public ConnectionData getData(int index)
	{
		if (index < 0 || index >= rows.length)
		{
			return null;
		}
		return rows[index];
	}

	public void changeData(ConnectionData data, ConnectionData olddata)
	{
		for (int i = 0; i < rows.length; i++)
		{
			if (rows[i] == olddata)
			{
				rows[i] = data;
				fireTableRowsUpdated(i, i);
				return;
			}
		}
		addData(data);
	}

	public void deleteData(int index)
	{
		if (index >= 0 && index < rows.length)
		{
			ConnectionData[] nrows = new ConnectionData[rows.length - 1];
			for (int i = 0; i < index; i++)
			{
				nrows[i] = rows[i];
			}
			for (int i = index + 1; i < rows.length; i++)
			{
				nrows[i - 1] = rows[i];
			}
			rows = nrows;
			fireTableRowsDeleted(index, index);
		}
	}

	void clear()
	{
		int old = rows.length;
		rows = new ConnectionData[0];
		fireTableRowsDeleted(1, old);
	}
}
