package fenster;

import javax.swing.UIManager;
import java.awt.*;

public class fenstertest {
  boolean packFrame = false;

  /**Die Anwendung konstruieren*/
  public fenstertest() {
    Frame1 frame = new Frame1();
    //Frames überprüfen, die voreingestellte Größe haben
    //Frames packen, die nutzbare bevorzugte Größeninformationen enthalten, z.B. aus ihrem Layout
    if (packFrame) {
      frame.pack();
    }
    else {
      frame.validate();
    }
    //Das Fenster zentrieren
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
  }
  /**Main-Methode*/
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    new fenstertest();
  }
}