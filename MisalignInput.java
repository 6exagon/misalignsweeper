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
      Poly poly = MisalignSweeper.getClickedPolygon((int)(e.getX() / MisalignGraphics.xMultiplier), (int)(e.getY() / MisalignGraphics.yMultiplier));
      if (poly != null) {
         poly.setHighlighted(true);
         MisalignSweeper.repaint();
      } else {
         System.err.println("Could not find a polygon at that location. :(");
      }
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