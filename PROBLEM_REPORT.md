# Water Reminder - Problem Report & Debug Guide

## Status
- **Date:** 2026-04-12
- **Tester:** Dan Butuc (Samsung S24 Ultra)
- **Current Version:** v1.2 (versionCode=2)
- **Last Commit:** cb63740 (beer emoji fix)

---

## Problems Identified

### 1. ❌ BEER/WINE Preset Quantities Incorrect
**Status:** NOT FIXED

**Symptoms:**
- In "Today's log", WINE preset amounts show wrong effective values:
  - Glass Wine: shows ~112 ml (should be 150 ml * 0.75 = 112.5 ml) ← **This is correct!**
  - Carafe Wine: shows ~375 ml (should be 500 ml * 0.75 = 375 ml) ← **This is correct!**
  - Bottle Wine: shows ~562 ml (should be 750 ml * 0.75 = 562.5 ml) ← **This is correct!**
  - Small Beer: shows ~280 ml (should be 330 ml * 0.85 = 280.5 ml) ← **This is correct!**

**Root Cause Analysis:**
- The "effective" amounts shown in Today's log ARE mathematically correct (amount × hydrationFactor)
- BUT the preset DEFINITIONS in code are correct:
  - WINE: Glass=150ml, Carafe=500ml, Bottle=750ml
  - BEER: Small=330ml, 0.5L=500ml, 1L=1000ml, 2L=2000ml
- **Problem:** Entries in database have OLD values from OLD code version
  - User installed v1.0/v1.1 which had wrong preset values
  - Database saved entries with those old values
  - Even though code is now correct, old database entries persist

**Evidence:**
- Code check: DrinkPresets.kt has CORRECT values (330ml, 150ml, 500ml, 750ml)
- User tested Clear Data + reinstall but problem persists
- This suggests database is not being cleared OR entries are pre-populated from somewhere

### 2. ✅ BEER Emoji Wrong
**Status:** FIXED in commit cb63740

**What was wrong:**
- Emoji was "\uD83C\uDF3B" = 🌻 (sunflower)
- Changed to "\uD83C\uDF7A" = 🍺 (beer mug)

**Symptom Fixed:**
- User reported "iconita e o floare" - now shows 🍺 instead of 🌻

---

## Investigation Needed

### 1. Where are the old preset values coming from?

**Hypothesis 1:** Presets defined elsewhere in code (not just DrinkPresets.kt)
- Check: TodayScreen.kt, SettingsScreen.kt, WaterViewModel.kt for hardcoded values
- Check: Build gradle for any resource overlays

**Hypothesis 2:** Database has default presets hardcoded
- Check: AppDatabase.kt, WaterRepository.kt for any prepopulated data
- Check: Room migrations for default values

**Hypothesis 3:** APK built from wrong commit
- The APK user downloaded might be from commit 5ec3172 (before fixes)
- GitHub Actions might not have rebuilt properly

**Hypothesis 4:** Presets values are in Room entity defaults
- Check: WaterIntake.kt, DrinkType.kt for any @ColumnInfo defaults
- Check: DAO default queries

### 2. Clear Data not working properly
- User followed: Settings → Apps → Water Reminder → Storage → Clear Data
- Reinstalled APK
- Problem still persists
- **This is NOT normal** - Clear Data should delete database
- Possible: Database recreation from defaults, or cache issue

---

## Steps Already Taken

✅ Fixed BEER emoji (🌻 → 🍺)
✅ Verified DrinkPresets.kt has correct values
✅ Version bumped to 1.2
✅ Commits pushed to GitHub
❌ User Clear Data didn't fix the issue
❌ Reinstall didn't fix the issue

---

## What Needs Investigation

### For Next Session (use Sonnet):

1. **Search exhaustively for where old preset values are defined:**
   - `grep -r "280\|112\|375\|562" app/src/`
   - Check all resource files (xml, strings)
   - Check gradle build files for flavor-specific values
   - Check AndroidManifest for metadata

2. **Verify DrinkPresets.kt is actually being used:**
   - Trace the code path: TodayScreen → defaultPresetsFor() → DrinkPresets
   - Check if there are any conditional branches that load different presets
   - Verify that `presets` variable in TodayScreen gets correct values

3. **Check Room Database for default values:**
   - Look at WaterRepository.kt initialization
   - Check if any migrations insert default drink presets
   - Verify database is actually cleared when user does Clear Data

4. **Verify APK actually has latest code:**
   - Check GitHub Actions build logs for commit cb63740
   - Verify the APK timestamp matches recent build
   - Consider if APK caching is an issue

5. **Check WaterIntake entity for amount field:**
   - Verify amount is not being modified elsewhere
   - Check if there's any automatic unit conversion happening (ml ↔ oz)
   - Check presetLabel field - is it being saved correctly?

---

## How to Proceed

**Option A: Full Clean Install**
1. `adb uninstall com.example.waterreminder`
2. Delete app from phone entirely (including cache)
3. Download fresh APK from GitHub Releases
4. Install clean
5. Test BEFORE adding any drinks (verify chip values are correct first)

**Option B: Debug from Code**
1. Use Sonnet to:
   - Search for ALL occurrences of preset amounts in codebase
   - Find where presets are loaded in TodayScreen
   - Add debug logging to trace which values are being used
   - Rebuild with debug APK

**Option C: Database Reset**
1. Check if clear data works on Samsung S24 Ultra
2. Try adb to clear app data: `adb shell pm clear com.example.waterreminder`
3. Verify database file is actually deleted

---

## Files to Review

- `app/src/main/java/com/example/waterreminder/util/DrinkPresets.kt` - PRIMARY SOURCE
- `app/src/main/java/com/example/waterreminder/ui/screens/TodayScreen.kt` - HOW PRESETS ARE USED
- `app/src/main/java/com/example/waterreminder/data/repository/WaterRepository.kt` - DATABASE OPS
- `app/src/main/java/com/example/waterreminder/data/db/AppDatabase.kt` - DATABASE CONFIG
- `app/build.gradle.kts` - VERSION & BUILD CONFIG
- `.github/workflows/main.yml` - BUILD AUTOMATION

---

## Notes

- User is frustrated - previous suggestions (Clear Data, reinstall) didn't work
- This suggests a DEEPER issue than just outdated database entries
- The fact that emoji fixed but amounts didn't = different issue
- Sonnet should do exhaustive code search for the root cause
