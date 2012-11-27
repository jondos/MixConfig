package mixconfig.tools.dataretention;

import java.util.Vector;

import anon.crypto.X509DistinguishedName;
import anon.crypto.MyX509Extensions;
import mixconfig.ICertCreationValidator;

public class DataRetentionEncryptionCertCreationValidator implements
		ICertCreationValidator {

	public MyX509Extensions getExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<String> getInvalidityMessages() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPasswordInfoMessage() {
		// TODO Auto-generated method stub
		return "Enter the password to protect the data retention decryption key";
	}

	public X509DistinguishedName getSigName() {
		// TODO Auto-generated method stub
		return new X509DistinguishedName("cn=Date Retention Key");
	}

	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

}
