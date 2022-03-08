import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GamePanel extends JPanel {
   
   private static HashMap<Poly, Polygon> polyToGon;
   private static Random rand;
      
   public GamePanel(HashMap<Poly, Polygon> polyToGon, Random r) {
      this.polyToGon = polyToGon;
      this.rand = rand;
      this.setPreferredSize(new Dimension(500, 500));
   }
   
   @Override
   public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
   
      if (!MisalignGraphics.playingLossAnimation && !MisalignGraphics.gamePaused)
         MisalignGraphics.checkGameEnd();

      if (!MisalignGraphics.gamePaused) {
         MisalignGraphics.setXM(this.getWidth());
         MisalignGraphics.setYM(this.getHeight());
         MisalignSweeper.generateAWTPolygons(MisalignGraphics.getXM(), MisalignGraphics.getYM());
         for (Poly poly : polyToGon.keySet()) {
            displayPolyColor(poly, g2);
            drawImageInMine(poly, g2);
            drawPolyOutline(polyToGon, g2);
            displayResult(g2);
         }

      }
   }


   public void displayPolyColor(Poly poly, Graphics2D g2) {
      if (poly.isPressed()) {
         switch (poly.getDisplayState()) {
            case -2:
               g2.setColor(Color.RED); //mine that was actually clicked is in red
               break;
            case -1:
               g2.setColor(Color.LIGHT_GRAY); //other mines revealed are light gray
               break;
            default:
               g2.setColor(MisalignGraphics.getColor(poly.getDisplayState()));
         }
      } else if (poly.isFlagged())
         g2.setColor(Color.YELLOW);
      else if (MisalignGraphics.getSettings().colorfulModeChecked())
         g2.setColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256))); //colorful mode
      else
         g2.setColor(Color.WHITE);
      g2.fillPolygon(polyToGon.get(poly));
   }



   public void drawImageInMine(Poly poly, Graphics2D g2) {
      //draws flag/num/mine in poly after coloring poly
      if (poly.isPressed() && poly.getDisplayState() > 0)
         poly.drawNum(g2);
      else if (poly.isFlagged())
         poly.drawImageInPoly(g2, MisalignGraphics.FLAG_IMAGE);
      else if (poly.getDisplayState() < 0 && MisalignGraphics.playingLossAnimation)
         poly.drawImageInPoly(g2, MisalignGraphics.MINE_IMAGE);   
   }



   public void drawPolyOutline(HashMap<Poly, Polygon> polyToGon, Graphics2D g2) {
      g2.setColor(Color.BLACK);
      if (!MisalignGraphics.getSettings().noLinesModeChecked()) {
         for (Polygon gon : polyToGon.values())
            g2.drawPolygon(gon); // render polygons' outlines
      }   
   }

   public void displayResult(Graphics2D g2) {
      // win and loss text              
      g2.setFont(new Font("Monospaced", Font.BOLD, 64));
      FontMetrics fm = g2.getFontMetrics();//used to get width of string with current font
      if (MisalignGraphics.playingLossAnimation) {
         g2.setColor(Color.RED);
         String lossText = "You lose";                  
         g2.drawString(lossText, (this.getWidth() - fm.stringWidth(lossText)) / 2, (this.getHeight() - fm.getHeight()) / 2);//centered horizontally, just above middle vertically
      } else if (MisalignGraphics.gameWon()) {
         g2.setColor(Color.GREEN);
         String winText = "YOU WIN!";
         g2.drawString(winText, (this.getWidth() - fm.stringWidth(winText)) / 2, (this.getHeight() - fm.getHeight()) / 2);               
      }
   }
   
}