# POS RF Overhaul

A Kotlin-based Android point-of-sale application built with Jetpack Compose and Gradle Kotlin DSL. The project includes a modern UI layer, state management, repository/data access code, and Firebase AI integration.

## Overview

This repository appears to be an Android app focused on POS workflows, including a main app screen and a baking-related workflow. It uses:

- Kotlin
- Jetpack Compose
- Material 3
- ViewModel-driven state handling
- Firebase AI
- Android Gradle project structure

## Tech Stack

- Kotlin
- Android SDK 37
- Jetpack Compose
- Material 3
- Firebase AI
- Gradle Kotlin DSL

## Project Structure

```text
POS-RF-OVERHAUL/
├── .gitignore
├── .gitattributes
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── gradle/
│   └── wrapper/
├── app/
│   ├── build.gradle.kts
│   ├── .gitignore
│   └── src/
│       ├── androidTest/
│       ├── test/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── keepRules/
│           └── java/com/example/posrf/
│               ├── MainActivity.kt
│               ├── BakingScreen.kt
│               ├── BakingViewModel.kt
│               ├── UiState.kt
│               ├── data/
│               │   ├── PosDatabaseHelper.kt
│               │   ├── PosModels.kt
│               │   └── PosRepository.kt
│               └── ui/
│                   ├── PosAppScreen.kt
│                   ├── PosViewModel.kt
│                   └── theme/
```

## Main Components

- `MainActivity.kt` — app entry point and Compose setup
- `BakingScreen.kt` — screen logic for the baking workflow
- `BakingViewModel.kt` — view-model logic for the baking screen
- `PosAppScreen.kt` — main application screen/UI
- `PosViewModel.kt` — app-level state and screen behavior
- `PosRepository.kt` — repository layer for app data access
- `PosDatabaseHelper.kt` — local database helper logic
- `PosModels.kt` — models for POS data structures

## Prerequisites

Before building the project, make sure you have:

- Android Studio
- JDK 11
- Android SDK configured
- Gradle wrapper available in the repo

## Build and Run

Clone the repository and open it in Android Studio:

```bash
git clone https://github.com/ivanKresnha/POS-RF-OVERHAUL.git
cd POS-RF-OVERHAUL
./gradlew assembleDebug
```

To run the app on an emulator or connected device:

```bash
./gradlew installDebug
```

## Notes

This project includes Firebase AI support via Gradle dependencies, so Firebase configuration may be required for full runtime functionality. Ensure the Android project is synced with the proper Firebase and Google services setup in your development environment.

## License

This repository does not currently show a license file in the root. If you plan to publish or distribute it, add an appropriate license before release.
