import java.util.*;
import java.awt.*;

public class Poly {
   private Line[] lines;
   private ArrayList<Point> points;
   private int surroundingMines;
   private Visibility visible;
   private Point midpoint;
   
   public Poly(ArrayList<Point> points) {
      this.points = points;
      getLinesFromPoints();
      calcMidpoint();
      this.surroundingMines = 0;
      if (new Random().nextInt(10) == 0) this.surroundingMines = -1;  // CHANGE ME
      this.visible = Visibility.NORMAL;
   }
   
   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Poly other = (Poly) o;
      return MisalignSweeper.areArraysEqualDisorderly(lines, other.lines);
   }
   
   public Point getPoint(int index) {
      return points.get(index);
   }
   
   public ArrayList<Point> getPoints() {
      return this.points;
   }
   
   public int numPoints() {
      return points.size();
   }
   
   //Returns -1 if mine, -2 if activated mine, or number of surrounding mines
   public int getDisplayState() {
      return this.surroundingMines;
   }
   
   public void setMine() {
      this.surroundingMines = -1;
   }
   
   public void drawNum(Graphics2D g2) {
      g2.setColor(Color.black);
      int pt = g2.getFont().getSize();
      g2.drawString(this.surroundingMines + "", this.midpoint.getX() - pt / 4, this.midpoint.getY() + pt / 2);
   }  
   
   public void reveal() {
      if (this.visible == Visibility.NORMAL) {
         this.visible = Visibility.PRESSED;
         if (this.surroundingMines == -1) {
            this.surroundingMines = -2;
         }
      }
   }
   
   //Updates surrounding mine (should be done once all mines are placed)
   public void updateMines() {
      if (this.surroundingMines == 0) {
         for (Line l : lines) {
            for (Poly p : l.getPolys())
               this.surroundingMines += (p.getDisplayState() == -1) ? 1 : 0;
         }
      }
   }
   
   public void calcMidpoint() {
      if (this.numPoints() == 3) { // calculates the centroid if a triangle
         Line l1 = new Line(getPoint(0), new Point((getPoint(1).getX() + getPoint(2).getX()) / 2, (getPoint(1).getY() + getPoint(2).getY()) / 2));
         Line l2 = new Line(getPoint(1), new Point((getPoint(0).getX() + getPoint(2).getX()) / 2, (getPoint(0).getY() + getPoint(2).getY()) / 2));
         double intersectX = (l2.getB() - l1.getB()) / (l1.getM() - l2.getM());
         this.midpoint = new Point((int)intersectX, (int)(l1.getM() * intersectX + l1.getB()));
      } else {    // if not a triangle, just take the average of all the points
         int x = 0, y = 0;
         for (Point p : this.points) {
            x += p.getX();
            y += p.getY();
         }
         this.midpoint = new Point(x / this.numPoints(), y / this.numPoints());
      }
   }
   
   public void getLinesFromPoints() {
      this.lines = new Line[this.points.size()];
      for (int i = 0; i < this.lines.length; i++) {
         Line l;
         if (i == this.points.size() - 1) {
            l = new Line(this.points.get(i), this.points.get(0));
         } else {
            l = new Line(this.points.get(i), this.points.get(i+1));
         }
         if (MisalignSweeper.lines.contains(l)) {
            this.lines[i] = MisalignSweeper.lines.get(MisalignSweeper.lines.indexOf(l));
         }
      }
   }
   
   // Adds references to Polys in Lines
   public void addPolysToLines() {
      for (Line l : this.lines) {
         l.getPolys().add(this);
      }
      
   }
   
   public boolean hasLine(Line line) {
      return Arrays.asList(lines).contains(line);
   }
   
   //Returns number of intersections with line extending from point
   public int raycast(int x, int y) {
      int intersections = 0;
      for (Line l : this.lines)
         if (l.spans(x) && l.getM() * x + l.getB() > y)
            intersections++;
      return intersections;
   }
   
   public boolean isPressed() {
      return this.visible == Visibility.PRESSED;
   }
   
   public boolean isFlagged() {
      return this.visible == Visibility.FLAG;
   }
   
   enum Visibility {
      NORMAL,
      PRESSED,
      FLAG;
   }
}
