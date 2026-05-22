# Refactoring Report

## Behavior characterized before refactoring

I added characterization checks for the implemented behavior rather than ideal UNO rules. The checks cover:

- card parsing for color, rank, number, and points
- legal play by matching color
- legal play by matching number
- legal play by matching action type, such as skip on skip
- wild and wild draw four cards always being legal
- called color after a wild card
- illegal mismatches being rejected
- current bot card priority: draw two, then skip, then number card, then wild
- bot color choice based on the most common color in its hand
- human command parsing allowing `draw` even when the human has a legal playable card
- skip, reverse, draw two, and wild draw four turn effects
- drawing from the deck
- the current edge case where drawing from an empty deck and empty discard pile returns `W`
- scoring from the non-winning players' remaining hands

These tests are characterization tests: they describe what the current program does, including quirks, so that refactoring does not accidentally change behavior.

## Worst design problems found

The original design had several risky areas:

- `Main.java` contained almost every responsibility: setup, turn flow, rule checking, bot decisions, scoring, command parsing, and console output.
- Legal-play logic was duplicated in the main turn loop and bot selection logic.
- Cards were represented as primitive strings, so parsing and rule checks were repeated through string operations.
- Console input was mixed directly into the game flow, which made rule behavior harder to test without running the CLI.
- Scoring was embedded directly inside the win branch instead of being isolated.
- Card effects were buried inside the main loop, making skip/reverse/draw effects hard to test directly.

## Refactorings performed

I refactored incrementally while keeping the existing scripts working.

1. Extracted `CardRules`
   - Moved card color, rank, number, legal-play, and point-value behavior into one class.
   - `Main` still exposes wrapper methods for compatibility with the existing self-test style.
   - This reduced duplicated legal-play logic.

2. Extracted `BotStrategy`
   - Moved bot card selection and bot color selection out of `Main`.
   - Preserved the existing priority order: draw two, skip, number, then wild.

3. Extracted `ConsolePrompter`
   - Moved human card and color prompts into a separate class.
   - This separates at least part of the CLI input/parsing behavior from the game flow.

4. Extracted `ScoreCalculator`
   - Moved score calculation for a winning hand out of the main turn loop.

5. Extracted `applyPlayedCardEffect`
   - Moved skip, reverse, draw two, wild draw four, and normal next-turn behavior into a named method.
   - This makes those rule effects testable without running a full CLI game.

6. Expanded characterization checks
   - The existing `--self-test` mode now checks more game behavior and protects the refactoring.

## Behavior intentionally preserved

The refactoring intentionally preserves these current behaviors:

- Cards still use the same string format, such as `R5`, `YS`, `G+2`, `W`, and `W4`.
- Wild and wild draw four are legal on any top card.
- A called color after a wild allows matching by that color.
- Human players may type `draw` even if they already hold a legal playable card.
- Bots automatically choose the first legal card based on the existing priority order.
- Draw two and wild draw four both make the next player draw cards and lose their turn.
- In a two-player game, skip returns the turn to the same player.
- Drawing from an empty deck with no discard pile still returns `W`.
- The CLI commands and scripts remain the same.

## Risks that remain

Some risks remain because this was an incremental refactor, not a rewrite:

- `Main` still owns a lot of global mutable state.
- Cards are still primitive strings instead of a true `Card` value object.
- The full turn loop still mixes some output messages with state changes.
- There are no JUnit tests; the project still uses the provided `--self-test` style.
- The game state is not yet represented by a dedicated `GameState` object.
- Some behavior, especially full interactive human play, still needs manual testing.

The project is safer than before because important rules are now centralized and characterized, but deeper design cleanup would require another incremental pass.
