package studio.trottier.ui.panels;

import studio.trottier.logic.base.AssessorListener;
import studio.trottier.logic.base.ProgramListener;
import studio.trottier.logic.pages.base.ProfilePage;
import studio.trottier.ui.base.WrappingPanel;

import javax.swing.*;
import java.awt.*;

public class ResultPanel extends WrappingPanel implements ProgramListener, AssessorListener {
    private final JLabel scoreLabel;
    private final JButton skipButton;

    public ResultPanel(JPanel parent, Runnable onSkip) {
        super(parent);

        scoreLabel = new JLabel();
        add(scoreLabel);

        skipButton = new JButton("Skip");
        skipButton.addActionListener(e -> onSkip.run());
        add(skipButton);

        reset();
    }

    private void reset(){
        scoreLabel.setText("Waiting for assessment...");
        scoreLabel.setForeground(Color.GRAY);

        skipButton.setVisible(false);
    }

    @Override
    public void onNewAssessment(boolean isMatch, float score, float outOf) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> onNewAssessment(isMatch, score, outOf));
            return;
        }

        scoreLabel.setText(String.format("Score: %.2f / %.2f - %s", score, outOf, isMatch ? "Good Match" : "Bad Match"));
        scoreLabel.setForeground(isMatch ? Color.GREEN.darker() : Color.RED);

        skipButton.setVisible(isMatch);
    }

    @Override
    public void onNewProfile(ProfilePage profile) {
        reset();
    }
}
