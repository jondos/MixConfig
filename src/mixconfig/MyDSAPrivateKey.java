package mixconfig;

import java.security.interfaces.DSAPrivateKey;
import java.security.InvalidKeyException;
import java.math.BigInteger;
import java.security.interfaces.DSAParams;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERInputStream;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;

public class MyDSAPrivateKey implements DSAPrivateKey
	{
		private BigInteger m_X;
		private DSAParams m_params;

		public MyDSAPrivateKey(byte[] encoded) throws InvalidKeyException
			{
				try
					{
						ByteArrayInputStream bIn =new ByteArrayInputStream(encoded);
						DERInputStream dIn = new DERInputStream(bIn);
						PrivateKeyInfo privKeyInfo=new PrivateKeyInfo((ASN1Sequence)dIn.readObject());
						AlgorithmIdentifier algId=privKeyInfo.getAlgorithmId();
						DERInteger X=(DERInteger)privKeyInfo.getPrivateKey();
						m_X=X.getValue();
						m_params=new MyDSAParams(new DSAParameter((ASN1Sequence)algId.getParameters()));
					}

				catch (IOException e)
					{
						throw new InvalidKeyException("IOException while decoding private key");
					}

			}

		public MyDSAPrivateKey(DSAPrivateKeyParameters keyParams)
			{
				m_X= keyParams.getX();
				m_params=new MyDSAParams(keyParams.getParameters());
			}

		public String getAlgorithm()
			{
				return "DSA";
			}

		public String getFormat()
			{
				return "PKCS#8";
			}

		public BigInteger getX()
			{
				return m_X;
			}



		public byte[] getEncoded()
			{
				ByteArrayOutputStream bOut =new ByteArrayOutputStream();
				DEROutputStream dOut = new DEROutputStream(bOut);

				try
					{
						DERObject derParam =
											new DSAParameter(
													m_params.getP(),
													m_params.getQ(),
													m_params.getG())
													.getDERObject();

						dOut.writeObject(
								new PrivateKeyInfo(
										new AlgorithmIdentifier(
												X9ObjectIdentifiers.id_dsa,
												derParam),
										new DERInteger(getX())));
						dOut.close();
					}
				catch (IOException e)
					{
						throw new RuntimeException("IOException while encoding private key");
					}
				return bOut.toByteArray();
		}

		public DSAParams getParams()
			{
				return m_params;
			}
 }
