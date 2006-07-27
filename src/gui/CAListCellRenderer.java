package gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import anon.crypto.CertificateInfoStructure;

final public class CAListCellRenderer extends JLabel implements ListCellRenderer
{
	// This is the only method defined by ListCellRenderer.
	// We just reconfigure the JLabel each time we're called.

	public Component getListCellRendererComponent(
		JList list,
		Object value, // value to display
		int a_index, // cell index
		boolean isSelected, // is the cell selected
		boolean cellHasFocus) // the list and the cell have the focus
	{
		CertificateInfoStructure j = (CertificateInfoStructure) value;
		String subjectCN = j.getCertificate().getSubject().getCommonName();
		if (subjectCN == null)
		{
			subjectCN = j.getCertificate().getSubject().toString();
		}
		setText(subjectCN);
		setEnabled(list.isEnabled());

//         setIcon((s.length() > 10) ? longIcon : shortIcon);
		if (isSelected)
		{
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		}
		else
		{
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		if (j.isEnabled())
		{
			setIcon(CertDetailsDialog.CERTENABLEDICON);
		}
		else
		{
			setForeground(Color.red);
			setIcon(CertDetailsDialog.CERTDISABLEDICON);
		}
		setFont(list.getFont());
		setOpaque(true);
		return this;
	}
}