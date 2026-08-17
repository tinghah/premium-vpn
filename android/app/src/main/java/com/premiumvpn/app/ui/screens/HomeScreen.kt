package com.premiumvpn.app.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.premiumvpn.app.data.local.KeyEntity
import com.premiumvpn.app.data.repository.KeyRepository
import com.premiumvpn.app.domain.model.KeyUsageStats
import com.premiumvpn.app.service.VpnService
import com.premiumvpn.app.util.Formatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val keyRepository: KeyRepository
) : ViewModel() {

    val keys: StateFlow<List<KeyEntity>> = keyRepository.getAllKeys()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _connectionState = MutableStateFlow(VpnService.ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<VpnService.ConnectionState> = _connectionState.asStateFlow()

    private val _activeKeyStats = MutableStateFlow<KeyUsageStats?>(null)
    val activeKeyStats: StateFlow<KeyUsageStats?> = _activeKeyStats.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var refreshJob: kotlinx.coroutines.Job? = null

    init {
        refreshStats()
        startPeriodicRefresh()
    }

    private fun startPeriodicRefresh() {
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(30000)
                val activeKey = keyRepository.getActiveKey()
                activeKey?.let {
                    keyRepository.refreshKeyStats(it.id)
                        .onSuccess { stats -> _activeKeyStats.value = stats }
                }
            }
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val activeKey = keyRepository.getActiveKey()
            activeKey?.let {
                keyRepository.refreshKeyStats(it.id)
                    .onSuccess { stats -> _activeKeyStats.value = stats }
            }
            _isRefreshing.value = false
        }
    }

    fun connect(key: KeyEntity, context: Context) {
        viewModelScope.launch {
            keyRepository.setActiveKey(key.id)
            val intent = Intent(context, VpnService::class.java).apply {
                action = VpnService.ACTION_CONNECT
                putExtra(VpnService.EXTRA_SERVER_ADDR, "${key.host}:${key.port}")
                putExtra(VpnService.EXTRA_PASSWORD, key.password)
                putExtra(VpnService.EXTRA_METHOD, key.method)
            }
            context.startForegroundService(intent)
        }
    }

    fun disconnect(context: Context) {
        val intent = Intent(context, VpnService::class.java).apply {
            action = VpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddKey: () -> Unit,
    onLogin: () -> Unit,
    onKeyClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val keys by viewModel.keys.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val activeStats by viewModel.activeKeyStats.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium VPN") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { viewModel.refreshStats() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = onLogin,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Login")
                }
                FloatingActionButton(
                    onClick = onAddKey,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Key")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ConnectionStatusCard(
                    isConnected = connectionState == VpnService.ConnectionState.CONNECTED,
                    isConnecting = connectionState == VpnService.ConnectionState.CONNECTING,
                    stats = activeStats,
                    onToggle = {
                        if (connectionState == VpnService.ConnectionState.CONNECTED) {
                            viewModel.disconnect(context)
                        } else {
                            keys.firstOrNull()?.let { viewModel.connect(it, context) }
                        }
                    }
                )
            }

            item {
                Text(
                    text = "My Keys",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (keys.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No keys added yet.\nTap + to add your first VPN key.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(keys, key = { it.id }) { key ->
                KeyListItem(
                    key = key,
                    isActive = connectionState == VpnService.ConnectionState.CONNECTED,
                    onClick = { onKeyClick(key.id) }
                )
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(
    isConnected: Boolean,
    isConnecting: Boolean,
    stats: KeyUsageStats?,
    onToggle: () -> Unit
) {
    val statusColor by animateColorAsState(
        targetValue = when {
            isConnected -> Color(0xFF34A853)
            isConnecting -> Color(0xFFFBBC04)
            else -> Color(0xFFEA4335)
        }
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(12.dp),
                    shape = MaterialTheme.shapes.small,
                    color = statusColor
                ) {}
                Text(
                    text = when {
                        isConnected -> "Connected"
                        isConnecting -> "Connecting..."
                        else -> "Disconnected"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (stats != null && stats.hasLimit) {
                val usedFormatted = Formatter.formatBytes(stats.bytesUsed)
                val limitFormatted = Formatter.formatBytes(stats.dataLimitBytes ?: 0)
                val remainingFormatted = Formatter.formatBytes(stats.remainingBytes)

                Text(
                    text = "$usedFormatted / $limitFormatted",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                LinearProgressIndicator(
                    progress = { stats.usagePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(vertical = 8.dp),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Text(
                    text = "Remaining: $remainingFormatted",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (stats != null) {
                Text(
                    text = Formatter.formatBytes(stats.bytesUsed),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "No data limit set",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Select a key to connect",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onToggle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Icon(
                    Icons.Default.PowerSettingsNew,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isConnected) "Disconnect" else "Connect",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun KeyListItem(
    key: KeyEntity,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = key.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${key.host}:${key.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isActive) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = MaterialTheme.shapes.small,
                    color = Color(0xFF34A853)
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
            }

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "View stats",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
