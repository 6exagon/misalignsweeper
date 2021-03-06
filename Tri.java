import java.util.*;
import java.util.stream.Stream;
import java.awt.*;
import javax.swing.ImageIcon;

public class Tri {

   private Poly containingPoly = null;
   private Line[] lines;
   private Point[] points;
   
   //Correctly initializes using Line and list of points to pull from
   public Tri(Line start, ArrayList<Point> pts, ArrayDeque<Line> lstack) throws IndexOutOfBoundsException {
      Point pt = start.getNewPoint(pts, lstack);
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
   
   public boolean isCorner() {
      return Stream.of(this.lines).filter(Line::isInBounds).count() == 1;
   }
   
   // Gets the point in this Tri that isn't on the line
   public Point getThirdPoint(Line l) {
      return Stream.of(this.points).filter(p -> !l.hasPoint(p)).findFirst().orElse(null);
   }
   
   // Returns the area of this tri
   public double area() {
      return this.lines[0].areaWith(this.points[0]);
   }
   
   //Faster than any iteration, conversion to ArrayList, etc.
   public boolean hasLine(Line line) {
      return lines[0] == line || lines[1] == line || lines[2] == line;
   }
   
   // Generates a hash for the tri for use in HashSets/Maps
   public int hashCode() {
      int hash = 0;
      for (Point p : this.points) {
         hash += (int) (512 * p.getX()) << 16 + (int) (512 * p.getY());
      }
      return hash;
   }
}
