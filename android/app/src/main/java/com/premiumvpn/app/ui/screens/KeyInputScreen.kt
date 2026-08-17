package com.premiumvpn.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.premiumvpn.app.data.repository.KeyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeyInputViewModel @Inject constructor(
    private val keyRepository: KeyRepository
) : ViewModel() {

    private val _isAdding = MutableStateFlow(false)
    val isAdding: StateFlow<Boolean> = _isAdding.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun addKey(ssUrl: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isAdding.value = true
            _error.value = null

            keyRepository.addKey(ssUrl, name)
                .onSuccess { onSuccess() }
                .onFailure { _error.value = it.message ?: "Failed to add key" }

            _isAdding.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyInputScreen(
    onBack: () -> Unit,
    onKeyAdded: () -> Unit,
    viewModel: KeyInputViewModel = hiltViewModel()
) {
    var ssUrl by remember { mutableStateOf("") }
    var keyName by remember { mutableStateOf("") }
    val isAdding by viewModel.isAdding.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add VPN Key") },
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
            Text(
                text = "Paste your Outline access key below",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = ssUrl,
                onValueChange = { ssUrl = it },
                label = { Text("Access Key (ss://...)") },
                placeholder = { Text("ss://YWJjZGVm...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            OutlinedTextField(
                value = keyName,
                onValueChange = { keyName = it },
                label = { Text("Key Name (optional)") },
                placeholder = { Text("My US Server") },
                modifier = Modifier.fillMaxWidth()
            )

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { viewModel.addKey(ssUrl, keyName, onKeyAdded) },
                modifier = Modifier.fillMaxWidth(),
                enabled = ssUrl.isNotBlank() && !isAdding
            ) {
                if (isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.ContentPaste, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Key")
                }
            }

            HorizontalDivider()

            Text(
                text = "Supported formats:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = """• ss://BASE64(method:password)@host:port/?outline=1
• ss://method:password@host:port/?outline=1
• QR code scan (coming soon)""",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
