package mixconfig;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
// import java.security.Security;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class MixConfig extends JApplet
{
	private static ConfigFrame m_ConfigFrame;
	private static Frame m_MainWindow;
        private static File currentDir;
	public final static int SAVE_DIALOG=1;
	public final static int OPEN_DIALOG=2;
	public final static int FILTER_CER=1;
	public final static int FILTER_XML=2;
	public final static int FILTER_PFX=4;
	public final static int FILTER_B64_CER=8;
	public final static String VERSION="00.02.001";

	public static void main(String[] args)
	{
//			byte[]buff=new byte[120];
//			Base64.encodeBytes(buff,true);

			JFrame MainWindow = new JFrame("Mix Configuration Tool");
			m_MainWindow = MainWindow;
			ImageIcon icon=loadImage("icon.gif");
			if(icon!=null)
				m_MainWindow.setIconImage(icon.getImage());
			m_ConfigFrame = new ConfigFrame(MainWindow);

			MainWindow.addWindowListener(new WindowAdapter()
			{
				 public void windowClosing(WindowEvent e)
				 {
						 System.exit(0);
				 }
			});

			MainWindow.setJMenuBar(m_ConfigFrame.getMenuBar());
			MainWindow.setContentPane(m_ConfigFrame);
			MainWindow.pack();
			Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
			Dimension size=MainWindow.getSize();
			MainWindow.setLocation((d.width-size.width)/2,(d.height-size.height)/2);
			MainWindow.show();
	}

	 public void init() // For the applet.
	{

		// Let's search for the parent frame:
		java.awt.Component comp = this;
		while((comp=comp.getParent())!=null &&
		!(comp instanceof java.awt.Frame));
		m_MainWindow=(Frame) comp;
		m_ConfigFrame = new ConfigFrame(null);
		//myFrame.pack();//setBounds(10,10,600,650);
		setJMenuBar(m_ConfigFrame.getMenuBar());
		setContentPane(m_ConfigFrame);
	}

	public static Frame getMainWindow()
		{
			return m_MainWindow;
		}

	 public static ImageIcon loadImage(String name)
		{
	return new ImageIcon(MixConfig.class.getResource(name));
		}

		 public static JFileChooser showFileDialog(int type, int filter_type)
			{
	SimpleFileFilter active = null;
	JFileChooser fd2= new JFileChooser(currentDir);
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
		fd2.showSaveDialog(m_ConfigFrame);
	else
		fd2.showOpenDialog(m_ConfigFrame);
        currentDir = fd2.getCurrentDirectory();
	return fd2;
			}

}
