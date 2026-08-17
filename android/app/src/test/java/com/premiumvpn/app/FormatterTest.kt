package com.premiumvpn.app.util

import org.junit.Assert.*
import org.junit.Test

class FormatterTest {

    @Test
    fun `formatBytes returns bytes for small values`() {
        assertEquals("512 B", Formatter.formatBytes(512))
    }

    @Test
    fun `formatBytes returns KB`() {
        assertEquals("1.50 KB", Formatter.formatBytes(1536))
    }

    @Test
    fun `formatBytes returns MB`() {
        assertEquals("2.50 MB", Formatter.formatBytes(2621440))
    }

    @Test
    fun `formatBytes returns GB`() {
        assertEquals("1.00 GB", Formatter.formatBytes(1073741824))
    }

    @Test
    fun `formatBytes returns TB`() {
        assertEquals("1.50 TB", Formatter.formatBytes(1649267441664))
    }

    @Test
    fun `formatDuration returns seconds only`() {
        assertEquals("30s", Formatter.formatDuration(30))
    }

    @Test
    fun `formatDuration returns minutes and seconds`() {
        assertEquals("5m 30s", Formatter.formatDuration(330))
    }

    @Test
    fun `formatDuration returns hours minutes seconds`() {
        assertEquals("2h 15m 45s", Formatter.formatDuration(8145))
    }

    @Test
    fun `formatTimestamp returns now for recent timestamps`() {
        val now = System.currentTimeMillis()
        assertEquals("now", Formatter.formatTimestamp(now))
    }

    @Test
    fun `formatTimestamp returns minutes ago`() {
        val fiveMinAgo = System.currentTimeMillis() - 5 * 60 * 1000
        val result = Formatter.formatTimestamp(fiveMinAgo)
        assertTrue(result.contains("m ago"))
    }

    @Test
    fun `formatTimestamp returns Never for 0`() {
        assertEquals("Never", Formatter.formatTimestamp(0))
    }

    @Test
    fun `formatUsagePercent calculates correctly`() {
        assertEquals("50.0%", Formatter.formatUsagePercent(500, 1000))
        assertEquals("100.0%", Formatter.formatUsagePercent(1000, 1000))
        assertEquals("0.0%", Formatter.formatUsagePercent(0, 1000))
        assertEquals("N/A", Formatter.formatUsagePercent(100, 0))
    }
}
