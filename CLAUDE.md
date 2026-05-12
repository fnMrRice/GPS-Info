# GPS Info

Android app for displaying satellite positioning data from the device's GNSS hardware. Shows a list of visible satellites with their details (constellation type, signal strength, elevation, azimuth, etc.), along with current location information.

## Core Features
- **Satellite List**: Display all visible GNSS satellites (GPS, GLONASS, Galileo, BeiDou, QZSS, SBAS, IRNSS) with real-time data
- **Satellite Details**: Per-satellite info — PRN, constellation type, C/N0 (signal-to-noise ratio), elevation, azimuth, used in fix flag
- **Location Info**: Current latitude, longitude, altitude, speed, bearing, accuracy
- **GNSS Status**: Number of satellites total, satellites used in fix, time to first fix

## Key Android APIs
- `LocationManager` — request location updates, get GPS provider status
- `GnssStatus.Callback` — receive satellite list updates (replaces deprecated `GpsStatus`)
- `GnssMeasurementsEvent.Callback` — raw GNSS measurements (requires `ACCESS_FINE_LOCATION`)
- `OnNmeaMessageListener` — NMEA sentence data
- `LocationManager.getGnssCapabilities()` — device GNSS hardware capabilities (Android 12+)
- Permissions: `ACCESS_FINE_LOCATION` (mandatory), `ACCESS_COARSE_LOCATION` (fallback)

## Navigation Structure
- **Home**: Real-time satellite sky view / signal overview
- **Favorites**: Saved locations or satellite snapshots
- **Profile**: App settings, GNSS capabilities display

## Tech Stack
- Kotlin, Jetpack Compose, Material 3
- Adaptive Navigation Suite (phone/tablet/desktop)
- Gradle 9.0.0-alpha06, AGP 9.0.0-alpha06
- compileSdk 36, minSdk 24, targetSdk 36

## Project Structure
- `app/src/main/java/cn/fnrice/gpsinfo/` — main source
- `app/src/main/java/cn/fnrice/gpsinfo/ui/theme/` — Compose theme (Color, Theme, Type)
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
- Satellite data should update in real-time when GPS is active
- Handle permission denials gracefully with explanatory UI
- Support both `GnssStatus` API (API 24+) and gracefully degrade for older devices
