# GPS Info

English | [中文](README_zh.md)

A real-time GNSS satellite positioning viewer for Android. Visualize visible satellites on a sky plot, inspect signal strength per constellation, and monitor device sensors — all in a Material 3 adaptive UI.

## Features

- **Satellite Sky View** — polar plot overlay on a map showing satellite positions, color-coded by constellation (GPS, GLONASS, Galileo, BeiDou, QZSS, SBAS, NavIC)
- **Real-time Satellite List** — PRN, C/N0, elevation, azimuth, carrier frequency, used-in-fix status; multi-frequency signals (L1/L5/E5) grouped per satellite
- **Location Card** — latitude, longitude, altitude, speed, bearing, accuracy with GPS/Network/Last Known source indicator
- **Sensor Dashboard** — orientation, motion (accelerometer, gyroscope, gravity), and environment (magnetic field, light, proximity, pressure)
- **Device Info & GNSS Capabilities** — manufacturer, model, Android version, hardware GNSS feature flags (Android 12+)
- **Adaptive Navigation** — bottom bar on phones, navigation rail on tablets, drawer on desktop via `NavigationSuiteScaffold`
- **Multiple Map Providers** — Google Maps, AMap (Gaode), Baidu Maps with auto-detection based on connectivity
- **Dark Theme & Dynamic Colors** — Material You theming on Android 12+, dark mode support with custom map styles
- **Localization** — English and Simplified Chinese

## Screenshots

> _Add screenshots here_

## Requirements

- Android 7.0 (API 24) or higher
- `ACCESS_FINE_LOCATION` permission (required for GNSS data)
- A map API key for at least one provider (Google Maps, AMap, or Baidu Maps)

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/fnrice/GPS-Info.git
cd GPS-Info
```

### 2. Configure API keys

Map SDK API keys are injected via `gradle.properties` or `local.properties`:

```properties
GOOGLE_MAPS_KEY=your_google_maps_api_key
AMAP_KEY=your_amap_api_key
BAIDU_MAPS_KEY=your_baidu_maps_api_key
```

See [`docs/api_keys.md`](docs/api_keys.md) (Chinese) for detailed instructions on obtaining keys for each provider.

### 3. Build and run

```bash
./gradlew installDebug
```

Or build the APK directly:

```bash
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### 4. Run tests

```bash
./gradlew test                 # Unit tests
./gradlew connectedAndroidTest # Instrumented tests
```

## Project Structure

```
app/src/main/java/cn/fnrice/gpsinfo/
  MainActivity.kt              -- Entry point, navigation host, permission handling
  data/
    GnssModels.kt              -- SatelliteInfo, SatelliteGroup, LocationInfo, GnssState
    SettingsRepository.kt      -- DataStore preferences
    MockDataProvider.kt        -- Simulated data for testing/development
  viewmodel/
    GnssViewModel.kt           -- GNSS, sensor, and settings state management
  ui/
    screen/
      HomeScreen.kt            -- Satellite list + sky view
      SensorScreen.kt          -- Hardware sensor readings
      DeviceScreen.kt          -- Device info, capabilities, settings
    components/
      SkyView.kt               -- Canvas-based polar sky plot
      MapComponents.kt         -- AMap and Google Maps integration
      SatelliteCard.kt         -- Expandable satellite detail card
      ...
    theme/                     -- Material 3 theme, colors, typography
```

## Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | NavigationSuiteScaffold (adaptive) |
| State | Kotlin StateFlow |
| Persistence | DataStore Preferences |
| Maps | Google Maps Compose, AMap 3D SDK |
| Build | Gradle 9.5.1 (Kotlin DSL), AGP 9.2.1 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 37 |

## Developer Mode

Tap the app version in the Device screen 7 times to unlock developer options:

- **Custom API keys** — override per-provider map keys with test buttons
- **Mock Data Mode** — simulated satellite data centered on Beijing for testing without GPS signal
- **Log Viewer** — system log with auto-scroll and clear

## License

See [LICENSE](LICENSE) for details.
