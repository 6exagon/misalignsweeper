import java.util.*;
import java.util.stream.*;

public class Line {
   private Tri[] tris;
   private final double VERTICAL_SLOPE = 100000;
   private final double AREA_MAX = 0.015;
   private Point p;
   private Point q;
   private boolean extUp = true;
   private double m;        // slope
   private double b;        // y-intercept
   private boolean inBounds;
   private Point midpoint;
   
   public Line(Point p, Point q) {
      this.p = p;
      this.q = q;
      double denom = q.getX() - p.getX();
      this.m = (denom == 0) ? VERTICAL_SLOPE : (q.getY() - p.getY()) / denom;
      this.b = p.getY() - this.m * p.getX();
      this.tris = new Tri[2];
      this.inBounds = (this.p.isInBounds() || this.q.isInBounds());
      if (!this.inBounds && Math.floor(p.getX()) != Math.floor(q.getX()) && Math.floor(p.getY()) != Math.floor(q.getY())) {
         double wallX = (Math.floor(p.getX()) == Math.ceil(q.getX())) ? Math.floor(p.getX()) : Math.floor(q.getX());
         this.inBounds = (this.at(wallX) > 0 && this.at(wallX) < 1);
      }
      this.midpoint = new Point((this.p.getX() + this.q.getX()) / 2, (this.p.getY() + this.q.getY()) / 2);
   }
   
   //Lines must create triangles from their extended sides only, to prevent overlap
   public boolean isOnExtendedSide(Point pt) {
      return (pt.getY() > this.at(pt.getX())) == this.extUp;
   }
   
   public void extend(boolean up) {
      this.extUp = up;
   }
   
   // Returns y value at given x, ie, line.at(x) = f(x)
   public double at(double x) {
      return x * m + b;
   }
   
   //Returns (squared) distance to Point from line's midpoint
   public double distanceTo(Point pt) {
      return Math.pow(pt.getX() - this.midpoint.getX(), 2) +
             Math.pow(pt.getY() - this.midpoint.getY(), 2);
   }
   
   //Returns the area of triangle formed with Point
   public double areaWith(Point pt) {
      return Math.abs(this.p.getX() * (this.q.getY() - pt.getY())
                    + this.q.getX() * (pt.getY() - this.p.getY())
                    + pt.getX() * (this.p.getY() - this.q.getY())) / 2;
   }

   //Gets new Point on the correct side of the line pulled from the list and not outside of lstack
   public Point getNewPoint(ArrayList<Point> plist, ArrayDeque<Line> lstack) {
      if (this.inBounds) {
         TreeMap<Double, Point> distPoint = new TreeMap<>();
         plist.forEach(p -> distPoint.put(this.distanceTo(p), p));       // keep track of how far away each point is
         while (!distPoint.isEmpty()) {
            Point np = distPoint.remove(distPoint.firstKey());    // go through points closest to farthest
            if (!this.hasPoint(np) && isOnExtendedSide(np) && areaWith(np) < AREA_MAX) {
               if (np.isFresh()) {
                  np.setNotFresh();
                  return np;
               }
               if (lstack.stream().anyMatch(l -> l.hasPoint(np) && (l.hasPoint(this.p) || l.hasPoint(this.q)))) {
                  return np;
               }
            }
         }
      }
      return null;
   }
   
   //Gets line in list that shares points, if applicable
   public Line getSimilarLine(AbstractCollection<Line> lines) {
      return lines.stream().filter(l -> l.hasPoint(this.p) && l.hasPoint(this.q)).findFirst().orElse(null);
   }
   
   public double getM() {
      return m;
   }
   
   public double getB() {
      return b;
   }
   
   public Tri[] getTris() {
      return this.tris;
   }
   
   public boolean isInBounds() {
      return this.inBounds;
   }
   
   //Assumes there will be no more than 2 Tris added
   public void addTri(Tri p) {
      this.tris[(this.tris[0] == null) ? 0 : 1] = p;
   }
   
   public Point getOtherPoint(Point point) {
      return point == this.p ? this.q : this.p;
   }
   
   public boolean hasPoint(Point p) {
      return this.p == p || this.q == p;
   }
   
   public Point getP() {
      return this.p;
   }
   
   public Point getQ() {
      return this.q;
   }
   
   //Returns if the line's points are on either side of an x coordinate
   public boolean spansX(double x) {
      return (x > this.p.getX()) ^ (x > this.q.getX());
   }
   
   //Returns if the line's points are on either side of an y coordinate
   public boolean spansY(double y) {
      return (y > this.p.getY()) ^ (y > this.q.getY());
   }
   
   public int hashCode() {
      return (int) (512 * p.getX()) << 16 + (int) (512 * p.getY())
           + (int) (512 * q.getX()) << 16 + (int) (512 * q.getY());
   }
}
