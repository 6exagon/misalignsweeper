import java.util.*;

public class Point {
   private final double B_RANGE = 0.02;
   private double x, y;
   private boolean inBounds;
   private boolean fresh = true;

   public Point(double x, double y) {
      this.x = x;
      this.y = y;
      this.inBounds = (x > 0 && x < 1 && y > 0 && y < 1);
   }

   public double getX() {
      return this.x;
   }
   
   public double getY() {
      return this.y;
   }
   
   public boolean isInBounds() {
      return this.inBounds;
   }
   
   public boolean isFresh() {
      return this.fresh;
   }
   
   public void setNotFresh() {
      this.fresh = false;
   }
   
   //Point is within B_RANGE of the playfield border (which spans from 0 to 1 in x and y)
   public boolean isNearBorder() {
      double bmin = B_RANGE / 2 - 0.001;
      double bmax = 1 - bmin;
      return (Math.abs(this.x - bmin) < B_RANGE || Math.abs(this.x - bmax) < B_RANGE
           || Math.abs(this.y - bmin) < B_RANGE || Math.abs(this.y - bmax) < B_RANGE);
   }
   
   @Override
   public String toString() {
      return "(" + x + ", " + y + ")"; 
   }
}
