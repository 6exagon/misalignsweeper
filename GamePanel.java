import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GamePanel extends JPanel {
   
   private HashMap<Poly, Polygon> polyToGon;
   private Random rand;
      
   //Intializes game panel with the polygon and random object from MisalignGraphics
   public GamePanel(HashMap<Poly, Polygon> polyToGon, Random rand) {
      this.polyToGon = polyToGon;
      this.rand = rand;
      this.setPreferredSize(new Dimension(500, 500));
      this.setFont(new Font("Impact", Font.PLAIN, 64));
   }
   
   //Draws the game board
   @Override
   public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g; //Graphics2D is a better version of the Graphics object
   
      if (!MisalignGraphics.playingLossAnimation && !MisalignGraphics.gamePaused)
         MisalignGraphics.checkGameEnd();

      if (!MisalignGraphics.gamePaused) { 
         MisalignGraphics.setXM(this.getWidth());
         MisalignGraphics.setYM(this.getHeight());
         Misalignsweeper.generateAWTPolygons(MisalignGraphics.getXM(), MisalignGraphics.getYM());
         for (Poly poly : polyToGon.keySet()) {
            displayPolyColor(poly, g2);
            drawImageInMine(poly, g2);
            drawPolyOutline(polyToGon.get(poly), g2);
            displayResult(g2);
         }
      }
      
   }

   //Fills poly with correct color based on display state and settings
   public void displayPolyColor(Poly poly, Graphics2D g2) {
      if (poly.isPressed()) {
         switch (poly.getDisplayState()) {
            case -2:
               g2.setColor(MisalignGraphics.getSettings().getColor(1)); //color of clicked mine
               break;
            case -1:
               g2.setColor(MisalignGraphics.getSettings().getColor(2)); //color of other mines
               break;
            default:
               g2.setColor(MisalignGraphics.getColor(poly.getDisplayState()));
         }
      } else if (poly.isFlagged())
         g2.setColor(MisalignGraphics.getSettings().getColor(3));
      else
         g2.setColor(MisalignGraphics.getSettings().getColor(4)); //unrevealed tile color
      g2.fillPolygon(polyToGon.get(poly));
   }

   //Draws flag, mine, or number within a poly
   public void drawImageInMine(Poly poly, Graphics2D g2) {
      if (poly.isPressed() && poly.getDisplayState() > 0) {
         g2.setColor(MisalignGraphics.getSettings().getColor(20));
         poly.drawNum(g2);  
      } else if (poly.isFlagged()) {
         poly.drawImageInPoly(g2, MisalignGraphics.FLAG_IMAGE);
      } else if (poly.getDisplayState() < 0 && MisalignGraphics.playingLossAnimation) {
         poly.drawImageInPoly(g2, MisalignGraphics.MINE_IMAGE);   
      }
   }

   //Draw the outline of a polygon
   public void drawPolyOutline(Polygon gon, Graphics2D g2) {
      g2.setColor(MisalignGraphics.getSettings().getColor(0));
      if (!MisalignGraphics.getSettings().noLinesModeChecked()) {
         g2.drawPolygon(gon);
      }   
   }

   //Displays win or loss text (centered horizontally, just above the middle vertically)
   public void displayResult(Graphics2D g2) {
      g2.setFont(g2.getFont().deriveFont(70f));
      FontMetrics fm = g2.getFontMetrics();
      boolean won = MisalignGraphics.gameWon();
      if (MisalignGraphics.playingLossAnimation || won) {
         String endText = won ? "YOU WIN" : "YOU LOSE";
         int tx = (this.getWidth() - fm.stringWidth(endText)) / 2;
         int ty = (this.getHeight() - fm.getHeight()) / 2;
         g2.setColor(MisalignGraphics.getSettings().getColor(8));
         g2.drawString(endText, tx - 2, ty + 2);  // draw shadow
         g2.setColor(MisalignGraphics.getSettings().getColor(won ? 6 : 5));
         g2.drawString(endText, tx, ty);
      }
   }
}