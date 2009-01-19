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
