import java.util.ArrayList;

/** Calculates scores using the current implementation's card values. */
public final class ScoreCalculator {
    private ScoreCalculator() {
    }

    static int scoreOtherHands(ArrayList<ArrayList<String>> hands, int winnerIndex) {
        int points = 0;
        for (int i = 0; i < hands.size(); i++) {
            if (i != winnerIndex) {
                for (int j = 0; j < hands.get(i).size(); j++) {
                    points += CardRules.points(hands.get(i).get(j));
                }
            }
        }
        return points;
    }
}
