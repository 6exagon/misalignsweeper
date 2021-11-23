public class Line {
   public Point[] points;
   public double m;        // slope
   public double b;        // y-intercept
   
   public Line(Point p1, Point p2) {
      try {
         this.points = new Point[] {p1, p2};
         this.m = ((double)p2.y - p1.y) / (p2.x - p1.x);
      } catch (ArithmeticException ame) {
         this.m = Double.MAX_VALUE;
      }
      this.b = p1.y - this.m * p1.x;
   }
   
   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Line other = (Line) o;
      return MisalignSweeper.areArraysEqualDisorderly(points, other.points);
   }

   public void reversePoints() {
      Point temp = points[0];
      points[0] = points[1];
      points[1] = temp;
   }

   public Point getOtherPoint(Point p) {
      return points[0] == p ? points[1] : points[0];
   }
   
   //Returns if the line's points are on either side of an x coordinate
   public boolean spans(int x) {
      return (x > this.points[0].x) ^ (x > this.points[1].x);
   }
}
