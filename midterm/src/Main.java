import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static ArrayList<String> playerNames = new ArrayList<String>();
    static ArrayList<Boolean> humanPlayers = new ArrayList<Boolean>();
    static ArrayList<ArrayList<String>> hands = new ArrayList<ArrayList<String>>();
    static ArrayList<String> deck = new ArrayList<String>();
    static ArrayList<String> discard = new ArrayList<String>();
    static int[] scores = new int[10];
    static int currentPlayer = 0;
    static int direction = 1;
    static String upCard = "";
    static String calledColor = "";
    static boolean quiet = false;
    static Random random = new Random();
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int bots = 3;
        int games = 1;
        boolean human = false;
        long seed = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--bots") && i + 1 < args.length) {
                bots = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--games") && i + 1 < args.length) {
                games = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--human")) {
                human = true;
            } else if (args[i].equals("--quiet")) {
                quiet = true;
            } else if (args[i].equals("--seed") && i + 1 < args.length) {
                seed = Long.parseLong(args[++i]);
            } else if (args[i].equals("--self-test")) {
                selfTest();
                return;
            } else if (args[i].equals("--help")) {
                System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                return;
            }
        }

        random = new Random(seed);
        setupPlayers(bots, human);

        if (playerNames.size() < 2 || playerNames.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        for (int g = 1; g <= games; g++) {
            if (!quiet) {
                System.out.println("\n=== Game " + g + " ===");
            }
            playGame();
        }

        System.out.println("\nFinal scores:");
        for (int i = 0; i < playerNames.size(); i++) {
            System.out.println(playerNames.get(i) + ": " + scores[i]);
        }
    }

    static void setupPlayers(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();
        if (human) {
            playerNames.add("You");
            humanPlayers.add(Boolean.TRUE);
            hands.add(new ArrayList<String>());
        }
        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(Boolean.FALSE);
            hands.add(new ArrayList<String>());
        }
    }

    static void playGame() {
        deck.clear();
        String[] colors = {"R", "Y", "G", "B"};
        for (int c = 0; c < colors.length; c++) {
            deck.add(colors[c] + "0");
            for (int n = 1; n <= 9; n++) {
                deck.add(colors[c] + n);
                deck.add(colors[c] + n);
            }
            deck.add(colors[c] + "S");
            deck.add(colors[c] + "S");
            deck.add(colors[c] + "R");
            deck.add(colors[c] + "R");
            deck.add(colors[c] + "+2");
            deck.add(colors[c] + "+2");
        }
        for (int i = 0; i < 4; i++) {
            deck.add("W");
            deck.add("W4");
        }
        Collections.shuffle(deck, random);
        discard.clear();
        for (int i = 0; i < hands.size(); i++) {
            hands.get(i).clear();
        }
        for (int i = 0; i < playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) {
                hands.get(i).add(draw());
            }
        }
        upCard = draw();
        while (upCard.startsWith("W")) {
            discard.add(upCard);
            upCard = draw();
        }
        calledColor = "";
        direction = 1;
        currentPlayer = random.nextInt(playerNames.size());

        int guard = 0;
        while (guard < 3000) {
            guard++;
            String name = playerNames.get(currentPlayer);
            ArrayList<String> hand = hands.get(currentPlayer);

            if (!quiet) {
                System.out.println("\nUp card: " + upCard + (calledColor.equals("") ? "" : " called " + calledColor));
                System.out.println(name + " hand: " + join(hand));
            }

            int chosen = -1;
            if (humanPlayers.get(currentPlayer).booleanValue()) {
                chosen = askHuman(hand);
            } else {
                chosen = chooseBotCard(hand);
            }

            if (chosen == -1) {
                String drawn = draw();
                hand.add(drawn);
                if (!quiet) {
                    System.out.println(name + " draws " + drawn);
                }
                if (isLegal(drawn, upCard, calledColor)) {
                    if (!humanPlayers.get(currentPlayer).booleanValue()) {
                        chosen = hand.size() - 1;
                    } else {
                        System.out.print("Play drawn card " + drawn + "? y/n: ");
                        String answer = scanner.nextLine();
                        if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
                            chosen = hand.size() - 1;
                        }
                    }
                }
            }

            if (chosen >= 0) {
                if (chosen >= hand.size()) {
                    if (!quiet) {
                        System.out.println(name + " selected an invalid index and draws a penalty card.");
                    }
                    hand.add(draw());
                    next();
                    continue;
                }

                String card = hand.get(chosen);
                boolean ok = isLegal(card, upCard, calledColor);

                if (!ok) {
                    if (!quiet) {
                        System.out.println(name + " tried illegal card " + card + " and draws a penalty card.");
                    }
                    hand.add(draw());
                    next();
                    continue;
                }

                hand.remove(chosen);
                discard.add(upCard);
                upCard = card;
                calledColor = "";
                if (!quiet) {
                    System.out.println(name + " plays " + card);
                }

                if (card.equals("W") || card.equals("W4")) {
                    if (humanPlayers.get(currentPlayer).booleanValue()) {
                        calledColor = askColor();
                    } else {
                        calledColor = chooseBotColor(hand);
                    }
                    if (!quiet) {
                        System.out.println(name + " calls " + calledColor);
                    }
                }

                if (hand.size() == 1 && !quiet) {
                    System.out.println(name + " says UNO!");
                }

                if (hand.size() == 0) {
                    int points = ScoreCalculator.scoreOtherHands(hands, currentPlayer);
                    scores[currentPlayer] += points;
                    if (!quiet) {
                        System.out.println(name + " wins and scores " + points);
                    }
                    return;
                }

                applyPlayedCardEffect(card);
            } else {
                next();
            }
        }
        if (!quiet) {
            System.out.println("Game stopped at safety limit.");
        }
    }

    static void applyPlayedCardEffect(String card) {
        if (rank(card).equals("SKIP")) {
            next();
            next();
        } else if (rank(card).equals("REVERSE")) {
            direction = direction * -1;
            if (playerNames.size() == 2) {
                next();
                next();
            } else {
                next();
            }
        } else if (rank(card).equals("DRAW_TWO")) {
            next();
            hands.get(currentPlayer).add(draw());
            hands.get(currentPlayer).add(draw());
            if (!quiet) {
                System.out.println(playerNames.get(currentPlayer) + " draws two.");
            }
            next();
        } else if (rank(card).equals("WILD_DRAW_FOUR")) {
            next();
            for (int i = 0; i < 4; i++) {
                hands.get(currentPlayer).add(draw());
            }
            if (!quiet) {
                System.out.println(playerNames.get(currentPlayer) + " draws four.");
            }
            next();
        } else {
            next();
        }
    }

    static String draw() {
        if (deck.size() == 0) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.size() == 0) {
            return "W";
        }
        return deck.remove(0);
    }

    static int chooseBotCard(ArrayList<String> hand) {
        return BotStrategy.chooseCard(hand, upCard, calledColor);
    }

    static int askHuman(ArrayList<String> hand) {
        return ConsolePrompter.askHumanCard(scanner, hand, upCard, calledColor);
    }

    static String askColor() {
        return ConsolePrompter.askColor(scanner);
    }

    static String chooseBotColor(ArrayList<String> hand) {
        return BotStrategy.chooseColor(hand);
    }

    static boolean isLegal(String card, String up, String call) {
        return CardRules.isLegal(card, up, call);
    }

    static String color(String card) {
        return CardRules.color(card);
    }

    static String rank(String card) {
        return CardRules.rank(card);
    }

    static int number(String card) {
        return CardRules.number(card);
    }

    static int points(String card) {
        return CardRules.points(card);
    }

    static void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) {
            currentPlayer = 0;
        }
        if (currentPlayer < 0) {
            currentPlayer = playerNames.size() - 1;
        }
    }

    static String join(ArrayList<String> cards) {
        String out = "";
        for (int i = 0; i < cards.size(); i++) {
            out += i + ":" + cards.get(i);
            if (i < cards.size() - 1) {
                out += " ";
            }
        }
        return out;
    }

    static void selfTest() {
        int passed = 0;

        // Card parsing and score characterization.
        if (color("R5").equals("R")) passed++; else fail("color R5");
        if (rank("G+2").equals("DRAW_TWO")) passed++; else fail("rank +2");
        if (points("W4") == 50) passed++; else fail("wild draw four points");
        if (points("YS") == 20) passed++; else fail("action points");
        if (points("B7") == 7) passed++; else fail("number points");

        // Legal-play characterization.
        if (isLegal("R2", "R9", "")) passed++; else fail("matching by color");
        if (isLegal("G9", "R9", "")) passed++; else fail("matching by number");
        if (isLegal("BS", "RS", "")) passed++; else fail("matching by action type");
        if (isLegal("W", "R9", "")) passed++; else fail("wild is legal");
        if (isLegal("W4", "R9", "")) passed++; else fail("wild draw four is legal");
        if (isLegal("B3", "W", "B")) passed++; else fail("called color after wild");
        if (!isLegal("B3", "R9", "")) passed++; else fail("illegal mismatch");

        // Bot strategy characterization: current bot prefers draw two, then skip,
        // then number cards, and only then wild cards.
        ArrayList<String> h = new ArrayList<String>();
        h.add("B3");
        h.add("R4");
        h.add("W");
        upCard = "R9";
        calledColor = "";
        if (chooseBotCard(h) == 1) passed++; else fail("bot normal before wild");

        ArrayList<String> h3 = new ArrayList<String>();
        h3.add("R7");
        h3.add("YS");
        h3.add("G+2");
        upCard = "B+2";
        calledColor = "";
        if (chooseBotCard(h3) == 2) passed++; else fail("bot prefers draw two");

        ArrayList<String> h4 = new ArrayList<String>();
        h4.add("R7");
        h4.add("YS");
        h4.add("W");
        upCard = "BS";
        calledColor = "";
        if (chooseBotCard(h4) == 1) passed++; else fail("bot prefers skip before wild");

        ArrayList<String> h2 = new ArrayList<String>();
        h2.add("B1");
        h2.add("B2");
        h2.add("R3");
        if (chooseBotColor(h2).equals("B")) passed++; else fail("bot color");

        // Console parsing characterization: a human may type draw even while holding a legal card.
        scanner = new Scanner("draw\n");
        upCard = "R9";
        calledColor = "";
        ArrayList<String> humanHand = new ArrayList<String>();
        humanHand.add("R2");
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(new java.io.ByteArrayOutputStream()));
        int humanChoice = askHuman(humanHand);
        System.setOut(originalOut);
        if (humanChoice == -1) passed++; else fail("human can draw despite legal card");

        // Turn-effect characterization without running the full CLI game.
        setupPlayers(2, false);
        quiet = true;
        currentPlayer = 0;
        direction = 1;
        applyPlayedCardEffect("RS");
        if (currentPlayer == 0) passed++; else fail("skip skips next player in two-player game");

        setupPlayers(3, false);
        quiet = true;
        currentPlayer = 0;
        direction = 1;
        applyPlayedCardEffect("BR");
        if (direction == -1 && currentPlayer == 2) passed++; else fail("reverse changes direction");

        setupPlayers(3, false);
        quiet = true;
        currentPlayer = 0;
        direction = 1;
        deck.clear();
        deck.add("R1");
        deck.add("R2");
        applyPlayedCardEffect("G+2");
        if (currentPlayer == 2 && hands.get(1).size() == 2) passed++; else fail("draw two gives two and skips");

        setupPlayers(3, false);
        quiet = true;
        currentPlayer = 0;
        direction = 1;
        deck.clear();
        deck.add("R1");
        deck.add("R2");
        deck.add("R3");
        deck.add("R4");
        applyPlayedCardEffect("W4");
        if (currentPlayer == 2 && hands.get(1).size() == 4) passed++; else fail("wild draw four gives four and skips");

        // Drawing characterization.
        deck.clear();
        discard.clear();
        deck.add("G5");
        if (draw().equals("G5")) passed++; else fail("draw removes top deck card");
        if (draw().equals("W")) passed++; else fail("empty deck surprising fallback wild");

        // Scoring characterization.
        setupPlayers(3, false);
        hands.get(0).clear();
        hands.get(1).add("R5");
        hands.get(1).add("YS");
        hands.get(2).add("W4");
        if (ScoreCalculator.scoreOtherHands(hands, 0) == 75) passed++; else fail("score other hands");

        System.out.println("Passed " + passed + " characterization checks.");
    }

    static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}
