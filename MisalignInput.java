import java.awt.event.*;
import java.util.*;

public class MisalignInput implements MouseListener {
   
   // Required methods for MouseListener
   @Override
   public void mouseClicked(MouseEvent e) {
      Poly poly = MisalignSweeper.getClickedPolygon((int)(e.getX() / MisalignGraphics.xMultiplier), (int)(e.getY() / MisalignGraphics.yMultiplier));
      if (poly != null && e.getButton() == MouseEvent.BUTTON1 && poly.isNormal()) { //left click on open tile
         poly.reveal();
         MisalignSweeper.repaint();
         //System.out.println("Tile revealed");
      } else if (poly != null && e.getButton() == MouseEvent.BUTTON3 && !poly.isPressed()) { //right click on open or flagged tile
         poly.flag();
         MisalignSweeper.repaint();
         //System.out.println("Tile flagged/unflagged");
      } else if (poly.isPressed()) { // right or left clicked on revealed tile
         System.out.println("Tile already revealed");
      } else if (poly.isFlagged()) { // left clicked on flagged tile
         System.out.println("Cannot reveal flagged tile");
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
