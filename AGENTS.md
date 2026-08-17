# AGENTS.md — Coding Agent Instructions

> This file tells coding agents (opencode, Cursor, Copilot, etc.) how to work on this project.

---

## Project Overview

**Premium VPN** is an Android VPN app built on the Outline SDK. It lets users connect to Shadowsocks VPN servers and monitor their key usage (data used, remaining limit, connection status).

## Architecture

- **Go module** (`go/`) — VPN tunnel logic, compiled to Android AAR via gomobile
- **Android app** (`android/`) — Kotlin + Jetpack Compose UI, uses the Go AAR

## Key Repositories & References

| Resource | URL |
|----------|-----|
| Outline SDK | https://github.com/OutlineFoundation/outline-sdk |
| Outline Apps (reference client) | https://github.com/OutlineFoundation/outline-apps |
| MobileProxy pre-built AAR | https://github.com/OutlineFoundation/mobileproxylib |
| Outline Server API spec | https://github.com/OutlineFoundation/outline-server/blob/master/src/shadowbox/server/api.yml |
| Go Mobile docs | https://pkg.go.dev/golang.org/x/mobile/cmd/gomobile |

## Build Commands

```bash
# Go AAR (from go/ directory)
gomobile bind -ldflags='-s -w' -target=android -androidapi=21 \
  -o ../android/app/libs/outline.aar .

# Android debug build (from android/ directory)
./gradlew assembleDebug

# Android release build
./gradlew bundleRelease

# Lint
./gradlew lint

# Type check (if using ktlint)
./gradlew ktlintCheck
```

## Code Style

### Kotlin
- Follow Kotlin coding conventions
- Use Jetpack Compose for all UI
- Use Hilt for dependency injection
- Use Coroutines + Flow for async operations
- Use Room for local database
- Use Retrofit for networking

### Go
- Follow standard Go conventions (`gofmt`, `go vet`)
- Export only functions needed for gomobile binding
- Keep the public API minimal (StartTunnel, StopTunnel, IsConnected, GetBytesTransferred, GetDuration)
- Use build tags if needed for platform-specific code

## File Ownership by Phase

| Phase | Owner | Key Files |
|-------|-------|-----------|
| Phase 1 | Go agent | `go/*.go`, `go/go.mod` |
| Phase 2 | Android agent | `android/.../service/VpnService.kt`, `GoVpnBridge.kt` |
| Phase 3 | Android agent | `android/.../data/local/*`, `KeyParser.kt` |
| Phase 4 | Android agent | `android/.../data/remote/*`, `StatsRepository.kt` |
| Phase 5 | UI agent | `android/.../ui/**/*` |
| Phase 6 | Integration agent | All files — wiring |
| Phase 7 | DevOps agent | Build config, ProGuard, signing |

## Testing

- **Unit tests**: `android/app/src/test/` — key parser, repository, API models
- **Integration tests**: `android/app/src/androidTest/` — Room DB, API calls
- **Go tests**: `go/*_test.go` — tunnel logic, stats

## Common Issues

1. **`gomobile bind` fails**: Ensure Go 1.21+, Android NDK installed, `ANDROID_HOME` set
2. **VPN won't connect**: Check VPN permission granted, VPN notification visible
3. **Stats not loading**: Check server API URL is correct, network accessible
4. **Build fails**: Run `./gradlew clean` then rebuild

## Important Notes

- The Outline Server API requires the **secret path** in the URL (e.g., `https://server.com/SECRET_PATH/`)
- The `ss://` URL format can be Base64-encoded or plain-text — parser must handle both
- VPN apps require a **privacy policy** for Play Store
- Test on real devices — VPN behavior differs on emulators
