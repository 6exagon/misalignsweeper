import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class MisalignGraphics {
   private static double xm = 500.0;
   private static double ym = 500.0;
   public static boolean gamePaused = false;
   public static boolean playingLossAnimation = false;
   public static boolean gameWon = false;
   private static HashMap<Poly, Polygon> polyToGon;
   
   //Constants
   private static final Border LOWERED = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
   private static final Border RAISED = BorderFactory.createBevelBorder(BevelBorder.RAISED); 
   private static final int ICON_SIZE = 30;
   private static final ImageIcon SMILE_ICON = getScaledImageIcon("images/smile.png", ICON_SIZE, -1); //-1 keeps original w:h ratio
   private static final ImageIcon FROWN_ICON = getScaledImageIcon("images/dead.png", ICON_SIZE, -1);
   private static final ImageIcon GLASSES_ICON = getScaledImageIcon("images/glasses.png", ICON_SIZE, -1);
   private static final ImageIcon PAUSE_ICON = getScaledImageIcon("images/pause.png", ICON_SIZE, -1);
   private static final Image FLAG_IMAGE = new ImageIcon(MisalignGraphics.class.getResource("images/flag.png")).getImage();
   private static final Image MINE_IMAGE = new ImageIcon(MisalignGraphics.class.getResource("images/mine.png")).getImage();
   private static final int LOSS_ANIMATION_DELAY = 25; //in milliseconds
     
   //JComponents (swing components)
   private static JFrame frame;
   private static JPanel mainPanel;
   private static JPanel cardPanel;
   private static JPanel gamePanel;
   private static SettingsPanel settings;
   private static JPanel buttonPanel;
   private static CustomTimer timer;
   private static JLabel mineCounter;
   private static JLabel smile;
   private static JLabel pause; 
   
   //Layouts/layout managers for components
   private static CardLayout cardLayout;
   private static GridBagConstraints cMain;
   private static GridBagConstraints cButtons;
   
   //Creates the graphics for the whole game
   public static void createAndShowGUI(HashMap<Poly, Polygon> polygonMap, Random rand) {
      polyToGon = polygonMap;
      
      createFrame();
      addCardPanel();
      addGamePanel(rand);
      addSettingsPanel();
      addButtonPanel();
      addTimer();
      addMineCounter();
      addSmile();
      addPause();
    }
   
   //Creates the window and the main panel that contain everything
   public static void createFrame() {
      frame = new JFrame("Misalignsweeper");
      frame.setMinimumSize(new Dimension(400, 425));
      frame.setFocusable(true);
      frame.addKeyListener(new MisalignInput());
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      mainPanel = new JPanel(new GridBagLayout()); //mainPanel contains everything
      mainPanel.setBorder(RAISED);
      frame.add(mainPanel);
            
      cMain = new GridBagConstraints(); //constraints for mainPanel layout
      int insetMain = 5;
      cMain.insets = new Insets(insetMain, insetMain, insetMain, insetMain); //no, there isn't a better constructor
      cMain.gridwidth = 2;
      cMain.anchor = GridBagConstraints.CENTER;
      cMain.fill = GridBagConstraints.BOTH;
      cMain.weightx = 1;
   }
   
   // Creates the cardPanel containing the game board and settings
   public static void addCardPanel() {
      cardPanel = new JPanel(new CardLayout());
      cardPanel.setBorder(new CompoundBorder(LOWERED, new LineBorder(Color.BLACK, 1)));
      cardLayout = (CardLayout) cardPanel.getLayout();

      cMain.weighty = 1;
      cMain.gridx = 0;
      cMain.gridy = 1;
      mainPanel.add(cardPanel, cMain);
   }
   
   //Creates panel that game is played on
   public static void addGamePanel(Random rand) {
      gamePanel = new JPanel() {
         @Override
         public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
         
            if (!playingLossAnimation && !gamePaused)
               checkGameEnd();

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
                           g2.setColor(Color.LIGHT_GRAY); //other mines revealed are light gray
                           break;
                        default:
                           g2.setColor(getColor(poly.getDisplayState()));
                     }
                  } else if (poly.isFlagged())
                     g2.setColor(Color.YELLOW);
                  else if (settings.colorfulModeChecked())
                     g2.setColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256))); //colorful mode
                  else
                     g2.setColor(Color.WHITE);
                  g2.fillPolygon(polyToGon.get(poly));
                  
                  //draws flag/num/mine in poly after coloring poly
                  if (poly.isPressed() && poly.getDisplayState() > 0)
                     poly.drawNum(g2);
                  else if (poly.isFlagged())
                     poly.drawImageInPoly(g2, FLAG_IMAGE);
                  else if (poly.getDisplayState() < 0 && playingLossAnimation)
                     poly.drawImageInPoly(g2, MINE_IMAGE);
               }
               
               g2.setColor(Color.BLACK);
               if (!settings.noLinesModeChecked()) {
                  for (Polygon gon : polyToGon.values())
                     g2.drawPolygon(gon); // render polygons' outlines
               }
               
               // win and loss text              
               g2.setFont(new Font("Monospaced", Font.BOLD, 64));
               FontMetrics fm = g2.getFontMetrics();//used to get width of string with current font
               if (playingLossAnimation) {
                  g2.setColor(Color.RED);
                  String lossText = "You lose";                  
                  g2.drawString(lossText, (gamePanel.getWidth() - fm.stringWidth(lossText)) / 2, (gamePanel.getHeight() - fm.getHeight()) / 2);//centered horizontally, just above middle vertically
               } else if (gameWon) {
                  g2.setColor(Color.GREEN);
                  String winText = "YOU WIN!";
                  g2.drawString(winText, (gamePanel.getWidth() - fm.stringWidth(winText)) / 2, (gamePanel.getHeight() - fm.getHeight()) / 2);               
               }
            }
         }
      };
      gamePanel.setPreferredSize(new Dimension(500, 500));
      cardPanel.add(gamePanel, "gamePanel"); 
   }
   
   //Creates settings
   public static void addSettingsPanel() {
      settings = new SettingsPanel();
      cardPanel.add(settings, "settings");
   }
   
   // Creates panel to hold buttons, timer, mine counter
   public static void addButtonPanel() {
      buttonPanel = new JPanel(new GridBagLayout());
      buttonPanel.setBorder(LOWERED);
      cMain.gridx = 0;
      cMain.gridy = 0;
      cMain.weighty = 0;
      mainPanel.add(buttonPanel, cMain);
      
      cButtons = new GridBagConstraints(); //constraints for layout within panel
      int insetButtons = 3;
      cButtons.insets = new Insets(insetButtons, insetButtons, insetButtons, insetButtons);
      cButtons.weightx = 1.0;
   }
   
   //Creates in-game timer
   public static void addTimer() {
      timer = new CustomTimer();
      timer.start();
      cButtons.gridx = 0;
      cButtons.anchor = GridBagConstraints.LINE_START;
      buttonPanel.add(timer, cButtons);
   }
   
   //Creates mine counter (displays number of flags player has left)
   public static void addMineCounter() {
      mineCounter = new JLabel(MisalignSweeper.numFlags + "");
      mineCounter.setForeground(Color.RED);
      mineCounter.setBackground(Color.BLACK);
      mineCounter.setOpaque(true);
      mineCounter.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 5));
      mineCounter.setFont(new Font("Consolas", Font.PLAIN, 20));
      cButtons.gridx = 2;
      cButtons.anchor = GridBagConstraints.LINE_END;
      buttonPanel.add(mineCounter, cButtons);
   }
   
   //Creates smile/reset button
   public static void addSmile() {
      frame.setIconImage(SMILE_ICON.getImage());
      smile = new JLabel(SMILE_ICON);
      smile.setBorder(RAISED);
      smile.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            if (!gamePaused && !playingLossAnimation)
               smile.setBorder(LOWERED);
         }
      
         @Override
         public void mouseReleased(MouseEvent e) {
            if (!gamePaused && !playingLossAnimation) {
               MisalignSweeper.numPoints = settings.getPoints();
               MisalignSweeper.numMines = settings.getMines();
               MisalignSweeper.numFlags = MisalignSweeper.numMines;
               MisalignSweeper.triToPolyRate = settings.getTriRate();
               //MisalignSweeper.seed = settings.getSeed();
               
               smile.setBorder(RAISED);
               smile.setIcon(SMILE_ICON);
               gameWon = false;
               MisalignSweeper.generateBoard();
               mineCounter.setText(MisalignSweeper.numFlags + "");
               cardLayout.show(cardPanel, "gamePanel");
               timer.restart();
               mainPanel.repaint();
            }
         }
      });       
      cButtons.gridx = 1;
      cButtons.anchor = GridBagConstraints.CENTER;
      buttonPanel.add(smile, cButtons);
   }
   
   //Creates pause button (that toggles between settings and the game)
   public static void addPause() {
      pause = new JLabel(PAUSE_ICON);
      pause.setBorder(RAISED);
      pause.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            pause.setBorder(LOWERED);
            timer.togglePause();
            if (gamePaused)
               cardLayout.show(cardPanel, "gamePanel");
            else {
               cardLayout.show(cardPanel, "settings");
               SettingsPanel.seedTextField.setText("" + MisalignSweeper.seed);
            }
            gamePaused = !gamePaused;
         }
      
         @Override
         public void mouseReleased(MouseEvent e) {
            pause.setBorder(RAISED);
         }
      });
      cButtons.gridx = 3;
      cButtons.anchor = GridBagConstraints.LINE_END;
      buttonPanel.add(pause, cButtons);
      

      gamePanel.addMouseListener(new MisalignInput());
      frame.pack();
      frame.setVisible(true);
   }

   
   
   //Returns what color a revealed poly should be
   public static Color getColor(int level) {
      Color[] colors = {         // Dark green
         Color.GRAY, Color.BLUE, new Color(9, 137, 58), Color.RED, new Color(0, 0, 60),
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
   public static void checkGameEnd() {
      if (gameLost()) {
         smile.setIcon(FROWN_ICON);
         timer.stop();
         revealAllMines();
      } else if (gameWon()) {
         smile.setIcon(GLASSES_ICON);
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
      
      Timer mineRevealTimer = new Timer(LOSS_ANIMATION_DELAY, null);
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
   
   //Returns frame
   public static JFrame getFrame() {
      return frame;
   }
   
   //Returns custom timer
   public static CustomTimer getTimer() {
      return timer;
   }
   
   //Returns settings
   public static SettingsPanel getSettings() {
      return settings;
   }
   
   //Returns mine counter
   public static JLabel getMineCounter() {
      return mineCounter;
   }
   
}