# Premium VPN — Progress Tracker

> Each phase has checkboxes. Coding agents: check off items as you complete them.

## Status Legend

- [ ] Not started
- [-] In progress
- [x] Complete

---

## Phase 1: Go VPN Bridge

- [ ] Initialize Go module (`go mod init premium-vpn-go`)
- [ ] Add Outline SDK dependency
- [ ] Implement `StartTunnel(localPort, serverAddr, password, method)` function
- [ ] Implement `StopTunnel()` function
- [ ] Implement `IsConnected()` function
- [ ] Implement `GetBytesTransferred()` function
- [ ] Implement `GetDuration()` function
- [ ] Add `golang.org/x/mobile` dependency
- [ ] Test `gomobile bind` generates AAR successfully
- [ ] Verify AAR loads in Android project
- [ ] Verify tunnel starts/stops from Android

**Phase 1 Status:** `NOT STARTED`

---

## Phase 2: Android VPN Service

- [ ] Create `VpnService.kt` extending `android.net.VpnService`
- [ ] Implement `VpnService.Builder` TUN interface creation
- [ ] Implement file descriptor passing to Go native code
- [ ] Create foreground service with notification
- [ ] Create `GoVpnBridge.kt` JNI bridge
- [ ] Add VPN permissions to `AndroidManifest.xml`
- [ ] Implement connection state management
- [ ] Implement reconnection on network change
- [ ] Add `BOOT_COMPLETED` receiver for auto-connect
- [ ] Test VPN connects on physical device
- [ ] Test VPN survives background/foreground cycle

**Phase 2 Status:** `NOT STARTED`

---

## Phase 3: Key Management & Storage

- [ ] Create `KeyEntity.kt` Room entity
- [ ] Create `KeyDao.kt` DAO interface
- [ ] Create `AppDatabase.kt` Room database
- [ ] Implement `KeyParser.kt` — parse `ss://` URLs
- [ ] Handle Base64-encoded credentials
- [ ] Handle plain-text credentials
- [ ] Extract server API secret from URL
- [ ] Create `KeyRepository.kt`
- [ ] Implement `addKey()` with validation
- [ ] Implement `deleteKey()`
- [ ] Implement `setActiveKey()`
- [ ] Implement `getAllKeys()` as Flow
- [ ] Write unit tests for key parser
- [ ] Write unit tests for repository

**Phase 3 Status:** `NOT STARTED`

---

## Phase 4: Outline Server API Client

- [ ] Create `OutlineApiService.kt` Retrofit interface
- [ ] Define `GET /server` endpoint
- [ ] Define `GET /access-keys` endpoint
- [ ] Define `GET /experimental/server/metrics` endpoint
- [ ] Create `ServerInfo` data class
- [ ] Create `AccessKey` data class
- [ ] Create `ServerMetrics` data class
- [ ] Create `AccessKeyMetrics` data class
- [ ] Create `ConnectionInfo` data class
- [ ] Implement `StatsRepository.kt`
- [ ] Add periodic stats refresh (30s interval)
- [ ] Add pull-to-refresh support
- [ ] Cache stats locally for offline display
- [ ] Handle server unreachable gracefully
- [ ] Write MockWebServer tests

**Phase 4 Status:** `NOT STARTED`

---

## Phase 5: UI Screens

- [ ] Set up Material 3 theme
- [ ] Create navigation graph
- [ ] Build `HomeScreen.kt` — main dashboard
- [ ] Build `KeyInputScreen.kt` — paste ss:// key
- [ ] Build `StatsScreen.kt` — per-key usage details
- [ ] Build `LoginScreen.kt` — platform auth
- [ ] Build `KeyListSection` component
- [ ] Build `ConnectionStatusCard` component
- [ ] Build `UsageProgressBar` component
- [ ] Build `DataUsageCard` component
- [ ] Add empty states for all screens
- [ ] Add loading states for all screens
- [ ] Add error states for all screens
- [ ] Add pull-to-refresh on stats
- [ ] Add QR code scanning (optional)

**Phase 5 Status:** `NOT STARTED`

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
| Phase 1: Go VPN Bridge | NOT STARTED | 0% |
| Phase 2: Android VPN Service | NOT STARTED | 0% |
| Phase 3: Key Management | NOT STARTED | 0% |
| Phase 4: Server API Client | NOT STARTED | 0% |
| Phase 5: UI Screens | NOT STARTED | 0% |
| Phase 6: Integration | NOT STARTED | 0% |
| Phase 7: Production | NOT STARTED | 0% |

**Overall: 0% complete**
