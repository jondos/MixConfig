package mixconfig.networkpanel;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public final class IncomingConnectionTableModel extends ConnectionTableModel
{
		private static final String[] columnNames =
						{"No.","Virtual","Hidden","Transport", "Host / FileName",
						 "IP Address", "Port" };

		public static final int SERIAL_NR = 0;
		public static final int VIRTUAL = 1;
		public static final int HIDDEN = 2;
		public static final int TRANSPORT = 3;
		public static final int NAME = 4;
		public static final int IP_ADDR = 5;
		public static final int PORT = 6;

		public Object getValueAt(int row, int column)
		{
				ConnectionData data = getData(row);
				if(data==null)
						return null;
				switch(column)
				{
						case SERIAL_NR: return new Integer(row+1);
						case VIRTUAL: return new Boolean(data.isVirtual());
						case HIDDEN: return new Boolean(data.isHidden());
						case TRANSPORT: return new Integer(data.getTransport());
						case NAME: return data.getName();
						case IP_ADDR: return data.getIPAddr();
						case PORT: return new Integer(data.getPort());
				}
				return null;
		}

		public Class getColumnClass(int column)
		{
				switch(column)
				{
						case SERIAL_NR: return Integer.class;
						case VIRTUAL: return Boolean.class;
						case HIDDEN: return Boolean.class;
						case NAME: return String.class;
						case PORT: return Integer.class;
						// Transport und IP-Addresse muessen wir in der Tabelle
						// gesondert behandeln.
				}
				return Object.class;
		}

		public int getColumnCount()
		{
				return columnNames.length;
		}

		public String getColumnName(int col)
		{
				return columnNames[col];
		}

		public Element createAsElement(Document doc)
		{
				Element ifaces = doc.createElement("ListenerInterfaces");
				for(int i=0;i<getRowCount();i++)
						ifaces.appendChild(getData(i).createAsElement(doc));
				return ifaces;
		}

		public void readFromElement(org.w3c.dom.Element iface)
		{
				if(iface.getTagName().equals("ListenerInterfaces"))
				{
						for(int i=getRowCount()-1;i>=0;i--)
								deleteData(i);
						org.w3c.dom.Node child = iface.getFirstChild();
						while(child!=null)
						{
								if(child.getNodeType() == Node.ELEMENT_NODE)
								{
										ConnectionData data = ConnectionData.createFromElement(
														"ListenerInterface", (org.w3c.dom.Element)child);
										if(data!=null)
												addData(data);
								}
								child = child.getNextSibling();
						}
				}
		}
}
