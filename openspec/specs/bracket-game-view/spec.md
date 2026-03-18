## Purpose

Define the visual display of an individual padel game as a bracket card showing players and the winning pair.

## Requirements

### Requirement: Bracket Game Card Layout

The app SHALL display each game in a bracket-style card with players positioned in four quadrants.

#### Scenario: Player quadrant positions

- **WHEN** a bracket game card is rendered
- **THEN** Team A Player 1 (pair1Player1) is shown in the top-left quadrant
- **AND** Team A Player 2 (pair1Player2) is shown in the bottom-left quadrant
- **AND** Team B Player 1 (pair2Player1) is shown in the top-right quadrant
- **AND** Team B Player 2 (pair2Player2) is shown in the bottom-right quadrant

#### Scenario: Game number shown as compact label

- **WHEN** a bracket game card is rendered
- **THEN** the game number is shown as a compact numeric label (e.g. "1", "2", "3") in the centre divider between the two teams
- **AND** the label does NOT include the word "Partido" or any prefix

#### Scenario: No score displayed

- **WHEN** a bracket game card is rendered
- **THEN** no score is shown — only the game number in the centre divider

### Requirement: Winner Highlight in Bracket

The bracket game card SHALL visually distinguish the winning team from the losing team.

#### Scenario: Winning team highlighted

- **WHEN** a game has a recorded winner (winningPair = 1 or 2)
- **THEN** the winning team's side of the bracket is shown with the primary color background and bold player names
- **AND** a 🏆 icon is shown pinned to the **outer edge** of the winning team's panel (far left if right panel wins, far right if left panel wins)
- **AND** the losing team's side is shown with a muted/dimmed appearance

#### Scenario: No winner

- **WHEN** a game has no recorded winner (winningPair = null)
- **THEN** both sides are displayed with equal visual weight and no trophy icon
