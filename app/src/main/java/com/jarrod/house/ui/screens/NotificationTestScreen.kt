package com.jarrod.house.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jarrod.house.ui.viewmodel.NotificationTestViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationTestScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationTestViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    
    var title by remember { mutableStateOf("Test Notification") }
    var body by remember { mutableStateOf("This is a test notification") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Test notification form
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Send Test Notification",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Body") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.sendTestNotification(context, title, body)
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Send Test")
                        }
                    }
                }
            }
            
            // Status messages
            uiState.message?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isError) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = if (uiState.isError) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }
            
            // FCM Token info
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "FCM Token Status",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Text(
                        text = "Token: ${uiState.fcmToken?.take(50) ?: "Not available"}...",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.refreshFcmToken(context)
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Refresh Token")
                    }
                }
            }
        }
    }
}