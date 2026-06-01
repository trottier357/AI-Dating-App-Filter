package studio.trottier.logic.pages.debug;

import ai.djl.util.Pair;
import studio.trottier.logic.pages.base.ProfilePage;

import java.util.List;

public class DummyProfilePage extends ProfilePage {
    private String output;

    public DummyProfilePage(String output) {
        super(null);
        this.output = output;
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getUrl() {
        return "";
    }

    @Override
    public String getBio() {
        return "";
    }

    @Override
    public List<String> getTags() {
        return null;
    }

    @Override
    public List<Pair<String, String>> getPrompts() {
        return null;
    }

    @Override
    public String getInfo() {
        return output;
    }

    @Override
    public List<String> getPhotoUrls(){
        return null;
    }

    @Override
    public void reject() {
        // Nothing
    }
}
