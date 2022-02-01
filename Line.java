import java.util.*;
import java.util.stream.*;

public class Line {
   private Tri[] tris;
   private final double VERTICAL_SLOPE = 100000;
   private final int AREA_MAX = 5000;
   private Point p;
   private Point q;
   private boolean extUp = true;
   private double m;        // slope
   private double b;        // y-intercept
   
   public Line(Point p, Point q) {
      this.p = p;
      this.q = q;
      double denom = q.getX() - p.getX();
      this.m = denom == 0 ? VERTICAL_SLOPE : (q.getY() - p.getY()) / denom;
      this.b = p.getY() - this.m * p.getX();
      this.tris = new Tri[2];
   }
   
   //Lines must create triangles from their extended sides only, to prevent overlap
   public boolean isOnExtendedSide(Point pt) {
      return (pt.getY() > this.at(pt.getX())) == this.extUp;
   }
   
   public void extend(boolean up) {
      this.extUp = up;
   }
   
   public boolean getExt() {
      return this.extUp;
   }
   
   // returns y value at given x, ie, line.at(x) = f(x)
   public int at(int x) {
      return (int) (x * m + b);
   }
   
   //Returns sum of distances to Point from line ends
   public int distanceTo(Point pt) {
      return (int) (Math.hypot(pt.getX() - this.p.getX(), pt.getY() - this.p.getY())
                  + Math.hypot(pt.getX() - this.q.getX(), pt.getY() - this.q.getY()));
   }
   
   //Returns area of triangle formed with Point
   public int areaWith(Point pt) {
      return Math.abs((this.p.getX() * (this.q.getY() - pt.getY())
                     + this.q.getX() * (pt.getY() - this.p.getY())
                     + pt.getX() * (this.p.getY() - this.q.getY())) / 2);
   }

   //Gets new Point on the correct side of the line pulled from the list and not outside of lstack
   public Point getNewPoint(ArrayList<Point> plist, HashSet<Point> freshPoints, ArrayDeque<Line> lstack) {
      TreeMap<Integer, Point> distPoint = new TreeMap<>();
      for (Point p : plist)
         distPoint.put(this.distanceTo(p), p);       // keep track of how far away each point is
      for (int i = 1; i < 6; i++) {
         Point np = distPoint.remove(distPoint.firstKey());
         int area = areaWith(np);
         if (isOnExtendedSide(np) && 0 < area && area < AREA_MAX)
            if (freshPoints.remove(np) || 
               lstack.stream().anyMatch(l -> l.hasPoint(np) && (l.hasPoint(this.p) || l.hasPoint(this.q))))
               return np;
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
   
   //Assumes there will be no more than 2 Polys added
   public void addTri(Tri p) {
      this.tris[(this.tris[0] == null) ? 0 : 1] = p;
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
   public boolean spans(int x) {
      return (x > this.p.getX()) ^ (x > this.q.getX());
   }
}
