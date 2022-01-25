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
   
   public int getX() {
      return this.x;
   }
   
   public int getY() {
      return this.y;
   }
   
   @Override
   public String toString() {
      return "(" + x + ", " + y + ")"; 
   }
}
