import java.util.ArrayList;

/**
 * Keeps the current bot behavior in one place.
 *
 * The bot still prefers draw two, then skip, then number cards, then wilds.
 */
public final class BotStrategy {
    private BotStrategy() {
    }

    static int chooseCard(ArrayList<String> hand, String upCard, String calledColor) {
        int drawTwo = firstLegalCardWithRank(hand, upCard, calledColor, "DRAW_TWO");
        if (drawTwo >= 0) {
            return drawTwo;
        }

        int skip = firstLegalCardWithRank(hand, upCard, calledColor, "SKIP");
        if (skip >= 0) {
            return skip;
        }

        int number = firstLegalCardWithRank(hand, upCard, calledColor, "NUMBER");
        if (number >= 0) {
            return number;
        }

        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).startsWith("W")) {
                return i;
            }
        }
        return -1;
    }

    private static int firstLegalCardWithRank(ArrayList<String> hand, String upCard, String calledColor, String rank) {
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (CardRules.rank(card).equals(rank) && CardRules.isLegal(card, upCard, calledColor)) {
                return i;
            }
        }
        return -1;
    }

    static String chooseColor(ArrayList<String> hand) {
        int r = 0;
        int y = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < hand.size(); i++) {
            String c = CardRules.color(hand.get(i));
            if (c.equals("R")) {
                r++;
            } else if (c.equals("Y")) {
                y++;
            } else if (c.equals("G")) {
                g++;
            } else if (c.equals("B")) {
                b++;
            }
        }
        if (r >= y && r >= g && r >= b) {
            return "R";
        } else if (y >= r && y >= g && y >= b) {
            return "Y";
        } else if (g >= r && g >= y && g >= b) {
            return "G";
        } else {
            return "B";
        }
    }
}
