# CodeLens

> Yet another code scanner for Android. Simple, fast, and ad-free.

CodeLens is a minimalist QR & Barcode scanner built with modern Android technologies. It features a unique hybrid scanning engine that dynamically chooses between Google Play Services (GMS) and a local CameraX implementation to ensure the best experience on any device.

## ✨ Features

- **Hybrid Engine**:
  - **GMS Mode**: leverages the [Google Play Services Code Scanner](https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner) for instant, permission-less scanning (zero camera permission required usually).
  - **Offline Mode**: Seamlessly falls back to **CameraX + ML Kit** (bundled) for devices without GMS or offline environments.
- **Quick Settings Tile**: Launch the scanner instantly from your system's Quick Settings panel (Notification shade).
- **Smart Actions**:
  - Automatically detects URLs and opens them in your default browser.
  - Copies plain text to clipboard.
  - Supports Magnet links and custom URI schemes.
- **Privacy Focused**: No internet permission required for core functionality.
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
   git clone https://github.com/RoyRao2333/code-lens.git
   cd code-lens
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

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## 📄 License

[BSD 3-Clause](LICENSE)