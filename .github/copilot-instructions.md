# PadelMatch — Copilot Instructions

## Project Overview

PadelMatch is an Android app for tracking padel match sessions. Users record sessions (one per day), each containing multiple sets (bracket games between pairs of players). The app tracks player statistics and win-ratio trends over time.

## Tech Stack

- **Language**: Kotlin 2.0
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Repository pattern
- **DI**: Hilt (`@HiltViewModel`, `@Inject`, `@Singleton`)
- **Database**: Room (SQLite) with DAOs and Flow
- **Navigation**: Compose Navigation with typed routes (kotlinx.serialization)
- **Async**: Kotlin Coroutines + StateFlow / SharedFlow
- **Serialization**: kotlinx.serialization (JSON import/export)
- **Min SDK**: 26 (Android 8.0), Target SDK: 35

## Project Structure

```
app/src/main/java/com/davidpv/padelmatch/
├── data/
│   ├── db/           # Room database, DAOs, entities
│   ├── model/        # Domain data classes (PlayerStats, PlayerSessionEntry, …)
│   ├── repository/   # Repository classes — single source of truth for UI
│   ├── importer/     # JSON import
│   └── exporter/     # JSON export
├── di/               # Hilt DatabaseModule
└── ui/
    ├── history/      # MatchHistoryScreen, SessionCard, BracketGameCard, ClassificationChart, DayCell
    ├── navigation/   # AppNavigation, HomeScreen, HomeNavigation (bottom tabs)
    ├── newmatch/     # NewMatchScreen, NewMatchViewModel, PlayerSelectionStep
    ├── results/      # EditResultsScreen, GamePickerSheet
    ├── session/      # SessionDetailScreen, SessionDetailViewModel
    ├── statistics/   # StatisticsScreen, PlayerDetailScreen, PlayerDetailViewModel, CombinedWinRatioChartScreen
    └── theme/        # Color, Typography, playerColors()
```

## Domain Terminology

- **Partido / Session**: one match day — one `SessionEntity` per calendar day
- **Set**: a single bracket game within a session — one `GameEntity` per set
- **Pair**: two players on one side of a set

## Key Conventions

### Code Style
- Minimal comments — only comment genuinely non-obvious logic
- No trailing `Co-authored-by` trailers in git commits
- Kotlin idioms preferred (`when`, `let`, `also`, `mapNotNull`, etc.)

### Compose
- Avoid inline lambda allocations inside `LazyColumn` items — use `remember(key) { lambda }`
- Memoize expensive computations inside composables with `remember(key) { ... }`
- Use `remember` for sorted lists, color lookups (`playerColors()`), date parsing, Paint objects
- Prefer `MutableState.value` over `by` delegate when IDE warns about unused assignments
- Always consider the `mobile-android-design` skill before making Android UI, UX, navigation, or Material 3 changes

### Architecture
- ViewModels expose `StateFlow<UiState>` and `SharedFlow<Event>` (for one-shot navigation/toasts)
- Repositories are `@Singleton` and injected via Hilt
- Room DAOs return `Flow<T>` for reactive queries; suspend functions for one-shot writes
- Navigation events carry data (e.g. `SharedFlow<Long>` for sessionId after creation)

### Naming
- Screens: `<Feature>Screen.kt` + `<Feature>ViewModel.kt`
- DAOs: `<Entity>Dao.kt` in `data/db/dao/`
- Entities: `<Name>Entity` in `data/db/entity/`
- Repositories: `<Domain>Repository.kt` in `data/repository/`

### UI Patterns
- Player colour badges use `playerColors(name: String): Pair<Color, Color>` from `ui/theme/`
- Win-ratio badge gradient: red(0%) → orange(30%) → light green(50%) → dark green(100%)
- Charts use time-proportional X axis (`LocalDate.toEpochDay()` for positioning)
- Combined chart is rotated 90° via `RotatedLayout` (swaps width/height constraints + `graphicsLayer { rotationZ = 90f }`) for landscape display within portrait layout
- Chart line colours use `lerp(bg, onColor, 0.45f)` for a vivid mid-tone between badge background and text colour
- Canvas tooltips: width from `Paint.measureText()` + padding; height from `fontMetrics.ascent/descent` for pixel-perfect equal padding; text positioned at baseline using `-fm.ascent` offset
- Win ratio percentages use `roundToInt()` (never `toInt()`) everywhere; global win ratio shown with one decimal (`"%.1f%%".format(...)`)
- Player badges sorted alphabetically wherever displayed
- Spanish locale for all user-facing date strings (`Locale.forLanguageTag("es")`)
- All user-facing text is in Spanish

## Build

```bash
# Debug APK (auto-signed, installable)
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Install via ADB
adb install app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 21 (JetBrains). Set `JAVA_HOME` to your JetBrains JDK 21 install if building from CLI.

## CI

GitHub Actions (`.github/workflows/debug-apk.yml`) builds a debug APK on every push to `master` and uploads it as an artifact for 7 days.
