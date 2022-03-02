import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SettingsPanel extends JPanel {

   private ArrayList<JSpinner> spinners;
   private GridBagConstraints c;
   private JCheckBox colorfulModeCheckBox;
   private JCheckBox noLinesModeCheckBox;
   
   public SettingsPanel() {
      this.spinners = new ArrayList<JSpinner>();
      this.setLayout(new GridBagLayout());
      this.c = new GridBagConstraints();
      this.add(new JLabel("Settings"));
      c.gridy = 1;
      this.addLabeledSpinner("Number of Points", MisalignSweeper.numPoints, 400, 800, 25, c);
      c.gridy++;
      this.addLabeledSpinner("Number of Mines", MisalignSweeper.numMines, 25, 100, 5, c); 
      c.gridy++;
      this.addLabeledSpinner("Triangle Rate", (int)(MisalignSweeper.triToPolyRate * 10), 0, 10, 1, c); 
      this.colorfulModeCheckBox = new JCheckBox("Colorful");
      this.noLinesModeCheckBox = new JCheckBox("No lines");
   }
   
   //Add secret settings to setting menu (colorful mode, no lines mode)
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
   
   // Returns if check box is checked
   public boolean colorfulModeChecked() {
      return this.colorfulModeCheckBox.isSelected();
   }
   
   // Returns if check box is checked
   public boolean noLinesModeChecked() {
      return this.noLinesModeCheckBox.isSelected();
   }
   
}     