package gui.dialog;

import gui.dialog.JAPDialog.ILinkedInformation;

import java.io.IOException;
import java.net.URISyntaxException;

import mixconfig.panels.OwnCertificatesPanel;
import mixconfig.tools.EmailComposer;

public class LinkedEmailComposer implements ILinkedInformation {

	String m_message = null;
	OwnCertificatesPanel m_panel = null;
	
	public LinkedEmailComposer(String message, OwnCertificatesPanel panel) 
	{
		m_message = message;
		m_panel = panel;
	}
	
	public void clicked(boolean state) {
		try {
			EmailComposer.composeEmail();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getMessage() 
	{
		return m_message;
	}

	public int getType() 
	{
		return 0;
	}

	public boolean isApplicationModalityForced() 
	{
		return false;
	}

	public boolean isCloseWindowActive() 
	{
		return false;
	}

	public boolean isOnTop() 
	{
		return false;
	}

	public String getTooltipText() {
		// TODO Auto-generated method stub
		return "";
	}
}
