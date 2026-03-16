## Purpose

Define the player detail statistics screen, providing a full breakdown of an individual player's performance history including aggregate metrics and a time-proportional win-ratio chart.

## Requirements

### Requirement: Player Detail Statistics Screen

The app SHALL provide a dedicated screen showing full statistics for a single player, accessible by tapping their card in the Statistics tab.

#### Scenario: Detail screen opened

- **WHEN** the user taps a player card in the Statistics tab
- **THEN** the app navigates to `PlayerDetailScreen` for that player
- **AND** the TopAppBar displays the player's name

#### Scenario: Aggregate stats displayed

- **WHEN** the player detail screen loads
- **THEN** the screen SHALL display in a 4-column table:
  - Partidos (sessions attended)
  - Sets (total games played)
  - Victorias (total set wins)
  - Ratio (overall win ratio as percentage)

#### Scenario: Per-session win-ratio chart displayed

- **WHEN** the player has attended at least 2 sessions
- **THEN** a full-size line chart is displayed with:
  - X-axis spacing proportional to actual time between sessions
  - Y-axis labels at 0%, 25%, 50%, 75%, 100%
  - Line and dots in a darkened version of the player's badge color
  - A dashed reference line at 50%

#### Scenario: Session history list displayed

- **WHEN** the player has attended at least 2 sessions
- **THEN** below the chart, each session is listed with:
  - Full date (e.g. "Lunes, 15 de marzo de 2026")
  - Win ratio badge colored on a red→orange→green gradient

#### Scenario: Single session player on detail screen

- **WHEN** the player has attended only one session
- **THEN** the chart and session list are not shown
- **AND** aggregate stats are displayed normally

#### Scenario: Back navigation from detail screen

- **WHEN** the user taps the back button in the TopAppBar
- **THEN** the app returns to the Statistics tab
