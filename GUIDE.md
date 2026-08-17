# Premium VPN — Build & Install Guide

This guide walks you through setting up your development environment, building the native Go VPN library, compiling the Android application, and installing it on your device.

## Prerequisites

Ensure you have the following installed on your machine:

1.  **Git**: [Download Git](https://git-scm.com/downloads)
2.  **Go 1.21+**: [Download Go](https://go.dev/doc/install)
3.  **Android Studio**: [Download Android Studio](https://developer.android.com/studio)
    *   Install the **Android SDK** (API 34+ recommended).
    *   Install the **Android NDK** (Required for Go native builds).
4.  **ADB (Android Debug Bridge)**: Included with Android Studio / Platform Tools.

---

## 1. Clone the Repository

```bash
git clone https://github.com/tinghah/premium-vpn.git
cd premium-vpn
```

## 2. Build the Go VPN Bridge (AAR)

The project requires a native Go library to handle the VPN tunnel.

```bash
# 1. Setup Gomobile
go install golang.org/x/mobile/cmd/gomobile@latest
go install golang.org/x/mobile/cmd/gobind@latest
gomobile init

# 2. Generate the Android Archive (AAR)
cd go/
go mod tidy
# Note: Ensure ANDROID_NDK_HOME is set in your environment
gomobile bind -ldflags='-s -w' -target=android -androidapi=21 \
  -o ../android/app/libs/outline.aar .
cd ..
```

## 3. Build the Android App

Use Gradle to compile the Android application.

```bash
cd android/
# Build the Debug APK
./gradlew assembleDebug
```

The APK will be located at:
`android/app/build/outputs/apk/debug/app-debug.apk`

## 4. Install via ADB

1.  Enable **Developer Options** and **USB Debugging** on your Android device.
2.  Connect your device to your computer.
3.  Run the following command to install the app:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Troubleshooting

*   **Go/NDK issues**: If `gomobile` fails, ensure `ANDROID_HOME` and `ANDROID_NDK_HOME` are set in your shell environment pointing to your local Android SDK/NDK paths.
*   **Gradle sync errors**: Open the `android/` folder in Android Studio; it will automatically download necessary Gradle dependencies and configure the project.
*   **ADB not found**: Ensure `platform-tools` is in your system PATH.
