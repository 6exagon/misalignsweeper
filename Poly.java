import java.util.*;

public class Poly {
   private Line[] lines;
   private Point[] points;
   private boolean highlighted;
   
   public Poly(Point[] points) {
      this.points = points;
      getLinesFromPoints();
      this.highlighted = false;
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
   
   public boolean isHighlighted() {
      return this.highlighted;
   }
   
   public void setHighlighted(boolean highlight) {
      this.highlighted = highlight;
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
