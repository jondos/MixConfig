package mixconfig;
import java.util.*;
import java.net.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.Font.*;
import java.io.*;
import javax.swing.*;
import java.applet.*;
import java.lang.Object;
import java.math.*;
import java.net.URLEncoder;
import javax.swing.BorderFactory;
import javax.swing.table.*;
import javax.swing.event.*;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JFrame;

import javax.swing.border.TitledBorder;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.security.*;
import org.bouncycastle.util.encoders.*;
import org.bouncycastle.jce.X509V3CertificateGenerator;
import org.bouncycastle.jce.provider.*;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.crypto.BlockCipher;

class CertificatesPanel extends JPanel implements ActionListener
  {
    JPanel panel1,panel2,panel3;
    JTextField text1,from_text1,to_text1;
    JTextField text2,from_text2,to_text2;
    JTextField text3,from_text3,to_text3;
    JButton import1,import2,create;

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
    panel1.setBorder(BorderFactory.createTitledBorder("Own Mix Certificate"));
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
    d.fill = GridBagConstraints.HORIZONTAL;
    create.addActionListener(this);
    create.setActionCommand("Create");
    Own.setConstraints(create,d);
    panel1.add(create);
    JButton passwd = new JButton("Change Password");
    d.gridx = 3;
    passwd.addActionListener(this);
    passwd.setActionCommand("passwd");
    Own.setConstraints(passwd,d);
    panel1.add(passwd);

    d.gridx = 0;
    d.gridy = 1;
    d.fill = GridBagConstraints.HORIZONTAL;
    JLabel name1 = new JLabel("Name");
    Own.setConstraints(name1,d);
    panel1.add(name1);
    text1 = new JTextField(20);
    d.gridx = 1;
    d.gridwidth = 3;
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
    from_text1 = new JTextField(20);
    d.gridx = 1;
    d.gridwidth = 3;
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
    to_text1 = new JTextField(20);
    d.gridx = 1;
    d.gridwidth = 3;
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
    panel2.setBorder(BorderFactory.createTitledBorder("Previous Mix Certificate"));
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
    text2 = new JTextField(26);
    e.gridx = 1;
    e.gridwidth = 3;
    e.weightx = 1;
    Previous.setConstraints(text2,e);
    panel2.add(text2);

    JLabel from2 = new JLabel("Valid From");
    e.gridx = 0;
    e.gridy = 2;
    e.gridwidth = 1;
    e.weightx = 0;
    Previous.setConstraints(from2,e);
    panel2.add(from2);
    from_text2 = new JTextField(26);
    e.gridx = 1;
    e.gridwidth = 4;
    e.weightx = 1;
    Previous.setConstraints(from_text2,e);
    panel2.add(from_text2);

    JLabel to2 = new JLabel("Valid To");
    e.gridx = 0;
    e.gridy = 3;
    e.gridwidth = 1;
    e.weightx = 0;
    Previous.setConstraints(to2,e);
    panel2.add(to2);
    to_text2 = new JTextField(26);
    e.gridx = 1;
    e.gridwidth = 4;
    e.weightx = 1;
    Previous.setConstraints(to_text2,e);
    panel2.add(to_text2);

    c.gridy = 2;
    panel3 = new JPanel(Next);
    GridBagConstraints f=new GridBagConstraints();
    f.anchor=GridBagConstraints.NORTHWEST;
    f.insets=new Insets(5,5,5,5);
    f.fill = GridBagConstraints.HORIZONTAL;
    panel3.setBorder(BorderFactory.createCompoundBorder(
		BorderFactory.createTitledBorder("Next Mix Certificate"),
		BorderFactory.createEmptyBorder(0,0,0,0)));
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
    text3 = new JTextField(26);
    f.gridx = 1;
    f.gridwidth = 5;
    f.weightx = 1;
    Next.setConstraints(text3,f);
    panel3.add(text3);

    JLabel from3 = new JLabel("Valid From");
    f.gridx = 0;
    f.gridy = 2;
    f.gridwidth = 1;
    f.weightx = 0;
    Next.setConstraints(from3,f);
    panel3.add(from3);
    from_text3 = new JTextField(26);
    f.gridx = 1;
    f.gridwidth = 5;
    f.weightx = 1;
    Next.setConstraints(from_text3,f);
    panel3.add(from_text3);

    JLabel to3 = new JLabel("Valid To");
    f.gridx = 0;
    f.gridy = 3;
    f.gridwidth = 1;
    f.weightx = 0;
    Next.setConstraints(to3,f);
    panel3.add(to3);
    to_text3 = new JTextField(26);
    f.gridx = 1;
    f.gridwidth = 5;
    f.weightx = 1;
    Next.setConstraints(to_text3,f);
    panel3.add(to_text3);
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

    public void setOwnPrivCert(byte[] cert, char[] passwd)
      {
        try
          {
            if(cert!=null)
              {
                KeyStore kstore = KeyStore.getInstance("PKCS12","BC");
                kstore.load(new ByteArrayInputStream(cert),passwd);
                X509Certificate c=(X509Certificate)kstore.getCertificate((String)kstore.aliases().nextElement());
                from_text1.setText(c.getNotBefore().toString());
                to_text1.setText(c.getNotAfter().toString());
                text1.setText(c.getSubjectDN().getName());
                m_ownPrivCert=cert;
                m_ownPubCert=c.getEncoded();
              }
            else
              {
                from_text1.setText(null);
                to_text1.setText(null);
                text1.setText(null);
                m_ownPrivCert=null;
              }
          }
        catch(Exception e)
          {
            System.out.println("Own Cert not set: "+e.getMessage());
            setOwnPrivCert(null,null);
          }
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
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert1 = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(cert));
                text2.setText(cert1.getSubjectDN().getName());
                from_text2.setText(cert1.getNotBefore().toString());
                to_text2.setText(cert1.getNotAfter().toString());
                m_prevPubCert=cert1.getEncoded();
              }
            else
              {
                from_text2.setText(null);
                text2.setText(null);
                to_text2.setText(null);
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
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert1 = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(cert));
                text3.setText(cert1.getSubjectDN().getName());
                from_text3.setText(cert1.getNotBefore().toString());
                to_text3.setText(cert1.getNotAfter().toString());
                m_nextPubCert=cert1.getEncoded();
              }
            else
              {
                from_text3.setText(null);
                text3.setText(null);
                to_text3.setText(null);
                m_nextPubCert=null;
              }
          }
        catch(Exception e)
          {
            System.out.println("Next Cert not set: "+e.getMessage());
            setNextPubCert(null);
          }
      }

/*     public void setName1(String name)
     {
       text1.setText(name);
     }
     public void setFrom1(String from)
     {
       from_text1.setText(from);
     }
     public void setTo1(String to)
     {
       to_text1.setText(to);
     }

     public void setName2(String name)
     {
       text2.setText(name);
     }
     public void setFrom2(String from)
     {
       from_text2.setText(from);
     }
     public void setTo2(String to)
     {
       to_text2.setText(to);
     }

     public void setName3(String name)
     {
       text3.setText(name);
     }
     public void setFrom3(String from)
     {
       from_text3.setText(from);
     }
     public void setTo3(String to)
     {
       to_text3.setText(to);
     }
*/
    public void generateNewCert()
    {
      PasswordBox dialog = new PasswordBox(TheApplet.myFrame,"New Password",PasswordBox.NEW_PASSWORD);
      dialog.show();
      char[] passwd=dialog.getPassword();
      if(passwd==null)
        return;
      String mixid = MyFrame.m_GeneralPanel.getMixID();
      mixid=URLEncoder.encode(mixid);
      X509V3CertificateGenerator gen=new X509V3CertificateGenerator();
      gen.setSignatureAlgorithm("DSAWITHSHA1");
      gen.setNotBefore(new Date(System.currentTimeMillis()));
      gen.setNotAfter(new Date(System.currentTimeMillis()+3600*1000*24*365 ));

      gen.setIssuerDN(new X509Name("CN=<Mix id=\""+mixid+"\"/>"));
      gen.setSubjectDN(new X509Name("CN=<Mix id=\""+mixid+"\"/>"));
      gen.setSerialNumber(new BigInteger("1"));

      try
      {
        KeyPairGenerator kpg=KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        KeyPair kp=kpg.generateKeyPair();
        gen.setPublicKey(kp.getPublic());
        X509Certificate cert=gen.generateX509Certificate(kp.getPrivate());
        //m_ownPubCert=cert.getEncoded();

        //PKCS12 generation
        KeyStore kstore = KeyStore.getInstance("PKCS12","BC");
        kstore.load(null, null);

        Certificate[] chain=new  Certificate[1];
        chain[0] = cert;
        kstore.setKeyEntry("<Mix id=\""+mixid+"\"/>",(Key)kp.getPrivate(),(char[])null, chain);
        ByteArrayOutputStream out=new ByteArrayOutputStream();

        //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        //passwd = br.readLine();
        kstore.store(out, passwd);
        setOwnPrivCert(out.toByteArray(),passwd);
      }
      catch(Exception e)
      {
        System.out.println("Error in Key generation and storage!!");
        e.printStackTrace();
      }
    }

  public void actionPerformed(ActionEvent ae)
  {

    if(ae.getActionCommand().equals("Create"))
    {
      generateNewCert();
    }

    else if(ae.getActionCommand().equals("passwd"))
      {
        PasswordBox dialog = new PasswordBox(TheApplet.myFrame,"Change Password",PasswordBox.CHANGE_PASSWORD);
        dialog.setVisible(true);
      }

    else if(ae.getActionCommand().equals("Import1"))
      setPrevPubCert(openFile());
    else if(ae.getActionCommand().equals("Import2"))
      setNextPubCert(openFile());
  }



    private byte[] openFile()
      {
        FileDialog fd = new FileDialog(TheApplet.myFrame,"Open File",FileDialog.LOAD);
        fd.show();
        String myFile = fd.getDirectory()+fd.getFile();
        if(fd.getFile() != null)
          {
            try
              {
                File file=new File(myFile);
                byte[] buff=new byte[(int)file.length()];
                FileInputStream fin = new FileInputStream(file);
                fin.read(buff);
                fin.close();
                return buff;
              }
            catch(Exception e)
              {
                System.out.println("Error reading: "+myFile);
                return null;
              }
          }
        return null;
      }
  }