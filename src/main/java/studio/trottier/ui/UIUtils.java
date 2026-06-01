package studio.trottier.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class UIUtils {
    public static JPanel labeled(String text, JComponent c){
        return labeled(text, c, false);
    }

    public static JPanel labeled(String text, JComponent c, boolean stacked) {
        JPanel p = new JPanel();
        if (stacked) {
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        } else {
            p.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
        }
        JLabel l = new JLabel(text);
        l.setLabelFor(c);
        if (stacked) {
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        p.add(l);
        p.add(c);
        return p;
    }

    public static String chooseFilePath(){
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(null);

        if(result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            String path = selectedFile.getAbsolutePath();
            System.out.println("You picked: " + path);
            return path;
        }

        System.out.println("No file selected.");

        return "";
    }

    public static void addStacked(JPanel parent, JPanel child){
        child.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(child);
    }
}
