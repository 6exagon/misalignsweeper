import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;

public class MisalignGraphics {
   public static final int HEIGHT = 256;
   public static final int WIDTH = 256;
   public static double xMultiplier = 1.0;
   public static double yMultiplier = 1.0;
   public ArrayList<Line> lines;
   public HashMap<Poly, Polygon> polytogon;
   public JFrame frame;
   public boolean gamePaused = false;
   private SettingsPanel settings;
   
   public MisalignGraphics(ArrayList<Line> lines, HashMap<Poly, Polygon> polytogon) {
      this.lines = lines;
      this.polytogon = polytogon;
   }
   
   public void createAndShowGUI(MisalignInput input, Random rand) {

      Border lowered = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
      Border raised = BorderFactory.createBevelBorder(BevelBorder.RAISED);

      // Creates window and main mainPanel
      this.frame = new JFrame("Misalignsweeper");
      JPanel mainPanel = new JPanel(new GridBagLayout());
      mainPanel.setBorder(raised);
      frame.add(mainPanel);
      
      // Creates mainPanel that holds cards (gamePanel, settingPanel)
      JPanel cardPanel = new JPanel(new CardLayout());
      cardPanel.setBorder(lowered);
      GridBagConstraints cMain = new GridBagConstraints(); //constraints for mainPanel
      int insetMain = 5;
      cMain.insets = new Insets(insetMain, insetMain, insetMain, insetMain); //no, there isn't a better constructor
      cMain.gridwidth = 2;
      cMain.anchor = GridBagConstraints.CENTER;
      cMain.fill = GridBagConstraints.BOTH;
      cMain.gridx = 0;
      cMain.gridy = 1;
      cMain.weighty = 1;
      cMain.weightx = 1;
      mainPanel.add(cardPanel, cMain);
      
      // Creates game panel
      JPanel gamePanel = new JPanel() {
         @Override
         public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            
            if (!gamePaused) {
               MisalignGraphics.xMultiplier = this.getWidth() / (double)MisalignGraphics.WIDTH;
               MisalignGraphics.yMultiplier = this.getHeight() / (double)MisalignGraphics.HEIGHT;
               g2.scale(MisalignGraphics.xMultiplier, MisalignGraphics.yMultiplier);
               
               // for (Poly poly : polytogon.keySet()) { 
//                   if (poly.isPressed())
//                      g2.setColor(Color.WHITE);
//                   else if (poly.isFlagged())
//                      g2.setColor(Color.RED);
//                   else
//                      g2.setColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
//                }
                  //g2.setColor(colorPoly(poly.getDisplayState())); // does not work
               for (Poly poly : polytogon.keySet()) {
                  if (poly.isPressed()) {
                     switch (poly.getDisplayState()) {
                        case -2:
                        case -1:
                           g2.setColor(Color.BLACK);
                           break;
                        default:
                           g2.setColor(getColor(poly.getDisplayState()));
                     }
                  } else if (poly.isFlagged()) {
                     //g2.drawImage(new ImageIcon("flag.png").getImage(), 5, 5, null); //null for ImageObserver parameter
                     g2.setColor(Color.YELLOW);
                  } else {
                     g2.setColor(Color.WHITE);
                  }
                  g2.fillPolygon(polytogon.get(poly));
                  
                  if (poly.isPressed())
                     poly.drawNum(g2);
                  else if (poly.isFlagged()) //must draw flag after updating color
                     poly.drawFlag(g2);
               }
               g2.setColor(Color.BLACK);            
               for (Line l : lines)
                  g2.drawLine(l.getPoint(0).getX(), l.getPoint(0).getY(), l.getPoint(1).getX(), l.getPoint(1).getY());
            }
         }
      };
      gamePanel.setPreferredSize(new Dimension(MisalignGraphics.WIDTH, MisalignGraphics.HEIGHT)); //add 4 for border?
      cardPanel.add(gamePanel, "gamePanel");
      
      // Creates settings panel
      SettingsPanel settings = new SettingsPanel();
      this.settings = settings;
      cardPanel.add(settings, "settings");
      
      // Creates panel to hold buttons, timer, mine counter
      JPanel buttonPanel = new JPanel(new GridBagLayout());
      buttonPanel.setBorder(lowered);
      cMain.gridx = 0;
      cMain.gridy = 0;
      cMain.weighty = 0;
      cMain.weightx = 1.0;
      mainPanel.add(buttonPanel, cMain);
      GridBagConstraints cButtons = new GridBagConstraints();
      int insetButtons = 3;
      cButtons.insets = new Insets(insetButtons, insetButtons, insetButtons, insetButtons);
      cButtons.weightx = 1.0;
      
      // Creates timer
      CustomTimer timer = new CustomTimer();
      timer.start();
      cButtons.gridx = 0;
      cButtons.anchor = GridBagConstraints.LINE_START;
      buttonPanel.add(timer, cButtons);

      // Creates mine counter
      JLabel mineCounter = new JLabel(String.format("%03d", MisalignSweeper.numMines));
      mineCounter.setForeground(Color.RED);
      mineCounter.setBackground(Color.BLACK);
      mineCounter.setOpaque(true);
      mineCounter.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 5));
      mineCounter.setFont(new Font("Consolas", Font.PLAIN, 20));
      cButtons.gridx = 2;
      cButtons.anchor = GridBagConstraints.LINE_END;
      buttonPanel.add(mineCounter, cButtons);
      //update minecount after every poly click

      // Creates smile (reset) button
      int iconSize = 30;
      ImageIcon smileIcon = getScaledImageIcon("minesweeper smile.png", iconSize, -1); 
      ImageIcon frownIcon = getScaledImageIcon("pause.png", iconSize, -1);
      this.frame.setIconImage(smileIcon.getImage());
      JLabel smile = new JLabel(smileIcon);
      smile.setBorder(raised);
      smile.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
             if (!gamePaused) {
               smile.setBorder(lowered);
               timer.restart();
            }
         }

         @Override
         public void mouseReleased(MouseEvent e) {
            if (!gamePaused) {
               smile.setBorder(raised);
               MisalignSweeper.generateBoard(new Random());
               mineCounter.setText(String.format("%03d", MisalignSweeper.numMines));
               mainPanel.repaint();
            }
         }
      });       
      cButtons.gridx = 1;
      cButtons.anchor = GridBagConstraints.CENTER;
      buttonPanel.add(smile, cButtons);
      
      // Creates pause button
      JLabel pause = new JLabel(frownIcon);
      pause.setBorder(raised);
      pause.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            pause.setBorder(lowered);
            timer.togglePause();
            
            if (gamePaused) {
               MisalignSweeper.numPoints = MisalignGraphics.this.settings.getPoints();
               MisalignSweeper.numMines = MisalignGraphics.this.settings.getMines();
               MisalignSweeper.numNears = MisalignGraphics.this.settings.getNears();
            }
            
            CardLayout c = (CardLayout)(cardPanel.getLayout());
            c.next(cardPanel);  
            gamePaused = !gamePaused;
         }

         @Override
         public void mouseReleased(MouseEvent e) {
            pause.setBorder(raised);
         }
      });
      cButtons.gridx = 3;
      cButtons.anchor = GridBagConstraints.LINE_END;
      buttonPanel.add(pause, cButtons);
      
      frame.setFocusable(true);
      gamePanel.addMouseListener(input);
      frame.add(mainPanel);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
   }
   
   // Resizes an ImageIcon given file path (there's probably a better way to do this)
   private ImageIcon getScaledImageIcon(String path, int width, int height) {
      ImageIcon icon = new ImageIcon(path);
      Image image = icon.getImage();
      Image newImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
      return new ImageIcon(newImage);
   }
   
   public SettingsPanel getSettings() {
      return this.settings;
   }
   
   public Color getColor(int level) {
      Color[] colors = {
         Color.GRAY, Color.BLUE, Color.GREEN, Color.RED, new Color(0, 0, 60),
         Color.MAGENTA, Color.CYAN, Color.BLACK, Color.GRAY, Color.PINK, Color.YELLOW};
      if (level < 11) {
         return colors[level];
      } else if (level < 30) {
         return new Color(256 - level * 8, 300 - level * 10, 0);
      } else {
         return Color.WHITE;
      }
   }
}