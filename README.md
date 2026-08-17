# Premium VPN

Android VPN app built on the Outline SDK with key usage monitoring for premium users.

## Features

- Connect to Shadowsocks VPN servers using Outline shared keys (`ss://`)
- Monitor key usage: data used, remaining limit, connection status
- Platform login support for managed key distribution
- Material Design 3 UI with dark mode support

## Architecture

- **Go module** (`go/`) — VPN tunnel logic via Outline SDK, compiled to Android AAR
- **Android app** (`android/`) — Kotlin + Jetpack Compose

## Prerequisites

- Go 1.21+
- Android Studio (Ladybug or later)
- JDK 17+
- Android SDK 34
- gomobile (`go install golang.org/x/mobile/cmd/gomobile@latest`)

## Build

### 1. Generate Go AAR

```bash
cd go/
go mod tidy
gomobile bind -ldflags='-s -w' -target=android -androidapi=21 \
  -o ../android/app/libs/outline.aar .
```

### 2. Build Android App

```bash
cd android/
./gradlew assembleDebug
```

### 3. Install on Device

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure

See [PLAN.md](PLAN.md) for full architecture and phase details.

## Progress

See [PROGRESS.md](PROGRESS.md) for build progress tracking.

## License

Private — All rights reserved.
