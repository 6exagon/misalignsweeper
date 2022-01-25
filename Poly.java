import java.util.*;
import java.awt.*;
import javax.swing.ImageIcon;

public class Poly {
   private Line[] lines;
   private Point[] points;
   private int surroundingMines;
   private Visibility visible;
   private Point midpoint;
   
   //Correctly initializes using Line and list of points to pull from
   public Poly(Line start, ArrayList<Point> pts, HashSet<Point> fps, ArrayDeque<Line> lstack) throws IndexOutOfBoundsException {
      Point pt = start.getNewPoint(pts, fps, lstack);
      if (pt == null) {
         throw new IndexOutOfBoundsException();
      }
      Line l2 = new Line(start.getQ(), pt);
      l2.extend(!l2.isOnExtendedSide(start.getP()));
      Line l3 = new Line(pt, start.getP());
      l3.extend(!l3.isOnExtendedSide(start.getQ()));
      Line[] linearray = {start, l2, l3};
      this.lines = linearray;
      this.addPolysToLines();
      HashSet<Point> pointSet = new HashSet<Point>();
      for (Line l : this.lines) {
         pointSet.add(l.getP());
         pointSet.add(l.getQ());
      }
      this.points = pointSet.toArray(new Point[3]);
      this.surroundingMines = 0;
      this.visible = Visibility.NORMAL;
   }
   
   //Not sure what the "Point" of having both of these methods here is but I'll leave it for now
   public Point getPoint(int index) {
      return this.points[index];
   }
   
   public Point[] getPoints() {
      return this.points;
   }
   
   public Line[] getLines() {
      return this.lines;
   }
   
   //Returns -1 if mine, -2 if activated mine, or number of surrounding mines
   public int getDisplayState() {
      return this.surroundingMines;
   }
   
   public void setMine() {
      this.surroundingMines = -1;
   }
   
   public void drawNum(Graphics2D g2) {
      g2.setColor(Color.WHITE);
      
      int midX = this.midpoint.getX();
      int midY = this.midpoint.getY();
      
      double polyHeight = 0;
      for (Line edge : this.lines)
         if (edge.spans(midX))
            polyHeight = Math.abs(polyHeight - (edge.getM() * midX + edge.getB()));
      
      midX -= polyHeight/6;
      midY += polyHeight*2/9;
      g2.setFont(new Font(g2.getFont().getName(), g2.getFont().getStyle(), (int)(polyHeight * 2/3)));
      
      g2.drawString(this.surroundingMines + "", midX, midY);
   }
   
   public void drawFlag(Graphics2D g2) {
      int midX = this.midpoint.getX();
      int midY = this.midpoint.getY();
      
      double polyHeight = 0;
      for (Line edge : this.lines)
         if (edge.spans(midX))
            polyHeight = Math.abs(polyHeight - (edge.getM() * midX + edge.getB()));
      
      polyHeight *= 2/3.0;
      midX -= polyHeight/2;
      midY -= polyHeight/2;
      
      g2.drawImage(new ImageIcon("flag.png").getImage(), midX, midY, (int)polyHeight, (int)polyHeight, null);
      
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
               for (Poly p : l.getPolys())
                  if (p != this && p != null)
                     p.reveal();
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
      if (this.surroundingMines == 0)
         for (Line l : lines)
            for (Poly p : l.getPolys())
               this.surroundingMines += (p.getDisplayState() == -1) ? 1 : 0;
   }
   
   public void calcMidpoint() {
//       if (this.numPoints() == 3) { // calculates the centroid if a triangle
         Line l1 = new Line(getPoint(0), new Point((getPoint(1).getX() + getPoint(2).getX()) / 2, (getPoint(1).getY() + getPoint(2).getY()) / 2));
         Line l2 = new Line(getPoint(1), new Point((getPoint(0).getX() + getPoint(2).getX()) / 2, (getPoint(0).getY() + getPoint(2).getY()) / 2));
         double intersectX = (l2.getB() - l1.getB()) / (l1.getM() - l2.getM());
         this.midpoint = new Point((int)intersectX, (int)(l1.getM() * intersectX + l1.getB()));
//       } else {    // if not a triangle, just take the average of all the points
//          int x = 0, y = 0;
//          for (Point p : this.points) {
//             x += p.getX();
//             y += p.getY();
//          }
//          this.midpoint = new Point(x / this.numPoints(), y / this.numPoints());
//       }
   }
   
   // Adds references to Polys in Lines
  public void addPolysToLines() {
     for (Line l : this.lines)
        l.addPoly(this);
   }
   
   //Faster than any iteration, conversion to ArrayList, etc.
   public boolean hasLine(Line line) {
      return (lines[0] == line || lines[1] == line || lines[2] == line);
   }
   
   //Returns number of intersections with line extending from point
   public int raycast(int x, int y) {
      int intersections = 0;
      for (Line l : this.lines) {
         if (l.spans(x) && l.getM() * x + l.getB() > y) {
            intersections++;
         }
      }
      return intersections;
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
