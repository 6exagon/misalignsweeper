import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SettingsPanel extends JPanel {

   private ArrayList<JSpinner> spinners;

   public SettingsPanel() {
      this.spinners = new ArrayList<JSpinner>();
      this.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      this.add(new JLabel("Settings"));
      c.gridy = 1;
      this.addLabeledSpinner("Number of Points", MisalignSweeper.numPoints, 10, 1000, 5, c);
      c.gridy = 2;
      this.addLabeledSpinner("Number of Mines", MisalignSweeper.numMines, 5, 100, 1, c); 
      c.gridy = 3;
      this.addLabeledSpinner("Triangle Rate", (int)(MisalignSweeper.triToPolyRate * 10), 0, 10, 1, c); 
   }
   
   // Adds a panel containing a label and spinner to the settings panel
   private void addLabeledSpinner(String text, int start, int min, int max, int step, GridBagConstraints c) {
      JPanel labelWithSpinner = new JPanel();
      
      JLabel label = new JLabel(text + ":   ");
      labelWithSpinner.add(label);
      
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
   
}     