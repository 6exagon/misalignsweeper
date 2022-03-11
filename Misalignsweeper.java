import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.stream.*;

public class Misalignsweeper {
   
   private static final HashSet<Poly> polys = new HashSet<>();
   private static final HashMap<Poly, Polygon> polyToGon = new HashMap<>();
   private static final Random rand = new Random();
   
   public static int numMines = 50;
   public static int numFlags = numMines;
   public static int numPoints = 700;
   public static int polyIteration = 3;
   public static final double SEP_DIST = 0.05;
   public static boolean firstClick = true;
   public static double triToPolyRate = 0.8;
   public static long seed;
   
   //Scheduled main method essentially
   public static void create() {
      generateBoard();
      MisalignGraphics.createAndShowGUI(polyToGon, rand);
   }

   // Re-draws the game board without re-generating
   public static void repaint() {
      MisalignGraphics.getFrame().repaint();
   }
   
   // Called whenever the board is generated or re-generated
   public static void generateBoard() {
      if (SettingsPanel.customSeedEntered) {
         try {
            seed = Long.valueOf(SettingsPanel.seedTextField.getText());
         } catch (NumberFormatException e) {
            seed = 0;
         }
         SettingsPanel.customSeedEntered = false;
      } else {
         seed = rand.nextLong() * System.nanoTime();
      }
      rand.setSeed(seed);
      polys.clear();
      polyToGon.clear();
      
      generatePolys();
      generateAWTPolygons(500, 500);
      firstClick = true;
      
   }
   
   // Generates the Points for the game board
   private static ArrayList<Point> generatePoints() {
      ArrayList<Point> points = new ArrayList<>();
      Collections.addAll(points, new Point(0.45, 0.5), new Point(0.55, 0.5));
      points.forEach(Point::setNotFresh);
      
      pointLoop:
      for (int x = 0; x < numPoints;) {
         Point p = new Point(rand.nextDouble() * 1.4 - 0.2, rand.nextDouble() * 1.4 - 0.2);
         if (p.isNearBorder()) {
            continue;
         }
         for (Point p2 : points) {
            if (p.isInBounds() == p2.isInBounds()) {
               if (Math.abs(p.getX() - p2.getX()) + Math.abs(p.getY() - p2.getY()) < SEP_DIST) {
                  continue pointLoop;
               }
            }
         }
         points.add(p);
         x++;
      }
      return points;
   }
   
   //Generates all Tris
   private static HashSet<Tri> generateTris() {
      ArrayList<Point> pts = generatePoints();
   
      HashSet<Tri> tris = new HashSet<>();
      Line startLine = new Line(pts.get(0), pts.get(1));
      ArrayDeque<Line> lineStack = new ArrayDeque<>();
      Tri initialTriangle = new Tri(startLine, pts, lineStack);
      Collections.addAll(lineStack, initialTriangle.getLines());
      startLine.extend(false);
      tris.add(initialTriangle);
      while (lineStack.stream().anyMatch(Line::isInBounds)) {
         Line firstLine = lineStack.removeFirst();
         try {
            Tri tri = new Tri(firstLine, pts, lineStack);
            tris.add(tri);
            // Make line references match those in the line stack
            for (int y = 1; y < 3; y++) {
               Line ythLine = tri.getLines()[y];
               Line simLine = ythLine.getSimilarLine(lineStack);
               if (simLine != null) {
                  lineStack.remove(simLine);
                  tri.getLines()[y] = simLine;
                  simLine.addTri(tri);
               } else {
                  lineStack.add(ythLine);
               }
            }
         } catch (IndexOutOfBoundsException e) {
            lineStack.add(firstLine);
         }
      }
      return tris;
   }
   
   // Generates higher-sided polygons from triangles
   private static void generatePolys() {
      for (Tri tri : generateTris()) {
         if (tri.getPoly() != null)
            continue;
         HashSet<Tri> polysTris = new HashSet<>();
         HashSet<Line> polysLines = new HashSet<>();
         Collections.addAll(polysLines, tri.getLines());
         polysTris.add(tri);
         
         // Adds the tris on the outskirts of the poly several times
         for (int i = 0; i <= rand.nextInt(polyIteration) + rand.nextInt(polyIteration); i++) {
            addSurroundingTrisToPoly(polysTris, polysLines, false);
         }
         addSurroundingTrisToPoly(polysTris, polysLines, true); // run through to catch the corners
         
         polys.add(new Poly(polysTris.toArray(new Tri[0]), polysLines.toArray(new Line[0])));
      }
      polys.forEach(Poly::calcMidpoint);
   }
   
   // Each round of poly-nation
   private static void addSurroundingTrisToPoly(HashSet<Tri> polysTris, HashSet<Line> polysLines, boolean onlyCorners) {
      HashSet<Line> lineShell = new HashSet<>(polysLines);
      for (Line line : lineShell) {   // goes through all adjacent tri's
         Tri otherTri = line.getTris()[polysTris.contains(line.getTris()[0]) ? 1 : 0];
         if (otherTri == null || otherTri.getPoly() != null || polysTris.contains(otherTri))
            continue;
         
         Point newPoint = otherTri.getThirdPoint(line); // prevents looping back around on itself
         if (polysLines.stream().anyMatch(l -> l.hasPoint(newPoint)) && !onlyCorners)
            continue;
                           //    random chance of merging          auto-merge if too small
         if (!onlyCorners ? (rand.nextDouble() >= triToPolyRate || otherTri.area() < 0.001)
                          : Stream.of(line.getTris()).anyMatch(Tri::isCorner)) { // either side is a corner
            polysTris.add(otherTri);
            for (Line otherLine : otherTri.getLines()) {
               if (!polysLines.remove(otherLine)) {  // remove repeats to avoid stray lines
                  polysLines.add(otherLine);
               }
            }
         }
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
      SwingUtilities.invokeLater(Misalignsweeper::create);
   }
}