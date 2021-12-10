import java.util.*;

public class Poly {
   private Line[] lines;
   private Point[] points;
   private boolean highlighted;
   
   public Poly(Point[] points) {
      this.points = points;
      getLinesFromPoints();
      addPolysToLines();
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
   
   public Point[] getPoints() {
      return this.points;
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
         if (i == this.points.length - 1)
            this.lines[i] = new Line(this.points[i], this.points[0]);
         else
            this.lines[i] = new Line(this.points[i], this.points[i+1]);
      }
   }
   
   // Adds references to Polys in Lines
   public void addPolysToLines() {
      for (Line l : this.lines)
         l.getPolys().add(this);
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
}
