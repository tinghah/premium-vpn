# Premium VPN — Progress Tracker

> Each phase has checkboxes. Coding agents: check off items as you complete them.

## Status Legend

- [ ] Not started
- [-] In progress
- [x] Complete

---

## Phase 1: Go VPN Bridge

- [x] Initialize Go module (`go mod init premium-vpn-go`)
- [x] Add Outline SDK dependency (go.mod created, needs `go mod tidy` with Go installed)
- [x] Implement `StartTunnel(localPort, serverAddr, password, method)` function
- [x] Implement `StopTunnel()` function
- [x] Implement `IsConnected()` function
- [x] Implement `GetBytesTransferred()` function
- [x] Implement `GetDuration()` function
- [x] Add `golang.org/x/mobile` dependency
- [ ] Test `gomobile bind` generates AAR successfully
- [ ] Verify AAR loads in Android project
- [ ] Verify tunnel starts/stops from Android

**Phase 1 Status:** `IN PROGRESS` — Go code written, needs Go toolchain to build AAR

---

## Phase 2: Android VPN Service

- [x] Create `VpnService.kt` extending `android.net.VpnService`
- [x] Implement `VpnService.Builder` TUN interface creation
- [x] Implement file descriptor passing to Go native code (GoVpnBridge)
- [x] Create foreground service with notification
- [x] Create `GoVpnBridge.kt` JNI bridge
- [x] Add VPN permissions to `AndroidManifest.xml`
- [x] Implement connection state management
- [ ] Implement reconnection on network change
- [x] Add `BOOT_COMPLETED` receiver for auto-connect
- [ ] Test VPN connects on physical device
- [ ] Test VPN survives background/foreground cycle

**Phase 2 Status:** `IN PROGRESS` — VPN Service wired to GoVpnBridge, ready for AAR testing

---

## Phase 3: Key Management & Storage

- [x] Create `KeyEntity.kt` Room entity
- [x] Create `KeyDao.kt` DAO interface
- [x] Create `AppDatabase.kt` Room database
- [x] Implement `KeyParser.kt` — parse `ss://` URLs
- [x] Handle Base64-encoded credentials
- [x] Handle plain-text credentials
- [ ] Extract server API secret from URL
- [x] Create `KeyRepository.kt`
- [x] Implement `addKey()` with validation
- [x] Implement `deleteKey()`
- [x] Implement `setActiveKey()`
- [x] Implement `getAllKeys()` as Flow
- [x] Write unit tests for key parser
- [ ] Write unit tests for repository

**Phase 3 Status:** `COMPLETE` — Core implementation done, tests written

---

## Phase 4: Outline Server API Client

- [x] Create `OutlineApiService.kt` Retrofit interface
- [x] Define `GET /server` endpoint
- [x] Define `GET /access-keys` endpoint
- [x] Define `GET /experimental/server/metrics` endpoint
- [x] Create `ServerInfo` data class
- [x] Create `AccessKey` data class
- [x] Create `ServerMetrics` data class
- [x] Create `AccessKeyMetrics` data class
- [x] Create `ConnectionInfo` data class
- [x] Implement `StatsRepository.kt`
- [x] Add periodic stats refresh (30s interval)
- [ ] Add pull-to-refresh support
- [ ] Cache stats locally for offline display
- [ ] Handle server unreachable gracefully
- [ ] Write MockWebServer tests

**Phase 4 Status:** `IN PROGRESS` — API client and models done, refresh/caching needed

---

## Phase 5: UI Screens

- [x] Set up Material 3 theme
- [x] Create navigation graph
- [x] Build `HomeScreen.kt` — main dashboard
- [x] Build `KeyInputScreen.kt` — paste ss:// key
- [x] Build `StatsScreen.kt` — per-key usage details
- [x] Build `LoginScreen.kt` — platform auth
- [ ] Build `KeyListSection` component
- [x] Build `ConnectionStatusCard` component
- [ ] Build `UsageProgressBar` component
- [ ] Build `DataUsageCard` component
- [ ] Add empty states for all screens
- [ ] Add loading states for all screens
- [ ] Add error states for all screens
- [ ] Add pull-to-refresh on stats
- [ ] Add QR code scanning (optional)

**Phase 5 Status:** `IN PROGRESS` — Main screens done, components and polish needed

---

## Phase 6: Integration & Polish

- [ ] Wire VPN Service state → Compose UI via StateFlow
- [ ] Wire UI connect/disconnect → VPN Service
- [ ] Wire API client → Stats display
- [ ] Implement 30s periodic stats refresh
- [ ] Test background/foreground transitions
- [ ] Test network change reconnection
- [ ] Test multiple key switching
- [ ] Test on Android 8 (API 26)
- [ ] Test on Android 12 (API 31)
- [ ] Test on Android 14 (API 34)
- [ ] Run lint checks
- [ ] Run type checks

**Phase 6 Status:** `NOT STARTED`

---

## Phase 7: Production Readiness

- [ ] Enable ProGuard/R8 obfuscation
- [ ] Add certificate pinning for backend API
- [ ] Audit key storage security
- [ ] Add adaptive refresh rate for battery
- [ ] Profile memory usage
- [ ] Profile battery impact
- [ ] Generate signed AAB
- [ ] Write privacy policy
- [ ] Prepare Play Store listing
- [ ] Complete Data Safety form

**Phase 7 Status:** `NOT STARTED`

---

## Overall Progress

| Phase | Status | Completion |
|-------|--------|------------|
| Phase 1: Go VPN Bridge | IN PROGRESS | 70% |
| Phase 2: Android VPN Service | IN PROGRESS | 80% |
| Phase 3: Key Management | COMPLETE | 90% |
| Phase 4: Server API Client | IN PROGRESS | 70% |
| Phase 5: UI Screens | IN PROGRESS | 70% |
| Phase 6: Integration | NOT STARTED | 0% |
| Phase 7: Production | NOT STARTED | 0% |

**Overall: ~54% complete** — Core implementations done, integration and polish needed
