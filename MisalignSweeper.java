import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class MisalignSweeper {
   public static int numMines = 50;
   public static int numFlags = numMines;
   public static int numPoints = 700;
   public static final double SEP_DIST = 0.05;
   public static boolean firstClick = true;
   public static double triToPolyRate = 0.3;
   
   private static final HashSet<Poly> polys = new HashSet<>();
   private static final HashSet<Tri> tris = new HashSet<>();
   private static final HashMap<Poly, Polygon> polyToGon = new HashMap<>();
   private static final Random rand = new Random();
   
   //Scheduled main method essentially
   public static void create() {
      generateBoard();
      MisalignGraphics.createAndShowGUI(polyToGon, rand);
   }

   // Re-draws the game board without re-generating
   public static void repaint() {
      MisalignGraphics.frame.repaint();
   }
   
   // Called whenever the board is generated or re-generated
   public static void generateBoard() {
      polys.clear();
      tris.clear();
      HashSet<Line> lines = new HashSet<>();
      ArrayList<Point> points = new ArrayList<>();
      Collections.addAll(points, new Point(0.45, 0.5), new Point(0.55, 0.5));
      HashSet<Point> freshPoints = new HashSet<>();
      generatePoints(points, freshPoints);
      generateTris(points, freshPoints, lines);
      generatePolys(lines);
      generateAWTPolygons(500, 500);
      firstClick = true;
   }
   
   // Generates the Points for the game board
   private static void generatePoints(ArrayList<Point> pts, HashSet<Point> fps) {
      int x = 0;
      pointLoop:
      while (x < numPoints) {
         Point p = new Point(rand.nextDouble() * 1.4 - 0.2, rand.nextDouble() * 1.4 - 0.2);
         if (p.isNearBorder()) {
            continue;
         }
         for (Point p2 : pts) {
            if (p.isInBounds() == p2.isInBounds()) {
               if (Math.abs(p.getX() - p2.getX()) + Math.abs(p.getY() - p2.getY()) < SEP_DIST) {
                  continue pointLoop;
               }
            }
         }
         pts.add(p);
         fps.add(p);
         x++;
      }
   }
   
   //Generates all Polys
   private static void generateTris(ArrayList<Point> pts, HashSet<Point> fps, HashSet<Line> lines) {
      Line startLine = new Line(pts.get(0), pts.get(1));
      ArrayDeque<Line> lineStack = new ArrayDeque<Line>();
      Tri initialTriangle = new Tri(startLine, pts, fps, lineStack);
      startLine.extend(false);
      Collections.addAll(lineStack, initialTriangle.getLines());
      Collections.addAll(lines, initialTriangle.getLines());
      tris.add(initialTriangle);
      while (lineStack.stream().anyMatch(l -> l.isInBounds())) {
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
   private static void generatePolys(HashSet<Line> lines) {
      for (Tri tri : tris) {
         if (polys.stream().anyMatch(p -> p.containsTri(tri)))
            continue;
         HashSet<Tri> polysTris = new HashSet<>();
         HashSet<Line> polysLines = new HashSet<>();
         Collections.addAll(polysLines, tri.getLines());
         polysTris.add(tri);
         for (Line l : tri.getLines()) {   // goes through all adjacent tri's
            Tri otherTri = l.getTris()[l.getTris()[0] == tri ? 1 : 0];
            if (rand.nextDouble() < triToPolyRate || otherTri == null || otherTri.getPoly() != null)
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
      polyToGon.clear();
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
   public static void generateMines(Poly except) {
      for (int i = 0; i < numMines; i++) {
         Poly poly = polys.stream().skip(rand.nextInt(polys.size())).findFirst().get();
         if (poly != except && poly.getDisplayState() != -1)
            poly.setMine();
         else
            i--;
      }
      polys.forEach(Poly::updateMines);
   }
   
   // Returns the polygon surrounding a coordinate pair
   public static Poly getClickedPoly(double x, double y) {
      return polys.stream().filter(poly -> poly.raycast(x, y) % 2 == 1).findAny().orElse(null);
   }

   public static void main(String[] args) {
      //Schedule a job for the event-dispatching thread
      SwingUtilities.invokeLater(MisalignSweeper::create);
   }
}