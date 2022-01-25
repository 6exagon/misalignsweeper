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
      this.addLabeledSpinner("Number of Points", 200, 10, 1000, 5, c);
      c.gridy = 2;
      this.addLabeledSpinner("Number of Mines", 15, 5, 50, 1, c); 
      c.gridy = 3;
      this.addLabeledSpinner("Max Lines Per Point", 6, 3, 25, 1, c);
//       c.gridy = 4;
//       this.add(new JLabel(" "), c); //this is bad
//       c.gridy = 5;
//       this.add(new JLabel(" "), c); //this is bad (again)
//       c.gridy = 6;
//       c.anchor = GridBagConstraints.PAGE_END;  
//       this.add(new JLabel("Reset game for new settings"), c);
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
   
   // Returns connections spinner value
   public int getNears() {
      return (Integer)spinners.get(2).getValue();
   }
   
}     