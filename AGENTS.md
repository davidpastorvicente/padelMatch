# AGENTS

## Build And Verify
- Use JDK 21. CI builds with the JetBrains distribution via `actions/setup-java`.
- Main local verification: `./gradlew :app:assembleDebug`
- Release build: `./gradlew :app:assembleRelease`
- Release signing is driven by `RELEASE_KEYSTORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and `RELEASE_KEY_PASSWORD`. If they are unset, local release builds fall back to the debug signing config.
- There are currently no sources under `app/src/test/` or `app/src/androidTest/`.

## Architecture
- This is a single-module Android project. `settings.gradle.kts` includes only `:app`.
- The real app namespace is `com.davidpv.padelmatch` from `app/build.gradle.kts`. Follow Kotlin `package` declarations, not the stale source folder path under `app/src/main/java/com/padelgroup/padelMatch/`.
- App startup flow is `PadelMatchApp` -> `MainActivity` -> `PadelMatchTheme` -> `AppNavigation`.
- `MainActivity` creates a shared `MatchHistoryViewModel` and calls `triggerImportIfNeeded()` on launch. Be careful with changes that affect startup side effects or import behavior.
- Navigation uses typed `@Serializable` routes in `ui/navigation/Routes.kt`, not string route constants.
- Room uses `padel_match.db` and `fallbackToDestructiveMigration(dropAllTables = true)` in `DatabaseModule`. Any schema version bump without a real migration wipes local data.

## Repo Conventions
- Keep user-facing text in Spanish.
- Import/export backup format lives in `data/format/PadelMatchJson.kt`; `JsonExporter` writes `padelMatch.json` through the app `FileProvider`.
- Player badge colors live in `ui/theme/PlayerColors.kt`. Preserve the explicit accented and unaccented name mappings where they exist, for example `rubén` and `ruben`.

## Release Flow
- `versionName` is derived from the latest git tag with the leading `v` removed; if no tag is available it falls back to `0.0.0`.
- `versionCode` is `git rev-list --count HEAD`.
- `.github/workflows/release-apk.yml` only publishes releases when a tag matching `v*` is pushed.
