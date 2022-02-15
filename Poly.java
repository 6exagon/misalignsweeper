import java.util.*;
import java.util.stream.Stream;
import javax.swing.*;
import java.awt.*;

public class Poly {
   
   private Tri[] tris;
   private Line[] lines;
   private Point[] points;
   private Point midpoint;
   private int surroundingMines = 0;
   private Visibility visible = Visibility.NORMAL;
   private double polySize;
   
   public Poly(Tri[] tris, Line[] lines) {
      this.tris = tris;
      this.lines = lines;
      this.genPoints();
      this.calcMidpoint();
   }
   
   public boolean containsTri(Tri tri) {
      return Stream.of(tris).anyMatch(tri::equals);
   }
   
   public int numPoints() {
      return this.points.length;
   }
   
   public Point getPoint(int index) {
      return this.points[index];
   }
   
   //Returns -1 if mine, -2 if activated mine, or number of surrounding mines
   public int getDisplayState() {
      return this.surroundingMines;
   }
   
   public void setMine() {
      this.surroundingMines = -1;
   }
   
   public Point getMidpoint() {
      return this.midpoint;
   }
   
   public void drawNum(Graphics2D g2) {
      g2.setColor(Color.WHITE);
      
      int midX = this.midpoint.getX();
      int midY = this.midpoint.getY();
      
      double numSize = polySize;
      numSize *= (2 / 3.0  * Math.min(MisalignGraphics.getXM(), MisalignGraphics.getYM()));
      
      g2.setFont(new Font(g2.getFont().getName(), 
                  g2.getFont().getStyle(), 
                  (int)(numSize)));
      
      midX *= MisalignGraphics.getXM();
      midY *= MisalignGraphics.getYM();
      
      midX -= numSize / 4;  // about 1/2 as wide as tall
      midY += numSize / 2;
      
      g2.drawString(this.surroundingMines + "", midX, midY);
   }
   
   public void drawImageInPoly(Graphics2D g2, Image img) {
      int midX = this.midpoint.getX();
      int midY = this.midpoint.getY();
      
      double imgSize = polySize;
      imgSize *= (2 / 3.0 * Math.min(MisalignGraphics.getXM(), MisalignGraphics.getYM()));
      
      midX *= MisalignGraphics.getXM();
      midY *= MisalignGraphics.getYM();
      
      midX -= imgSize / 2;
      midY -= imgSize / 2;
      
      g2.drawImage(img, midX, midY, (int)imgSize, (int)imgSize, null);
   }
   
   private void calcMidpoint() {
      int midX = 0;
      int midY = 0;
      if (this.tris.length == 1) { // calculates the centroid if a triangle
         Tri tri = this.tris[0];
         Line l1 = new Line(tri.getPoint(0), new Point((tri.getPoint(1).getX() + tri.getPoint(2).getX()) / 2, (tri.getPoint(1).getY() + tri.getPoint(2).getY()) / 2));
         Line l2 = new Line(tri.getPoint(1), new Point((tri.getPoint(0).getX() + tri.getPoint(2).getX()) / 2, (tri.getPoint(0).getY() + tri.getPoint(2).getY()) / 2));
         int intersectX = (int)((l2.getB() - l1.getB()) / (l1.getM() - l2.getM()));
         midX = intersectX;
         midY = l1.at(intersectX);
      } else {    // if not a triangle, just take the average of all the points
         int x = 0, y = 0;
         for (Point p : this.points) {
            x += p.getX();
            y += p.getY();
         }
         midX = x / this.numPoints();
         midY = y / this.numPoints();
      }
      this.midpoint = new Point(midX, midY);
      
      double polyHeight = 0;
      double polyWidth = 0;
      for (Line edge : this.lines) {
         if (edge.spansX(midX))
            polyHeight = Math.abs(polyHeight - edge.at(midX));
         if (edge.spansY(midY))
            polyWidth = Math.abs(polyWidth - (midY - edge.getB()) / edge.getM());
      }
      this.polySize = Math.min(polyHeight, polyWidth);
   }
   
   // Generates and orders the points cyclically for awt Polygon purposes
   private void genPoints() {
      ArrayList<Point> points = new ArrayList<>();
      Point p = lines[0].getP();
      Point prevP = lines[0].getQ();
      Point temp;
      pointLoop:
      do {
         points.add(p);
         for (Line l : lines) {
            if (l.hasPoint(p) && !l.hasPoint(prevP)) {
               prevP = p;
               p = l.getOtherPoint(p);
               continue pointLoop;
            }
         }
         temp = p;
         p = prevP;
         prevP = temp;
      } while (p != lines[0].getP());
      this.points = points.toArray(new Point[0]);
   }
   
   public void reveal() {
      if (this.visible == Visibility.NORMAL) {
         this.visible = Visibility.PRESSED;
         if (this.surroundingMines == -1) {
            if (!MisalignGraphics.playingLossAnimation) {
               this.surroundingMines = -2;
               System.out.println("You lost"); //prints once (not for every mine during animation)
            }
         } else if (this.surroundingMines == 0)
            for (Line l : lines)
               for (Tri t : l.getTris())
                  if (t != null && t.getPoly() != this && t.getPoly() != null)
                     t.getPoly().reveal();
      }
   }
   
   public void flag() {
      if (this.visible == Visibility.FLAG) {
         this.visible = Visibility.NORMAL;
         MisalignSweeper.numFlags++;
      } else if (MisalignSweeper.numFlags != 0 && this.visible == Visibility.NORMAL) {
         this.visible = Visibility.FLAG;
         MisalignSweeper.numFlags--;
      }
      MisalignSweeper.getGraphics().getMineCounter().setText(MisalignSweeper.numFlags + "");
   }
   
   //Updates surrounding mine (should be done once all mines are placed)
   public void updateMines() {
      if (this.surroundingMines < 0)
         return;
      HashSet<Poly> addedPolys = new HashSet<>();
      for (Line l : lines) {
         for (Tri t : l.getTris()) {
            if (t != null) {
               Poly poly = t.getPoly();
               if (poly != null && !addedPolys.contains(poly) && poly != this) {
                  addedPolys.add(poly);
                  if (poly.getDisplayState() == -1)
                     this.surroundingMines++;
               }
            }
         }
      }
   }
   
   //Returns the number of intersections with line extending from point
   public int raycast(int x, int y) {
      return (int) Stream.of(lines).filter(l -> l.spansX(x) && l.at(x) > y).count();
   }
   
   public boolean isPressed() {
      return this.visible == Visibility.PRESSED;
   }
   
   public boolean isFlagged() {
      return this.visible == Visibility.FLAG;
   }
   
   public boolean isNormal() {
      return this.visible == Visibility.NORMAL;
   }
   
   enum Visibility {
      NORMAL,
      PRESSED,
      FLAG;
   }
}