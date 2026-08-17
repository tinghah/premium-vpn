package com.premiumvpn.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object Formatter {

    fun formatBytes(bytes: Long): String {
        val kb = 1024L
        val mb = kb * 1024
        val gb = mb * 1024
        val tb = gb * 1024

        return when {
            bytes >= tb -> String.format("%.2f TB", bytes.toFloat() / tb)
            bytes >= gb -> String.format("%.2f GB", bytes.toFloat() / gb)
            bytes >= mb -> String.format("%.2f MB", bytes.toFloat() / mb)
            bytes >= kb -> String.format("%.2f KB", bytes.toFloat() / kb)
            else -> "$bytes B"
        }
    }

    fun formatDuration(seconds: Long): String {
        val hours = TimeUnit.SECONDS.toHours(seconds)
        val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
        val secs = seconds % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m ${secs}s"
            minutes > 0 -> "${minutes}m ${secs}s"
            else -> "${secs}s"
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        if (timestamp <= 0) return "Never"
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
        }
    }

    fun formatUsagePercent(used: Long, limit: Long): String {
        if (limit <= 0) return "N/A"
        val pct = (used.toFloat() / limit * 100).coerceIn(0f, 100f)
        return String.format("%.1f%%", pct)
    }
}
