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
package mixconfig.networkpanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.w3c.dom.Node;


import mixconfig.IntegerDocument;
import mixconfig.MixConfig;
import mixconfig.ConfigFrame;

class IncomingDialog extends ConnectionDialog
{
		protected String getType()
		{
				return "ListenerInterface";
		}

		private void createDialog(final ConnectionData data, final IncomingModel where)
		{
				setSize(500,350);

				GridBagLayout layout=new GridBagLayout();
				getContentPane().setLayout(layout);

				// Constraints for the labels
				GridBagConstraints lc=new GridBagConstraints();
				lc.anchor =GridBagConstraints.WEST;
				lc.insets = new Insets(5,5,5,5);
				lc.gridx = 0;
				lc.gridy = 0;
				lc.weightx = 1;

				// Constraints for all the other things...
				GridBagConstraints rc=new GridBagConstraints();
				rc.anchor = GridBagConstraints.WEST;
				rc.insets = new Insets(5,5,5,5);
				rc.gridx = 1;
				rc.gridy = 0;
				rc.weightx = 0;

				addTransport(data, layout, lc, rc);
				addName(data, layout, lc, rc);
				addIP(data, layout, lc, rc);
				addPort(data, layout, lc, rc);
				addKeys(data, where, layout, lc, rc);

				pack();
				firstone.requestFocus();
		}

		IncomingDialog(Frame parent, String title, final IncomingModel where)
		{
			 super(parent,title);
			 createDialog(null,where);
			 this.setLocationRelativeTo(parent);
		}

		IncomingDialog(Frame parent, String title, final IncomingModel where, ConnectionData data)
		{
			 super(parent,title);
			 createDialog(data,where);
			 this.setLocationRelativeTo(parent);
		}
}

class OutgoingDialog extends ConnectionDialog
{
		private ButtonGroup proxytype;

		protected String getType()
		{
				return (proxytype==null)?"NextMix":"Proxy";
		}

		protected ConnectionData getData()
		{
				ConnectionData data = super.getData();
				if(proxytype!=null)
						data.setFlags(proxytype.getSelection().getActionCommand().equals("HTTP")
													?ConnectionData.HTTP_PROXY:ConnectionData.SOCKS_PROXY);
				return data;
		}

		protected void addType(final ConnectionData data, GridBagLayout layout, GridBagConstraints lc, GridBagConstraints rc)
		{
				JLabel label = new JLabel("Proxy Type");
				layout.setConstraints(label, lc);
				getContentPane().add(label);
				lc.gridy++;

				int ptype;
				if(data==null)
						ptype = ConnectionData.HTTP_PROXY;
				else
						ptype = data.getFlags() & ConnectionData.PROXY_MASK;

				rc.anchor = GridBagConstraints.CENTER;
				rc.gridwidth = 3;
				proxytype = new ButtonGroup();
				JRadioButton t = new JRadioButton("HTTP",ptype!=ConnectionData.SOCKS_PROXY);
				t.setActionCommand("HTTP");
				layout.setConstraints(t,rc);
				getContentPane().add(t);
				proxytype.add(t);
				if(firstone==null)
						firstone = t;

				rc.gridx+=4;
				t = new JRadioButton("Socks",ptype==ConnectionData.SOCKS_PROXY);
				t.setActionCommand("Socks");
				layout.setConstraints(t,rc);
				getContentPane().add(t);
				proxytype.add(t);
				rc.gridy++;
				rc.gridx-=4;
		}

		private void createDialog(final ConnectionData data, final OutgoingModel where)
		{
				setSize(500,350);

				GridBagLayout layout=new GridBagLayout();
				getContentPane().setLayout(layout);

				// Constraints for the labels
				GridBagConstraints lc=new GridBagConstraints();
				lc.anchor =GridBagConstraints.WEST;
				lc.insets = new Insets(5,5,5,5);
				lc.gridx = 0;
				lc.gridy = 0;
				lc.weightx = 1;

				// Constraints for all the other things...
				GridBagConstraints rc=new GridBagConstraints();
				rc.anchor = GridBagConstraints.WEST;
				rc.insets = new Insets(5,5,5,5);
				rc.gridx = 1;
				rc.gridy = 0;
				rc.weightx = 0;

				if(ConfigFrame.m_GeneralPanel.getMixType().equals("LastMix"))
						addType(data, layout, lc, rc);
				else
						proxytype = null;
				addTransport(data, layout, lc, rc);
				addName(data, layout, lc, rc);
				addIP(data, layout, lc, rc);
				addPort(data, layout, lc, rc);
				addKeys(data, where, layout, lc, rc);

				pack();
				firstone.requestFocus();
		}

		OutgoingDialog(Frame parent, String title, final OutgoingModel where)
		{
			 super(parent,title);
			 createDialog(null,where);
			 this.setLocationRelativeTo(parent);
		}

		OutgoingDialog(Frame parent, String title, final OutgoingModel where, ConnectionData data)
		{
			 super(parent,title);
			 createDialog(data,where);
			 this.setLocationRelativeTo(parent);
		}
}




public final class NetworkPanel extends JPanel
{
	 JPanel panel1,panel2,panel3;
	 JTable table1,table2;
	 JTextField Host_Text,Port_Text;
    public IPTextField IP_Text;
	 IncomingModel imodel;
	 OutgoingModel omodel;

	 public IncomingModel getIncomingModel()
	 {
			 return imodel;
	 }

	 public OutgoingModel getOutgoingModel()
	 {
			 return omodel;
	 }

	 public String getHost()
	 {
		 return Host_Text.getText();
	 }

	 public String getIP()
	 {
			 return IP_Text.getText();
	 }

	 public String getPort()
	 {
		 return Port_Text.getText();
	 }

	 public void setInfoHost(String info)
	 {
			 Host_Text.setText(info);
	 }

	 public void setInfoIP(String info)
	 {
			 IP_Text.setText(info);
	 }

	 public void setInfoPort(String info)
	 {
		 Port_Text.setText(info);
	 }

	public NetworkPanel()
	{
		GridBagLayout layout=new GridBagLayout();
		setLayout(layout);

		GridBagLayout In_Layout = new GridBagLayout();
		GridBagLayout Out_Layout = new GridBagLayout();
		GridBagLayout Info_Layout = new GridBagLayout();

		GridBagConstraints c=new GridBagConstraints();
		c.anchor=GridBagConstraints.NORTHWEST;
		c.insets=new Insets(10,10,10,10);
		c.fill=GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		panel1 = new JPanel(In_Layout);
		panel1.setBorder(new TitledBorder("Incoming"));
		layout.setConstraints(panel1,c);
		add(panel1);

		int[] columnSizes1 = {15, 60, 195, 110, 40};
		// table1 = new JTable(data1, columnNames1);

		final TableCellRenderer IPRenderer = new DefaultTableCellRenderer()
		{
				protected void setValue(Object v)
				{
						if(v==null)
								super.setValue("");
						else
						{
								int[] ips = (int[])v;
								super.setValue(ips[0]+"."+ips[1]+"."+ips[2]+"."+ips[3]);
								setHorizontalAlignment(CENTER);
						}
				}
		};
		final TableCellRenderer PortRenderer = new DefaultTableCellRenderer()
		{
				protected void setValue(Object v)
				{
						int t = ((Integer)v).intValue();
						if(t<=0)
								super.setValue("");
						else
						{
								setHorizontalAlignment(CENTER);
								super.setValue(v.toString());
						}
				}
		};
		final TableCellRenderer transportRenderer = new DefaultTableCellRenderer()
		{
				protected void setValue(Object v)
				{
						int t = ((Integer)v).intValue();
						super.setValue(
										(((t&ConnectionData.SSL)==0)?"Raw/":"SSL/")+
										(((t&ConnectionData.UNIX)==0)?"TCP":"Unix"));
						setHorizontalAlignment(CENTER);
				}
		};
		final TableCellRenderer centeringRenderer = new DefaultTableCellRenderer()
		{
				protected void setValue(Object v)
				{
						super.setValue(v);
						setHorizontalAlignment(CENTER);
				}
		};
								/*
		final TableCellRenderer emptyRenderer = new DefaultTableCellRenderer()
		{
				protected void setValue(Object v)
				{
						super.setValue("");
				}
		};
								*/

		imodel = new IncomingModel();

		table1 = new JTable(imodel)
		{
				public TableCellRenderer getCellRenderer(int row, int column)
				{
						switch(column)
						{
								case IncomingModel.TRANSPORT:
										return transportRenderer;
								case IncomingModel.IP_ADDR:
										return IPRenderer;
								case IncomingModel.PORT:
										return PortRenderer;
								default:
										return super.getCellRenderer(row, column);
						}
				}
		};

		JScrollPane scrollPane1 = new JScrollPane(table1,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// Man kann nur eine Zeile selektieren
		table1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		table1.setPreferredScrollableViewportSize(new Dimension(450,90));

		// table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		for(int Index=0; Index<columnSizes1.length;Index++)
		{
				TableColumn column = table1.getColumnModel().getColumn(Index);
				column.setPreferredWidth(columnSizes1[Index]);
				// Die Spalten kann der Nutzer ruhig anpassen, wenn eine
				// Spalte zu klein ist (z.B. aufgrund anderer Schriftarten)
				// column.setMinWidth(columnSizes1[Index]);
				// column.setMaxWidth(columnSizes1[Index]);
		}

		GridBagConstraints d=new GridBagConstraints();
		d.anchor=GridBagConstraints.CENTER;
		d.insets=new Insets(10,10,10,10);
		d.gridx = 0;
		d.gridy = 0;
		d.weightx = 1;
		d.weighty = 1;
		d.gridheight = 3;
		d.fill = GridBagConstraints.BOTH;
		In_Layout.setConstraints(scrollPane1,d);
		panel1.add(scrollPane1);

		for(int Nr=0;Nr<3;Nr++)
		{
				JButton InButton;
				switch (Nr)
				{
						case 0: InButton = new JButton("Add");
								InButton.setActionCommand("Add");
								InButton.addActionListener(new ActionListener()
										{
												public void actionPerformed(ActionEvent a)
												{
														if(a.getActionCommand().equals("Add"))
														{
																IncomingDialog dialog = new IncomingDialog(MixConfig.getMainWindow(),"Add",imodel);
																dialog.show();
														}
												}
										});
								break;
						case 1: final JButton cb = new JButton("Change");
								InButton = cb;
								table1.getSelectionModel().addListSelectionListener(new ListSelectionListener()
										{
												public void valueChanged(ListSelectionEvent e)
												{
														if(e.getValueIsAdjusting())
																return;
														cb.setEnabled(!((ListSelectionModel) e.getSource()).isSelectionEmpty());
												}
										});
								cb.setEnabled(false);
								cb.setActionCommand("Change");
								cb.addActionListener(new ActionListener()
										{
												public void actionPerformed(ActionEvent a)
												{
														if(a.getActionCommand().equals("Change"))
														{
																IncomingDialog dialog = new IncomingDialog(MixConfig.getMainWindow(),"Change",imodel,
																				((IncomingModel)table1.getModel()).getData(table1.getSelectedRow()));
																dialog.show();
														}
												}
										});
								break;
						case 2: final JButton db = new JButton("Delete");
								InButton = db;
								table1.getSelectionModel().addListSelectionListener(new ListSelectionListener()
										{
												public void valueChanged(ListSelectionEvent e)
												{
														if(e.getValueIsAdjusting())
																return;
														db.setEnabled(!((ListSelectionModel) e.getSource()).isSelectionEmpty());
												}

										});
								db.setEnabled(false);
								db.setActionCommand("Delete");
								db.addActionListener(new ActionListener()
										{
												public void actionPerformed(ActionEvent a)
												{
														if(a.getActionCommand().equals("Delete"))
														{
																((IncomingModel)table1.getModel()).deleteData(table1.getSelectedRow());
														}
												}
										});
								break;
						default:
								throw(new RuntimeException("Unknown Button should be created."));
				}
				GridBagConstraints ibd = new GridBagConstraints();
				ibd.anchor=GridBagConstraints.NORTHWEST;
				ibd.insets = new Insets(10,10,10,10);
				ibd.gridx = 1;
				ibd.gridy = Nr;
				ibd.weightx = 0.1;
				ibd.weighty = 0.2;
				ibd.fill = GridBagConstraints.HORIZONTAL;
				In_Layout.setConstraints(InButton, ibd);
				panel1.add(InButton);
		}

		// Now the outgoing connections
		c.gridx = 0;
		c.gridy = 1;
		panel2 = new JPanel(Out_Layout);
		panel2.setBorder(new TitledBorder("Outgoing"));
		layout.setConstraints(panel2,c);
		add(panel2);

		int[] columnSizes2 = {15, 70, 60, 125, 110, 40};
		omodel = new OutgoingModel();
		table2 = new JTable(omodel)
		{
				public TableCellRenderer getCellRenderer(int row, int column)
				{
						switch(column)
						{
								case OutgoingModel.TYPE:
										return centeringRenderer;
								case OutgoingModel.TRANSPORT:
										return transportRenderer;
								case OutgoingModel.IP_ADDR:
										return IPRenderer;
								case OutgoingModel.PORT:
										return PortRenderer;
								default:
										return super.getCellRenderer(row, column);
						}
				}
		};

		JScrollPane scrollPane2 = new JScrollPane(table2,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// Man kann nur eine Zeile selektieren
		table2.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		table2.setPreferredScrollableViewportSize(new Dimension(450,90));

		for(int Index=0; Index<columnSizes2.length;Index++)
		{
				TableColumn column = table2.getColumnModel().getColumn(Index);
				column.setPreferredWidth(columnSizes2[Index]);
				// Die Spalten kann der Nutzer ruhig anpassen, wenn eine
				// Spalte zu klein ist (z.B. aufgrund anderer Schriftarten)
				// column.setMinWidth(columnSizes1[Index]);
				// column.setMaxWidth(columnSizes1[Index]);
		}

		d.anchor=GridBagConstraints.CENTER;
		d.insets=new Insets(10,10,10,10);
		d.gridx = 0;
		d.gridy = 0;
		d.weightx = 1;
		d.weighty = 1;
		d.gridheight = 3;
		d.fill = GridBagConstraints.BOTH;
		Out_Layout.setConstraints(scrollPane2,d);
		panel2.add(scrollPane2);

		for(int Nr=0;Nr<3;Nr++)
		{
				JButton OutButton;
				switch (Nr)
				{
						case 0: final JButton ob = new JButton("Add");
								OutButton = ob;
								omodel.addTableModelListener(new TableModelListener()
										{
												public void tableChanged(TableModelEvent e)
												{
														if(!ConfigFrame.m_GeneralPanel.getMixType().equals("LastMix"))
																ob.setEnabled(omodel.getRowCount()==0);
														else
																ob.setEnabled(true);
												}
										});
								OutButton.setActionCommand("Add");
								OutButton.addActionListener(new ActionListener()
										{
												public void actionPerformed(ActionEvent a)
												{
														if(a.getActionCommand().equals("Add"))
														{
																OutgoingDialog dialog = new OutgoingDialog(MixConfig.getMainWindow(),
																				(ConfigFrame.m_GeneralPanel.getMixType().equals("LastMix"))?"Add Proxy":"Add Next Mix",
																				omodel);
																dialog.show();
														}
												}
										});
								break;
						case 1: final JButton cb = new JButton("Change");
								OutButton = cb;
								table2.getSelectionModel().addListSelectionListener(new ListSelectionListener()
										{
												public void valueChanged(ListSelectionEvent e)
												{
														cb.setEnabled(!((ListSelectionModel) e.getSource()).isSelectionEmpty());
												}
										});
								cb.setEnabled(false);
								cb.setActionCommand("Change");
								cb.addActionListener(new ActionListener()
										{
												public void actionPerformed(ActionEvent a)
												{
														if(a.getActionCommand().equals("Change"))
														{
																OutgoingDialog dialog = new OutgoingDialog(MixConfig.getMainWindow(),
																				(ConfigFrame.m_GeneralPanel.getMixType().equals("LastMix"))?"Change Proxy":"Change Next Mix",
																				omodel,
																				((OutgoingModel)table2.getModel()).getData(table2.getSelectedRow()));
																dialog.show();
														}
												}
										});
								break;
						case 2: final JButton db = new JButton("Delete");
								OutButton = db;
								table2.getSelectionModel().addListSelectionListener(new ListSelectionListener()
										{
												public void valueChanged(ListSelectionEvent e)
												{
														db.setEnabled(!((ListSelectionModel) e.getSource()).isSelectionEmpty());
												}

										});
								db.setEnabled(false);
								db.setActionCommand("Delete");
								db.addActionListener(new ActionListener()
										{
												public void actionPerformed(ActionEvent a)
												{
														if(a.getActionCommand().equals("Delete"))
														{
																((OutgoingModel)table2.getModel()).deleteData(table2.getSelectedRow());
														}
												}
										});
								break;
						default:
								throw(new RuntimeException("Unknown Button should be created."));
				}
				GridBagConstraints ibd = new GridBagConstraints();
				ibd.anchor=GridBagConstraints.NORTHWEST;
				ibd.insets = new Insets(10,10,10,10);
				ibd.gridx = 1;
				ibd.gridy = Nr;
				ibd.weightx = 0.1;
				ibd.weighty = 0.2;
				ibd.fill = GridBagConstraints.HORIZONTAL;
				Out_Layout.setConstraints(OutButton, ibd);
				panel2.add(OutButton);
		}

		c.gridx = 0;
		c.gridy = 2;
		c.weighty=0;
		panel3 = new JPanel(Info_Layout);
		panel3.setBorder(new TitledBorder("Info Service"));
		layout.setConstraints(panel3,c);
		add(panel3);

		GridBagConstraints f=new GridBagConstraints();
		f.anchor=GridBagConstraints.NORTHWEST;
		f.insets=new Insets(10,10,10,10);
		f.fill = GridBagConstraints.HORIZONTAL;

		JLabel host = new JLabel("Host");
		f.gridx = 0;
		f.gridy = 0;
		Info_Layout.setConstraints(host,f);
		panel3.add(host);
		Host_Text = new JTextField(38);
		Host_Text.setText("");
		f.gridx = 1;
		f.weightx = 1;
		Info_Layout.setConstraints(Host_Text,f);
		panel3.add(Host_Text);

		JLabel IP = new JLabel("IP");
		f.gridy = 1;
		f.gridx = 0;
		f.weightx = 0;
		Info_Layout.setConstraints(IP,f);
		panel3.add(IP);
		IP_Text = new IPTextField();
		IP_Text.setText("");
		f.gridx = 1;
		f.weightx = 0;
		f.fill = GridBagConstraints.NONE;
		Info_Layout.setConstraints(IP_Text,f);
		panel3.add(IP_Text);

		JLabel port = new JLabel("Port");
		f.gridy = 2;
		f.gridx = 0;
		f.weightx = 0;
		Info_Layout.setConstraints(port,f);
		panel3.add(port);
		Port_Text = new JTextField(5);
		Port_Text.setText("");
		Port_Text.setDocument(new IntegerDocument(65535));
		Port_Text.setMinimumSize(Port_Text.getPreferredSize());
		f.gridx = 1;
		Info_Layout.setConstraints(Port_Text,f);
		panel3.add(Port_Text);
		}

		public void clear()
		{
				imodel.clear();
				omodel.clear();
				Host_Text.setText("");
				setInfoIP("");
				Port_Text.setText("");
		}
}
