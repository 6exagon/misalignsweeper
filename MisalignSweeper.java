import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class MisalignSweeper {

   public static int numFlags = 15;
   public static int numMines = 15;
   public static int numPoints = 150;
   public static final int SEP_DIST = 30;
   
   private static final HashSet<Tri> tris = new HashSet<>();
   private static final HashSet<Line> lines = new HashSet<>();
   private static final HashMap<Tri, Polygon> triToGon = new HashMap<>();
   private static MisalignGraphics graphics;
   private static final Random rand = new Random();
   
   //Scheduled main method essentially
   public static void create() {
      generateBoard();
      graphics = new MisalignGraphics(lines, triToGon);
      graphics.createAndShowGUI(new MisalignInput(), rand);
   }

   // Re-draws the game board without re-generating
   public static void repaint() {
      graphics.frame.repaint();
   }
   
   // Called whenever the board is generated or re-generated
   public static void generateBoard() {
      tris.clear();
      lines.clear();
      triToGon.clear();
      
      ArrayList<Point> points = new ArrayList<>();
      points.add(new Point(220, 250));
      points.add(new Point(280, 250));
      HashSet<Point> freshPoints = new HashSet<>();
      generatePoints(points, freshPoints);
      System.out.print('a');
      generatePolys(points, freshPoints);
      generateAWTPolygons();
      generateMines();
      tris.forEach(Tri::updateMines);
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
      for (int x = 5; x < MisalignGraphics.WIDTH - 5; x += SEP_DIST) {
         Point topPoint = new Point(x, 5);
         Point bottomPoint = new Point(x, MisalignGraphics.HEIGHT - 5);
         pts.add(topPoint);
         pts.add(bottomPoint);
         fps.add(topPoint);
         fps.add(bottomPoint);
      }
      for (int y = SEP_DIST + 10; y < MisalignGraphics.HEIGHT - SEP_DIST - 10; y += SEP_DIST) {
         Point leftPoint = new Point(5, y);
         Point rightPoint = new Point(MisalignGraphics.WIDTH - 5, y);
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
                  simline.addTri(tri);
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

   // Converts a Tri into a renderable Polygon
   private static void generateAWTPolygons() {
      for (Tri tri : tris) {
         int num = 3; //Hardcoded triangles for now
         int[] x = new int[num];
         int[] y = new int[num];
         for (int i = 0; i < num; i++) {
            Point p = tri.getPoint(i);
            x[i] = p.getX();
            y[i] = p.getY();
         }
         triToGon.put(tri, new Polygon(x, y, num));
      }
   }
   
   // Places mines into random Polys
   public static void generateMines() {
      for (int i = 0; i < numMines; i++) {
         Tri tri = tris.stream().skip(rand.nextInt(tris.size())).findFirst().get();
         if (tri.getDisplayState() != -1)
            tri.setMine();
         else
            i--;
      }
   }
   
   // Returns the polygon surrounding a coordinate pair
   public static Tri getClickedTri(int x, int y) {
      return tris.stream().filter(tri -> tri.raycast(x, y) == 1).findAny().orElse(null);
   }

   public static void main(String[] args) {
      //Schedule a job for the event-dispatching thread
      SwingUtilities.invokeLater(MisalignSweeper::create);
   }
}