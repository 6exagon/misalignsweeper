import java.util.*;
import java.util.stream.Stream;
import java.awt.*;
import javax.swing.ImageIcon;

public class Tri {

   private Poly containingPoly = null;
   private Line[] lines;
   private Point[] points;
   
   //Correctly initializes using Line and list of points to pull from
   public Tri(Line start, ArrayList<Point> pts, HashSet<Point> fps, ArrayDeque<Line> lstack) throws IndexOutOfBoundsException {
      Point pt = start.getNewPoint(pts, fps, lstack);
      if (pt == null)
         throw new IndexOutOfBoundsException();
      Line l2 = new Line(start.getQ(), pt);
      l2.extend(!l2.isOnExtendedSide(start.getP()));
      Line l3 = new Line(pt, start.getP());
      l3.extend(!l3.isOnExtendedSide(start.getQ()));
      this.lines = new Line[] {start, l2, l3};
      this.points = new Point[] {pt, start.getP(), start.getQ()};
      this.addTrisToLines();
   }
   
   public Line[] getLines() {
      return this.lines;
   }

   // Adds references to Polys in Lines
   public void addTrisToLines() {
     for (Line l : this.lines)
        l.addTri(this);
   }
   
   public void addPoly(Poly p) {
      this.containingPoly = p;
   }
   
   public Poly getPoly() {
      return this.containingPoly;
   }
   
   public Point getPoint(int index) {
      return this.points[index];
   }
   
   public Point[] getPoints() {
      return this.points;
   }
   
   //Faster than any iteration, conversion to ArrayList, etc.
   public boolean hasLine(Line line) {
      return lines[0] == line || lines[1] == line || lines[2] == line;
   }
}
