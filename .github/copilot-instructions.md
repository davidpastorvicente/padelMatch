# PadelMatch — Copilot Instructions

## Project Overview

PadelMatch is an Android app for tracking padel match sessions. Users record sessions, each session contains multiple sets, and the app computes player statistics and win-ratio trends over time.

## Build Commands

```bash
# Debug APK
./gradlew assembleDebug

# Clean debug build
./gradlew clean assembleDebug

# Install with ADB
adb install app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 21 (JetBrains). Set `JAVA_HOME` to your JetBrains JDK 21 install when building from the CLI.

## Tech Stack

- **Language**: Kotlin 2.0
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Repository pattern
- **DI**: Hilt
- **Database**: Room
- **Navigation**: Compose Navigation with typed routes (`kotlinx.serialization`)
- **Async**: Kotlin Coroutines + StateFlow / SharedFlow
- **Serialization**: kotlinx.serialization
- **Min SDK**: 26, Target SDK: 35

## Project Structure

```text
app/src/main/java/com/davidpv/padelmatch/
├── data/
│   ├── db/           # Room database, DAOs, entities
│   ├── model/        # Domain data classes
│   ├── repository/   # Repository classes
│   ├── importer/     # JSON import
│   └── exporter/     # JSON export
├── di/               # Hilt database module
└── ui/
    ├── history/      # Match history, charts, and calendar
    ├── navigation/   # App navigation and bottom tabs
    ├── newmatch/     # New match flow
    ├── results/      # Edit results screens and sheets
    ├── session/      # Session detail screens
    ├── statistics/   # Player statistics and detailed charts
    └── theme/        # Color, typography, and player colors
```

## Architecture

### Layer Overview

- **UI** (`ui/`): Compose screens are driven by ViewModels exposing `StateFlow<UiState>` and `SharedFlow<Event>`.
- **Data** (`data/`): Repositories coordinate Room entities, domain models, and JSON import/export flows.
- **DI** (`di/`): Hilt provides the database, DAOs, and repositories.

### Key Data Flow

1. User creates or edits a session from the UI flow
2. ViewModels validate inputs and call repositories
3. Repositories persist data through Room DAOs
4. Reactive `Flow` queries update history, statistics, and charts automatically

### Error Handling

Validation issues and one-shot UI events should be surfaced through ViewModel state or `SharedFlow<Event>` rather than hidden in repositories.

## Key Conventions

### Code Style

- Minimal comments — only comment genuinely non-obvious logic
- No Copilot co-author trailers in commit messages
- Prefer Kotlin idioms such as `when`, `let`, `also`, and `mapNotNull`

### Compose

- Avoid unnecessary allocations inside `LazyColumn` items
- Memoize expensive computations with `remember(key) { ... }`
- Prefer `MutableState.value` over `by` delegate when IDE inspections complain about unused assignments
- Always consider the `mobile-android-design` skill before making Android UI, UX, navigation, or Material 3 changes

### Architecture

- ViewModels expose `StateFlow<UiState>` and `SharedFlow<Event>`
- Repositories are `@Singleton` and injected with Hilt
- Room DAOs return `Flow<T>` for reactive queries and suspend functions for writes

### UI Patterns

- Player colour badges use `playerColors(name: String): Pair<Color, Color>` from `ui/theme/`
- Win-ratio badge gradient: red → orange → light green → dark green
- Charts use time-proportional X positions via `LocalDate.toEpochDay()`
- All user-facing text is in Spanish

### Domain Terminology

- **Partido / Session**: one match day
- **Set**: one bracket game inside a session
- **Pair**: two players on one side of a set
