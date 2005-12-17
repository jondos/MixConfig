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
package mixconfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.bcpg.BCPGInputStream;
import org.bouncycastle.bcpg.Packet;
import org.bouncycastle.bcpg.RSAPublicBCPGKey;
import org.bouncycastle.bcpg.RSASecretBCPGKey;
import org.bouncycastle.bcpg.S2K;
import org.bouncycastle.bcpg.SecretKeyPacket;
import org.bouncycastle.bcpg.SecretSubkeyPacket;
import org.bouncycastle.bcpg.UserIDPacket;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.CAST5Engine;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import anon.crypto.AsymmetricCryptoKeyPair;
import anon.crypto.MyRSAPrivateKey;
import anon.crypto.PKCS12;
import anon.crypto.Validity;
import anon.crypto.X509DistinguishedName;
import gui.*;
import javax.swing.JFileChooser;




public class PGPtoX509Tool extends JDialog implements ActionListener
{

	private JTextField m_textFile;
	private File m_File;
	private Frame m_Parent;
	public PGPtoX509Tool(Frame parent)
	{
		super(parent, "PGP to X.509 key converter", true);
		m_Parent = parent;
		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);
		GridBagLayout layoutDecryptWith = new GridBagLayout();
		GridBagLayout layoutBttns = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 10, 10, 10);
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridx = 0;
		c.gridy = 0;
		JPanel panel2 = new JPanel(layoutDecryptWith);
		GridBagConstraints e = new GridBagConstraints();
		e.anchor = GridBagConstraints.NORTHWEST;
		e.insets = new Insets(5, 5, 5, 5);
		e.fill = GridBagConstraints.HORIZONTAL;
		panel2.setBorder(new TitledBorder("Key to convert"));
		layout.setConstraints(panel2, c);
		getContentPane().add(panel2);

		JButton import1 = new JButton("Select...");
		e.gridx = 1;
		e.gridy = 0;
		e.fill = GridBagConstraints.NONE;
		import1.addActionListener(this);
		import1.setActionCommand("selectKey");
		layoutDecryptWith.setConstraints(import1, e);
		panel2.add(import1);
		e.fill = GridBagConstraints.HORIZONTAL;

		JLabel name2 = new JLabel("File");
		e.gridx = 0;
		e.gridy = 1;
		layoutDecryptWith.setConstraints(name2, e);
		panel2.add(name2);
		m_textFile = new JTextField(26);
		m_textFile.setEditable(false);
		e.gridx = 1;
		e.gridwidth = 3;
		e.weightx = 1;
		layoutDecryptWith.setConstraints(m_textFile, e);
		panel2.add(m_textFile);

		JPanel panel3 = new JPanel(layoutBttns);
		GridBagConstraints f = new GridBagConstraints();
		f.anchor = GridBagConstraints.NORTHEAST;
		f.insets = new Insets(5, 5, 5, 5);

		JLabel space = new JLabel();
		f.gridx = 0;
		f.gridy = 0;
		f.weightx = 1;
		f.fill = f.HORIZONTAL;
		layoutBttns.setConstraints(space, f);
		panel3.add(space);

		JButton bttnSign = new JButton("Convert and Save");
		bttnSign.setActionCommand("Convert");
		bttnSign.addActionListener(this);
		f.gridx = 0;
		f.gridy = 0;
		f.weightx = 0;
		f.fill = f.NONE;
		layoutBttns.setConstraints(bttnSign, f);
		panel3.add(bttnSign);

		JButton bttnCancel = new JButton("Cancel");
		bttnCancel.setActionCommand("Cancel");
		bttnCancel.addActionListener(this);
		f.gridx = 1;
		f.gridy = 0;
		layoutBttns.setConstraints(bttnCancel, f);
		panel3.add(bttnCancel);

		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1;
		c.weighty = 1;
		layout.setConstraints(panel3, c);
		getContentPane().add(panel3);

		pack();
		GUIUtils.positionWindow(this, parent);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		String strCmd = e.getActionCommand();
		if (strCmd.equals("Cancel"))
		{
			dispose();
		}
		else if (strCmd.equals("Convert"))
		{
			doConvert();
		}
		else if (strCmd.equals("selectKey"))
		{
			JFileChooser fileChooser = MixConfig.showFileDialog(MixConfig.OPEN_DIALOG,
				MixConfig.FILTER_ALL);

			if (fileChooser != null)
			{
				File f = fileChooser.getSelectedFile();
				if (f != null)
				{
					m_textFile.setText(f.getAbsolutePath());
					m_File = f;
				}
			}
		}
	}

	private void doConvert()
	{
		try
		{
			doPGPtoX509();
			JOptionPane.showMessageDialog(this, "Converted and saved successfully!");
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(this, "Ooops, error during transformation: " + e.toString());
		}
	}

	/*	public static void main_dsa(String[] args)
	  {
	   try
	 {
	   FileInputStream fin=new FileInputStream("testtest.asc");
	  BCPGInputStream  bin=new BCPGInputStream(new ArmoredInputStream(fin));
	  Packet p=bin.readPacket();
	  SecretKeyPacket skp=(SecretKeyPacket)p;
	  DSAPublicBCPGKey pubKey=(DSAPublicBCPGKey)skp.getPublicKeyPacket().getKey();
	  String passPhrase="test";

	 byte[] key = makeKeyFromPassPhrase(skp.getEncAlgorithm(),
		skp.getS2K(), passPhrase.toCharArray(),"BC");

	 //	DSASecretBCPGKey sk=new DSASecretBCPGKey(bin);
	  String	cName = "CAST5";//PGPUtil.getSymmetricCipherName(skp.getEncAlgorithm());
	  //			Cipher										c = null;

	  CAST5Engine cast5=new CAST5Engine();
	  CFBBlockCipher cfb=new CFBBlockCipher(cast5,64);
	 BufferedBlockCipher c=new BufferedBlockCipher(cfb);
	 ParametersWithIV params=new ParametersWithIV(new KeyParameter(key),skp.getIV());
	  c.init(false,params);
	  byte[] cin=skp.getSecretKeyData();
	   byte[] decoded=new byte[cin.length+8];
	 int outlen=c.processBytes(cin,0,cin.length,decoded,0);

	 c.doFinal(decoded,outlen);
	 DSASecretBCPGKey secKey=new DSASecretBCPGKey(new BCPGInputStream(new ByteArrayInputStream(decoded)));

	   //now make an X.509 cert....
	  DSAParameters dsap=new DSAParameters(pubKey.getP(),pubKey.getQ(),pubKey.getG());
	  DSAPublicKeyParameters dsaparams=new DSAPublicKeyParameters(pubKey.getY(),dsap);
	  MyDSAPublicKey tmpPubKey=new MyDSAPublicKey(dsaparams);
	  DSAPrivateKeyParameters dsaprivateparams=new DSAPrivateKeyParameters(secKey.getX(),dsap);
	  MyDSAPrivateKey tmpSecKey=new MyDSAPrivateKey(dsaprivateparams);
	  X509Name x509jap=new X509Name("CN=JAP-Team,E=jap@inf.tu-dresden.de,C=DE,ST=Saxony,O=\"Dresden, University of Technology\",L=Dresden,OU=\"Department of Computer Science, Institute for System Architecture\"");
	  X509CertGenerator v3certgen=new X509CertGenerator();
	  v3certgen.setSubjectPublicKeyInfo(tmpPubKey.getAsSubjectPublicKeyInfo());
	  v3certgen.setSubject(x509jap);

	  v3certgen.setEndDate(new Time(new Date(2000000000000L)));
	  Date d=new Date(100,11,8,1,0,0);
	  v3certgen.setStartDate(new Time(d));
	  v3certgen.setSerialNumber(new DERInteger(1));
	  X509CertificateStructure cert=v3certgen.sign(x509jap,tmpSecKey);
	  FileOutputStream fout=new FileOutputStream("japroot.cer");
	  DEROutputStream dout=new DEROutputStream(fout);
	  dout.writeObject(cert);
	  fout.close();
	  PKCS12 pkcs12=new PKCS12("test",tmpSecKey,cert,tmpPubKey);
	  fout=new FileOutputStream("test.pfx");
	  pkcs12.store(fout,"test".toCharArray());
	 }
	   catch(Exception e)
	 {
	  e.printStackTrace();
	 }
	  }
	 */
	public void doPGPtoX509() throws Exception
	{
		FileInputStream fin = new FileInputStream(m_File);
		BCPGInputStream bin = new BCPGInputStream(new ArmoredInputStream(fin));
		SecretKeyPacket skp = null;
		UserIDPacket uidp = null;
		Packet p = null;
		while ( (p = bin.readPacket()) != null)
		{
			if (p instanceof SecretSubkeyPacket||p instanceof SecretKeyPacket)
			{
				skp = (SecretKeyPacket) p;
			}
			else if (p instanceof UserIDPacket)
			{
				uidp = (UserIDPacket) p;

			}
		}
		RSAPublicBCPGKey pubKey = (RSAPublicBCPGKey) skp.getPublicKeyPacket().getKey();
		PasswordBox pb = new PasswordBox(m_Parent, "Passphrase for reading PGP Key",
										 PasswordBox.ENTER_PASSWORD,
										 "Please enter the passphrase for the PGP key:");
		pb.setVisible(true);
		char[] passPhrase = pb.getPassword();

		byte[] key = makeKeyFromPassPhrase(skp.getEncAlgorithm(),
										   skp.getS2K(), passPhrase);

		//	DSASecretBCPGKey sk=new DSASecretBCPGKey(bin);
		String cName = "CAST5"; //PGPUtil.getSymmetricCipherName(skp.getEncAlgorithm());
		//			Cipher										c = null;

		CAST5Engine cast5 = new CAST5Engine();
		CFBBlockCipher cfb = new CFBBlockCipher(cast5, 64);
		BufferedBlockCipher c = new BufferedBlockCipher(cfb);
		ParametersWithIV params = new ParametersWithIV(new KeyParameter(key), skp.getIV());
		c.init(false, params);
		byte[] cin = skp.getSecretKeyData();
		byte[] decoded = new byte[cin.length + 8];
		int outlen = c.processBytes(cin, 0, cin.length, decoded, 0);

		c.doFinal(decoded, outlen);
		RSASecretBCPGKey secKey = new RSASecretBCPGKey(new BCPGInputStream(new ByteArrayInputStream(decoded)));
		//now make an X.509 cert....
		//DSAParameters dsap=new DSAParameters(pubKey.getP(),pubKey.getQ(),pubKey.getG());
		//			DSAPublicKeyParameters dsaparams=new DSAPublicKeyParameters(pubKey.getY(),dsap);
		MyRSAPrivateKey tmpSecKey = new MyRSAPrivateKey(secKey.getModulus(),
			pubKey.getPublicExponent(),
			secKey.getPrivateExponent(),
			secKey.getPrimeP(),
			secKey.getPrimeQ(),
			secKey.getPrimeExponentP(),
			secKey.getPrimeExponentQ(),
			secKey.getCrtCoefficient());


		PKCS12 privateCertificate = new PKCS12(new X509DistinguishedName("CN=" +uidp.getID()),
											   new AsymmetricCryptoKeyPair(tmpSecKey),
											   new Validity(Calendar.getInstance(), 10));

		File fileDir = new File(m_File.getParent());
		FileOutputStream fout = new FileOutputStream(new File(fileDir, m_File.getName() + ".cer"));
		privateCertificate.getX509Certificate().store(fout);
		fout.close();

		fout = new FileOutputStream(new File(fileDir, m_File.getName() + ".pfx"));
		pb = new PasswordBox(m_Parent, "Password for X.509 key", PasswordBox.NEW_PASSWORD,
							 "Please enter a password for the X.509 key:");
		pb.setVisible(true);
		passPhrase = pb.getPassword();
		privateCertificate.store(fout, passPhrase);
	}

	public static byte[] makeKeyFromPassPhrase(
		int algorithm,
		S2K s2k,
		char[] passPhrase)
	{
		String algName = null;
		int keySize = 0;

		/*			switch (algorithm)
		   {
		  case SymmetricKeyAlgorithmTags.TRIPLE_DES:
		 keySize = 192;
		 algName = "DES_EDE";
		 break;
		   case SymmetricKeyAlgorithmTags.IDEA:
		 keySize = 128;
		 algName = "IDEA";
		 break;
		   case SymmetricKeyAlgorithmTags.CAST5:
		 */
		keySize = 128;
		algName = "CAST5";
		/*					break;
		   case SymmetricKeyAlgorithmTags.BLOWFISH:
		  keySize = 128;
		  algName = "Blowfish";
		  break;
		 case SymmetricKeyAlgorithmTags.SAFER:
		  keySize = 128;
		  algName = "SAFER";
		  break;
		   case SymmetricKeyAlgorithmTags.DES:
		  keySize = 64;
		  algName = "DES";
		  break;
		   case SymmetricKeyAlgorithmTags.AES_128:
		  keySize = 128;
		  algName = "AES";
		  break;
		   case SymmetricKeyAlgorithmTags.AES_192:
		  keySize = 192;
		  algName = "AES";
		  break;
		   case SymmetricKeyAlgorithmTags.AES_256:
		  keySize = 256;
		  algName = "AES";
		  break;
		   case SymmetricKeyAlgorithmTags.TWOFISH:
		  keySize = 256;
		  algName = "Twofish";
		  break;
		 default:
		  throw new PGPException("unknown symmetric algorithm: " + algorithm);
		 }
		 */
		byte[] pBytes = new byte[passPhrase.length];
		GeneralDigest digest;

		for (int i = 0; i != passPhrase.length; i++)
		{
			pBytes[i] = (byte) passPhrase[i];
		}

		if (s2k != null)
		{
			digest = new SHA1Digest();
			byte[] iv = s2k.getIV();

			switch (s2k.getType())
			{
				case S2K.SIMPLE:
					digest.update(pBytes, 0, pBytes.length);
					break;
				case S2K.SALTED:
					digest.update(iv, 0, iv.length);
					digest.update(pBytes, 0, pBytes.length);
					break;
				case S2K.SALTED_AND_ITERATED:
					long count = s2k.getIterationCount();
					digest.update(iv, 0, iv.length);
					digest.update(pBytes, 0, pBytes.length);

					count -= iv.length + passPhrase.length;

					while (count > 0)
					{
						if (count < iv.length)
						{
							digest.update(iv, 0, (int) count);
							count = 0;
						}
						else
						{
							digest.update(iv, 0, iv.length);
							count -= iv.length;
						}

						if (count < pBytes.length)
						{
							digest.update(pBytes, 0, (int) count);
							count = 0;
						}
						else
						{
							digest.update(pBytes, 0, pBytes.length);
							count -= pBytes.length;
						}
					}
					break;
				default:
					return null;
			}
		}
		else
		{
			digest = new MD5Digest();
			digest.update(pBytes, 0, pBytes.length);

		}

		for (int i = 0; i != pBytes.length; i++)
		{
			pBytes[i] = 0;
		}

		byte[] dig = new byte[digest.getDigestSize()];
		digest.doFinal(dig, 0);

		byte[] keyBytes = new byte[ (keySize + 7) / 8];

		System.arraycopy(dig, 0, keyBytes, 0, keyBytes.length);

		return keyBytes;
	}

}
