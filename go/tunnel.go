package mobileproxy

import (
	"fmt"
	"sync"
	"time"
)

// TunnelState represents the current state of the VPN tunnel
type TunnelState int

const (
	StateDisconnected TunnelState = iota
	StateConnecting
	StateConnected
	StateError
)

// Stats holds traffic statistics for the tunnel
type Stats struct {
	BytesSent     int64
	BytesReceived int64
	StartTime     time.Time
	LastError     string
}

// Tunnel manages the VPN tunnel connection
type Tunnel struct {
	mu         sync.RWMutex
	state      TunnelState
	stats      Stats
	serverAddr string
	password   string
	method     string
	localPort  int
	stopCh     chan struct{}
}

var (
	defaultTunnel *Tunnel
	once          sync.Once
)

func getTunnel() *Tunnel {
	once.Do(func() {
		defaultTunnel = &Tunnel{
			state:  StateDisconnected,
			stopCh: make(chan struct{}),
		}
	})
	return defaultTunnel
}

// StartTunnel creates a Shadowsocks tunnel and begins routing traffic.
// This is the main entry point called from Android via gomobile.
//
// Parameters:
//   - localPort: local port to bind the SOCKS5 proxy (0 = auto-assign)
//   - serverAddr: remote Shadowsocks server address (host:port)
//   - password: Shadowsocks password
//   - method: encryption method (e.g., "aes-256-gcm", "chacha20-ietf-poly1305")
func StartTunnel(localPort int, serverAddr, password, method string) error {
	t := getTunnel()
	t.mu.Lock()
	defer t.mu.Unlock()

	if t.state == StateConnected || t.state == StateConnecting {
		return fmt.Errorf("tunnel already active (state: %d)", t.state)
	}

	t.state = StateConnecting
	t.serverAddr = serverAddr
	t.password = password
	t.method = method
	t.localPort = localPort
	t.stats = Stats{StartTime: time.Now()}
	t.stopCh = make(chan struct{})

	go t.run()

	return nil
}

// run performs the actual tunnel setup and traffic forwarding
func (t *Tunnel) run() {
	defer func() {
		t.mu.Lock()
		t.state = StateDisconnected
		t.mu.Unlock()
	}()

	// TODO: Implement actual Shadowsocks tunnel using Outline SDK:
	// 1. Create a Shadowsocks dialer with the provided credentials
	// 2. Set up a local SOCKS5 proxy
	// 3. Forward traffic through the encrypted tunnel
	// 4. Collect traffic stats
	//
	// Example with Outline SDK (when available):
	//   dialer, err := shadowsocks.NewDialer(t.serverAddr, &shadowsocks.Config{
	//       Password: t.password,
	//       Cipher:   t.method,
	//   })
	//   ln, err := net.Listen("tcp", fmt.Sprintf(":%d", t.localPort))
	//   // ... accept and forward connections

	<-t.stopCh
}

// StopTunnel gracefully stops the VPN tunnel
func StopTunnel() {
	t := getTunnel()
	t.mu.Lock()
	defer t.mu.Unlock()

	if t.state == StateDisconnected {
		return
	}

	close(t.stopCh)
	t.state = StateDisconnected
}

// IsConnected returns true if the tunnel is currently active
func IsConnected() bool {
	t := getTunnel()
	t.mu.RLock()
	defer t.mu.RUnlock()
	return t.state == StateConnected
}

// GetState returns the current tunnel state as an integer
// 0=Disconnected, 1=Connecting, 2=Connected, 3=Error
func GetState() int {
	t := getTunnel()
	t.mu.RLock()
	defer t.mu.RUnlock()
	return int(t.state)
}

// GetBytesSent returns total bytes sent through the tunnel
func GetBytesSent() int64 {
	t := getTunnel()
	t.mu.RLock()
	defer t.mu.RUnlock()
	return t.stats.BytesSent
}

// GetBytesReceived returns total bytes received through the tunnel
func GetBytesReceived() int64 {
	t := getTunnel()
	t.mu.RLock()
	defer t.mu.RUnlock()
	return t.stats.BytesReceived
}

// GetBytesTransferred returns total bytes (sent + received) through the tunnel
func GetBytesTransferred() int64 {
	t := getTunnel()
	t.mu.RLock()
	defer t.mu.RUnlock()
	return t.stats.BytesSent + t.stats.BytesReceived
}

// GetDuration returns the connection duration in seconds
func GetDuration() int64 {
	t := getTunnel()
	t.mu.RLock()
	defer t.mu.RUnlock()
	if t.state != StateConnected {
		return 0
	}
	return int64(time.Since(t.stats.StartTime).Seconds())
}

// GetLastError returns the last error message, or empty string if no error
func GetLastError() string {
	t := getTunnel()
	t.mu.RLock()
	defer t.mu.RUnlock()
	return t.stats.LastError
}

// ResetStats resets all traffic statistics to zero
func ResetStats() {
	t := getTunnel()
	t.mu.Lock()
	defer t.mu.Unlock()
	t.stats = Stats{StartTime: time.Now()}
}
