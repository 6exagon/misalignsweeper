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
      for (Tri t : tris)
         t.addPoly(this);
      this.tris = tris;
      this.lines = lines;
      this.genPoints();
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
      int midX = (int) (this.midpoint.getX() * MisalignGraphics.getXM());
      int midY = (int) (this.midpoint.getY() * MisalignGraphics.getYM());
      
      int numSize = (int)(polySize * Math.min(MisalignGraphics.getXM(), MisalignGraphics.getYM()));
      
      g2.setFont(new Font(g2.getFont().getName(), 
                  g2.getFont().getStyle(), 
                  numSize));
      
      midX -= numSize / 4;  // about 1/2 as wide as tall
      midY += numSize / 2;
      
      g2.drawString(this.surroundingMines + "", midX, midY);
   }
   
   public void drawImageInPoly(Graphics2D g2, Image img) {
      int midX = (int) (this.midpoint.getX() * MisalignGraphics.getXM());
      int midY = (int) (this.midpoint.getY() * MisalignGraphics.getYM());
      
      int imgSize = (int)(polySize * Math.min(MisalignGraphics.getXM(), MisalignGraphics.getYM()));
      
      midX -= imgSize / 2;
      midY -= imgSize / 2;
      
      g2.drawImage(img, midX, midY, imgSize, imgSize, null);
   }
   
   // Finds the middle of the Poly for rendering things
   public void calcMidpoint() {
      Point centroid = calcCentroid(this.points);
      Optional<Tri> bigTri = Optional.empty();  // keep track of biggest tri, if it uses it, for size purposes
      if (Misalignsweeper.getClickedPoly(centroid.getX(), centroid.getY()) != this) {
         bigTri = Stream.of(tris).max((t, t2) -> Double.compare(t.area(), t2.area()));
         centroid = calcCentroid(bigTri.get().getPoints());
      }
      this.midpoint = centroid;

      double midX = this.midpoint.getX();
      double midY = this.midpoint.getY();
      
      // Calculate poly's size
      double polyHeight = 0;
      double polyWidth = 0;
      for (Line edge : this.lines) {
         if (edge.spansX(midX))
            polyHeight = Math.abs(polyHeight - edge.at(midX));
         if (edge.spansY(midY))
            polyWidth = Math.abs(polyWidth - (midY - edge.getB()) / edge.getM());
      }
      this.polySize = Math.min(polyHeight, polyWidth) * 2 / 3.0;
   }
   
   // Calculates the average of all the points
   public static Point calcCentroid(Point[] ps) {
      double x = 0, y = 0;
      for (Point p : ps) {
         x += p.getX() / ps.length;
         y += p.getY() / ps.length;
      }
      return new Point(x, y);
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
         if (Misalignsweeper.firstClick) {
            Misalignsweeper.firstClick = false;
            Misalignsweeper.generateMines(this);
         }
         this.visible = Visibility.PRESSED;
         if (this.surroundingMines == -1) {
            if (!MisalignGraphics.playingLossAnimation) {
               this.surroundingMines = -2;
               System.out.println("You lost"); //prints once (not for every mine during animation)
            }
         } else if (this.surroundingMines == 0)
            for (Line l : lines)
               for (Tri t : l.getTris())
                  if (t != null)
                     t.getPoly().reveal();
      }
   }
   
   public void flag() {
      if (this.visible == Visibility.FLAG) {
         this.visible = Visibility.NORMAL;
         Misalignsweeper.numFlags++;
      } else if (Misalignsweeper.numFlags != 0 && this.visible == Visibility.NORMAL) {
         this.visible = Visibility.FLAG;
         Misalignsweeper.numFlags--;
      }
      MisalignGraphics.getMineCounter().setText(Misalignsweeper.numFlags + "");
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
   public int raycast(double x, double y) {
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