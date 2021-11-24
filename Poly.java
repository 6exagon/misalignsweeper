import java.util.*;

public class Poly {
   public Line[] lines;
   public Point[] points;
   public boolean highlighted;
   
   public Poly(Line[] lines) {
      this.lines = lines;
      //this.points = getPointsFromLines(lines);
      this.highlighted = false;
   }
   
   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Poly other = (Poly) o;
      return MisalignSweeper.areArraysEqualDisorderly(lines, other.lines);
   }
   
   // Unused currently
   public Point[] getPointsFromLines(Line[] lines) {
      ArrayList<Point> ps = new ArrayList<>();
      for (int i = 0; i < lines.length; i++) {
         Line l = lines[i];
         if (!ps.contains(l.points[0])) ps.add(l.points[0]);
         if (!ps.contains(l.points[1])) ps.add(l.points[1]);        
      }
      return ps.toArray(new Point[0]);
   }
   
   
   //Returns number of intersections with line extending from point
   public int raycast(int x, int y) {
      int intersections = 0;
      for (Line l : this.lines) {
         if (l.spans(x) && l.m * x + l.b > y) {
            intersections++;
         }
      }
      return intersections;
   }
}
