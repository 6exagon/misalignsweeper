import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

// According to my IDE, 'Misalign' isn't a word but who cares.
public class MisalignSweeper {

   public static final int NUM_POINTS = 50;
   public static final int HEIGHT = 256;
   public static final int WIDTH = 256;
   public static final int MIN_DIST = 20;
   public static final int NUM_NEARS = 4;
   public static final ArrayList<Poly> POLYS = new ArrayList<>();
   public static final ArrayList<Line> LINES = new ArrayList<>();
   public static final Point[] POINTS = new Point[NUM_POINTS];
   public static final HashMap<Poly, Polygon> POLY_TO_GON = new HashMap<>();  // Doesn't actually need to be a map, but it'll prob be useful in future.
   public static JFrame FRAME;

   public static void createAndShowGUI(MisalignInput input) {
      Random rand = new Random();
      generateBoard(rand);

      //Create and set up the window
      JFrame frame = new JFrame("MisalignSweeper");
      JPanel panel = new JPanel() {
         @Override
         public void paintComponent(Graphics g) {   // Anonymous class b/c it's only used once; it's pretty small; and I'm afraid of other files.
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            for (Polygon gon : POLY_TO_GON.values()) {
               g2.setColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
               g2.fillPolygon(gon);
            }
            
            g2.setColor(Color.black);            
            for (Line l : LINES) {
               g2.drawLine(l.points[0].x, l.points[0].y, l.points[1].x, l.points[1].y);
            }            
         }
      };
      frame.setFocusable(true);
      frame.addKeyListener(input);
      frame.addMouseListener(input);
      frame.add(panel);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setPreferredSize(new Dimension(WIDTH, HEIGHT + 22));   // 22 seems to be the height of the bar at the top
      frame.pack();
      frame.setVisible(true);
      FRAME = frame;
   }
   
   public static void generateBoard(Random rand) {
      LINES.clear();
      POLYS.clear();
      POLY_TO_GON.clear();
      
      generatePoints(rand);
      generateLines();
      generatePolys();
      generateAWTPolygons();

      System.out.println(POLYS.size());    // just for debug.
      POLYS.forEach((poly) -> {
         //for (Line l : poly.lines) System.out.println(l.points[0].x + " " + l.points[0].y + "   " + l.points[1].x + " " + l.points[1].y);
      });
   }

   public static void generatePoints(Random rand) {
      for (int i = 0; i < NUM_POINTS; i++) {
         Point p = new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT));
         boolean farEnough = true;
         for (Point p2 : POINTS) {
            if (p2 != null && getDistance(p, p2) < Math.pow(MIN_DIST, 2)) farEnough = false;
         }
         POINTS[i] = p;
         if (!farEnough) i--;   // if it's not far enough away, it decrements, effectively just running through this 'i' again until it is far enough.
      }
   }

   public static void generateLines() {
      for (Point p : POINTS) {
         Map<Integer, Point> distPoint = new HashMap<>();
         int[] dists = new int[NUM_POINTS];
         for (int i = 0; i < NUM_POINTS; i++) {
            dists[i] = getDistance(p, POINTS[i]);     // get the distance to each point
            distPoint.put(dists[i], POINTS[i]);       // keep track of how far away each point is
         }
         Arrays.sort(dists);     // sort the distances in ascending order
         for (int i = 1; i <= NUM_NEARS; i++) {          // dists[0] is itself
            Point ithNear = distPoint.get(dists[i]);
            p.nears[i - 1] = ithNear;
            Line l = new Line(p, ithNear);                        // Add the newly-made Line object to:
            if         (!LINES.contains(l)) LINES.add(l);            // the class-wide list of all lines
            if       (!p.lines.contains(l)) p.lines.add(l);          // the first point's list of lines
            if (!ithNear.lines.contains(l)) ithNear.lines.add(l);    // the second point's list of lines
         }
      }
   }

   public static void generatePolys() {
      for (Point p : POINTS) {                                 // For each point,
         for (Line startLine : p.lines) {                      // and each line coming off of that point,
            ArrayList<Line> linesInPoly = new ArrayList<>();   // move around to the next line counter-clockwise of that line.
            linesInPoly.add(startLine);                        // Once you reach the original point, you'll have made a complete loop.
            Line line;                                         // This is then saved as a polygon (Poly).
            Point temp;
            Point point = p;
            Point other = startLine.getOtherPoint(point);
            while (other != p) {
               point = getNextCounterClockwisePoint(other, point);
               line = point.getLineWith(other);
               for (Line l : linesInPoly) if (l.points[0].equals(line.points[0])) line.reversePoints();
               linesInPoly.add(line);
               temp = point;
               point = other;
               other = temp;
            }
            Poly poly = new Poly(linesInPoly.toArray(new Line[linesInPoly.size()]), null);    // DON'T CHANGE THIS; IT BREAKS
            
            if (!POLYS.contains(poly) && poly.lines.length < 15) POLYS.add(poly);   // if the poly is too big, it doesn't add it
         }                                                                          // as it's probably wrapped all around the board.
      }
   }

   // Converts a Poly into a renderable Polygon
   public static void generateAWTPolygons() {
      for (Poly poly : POLYS) {
         int num = poly.lines.length;
         int[] x = new int[num];
         int[] y = new int[num];
         for (int i = 0; i < num; i++) {
            Line l = poly.lines[i];
            x[i] = l.points[0].x;
            y[i] = l.points[0].y;
         }
         Polygon gon = new Polygon(x, y, num);
         POLY_TO_GON.put(poly, gon);
      }
   }

   // https://i.ibb.co/642qj4r/mspoints.png
   public static Point getNextCounterClockwisePoint(Point pivot, Point old) {
      Map<Double, Point> angleToPoint = new HashMap<>();
      ArrayList<Double> angles = new ArrayList<>();
      double oldAngle = getAngle(pivot, old);
      pivot.lines.forEach((line) -> {              // ooh look, a lambda!
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
      double base = Math.abs(Math.atan((double)(vertex.y - other.y) / (vertex.x - other.x)));
      return other.y < vertex.y
              ? (other.x < vertex.x ? Math.PI - base : base)
              : (other.x < vertex.x ? Math.PI + base : 2 * Math.PI - base);
   }

   // Returns the distance (squared) between two points
   public static int getDistance(Point p1, Point p2) {
      return (int)Math.pow(p2.x - p1.x, 2) + (int)Math.pow(p2.y - p1.y, 2);
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
      MisalignInput input = new MisalignInput();
      //Schedule a job for the event-dispatching thread
      SwingUtilities.invokeLater(() -> MisalignSweeper.createAndShowGUI(input));
   }
}
