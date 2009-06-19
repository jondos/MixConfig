package mixconfig.tools.dataretention;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
public class SquidLogParser {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
			FileInputStream fin=new FileInputStream("c:/access.log");
			DataInputStream din=new DataInputStream(fin);
			din.readLine();
			for(int i=0;i<100;i++)
			{
			long start=din.readInt();
			long end=din.readInt();
			byte ip_mix[]=new byte[4];
			din.read(ip_mix);
			byte ip_proxy[]=new byte[4];
			din.read(ip_proxy);
			int port_mix=din.readShort();
			port_mix&=0x00FFFF;
			int port_proxy=din.readShort();
			port_proxy&=0x00FFFF;
			InetAddress iin=InetAddress.getByAddress(ip_mix);
			InetAddress iout=InetAddress.getByAddress(ip_proxy);
			System.out.println("t_start="+(new Date(start*1000)).toString()+" t_end="+(new Date(end*1000)).toString()+" IP_mix="+iin.toString()+":"+port_mix+" IP_proxy="+iout.toString()+":"+port_proxy);
			}
			}

}
