package com.premiumvpn.app.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Monitors network connectivity changes and triggers VPN reconnection.
 */
class NetworkMonitor(context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _networkType = MutableStateFlow(NetworkType.UNKNOWN)
    val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()

    private var onNetworkRestored: (() -> Unit)? = null
    private var wasConnected = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isConnected.value = true
            val caps = connectivityManager.getNetworkCapabilities(network)
            _networkType.value = when {
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
                else -> NetworkType.OTHER
            }

            if (wasConnected.not() && onNetworkRestored != null) {
                Log.i(TAG, "Network restored, triggering reconnection")
                onNetworkRestored?.invoke()
            }
            wasConnected = true
        }

        override fun onLost(network: Network) {
            _isConnected.value = false
            _networkType.value = NetworkType.UNKNOWN
            wasConnected = false
            Log.w(TAG, "Network lost")
        }

        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            _networkType.value = when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                else -> NetworkType.OTHER
            }
        }
    }

    fun startMonitoring(onRestore: () -> Unit) {
        onNetworkRestored = onRestore
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)

        // Check initial state
        val activeNetwork = connectivityManager.activeNetwork
        val caps = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        _isConnected.value = caps != null
    }

    fun stopMonitoring() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unregister network callback", e)
        }
        onNetworkRestored = null
    }

    enum class NetworkType {
        WIFI, CELLULAR, ETHERNET, OTHER, UNKNOWN
    }

    companion object {
        private const val TAG = "NetworkMonitor"
    }
}
