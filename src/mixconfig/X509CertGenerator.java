package mixconfig;

import java.io.ByteArrayOutputStream;
import java.security.interfaces.DSAPrivateKey;
import java.math.BigInteger;

import org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.signers.DSASigner;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DEREncodableVector;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.asn1.DERSequence;


final public class X509CertGenerator extends V3TBSCertificateGenerator
	{
			public X509CertGenerator()
				{
				}

			public X509CertGenerator(X509CertificateStructure cert)
				{
					this(cert.getTBSCertificate());
				}

			public X509CertGenerator(TBSCertificateStructure tbs)
				{
					setEndDate(tbs.getEndDate());
					setExtensions(tbs.getExtensions());
					setIssuer(tbs.getIssuer());
					setSerialNumber(tbs.getSerialNumber());
					setSignature(tbs.getSignature());
					setStartDate(tbs.getStartDate());
					setSubject(tbs.getSubject());
					setSubjectPublicKeyInfo(tbs.getSubjectPublicKeyInfo());
				}

			public X509CertificateStructure sign(X509Name issuer,DSAPrivateKey key)
				{
					try
						{
							setIssuer(issuer);
							AlgorithmIdentifier algID = new AlgorithmIdentifier( X9ObjectIdentifiers.id_dsa_with_sha1);
							setSignature(algID);

							DSASigner signer = new DSASigner();
							SHA1Digest digest = new SHA1Digest();
							DSAPrivateKeyParameters signkey=new DSAPrivateKeyParameters(key.getX(),new MyDSAParams(key));
							signer.init(true, signkey);

							TBSCertificateStructure tbsCert = generateTBSCertificate();
							ByteArrayOutputStream bOut = new ByteArrayOutputStream();
							(new DEROutputStream(bOut)).writeObject(tbsCert);
							digest.update(bOut.toByteArray(), 0, bOut.size());
							byte[] hash = new byte[digest.getDigestSize()];
							digest.doFinal(hash, 0);
							BigInteger[] r_and_s = signer.generateSignature(hash);
							DEREncodableVector sigvalue = new DEREncodableVector();
							sigvalue.add(new DERInteger(r_and_s[0]));
							sigvalue.add(new DERInteger(r_and_s[1]));
							DEREncodableVector seqv = new DEREncodableVector();
							seqv.add(tbsCert);
							seqv.add(algID);
							seqv.add(new DERBitString(new DERSequence(sigvalue)));
							return new X509CertificateStructure(new DERSequence(seqv));
						}
					catch(Throwable t)
						{
							return null;
						}
				}

}