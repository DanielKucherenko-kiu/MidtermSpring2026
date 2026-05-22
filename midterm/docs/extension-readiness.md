# Extension Readiness Note

## Best-supported extension

The design now best supports adding a new card effect or rule variant.

For example, adding a custom action card would be easier now because card parsing and legal-play checks are centralized in `CardRules`, while turn effects are grouped in `Main.applyPlayedCardEffect`.

## Where the change would be implemented

A new card effect would mainly require changes in these places:

1. `CardRules.rank`
   - Add recognition for the new card code.

2. `CardRules.points`
   - Add the new card's score value.

3. `CardRules.isLegal`
   - If the new card has special legal-play behavior, add it here.

4. `Main.applyPlayedCardEffect`
   - Add the turn effect after the card is played.

5. `BotStrategy`
   - Decide where the bot should prioritize the new card.

6. `Main.selfTest`
   - Add characterization checks for the new behavior.

## Why the current design helps

Before refactoring, rule checks were duplicated in multiple places. A change to card legality could easily be made in one location but forgotten in another.

Now, legal-play logic goes through `CardRules.isLegal`, so the same rule is used by both the main turn flow and the bot strategy. Scoring is also isolated in `ScoreCalculator`, and bot decisions are isolated in `BotStrategy`.

This makes a rule extension more plausible because related responsibilities are easier to find.

## What still makes change difficult

The biggest remaining difficulty is that game state is still stored in static fields inside `Main`. Because of that, some features would still be awkward, especially:

- replay logs
- saving and loading games
- replacing the CLI with another interface
- testing a full turn without preparing global state

A future refactor should introduce a `GameState` object containing deck, discard pile, hands, current player, direction, top card, and called color. After that, a separate turn engine could apply commands to `GameState` without depending on the console.
