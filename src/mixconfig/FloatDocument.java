package mixconfig;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *  A document that accepts only floating points.
 */
class FloatDocument extends PlainDocument
{
    private float max, min;

    // minval should be < 0, maxval > 0
    FloatDocument(float minval, float maxval)
    {
            super();
            max = maxval;
            min = minval;
    }

    // Only positive floating points
    FloatDocument(float maxval)
    {
            super();
            max = maxval;
            min = 0;
    }

    FloatDocument()
    {
            super();
            max = 0;
            min = 0;
    }

    public void insertString(int offset, String str, AttributeSet attr)
                    throws BadLocationException
    {
            if(str==null)
                    return;

            String p1 = getText(0,offset);
            String p2 = getText(offset, getLength()-offset);
            String res = "";

            boolean hasPoint = p1.indexOf('.')>=0 || p2.indexOf(',')>=0;

            for(int i=0;i<str.length();i++)
                    if(str.charAt(i)=='.' || str.charAt(i)==',')
                    {
                        if(hasPoint)
                            java.awt.Toolkit.getDefaultToolkit().beep();
                        else
                        {
                            res+=".";
                            hasPoint = true;
                        }
                    }
                    else if(min<0 && str.charAt(i)=='-')
                    {
                        if(p1.length()==0 && res.length()==0)
                            res="-";
                        else                        
                            java.awt.Toolkit.getDefaultToolkit().beep();
                    }
                    else if(!Character.isDigit(str.charAt(i)))
                            java.awt.Toolkit.getDefaultToolkit().beep();
                    else
                    {
                            String sstr = str.substring(i,i+1);
                            float val = (new Float(p1+res+sstr+p2)).floatValue();
                            if((max>0 && val>max) || (min<0 && val<min))
                                    java.awt.Toolkit.getDefaultToolkit().beep();
                            else
                                    res+=sstr;
                    }
            super.insertString(offset,res,attr);
    }
}
