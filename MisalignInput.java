import java.awt.Cursor;
import java.awt.event.*;
import java.util.*;

public class MisalignInput implements MouseListener, KeyListener {
   
   public static final int[] code = {38, 38, 40, 40, 37, 39, 37, 39};
   public static int iKonami = 0;
   public static boolean konamiCodeEntered = false;
   
   // Read mouse click (left, right, ctrl click) and determines what to do when mouse clicks a poly
   @Override
   public void mousePressed(MouseEvent e) {
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
   public void mouseEntered(MouseEvent e) {
      MisalignGraphics.getGamePanel().setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
   }
   
   @Override
   public void mouseExited(MouseEvent e) {
      MisalignGraphics.getGamePanel().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   }
   
   // Reads key press and compares to Komani Code
   @Override
   public void keyPressed(KeyEvent e) {
      if (!konamiCodeEntered) {
         checkKonami(e.getKeyCode());
      }
   }
   
   // Checks if the Konami Code has been entered
   private void checkKonami(int keyCode) {
      if (keyCode == code[iKonami]) {
         if (iKonami + 1 == code.length) {
            konamiCodeEntered = true;
            MisalignGraphics.getSettings().addSecretSettings();
            Misalignsweeper.repaint();
         } 
         iKonami++;
      } else if (!(keyCode == 38 && iKonami == 2)) // accounts for pressing up more than twice to start
         iKonami = 0;
   }

   // Required methods for MouseListener and KeyListener that are unused
   @Override
   public void mouseClicked(MouseEvent e) { }

   @Override
   public void mouseReleased(MouseEvent e) { }

//    @Override
//    public void mouseEntered(MouseEvent e) { }
// 
//    @Override
//    public void mouseExited(MouseEvent e) { }
   
   @Override
   public void keyTyped(KeyEvent e) { }
   
   @Override
   public void keyReleased(KeyEvent e) { }
}
