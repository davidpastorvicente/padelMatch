# AGENTS

## Project Overview
- PadelMatch is an Android app for tracking padel match sessions.
- Users record sessions, each session contains multiple sets, and the app computes player statistics and win-ratio trends over time.

## Build And Verify
- Use JDK 21. CI builds with the JetBrains distribution via `actions/setup-java`.
- Main local verification: `./gradlew :app:assembleDebug`
- Useful local commands: `./gradlew assembleDebug`, `./gradlew clean assembleDebug`, and `adb install app/build/outputs/apk/debug/app-debug.apk`.
- Release build: `./gradlew :app:assembleRelease`
- Release signing is driven by `RELEASE_KEYSTORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and `RELEASE_KEY_PASSWORD`. If they are unset, local release builds fall back to the debug signing config.
- There are currently no sources under `app/src/test/` or `app/src/androidTest/`.

## Tech Stack
- Kotlin 2.0, Jetpack Compose, and Material 3.
- MVVM with repository pattern.
- Hilt for dependency injection.
- Room for persistence.
- Compose Navigation with typed `kotlinx.serialization` routes.
- Kotlin Coroutines with `StateFlow` and `SharedFlow`.
- `kotlinx.serialization` for serialization.
- Min SDK 26 and target SDK 35.

## Project Structure
- Main source root: `app/src/main/java/com/davidpv/padelmatch/`.
- `data/db/`: Room database, DAOs, entities.
- `data/model/`: domain data classes.
- `data/repository/`: repository classes.
- `data/importer/`: JSON import.
- `data/exporter/`: JSON export.
- `di/`: Hilt modules.
- `ui/history/`: match history, charts, calendar.
- `ui/navigation/`: app navigation and bottom tabs.
- `ui/newmatch/`: new match flow.
- `ui/results/`: edit results screens and sheets.
- `ui/session/`: session detail screens.
- `ui/statistics/`: player statistics and detailed charts.
- `ui/theme/`: color, typography, and player colors.

## Architecture
- This is a single-module Android project. `settings.gradle.kts` includes only `:app`.
- The real app namespace is `com.davidpv.padelmatch` from `app/build.gradle.kts`. Follow Kotlin `package` declarations, not the stale source folder path under `app/src/main/java/com/padelgroup/padelMatch/`.
- App startup flow is `PadelMatchApp` -> `MainActivity` -> `PadelMatchTheme` -> `AppNavigation`.
- `MainActivity` creates a shared `MatchHistoryViewModel` and calls `triggerImportIfNeeded()` on launch. Be careful with changes that affect startup side effects or import behavior.
- Navigation uses typed `@Serializable` routes in `ui/navigation/Routes.kt`, not string route constants.
- Room uses `padel_match.db` and `fallbackToDestructiveMigration(dropAllTables = true)` in `DatabaseModule`. Any schema version bump without a real migration wipes local data.
- UI layers should expose `StateFlow<UiState>` and `SharedFlow<Event>` from ViewModels.
- Repositories coordinate Room entities, domain models, and JSON import/export flows.
- Room DAOs should return `Flow<T>` for reactive queries and suspend functions for writes.
- Validation issues and one-shot UI events should surface through ViewModel state or `SharedFlow<Event>`, not be hidden in repositories.

## Repo Conventions
- Keep user-facing text in Spanish.
- Import/export backup format lives in `data/format/PadelMatchJson.kt`; `JsonExporter` writes `padelMatch.json` through the app `FileProvider`.
- Player badge colors live in `ui/theme/PlayerColors.kt`. Preserve the explicit accented and unaccented name mappings where they exist, for example `rubén` and `ruben`.
- Keep comments minimal and only explain genuinely non-obvious logic.
- Do not add Copilot co-author trailers to commit messages.
- Prefer Kotlin idioms such as `when`, `let`, `also`, and `mapNotNull`.
- Repositories should be `@Singleton` and injected with Hilt.

## Compose And UI
- Avoid unnecessary allocations inside `LazyColumn` items.
- Memoize expensive computations with `remember(key) { ... }`.
- Prefer `MutableState.value` over the `by` delegate when IDE inspections complain about unused assignments.
- Always consider the `mobile-android-design` skill before Android UI, UX, navigation, or Material 3 changes.
- Win-ratio badge gradient is red -> orange -> light green -> dark green.
- Charts use time-proportional X positions via `LocalDate.toEpochDay()`.

## Domain Terminology
- Partido / Session: one match day.
- Set: one bracket game inside a session.
- Pair: two players on one side of a set.

## Release Flow
- `versionName` is derived from the latest git tag with the leading `v` removed; if no tag is available it falls back to `0.0.0`.
- `versionCode` is `git rev-list --count HEAD`.
- `.github/workflows/release-apk.yml` only publishes releases when a tag matching `v*` is pushed.
