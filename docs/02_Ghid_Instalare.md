# 📱 Ghid Pas cu Pas: Instalare Aplicație Water Reminder pe Android

## Cerințe Preliminare

- Computer (Windows, Mac sau Linux)
- Telefon Android
- Cablu USB
- Android Studio instalat

## Pasul 1: Instalarea Android Studio

1. **Descarcă Android Studio**
   - Accesează: https://developer.android.com/studio
   - Descarcă versiunea pentru sistemul tău de operare
   - Rulează instalatorul și urmează instrucțiunile

2. **Prima configurare**
   - La prima pornire, alege "Standard Setup"
   - Descarcă componentele necesare (durează ~15-30 minute)

## Pasul 2: Crearea Proiectului

1. **Deschide Android Studio**
   - Click pe "New Project"
   - Selectează "Empty Activity"
   - Click "Next"

2. **Configurare proiect**
   - Name: `Water Reminder`
   - Package name: `com.example.waterreminder`
   - Save location: alege o locație pe computer
   - Language: `Kotlin`
   - Minimum SDK: `API 24 (Android 7.0)`
   - Click "Finish"

## Pasul 3: Adăugarea Codului

### 3.1 Creare structură de foldere

În stânga, în tab-ul "Project", navighează la:
```
app/src/main/java/com/example/waterreminder/
```

Creează următoarele foldere (click dreapta pe `waterreminder` > New > Package):
- `ui`
- `ui.theme`

### 3.2 Copierea fișierelor

**Fișier 1: MainActivity.kt**
- Locație: `app/src/main/java/com/example/waterreminder/MainActivity.kt`
- Copiază codul din artefactul "Water Reminder App - MainActivity.kt"

**Fișier 2: WaterReminderReceiver.kt**
- Click dreapta pe `waterreminder` > New > Kotlin Class/File
- Nume: `WaterReminderReceiver`
- Tip: `Class`
- Copiază codul din artefactul "WaterReminderReceiver.kt"

**Fișier 3: Theme.kt**
- Locație: `app/src/main/java/com/example/waterreminder/ui/theme/Theme.kt`
- Click dreapta pe folderul `theme` > New > Kotlin Class/File
- Nume: `Theme`
- Copiază codul din artefactul "Theme.kt"

**Fișier 4: Type.kt**
- Locație: `app/src/main/java/com/example/waterreminder/ui/theme/Type.kt`
- În același folder `theme`, creează `Type.kt`
- Copiază codul din artefactul "Type.kt"

### 3.3 Fișiere de configurare

**AndroidManifest.xml**
- Locație: `app/src/main/AndroidManifest.xml`
- Înlocuiește conținutul cu codul din artefactul "AndroidManifest.xml"

**build.gradle.kts**
- Locație: `app/build.gradle.kts`
- Înlocuiește conținutul cu codul din artefactul "build.gradle.kts (app)"

## Pasul 4: Sincronizare Gradle

1. Click pe "Sync Now" (apare în partea de sus după modificarea build.gradle)
2. Așteaptă sincronizarea (durează 2-5 minute)
3. Verifică că nu sunt erori în tab-ul "Build" de jos

## Pasul 5: Pregătirea Telefonului

### Activare Developer Mode

1. **Pe telefon:**
   - Deschide Settings (Setări)
   - Scroll la "About phone" (Despre telefon)
   - Găsește "Build number" (Număr compilare)
   - Apasă de 7 ori pe "Build number"
   - Vei vedea mesaj: "You are now a developer!"

### Activare USB Debugging

2. **Înapoi în Settings:**
   - Găsește "Developer options" (Opțiuni pentru dezvoltatori)
   - Activează "USB debugging"
   - Confirmă în pop-up

## Pasul 6: Conectarea Telefonului

1. **Conectează telefonul la computer cu cablul USB**

2. **Pe telefon:**
   - Va apărea un pop-up "Allow USB debugging?"
   - Bifează "Always allow from this computer"
   - Click "Allow" sau "OK"

3. **În Android Studio:**
   - În bara de sus, lângă butonul Run (▶️)
   - Ar trebui să apară numele telefonului tău
   - Dacă nu apare, click pe dropdown și selectează telefonul

## Pasul 7: Instalarea Aplicației

1. **Click pe butonul verde Run (▶️)** în Android Studio

2. **Așteaptă compilarea:**
   - Prima compilare durează 3-5 minute
   - Vezi progresul în bara de jos ("Build: Running")

3. **Instalare automată:**
   - După compilare, aplicația se instalează automat pe telefon
   - Se va deschide automat după instalare

## Pasul 8: Permisiuni pe Telefon

1. **La prima deschidere:**
   - Aplicația va cere permisiune pentru notificări
   - Click "Allow" sau "Permite"

2. **Setează notificările:**
   - În aplicație, configurează intervalul orar (ex: 8:00 - 22:00)
   - Setează frecvența (ex: la fiecare 60 minute)
   - Click pe "Activează Notificările"

## Pasul 9: Verificare Funcționalitate

### Test notificări

1. Setează un interval scurt (ex: 15 minute)
2. Așteaptă notificarea
3. Când apare, ai două opțiuni:
   - Deschide aplicația (click pe notificare)
   - Click pe "Am băut" direct din notificare

### Test statistici

1. Click pe "Am băut un pahar" de câteva ori
2. Verifică tab-urile: Astăzi, Săptămână, Lună
3. Contorizarea ar trebui să fie corectă

## Pasul 10: Dezinstalare Cablu USB

După instalare, poți deconecta cablul USB. Aplicația va rămâne pe telefon și va funcționa independent.

## 🎉 Felicitări!

Aplicația este acum instalată și funcțională pe telefonul tău!

## Troubleshooting (Probleme Comune)

### Telefonul nu apare în Android Studio

**Soluție:**
- Verifică că USB debugging este activat
- Încearcă alt cablu USB (unele cabluri sunt doar pentru încărcare)
- Schimbă modul USB: pe telefon, când îl conectezi, selectează "File Transfer" sau "MTP"

### Erori de compilare

**Soluție:**
- Click pe "File" > "Invalidate Caches / Restart"
- Click "Invalidate and Restart"
- Așteaptă repornirea Android Studio

### Notificările nu apar

**Soluție:**
- Verifică setările telefonului: Settings > Apps > Water Reminder > Notifications
- Asigură-te că notificările sunt permise
- Verifică că telefonul nu e în modul "Do Not Disturb"

### Aplicația se oprește

**Soluție:**
- În Android Studio, vezi tab-ul "Logcat" pentru erori
- Verifică că ai copiat corect tot codul
- Asigură-te că ai sincronizat Gradle

## Notă Importantă

- Aplicația salvează datele local pe telefon
- Dacă dezinstalezi aplicația, toate datele se pierd
- Pentru a păstra datele, nu dezinstala aplicația
