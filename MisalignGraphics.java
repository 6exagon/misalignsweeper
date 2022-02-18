import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class MisalignGraphics {
   public static boolean playingLossAnimation = false;
   private static double xm = 500.0;
   private static double ym = 500.0;
   public static boolean gamePaused = false;
   public static boolean gameWon = false;
   public static JLabel mineCounter;
   public static CustomTimer timer;
   public static JFrame frame;
   
   private static HashMap<Poly, Polygon> polyToGon;
   private static SettingsPanel settings;
   private static JPanel gamePanel;
   private static JPanel cardPanel;
   private static JLabel smile;
   
   public static void createAndShowGUI(HashMap<Poly, Polygon> polygonMap, Random rand) {
      polyToGon = polygonMap;
      
      Border lowered = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
      Border raised = BorderFactory.createBevelBorder(BevelBorder.RAISED);
      int iconSize = 30;
      ImageIcon smileIcon = getScaledImageIcon("images/smile.png", iconSize, -1);
      ImageIcon frownIcon = getScaledImageIcon("images/dead.png", iconSize, -1);
      ImageIcon glassesIcon = getScaledImageIcon("images/glasses.png", iconSize, -1);
      ImageIcon pauseIcon = getScaledImageIcon("images/pause.png", iconSize, -1);
      Image flagImage = new ImageIcon(MisalignGraphics.class.getResource("images/flag.png")).getImage();
      Image mineImage = new ImageIcon(MisalignGraphics.class.getResource("images/mine.png")).getImage();
   
      // Creates window and main mainPanel
      frame = new JFrame("Misalignsweeper");
      JPanel mainPanel = new JPanel(new GridBagLayout());
      //frame.setResizable(false);
      mainPanel.setBorder(raised);
      frame.add(mainPanel);
      
      // Creates mainPanel that holds cards (gamePanel, settingPanel)
      cardPanel = new JPanel(new CardLayout());
      cardPanel.setBorder(lowered);
      CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
      
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
      gamePanel = new JPanel() {
         @Override
         public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
         
            if (!playingLossAnimation && !gamePaused)
               checkGameEnd(glassesIcon, frownIcon);

            if (!gamePaused) {
               xm = this.getWidth();
               ym = this.getHeight();
               MisalignSweeper.generateAWTPolygons(xm, ym);
               for (Poly poly : polyToGon.keySet()) {
                  if (poly.isPressed()) {
                     switch (poly.getDisplayState()) {
                        case -2:
                           g2.setColor(Color.RED); //mine that was actually clicked is in red
                           break;
                        case -1:
                           g2.setColor(Color.LIGHT_GRAY);
                           break;
                        default:
                           g2.setColor(getColor(poly.getDisplayState()));
                     }
                  } else if (poly.isFlagged())
                     g2.setColor(Color.YELLOW);
                  else
                     g2.setColor(Color.WHITE);
                     //g2.setColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));   // epilepsy mode
                  
                  g2.fillPolygon(polyToGon.get(poly));
                  
                  if (poly.isPressed() && poly.getDisplayState() > 0)
                     poly.drawNum(g2);
                  else if (poly.isFlagged()) //must draw flag after updating color
                     poly.drawImageInPoly(g2, flagImage);
                  else if (poly.getDisplayState() < 0 && playingLossAnimation)
                     poly.drawImageInPoly(g2, mineImage);
               }
               g2.setColor(Color.BLACK);
               for (Polygon gon : polyToGon.values())
                  g2.drawPolygon(gon); // render polygons' outlines
               
               // win and loss text              
               g2.setFont(new Font("Monospaced", Font.BOLD, 64));
               if (playingLossAnimation) {
                  g2.setColor(Color.RED);
                  g2.drawString("You Lose", 100, 100);
               } else if (gameWon) {
                  g2.setColor(Color.GREEN);
                  g2.drawString("YOU WIN!", 100, 100);
               }
            }
         }
      };
      gamePanel.setPreferredSize(new Dimension(500, 500)); //add 4 for border?
      cardPanel.add(gamePanel, "gamePanel");
      
      // Creates settings panel
      settings = new SettingsPanel();
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
      timer = new CustomTimer();
      timer.start();
      cButtons.gridx = 0;
      cButtons.anchor = GridBagConstraints.LINE_START;
      buttonPanel.add(timer, cButtons);
   
      // Creates mine counter
      mineCounter = new JLabel(MisalignSweeper.numFlags + "");
      mineCounter.setForeground(Color.RED);
      mineCounter.setBackground(Color.BLACK);
      mineCounter.setOpaque(true);
      mineCounter.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 5));
      mineCounter.setFont(new Font("Consolas", Font.PLAIN, 20));
      cButtons.gridx = 2;
      cButtons.anchor = GridBagConstraints.LINE_END;
      buttonPanel.add(mineCounter, cButtons);
      
      // Creates smile (reset) button
      frame.setIconImage(smileIcon.getImage());
      smile = new JLabel(smileIcon);
      smile.setBorder(raised);
      smile.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            if (!gamePaused && !playingLossAnimation)
               smile.setBorder(lowered);
         }
      
         @Override
         public void mouseReleased(MouseEvent e) {
            if (!gamePaused && !playingLossAnimation) {
               MisalignSweeper.numPoints = settings.getPoints();
               MisalignSweeper.numMines = settings.getMines();
               MisalignSweeper.numFlags = MisalignSweeper.numMines;
               
               smile.setBorder(raised);
               smile.setIcon(smileIcon);
               gameWon = false;
               MisalignSweeper.generateBoard();
               mineCounter.setText(MisalignSweeper.numFlags + "");
               cardLayout.show(cardPanel, "gamePanel");
               timer.restart();
               mainPanel.repaint();
               mineCounter.repaint();
            }
         }
      });       
      cButtons.gridx = 1;
      cButtons.anchor = GridBagConstraints.CENTER;
      buttonPanel.add(smile, cButtons);
      
      // Creates pause button
      JLabel pause = new JLabel(pauseIcon);
      pause.setBorder(raised);
      pause.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            pause.setBorder(lowered);
            timer.togglePause();
            if (gamePaused)
               cardLayout.show(cardPanel, "gamePanel");
            else
               cardLayout.show(cardPanel, "settings");
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
      gamePanel.addMouseListener(new MisalignInput());
      frame.add(mainPanel);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
   }
   
   public static Color getColor(int level) {
      Color[] colors = {
         Color.GRAY, Color.BLUE, Color.GREEN, Color.RED, new Color(0, 0, 60),
         Color.MAGENTA, Color.CYAN, Color.BLACK, Color.GRAY, Color.PINK, new Color(200, 100, 0)};
      if (level < 11) {
         return colors[level];
      } else if (level < 30) {
         return new Color(256 - level * 8, 300 - level * 10, 0);
      } else {
         return Color.WHITE;
      }
   }
   
   // Checks if the player has won or lost
   public static void checkGameEnd(ImageIcon winIcon, ImageIcon loseIcon) {
      if (gameLost()) {
         smile.setIcon(loseIcon);
         timer.stop();
         revealAllMines();
      } else if (gameWon()) {
         smile.setIcon(winIcon);
         timer.stop();
         gameWon = true;
      }  
   }
  
   // Checks if player won the game (all non-mines are revealed) 
   public static boolean gameWon() {
      return polyToGon.keySet().stream().noneMatch(p -> p.getDisplayState() != -1 && !p.isPressed());
   }
   
   // Checks if the player lost (revealed a mine)
   public static boolean gameLost() {
      return polyToGon.keySet().stream().anyMatch(p -> p.getDisplayState() == -2);
   }
   
   // Reveals all mines when player loses
   public static void revealAllMines() { 
      playingLossAnimation = true; // prevents clicking and button presses during loss animation
      HashSet<Poly> mines = new HashSet<Poly>(polyToGon.keySet());
      mines.removeIf(p -> p.getDisplayState() != -1);
      
      int delay = 50; // in milliseconds
      Timer mineRevealTimer = new Timer(delay, null);
      mineRevealTimer.addActionListener((e) -> { //adding listener later lets us stop the timer within the listener more easily
         if (mines.size() > 0 && playingLossAnimation) {
            Poly poly = mines.stream().findAny().get();
            poly.reveal();
            mines.remove(poly); 
            gamePanel.repaint();   
         } else {
            playingLossAnimation = false;
            mineRevealTimer.stop();
         }
      });
      mineRevealTimer.start();
   }
   
   // Resizes an ImageIcon given file path (there's probably a better way to do this)
   private static ImageIcon getScaledImageIcon(String path, int width, int height) {   
      return new ImageIcon(new ImageIcon(MisalignGraphics.class.getResource(path)).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
   }
   
   //Returns x multiplier
   public static double getXM() {
      return xm;
   }
   
   //Returns y multiplier
   public static double getYM() {
      return ym;
   }
}