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

   public Poly(Line[] lines, Point[] points) {
      this.lines = lines;
      this.points = points;
   }
}
