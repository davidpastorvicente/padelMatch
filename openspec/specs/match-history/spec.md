## Purpose

Define the home screen that lists all past padel sessions with expandable cards for scores, classification, and actions.
## Requirements
### Requirement: Session List

The app SHALL display all padel sessions in reverse-chronological order on the home screen.

#### Scenario: Sessions loaded from database

- **WHEN** the user opens the app
- **THEN** the **Historial** tab shows a list of session cards, each displaying the date and attending player names
- **AND** sessions are ordered from most recent to oldest

#### Scenario: Session card expanded

- **WHEN** the user taps a session card
- **THEN** the card expands to show the full list of games as bracket cards, player win ratios, and action buttons
- **AND** each game is displayed as a `BracketGameCard` with players in quadrant positions, game number centred, and winning team highlighted

#### Scenario: Bottom navigation visible (MODIFIED)

- **WHEN** the user is on any of the three home tabs (Historial, Calendario, Estadísticas)
- **THEN** a `NavigationBar` is visible at the bottom of the screen
- **AND** the active tab is highlighted

### Requirement: Game Winner Highlight

The app SHALL visually distinguish the winning pair in each game row within an expanded session.

#### Scenario: Winning pair displayed

- **WHEN** a session card is expanded and a game has a recorded winner
- **THEN** the winning pair's player names are displayed in bold with a trophy icon (🏆) prefix
- **AND** the losing pair is displayed in normal weight

#### Scenario: No winner recorded

- **WHEN** a session card is expanded and a game has no winner recorded (winningPair is null)
- **THEN** both pairs are displayed in normal weight with no highlight
- **AND** no trophy icon is shown

### Requirement: Player Win Ratios Per Session

The app SHALL display each player's win ratio (Puntos) for a session when that session is expanded.

#### Scenario: Classification displayed

- **WHEN** a session card is expanded
- **THEN** the "Clasificación" section shows a `ClassificationChart` composable instead of a text list
- **AND** players are sorted by win ratio descending

### Requirement: Empty State

The app SHALL show a friendly empty state when no sessions exist.

#### Scenario: No sessions in database

- **WHEN** the database contains no sessions
- **THEN** the home screen shows an illustration and the message "No hay partidas todavía"
- **AND** a call-to-action button to create the first match is displayed

### Requirement: Session Action Buttons

The session card action buttons SHALL use concise labels.

#### Scenario: Edit button label (MODIFIED)

- **WHEN** a session card is expanded
- **THEN** the edit action button is labelled "Editar" (not "Editar resultados")

