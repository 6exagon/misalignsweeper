import java.util.*;

public class Point {
   // private ArrayList<Line> lines = new ArrayList<>();
   private int x, y;

   public Point(int x, int y) {
      this.x = x;
      this.y = y;
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
