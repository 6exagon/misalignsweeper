import java.util.*;

public class Line {
   private Poly[] polys;
   private final double VERTICAL_SLOPE = 100000;
   private final int AREA_MAX = 5000;
   private Point p;
   private Point q;
   private boolean extUp;
   private double m;        // slope
   private double b;        // y-intercept
   
   public Line(Point p, Point q) {
      this.p = p;
      this.q = q;
      this.extUp = true;
      double denom = q.getX() - p.getX();
      if (denom == 0)
         this.m = VERTICAL_SLOPE;
      else
         this.m = (q.getY() - p.getY()) / denom;
      this.b = p.getY() - this.m * p.getX();
      this.polys = new Poly[2];
   }
   
   //Lines must create triangles from their extended sides only, to prevent overlap
   public boolean isOnExtendedSide(Point pt) {
      return (pt.getY() > this.m * pt.getX() + this.b) == this.extUp;
   }
   
   public void extend(boolean up) {
      this.extUp = up;
   }
   
   public boolean getExt() {
      return this.extUp;
   }
   
   //Returns sum of distances to Point from line ends
   public int distanceTo(Point pt) {
      return (int) (Math.pow(Math.pow(pt.getX() - this.p.getX(), 2) + Math.pow(pt.getY() - this.p.getY(), 2), 0.5)
         + Math.pow(Math.pow(pt.getX() - this.q.getX(), 2) + Math.pow(pt.getY() - this.q.getY(), 2), 0.5));
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
      for (int i = 0; i < plist.size(); i++)
         distPoint.put(this.distanceTo(plist.get(i)), plist.get(i));       // keep track of how far away each point is
      for (int topchoice : distPoint.keySet()) {
         Point np = distPoint.get(topchoice);
         int area = areaWith(np);
         if (isOnExtendedSide(np) && area < AREA_MAX && area > 0) {
            if (freshPoints.remove(np)) {
               return np;
            } else if (np.validate(lstack)) {
               Line testl1 = new Line(this.q, np);
               Line testl2 = new Line(np, this.p);
               if (testl1.getSimilarLine(lstack) != null || testl2.getSimilarLine(lstack) != null) {
                  return np;
               }
            }
         }
      }
      return null;
   }
   
   //Gets line in list that shares points, if applicable
   public Line getSimilarLine(AbstractCollection<Line> linelist) {
      for (Line l : linelist) {
         if ((this.p == l.getP() && this.q == l.getQ())
            || (this.p == l.getQ() && this.q == l.getP())) {
            return l;
         }
      }
      return null;
   }
   
   public double getM() {
      return m;
   }
   
   public double getB() {
      return b;
   }
   
   public Poly[] getPolys() {
      return this.polys;
   }
   
   //Assumes there will be no more than 2 Polys added
   public void addPoly(Poly p) {
      this.polys[(this.polys[0] == null) ? 0 : 1] = p;
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
