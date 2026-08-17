package com.premiumvpn.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var keyRepository: com.premiumvpn.app.data.repository.KeyRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("vpn_prefs", Context.MODE_PRIVATE)
            val autoConnect = prefs.getBoolean(KEY_AUTO_CONNECT, false)

            if (autoConnect) {
                goAsync().let { pendingResult ->
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        try {
                            val activeKey = keyRepository.getActiveKey()
                            activeKey?.let { key ->
                                val vpnIntent = Intent(context, VpnService::class.java).apply {
                                    action = VpnService.ACTION_CONNECT
                                    putExtra(VpnService.EXTRA_SERVER_ADDR, "${key.host}:${key.port}")
                                    putExtra(VpnService.EXTRA_PASSWORD, key.password)
                                    putExtra(VpnService.EXTRA_METHOD, key.method)
                                }
                                context.startForegroundService(vpnIntent)
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val KEY_AUTO_CONNECT = "auto_connect"
    }
}
