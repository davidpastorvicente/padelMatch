## Purpose

Define the visual classification chart shown inside a session card, displaying player win ratios as horizontal bars.

## Requirements

### Requirement: Session Classification Chart

The session card SHALL display player win ratios as a horizontal bar chart when the card is expanded.

#### Scenario: Chart rendered with results

- **WHEN** a session card is expanded and at least one game has a `winningPair` set
- **THEN** the "Clasificación" section shows a horizontal bar chart with one row per player
- **AND** each row shows the player's badge-colour bar scaled to their win ratio (0–100%)
- **AND** the player name appears to the left of the bar using their player badge colour
- **AND** the win ratio percentage label appears at the right end of the bar
- **AND** rows are sorted descending by win ratio (highest at top)

#### Scenario: Chart rendered with no results yet

- **WHEN** a session card is expanded and no games have a `winningPair` set
- **THEN** the "Clasificación" section shows all players with 0% bars
- **AND** no error or empty state is shown — the chart still renders

#### Scenario: Players with equal win ratio

- **WHEN** two or more players share the same win ratio
- **THEN** those players are sorted alphabetically among themselves
