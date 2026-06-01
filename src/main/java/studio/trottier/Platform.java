package studio.trottier;

import studio.trottier.logic.pages.base.ProfilePage;

public class Platform {
    private String name;
    private Class<? extends ProfilePage> platformClass;
    private String browseUrl;

    public Platform(String name, Class<? extends ProfilePage> platformClass, String browseUrl) {
        this.name = name;
        this.platformClass = platformClass;
        this.browseUrl = browseUrl;
    }

    public String getName() {
        return name;
    }

    public Class<? extends ProfilePage> getPlatformClass() {
        return platformClass;
    }

    public String getBrowseUrl() {
        return browseUrl;
    }
}
