import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class SettingsPanel extends JPanel {

   private GridBagConstraints c;
   private ArrayList<JSpinner> spinners;
   public static JTextField seedTextField;
   private JCheckBox colorfulModeCheckBox;
   private JCheckBox noLinesModeCheckBox;
   public static boolean customSeedEntered = false;
   private static final Random rand = new Random();
   private Color[][] colors;
   
   public SettingsPanel(){
      this.spinners = new ArrayList<JSpinner>();
      this.setLayout(new GridBagLayout());
      this.c = new GridBagConstraints();
      this.add(new JLabel("Settings"));
      c.gridy = 0;
      this.addLabeledSpinner("Number of Points", Misalignsweeper.numPoints, 600, 800, 25, c);
      this.addLabeledSpinner("Number of Mines", Misalignsweeper.numMines, 25, 100, 5, c);
      this.addLabeledSpinner("Triangle Rate", (int)(Misalignsweeper.triToPolyRate * 10), 0, 10, 1, c);
      this.addLabeledSpinner("Polygon Iteration", Misalignsweeper.polyIteration, 2, 4, 1, c);
      this.addLabeledTextField("Seed:", "" + Misalignsweeper.seed);
      this.addLabeledSpinner("Theme", 1, 1, 5, 1, c);
      this.colorfulModeCheckBox = new JCheckBox("Colorful");
      this.noLinesModeCheckBox = new JCheckBox("No lines");
      readColors("palette.ppm");
   }
   
   // Add secret settings to setting menu (colorful mode, no lines mode)
   public void addSecretSettings() {
      c.gridy++;
      this.add(new JLabel("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"), c);
      c.gridy++;
      this.add(new JLabel("Secret Settings"), c);
      c.gridy++;
      this.add(colorfulModeCheckBox, c);
      c.gridy++;
      this.add(noLinesModeCheckBox, c);
      this.repaint();
      this.revalidate(); // revalidate needed to see immediate update if in settings when code is entered
   }
   
   // Adds a panel containing a label and spinner to the settings panel
   private void addLabeledSpinner(String text, int start, int min, int max, int step, GridBagConstraints c) {
      JPanel labelWithSpinner = new JPanel();
      labelWithSpinner.add(new JLabel(text + ":"));
      
      JSpinner spin = new JSpinner(new SpinnerNumberModel(start, min, max, step));
      spin.setEditor(new JSpinner.DefaultEditor(spin));
      spin.setPreferredSize(new Dimension(45, 30));
      
      labelWithSpinner.add(spin);
      spinners.add(spin);
      c.gridy++;
      this.add(labelWithSpinner, c);      
   }
   
   // Adds textbox where a seed can be enetered/copied
   private void addLabeledTextField(String labelText, String fieldText) {
      JPanel labelWithTextField = new JPanel(); // combines label, textfield, and button into one panel
      seedTextField = new JTextField(fieldText);
      JButton seedSubmitButton = new JButton("Submit");
      seedSubmitButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            customSeedEntered = true;
         }
      });
      
      // button formatting
      seedSubmitButton.setFocusPainted(false);
      seedSubmitButton.setContentAreaFilled(false);
      seedSubmitButton.setMargin(new Insets(0,2,0,2));
      
      labelWithTextField.add(new JLabel(labelText));
      labelWithTextField.add(seedTextField);
      labelWithTextField.add(seedSubmitButton);
      c.gridy++;
      this.add(labelWithTextField, c);   
   }
   
   //Reads color palette from (ASCII) .ppm image
   private void readColors(String filename) {
      Scanner sc = new Scanner(getClass().getResourceAsStream(filename));
      if (!sc.next().equals("P3")) {
         return;
      }
      int width = sc.nextInt();
      int height = sc.nextInt();
      colors = new Color[height][width];
      if (sc.nextInt() != 255) {
         return;
      }
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            colors[y][x] = new Color(sc.nextInt(), sc.nextInt(), sc.nextInt());
         }
      }
   }
   
   //Gets correct color given palette
   public Color getColor(int index) {
      if ((index == 0 || index == 4) && this.colorfulModeCheckBox.isSelected()) {
         return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
      }
      return colors[(Integer) spinners.get(4).getValue() - 1][index];
   }
   
   // Returns points spinner value
   public int getPoints() {
      return (Integer)spinners.get(0).getValue();
   }

   // Returns mines spinner value   
   public int getMines() {
      return (Integer)spinners.get(1).getValue();
   }
   
   // Returns tri to poly rate spinner value
   public double getTriRate() {
      return (int) spinners.get(2).getValue() / 10.0;
   }
   
   // Returns the width of the bell curve on polygon generation
   public int getPolyIteration() {
      return (Integer)spinners.get(3).getValue();
   }
   
   // Returns if no lines mode is enabled
   public boolean noLinesModeChecked() {
      return this.noLinesModeCheckBox.isSelected();
   }
}     