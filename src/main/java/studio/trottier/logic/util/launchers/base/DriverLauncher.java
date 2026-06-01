package studio.trottier.logic.util.launchers.base;

import org.openqa.selenium.WebDriver;

import java.io.IOException;

public abstract class DriverLauncher {
    protected final String LOCAL_HOST = "127.0.0.1";

    protected String getAddress(int port){
        return LOCAL_HOST + ":" + port;
    }

    public abstract WebDriver launch(String startUrl, int debugPort, long timeoutMillis) throws InterruptedException, IOException;
}
