# Water Reminder — Android App Skill

## Project Overview
- **App:** Water Reminder — daily hydration tracking app
- **Platform:** Android (native)
- **Language:** Kotlin
- **Developer:** Dan Butuc / PINECONE S.à.r.l.-S

## Tech Stack

### Core
- **Language:** Kotlin (100% — no Java mixing unless legacy dependency)
- **Min SDK:** 26 (Android 8.0) — assume this unless told otherwise
- **Target SDK:** latest stable
- **Build system:** Gradle (Kotlin DSL — `build.gradle.kts`)

### Architecture
- **Pattern:** MVVM (Model-View-ViewModel)
- **UI:** Jetpack Compose (prefer over XML layouts)
- **Navigation:** Jetpack Navigation Compose
- **State:** `StateFlow` + `collectAsStateWithLifecycle()`

### Libraries (assume available unless told otherwise)
```kotlin
// DI
implementation("com.google.dagger:hilt-android:...")

// Room (local DB)
implementation("androidx.room:room-runtime:...")
implementation("androidx.room:room-ktx:...")
kapt("androidx.room:room-compiler:...")

// DataStore (preferences)
implementation("androidx.datastore:datastore-preferences:...")

// WorkManager (background reminders)
implementation("androidx.work:work-runtime-ktx:...")

// Compose BOM
implementation(platform("androidx.compose:compose-bom:..."))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
```

## Project Structure
```
app/src/main/
  java/com/pinecone/waterreminder/
    data/
      local/
        dao/          ← Room DAOs
        database/     ← AppDatabase.kt
        entity/       ← Room entities
      repository/     ← Repository interfaces + implementations
    di/               ← Hilt modules
    domain/
      model/          ← domain data classes
      usecase/        ← use case classes
    ui/
      screens/        ← one folder per screen
        home/
          HomeScreen.kt
          HomeViewModel.kt
        settings/
      components/     ← reusable Composables
      theme/
        Color.kt
        Theme.kt
        Type.kt
    util/             ← extensions, constants, helpers
    MainActivity.kt
    MainApplication.kt
  res/
    drawable/         ← vector assets (SVG → XML via Android Studio)
    values/
      strings.xml     ← all user-facing strings here, never hardcoded
      colors.xml      ← brand color references
```

## Kotlin Code Conventions

### General
- Prefer `val` over `var` — immutability by default
- Use `data class` for models, `sealed class` for UI state and results
- Extension functions for utility logic, not static methods
- No `!!` (non-null assertion) — use `?.let { }`, `?: return`, or `requireNotNull()`
- Coroutines for all async work — no callbacks, no RxJava

### Compose conventions
```kotlin
// Composable naming: PascalCase, noun-based
@Composable
fun WaterIntakeCard(
    intake: Int,
    goal: Int,
    onAddIntake: (Int) -> Unit,
    modifier: Modifier = Modifier  // always last, always defaulted
) { ... }

// State hoisting — keep Composables stateless where possible
// ViewModel holds state, Composable observes it
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

### ViewModel pattern
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getIntakeUseCase: GetTodayIntakeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTodayIntake()
    }
}

// UI state as sealed class or data class
data class HomeUiState(
    val isLoading: Boolean = false,
    val totalIntake: Int = 0,
    val dailyGoal: Int = 2000,
    val error: String? = null
)
```

### Room conventions
```kotlin
@Entity(tableName = "water_intake")
data class WaterIntakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Int,           // ml
    val timestamp: Long,       // epoch millis
    val note: String? = null
)

@Dao
interface WaterIntakeDao {
    @Query("SELECT * FROM water_intake WHERE timestamp >= :startOfDay")
    fun getTodayIntake(startOfDay: Long): Flow<List<WaterIntakeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WaterIntakeEntity)
}
```

### WorkManager (reminders)
```kotlin
// Periodic reminder worker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // show notification
        return Result.success()
    }
}
```

## Brand / UI Guidelines
- **Primary color:** `#2D6A4F` (Pinecone green) — used for primary buttons, progress indicators
- **Accent:** `#cc561e` (Pinecone orange) — used for alerts, milestones reached
- **Font:** preferably Nunito or Poppins for a friendly, health-app feel
- Water amounts in **ml** internally, display in ml or oz based on user preference (DataStore)
- Daily goal default: **2000ml**

## Notification conventions
- Channel ID: `water_reminder_channel`
- Channel name: "Hydration Reminders"
- Importance: `IMPORTANCE_DEFAULT` (not HIGH — avoid intrusive)
- Always include a quick-action button: "Log 250ml"

## Testing conventions
- Unit tests in `test/` — ViewModels, UseCases, Repositories (with fakes)
- UI tests in `androidTest/` — critical flows only (Compose testing APIs)
- Test naming: `givenX_whenY_thenZ()`

## When working on this project
1. Check for `TODO:` and `FIXME:` comments before starting new features
2. Run Lint before committing: `./gradlew lint`
3. After adding Room entities/DAOs: increment database version + add migration
4. All user-facing strings → `strings.xml`, never hardcoded in Kotlin/Compose
5. Test on API 26 (min) and latest emulator
