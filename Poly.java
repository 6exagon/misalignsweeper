import java.util.*;

public class Poly {
   private Line[] lines;
   private ArrayList<Point> points;
   
   public Poly(Line[] lines, ArrayList<Point> points) {
      this.lines = lines;
      this.points = points;
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
   
   public int numPoints() {
      return points.size();
   }
   
   public boolean hasLine(Line line) {
      for (Line x : lines) {
         if (line == x) {
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
