import java.awt.event.*;
import java.util.*;

public class MisalignInput implements MouseListener, KeyListener {
   
   public static final String[] code = {"Up", "Up", "Down", "Down", "Left", "Right", "Left", "Right"};
   public static int i = 0;
   public static boolean konamiCodeEntered = false;
   
   // Required methods for MouseListener
   @Override
   public void mouseClicked(MouseEvent e) {
      Poly poly = Misalignsweeper.getClickedPoly(e.getX() / MisalignGraphics.getXM(), e.getY() / MisalignGraphics.getYM());
      
      if (poly == null || MisalignGraphics.playingLossAnimation || !MisalignGraphics.getTimer().getSwingTimer().isRunning()) {
         return;
      }
      // right click (or ctrl click) on open or flagged tile
      if (((e.getButton() == MouseEvent.BUTTON3) || (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown()))) {
         poly.flag();
      } else if (e.getButton() == MouseEvent.BUTTON1) { // left click on open tile
         poly.reveal();
      }
      Misalignsweeper.repaint();
   }
   
   @Override
   public void keyPressed(KeyEvent e) {
      String keyText = e.getKeyText(e.getKeyCode());
      System.out.println(keyText);
      if (!konamiCodeEntered)
         checkKonami(keyText);
   }
   
   //Checks if the Konami Code has been entered
   private void checkKonami(String keyText) {
      if (keyText == code[i]) {
         if (i + 1 == code.length) {
            konamiCodeEntered = true;
            MisalignGraphics.getSettings().addSecretSettings();
            Misalignsweeper.repaint();
         } 
         i++;
      } else if (!(keyText.equals("Up") && i == 2))//accounts for pressing Up more than twice to start
         i = 0;
   }

   @Override
   public void mousePressed(MouseEvent e) { }

   @Override
   public void mouseReleased(MouseEvent e) { }

   @Override
   public void mouseEntered(MouseEvent e) { }

   @Override
   public void mouseExited(MouseEvent e) { }
   
   @Override
   public void keyTyped(KeyEvent e) { }
   
   @Override
   public void keyReleased(KeyEvent e) { }
}
