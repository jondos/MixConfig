package fenster;

import java.awt.event.*;
import java.awt.*;
/**
 * <p>Überschrift: </p>
 * <p>Beschreibung: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Organisation: </p>
 * @author unbekannt
 * @version 1.0
 */

class ErrorDialog extends Dialog implements ActionListener {
  // Label mit Fehlermeldung
  private Label errorLabel;

  public ErrorDialog(Frame parent,
                     String name, boolean modal) {
    super(parent, name, modal);
    setSize(200, 150);
    Button ok = new Button("ok");
    ok.addActionListener(this);
    add("South",ok);
    errorLabel = new Label();
    errorLabel.setAlignment(Label.CENTER);
    add("Center", errorLabel);
  }

  public void showError(String error) {
    // Anzeigen des Fehler-Dialoges mit dem
    // übergebenen Fehlertext
    errorLabel.setText(error);
    show();
  }

  public void actionPerformed(ActionEvent e) {
    // Bestätigung der Fehlermeldung
    if ("ok".equals(e.getActionCommand()))
       dispose();
  }
}