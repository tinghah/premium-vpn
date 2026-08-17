package mobileproxy

import (
	"fmt"
	"time"
)

// FormatBytes formats bytes into human-readable string
func FormatBytes(bytes int64) string {
	const (
		KB = 1024
		MB = 1024 * KB
		GB = 1024 * MB
		TB = 1024 * GB
	)

	switch {
	case bytes >= TB:
		return fmt.Sprintf("%.2f TB", float64(bytes)/float64(TB))
	case bytes >= GB:
		return fmt.Sprintf("%.2f GB", float64(bytes)/float64(GB))
	case bytes >= MB:
		return fmt.Sprintf("%.2f MB", float64(bytes)/float64(MB))
	case bytes >= KB:
		return fmt.Sprintf("%.2f KB", float64(bytes)/float64(KB))
	default:
		return fmt.Sprintf("%d B", bytes)
	}
}

// FormatDuration formats seconds into human-readable duration
func FormatDuration(seconds int64) string {
	d := time.Duration(seconds) * time.Second
	hours := int(d.Hours())
	minutes := int(d.Minutes()) % 60
 secs := int(d.Seconds()) % 60

	if hours > 0 {
		return fmt.Sprintf("%dh %dm %ds", hours, minutes, secs)
	}
	if minutes > 0 {
		return fmt.Sprintf("%dm %ds", minutes, secs)
	}
	return fmt.Sprintf("%ds", secs)
}

// UsagePercent calculates usage percentage
func UsagePercent(used, limit int64) float64 {
	if limit <= 0 {
		return 0
	}
	pct := float64(used) / float64(limit) * 100
	if pct > 100 {
		pct = 100
	}
	return pct
}

// RemainingBytes calculates remaining bytes
func RemainingBytes(used, limit int64) int64 {
	remaining := limit - used
	if remaining < 0 {
		remaining = 0
	}
	return remaining
}
