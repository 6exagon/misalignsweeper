import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.BevelBorder;

public class MisalignGraphics {
   public static final int HEIGHT = 256;
   public static final int WIDTH = 256;
   public ArrayList<Line> lines;
   public HashMap<Poly, Polygon> polytogon;
   public JFrame frame;
   public boolean gamePaused = false;
   
   public MisalignGraphics(ArrayList<Line> lines, HashMap<Poly, Polygon> polytogon) {
      this.lines = lines;
      this.polytogon = polytogon;
   }
   
   public void createAndShowGUI(MisalignInput input, Random rand) {

     // Border lowered =

      // Creates window and main panel
      this.frame = new JFrame("Misalignsweeper");
      JPanel panel = new JPanel(new GridBagLayout());
      panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
      frame.add(panel);
      
      // Creates game panel
      JPanel gamePanel = new JPanel() {
         @Override
         public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            if (gamePaused) {
               g2.setColor(Color.BLACK);
               g2.fillRect(0, 0, MisalignGraphics.WIDTH, MisalignGraphics.HEIGHT);
               g2.setColor(Color.WHITE);
               g2.setFont(new Font("Consolas", Font.PLAIN, 25)); 
               g2.drawString("Paused", 50, 50);
               g2.setColor(Color.BLACK);
            } else {
               for (Polygon gon : polytogon.values()) { 
                  g2.setColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256))); //probably should save colors so it's the same after pausing
                  g2.fillPolygon(gon);
               }
               g2.setColor(Color.BLACK);            
               for (Line l : lines) {
                  g2.drawLine(l.points[0].x, l.points[0].y, l.points[1].x, l.points[1].y);
               }
            }
            //g2.setColor(Color.RED);
            //g2.drawRect(2, 2, MisalignGraphics.WIDTH, MisalignGraphics.HEIGHT); 
            //somethings slightly off between this square and the size of the game panel
         }
      };
      gamePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
      gamePanel.setPreferredSize(new Dimension(MisalignGraphics.WIDTH + 4, MisalignGraphics.HEIGHT + 4)); //add 4 for border?
      GridBagConstraints cMain = new GridBagConstraints(); //constraints for panel that covers entire window
      int insetMain = 5;
      cMain.insets = new Insets(insetMain, insetMain, insetMain, insetMain); //no, there isn't a better constructor
      cMain.gridwidth = 2;
      cMain.anchor = GridBagConstraints.CENTER;
      cMain.fill = GridBagConstraints.BOTH;
      cMain.gridx = 0;
      cMain.gridy = 1;
      cMain.weighty = 1;
      cMain.weightx = 1;
      panel.add(gamePanel, cMain);
      
      // Creates panel that buttons and timer will go in
      JPanel buttonPanel = new JPanel(new GridBagLayout());
      buttonPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
      cMain.gridx = 0;
      cMain.gridy = 0;
      cMain.weighty = 0;
      cMain.weightx = 1.0;
      panel.add(buttonPanel, cMain);
      GridBagConstraints cButtons = new GridBagConstraints();
      int insetButtons = 3;
      cButtons.insets = new Insets(insetButtons, insetButtons, insetButtons, insetButtons);
      cButtons.weightx = 1.0;
      
      // Creates timer
      CustomTimer timer = new CustomTimer(1000);
      timer.start();
      cButtons.gridx = 0;
      cButtons.anchor = GridBagConstraints.LINE_START;
      buttonPanel.add(timer, cButtons);
      
      // Creates smile (reset) button
      ImageIcon smileIcon = getScaledImageIcon("minesweeper smile.png", 30, -1); 
      ImageIcon frownIcon = getScaledImageIcon("minesweeper frown.png", 30, -1);
      this.frame.setIconImage(smileIcon.getImage());
      JLabel smile = new JLabel(smileIcon);
      smile.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
      smile.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
             if (!gamePaused) {
               smile.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
               timer.restart();
            }
         }

         @Override
         public void mouseReleased(MouseEvent e) {
            if (!gamePaused) {
               smile.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
               MisalignSweeper.generateBoard(new Random());
               panel.repaint();
            }
         }
      });       
      cButtons.gridx = 1;
      cButtons.anchor = GridBagConstraints.CENTER;
      buttonPanel.add(smile, cButtons);
      
      // Creates pause button
      JLabel pause = new JLabel(frownIcon);
//       JLabel pause = new JLabel() {
//            @Override
//            public void paintComponent(Graphics g) {
//               super.paintComponent(g);
//               Graphics2D g2 = (Graphics2D) g;
//            }
//       };
      pause.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
      pause.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            pause.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            timer.togglePause();
            gamePanel.repaint();
            gamePaused = !gamePaused;
         }

         @Override
         public void mouseReleased(MouseEvent e) {
            pause.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
         }
      });
      cButtons.gridx = 2;
      cButtons.anchor = GridBagConstraints.LINE_END;
      buttonPanel.add(pause, cButtons);
      
      //this is bad (maybe change layout managers?)
//       JLabel blank = new JLabel();
//       blank.setPreferredSize(new Dimension(pause.getPreferredSize())); // adds blank space equal to width of pause button on left
//       cButtons.gridx = 0;                                              // in order to center the replay button
//       buttonPanel.add(blank, cButtons);
      
      
      frame.setFocusable(true);
      frame.addKeyListener(input);
      frame.addMouseListener(input);
      frame.add(panel);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      //frame.setPreferredSize(new Dimension(WIDTH, HEIGHT + 22) // 22 seems to be the height of the bar at the top
      frame.pack();
      frame.setVisible(true);
   }
   
   //resizes an ImageIcon given file path
   private static ImageIcon getScaledImageIcon(String path, int width, int height) {
      ImageIcon icon = new ImageIcon(path);
      Image image = icon.getImage();
      Image newImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
      return new ImageIcon(newImage);
    }
}