package mixconfig.tools.dataretention;

import java.util.Vector;

import anon.crypto.X509DistinguishedName;
import anon.crypto.X509Extensions;
import mixconfig.ICertCreationValidator;

public class DataRetentionEncryptionCertCreationValidator implements
		ICertCreationValidator {

	@Override
	public X509Extensions getExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector<String> getInvalidityMessages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPasswordInfoMessage() {
		// TODO Auto-generated method stub
		return "Enter the password to protect the data retention decryption key";
	}

	@Override
	public X509DistinguishedName getSigName() {
		// TODO Auto-generated method stub
		return new X509DistinguishedName("cn=Date Retention Key");
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

}
