package com.premiumvpn.app.util

import android.net.Uri
import android.util.Base64

data class ParsedKey(
    val password: String,
    val host: String,
    val port: Int,
    val method: String,
    val name: String,
    val serverApiSecret: String? = null
)

object KeyParser {

    private const val DEFAULT_PORT = 8388
    private const val DEFAULT_METHOD = "aes-256-gcm"

    fun parse(ssUrl: String): ParsedKey? {
        if (!ssUrl.startsWith("ss://")) return null

        return try {
            val uri = Uri.parse(ssUrl)
            val host = uri.host ?: return null
            val port = if (uri.port > 0) uri.port else DEFAULT_PORT

            val userInfo = uri.userInfo
            var password = ""
            var method = DEFAULT_METHOD

            if (userInfo != null) {
                // Try Base64 decode first (method:password)
                try {
                    val decoded = String(Base64.decode(userInfo, Base64.DEFAULT))
                    val parts = decoded.split(":", limit = 2)
                    if (parts.size == 2) {
                        method = parts[0]
                        password = parts[1]
                    }
                } catch (e: Exception) {
                    // Not Base64 — try plain method:password
                    val parts = userInfo.split(":", limit = 2)
                    if (parts.size == 2) {
                        method = parts[0]
                        password = parts[1]
                    }
                }
            }

            if (password.isEmpty()) return null

            val name = uri.getQueryParameter("tag")
                ?: "$host:$port"

            // Extract server API secret from path or query parameter
            val serverApiSecret = uri.getQueryParameter("secret")
                ?: uri.lastPathSegment

            ParsedKey(
                password = password,
                host = host,
                port = port,
                method = method,
                name = name,
                serverApiSecret = serverApiSecret
            )
        } catch (e: Exception) {
            null
        }
    }
}
