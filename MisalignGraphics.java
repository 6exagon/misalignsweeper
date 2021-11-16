import javax.swing.*;
import java.util.*;
import java.awt.*;

public class MisalignGraphics {
   public static final int HEIGHT = 256;
   public static final int WIDTH = 256;
   public ArrayList<Line> lines;
   public HashMap<Poly, Polygon> polytogon;
   public JFrame frame;
   
   public MisalignGraphics(ArrayList<Line> lines, HashMap<Poly, Polygon> polytogon) {
      this.lines = lines;
      this.polytogon = polytogon;
   }
   
   public void createAndShowGUI(MisalignInput input, Random rand) {
      //Create and set up the window
      this.frame = new JFrame("Misalignsweeper");
      JPanel panel = new JPanel() {
         @Override
         public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            for (Polygon gon : polytogon.values()) {
               g2.setColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
               g2.fillPolygon(gon);
            }
            
            g2.setColor(Color.black);            
            for (Line l : lines) {
               g2.drawLine(l.points[0].x, l.points[0].y, l.points[1].x, l.points[1].y);
            }            
         }
      };
      frame.setFocusable(true);
      frame.addKeyListener(input);
      frame.addMouseListener(input);
      frame.add(panel);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setPreferredSize(new Dimension(WIDTH, HEIGHT + 22));   // 22 seems to be the height of the bar at the top
      frame.pack();
      frame.setVisible(true);
   }
}