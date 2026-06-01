package studio.trottier.ui.panels;

import studio.trottier.Constants;
import studio.trottier.Platform;
import studio.trottier.ui.UIUtils;
import studio.trottier.ui.base.WrappingPanel;

import javax.swing.*;

public class ControlPanel extends WrappingPanel {
    private final JComboBox<String> platformDropdown;
    private String selectedPlatform;

    public ControlPanel(JPanel parent, Runnable start, Runnable pause) {
        super(parent);

        platformDropdown = new JComboBox<>(Constants.PLATFORMS.keySet().toArray(new String[0]));
        platformDropdown.addActionListener(e -> {
            selectedPlatform = (String) platformDropdown.getSelectedItem();
        });
        platformDropdown.setSelectedIndex(0);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            platformDropdown.setEditable(false);
            start.run();
        });
        add(startButton);

        JButton pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> {
            pause.run();
        });
        add(pauseButton);

        // TODO add stop that makes platformDropdown editable

        add(UIUtils.labeled("Platform: ", platformDropdown));
    }

    public Platform getSelectedPlatform() {
        return Constants.PLATFORMS.get(selectedPlatform);
    }
}
