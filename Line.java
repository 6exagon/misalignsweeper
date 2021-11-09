public class Line {
   public Point[] points;
      
   public Line(Point p1, Point p2) {
      this.points = new Point[] {p1, p2};
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
}
