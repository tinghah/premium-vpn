package com.premiumvpn.app.domain.model

data class KeyUsageStats(
    val keyId: String,
    val bytesUsed: Long,
    val tunnelTimeSeconds: Long,
    val lastTrafficSeen: Long,
    val peakDevices: Int,
    val dataLimitBytes: Long?
) {
    val remainingBytes: Long
        get() = if (dataLimitBytes != null) {
            (dataLimitBytes - bytesUsed).coerceAtLeast(0)
        } else Long.MAX_VALUE

    val usagePercent: Float
        get() = if (dataLimitBytes != null && dataLimitBytes > 0) {
            (bytesUsed.toFloat() / dataLimitBytes * 100).coerceIn(0f, 100f)
        } else 0f

    val hasLimit: Boolean
        get() = dataLimitBytes != null && dataLimitBytes > 0
}
