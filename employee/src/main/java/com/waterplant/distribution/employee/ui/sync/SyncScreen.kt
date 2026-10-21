package com.waterplant.distribution.employee.ui.sync

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waterplant.distribution.core.ui.components.PrimaryButton
import com.waterplant.distribution.core.ui.theme.SuccessGreen
import com.waterplant.distribution.core.ui.theme.ErrorRed
import com.waterplant.distribution.core.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    employeeId: String,
    onBack: () -> Unit,
    viewModel: SyncViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val syncState by viewModel.syncState.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState(initial = 0)
    val isConnected by viewModel.isConnected.collectAsState()
    val customIp by viewModel.customIp.collectAsState()
    
    var showIpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkConnection(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync with Plant") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.grid3),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.grid2)
        ) {
            // Connection status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected)
                        SuccessGreen.copy(alpha = 0.15f)
                    else
                        ErrorRed.copy(alpha = 0.15f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.grid2),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = if (isConnected) SuccessGreen else ErrorRed,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.grid2))
                    Column {
                        Text(
                            if (isConnected) "Plant Phone Detected" else "Not Connected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) SuccessGreen else ErrorRed
                        )
                        Text(
                            if (isConnected) "Ready to sync" else "Connect to Plant Phone hotspot",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Pending records
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.grid3),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Pending Records", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(Dimens.grid1))
                    Text(
                        "$pendingCount",
                        style = MaterialTheme.typography.displayLarge,
                        color = if (pendingCount > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (pendingCount > 0) "records waiting to sync" else "all synced ✓",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.grid1))

            // Sync result
            when (val state = syncState) {
                is SyncState.Idle -> {}
                is SyncState.Syncing -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text("Syncing...", style = MaterialTheme.typography.titleMedium)
                }
                is SyncState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.grid2),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(Dimens.grid1))
                            Text("Sync Complete!", style = MaterialTheme.typography.titleLarge, color = SuccessGreen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Uploaded: ${state.response.uploaded}", style = MaterialTheme.typography.bodyLarge)
                            Text("Imported: ${state.response.imported}", style = MaterialTheme.typography.bodyLarge)
                            Text("Duplicates: ${state.response.duplicates}", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                is SyncState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.grid2),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Error, null, tint = ErrorRed, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(Dimens.grid1))
                            Text("Sync Failed", style = MaterialTheme.typography.titleLarge, color = ErrorRed)
                            Text(state.message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sync button
            PrimaryButton(
                text = "SYNC NOW",
                onClick = { viewModel.sync(context, employeeId) },
                enabled = pendingCount > 0 && syncState !is SyncState.Syncing
            )

            // Refresh connection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = { viewModel.checkConnection(context) }) {
                    Text("Refresh Connection")
                }
                TextButton(onClick = { showIpDialog = true }) {
                    Text(if (customIp.isBlank()) "Enter IP Manually" else "Change IP")
                }
            }
        }
    }

    if (showIpDialog) {
        var tempIp by remember { mutableStateOf(customIp) }
        AlertDialog(
            onDismissRequest = { showIpDialog = false },
            title = { Text("Enter Plant Phone IP") },
            text = {
                Column {
                    Text("Check the Admin app dashboard for the IP address.", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempIp,
                        onValueChange = { tempIp = it },
                        placeholder = { Text("e.g. 192.168.43.1") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.setCustomIp(tempIp)
                    viewModel.checkConnection(context)
                    showIpDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
