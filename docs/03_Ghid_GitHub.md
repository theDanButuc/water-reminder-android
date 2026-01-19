# 🚀 Ghid Pas cu Pas: Încărcare Aplicație Water Reminder pe GitHub

## Cerințe Preliminare

- Cont GitHub (dacă nu ai, creează unul gratuit pe github.com)
- Git instalat pe computer
- Proiectul Water Reminder creat în Android Studio

## Pasul 1: Instalarea Git

### Windows

1. Descarcă Git de la: https://git-scm.com/download/win
2. Rulează instalatorul
3. La "Adjusting your PATH environment", alege "Git from the command line and also from 3rd-party software"
4. La "Choose a credential helper", alege "Git Credential Manager"
5. Finalizează instalarea cu restul opțiunilor default

### Mac

1. Deschide Terminal
2. Rulează: `git --version`
3. Dacă nu e instalat, macOS va oferi să-l instaleze
4. Sau instalează prin Homebrew: `brew install git`

### Linux

```bash
# Ubuntu/Debian
sudo apt-get install git

# Fedora
sudo dnf install git
```

## Pasul 2: Configurare Git

1. **Deschide Terminal/Command Prompt**

2. **Configurează numele tău:**
```bash
git config --global user.name "Numele Tău"
```

3. **Configurează email-ul:**
```bash
git config --global user.email "email@tau.com"
```

## Pasul 3: Crearea Contului GitHub (dacă nu ai)

1. Accesează: https://github.com
2. Click "Sign up"
3. Completează formularul:
   - Username (alege un nume unic)
   - Email
   - Parolă
4. Verifică email-ul primit de la GitHub

## Pasul 4: Crearea Repository-ului pe GitHub

1. **Autentifică-te pe GitHub**

2. **Click pe "+" în colțul dreapta sus > "New repository"**

3. **Completează detaliile:**
   - Repository name: `water-reminder-android`
   - Description: `Aplicație Android pentru monitorizarea consumului de apă`
   - Visibility: 
     - **Public** - oricine poate vedea codul (recomandat pentru portofoliu)
     - **Private** - doar tu poți vedea codul
   - **NU bifa** "Initialize this repository with a README"
   - Click "Create repository"

4. **Salvează URL-ul repository-ului:**
   - Vei vedea ceva de genul: `https://github.com/username/water-reminder-android.git`
   - Păstrează pagina deschisă

## Pasul 5: Pregătirea Proiectului

### 5.1 Crearea fișierului .gitignore

1. **În Android Studio:**
   - Click dreapta pe folderul rădăcină al proiectului
   - New > File
   - Nume: `.gitignore`

2. **Conținutul fișierului .gitignore:**

```gitignore
# Built application files
*.apk
*.aar
*.ap_
*.aab

# Files for the ART/Dalvik VM
*.dex

# Java class files
*.class

# Generated files
bin/
gen/
out/
release/

# Gradle files
.gradle/
build/

# Local configuration file (sdk path, etc)
local.properties

# Android Studio
*.iml
.idea/
.DS_Store
/captures
.externalNativeBuild
.cxx

# Keystore files
*.jks
*.keystore

# Version control
.vcs/

# Lint
lint/reports/
lint/generated/
lint/outputs/
lint-baseline.xml
```

### 5.2 Crearea fișierului README.md

1. **În Android Studio:**
   - Click dreapta pe folderul rădăcină
   - New > File
   - Nume: `README.md`

2. **Conținutul fișierului README.md:**

```markdown
# 💧 Water Reminder - Aplicație Android

Aplicație Android pentru monitorizarea și urmărirea consumului zilnic de apă.

## 📱 Caracteristici

- ⏰ Notificări personalizabile pentru a te reaminti să bei apă
- 📊 Statistici detaliate (zilnic, săptămânal, lunar)
- 🌓 Suport pentru Dark Mode / Light Mode
- ⚙️ Interval orar configurabil pentru notificări
- 📈 Tracking automat al consumului (250ml per pahar)
- ✅ Confirmare rapidă din notificare

## 🛠️ Tehnologii Utilizate

- **Kotlin** - Limbaj de programare
- **Jetpack Compose** - UI modern
- **Material Design 3** - Design system
- **SharedPreferences** - Stocare locală date
- **AlarmManager** - Notificări programate

## 📋 Cerințe

- Android 7.0 (API 24) sau mai nou
- Permisiuni necesare:
  - POST_NOTIFICATIONS (pentru notificări)
  - SCHEDULE_EXACT_ALARM (pentru alarme exacte)

## 🚀 Instalare

1. Clonează repository-ul:
```bash
git clone https://github.com/username/water-reminder-android.git
```

2. Deschide proiectul în Android Studio

3. Sincronizează Gradle

4. Rulează aplicația pe emulator sau device fizic

## 📖 Cum se folosește

1. **Prima configurare:**
   - Deschide aplicația
   - Setează intervalul orar (ex: 8:00 - 22:00)
   - Alege frecvența notificărilor (ex: la fiecare oră)
   - Click pe "Activează Notificările"

2. **Tracking zilnic:**
   - Când primești notificarea, confirmă că ai băut apă
   - Sau deschide aplicația și click "Am băut un pahar"

3. **Vezi statisticile:**
   - Tab "Astăzi" - consumul zilei curente
   - Tab "Săptămână" - ultimele 7 zile
   - Tab "Lună" - ultimele 30 zile

## 🎯 Obiectivul proiectului

Această aplicație a fost creată pentru a ajuta utilizatorii să mențină o hidratare adecvată pe parcursul zilei, cu reminder-uri programate și tracking detaliat.

## 📄 Licență

MIT License - vezi fișierul [LICENSE](LICENSE) pentru detalii

## 👤 Autor

**Numele Tău**
- GitHub: [@username](https://github.com/username)

## 🤝 Contribuții

Contribuțiile sunt binevenite! Pentru schimbări majore, te rog deschide mai întâi un issue pentru a discuta ce ai dori să modifici.

## 📸 Screenshots

_(Adaugă aici capturi de ecran cu aplicația)_

## ⭐ Support

Dacă îți place proiectul, lasă un star ⭐ pe GitHub!
```

**Înlocuiește:**
- `username` cu username-ul tău GitHub
- `Numele Tău` cu numele tău real

## Pasul 6: Inițializarea Git Local

1. **Deschide Terminal în Android Studio:**
   - View > Tool Windows > Terminal
   - Sau click pe tab-ul "Terminal" jos

2. **Verifică locația:**
```bash
pwd  # pe Mac/Linux
cd   # pe Windows
```
Ar trebui să fii în folderul proiectului.

3. **Inițializează Git:**
```bash
git init
```

4. **Adaugă toate fișierele:**
```bash
git add .
```

5. **Primul commit:**
```bash
git commit -m "Initial commit - Water Reminder App"
```

## Pasul 7: Conectarea la GitHub

1. **Adaugă remote (înlocuiește cu URL-ul tău):**
```bash
git remote add origin https://github.com/username/water-reminder-android.git
```

2. **Verifică remote-ul:**
```bash
git remote -v
```

3. **Setează branch-ul principal:**
```bash
git branch -M main
```

## Pasul 8: Încărcarea pe GitHub

1. **Push la GitHub:**
```bash
git push -u origin main
```

2. **Autentificare:**
   - Vei fi întrebat de username și parolă
   - Pentru parolă, **NU folosi parola contului!**
   - Trebuie să creezi un **Personal Access Token**

### Crearea Personal Access Token

1. **Pe GitHub:**
   - Click pe avatar (dreapta sus) > Settings
   - Scroll jos: Developer settings
   - Personal access tokens > Tokens (classic)
   - Generate new token (classic)

2. **Configurare token:**
   - Note: `Water Reminder Upload`
   - Expiration: `90 days` (sau custom)
   - Bifează: `repo` (toate sub-opțiunile)
   - Scroll jos și click "Generate token"

3. **Copiază token-ul** (apare o singură dată!)

4. **Folosește token-ul ca parolă** când dai push

## Pasul 9: Verificare

1. **Reîmprospătează pagina GitHub** în browser

2. **Ar trebui să vezi:**
   - Toate fișierele proiectului
   - README.md vizibil pe pagina principală
   - Commit-ul tău inițial

## Pasul 10: Adăugarea unei Licențe (Opțional)

1. **Pe GitHub, pe pagina repository-ului:**
   - Click pe "Add file" > "Create new file"
   - Nume fișier: `LICENSE`
   - Click pe "Choose a license template"
   - Selectează "MIT License"
   - Completează anul și numele
   - Scroll jos și click "Commit new file"

## Pasul 11: Adăugarea de Screenshots (Opțional dar recomandat)

1. **Fă screenshot-uri pe telefon:**
   - Ecranul principal cu pahare băute
   - Setările de notificări
   - Statisticile (tabs diferite)
   - O notificare

2. **Creează folder în proiect:**
```bash
mkdir screenshots
```

3. **Copiază imaginile** în acest folder

4. **Commit și push:**
```bash
git add screenshots/
git commit -m "Add app screenshots"
git push
```

5. **Actualizează README.md** cu linkuri la imagini:
```markdown
## 📸 Screenshots

![Main Screen](screenshots/main_screen.png)
![Settings](screenshots/settings.png)
![Statistics](screenshots/stats.png)
```

## Comenzi Git Utile pentru Viitor

### Când faci modificări

```bash
# Verifică ce s-a modificat
git status

# Adaugă fișierele modificate
git add .

# Sau adaugă un fișier specific
git add app/src/main/java/com/example/waterreminder/MainActivity.kt

# Commit cu mesaj descriptiv
git commit -m "Descriere modificare"

# Push la GitHub
git push
```

### Mesaje commit bune

```bash
git commit -m "Add water intake goal feature"
git commit -m "Fix notification not showing on Android 13+"
git commit -m "Update README with installation guide"
git commit -m "Improve UI for dark mode"
```

### Verificare istoric

```bash
# Vezi istoricul commit-urilor
git log

# Versiune mai scurtă
git log --oneline

# Vezi diferențele
git diff
```

## 🎉 Felicitări!

Aplicația ta este acum pe GitHub! Acum poți:
- Împărtăși link-ul cu alții
- Adăuga proiectul în CV/portofoliu
- Colabora cu alți developeri
- Track modificările și versiunile

## Link-uri Utile

- **Repository-ul tău:** `https://github.com/username/water-reminder-android`
- **GitHub Docs:** https://docs.github.com
- **Git Documentation:** https://git-scm.com/doc
- **Markdown Guide:** https://www.markdownguide.org

## Troubleshooting

### Eroare: "Permission denied"

**Soluție:** Verifică Personal Access Token sau configurează SSH keys.

### Eroare: "Repository not found"

**Soluție:** Verifică URL-ul remote:
```bash
git remote -v
git remote remove origin
git remote add origin URL-ul-corect
```

### Conflicte de merge

**Soluție:** 
```bash
git pull origin main
# Rezolvă conflictele manual în fișiere
git add .
git commit -m "Resolve merge conflicts"
git push
```

### Ai uitat să adaugi .gitignore

**Soluție:**
```bash
git rm -r --cached .
git add .
git commit -m "Add .gitignore and remove ignored files"
git push
```
