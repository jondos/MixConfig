package mixconfig;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

import java.security.Key;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.KeyStoreException;

//Needed
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERInputStream;
import org.bouncycastle.asn1.BERConstructedOctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERConstructedSequence;
import org.bouncycastle.asn1.DERConstructedSet;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.BEROutputStream;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.EncryptedData;
import org.bouncycastle.asn1.pkcs.Pfx;
import org.bouncycastle.asn1.pkcs.MacData;
import org.bouncycastle.asn1.pkcs.AuthenticatedSafe;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.CertBag;
import org.bouncycastle.asn1.pkcs.SafeBag;
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateEncodingException;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKey;
import org.bouncycastle.jce.provider.JCEBlockCipher;
import javax.crypto.CipherSpi;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.jce.provider.JCEPBEKey;
import org.bouncycastle.jce.provider.PBE;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.jce.provider.JCESecretKeyFactory;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.engines.RC2Engine;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.DESParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;



public class JDKPKCS12KeyStore
    //extends KeyStoreSpi
    implements PKCSObjectIdentifiers, X509ObjectIdentifiers/*, BCKeyStore*/
{
    private static final int    STORE_VERSION = 1;

    private static final int    SALT_SIZE = 20;
    private static final int    MIN_ITERATIONS = 100;

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
    private static final String MAC_ALGORITHM = "1.3.14.3.2.26";

    private Hashtable                       keys = new Hashtable();
    private Hashtable                       localIds = new Hashtable();
    private Hashtable                       certs = new Hashtable();
    private Hashtable                       chainCerts = new Hashtable();
    private Hashtable                       keyCerts = new Hashtable();

    //
    // generic object types
    //
    static final int NULL           = 0;
    static final int CERTIFICATE    = 1;
    static final int KEY            = 2;
    static final int SECRET         = 3;
    static final int SEALED         = 4;

    //
    // key types
    //
    static final int    KEY_PRIVATE = 0;
    static final int    KEY_PUBLIC  = 1;
    static final int    KEY_SECRET  = 2;

    protected SecureRandom      random = new SecureRandom();

    private CertificateFactory  certFact = null;

    private class CertId
    {
        byte[]  id;

        CertId(
            PublicKey  key)
        {
            this.id = createSubjectKeyId(key).getKeyIdentifier();
        }

        CertId(
            byte[]  id)
        {
            this.id = id;
        }

        public int hashCode()
        {
            int hash = id[0] & 0xff;

            for (int i = 1; i != id.length - 4; i++)
            {
                hash ^= ((id[i] & 0xff) << 24) | ((id[i + 1] & 0xff) << 16)
                          | ((id[i + 2] & 0xff) << 8) | (id[i + 3] & 0xff);
            }

            return hash;
        }

        public boolean equals(
            Object  o)
        {
            if (!(o instanceof CertId))
            {
                return false;
            }

            CertId  cId = (CertId)o;

            if (cId.id.length != id.length)
            {
                return false;
            }

            for (int i = 0; i != id.length; i++)
            {
                if (cId.id[i] != id[i])
                {
                    return false;
                }
            }

            return true;
        }
    }

    public JDKPKCS12KeyStore(
        String provider)
    {
        try
        {
           /* if (provider != null)
            {
                certFact = CertificateFactory.getInstance("X.509", provider);
            }
            else
            {
                certFact = CertificateFactory.getInstance("X.509");
            }*/
            certFact=null;
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("can't create cert factory - " + e.toString());
        }
    }

    private SubjectKeyIdentifier createSubjectKeyId(
        PublicKey   pubKey)
    {
        try
        {
            ByteArrayInputStream    bIn = new ByteArrayInputStream(
                                                    pubKey.getEncoded());
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
                (DERConstructedSequence)new DERInputStream(bIn).readObject());

            return new SubjectKeyIdentifier(info);
        }
        catch (Exception e)
        {
            throw new RuntimeException("error creating key");
        }
    }

    public void setRandom(
        SecureRandom    rand)
    {
        this.random = rand;
    }

    public Enumeration engineAliases()
    {
        Hashtable  tab = new Hashtable();

        Enumeration e = certs.keys();
        while (e.hasMoreElements())
        {
            tab.put(e.nextElement(), "cert");
        }

        e = keys.keys();
        while (e.hasMoreElements())
        {
            String  a = (String)e.nextElement();
            if (tab.get(a) == null)
            {
                tab.put(a, "key");
            }
        }

        return tab.keys();
    }

    public boolean engineContainsAlias(
        String  alias)
    {
        return (certs.get(alias) != null || keys.get(alias) != null);
    }

    /**
     * this is quite complete - we should follow up on the chain, a bit
     * tricky if a certificate appears in more than one chain...
     */
  /*  public void engineDeleteEntry(
        String  alias)
        throws KeyStoreException
    {
        Key k = (Key)keys.remove(alias);

        Certificate c = (Certificate)certs.remove(alias);

        if (c != null)
        {
            chainCerts.remove(new CertId(c.getPublicKey()));
        }

        if (k != null)
        {
            String  id = (String)localIds.remove(alias);
            if (id != null)
            {
                c = (Certificate)keyCerts.remove(id);
            }
            if (c != null)
            {
                chainCerts.remove(new CertId(c.getPublicKey()));
            }
        }

        if (c == null && k == null)
        {
            throw new KeyStoreException("no such entry as " + alias);
        }
    }*/

    /**
     * simply return the cert for the private key
     */
    public Certificate engineGetCertificate(
        String alias)
    {
        Certificate c = (Certificate)certs.get(alias);

        //
        // look up the key table - and try the local key id
        //
        if (c == null)
        {
            String  id = (String)localIds.get(alias);
            if (id != null)
            {
                c = (Certificate)keyCerts.get(id);
            }
        }

        return c;
    }
/*
    public String engineGetCertificateAlias(
        Certificate cert)
    {
        Enumeration c = certs.elements();
        Enumeration k = certs.keys();

        while (c.hasMoreElements())
        {
            Certificate tc = (Certificate)c.nextElement();
            String      ta = (String)k.nextElement();

            if (tc.equals(cert))
            {
                return ta;
            }
        }

        return null;
    }
*/
/*    public Certificate[] engineGetCertificateChain(
        String alias)
    {
        Certificate c = engineGetCertificate(alias);

        if (c != null)
        {
            Vector  cs = new Vector();

            while (c != null)
            {
                X509Certificate     x509c = (X509Certificate)c;
                Certificate         nextC = null;

                byte[]  bytes = x509c.getExtensionValue(X509Extensions.AuthorityKeyIdentifier.getId());
                if (bytes != null)
                {
                    try
                    {
                        ByteArrayInputStream    bIn = new ByteArrayInputStream(bytes);
                        DERInputStream          dIn = new DERInputStream(bIn);


                        bIn = new ByteArrayInputStream(((DEROctetString)dIn.readObject()).getOctets());
                        dIn = new DERInputStream(bIn);

                        AuthorityKeyIdentifier id = new AuthorityKeyIdentifier((DERConstructedSequence)dIn.readObject());
                        if (id.getKeyIdentifier() != null)
                        {
                            nextC = (Certificate)chainCerts.get(new CertId(id.getKeyIdentifier()));
                        }

                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e.toString());
                    }
                }

                if (nextC == null)
                {
                    //
                    // no authority key id, try the Issuer DN
                    //
                    Principal  i = x509c.getIssuerDN();
                    Principal  s = x509c.getSubjectDN();

                    if (!i.equals(s))
                    {
                        Enumeration e = chainCerts.keys();

                        while (e.hasMoreElements())
                        {
                            X509Certificate crt = (X509Certificate)chainCerts.get(e.nextElement());
                            Principal  sub = crt.getSubjectDN();
                            if (sub.equals(i))
                            {
                                try
                                {
                                    x509c.verify(crt.getPublicKey());
                                    nextC = crt;
                                    break;
                                }
                                catch (Exception ex)
                                {
                                    // continue
                                }
                            }
                        }
                    }
                }

                //
                // if c == nextC and cs.size == 0 we have a chain of depth
                // one - just add c and we'll finish...
                //
                if (nextC != c)
                {
                    cs.addElement(c);
                    c = nextC;
                }
                else if (cs.size() == 0)
                {
                    cs.addElement(c);
                    c = null;
                }
                else
                {
                    c = null;
                }
            }

            Certificate[]   certChain = new Certificate[cs.size()];

            for (int i = 0; i != certChain.length; i++)
            {
                certChain[i] = (Certificate)cs.elementAt(i);
            }

            return certChain;
        }

        return null;
    }
*/
  /*  public Date engineGetCreationDate(String alias)
    {
        return new Date();
    }
*/
 /*   public Key engineGetKey(
        String alias,
        char[] password)
        throws NoSuchAlgorithmException, UnrecoverableKeyException
    {
        return (Key)keys.get(alias);
    }
*/
  /*  public boolean engineIsCertificateEntry(
        String alias)
    {
        return (certs.get(alias) != null && keys.get(alias) == null);
    }

    public boolean engineIsKeyEntry(
        String alias)
    {
        return (keys.get(alias) != null);
    }*/

   /* public void engineSetCertificateEntry(
        String      alias,
        Certificate cert)
        throws KeyStoreException
    {
        if (certs.get(alias) != null)
        {
            throw new KeyStoreException("There is already a certificate with the name " + alias + ".");
        }

        certs.put(alias, cert);
        chainCerts.put(new CertId(cert.getPublicKey()), cert);
    }
*/
  /*  public void engineSetKeyEntry(
        String alias,
        byte[] key,
        Certificate[] chain)
        throws KeyStoreException
    {
        throw new RuntimeException("operation not supported");
    }*/

    public void engineSetKeyEntry(
        String          alias,
        Key             key,
        char[]          password,
        Certificate[]   chain,
        PublicKey       pubKey
        )
        throws KeyStoreException
    {
        if ((key instanceof PrivateKey) && (chain == null))
        {
            throw new KeyStoreException("no certificate chain for private key");
        }

        if (keys.get(alias) != null && !key.equals((Key)keys.get(alias)))
        {
            throw new KeyStoreException("There is already a key with the name " + alias + ".");
        }

        keys.put(alias, key);
        certs.put(alias, chain[0]);

        for (int i = 0; i != chain.length; i++)
        {
            chainCerts.put(new CertId(pubKey), chain[i]);
        }
    }

  /*  public int engineSize()
    {
        Hashtable  tab = new Hashtable();

        Enumeration e = certs.keys();
        while (e.hasMoreElements())
        {
            tab.put(e.nextElement(), "cert");
        }

        e = keys.keys();
        while (e.hasMoreElements())
        {
            String  a = (String)e.nextElement();
            if (tab.get(a) == null)
            {
                tab.put(a, "key");
            }
        }

        return tab.size();
    }*/

 /*   protected PrivateKey unwrapKey(
        AlgorithmIdentifier   algId,
        byte[]                data,
        char[]                password)
        throws IOException
    {
        String              algorithm = algId.getObjectId().getId();
        PKCS12PBEParams     pbeParams = new PKCS12PBEParams((DERConstructedSequence)algId.getParameters());

        PBEKeySpec          pbeSpec = new PBEKeySpec(password);
        PrivateKey          out = null;

        try
        {
            SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(
                                                algorithm, "BC");
            PBEParameterSpec    defParams = new PBEParameterSpec(
                                                pbeParams.getIV(),
                                                pbeParams.getIterations().intValue());

            Cipher cipher = Cipher.getInstance(algorithm, "BC");

            cipher.init(Cipher.UNWRAP_MODE, keyFact.generateSecret(pbeSpec), defParams);

            // we pass "" as the key algorithm type as it is unknown at this point
            out = (PrivateKey)cipher.unwrap(data, "", Cipher.PRIVATE_KEY);
        }
        catch (Exception e)
        {
e.printStackTrace();
            throw new IOException("exception unwrapping private key - " + e.toString());
        }

		return out;
    }
*/
    protected byte[] wrapKey(
        String                  algorithm,
        Key                     key,
        PKCS12PBEParams         pbeParams,
        char[]                  password)
        throws IOException
    {
        PBEKeySpec          pbeSpec = new PBEKeySpec(password);
        byte[]              my_out;

        try
        {
      /*    System.out.println("Algorithm: "+algorithm);
           //Original
            SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(
                                                algorithm);
            PBEParameterSpec    defParams = new PBEParameterSpec(
                                                pbeParams.getIV(),
                                                pbeParams.getIterations().intValue());

            Cipher cipher = Cipher.getInstance(algorithm);

            cipher.init(Cipher.WRAP_MODE, keyFact.generateSecret(pbeSpec), defParams,new SecureRandom());

            out = cipher.wrap(key);*/

//My
   //         SecretKeyFactory    my_keyFact = SecretKeyFactory.getInstance(
     //                                           algorithm);
            PBEParameterSpec    my_defParams = new PBEParameterSpec(
                                                pbeParams.getIV(),
                                                pbeParams.getIterations().intValue());

             My_PBEKey my_key=new My_PBEKey(true, "PBE/PKCS12", pbeSpec.getPassword());//(JCEPBEKey)keyFact.generateSecret(my_pbeSpec);

            BufferedBlockCipher my_cipher =
            new PaddedBufferedBlockCipher(new CBCBlockCipher(new DESedeEngine()));
            //, PKCS12, SHA1, 192, 64);
           // my_cipher.init(Cipher.WRAP_MODE, my_keyFact.generateSecret(pbeSpec), my_defParams,new SecureRandom());

            //JCEPBEKey my_key=(JCEPBEKey)my_keyFact.generateSecret(pbeSpec);
            CipherParameters        my_param;

        //
        // a note on iv's - if ivLength is zero the IV gets ignored (we don't use it).
        //
        ParametersWithIV my_ivParam;
        //if (my_key instanceof JCEPBEKey)
        {
            my_param = makePBEParameters((SecretKey)my_key, my_defParams, PBE.PKCS12, PBE.SHA1, /*pbeType, pbeHash,*/
                        my_cipher.getUnderlyingCipher().getAlgorithmName(),192,64 /*pbeKeySize, pbeIvSize*/);

            my_ivParam = (ParametersWithIV)my_param;

        }

        my_param = new ParametersWithRandom(my_param, new SecureRandom());
        my_cipher.init(true, my_param);




           // byte[] my_out = cipher.wrap(key);
        byte[] my_input=key.getEncoded();
        int my_inputlen=my_input.length;
        int     my_len = 0;
        byte[]  my_tmp = new byte[my_cipher.getOutputSize(my_inputlen)];

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

        //    System.out.println("Compare Warpa:");
         //   for(int i=0;i<out.length;i++)
          //    System.out.println(out[i]+" :"+my_out[i]);
        }
        catch (Exception e)
        {
            throw new IOException("exception encrypting data - " + e.toString());
        }

        return my_out;
	}

 /*   protected DERConstructedSequence decryptData(
        AlgorithmIdentifier   algId,
        byte[]                data,
        char[]                password)
        throws IOException
    {
        String              algorithm = algId.getObjectId().getId();
        PKCS12PBEParams     pbeParams = new PKCS12PBEParams((DERConstructedSequence)algId.getParameters());

        PBEKeySpec          pbeSpec = new PBEKeySpec(password);
        byte[]              out = null;

        try
        {
            SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(
                                                algorithm, "BC");
            PBEParameterSpec    defParams = new PBEParameterSpec(
                                                pbeParams.getIV(),
                                                pbeParams.getIterations().intValue());

            Cipher cipher = Cipher.getInstance(algorithm, "BC");

            cipher.init(Cipher.DECRYPT_MODE, keyFact.generateSecret(pbeSpec), defParams);

            out = cipher.doFinal(data);
        }
        catch (Exception e)
        {
            throw new IOException("exception decrypting data - " + e.toString());
        }

        BERInputStream  bIn = new BERInputStream(new ByteArrayInputStream(out));

        return (DERConstructedSequence)bIn.readObject();
    }
*/
    protected byte[] encryptData(
        String                  algorithm,
        byte[]                  data,
        PKCS12PBEParams         pbeParams,
        char[]                  password)
        throws IOException
    {
        PBEKeySpec          pbeSpec = new PBEKeySpec(password);
        byte[]              my_out;

        try
        {
            //Original
       /*     System.out.println("Algorithm: "+algorithm);
            SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(
                                                algorithm, "BC");
            PBEParameterSpec    defParams = new PBEParameterSpec(
                                                pbeParams.getIV(),
                                                pbeParams.getIterations().intValue());

            Cipher cipher = Cipher.getInstance(algorithm, "BC");

            cipher.init(Cipher.ENCRYPT_MODE, keyFact.generateSecret(pbeSpec), defParams);

            out = cipher.doFinal(data);*/
//My
        //    SecretKeyFactory    my_keyFact = SecretKeyFactory.getInstance(
          //                                      algorithm, "BC");
            PBEParameterSpec    my_defParams = new PBEParameterSpec(
                                                pbeParams.getIV(),
                                                pbeParams.getIterations().intValue());

            //Cipher my_cipher = Cipher.getInstance(algorithm, "BC");

            //my_cipher.init(Cipher.ENCRYPT_MODE, my_keyFact.generateSecret(pbeSpec), my_defParams);

            My_PBEKey my_key=new My_PBEKey(true, "PBE/PKCS12", pbeSpec.getPassword());//(JCEPBEKey)keyFact.generateSecret(my_pbeSpec);

            BufferedBlockCipher my_cipher =
            new PaddedBufferedBlockCipher(new CBCBlockCipher(new RC2Engine()));
            //, PKCS12, SHA1, 192, 64);
           // my_cipher.init(Cipher.WRAP_MODE, my_keyFact.generateSecret(pbeSpec), my_defParams,new SecureRandom());

            //JCEPBEKey my_key=(JCEPBEKey)my_keyFact.generateSecret(pbeSpec);
            CipherParameters        my_param;

        //
        // a note on iv's - if ivLength is zero the IV gets ignored (we don't use it).
        //
            ParametersWithIV my_ivParam;
            my_param = makePBEParameters((SecretKey)my_key, my_defParams, PBE.PKCS12, PBE.SHA1, /*pbeType, pbeHash,*/
                        my_cipher.getUnderlyingCipher().getAlgorithmName(),40,64 /*pbeKeySize, pbeIvSize*/);

            my_ivParam = (ParametersWithIV)my_param;


            my_param = new ParametersWithRandom(my_param, new SecureRandom());
            my_cipher.init(true, my_param);


            //byte[] my_out = my_cipher.doFinal(data);
            byte[] my_input=data;
            int my_inputlen=my_input.length;
            int     my_len = 0;
            byte[]  my_tmp = new byte[my_cipher.getOutputSize(my_inputlen)];

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

 //           System.out.println("Compare encode:");
 //           for(int i=0;i<out.length;i++)
 //             System.out.println(out[i]+" :"+my_out[i]);
          }
        catch (Exception e)
        {
            throw new IOException("exception encrypting data - " + e.toString());
        }

        return my_out;
    }

    /*public void engineLoad(
        InputStream stream,
        char[]      password)
        throws IOException
    {
        if (stream == null)     // just initialising
        {
            return;
        }

		if ( password == null )
		{
			throw new NullPointerException("No password supplied for PKCS#12 KeyStore.");
		}

        BufferedInputStream             bufIn = new BufferedInputStream(stream);

        bufIn.mark(10);

        int head = bufIn.read();

        if (head != 0x30)
        {
            throw new IOException("stream does not represent a PKCS12 key store");
        }

        bufIn.reset();

        BERInputStream  				bIn = new BERInputStream(bufIn);
        DERConstructedSequence          obj = (DERConstructedSequence)bIn.readObject();
		Pfx							    bag = new Pfx(obj);
        ContentInfo                     info = bag.getAuthSafe();
        Vector                          chain = new Vector();

        if (bag.getMacData() != null)           // check the mac code
        {
            ByteArrayOutputStream       bOut = new ByteArrayOutputStream();
            BEROutputStream             berOut = new BEROutputStream(bOut);
            MacData                     mData = bag.getMacData();
            DigestInfo                  dInfo = mData.getMac();
            AlgorithmIdentifier         algId = dInfo.getAlgorithmId();
            byte[]                      salt = mData.getSalt();
            int                         itCount = mData.getIterationCount().intValue();

            berOut.writeObject(info);

            byte[]  data = ((DEROctetString)info.getContent()).getOctets();

            try
            {
                Mac                 mac = Mac.getInstance(algId.getObjectId().getId(), "BC");
                SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(algId.getObjectId().getId(), "BC");
                PBEParameterSpec    defParams = new PBEParameterSpec(salt, itCount);
                PBEKeySpec          pbeSpec = new PBEKeySpec(password);

                mac.init(keyFact.generateSecret(pbeSpec), defParams);

                mac.update(data);

                byte[]  res = mac.doFinal();
                byte[]  dig = dInfo.getDigest();

                if (res.length != dInfo.getDigest().length)
                {
                    throw new IOException("PKCS12 key store mac invalid.");
                }

                for (int i = 0; i != res.length; i++)
                {
                    if (res[i] != dig[i])
                    {
                        throw new IOException("PKCS12 key store mac invalid.");
                    }
                }
            }
            catch (Exception e)
            {
                throw new IOException("error constructing MAC: " + e.toString());
            }
        }

        keys = new Hashtable();
        localIds = new Hashtable();

        if (info.getContentType().equals(data))
        {
            bIn = new BERInputStream(new ByteArrayInputStream(((DEROctetString)info.getContent()).getOctets()));

            AuthenticatedSafe   authSafe = new AuthenticatedSafe((DERConstructedSequence)bIn.readObject());
            ContentInfo[]       c = authSafe.getContentInfo();

            for (int i = 0; i != c.length; i++)
            {
                if (c[i].getContentType().equals(data))
                {
                    DERInputStream dIn = new DERInputStream(new ByteArrayInputStream(((DEROctetString)c[i].getContent()).getOctets()));
                    DERConstructedSequence seq = (DERConstructedSequence)dIn.readObject();

                    for (int j = 0; j != seq.getSize(); j++)
                    {
                        SafeBag b = new SafeBag((DERConstructedSequence)seq.getObjectAt(j));
                        if (b.getBagId().equals(pkcs8ShroudedKeyBag))
                        {
                            org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo eIn = new org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo((DERConstructedSequence)b.getBagValue());
                            PrivateKey              privKey = unwrapKey(eIn.getEncryptionAlgorithm(), eIn.getEncryptedData(), password);

                            //
                            // set the attributes on the key
                            //
                            PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)privKey;
                            String                      alias = null;
                            DEROctetString              localId = null;

                            Enumeration e = b.getBagAttributes().getObjects();
                            while (e.hasMoreElements())
                            {
                                DERConstructedSequence  sq = (DERConstructedSequence)e.nextElement();
                                DERObjectIdentifier     aOid = (DERObjectIdentifier)sq.getObjectAt(0);
                                DERObject               attr = (DERObject)((DERSet)sq.getObjectAt(1)).getObjectAt(0);
                                bagAttr.setBagAttribute(aOid, attr);

                                if (aOid.equals(pkcs_9_at_friendlyName))
                                {
                                    alias = ((DERBMPString)attr).getString();
                                    keys.put(alias, privKey);
                                }
                                else if (aOid.equals(pkcs_9_at_localKeyId))
                                {
                                    localId = (DEROctetString)attr;
                                }
                            }

                            String name = new String(Hex.encode(localId.getOctets()));

                            if (alias == null)
                            {
                                keys.put(name, privKey);
                            }
                            else
                            {
                                localIds.put(alias, name);
                            }
                        }
                        else if (b.getBagId().equals(certBag))
                        {
                            chain.addElement(b);
                        }
                        else
                        {
                            System.out.println("extra " + b.getBagId());
                            System.out.println("extra " + org.bouncycastle.asn1.util.ASN1Dump.dumpAsString(b));
                        }
                    }
                }
                else if (c[i].getContentType().equals(encryptedData))
                {
                    EncryptedData d = new EncryptedData((DERConstructedSequence)c[i].getContent());
                    DERConstructedSequence seq = decryptData(d.getEncryptionAlgorithm(), ((DEROctetString)d.getContent()).getOctets(), password);

                    for (int j = 0; j != seq.getSize(); j++)
                    {
                        SafeBag b = new SafeBag((DERConstructedSequence)seq.getObjectAt(j));

                        if (b.getBagId().equals(certBag))
                        {
                            chain.addElement(b);
                        }
                    }
                }
                else
                {
                    System.out.println("extra " + c[i].getContentType().getId());
                    System.out.println("extra " + org.bouncycastle.asn1.util.ASN1Dump.dumpAsString(c[i].getContent()));
                }
            }
        }

        certs = new Hashtable();
        chainCerts = new Hashtable();
        keyCerts = new Hashtable();

        for (int i = 0; i != chain.size(); i++)
        {
            SafeBag     b = (SafeBag)chain.elementAt(i);
            CertBag     cb = new CertBag((DERConstructedSequence)b.getBagValue());
            Certificate cert = null;

            try
            {
                ByteArrayInputStream  cIn = new ByteArrayInputStream(
                                ((DEROctetString)cb.getCertValue()).getOctets());
                cert = certFact.generateCertificate(cIn);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.toString());
            }


            //
            // set the attributes
            //
            DEROctetString              localId = null;
            String                      alias = null;

            if (b.getBagAttributes() != null)
            {
                Enumeration e = b.getBagAttributes().getObjects();
                while (e.hasMoreElements())
                {
                    DERConstructedSequence  sq = (DERConstructedSequence)e.nextElement();
                    DERObjectIdentifier     oid = (DERObjectIdentifier)sq.getObjectAt(0);
                    DERObject               attr = (DERObject)((DERSet)sq.getObjectAt(1)).getObjectAt(0);

                    if (cert instanceof PKCS12BagAttributeCarrier)
                    {
                        PKCS12BagAttributeCarrier   bagAttr = (PKCS12BagAttributeCarrier)cert;
                        bagAttr.setBagAttribute(oid, attr);
                    }

                    if (oid.equals(pkcs_9_at_friendlyName))
                    {
                        alias = ((DERBMPString)attr).getString();
                    }
                    else if (oid.equals(pkcs_9_at_localKeyId))
                    {
                        localId = (DEROctetString)attr;
                    }
                }
            }

            chainCerts.put(new CertId(cert.getPublicKey()), cert);

            if (alias == null)
            {
                if (localId != null)
                {
                    String name = new String(Hex.encode(localId.getOctets()));
                    keyCerts.put(name, cert);
                }
            }
            else
            {
                certs.put(alias, cert);
            }
        }
	}
*/
    public void engineStore(OutputStream stream, char[] password,PublicKey pubkey)
        throws IOException
    {
		if ( password == null )
		{
			throw new NullPointerException("No password supplied for PKCS#12 KeyStore.");
		}

        ContentInfo[]   c = new ContentInfo[2];


        //
        // handle the key
        //
        DERConstructedSequence  keyS = new DERConstructedSequence();


        Enumeration ks = keys.keys();

        while (ks.hasMoreElements())
        {
            byte[]                  kSalt = new byte[SALT_SIZE];

            random.nextBytes(kSalt);

            String                  name = (String)ks.nextElement();
            PrivateKey              privKey = (PrivateKey)keys.get(name);
            PKCS12PBEParams         kParams = new PKCS12PBEParams(kSalt, MIN_ITERATIONS);
            byte[]                  kBytes = wrapKey(KEY_ALGORITHM, privKey, kParams, password);
            AlgorithmIdentifier     kAlgId = new AlgorithmIdentifier(new DERObjectIdentifier(KEY_ALGORITHM), kParams.getDERObject());
            org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo kInfo = new org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo(kAlgId, kBytes);
            boolean                 attrSet = false;
            DERConstructedSet       kName = new DERConstructedSet();

            if (privKey instanceof PKCS12BagAttributeCarrier)
            {
                PKCS12BagAttributeCarrier   bagAttrs = (PKCS12BagAttributeCarrier)privKey;
                Enumeration e = bagAttrs.getBagAttributeKeys();

                while (e.hasMoreElements())
                {
                    DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                    DERConstructedSequence  kSeq = new DERConstructedSequence();

                    kSeq.addObject(oid);
                    kSeq.addObject(new DERSet(bagAttrs.getBagAttribute(oid)));

                    attrSet = true;

                    kName.addObject(kSeq);
                }
            }

            if (!attrSet)
            {
                //
                // set a default friendly name (from the key id) and local id
                //
                DERConstructedSequence  kSeq = new DERConstructedSequence();
                Certificate             ct = engineGetCertificate(name);

                kSeq.addObject(pkcs_9_at_localKeyId);
                kSeq.addObject(new DERSet(createSubjectKeyId(pubkey)));

                kName.addObject(kSeq);

                kSeq = new DERConstructedSequence();

                kSeq.addObject(pkcs_9_at_friendlyName);
                kSeq.addObject(new DERSet(new DERBMPString(name)));

                kName.addObject(kSeq);
            }

            SafeBag kBag = new SafeBag(pkcs8ShroudedKeyBag, kInfo.getDERObject(), kName);
            keyS.addObject(kBag);
        }

        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);

        dOut.writeObject(keyS);

        BERConstructedOctetString          keyString = new BERConstructedOctetString(bOut.toByteArray());

        //
        // certficate processing
        //
        byte[]                  cSalt = new byte[SALT_SIZE];

        random.nextBytes(cSalt);

        DERConstructedSequence  certSeq = new DERConstructedSequence();
        PKCS12PBEParams         cParams = new PKCS12PBEParams(cSalt, MIN_ITERATIONS);
        AlgorithmIdentifier     cAlgId = new AlgorithmIdentifier(new DERObjectIdentifier(CERT_ALGORITHM), cParams.getDERObject());
        Hashtable               doneCerts = new Hashtable();

        Enumeration cs = keys.keys();
        while (cs.hasMoreElements())
        {
            try
            {
                String              name = (String)cs.nextElement();
                Certificate         cert = engineGetCertificate(name);
                boolean             cAttrSet = false;
                CertBag             cBag = new CertBag(
                                        x509certType,
                                        new DEROctetString(cert.getEncoded()));
                DERConstructedSet   fName = new DERConstructedSet();

                if (cert instanceof PKCS12BagAttributeCarrier)
                {
                    PKCS12BagAttributeCarrier   bagAttrs = (PKCS12BagAttributeCarrier)cert;
                    Enumeration e = bagAttrs.getBagAttributeKeys();

                    while (e.hasMoreElements())
                    {
                        DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                        DERConstructedSequence  fSeq = new DERConstructedSequence();

                        fSeq.addObject(oid);
                        fSeq.addObject(new DERSet(bagAttrs.getBagAttribute(oid)));
                        fName.addObject(fSeq);

                        cAttrSet = true;
                    }
                }

                if (!cAttrSet)
                {
                    DERConstructedSequence  fSeq = new DERConstructedSequence();

                    fSeq.addObject(pkcs_9_at_localKeyId);
                    fSeq.addObject(new DERSet(createSubjectKeyId(pubkey)));
                    fName.addObject(fSeq);

                    fSeq = new DERConstructedSequence();

                    fSeq.addObject(pkcs_9_at_friendlyName);
                    fSeq.addObject(new DERSet(new DERBMPString(name)));

                    fName.addObject(fSeq);
                }

                SafeBag sBag = new SafeBag(certBag, cBag.getDERObject(), fName);

                certSeq.addObject(sBag);

                doneCerts.put(cert, cert);
            }
            catch (CertificateEncodingException e)
            {
                throw new IOException("Error encoding certificate: " + e.toString());
            }
        }

        cs = certs.keys();
        while (cs.hasMoreElements())
        {
            try
            {
                String              certId = (String)cs.nextElement();
                Certificate         cert = (Certificate)certs.get(certId);
                boolean             cAttrSet = false;

                if (doneCerts.get(cert) != null)
                {
                    continue;
                }

                CertBag             cBag = new CertBag(
                                        x509certType,
                                        new DEROctetString(cert.getEncoded()));
                DERConstructedSet   fName = new DERConstructedSet();

                if (cert instanceof PKCS12BagAttributeCarrier)
                {
                    PKCS12BagAttributeCarrier   bagAttrs = (PKCS12BagAttributeCarrier)cert;
                    Enumeration e = bagAttrs.getBagAttributeKeys();

                    while (e.hasMoreElements())
                    {
                        DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                        DERConstructedSequence  fSeq = new DERConstructedSequence();

                        fSeq.addObject(oid);
                        fSeq.addObject(new DERSet(bagAttrs.getBagAttribute(oid)));
                        fName.addObject(fSeq);

                        cAttrSet = true;
                    }
                }

                if (!cAttrSet)
                {
                    DERConstructedSequence  fSeq = new DERConstructedSequence();

                    fSeq.addObject(pkcs_9_at_friendlyName);
                    fSeq.addObject(new DERSet(new DERBMPString(certId)));

                    fName.addObject(fSeq);
                }

                SafeBag sBag = new SafeBag(certBag, cBag.getDERObject(), fName);

                certSeq.addObject(sBag);

                doneCerts.put(cert, cert);
            }
            catch (CertificateEncodingException e)
            {
                throw new IOException("Error encoding certificate: " + e.toString());
            }
        }

        cs = chainCerts.keys();
        while (cs.hasMoreElements())
        {
            try
            {
                CertId              certId = (CertId)cs.nextElement();
                Certificate         cert = (Certificate)chainCerts.get(certId);

                if (doneCerts.get(cert) != null)
                {
                    continue;
                }

                CertBag             cBag = new CertBag(
                                        x509certType,
                                        new DEROctetString(cert.getEncoded()));
                DERConstructedSet   fName = new DERConstructedSet();

                if (cert instanceof PKCS12BagAttributeCarrier)
                {
                    PKCS12BagAttributeCarrier   bagAttrs = (PKCS12BagAttributeCarrier)cert;
                    Enumeration e = bagAttrs.getBagAttributeKeys();

                    while (e.hasMoreElements())
                    {
                        DERObjectIdentifier oid = (DERObjectIdentifier)e.nextElement();
                        DERConstructedSequence  fSeq = new DERConstructedSequence();

                        fSeq.addObject(oid);
                        fSeq.addObject(new DERSet(bagAttrs.getBagAttribute(oid)));
                        fName.addObject(fSeq);
                    }
                }

                SafeBag sBag = new SafeBag(certBag, cBag.getDERObject(), fName);

                certSeq.addObject(sBag);
            }
            catch (CertificateEncodingException e)
            {
                throw new IOException("Error encoding certificate: " + e.toString());
            }
        }

        bOut.reset();

        dOut = new DEROutputStream(bOut);

        dOut.writeObject(certSeq);

        dOut.close();

        byte[]                  certBytes = encryptData(CERT_ALGORITHM, bOut.toByteArray(), cParams, password);
        EncryptedData           cInfo = new EncryptedData(data, cAlgId, new BERConstructedOctetString(certBytes));

        c[0] = new ContentInfo(data, keyString);

        c[1] = new ContentInfo(encryptedData, cInfo.getDERObject());

        AuthenticatedSafe   auth = new AuthenticatedSafe(c);

        bOut.reset();

        BEROutputStream         berOut = new BEROutputStream(bOut);

        berOut.writeObject(auth);

        byte[]              pkg = bOut.toByteArray();

        ContentInfo         mainInfo = new ContentInfo(data, new BERConstructedOctetString(pkg));

        //
        // create the mac
        //
        byte[]                      mSalt = new byte[20];
        int                         itCount = MIN_ITERATIONS;

        random.nextBytes(mSalt);

        byte[]  data = ((DEROctetString)mainInfo.getContent()).getOctets();

        MacData                 mData = null;

        try
        {
            //Orginal
       /*     Mac                 mac = Mac.getInstance(id_SHA1.getId(), "BC");
            System.out.println("Original Mac: "+mac.getAlgorithm());
            SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(id_SHA1.getId(), "BC");
            System.out.println("Original SecretKeyFac: "+keyFact.getAlgorithm());
            PBEParameterSpec    defParams = new PBEParameterSpec(mSalt, itCount);
            PBEKeySpec          pbeSpec = new PBEKeySpec(password);

            mac.init(keyFact.generateSecret(pbeSpec), defParams);

            mac.update(data);

            byte[]      res = mac.doFinal();

            AlgorithmIdentifier     algId = new AlgorithmIdentifier(id_SHA1, null);
            DigestInfo              dInfo = new DigestInfo(algId, res);

            mData = new MacData(dInfo, mSalt, itCount);
*/

          //My


            org.bouncycastle.crypto.Mac my_mac=new HMac(new SHA1Digest());
            PBEParameterSpec    my_defParams = new PBEParameterSpec(mSalt, itCount);
            PBEKeySpec          my_pbeSpec = new PBEKeySpec(password);

            CipherParameters        my_param;
            My_PBEKey my_key=new My_PBEKey(true, "PBE/PKCS12", my_pbeSpec.getPassword());//(JCEPBEKey)keyFact.generateSecret(my_pbeSpec);
            my_param = makePBEMacParameters(my_key, my_defParams, PBE.PKCS12, PBE.SHA1, 160);///pbeType, pbeHash, keySize);
            my_mac.init(my_param);
            my_mac.update(data,0,data.length);
            byte[]      my_res = new byte[my_mac.getMacSize()];
            my_mac.doFinal(my_res,0);

            AlgorithmIdentifier     my_algId = new AlgorithmIdentifier(id_SHA1, null);
            DigestInfo              my_dInfo = new DigestInfo(my_algId, my_res);

            mData = new MacData(my_dInfo, mSalt, itCount);
        }
        catch (Exception e)
        {
            throw new IOException("error constructing MAC: " + e.toString());
        }

        //
        // output the Pfx
        //
        Pfx                 pfx = new Pfx(mainInfo, mData);

        berOut = new BEROutputStream(stream);

        berOut.writeObject(pfx);
    }

    public static class BCPKCS12KeyStore
        extends JDKPKCS12KeyStore
    {
        public BCPKCS12KeyStore()
        {
            super("BC");
        }
    }

    public static class DefPKCS12KeyStore
        extends JDKPKCS12KeyStore
    {
        public DefPKCS12KeyStore()
        {
            super(null);
        }
    }


         static private PBEParametersGenerator makePBEGenerator(
            int                     type,
            int                     hash)
        {
            PBEParametersGenerator  generator;

  /*          if (type == PBE.PKCS5S1)
            {
                switch (hash)
                {
                case PBE.MD5:
                    generator = new PKCS5S1ParametersGenerator(new MD5Digest());
                    break;
                case PBE.SHA1:
                    generator = new PKCS5S1ParametersGenerator(new SHA1Digest());
                    break;
                default:
                    throw new IllegalStateException("PKCS5 scheme 1 only supports only MD5 and SHA1.");
                }
            }
            else if (type == PBE.PKCS5S2)
            {
                generator = new PKCS5S2ParametersGenerator();
            }
            else
  */          {
                switch (hash)
                {
             //   case PBE.MD5:
             //       generator = new PKCS12ParametersGenerator(new MD5Digest());
             //       break;
                case PBE.SHA1:
                    generator = new PKCS12ParametersGenerator(new SHA1Digest());
                    break;
             //   case PBE.RIPEMD160:
             //       generator = new PKCS12ParametersGenerator(new RIPEMD160Digest());
             //       break;
             //   case PBE.TIGER:
             //       generator = new PKCS12ParametersGenerator(new TigerDigest());
             //       break;
                default:
                    throw new IllegalStateException("unknown digest scheme for PBE encryption.");
                }
            }

            return generator;
        }

      static CipherParameters makePBEMacParameters(
            SecretKey               pbeKey,
            PBEParameterSpec  spec,
            int                     type,
            int                     hash,
            int                     keySize)
        {
            if ((spec == null) || !(spec instanceof PBEParameterSpec))
            {
                throw new IllegalArgumentException("Need a PBEParameter spec with a PBE key.");
            }

            PBEParameterSpec        pbeParam = (PBEParameterSpec)spec;
            PBEParametersGenerator  generator = makePBEGenerator(type, hash);
            byte[]                  key = pbeKey.getEncoded();
            CipherParameters        param;

            generator.init(key, pbeParam.getSalt(), pbeParam.getIterationCount());

            param = generator.generateDerivedMacParameters(keySize);

            for (int i = 0; i != key.length; i++)
            {
                key[i] = 0;
            }

            return param;
        }

    static CipherParameters makePBEParameters(
            SecretKey               pbeKey,
            PBEParameterSpec        spec,
            int                     type,
            int                     hash,
            String                  targetAlgorithm,
            int                     keySize,
            int                     ivSize)
        {
            if ((spec == null) || !(spec instanceof PBEParameterSpec))
            {
                throw new IllegalArgumentException("Need a PBEParameter spec with a PBE key.");
            }

            PBEParameterSpec        pbeParam = (PBEParameterSpec)spec;
            PBEParametersGenerator  generator = makePBEGenerator(type, hash);
            byte[]                  key = pbeKey.getEncoded();
            CipherParameters        param;

            generator.init(key, pbeParam.getSalt(), pbeParam.getIterationCount());

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
                    KeyParameter    kParam = (KeyParameter)((ParametersWithIV)param).getParameters();

                    DESParameters.setOddParity(kParam.getKey());
                }
                else
                {
                    KeyParameter    kParam = (KeyParameter)param;

                    DESParameters.setOddParity(kParam.getKey());
                }
            }

            for (int i = 0; i != key.length; i++)
            {
                key[i] = 0;
            }

            return param;
        }



    class My_PBEKey
    implements SecretKey
{
    String  algorithm;
    char[]  password;
    boolean pkcs12;

    My_PBEKey(
        boolean pkcs12,
        String  algorithm,
        char[]  password)
    {
        this.pkcs12 = pkcs12;
        this.algorithm = algorithm;
        this.password = password;
    }

    public String getAlgorithm()
    {
        return algorithm;
    }

    public String getFormat()
    {
        return "RAW";
    }

    public byte[] getEncoded()
    {
        if (pkcs12)
        {
            return PBEParametersGenerator.PKCS12PasswordToBytes(password);
        }
        else
        {
            return PBEParametersGenerator.PKCS5PasswordToBytes(password);
        }
    }
}


}
