import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Swept {

   public static final int NUM_POINTS = 50;
   public static final int HEIGHT = 256;
   public static final int WIDTH = 256;
   public static final int MIN_DIST = 20;
   public static final ArrayList<Poly> POLYS = new ArrayList<>();
   public static final ArrayList<Line> LINES = new ArrayList<>();
   public static final Point[] POINTS = new Point[NUM_POINTS];

   public static void createAndShowGUI() {
      // Generate points
      Random rand = new Random();
      for (int i = 0; i < NUM_POINTS; i++) {
         Point p = new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT));
         boolean farEnough = true;
         for (Point p2 : POINTS) {
            if (p2 != null && getDistance(p, p2) < Math.pow(MIN_DIST, 2)) farEnough = false;
         }
         POINTS[i] = p;
         if (!farEnough) i--;
      }

      // Generate lines
      for (Point p : POINTS) {
         Map<Integer, Point> distPoint = new HashMap<>();
         int[] dists = new int[NUM_POINTS];
         for (int i = 0; i < NUM_POINTS; i++) {
            dists[i] = getDistance(p, POINTS[i]);
            distPoint.put(dists[i], POINTS[i]);
         }
         Arrays.sort(dists);
         p.nears = new Point[] {distPoint.get(dists[1]), distPoint.get(dists[2]), distPoint.get(dists[3]), distPoint.get(dists[4])}; // dists[0] is itself
         for (int i = 0; i < 4; i++) {
            p.nears[i] = distPoint.get(dists[i]);
            Line l = new Line(p, distPoint.get(dists[i]));
            if (!LINES.contains(l))    LINES.add(l);
            if (!p.lines.contains(l))  p.lines.add(l);
            if (!distPoint.get(dists[i]).lines.contains(l))  distPoint.get(dists[i]).lines.add(l);
         }
      }

      // Generate polys
      for (Point p : POINTS) {
         for (Line startLine : p.lines) {
            ArrayList<Line> linesInPoly = new ArrayList<>();
            Line line;
            Point temp;
            Point point = p;
            Point other = startLine.getOtherPoint(point);
            while (other != p) {
               point = getNextCounterClockwisePoint(other, point);
               line = point.getLineWith(other);
               linesInPoly.add(line);
               temp = point;
               point = other;
               other = temp;
            }
            Poly poly = new Poly(linesInPoly.toArray(new Line[linesInPoly.size()]));
            if (!POLYS.contains(poly) && poly.lines.length < 20) POLYS.add(poly);
         }
      }
      
      System.out.println(POLYS.size());

      //Create and set up the window.
      JFrame frame = new JFrame("Sweeping Up");
      JPanel panel = new JPanel() {
         @Override
         public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            /*
            for (Point p : POINTS) {
               //g2.drawOval(p.x, p.y, 5, 5);
               for (int i = 0; i < 4; i++)
                  g2.drawLine(p.x, p.y, p.nears[i].x, p.nears[i].y);
            }
            */
            for (Poly poly : POLYS) {
               int[] x = new int[poly.lines.length];
               int[] y = new int[poly.lines.length];
               for (int i = 0; i < poly.lines.length; i++) {
                  Line l = poly.lines[i];
                  x[i] = l.points[0].x;
                  y[i] = l.points[0].y;
               }
               Polygon gon = new Polygon(x, y, poly.lines.length);
               g2.setColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
               g2.fillPolygon(gon);
            }
            
            g2.setColor(Color.black);            
            for (Line l : LINES) {
               g2.drawLine(l.points[0].x, l.points[0].y, l.points[1].x, l.points[1].y);
            }            
         }
      };
      frame.add(panel);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setPreferredSize(new Dimension(WIDTH, HEIGHT + 22));   // 22 seems to be the height of the bar at the top
      frame.pack();
      frame.setVisible(true);
   }

   // https://i.ibb.co/642qj4r/mspoints.png
   public static Point getNextCounterClockwisePoint(Point pivot, Point old) {
      Map<Double, Point> angleToPoint = new HashMap<>();
      ArrayList<Double> angles = new ArrayList<>();
      double oldAngle = getAngle(pivot, old);
      pivot.lines.forEach((line) -> {
         Point other = line.getOtherPoint(pivot);
         angles.add(getAngle(pivot, other));
         angleToPoint.put(getAngle(pivot, other), other);
      });
      Collections.sort(angles);
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
   
   public static <T> boolean areArraysEqualDisorderly(T[] a1, T[] a2) {
      boolean bl = true;
      for (T a : a1) {
         boolean bl2 = false;
         for (T b : a2) {
            if (b.equals(a)) bl2 = true;
         }
         if (!bl2) bl = false;
      }
      return bl;
   }

   public static void main(String[] args) {
      //Schedule a job for the event-dispatching thread
      SwingUtilities.invokeLater(Swept::createAndShowGUI);
   }

   public static class Poly {
      public Line[] lines;
      
      @Override
      public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         Poly other = (Poly) o;
         return Swept.areArraysEqualDisorderly(lines, other.lines);
      }

      public Poly(Line[] lines) {
         this.lines = lines;
      }
   }

   public static class Line {
      public Point[] points;
      
      public Line(Point p1, Point p2) {
         this.points = new Point[] {p1, p2};
      }
      
      @Override
      public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         Line other = (Line) o;
         return Swept.areArraysEqualDisorderly(points, other.points);
      }

      public Point getOtherPoint(Point p) {
         return points[0] == p ? points[1] : points[0];
      }
   }

   public static class Point {
      public ArrayList<Line> lines = new ArrayList<>();
      public Point[] nears = new Point[4];
      public int x,y;

      public Line getLineWith(Point p2) {
         for (Line line : lines) {
            if (line.getOtherPoint(this).equals(p2)) return line;
         }
         return null;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         Point point = (Point) o;
         return x == point.x && y == point.y && Objects.equals(lines, point.lines) && Arrays.equals(nears, point.nears);
      }

      public Point(int x, int y) {
         this.x = x; this.y = y;
      }
   }
}