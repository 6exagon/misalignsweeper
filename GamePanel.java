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
      else if (MisalignGraphics.getSettings().colorfulModeChecked())
         g2.setColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256))); //colorful mode
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
      g2.setFont(new Font("Monospaced", Font.BOLD, 64));
      FontMetrics fm = g2.getFontMetrics();//used to get width of string with current font
      if (MisalignGraphics.playingLossAnimation) {
         g2.setColor(MisalignGraphics.getSettings().getColor(5));
         String lossText = "YOU LOSE";                  
         g2.drawString(lossText, (this.getWidth() - fm.stringWidth(lossText)) / 2, (this.getHeight() - fm.getHeight()) / 2);
      } else if (MisalignGraphics.gameWon()) {
         g2.setColor(MisalignGraphics.getSettings().getColor(6));
         String winText = "YOU WIN";
         g2.drawString(winText, (this.getWidth() - fm.stringWidth(winText)) / 2, (this.getHeight() - fm.getHeight()) / 2);               
      }
   }
   
}