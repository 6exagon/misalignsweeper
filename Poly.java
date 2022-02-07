import java.util.*;
import java.util.stream.Stream;
import javax.swing.*;
import java.awt.*;

public class Poly {
   
   private final Image flagImage = new ImageIcon(getClass().getResource("flag.png")).getImage();
   private Tri[] tris;
   private Line[] lines;
   private Point[] points;
   private Point midpoint;
   private int surroundingMines = 0;
   private Visibility visible = Visibility.NORMAL;
   
   public Poly(Tri[] tris, Line[] lines) {
      this.tris = tris;
      this.lines = lines;
      HashSet<Point> points = new HashSet<>();
      for (Line l : lines) {
         points.add(l.getP());
         points.add(l.getQ());
      }
      this.points = points.toArray(new Point[points.size()]);
      this.calcMidpoint();
      this.sortPoints();
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
      
      double polyHeight = 0;
      for (Line edge : this.lines)
         if (edge.spans(midX))
            polyHeight = Math.abs(polyHeight - edge.at(midX));
      
      midX -= polyHeight/6;
      midY += polyHeight*2/9;
      g2.setFont(
         new Font(g2.getFont().getName(), 
                  g2.getFont().getStyle(), 
                  (int)(polyHeight * 2/3  * Math.min(MisalignGraphics.getXM(), MisalignGraphics.getYM()))));
      
      midX *= MisalignGraphics.getXM();
      midY *= MisalignGraphics.getYM();
      
      g2.drawString(this.surroundingMines + "", midX, midY);
   }
   
   public void drawFlag(Graphics2D g2) {
      int midX = this.midpoint.getX();
      int midY = this.midpoint.getY();
      
      double polyHeight = 0;
      for (Line edge : this.lines)
         if (edge.spans(midX))
            polyHeight = Math.abs(polyHeight - edge.at(midX));
      
      polyHeight *= 2/3.0 * Math.min(MisalignGraphics.getXM(), MisalignGraphics.getYM());
      midX -= polyHeight/2;
      midY -= polyHeight/2;
      
      midX *= MisalignGraphics.getXM();
      midY *= MisalignGraphics.getYM();
      
      g2.drawImage(flagImage, midX, midY, (int)polyHeight, (int)polyHeight, null);
   }
   
   private void calcMidpoint() {
      if (this.tris.length == 1) { // calculates the centroid if a triangle
         Tri tri = this.tris[0];
         Line l1 = new Line(tri.getPoint(0), new Point((tri.getPoint(1).getX() + tri.getPoint(2).getX()) / 2, (tri.getPoint(1).getY() + tri.getPoint(2).getY()) / 2));
         Line l2 = new Line(tri.getPoint(1), new Point((tri.getPoint(0).getX() + tri.getPoint(2).getX()) / 2, (tri.getPoint(0).getY() + tri.getPoint(2).getY()) / 2));
         int intersectX = (int)((l2.getB() - l1.getB()) / (l1.getM() - l2.getM()));
         this.midpoint = new Point(intersectX, l1.at(intersectX));
      } else {    // if not a triangle, just take the average of all the points
         int x = 0, y = 0;
         for (Point p : this.points) {
            x += p.getX();
            y += p.getY();
         }
         this.midpoint = new Point(x / this.numPoints(), y / this.numPoints());
      }
   }
   
   // orders the points cyclically for awt Polygon purposes
   private void sortPoints() {
      TreeMap<Double, Point> angleToPoint = new TreeMap<>();
      for (Point p : this.points)
         angleToPoint.put(this.getAngle(p), p);
      this.points = angleToPoint.values().toArray(new Point[this.points.length]);
      for (int i = 0; i < this.points.length; i++) {
         Point p1 = this.points[i];
         Point p2 = this.points[(i + 1) % this.points.length];
         if (Stream.of(this.lines).anyMatch(l -> l.hasPoint(p1) && l.hasPoint(p2)))
            continue;
         this.points[(i + 1) % this.points.length] = this.points[(i + 2) % this.points.length];
         this.points[(i + 2) % this.points.length] = p2;
      }
   }
   
   private double getAngle(Point p) {
      double base = Math.abs(Math.atan((double)(p.getY() - this.midpoint.getY()) / (p.getX() - this.midpoint.getX())));
      // The inverse tangent only goes between 0 and pi/2, so we need to find out what quadrant it's in and change it.
      return p.getY() < this.midpoint.getY()
              ? (p.getX() < this.midpoint.getX() ? Math.PI - base : base)                 // 2nd and 1st Quadrants
              : (p.getX() < this.midpoint.getX() ? Math.PI + base : 2 * Math.PI - base);  // 3rd and 4th Quadrants
   }
   
   public void reveal() {
      if (this.visible == Visibility.NORMAL) {
         this.visible = Visibility.PRESSED;
         if (this.surroundingMines == -1) {
            this.surroundingMines = -2;
            System.out.println("You lost");
            //lose game
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
   }
   
   //Updates surrounding mine (should be done once all mines are placed)
   public void updateMines() {
      for (Line l : lines)
         for (Tri t : l.getTris())
            if (t != null && t.getPoly() != null && t.getPoly() != this)
               this.surroundingMines += (t.getPoly().getDisplayState() == -1) ? 1 : 0;
   }
   
   //Returns the number of intersections with line extending from point
   public int raycast(int x, int y) {
      return (int) Stream.of(lines).filter(l -> l.spans(x) && l.at(x) > y).count();
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