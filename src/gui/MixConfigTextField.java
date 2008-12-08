package gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;

public class MixConfigTextField extends JTextField {

	public MixConfigTextField()
	{
		super();
	}
	
	public MixConfigTextField(int columns)
	{
		super(columns);		
	}
	
	public MixConfigTextField(String text,int columns)
	{
		super(text, columns);
	}
	
	protected final Document createDefaultModel()
	{
		return new MixConfigDocument();
	}
	
	private final class MixConfigDocument extends PlainDocument
	{
		// Blacklist of characters that may not be entered
		String sBlacklistedChars = "\u00FC\u00FC\u00C4\u00E4\u00D6\u00F6<>&"; //Ue ue Ae ae Oe oe
        String sRegexAllowedChar = "[^"+sBlacklistedChars+"]";
        
        // Compile blacklisted chars for replacing them in strings with length > 1
		Pattern p = Pattern.compile("["+sBlacklistedChars+"]");
        
		public void insertString(int offSet, String string, AttributeSet attributeSet) 
			throws BadLocationException
		{
			//LogHolder.log(LogLevel.DEBUG, LogType.GUI, "Current string: " + string);
			
			// Insert single characters if allowed
			if (string.length() == 1 && string.matches(sRegexAllowedChar))
			{
				//LogHolder.log(LogLevel.DEBUG, LogType.GUI, "Inserting " + string);
				super.insertString(offSet, string, attributeSet);				
			} 
			else if (string.length() > 1) 
			{
				// Replace blacklisted characters by whitespaces
				Matcher m = p.matcher(string);
				String newString = m.replaceAll("");
				//LogHolder.log(LogLevel.DEBUG, LogType.GUI, "Inserting " + string);
				super.insertString(offSet, newString, attributeSet);
			}
			else 
			{
				LogHolder.log(LogLevel.DEBUG,LogType.GUI, "Rejecting " + string);
				return;
			}
		}
	}
}
