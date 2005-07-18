/*
 Copyright (c) 2000, The JAP-Team
 All rights reserved.
 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice,
  this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation and/or
  other materials provided with the distribution.

 - Neither the name of the University of Technology Dresden, Germany nor the names of its contributors
  may be used to endorse or promote products derived from this software without specific
  prior written permission.


 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS
 OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS
 BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 */
package anon.crypto;

/* Hint: This file may be only a copy of the original file which is always in the JAP source tree!
 * If you change something - do not forget to add the changes also to the JAP source tree!
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Enumeration;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.BERConstructedOctetString;
import org.bouncycastle.asn1.BEROutputStream;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DEREncodableVector;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.pkcs.AuthenticatedSafe;
import org.bouncycastle.asn1.pkcs.CertBag;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.EncryptedData;
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.MacData;
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.Pfx;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.SafeBag;
import org.bouncycastle.asn1.pkcs.SignedData;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.engines.RC2Engine;
import org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.DESParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.asn1.DERTags;
import org.bouncycastle.asn1.ASN1InputStream;

/**
 * This class creates and handles PKCS12 certificates, that include a private key,
 * a public key and an X509 certificate.
 */
public final class PKCS12 implements PKCSObjectIdentifiers, X509ObjectIdentifiers, ICertificate
{
	private static final int SALT_SIZE = 20;
	private static final int MIN_ITERATIONS = 100;

	//
	// SHA-1 and 3-key-triple DES.
	//
	private static final String KEY_ALGORITHM = "1.2.840.113549.1.12.1.3";

	//
	// SHA-1 and 40 bit RC2.
	//
	private static final String CERT_ALGORITHM = "1.2.840.113549.1.12.1.6";

	//
	// SHA-1 HMAC
	//
	// private static final String MAC_ALGORITHM = "1.3.14.3.2.26";

	private SecureRandom random = new SecureRandom();
	private String m_ownerAlias;
	private AsymmetricCryptoKeyPair m_keyPair;
	private JAPCertificate m_x509certificate;

	/**
	 *
	 * @param al String
	 * @param privkey IMyPrivateKey
	 * @param cert X509CertificateStructure
	 * @deprecated this constructor is not safe, as the certificate could contain a
	 *             public key that is incompatible to the private key;
	 *             scheduled for removal on 04/12/13
	 */
	public PKCS12(String al, IMyPrivateKey privkey, X509CertificateStructure cert)
	{
		m_ownerAlias = al;
		m_keyPair = new AsymmetricCryptoKeyPair(privkey);
		m_x509certificate = JAPCertificate.getInstance(cert);
	}

	/**
	 * Creates a new PKCS12 certificate.
	 * @param a_ownerAlias The owner of the certificate. The name is set as the common name (CN).
	 * @param a_keyPair a key pair with a private and a public key
	 * @param a_validFrom The date from which the certificate is valid.
	 * @param a_validTo The date until which the certificate is valid.
	 */
	public PKCS12(String a_ownerAlias, AsymmetricCryptoKeyPair a_keyPair,
				  Calendar a_validFrom, Calendar a_validTo)
	{
		m_ownerAlias = a_ownerAlias;
		m_keyPair = a_keyPair;
		m_x509certificate =
			JAPCertificate.getInstance(a_ownerAlias, a_keyPair, a_validFrom, a_validTo);
	}

	/**
	 * Creates a new PKCS12 certificate.
	 * @param a_ownerAlias The owner of the certificate. The name is set as the common name (CN).
	 * @param a_keyPair a key pair with a private and a public key
	 * @param a_validFrom The date from which the certificate is valid.
	 * @param a_validityInYears the number of years the certificate is valid;
	 *                          if the amount is 0 or less, the validity will
	 *                          only be ahead from a_validFrom for a very small amount of time
	 *                          (some minutes)
	 */
	public PKCS12(String a_ownerAlias, AsymmetricCryptoKeyPair a_keyPair,
				  Calendar a_validFrom, int a_validityInYears)
	{
		this(a_ownerAlias, a_keyPair, a_validFrom,
			 JAPCertificate.createValidTo(a_validFrom, a_validityInYears));
	}

	/**
	 * Creates a new PKCS12 certificate. This constructor is not public as is is possible
	 * that the X509 certificate owner alias and the PKCS12 owner alias differ, what they
	 * should not!
	 * @param a_ownerAlias The owner of the certificate. The name is set as the common name (CN).
	 * @param a_keyPair a key pair with a private and a public key
	 * @param a_X509certificate an X509 certificate
	 */
	private PKCS12(String a_ownerAlias, AsymmetricCryptoKeyPair a_keyPair,
				   JAPCertificate a_X509certificate)
	{
		m_ownerAlias = a_ownerAlias;
		m_keyPair = a_keyPair;
		m_x509certificate = a_X509certificate;
	}


	/**
	 * Loads a PKCS12 certificate from a byte array. The type of the encryption
	 * algorithm is recognized dynamically.
	 * @param a_bytes a byte array
	 * @param a_password a password
	 * @see anon.crypto.IMyPrivateKey
	 * @see anon.util.ClassUtil#loadClasses()
	 * @see anon.crypto.AsymmetricKeyPair
	 * @return a PKCS12 certificate or null if an error occured
	 */
	public static PKCS12 getInstance(byte[] a_bytes, char[] a_password)
	{
		if (a_bytes == null)
		{
			return null;
		}

		return getInstance(new ByteArrayInputStream(a_bytes), a_password);
	}

	/**
	 * Loads a PKCS12 certificate from an input stream. The type of the encryption
	 * algorithm is recognized dynamically.
	 * @param stream InputStream
	 * @param password char[]
	 * @see anon.crypto.IMyPrivateKey
	 * @see anon.util.ClassUtil#loadClasses()
	 * @see anon.crypto.AsymmetricKeyPair
	 * @return PKCS12
	 * @deprecated use {@link getInstance(InputStream, char[])} instead;
	 *             scheduled for deletion on 04/12/16
	 */
	public static PKCS12 load(InputStream stream, char[] password)
	{
		return getInstance(stream, password);
	}

	/**
	 * Loads a PKCS12 certificate from an input stream. The type of the encryption
	 * algorithm is recognized dynamically.
	 * @param a_stream InputStream
	 * @param password char[]
	 * @see anon.crypto.IMyPrivateKey
	 * @see anon.util.ClassUtil#loadClasses()
	 * @see anon.crypto.AsymmetricKeyPair
	 * @return PKCS12
	 */
	public static PKCS12 getInstance(InputStream a_stream, char[] password)
	{
		try
		{
			BufferedInputStream stream = new BufferedInputStream(a_stream);

			stream.mark(1);
			if (stream.read() != (DERTags.SEQUENCE | DERTags.CONSTRUCTED))
			{
				// this is no a valid PKCS12 stream
				return null;
			}
			stream.reset();

			String alias = null;
			IMyPrivateKey privKey = null;
			X509CertificateStructure x509cert = null;

			ASN1InputStream is = new ASN1InputStream(stream);
			ContentInfo contentInfo = new Pfx((ASN1Sequence) is.readObject()).getAuthSafe();
			/** @todo Check MAC */
			// Look at JDKPKCS12KeyStore.engineLoad (starting with bag.getMacData())

			if (!contentInfo.getContentType().equals(PKCSObjectIdentifiers.data))
			{
				return null;
			}

			is = new ASN1InputStream(new ByteArrayInputStream( ( (DEROctetString) contentInfo.getContent()).
				getOctets()));
			ContentInfo[] cinfos = (new AuthenticatedSafe( (ASN1Sequence) is.readObject())).getContentInfo();

			for (int i = 0; i < cinfos.length; i++)
			{
				ASN1Sequence cseq;
				if (cinfos[i].getContentType().equals(PKCSObjectIdentifiers.data))
				{
					ASN1InputStream dis = new ASN1InputStream(new ByteArrayInputStream(
						( (DEROctetString) cinfos[i].getContent())
						.getOctets()));
					cseq = (ASN1Sequence) dis.readObject();
				}
				else if (cinfos[i].getContentType().equals(PKCSObjectIdentifiers.encryptedData))
				{
					EncryptedData ed = new EncryptedData( (ASN1Sequence) cinfos[i].getContent());
					String algId = ed.getEncryptionAlgorithm().getObjectId().getId();
					MyCipher cipher = getCipher(algId);
					if (cipher == null)
					{
						return null;
					}
					PKCS12PBEParams pbeParams = new PKCS12PBEParams( (ASN1Sequence) ed.getEncryptionAlgorithm()
						.getParameters());
					ASN1InputStream bis = new ASN1InputStream(new ByteArrayInputStream(
						PKCS12.codeData(false, ed.getContent().getOctets(), pbeParams, password,
										cipher.cipher, cipher.keysize)));
					cseq = (ASN1Sequence) bis.readObject();
				}
				else
				{
					continue;
				}

				for (int j = 0; j < cseq.size(); j++)
				{
					SafeBag sb = new SafeBag( (ASN1Sequence) cseq.getObjectAt(j));

					if (sb.getBagId().equals(PKCSObjectIdentifiers.certBag))
					{
						ASN1InputStream cin = new ASN1InputStream(
							new ByteArrayInputStream( ( (DEROctetString)new CertBag( (ASN1Sequence)
							sb.getBagValue()).getCertValue()).getOctets()));

						ASN1Sequence xseq = (ASN1Sequence) cin.readObject();
						if (xseq.size() > 1
							&& xseq.getObjectAt(1) instanceof DERObjectIdentifier
							&& xseq.getObjectAt(0).equals(PKCSObjectIdentifiers.signedData))
						{
							x509cert = X509CertificateStructure.getInstance(
								new SignedData(
								ASN1Sequence.getInstance(
								(ASN1TaggedObject) xseq.getObjectAt(1),
								true))
								.getCertificates()
								.getObjectAt(0));
						}
						else
						{
							x509cert = X509CertificateStructure.getInstance(xseq);
						}
					}
					else if (sb.getBagId().equals(PKCSObjectIdentifiers.pkcs8ShroudedKeyBag))
					{
						EncryptedPrivateKeyInfo ePrivKey = new EncryptedPrivateKeyInfo( (ASN1Sequence) sb.
							getBagValue());
						MyCipher cipher = getCipher(ePrivKey.getEncryptionAlgorithm().getObjectId().getId());
						if (cipher == null)
						{
							return null;
						}
						PKCS12PBEParams pbeParams = new PKCS12PBEParams( (ASN1Sequence) ePrivKey
							.getEncryptionAlgorithm().getParameters());
						byte[] pkData = PKCS12.codeData(false, ePrivKey.getEncryptedData(),
							pbeParams, password, cipher.cipher, cipher.keysize);
						ByteArrayInputStream bIn = new ByteArrayInputStream(pkData);
						ASN1InputStream dIn = new ASN1InputStream(bIn);
						PrivateKeyInfo privKeyInfo = new PrivateKeyInfo( (ASN1Sequence) dIn.readObject());
						try
						{
							privKey = (new AsymmetricCryptoKeyPair(privKeyInfo)).getPrivate();
						}
						catch (Exception a_e)
						{
							return null;
						}
					}

					if (alias == null && sb.getBagAttributes() != null)
					{
						Enumeration e = sb.getBagAttributes().getObjects();
						while (e.hasMoreElements())
						{
							ASN1Sequence ba = (ASN1Sequence) e.nextElement();
							DERObjectIdentifier oid = (DERObjectIdentifier) ba.getObjectAt(0);
							DERObject att = (DERObject) ( (ASN1Set) ba.getObjectAt(1)).getObjectAt(0);
							if (oid.equals(pkcs_9_at_friendlyName))
							{
								alias = ( (DERBMPString) att).getString();
							}
						}
					}
				}
			}
			if (alias != null && x509cert != null) //????
			{
				return new PKCS12(alias, new AsymmetricCryptoKeyPair(privKey),
								  JAPCertificate.getInstance(x509cert));
			}
		}
		catch (Throwable t)
		{
		}
		return null;
	}


	/**
	 * Converts the certificate to a byte array.
	 * @return the certificate as a byte array
	 */
	public byte[] toByteArray()
	{
		return toByteArray("".toCharArray());
	}

	/**
	 * Converts the certificate to a (optionally encrypted) byte array.
	 * @param a_password a password
	 * @throws IOException
	 * @return the certificate as a byte array
	 */
	public byte[] toByteArray(char[] a_password)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try
		{
			store(out, a_password);
			out.close();
		}
		catch (IOException a_e)
		{
			// I don`t think this is possible
		}

		return out.toByteArray();
	}


	public void store(OutputStream stream, char[] password) throws IOException
	{
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		DEROutputStream dOut;

		//
		// handle the key
		//
		BERConstructedOctetString keyString;
		{

			byte[] kSalt = new byte[SALT_SIZE];
			random.nextBytes(kSalt);
			PKCS12PBEParams kParams = new PKCS12PBEParams(kSalt, MIN_ITERATIONS);
			byte[] kBytes = codeData(true,
									 m_keyPair.getPrivate().getEncoded(),
									 kParams,
									 password,
									 new DESedeEngine(),
									 192);
			AlgorithmIdentifier kAlgId = new AlgorithmIdentifier(
				new DERObjectIdentifier(KEY_ALGORITHM),
				kParams.getDERObject());
			EncryptedPrivateKeyInfo kInfo = new EncryptedPrivateKeyInfo(kAlgId, kBytes);

			//
			// set a default friendly name (from the key id) and local id
			//
			DERSequence[] seqs=new DERSequence[2];
			DEREncodableVector kSeq = new DEREncodableVector();
			kSeq.add(pkcs_9_at_localKeyId);
			kSeq.add(new DERSet(createSubjectKeyId()));
			seqs[0]=new DERSequence(kSeq);
			//kName.addObject(new DERSequence(kSeq));
			kSeq = new DEREncodableVector();
			kSeq.add(pkcs_9_at_friendlyName);
			kSeq.add(new DERSet(new DERBMPString(m_ownerAlias)));
			seqs[1]=new DERSequence(kSeq);
			DERSet kName = new DERSet(seqs);
			//kName.addObject(new DERSequence(kSeq));

			keyString = new BERConstructedOctetString(new DERSequence(
				new SafeBag(pkcs8ShroudedKeyBag, kInfo.getDERObject(), kName)));
		}

		//
		// certficate processing
		//
		EncryptedData cInfo;
		{
			byte[] cSalt = new byte[SALT_SIZE];
			random.nextBytes(cSalt);
			PKCS12PBEParams cParams = new PKCS12PBEParams(cSalt, MIN_ITERATIONS);
			AlgorithmIdentifier cAlgId = new AlgorithmIdentifier(new DERObjectIdentifier(CERT_ALGORITHM),
				cParams);
			CertBag cBag = new CertBag(x509certType, new DEROctetString(m_x509certificate));
			DEREncodableVector fSeq = new DEREncodableVector();
			fSeq.add(pkcs_9_at_localKeyId);
			fSeq.add(new DERSet(createSubjectKeyId()));
			DERSequence[] seqs=new DERSequence[2];
			seqs[0]=new DERSequence(fSeq);

			fSeq = new DEREncodableVector();
			fSeq.add(pkcs_9_at_friendlyName);
			fSeq.add(new DERSet(new DERBMPString(m_ownerAlias)));
			seqs[1]=new DERSequence(fSeq);
			DERSet fName = new DERSet(seqs);

			SafeBag sBag = new SafeBag(certBag, cBag.getDERObject(), fName);

			bOut.reset();
			dOut = new DEROutputStream(bOut);
			dOut.writeObject(new DERSequence(sBag));
			dOut.close();

			byte[] certBytes = codeData(true, bOut.toByteArray(), cParams, password, new RC2Engine(), 40);
			cInfo = new EncryptedData(data, cAlgId, new BERConstructedOctetString(certBytes));
		}

		ContentInfo[] c = new ContentInfo[2];
		c[0] = new ContentInfo(data, keyString);
		c[1] = new ContentInfo(encryptedData, cInfo);

		/*
		 bOut.reset();
		 AuthenticatedSafe auth = new AuthenticatedSafe(c);
		 BEROutputStream berOut = new BEROutputStream(bOut);
		 berOut.writeObject(auth);
		 byte[] pkg = bOut.toByteArray();

		 ContentInfo mainInfo =
		   new ContentInfo(data, new BERConstructedOctetString(pkg));
		 */
		ContentInfo mainInfo = new ContentInfo(data, new BERConstructedOctetString(new AuthenticatedSafe(c)));
		//
		// create the mac
		//
		byte[] mSalt = new byte[SALT_SIZE];
		int itCount = MIN_ITERATIONS;
		random.nextBytes(mSalt);
		byte[] contentData = ( (DEROctetString) mainInfo.getContent()).getOctets();
		MacData mData = null;

		try
		{
			Mac certMac = new HMac(new SHA1Digest());
			CipherParameters cParam = makePBEMacParameters(password, new PKCS12PBEParams(mSalt, itCount), 160);
			certMac.init(cParam);
			certMac.update(contentData, 0, contentData.length);
			byte[] my_res = new byte[certMac.getMacSize()];
			certMac.doFinal(my_res, 0);
			AlgorithmIdentifier my_algId = new AlgorithmIdentifier(id_SHA1, null);
			DigestInfo my_dInfo = new DigestInfo(my_algId, my_res);

			mData = new MacData(my_dInfo, mSalt, itCount);
		}
		catch (Exception e)
		{
			throw new IOException("error constructing MAC: " + e.toString());
		}

		//
		// output the Pfx
		//
		Pfx pfx = new Pfx(mainInfo, mData);
		BEROutputStream berOut = new BEROutputStream(stream);
		berOut.writeObject(pfx);
	}

	public String getAlias()
	{
		return m_ownerAlias;
	}


	/**
	 *
	 * @return IMyPrivateKey
	 * @deprecated use {@link getPrivateKey()} instead; scheduled for removal on 04/12/13
	 */
	public IMyPrivateKey getPrivKey()
	{
		return m_keyPair.getPrivate();
	}

	/**
	 * Returns the private key of this certificate.
	 * @return the private key of this certificate
	 */
	public IMyPrivateKey getPrivateKey()
	{
		return m_keyPair.getPrivate();
	}

	/**
	 * Returns the public key of this certificate.
	 * @return the public key of this certificate
	 */
	public IMyPublicKey getPublicKey()
	{
		return m_keyPair.getPublic();
	}

	/**
	 * Returns the key pair of this certificate.
	 * @return the key pair of this certificate
	 */
	public AsymmetricCryptoKeyPair getKeyPair()
	{
		return m_keyPair;
	}

	/**
	 * @deprecated use {@link getX509Certificate()} instead;
	 *             scheduled for removal on 04/12/13
	 * @return the X509 certificate corresponding to this PKCS12 certificate
	 */
	public X509CertificateStructure getX509cert()
	{
		return m_x509certificate;
	}

	/**
	 * Returns the X509 certificate corresponding to this PKCS12 certificate.
	 * The certificate is enabled by default
	 * @return the X509 certificate corresponding to this PKCS12 certificate
	 */
	public JAPCertificate getX509Certificate()
	{
		return m_x509certificate;
	}

	/**
	 * Replaces the current X509 certificate by a clone of the given certificate if the given
	 * certificate has the same public key as the current certificate.
	 * @param a_X509certificate JAPCertificate
	 * @return true if the current X509 certificate has been replaced; false otherwise
	 */
	public boolean setX509Certificate(JAPCertificate a_X509certificate)
	{
		if (a_X509certificate != null &&
			m_x509certificate.getPublicKey().equals(a_X509certificate.getPublicKey()))
		{
			m_x509certificate = (JAPCertificate)a_X509certificate.clone();
			return true;
		}

		return false;
	}

	/**
	 * Signs the coresponding X509 certificate with an other pkcs12 certificate.
	 * Any previous signature is removed. If the signature was self-signed before, it is no
	 * more (but it can still be verified by this PKCS12 certificate via the public key).
	 * @param a_pkcs12Certificate a PKCS12 certificate
	 */
	public void sign(PKCS12 a_pkcs12Certificate)
	{
		m_x509certificate = m_x509certificate.sign(a_pkcs12Certificate);
	}

	private static byte[] codeData(
		boolean encrypt,
		byte[] data,
		PKCS12PBEParams pbeParams,
		char[] password,
		BlockCipher cipher,
		int keySize) throws IOException
	{
		byte[] my_out;

		try
		{
			BufferedBlockCipher my_cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(cipher));
			CipherParameters my_param = makePBEParameters(password,
				pbeParams,
				my_cipher.getUnderlyingCipher().getAlgorithmName(),
				keySize,
				64);

			my_param = new ParametersWithRandom(my_param, new SecureRandom());
			my_cipher.init(encrypt, my_param);

			byte[] my_input = data;
			int my_inputlen = my_input.length;
			int my_len = 0;
			byte[] my_tmp = new byte[my_cipher.getOutputSize(my_inputlen)];

			if (my_inputlen != 0)
			{
				my_len = my_cipher.processBytes(my_input, 0, my_inputlen, my_tmp, 0);

			}
			try
			{
				my_len += my_cipher.doFinal(my_tmp, my_len);
			}
			catch (Exception e)
			{
			}

			my_out = new byte[my_len];
			System.arraycopy(my_tmp, 0, my_out, 0, my_len);
		}
		catch (Exception e)
		{
			throw new IOException(
				"exception encrypting data - " + e.toString());
		}

		return my_out;
	}

	private static CipherParameters makePBEMacParameters(char[] password, PKCS12PBEParams pbeParams,
		int keySize)
	{
		PBEParametersGenerator generator = makePBEGenerator();
		byte[] key = PBEParametersGenerator.PKCS12PasswordToBytes(password);
		CipherParameters param;

		generator.init(key, pbeParams.getIV(), pbeParams.getIterations().intValue());

		param = generator.generateDerivedMacParameters(keySize);

		for (int i = 0; i != key.length; i++)
		{
			key[i] = 0;
		}

		return param;
	}

	private static CipherParameters makePBEParameters(char[] password, PKCS12PBEParams pbeParams,
		String targetAlgorithm, int keySize, int ivSize)
	{
		PBEParametersGenerator generator = makePBEGenerator();
		byte[] key = PBEParametersGenerator.PKCS12PasswordToBytes(password);
		CipherParameters param;

		generator.init(key, pbeParams.getIV(), pbeParams.getIterations().intValue());

		if (ivSize != 0)
		{
			param = generator.generateDerivedParameters(keySize, ivSize);
		}
		else
		{
			param = generator.generateDerivedParameters(keySize);
		}

		if (targetAlgorithm.startsWith("DES"))
		{
			if (param instanceof ParametersWithIV)
			{
				KeyParameter kParam = (KeyParameter) ( (ParametersWithIV) param).getParameters();
				DESParameters.setOddParity(kParam.getKey());
			}
			else
			{
				KeyParameter kParam = (KeyParameter) param;
				DESParameters.setOddParity(kParam.getKey());
			}
		}

		for (int i = 0; i != key.length; i++)
		{
			key[i] = 0;
		}

		return param;
	}


	private static PBEParametersGenerator makePBEGenerator()
	{
		return new PKCS12ParametersGenerator(new SHA1Digest());
	}


	private static MyCipher getCipher(String algId)
	{
		if (algId.equals("1.2.840.113549.1.12.1.3"))
		{

			// PBE with SHA and 3-Key TripleDES-CBC
			return new MyCipher(new DESedeEngine(), 192);
		}
		else if (algId.equals("1.2.840.113549.1.12.1.4"))
		{

			// PBE with SHA and 2-Key TripleDES-CBC
			return new MyCipher(new DESedeEngine(), 128);
		}
		else if (algId.equals("1.2.840.113549.1.12.1.5"))
		{

			// PBE with SHA and 128 Bit-RC2-CBC
			return new MyCipher(new RC2Engine(), 128);
		}
		else if (algId.equals("1.2.840.113549.1.12.1.6"))
		{

			// PBE with SHA and 40 Bit-RC2-CBC
			return new MyCipher(new RC2Engine(), 40);
		}
		else
		{
			return null;
		}
	}

	private static class MyCipher
	{
		public BlockCipher cipher;
		public int keysize;
		MyCipher(BlockCipher c, int ks)
		{
			cipher = c;
			keysize = ks;
		}
	};


	private SubjectKeyIdentifier createSubjectKeyId()
	{
		try
		{
			return new SubjectKeyIdentifier(m_x509certificate.getSubjectPublicKeyInfo());
			/*			ByteArrayInputStream bIn =
			 new ByteArrayInputStream(pubKey.getEncoded());
			   SubjectPublicKeyInfo info =
			 new SubjectPublicKeyInfo(
			 (ASN1Sequence)new DERInputStream(bIn).readObject());
			   return new SubjectKeyIdentifier(info);*/
		}
		catch (Exception e)
		{
			throw new RuntimeException("error creating key");
		}
	}
}