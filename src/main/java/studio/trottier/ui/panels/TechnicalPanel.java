package studio.trottier.ui.panels;

import studio.trottier.Constants;
import studio.trottier.ui.UIUtils;
import studio.trottier.ui.base.WrappingPanel;

import javax.swing.*;

public class TechnicalPanel extends WrappingPanel {
    private final JSpinner browserPortField;
    private final JSpinner timeoutField;
    private final JTextField modelNameField;

    private String chromePath = Constants.DEFAULT_CHROME_PATH;

    public TechnicalPanel(JPanel parent){
        super(parent);

        JButton saveButton = new JButton("Save Settings");
        saveButton.addActionListener(e -> {
            // TODO
        });
        add(saveButton);

        JButton loadButton = new JButton("Load Settings");
        loadButton.addActionListener(e -> {
            // TODO
        });
        add(loadButton);

        browserPortField = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_DEBUG_PORT, 1024, 65535, 1));
        JSpinner.NumberEditor portFormat = new JSpinner.NumberEditor(browserPortField, "#");
        browserPortField.setEditor(portFormat);
        add(UIUtils.labeled("Browser Port", browserPortField));

        timeoutField = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_TIMEOUT_SECONDS, 1, 120, 1));
        add(UIUtils.labeled("Maximum Timeout (seconds): ", timeoutField));

        modelNameField = new JTextField(Constants.DEFAULT_MODEL, 32);
        add(UIUtils.labeled("Model Name: ", modelNameField));

        JButton chromePathButton = new JButton(chromePath);
        chromePathButton.addActionListener(e -> {
            String picked = UIUtils.chooseFilePath();
            if(!picked.isEmpty()){
                chromePath = picked;
                chromePathButton.setText(chromePath);
            }
        });
        JPanel chromePathPanel = UIUtils.labeled("Chrome Path: ", chromePathButton);
        add(chromePathPanel);

        //UIUtils.addStacked(root, technicalPanel);
    }

    public int getBrowserPort(){
        return (Integer)browserPortField.getValue();
    }

    public int getTimeoutSeconds(){
        return (Integer)timeoutField.getValue();
    }

    public String getModelName(){
        return modelNameField.getText();
    }

    public String getChromePath(){
        return chromePath;
    }
}
