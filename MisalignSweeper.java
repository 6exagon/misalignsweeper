import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class MisalignSweeper {

   public static int numPoints = 50;
   public static int numMines = 15;
   public static int minDist = 15;
   public static int numNears = 6;
   
   private static final ArrayList<Poly> polys = new ArrayList<>();
   public  static final ArrayList<Line> lines = new ArrayList<>();
   private static final ArrayList<Point> points = new ArrayList<>();
   private static final HashMap<Poly, Polygon> polyToGon = new HashMap<>();  // Doesn't actually need to be a map, but it'll prob be useful in future.
   private static MisalignGraphics graphics;
   private static Point[] corners;
   
   public static void create() {
      MisalignInput input = new MisalignInput();
      Random rand = new Random();
      generateBoard(rand);
      graphics = new MisalignGraphics(lines, polyToGon);
      graphics.createAndShowGUI(input, rand);
   }

   // Re-draws the game board without re-generating
   public static void repaint() {
      graphics.frame.repaint();
   }
   
   // Called whenever the board is generated or re-generated
   public static void generateBoard(Random rand) {
      points.clear();
      lines.clear();
      polys.clear();
      polyToGon.clear();      
      
      generatePoints(rand);
      generateLines();
      generatePolys();
      generateAWTPolygons();
      generateMines();
      for (Poly x : polys) {
         x.updateMines();
      }
   }

   // Generates the Points for the game board
   public static void generatePoints(Random rand) {
      addEdgeAndCornerPoints(rand);
      for (int i = 16; i < numPoints; i++) { // start at 16 b/c of edges and corners
         Point p = new Point(rand.nextInt(MisalignGraphics.WIDTH), rand.nextInt(MisalignGraphics.HEIGHT));
         boolean farEnough = true;
         for (Point p2 : points)
            if (getDistance(p, p2) < Math.pow(minDist, 2))
               farEnough = false;
         if (!farEnough)
            i--;   // if it's not far enough away, it decrements, effectively just running through this 'i' again until it is far enough.
         else
            points.add(p);
      }
   }

   // Generates the Lines for the game board
   public static void generateLines() {
      for (Point p : points) {
         Map<Integer, Point> distPoint = new HashMap<>();
         int[] dists = new int[numPoints];
         for (int i = 0; i < numPoints; i++) {
            dists[i] = getDistance(p, points.get(i));     // get the distance to each point
            distPoint.put(dists[i], points.get(i));       // keep track of how far away each point is
         }
         Arrays.sort(dists);     // sort the distances in ascending order
         for (int i = 1; i <= numNears; i++) {          // dists[0] is itself
            Point ithNear = distPoint.get(dists[i]);
            Line l = new Line(p, ithNear);                        // Add the newly-made Line object to:
            if (!intersects(l)) {
               addTo(lines, l);               // the class-wide list of all lines
               addTo(p.getLines(), l);          // the first point's list of lines
               addTo(ithNear.getLines(), l);    // the second point's list of lines
            }
         }
      }
      removeLoneLines();
   }

   // Generates all the Polys for the game board
   public static void generatePolys() {
      Point temp;
      for (Point p : points) {                                 // For each point,
         for (Line startLine : p.getLines()) {                 // and each line coming off of that point,
            ArrayList<Point> pointsInPoly = new ArrayList<>(); // move around to the next line counter-clockwise of that line.
            Point point = p;                                   // Once you reach the original point, you'll have made a complete loop.
            Point other = startLine.getOtherPoint(point);      // This is then saved as a polygon (Poly).
            while (!pointsInPoly.contains(other)) {
               pointsInPoly.add(other);
               temp = other;
               other = getNextCounterClockwisePoint(other, point);
               point = temp;
            }
            while (!pointsInPoly.get(0).equals(other)) {  // This is my solution for getting rid of invisible tails
               pointsInPoly.remove(0);
            }
            if (!pointsInPoly.containsAll(Arrays.asList(corners))) {  // Checks if the poly has all four corner Points in it.
               Poly poly = new Poly(pointsInPoly);                    // this should maybe be changed to including any 2 corners, but I haven't seen any issues recently               
               if (addTo(polys, poly)) {
                  poly.addPolysToLines();
               }
            }
         }
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
         polyToGon.put(poly, new Polygon(x, y, num));
      }
   }
   
   // Places mines into random Polys
   public static void generateMines() {
      Random rand = new Random();
      for (int i = 0; i < numMines; i++) {
         Poly poly = polys.get(rand.nextInt(polys.size()));
         if (poly.getDisplayState() != -1)
            poly.setMine();
         else
            i--;
      }
   }
   
   // Adds mandatory points on the edges and corners.
   public static void addEdgeAndCornerPoints(Random rand) {
      corners = new Point[] {
         new Point(0, 0),
         new Point(MisalignGraphics.WIDTH, 0),
         new Point(MisalignGraphics.WIDTH, MisalignGraphics.HEIGHT),
         new Point(0, MisalignGraphics.HEIGHT)
      };
      
      for (Point p : corners) points.add(p);
      
      for (int i = 0; i < 4; i++) {
         int dim = i % 2 == 0 ? MisalignGraphics.WIDTH : MisalignGraphics.HEIGHT;
         Point p1 = makeSidePoint(i, rand.nextInt(3 * dim / 16) + dim / 8);       // The three edge points are distributed so they're
         Point p2 = makeSidePoint(i, rand.nextInt(3 * dim / 16) + 3 * dim / 8);   // relatively far away from each other but still random.
         Point p3 = makeSidePoint(i, rand.nextInt(3 * dim / 16) + 5 * dim / 8);
         // We have to force the edge points to connect, otherwise we'd get empty tiles
         Line l1 = new Line(corners[i], p1);
         corners[i].getLines().add(l1);
         p1.getLines().add(l1);
         Line l2 = new Line(p1, p2);
         p1.getLines().add(l2);
         p2.getLines().add(l2);
         Line l3 = new Line(p2, p3);
         p2.getLines().add(l3);
         p3.getLines().add(l3);
         Line l4 = new Line(p3, corners[(i+1)%4]);
         p3.getLines().add(l4);
         corners[(i+1)%4].getLines().add(l4);
         // Only four statements per line? Child's play.
         lines.add(l1); lines.add(l2); lines.add(l3); lines.add(l4);
         points.add(p1); points.add(p2); points.add(p3);
      }
   }
   
   // Creates a point on one of the edges, which one is determined by 'side'
   public static Point makeSidePoint(int side, int otherVal) {
      switch (side) {
         case 0: return new Point(otherVal, 0);
         case 1: return new Point(MisalignGraphics.WIDTH, otherVal);
         case 2: return new Point(MisalignGraphics.WIDTH - otherVal, MisalignGraphics.HEIGHT);
         case 3: return new Point(0, MisalignGraphics.HEIGHT - otherVal);
      }
      return null;
   }
   
   // Returns the polygon surrounding a coordinate pair
   public static Poly getClickedPolygon(int x, int y) {
      Map<Double, Line> distLine = new HashMap<>();
      for (Line line : lines)
         if (line.spans(x))
            distLine.put(Math.abs(line.getM() * x + line.getB() - y), line);
      Line closestLine = distLine.get(Collections.min(distLine.keySet()));
      for (Poly p : polys)
         if (p.hasLine(closestLine) && p.raycast(x, y) % 2 == 1)
            return p;
      return null;
   }
   
   // Returns whether line is intersecting any other Line
   public static boolean intersects(Line line) {
      if (line.getM() == Double.MAX_VALUE && line.getPoint(0).getX() % MisalignGraphics.WIDTH != 0) 
            return true;   // This is a weird (but apparently mostly functional) fix for those weird vertical lines
      for (Line line2 : lines) {
         if (!line2.sharesPointWith(line) && line.getM() != line2.getM()) {
            double intersectX = (line2.getB() - line.getB()) / (line.getM() - line2.getM());
            if (line.spans((int)Math.round(intersectX)) && line2.spans((int)Math.round(intersectX)))
               return true;
         }
      }
      return false;
   }
   
   // Removes all the points and lines that aren't connected to anything
   public static void removeLoneLines() {
      ArrayList<Point> pointsToRemove = new ArrayList<>();
      for (Point p : points) {
         if (p.getLines().size() == 1) {
            pointsToRemove.add(p);
            Line line = p.getLines().get(0);
            lines.remove(line);
            // We also have to get rid of references to this line from other points.
            line.getOtherPoint(p).getLines().remove(line);
         }
      }
      pointsToRemove.forEach(points::remove);  // To avoid ConcurrentModificationException
   }

   // Goes through all the points connected to 'pivot', and returns the one with
   // the lowest angle (as defined by getAngle) greater than 'old'
   public static Point getNextCounterClockwisePoint(Point pivot, Point old) {
      Map<Double, Point> angleToPoint = new HashMap<>();
      double oldAngle = getAngle(pivot, old);
      pivot.getLines().forEach(line -> {              // ooh look, a lambda!
         Point other = line.getOtherPoint(pivot);
         angleToPoint.put(getAngle(pivot, other), other);
      });
      double newAngle = 100000;
      for (double angle : angleToPoint.keySet())
         if (angle > oldAngle && angle < newAngle) // allows us to find the smallest angle > the old angle
            newAngle = angle;
      if (newAngle == 100000)  // If none are greater than old, we want the lowest value.
         newAngle = Collections.min(angleToPoint.keySet());
      return angleToPoint.get(newAngle);
   }

   // Returns the angle between a horizontal line going to the right of 'vertex' and a line from 'vertex' to 'other'
   public static double getAngle(Point vertex, Point other) {
      double base = Math.abs(Math.atan((double)(other.getY() - vertex.getY()) / (other.getX() - vertex.getX())));
      // The inverse tangent only goes between 0 and pi/2, so we need to find out what quadrant it's in and change it.
      return other.getY() < vertex.getY()
              ? (other.getX() < vertex.getX() ? Math.PI - base : base)                 // 2nd and 1st Quadrants
              : (other.getX() < vertex.getX() ? Math.PI + base : 2 * Math.PI - base);  // 3rd and 4th Quadrants
   }

   // Returns the distance (squared) between two points
   public static int getDistance(Point p1, Point p2) {
      return (int)Math.pow(p2.getX() - p1.getX(), 2) + (int)Math.pow(p2.getY() - p1.getY(), 2);
   }
   
   // Adds an item to an ArrayList if it is not already there
   public static <T> boolean addTo(ArrayList<T> list, T item) {
      if (!list.contains(item)) {
         list.add(item);
         return true;
      }
      return false;
   }

   // Checks if the two arrays have the same elements, but not necessarily in the same order
   public static <T> boolean areArraysEqualDisorderly(T[] a1, T[] a2) {
      array1:
      for (T a : a1) {
         for (T b : a2)
            if (b.equals(a))
               continue array1;
         return false;
      }
      return true;
   }

   public static void main(String[] args) {
      //Schedule a job for the event-dispatching thread
      SwingUtilities.invokeLater(MisalignSweeper::create);
   }    
}