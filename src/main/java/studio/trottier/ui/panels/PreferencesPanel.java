package studio.trottier.ui.panels;

import studio.trottier.Constants;
import studio.trottier.ui.UIUtils;
import studio.trottier.ui.base.WrappingPanel;

import javax.swing.*;
import java.awt.*;

public class PreferencesPanel extends WrappingPanel {
    private final JTextArea goodReferenceArea;
    private final JTextArea badReferenceArea;
    private final JSpinner thresholdField;

    public PreferencesPanel(JPanel parent) {
        super(parent);

        goodReferenceArea = new JTextArea(Constants.DEFAULT_GOOD_REFERENCE, 5, 20);
        goodReferenceArea.setLineWrap(true);
        goodReferenceArea.setWrapStyleWord(true);
        add(UIUtils.labeled("Ideal Description", new JScrollPane(goodReferenceArea), true));

        badReferenceArea = new JTextArea(Constants.DEFAULT_BAD_REFERENCE, 5, 20);
        badReferenceArea.setLineWrap(true);
        badReferenceArea.setWrapStyleWord(true);
        add(UIUtils.labeled("Opposite (Bad) Description", new JScrollPane(badReferenceArea), true));

        thresholdField = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_THRESHOLD, 0, 1, 0.01));
        thresholdField.setPreferredSize(new Dimension(60, thresholdField.getPreferredSize().height));
        add(UIUtils.labeled("Minimum Score: ", thresholdField));
    }

    public String getGoodReference() {
        return goodReferenceArea.getText();
    }

    public String getBadReference() {
        return badReferenceArea.getText();
    }

    public float getThreshold() {
        return ((Double)thresholdField.getValue()).floatValue();
    }
}
