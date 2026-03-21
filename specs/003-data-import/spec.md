## Purpose

Define how historical session data is imported into the app on first launch from bundled assets.

## Requirements

### Requirement: First-Launch Data Import

The app SHALL import historical session data from the bundled matches.xlsx file on first launch.

#### Scenario: First launch import

- **WHEN** the user launches the app for the first time (empty database)
- **THEN** the app reads matches.xlsx from the assets folder
- **AND** parses all session sheets (sheets named as DDMMYY dates)
- **AND** imports players, games, win-ratio data, and winning pair per game into the Room database
- **AND** the import runs in the background without blocking the UI

#### Scenario: Import already done

- **WHEN** the user launches the app and the database already contains sessions
- **THEN** the app does NOT re-import from xlsx
- **AND** the app proceeds directly to the Match History screen

#### Scenario: Import failure

- **WHEN** the xlsx parsing fails for any reason
- **THEN** the app logs the error and shows a non-blocking snackbar message
- **AND** the app still opens with an empty database (no crash)

### Requirement: Game Winner Detection

The importer SHALL detect the winning pair for each game by reading cell background color.

#### Scenario: Pair 1 wins

- **WHEN** the xlsx cells for Pair 1 (columns E and F) in a game row have the green background color (RGB `FFD9EAD3`)
- **THEN** the imported game's `winningPair` SHALL be set to `1`

#### Scenario: Pair 2 wins

- **WHEN** the xlsx cells for Pair 2 (columns G and H) in a game row have the green background color (RGB `FFD9EAD3`)
- **THEN** the imported game's `winningPair` SHALL be set to `2`

#### Scenario: No winner

- **WHEN** no cells in the game row have the green background color
- **THEN** the imported game's `winningPair` SHALL be set to `null`

### Requirement: Player Registry Seeded from Import

The app SHALL extract the full player list from imported sessions and seed the player registry.

#### Scenario: Players extracted

- **WHEN** the xlsx import completes
- **THEN** all unique player names found across all session sheets are stored in the players table
- **AND** duplicate names are deduplicated (case-insensitive)

#### Scenario: Import already done

- **WHEN** the user launches the app and the database already contains sessions
- **THEN** the app does NOT re-import from xlsx
- **AND** the app proceeds directly to the Match History screen

#### Scenario: Import failure

- **WHEN** the xlsx parsing fails for any reason
- **THEN** the app logs the error and shows a non-blocking snackbar message
- **AND** the app still opens with an empty database (no crash)

### Requirement: Player Registry Seeded from Import

The app SHALL extract the full player list from imported sessions and seed the player registry.

#### Scenario: Players extracted

- **WHEN** the xlsx import completes
- **THEN** all unique player names found across all session sheets are stored in the players table
- **AND** duplicate names are deduplicated (case-insensitive)
