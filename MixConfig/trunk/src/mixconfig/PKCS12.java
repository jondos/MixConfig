package mixconfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.bouncycastle.asn1.BERConstructedOctetString;
import org.bouncycastle.asn1.BEROutputStream;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERConstructedSequence;
import org.bouncycastle.asn1.DERConstructedSet;
import org.bouncycastle.asn1.DERInputStream;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
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
import org.bouncycastle.asn1.pkcs.SafeBag;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
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
import org.bouncycastle.jce.provider.PBE;


public class PKCS12
    implements PKCSObjectIdentifiers, X509ObjectIdentifiers
{
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

    protected SecureRandom      random = new SecureRandom();

    private String alias;
    private PrivateKey privKey;
    private PublicKey pubKey;
    private X509CertificateStructure x509cert;

    public PKCS12(
        String          al,
        PrivateKey      privkey,
        X509CertificateStructure   cert,
        PublicKey       pubkey
        )
    {
        alias = al;
        privKey = privkey;
        pubKey = pubkey;
        x509cert = cert;
    }

    private SubjectKeyIdentifier createSubjectKeyId(
        PublicKey   pubKey)
    {
        try
        {
            ByteArrayInputStream    bIn = new ByteArrayInputStream(pubKey.getEncoded());
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
                (DERConstructedSequence)new DERInputStream(bIn).readObject());
            return new SubjectKeyIdentifier(info);
        }
        catch (Exception e)
        {
            throw new RuntimeException("error creating key");
        }
    }

    static public byte[] codeData(
        boolean                 encrypt,
        byte[]                  data,
        PKCS12PBEParams         pbeParams,
        char[]                  password,
        BlockCipher             cipher,
        int                     keySize)
        throws IOException
    {
        PBEKeySpec          pbeSpec = new PBEKeySpec(password);
        byte[]              my_out;

        try
        {
            PBEParameterSpec    my_defParams = new PBEParameterSpec(
                                                pbeParams.getIV(),
                                                pbeParams.getIterations().intValue());
            My_PBEKey           my_key = new My_PBEKey(true, "PBE/PKCS12", pbeSpec.getPassword());
            BufferedBlockCipher my_cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(cipher));
            CipherParameters    my_param = makePBEParameters((SecretKey)my_key, my_defParams, PBE.PKCS12, PBE.SHA1,
                                            my_cipher.getUnderlyingCipher().getAlgorithmName(),keySize,64);
            ParametersWithIV   my_ivParam = (ParametersWithIV)my_param;

            my_param = new ParametersWithRandom(my_param, new SecureRandom());
            my_cipher.init(encrypt, my_param);

            byte[]  my_input=data;
            int     my_inputlen=my_input.length;
            int     my_len = 0;
            byte[]  my_tmp = new byte[my_cipher.getOutputSize(my_inputlen)];

            if (my_inputlen != 0)
                my_len = my_cipher.processBytes(my_input, 0, my_inputlen, my_tmp, 0);

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
            e.printStackTrace();
            throw new IOException("exception encrypting data - " + e.toString());
        }

        return my_out;
    }

    public void store(OutputStream stream, char[] password)
        throws IOException
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        DEROutputStream         dOut = new DEROutputStream(bOut);

        //
        // handle the key
        //
        {
            DERConstructedSequence  keyS = new DERConstructedSequence();

            byte[]                  kSalt = new byte[SALT_SIZE];
            random.nextBytes(kSalt);
            PKCS12PBEParams         kParams = new PKCS12PBEParams(kSalt, MIN_ITERATIONS);
            byte[]                  kBytes = codeData(true, privKey.getEncoded(), kParams, password, new DESedeEngine(), 192);
            AlgorithmIdentifier     kAlgId = new AlgorithmIdentifier(new DERObjectIdentifier(KEY_ALGORITHM), kParams.getDERObject());
            EncryptedPrivateKeyInfo kInfo = new EncryptedPrivateKeyInfo(kAlgId, kBytes);
            DERConstructedSet       kName = new DERConstructedSet();

            //
            // set a default friendly name (from the key id) and local id
            //
            DERConstructedSequence  kSeq = new DERConstructedSequence();
            kSeq.addObject(pkcs_9_at_localKeyId);
            kSeq.addObject(new DERSet(createSubjectKeyId(pubKey)));
            kName.addObject(kSeq);

            kSeq = new DERConstructedSequence();
            kSeq.addObject(pkcs_9_at_friendlyName);
            kSeq.addObject(new DERSet(new DERBMPString(alias)));
            kName.addObject(kSeq);

            SafeBag kBag = new SafeBag(pkcs8ShroudedKeyBag, kInfo.getDERObject(), kName);
            keyS.addObject(kBag);
            dOut.writeObject(keyS);
            dOut.close();
        }
        BERConstructedOctetString          keyString = new BERConstructedOctetString(bOut.toByteArray());

        //
        // certficate processing
        //
        EncryptedData           cInfo;
        {
            byte[]                  cSalt = new byte[SALT_SIZE];
            random.nextBytes(cSalt);
            DERConstructedSequence  certSeq = new DERConstructedSequence();
            PKCS12PBEParams         cParams = new PKCS12PBEParams(cSalt, MIN_ITERATIONS);
            AlgorithmIdentifier     cAlgId = new AlgorithmIdentifier(new DERObjectIdentifier(CERT_ALGORITHM), cParams.getDERObject());

            try
            {
                bOut.reset();
                dOut = new DEROutputStream(bOut);
                dOut.writeObject(x509cert);
                CertBag             cBag = new CertBag(x509certType, new DEROctetString(bOut.toByteArray()));
                DERConstructedSet   fName = new DERConstructedSet();

                DERConstructedSequence  fSeq = new DERConstructedSequence();
                fSeq.addObject(pkcs_9_at_localKeyId);
                fSeq.addObject(new DERSet(createSubjectKeyId(pubKey)));
                fName.addObject(fSeq);

                fSeq = new DERConstructedSequence();
                fSeq.addObject(pkcs_9_at_friendlyName);
                fSeq.addObject(new DERSet(new DERBMPString(alias)));
                fName.addObject(fSeq);

                SafeBag sBag = new SafeBag(certBag, cBag.getDERObject(), fName);
                certSeq.addObject(sBag);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                throw new IOException("Error encoding certificate: " + e.toString());
            }
            bOut.reset();
            dOut = new DEROutputStream(bOut);
            dOut.writeObject(certSeq);
            dOut.close();

            byte[] certBytes = codeData(true, bOut.toByteArray(), cParams, password, new RC2Engine(), 40);
            cInfo = new EncryptedData(data, cAlgId, new BERConstructedOctetString(certBytes));
        }

        ContentInfo[]   c = new ContentInfo[2];
        c[0] = new ContentInfo(data, keyString);
        c[1] = new ContentInfo(encryptedData, cInfo.getDERObject());

        bOut.reset();
        AuthenticatedSafe   auth = new AuthenticatedSafe(c);
        BEROutputStream     berOut = new BEROutputStream(bOut);
        berOut.writeObject(auth);
        byte[]              pkg = bOut.toByteArray();

        ContentInfo         mainInfo = new ContentInfo(data, new BERConstructedOctetString(pkg));

        //
        // create the mac
        //
        byte[]                      mSalt = new byte[SALT_SIZE];
        int                         itCount = MIN_ITERATIONS;
        random.nextBytes(mSalt);
        byte[]  contentData = ((DEROctetString)mainInfo.getContent()).getOctets();
        MacData                 mData = null;

        try
        {
            Mac                 certMac = new HMac(new SHA1Digest());
            PBEParameterSpec    defParams = new PBEParameterSpec(mSalt, itCount);
            PBEKeySpec          pbeSpec = new PBEKeySpec(password);

            My_PBEKey           my_key = new My_PBEKey(true, "PBE/PKCS12", pbeSpec.getPassword());
            CipherParameters    cParam = makePBEMacParameters(my_key, defParams, PBE.PKCS12, PBE.SHA1, 160);
            certMac.init(cParam);
            certMac.update(contentData,0,contentData.length);
            byte[]      my_res = new byte[certMac.getMacSize()];
            certMac.doFinal(my_res,0);

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


         static private PBEParametersGenerator makePBEGenerator(
            int                     type,
            int                     hash)
        {
            PBEParametersGenerator  generator;


            {
                switch (hash)
                {
                case PBE.SHA1:
                    generator = new PKCS12ParametersGenerator(new SHA1Digest());
                    break;
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



    static class My_PBEKey implements SecretKey
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
                return PBEParametersGenerator.PKCS12PasswordToBytes(password);
            else
                return PBEParametersGenerator.PKCS5PasswordToBytes(password);
        }
    }
}
