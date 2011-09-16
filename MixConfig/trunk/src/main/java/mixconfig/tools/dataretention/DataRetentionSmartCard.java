/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mixconfig.tools.dataretention;

import java.security.PublicKey;
import java.util.List;
import java.math.BigInteger;

import javax.crypto.Cipher;
import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.digests.SHA512Digest;
import anon.crypto.MyRSAPublicKey;

/**
 *
 * @author Petr Svenda
 */
public class DataRetentionSmartCard {
    CardTerminal  m_terminal = null;
    CardChannel   m_channel = null;
    Card          m_card = null;

    private final byte OFFSET_CLA = 0x00;
    private final byte OFFSET_INS = 0x01;
    private final byte OFFSET_P1 = 0x02;
    private final byte OFFSET_P2 = 0x03;
    private final byte OFFSET_LC = 0x04;
    private final byte OFFSET_DATA = 0x05;
    private final byte HEADER_LENGTH = 0x05;
    private final short RSA_KEY_LENGTH = (short) 256;

    private final byte CLA_ANON = (byte) 0xB0;
    private final byte INS_AUTHADMIN = 0x30;
    private final byte INS_SETDATE = 0x31;
    private final byte INS_UNBLOCKUSERPIN = 0x32;
    private final byte INS_SETADMINPIN = 0x33;

    private final byte INS_AUTHUSER = (byte) 0x50;
    private final byte INS_GETLOGKEY = (byte) 0x51;
    private final byte INS_GETPUBLICKEY_EXP = (byte) 0x52;
    private final byte INS_GETPUBLICKEY_MOD = (byte) 0x53;
    private final byte ANON_LOG_KEY_LENGTH = (byte) 0x10;
    private final short APDU_DATA_LENGTH                = (short) 0xc8;

    public final static byte DATE_LENGTH                      = (short) 0x04;
    public final static byte DATE_AUTH_TAG_LENGTH             = (short) 0x10;
    public final static short GCM_AUTH_TAG_LENGTH             = (short) 128;
    public final static short ENCRYPTED_KEY_LENGTH            = (short) 256;

    private final byte selectANONApplet[] = {
        (byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x0D, (byte) 0x41, (byte) 0x4E, (byte) 0x4F, (byte) 0x4E,
        (byte) 0x4C, (byte) 0x6F, (byte) 0x67, (byte) 0x41, (byte) 0x70, (byte) 0x70, (byte) 0x6C, (byte) 0x65, (byte) 0x74};

   public static byte entity_entry_lengths[] = {(byte) 0, (byte) 18, (byte) 16, (byte) 18, (byte) 12};

   public final static short AES_BLOCK_LENGTH            = (short) 16;
   public final static short AES128_KEY_LENGTH           = (short) 16;

  private static final short EXCEPTION_NEW_DATE_IN_PAST      = (short) 0x6001;
  private static final short EXCEPTION_OUTSIDE_RETENTION_PERIOD = (short) 0x6002;
  private static final short SW_SECURITY_STATUS_NOT_SATISFIED = (short) 0x6002;

   /**
     * Method returns list of smart card readers available in system
     * @returns readersList
     *
     */
    public List GetReaderList() {
         try {
            TerminalFactory factory = TerminalFactory.getDefault();
            List readersList = factory.terminals().list();
            return readersList;
         }
         catch (Exception ex) {
            System.out.println("Exception : " + ex);
            return null;
         }
    }
    
   /**
     * Method search all available readers and connect to first ANON card found and select ANONLog applet
     * @returns true if such card is found
     *
     */
    public boolean connectToSmartCard() throws Exception {
        // TRY ALL READERS, FIND FIRST SELECTABLE
        List terminalList = GetReaderList();

        //List numbers of Card readers
        boolean     cardFound = false;
        for (int i=0;  i < terminalList.size(); i++){
            System.out.println(i + " : " + terminalList.get(i));
            m_terminal = (CardTerminal) terminalList.get(i);
            if (m_terminal.isCardPresent()) {
                // TRY TO CONNECT VIA T0 AND IF FAIL THEN BY T1
                //m_card = m_terminal.connect("T=0");
                m_card = m_terminal.connect("*");

                System.out.println("card: " + m_card);
                m_channel = m_card.getBasicChannel();

                //reset the card
                ATR atr = m_card.getATR();
                System.out.println(atr.toString());

                // SELECT APPLET
                ResponseAPDU resp = sendAPDU(selectANONApplet);
                if (resp.getSW() != 0x9000) {
                    System.out.println("Not ANON card.");
                }
                else {
                    // CARD FOUND
                    cardFound = true;
                    break;
                }
            }
        }

        return cardFound;
    }

   /**
     * Method disconnect from cad and close session
     */
    public void DisconnectFromCard() throws Exception {
        if (m_card != null) {
            m_card.disconnect(false);
            m_card = null;
        }
    }
    
   /**
     * Method uses existing connection to card, retrieve public exponent and modulus (in case of RSA) and construct public key
     * @returns retrieved public key
     *
     */
    public MyRSAPublicKey retrievePublicKey() throws Exception {
    	MyRSAPublicKey     key = null;
        byte[]  exponent = null;
        byte[]  modulus = null;
        short   modulusOffset = 0;

        // GET PUBLIC KEY EXPONENT
        byte apdu[] = new byte[HEADER_LENGTH];
        apdu[OFFSET_CLA] = CLA_ANON;
        apdu[OFFSET_INS] = INS_GETPUBLICKEY_EXP;
        apdu[OFFSET_P1] = 0x00;
        apdu[OFFSET_P2] = 0x00;
        apdu[OFFSET_LC] = 0x00;

        ResponseAPDU resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            System.out.println("Fail to get public exponent");
        }
        else {
            byte temp[] = resp.getData();
            exponent = new byte[temp.length];
            System.arraycopy(temp, 0, exponent, 0, temp.length);
        }

        apdu[OFFSET_CLA] = (byte) CLA_ANON;
        apdu[OFFSET_INS] = (byte) INS_GETPUBLICKEY_MOD;
        apdu[OFFSET_P1] = 0x00;

        resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            System.out.println("Fail to get public modulus");
        }
        else {
            byte temp[] = resp.getData();
            modulus = new byte[RSA_KEY_LENGTH];
            System.arraycopy(temp, 0, modulus, 0, temp.length);
            modulusOffset += temp.length;
        }

        apdu[OFFSET_CLA] = (byte) CLA_ANON;
        apdu[OFFSET_INS] = (byte) INS_GETPUBLICKEY_MOD;
        apdu[OFFSET_P1] = 0x01;

        resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            System.out.println("Fail to get public modulus");
        }
        else {
            byte temp[] = resp.getData();
            System.arraycopy(temp, 0, modulus, modulusOffset, temp.length);
            modulusOffset += temp.length;
        }

        BigInteger modulusInt = new BigInteger(1,modulus);
        System.out.println("Key modulus is :" + modulusInt);
        BigInteger exponentInt = new BigInteger(1,exponent);
        System.out.println("Key exponent is :" + exponentInt);
        key=new MyRSAPublicKey(modulusInt, exponentInt);

        return key;
    }

   /**
     * Method encrypts provided log key by public key of secure hardware (e.g. smart card) together with date of log key creation. Log key can be retrieved back only of entity controling private key (smart card) verifies date to be in data retention period.
    * @param logKey value of symmetrci cryptography key used to encrypt log entries
    * @param day actual day of log file creation
    * @param month actual month of log file creation
    * @param year actual year of log file creation
    * @param publicKey RSA public key used to store log key. 
    * @return encrypted block by publicKey with logKey and date inside
    *
     *
     */
    public byte[] EncryptLogKey(byte[] logKey, byte day, byte month, short year, PublicKey publicKey) throws Exception {
        // TODO: return explaining sttaus
        if (logKey.length != ANON_LOG_KEY_LENGTH) return null;
        //
        // Creates an RSA Cipher object (specifying the algorithm, mode, and padding).
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        //Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        //
        // Print the provider information
        System.out.println( "\n" + cipher.getProvider().getInfo() );
        System.out.println( "\nStart encryption" );

        // FORMAT KEY BLOCK
        byte[] keyBlock = new byte[logKey.length + DATE_LENGTH + DATE_AUTH_TAG_LENGTH];
        //byte[] keyBlock = new byte[256];
        System.arraycopy(logKey, 0, keyBlock, 0, logKey.length);
        keyBlock[logKey.length] = day;
        keyBlock[logKey.length + 1] = month;
        keyBlock[logKey.length + 2] = (byte) (year >> 8);
        keyBlock[logKey.length + 3] = (byte) (year & 0xff);

        // COMPUTE AUTHENTICATION TAG
        GCMBlockCipher gcmCipher = new GCMBlockCipher(new AESFastEngine());
        byte iv[] = new byte[16];
        gcmCipher.init(true, new AEADParameters(new KeyParameter(logKey), DATE_AUTH_TAG_LENGTH * 8, iv, null));
        int tmp = gcmCipher.getOutputSize(logKey.length + DATE_LENGTH);
        byte[] outblock = new byte[tmp];
        int outL = gcmCipher.processBytes(keyBlock, 0, logKey.length + DATE_LENGTH, outblock, 0);
        gcmCipher.doFinal(outblock, outL);
        byte[] gcmMAC = gcmCipher.getMac();
        System.arraycopy(gcmMAC, 0, keyBlock, logKey.length + DATE_LENGTH, gcmMAC.length);

        //
        // Initializes the Cipher object.
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        //
        // Encrypt the plaintext using the public key
        byte[] cipherText = cipher.doFinal(keyBlock);
        System.out.println( "Finish encryption: " );
        System.out.println(bytesToHex(cipherText));

        return cipherText;
    }


    public int GetExpectedLogLineLength(int plainDataLength) throws Exception {
        // COMPUTE AUTHENTICATION TAG
        GCMBlockCipher gcmCipher = new GCMBlockCipher(new AESFastEngine());
        byte iv[] = new byte[16];
        byte logKey[] = new byte[16];
        gcmCipher.init(true, new AEADParameters(new KeyParameter(logKey), GCM_AUTH_TAG_LENGTH, iv, null));
        return gcmCipher.getOutputSize(plainDataLength);
    }

   /**
     * Method encrypts provided data in GCM mode with AES 128bits keys
     * @param logKey value of symmetrci cryptography key used to encrypt log entries
    * @param data raw log data to be encrypted
    * @param iv initialization vector used to symmetric cryptography encryption
    * @param year actual year of log file creation
    * @param publicKey RSA public key used to store log key. Log key can be retrieved back only of entity controling private key (smart card) verifies date to be in data retention period
    * @return encrypted block by publicKey with logKey and date inside
    *
     *
     */
    public byte[] EncryptLogLine(byte[] logKey, byte[] data, byte[] iv) throws Exception {
        // ENCRYPT AND COMPUTE AUTHENTICATION TAG
        GCMBlockCipher gcmCipher = new GCMBlockCipher(new AESFastEngine());
        gcmCipher.init(true, new AEADParameters(new KeyParameter(logKey), GCM_AUTH_TAG_LENGTH, iv, null));
        int tmp = gcmCipher.getOutputSize(data.length);
        byte[] cipherText = new byte[tmp];
        int outL = gcmCipher.processBytes(data, 0, data.length, cipherText, 0);
        gcmCipher.doFinal(cipherText, outL);

        return cipherText;
    }

   /**
     * Method tries to retrieve decrypted log key from smart card and then decrypt provided data. Key is returned only if request is within data reteion period. User must be authenticated to smart card by User_Authenticate() method in advance.
     * @param encrLogKey encrypted value of symmetric cryptography key used to encrypt log entries
    * @param encrData encrypted raw log data to be decrypted
    * @param iv initialization vector used to symmetric cryptography encryption
    * @return decrypted raw log data
     */
    public byte[] DecryptLogLine(byte[] encrLogKey, byte[] encrData, byte[] iv) throws Exception {
        // RETRIEVE KEY FROM CARD
        byte[] plainKey = null;
        
        if ((plainKey = decrpytSymmetricKey(encrLogKey)) != null) {
            return DecryptLogLineKey(plainKey, encrData, iv);
        }
        else return null;
    }

   /**
     * Method decrypt provided data with provided log key.
     * @param logKey AES128bits key used to encrypt log entries
    * @param encrData encrypted raw log data to be decrypted
    * @param iv initialization vector used to symmetric cryptography encryption
    * @return decrypted raw log data
     */
    public byte[] DecryptLogLineKey(byte[] logKey, byte[] encrData, byte[] iv) throws Exception {
        // DECRYPT AND VERIFY AUTHENTICATION TAG
      /*  GCMBlockCipher gcmCipher = new GCMBlockCipher(new AESFastEngine());
        byte iv1[] = new byte[12];
        gcmCipher.init(true, new AEADParameters(new KeyParameter(logKey), GCM_AUTH_TAG_LENGTH, iv1, null));
        int tmp = gcmCipher.getOutputSize(encrData.length);
        byte[] plainText = new byte[128];
        int outL = gcmCipher.processBytes(plainText, 0, 16, plainText, 64);
        gcmCipher.doFinal(plainText, outL+64);
*/
    	GCMBlockCipher gcmCipher = new GCMBlockCipher(new AESFastEngine());
        gcmCipher.init(false, new AEADParameters(new KeyParameter(logKey), GCM_AUTH_TAG_LENGTH, iv, null));
        int tmp = gcmCipher.getOutputSize(encrData.length);
        byte[] plainText = new byte[tmp];
        int outL = gcmCipher.processBytes(encrData, 0, encrData.length, plainText, 0);
        gcmCipher.doFinal(plainText, outL);

        return plainText;
    }

   /**
     * Method verifies supplied user PIN on smart card and allows to call protected function subsequently.
     * @param pin user PIN
    * @return true if PIN verification was succesfull, false otherwise
     */
    public boolean authenticateUser(byte[] pin) throws Exception {
        boolean     status = false;
        // AUTH USER PIN
        byte apdu[] = new byte[HEADER_LENGTH + pin.length];
        apdu[OFFSET_CLA] = CLA_ANON;
        apdu[OFFSET_INS] = INS_AUTHUSER;
        apdu[OFFSET_P1] = 0x00;
        apdu[OFFSET_P2] = 0x00;         // final apdu
        apdu[OFFSET_LC] = (byte) pin.length;
        System.arraycopy(pin, 0, apdu, OFFSET_DATA, pin.length);

        ResponseAPDU resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            System.out.println("Fail to verify User PIN");
            status = false;
        }
        else {
            System.out.println("User PIN verification OK");
            status = true;
        }
        return status;
    }

   /**
     * Method authenticate admin credentials allows to call protected function subsequently.
     * @param key admin credentials (currently PIN)
     * @return true if credentials verification was succesfull, false otherwise
     */
    public boolean Admin_Authenticate(byte[] key) throws Exception {
        boolean     status = false;

        // TODO: GET ADMIN CHALLENGE
        // AUTH ADMIN PIN
        byte apdu[] = new byte[HEADER_LENGTH + key.length];
        apdu[OFFSET_CLA] = CLA_ANON;
        apdu[OFFSET_INS] = INS_AUTHADMIN;
        apdu[OFFSET_P1] = 0x00;
        apdu[OFFSET_P2] = 0x00;         // final apdu
        apdu[OFFSET_LC] = (byte) key.length;
        System.arraycopy(key, 0, apdu, OFFSET_DATA, key.length);

        ResponseAPDU resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            System.out.println("Fail to verify Admin PIN");
            status = false;
        }
        else {
            System.out.println("Admin PIN verification OK");
            status = true;
        }
        return status;
    }

   /**
     * Method set current trusted date on smart card, used later to check data retention period. Administrator must be authenticated to smart card in advance via Admin_Authenticate() method.
     * @return true if trusted date was set, false otherwise
     */
    public boolean Admin_SetCurrentDate(byte day, byte month, short year) throws Exception {
        boolean     status = false;

        byte apdu[] = new byte[HEADER_LENGTH + DATE_LENGTH];
        apdu[OFFSET_CLA] = CLA_ANON;
        apdu[OFFSET_INS] = INS_SETDATE;
        apdu[OFFSET_P1] = 0x00;
        apdu[OFFSET_P2] = 0x00;         // final apdu
        apdu[OFFSET_LC] = (byte) DATE_LENGTH;
        apdu[OFFSET_DATA] = day;
        apdu[OFFSET_DATA + 1] = month;
        apdu[OFFSET_DATA + 2] = (byte) (year >> 8);
        apdu[OFFSET_DATA + 3] = (byte) (year & 0xff);

        ResponseAPDU resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            System.out.println("Fail to set current date, check if admin is authenticated.");
            status = false;
        }
        else {
            System.out.println("Actual date set OK");
            status = true;
        }
        return status;
    }

   /**
     * Method set new value of administration PIN. Administrator must be authenticated to smart card in advance via Admin_Authenticate() method.
     * @return true if new PIN was set succesfully, false otherwise
     */
    public boolean Admin_SetAdminPIN(byte[] newPINValue) throws Exception {
        boolean     status = false;

        byte apdu[] = new byte[HEADER_LENGTH + newPINValue.length];

        apdu[OFFSET_CLA] = CLA_ANON;
        apdu[OFFSET_INS] = INS_SETADMINPIN;
        apdu[OFFSET_P1] = 0x00;
        apdu[OFFSET_P2] = 0x00;
        apdu[OFFSET_LC] = (byte)(newPINValue.length);

        ResponseAPDU resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            System.out.println("Fail to set admin PIN, check if admin is authenticated.");
            status = false;
        }
        else {
            System.out.println("Admin PIN set");
            status = true;
        }
        return status;
    }

   /**
     * Method set unblock or set new PIN value for user PIN. Administrator must be authenticated to smart card in advance via Admin_Authenticate() method.
     * @param newPINValue if null, user PIN is only unblocked. If not null then new user PIN is set to given value.
    * @return true if new PIN was set succesfully, false otherwise
     */
    public boolean Admin_UnblockUserPIN(byte[] newPINValue) throws Exception {
        boolean     status = false;

        byte apdu[] = null;
        if (newPINValue == null) apdu = new byte[HEADER_LENGTH];
        else apdu = new byte[HEADER_LENGTH + newPINValue.length];

        apdu[OFFSET_CLA] = CLA_ANON;
        apdu[OFFSET_INS] = INS_UNBLOCKUSERPIN;
        apdu[OFFSET_P1] = (byte)((newPINValue != null) ? 0x01 : 0x00);
        apdu[OFFSET_P2] = 0x00;         
        apdu[OFFSET_LC] = (byte)((newPINValue != null) ? newPINValue.length : 0x00);

        ResponseAPDU resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            System.out.println("Fail to unblock user PIN, check if admin is authenticated.");
            status = false;
        }
        else {
            System.out.println("User PIN ublocked");
            status = true;
        }
        return status;
    }

   /**
     * Method try to retrieve decrypted key from smart card. Sucesfull only if key usage is within data retention period. User must be authenticated to smart card in advance via User_Authenticate() method.
     * @param encryptedKey block containing encrypted log key with usage date.
     * @return decrypted lo key, if key can be retrieved, null otherwise
     */
    public byte[] decrpytSymmetricKey(byte[] encryptedKey) throws Exception {
    	
    	int remainingLength = encryptedKey.length;
        byte apduCounter = 0;
        int offset = 0;
        while (remainingLength > APDU_DATA_LENGTH) {
        // PREPARE GET LOG KEY - INTERMEDIATE APDU
            byte apdu[] = new byte[HEADER_LENGTH + APDU_DATA_LENGTH];
            apdu[OFFSET_CLA] = CLA_ANON;
            apdu[OFFSET_INS] = INS_GETLOGKEY;
            apdu[OFFSET_P1] = apduCounter;
            apdu[OFFSET_P2] = 0x00;         // intermediate command
            apdu[OFFSET_LC] = (byte) APDU_DATA_LENGTH;
            System.arraycopy(encryptedKey, offset, apdu, OFFSET_DATA, APDU_DATA_LENGTH);

            ResponseAPDU resp = sendAPDU(apdu);
            if (resp.getSW() != 0x9000) {
                System.out.println("Fail to decrypt key");
                return null;
            }

            offset += APDU_DATA_LENGTH;
            remainingLength -= APDU_DATA_LENGTH;
            apduCounter++;
        }

        // PREPARE GET LOG KEY - FINAL APDU
        byte apdu[] = new byte[HEADER_LENGTH + remainingLength];
        apdu[OFFSET_CLA] = CLA_ANON;
        apdu[OFFSET_INS] = INS_GETLOGKEY;
        apdu[OFFSET_P1] = apduCounter;
        apdu[OFFSET_P2] = 0x01;         // final apdu
        apdu[OFFSET_LC] = (byte) remainingLength;
        System.arraycopy(encryptedKey, offset, apdu, OFFSET_DATA, remainingLength);

        ResponseAPDU resp = sendAPDU(apdu);
        if (resp.getSW() != 0x9000) {
            System.out.println("Fail to decrypt key");
           return null;
        }
        else {
            System.out.println(bytesToHex(resp.getBytes()));
        }

        byte returnKey[] = null;
        byte[] key = resp.getData();
        if (key.length > AES128_KEY_LENGTH) {
            returnKey = new byte[AES128_KEY_LENGTH];
            System.arraycopy(key, 0, returnKey, 0, AES128_KEY_LENGTH);
    		byte[] iv = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
    				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
    				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE };
    		DataRetentionLogFileHeader.verifyMac(key,0,20,key,20,iv,returnKey);
    		SHA512Digest sha = new SHA512Digest();
    		sha.update(key, 16, 4);
    		byte digest[] = new byte[sha.getDigestSize()];
    		sha.doFinal(digest, 0);
    		for (int i = 0; i < 16; i++)
    			returnKey[i] = (byte) (key[i] ^ digest[i]);
        }
        else 
        	returnKey = key;

        return returnKey;
    }

   /**
     * Method creates proper raw initialization vector from value of blocksCounter.
     * @param blocksCounter Actual counter of blocks (aka log lines)
    * @return Array with initialization vector.
     */
    public byte[] CreateIV(int blocksCounter) {
        byte[] iv = new byte[12];
        for (int i = 0; i < iv.length; i++) iv[i] = 0;
        iv[8] = (byte) (blocksCounter >> 24 & 0xff);
        iv[9] = (byte) (blocksCounter >> 16 & 0xff);
        iv[10] = (byte) (blocksCounter >> 8 & 0xff);
        iv[11] = (byte) (blocksCounter & 0xff);
        return iv;
    }

   /**
     * Method creates footer from total number of blocks value and encrypts it with log key for this file.
     * @param blocksCounter Total blocks (aka log lines) in file.
    * @return Array with footer bytes.
     */
    public byte[] CreateFooter(byte[] logKey, int blocksCounter) throws Exception {
        blocksCounter++;
        byte[] data = new byte[4];
        data[0] = (byte) (blocksCounter >> 24 & 0xff);
        data[1] = (byte) (blocksCounter >> 16 & 0xff);
        data[2] = (byte) (blocksCounter >> 8 & 0xff);
        data[3] = (byte) (blocksCounter & 0xff);
        return EncryptLogLine(logKey, data, CreateIV(blocksCounter));
    }

   /**
     * Method verifies footer integrity and return number of blocks (log lines) stored in file.
     * @param logKey Log key used to encrypt and MAC file
     * @param footer Encrypted footer.
     * @param counter Expected value of blocks stored in file - must be supplied as is used as IV for decryption.
    * @return Number of blocks that should be present in file.
     */
    public int VerifyFooter(byte[] logKey, byte[] footer, int counter) throws Exception {
        byte[] iv = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        byte[] data = DecryptLogLineKey(logKey, footer, iv);
        int numBlocks = data[0] << 24 | ((data[1] << 16)&0x00FF0000) | ((data[2] << 8)&0x00FF00) | (data[3]&0x00FF);
        return numBlocks;
    }
    

    public String byteToHex(byte data) {
        StringBuffer buf = new StringBuffer();
        buf.append(toHexChar((data>>>4)&0x0F));
        buf.append(toHexChar(data&0x0F));
        return buf.toString();
    }

    public char toHexChar(int i) {
        if ((0 <= i) && (i <= 9 ))
        return (char)('0' + i);
        else
        return (char)('a' + (i-10));
    }

    public String bytesToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for ( int i = 0; i < data.length; i++ ) {
            buf.append(byteToHex(data[i]) );
            buf.append(" ");
        }
        return(buf.toString());
    }


    private ResponseAPDU sendAPDU(byte apdu[]) throws Exception {
        CommandAPDU commandAPDU = new CommandAPDU(apdu);

        System.out.println(">>>>");
        System.out.println(commandAPDU);

        System.out.println(bytesToHex(commandAPDU.getBytes()));

        ResponseAPDU responseAPDU = m_channel.transmit(commandAPDU);

        System.out.println(responseAPDU);
        System.out.println(bytesToHex(responseAPDU.getBytes()));

        if (responseAPDU.getSW1() == (byte) 0x61) {
            CommandAPDU apduToSend = new CommandAPDU((byte) 0x00,
                    (byte) 0xC0, (byte) 0x00, (byte) 0x00,
                    (int) responseAPDU.getSW1());

            responseAPDU = m_channel.transmit(apduToSend);
            System.out.println(bytesToHex(responseAPDU.getBytes()));
        }

        System.out.println("<<<<");

        return (responseAPDU);
    }
}
