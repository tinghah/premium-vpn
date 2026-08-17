package com.premiumvpn.app.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.premiumvpn.app.App
import com.premiumvpn.app.MainActivity
import com.premiumvpn.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class VpnService : VpnService() {

    @Inject
    lateinit var keyRepository: com.premiumvpn.app.data.repository.KeyRepository

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var statsJob: Job? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _stats = MutableStateFlow(VpnStats())
    val stats: StateFlow<VpnStats> = _stats.asStateFlow()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val serverAddr = intent.getStringExtra(EXTRA_SERVER_ADDR) ?: return START_NOT_STICKY
                val password = intent.getStringExtra(EXTRA_PASSWORD) ?: return START_NOT_STICKY
                val method = intent.getStringExtra(EXTRA_METHOD) ?: "aes-256-gcm"
                startVpn(serverAddr, password, method)
            }
            ACTION_DISCONNECT -> {
                stopVpn()
            }
        }
        return START_STICKY
    }

    private fun startVpn(serverAddr: String, password: String, method: String) {
        serviceScope.launch {
            _connectionState.value = ConnectionState.CONNECTING
            startForeground(NOTIFICATION_ID, buildNotification("Connecting..."))

            try {
                val builder = Builder()
                    .setSession("Premium VPN")
                    .setMtu(1500)
                    .addAddress("10.0.0.2", 32)
                    .addRoute("0.0.0.0", 0)
                    .addDnsServer("8.8.8.8")
                    .addDnsServer("8.8.4.4")

                vpnInterface = builder.establish()

                if (vpnInterface == null) {
                    _connectionState.value = ConnectionState.ERROR
                    stopSelf()
                    return@launch
                }

                // TODO: Pass vpnInterface.fd to Go native code
                // The Go code will:
                // 1. Create Shadowsocks connection to serverAddr
                // 2. Set up tun2socks using the TUN file descriptor
                // 3. Forward all traffic through the encrypted tunnel

                _connectionState.value = ConnectionState.CONNECTED
                updateNotification("Connected to $serverAddr")
                startStatsCollection()

            } catch (e: Exception) {
                Log.e(TAG, "VPN connection failed", e)
                _connectionState.value = ConnectionState.ERROR
                stopSelf()
            }
        }
    }

    private fun stopVpn() {
        statsJob?.cancel()
        vpnInterface?.close()
        vpnInterface = null
        _connectionState.value = ConnectionState.DISCONNECTED
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startStatsCollection() {
        statsJob = serviceScope.launch {
            while (isActive) {
                // TODO: Read stats from Go native code
                // val bytesSent = Mobileproxy.getBytesSent()
                // val bytesReceived = Mobileproxy.getBytesReceived()
                _stats.value = VpnStats(
                    bytesSent = 0,
                    bytesReceived = 0,
                    durationSeconds = 0
                )
                delay(1000)
            }
        }
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = PendingIntent.getService(
            this, 1,
            Intent(this, VpnService::class.java).apply { action = ACTION_DISCONNECT },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, App.VPN_CHANNEL_ID)
            .setContentTitle("Premium VPN")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Disconnect", disconnectIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        vpnInterface?.close()
    }

    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, ERROR
    }

    data class VpnStats(
        val bytesSent: Long = 0,
        val bytesReceived: Long = 0,
        val durationSeconds: Long = 0
    )

    companion object {
        private const val TAG = "VpnService"
        private const val NOTIFICATION_ID = 1
        const val ACTION_CONNECT = "com.premiumvpn.CONNECT"
        const val ACTION_DISCONNECT = "com.premiumvpn.DISCONNECT"
        const val EXTRA_SERVER_ADDR = "server_addr"
        const val EXTRA_PASSWORD = "password"
        const val EXTRA_METHOD = "method"
    }
}
