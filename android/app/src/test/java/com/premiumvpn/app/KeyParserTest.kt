package com.premiumvpn.app.util

import org.junit.Assert.*
import org.junit.Test

class KeyParserTest {

    @Test
    fun `parse valid Base64 encoded ss URL`() {
        // ss://YWVzLTI1Ni1nY206cGFzc3dvcmQ=@example.com:8388/?outline=1&tag=MyServer
        // Base64("aes-256-gcm:password") = "YWVzLTI1Ni1nY206cGFzc3dvcmQ="
        val url = "ss://YWVzLTI1Ni1nY206cGFzc3dvcmQ=@example.com:8388/?outline=1&tag=MyServer"
        val result = KeyParser.parse(url)

        assertNotNull(result)
        assertEquals("password", result!!.password)
        assertEquals("example.com", result.host)
        assertEquals(8388, result.port)
        assertEquals("aes-256-gcm", result.method)
        assertEquals("MyServer", result.name)
    }

    @Test
    fun `parse valid plain text ss URL`() {
        val url = "ss://aes-256-gcm:mypassword@192.168.1.1:443/?outline=1"
        val result = KeyParser.parse(url)

        assertNotNull(result)
        assertEquals("mypassword", result!!.password)
        assertEquals("192.168.1.1", result.host)
        assertEquals(443, result.port)
        assertEquals("aes-256-gcm", result.method)
        assertEquals("192.168.1.1:443", result.name) // default name from host:port
    }

    @Test
    fun `parse ss URL without port uses default 8388`() {
        val url = "ss://aes-256-gcm:pass@example.com/?outline=1"
        val result = KeyParser.parse(url)

        assertNotNull(result)
        assertEquals(8388, result!!.port)
    }

    @Test
    fun `parse ss URL without tag uses host:port as name`() {
        val url = "ss://aes-256-gcm:pass@example.com:8388/?outline=1"
        val result = KeyParser.parse(url)

        assertNotNull(result)
        assertEquals("example.com:8388", result!!.name)
    }

    @Test
    fun `parse ss URL without method defaults to aes-256-gcm`() {
        val url = "ss://cGFzc3dvcmQ=@example.com:8388/?outline=1"
        val result = KeyParser.parse(url)

        assertNotNull(result)
        assertEquals("aes-256-gcm", result!!.method)
    }

    @Test
    fun `reject non ss URL`() {
        val url = "https://example.com/vpn"
        val result = KeyParser.parse(url)
        assertNull(result)
    }

    @Test
    fun `reject URL without password`() {
        val url = "ss://example.com:8388/?outline=1"
        val result = KeyParser.parse(url)
        assertNull(result)
    }

    @Test
    fun `reject empty string`() {
        val result = KeyParser.parse("")
        assertNull(result)
    }

    @Test
    fun `roundtrip - parse then convert back to string`() {
        val url = "ss://YWVzLTI1Ni1nY206cGFzc3dvcmQ=@example.com:8388/?outline=1&tag=MyServer"
        val parsed = KeyParser.parse(url)

        assertNotNull(parsed)
        assertEquals("example.com", parsed!!.host)
        assertEquals(8388, parsed.port)
        assertEquals("password", parsed.password)
        assertEquals("aes-256-gcm", parsed.method)
    }

    @Test
    fun `parse chacha20 method`() {
        val url = "ss://Y2hhY2hhMjAtaWV0Zi1wb2x5MTMwNTpwYXNzQDEyNy4wLjAuMTo0NDM/?tag=Fast"
        val result = KeyParser.parse(url)

        assertNotNull(result)
        assertEquals("chacha20-ietf-poly1305", result!!.method)
        assertEquals("pass@127.0.0.1", result.password) // Note: @ in password is part of it
        assertEquals("127.0.0.1", result.host)
        assertEquals(443, result.port)
        assertEquals("Fast", result.name)
    }
}
