import java.awt.event.*;
import java.util.*;

public class MisalignInput implements MouseListener {
   
   // Required methods for MouseListener
   @Override
   public void mouseClicked(MouseEvent e) {
      Tri tri = MisalignSweeper.getClickedTri((int)(e.getX() / MisalignGraphics.xMultiplier), (int)(e.getY() / MisalignGraphics.yMultiplier));
      System.out.println("MisalignInput - Before click: " + tri.getDisplayState());
      if (tri == null) {
         System.err.println("Could not find a polygon at that location. :(");
         return;
      } else if (MisalignGraphics.playingLossAnimation) {
         System.out.println("MisalignInput - Game already lost :(");
         return;
      }
      if (e.getButton() == MouseEvent.BUTTON1 && tri.isNormal()) { //left click on open tile
         tri.reveal();
         MisalignSweeper.repaint();
         //MisalignGraphics.gamePanel.repaint();
         System.out.println("MisalignInput - After click: " + tri.getDisplayState());
      } else if (e.getButton() == MouseEvent.BUTTON3 && !tri.isPressed()) { //right click on open or flagged tile
         tri.flag();
         MisalignSweeper.repaint();
      } else if (tri.isPressed()) { // right or left clicked on revealed tile
         System.out.println("Tile already revealed");
      } else if (tri.isFlagged()) { // left clicked on flagged tile
         System.out.println("Cannot reveal flagged tile");
      }
      
    //  if (MisalignSweeper.getWinState().equals("Win"))
      //   System.out.println("3 mines / win");

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
