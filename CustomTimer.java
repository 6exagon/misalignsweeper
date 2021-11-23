import javax.swing.*;
import java.awt.event.*;
import java.awt.Font;
import java.awt.Color;
import javax.swing.border.*;
//import java.util.*;

public class CustomTimer extends JLabel {
   
   private int secondsPassed;
   private int delay;
   private ActionListener taskPerformer;
   private Timer timer;
   private boolean timerPaused;

   public CustomTimer(int delay) {
      this.delay = delay;
      this.taskPerformer = new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (!timerPaused)
               secondsPassed++;
            String minutes = String.format("%02d", secondsPassed / 60);
            String seconds = String.format("%02d", secondsPassed % 60);
            setText(minutes + ":" + seconds);
         }
      };
      this.timer = new Timer(delay, taskPerformer);
      this.timerPaused = false;
      this.setForeground(Color.RED);
      this.setFont(new Font("Consolas", Font.PLAIN, 20));
      this.setText("00:00");
      this.setBackground(Color.BLACK);
      this.setOpaque(true);
      this.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
   }
   
   // Starts timer
   public void start() {
      this.timer.start();
   }
   
   // Pause/Unpauses timer
   public void togglePause() {
      this.timerPaused = !this.timerPaused;
   }
   
   // Restarts timer
   public void restart() {
      this.timer.stop();
      this.secondsPassed = 0;
      this.setText("00:00");
      this.timer.start();
   }
   
}