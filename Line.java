import java.util.*;

public class Line {
   private Point[] points;
   private double m;        // slope
   private double b;        // y-intercept
   
   public Line(Point p1, Point p2) {
      try {
         this.points = new Point[] {p1, p2};
         this.m = ((double) p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
      } catch (ArithmeticException ame) {
         this.m = Double.MAX_VALUE;
      }
      this.b = p1.getY() - this.m * p1.getX();
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

   public void reversePoints() {
      Point temp = points[0];
      points[0] = points[1];
      points[1] = temp;
   }
   
   public Point getPoint(int index) {
      return points[index];
   }
   
   public Point getOtherPoint(Point p) {
      if (points[0] == p) {
         return points[1];
      } else if (points[1] == p) {
         return points[0];
      } else {
         return null;
      }
   }
   
   public boolean sharesPointWith(Line other) {
      return (getOtherPoint(other.getPoint(0)) != null || getOtherPoint(other.getPoint(1)) != null);
   }
   
   //Returns if the line's points are on either side of an x coordinate
   public boolean spans(int x) {
      return (x > this.points[0].getX()) ^ (x > this.points[1].getX());
   }
}
