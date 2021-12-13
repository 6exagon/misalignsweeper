import java.awt.event.*;
import java.util.*;

public class MisalignInput implements MouseListener {
   
   // Required methods for MouseListener
   @Override
   public void mouseClicked(MouseEvent e) {
      Poly poly = MisalignSweeper.getClickedPolygon((int)(e.getX() / MisalignGraphics.xMultiplier), (int)(e.getY() / MisalignGraphics.yMultiplier));
      if (poly != null && e.getButton() == MouseEvent.BUTTON1) { //left click
         poly.reveal();
         MisalignSweeper.repaint();
      } else if (poly != null && e.getButton() == MouseEvent.BUTTON3) { //right click
         poly.flag();
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
