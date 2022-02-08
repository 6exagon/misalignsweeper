import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;

public class MisalignGraphics {
   public static final int HEIGHT = 500;
   public static final int WIDTH = 500;
   public static double xMultiplier = 1.0;
   public static double yMultiplier = 1.0;
   public static boolean playingLossAnimation = false;
   public boolean gameWon = false;
   public HashSet<Line> lines;
   public HashMap<Tri, Polygon> tritogon;
   public JFrame frame;
   public boolean gamePaused = false;
   private SettingsPanel settings;
   private JPanel cardPanel;
   private CustomTimer timer;
   private JLabel mineCounter;
   private JLabel smile;
   
   public JPanel gamePanel;

   public MisalignGraphics(HashSet<Line> lines, HashMap<Tri, Polygon> tritogon) {
      this.lines = lines;
      this.tritogon = tritogon;
   }
   
   public void createAndShowGUI(MisalignInput input, Random rand) {
   
      Border lowered = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
      Border raised = BorderFactory.createBevelBorder(BevelBorder.RAISED);
      int iconSize = 30;
      ImageIcon smileIcon = getScaledImageIcon("minesweeper smile.png", iconSize, -1);
      ImageIcon frownIcon = getScaledImageIcon("minesweeper frown.png", iconSize, -1);
      ImageIcon glassesIcon = getScaledImageIcon("minesweeper glasses.png", iconSize, -1);
      ImageIcon pauseIcon = getScaledImageIcon("pause.png", iconSize, -1);
   
      // Creates window and main mainPanel
      this.frame = new JFrame("Misalignsweeper");
      JPanel mainPanel = new JPanel(new GridBagLayout());
      //frame.setResizable(false);
      mainPanel.setBorder(raised);
      frame.add(mainPanel);
      
      // Creates mainPanel that holds cards (gamePanel, settingPanel)
      cardPanel = new JPanel(new CardLayout());
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
      this.gamePanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
               super.paintComponent(g);
               Graphics2D g2 = (Graphics2D) g;
            
               if (!playingLossAnimation && !gamePaused)
                  checkGameEnd(glassesIcon, frownIcon);
            
               if (!gamePaused) {
                  MisalignGraphics.xMultiplier = this.getWidth() / (double)MisalignGraphics.WIDTH;
                  MisalignGraphics.yMultiplier = this.getHeight() / (double)MisalignGraphics.HEIGHT;
                  g2.scale(MisalignGraphics.xMultiplier, MisalignGraphics.yMultiplier);
               
                  for (Tri tri : tritogon.keySet()) {
                     if (tri.isPressed()) {
                        switch (tri.getDisplayState()) {
                           case -2:
                           case -1:
                              g2.setColor(Color.BLACK);
                              break;
                           default:
                              g2.setColor(getColor(tri.getDisplayState()));
                        }
                     } else if (tri.isFlagged()) {
                        g2.setColor(Color.YELLOW);
                     } else {
                        g2.setColor(Color.WHITE);
                     }
                     g2.fillPolygon(tritogon.get(tri));
                  
                     if (tri.isPressed() && tri.getDisplayState() > 0)
                        tri.drawNum(g2);
                     else if (tri.isFlagged()) //must draw flag after updating color
                        tri.drawFlag(g2);
                     
                  }
                  for (Line l : lines) {
                     g2.setColor(Color.BLACK); 
                     g2.drawLine(l.getP().getX(), l.getP().getY(), l.getQ().getX(), l.getQ().getY());
                  }
               
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
      this.timer = new CustomTimer();
      timer.start();
      cButtons.gridx = 0;
      cButtons.anchor = GridBagConstraints.LINE_START;
      buttonPanel.add(timer, cButtons);
   
      // Creates mine counter
      this.mineCounter = new JLabel(String.format("%03d", MisalignSweeper.numFlags)) {
            @Override
            public void paintComponent(Graphics g) {
               super.paintComponent(g);
               this.setText(MisalignSweeper.numFlags + "");
            }
         };
      mineCounter.setForeground(Color.RED);
      mineCounter.setBackground(Color.BLACK);
      mineCounter.setOpaque(true);
      mineCounter.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 5));
      mineCounter.setFont(new Font("Consolas", Font.PLAIN, 20));
      cButtons.gridx = 2;
      cButtons.anchor = GridBagConstraints.LINE_END;
      buttonPanel.add(mineCounter, cButtons);
      
      // Creates smile (reset) button
      this.frame.setIconImage(smileIcon.getImage());
      this.smile = new JLabel(smileIcon);
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
                  MisalignSweeper.numNears = settings.getNears();
                  
                  CardLayout c = (CardLayout)(cardPanel.getLayout());
                  smile.setBorder(raised);
                  smile.setIcon(smileIcon);
                  gameWon = false;
                  MisalignSweeper.generateBoard();
                  mineCounter.setText(String.format("%03d", MisalignSweeper.numFlags));
                  c.show(cardPanel, "gamePanel");
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
               CardLayout c = (CardLayout)(cardPanel.getLayout());
               if (gamePaused)
                  c.show(cardPanel, "gamePanel");
               else
                  c.show(cardPanel, "settings");
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
      return new ImageIcon(new ImageIcon(getClass().getResource(path)).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
   }
   
   public SettingsPanel getSettings() {
      return this.settings;
   }
   
   // Returns color based on adjacent mines
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
   
   // Checks if the player has won or lost
   public void checkGameEnd(ImageIcon winIcon, ImageIcon loseIcon) {
      CardLayout c = (CardLayout)(cardPanel.getLayout());
      if (gameLost()) {
         smile.setIcon(loseIcon);
         timer.stop();
         revealAllMines();
      } else if (gameWon()) {
         smile.setIcon(winIcon);
         timer.stop();
         this.gameWon = true;
      }  
   }
  
   // Checks if player won the game (all non-mines are revealed) 
   public boolean gameWon() {
      for (Tri t : tritogon.keySet())
         if (t.getDisplayState() != -1 && !t.isPressed())
            return false;
      return true;
   }
   
   // Checks if the player lost (revealed a mine)
   public boolean gameLost() {
      for (Tri t : tritogon.keySet())
         if (t.getDisplayState() == -2)
            return true;
      return false;
   }
   
   // Reveals all mines and swtiches to losing screen
   public void revealAllMines() { 
      playingLossAnimation = true; // prevents clicking and button presses during loss animation
      ArrayList<Tri> mines = new ArrayList<Tri>();
      for (Tri poly : tritogon.keySet())
         if (poly.getDisplayState() == -1)
            mines.add(poly);
                  
      int delay = 150; // in milliseconds
      javax.swing.Timer mineRevealTimer = new javax.swing.Timer(delay, null); //specific Timer name since there's a Timer in both .util and .swing
      mineRevealTimer.addActionListener(new ActionListener() { //adding listener later lets us stop the timer within the listener more easily
            @Override
            public void actionPerformed(ActionEvent e) {
               if (mines.size() >= 1) {
                  mines.get(0).reveal();
                  mines.remove(0); 
                  gamePanel.repaint();   
               } else {
                  playingLossAnimation = false;
                  mineRevealTimer.stop();
               }
            }
         });
      mineRevealTimer.start();
   }

}