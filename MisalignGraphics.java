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
   public static final ImageIcon SMILE_ICON = getScaledImageIcon("images/smile.png", ICON_SIZE, -1); //-1 keeps original w:h ratio
   public static final ImageIcon FROWN_ICON = getScaledImageIcon("images/dead.png", ICON_SIZE, -1);
   public static final ImageIcon GLASSES_ICON = getScaledImageIcon("images/glasses.png", ICON_SIZE, -1);
   public static final ImageIcon PAUSE_ICON = getScaledImageIcon("images/pause.png", ICON_SIZE, -1);
   public static final Image FLAG_IMAGE = new ImageIcon(MisalignGraphics.class.getResource("images/flag.png")).getImage();
   public static final Image MINE_IMAGE = new ImageIcon(MisalignGraphics.class.getResource("images/mine.png")).getImage();
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
      
      settings = new SettingsPanel();
      createFrame();
      addCardPanel();
      addGamePanel(rand);
      cardPanel.add(settings, "settings");
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
      int iMain = 5;
      cMain.insets = new Insets(iMain, iMain, iMain, iMain); //no, there isn't a better constructor
      cMain.gridwidth = 2;
      cMain.anchor = GridBagConstraints.CENTER;
      cMain.fill = GridBagConstraints.BOTH;
      cMain.weightx = 1;
   }
   
   // Creates the cardPanel containing the game board and settings
   public static void addCardPanel() {
      cardPanel = new JPanel(new CardLayout());
      cardPanel.setBorder(new CompoundBorder(LOWERED, new LineBorder(settings.getColor(0), 1)));
      cardLayout = (CardLayout) cardPanel.getLayout();

      cMain.weighty = 1;
      cMain.gridx = 0;
      cMain.gridy = 1;
      mainPanel.add(cardPanel, cMain);
   }
   
   //Creates panel that game is played on
   public static void addGamePanel(Random rand) {
      gamePanel = new GamePanel(polyToGon, rand);
      cardPanel.add(gamePanel, "gamePanel"); 
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
      int iButtons = 3;
      cButtons.insets = new Insets(iButtons, iButtons, iButtons, iButtons);
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
      mineCounter = new JLabel(Misalignsweeper.numFlags + "");
      mineCounter.setForeground(settings.getColor(7));
      mineCounter.setBackground(settings.getColor(8));
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
               Misalignsweeper.numPoints = settings.getPoints();
               Misalignsweeper.numMines = settings.getMines();
               Misalignsweeper.numFlags = Misalignsweeper.numMines;
               Misalignsweeper.triToPolyRate = settings.getTriRate();
               
               smile.setBorder(RAISED);
               smile.setIcon(SMILE_ICON);
               gameWon = false;
               Misalignsweeper.generateBoard();
               mineCounter.setText(Misalignsweeper.numFlags + "");
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
               SettingsPanel.seedTextField.setText("" + Misalignsweeper.seed);
            }
            gamePaused = !gamePaused;
         }
      
         @Override
         public void mouseReleased(MouseEvent e) {
            pause.setBorder(RAISED);
            
            //sets counter and timer color to match theme
            mineCounter.setForeground(settings.getColor(7));
            mineCounter.setBackground(settings.getColor(8));
            timer.setForeground(settings.getColor(7));
            timer.setBackground(settings.getColor(8));
            
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
      return (level == 0) ? settings.getColor(9) : settings.getColor((level - 1) % 10 + 10);
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
   
   public static void setXM(double value) {
      xm = value;
   }  
   public static void setYM(double value) {
      ym = value;
   }
}