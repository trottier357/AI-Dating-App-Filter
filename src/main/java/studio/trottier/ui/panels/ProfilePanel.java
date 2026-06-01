package studio.trottier.ui.panels;

import studio.trottier.logic.base.ProgramListener;
import studio.trottier.logic.pages.base.ProfilePage;
import studio.trottier.ui.base.WrappingPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ProfilePanel extends WrappingPanel implements ProgramListener {
    public ProfilePanel(JPanel parent) {
        super(parent);
    }

    @Override
    public void onNewProfile(ProfilePage profile) {
        final String info;
        final List<String> photoUrls;
        try {
            info = profile.getInfo();
            photoUrls = profile.getPhotoUrls();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        SwingUtilities.invokeLater(() -> renderProfile(info, photoUrls));
    }

    private void renderProfile(String info, List<String> photoUrls) {
        removeAll();
        int wrapWidth = Math.max(200, getWidth() - 20);
        JLabel infoLabel = new JLabel("<html><body style='width:" + wrapWidth + "px'>"
                + info.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>")
                + "</body></html>");
        add(infoLabel);

        final int THUMB_H = 200;
        final int PLACEHOLDER_W = 120;

        JPanel imagesPanel = new WrappingPanel(this);
        for(String url : photoUrls){
            JLabel imageComponent = new JLabel("Loading...", SwingConstants.CENTER);
            imageComponent.setPreferredSize(new Dimension(PLACEHOLDER_W, THUMB_H));
            imageComponent.setForeground(Color.ORANGE.darker());
            imagesPanel.add(imageComponent);
            new SwingWorker<BufferedImage, Void>() {
                protected BufferedImage doInBackground() throws Exception {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestProperty("User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                                    + "(KHTML, like Gecko) Chrome/124.0 Safari/537.36");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(15000);
                    conn.setInstanceFollowRedirects(true);
                    try (InputStream in = conn.getInputStream()) {
                        return ImageIO.read(in);
                    }
                }
                protected void done() {
                    try {
                        BufferedImage img = get();
                        if (img == null) {
                            imageComponent.setText("Unsupported");
                        } else {
                            int srcW = img.getWidth();
                            int srcH = img.getHeight();
                            int scaledW = (srcH > 0)
                                    ? Math.max(1, Math.round(srcW * (THUMB_H / (float) srcH)))
                                    : PLACEHOLDER_W;
                            Image scaled = img.getScaledInstance(scaledW, THUMB_H, Image.SCALE_SMOOTH);
                            imageComponent.setIcon(new ImageIcon(scaled));
                            imageComponent.setText(null);
                            imageComponent.setPreferredSize(new Dimension(scaledW, THUMB_H));
                        }
                    } catch (Exception e) {
                        imageComponent.setText("Failed");
                        e.printStackTrace();
                    }
                    revalidate();
                }
            }.execute();
        }

        revalidate();
        repaint();
    }
}
