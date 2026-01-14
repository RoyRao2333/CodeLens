# CodeLens

> Yet another code scanner for Android. Simple, fast, and ad-free.

CodeLens is a minimalist QR & Barcode scanner built with modern Android technologies. It offers flexibility by supporting both a standard in-app camera scanner and a lightweight Google Play Services (GMS) scanner.

## ✨ Features

- **Dual Scanning Modes**:
  - **CameraX Mode (Default)**: Uses the device camera with [ML Kit](https://developers.google.com/ml-kit/vision/barcode-scanning) for fast, offline scanning within the app. Requires camera permission.
  - **GMS Quick Scan**: Leverages the [Google Play Services Code Scanner](https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner) for instant, installation-free scanning without requiring camera permissions in the app itself.
- **Quick Settings Tiles**:
  - **Scan (扫一扫)**: Launches the default in-app CameraX scanner.
  - **Quick Scan (快速扫描)**: Directly launches the GMS barcode scanner overlay for rapid capture.
- **Smart Actions**:
  - Automatically detects URLs and opens them in your default browser.
  - Copies plain text to clipboard.
  - Supports Magnet links and custom URI schemes.
- **Privacy Focused**: Core logic runs entirely on-device. Uses system capabilities where possible.
- **Modern UI**: Built entirely with Jetpack Compose and Material 3.

## 🛠️ Technology Stack

- **Language**: Kotlin
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetbrains/compose) (Material 3)
- **Camera Infrastructure**: [CameraX](https://developer.android.com/training/camerax) (with specialized `ImageAnalysis` for ML Kit)
- **Machine Learning**: 
  - `com.google.android.gms:play-services-code-scanner`
  - `com.google.mlkit:barcode-scanning`
- **Build System**: Gradle Kotlin DSL (`.kts`)

## 🏗️ Build from Source

### Prerequisites
- Android Studio Hedgehog (or newer)
- JDK 17
- Android SDK API 34 (Build Tools)
- **Minimum Device Support**: Android 7.0 (API 24)

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/RoyRao2333/CodeLens.git
   cd CodeLens
   ```

2. Open the project in Android Studio.

3. Sync Gradle dependencies:
   ```bash
   ./gradlew clean build
   ```

4. Run on a connected device or emulator:
   ```bash
   ./gradlew installDebug
   ```


## 🔐 Signing & Release

To generate a signed release APK, you need a keystore. We provide a helper script to set this up quickly for local development.

### 1. Setup Signing Config
Run the setup script to generate a local keystore (`release-key.jks`) and configuration file (`keystore.properties`):
```bash
./setup-signing.sh
```
*Note: Your secrets are stored in `keystore.properties` which is git-ignored to protect your privacy.*

### 2. Build Release APK
```bash
./gradlew assembleRelease
```

### 3. Output Location
The signed APK will be generated at:
`app/build/outputs/apk/release/app-release.apk`

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## 📄 License

[BSD 3-Clause](LICENSE)