package mixconfig;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;

import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.BERInputStream;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEREncodableVector;
import org.bouncycastle.asn1.DERInputStream;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTCTime;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.SignedData;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.asn1.x509.V3TBSCertificateGenerator;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.generators.DSAKeyPairGenerator;
import org.bouncycastle.crypto.generators.DSAParametersGenerator;
import org.bouncycastle.crypto.params.DSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle.crypto.signers.DSASigner;

class CertificatesPanel extends JPanel implements ActionListener
{
		JTextField m_textOwnCertCN, m_textOwnCertValidFrom, m_textOwnCertValidTo;
		JTextField m_textPrevCertCN, m_textPrevCertValidFrom, m_textPrevCertValidTo;
		JTextField m_textNextCertCN, m_textNextCertValidFrom, m_textNextCertValidTo;
		JButton m_bttnPrevCertImport,
				m_btnnNextCertImport,
				m_bttnPrevCertExport,
				m_bttnNextCertExport,
				m_bttnPrevCertRemove,
				m_bttnNextCertRemove,
				m_bttnCreateOwnCert,
				m_bttnExportOwnPub,
				m_bttnImportOwnPub,
				m_bttnChangePasswd,
				m_bttnRemoveOwnCert;

		byte[] m_ownPubCert;
		byte[] m_ownPrivCert;
		byte[] m_nextPubCert;
		byte[] m_prevPubCert;

		public CertificatesPanel()
		{
				m_ownPubCert = null;
				m_ownPrivCert = null;
				GridBagLayout layout = new GridBagLayout();
				setLayout(layout);
				GridBagLayout Own = new GridBagLayout();
				GridBagLayout Previous = new GridBagLayout();
				GridBagLayout Next = new GridBagLayout();

				GridBagConstraints c = new GridBagConstraints();
				c.anchor = GridBagConstraints.NORTHWEST;
				c.insets = new Insets(10, 10, 10, 10);
				c.fill = GridBagConstraints.HORIZONTAL;

				JPanel panel1 = new JPanel(Own);
				GridBagConstraints d = new GridBagConstraints();
				d.anchor = GridBagConstraints.NORTHWEST;
				d.insets = new Insets(5, 5, 5, 5);
				panel1.setBorder(new TitledBorder("Own Mix Certificate"));
				c.gridx = 0;
				c.gridy = 1;
				c.weightx = 1;
				c.weighty = 1;
				layout.setConstraints(panel1, c);
				add(panel1);

				m_bttnCreateOwnCert = new JButton("Create a New One");
				d.gridx = 1;
				d.gridy = 0;
				d.gridwidth = 1;
				d.fill = GridBagConstraints.NONE;
				m_bttnCreateOwnCert.addActionListener(this);
				m_bttnCreateOwnCert.setActionCommand("Create");
				Own.setConstraints(m_bttnCreateOwnCert, d);
				panel1.add(m_bttnCreateOwnCert);
				m_bttnImportOwnPub = new JButton("Import...");
				d.gridx = 2;
				d.gridy = 0;
				d.gridwidth = 1;
				m_bttnImportOwnPub.addActionListener(this);
				m_bttnImportOwnPub.setActionCommand("ImportOwnCert");
				Own.setConstraints(m_bttnImportOwnPub, d);
				panel1.add(m_bttnImportOwnPub);
				m_bttnExportOwnPub = new JButton("Export...");
				d.gridx = 3;
				d.gridy = 0;
				d.gridwidth = 1;
				m_bttnExportOwnPub.addActionListener(this);
				m_bttnExportOwnPub.setActionCommand("ExportOwnPubCert");
				m_bttnExportOwnPub.setEnabled(false);
				Own.setConstraints(m_bttnExportOwnPub, d);
				panel1.add(m_bttnExportOwnPub);
				m_bttnChangePasswd = new JButton("Change Password");
				d.gridx = 4;
				m_bttnChangePasswd.addActionListener(this);
				m_bttnChangePasswd.setActionCommand("passwd");
				m_bttnChangePasswd.setEnabled(false);
				Own.setConstraints(m_bttnChangePasswd, d);
				panel1.add(m_bttnChangePasswd);
				m_bttnRemoveOwnCert = new JButton("Remove");
				d.gridx = 5;
				m_bttnRemoveOwnCert.addActionListener(this);
				m_bttnRemoveOwnCert.setActionCommand("RemoveOwnCert");
				m_bttnRemoveOwnCert.setEnabled(false);
				Own.setConstraints(m_bttnRemoveOwnCert, d);
				panel1.add(m_bttnRemoveOwnCert);

				d.gridx = 0;
				d.gridy = 1;
				d.fill = GridBagConstraints.HORIZONTAL;
				JLabel name1 = new JLabel("Name");
				Own.setConstraints(name1, d);
				panel1.add(name1);
				m_textOwnCertCN = new JTextField();
				m_textOwnCertCN.setEditable(false);
				d.gridx = 1;
				d.gridwidth = 5;
				d.weightx = 1;
				Own.setConstraints(m_textOwnCertCN, d);
				panel1.add(m_textOwnCertCN);

				JLabel from1 = new JLabel("Valid From");
				d.gridx = 0;
				d.gridy = 2;
				d.gridwidth = 1;
				d.weightx = 0;
				Own.setConstraints(from1, d);
				panel1.add(from1);
				m_textOwnCertValidFrom = new JTextField();
				m_textOwnCertValidFrom.setEditable(false);
				d.gridx = 1;
				d.gridwidth = 5;
				d.weightx = 1;
				Own.setConstraints(m_textOwnCertValidFrom, d);
				panel1.add(m_textOwnCertValidFrom);

				JLabel to1 = new JLabel("Valid To");
				d.gridx = 0;
				d.gridy = 3;
				d.gridwidth = 1;
				d.weightx = 0;
				Own.setConstraints(to1, d);
				panel1.add(to1);
				m_textOwnCertValidTo = new JTextField();
				m_textOwnCertValidTo.setEditable(false);
				d.gridx = 1;
				d.gridwidth = 5;
				d.weightx = 1;
				Own.setConstraints(m_textOwnCertValidTo, d);
				panel1.add(m_textOwnCertValidTo);

				c.gridx = 0;
				c.gridy = 0;
				JPanel panel2 = new JPanel(Previous);
				GridBagConstraints e = new GridBagConstraints();
				e.anchor = GridBagConstraints.NORTHWEST;
				e.insets = new Insets(5, 5, 5, 5);
				e.fill = GridBagConstraints.HORIZONTAL;
				/*  panel2.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder("Previous Mix Certificate"),
						BorderFactory.createEmptyBorder(0,0,0,0))); */
				panel2.setBorder(new TitledBorder("Previous Mix Certificate"));
				layout.setConstraints(panel2, c);
				add(panel2);

				m_bttnPrevCertImport = new JButton("Import...");
				e.gridx = 1;
				e.gridy = 0;
				e.fill = GridBagConstraints.NONE;
				m_bttnPrevCertImport.addActionListener(this);
				m_bttnPrevCertImport.setActionCommand("Import1");
				m_bttnPrevCertImport.setEnabled(false);
				Previous.setConstraints(m_bttnPrevCertImport, e);
				panel2.add(m_bttnPrevCertImport);
				m_bttnPrevCertExport = new JButton("Export...");
				e.gridx = 2;
				m_bttnPrevCertExport.addActionListener(this);
				m_bttnPrevCertExport.setActionCommand("Export1");
				m_bttnPrevCertExport.setEnabled(false);
				Previous.setConstraints(m_bttnPrevCertExport, e);
				panel2.add(m_bttnPrevCertExport);
				m_bttnPrevCertRemove = new JButton("Remove");
				e.gridx = 3;
				m_bttnPrevCertRemove.addActionListener(this);
				m_bttnPrevCertRemove.setActionCommand("Remove1");
				m_bttnPrevCertRemove.setEnabled(false);
				Previous.setConstraints(m_bttnPrevCertRemove, e);
				panel2.add(m_bttnPrevCertRemove);
				e.fill = GridBagConstraints.HORIZONTAL;

				JLabel name2 = new JLabel("Name");
				e.gridx = 0;
				e.gridy = 1;
				Previous.setConstraints(name2, e);
				panel2.add(name2);
				m_textPrevCertCN = new JTextField(26);
				m_textPrevCertCN.setEditable(false);
				e.gridx = 1;
				e.gridwidth = 3;
				e.weightx = 1;
				Previous.setConstraints(m_textPrevCertCN, e);
				panel2.add(m_textPrevCertCN);

				JLabel from2 = new JLabel("Valid From");
				e.gridx = 0;
				e.gridy = 2;
				e.gridwidth = 1;
				e.weightx = 0;
				Previous.setConstraints(from2, e);
				panel2.add(from2);
				m_textPrevCertValidFrom = new JTextField(26);
				m_textPrevCertValidFrom.setEditable(false);
				e.gridx = 1;
				e.gridwidth = 3;
				e.weightx = 1;
				Previous.setConstraints(m_textPrevCertValidFrom, e);
				panel2.add(m_textPrevCertValidFrom);

				JLabel to2 = new JLabel("Valid To");
				e.gridx = 0;
				e.gridy = 3;
				e.gridwidth = 1;
				e.weightx = 0;
				Previous.setConstraints(to2, e);
				panel2.add(to2);
				m_textPrevCertValidTo = new JTextField(26);
				m_textPrevCertValidTo.setEditable(false);
				e.gridx = 1;
				e.gridwidth = 3;
				e.weightx = 1;
				Previous.setConstraints(m_textPrevCertValidTo, e);
				panel2.add(m_textPrevCertValidTo);

				c.gridy = 2;
				JPanel panel3 = new JPanel(Next);
				GridBagConstraints f = new GridBagConstraints();
				f.anchor = GridBagConstraints.NORTHWEST;
				f.insets = new Insets(5, 5, 5, 5);
				f.fill = GridBagConstraints.HORIZONTAL;
				panel3.setBorder(new TitledBorder("Next Mix Certificate"));
				layout.setConstraints(panel3, c);
				add(panel3);

				m_btnnNextCertImport = new JButton("Import...");
				f.gridx = 1;
				f.gridy = 0;
				f.fill = GridBagConstraints.NONE;
				m_btnnNextCertImport.addActionListener(this);
				m_btnnNextCertImport.setActionCommand("Import2");
				Next.setConstraints(m_btnnNextCertImport, f);
				panel3.add(m_btnnNextCertImport);
				m_bttnNextCertExport = new JButton("Export...");
				f.gridx = 2;
				m_bttnNextCertExport.addActionListener(this);
				m_bttnNextCertExport.setActionCommand("Export2");
				m_bttnNextCertExport.setEnabled(false);
				Next.setConstraints(m_bttnNextCertExport, f);
				panel3.add(m_bttnNextCertExport);
				m_bttnNextCertRemove = new JButton("Remove");
				f.gridx = 3;
				m_bttnNextCertRemove.addActionListener(this);
				m_bttnNextCertRemove.setActionCommand("Remove2");
				m_bttnNextCertRemove.setEnabled(false);
				Next.setConstraints(m_bttnNextCertRemove, f);
				panel3.add(m_bttnNextCertRemove);
				f.fill = GridBagConstraints.HORIZONTAL;

				JLabel name3 = new JLabel("Name");
				f.gridx = 0;
				f.gridy = 1;
				f.weightx = 0;
				Next.setConstraints(name3, f);
				panel3.add(name3);
				m_textNextCertCN = new JTextField(26);
				m_textNextCertCN.setEditable(false);
				f.gridx = 1;
				f.gridwidth = 3;
				f.weightx = 1;
				Next.setConstraints(m_textNextCertCN, f);
				panel3.add(m_textNextCertCN);

				JLabel from3 = new JLabel("Valid From");
				f.gridx = 0;
				f.gridy = 2;
				f.gridwidth = 1;
				f.weightx = 0;
				Next.setConstraints(from3, f);
				panel3.add(from3);
				m_textNextCertValidFrom = new JTextField(26);
				m_textNextCertValidFrom.setEditable(false);
				f.gridx = 1;
				f.gridwidth = 3;
				f.weightx = 1;
				Next.setConstraints(m_textNextCertValidFrom, f);
				panel3.add(m_textNextCertValidFrom);

				JLabel to3 = new JLabel("Valid To");
				f.gridx = 0;
				f.gridy = 3;
				f.gridwidth = 1;
				f.weightx = 0;
				Next.setConstraints(to3, f);
				panel3.add(to3);
				m_textNextCertValidTo = new JTextField(26);
				m_textNextCertValidTo.setEditable(false);
				f.gridx = 1;
				f.gridwidth = 3;
				f.weightx = 1;
				Next.setConstraints(m_textNextCertValidTo, f);
				panel3.add(m_textNextCertValidTo);
		}

		public void clear()
		{
				setOwnPrivCert(null, null);
				setPrevPubCert(null);
				setNextPubCert(null);
		}

		public void updateButtons(boolean hasPrevious, boolean hasNext)
		{
				m_bttnPrevCertImport.setEnabled(hasPrevious);
				m_btnnNextCertImport.setEnabled(hasNext);
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
				if (cert == null)
						setOwnPrivCert(null, null);
				else
				{
						char[] passwd = new char[] {
						};
						while (passwd != null && !setOwnPrivCert(cert, passwd))
						{
								PasswordBox pb =
										new PasswordBox(
												MixConfig.getMainWindow(),
												"Enter the password",
												PasswordBox.ENTER_PASSWORD, null);
								pb.show();
								passwd = pb.getPassword();
						}
				}
		}


		private boolean setOwnPrivCert(byte[] cert, char[] passwd)
		{
				try
				{
						if (cert != null)
						{
								if (cert[0]
										!= (DERInputStream.SEQUENCE | DERInputStream.CONSTRUCTED))
										throw (new RuntimeException("Not a PKCS 12 stream."));

								PKCS12 pkcs12 = PKCS12.load(new ByteArrayInputStream(cert), passwd);

								m_textOwnCertValidFrom.setText(
										pkcs12.getX509cert().getStartDate().getDate().toString());
								m_textOwnCertValidTo.setText(pkcs12.getX509cert().getEndDate().getDate().toString());
								m_textOwnCertCN.setText(pkcs12.getX509cert().getSubject().toString());
								m_ownPrivCert = cert;
								ByteArrayOutputStream out = new ByteArrayOutputStream();
								new DEROutputStream(out).writeObject(pkcs12.getX509cert());
								m_ownPubCert = out.toByteArray();
								m_bttnExportOwnPub.setEnabled(true);
								m_bttnChangePasswd.setEnabled(true);
								m_bttnRemoveOwnCert.setEnabled(true);
								return true;

						}
						else
						{
								m_textOwnCertValidFrom.setText(null);
								m_textOwnCertValidTo.setText(null);
								m_textOwnCertCN.setText(null);
								m_ownPrivCert = null;
								m_bttnExportOwnPub.setEnabled(false);
								m_bttnChangePasswd.setEnabled(false);
								m_bttnRemoveOwnCert.setEnabled(false);
						}
				}
				catch (PKCS12.IllegalCertificateException e)
				{
						JOptionPane.showMessageDialog(
								this,
								e.getMessage(),
								"Error while reading the certificate.",
								JOptionPane.ERROR_MESSAGE);
						return true;

				}
				catch (Throwable e)
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
						if (cert != null)
						{
								X509CertificateStructure cert1 = MixConfig.readCertificate(cert);
								m_textPrevCertCN.setText(cert1.getSubject().toString());
								m_textPrevCertValidFrom.setText(
										cert1.getStartDate().getDate().toString());
								m_textPrevCertValidTo.setText(
										cert1.getEndDate().getDate().toString());
								ByteArrayOutputStream out = new ByteArrayOutputStream();
								new DEROutputStream(out).writeObject(cert1);
								m_prevPubCert = out.toByteArray();

								/*
								CertificateFactory cf = CertificateFactory.getInstance("X.509");
								X509Certificate cert1 = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(cert));
								m_textPrevCertCN.setText(cert1.getSubjectDN().getName());
								m_textPrevCertValidFrom.setText(cert1.getNotBefore().toString());
								m_textPrevCertValidTo.setText(cert1.getNotAfter().toString());
								m_prevPubCert=cert1.getEncoded();
								*/

								m_bttnPrevCertExport.setEnabled(true);
								m_bttnPrevCertRemove.setEnabled(true);
						}
						else
						{
								m_textPrevCertCN.setText(null);
								m_textPrevCertValidFrom.setText(null);
								m_textPrevCertValidTo.setText(null);
								m_prevPubCert = null;
								m_bttnPrevCertExport.setEnabled(false);
								m_bttnPrevCertRemove.setEnabled(false);
						}
				}
				catch (Exception e)
				{
						System.out.println("Prev Cert not set: " + e.getMessage());
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
						if (cert != null)
						{
								X509CertificateStructure cert1 = MixConfig.readCertificate(cert);
								m_textNextCertCN.setText(cert1.getSubject().toString());
								m_textNextCertValidFrom.setText(
										cert1.getStartDate().getDate().toString());
								m_textNextCertValidTo.setText(
										cert1.getEndDate().getDate().toString());
								ByteArrayOutputStream out = new ByteArrayOutputStream();
								new DEROutputStream(out).writeObject(cert1);
								m_nextPubCert = out.toByteArray();
								/*
								CertificateFactory cf = CertificateFactory.getInstance("X.509");
								X509Certificate cert1 = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(cert));
								m_textNextCertCN.setText(cert1.getSubjectDN().getName());
								m_textNextCertValidFrom.setText(cert1.getNotBefore().toString());
								m_textNextCertValidTo.setText(cert1.getNotAfter().toString());
								m_nextPubCert=cert1.getEncoded();
								*/
								m_bttnNextCertExport.setEnabled(true);
								m_bttnNextCertRemove.setEnabled(true);
						}
						else
						{
								m_textNextCertCN.setText(null);
								m_textNextCertValidFrom.setText(null);
								m_textNextCertValidTo.setText(null);
								m_nextPubCert = null;
								m_bttnNextCertExport.setEnabled(false);
								m_bttnNextCertRemove.setEnabled(false);
						}
				}
				catch (Exception e)
				{
						System.out.println("Next Cert not set: " + e.getMessage());
						setNextPubCert(null);
				}
		}

		class DateTextField extends JPanel
		{
				private JTextField hour, min, sec, day, year;
				private JComboBox month;

				class DayDocument extends PlainDocument
				{
						Component which;

						private final int daysPerMonth[] =
								{ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

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
								String p1 = getText(0, offset);
								String p2 = getText(offset, getLength() - offset);
								String res = "";

								int max = (month == null) ? 0 : (month.getSelectedIndex());
								if (max == 1)
								{
										int y = Integer.parseInt(year.getText());
										if ((y % 4) == 0 && ((y % 100) != 0 || (y % 400) == 0))
												max = 29;
										else
												max = 28;
								}
								else
										max = daysPerMonth[max];

								for (int i = 0; i < str.length(); i++)
										if (!Character.isDigit(str.charAt(i)))
												java.awt.Toolkit.getDefaultToolkit().beep();
										else
										{
												String sstr = str.substring(i, i + 1);
												int val = Integer.parseInt(p1 + res + sstr + p2, 10);
												if (max > 0 && val > max)
														java.awt.Toolkit.getDefaultToolkit().beep();
												else
														res += sstr;
										}
								super.insertString(offset, res, attr);
								if (which != null
										&& max > 0
										&& getLength() > 0
										&& 10 * Integer.parseInt(getText(0, getLength()), 10) > max)
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

						GridBagConstraints gbc = new GridBagConstraints();
						gbc.anchor = GridBagConstraints.WEST;
						gbc.fill = GridBagConstraints.HORIZONTAL;
						gbc.insets = new Insets(5, 5, 5, 5);
						gbc.gridx = 0;
						gbc.gridy = 0;

						day = new JTextField(2);
						day.setMinimumSize(day.getPreferredSize());
						day.setDocument(new DayDocument(day));
						day.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
						gbc.weightx = 1;
						gbc.insets.right = 1;
						layout.setConstraints(day, gbc);
						add(day);
						gbc.gridx++;
						label = new JLabel(".");
						gbc.weightx = 0;
						gbc.insets.right = 5;
						gbc.insets.left = 1;
						layout.setConstraints(label, gbc);
						add(label);
						gbc.gridx++;
						gbc.insets.left = 5;

						month =
								new JComboBox(
										new String[] {
												"January",
												"February",
												"March",
												"April",
												"May",
												"June",
												"July",
												"August",
												"September",
												"Oktober",
												"November",
												"December" });
						month.setKeySelectionManager(new JComboBox.KeySelectionManager()
						{
								public int selectionForKey(
										char key,
										javax.swing.ComboBoxModel cbm)
								{
										int nr = key - '0';
										if (nr < 0 || nr > 9)
												return -1;

										if (nr < 3 && month.getSelectedIndex() == 0)
												nr = 10 + nr;

										return nr - 1;
								}
						});
						month.setSelectedIndex(cal.get(Calendar.MONTH));
						gbc.weightx = 1;
						layout.setConstraints(month, gbc);
						add(month);
						gbc.gridx++;

						year = new JTextField(4);
						year.setMinimumSize(year.getPreferredSize());
						year.setDocument(new IntegerDocument(year));
						year.setText(Integer.toString(cal.get(Calendar.YEAR)));
						gbc.weightx = 1;
						layout.setConstraints(year, gbc);
						add(year);
						gbc.gridx++;

						hour = new JTextField(2);
						hour.setMinimumSize(day.getPreferredSize());
						hour.setDocument(new IntegerDocument(23, hour));
						hour.setText(Integer.toString(cal.get(Calendar.HOUR)));
						gbc.weightx = 1;
						gbc.insets.right = 1;
						layout.setConstraints(hour, gbc);
						add(hour);
						gbc.gridx++;
						label = new JLabel(":");
						gbc.weightx = 0;
						gbc.insets.left = 1;
						layout.setConstraints(label, gbc);
						add(label);
						gbc.gridx++;
						min = new JTextField(2);
						min.setMinimumSize(day.getPreferredSize());
						min.setDocument(new IntegerDocument(59, min));
						min.setText(Integer.toString(cal.get(Calendar.MINUTE)));
						gbc.weightx = 1;
						layout.setConstraints(min, gbc);
						add(min);
						gbc.gridx++;
						label = new JLabel(":");
						gbc.weightx = 0;
						layout.setConstraints(label, gbc);
						add(label);
						gbc.gridx++;
						sec = new JTextField(2);
						sec.setMinimumSize(day.getPreferredSize());
						sec.setDocument(new IntegerDocument(59, sec));
						sec.setText(Integer.toString(cal.get(Calendar.SECOND)));
						gbc.weightx = 1;
						layout.setConstraints(sec, gbc);
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
						day.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
						month.setSelectedIndex(cal.get(Calendar.MONTH));
						year.setText(Integer.toString(cal.get(Calendar.YEAR)));
						hour.setText(Integer.toString(cal.get(Calendar.HOUR)));
						min.setText(Integer.toString(cal.get(Calendar.MINUTE)));
						sec.setText(Integer.toString(cal.get(Calendar.SECOND)));
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
						GridBagLayout layout = new GridBagLayout();
						getContentPane().setLayout(layout);

						// Constraints for the labels
						GridBagConstraints gbc = new GridBagConstraints();
						gbc.anchor = GridBagConstraints.WEST;
						gbc.insets = new Insets(5, 5, 5, 5);
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
						layout.setConstraints(from, gbc);
						getContentPane().add(from);
						gbc.gridx = 2;
						gbc.weightx = 1;
						JButton nowButton = new JButton("Now");
						nowButton.setActionCommand("Now");
						nowButton.addActionListener(new ActionListener()
						{
								public void actionPerformed(ActionEvent ev)
								{
										if (ev.getActionCommand().equals("Now"))
										{
												from.setDate(new Date(System.currentTimeMillis()));
										}
								}
						});
						layout.setConstraints(nowButton, gbc);
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
						cal.add(Calendar.YEAR, 1);
						to = new DateTextField(cal.getTime());
						layout.setConstraints(to, gbc);
						getContentPane().add(to);
						gbc.gridx = 2;
						gbc.weightx = 1;
						JButton y1Button = new JButton("1 Year");
						y1Button.setActionCommand("1 Year");
						y1Button.addActionListener(new ActionListener()
						{
								public void actionPerformed(ActionEvent ev)
								{
										if (ev.getActionCommand().equals("1 Year"))
										{
												Calendar cal = Calendar.getInstance();
												cal.setTime(from.getDate());
												cal.add(Calendar.YEAR, 1);
												to.setDate(cal.getTime());
										}
								}
						});
						layout.setConstraints(y1Button, gbc);
						getContentPane().add(y1Button);
						gbc.gridy++;

						GridBagLayout keylayout = new GridBagLayout();
						JPanel keys = new JPanel(keylayout);
						GridBagConstraints kc = new GridBagConstraints();
						kc.weightx = 1;
						kc.gridx = 0;
						kc.gridy = 0;
						kc.gridwidth = 1;
						kc.fill = GridBagConstraints.HORIZONTAL;
						kc.insets = new Insets(1, 1, 1, 1);
						JButton key = new JButton("OK");
						key.addActionListener(new ActionListener()
						{
								public void actionPerformed(ActionEvent ev)
								{
										dispose();
								}
						});
						keylayout.setConstraints(key, kc);
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
						keylayout.setConstraints(key, kc);
						keys.add(key);

						gbc.gridx = 0;
						gbc.gridwidth = 3;
						gbc.fill = GridBagConstraints.HORIZONTAL;
						layout.setConstraints(keys, gbc);
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
				String oMixid = ConfigFrame.m_GeneralPanel.getMixID();

				if (oMixid == null || oMixid.length() == 0)
				{
						javax.swing.JOptionPane.showMessageDialog(
								this,
								"Please enter Mix ID in general panel.",
								"No Mix ID!",
								javax.swing.JOptionPane.ERROR_MESSAGE);
						return;
				}
				else if (!ConfigFrame.m_GeneralPanel.isMixIDValid())
				{
						javax.swing.JOptionPane.showMessageDialog(
								this,
								"Please enter a valid Mix ID in general panel,\n"
										+ "starting with a 'm' and containing only letters,\n"
										+ "digits, dots, underscores and minuses.",
								"Invalid Mix ID!",
								javax.swing.JOptionPane.ERROR_MESSAGE);
						return;
				}

				final ValidityDialog vdialog =
						new ValidityDialog(MixConfig.getMainWindow(), "Validity");
				vdialog.show();
				if (vdialog.from == null)
						return;
				PasswordBox dialog =
						new PasswordBox(
								MixConfig.getMainWindow(),
								"New Password",
								PasswordBox.NEW_PASSWORD,
								"This password has to be entered every time the Mix server starts. "+
								"So if you want to start it automatically you shouldn't enter a password.");
				dialog.show();
				final char[] passwd = dialog.getPassword();
				if (passwd == null)
						return;
				final String mixid = URLEncoder.encode(oMixid);

				final BusyWindow waitWindow =
						new BusyWindow(MixConfig.getMainWindow(), "Generating Key Pair.");

				SwingWorker worker = new SwingWorker()
				{
						public Object construct()
						{
								X509CertGenerator v3certgen =new X509CertGenerator();
								v3certgen.setStartDate(new DERUTCTime(vdialog.from.getDate()));
								v3certgen.setEndDate(new DERUTCTime(vdialog.to.getDate()));
								v3certgen.setSubject(
										new X509Name("CN=<Mix id=\"" + mixid + "\"/>"));
								v3certgen.setSerialNumber(new DERInteger(1));

								try
								{
										SecureRandom random = new SecureRandom();
										DSAParametersGenerator pGen = new DSAParametersGenerator();
										DSAKeyPairGenerator kpGen = new DSAKeyPairGenerator();
										pGen.init(1024, 20, random);
										kpGen.init(	new DSAKeyGenerationParameters(	random,pGen.generateParameters()));
										AsymmetricCipherKeyPair ackp =kpGen.generateKeyPair();
										MyDSAPublicKey pubKey=new MyDSAPublicKey((DSAPublicKeyParameters)ackp.getPublic());
										MyDSAPrivateKey privKey=new MyDSAPrivateKey((DSAPrivateKeyParameters)ackp.getPrivate());

										v3certgen.setSubjectPublicKeyInfo(
												new SubjectPublicKeyInfo(
														(ASN1Sequence) new DERInputStream(new ByteArrayInputStream(pubKey.getEncoded()))
																.readObject()));
										X509CertificateStructure x509cert =
												v3certgen.sign(new X509Name("CN=<Mix id=\"" + mixid + "\"/>"),privKey);

										PKCS12 pkcs12 =
												new PKCS12(
														"<Mix id=\"" + mixid + "\"/>",
														privKey,
														x509cert,
														pubKey);
										ByteArrayOutputStream out = new ByteArrayOutputStream();
										pkcs12.store(out, passwd);
										out.close();
										return out.toByteArray();
								}
								catch (Exception e)
								{
										if (Thread.interrupted())
												return null;
										System.out.println("Error in Key generation and storage!!");
										e.printStackTrace();
								}
								return null;
						}

						public void finished()
						{
								Object cert = get();
								if (cert != null)
										setOwnPrivCert((byte[]) cert, passwd);
								waitWindow.dispose();
						}
				};
				waitWindow.setSwingWorker(worker);
				worker.start();
		}

		public void exportCert(byte[] cert)
		{
				if (cert == null)
						return;
				try
				{
						JFileChooser fd =
								MixConfig.showFileDialog(
										MixConfig.SAVE_DIALOG,
										MixConfig.FILTER_CER | MixConfig.FILTER_B64_CER);
						FileFilter ff = fd.getFileFilter();
						int type;
						if (ff instanceof SimpleFileFilter)
								type = ((SimpleFileFilter) ff).getFilterType();
						else
								type = MixConfig.FILTER_B64_CER;
						File file = fd.getSelectedFile();
						if (file != null)
						{
								String fname = file.getName();
								if (fname.indexOf('.') < 0)
										switch (type)
										{
												case MixConfig.FILTER_CER :
														file = new File(file.getParent(), fname + ".der.cer");
														break;
												case MixConfig.FILTER_B64_CER :
														file = new File(file.getParent(), fname + ".b64.cer");
														break;
										}
								try
								{
										FileOutputStream fout = new FileOutputStream(file);
										switch (type)
										{
												case MixConfig.FILTER_CER :
														fout.write(cert);
														break;
												case MixConfig.FILTER_B64_CER :
														fout.write("-----BEGIN CERTIFICATE-----\n".getBytes());
														fout.write(Base64.encodeBytes(cert).getBytes());
														fout.write("\n-----END CERTIFICATE-----\n".getBytes());
														break;
										}
										fout.close();
								}
								catch (Exception e)
								{
								}
						}
						return;
				}
				catch(Exception e)
				{
				}
				// Wenn wir hier sind, hat etwas nicht geklapppt.

				try
				{
						ClipFrame Save =
								new ClipFrame(
										"Copy and Save this file in a new Location.",
										false);
						Save.setText("-----BEGIN CERTIFICATE-----\n"+
												 Base64.encodeBytes(cert)+
												 "\n-----END CERTIFICATE-----\n");
						Save.show();
				}
				catch (Exception e)
				{
						e.printStackTrace();
				}

		}

		public void actionPerformed(ActionEvent ae)
		{
				if (ae.getActionCommand().equals("Create"))
				{
						generateNewCert();
				}

				else if (ae.getActionCommand().equals("passwd"))
				{
						PasswordBox dialog =
								new PasswordBox(
										MixConfig.getMainWindow(),
										"Change Password",
										PasswordBox.CHANGE_PASSWORD, null);
						while (true)
						{
								dialog.show();
								char[] passwd = dialog.getPassword();
								char[] oldpasswd = dialog.getOldPassword();
								if (passwd == null)
										break;
								try
								{
										ByteArrayOutputStream out = new ByteArrayOutputStream();
										PKCS12.load(new ByteArrayInputStream(m_ownPrivCert), oldpasswd)
												.store(out, passwd);

										/*
										KeyStore kstore = KeyStore.getInstance("PKCS12", "BC");
										kstore.load(
												new ByteArrayInputStream(m_ownPrivCert),
												oldpasswd);
										String alias = (String) kstore.aliases().nextElement();
										Key privKey = kstore.getKey(alias, null);
										Certificate[] chain = kstore.getCertificateChain(alias);
										kstore.setKeyEntry(alias, privKey, passwd, chain);
										ByteArrayOutputStream out = new ByteArrayOutputStream();
										kstore.store(out, passwd);
										*/
										setOwnPrivCert(out.toByteArray(), passwd);
										break;
								}
								catch (Exception e)
								{
										javax.swing.JOptionPane.showMessageDialog(
												this,
												"Wrong Password.",
												"Password Error",
												javax.swing.JOptionPane.ERROR_MESSAGE);
								}
						}
				}
				else if (ae.getActionCommand().equalsIgnoreCase("ExportOwnPubCert"))
				{
						try
						{
								JFileChooser fd =
										MixConfig.showFileDialog(
												MixConfig.SAVE_DIALOG,
												MixConfig.FILTER_CER
														| MixConfig.FILTER_B64_CER
														| MixConfig.FILTER_PFX);
								File file = fd.getSelectedFile();
								FileFilter ff = fd.getFileFilter();
								int type;
								if (ff instanceof SimpleFileFilter)
										type = ((SimpleFileFilter) ff).getFilterType();
								else
										type = MixConfig.FILTER_B64_CER;
								if (file != null)
								{
										String fname = file.getName();
										if (fname.indexOf('.') < 0)
												switch (type)
												{
														case MixConfig.FILTER_PFX :
																file = new File(file.getParent(), fname + ".pfx");
																break;
														case MixConfig.FILTER_CER :
																file =
																		new File(file.getParent(), fname + ".der.cer");
																break;
														case MixConfig.FILTER_B64_CER :
																file =
																		new File(file.getParent(), fname + ".b64.cer");
																break;
												}
										try
										{
												FileOutputStream fout = new FileOutputStream(file);
												switch (type)
												{
														case MixConfig.FILTER_PFX :
																fout.write(getOwnPrivCert());
																break;
														case MixConfig.FILTER_CER :
																fout.write(getOwnPubCert());
																break;
														case MixConfig.FILTER_B64_CER :
																fout.write(
																		"-----BEGIN CERTIFICATE-----\n".getBytes());
																fout.write(
																		Base64.encodeBytes(getOwnPubCert()).getBytes());
																fout.write(
																		"\n-----END CERTIFICATE-----\n".getBytes());
																break;
												}
												fout.close();
										}
										catch (Exception e)
										{
										}
								}
								return;
						}
						catch(Exception e)
						{}

						try
						{
								ClipFrame Save =
										new ClipFrame(
												"Copy and Save this file in a new Location.",
												false);
								Save.setText("-----BEGIN CERTIFICATE-----\n"+
										Base64.encodeBytes(getOwnPubCert())+
										"\n-----END CERTIFICATE-----\n");
								Save.show();
						}
						catch (Exception e)
						{
								e.printStackTrace();
						}

				}
				else if (ae.getActionCommand().equals("ImportOwnCert"))
				{
						byte[] buff;
						try
						{
								buff = MixConfig.openFile(MixConfig.FILTER_PFX);
						}
						catch(Exception e)
						{
								javax.swing.JOptionPane.showMessageDialog(
										this,
										"Import of a private key with certificate\n" +
										"is not supported when running as an applet.",
										"Not supported!",
										javax.swing.JOptionPane.ERROR_MESSAGE);
								m_bttnImportOwnPub.setEnabled(false);
								return;
						}
						if (buff != null)
								setOwnPrivCert(buff);
				}
				else if (ae.getActionCommand().equals("RemoveOwnCert"))
						setOwnPrivCert(null);
				else if (ae.getActionCommand().equals("Import1"))
				{
						byte [] cert;
						try
						{
								cert = MixConfig.openFile(MixConfig.FILTER_CER);
						}
						catch(Exception e)
						{

								ClipFrame Open =
										new ClipFrame(
												"Paste a certificate to be imported in the area provided.",
												true);
								Open.show();
								cert = Open.getText().getBytes();
						}
						setPrevPubCert(cert);
				}
				else if (ae.getActionCommand().equals("Export1"))
						exportCert(getPrevPubCert());
				else if (ae.getActionCommand().equals("Remove1"))
						setPrevPubCert(null);
				else if (ae.getActionCommand().equals("Import2"))
				{
						byte [] cert;
						try
						{
								cert = MixConfig.openFile(MixConfig.FILTER_CER);
						}
						catch(Exception e)
						{

								ClipFrame Open =
										new ClipFrame(
												"Paste a certificate to be imported in the area provided.",
												true);
								Open.show();
								cert = Open.getText().getBytes();
						}
						setNextPubCert(cert);
				}
				else if (ae.getActionCommand().equals("Export2"))
						exportCert(getNextPubCert());
				else if (ae.getActionCommand().equals("Remove2"))
						setNextPubCert(null);
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

				GridBagLayout layout = new GridBagLayout();
				p.setLayout(layout);

				// Constraints for the labels
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.anchor = GridBagConstraints.CENTER;
				gbc.insets = new Insets(5, 5, 5, 5);
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
				ImageIcon img = MixConfig.loadImage("busy.gif");
				MediaTracker mt = new MediaTracker(this);
				mt.addImage(img.getImage(), 1);
				try
				{
						mt.waitForAll();
				}
				catch (Exception e)
				{
						e.printStackTrace();
				}
				label = new JLabel(img);
				layout.setConstraints(label, gbc);
				p.add(label);
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
				this.setLocation(
						l.x + (d.width - d2.width) / 2,
						l.y + (d.height - d2.height) / 2);
				this.setVisible(true);
		}

		public void update(Graphics g)
		{
				paint(g);
		}

		public void setSwingWorker(SwingWorker s)
		{
				sw = s;
		}

		public void actionPerformed(ActionEvent ev)
		{
				if (sw != null)
						sw.interrupt();
		}
}
