## Purpose

Define the flow for creating a new padel match session, including player selection and session initialisation.

## Requirements

### Requirement: Player Selection

The app SHALL allow the user to select between 4 and 7 players for a new match session.

#### Scenario: Opening new match screen

- **WHEN** the user taps the "Nueva partido" FAB
- **THEN** the new match screen opens showing the list of known players (Jugadores) as selectable chips
- **AND** each chip shows the player's name

#### Scenario: Today's match already exists

- **WHEN** the user opens the app and a session for today already exists
- **THEN** the "Nueva partido" FAB is disabled
- **AND** a tooltip or message explains that a match for today already exists

#### Scenario: Valid selection

- **WHEN** the user selects between 4 and 7 players
- **THEN** a "Crear partido" button becomes enabled

#### Scenario: Invalid selection

- **WHEN** the user selects fewer than 4 or more than 7 players
- **THEN** the "Continuar" button remains disabled
- **AND** an inline message explains the valid range

### Requirement: Template-Based Game Schedule Generation

The app SHALL auto-generate a randomised game rotation schedule based on the number of selected players using the matching Plantilla template.

#### Scenario: Schedule generated with random slot assignment

- **WHEN** the user confirms player selection with 4, 5, 6, or 7 players
- **THEN** the selected players are shuffled randomly before being assigned to template slots (A, B, C, D…)
- **AND** the game schedule is generated using the corresponding Plantilla for the shuffled player list
- **AND** re-running with the same players produces a different assignment each time (probabilistically)

#### Scenario: Template applied correctly

- **WHEN** N players are selected
- **THEN** the generated schedule matches the rotation pattern defined in the corresponding Plantilla N template
- **AND** each player appears in the correct number of games as per the template

### Requirement: Live Score Entry

The app SHALL allow the user to enter game scores during the session.

#### Scenario: Entering a score

- **WHEN** the user taps on a game row
- **THEN** an inline score entry field appears for Pair 1 and Pair 2 game points
- **AND** the user can enter integer scores for each pair

#### Scenario: Score saved

- **WHEN** the user confirms a score entry
- **THEN** the score is saved and the game row shows the recorded result
- **AND** the player win ratios are updated accordingly

### Requirement: Session Saved

The app SHALL persist the completed session to the local database when the user saves it.

#### Scenario: Saving session

- **WHEN** the user taps "Guardar partido"
- **THEN** the session (players, games, scores, date) is written to the Room database
- **AND** the user is returned to the Match History screen where the new session appears at the top

### Requirement: Session Saved Immediately

The app SHALL persist the new session to the local database as soon as the user confirms player selection, without requiring score entry.

#### Scenario: Saving session on confirm

- **WHEN** the user taps "Crear partido" with a valid player selection
- **THEN** the session (date = today, players, generated games) is immediately written to the Room database
- **AND** all `winningPair` values start as `null`
- **AND** the user is returned to the Match History screen where the new session appears at the top
