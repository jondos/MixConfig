package mixconfig;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
// import java.security.Security;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class TheApplet extends JApplet
{
	private static ConfigFrame myFrame;
	private static Frame m_MainWindow;
	public final static int SAVE_DIALOG=1;
	public final static int OPEN_DIALOG=2;
	public final static int FILTER_CER=1;
	public final static int FILTER_XML=2;
	public final static int FILTER_PFX=4;
	public final static int FILTER_B64_CER=8;
	public final static String VERSION="00.01.018";

	public static void main(String[] args)
	{
			JFrame MainWindow = new JFrame("Mix Configuration Tool");
			m_MainWindow = MainWindow;
			ImageIcon icon=loadImage("icon.gif");
			if(icon!=null)
				m_MainWindow.setIconImage(icon.getImage());
			myFrame = new ConfigFrame(MainWindow);

			MainWindow.addWindowListener(new WindowAdapter()
			{
				 public void windowClosing(WindowEvent e)
				 {
						 System.exit(0);
				 }
			});

			MainWindow.setJMenuBar(myFrame.getMenuBar());
			MainWindow.setContentPane(myFrame);
			MainWindow.pack();
			Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
			Dimension size=MainWindow.getSize();
			MainWindow.setLocation((d.width-size.width)/2,(d.height-size.height)/2);
			MainWindow.show();
	}

	 public void init() // For the applet.
	{
		//Security.addProvider(new BouncyCastleProvider());

		// Let's search for the parent frame:
		java.awt.Component comp = this;
		while((comp=comp.getParent())!=null &&
		!(comp instanceof java.awt.Frame));
		m_MainWindow=(Frame) comp;
		myFrame = new ConfigFrame(null);
		//myFrame.pack();//setBounds(10,10,600,650);
		setJMenuBar(myFrame.getMenuBar());
		setContentPane(myFrame);
	}

	public static Frame getMainWindow()
		{
			return m_MainWindow;
		}

	 public static ImageIcon loadImage(String name)
		{
	return new ImageIcon(TheApplet.class.getResource(name));
		}

		 public static JFileChooser showFileDialog(int type, int filter_type)
			{
	SimpleFileFilter active = null;
	JFileChooser fd2= new JFileChooser();
	fd2.setFileSelectionMode(JFileChooser.FILES_ONLY);
	if((filter_type&FILTER_CER)!=0)
		fd2.addChoosableFileFilter(active=new SimpleFileFilter(FILTER_CER));
	if((filter_type&FILTER_B64_CER)!=0)
		fd2.addChoosableFileFilter(active=new SimpleFileFilter(FILTER_B64_CER));
	if((filter_type&FILTER_XML)!=0)
		fd2.addChoosableFileFilter(active=new SimpleFileFilter(FILTER_XML));
	if((filter_type&FILTER_PFX)!=0)
		fd2.addChoosableFileFilter(new SimpleFileFilter(FILTER_PFX));
	if(active!=null)
			fd2.setFileFilter(active);
	fd2.setFileHidingEnabled(false);
	if(type==SAVE_DIALOG)
		fd2.showSaveDialog(myFrame);
	else
		fd2.showOpenDialog(myFrame);
	return fd2;
			}

}
