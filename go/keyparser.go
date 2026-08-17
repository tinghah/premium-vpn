package mobileproxy

import (
	"encoding/base64"
	"fmt"
	"net/url"
	"strconv"
	"strings"
)

// AccessKey represents a parsed Outline VPN access key
type AccessKey struct {
	Password string
	Host     string
	Port     int
	Method   string
	Name     string
	Tag      string
}

// ParseAccessKey parses an Outline ss:// access key URL.
// Supports both Base64-encoded and plain-text formats.
//
// Format 1 (Base64): ss://BASE64(method:password)@host:port/?outline=1&tag=Name
// Format 2 (plain):  ss://method:password@host:port/?outline=1&tag=Name
func ParseAccessKey(rawURL string) (*AccessKey, error) {
	if !strings.HasPrefix(rawURL, "ss://") {
		return nil, fmt.Errorf("invalid ss:// URL: missing ss:// prefix")
	}

	// Parse the URL
	u, err := url.Parse(rawURL)
	if err != nil {
		return nil, fmt.Errorf("failed to parse URL: %w", err)
	}

	host := u.Hostname()
	portStr := u.Port()

	if host == "" {
		return nil, fmt.Errorf("invalid ss:// URL: missing host")
	}

	port := 8388 // default Shadowsocks port
	if portStr != "" {
		port, err = strconv.Atoi(portStr)
		if err != nil {
			return nil, fmt.Errorf("invalid port: %w", err)
		}
	}

	// Extract password and method from userinfo
	var password, method string
	userInfo := u.User.String()

	if userInfo != "" {
		// Try Base64-decoded format first
		decoded, err := base64.StdEncoding.DecodeString(userInfo)
		if err == nil {
			// Successfully decoded — format is method:password
			parts := strings.SplitN(string(decoded), ":", 2)
			if len(parts) == 2 {
				method = parts[0]
				password = parts[1]
			}
		} else {
			// Not Base64 — try plain format method:password
			parts := strings.SplitN(userInfo, ":", 2)
			if len(parts) == 2 {
				method = parts[0]
				password = parts[1]
			}
		}
	}

	if password == "" {
		return nil, fmt.Errorf("invalid ss:// URL: missing password")
	}

	// Default method
	if method == "" {
		method = "aes-256-gcm"
	}

	// Extract tag (name) from query params
	name := u.Query().Get("tag")
	if name == "" {
		name = fmt.Sprintf("%s:%d", host, port)
	}

	return &AccessKey{
		Password: password,
		Host:     host,
		Port:     port,
		Method:   method,
		Name:     name,
		Tag:      u.Query().Get("tag"),
	}, nil
}

// ServerAddress returns the full server address as host:port
func (a *AccessKey) ServerAddress() string {
	return fmt.Sprintf("%s:%d", a.Host, a.Port)
}

// String returns the ss:// URL representation
func (a *AccessKey) String() string {
	cred := base64.StdEncoding.EncodeToString([]byte(a.Method + ":" + a.Password))
	return fmt.Sprintf("ss://%s@%s:%d/?outline=1&tag=%s", cred, a.Host, a.Port, url.QueryEscape(a.Name))
}
