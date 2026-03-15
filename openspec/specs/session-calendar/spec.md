# session-calendar Specification

## Purpose
TBD - created by archiving change calendar-and-statistics. Update Purpose after archive.
## Requirements
### Requirement: Monthly Calendar Grid

The calendar screen SHALL display a monthly grid of days, highlighting days on which a padel session took place.

#### Scenario: Calendar loaded

- **WHEN** the user opens the Calendar tab
- **THEN** the current month is displayed as a 7-column grid (Mon–Sun header row + day cells)
- **AND** days that have a recorded session are visually marked with the primary color dot/badge

#### Scenario: Empty day tapped

- **WHEN** the user taps a day with no session
- **THEN** no action occurs (or a subtle "no session" indicator is shown)

#### Scenario: Session day tapped

- **WHEN** the user taps a day that has a recorded session
- **THEN** a session detail card expands below the calendar showing the players and game results for that session

### Requirement: Month Navigation

The calendar screen SHALL allow the user to navigate between months.

#### Scenario: Navigate to previous month

- **WHEN** the user taps the previous-month arrow
- **THEN** the calendar updates to show the preceding month
- **AND** session highlights update accordingly

#### Scenario: Navigate to next month

- **WHEN** the user taps the next-month arrow
- **THEN** the calendar updates to show the following month
- **AND** future months with no sessions show an empty grid

#### Scenario: Month/year label

- **WHEN** any month is displayed
- **THEN** the month name and year (e.g., "Marzo 2026") are shown in the screen title or a header row above the grid

