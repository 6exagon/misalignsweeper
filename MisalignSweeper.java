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
            rand.nextInt(MisalignGraphics.WIDTH), rand.nextInt(MisalignGraphics.HEIGHT));
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
      while (fps.size() > 0) {
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
   
   // Returns the polygon surrounding a coordinate pair
   public static Poly getClickedPolygon(int x, int y) {
      TreeMap<Double, Line> distLine = new TreeMap<>();
      for (Line line : lines)
         if (line.spans(x))
            distLine.put(Math.abs(line.getM() * x + line.getB() - y), line);
      Line closestLine = distLine.get(distLine.firstKey());
      for (Poly p : polys)
         if (p.hasLine(closestLine) && p.raycast(x, y) % 2 == 1)
            return p;
      return null;
   }

   public static void main(String[] args) {
      //Schedule a job for the event-dispatching thread
      SwingUtilities.invokeLater(MisalignSweeper::create);
   }    
}