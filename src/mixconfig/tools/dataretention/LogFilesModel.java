package mixconfig.tools.dataretention;
/*
 * %W% %E%
 *
 * Copyright 1997, 1998 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer. 
 *   
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution. 
 *   
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.  
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,   
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS 
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * FileSystemModel is a TreeTableModel representing a hierarchical file 
 * system. Nodes in the FileSystemModel are FileNodes which, when they 
 * are directory nodes, cache their children to avoid repeatedly querying 
 * the real file system. 
 * 
 * @version %I% %G%
 *
 * @author Philip Milne
 * @author Scott Violet
 */

public class LogFilesModel extends AbstractTreeTableModel 
                             implements TreeTableModel {

    // Names of the columns.
    static protected String[]  cNames = {"Anfragedatum", "Sendezeit", "Eingangskanal", "Empfangszeit","Ausgangskanal"};

    // Types of the columns.
    static protected Class[]  cTypes = {TreeTableModel.class, String.class, Long.class, String.class,Long.class};

    // The the returned file length for directories. 
    public static final Integer ZERO = new Integer(0); 

    public LogFilesModel() { 
	super(new Requests()); 
    }

    //
    // Some convenience methods. 
    //


    protected Object[] getChildren(Object node) {
    	if(node instanceof ChannelNode)
    		return null;
	return ((IMyNode)node).getChildren(); 
    }

    //
    // The TreeModel interface
    //

    public int getChildCount(Object node) { 
	Object[] children = getChildren(node); 
	return (children == null) ? 0 : children.length;
    }

    public Object getChild(Object node, int i) { 
	return getChildren(node)[i]; 
    }

    // The superclass's implementation would work, but this is more efficient. 
    //public boolean isLeaf(Object node) { return getFile(node).isFile(); }

    //
    //  The TreeTableNode interface. 
    //

    public int getColumnCount() {
	return cNames.length;
    }

    public String getColumnName(int column) {
	return cNames[column];
    }

    public Class getColumnClass(int column) {
	return cTypes[column];
    }
 
    public Object getValueAt(Object node, int column) {
    	if(node instanceof RequestNode)
    		return null;
       	if(node instanceof Requests)
    		return null;
		SimpleDateFormat sf=new SimpleDateFormat("    dd.MM.yyyy HH:mm:ss");
			
	    switch(column) {
	    case 0:
	      	return "hallo";
	    case 1:
	    	return sf.format(((ChannelNode)node).t_out);
	    case 2:
	return ((ChannelNode)node).id_in;
	    case 3:
	    	return sf.format(((ChannelNode)node).t_in);
	    case 4:
	    	return ((ChannelNode)node).id_out;
	    }
    	return "hallo";
    }
}

interface IMyNode
{
	Object[] getChildren();
}

class Requests implements IMyNode
{
	RequestNode[] theRequests;
	static String dates[]={"12.01.2009 13:45:23","13.01.2009 11:37:15","13.01.2009 13:22:56"};
	static int nrresults[]={5,5,7};
	Requests()
	{
		theRequests=new RequestNode[3];
		for(int i=0;i<theRequests.length;i++)
			theRequests[i]=new RequestNode(dates[i],i+1,nrresults[i]);
		
	}
	
	public Object[] getChildren()
	{
		return theRequests;
	}
	
	public String toString()
			{
				return "Anfragen";
			}
}

class RequestNode implements IMyNode
{
	ChannelNode[] resultChannels;
	int requestNr;
	String requestDate;
	
	RequestNode(String d,int nr,int c)
	{
		resultChannels=new ChannelNode[c];
		SimpleDateFormat sf=new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		Date da=null;
		try {
			da = sf.parse(d);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Random r=new Random();
		for(int i=0;i<resultChannels.length;i++)
			resultChannels[i]=new ChannelNode(i+1,new Date(da.getTime()-10000+(i*2000)+r.nextInt(3)*1000));
		requestDate=d;
		requestNr=nr;
	}
	
	public Object[] getChildren()
	{
		return resultChannels;
	}
	
	public String toString()
	{
		return requestDate;
	}
	
}

class ChannelNode { 
    public long     id_in,id_out; 
    Date t_in,t_out;
    int nr;
    public ChannelNode(int n,Date reqd) {
    	nr=n;
    	Random r=new Random();
    	t_out=reqd;
    	t_in=new Date(t_out.getTime()-r.nextInt(30)*1000);
    	id_in=(r.nextLong()&0x00000000FFFFFFFFL);
    	id_out=(r.nextLong()&0x00000000FFFFFFFFL);
    }

    /**
     * Returns the the string to be used to display this leaf in the JTree.
     */
    public String toString() { 
	return Integer.toString(nr);
    }


}


