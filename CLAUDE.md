# CLAUDE.md — Water Reminder Android

> This file is read automatically by Claude Code when working in this repository.
> It contains project context, architecture rules, coding standards, and a prioritized task list.

---

## Project Overview

- **Name:** Water Reminder
- **Platform:** Android (Native)
- **Language:** Kotlin
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34
- **Build System:** Gradle (Kotlin DSL — `build.gradle.kts`)
- **CI/CD:** GitHub Actions (`.github/workflows/`)
- **Current Version:** v1.0
- **Package:** `com.example.waterreminder` *(to be migrated to `com.danbutuc.waterreminder` before Play Store)*
- **Repo:** https://github.com/theDanButuc/water-reminder-android

---

## Architecture

Follow **MVVM + Repository Pattern** with **Jetpack Compose** UI.

```
app/src/main/java/com/example/waterreminder/
├── MainActivity.kt                # Single Activity, hosts all Compose screens
├── WaterReminderApplication.kt    # Application class (currently inside MainActivity.kt)
├── NotificationScheduler.kt       # AlarmManager notifications (to be migrated to WorkManager)
├── BootReceiver.kt                # Re-schedules alarms on boot
├── data/
│   ├── WaterRepository.kt         # Repository + Room DB + DAO + Entity (all-in-one currently)
│   └── (future) db/
│   │   ├── AppDatabase.kt         # Room database singleton
│   │   ├── dao/
│   │   │   ├── DrinkEntryDao.kt
│   │   │   └── DailyGoalDao.kt
│   │   └── entity/
│   │       ├── DrinkEntry.kt
│   │       └── DailyGoal.kt
│   ├── (future) repository/
│   │   └── WaterRepository.kt     # Single source of truth
│   └── (future) preferences/
│       └── UserPreferences.kt     # Jetpack DataStore
├── ui/
│   ├── theme/
│   │   ├── Theme.kt               # Material 3 + Dynamic Colors
│   │   └── Type.kt                # Typography
│   ├── (future) screens/
│   │   ├── HomeScreen.kt
│   │   ├── StatsScreen.kt
│   │   └── SettingsScreen.kt
├── (future) workers/
│   └── ReminderWorker.kt          # WorkManager replacement
└── (future) util/
    ├── Extensions.kt
    └── Constants.kt
```

### Current state

Everything lives in two files:
- `MainActivity.kt` (539 lines) — contains Application class, all Compose screens, ViewModel, ViewModelFactory, chart composables
- `data/WaterRepository.kt` (71 lines) — contains Entity, DAO, Database, and Repository

**Refactoring priority:** Split `MainActivity.kt` into separate files as features are added.

---

## Dependencies (Current)

```kotlin
// build.gradle.kts (app)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

// Room (2.6.1)
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// Compose (BOM 2023.08.00)
implementation(platform("androidx.compose:compose-bom:2023.08.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-graphics")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended")

// Core
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
implementation("androidx.activity:activity-compose:1.8.2")
```

### Dependencies to add (as needed per task)

```kotlin
// DataStore — add when implementing UserPreferences
implementation("androidx.datastore:datastore-preferences:1.1.1")

// WorkManager — add when migrating notifications
implementation("androidx.work:work-runtime-ktx:2.9.0")

// Compose Navigation — add when implementing Settings screen
implementation("androidx.navigation:navigation-compose:2.8.0")

// ViewModel Compose — add for better ViewModel integration
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

// Charts (for statistics improvements) — choose one:
implementation("com.patrykandpatrick.vico:compose-m3:1.15.0")

// Health Connect — add in Phase 3
implementation("androidx.health.connect:connect-client:1.1.0-alpha07")

// Glance — for Compose-based widgets (Phase 3)
implementation("androidx.glance:glance-appwidget:1.1.0")
implementation("androidx.glance:glance-material3:1.1.0")
```

Do not introduce alternatives to these without asking.

---

## Data Models

### Current entity

```kotlin
// WaterIntake — currently in WaterRepository.kt
@Entity(tableName = "water_intake")
data class WaterIntake(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Int,           // in ml
    val timestamp: Long = System.currentTimeMillis()
)
```

### Target entities (requires Room migration from v1 to v2)

```kotlin
// DrinkEntry.kt — replaces WaterIntake
@Entity(tableName = "drink_entries")
data class DrinkEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val drinkType: DrinkType = DrinkType.WATER,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null
)

enum class DrinkType(val hydrationFactor: Float, val emoji: String) {
    WATER(1.0f, "\uD83D\uDCA7"),
    COFFEE(0.8f, "\u2615"),
    TEA(0.9f, "\uD83C\uDF75"),
    JUICE(0.85f, "\uD83E\uDDC3"),
    MILK(0.9f, "\uD83E\uDD5B"),
    ENERGY_DRINK(0.7f, "\u26A1"),
    OTHER(0.8f, "\uD83E\uDD64")
}

// DailyGoal.kt
@Entity(tableName = "daily_goals")
data class DailyGoal(
    @PrimaryKey val date: String,  // format: "2026-04-01"
    val goalMl: Int = 2000,
    val achievedMl: Int = 0
)
```

**Important:** When migrating, write a proper Room `Migration(1, 2)` — do not use `fallbackToDestructiveMigration()` as users will lose data.

---

## Compose UI Patterns

### State collection in Compose

```kotlin
// Correct — use collectAsState() in @Composable functions
val todayIntake by viewModel.todayIntake.collectAsState()

// Never use repeatOnLifecycle in Compose — that's a Fragment/View pattern
```

### ViewModel scope

```kotlin
// Correct — use viewModelScope for coroutines in ViewModel
fun addWaterIntake(amount: Int) {
    viewModelScope.launch {
        repository.addWaterIntake(amount)
    }
}

// Current code uses MainScope() — migrate to viewModelScope
```

### Navigation (when adding Settings)

```kotlin
// Use Compose Navigation, not Fragment Navigation Component
val navController = rememberNavController()
NavHost(navController, startDestination = "home") {
    composable("home") { HomeScreen(viewModel, navController) }
    composable("settings") { SettingsScreen(settingsViewModel, navController) }
}
```

---

## Async Rules

- **All DB operations** must use `Flow` or `suspend` functions — never blocking calls on main thread
- **All ViewModel operations** must use `viewModelScope` (not `MainScope()`)
- **Collect flows** in Compose using `collectAsState()` or `collectAsStateWithLifecycle()`

---

## UserPreferences (DataStore) — To Be Implemented

All user settings must go through `UserPreferences.kt`. Never use SharedPreferences.

```kotlin
// Keys to define in UserPreferences.kt
object PreferenceKeys {
    val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")           // default: 2000
    val DEFAULT_CUP_ML = intPreferencesKey("default_cup_ml")         // default: 250
    val NOTIFICATION_INTERVAL_H = intPreferencesKey("notif_interval") // default: 2
    val QUIET_HOURS_START = stringPreferencesKey("quiet_start")       // default: "22:00"
    val QUIET_HOURS_END = stringPreferencesKey("quiet_end")           // default: "07:00"
    val WAKE_TIME = stringPreferencesKey("wake_time")                 // default: "07:00"
    val SLEEP_TIME = stringPreferencesKey("sleep_time")               // default: "22:00"
    val UNIT_ML = booleanPreferencesKey("unit_ml")                    // true = ml, false = oz
    val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")    // default: false
}
```

---

## Notifications

### Current implementation (AlarmManager — to be replaced)

- `NotificationScheduler.kt` uses `AlarmManager.setInexactRepeating()` every hour
- `AlarmReceiver` BroadcastReceiver triggers `showNotification()`
- `BootReceiver` reschedules on device boot
- Channel ID: `"water_reminder_channel"`
- Fixed message: "Time for Water! Don't forget to stay hydrated."

### Target implementation (WorkManager)

**Never use AlarmManager or Handler for notifications.** Use WorkManager exclusively.

```kotlin
class ReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val prefs = UserPreferences(applicationContext)
        if (prefs.isQuietHours()) return Result.success()
        if (prefs.isDailyGoalAchieved()) return Result.success()
        showNotification()
        return Result.success()
    }
}
```

When migrating:
1. Remove `NotificationScheduler.kt`, `AlarmReceiver`, and `BootReceiver`
2. Remove `SCHEDULE_EXACT_ALARM` permission from manifest
3. Remove exact alarm permission check from `PermissionWrapper`
4. Add WorkManager dependency
5. Schedule `PeriodicWorkRequest` on app start
6. Add quiet hours check from UserPreferences

---

## UI Rules

- Use **Material Design 3** components exclusively (not Material 2)
- Enable **Dynamic Color** on Android 12+ with fallback to app theme on older versions (already implemented in `Theme.kt`)
- **Never hardcode colors** — use `MaterialTheme.colorScheme.*` in Compose
- All user-facing strings should be in `res/values/strings.xml` — minimize hardcoded strings in Kotlin
- Dark mode must work via Material 3 theme attributes (already set up)

---

## Security & Build

- **Never commit** `keystore.jks`, `keystore.properties`, `local.properties`, `google-services.json`
- These must be in `.gitignore` — verify before every commit
- CI/CD must use **release build** (`assembleRelease`), not debug
- Signing keys stored as **GitHub Actions secrets** only
- APK distributed publicly must always be a **signed release build**

```
# .gitignore — must contain:
*.jks
*.keystore
keystore.properties
local.properties
google-services.json
```

---

## Prioritized Task List

Work through these in order. Each phase builds on the previous.

---

### PHASE 0 — Code Cleanup (Prerequisites)

- [ ] **Clean 0.1** — Split `MainActivity.kt` into separate files
  - Extract `WaterReminderApplication` to its own file
  - Extract `WaterViewModel` + `WaterViewModelFactory` to `ui/home/WaterViewModel.kt`
  - Extract `AnimatedWaterBottle` composable to `ui/components/AnimatedWaterBottle.kt`
  - Extract `BarChart` + `ChartData` to `ui/components/BarChart.kt`
  - Extract screen composables to `ui/screens/` (TodayScreen, StatisticsScreen)
  - Keep `MainActivity` thin — only `setContent { }` call

- [ ] **Clean 0.2** — Split `WaterRepository.kt` into proper structure
  - Move `WaterIntake` entity to `data/db/entity/WaterIntake.kt`
  - Move `WaterIntakeDao` to `data/db/dao/WaterIntakeDao.kt`
  - Move `AppDatabase` to `data/db/AppDatabase.kt`
  - Keep `WaterRepository` in `data/repository/WaterRepository.kt`

- [ ] **Clean 0.3** — Fix ViewModel scope
  - Replace `MainScope()` with `viewModelScope` in `WaterViewModel`
  - Remove manual `coroutineScope` field

- [ ] **Clean 0.4** — Move hardcoded strings to `strings.xml`
  - "Today", "Week", "Month" tab labels
  - "I drank a glass (250ml)" button text
  - "Time for Water!" notification title/text
  - Permission screen titles and descriptions
  - "No data available for this period yet."

---

### PHASE 1 — Core Features

- [ ] **Fix 1.1** — Replace fixed 250ml with custom volume input
  - Add `amountMl: Int` parameter to `addWaterIntake()` instead of hardcoded 250
  - Add quick-select buttons: 100 / 200 / 250 / 330 / 500 / 750ml
  - Add "Custom" option that opens a number input dialog
  - Save last-used amount to UserPreferences as `DEFAULT_CUP_ML`

- [ ] **Fix 1.2** — Add configurable daily goal
  - Add DataStore dependency and implement `UserPreferences.kt`
  - Add `DAILY_GOAL_ML` preference (default: 2000ml)
  - Replace hardcoded `goal = 2000` in `TodayScreen` with preference value
  - Show progress: `achievedMl / goalMl` with percentage and remaining ml
  - Show congratulations Snackbar when goal is first reached for the day

- [ ] **Fix 1.3** — Add DrinkType selector
  - Add `DrinkType` enum (see Data Models section)
  - Create Room migration v1 -> v2 (add drinkType column with default WATER)
  - Add type picker (icon grid or horizontal scroll chips)
  - Apply `hydrationFactor` when calculating effective hydration
  - Show drink type emoji in today's log list

- [ ] **Fix 1.4** — Migrate notifications to WorkManager
  - Add WorkManager dependency
  - Implement `ReminderWorker` with quiet hours check
  - Schedule with `PeriodicWorkRequest` on app start
  - Remove `NotificationScheduler.kt`, `AlarmReceiver`, `BootReceiver`
  - Remove `SCHEDULE_EXACT_ALARM` permission
  - Cancel and reschedule when interval changes in Settings

---

### PHASE 2 — Feature Additions

- [ ] **Feat 2.1** — Settings Screen
  - Add Compose Navigation dependency
  - Add navigation: Home <-> Settings
  - Settings items:
    - Daily goal (ml input)
    - Default cup size (ml input or preset selector)
    - Notification interval (1h / 1.5h / 2h / 3h / Off)
    - Quiet hours (time picker: start + end)
    - Wake time / sleep time
    - Unit toggle: ml <-> oz
    - Reset today's data (with confirmation dialog)
    - Export data as CSV

- [ ] **Feat 2.2** — Streak counter
  - Calculate streak from Room DB: consecutive days where `achievedMl >= goalMl`
  - Show current streak and best streak on TodayScreen
  - Show streak in Statistics tab
  - Show calendar heatmap view (monthly): green = goal met, red = missed, grey = no data

- [ ] **Feat 2.3** — Statistics improvements
  - Replace manual Canvas bar chart with Vico library
  - Add color coding per bar (red < 50%, yellow 50-79%, green >= 80% of goal)
  - Add monthly average line
  - Add per-drink-type breakdown (pie chart or stacked bar)

- [ ] **Feat 2.4** — Smart notification content
  - Vary notification message based on time of day and progress
  - Morning (07:00-10:00): "Start your day hydrated!"
  - Afternoon (12:00-16:00): "Halfway through the day — how's your water intake?"
  - Evening (18:00-21:00): "Almost done! You need X ml to reach your goal."
  - Add quick-action button in notification: "+250ml" via PendingIntent -> BroadcastReceiver

---

### PHASE 3 — Polish & Distribution

- [ ] **Feat 3.1** — Home Screen Widget
  - Use Jetpack Glance (Compose for widgets) instead of AppWidgetProvider XML
  - Small (2x1): Progress text + "Add 250ml" button
  - Large (4x2): Progress ring + 3 quick-add buttons + today's log count
  - Update widget after every drink entry

- [ ] **Feat 3.2** — Health Connect integration
  - Add `connect-client` dependency
  - Add permission request flow (only if user enables in Settings)
  - Sync each `DrinkEntry` to Health Connect as `HydrationRecord`
  - Handle unavailability gracefully

- [ ] **Feat 3.3** — Onboarding flow
  - Show only on first launch (`ONBOARDING_DONE == false`)
  - Screen 1: Welcome + app description
  - Screen 2: Set daily goal (with weight-based calculator option)
  - Screen 3: Set wake time + sleep time
  - Screen 4: Enable notifications (request permission here)
  - Mark `ONBOARDING_DONE = true` on finish

- [ ] **Feat 3.4** — Play Store preparation
  - Migrate package from `com.example.waterreminder` to `com.danbutuc.waterreminder`
  - Bump `targetSdkVersion` to 35
  - Verify all permissions in manifest are necessary and declared
  - Add `android:exported` to all Activities, Receivers, Services
  - Enable R8/ProGuard (`isMinifyEnabled = true`) for release
  - Generate signed release AAB with `./gradlew bundleRelease`
  - Write Play Store listing text in `fastlane/metadata/android/en-US/`

---

### PHASE 4 — Repository Cleanup

- [ ] **Repo 4.1** — Replace `README.md` with proper project documentation
- [ ] **Repo 4.2** — Add `CHANGELOG.md` with all past releases documented
- [ ] **Repo 4.3** — Add `LICENSE` file (MIT)
- [ ] **Repo 4.4** — Add `CONTRIBUTING.md`
- [ ] **Repo 4.5** — Add `.github/ISSUE_TEMPLATE/bug_report.md` and `feature_request.md`
- [ ] **Repo 4.6** — Update CI/CD workflow to use signed release APK
- [ ] **Repo 4.7** — Add screenshots to `docs/screenshots/` for README

---

## Things Claude Code Must Never Do

- Never use `SharedPreferences` — use `DataStore` only
- Never use `AlarmManager` for notifications — use `WorkManager` only
- Never hardcode colors in Kotlin — use `MaterialTheme.colorScheme.*`
- Never make DB calls on the main thread
- Never commit or expose `keystore.jks` or `keystore.properties`
- Never use `GlobalScope` — use `viewModelScope` or coroutine scope tied to lifecycle
- Never use `LiveData` for new code — use `StateFlow` / `SharedFlow`
- Never skip null safety — use `?.let`, `?: return`, or `requireNotNull`
- Never add a dependency that conflicts with the ones listed above
- Never use Fragment-based navigation — this is a Compose-only project
- Never use `repeatOnLifecycle` — use `collectAsState()` in Compose

---

## Testing Requirements

Every new feature must include:

- **Unit test** for ViewModel logic (`test/`)
- **Unit test** for Repository logic with in-memory Room DB
- **Instrumentation test** for DAO queries (`androidTest/`)

```kotlin
// In-memory Room DB for tests
@Before
fun setup() {
    db = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        AppDatabase::class.java
    ).allowMainThreadQueries().build()
    dao = db.waterIntakeDao()
}
```

---

## Commit Convention

Use Conventional Commits format:

```
feat: add custom volume input
fix: notification not firing after quiet hours
refactor: migrate SharedPreferences to DataStore
chore: update dependencies to latest stable
docs: update README with new screenshots
test: add unit tests for WaterRepository
```

---

*Last updated: April 2026 | Maintained by Dan Butuc*
