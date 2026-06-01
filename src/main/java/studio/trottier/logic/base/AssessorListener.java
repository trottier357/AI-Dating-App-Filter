package studio.trottier.logic.base;

public interface AssessorListener {
    void onNewAssessment(boolean isMatch, float score, float outOf);
}
