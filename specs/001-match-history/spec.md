## Purpose

Define the home screen that lists all past padel sessions with expandable cards for scores, classification, and actions.
## Requirements
### Requirement: Session List

The app SHALL display all padel sessions in reverse-chronological order on the home screen.

#### Scenario: Sessions loaded from database

- **WHEN** the user opens the app
- **THEN** the Historial tab shows a list of compact session cards, each displaying only the date and attending player name badges
- **AND** sessions are ordered from most recent to oldest

#### Scenario: Session list filtered by date

- **WHEN** the user selects a date via the inline calendar panel
- **THEN** the session list SHALL show only sessions matching that date
- **AND** if no session exists for that date, the list SHALL show an empty state message "Sin partido ese día"

#### Scenario: Bottom navigation

- **WHEN** the user is on any home tab
- **THEN** the bottom NavigationBar SHALL show exactly two tabs: Historial and Estadísticas
- **AND** the Calendario tab SHALL NOT appear in the bottom navigation

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
- **THEN** the home screen shows an illustration and the message "No hay partidos todavía"
- **AND** a call-to-action button to create the first match is displayed

### Requirement: Session Action Buttons

The session card action buttons SHALL use concise labels.

#### Scenario: Edit button label

- **WHEN** a session card is expanded
- **THEN** the edit action button is labelled "Editar" (not "Editar resultados")

