package mixconfig;

import java.awt.Component;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *  A document that accepts only non-negatives.
 */

public class IntegerDocument extends PlainDocument {
    int max;
    Component which;

    IntegerDocument(int maxval, Component comp)
    {
            super();
            max = maxval;
            which = comp;
    }

    IntegerDocument(int maxval)
    {
            super();
            max = maxval;
            which = null;
    }

    IntegerDocument()
    {
            super();
            max = 0;
            which = null;
    }

    IntegerDocument(Component comp)
    {
            super();
            max = 0;
            which = comp;
    }

    public void insertString(int offset, String str, AttributeSet attr)
                    throws BadLocationException
    {
            if(str==null)
                    return;

            String p1 = getText(0,offset);
            String p2 = getText(offset, getLength()-offset);
            String res = "";

            for(int i=0;i<str.length();i++)
                    if(!Character.isDigit(str.charAt(i)))
                            java.awt.Toolkit.getDefaultToolkit().beep();
                    else
                    {
                            String sstr = str.substring(i,i+1);
                            int val = Integer.parseInt(p1+res+sstr+p2,10);
                            if(max>0 && val>max)
                                    java.awt.Toolkit.getDefaultToolkit().beep();
                            else
                                    res+=sstr;
                    }
            super.insertString(offset,res,attr);
            if(which!=null && max>0 && getLength()>0 && 10*Integer.parseInt(getText(0,getLength()),10)>max)
                    which.transferFocus();
    }

}
