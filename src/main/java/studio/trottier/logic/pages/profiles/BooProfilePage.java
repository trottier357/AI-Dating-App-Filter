package studio.trottier.logic.pages.profiles;

import ai.djl.util.Pair;
import studio.trottier.logic.pages.base.ProfilePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class BooProfilePage extends ProfilePage {

    public BooProfilePage(WebDriver driver) {
        super(driver);
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getName() {
        return readFrontTextById("profileFirstName");
    }

    @Override
    public String getUrl() {
        return "";
    }

    @Override
    public String getBio() {
        return readFrontTextByCss(
                "[id^=\"profileColumn-\"]",
                "./div[3]/div/div[1]/div[2]/p"
        );
    }

    @Override
    public List<String> getTags() {
        List<String> raw = readFrontChildTextsByCss(
                "[id^=\"profileColumn-\"]",
                "./div[3]/div/div[2]/div[1]"
        );

        List<String> result = new ArrayList<>();
        for(String tag : raw){
            if(tag.startsWith("#")){
                result.add(tag.substring(1));
            }
        }

        return result;
    }

    @Override
    public List<Pair<String, String>> getPrompts() {
        List<Pair<String, String>> raw = readFrontPromptPairs("[id^=\"profileColumn-\"]");

        List<Pair<String, String>> result = new ArrayList<>();
        for(Pair<String, String> pair : raw){
            if(pair.getKey().equals("Languages")){
                continue;
            }
            result.add(pair);
        }
        return preventDuplication(result);
    }

    @Override
    public List<String> getPhotoUrls(){
        List<String> result = new ArrayList<>();

        WebElement front = findFrontMost(By.cssSelector("[id^=\"profileColumn-\"]"));
        if(front != null){
            try{
                WebElement img = front.findElement(By.xpath("./div[1]/div/img"));
                String src = img.getAttribute("src");
                if(src != null && !src.isBlank()){
                    result.add(src);
                }
            }catch(Exception ignored){
                // Front card didn't expose the expected hero <img>; skip rather than crash.
            }
        }

        return result;
    }

    @Override
    public void reject() {
        String previousCardId = frontIdByCss("[id^=\"profileColumn-\"]");
        clickFrontById("passButton");
        waitForFrontCardChange("[id^=\"profileColumn-\"]", previousCardId);
    }
}
