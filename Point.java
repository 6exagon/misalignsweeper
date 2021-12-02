import java.util.*;

public class Point {
   private ArrayList<Line> lines = new ArrayList<>();
   private int x, y;

   public Point(int x, int y) {
      this.x = x;
      this.y = y;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Point point = (Point) o;
      return x == point.x && y == point.y; //&& Objects.equals(lines, point.lines) && Arrays.equals(nears, point.nears);
   }
   
   public int getX() {
      return this.x;
   }
   
   public int getY() {
      return this.y;
   }
   
   public ArrayList<Line> getLines() {
      return this.lines;
   }
   
   public Line getLineWith(Point p2) {
      for (Line line : lines) {
         if (line.getOtherPoint(this).equals(p2)) return line;
      }
      return null;
   }
}
