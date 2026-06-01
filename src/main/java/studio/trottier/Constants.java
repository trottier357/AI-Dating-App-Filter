package studio.trottier;

import studio.trottier.logic.pages.profiles.BooProfilePage;

import java.util.HashMap;

public class Constants {
    /**
     * Register supported platforms here.
     */
    public static final HashMap<String, Platform> PLATFORMS = new HashMap<>(){{
        put("Boo", new Platform("Boo", BooProfilePage.class, "https://boo.world/match"));
    }};

    public static final String APPLICATION_NAME = "AI Dating App Filter";

    // ---------- DEFAULT CONFIGURATION VALUES ----------
    public static final String DEFAULT_MODEL = "llama3.1:8b";
    public static final int DEFAULT_DEBUG_PORT = 9222;
    public static final int DEFAULT_TIMEOUT_SECONDS = 40;
    public static final String DEFAULT_GOOD_REFERENCE = "Calm and thoughtful person.";
    public static final String DEFAULT_BAD_REFERENCE = "Obnoxious and impulsive person.";
    public static final float DEFAULT_THRESHOLD = 0.8f;
    public static final String DEFAULT_CHROME_PATH = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
}
