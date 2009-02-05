/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mixconfig.tools.dataretention;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

import anon.crypto.MyRSA;
import anon.crypto.MyRSAPrivateKey;
import anon.util.Util;

/**
 * 
 * @author Petr Svenda
 */

public class DataRetentionLogFileHeader {
	class t_encrypted_key {
		public static final short ENCRYPTED_KEY_LENGTH = 256;
		public byte[] encryptedKeyBlock = null;
	}

	public static final byte MAX_nr_of_keys = 10;

	private byte version = 0;
	private byte reserved1 = 0;
	private byte reserved2 = 0;
	private byte reserved3 = 0;
	private byte day = 0;
	private byte month = 0;
	private short year = 0;
	private long m_BaseTime;
	private byte logging_entity = 0;
	private byte logged_fields = 0;
	private short nr_of_log_entries_per_encrypted_log_line = 0;
	private byte nr_of_keys = 0;
	private t_encrypted_key[] keys = null;
	private byte auth_tag[] = null;

	private byte m_sizeOfLogEntry;

	public DataRetentionLogFileHeader() {
		keys = new t_encrypted_key[MAX_nr_of_keys];
		for (int i = 0; i < MAX_nr_of_keys; i++)
			keys[i] = new t_encrypted_key();
		auth_tag = new byte[DataRetentionSmartCard.GCM_AUTH_TAG_LENGTH / 8];
	}

	public void writeToFile(FileOutputStream file) throws Exception {
		writeWithoutAuthTag(file);
		// todo: compute auth tag
		file.write(auth_tag);
	}

	private void writeWithoutAuthTag(OutputStream out) throws Exception {
		out.write(version);
		out.write(reserved1);
		out.write(reserved2);
		out.write(reserved3);
		out.write(day);
		out.write(month);
		byte year1 = (byte) (year >> 8);
		byte year2 = (byte) (year & 0xff);
		out.write(year1);
		out.write(year2);
		out.write(logging_entity);
		out.write(logged_fields);
		out.write(nr_of_log_entries_per_encrypted_log_line);
		out.write(nr_of_keys);

		for (int i = 0; i < nr_of_keys; i++)
			out.write(keys[i].encryptedKeyBlock);
		out.flush();
	}

	public void parseFromFile(FileInputStream file) throws IOException {
		version = (byte) file.read();
		if (version != 0)
			throw new IOException("Unknown version");
		reserved1 = (byte) file.read();
		reserved2 = (byte) file.read();
		reserved3 = (byte) file.read();
		day = (byte) file.read();
		month = (byte) file.read();
		short year1 = (short) file.read();
		short year2 = (short) file.read();
		year = (short) ((year1 << 8) | year2);
		Date d=new Date(year-1900,month-1,day);
		m_BaseTime=d.getTime()/1000;
		logging_entity = (byte) file.read();
		logged_fields = (byte) file.read();
		nr_of_log_entries_per_encrypted_log_line = (short) file.read();
		nr_of_keys = (byte) file.read();
		for (int i = 0; i < nr_of_keys; i++) {
			keys[i].encryptedKeyBlock = new byte[t_encrypted_key.ENCRYPTED_KEY_LENGTH];
			file.read(keys[i].encryptedKeyBlock);
		}
		file.read(auth_tag);
		
		//Calc size of log entry
		///todo
		m_sizeOfLogEntry=18;
	}

	public int getLength() {
		return 12 + (t_encrypted_key.ENCRYPTED_KEY_LENGTH * nr_of_keys)
				+ (DataRetentionSmartCard.GCM_AUTH_TAG_LENGTH / 8); // simple values +
														// t_encrypted_key[nr_of_keys]
														// + auth_tag[]
	}

	public byte[] getEncryptedKey(int index) {
		return keys[index].encryptedKeyBlock;
	}

	public int getEncryptedKeyCount() {
		return nr_of_keys;
	}

	public static byte[] decryptSymKey(byte[] encSymKey, MyRSAPrivateKey privKey)
			throws Exception {
		MyRSA rsa = new MyRSA();
		rsa.init(privKey);
		byte[] decBlock = rsa.processBlockPKCS1(encSymKey, 0, encSymKey.length);
		byte[] key = new byte[16];
		System.arraycopy(decBlock, 0, key, 0, 16);
		byte[] iv = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE };
		verifyMac(decBlock,0,20,decBlock,20,iv,key);
		SHA512Digest sha = new SHA512Digest();
		sha.update(decBlock, 16, 4);
		byte digest[] = new byte[sha.getDigestSize()];
		sha.doFinal(digest, 0);
		for (int i = 0; i < 16; i++)
			key[i] = (byte) (decBlock[i] ^ digest[i]);
		return key;
	}

	public void verifyHeader(byte[] key) throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		writeWithoutAuthTag(bout);
		byte[] header = bout.toByteArray();
		byte[] iv = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFD };
		verifyMac(header,auth_tag,iv,key);
	}


	public int getSizeOfLogLine()
	{
		return nr_of_log_entries_per_encrypted_log_line*m_sizeOfLogEntry+16;
	}
	
	public int getNrOfLogEntriesPerLogLine()
	{
		return nr_of_log_entries_per_encrypted_log_line;
	}
	
	public int getSizeOfLogEntry()
	{
		return m_sizeOfLogEntry;
	}
	
	public long getBaseTime()
	{
		return m_BaseTime;
	}
	
	public static int decryptAndVerify(byte[] in, byte[] iv, byte[] key,
			byte[] plainOut) throws Exception {
		return decryptAndVerify(in,0,in.length, iv,key,plainOut);
	}

	/** Decrypts and verifes an AES_GCM encrypted buffer.
	 * 
	 * @param in ciphertext
	 * @param inOff ciphtertext offset
	 * @param inLen length of ciphertext
	 * @param iv IV
	 * @param key key
	 * @param plainOut plaintext
	 * @return length of plntext
	 * @throws Exception
	 */
	public static int decryptAndVerify(byte[] in,int inOff,int inLen, byte[] iv, byte[] key,
			byte[] plainOut) throws Exception {
		GCMBlockCipher gcmCipher = new GCMBlockCipher(new AESFastEngine());
		gcmCipher.init(false, new AEADParameters(new KeyParameter(key), 128,
				iv, null));
		byte[] plainText = new byte[gcmCipher.getOutputSize(inLen)];
		int outL = gcmCipher.processBytes(in, inOff, inLen, plainText, 0);
		outL+=gcmCipher.doFinal(plainText, outL);
		if(plainOut!=null)
			System.arraycopy(plainText, 0, plainOut, 0, Math.min(outL,plainOut.length));
		return outL;
	}
	
	private static void verifyMac(byte[] in, byte[] tag, byte[] iv,
			byte[] key)throws Exception {
		verifyMac(in,0,in.length, tag,0, iv,key);

	}
	static void verifyMac(byte[] in,int inOff,int inLen, byte[] tag,int tagOff, byte[] iv,
			byte[] key)throws Exception {
		GCMBlockCipher gcmCipher = new GCMBlockCipher(new AESFastEngine());
		gcmCipher.init(true, new AEADParameters(new KeyParameter(key), 128,
				iv, null));
		byte[] cipherText = new byte[gcmCipher.getOutputSize(inLen)];
		int outL = gcmCipher.processBytes(in, inOff, inLen, cipherText, 0);
		outL+=gcmCipher.doFinal(cipherText, outL);
		if(!Util.arraysEqual(tag, tagOff, cipherText, cipherText.length-16, 16))
			throw new Exception("Wrong MAC");
	}

}
