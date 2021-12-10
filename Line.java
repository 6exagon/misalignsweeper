import java.util.*;

public class Line {
   private ArrayList<Poly> polys;
   private Point[] points;
   private double m;        // slope
   private double b;        // y-intercept
   
   public Line(Point p1, Point p2) {
      this.points = new Point[] {p1, p2};
      double denom = p2.getX() - p1.getX();
      if (denom == 0)
         this.m = Double.MAX_VALUE;
      else
         this.m = (p2.getY() - p1.getY()) / denom;
      this.b = p1.getY() - this.m * p1.getX();
      if (m == Double.MAX_VALUE || Double.isNaN(b) || !Double.isFinite(b))
         this.b = Double.MAX_VALUE;
      this.polys = new ArrayList<>();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Line other = (Line) o;
      return MisalignSweeper.areArraysEqualDisorderly(points, other.points);
   }
   
   public double getM() {
      return m;
   }
   
   public double getB() {
      return b;
   }
   
   public ArrayList<Poly> getPolys() {
      return polys;
   }
   
   public Point getPoint(int index) {
      return points[index];
   }
   
   public Point getOtherPoint(Point p) {
      if (points[0] == p)
         return points[1];
      else if (points[1] == p)
         return points[0];
      return null;
   }
   
   public boolean sharesPointWith(Line other) {
      return getOtherPoint(other.getPoint(0)) != null || getOtherPoint(other.getPoint(1)) != null;
   }
   
   //Returns if the line's points are on either side of an x coordinate
   public boolean spans(int x) {
      return (x > this.points[0].getX()) ^ (x > this.points[1].getX());
   }
}
