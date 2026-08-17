package com.premiumvpn.app.service

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * JNI bridge between Android VPN service and Go native code (outline.aar).
 *
 * When the Go AAR is built via `gomobile bind`, it generates a `mobileproxy` package
 * with Java bindings. This bridge wraps those calls and adds error handling.
 *
 * If the native library is not available (debug builds without AAR), it uses
 * stub implementations so the app can still be tested.
 */
object GoVpnBridge {

    private const val TAG = "GoVpnBridge"
    private var nativeAvailable = false

    private val _bytesSent = MutableStateFlow(0L)
    val bytesSent: StateFlow<Long> = _bytesSent.asStateFlow()

    private val _bytesReceived = MutableStateFlow(0L)
    val bytesReceived: StateFlow<Long> = _bytesReceived.asStateFlow()

    init {
        try {
            System.loadLibrary("mobileproxy")
            nativeAvailable = true
            Log.i(TAG, "Native library loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native library not available, using stubs", e)
            nativeAvailable = false
        }
    }

    /**
     * Start the Shadowsocks VPN tunnel.
     *
     * @param localPort local port for SOCKS5 proxy (0 = auto-assign)
     * @param serverAddr remote server address (host:port)
     * @param password Shadowsocks password
     * @param method encryption method (e.g., "aes-256-gcm")
     * @return true if tunnel started successfully
     */
    fun startTunnel(localPort: Int, serverAddr: String, password: String, method: String): Boolean {
        return if (nativeAvailable) {
            try {
                // When Go AAR is built, this calls:
                // mobileproxy.Mobileproxy.StartTunnel(localPort, serverAddr, password, method)
                nativeStartTunnel(localPort, serverAddr, password, method)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start tunnel", e)
                false
            }
        } else {
            Log.w(TAG, "Stub: startTunnel($localPort, $serverAddr, ...)")
            true // Stub always succeeds
        }
    }

    /**
     * Stop the VPN tunnel.
     */
    fun stopTunnel() {
        if (nativeAvailable) {
            try {
                nativeStopTunnel()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop tunnel", e)
            }
        } else {
            Log.w(TAG, "Stub: stopTunnel()")
        }
    }

    /**
     * Check if the tunnel is currently connected.
     */
    fun isConnected(): Boolean {
        return if (nativeAvailable) {
            try {
                nativeIsConnected()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check connection", e)
                false
            }
        } else {
            Log.w(TAG, "Stub: isConnected() = false")
            false
        }
    }

    /**
     * Get total bytes transferred (sent + received).
     */
    fun getBytesTransferred(): Long {
        return if (nativeAvailable) {
            try {
                nativeGetBytesTransferred()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get bytes", e)
                0L
            }
        } else {
            0L
        }
    }

    /**
     * Get connection duration in seconds.
     */
    fun getDuration(): Long {
        return if (nativeAvailable) {
            try {
                nativeGetDuration()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get duration", e)
                0L
            }
        } else {
            0L
        }
    }

    /**
     * Get last error message from the tunnel.
     */
    fun getLastError(): String {
        return if (nativeAvailable) {
            try {
                nativeGetLastError()
            } catch (e: Exception) {
                ""
            }
        } else {
            ""
        }
    }

    // Native methods - these will be implemented by the Go AAR
    // When gomobile bind generates the AAR, it creates these bindings
    @JvmStatic
    private external fun nativeStartTunnel(
        localPort: Int,
        serverAddr: String,
        password: String,
        method: String
    ): Boolean

    @JvmStatic
    private external fun nativeStopTunnel()

    @JvmStatic
    private external fun nativeIsConnected(): Boolean

    @JvmStatic
    private external fun nativeGetBytesTransferred(): Long

    @JvmStatic
    private external fun nativeGetDuration(): Long

    @JvmStatic
    private external fun nativeGetLastError(): String
}
