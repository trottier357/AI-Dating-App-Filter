package studio.trottier.logic.ai;

import studio.trottier.logic.base.AssessorListener;
import studio.trottier.logic.pages.base.Assessible;
import java.util.List;

public class Assessor {
    private final float standard;
    private final Weigher goodWeigher;
    private final Weigher badWeigher;

    private List<AssessorListener> listeners;

    public Assessor(String model, String goodReference, String badReference, float standard){
        this(model, goodReference, badReference, standard, List.of());
    }

    public Assessor(String model, String goodReference, String badReference, float standard, List<AssessorListener> listeners){
        this.standard = standard;

        this.goodWeigher = new Weigher(model, goodReference);
        this.badWeigher = new Weigher(model, badReference);

        this.listeners = listeners;
    }

    public boolean check(Assessible profile){
        float rawValue = goodWeigher.score(profile.getInfo()) - badWeigher.score(profile.getInfo());
        float value = (rawValue + 1) / 2; // Normalize [-1, 1] to [0, 1]
        boolean isMatch = value >= standard;

        for(AssessorListener listener : listeners){
            listener.onNewAssessment(isMatch, value, standard);
        }

        return isMatch;
    }
}
