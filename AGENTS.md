# AGENTS

## Project Snapshot
- PadelMatch is a single-module Android app for tracking padel match sessions and player statistics over time.
- The only Gradle module is `:app`.
- The real app namespace is `com.davidpv.padelmatch` from `app/build.gradle.kts`. Follow Kotlin `package` declarations, not the stale folder path under `app/src/main/java/com/padelgroup/padelMatch/`.

## Build And Verify
- Use JDK 21. CI uses the JetBrains distribution.
- Primary local verification: `./gradlew :app:assembleDebug`
- Other useful commands: `./gradlew clean assembleDebug`, `adb install app/build/outputs/apk/debug/app-debug.apk`, `./gradlew :app:assembleRelease`
- Release signing uses `RELEASE_KEYSTORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and `RELEASE_KEY_PASSWORD`. If unset, local release builds fall back to debug signing.
- There are currently no sources under `app/src/test/` or `app/src/androidTest/`. Do not imply automated test coverage unless you added it.
- `padel_match_analyzer.py` is the repository helper used to validate the session distributions used by the app. When changing `TemplateRepository`, prefer checking candidate schedules with `python3 padel_match_analyzer.py path/to/schedule.txt`.

## Architecture
- App startup flow is `PadelMatchApp` -> `MainActivity` -> `PadelMatchTheme` -> `AppNavigation`.
- `MainActivity` creates a shared `MatchHistoryViewModel` and calls `triggerImportIfNeeded()` on launch. Be careful with startup side effects and import behavior.
- Navigation uses typed `@Serializable` routes in `ui/navigation/Routes.kt`, not string route constants.
- When adding screens, define the typed route first and wire it through `AppNavigation`.
- UI layers should expose `StateFlow<UiState>` and `SharedFlow<Event>` from ViewModels.
- Repositories coordinate Room entities, domain models, and JSON import/export flows.
- Room DAOs should return `Flow<T>` for reactive reads and `suspend` functions for writes.
- Keep validation issues and one-shot UI events in ViewModel state or `SharedFlow<Event>`, not hidden in repositories.
- Keep reusable business logic, derived statistics, and persistence coordination out of composables.

## Persistence And Backup Safety
- Room uses `padel_match.db` and `fallbackToDestructiveMigration(dropAllTables = true)` in `DatabaseModule`.
- Any schema version bump without a real migration wipes local data. Treat Room entity, DAO, and schema changes as data-loss-sensitive.
- Backup format lives in `data/format/PadelMatchJson.kt`.
- If you change the backup format, update importer and exporter together and avoid silently breaking existing backups.
- `JsonExporter` writes `padelMatch.json` through the app `FileProvider`.

## Project Map
- Main source root: `app/src/main/java/com/davidpv/padelmatch/`
- `data/db/`: Room database, DAOs, entities
- `data/model/`: domain models
- `data/repository/`: repositories
- `data/importer/`: JSON import
- `data/exporter/`: JSON export
- `di/`: Hilt modules
- `ui/navigation/`: navigation routes and graph
- `ui/history/`, `ui/newmatch/`, `ui/results/`, `ui/session/`, `ui/statistics/`: main feature areas
- `ui/theme/`: theme, typography, and player colors

## Repo Conventions
- Keep user-facing text in Spanish.
- Keep code identifiers and comments in English unless the surrounding file already follows a different pattern.
- Keep comments minimal and only explain genuinely non-obvious logic.
- Do not add Copilot co-author trailers to commit messages.
- Prefer Kotlin idioms such as `when`, `let`, `also`, and `mapNotNull`.
- Repositories should be `@Singleton` and injected with Hilt.
- The analyzer script currently validates these schedule rules: same matches played by all players, no player rests more than 1 match in a row, max consecutive matches played: 4 for 5 players, 3 for 6 or more players, every player partners with every other player at least once, every player faces every other player at least once, no couple appears in two consecutive matches, and every possible disjoint couple-vs-couple matchup appears at least once.

## Compose And UI
- Preserve the existing Material 3 visual language unless the task explicitly asks for redesign.
- Consider the `mobile-android-design` skill before significant Android UI, UX, navigation, or Material 3 changes.
- Avoid unnecessary allocations inside `LazyColumn` items.
- Memoize expensive computations with `remember(key) { ... }`.
- Prefer `MutableState.value` over the `by` delegate when IDE inspections complain about unused assignments.
- Player badge colors live in `ui/theme/PlayerColors.kt`. Preserve explicit accented and unaccented mappings such as `rubén` and `ruben`.
- Win-ratio badge gradient is red -> orange -> light green -> dark green.
- Charts use time-proportional X positions via `LocalDate.toEpochDay()`.

## Domain Terminology
- Partido / Session: one match day
- Set: one bracket game inside a session
- Pair: two players on one side of a set

## Release Flow
- `versionName` is derived from the latest git tag with the leading `v` removed; if no tag exists it falls back to `0.0.0`.
- `versionCode` is `git rev-list --count HEAD`.
- `.github/workflows/release-apk.yml` only publishes releases when a tag matching `v*` is pushed.
