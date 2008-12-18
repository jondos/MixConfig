/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mixconfig.tools.dataretention;
import java.io.FileOutputStream;
import java.io.FileInputStream;

/**
 *
 * @author Petr Svenda
 */

public class LogHeader {
    public static final byte MAX_nr_of_keys = 10;

    public byte	version = 0;
    public byte	reserved1 = 0;
    public byte reserved2 = 0;
    public byte	reserved3 = 0;
    public byte	day = 0;
    public byte	month = 0;
    public short year = 0;
    public byte	logging_entity = 0;
    public byte	logged_fields = 0;
    public byte	nr_of_log_entries_per_encrypted_log_line = 1;
    public byte	nr_of_keys = 1;
    public t_encrypted_key[] keys = null;
    public byte auth_tag[] = null;

    public LogHeader() {
        keys = new t_encrypted_key[MAX_nr_of_keys];
        for (int i = 0; i < MAX_nr_of_keys; i++) keys[i] = new t_encrypted_key();
        auth_tag = new byte[ANONSCLog.GCM_AUTH_TAG_LENGTH / 8];
    }
    public void writeToFile(FileOutputStream file) throws Exception {
        file.write(version);
        file.write(reserved1);
        file.write(reserved2);
        file.write(reserved3);
        file.write(day);
        file.write(month);
        byte year1 = (byte) (year >> 8);
        byte year2 = (byte) (year & 0xff);
        file.write(year1);
        file.write(year2);
        file.write(logging_entity);
        file.write(logged_fields);
        file.write(nr_of_log_entries_per_encrypted_log_line);
        file.write(nr_of_keys);

        for (int i = 0; i < nr_of_keys; i++) file.write(keys[i].encryptedKeyBlock);
        // todo: compute auth tag
        file.write(auth_tag);
    }
    public void parseFromFile(FileInputStream file) throws Exception {
        version = (byte) file.read();
        reserved1 = (byte) file.read();
        reserved2 = (byte) file.read();
        reserved3 = (byte) file.read();
        day = (byte) file.read();
        month = (byte) file.read();
        short year1 = (short) file.read();
        short year2 = (short) file.read();
        year = (short) ((year1 << 8) | year2);
        logging_entity = (byte) file.read();
        logged_fields = (byte) file.read();
        nr_of_log_entries_per_encrypted_log_line = (byte) file.read();
        nr_of_keys = (byte) file.read();
        for (int i = 0; i < nr_of_keys; i++) {
            keys[i].encryptedKeyBlock = new byte[t_encrypted_key.ENCRYPTED_KEY_LENGTH];
            file.read(keys[i].encryptedKeyBlock);
        }
        file.read(auth_tag);
    }

    public int GetLength() {
        return 12 + (t_encrypted_key.ENCRYPTED_KEY_LENGTH * nr_of_keys) + (ANONSCLog.GCM_AUTH_TAG_LENGTH / 8);   // simple values + t_encrypted_key[nr_of_keys] + auth_tag[]
    }
}
