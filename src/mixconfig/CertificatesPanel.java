package mixconfig;
import java.util.Date;
import java.util.Calendar;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.MediaTracker;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;

import java.math.BigInteger;

import java.io.*;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;
import javax.swing.BorderFactory;
import java.net.URLEncoder;

import javax.swing.border.TitledBorder;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.DSAParameterSpec;
import org.bouncycastle.jce.X509V3CertificateGenerator;
import org.bouncycastle.jce.provider.*;

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.*;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.x9.*;
import org.bouncycastle.crypto.generators.DSAParametersGenerator;
import org.bouncycastle.crypto.generators.DSAKeyPairGenerator;
import org.bouncycastle.crypto.params.DSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.signers.DSASigner;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.CipherParameters;

class CertificatesPanel extends JPanel implements ActionListener
  {
    JPanel panel1,panel2,panel3;
    JTextField text1,from_text1,to_text1;
    JTextField m_textPrevCertCN,m_textPrevCertValidFrom,m_textPrevCertValidTo;
    JTextField m_textNextCertCN,m_textNextCertValidFrom,m_textNextCertValidTo;
    JButton import1,import2,create,m_bttnExportOwnPub,m_bttnChangePasswd;

    byte[] m_ownPubCert;
    byte[] m_ownPrivCert;
    byte[] m_nextPubCert;
    byte[] m_prevPubCert;

    public CertificatesPanel()
  {
    m_ownPubCert=null;
    m_ownPrivCert=null;
    GridBagLayout layout=new GridBagLayout();
    setLayout(layout);
    GridBagLayout Own=new GridBagLayout();
    GridBagLayout Previous=new GridBagLayout();
    GridBagLayout Next=new GridBagLayout();

    GridBagConstraints c=new GridBagConstraints();
    c.anchor=GridBagConstraints.NORTHWEST;
    c.insets=new Insets(10,10,10,10);
    c.fill = GridBagConstraints.HORIZONTAL;

    panel1 = new JPanel(Own);
    GridBagConstraints d=new GridBagConstraints();
    d.anchor=GridBagConstraints.NORTHWEST;
    d.insets=new Insets(5,5,5,5);
    panel1.setBorder(new TitledBorder("Own Mix Certificate"));
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1;
    c.weighty = 1;
    layout.setConstraints(panel1,c);
    add(panel1);

    create = new JButton("Create a New One");
    d.gridx = 1;
    d.gridy = 0;
    d.gridwidth = 1;
    d.fill = GridBagConstraints.NONE;
    create.addActionListener(this);
    create.setActionCommand("Create");
    Own.setConstraints(create,d);
    panel1.add(create);
    JButton bttn = new JButton("Import...");
    d.gridx = 2;
    d.gridy = 0;
    d.gridwidth = 1;
    bttn.addActionListener(this);
    bttn.setActionCommand("ImportOwnCert");
    Own.setConstraints(bttn,d);
    panel1.add(bttn);
    m_bttnExportOwnPub = new JButton("Export...");
    d.gridx = 3;
    d.gridy = 0;
    d.gridwidth = 1;
    m_bttnExportOwnPub.addActionListener(this);
    m_bttnExportOwnPub.setActionCommand("ExportOwnPubCert");
    m_bttnExportOwnPub.setEnabled(false);
    Own.setConstraints(m_bttnExportOwnPub,d);
    panel1.add(m_bttnExportOwnPub);
    m_bttnChangePasswd = new JButton("Change Password");
    d.gridx = 4;
    m_bttnChangePasswd.addActionListener(this);
    m_bttnChangePasswd.setActionCommand("passwd");
    m_bttnChangePasswd.setEnabled(false);
    Own.setConstraints(m_bttnChangePasswd,d);
    panel1.add(m_bttnChangePasswd);

    d.gridx = 0;
    d.gridy = 1;
    d.fill = GridBagConstraints.HORIZONTAL;
    JLabel name1 = new JLabel("Name");
    Own.setConstraints(name1,d);
    panel1.add(name1);
    text1 = new JTextField();
    text1.setEditable(false);
    d.gridx = 1;
    d.gridwidth = 4;
    d.weightx = 1;
    Own.setConstraints(text1,d);
    panel1.add(text1);

    JLabel from1 = new JLabel("Valid From");
    d.gridx = 0;
    d.gridy = 2;
    d.gridwidth = 1;
    d.weightx = 0;
    Own.setConstraints(from1,d);
    panel1.add(from1);
    from_text1 = new JTextField();
    from_text1.setEditable(false);
    d.gridx = 1;
    d.gridwidth = 4;
    d.weightx = 1;
    Own.setConstraints(from_text1,d);
    panel1.add(from_text1);

    JLabel to1 = new JLabel("Valid To");
    d.gridx = 0;
    d.gridy = 3;
    d.gridwidth = 1;
    d.weightx = 0;
    Own.setConstraints(to1,d);
    panel1.add(to1);
    to_text1 = new JTextField();
    to_text1.setEditable(false);
    d.gridx = 1;
    d.gridwidth = 4;
    d.weightx = 1;
    Own.setConstraints(to_text1,d);
    panel1.add(to_text1);

    c.gridx = 0;
    c.gridy = 1;
    panel2 = new JPanel(Previous);
    GridBagConstraints e=new GridBagConstraints();
    e.anchor=GridBagConstraints.NORTHWEST;
    e.insets=new Insets(5,5,5,5);
    e.fill = GridBagConstraints.HORIZONTAL;
  /*  panel2.setBorder(BorderFactory.createCompoundBorder(
		BorderFactory.createTitledBorder("Previous Mix Certificate"),
		BorderFactory.createEmptyBorder(0,0,0,0))); */
    panel2.setBorder(new TitledBorder("Previous Mix Certificate"));
    layout.setConstraints(panel2,c);
    add(panel2);

    import1 = new JButton("Import...");
    e.gridx = 1;
    e.gridy = 0;
    import1.addActionListener(this);
    import1.setActionCommand("Import1");
    Previous.setConstraints(import1,e);
    panel2.add(import1);

    JLabel name2 = new JLabel("Name");
    e.gridx = 0;
    e.gridy = 1;
    Previous.setConstraints(name2,e);
    panel2.add(name2);
    m_textPrevCertCN = new JTextField(26);
    m_textPrevCertCN.setEditable(false);
    e.gridx = 1;
    e.gridwidth = 3;
    e.weightx = 1;
    Previous.setConstraints(m_textPrevCertCN,e);
    panel2.add(m_textPrevCertCN);

    JLabel from2 = new JLabel("Valid From");
    e.gridx = 0;
    e.gridy = 2;
    e.gridwidth = 1;
    e.weightx = 0;
    Previous.setConstraints(from2,e);
    panel2.add(from2);
    m_textPrevCertValidFrom = new JTextField(26);
    m_textPrevCertValidFrom.setEditable(false);
    e.gridx = 1;
    e.gridwidth = 4;
    e.weightx = 1;
    Previous.setConstraints(m_textPrevCertValidFrom,e);
    panel2.add(m_textPrevCertValidFrom);

    JLabel to2 = new JLabel("Valid To");
    e.gridx = 0;
    e.gridy = 3;
    e.gridwidth = 1;
    e.weightx = 0;
    Previous.setConstraints(to2,e);
    panel2.add(to2);
    m_textPrevCertValidTo = new JTextField(26);
    m_textPrevCertValidTo.setEditable(false);
    e.gridx = 1;
    e.gridwidth = 4;
    e.weightx = 1;
    Previous.setConstraints(m_textPrevCertValidTo,e);
    panel2.add(m_textPrevCertValidTo);

    c.gridy = 2;
    panel3 = new JPanel(Next);
    GridBagConstraints f=new GridBagConstraints();
    f.anchor=GridBagConstraints.NORTHWEST;
    f.insets=new Insets(5,5,5,5);
    f.fill = GridBagConstraints.HORIZONTAL;
    panel3.setBorder(new TitledBorder("Next Mix Certificate"));
    layout.setConstraints(panel3,c);
    add(panel3);

    import2 = new JButton("Import...");
    f.gridx = 1;
    f.gridy = 0;
    import2.addActionListener(this);
    import2.setActionCommand("Import2");
    Next.setConstraints(import2,f);
    panel3.add(import2);

    JLabel name3 = new JLabel("Name");
    f.gridx = 0;
    f.gridy = 1;
    f.weightx = 0;
    Next.setConstraints(name3,f);
    panel3.add(name3);
    m_textNextCertCN = new JTextField(26);
    m_textNextCertCN.setEditable(false);
    f.gridx = 1;
    f.gridwidth = 5;
    f.weightx = 1;
    Next.setConstraints(m_textNextCertCN,f);
    panel3.add(m_textNextCertCN);

    JLabel from3 = new JLabel("Valid From");
    f.gridx = 0;
    f.gridy = 2;
    f.gridwidth = 1;
    f.weightx = 0;
    Next.setConstraints(from3,f);
    panel3.add(from3);
    m_textNextCertValidFrom = new JTextField(26);
    m_textNextCertValidFrom.setEditable(false);
    f.gridx = 1;
    f.gridwidth = 5;
    f.weightx = 1;
    Next.setConstraints(m_textNextCertValidFrom,f);
    panel3.add(m_textNextCertValidFrom);

    JLabel to3 = new JLabel("Valid To");
    f.gridx = 0;
    f.gridy = 3;
    f.gridwidth = 1;
    f.weightx = 0;
    Next.setConstraints(to3,f);
    panel3.add(to3);
    m_textNextCertValidTo = new JTextField(26);
    m_textNextCertValidTo.setEditable(false);
    f.gridx = 1;
    f.gridwidth = 5;
    f.weightx = 1;
    Next.setConstraints(m_textNextCertValidTo,f);
    panel3.add(m_textNextCertValidTo);
  }

    public void clear()
      {
	setOwnPrivCert(null,null);
	setPrevPubCert(null);
	setNextPubCert(null);
      }

    public byte[] getOwnPubCert()
      {
	return m_ownPubCert;
      }

    public byte[] getOwnPrivCert()
      {
	return m_ownPrivCert;
      }

    public void setOwnPrivCert(byte[] cert)
    {
	if(cert==null)
	  setOwnPrivCert(null,null);
	else
	{
	    char[] passwd=new char[] {};
	    while(passwd!=null&&!setOwnPrivCert(cert,passwd))
	    {
		PasswordBox pb=new PasswordBox(TheApplet.getMainWindow(),"Enter the password",PasswordBox.ENTER_PASSWORD);
		pb.show();
		passwd=pb.getPassword();
	    }
	}
    }

    public X509CertificateStructure readCertificate(byte[] cert)
	    throws IOException
    {
	ByteArrayInputStream bin=null;

	if(cert[0]!=(DERInputStream.SEQUENCE|DERInputStream.CONSTRUCTED))
	{
	    // Probably a Base64 encoded certificate
	    BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cert)));
	    StringBuffer sbuf = new StringBuffer();
	    String line;

	    while((line=in.readLine())!=null)
	    {
		if(line.equals("-----BEGIN CERTIFICATE-----") ||
		   line.equals("-----BEGIN X509 CERTIFICATE-----"))
		    break;
	    }

	    while((line=in.readLine())!=null)
	    {
		if(line.equals("-----END CERTIFICATE-----") ||
		   line.equals("-----END X509 CERTIFICATE-----"))
		    break;
		sbuf.append(line);
	    }
	    bin = new ByteArrayInputStream(Base64.decode(sbuf.toString()));
	}

	if(bin==null && cert[1]==0x80)
	{
	    // a BER encoded certificate
	    BERInputStream in = new BERInputStream(new ByteArrayInputStream(cert));
	    ASN1Sequence seq = (ASN1Sequence)in.readObject();
	    DERObjectIdentifier oid = (DERObjectIdentifier)seq.getObjectAt(0);
	    if(oid.equals(PKCSObjectIdentifiers.signedData))
		return new X509CertificateStructure(
			(ASN1Sequence)new SignedData((ASN1Sequence)((DERTaggedObject)seq.getObjectAt(1)).getObject()).getCertificates().getObjectAt(0));
	}
	else
	{
	    if(bin==null)
		bin = new ByteArrayInputStream(cert);
	    // DERInputStream
	    DERInputStream in = new DERInputStream(bin);
	    ASN1Sequence seq = (ASN1Sequence)in.readObject();
	    if(seq.size()>1 && seq.getObjectAt(1) instanceof DERObjectIdentifier &&
		seq.getObjectAt(0).equals(PKCSObjectIdentifiers.signedData))
	    {
		return X509CertificateStructure.getInstance(
			new SignedData(ASN1Sequence.getInstance((ASN1TaggedObject)seq.getObjectAt(1),true)).getCertificates().getObjectAt(0));
	    }
	    return X509CertificateStructure.getInstance(seq);
	}
	throw(new RuntimeException("Couldn't read certificate."));
    }

    private boolean setOwnPrivCert(byte[] cert, char[] passwd)
    {
	try
	{
	    if(cert!=null)
	    {
		/* -- This stuff with the light weight API --
		if(cert[0]!=(DERInputStream.SEQUENCE|DERInputStream.CONSTRUCTED))
		    throw(new RuntimeException("Not a PKCS 12 stream."));
		BERInputStream is = new BERInputStream(new ByteArrayInputStream(cert));
		DERConstructedSequence dcs = (DERConstructedSequence) is.readObject();
		Pfx pfx = new Pfx(dcs);
		ContentInfo cinfo = pfx.getAuthSafe();
		// Todo: Check MAC

		if(!cinfo.getContentType().equals(PKCSObjectIdentifiers.data))
		    throw(new RuntimeException("Does not contain any certificates."));

		is = new BERInputStream(new ByteArrayInputStream(
			((DEROctetString)cinfo.getContent()).getOctets()));
		ContentInfo[] cinfos = (new AuthenticatedSafe((DERConstructedSequence)is.readObject())).getContentInfo();
		for(int i=0;i<cinfos.length;i++)
		{
		    System.out.println(i+": "+cinfos[i].getContentType().getId());
		    DERConstructedSequence cseq;
		    if(cinfos[i].getContentType().equals(PKCSObjectIdentifiers.data))
		    {
			DERInputStream dis = new DERInputStream(new ByteArrayInputStream(
				((DEROctetString)cinfos[i].getContent()).getOctets()));
			cseq = (DERConstructedSequence)dis.readObject();
		    }
		    else if(cinfos[i].getContentType().equals(PKCSObjectIdentifiers.encryptedData))
		    {
			// EncryptedData ed = new EncryptedData((DERConstructedSequence)cinfos[i].getContent());
			// TODO: Decrypt this...
			continue;
		    }
		    else
			continue;

		    for(int j=0;j<cseq.getSize();j++)
		    {
			SafeBag sb = new SafeBag((DERConstructedSequence)cseq.getObjectAt(j));
			if(!sb.getBagId().equals(PKCSObjectIdentifiers.certBag))
			    continue;

			X509CertificateStructure c = readCertificate(((DEROctetString)new CertBag((ASN1Sequence)sb.getBagValue()).getCertValue()).getOctets());
			from_text1.setText(c.getStartDate().getDate().toString());
			to_text1.setText(c.getEndDate().getDate().toString());
			text1.setText(c.getSubject().toString());
			m_ownPrivCert=cert;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			new DEROutputStream(out).writeObject(c);
			m_ownPubCert=out.toByteArray();
			m_bttnExportOwnPub.setEnabled(true);
			m_bttnChangePasswd.setEnabled(true);
			return true;
		    }
		}
		throw(new RuntimeException("Didn't found anything."));
		*/
		KeyStore kstore = KeyStore.getInstance("PKCS12","BC");
		kstore.load(new ByteArrayInputStream(cert),passwd);
		X509Certificate c=(X509Certificate)kstore.getCertificate((String)kstore.aliases().nextElement());
		from_text1.setText(c.getNotBefore().toString());
		to_text1.setText(c.getNotAfter().toString());
		text1.setText(c.getSubjectDN().getName());
		m_ownPrivCert=cert;
		m_ownPubCert=c.getEncoded();
		m_bttnExportOwnPub.setEnabled(true);
		m_bttnChangePasswd.setEnabled(true);
	    }
	    else
	    {
		from_text1.setText(null);
		to_text1.setText(null);
		text1.setText(null);
		m_ownPrivCert=null;
		m_bttnExportOwnPub.setEnabled(false);
		m_bttnChangePasswd.setEnabled(false);
	    }
	}
	catch(Exception e)
	{
	    return false;
	}
	return true;
    }

    public byte[] getPrevPubCert()
    {
	return m_prevPubCert;
    }

    public void setPrevPubCert(byte[] cert)
    {
	try
	{
	    if(cert!=null)
	    {
		X509CertificateStructure cert1 = readCertificate(cert);
		m_textPrevCertCN.setText(cert1.getSubject().toString());
		m_textPrevCertValidFrom.setText(cert1.getStartDate().getDate().toString());
		m_textPrevCertValidTo.setText(cert1.getEndDate().getDate().toString());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new DEROutputStream(out).writeObject(cert1);
		m_prevPubCert=out.toByteArray();

		/*
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate cert1 = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(cert));
		m_textPrevCertCN.setText(cert1.getSubjectDN().getName());
		m_textPrevCertValidFrom.setText(cert1.getNotBefore().toString());
		m_textPrevCertValidTo.setText(cert1.getNotAfter().toString());
		m_prevPubCert=cert1.getEncoded();
		*/
	    }
	    else
	    {
		m_textPrevCertCN.setText(null);
		m_textPrevCertValidFrom.setText(null);
		m_textPrevCertValidTo.setText(null);
		m_prevPubCert=null;
	    }
	}
	catch(Exception e)
	{
	    System.out.println("Prev Cert not set: "+e.getMessage());
	    setPrevPubCert(null);
	}
    }

    public byte[] getNextPubCert()
      {
	return m_nextPubCert;
      }

    public void setNextPubCert(byte[] cert)
      {
	try
	  {
	    if(cert!=null)
	      {
		X509CertificateStructure cert1 = readCertificate(cert);
		m_textNextCertCN.setText(cert1.getSubject().toString());
		m_textNextCertValidFrom.setText(cert1.getStartDate().getDate().toString());
		m_textNextCertValidTo.setText(cert1.getEndDate().getDate().toString());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new DEROutputStream(out).writeObject(cert1);
		m_nextPubCert=out.toByteArray();
		/*
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate cert1 = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(cert));
		m_textNextCertCN.setText(cert1.getSubjectDN().getName());
		m_textNextCertValidFrom.setText(cert1.getNotBefore().toString());
		m_textNextCertValidTo.setText(cert1.getNotAfter().toString());
		m_nextPubCert=cert1.getEncoded();
		*/
	      }
	    else
	      {
		m_textNextCertCN.setText(null);
		m_textNextCertValidFrom.setText(null);
		m_textNextCertValidTo.setText(null);
		m_nextPubCert=null;
	      }
	  }
	catch(Exception e)
	  {
	    System.out.println("Next Cert not set: "+e.getMessage());
	    setNextPubCert(null);
	  }
      }

      class DateTextField extends JPanel
      {
	  private JTextField hour,min,sec,day,year;
	  private JComboBox month;

	  class DayDocument extends PlainDocument
	  {
	      Component which;

	      private final int daysPerMonth[] =
		  { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

	      DayDocument(Component comp)
	      {
		  super();
		  which = comp;
	      }

	      DayDocument()
	      {
		  super();
		  which = null;
	      }

	      public void insertString(int offset, String str, AttributeSet attr)
		      throws BadLocationException
	      {
		  String p1 = getText(0,offset);
		  String p2 = getText(offset, getLength()-offset);
		  String res = "";

		  int max = (month==null)?0:(month.getSelectedIndex());
		  if(max==1)
		  {
		      int y = Integer.parseInt(year.getText());
		      if((y%4)==0 && ((y%100)!=0 || (y%400)==0))
			  max = 29;
		      else
			  max = 28;
		  }
		  else
		      max = daysPerMonth[max];

		  for(int i=0;i<str.length();i++)
		      if(!Character.isDigit(str.charAt(i)))
			  java.awt.Toolkit.getDefaultToolkit().beep();
		      else
		      {
			  String sstr = str.substring(i,i+1);
			  int val = Integer.parseInt(p1+res+sstr+p2,10);
			  if(max>0 && val>max)
			      java.awt.Toolkit.getDefaultToolkit().beep();
			  else
			      res+=sstr;
		      }
		  super.insertString(offset,res,attr);
		  if(which!=null && max>0 && getLength()>0 &&
		     10*Integer.parseInt(getText(0,getLength()),10)>max)
		      which.transferFocus();
	      }
	  }

	  private void initDateTextField(Date date)
	  {
	      Calendar cal = Calendar.getInstance();
	      cal.setTime(date);
	      JLabel label;

	      GridBagLayout layout = new GridBagLayout();
	      setLayout(layout);

	      GridBagConstraints gbc=new GridBagConstraints();
	      gbc.anchor = GridBagConstraints.WEST;
	      gbc.fill = GridBagConstraints.HORIZONTAL;
	      gbc.insets = new Insets(5,5,5,5);
	      gbc.gridx = 0;
	      gbc.gridy = 0;

	      day = new JTextField(2);
	      day.setMinimumSize(day.getPreferredSize());
	      day.setDocument(new DayDocument(day));
	      day.setText(Integer.toString(cal.get(cal.DAY_OF_MONTH)));
	      gbc.weightx = 1;
	      gbc.insets.right = 1;
	      layout.setConstraints(day,gbc);
	      add(day);
	      gbc.gridx++;
	      label = new JLabel(".");
	      gbc.weightx = 0;
	      gbc.insets.right = 5;
	      gbc.insets.left = 1;
	      layout.setConstraints(label,gbc);
	      add(label);
	      gbc.gridx++;
	      gbc.insets.left = 5;

	      month = new JComboBox(new String[]
		  { "January", "February", "March", "April",
		    "May", "June", "July", "August",
		    "September", "Oktober", "November", "December" });
	      month.setKeySelectionManager(new JComboBox.KeySelectionManager()
	      {
		  public int selectionForKey(char key, javax.swing.ComboBoxModel cbm)
		  {
		      int nr = key-'0';
		      if(nr<0 || nr>9)
			  return -1;

		      if(nr<3 && month.getSelectedIndex()==0)
			  nr = 10+nr;

		      return nr-1;
		  }
	      });
	      month.setSelectedIndex(cal.get(cal.MONTH));
	      gbc.weightx = 1;
	      layout.setConstraints(month,gbc);
	      add(month);
	      gbc.gridx++;

	      year = new JTextField(4);
	      year.setMinimumSize(year.getPreferredSize());
	      year.setDocument(new IntegerDocument(year));
	      year.setText(Integer.toString(cal.get(cal.YEAR)));
	      gbc.weightx = 1;
	      layout.setConstraints(year,gbc);
	      add(year);
	      gbc.gridx++;

	      hour = new JTextField(2);
	      hour.setMinimumSize(day.getPreferredSize());
	      hour.setDocument(new IntegerDocument(23,hour));
	      hour.setText(Integer.toString(cal.get(cal.HOUR)));
	      gbc.weightx = 1;
	      gbc.insets.right = 1;
	      layout.setConstraints(hour,gbc);
	      add(hour);
	      gbc.gridx++;
	      label = new JLabel(":");
	      gbc.weightx = 0;
	      gbc.insets.left = 1;
	      layout.setConstraints(label,gbc);
	      add(label);
	      gbc.gridx++;
	      min = new JTextField(2);
	      min.setMinimumSize(day.getPreferredSize());
	      min.setDocument(new IntegerDocument(59,min));
	      min.setText(Integer.toString(cal.get(cal.MINUTE)));
	      gbc.weightx = 1;
	      layout.setConstraints(min,gbc);
	      add(min);
	      gbc.gridx++;
	      label = new JLabel(":");
	      gbc.weightx = 0;
	      layout.setConstraints(label,gbc);
	      add(label);
	      gbc.gridx++;
	      sec = new JTextField(2);
	      sec.setMinimumSize(day.getPreferredSize());
	      sec.setDocument(new IntegerDocument(59,sec));
	      sec.setText(Integer.toString(cal.get(cal.SECOND)));
	      gbc.weightx = 1;
	      layout.setConstraints(sec,gbc);
	      add(sec);
	  }

	  public DateTextField(Date date)
	  {
	      super();
	      initDateTextField(date);
	  }

	  public void setDate(Date date)
	  {
	      Calendar cal = Calendar.getInstance();
	      cal.setTime(date);
	      day.setText(Integer.toString(cal.get(cal.DAY_OF_MONTH)));
	      month.setSelectedIndex(cal.get(cal.MONTH));
	      year.setText(Integer.toString(cal.get(cal.YEAR)));
	      hour.setText(Integer.toString(cal.get(cal.HOUR)));
	      min.setText(Integer.toString(cal.get(cal.MINUTE)));
	      sec.setText(Integer.toString(cal.get(cal.SECOND)));
	  }

	  public Date getDate()
	  {
	      Calendar cal = Calendar.getInstance();
	      cal.set(
		      Integer.parseInt(year.getText()),
		      month.getSelectedIndex(),
		      Integer.parseInt(day.getText()),
		      Integer.parseInt(hour.getText()),
		      Integer.parseInt(min.getText()),
		      Integer.parseInt(sec.getText()));
	      return cal.getTime();
	  }
      }

    private class ValidityDialog extends JDialog
    {
	public DateTextField from, to;

	protected void createValidityDialog()
	{
	    GridBagLayout layout=new GridBagLayout();
	    getContentPane().setLayout(layout);

	    // Constraints for the labels
	    GridBagConstraints gbc=new GridBagConstraints();
	    gbc.anchor =GridBagConstraints.WEST;
	    gbc.insets = new Insets(5,5,5,5);
	    gbc.gridy = 0;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    JLabel label;

	    label = new JLabel("Valid from:");
	    gbc.gridx = 0;
	    gbc.weightx = 0;
	    layout.setConstraints(label, gbc);
	    getContentPane().add(label);
	    gbc.gridx = 1;
	    gbc.weightx = 5;
	    Date now = new Date(System.currentTimeMillis());
	    from = new DateTextField(now);
	    layout.setConstraints(from,gbc);
	    getContentPane().add(from);
	    gbc.gridx = 2;
	    gbc.weightx = 1;
	    JButton nowButton = new JButton("Now");
	    nowButton.setActionCommand("Now");
	    nowButton.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ev)
		{
		    if(ev.getActionCommand().equals("Now"))
		    {
			from.setDate(new Date(System.currentTimeMillis()));
		    }
		}
	    });
	    layout.setConstraints(nowButton,gbc);
	    getContentPane().add(nowButton);
	    gbc.gridy++;

	    label = new JLabel("Valid to:");
	    gbc.gridx = 0;
	    gbc.weightx = 0;
	    layout.setConstraints(label, gbc);
	    getContentPane().add(label);
	    gbc.gridx = 1;
	    gbc.weightx = 5;

	    Calendar cal = Calendar.getInstance();
	    cal.setTime(now);
	    cal.add(cal.YEAR, 1);
	    to = new DateTextField(cal.getTime());
	    layout.setConstraints(to,gbc);
	    getContentPane().add(to);
	    gbc.gridx = 2;
	    gbc.weightx = 1;
	    JButton y1Button = new JButton("1 Year");
	    y1Button.setActionCommand("1 Year");
	    y1Button.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ev)
		{
		    if(ev.getActionCommand().equals("1 Year"))
		    {
			Calendar cal = Calendar.getInstance();
			cal.setTime(from.getDate());
			cal.add(cal.YEAR, 1);
			to.setDate(cal.getTime());
		    }
		}
	    });
	    layout.setConstraints(y1Button,gbc);
	    getContentPane().add(y1Button);
	    gbc.gridy++;

	    GridBagLayout keylayout = new GridBagLayout();
	    JPanel keys = new JPanel(keylayout);
	    GridBagConstraints kc = new GridBagConstraints();
	    kc.weightx=1;
	    kc.gridx=0;
	    kc.gridy=0;
	    kc.gridwidth=1;
	    kc.fill=GridBagConstraints.HORIZONTAL;
	    kc.insets = new Insets(1,1,1,1);
	    JButton key = new JButton("OK");
	    key.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ev)
		{
		    dispose();
		}
	    });
	    keylayout.setConstraints(key,kc);
	    keys.add(key);
	    kc.gridx++;
	    key = new JButton("Cancel");
	    key.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ev)
		{
		    from = null;
		    to = null;
		    dispose();
		}
	    });
	    keylayout.setConstraints(key,kc);
	    keys.add(key);

	    gbc.gridx=0;
	    gbc.gridwidth=3;
	    gbc.fill=GridBagConstraints.HORIZONTAL;
	    layout.setConstraints(keys,gbc);
	    getContentPane().add(keys);

	    pack();
	}

	ValidityDialog(Frame parent, String title)
	{
	    super(parent, title, true);
	    createValidityDialog();
	    setLocationRelativeTo(parent);
	}
    }

    public void generateNewCert()
    {
      String oMixid=MyFrame.m_GeneralPanel.getMixID();

      if(oMixid==null || oMixid.length()==0)
      {
	 javax.swing.JOptionPane.showMessageDialog(this,
		 MyFrame.m_GeneralPanel.getMixAuto().equals("True")?
		 "Please enter main incoming connection in the network panel.":
		 "Please enter Mix ID in general panel.",
		 "No Mix ID!", javax.swing.JOptionPane.ERROR_MESSAGE);
	 return;
      }

      final ValidityDialog vdialog = new ValidityDialog(TheApplet.getMainWindow(), "Validity");
      vdialog.show();
      if(vdialog.from==null)
	  return;
      PasswordBox dialog = new PasswordBox(TheApplet.getMainWindow(),"New Password",PasswordBox.NEW_PASSWORD);
      dialog.show();
      final char[] passwd=dialog.getPassword();
      if(passwd==null)
	return;
      final String mixid=URLEncoder.encode(oMixid);

      final BusyWindow waitWindow = new BusyWindow( TheApplet.getMainWindow(),
	      "Generating Key Pair.");

      SwingWorker worker = new SwingWorker()
      {
	  public Object construct()
	  {
	      V3TBSCertificateGenerator v3certgen=new V3TBSCertificateGenerator();
	      v3certgen.setStartDate(new DERUTCTime(vdialog.from.getDate()));
	      v3certgen.setEndDate(new DERUTCTime(vdialog.to.getDate()));
	      v3certgen.setIssuer(new X509Name("CN=<Mix id=\""+mixid+"\"/>"));
	      v3certgen.setSubject(new X509Name("CN=<Mix id=\""+mixid+"\"/>"));
	      v3certgen.setSerialNumber(new DERInteger(1));

	      try
	      {
		  SecureRandom random = new SecureRandom();
		  DSAParametersGenerator pGen = new DSAParametersGenerator();
		  DSAKeyPairGenerator kpGen = new DSAKeyPairGenerator();
		  pGen.init(1024,20,random);
		  kpGen.init(new DSAKeyGenerationParameters(random, pGen.generateParameters()));
		  final AsymmetricCipherKeyPair ackp = kpGen.generateKeyPair();
		  DSAParameters dsaPars = ((DSAPrivateKeyParameters)ackp.getPrivate()).getParameters();
		  final DSAParams dsaSpec = new DSAParameterSpec(dsaPars.getP(), dsaPars.getQ(), dsaPars.getG());
		  final DERObject derParam = new DSAParameter(dsaPars.getP(), dsaPars.getQ(), dsaPars.getG()).getDERObject();
		  KeyPair kp = new KeyPair(
		      new DSAPublicKey()
		      {
			  public BigInteger getY() { return ((DSAPublicKeyParameters)ackp.getPublic()).getY(); }
			  public DSAParams getParams() { return dsaSpec; }
			  public String getAlgorithm() { return "DSA"; }
			  public String getFormat() { return "X.509"; }
			  public byte[] getEncoded()
			  {
			      ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			      DEROutputStream dOut = new DEROutputStream(bOut);
			      try
			      {
				  dOut.writeObject(
				      new SubjectPublicKeyInfo(
					  new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, derParam),
					  new DERInteger(getY())));
				  dOut.close();
			      }
			      catch(IOException e)
			      {
				  throw new RuntimeException("IOException while encoding public key");
			      }
			      return bOut.toByteArray();
			  }
		      },
		      new DSAPrivateKey()
		      {
			  public BigInteger getX() { return ((DSAPrivateKeyParameters)ackp.getPrivate()).getX(); }
			  public DSAParams getParams() { return dsaSpec; }
			  public String getAlgorithm() { return "DSA"; }
			  public String getFormat() { return "PKCS#8"; }
			  public byte[] getEncoded()
			  {
			      ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			      DEROutputStream dOut = new DEROutputStream(bOut);
			      try
			      {
				  dOut.writeObject(
				      new PrivateKeyInfo(
					  new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, derParam),
					  new DERInteger(getX())));
				  dOut.close();
			      }
			      catch(IOException e)
			      {
				  throw new RuntimeException("IOException while encoding private key");
			      }
			      return bOut.toByteArray();
			  }
		      });

		  /*
		  KeyPairGenerator kpg=KeyPairGenerator.getInstance("DSA");
		  kpg.initialize(1024);
		  KeyPair kp=kpg.generateKeyPair();
		  */

		  v3certgen.setSubjectPublicKeyInfo( new SubjectPublicKeyInfo((DERConstructedSequence)new DERInputStream(
						    new ByteArrayInputStream(kp.getPublic().getEncoded())).readObject()));
		  AlgorithmIdentifier algID=new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa_with_sha1);
		  v3certgen.setSignature(algID);

		  DSASigner signer = new DSASigner();
		  SHA1Digest digest = new SHA1Digest();
		  signer.init(true, ackp.getPrivate());

		  /*
		  Signature sig = Signature.getInstance("DSA");
		  sig.initSign(kp.getPrivate());
		  */

		  TBSCertificateStructure tbsCert = v3certgen.generateTBSCertificate();
		  ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
		  (new DEROutputStream(bOut)).writeObject(tbsCert);
		  digest.update(bOut.toByteArray(),0,bOut.size());
		  byte[] hash = new byte[digest.getDigestSize()];
		  digest.doFinal(hash, 0);
		  BigInteger[] sig = signer.generateSignature(hash);
		  DEREncodableVector sigv = new DEREncodableVector();
		  sigv.add(new DERInteger(sig[0]));
		  sigv.add(new DERInteger(sig[1]));
		  DERConstructedSequence seq=new DERConstructedSequence();
		  seq.addObject(tbsCert);
		  seq.addObject(algID);
		  seq.addObject(new DERBitString(new DERSequence(sigv)));
		  X509CertificateStructure x509cert=new  X509CertificateStructure(seq);

		  //PKCS12 generation
		  JDKPKCS12KeyStore store = new JDKPKCS12KeyStore(null);
		  X509CertificateObject certobj=new X509CertificateObject(x509cert);
		  Certificate[] chain=new Certificate[] {certobj};

		  store.engineSetKeyEntry("<Mix id=\""+mixid+"\"/>",(Key) kp.getPrivate(), null, chain, kp.getPublic());
		  ByteArrayOutputStream out=new ByteArrayOutputStream();
		  store.engineStore(out,passwd,kp.getPublic());
		  out.close();
		  return out.toByteArray();
	      }
	      catch(Exception e)
	      {
		  if(Thread.interrupted())
		      return null;
		   System.out.println("Error in Key generation and storage!!");
		   e.printStackTrace();
	      }
	      return null;
	  }

	  public void finished()
	  {
	      Object cert = get();
	      if(cert!=null)
		  setOwnPrivCert((byte[])cert, passwd);
	      waitWindow.dispose();
	  }
      };
      waitWindow.setSwingWorker(worker);
      worker.start();
    }

  public void actionPerformed(ActionEvent ae)
  {
    if(ae.getActionCommand().equals("Create"))
    {
      generateNewCert();
    }

    else if(ae.getActionCommand().equals("passwd"))
    {
	PasswordBox dialog = new PasswordBox(TheApplet.getMainWindow(),"Change Password",PasswordBox.CHANGE_PASSWORD);
	while(true)
	{
	    dialog.show();
	    char[] passwd=dialog.getPassword();
	    char[] oldpasswd=dialog.getOldPassword();
	    if(passwd==null)
		break;
	    try
	    {
		KeyStore kstore = KeyStore.getInstance("PKCS12","BC");
		kstore.load(new ByteArrayInputStream(m_ownPrivCert),oldpasswd);
		String alias=(String)kstore.aliases().nextElement();
		Key privKey=kstore.getKey(alias,null);
		Certificate[] chain=kstore.getCertificateChain(alias);
		kstore.setKeyEntry(alias,privKey,passwd, chain);
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		kstore.store(out, passwd);
		setOwnPrivCert(out.toByteArray(),passwd);
		break;
	    }
	    catch(Exception e)
	    {
		javax.swing.JOptionPane.showMessageDialog(this,
			"Wrong Password.", "Password Error",
			javax.swing.JOptionPane.ERROR_MESSAGE);
	    }
	}
      }
    else if (ae.getActionCommand().equalsIgnoreCase("ExportOwnPubCert"))
      {
	JFileChooser fd=TheApplet.showFileDialog(TheApplet.SAVE_DIALOG,TheApplet.FILTER_CER|TheApplet.FILTER_B64_CER|TheApplet.FILTER_PFX);
	File file=fd.getSelectedFile();
	if(file!=null)
	  {
	    String fname = file.getName();
	    if(fname.indexOf('.')<0)
		switch(((SimpleFileFilter)fd.getFileFilter()).getFilterType())
		{
		    case TheApplet.FILTER_PFX:
			file = new File(file.getParent(), fname + ".pfx");
			break;
		    case TheApplet.FILTER_CER:
			file = new File(file.getParent(), fname + ".der.cer");
			break;
		    case TheApplet.FILTER_B64_CER:
			file = new File(file.getParent(), fname + ".b64.cer");
			break;
		}
	    try
	      {
		FileOutputStream fout=new FileOutputStream(file);
		switch(((SimpleFileFilter)fd.getFileFilter()).getFilterType())
		{
		    case TheApplet.FILTER_PFX:
			fout.write(getOwnPrivCert());
			break;
		    case TheApplet.FILTER_CER:
			fout.write(getOwnPubCert());
			break;
		    case TheApplet.FILTER_B64_CER:
			fout.write("-----BEGIN CERTIFICATE-----\n".getBytes());
			fout.write(Base64.encodeBytes(getOwnPubCert()).getBytes());
			fout.write("\n-----END CERTIFICATE-----\n".getBytes());
			break;
		}
		fout.close();
	      }
	    catch(Exception e)
	      {
	      }
	  }
      }
    else if(ae.getActionCommand().equals("ImportOwnCert"))
      {
	byte[] buff=openFile(TheApplet.FILTER_PFX);
	if(buff!=null)
	  setOwnPrivCert(buff);
      }
    else if(ae.getActionCommand().equals("Import1"))
      setPrevPubCert(openFile(TheApplet.FILTER_CER));
    else if(ae.getActionCommand().equals("Import2"))
      setNextPubCert(openFile(TheApplet.FILTER_CER));
  }



    private byte[] openFile(int type)
      {
	File file=TheApplet.showFileDialog(TheApplet.OPEN_DIALOG,type).getSelectedFile();
	if(file != null)
	  {
	    try
	      {
		byte[] buff=new byte[(int)file.length()];
		FileInputStream fin = new FileInputStream(file);
		fin.read(buff);
		fin.close();
		return buff;
	      }
	    catch(Exception e)
	      {
		System.out.println("Error reading: "+file);
		return null;
	      }
	  }
	return null;
      }
  }

class BusyWindow extends javax.swing.JWindow implements ActionListener
{
    private SwingWorker sw;

    public BusyWindow(Frame parent, String reason)
    {
	super(parent);
	JPanel p = new JPanel();
	p.setBorder(BorderFactory.createRaisedBevelBorder());
	getContentPane().add(p);

	GridBagLayout layout=new GridBagLayout();
	p.setLayout(layout);

	// Constraints for the labels
	GridBagConstraints gbc=new GridBagConstraints();
	gbc.anchor =GridBagConstraints.CENTER;
	gbc.insets = new Insets(5,5,5,5);
	gbc.gridy = 0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	JComponent label = new JLabel(reason);
	gbc.gridx = 0;
	gbc.weightx = 0;
	layout.setConstraints(label, gbc);
	p.add(label);
	gbc.gridy++;
	label = new JLabel("Please wait.");
	layout.setConstraints(label, gbc);
	p.add(label);
	gbc.gridy++;
	final java.awt.Image img = Toolkit.getDefaultToolkit().getImage("busy.gif");
	MediaTracker mt = new MediaTracker(this);
	mt.addImage(img,1);
	try { mt.waitForAll(); } catch(Exception e) {}
	JComponent imgComp = new JPanel()
	    {
		public void paintComponent(Graphics g)
		{
		    g.drawImage(img,0,0, this);
//                    super.paint(g);
		}

		public Dimension getPreferredSize()
		{
		    return new Dimension(img.getWidth(null), img.getHeight(null));
		}
	    };
	layout.setConstraints(imgComp, gbc);
	p.add(imgComp);
/* Funktioniert nicht. Der Abbruch wird irgendwo abgefangen.
	gbc.gridy++;
	JButton button = new JButton("Cancel");
	button.addActionListener(this);
	layout.setConstraints(button, gbc);
	p.add(button);
*/
	this.pack();
	Dimension d = parent.getSize();
	Dimension d2 = this.getSize();
	Point l = parent.getLocation();
	this.setLocation(l.x+(d.width-d2.width)/2, l.y+(d.height-d2.height)/2);
	this.setVisible(true);
    }

    public void update(Graphics g)
    {
       paint(g);
       System.out.print("Update called\n");
    }

    public void setSwingWorker(SwingWorker s)
    {
	sw = s;
    }

    public void actionPerformed(ActionEvent ev)
    {
	if(sw!=null)
	    sw.interrupt();
    }
}
