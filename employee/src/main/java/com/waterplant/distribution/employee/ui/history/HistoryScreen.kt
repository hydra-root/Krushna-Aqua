package com.waterplant.distribution.employee.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waterplant.distribution.core.data.model.Delivery
import com.waterplant.distribution.core.data.model.SyncStatus
import com.waterplant.distribution.core.ui.components.EmptyState
import com.waterplant.distribution.core.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    employeeId: String,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val todayDeliveries by viewModel.getTodayDeliveries(employeeId).collectAsState(initial = emptyList())
    val customers by viewModel.customers.collectAsState(initial = emptyList())
    val custMap = customers.associateBy { it.id }
    var selectedDeliveryForEdit by remember { mutableStateOf<Delivery?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (todayDeliveries.isEmpty()) {
            EmptyState(
                icon = Icons.Default.History,
                message = "No deliveries today yet.\nGo to a customer to record a delivery.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${todayDeliveries.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Text("Entries", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${todayDeliveries.sumOf { it.deliveredJars }}", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary)
                                Text("Delivered", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${todayDeliveries.sumOf { it.returnedJars }}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Text("Returned", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                items(todayDeliveries, key = { it.uuid }) { delivery ->
                    DeliveryHistoryCard(
                        delivery = delivery,
                        customerName = custMap[delivery.customerId]?.name ?: delivery.customerId,
                        onClick = {
                            if (delivery.syncStatus != SyncStatus.SYNCED.name) {
                                selectedDeliveryForEdit = delivery
                            }
                        }
                    )
                }
            }
        }
    }

    selectedDeliveryForEdit?.let { delivery ->
        EditDeliveryDialog(
            delivery = delivery,
            customerName = custMap[delivery.customerId]?.name ?: delivery.customerId,
            onDismiss = { selectedDeliveryForEdit = null },
            onSave = { del, ret, cash ->
                viewModel.updateDelivery(delivery, custMap[delivery.customerId], del, ret, cash)
                selectedDeliveryForEdit = null
            }
        )
    }
}

@Composable
private fun DeliveryHistoryCard(delivery: Delivery, customerName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(customerName, fontWeight = FontWeight.SemiBold)
                Text(
                    DateUtils.formatDateTime(delivery.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (delivery.deliveredJars > 0) {
                    Text("▲ ${delivery.deliveredJars} delivered",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium)
                }
                if (delivery.returnedJars > 0) {
                    Text("▼ ${delivery.returnedJars} returned",
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Sync status indicator
            Text(
                text = if (delivery.syncStatus == SyncStatus.SYNCED.name) "✓" else "⏳",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun EditDeliveryDialog(
    delivery: Delivery,
    customerName: String,
    onDismiss: () -> Unit,
    onSave: (delivered: Int, returned: Int, cashPaid: Double) -> Unit
) {
    var deliveredJars by remember { mutableStateOf(delivery.deliveredJars) }
    var returnedJars by remember { mutableStateOf(delivery.returnedJars) }
    var cashPaid by remember { mutableStateOf(if (delivery.cashPaid > 0) delivery.cashPaid.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Edit Entry: $customerName", fontWeight = FontWeight.Bold)
                Text("Modify delivered and returned jars", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Delivered Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Delivered Jars", fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (deliveredJars > 0) deliveredJars-- }) {
                            Icon(Icons.Default.Remove, "Decrease", tint = MaterialTheme.colorScheme.error)
                        }
                        Text("$deliveredJars", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { deliveredJars++ }) {
                            Icon(Icons.Default.Add, "Increase", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                
                // Returned Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Returned Jars", fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (returnedJars > 0) returnedJars-- }) {
                            Icon(Icons.Default.Remove, "Decrease", tint = MaterialTheme.colorScheme.error)
                        }
                        Text("$returnedJars", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { returnedJars++ }) {
                            Icon(Icons.Default.Add, "Increase", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                
                // Cash Paid
                OutlinedTextField(
                    value = cashPaid,
                    onValueChange = { cashPaid = it },
                    label = { Text("Cash Paid (Rs)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Text("₹", modifier = Modifier.padding(start = 12.dp)) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(deliveredJars, returnedJars, cashPaid.toDoubleOrNull() ?: 0.0) },
                enabled = deliveredJars > 0 || returnedJars > 0 || (cashPaid.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
