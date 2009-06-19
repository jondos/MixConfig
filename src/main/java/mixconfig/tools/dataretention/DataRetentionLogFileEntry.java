package mixconfig.tools.dataretention;

import java.util.Date;

public class DataRetentionLogFileEntry {

	private long m_tIn;
	private long m_tOut;
	private long m_ChannelIn=-1;
	private long m_ChannelOut=-1;
	public DataRetentionLogFileEntry(byte[] buff,int off,
			DataRetentionLogFileHeader header) 
	{
		m_tIn=(buff[off]<<24)&0x00FFFFFFFFL | (buff[off+1]<<16)&0x00FFFFFFL | (buff[off+2]<<8)&0x00FFFFL |(buff[off+3])&0x00FFL   ;
		m_tOut=(buff[off+4]<<24)&0x00FFFFFFFFL | (buff[off+5]<<16)&0x00FFFFFFL | (buff[off+6]<<8)&0x00FFFFL |(buff[off+7])&0x00FFL   ;
		switch(header.getLoggingEntity())
				{
			case DataRetentionLogFileHeader.LOGGING_ENTITY_FIRST_MIX:
				m_ChannelOut=(buff[off+8]<<24)&0x00FFFFFFFFL | (buff[off+9]<<16)&0x00FFFFFFL | (buff[off+10]<<8)&0x00FFFFL |(buff[off+11])&0x00FFL   ;
				break;
			case DataRetentionLogFileHeader.LOGGING_ENTITY_MIDDLE_MIX:
				break;
			case DataRetentionLogFileHeader.LOGGING_ENTITY_LAST_MIX:
				break;
				}
	}

	public String toString()
	{
		return "t_in="+(new Date(m_tIn*1000)).toGMTString()+", t_out="+(new Date(m_tOut*1000)).toGMTString()+", ChannelOut="+m_ChannelOut;
	}
}
