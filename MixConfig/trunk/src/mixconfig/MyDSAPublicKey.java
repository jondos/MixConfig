package mixconfig;

import java.security.interfaces.DSAPublicKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.interfaces.DSAParams;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;

public class MyDSAPublicKey implements DSAPublicKey
	{
		private BigInteger m_Y;
		private DSAParams m_params;

		public MyDSAPublicKey (DSAPublicKeyParameters params)
			{
				m_Y=params.getY();
				m_params=new MyDSAParams(params.getParameters());
			}

		public BigInteger getY()
			{
				return m_Y;
			}

		public DSAParams getParams()
			{
				return m_params;
			}

		public String getAlgorithm()
			{
				return "DSA";
			}

		public String getFormat()
			{
				return "X.509";
			}

		public SubjectPublicKeyInfo getAsSubjectPublicKeyInfo()
			{
				DERObject derParam = new DSAParameter( 	m_params.getP(),
																								m_params.getQ(),
																								m_params.getG()).getDERObject();
				AlgorithmIdentifier algID=new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa,
																													derParam);
				return new SubjectPublicKeyInfo(algID, new DERInteger(getY()));
			}

		public byte[] getEncoded()
			{
				ByteArrayOutputStream bOut =new ByteArrayOutputStream();
				DEROutputStream dOut = new DEROutputStream(bOut);
				try
					{
						dOut.writeObject(getAsSubjectPublicKeyInfo());
						dOut.close();
					}
				catch (IOException e)
					{
						throw new RuntimeException("IOException while encoding public key");
					}
				return bOut.toByteArray();
			}
	}