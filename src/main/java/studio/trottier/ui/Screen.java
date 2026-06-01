package studio.trottier.ui;

import studio.trottier.Constants;
import studio.trottier.logic.Program;
import studio.trottier.logic.ai.Assessor;
import studio.trottier.logic.util.launchers.ChromeLauncher;
import studio.trottier.ui.panels.*;
import org.openqa.selenium.WebDriver;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class Screen {

    private final ControlPanel controlPanel;
    private final TechnicalPanel technicalPanel;
    private final PreferencesPanel preferencesPanel;
    private final ProfilePanel profilePanel;
    private final ResultPanel resultPanel;

    private Program program;

    public Screen(){
        ImageIO.scanForPlugins(); // Allow WEBP support.

        JFrame frame = new JFrame(Constants.APPLICATION_NAME);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        frame.setContentPane(new JScrollPane(root,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        ));

        technicalPanel = new TechnicalPanel(root);
        preferencesPanel = new PreferencesPanel(root);
        controlPanel = new ControlPanel(root, this::start, this::pause);
        resultPanel = new ResultPanel(root, this::skip);
        profilePanel = new ProfilePanel(root);

        frame.setSize(1080, 720);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void start() {

        Assessor assessor = new Assessor(
                technicalPanel.getModelName(),
                preferencesPanel.getGoodReference(),
                preferencesPanel.getBadReference(),
                preferencesPanel.getThreshold(),
                List.of(resultPanel)
        );

        WebDriver driver;
        try {
            driver = new ChromeLauncher(technicalPanel.getChromePath()).launch(
                    controlPanel.getSelectedPlatform().getBrowseUrl(),
                    technicalPanel.getBrowserPort(),
                    technicalPanel.getTimeoutSeconds() * 1000L
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        program = new Program(
                driver,
                controlPanel.getSelectedPlatform().getPlatformClass(),
                assessor,
                List.of(profilePanel, resultPanel)
        );

        Thread programThread = new Thread(() -> program.run(), "Program-Runner");
        programThread.setDaemon(true);
        programThread.start();
    }

    private void pause(){
        if(program != null){
            program.pause();
        }
    }

    private void skip(){
        if(program != null){
            // TODO
        }
    }
}
