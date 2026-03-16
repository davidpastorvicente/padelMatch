## Purpose

Define the inline calendar filter panel in the Historial screen, allowing users to filter sessions by date without leaving the tab.

## Requirements

### Requirement: Inline Calendar Filter Panel

The Historial screen SHALL provide an inline calendar panel that slides down from the top bar to filter sessions by date.

#### Scenario: Opening the calendar panel

- **WHEN** the user taps the calendar icon in the TopAppBar
- **THEN** a calendar panel animates down below the TopAppBar
- **AND** the panel shows a month grid with session-day highlights
- **AND** the calendar icon appears active/highlighted

#### Scenario: Selecting a session date

- **WHEN** the user taps a day that has a recorded session
- **THEN** the history list below filters to show only that session
- **AND** the selected day is visually highlighted in the calendar

#### Scenario: Clearing the date filter

- **WHEN** the user taps the already-selected day again
- **THEN** the date filter is cleared and all sessions are shown again

#### Scenario: Closing the calendar panel

- **WHEN** the user taps the calendar icon again while the panel is open
- **THEN** the panel animates back up and collapses
- **AND** any active date filter is cleared

#### Scenario: Month navigation within panel

- **WHEN** the calendar panel is open
- **THEN** the user can tap left/right arrows to navigate between months
- **AND** session-day highlights update for the displayed month
