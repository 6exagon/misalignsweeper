import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class MisalignSweeper {

   public static int numMines = 15;
   public static int numPoints = 200;
   public static final int SEP_DIST = 30;
   
   private static final ArrayList<Poly> polys = new ArrayList<>();
   private static final ArrayList<Line> lines = new ArrayList<>();
   private static final HashMap<Poly, Polygon> polyToGon = new HashMap<>();  // Doesn't actually need to be a map, but it'll prob be useful in future.
   private static MisalignGraphics graphics;
   
   //Scheduled main method essentially
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
      polys.clear();
      lines.clear();
      polyToGon.clear();
      
      ArrayList<Point> points = new ArrayList<>();
      HashSet<Point> freshPoints = new HashSet<>();
      generatePoints(rand, points, freshPoints);
      generatePolys(points, freshPoints);
      generateAWTPolygons();
      //generateMines();
//       for (Poly x : polys) {
//          x.updateMines();
//       }
   }

   // Generates the Points for the game board
   private static void generatePoints(Random rand, ArrayList<Point> pts, HashSet<Point> fps) {
      pts.add(new Point(220, 250)); //Hardcoded for now
      pts.add(new Point(280, 250));
      generateEdgePoints(pts, fps);
      for (int x = 0; x < numPoints; x++) {
         Point p = new Point(
            rand.nextInt(MisalignGraphics.WIDTH - 100) + 50, rand.nextInt(MisalignGraphics.HEIGHT - 100) + 50);
         boolean farEnough = true;
         for (Point p2 : pts) {
            if (Math.abs(p.getX() - p2.getX()) + Math.abs(p.getY() - p2.getY()) < SEP_DIST) {
               farEnough = false;
               break;
            }
         }
         if (!farEnough) {
            x--;
         } else {
            pts.add(p);
            fps.add(p);
         }
      }
   }
   
   //Generates edge Points on board
   private static void generateEdgePoints(ArrayList<Point> pts, HashSet<Point> fps) {
      for (int x = 0; x < MisalignGraphics.WIDTH; x += SEP_DIST) {
         Point topPoint = new Point(x, 0);
         Point bottomPoint = new Point(x, MisalignGraphics.HEIGHT);
         pts.add(topPoint);
         pts.add(bottomPoint);
         fps.add(topPoint);
         fps.add(bottomPoint);
      }
      for (int y = SEP_DIST; y < MisalignGraphics.HEIGHT - SEP_DIST; y += SEP_DIST) {
         Point leftPoint = new Point(0, y);
         Point rightPoint = new Point(MisalignGraphics.WIDTH, y);
         pts.add(leftPoint);
         pts.add(rightPoint);
         fps.add(leftPoint);
         fps.add(rightPoint);
      }
   }
   
   //Generates all Polys
   private static void generatePolys(ArrayList<Point> pts, HashSet<Point> fps) {
      Line startLine = new Line(pts.get(0), pts.get(1));
      ArrayDeque<Line> linestack = new ArrayDeque<Line>();
      Poly initialTriangle = new Poly(startLine, pts, fps, linestack);
      startLine.extend(false);
      for (Line l : initialTriangle.getLines()) {
         linestack.add(l);
         lines.add(l);
      }
      polys.add(initialTriangle);
      for (int x = 0; x < 40; x++) {
         try {
            Poly tri = new Poly(linestack.getFirst(), pts, fps, linestack);
            polys.add(tri);
            for (int y = 1; y < 3; y++) {
               Line simline = tri.getLines()[y].getSimilarLine(linestack);
               if (simline != null) {
                  linestack.remove(simline);
                  tri.getLines()[y] = simline;
               } else {
                  linestack.add(tri.getLines()[y]);
                  lines.add(tri.getLines()[y]);
               }             }
         } catch (IndexOutOfBoundsException e) {
            linestack.add(linestack.getFirst());
         }
         linestack.removeFirst();
      }
   }

   // Converts a Poly into a renderable Polygon
   private static void generateAWTPolygons() {
      for (Poly poly : polys) {
         int num = 3; //Hardcoded triangles for now
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
   
   // // Creates a point on one of the edges, which one is determined by 'side'
   // public static Point makeSidePoint(int side, int otherVal) {
   //    switch (side) {
   //       case 0: return new Point(otherVal, 0);
   //       case 1: return new Point(MisalignGraphics.WIDTH, otherVal);
   //       case 2: return new Point(MisalignGraphics.WIDTH - otherVal, MisalignGraphics.HEIGHT);
   //       case 3: return new Point(0, MisalignGraphics.HEIGHT - otherVal);
   //    }
   //    return null;
   // }
   
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
   
   // // Returns whether line is intersecting any other Line
   // public static boolean intersects(Line line) {
   //    if (line.getM() == Double.MAX_VALUE && line.getPoint(0).getX() % MisalignGraphics.WIDTH != 0)
   //          return true;   // This is a weird (but apparently mostly functional) fix for those weird vertical lines
   //    for (Line line2 : lines) {
   //       if (!line2.sharesPointWith(line) && line.getM() != line2.getM()) {
   //          double intersectX = (line2.getB() - line.getB()) / (line.getM() - line2.getM());
   //          if (line.spans((int)Math.round(intersectX)) && line2.spans((int)Math.round(intersectX)))
   //             return true;
   //       }
   //    }
   //    return false;
   // }
   
   // // Removes all the points and lines that aren't connected to anything
   // public static void removeLoneLines() {
   //    ArrayList<Point> pointsToRemove = new ArrayList<>();
   //    for (Point p : points) {
   //       if (p.getLines().size() == 1) {
   //          pointsToRemove.add(p);
   //          Line line = p.getLines().get(0);
   //          lines.remove(line);
   //          // We also have to get rid of references to this line from other points.
   //          line.getOtherPoint(p).getLines().remove(line);
   //       }
   //    }
   //    pointsToRemove.forEach(points::remove);  // To avoid ConcurrentModificationException
   // }

   // // Goes through all the points connected to 'pivot', and returns the one with
   // // the lowest angle (as defined by getAngle) greater than 'old'
   // public static Point getNextCounterClockwisePoint(Point pivot, Point old) {
   //    Map<Double, Point> angleToPoint = new HashMap<>();
   //    double oldAngle = getAngle(pivot, old);
   //    pivot.getLines().forEach(line -> {              // ooh look, a lambda!
   //       Point other = line.getOtherPoint(pivot);
   //       angleToPoint.put(getAngle(pivot, other), other);
   //    });
   //    double newAngle = 100000;
   //    for (double angle : angleToPoint.keySet())
   //       if (angle > oldAngle && angle < newAngle) // allows us to find the smallest angle > the old angle
   //          newAngle = angle;
   //    if (newAngle == 100000)  // If none are greater than old, we want the lowest value.
   //       newAngle = Collections.min(angleToPoint.keySet());
   //    return angleToPoint.get(newAngle);
   // }

   // // Returns the angle between a horizontal line going to the right of 'vertex' and a line from 'vertex' to 'other'
   // public static double getAngle(Point vertex, Point other) {
   //    double base = Math.abs(Math.atan((double)(other.getY() - vertex.getY()) / (other.getX() - vertex.getX())));
   //    // The inverse tangent only goes between 0 and pi/2, so we need to find out what quadrant it's in and change it.
   //    return other.getY() < vertex.getY()
   //            ? (other.getX() < vertex.getX() ? Math.PI - base : base)                 // 2nd and 1st Quadrants
   //            : (other.getX() < vertex.getX() ? Math.PI + base : 2 * Math.PI - base);  // 3rd and 4th Quadrants
   // }
   
   // // Adds an item to an ArrayList if it is not already there
   // public static <T> boolean addTo(ArrayList<T> list, T item) {
   //    if (!list.contains(item)) {
   //       list.add(item);
   //       return true;
   //    }
   //    return false;
   // }

   // // Checks if the two arrays have the same elements, but not necessarily in the same order
   // public static <T> boolean areArraysEqualDisorderly(T[] a1, T[] a2) {
   //    array1:
   //    for (T a : a1) {
   //       for (T b : a2)
   //          if (b.equals(a))
   //             continue array1;
   //       return false;
   //    }
   //    return true;
   // }

   public static void main(String[] args) {
      //Schedule a job for the event-dispatching thread
      SwingUtilities.invokeLater(MisalignSweeper::create);
   }    
}