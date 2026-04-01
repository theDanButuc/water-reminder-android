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
├── MainActivity.kt
├── WaterReminderApplication.kt
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt
│   │   ├── dao/WaterIntakeDao.kt
│   │   └── entity/
│   │       ├── WaterIntake.kt
│   │       └── DrinkType.kt
│   ├── preferences/UserPreferences.kt
│   └── repository/WaterRepository.kt
├── ui/
│   ├── components/
│   │   ├── CircularProgressIndicator.kt  ← new (replaces water bottle)
│   │   └── BarChart.kt
│   ├── home/WaterViewModel.kt
│   ├── screens/
│   │   ├── MainScreen.kt
│   │   ├── TodayScreen.kt
│   │   ├── StatisticsScreen.kt
│   │   ├── SettingsScreen.kt             ← to be added
│   │   └── OnboardingScreen.kt           ← to be added
│   └── theme/
│       ├── Theme.kt
│       └── Type.kt
└── workers/
    ├── ReminderWorker.kt
    └── ReminderScheduler.kt
```

---

## Dependencies (Current)

```kotlin
// Room (2.6.1)
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// DataStore
implementation("androidx.datastore:datastore-preferences:1.1.1")

// WorkManager
implementation("androidx.work:work-runtime-ktx:2.9.0")

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
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
implementation("androidx.activity:activity-compose:1.8.2")
```

### Dependencies to add (as needed per task)

```kotlin
// Compose Navigation — add when implementing Settings + Onboarding
implementation("androidx.navigation:navigation-compose:2.8.0")

// Charts (for statistics improvements)
implementation("com.patrykandpatrick.vico:compose-m3:1.15.0")

// Health Connect — add in Phase 3
implementation("androidx.health.connect:connect-client:1.1.0-alpha07")

// Glance — for Compose-based widgets (Phase 3)
implementation("androidx.glance:glance-appwidget:1.1.0")
implementation("androidx.glance:glance-material3:1.1.0")
```

---

## Data Models

### Current entity (v2, already migrated)

```kotlin
@Entity(tableName = "water_intake")
data class WaterIntake(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val drinkType: String = DrinkType.WATER.name
)
```

### DrinkType enum

```kotlin
enum class DrinkType(val hydrationFactor: Float, val emoji: String, val displayName: String) {
    WATER(1.0f, "💧", "Water"),
    COFFEE(0.8f, "☕", "Coffee"),
    TEA(0.9f, "🍵", "Tea"),
    JUICE(0.85f, "🧃", "Juice"),
    MILK(0.9f, "🥛", "Milk"),
    ENERGY_DRINK(0.7f, "⚡", "Energy"),
    OTHER(0.8f, "🥤", "Other")
}
```

---

## UI Rules

- Use **Material Design 3** components exclusively
- Enable **Dynamic Color** on Android 12+ (already in `Theme.kt`)
- **Never hardcode colors** — use `MaterialTheme.colorScheme.*`
- All strings in `res/values/strings.xml`
- Dark mode works via Material 3 theme attributes

---

## Progress Indicator (Home Screen)

**Do NOT use the animated water bottle shape** — it is unclear to users and looks random.

Use a **large circular progress ring** instead:
- Outer ring: `CircularProgressIndicator` styled as a thick arc (strokeWidth ~16dp)
- Center text: consumed ml (large, bold) + goal ml (smaller, secondary color)
- Below ring: percentage and remaining ml text
- Color of ring: follows goal completion (use `MaterialTheme.colorScheme.primary` for normal, green tint at 100%)

```kotlin
// Example structure
Box(contentAlignment = Alignment.Center) {
    CircularProgressIndicator(
        progress = { fraction },
        modifier = Modifier.size(220.dp),
        strokeWidth = 16.dp,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("${consumed}ml", style = MaterialTheme.typography.headlineLarge)
        Text("/ ${goal}ml", style = MaterialTheme.typography.bodyMedium)
        Text("${percent}%", style = MaterialTheme.typography.titleMedium)
    }
}
```

---

## App Icon

Replace the current green upside-down drop icon.

**New icon spec:**
- Background: blue circle (`#1976D2` or similar)
- Foreground: white glass/cup shape (simple outline) with a white water drop inside or above it
- Style: flat, Material You compatible, clean
- Files needed: `ic_launcher.xml` (vector adaptive icon) + `ic_launcher_background.xml` + `ic_launcher_foreground.xml`
- The drop must point **downward** (standard orientation)

---

## Drink-Type-Specific Amount Presets

**Do NOT use the same amount list for all drink types.**

Each DrinkType has its own preset amounts that make sense for that category.
When the user selects a drink type, the amount chips update to that type's presets.
Always include a "Custom" option at the end.

```kotlin
// Define in a constants file or companion object
val DRINK_TYPE_PRESETS: Map<DrinkType, List<DrinkPreset>> = mapOf(

    DrinkType.WATER to listOf(
        DrinkPreset("100ml", 100), DrinkPreset("200ml", 200),
        DrinkPreset("250ml", 250), DrinkPreset("330ml", 330),
        DrinkPreset("500ml", 500), DrinkPreset("750ml", 750)
    ),

    DrinkType.COFFEE to listOf(
        DrinkPreset("Ristretto", 15),  DrinkPreset("Espresso", 30),
        DrinkPreset("Lungo", 60),      DrinkPreset("Americano", 240),
        DrinkPreset("Cappuccino", 150), DrinkPreset("Latte", 300)
    ),

    DrinkType.TEA to listOf(
        DrinkPreset("Cup", 200), DrinkPreset("Mug", 300),
        DrinkPreset("Carafe", 500), DrinkPreset("Large", 750)
    ),

    DrinkType.JUICE to listOf(
        DrinkPreset("Small", 150), DrinkPreset("Glass", 250),
        DrinkPreset("Bottle", 330), DrinkPreset("Large", 500)
    ),

    DrinkType.MILK to listOf(
        DrinkPreset("Small", 150), DrinkPreset("Glass", 250),
        DrinkPreset("Large", 350)
    ),

    DrinkType.ENERGY_DRINK to listOf(
        DrinkPreset("Can", 250), DrinkPreset("Large can", 355),
        DrinkPreset("Bottle", 500)
    ),

    DrinkType.OTHER to listOf(
        DrinkPreset("Small", 100), DrinkPreset("Medium", 250),
        DrinkPreset("Large", 500)
    )
)

data class DrinkPreset(val label: String, val amountMl: Int)
```

The selected preset label (e.g. "Espresso") is shown in Today's log alongside the emoji.

---

## Onboarding Flow

Show on first launch only (`ONBOARDING_DONE == false` in UserPreferences).

**Screen 1 — Welcome**
- App name + tagline
- "Let's set up your daily hydration plan"
- Next button

**Screen 2 — Daily Goal**
- Slider or number input: default 2000ml
- Optional: weight-based calculator (weight in kg × 33ml = suggested goal)
- Text: "We'll remind you throughout the day to reach this goal"

**Screen 3 — Wake & Sleep Time**
- Two time pickers: Wake time (default 07:00) + Sleep time (default 23:00)
- Text: "Reminders will only be sent during your waking hours"
- Notification interval derived automatically: `wakeHours / (goalMl / 250)`
  - Example: 16h awake, goal 2000ml → 8 glasses → reminder every 2h

**Screen 4 — Notification Permission**
- Explain why notifications are needed
- Request `POST_NOTIFICATIONS` permission here (not on app launch)
- "Allow" button → request permission → finish onboarding

On finish: save goal, wake time, sleep time to UserPreferences. Set `ONBOARDING_DONE = true`. Schedule WorkManager with computed interval.

---

## Notification Logic

Notifications must be **evenly distributed** between wake time and sleep time based on the daily goal.

```
interval = (sleepTime - wakeTime) / numberOfGlasses
numberOfGlasses = ceil(goalMl / 250)
```

- Minimum interval: 30 minutes
- Maximum interval: 4 hours
- Never notify outside wake/sleep window (quiet hours = before wakeTime or after sleepTime)
- Stop notifying once daily goal is reached

ReminderWorker checks on every trigger:
1. Is current time within wake/sleep window? → if not, skip
2. Is daily goal already reached? → if yes, skip
3. Show notification with contextual message based on time of day + progress

```kotlin
val message = when {
    progressPct < 25 -> "Time to start hydrating! 💧"
    progressPct < 50 -> "Keep it up! You're at $progressPct% of your goal."
    progressPct < 75 -> "Halfway there! $remainingMl ml to go."
    progressPct < 100 -> "Almost done! Just $remainingMl ml remaining."
    else -> return Result.success() // goal reached, skip
}
```

---

## UserPreferences Keys

```kotlin
object PreferenceKeys {
    val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")           // default: 2000
    val WAKE_TIME = stringPreferencesKey("wake_time")                 // default: "07:00"
    val SLEEP_TIME = stringPreferencesKey("sleep_time")               // default: "23:00"
    val NOTIFICATION_INTERVAL_MIN = intPreferencesKey("notif_interval_min") // computed on onboarding
    val UNIT_ML = booleanPreferencesKey("unit_ml")                    // true=ml, false=oz
    val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")    // default: false
    val LAST_SELECTED_DRINK_TYPE = stringPreferencesKey("last_drink_type") // default: "WATER"
}
```

---

## Async Rules

- All DB ops: `Flow` or `suspend` — never on main thread
- All ViewModel ops: `viewModelScope`
- Collect flows in Compose: `collectAsState()` or `collectAsStateWithLifecycle()`

---

## Security & Build

- **Never commit** `keystore.jks`, `keystore.properties`, `local.properties`, `google-services.json`
- CI/CD: debug APK via GitHub Actions (already configured in `.github/workflows/build-apk.yml`)
- Release: signing keys as GitHub Actions secrets only

---

## Prioritized Task List

Phases 0 and 1 are **DONE**. Continue from Phase 2.

---

### ✅ PHASE 0 — Code Cleanup (DONE)
- [x] 0.1 Split MainActivity.kt
- [x] 0.2 Split WaterRepository.kt
- [x] 0.3 Fix ViewModel scope
- [x] 0.4 Move strings to strings.xml

### ✅ PHASE 1 — Core Features (DONE)
- [x] 1.1 Custom volume input + quick-select chips
- [x] 1.2 Configurable daily goal + DataStore + congrats Snackbar
- [x] 1.3 DrinkType selector + Room migration v1→v2 + hydration factor
- [x] 1.4 Migrate notifications to WorkManager + quiet hours

---

### 🔴 PHASE 2 — UX Overhaul (Do Next)

- [ ] **Fix 2.1** — Replace water bottle with circular progress ring
  - Delete `AnimatedWaterBottle.kt`
  - Create `HydrationProgressRing.kt` composable using `CircularProgressIndicator`
  - Show consumed ml (large) + goal ml + percentage in center
  - Animate progress with `animateFloatAsState`

- [ ] **Fix 2.2** — New app icon
  - Create `ic_launcher_foreground.xml`: white glass with white downward drop
  - Create `ic_launcher_background.xml`: solid blue (`#1565C0`)
  - Update `AndroidManifest.xml` to use adaptive icon
  - Add `mipmap/ic_launcher.xml` adaptive icon wrapper

- [ ] **Fix 2.3** — Drink-type-specific amount presets
  - Create `DrinkPreset` data class + `DRINK_TYPE_PRESETS` map (see spec above)
  - When user selects a DrinkType chip, the amount chips below update to that type's presets
  - Show preset label (e.g. "Espresso") instead of just "30ml" in Today's log
  - Add `presetLabel: String?` field to `WaterIntake` entity → Room migration v2→v3
  - Save last selected DrinkType to UserPreferences as `LAST_SELECTED_DRINK_TYPE`

- [ ] **Fix 2.4** — Onboarding flow
  - Add Compose Navigation dependency
  - Create `OnboardingScreen.kt` with 4 steps (Welcome, Goal, Wake/Sleep, Notifications)
  - In `MainActivity`: check `ONBOARDING_DONE` → route to Onboarding or MainScreen
  - Compute notification interval from goal + wake/sleep times, save to UserPreferences
  - Request notification permission on Screen 4 (remove from `PermissionWrapper`)
  - On finish: schedule WorkManager with computed interval, set `ONBOARDING_DONE = true`

- [ ] **Fix 2.5** — Smart notification interval
  - Update `ReminderScheduler` to use `NOTIFICATION_INTERVAL_MIN` from UserPreferences
  - Update `ReminderWorker` to check wake/sleep window (replace current quiet hours logic)
  - Update `ReminderWorker` to skip if daily goal already reached
  - Add contextual notification messages based on progress percentage (see spec above)

---

### 🟡 PHASE 3 — Feature Additions

- [ ] **Feat 3.1** — Settings Screen
  - Compose Navigation: Home ↔ Settings
  - Items: daily goal, wake/sleep times, notification interval (auto or manual), unit ml/oz
  - Reset today's data (confirmation dialog)
  - Re-trigger onboarding (for goal recalculation)
  - When goal or wake/sleep times change → recompute interval → reschedule WorkManager

- [ ] **Feat 3.2** — Streak counter
  - Consecutive days where `effectiveMl >= goalMl`
  - Show current streak + best streak on TodayScreen
  - Calendar heatmap on Statistics tab: green=met, red=missed, grey=no data

- [ ] **Feat 3.3** — Statistics improvements
  - Replace manual Canvas BarChart with Vico library
  - Color coding: red < 50%, yellow 50–79%, green ≥ 80%
  - Monthly average line
  - Per-drink-type breakdown (stacked bar or pie)

- [ ] **Feat 3.4** — Smart notification messages (already specced in Notifications section)

---

### 🟢 PHASE 4 — Polish & Distribution

- [ ] **Feat 4.1** — Home Screen Widget (Jetpack Glance)
  - Small (2×1): circular progress ring + quick-add button
  - Large (4×2): ring + drink type selector + 3 presets

- [ ] **Feat 4.2** — Health Connect integration (optional, permission-based)

- [ ] **Feat 4.3** — Play Store preparation
  - Migrate package to `com.danbutuc.waterreminder`
  - Bump targetSdk to 35, enable R8
  - Signed AAB via `./gradlew bundleRelease`

---

### 📁 PHASE 5 — Repository Cleanup

- [ ] README, CHANGELOG, LICENSE, CONTRIBUTING, issue templates, screenshots

---

## Things Claude Code Must Never Do

- Never use `SharedPreferences` — use `DataStore` only
- Never use `AlarmManager` for notifications — use `WorkManager` only
- Never hardcode colors — use `MaterialTheme.colorScheme.*`
- Never make DB calls on the main thread
- Never commit or expose `keystore.jks` or `keystore.properties`
- Never use `GlobalScope` — use `viewModelScope`
- Never use `LiveData` for new code — use `StateFlow` / `SharedFlow`
- Never skip null safety
- Never use Fragment-based navigation — Compose only
- Never use `repeatOnLifecycle` — use `collectAsState()` in Compose
- Never use the animated water bottle for progress — use circular ring

---

## Testing Requirements

Every new feature must include:
- Unit test for ViewModel logic (`test/`)
- Unit test for Repository with in-memory Room DB
- Instrumentation test for DAO queries (`androidTest/`)

---

## Commit Convention

```
feat: add onboarding flow with goal and wake/sleep setup
fix: replace water bottle with circular progress ring
refactor: drink-type-specific amount presets
chore: update dependencies
```

---

*Last updated: April 2026 | Maintained by Dan Butuc*
