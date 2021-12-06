import java.awt.event.*;
import java.util.*;

public class MisalignInput implements KeyListener, MouseListener {
   
   @Override
   public void keyTyped(KeyEvent e) {
      if (e.getKeyChar() == 'r') {
         MisalignSweeper.generateBoard(new Random());
         MisalignSweeper.repaint();
      }
   }

   // Required methods for KeyListener and MouseListener
   @Override
   public void keyPressed(KeyEvent e) { }

   @Override
   public void keyReleased(KeyEvent e) { }

   @Override
   public void mouseClicked(MouseEvent e) {
      MisalignSweeper.getClickedPolygon(e.getX(), e.getY()).setHighlighted(true);
      MisalignSweeper.repaint();
   }

   @Override
   public void mousePressed(MouseEvent e) { }

   @Override
   public void mouseReleased(MouseEvent e) { }

   @Override
   public void mouseEntered(MouseEvent e) { }

   @Override
   public void mouseExited(MouseEvent e) { }
}