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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@AndroidEntryPoint
class VpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var statsJob: Job? = null
    private var networkMonitor: NetworkMonitor? = null

    private var pendingServerAddr: String? = null
    private var pendingPassword: String? = null
    private var pendingMethod: String? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _stats = MutableStateFlow(VpnStats())
    val stats: StateFlow<VpnStats> = _stats.asStateFlow()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                pendingServerAddr = intent.getStringExtra(EXTRA_SERVER_ADDR)
                pendingPassword = intent.getStringExtra(EXTRA_PASSWORD)
                pendingMethod = intent.getStringExtra(EXTRA_METHOD) ?: "aes-256-gcm"

                if (pendingServerAddr != null && pendingPassword != null) {
                    startVpn(pendingServerAddr!!, pendingPassword!!, pendingMethod!!)
                }
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
                    Log.e(TAG, "Failed to establish VPN interface")
                    _connectionState.value = ConnectionState.ERROR
                    stopSelf()
                    return@launch
                }

                val started = GoVpnBridge.startTunnel(
                    localPort = 0,
                    serverAddr = serverAddr,
                    password = password,
                    method = method
                )

                if (!started) {
                    Log.e(TAG, "Failed to start native tunnel")
                    _connectionState.value = ConnectionState.ERROR
                    vpnInterface?.close()
                    vpnInterface = null
                    stopSelf()
                    return@launch
                }

                _connectionState.value = ConnectionState.CONNECTED
                updateNotification("Connected to $serverAddr")
                startStatsCollection()
                startNetworkMonitoring()

            } catch (e: Exception) {
                Log.e(TAG, "VPN connection failed", e)
                _connectionState.value = ConnectionState.ERROR
                vpnInterface?.close()
                vpnInterface = null
                stopSelf()
            }
        }
    }

    private fun startNetworkMonitoring() {
        networkMonitor?.stopMonitoring()
        networkMonitor = NetworkMonitor(this).apply {
            startMonitoring {
                // Network restored — attempt reconnection
                serviceScope.launch {
                    Log.i(TAG, "Network restored, reconnecting VPN...")
                    _connectionState.value = ConnectionState.CONNECTING

                    GoVpnBridge.stopTunnel()
                    vpnInterface?.close()
                    vpnInterface = null

                    delay(1000) // Brief delay before reconnect

                    val server = pendingServerAddr
                    val pass = pendingPassword
                    val meth = pendingMethod
                    if (server != null && pass != null && meth != null) {
                        startVpn(server, pass, meth)
                    }
                }
            }
        }
    }

    private fun stopVpn() {
        statsJob?.cancel()
        networkMonitor?.stopMonitoring()
        networkMonitor = null
        GoVpnBridge.stopTunnel()
        vpnInterface?.close()
        vpnInterface = null
        _connectionState.value = ConnectionState.DISCONNECTED
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startStatsCollection() {
        statsJob = serviceScope.launch {
            while (isActive) {
                val transferred = GoVpnBridge.getBytesTransferred()
                val duration = GoVpnBridge.getDuration()

                _stats.value = VpnStats(
                    bytesSent = transferred / 2,
                    bytesReceived = transferred / 2,
                    durationSeconds = duration
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
        networkMonitor?.stopMonitoring()
        GoVpnBridge.stopTunnel()
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
