## Purpose

Define the player statistics screen showing per-player aggregate performance cards, and navigation to individual player detail screens.

## Requirements

### Requirement: Per-Player Aggregate Statistics

The statistics screen SHALL display one card per player, sorted by overall win ratio descending, showing aggregate performance across all sessions the player attended.

#### Scenario: Statistics loaded

- **WHEN** the user opens the Statistics tab
- **THEN** a list of player cards is shown, each containing:
  - Player name with their color badge
  - Number of partidos (sessions attended)
  - Number of sets (total games played across all sessions)
  - Total wins
  - Overall win ratio as a percentage

#### Scenario: No sessions recorded

- **WHEN** no sessions exist in the database
- **THEN** the statistics screen shows an empty-state message ("Sin datos todavía")

#### Scenario: Player card tapped

- **WHEN** the user taps a player card in the Statistics tab
- **THEN** the app SHALL navigate to `PlayerDetailScreen` for that player

### Requirement: Win-Ratio Trend Spark-Line

Each player card SHALL include a small trend chart showing the player's win ratio per session over their last 10 sessions.

#### Scenario: Trend displayed

- **WHEN** a player has attended at least 2 sessions
- **THEN** a compact polyline spark-line is drawn inside the card
- **AND** the horizontal axis represents sessions (oldest left → newest right, capped at last 10)
- **AND** the vertical axis represents win ratio (0.0 to 1.0)

#### Scenario: Single session player

- **WHEN** a player has attended only one session
- **THEN** no spark-line is shown (or a single-point indicator)
- **AND** the aggregate stats are still displayed normally

