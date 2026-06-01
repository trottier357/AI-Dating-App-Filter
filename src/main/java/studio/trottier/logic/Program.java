package studio.trottier.logic;

import studio.trottier.logic.base.ProgramListener;
import studio.trottier.logic.pages.base.ProfilePage;
import studio.trottier.logic.ai.Assessor;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.Scanner;

public class Program {

    private final Class<? extends ProfilePage> profileClass;
    private final Assessor assessor;
    private final WebDriver driver;

    private volatile boolean running = false;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();
    private List<ProgramListener> listeners;

    public <T extends ProfilePage> Program(WebDriver driver, Class<T> profileClass, Assessor assessor) {
        this(driver, profileClass, assessor, List.of());
    }

    public <T extends ProfilePage> Program(WebDriver driver, Class<T> profileClass, Assessor assessor, List<ProgramListener> listeners) {
        this.profileClass = profileClass;
        this.assessor = assessor;
        this.driver = driver;
        this.listeners = listeners;
    }

    public void run(){
        Scanner scanner = new Scanner(System.in);
        running = true;
        paused = false;

        while(running){

            // Block here while paused.
            synchronized (pauseLock) {
                while (paused && running) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        running = false;
                        break;
                    }
                }
            }
            if (!running) break;

            ProfilePage profile = newProfile(profileClass);
            System.out.println("\nNew Profile: " + profile.getName());
            System.out.println("\nInformation:\n" + profile.getInfo());
            boolean isGoodMatch = assessor.check(profile);

            if(isGoodMatch){
                System.out.print("Good match!\n[r] reject\n[q] quit\nChoice: ");
                String input = scanner.next();

                if(input.equals("r")){
                    profile.reject();
                }

                if(input.equals("q")){
                    break;
                }
            } else {
                System.out.println("Bad match.");
                //profile.reject();
            }
        }

        System.out.println("\nEXITED\n");

        scanner.close();
    }

    public void pause(){
        synchronized (pauseLock) {
            paused = true;
        }
    }

    public void resume(){
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    public void stop(){
        synchronized (pauseLock) {
            running = false;
            paused = false;
            pauseLock.notifyAll();
        }
    }

    public boolean isPaused(){
        return paused;
    }

    public boolean isRunning(){
        return running;
    }

    private <T extends ProfilePage> T newProfile(Class<T> profileClass) {
        try{
            T profile = profileClass.getConstructor(WebDriver.class).newInstance(driver);
            for(ProgramListener listener : listeners){
                listener.onNewProfile(profile);
            }
            return profile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
