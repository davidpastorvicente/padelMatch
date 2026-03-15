# game-management Specification

## Purpose

Define the in-session game management features: adding, editing, and deleting individual games within an existing session.
## Requirements
### Requirement: Add Game

The Edit screen SHALL allow the user to add a new game to a session.

#### Scenario: Opening add game picker

- **WHEN** the user taps the FAB ("+" button) on the Edit screen
- **THEN** a bottom sheet opens showing the session's players as selectable chips
- **AND** the user must select exactly 4 players to form two pairs

#### Scenario: Assigning pairs in the picker

- **WHEN** 4 players are selected in the picker
- **THEN** the first two selected are assigned to Pair 1 and the last two to Pair 2
- **AND** a preview of the bracket layout is shown before confirming

#### Scenario: Confirming a new game

- **WHEN** the user taps "Añadir" in the picker
- **THEN** a new game is appended to the local game list with the next sequential game number
- **AND** `winningPair` is null
- **AND** the bottom sheet closes

#### Scenario: Cancelling add game

- **WHEN** the user dismisses the bottom sheet without confirming
- **THEN** no game is added

### Requirement: Delete Game

The Edit screen SHALL allow the user to delete an individual game.

#### Scenario: Deleting a game

- **WHEN** the user taps the trash icon on a game's bracket card
- **THEN** a confirmation dialog appears ("¿Eliminar este partido?")

#### Scenario: Delete confirmed

- **WHEN** the user confirms the deletion
- **THEN** the game is removed from the local game list immediately
- **AND** game numbers are NOT renumbered

#### Scenario: Delete cancelled

- **WHEN** the user dismisses the confirmation
- **THEN** the game remains in the list unchanged

### Requirement: Edit Game Players

The Edit screen SHALL allow the user to reassign the players of an existing game.

#### Scenario: Opening edit game picker

- **WHEN** the user taps the pencil icon on a game's bracket card
- **THEN** the same player picker bottom sheet opens pre-populated with the game's current 4 players

#### Scenario: Confirming edited players

- **WHEN** the user taps "Guardar" in the picker
- **THEN** the game's player assignments are updated in the local state
- **AND** the `winningPair` for that game is reset to null

