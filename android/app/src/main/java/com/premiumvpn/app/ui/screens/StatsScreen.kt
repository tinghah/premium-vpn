package com.premiumvpn.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.premiumvpn.app.data.local.KeyEntity
import com.premiumvpn.app.data.repository.KeyRepository
import com.premiumvpn.app.domain.model.KeyUsageStats
import com.premiumvpn.app.ui.components.DataUsageCard
import com.premiumvpn.app.util.Formatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val keyRepository: KeyRepository
) : ViewModel() {

    private val _key = MutableStateFlow<KeyEntity?>(null)
    val key: StateFlow<KeyEntity?> = _key.asStateFlow()

    private val _stats = MutableStateFlow<KeyUsageStats?>(null)
    val stats: StateFlow<KeyUsageStats?> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadKey(keyId: String) {
        viewModelScope.launch {
            val found = keyRepository.getKeyById(keyId)
            _key.value = found
            if (found != null) {
                refreshStats(keyId)
            }
        }
    }

    fun refreshStats(keyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            keyRepository.refreshKeyStats(keyId)
                .onSuccess { _stats.value = it }
                .onFailure { /* Use cached stats */ }
            _isLoading.value = false
        }
    }

    fun deleteKey(keyId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            keyRepository.deleteKey(keyId)
            onDeleted()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    keyId: String,
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val key by viewModel.key.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(keyId) {
        viewModel.loadKey(keyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(key?.name ?: "Key Stats") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Key Info Card
            key?.let { k ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = k.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${k.host}:${k.port}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Method: ${k.method}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Data Usage Card (reusable component)
            DataUsageCard(
                bytesUsed = stats?.bytesUsed ?: 0,
                dataLimitBytes = stats?.dataLimitBytes
            )

            // Connection Details Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Connection Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    DetailRow("Tunnel Time", Formatter.formatDuration(stats?.tunnelTimeSeconds ?: 0))
                    DetailRow("Last Active", Formatter.formatTimestamp(stats?.lastTrafficSeen ?: 0))
                    DetailRow("Peak Devices", "${stats?.peakDevices ?: 0}")
                }
            }

            // Refresh button
            OutlinedButton(
                onClick = { stats?.let { viewModel.refreshStats(keyId) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh Stats")
            }

            // Delete button
            OutlinedButton(
                onClick = { viewModel.deleteKey(keyId, onBack) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete Key")
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
