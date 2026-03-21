## Purpose

Define the combined win-ratio chart screen that overlays all players' win-ratio trend lines on a single shared canvas for direct comparison.

## Requirements

### Requirement: Combined Win-Ratio Chart Screen

The app SHALL provide a full-screen combined chart showing every player's win-ratio history on a single canvas with a shared time axis.

#### Scenario: Chart opened from Statistics tab

- **WHEN** the user taps the chart icon button in the Statistics tab top app bar
- **THEN** the app SHALL navigate to the combined chart screen

#### Scenario: Layout orientation

- **WHEN** the combined chart screen is displayed
- **THEN** the entire content (chart + legend) SHALL be rotated 90° via `RotatedLayout` so the chart fills the full portrait screen height in landscape orientation
- **AND** the app orientation lock SHALL remain portrait

#### Scenario: All player lines rendered

- **WHEN** the combined chart screen is displayed and at least one player has session history
- **THEN** a Canvas chart SHALL be drawn with:
  - One polyline per player using `lerp(bg, onColor, 0.45f)` as the line colour (vivid badge-derived mid-tone)
  - Dots at each data point (radius 7) with white centre dots (radius 3)
  - Shared time-proportional X axis (global min date on the left, global max date on the right)
  - Y axis from 0% to 100% win ratio with grid lines and labels at 0, 25, 50, 75, 100%
  - Dashed grid line at 50%

#### Scenario: Tap to show tooltip

- **WHEN** the user taps within 60px of a data point
- **THEN** a tooltip SHALL appear near the tapped point with:
  - Player name in bold, coloured with the player's line colour
  - Date formatted as `d MMM yyyy` in Spanish locale
  - Win ratio as a rounded integer percentage (bold)
  - Dark semi-transparent rounded-rect background sized to fit the text (measured with `Paint.measureText`)
  - Equal padding on all sides using `Paint.fontMetrics` ascent/descent for correct vertical alignment
  - Tooltip positioned above the point when the point is in the lower half of the chart, below otherwise
- **WHEN** the user taps away from all data points
- **THEN** the tooltip SHALL be dismissed

#### Scenario: Player legend displayed

- **WHEN** the combined chart screen is displayed
- **THEN** a horizontal legend SHALL appear below the chart (left side in portrait) listing each player as a colour dot + name chip, sorted alphabetically

#### Scenario: No data

- **WHEN** no sessions exist
- **THEN** an empty-state message is shown instead of the chart
