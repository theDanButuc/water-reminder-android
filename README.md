# Water Reminder - Android App

An app to track your daily water intake and stay hydrated throughout the day.

<img src="Water Reminder preview.jpg" width="320"/>

## Download APK

You can find the latest version in the [Releases](../../releases) section.

## Features

- **Hydration progress ring** — large circular indicator with consumed ml, goal, and percentage
- **9 drink types** — Water, Coffee, Tea, Juice, Milk, Energy Drink, Beer, Wine, Other — each with hydration factor
- **Drink-type-specific presets** — e.g. Coffee shows Ristretto/Espresso/Lungo instead of generic ml values
- **Onboarding flow** — set daily goal, wake/sleep times, and notification permission on first launch
- **Smart notifications** — evenly distributed between wake and sleep time; stop when goal is reached
- **Contextual notification messages** — progress-based messages (e.g. "Almost done! Just 250ml remaining.")
- **Streak counter** — consecutive days goal was met, shown on Today screen
- **Statistics screen** — daily bar chart (day/week/month) with color coding (red/yellow/green by progress)
- **Settings screen** — change goal, wake/sleep times, notification interval, unit (ml/oz), reset today's data
- **Automatic Dark/Light mode** — via Material You dynamic color (Android 12+)
- **Quiet hours** — notifications only sent during waking hours
- **WorkManager-based scheduling** — reliable background notifications

## Installation

1. Download `app-debug.apk` from Releases
2. Allow installation from unknown sources
3. Install the APK
4. Enjoy the app!

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **Architecture:** MVVM + Repository Pattern
- **Database:** Room (v3 schema)
- **Preferences:** DataStore
- **Background work:** WorkManager
- **Min SDK:** 24 (Android 7.0) | **Target SDK:** 34 (Android 14)

## Author

Dan Butuc
