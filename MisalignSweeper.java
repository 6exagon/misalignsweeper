import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class MisalignSweeper {

   public static final int NUM_points = 50;
   public static final int HEIGHT = 256;
   public static final int WIDTH = 256;
   public static final int MIN_DIST = 15;
   public static final int NUM_NEARS = 6;
   
   private static final ArrayList<Poly> polys = new ArrayList<>();
   private static final ArrayList<Line> lines = new ArrayList<>();
   private static final ArrayList<Point> points = new ArrayList<>();
   private static final HashMap<Poly, Polygon> polyToGon = new HashMap<>();  // Doesn't actually need to be a map, but it'll prob be useful in future.
   private static MisalignGraphics graphics;
   
   public static void create() {
      MisalignInput input = new MisalignInput();
      Random rand = new Random();
      generateBoard(rand);
      graphics = new MisalignGraphics(lines, polyToGon);
      graphics.createAndShowGUI(input, rand);
   }
   
   public static void repaint() {
      graphics.frame.repaint();
   }
   
   public static void generateBoard(Random rand) {
      points.clear();
      lines.clear();
      polys.clear();
      polyToGon.clear();
      
      generatePoints(rand);
      generateLines();
      generatePolys();
      generateAWTPolygons();
   }

   public static void generatePoints(Random rand) {
      int num = 0;
      addEdgeAndCornerPoints(rand);
      for (int i = 16; i < 50; i++) {
         if (num >= 100000) { // to prevent "crashes"
            generateBoard(rand);
            return;
         } 
         Point p = new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT));
         boolean farEnough = true;
         for (Point p2 : points) {
            if (getDistance(p, p2) < Math.pow(MIN_DIST, 2)) farEnough = false;
         }
         if (!farEnough) {
            i--;   // if it's not far enough away, it decrements, effectively just running through this 'i' again until it is far enough.
            num++;
         } else {
            points.add(p);
         }
      }
   }

   public static void generateLines() {
      for (Point p : points) {
         Map<Integer, Point> distPoint = new HashMap<>();
         int[] dists = new int[NUM_points];
         for (int i = 0; i < NUM_points; i++) {
            dists[i] = getDistance(p, points.get(i));     // get the distance to each point
            distPoint.put(dists[i], points.get(i));       // keep track of how far away each point is
         }
         Arrays.sort(dists);     // sort the distances in ascending order
         for (int i = 1; i <= NUM_NEARS; i++) {          // dists[0] is itself
            Point ithNear = distPoint.get(dists[i]);
            Line l = new Line(p, ithNear);                        // Add the newly-made Line object to:
            if (!intersects(l)) {
               if         (!lines.contains(l)) lines.add(l);            // the class-wide list of all lines
               if       (!p.getLines().contains(l)) p.getLines().add(l);          // the first point's list of lines
               if (!ithNear.getLines().contains(l)) ithNear.getLines().add(l);    // the second point's list of lines
            }
         }
      }
      removeLoneLines();
   }

   public static void generatePolys() {
      for (Point p : points) {                                 // For each point,
         for (Line startLine : p.getLines()) {                      // and each line coming off of that point,
            ArrayList<Line> linesInPoly = new ArrayList<>();   // move around to the next line counter-clockwise of that line.
            ArrayList<Point> pointsInPoly = new ArrayList<>();
            pointsInPoly.add(p);
            linesInPoly.add(startLine);                        // Once you reach the original point, you'll have made a complete loop.
            Line line;                                         // This is then saved as a polygon (Poly).
            Point temp;
            Point point = p;
            Point other = startLine.getOtherPoint(point);
            while (!pointsInPoly.contains(other)) {
               pointsInPoly.add(other);
               point = getNextCounterClockwisePoint(other, point);
               line = point.getLineWith(other);
               linesInPoly.add(line);
               temp = point;
               point = other;
               other = temp;
            }
            ArrayList<Point> toBeRemoved = new ArrayList<>();
            for (Point pointerino : pointsInPoly) {
               if (pointerino.equals(other)) break;
               toBeRemoved.add(pointerino);
            }
            for (Point dead : toBeRemoved) {
               pointsInPoly.remove(dead);
            }
            
            Poly poly = new Poly(linesInPoly.toArray(new Line[linesInPoly.size()]), pointsInPoly);
            if (!polys.contains(poly) && poly.numPoints() < 20) {
               polys.add(poly);   // if the poly is too big, it doesn't add it
            }
         }                                                                          // as it's probably wrapped all around the board.
      }
   }

   // Converts a Poly into a renderable Polygon
   public static void generateAWTPolygons() {
      for (Poly poly : polys) {
         int num = poly.numPoints();
         int[] x = new int[num];
         int[] y = new int[num];
         for (int i = 0; i < num; i++) {
            Point p = poly.getPoint(i);
            x[i] = p.getX();
            y[i] = p.getY();
         }
         Polygon gon = new Polygon(x, y, num);
         polyToGon.put(poly, gon);
      }
   }
   
   public static void addEdgeAndCornerPoints(Random rand) {
      points.add(new Point(0, 0));
      points.add(new Point(0, HEIGHT));
      points.add(new Point(WIDTH, 0));
      points.add(new Point(WIDTH, HEIGHT));
      
      for (int i = 1; i <= 3; i++) {
         points.add(new Point(0,     rand.nextInt(HEIGHT / 3) + HEIGHT * (i - 1) / 3));
         points.add(new Point(WIDTH, rand.nextInt(HEIGHT / 3) + HEIGHT * (i - 1) / 3));
         points.add(new Point(rand.nextInt(WIDTH / 3) + WIDTH * (i - 1) / 3, 0));
         points.add(new Point(rand.nextInt(WIDTH / 3) + WIDTH * (i - 1) / 3, HEIGHT));
      }
   }
   
   //Returns the polygon surrounding a coordinate pair
   public static Poly getClickedPolygon(int x, int y) {
      Map<Integer, Line> distLine = new HashMap<>();
      for (Line l : lines) {
         if (l.spans(x)) {
            distLine.put((int) (Math.abs(l.getM() * x + l.getB() - y)), l);
         }
      }
      Object[] sortedDists = distLine.keySet().toArray();
      Arrays.sort(sortedDists);
      Line closestLine = distLine.get(sortedDists[0]);
      for (Poly p : polys) {
         if (p.hasLine(closestLine) && p.raycast(x, y) % 2 == 1) {
            return p;
         }
      }
      return null;
   }
   
   public static boolean intersects(Line line) {
      //if (true) return false;
      for (Line line2 : lines) {
         try { 
            double intersectX = (line2.getB() - line.getB()) / (line.getM() - line2.getM());
            if (line.spans((int) intersectX) && line2.spans((int) intersectX)) {
               return true;
            }
          } catch (ArithmeticException ame) { return false; }
      }
      return false;
   }
   
   public static void removeLoneLines() {
      ArrayList<Point> pointsToRemove = new ArrayList<>();
      for (Point p : points) {
         if (p.getLines().size() == 1) {
            Line l = p.getLines().get(0);
            lines.remove(l);
            for (int i = 0; i < l.getOtherPoint(p).getLines().size(); i++) {
               if (l.getOtherPoint(p).getLines().get(i).equals(l)) l.getOtherPoint(p).getLines().remove(i);
            }
            pointsToRemove.add(p);
         }
      }
      pointsToRemove.forEach(p -> points.remove(p));
   }

   // https://i.ibb.co/642qj4r/mspoints.png
   public static Point getNextCounterClockwisePoint(Point pivot, Point old) {
      Map<Double, Point> angleToPoint = new HashMap<>();
      ArrayList<Double> angles = new ArrayList<>();
      double oldAngle = getAngle(pivot, old);
      pivot.getLines().forEach((line) -> {              // ooh look, a lambda!
         Point other = line.getOtherPoint(pivot);
         angles.add(getAngle(pivot, other));
         angleToPoint.put(getAngle(pivot, other), other);
      });
      angles.sort(null);
      double newAngle = -100000;
      for (double angle : angles) {
         if (angle > oldAngle) { 
            newAngle = angle;
            break;
         }
      }
      if (newAngle == -100000) newAngle = angles.get(0);
      return angleToPoint.get(newAngle);
   }

   public static double getAngle(Point vertex, Point other) {
      if (vertex.getX() - other.getY() == 0)
         return (other.getY() > vertex.getY() ? 1 : 3) * Math.PI / 2;
      if (vertex.getY() - other.getY() == 0)
         return other.getX() > vertex.getX() ? 0 : Math.PI;
      double base = Math.abs(Math.atan((double)(vertex.getY() - other.getY()) / (vertex.getX() - other.getX())));
      return other.getY() < vertex.getY()
              ? (other.getX() < vertex.getX() ? Math.PI - base : base)
              : (other.getX() < vertex.getX() ? Math.PI + base : 2 * Math.PI - base);
   }

   // Returns the distance (squared) between two points
   public static int getDistance(Point p1, Point p2) {
      return (int)Math.pow(p2.getX() - p1.getX(), 2) + (int)Math.pow(p2.getY() - p1.getY(), 2);
   }

   // Checks if the two arrays have the same elements, but not necessarily in the same order
   public static <T> boolean areArraysEqualDisorderly(T[] a1, T[] a2) {
      boolean bl = true;
      for (T a : a1) {
         boolean bl2 = false;
         for (T b : a2) {
            if (b.equals(a)) {
               bl2 = true;
               break;
            }
         }
         if (!bl2) {
            bl = false;
            break;
         }
      }
      return bl;
   }

   public static void main(String[] args) {
      //Schedule a job for the event-dispatching thread
      SwingUtilities.invokeLater(MisalignSweeper::create);
   }    
}
