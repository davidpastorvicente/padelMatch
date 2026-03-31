[![Release APK](https://github.com/davidpastorvicente/padelMatch/actions/workflows/release-apk.yml/badge.svg)](https://github.com/davidpastorvicente/padelMatch/actions/workflows/release-apk.yml)

# PadelMatch

An Android app for tracking padel match sessions with your regular group. Record sets, track player statistics, and visualise win-ratio trends over time.

## Features

- **Match history** — browse all past sessions with an inline calendar filter
- **Session detail** — see every set played, the bracket result, and the classification chart
- **Player statistics** — per-player cards with aggregate stats (partidos, sets, wins, ratio) and a time-proportional win-ratio sparkline
- **Combined win-ratio chart** — full-screen landscape chart overlaying all players' win-ratio trends; tap any point for a date + ratio tooltip
- **Player detail** — individual breakdown with a full chart (tap points for tooltips) and clickable session history
- **New match wizard** — pick a date, select players (with duplicate-date warning), and save
- **Import / Export** — JSON backup and restore via the overflow menu

## Tech Stack

| Layer         | Technology                   |
|---------------|------------------------------|
| Language      | Kotlin 2.0                   |
| UI            | Jetpack Compose + Material 3 |
| Architecture  | MVVM + Repository            |
| DI            | Hilt                         |
| Database      | Room                         |
| Navigation    | Compose Navigation           |
| Async         | Kotlin Coroutines + Flow     |
| Serialization | kotlinx.serialization        |

## Requirements

- Android **8.0+** (API 26)
- Target SDK **35**
- JDK **21** (JetBrains runtime — see setup instructions)

## Project Structure

```
app/src/main/java/com/padelgroup/padelMatch/
├── data/
│   ├── db/          # Room database, DAOs, entities
│   ├── model/       # Domain models
│   ├── repository/  # Data access layer
│   ├── importer/    # JSON import logic
│   └── exporter/    # JSON export logic
├── di/              # Hilt modules
└── ui/
    ├── history/     # Match history screen + inline calendar
    ├── navigation/  # Bottom nav, app navigation graph
    ├── newmatch/    # New match creation wizard
    ├── results/     # Edit set results
    ├── session/     # Session detail screen
    ├── statistics/  # Player stats + player detail screens
    └── theme/       # Colours, typography, player colour palette
```

## Getting Started

Clone the repo and open in Android Studio. See [`.github/copilot-instructions.md`](.github/copilot-instructions.md) for architecture details and build instructions.

## CI

Every push to `master` triggers a GitHub Actions workflow that builds and uploads a debug APK as a workflow artifact (retained for 7 days).
