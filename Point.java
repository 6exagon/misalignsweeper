import java.util.*;

public class Point {
   // private ArrayList<Line> lines = new ArrayList<>();
   private int x, y;

   public Point(int x, int y) {
      this.x = x;
      this.y = y;
   }

   public boolean validate(AbstractCollection<Line> linelist) {
      for (Line l : linelist) {
         if (this == l.getP() || this == l.getQ()) {
            return true;
         }
      }
      return false;
   }
   
   // @Override
   // public boolean equals(Object o) {
   //    if (this == o) return true;
   //    if (o == null || getClass() != o.getClass()) return false;
   //    Point point = (Point) o;
   //    return x == point.x && y == point.y;
   // }
   
   public int getX() {
      return this.x;
   }
   
   public int getY() {
      return this.y;
   }
   
   // public ArrayList<Line> getLines() {
   //    return this.lines;
   // }
   
   // public Line getLineWith(Point p2) {
   //    for (Line line : lines)
   //       if (line.getOtherPoint(this).equals(p2))
   //          return line;
   //    return null;
   // }
   
   @Override
   public String toString() {
      return "(" + x + ", " + y + ")"; 
   }
   
//    // Compares by y
//    @Override
//    public int compareTo(Point other) {
//       if (this.equals(other)) return 0;
//       return other.y > this.y ? -1 : 1;
//    }
}
