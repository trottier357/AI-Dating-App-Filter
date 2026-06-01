package studio.trottier.logic.util.launchers;

import studio.trottier.logic.util.launchers.base.DriverLauncher;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChromeLauncher extends DriverLauncher {
    private static final Path USER_DATA_DIR = Paths.get(System.getProperty("user.home"), "chrome-dev-profile");

    private final String EXECUTABLE_PATH;

    public ChromeLauncher(String executablePath){
        this.EXECUTABLE_PATH = executablePath;
    }

    @Override
    public WebDriver launch(String startUrl, int debugPort, long timeoutMillis) throws InterruptedException, IOException {
        startChrome(startUrl, debugPort);

        waitForDebugger(getAddress(debugPort), timeoutMillis);

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", getAddress(debugPort));
        return new ChromeDriver(options);
    }

    private Process startChrome(String startUrl, int debugPort) throws IOException {
        Files.createDirectories(USER_DATA_DIR);

        ProcessBuilder pb = new ProcessBuilder(
                EXECUTABLE_PATH,
                "--remote-debugging-port=" + debugPort,
                "--user-data-dir=" + USER_DATA_DIR,
                "--no-first-run",
                "--no-default-browser-check",
                startUrl
        );

        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        return pb.start();
    }

    private static void waitForDebugger(String debugAddress, long timeoutMillis) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        URL url;
        try {
            url = new URL("http://" + debugAddress + "/json/version");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        while (System.currentTimeMillis() < deadline) {
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(500);
                conn.setReadTimeout(500);
                if (conn.getResponseCode() == 200) return;
            } catch (IOException ignored) {
                // Not up yet.
            }
            Thread.sleep(250);
        }
        System.out.println("Warning: DevTools endpoint not detected within "
                + timeoutMillis + "ms. Continuing anyway.");
    }
}
