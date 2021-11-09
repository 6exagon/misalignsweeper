import java.util.*;

public class Poly {
   public Line[] lines;
   public Point[] points;
      
   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Poly other = (Poly) o;
      return MisalignSweeper.areArraysEqualDisorderly(lines, other.lines);
   }
   
   public Point[] getPointsFromLines(Line[] lines) {
      ArrayList<Point> ps = new ArrayList<>();
      for (int i = 0; i < lines.length; i++) {
         Line l = lines[i];
         if (!ps.contains(l.points[0])) ps.add(l.points[0]);
         if (!ps.contains(l.points[1])) ps.add(l.points[1]);         
      }
      return ps.toArray(new Point[0]);
   }

   public Poly(Line[] lines) {
      this.lines = lines;
      this.points = getPointsFromLines(lines);
   }
}
