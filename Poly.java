import java.util.*;

public class Poly {
   private Line[] lines;
   private Point[] points;
   private int surroundingMines;
   private int visible;
   
   public Poly(Point[] points) {
      this.points = points;
      getLinesFromPoints();
      this.surroundingMines = 0;
      this.visible = 0;
   }
   
   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Poly other = (Poly) o;
      return MisalignSweeper.areArraysEqualDisorderly(lines, other.lines);
   }
   
   public Point getPoint(int index) {
      return points[index];
   }
   
   public int numPoints() {
      return points.length;
   }
   
   //Returns -1 if mine, -2 if activated mine, or number of surrounding mines
   public int getDisplayState() {
      return this.surroundingMines;
   }
   
   public void setMine() {
      this.surroundingMines = -1;
   }
   
   public boolean reveal() {
      if (this.visible == 0) {
         this.visible = 1;
         if (this.surroundingMines == -1) {
            this.surroundingMines = -2;
         }
      }
   }
   
   //Updates surrounding mine (should be done once all mines are placed)
   public void updateMines() {
      if (this.surroundingMines == 0) {
         for (Line l : lines) {
            for (Poly p : l.getPolys()) {
               this.surroundingMines += (p.getDisplayState() == -1) ? 1 : 0;
            }
         }
      }
   }
   
   public void getLinesFromPoints() {
      this.lines = new Line[this.points.length];
      for (int i = 0; i < this.points.length; i++) {
         if (i == this.points.length - 1) {
            this.lines[i] = new Line(this.points[i], this.points[0]);
         } else {
            this.lines[i] = new Line(this.points[i], this.points[i+1]);
         }
      }
   }
   
   public boolean hasLine(Line line) {
      for (Line x : lines) {
         if (line.equals(x)) {
            return true;
         }
      }
      return false;
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
}
