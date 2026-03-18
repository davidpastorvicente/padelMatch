# session-detail Specification

## Purpose
TBD - created by archiving change session-detail-screen. Update Purpose after archive.
## Requirements
### Requirement: Session Detail View

The app SHALL provide a dedicated screen for viewing the full detail of a single padel session.

#### Scenario: Navigating to detail

- **WHEN** the user taps a session card in the history list
- **THEN** the app navigates to `SessionDetailScreen` for that session
- **AND** the screen shows: the session date in the TopAppBar title, a row of player badges, the classification chart, and the full list of bracket game cards

#### Scenario: Edit action

- **WHEN** the user taps the edit icon in the TopAppBar of `SessionDetailScreen`
- **THEN** the app navigates to `EditResultsScreen` for that session

#### Scenario: Delete action

- **WHEN** the user taps the delete icon in the TopAppBar of `SessionDetailScreen`
- **THEN** a confirmation dialog is shown ("¿Eliminar este partido?")

#### Scenario: Delete confirmed

- **WHEN** the user confirms deletion
- **THEN** the session is deleted from the database
- **AND** the app navigates back to the history list

#### Scenario: Delete cancelled

- **WHEN** the user dismisses the confirmation dialog
- **THEN** no data is deleted and the detail screen remains

