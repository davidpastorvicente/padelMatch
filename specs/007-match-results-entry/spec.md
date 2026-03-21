## Purpose

Define the screen for editing game results within a session, including adding, editing, and deleting games.
## Requirements
### Requirement: Results Entry Screen

The edit screen SHALL support full CRUD for individual games within a session.

#### Scenario: Opening results screen (MODIFIED)

- **WHEN** the user taps "Editar" on a session card
- **THEN** the Edit screen opens for that session
- **AND** all games are displayed as bracket cards with winner selection, delete icon, and edit icon per game
- **AND** a FAB ("+" icon) is visible to add a new game

#### Scenario: Saving results (MODIFIED)

- **WHEN** the user taps "Guardar"
- **THEN** added games are inserted, deleted games are removed, and edited player assignments are updated in the database
- **AND** win ratios are recalculated across the final game list
- **AND** the user is navigated back to Match History

