package mixconfig.networkpanel;

import javax.swing.table.AbstractTableModel;
abstract public class ConnectionTableModel extends AbstractTableModel
{
    private ConnectionData[] rows = new ConnectionData[0];

    public int getRowCount()
    {
        return rows.length;
    }

    public boolean isCellEditable(int row, int col)
    {
        return false;
    }

    void addData(ConnectionData data)
    {
        ConnectionData[] nrows = new ConnectionData[rows.length+1];
        for(int i=0;i<rows.length;i++)
            nrows[i] = rows[i];
        nrows[rows.length] = data;
        rows = nrows;
        fireTableRowsInserted(rows.length-1,rows.length-1);
    }

    public ConnectionData getData(int index)
    {
        if(index<0 || index>=rows.length)
            return null;
        return rows[index];
    }

    void changeData(ConnectionData data, ConnectionData olddata)
    {
        for(int i=0;i<rows.length;i++)
            if(rows[i]==olddata)
            {
                rows[i]=data;
                fireTableRowsUpdated(i,i);
                return;
            }
        addData(data);
    }

    void deleteData(int index)
    {
        if(index>=0 && index<rows.length)
        {
            ConnectionData[] nrows = new ConnectionData[rows.length-1];
            for(int i=0;i<index;i++)
                nrows[i] = rows[i];
            for(int i=index+1;i<rows.length;i++)
                nrows[i-1] = rows[i];
            rows = nrows;
            fireTableRowsDeleted(index,index);
        }
    }

    void clear()
    {
        int old = rows.length;
        rows = new ConnectionData[0];
        fireTableRowsDeleted(1,old);
    }
}
