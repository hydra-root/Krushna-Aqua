package com.waterplant.distribution.admin.ui.backup

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.waterplant.distribution.core.backup.BackupManager
import com.waterplant.distribution.core.ui.components.PrimaryButton
import com.waterplant.distribution.core.ui.theme.SuccessGreen
import com.waterplant.distribution.core.ui.theme.WarningAmber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val backupManager = remember { BackupManager() }
    var backupFiles by remember { mutableStateOf(backupManager.getBackupFiles(context)) }
    var showRestoreDialog by remember { mutableStateOf<File?>(null) }
    
    val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    var perJarCost by remember { mutableStateOf(sharedPrefs.getFloat("per_jar_cost", 20f).toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Backup") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("General Settings", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = perJarCost,
                        onValueChange = { 
                            perJarCost = it
                            sharedPrefs.edit().putFloat("per_jar_cost", it.toFloatOrNull() ?: 20f).apply()
                        },
                        label = { Text("Default Per Jar Cost (Rs)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This is the default value when adding a new customer.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Change Admin PIN
            var currentPin by remember { mutableStateOf("") }
            var newPin by remember { mutableStateOf("") }
            var confirmPin by remember { mutableStateOf("") }
            var pinError by remember { mutableStateOf("") }
            var pinSuccess by remember { mutableStateOf(false) }
            val savedPin = sharedPrefs.getString("admin_pin", "1234") ?: "1234"

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Change Admin PIN", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = { currentPin = it; pinError = ""; pinSuccess = false },
                        label = { Text("Current PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it; pinError = ""; pinSuccess = false },
                        label = { Text("New PIN (4+ digits)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { confirmPin = it; pinError = ""; pinSuccess = false },
                        label = { Text("Confirm New PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    if (pinError.isNotEmpty()) {
                        Text(pinError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (pinSuccess) {
                        Text("✓ PIN changed successfully", color = SuccessGreen, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = {
                            when {
                                currentPin != savedPin -> pinError = "Current PIN is incorrect"
                                newPin.length < 4 -> pinError = "PIN must be at least 4 digits"
                                newPin != confirmPin -> pinError = "PINs do not match"
                                else -> {
                                    sharedPrefs.edit().putString("admin_pin", newPin).apply()
                                    currentPin = ""; newPin = ""; confirmPin = ""
                                    pinSuccess = true
                                }
                            }
                        },
                        enabled = currentPin.isNotEmpty() && newPin.isNotEmpty() && confirmPin.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change PIN")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Data Management", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            PrimaryButton(
                text = "💾  Create Backup Now",
                onClick = {
                    val result = backupManager.backupDatabase(context)
                    result.onSuccess {
                        Toast.makeText(context, "Backup saved: ${it.name}", Toast.LENGTH_LONG).show()
                        backupFiles = backupManager.getBackupFiles(context)
                    }
                    result.onFailure {
                        Toast.makeText(context, "Backup failed: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Previous Backups", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

            if (backupFiles.isEmpty()) {
                Text(
                    "No backups found",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    backupFiles.forEach { file ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(file.name, fontWeight = FontWeight.Medium)
                                    Text(
                                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                            .format(Date(file.lastModified())),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        "${file.length() / 1024} KB",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = { showRestoreDialog = file }) {
                                    Icon(Icons.Default.Restore, "Restore", tint = WarningAmber)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    showRestoreDialog?.let { file ->
        AlertDialog(
            onDismissRequest = { showRestoreDialog = null },
            title = { Text("Restore Backup") },
            text = { Text("Restore from ${file.name}?\n\nThis will REPLACE the current database. The app will need to be restarted.") },
            confirmButton = {
                TextButton(onClick = {
                    val result = backupManager.restoreDatabase(context, file)
                    result.onSuccess {
                        Toast.makeText(context, "Database restored. Please restart the app.", Toast.LENGTH_LONG).show()
                    }
                    result.onFailure {
                        Toast.makeText(context, "Restore failed: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                    showRestoreDialog = null
                }) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = null }) { Text("Cancel") }
            }
        )
    }
}
