package mixconfig.networkpanel;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

class IncomingDialog extends ConnectionDialog
{
		protected String getType()
		{
				return "ListenerInterface";
		}

		private void createDialog(final ConnectionData data, final IncomingConnectionTableModel where)
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
				addOptions(data,layout,lc,rc);
				addKeys(data, where, layout, lc, rc);


				pack();
				firstone.requestFocus();
		}

		IncomingDialog(Frame parent, String title, final IncomingConnectionTableModel where)
		{
			 super(parent,title);
			 createDialog(null,where);
			 this.setLocationRelativeTo(parent);
		}

		IncomingDialog(Frame parent, String title, final IncomingConnectionTableModel where, ConnectionData data)
		{
			 super(parent,title);
			 createDialog(data,where);
			 this.setLocationRelativeTo(parent);
		}
}
