# 🚀 Ghid Rapid de Referință - Water Reminder

## Comenzi Git Esențiale

```bash
# Verifică statusul
git status

# Adaugă toate modificările
git add .

# Commit
git commit -m "Descriere modificare"

# Push la GitHub
git push

# Pull de pe GitHub (dacă ai modificat pe site)
git pull

# Verifică remote-ul
git remote -v

# Vezi istoricul
git log --oneline
```

## Workflow Zilnic

### Când faci modificări în cod:

```bash
# 1. Salvează fișierele în Android Studio (Ctrl+S / Cmd+S)

# 2. Testează aplicația
# Click pe Run ▶️ în Android Studio

# 3. Commit local
git add .
git commit -m "Descriere clară a modificării"

# 4. Push la GitHub
git push
```

## Exemple de Mesaje Commit

```bash
✅ Bune:
git commit -m "Add weekly statistics chart"
git commit -m "Fix notification sound on Android 13"
git commit -m "Update app icon and splash screen"
git commit -m "Improve dark mode contrast"

❌ Rele:
git commit -m "update"
git commit -m "fix bug"
git commit -m "changes"
```

## Shortcuts Android Studio

### Windows/Linux
- `Ctrl + S` - Salvează
- `Shift + F10` - Run app
- `Alt + Enter` - Quick fix
- `Ctrl + /` - Comentează linia
- `Ctrl + D` - Duplică linia
- `Ctrl + Space` - Auto-complete

### Mac
- `Cmd + S` - Salvează
- `Ctrl + R` - Run app
- `Option + Enter` - Quick fix
- `Cmd + /` - Comentează linia
- `Cmd + D` - Duplică linia
- `Ctrl + Space` - Auto-complete

## Gradle Commands

```bash
# Curăță build
./gradlew clean

# Construiește aplicația
./gradlew build

# Instalează pe device conectat
./gradlew installDebug
```

## Troubleshooting Rapid

### Aplicația nu pornește
```
1. Check Logcat for errors
2. File > Invalidate Caches > Restart
3. Build > Clean Project
4. Build > Rebuild Project
```

### Notificările nu funcționează
```
1. Verifică permisiunile în Settings > Apps > Water Reminder
2. Dezactivează Battery Optimization pentru aplicație
3. Verifică că Do Not Disturb e oprit
```

### Git push eșuează
```bash
# Verifică authentication
git pull
# Rezolvă conflicte dacă apar
git push
```

### Erori de Gradle
```bash
# Sincronizează din nou
# Click pe "Sync Project with Gradle Files" în toolbar

# Sau din terminal:
./gradlew --refresh-dependencies
```

## Fișiere Importante - Quick Access

```
MainActivity.kt
└─ UI principal și logica aplicației

WaterReminderReceiver.kt
└─ Receiver pentru notificări

Theme.kt
└─ Configurare Dark/Light mode

AndroidManifest.xml
└─ Permisiuni și configurare app

build.gradle.kts
└─ Dependențe și configurare build
```

## Modificări Frecvente

### Schimbă intervalul default
În `MainActivity.kt`, linia ~20:
```kotlin
val startHour = prefs.getInt("startHour", 8)  // 8 = 8AM
val endHour = prefs.getInt("endHour", 22)     // 22 = 10PM
```

### Schimbă frecvența default
În `MainActivity.kt`, linia ~22:
```kotlin
val intervalMinutes = prefs.getInt("intervalMinutes", 60) // 60 min
```

### Schimbă cantitatea per pahar
În `MainActivity.kt`, caută `* 250` și înlocuiește cu cantitatea dorită:
```kotlin
Text(text = "${glassesConsumed * 250} ml")
```

### Schimbă textul notificării
În `WaterReminderReceiver.kt`, linia ~40:
```kotlin
.setContentTitle("💧 Timp să bei apă!")
.setContentText("Nu uita să bei un pahar cu apă (250ml)")
```

## Teste Rapide

### Test notificare immediată
```kotlin
// Adaugă în MainActivity.kt pentru test:
Button(onClick = { 
    val intent = Intent(context, WaterReminderReceiver::class.java)
    context.sendBroadcast(intent)
}) {
    Text("Test Notificare")
}
```

### Test salvare date
```kotlin
// În Logcat (Android Studio), caută:
println("Glasses today: $glassesConsumed")
```

## Comenzi ADB Utile

```bash
# Listează device-uri conectate
adb devices

# Instalează APK manual
adb install app-debug.apk

# Vezi loguri în timp real
adb logcat

# Clear app data (șterge toate datele)
adb shell pm clear com.example.waterreminder

# Force stop app
adb shell am force-stop com.example.waterreminder
```

## Linkuri Rapide

- **Android Documentation:** https://developer.android.com
- **Kotlin Documentation:** https://kotlinlang.org/docs
- **Jetpack Compose:** https://developer.android.com/jetpack/compose
- **Material Design 3:** https://m3.material.io
- **Git Cheat Sheet:** https://education.github.com/git-cheat-sheet-education.pdf

## Task Checklist pentru Îmbunătățiri

### Funcționalități Noi
- [ ] Adaugă widget pentru ecranul principal
- [ ] Integrare cu Google Fit
- [ ] Export date în CSV
- [ ] Obiective zilnice personalizabile
- [ ] Reminder-e inteligente (ML)
- [ ] Multiplayer/competiție cu prietenii
- [ ] Badges și achievements

### UI/UX
- [ ] Animații pentru adăugare pahar
- [ ] Grafice interactive pentru statistici
- [ ] Onboarding tutorial la prima lansare
- [ ] Teme de culoare multiple
- [ ] Sunete personalizabile
- [ ] Haptic feedback

### Tehnic
- [ ] Room Database în loc de SharedPreferences
- [ ] ViewModel pentru state management
- [ ] Unit tests
- [ ] UI tests
- [ ] Backup în cloud
- [ ] Multi-language support

## Performance Tips

### Optimizare baterie
```kotlin
// Folosește WorkManager în loc de AlarmManager
// pentru task-uri mai lungi
import androidx.work.WorkManager
```

### Reducere dimensiune APK
```kotlin
// În build.gradle.kts:
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
}
```

## Release Checklist

Înainte de a publica pe Play Store:
- [ ] Incrementează versionCode și versionName în build.gradle.kts
- [ ] Testează pe multiple device-uri (diferite versiuni Android)
- [ ] Verifică toate permisiunile
- [ ] Creează iconiță profesională
- [ ] Screenshots pentru Play Store (telefon + tabletă)
- [ ] Scrie descriere aplicație
- [ ] Adaugă privacy policy
- [ ] Generează signed APK/AAB
- [ ] Test pe device-uri fizice

## Cheatsheet Jetpack Compose

```kotlin
// Column - vertical layout
Column { }

// Row - horizontal layout  
Row { }

// Button
Button(onClick = { }) {
    Text("Click")
}

// Text
Text(
    text = "Hello",
    style = MaterialTheme.typography.headlineMedium
)

// Card
Card {
    // content
}

// Spacer
Spacer(modifier = Modifier.height(16.dp))
```

## Date Utile

- **Package name:** `com.example.waterreminder`
- **Min SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)
- **Versiune:** 1.0
- **Channel ID:** `water_reminder`
- **Notification ID:** 1

## Suport și Resurse

- **Stack Overflow:** Caută erori aici
- **Reddit:** r/androiddev
- **Discord:** Android Dev Community
- **YouTube:** Coding in Flow, Philipp Lackner

---

**Salvează acest fișier** ca referință rapidă! 📌
