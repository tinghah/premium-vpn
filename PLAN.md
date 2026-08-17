# Premium VPN — Build Plan

> Android VPN app built on the Outline SDK with key usage monitoring for premium users.

## Overview

Build a **native Android VPN app** (Kotlin + Jetpack Compose) that:
- Connects to Outline/Shadowsocks VPN servers using the Outline SDK
- Lets users paste `ss://` shared keys OR login via your platform to get keys
- Shows basic key usage stats: data used, remaining limit, connection status, last active
- Uses **gomobile bind** to generate an Android AAR from a Go wrapper around the Outline SDK

---

## Architecture

```
┌─────────────────────────────────────────────────┐
│                 Android App                      │
│  ┌─────────────┐  ┌──────────────────────────┐  │
│  │  Jetpack     │  │  VPN Foreground Service   │  │
│  │  Compose UI  │  │  ┌────────────────────┐  │  │
│  │             │──│──│ Go Native (AAR)     │  │  │
│  │  • Key Mgmt │  │  │ • Shadowsocks       │  │  │
│  │  • Stats    │  │  │ • Tun2socks         │  │  │
│  │  • Connect  │  │  │ • Traffic Stats     │  │  │
│  └─────────────┘  │  └────────────────────┘  │  │
│                   └──────────────────────────┘  │
│  ┌─────────────┐  ┌──────────────────────────┐  │
│  │  Room DB    │  │  Outline Server API       │  │
│  │  (Keys)     │  │  Client (Retrofit)        │  │
│  └─────────────┘  │  • /server                │  │
│                   │  • /access-keys           │  │
│                   │  • /experimental/metrics  │  │
│                   └──────────────────────────┘  │
└─────────────────────────────────────────────────┘
         │                          │
         │ ss://key                 │ REST API
         ▼                          ▼
┌─────────────────┐     ┌─────────────────────┐
│ Outline Server   │◄────│ Your Backend (opt.)  │
│ (Shadowsocks)   │     │ Auth + Key Assign    │
└─────────────────┘     └─────────────────────┘
```

---

## Technology Stack

| Layer | Technology | Why |
|-------|-----------|-----|
| **UI** | Kotlin + Jetpack Compose | Modern, declarative, Google-recommended |
| **VPN Service** | Android `VpnService` + foreground service | Required for system-wide VPN on Android |
| **VPN Tunnel** | Go (Outline SDK) via gomobile bind | Reuses battle-tested Outline shadowsocks + tun2socks |
| **Networking** | Retrofit + OkHttp | Type-safe HTTP client for server API |
| **Local Storage** | Room Database | Structured storage for access keys |
| **DI** | Hilt (Dagger) | Standard Android DI |
| **Async** | Kotlin Coroutines + Flow | Reactive data streams |
| **Build** | Gradle (Kotlin DSL) + Go modules | Standard Android build + Go cross-compilation |

---

## Project Structure

```
premium-vpn/
├── PLAN.md                          # This file
├── PROGRESS.md                      # Phase progress tracking
├── AGENTS.md                        # Coding agent instructions
├── go/                              # Go module (generates AAR)
│   ├── go.mod
│   ├── go.sum
│   ├── main.go                      # VPN tunnel logic, exports for gomobile
│   ├── shadowsocks.go               # SS connection management
│   └── stats.go                     # Traffic stats collection
├── android/                         # Android app
│   ├── app/
│   │   ├── build.gradle.kts
│   │   ├── libs/                    # Generated AAR goes here
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/premiumvpn/app/
│   │       │   ├── App.kt
│   │       │   ├── di/                    # Hilt modules
│   │       │   ├── ui/
│   │       │   │   ├── theme/
│   │       │   │   ├── navigation/
│   │       │   │   ├── screens/
│   │       │   │   │   ├── HomeScreen.kt
│   │       │   │   │   ├── KeyInputScreen.kt
│   │       │   │   │   ├── StatsScreen.kt
│   │       │   │   │   └── LoginScreen.kt
│   │       │   │   └── components/
│   │       │   ├── data/
│   │       │   │   ├── local/
│   │       │   │   │   ├── AppDatabase.kt
│   │       │   │   │   ├── KeyDao.kt
│   │       │   │   │   └── KeyEntity.kt
│   │       │   │   ├── remote/
│   │       │   │   │   ├── OutlineApiService.kt
│   │       │   │   │   ├── AuthApiService.kt
│   │       │   │   │   └── dto/
│   │       │   │   └── repository/
│   │       │   │       ├── KeyRepository.kt
│   │       │   │       └── StatsRepository.kt
│   │       │   ├── domain/
│   │       │   │   ├── model/
│   │       │   │   └── usecase/
│   │       │   ├── service/
│   │       │   │   ├── VpnService.kt
│   │       │   │   └── GoVpnBridge.kt
│   │       │   └── util/
│   │       │       ├── KeyParser.kt
│   │       │       └── Formatter.kt
│   │       └── res/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle/
└── README.md
```

---

## Phase 1: Go VPN Bridge

### Goal
Create a Go package that wraps the Outline SDK and generates an Android AAR.

### What to Build

**`go/main.go`** — Exports these functions for gomobile:

```go
// StartTunnel creates a Shadowsocks tunnel and routes traffic through TUN
func StartTunnel(localPort int, serverAddr, password, method string) error

// StopTunnel gracefully stops the tunnel
func StopTunnel()

// IsConnected returns connection status
func IsConnected() bool

// GetBytesTransferred returns total bytes (up+down)
func GetBytesTransferred() int64

// GetDuration returns connection duration in seconds
func GetDuration() int64
```

### Build Steps

```bash
# 1. Install gomobile
go install golang.org/x/mobile/cmd/gomobile@latest
go install golang.org/x/mobile/cmd/gobind@latest
gomobile init

# 2. Build Android AAR
cd go/
gomobile bind -ldflags='-s -w' \
  -target=android \
  -androidapi=21 \
  -o ../android/app/libs/outline.aar \
  .
```

### Key Dependencies (go.mod)

```go
module premium-vpn-go

go 1.21

require (
    golang.getoutline.org/sdk v0.0.0
    golang.org/x/mobile v0.0.0
)
```

### Deliverables
- [ ] Go package compiles with `gomobile bind`
- [ ] Generated `outline.aar` + `outline-sources.jar`
- [ ] Start/stop tunnel works from Android test harness
- [ ] Traffic stats reporting works

---

## Phase 2: Android VPN Service

### Goal
Implement the Android `VpnService` that uses the Go AAR to route all device traffic through Shadowsocks.

### Core Components

**`VpnService.kt`** — Extends `android.net.VpnService`:
- Creates TUN interface via `VpnService.Builder`
- Allocates UDP and TCP file descriptors
- Passes FDs to Go native code
- Runs as foreground service with persistent notification
- Handles reconnection on network changes

**`GoVpnBridge.kt`** — JNI bridge:
- Loads `outline.aar` native library
- Calls Go exported functions
- Handles callbacks for stats updates

### Android Manifest Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<service
    android:name=".service.VpnService"
    android:foregroundServiceType="specialUse"
    android:exported="false" />
```

### VPN Connection Flow

```
User taps Connect
    → VpnService.establish() creates TUN
    → Go code receives TUN FD
    → Go creates Shadowsocks connection to server
    → Go creates tun2socks pipeline (TUN → SS → Internet)
    → All device traffic now routes through SS tunnel
    → Stats collected in background
```

### Deliverables
- [ ] VPN connects and routes traffic
- [ ] Notification shows connection status
- [ ] Reconnects on network change
- [ ] Kill switch (optional, for Phase 6)

---

## Phase 3: Key Management & Storage

### Goal
Parse `ss://` shared keys, store them in Room DB, support multiple keys.

### Key Parser (`KeyParser.kt`)

```kotlin
// Input:  ss://YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=@example.com:8388/?outline=1
// Output: AccessKey(
//   password = "abcdefghijklmnopqrstuvwxyz123456",
//   host = "example.com",
//   port = 8388,
//   method = "aes-256-gcm",
//   name = "My VPN Key",
//   serverApiUrl = "https://example.com/secretPath"
// )
```

### Room Database Schema

```kotlin
@Entity(tableName = "access_keys")
data class KeyEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val password: String,
    val host: String,
    val port: Int,
    val method: String = "aes-256-gcm",
    val accessKeyUrl: String,          // full ss:// URL
    val serverApiSecret: String?,       // secret path for server API
    val dataLimitBytes: Long?,          // per-key limit
    val bytesUsed: Long = 0,            // cached from last poll
    val isActive: Boolean = false,      // currently connected key
    val lastConnectedAt: Long? = 0,
    val createdAt: Long = System.currentTimeMillis()
)
```

### Key Repository

```kotlin
interface KeyRepository {
    fun getAllKeys(): Flow<List<KeyEntity>>
    suspend fun addKey(ssUrl: String): Result<KeyEntity>
    suspend fun deleteKey(id: String)
    suspend fun setActiveKey(id: String)
    suspend fun getActiveKey(): KeyEntity?
    suspend fun refreshKeyStats(id: String): Result<KeyUsageStats>
}
```

### Key Addition Flow

```
User pastes ss:// URL
    → Validate URL format
    → Parse into AccessKey components
    → Optionally try connection test
    → Save to Room DB
    → Show in key list
```

### Deliverables
- [ ] Parse all valid `ss://` URL formats
- [ ] Store/retrieve keys from Room
- [ ] Switch between multiple keys
- [ ] Key list UI with connection status

---

## Phase 4: Outline Server API Client

### Goal
Query the Outline Server REST API to fetch real-time key usage stats.

### API Endpoints Used

| Endpoint | Purpose |
|----------|---------|
| `GET /server` | Server name, version, metrics status |
| `GET /access-keys` | List all keys (for your platform auth) |
| `GET /experimental/server/metrics` | **Key usage data**: data transferred, tunnel time, bandwidth, connected devices |

### Retrofit Service

```kotlin
interface OutlineApiService {
    @GET("server")
    suspend fun getServerInfo(): ServerInfo

    @GET("access-keys")
    suspend fun getAccessKeys(): AccessKeysResponse

    @GET("experimental/server/metrics")
    suspend fun getMetrics(@Query("since") since: String? = null): ServerMetrics
}
```

### Data Models

```kotlin
data class ServerMetrics(
    val server: ServerStats,
    val accessKeys: List<AccessKeyMetrics>
)

data class AccessKeyMetrics(
    val accessKeyId: Int,
    val tunnelTime: Duration,
    val dataTransferred: Bytes,
    val connection: ConnectionInfo?
)

data class ConnectionInfo(
    val lastTrafficSeen: Long,      // Unix timestamp
    val peakDeviceCount: PeakData
)
```

### Stats Mapping (API → UI)

| API Field | UI Display |
|-----------|-----------|
| `dataTransferred.bytes` | "Used: 2.4 GB" |
| `accessKeyDataLimit.bytes` | "Limit: 10 GB" |
| `limit - used` | "Remaining: 7.6 GB" |
| `connection.lastTrafficSeen` | "Last active: 2 min ago" |
| `connection.peakDeviceCount.data` | "Devices: 3" |
| `tunnelTime.seconds` | "Connected: 4h 23m" |

### Auth Scenarios

**Scenario A — Direct Key Paste:**
```
1. User pastes ss://... key
2. App parses host, port, password from URL
3. Server API URL = "https://{host}:{port}/{secretPath}"
4. Query API directly — no backend needed
```

**Scenario B — Platform Login:**
```
1. User logs in via your backend API
2. Backend returns list of assigned keys with server API URLs
3. App stores keys locally
4. App queries each server's API for stats
```

### Deliverables
- [ ] Retrofit client with all endpoints
- [ ] Stats refresh on pull-to-refresh + periodic (30s)
- [ ] Offline fallback (show cached stats)
- [ ] Error handling for unreachable servers

---

## Phase 5: UI Screens

### Screen Flow

```
Login Screen ──→ Home Screen ←── Key Input Screen (manual add)
                     │
                     ├── Key List (tap to connect)
                     ├── Stats Panel (per key)
                     └── Settings
```

### Home Screen

```
┌─────────────────────────────┐
│  Premium VPN         ⚙️     │
├─────────────────────────────┤
│  ┌─────────────────────┐    │
│  │  ● Connected        │    │
│  │  Server: US-East    │    │
│  │  2.4 GB / 10 GB     │    │
│  │  ████████░░ 24%     │    │
│  │  Remaining: 7.6 GB  │    │
│  │  Last active: now   │    │
│  └─────────────────────┘    │
│                             │
│  [    CONNECT / DISCONNECT ]│
│                             │
├─────────────────────────────┤
│  My Keys                    │
│  ┌───────────────────────┐  │
│  │ US-East     ● active  │  │
│  │ NL-West     ○         │  │
│  │ JP-Tokyo    ○         │  │
│  └───────────────────────┘  │
│                             │
│  [ + Add Key ]  [ Login ]   │
└─────────────────────────────┘
```

### Key Input Screen

```
┌─────────────────────────────┐
│  ← Add VPN Key             │
├─────────────────────────────┤
│                             │
│  Paste your outline key:   │
│  ┌───────────────────────┐  │
│  │ ss://...              │  │
│  └───────────────────────┘  │
│                             │
│  Or scan QR code           │
│                             │
│  Key Name:                  │
│  ┌───────────────────────┐  │
│  │ My US Server          │  │
│  └───────────────────────┘  │
│                             │
│  [        Add Key        ]  │
└─────────────────────────────┘
```

### Stats Detail Screen (per key)

```
┌─────────────────────────────┐
│  ← US-East Server          │
├─────────────────────────────┤
│  Status: ● Connected       │
│  Duration: 4h 23m          │
│                             │
│  Data Usage                 │
│  Used:      2.4 GB         │
│  Limit:    10.0 GB         │
│  Remaining: 7.6 GB         │
│  ████████████░░░░ 24%      │
│                             │
│  Connection                 │
│  Last active: 2 min ago    │
│  Peak devices: 3           │
│                             │
│  [ Disconnect ]  [ Delete ]│
└─────────────────────────────┘
```

### UI Tech Details

- **Navigation**: Compose Navigation (single-activity)
- **Theme**: Material 3 with dynamic color
- **Components**: Custom cards, progress bars, connection indicator
- **Animations**: Connection state transitions, stat counters

### Deliverables
- [ ] All screens implemented
- [ ] Navigation graph
- [ ] Material 3 theming
- [ ] Pull-to-refresh for stats
- [ ] Empty states & loading states

---

## Phase 6: Integration & Polish

### Integration Tasks

1. **Wire VPN Service ↔ UI**
   - Connection state flow from service to Compose
   - Start/stop from UI triggers service
   - Stats updates pushed to UI via StateFlow

2. **Wire API Client ↔ Stats Display**
   - Periodic stats refresh (every 30s while connected)
   - Cache stats locally for offline display
   - Handle server unreachable gracefully

3. **Boot persistence**
   - `BOOT_COMPLETED` broadcast receiver
   - Auto-connect on boot (if user enabled)

4. **Network monitoring**
   - `ConnectivityManager` callback
   - Auto-reconnect on network change
   - Show warning if no internet

### Testing

| Test | Method |
|------|--------|
| Key parsing | Unit tests (various `ss://` formats) |
| VPN connection | Integration test on emulator |
| API client | MockWebServer tests |
| UI | Screenshot tests (Paparazzi) |
| End-to-end | Manual test on physical device |

### Deliverables
- [ ] VPN connects on app launch
- [ ] Stats update in real-time
- [ ] Multiple key switching works
- [ ] App survives background/foreground cycles
- [ ] Works on Android 8+ (API 26+)

---

## Phase 7: Production Readiness

### Security

- [ ] Encrypt stored keys (EncryptedSharedPreferences for API tokens)
- [ ] Certificate pinning for your backend API
- [ ] No keys in logs
- [ ] ProGuard/R8 obfuscation

### Performance

- [ ] Lazy loading of stats
- [ ] Efficient TUN buffer sizes
- [ ] Battery optimization (adaptive refresh rate)
- [ ] Memory profiling

### Distribution

- [ ] Generate signed AAB for Play Store
- [ ] Store listing with screenshots
- [ ] Privacy policy (VPN apps require this)
- [ ] Data safety form

---

## Key Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|-----------|
| gomobile bind fails | Can't generate AAR | Use pre-built `mobileproxylib` AAR from JitPack as fallback |
| VPN drains battery | Poor UX | Adaptive refresh, efficient TUN buffers |
| Server API rate limiting | Stats fail to load | Cache locally, exponential backoff |
| Android API level fragmentation | VPN breaks on old devices | Target API 26+, test on API 26/30/34 |
| Play Store VPN policy | App rejected | Follow VPN app guidelines, clear privacy policy |

---

## Dependencies

### Go (go.mod)

```go
module premium-vpn-go

go 1.21

require (
    golang.getoutline.org/sdk v0.0.0
    golang.org/x/mobile v0.0.0
)
```

### Android (build.gradle.kts)

```kotlin
// UI
implementation("androidx.compose.material3:material3:1.2.0")
implementation("androidx.navigation:navigation-compose:2.7.0")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Database
implementation("androidx.room:room-runtime:2.6.0")
implementation("androidx.room:room-ktx:2.6.0")
kapt("androidx.room:room-compiler:2.6.0")

// DI
implementation("com.google.dagger:hilt-android:2.50")
kapt("com.google.dagger:hilt-compiler:2.50")
implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Security
implementation("androidx.security:security-crypto:1.1.0-alpha06")
```

---

## Build Commands Reference

```bash
# Go AAR build
cd go/
go mod tidy
gomobile bind -ldflags='-s -w' -target=android -androidapi=21 \
  -o ../android/app/libs/outline.aar ./

# Android build
cd android/
./gradlew assembleDebug

# Android release
./gradlew bundleRelease

# Run on device
adb install app/build/outputs/apk/debug/app-debug.apk
```
