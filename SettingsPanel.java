import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.event.*;

public class SettingsPanel extends JPanel {

   private GridBagConstraints c;
   private ArrayList<JSpinner> spinners;
   public static JTextField seedTextField;
   private JCheckBox colorfulModeCheckBox;
   private JCheckBox noLinesModeCheckBox;
   public static boolean customSeedEntered = false;
   
   public SettingsPanel() {
      this.spinners = new ArrayList<JSpinner>();
      this.setLayout(new GridBagLayout());
      this.c = new GridBagConstraints();
      this.add(new JLabel("Settings"));
      c.gridy = 1;
      this.addLabeledSpinner("Number of Points", MisalignSweeper.numPoints, 600, 800, 25, c);
      c.gridy = 2;
      this.addLabeledSpinner("Number of Mines", MisalignSweeper.numMines, 25, 100, 5, c); 
      c.gridy = 3;
      this.addLabeledSpinner("Triangle Rate", (int)(MisalignSweeper.triToPolyRate * 10), 0, 10, 1, c); 
      c.gridy = 4;
      this.addLabeledTextField("Seed: ", "" + MisalignSweeper.seed);
      this.colorfulModeCheckBox = new JCheckBox("Colorful");
      this.noLinesModeCheckBox = new JCheckBox("No lines");
   }
   
   //Add secret settings to setting menu (colorful mode, no lines mode)
   public void addSecretSettings() {
      c.gridy = 5;
      this.add(new JLabel("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"), c);
      c.gridy = 6;
      this.add(new JLabel("Secret Settings"), c);
      c.gridy = 7;
      this.add(colorfulModeCheckBox, c);
      c.gridy = 8;
      this.add(noLinesModeCheckBox, c);
      this.repaint();
      this.revalidate(); //revalidate needed to see immediate update if in settings when code is entered
   }
   
   // Adds a panel containing a label and spinner to the settings panel
   private void addLabeledSpinner(String text, int start, int min, int max, int step, GridBagConstraints c) {
      JPanel labelWithSpinner = new JPanel();
      labelWithSpinner.add(new JLabel(text + ":   "));
      
      JSpinner spin = new JSpinner(new SpinnerNumberModel(start, min, max, step));
      spin.setEditor(new JSpinner.DefaultEditor(spin));
      spin.setPreferredSize(new Dimension(44, 30));
      
      labelWithSpinner.add(spin);
      spinners.add(spin);
      this.add(labelWithSpinner, c);      
   }
   
   //Adds textbox where a seed can be enetered/copied
   private void addLabeledTextField(String labelText, String fieldText) {
      JPanel labelWithTextField = new JPanel(); //combines label, textfield, and button into one panel
      seedTextField = new JTextField(fieldText);
      JButton seedSubmitButton = new JButton("Submit");
      seedSubmitButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            customSeedEntered = true;
         }
      });
      //just button formatting
      seedSubmitButton.setFocusPainted(false);
      seedSubmitButton.setContentAreaFilled(false);
      seedSubmitButton.setMargin(new Insets(0,2,0,2));
      
      labelWithTextField.add(new JLabel(labelText));
      labelWithTextField.add(seedTextField);
      labelWithTextField.add(seedSubmitButton);
      this.add(labelWithTextField, c);   
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
      return (int)spinners.get(2).getValue() / 10.0;
   }
   
   // Returns if colorful mode is enabled
   public boolean colorfulModeChecked() {
      return this.colorfulModeCheckBox.isSelected();
   }
   
   // Returns if no lines mode is enabled
   public boolean noLinesModeChecked() {
      return this.noLinesModeCheckBox.isSelected();
   }
   
}     