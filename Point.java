import java.util.*;

public static class Point {
   public ArrayList<Line> lines = new ArrayList<>();
   public Point[] nears = new Point[NUM_NEARS];
   public int x,y;

   public Line getLineWith(Point p2) {
      for (Line line : lines) {
         if (line.getOtherPoint(this).equals(p2)) return line;
      }
      return null;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Point point = (Point) o;
      return x == point.x && y == point.y && Objects.equals(lines, point.lines) && Arrays.equals(nears, point.nears);
   }

   public Point(int x, int y) {
      this.x = x; this.y = y;
   }
}