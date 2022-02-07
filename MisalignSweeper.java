import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class MisalignSweeper {

   public static int numFlags = 15;
   public static int numMines = 15;
   public static int numPoints = 200;
   public static int numNears = 6;
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
      HashSet<Point> freshPoints = new HashSet<>();
      generatePoints(points, freshPoints);
      generateTris(points, freshPoints);
      generatePolys();
      generateAWTPolygons();
      generateMines();
      polys.forEach(Poly::updateMines);
   }

   // Generates the Points for the game board
   private static void generatePoints(ArrayList<Point> pts, HashSet<Point> fps) {
      pts.add(new Point(220, 250)); //Hardcoded for now
      pts.add(new Point(280, 250));
      generateEdgePoints(pts, fps);
      
      pointLoop:
      for (int x = 0; x < numPoints; x++) {
         Point p = new Point(rand.nextInt(MisalignGraphics.WIDTH), rand.nextInt(MisalignGraphics.HEIGHT));
         for (Point p2 : pts) {
            if (Math.abs(p.getX() - p2.getX()) + Math.abs(p.getY() - p2.getY()) < SEP_DIST) {
               x--;
               continue pointLoop;
            }
         }
         pts.add(p);
         fps.add(p);
      }
   }
   
   //Generates edge Points on board
   private static void generateEdgePoints(ArrayList<Point> pts, HashSet<Point> fps) {
      for (int x = 0; x < MisalignGraphics.WIDTH + SEP_DIST; x += SEP_DIST) {
         if (x > MisalignGraphics.WIDTH)
            x = MisalignGraphics.WIDTH;
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
   private static void generateTris(ArrayList<Point> pts, HashSet<Point> fps) {
      Line startLine = new Line(pts.get(0), pts.get(1));
      ArrayDeque<Line> linestack = new ArrayDeque<Line>();
      Tri initialTriangle = new Tri(startLine, pts, fps, linestack);
      startLine.extend(false);
      for (Line l : initialTriangle.getLines()) {
         linestack.add(l);
         lines.add(l);
      }
      tris.add(initialTriangle);
      while (fps.size() > 0) {
         Line firstLine = linestack.removeFirst();
         try {
            Tri tri = new Tri(firstLine, pts, fps, linestack);
            tris.add(tri);
            for (int y = 1; y < 3; y++) {
               Line simline = tri.getLines()[y].getSimilarLine(linestack);
               if (simline != null) {
                  linestack.remove(simline);
                  tri.getLines()[y] = simline;
               } else {
                  linestack.add(tri.getLines()[y]);
                  lines.add(tri.getLines()[y]);
               }
            }
         } catch (IndexOutOfBoundsException e) {
            linestack.add(firstLine);
         }
      }
   }
   
   // Generates higher-sided polygons from triangles
   private static void generatePolys() {
      for (Tri tri : tris) {
         if (polys.stream().anyMatch(p -> p.containsTri(tri)))
            continue;
         HashSet<Tri> polysTris = new HashSet<>();
         ArrayList<Line> polysLines = new ArrayList<>();
         for (Line linus : tri.getLines())
            polysLines.add(linus);
         polysTris.add(tri);
         for (Line l : tri.getLines()) {   // goes through all adjacent tri's
            Tri[] lTris = l.getTris();
            Tri otherTri = lTris[0].equals(tri) ? lTris[1] : lTris[0];
            if (rand.nextInt(4) != 0 || otherTri == null || otherTri.getPoly() != null)
               continue;   // 1/4 chance of combining w/ adjacent
            polysTris.add(otherTri);
            for (Line linus : otherTri.getLines())
               if (!polysLines.contains(linus))
                  polysLines.add(linus);
            polysLines.remove(l);
            lines.remove(l);
         }
         Poly poly = new Poly(polysTris.toArray(new Tri[polysTris.size()]), polysLines.toArray(new Line[polysLines.size()]));
         polysTris.forEach(t -> t.addPoly(poly));
         polys.add(poly);
      }
   }

   // Converts a Poly into a renderable Polygon
   private static void generateAWTPolygons() {
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