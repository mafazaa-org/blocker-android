# Ainaa Android


<img width="846" height="309" alt="شعار عينا احمر" src="https://github.com/user-attachments/assets/5f504e5c-a6f6-4262-b4f6-d54f36a69d88" />

---

<div align="start" markdown="1">

[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-blue?logo=kotlin)](https://kotlinlang.org/)
&#160;
[![Android](https://img.shields.io/badge/Platform-Android-brightgreen?logo=android)](https://developer.android.com/)
&#160;
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20+%20Clean%20Architecture-lightgrey)]()
&#160;
[![Event](https://img.shields.io/badge/Built%20For-Mafaza-black?logo=github)](https://ainaa.mafazaa.com/)
&#160;
[![Contributions](https://img.shields.io/badge/Contributions-Welcome-orange)]()

[![Build & Upload Debug to Firebase App Distribution](https://github.com/mafazaa-org/Ainaa-android/actions/workflows/firebase-dist.yml/badge.svg?branch=feature%2Fgh-action)](https://github.com/mafazaa-org/Ainaa-android/actions/workflows/firebase-dist.yml)


</div>

Ainaa is an Android application built with Kotlin, following MVVM architecture and using dependency injection. It provides advanced features such as VPN, accessibility services, overlays, and integrates with Firebase via Google Services.

## Table of Contents
- [Project Structure](#project-structure)
- [Features](#features)
- [Requirements](#requirements)
- [Setup](#setup)
- [Folder Overview](#folder-overview)
- [Source Code Overview](#source-code-overview)
- [Contributing](#contributing)
- [License](#license)

---

## Project Structure

```
Ainaa-android/
├── app/                  # Main Android application module
│   ├── build.gradle.kts  # App-level Gradle config
│   ├── google-services.json # Required for Firebase
│   ├── proguard-rules.pro   # ProGuard config
│   └── src/             # Source code and resources
├── build.gradle.kts      # Project-level Gradle config
├── gradle/               # Gradle wrapper and version catalog
├── gradlew, gradlew.bat  # Gradle wrapper scripts
├── README.md             # Project documentation
├── settings.gradle.kts   # Gradle settings
└── ...                   # Other config and build files
```

---

## Features

- VPN service and network protection
- Accessibility overlays and custom UI components
- Daily update notifications
- Data management with local and remote sources
- Firebase integration (requires `google-services.json`)
- MVVM architecture and dependency injection

---

## Requirements

- **Android Studio** (latest recommended)
- **Google Services**: Place a valid `google-services.json` in `app/`
- **Internet Access**: Required for some features

---

## Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/Ainaa-android.git
   cd Ainaa-android
   ```

2. **Open in Android Studio.**

3. **Add `google-services.json` to the `app/` folder.**
   - Obtain this file from your Firebase project.

4. **Build and run the app.**

---

## Folder Overview

- **app/**: Main application code and resources.
  - **build/**: Build outputs.
  - **src/**: Source code and resources.
    - **main/**: Main source set.
      - **java/com/mafazaa/ainaa/**: Core Kotlin source code.
      - **res/**: App resources (layouts, drawables, etc.)
      - **AndroidManifest.xml**: App manifest.
    - **test/**, **androidTest/**: Unit and instrumentation tests.
- **gradle/**: Gradle version catalog and wrapper.
- **build/**: Project build outputs.

---

## Source Code Overview

### Main Files (`app/src/main/java/com/mafazaa/ainaa/`)

- **AndroidDrawablePainter.kt**: Custom drawable painting logic.
- **AppActivity.kt**: Main activity, entry point for the app UI.
- **AppViewModel.kt**: ViewModel for managing app state and logic.
- **Constants.kt**: Constant values.
- **ContextUtils.kt**: Context utilities.
- **Lg.kt**: Logging utilities.
- **MyApp.kt**: Application class.
- **PermissionState.kt**: Permission state management.

### Folders

- **data/**: Data layer (repositories, sources, utilities).
- **di/**: Dependency injection setup.
- **model/**: Data models and DTOs.
- **receiver/**: Broadcast receivers.
- **service/**: App services and background workers.
- **ui/**: User interface components.

See the in-file comments and documentation for details on each class and module.

---

## Contributing

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/YourFeature`).
3. Commit your changes.
4. Push to the branch.
5. Open a pull request.

