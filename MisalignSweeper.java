import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class MisalignSweeper {

   public static int numFlags = 15;
   public static int numMines = 15;
   public static int numPoints = 150;
   public static final int SEP_DIST = 30;
   
   private static final HashSet<Poly> polys = new HashSet<>();
   private static final HashSet<Tri> tris = new HashSet<>();
   private static final HashSet<Line> lines = new HashSet<>();
   private static final HashMap<Poly, Polygon> polyToGon = new HashMap<>();
   private static MisalignGraphics graphics;
   private static final Random rand = new Random();
   
   //Scheduled main method essentially
   public static void create() {
      generateBoard();
      graphics = new MisalignGraphics(lines, polyToGon);
      graphics.createAndShowGUI(new MisalignInput(), rand);
   }

   // Re-draws the game board without re-generating
   public static void repaint() {
      graphics.frame.repaint();
   }
   
   // Called whenever the board is generated or re-generated
   public static void generateBoard() {
      polys.clear();
      tris.clear();
      lines.clear();
      polyToGon.clear();
      
      ArrayList<Point> points = new ArrayList<>();
      Collections.addAll(points, new Point(220, 250), new Point(280, 250));
      HashSet<Point> freshPoints = new HashSet<>();
      generatePoints(points, freshPoints);
      generateTris(points, freshPoints);
      generatePolys();
      generateAWTPolygons(1, 1);
      generateMines();
      polys.forEach(Poly::updateMines);
   }

   // Generates the Points for the game board
   private static void generatePoints(ArrayList<Point> pts, HashSet<Point> fps) {
      pointLoop:
      for (int x = 0; x < numPoints; x++) {
         int xc = rand.nextInt(MisalignGraphics.WIDTH - 2 * SEP_DIST) + SEP_DIST;
         int yc = rand.nextInt(MisalignGraphics.HEIGHT - 2 * SEP_DIST) + SEP_DIST;
         Point p = new Point(xc, yc);
         for (Point p2 : pts) {
            if (Math.abs(p.getX() - p2.getX()) + Math.abs(p.getY() - p2.getY()) < SEP_DIST) {
               x--;
               continue pointLoop;
            }
         }
         pts.add(p);
         fps.add(p);
      }
      generateEdgePoints(pts, fps);
   }
   
   //Generates edge Points on board
   private static void generateEdgePoints(ArrayList<Point> pts, HashSet<Point> fps) {
      for (int x = 5; x <= MisalignGraphics.WIDTH - 5; x += SEP_DIST) {
         Point topPoint = new Point(x, 5);
         Point bottomPoint = new Point(x, MisalignGraphics.HEIGHT - 5);
         Collections.addAll(pts, topPoint, bottomPoint);
         Collections.addAll(fps, topPoint, bottomPoint);
      }
      for (int y = SEP_DIST + 10; y < MisalignGraphics.HEIGHT - SEP_DIST - 10; y += SEP_DIST) {
         Point leftPoint = new Point(5, y);
         Point rightPoint = new Point(MisalignGraphics.WIDTH - 5, y);
         Collections.addAll(pts, leftPoint, rightPoint);
         Collections.addAll(fps, leftPoint, rightPoint);
      }
   }
   
   //Generates all Polys
   private static void generateTris(ArrayList<Point> pts, HashSet<Point> fps) {
      Line startLine = new Line(pts.get(0), pts.get(1));
      ArrayDeque<Line> lineStack = new ArrayDeque<Line>();
      Tri initialTriangle = new Tri(startLine, pts, fps, lineStack);
      startLine.extend(false);
      Collections.addAll(lineStack, initialTriangle.getLines());
      Collections.addAll(lines, initialTriangle.getLines());
      tris.add(initialTriangle);
      while (!fps.isEmpty()) {
         Line firstLine = lineStack.removeFirst();
         try {
            Tri tri = new Tri(firstLine, pts, fps, lineStack);
            tris.add(tri);
            for (int y = 1; y < 3; y++) {
               Line ythLine = tri.getLines()[y];
               Line simline = ythLine.getSimilarLine(lineStack);
               if (simline != null) {
                  lineStack.remove(simline);
                  tri.getLines()[y] = simline;
                  simline.addTri(tri);
               } else {
                  lineStack.add(ythLine);
                  lines.add(ythLine);
               }
            }
         } catch (IndexOutOfBoundsException e) {
            lineStack.add(firstLine);
         }
      }
   }
   
   // Generates higher-sided polygons from triangles
   private static void generatePolys() {
      for (Tri tri : tris) {
         if (polys.stream().anyMatch(p -> p.containsTri(tri)))
            continue;
         HashSet<Tri> polysTris = new HashSet<>();
         HashSet<Line> polysLines = new HashSet<>();
         Collections.addAll(polysLines, tri.getLines());
         polysTris.add(tri);
         for (Line l : tri.getLines()) {   // goes through all adjacent tri's
            Tri otherTri = l.getTris()[l.getTris()[0] == tri ? 1 : 0];
            if (rand.nextInt(4) != 0 || otherTri == null || otherTri.getPoly() != null)
               continue;   // 1/4 chance of combining w/ adjacent
            polysTris.add(otherTri);
            Collections.addAll(polysLines, otherTri.getLines());
            polysLines.remove(l);
            lines.remove(l);
         }
         Poly poly = new Poly(polysTris.toArray(new Tri[0]), polysLines.toArray(new Line[0]));
         polysTris.forEach(t -> t.addPoly(poly));
         polys.add(poly);
      }
   }

   // Converts a Poly into a renderable Polygon
   public static void generateAWTPolygons(double xm, double ym) {
      for (Poly poly : polys) {
         int num = poly.numPoints();
         int[] x = new int[num];
         int[] y = new int[num];
         for (int i = 0; i < num; i++) {
            Point p = poly.getPoint(i);
            x[i] = (int) (p.getX() * xm);
            y[i] = (int) (p.getY() * ym);
         }
         polyToGon.put(poly, new Polygon(x, y, num));
      }
   }
   
   // Places mines into random Polys
   public static void generateMines() {
      for (int i = 0; i < numMines; i++) {
         Poly poly = polys.stream().skip(rand.nextInt(polys.size())).findFirst().get();
         if (poly.getDisplayState() != -1)
            poly.setMine();
         else
            i--;
      }
   }
   
   // Returns the polygon surrounding a coordinate pair
   public static Poly getClickedPoly(int x, int y) {
      return polys.stream().filter(poly -> poly.raycast(x, y) == 1).findAny().orElse(null);
   }

   public static void main(String[] args) {
      //Schedule a job for the event-dispatching thread
      SwingUtilities.invokeLater(MisalignSweeper::create);
   }
}