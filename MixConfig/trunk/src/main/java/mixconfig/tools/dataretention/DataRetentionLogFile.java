package mixconfig.tools.dataretention;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DataRetentionLogFile {
	
	private File m_File;
	private DataRetentionLogFileHeader m_Header;
	private byte[] m_SymKey;
	private byte[] m_Footer;
	private FileInputStream m_LogFileInputStream;
	private int m_currentLogVerifyLine;
	private final static int FOOTER_SIZE=20;
	
	public DataRetentionLogFile(File logFile) throws IOException
	{
		m_File=logFile;
		parseHeader();
		readFooter();
	}
	
	private void readFooter() throws IOException{
		long fileSize=m_File.length();
		FileInputStream fin=new FileInputStream(m_File);
		fin.skip(fileSize-FOOTER_SIZE);
		m_Footer=new byte[FOOTER_SIZE];
		fin.read(m_Footer);
		fin.close();
		// TODO Auto-generated method stub
		
	}

	private void parseHeader() throws IOException {
		// TODO Auto-generated method stub
		m_Header=new DataRetentionLogFileHeader();
		m_Header.parseFromFile(new FileInputStream(m_File));
	}

	public DataRetentionLogFileHeader getHeader()
		{
			return m_Header;
		}
	
	public int getEncryptedKeyCount()
	{
		return m_Header.getEncryptedKeyCount();
	}
	
	public byte[] getEncryptedKey(int index)
	{
		return m_Header.getEncryptedKey(index);
	}
	
	public void setDecryptionKey(byte[] symkey)
	{
		m_SymKey=symkey;
	}

	public void verifyHeader() throws Exception
	{
		m_Header.verifyHeader(m_SymKey);
	}
	
	public void verifyFooter() throws Exception
	{
	   	byte[] iv={(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF
	   			,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF
	   			,(byte)0xFF,(byte)0xFF};
	   	byte[] buff=new byte[1024];
		int decSize=DataRetentionLogFileHeader.decryptAndVerify(m_Footer, iv, m_SymKey,buff);
		if(decSize!=4)
			throw new IOException("Wrong byte size of decrypted log entries number");
		int expNr=getExpectedNrOfLogEntries();
		int footerNr=buff[0]<<24|((buff[1]<<16)&0x00FFFFFF)|((buff[2]<<8)&0x00FFFF)|(buff[3]&0x00FF);
		if(expNr!=footerNr)
			throw new IOException("Nr of Log Entries reported by footer does not much expected number of log entries (based on file size)");
	}

	public int getNrOfLogLines()
	{
		long fileSize=m_File.length();
		long logSize=fileSize-FOOTER_SIZE-m_Header.getLength();
		return (int)((logSize+m_Header.getSizeOfLogLine()-1)/m_Header.getSizeOfLogLine());
		
	}
	
	public void verifyFirstLogLine() throws Exception
	{
		m_LogFileInputStream=new FileInputStream(m_File);
		m_LogFileInputStream.skip(m_Header.getLength());
		m_currentLogVerifyLine=0;
		verifyNextLogLine();
	}
	
	public void verifyNextLogLine() throws Exception
	{
		byte[] buff=new byte[m_Header.getSizeOfLogLine()];
		int ret=m_LogFileInputStream.read(buff);
		m_currentLogVerifyLine++;
		if(ret==-1||m_currentLogVerifyLine>getNrOfLogLines())
		{
			m_LogFileInputStream.close();
			throw new EOFException();
		}
		//last log line might be special --> if not completely filled...
		if(m_currentLogVerifyLine==getNrOfLogLines()&&(getExpectedNrOfLogEntries()%m_Header.getNrOfLogEntriesPerLogLine())!=0)
			ret=(getExpectedNrOfLogEntries()%m_Header.getNrOfLogEntriesPerLogLine())*m_Header.getSizeOfLogEntry()+16;
		
		DataRetentionLogFileHeader.decryptAndVerify(buff,0,ret, createIV(m_currentLogVerifyLine-1), m_SymKey,null);
			
	}
	
	public DataRetentionLogFileEntry[] search(long t_out,int d_t) throws Exception
	{
		int lowInd=0;
		int maxInd=getExpectedNrOfLogEntries()-1;
		int upInd=maxInd;
		int midInd=lowInd+(upInd-lowInd)/2;
		long l=getToutOfLogEntry(lowInd);
		long u=getToutOfLogEntry(upInd);
		long m=getToutOfLogEntry(midInd);
		long t_min=t_out-d_t;
		long t_max=t_out+d_t;
		if(u<t_min || t_max<l)
			return null;
		for(;;)
		{
			if(t_out<m)
			{
				upInd=midInd-1;
			}
			else if(t_out>m)
			{
				lowInd=midInd+1;
			}
			else
				break;
			if(upInd<=lowInd)
				break;
			midInd=lowInd+(upInd-lowInd)/2;
			m=getToutOfLogEntry(midInd);
		}
		
		lowInd=midInd;
		for(;;)
		{
			l=getToutOfLogEntry(lowInd);
			if(t_min<=l&&l<=t_max&&lowInd>0)
				{
					lowInd--;
					continue;
				}	
			else
				break;
		}
		
		upInd=midInd;
		for(;;)
		{
			u=getToutOfLogEntry(upInd);
			if(t_min<=u&&u<=t_max&&upInd<maxInd)
				{
					upInd++;
					continue;
				}	
			else
				break;
		}
		
		return getAllLogEntries(lowInd+1,upInd-1);		
	}
	
	private DataRetentionLogFileEntry[] getAllLogEntries(int lowInd, int upInd) throws Exception{
		int l=lowInd/m_Header.getNrOfLogEntriesPerLogLine();
		byte[]logLine=this.readAndDecrpytLogLine(l);
		int off=(lowInd%m_Header.getNrOfLogEntriesPerLogLine())*m_Header.getSizeOfLogEntry();
		DataRetentionLogFileEntry entries[]=new DataRetentionLogFileEntry[upInd-lowInd+1];
		int i=0;
		for(;;)
		{
			entries[i++]=new DataRetentionLogFileEntry(logLine,off,m_Header);
			lowInd++;
			if(lowInd>upInd)
				break;
			if((lowInd%m_Header.getNrOfLogEntriesPerLogLine())==0)
			{
				l++;
				logLine=this.readAndDecrpytLogLine(l);
				off=0;
			}
			else
				off+=m_Header.getSizeOfLogEntry();
		}
		return entries;
	}

	byte[] readAndDecrpytLogLine(int lineNr) throws Exception
	{
		FileInputStream fin=new FileInputStream(m_File);
		fin.skip(m_Header.getLength());
		if(lineNr>0)
			fin.skip(lineNr*m_Header.getSizeOfLogLine());

		int size=m_Header.getSizeOfLogLine();
		//last log line might be special --> if not completely filled...
		if(lineNr==getNrOfLogLines()-1&&(getExpectedNrOfLogEntries()%m_Header.getNrOfLogEntriesPerLogLine())!=0)
			size=(getExpectedNrOfLogEntries()%m_Header.getNrOfLogEntriesPerLogLine())*m_Header.getSizeOfLogEntry()+16;
		byte[]buff=new byte[size];
		byte[]plain=new byte[size-16];
		fin.read(buff);
		DataRetentionLogFileHeader.decryptAndVerify(buff,0,size, createIV(lineNr), m_SymKey,plain);		
		fin.close();
		return plain;
	}

	private long getToutOfLogEntry(int ind) throws Exception {
		byte[] logLine=readAndDecrpytLogLine(ind/m_Header.getNrOfLogEntriesPerLogLine());
		int i=(ind%m_Header.getNrOfLogEntriesPerLogLine())*m_Header.getSizeOfLogEntry()+4;
		long t=((logLine[i]<<24)&0x00FF000000L)|((logLine[i+1]<<16)&0x00FF0000L)|((logLine[i+2]<<8)&0x00FF00L)|(logLine[i+3]&0x00FFL);
		return t/*+m_Header.getBaseTime()*/;
	}

	/**
     * Method creates proper raw initialization vector from value of blocksCounter.
     * @param blocksCounter Actual counter of blocks (aka log lines)
    * @return Array with initialization vector.
     */
    private byte[] createIV(int blocksCounter) {
        byte[] iv = new byte[12];
        for (int i = 0; i < 8; i++) iv[i] = 0;
        iv[8] = (byte) (blocksCounter >> 24 & 0x00ff);
        iv[9] = (byte) (blocksCounter >> 16 & 0x00ff);
        iv[10] = (byte) (blocksCounter >> 8 & 0x00ff);
        iv[11] = (byte) (blocksCounter & 0x00ff);
        return iv;
    }

	private int getExpectedNrOfLogEntries() {
		// TODO Auto-generated method stub
		long fileSize=m_File.length();
		long logSize=fileSize-FOOTER_SIZE-m_Header.getLength();
		int entries=(int)(logSize/m_Header.getSizeOfLogLine())*m_Header.getNrOfLogEntriesPerLogLine();
		if(logSize%m_Header.getSizeOfLogLine()>0)
		{
			entries+=((logSize%m_Header.getSizeOfLogLine())-16)/m_Header.getSizeOfLogEntry();
		}
		return entries;
	}
}
