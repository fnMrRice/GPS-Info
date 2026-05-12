# GPS Info

Android app for displaying GPS/location information, built with Kotlin and Jetpack Compose.

## Tech Stack
- Kotlin, Jetpack Compose, Material 3
- Adaptive Navigation Suite (phone/tablet/desktop)
- Gradle 9.0.0-alpha06, AGP 9.0.0-alpha06
- compileSdk 36, minSdk 24, targetSdk 36

## Project Structure
- `app/src/main/java/cn/fnrice/gpsinfo/` - main source
- `app/src/main/java/cn/fnrice/gpsinfo/ui/theme/` - Compose theme (Color, Theme, Type)
- Package: `cn.fnrice.gpsinfo`

## Build & Run
```bash
./gradlew assembleDebug        # Debug APK
./gradlew installDebug         # Install on device
./gradlew test                 # Unit tests
./gradlew connectedAndroidTest # Instrumented tests
```

## Conventions
- Chinese comments in code are acceptable
- Use Material 3 components and adaptive layouts
