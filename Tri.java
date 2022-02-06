import java.util.*;
import java.util.stream.Stream;
import java.awt.*;
import javax.swing.ImageIcon;

public class Tri {
   private final Image flagImage = new ImageIcon(getClass().getResource("flag.png")).getImage();
   private Line[] lines;
   private Point[] points;
   private int surroundingMines = 0;
   private Visibility visible = Visibility.NORMAL;
   private Point midpoint;
   
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
      calcMidpoint();
   }
   
   public Point getPoint(int index) {
      return this.points[index];
   }
   
   public Line[] getLines() {
      return this.lines;
   }
   
   //Returns -1 if mine, -2 if activated mine, or number of surrounding mines
   public int getDisplayState() {
      return this.surroundingMines;
   }
   
   public void setMine() {
      this.surroundingMines = -1;
   }
   
   public void drawNum(Graphics2D g2) {
      g2.setColor(Color.WHITE);
      
      int midX = this.midpoint.getX();
      int midY = this.midpoint.getY();
      
      double triHeight = 0;
      for (Line edge : this.lines)
         if (edge.spans(midX))
            triHeight = Math.abs(triHeight - edge.at(midX));
      
      midX -= triHeight/6;
      midY += triHeight*2/9;
      g2.setFont(
         new Font(g2.getFont().getName(),
         g2.getFont().getStyle(),
         (int)(triHeight * 2/3 * Math.min(MisalignGraphics.getXM(), MisalignGraphics.getYM()))));
      
      midX *= MisalignGraphics.getXM();
      midY *= MisalignGraphics.getYM();
      
      g2.drawString(this.surroundingMines + "", midX, midY);
   }
   
   public void drawFlag(Graphics2D g2) {
      int midX = this.midpoint.getX();
      int midY = this.midpoint.getY();
      
      double triHeight = 0;
      for (Line edge : this.lines)
         if (edge.spans(midX))
            triHeight = Math.abs(triHeight - edge.at(midX));
      
      triHeight *= 2/3.0 * Math.min(MisalignGraphics.getXM(), MisalignGraphics.getYM());
      midX -= triHeight/2;
      midY -= triHeight/2;
      
      midX *= MisalignGraphics.getXM();
      midY *= MisalignGraphics.getYM();
      
      g2.drawImage(flagImage, midX, midY, (int)triHeight, (int)triHeight, null);
   }
   
   public void reveal() {
      if (this.visible == Visibility.NORMAL) {
         this.visible = Visibility.PRESSED;
         if (this.surroundingMines == -1) {
            this.surroundingMines = -2;
            System.out.println("You lost");
            //lose game
         } else if (this.surroundingMines == 0)
            for (Line l : lines)
               for (Tri t : l.getTris())
                  if (t != this && t != null)
                     t.reveal();
      }
   }
   
   public void flag() {
      if (this.visible == Visibility.FLAG) {
         this.visible = Visibility.NORMAL;
         MisalignSweeper.numFlags++;
      } else if (MisalignSweeper.numFlags != 0 && this.visible == Visibility.NORMAL) {
         this.visible = Visibility.FLAG;
         MisalignSweeper.numFlags--;
      }
   }
   
   //Updates surrounding mine (should be done once all mines are placed)
   public void updateMines() {
      for (Line l : lines)
         for (Tri t : l.getTris())
            if (t != null && t != this)
               this.surroundingMines += (t.getDisplayState() == -1) ? 1 : 0;
   }
   
   private void calcMidpoint() {
//       if (this.numPoints() == 3) { // calculates the centroid if a triangle
         Line l1 = new Line(getPoint(0), new Point((getPoint(1).getX() + getPoint(2).getX()) / 2, (getPoint(1).getY() + getPoint(2).getY()) / 2));
         Line l2 = new Line(getPoint(1), new Point((getPoint(0).getX() + getPoint(2).getX()) / 2, (getPoint(0).getY() + getPoint(2).getY()) / 2));
         int intersectX = (int)((l2.getB() - l1.getB()) / (l1.getM() - l2.getM()));
         this.midpoint = new Point(intersectX, l1.at(intersectX));
//       } else {    // if not a triangle, just take the average of all the points
//          int x = 0, y = 0;
//          for (Point p : this.points) {
//             x += p.getX();
//             y += p.getY();
//          }
//          this.midpoint = new Point(x / this.numPoints(), y / this.numPoints());
//       }
   }
   
   // Adds references to Polys in Lines
  public void addTrisToLines() {
     for (Line l : this.lines)
        l.addTri(this);
   }
   
   //Faster than any iteration, conversion to ArrayList, etc.
   public boolean hasLine(Line line) {
      return lines[0] == line || lines[1] == line || lines[2] == line;
   }
   
   //Returns the number of intersections with line extending from point
   public int raycast(int x, int y) {
      return (int) Stream.of(lines).filter(l -> l.spans(x) && l.at(x) > y).count();
   }
   
   public boolean isPressed() {
      return this.visible == Visibility.PRESSED;
   }
   
   public boolean isFlagged() {
      return this.visible == Visibility.FLAG;
   }
   
   public boolean isNormal() {
      return this.visible == Visibility.NORMAL;
   }
   
   enum Visibility {
      NORMAL,
      PRESSED,
      FLAG;
   }
}
