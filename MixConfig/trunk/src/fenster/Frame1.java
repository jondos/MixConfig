package fenster;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class Frame1 extends JFrame implements ActionListener {
  JPanel contentPane;
  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout2 = new BorderLayout();
  GridLayout gridLayout1 = new GridLayout();
  JTabbedPane jTabbedPane1 = new JTabbedPane();
  JTabbedPane jTabbedPane2 = new JTabbedPane();
  FileDialog filedialog;   // Dateiauswahl-Dialog
  ErrorDialog dialog;      // Fehlerdialog
  String directory = "";   // aktuelle Datei
  String file = "";        // aktuelles Verzeichnis

  JPanel contentTabbedReiter1 = new JPanel(borderLayout1);
  JPanel jPanel1 = new JPanel();
  JPanel contentTabbedReiter3 = new JPanel(borderLayout1);
  JPanel exitPanel = new JPanel();

  JButton jButton1 = new JButton();
  JButton jButton2 = new JButton();
  JButton jButton3 = new JButton();
  JButton exitButton = new JButton();

  JLabel jLabel1 = new JLabel("", JLabel.CENTER);

  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu jfileMenu = new JMenu("File");
  JMenuItem openItem = new JMenuItem("open");
  JMenuItem saveItem = new JMenuItem("save");

  JPanel jPanel2 = new JPanel();
  JLabel jLabel2 = new JLabel();
  JPanel jPanel3 = new JPanel();

  JLabel jLabel3 = new JLabel();
  JTextField jTextField1 = new JTextField();
  JLabel jLabel4 = new JLabel();
  JTextField jTextField2 = new JTextField();
  JLabel jLabel5 = new JLabel();
  JTextField jTextField3 = new JTextField();
  JLabel jLabel6 = new JLabel();
  JTextField jTextField4 = new JTextField();






  /**Den Frame konstruieren*/
  public Frame1() {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  /**Initialisierung der Komponenten*/
  private void jbInit() throws Exception  {
    //setIconImage(Toolkit.getDefaultToolkit().createImage(Frame1.class.getResource("[Ihr Symbol]")));
    contentPane = (JPanel) this.getContentPane();
    contentPane.setLayout(borderLayout2);
    this.setSize(new Dimension(640, 480));
    this.setTitle("Frame-Titel");
    // Erzeugen des Fehlerdialoges,
    // der bei Bedarf angezeigt wird
    dialog = new ErrorDialog(this, "Error", true);

    //ButtonController buttonContr = new ButtonController(this);
    //menu-zeile erzeugen
    this.setJMenuBar(jMenuBar1);

    jLabel2.setText("Daten zur Peson");

    jPanel3.setLayout(gridLayout1);
    gridLayout1.setColumns(2);
    gridLayout1.setRows(4);
    jLabel3.setText("jLabel3");

    jTextField1.setText("jTextField1");
    jLabel4.setText("jLabel4");
    jTextField2.setText("jTextField2");
    jLabel5.setText("jLabel5");
    jTextField3.setText("jTextField3");
    jLabel6.setText("jLabel6");
    jTextField4.setText("jTextField4");

    jMenuBar1.add(jfileMenu);
    jfileMenu.add(openItem);
    openItem.setActionCommand("open File");
    saveItem.setEnabled(false);
    jfileMenu.add(saveItem);
    saveItem.setActionCommand("save File");
    jfileMenu.addSeparator();
    JMenuItem quit = new JMenuItem("quit"/*, new JMenuShortcut('q')*/);
    jfileMenu.add(quit);
    quit.setActionCommand("exit");

    openItem.addActionListener(this);
    saveItem.addActionListener(this);
    quit.addActionListener(this);


    contentPane.add(jTabbedPane1, BorderLayout.CENTER );
    jButton1.setText("Exit");
    jButton1.setActionCommand("exit");
    jButton1.addActionListener(this);
    jButton2.setText("Exit");
    jButton2.setActionCommand("exit");
    jButton2.addActionListener(this);
    jButton3.setText("Exit");
    jButton3.setActionCommand("exit");
    jButton3.addActionListener(this);
    exitButton.setText("Exit");
    exitButton.setActionCommand("exit");
    exitButton.addActionListener(this);

    jLabel1.setText("Möglichkeit 1 (Einstellung beim Start)");

    jTabbedPane1.add(contentTabbedReiter1,   "Person 1");
    //jTabbedPane1.add(jPanel1, "Reiter 2");
    //jTabbedPane1.add(contentTabbedReiter3, "Reiter 3");

    jTabbedPane2.add(jButton3, "jButton3");
    //contentTabbedReiter1.add(jButton1, BorderLayout.SOUTH);
    contentTabbedReiter1.add(jLabel1, BorderLayout.NORTH);
    contentTabbedReiter1.add(jPanel3, BorderLayout.CENTER);
    jPanel3.add("Label3", jLabel3);
    jPanel3.add(jLabel4, null);
    jPanel3.add(jTextField1, null);
    jPanel3.add(jTextField2, null);
    jPanel3.add(jLabel5, null);
    jPanel3.add(jLabel6, null);
    jPanel3.add(jTextField3, null);
    jPanel3.add(jTextField4, null);
    contentTabbedReiter3.add(jTabbedPane2, BorderLayout.CENTER);

    jPanel1.add(jButton2, null);

    contentPane.add(exitPanel, BorderLayout.SOUTH );
    exitPanel.add(exitButton, "Exit");
    contentPane.add(jPanel2, BorderLayout.NORTH);
    jPanel2.add(jLabel2, null);

  }

  // Reaktion auf Druck der einzelnen Buttons
  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    System.out.println(cmd);
    if ("open File".equals(cmd)){     // Datei öffnen

      openFile();
      }
    if ("save File".equals(cmd)){     // Datei speichern

      saveFile();
      }

    if ("exit".equals(cmd)) {        //  beenden

      dispose();
      System.exit(0);
    }
  }

  public String openFileDialog(Frame parent,
                               String name, int mode) {
    // Anzeigen des FileDialogs
    filedialog = new FileDialog(parent, name, mode);
      if (! (directory.equals("")))
        filedialog.setDirectory(directory);
      filedialog.show();
    if (filedialog.getFile() != null)
      // Rückgabe des ausgewählten Namens
      return filedialog.getDirectory()+filedialog.getFile();
    else
      return "";
  }

  public void saveFile() {
    // Auswahl des Dateinamens
    String filename =
      openFileDialog(this, "save File", FileDialog.SAVE);
    /*
    // Wurde ein Name ausgewählt?
    if (! (filename.equals(""))) {
      File f = new File(filename);
      // Überprüfung der Schreibrechte
      if (f.canWrite()) {
        // Schreiben in die Datei
        try {

          PrintWriter out =
            new PrintWriter(new FileWriter(f));
          out.println(editor.getText());
          out.close();
        }
        catch (IOException e) {
          // Bei Schreibfehler Ausgabe einer Fehlermeldung
          dialog.showError("Output error !");
        }
      }
      else
        // Anzeigen des Fehlerdialoges,
        // falls die Datei schreibgeschützt ist
        dialog.showError("Cannot write File !");
    }*/
    jLabel1.setText("Möglichkeit 3 (saveFile-Dialog wurde aufgerufen)");
    jLabel4.setText("Dies ist der Pfad und der Name der zu speichernden Datei:");
    jTextField2.setText(filename);
    System.out.println(filename);
  }

  public void openFile() {
    // Auswahl de Dateinamens
    String filename =
      openFileDialog(this, "open File", FileDialog.LOAD);
    /*
    // Wurde ein Name ausgewählt?
    if (! (filename.equals(""))) {
       File f = new File(filename);
      // Uberprüfung auf Leserechte
      if (f.canRead()) {
        // Auslesen der Datei und Anzeigen des Inhaltes
        String text ="";
        try {
          BufferedReader in =
            new BufferedReader(
              new FileReader(filename));
          editor.setText("");
          while((text = in.readLine()) != null)
            editor.append(text+"\n");
          in.close();
          directory = filedialog.getDirectory();
          file = filedialog.getFile();
        }
        catch (IOException e) {
          // Bei Lesefehler Anzeigen einer Fehlermeldung
          dialog.showError("Input error !");
        }
      }
      else
        // Anzeigen des Fehlerdialoges,
        // falls die Datei lesegeschützt ist
        dialog.showError("Cannot read File !");
    }*/
    jLabel1.setText("Möglichkeit 2 (openFile-Dialog wurde aufgerufen)");
    saveItem.setEnabled(true);
    jLabel3.setText("Dies ist der Pfad und der Name der zu öffnenden Datei:");
    jTextField1.setText(filename);
    System.out.println(filename);
  }

  /**Überschrieben, so dass eine Beendigung beim Schließen des Fensters möglich ist.*/
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      System.exit(0);
    }
  }
}