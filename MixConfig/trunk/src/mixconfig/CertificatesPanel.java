package mixconfig;
import java.util.Date;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.math.BigInteger;

import java.io.*;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import java.net.URLEncoder;


import javax.swing.border.TitledBorder;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.security.*;
import org.bouncycastle.jce.X509V3CertificateGenerator;
import org.bouncycastle.jce.provider.*;
import org.bouncycastle.asn1.x509.X509Name;

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
    d.fill = GridBagConstraints.HORIZONTAL;
    create.addActionListener(this);
    create.setActionCommand("Create");
    Own.setConstraints(create,d);
    panel1.add(create);
    JButton bttn = new JButton("Import...");
    d.gridx = 2;
    d.gridy = 0;
    d.gridwidth = 1;
    d.fill = GridBagConstraints.HORIZONTAL;
    bttn.addActionListener(this);
    bttn.setActionCommand("ImportOwnCert");
    Own.setConstraints(bttn,d);
    panel1.add(bttn);
    m_bttnExportOwnPub = new JButton("Export...");
    d.gridx = 3;
    d.gridy = 0;
    d.gridwidth = 1;
    d.fill = GridBagConstraints.HORIZONTAL;
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
            char[] passwd="".toCharArray();
            while(passwd!=null&&!setOwnPrivCert(cert,passwd))
              {
                PasswordBox pb=new PasswordBox(TheApplet.getMainWindow(),"Enter the password",PasswordBox.ENTER_PASSWORD);
                pb.show();
                passwd=pb.getPassword();
              }
           }
      }

    private boolean setOwnPrivCert(byte[] cert, char[] passwd)
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
            System.out.println("Own Cert not set: "+e.getMessage());
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
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert1 = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(cert));
                m_textPrevCertCN.setText(cert1.getSubjectDN().getName());
                m_textPrevCertValidFrom.setText(cert1.getNotBefore().toString());
                m_textPrevCertValidTo.setText(cert1.getNotAfter().toString());
                m_prevPubCert=cert1.getEncoded();
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
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert1 = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(cert));
                m_textNextCertCN.setText(cert1.getSubjectDN().getName());
                m_textNextCertValidFrom.setText(cert1.getNotBefore().toString());
                m_textNextCertValidTo.setText(cert1.getNotAfter().toString());
                m_nextPubCert=cert1.getEncoded();
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

    public void generateNewCert()
    {
      PasswordBox dialog = new PasswordBox(TheApplet.getMainWindow(),"New Password",PasswordBox.NEW_PASSWORD);
      dialog.show();
      char[] passwd=dialog.getPassword();
      if(passwd==null)
        return;
      String mixid=MyFrame.m_GeneralPanel.getMixID();
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
        kstore.setKeyEntry("<Mix id=\""+mixid+"\"/>",(Key)kp.getPrivate(),passwd, chain);
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
        PasswordBox dialog = new PasswordBox(TheApplet.getMainWindow(),"Change Password",PasswordBox.CHANGE_PASSWORD);
        dialog.setVisible(true);
        char[] passwd=dialog.getPassword();
        char[] oldpasswd=dialog.getOldPassword();
    //    if(passwd==null||oldpasswd==null)
    //      return;
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
          }
        catch(Exception e)
          {
            e.printStackTrace();
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
                        file = new File(file.getParentFile(), fname + ".pfx");
                        break;
                    case TheApplet.FILTER_CER:
                        file = new File(file.getParentFile(), fname + ".der.cer");
                        break;
                    case TheApplet.FILTER_B64_CER:
                        file = new File(file.getParentFile(), fname + ".b64.cer");
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