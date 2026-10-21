package com.waterplant.distribution.admin.ui.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waterplant.distribution.core.data.model.Delivery
import com.waterplant.distribution.core.ui.components.EmptyState
import com.waterplant.distribution.core.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHistoryScreen(
    customerId: String,
    onBack: () -> Unit,
    onNavigateToPreview: (String) -> Unit,
    viewModel: CustomerHistoryViewModel = hiltViewModel()
) {
    val customer by viewModel.getCustomer(customerId).collectAsState(initial = null)
    val history by viewModel.getCustomerHistory(customerId).collectAsState(initial = emptyList())
    val balance by viewModel.getCustomerJarBalance(customerId).collectAsState(initial = 0)
    var selectedDeliveryForEdit by remember { mutableStateOf<Delivery?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(customer?.name ?: "Customer History")
                        if (customer?.company?.isNotBlank() == true) {
                            Text(customer!!.company, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    val currentCustomer = customer
                    if (currentCustomer != null) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        
                        IconButton(onClick = {
                            val totalDel = history.sumOf { it.deliveredJars }
                            val totalRet = history.sumOf { it.returnedJars }
                            val totalAmount = history.sumOf { it.totalAmount }
                            val totalPaid = history.sumOf { it.cashPaid }
                            val cachePath = com.waterplant.distribution.admin.utils.PdfGenerator.generateCustomerBill(
                                context, currentCustomer, totalDel, totalRet, totalAmount, totalPaid
                            )
                            if (cachePath != null) {
                                onNavigateToPreview(cachePath)
                            } else {
                                android.widget.Toast.makeText(context, "Failed to generate preview", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Share, "Generate Bill")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Current Jar Balance", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "$balance",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text("Delivery History", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

            if (history.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.History,
                    message = "No deliveries recorded for this customer."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(history, key = { it.uuid }) { delivery ->
                        AdminDeliveryHistoryCard(
                            delivery = delivery,
                            onClick = { selectedDeliveryForEdit = delivery }
                        )
                    }
                }
            }
        }
    }

    selectedDeliveryForEdit?.let { delivery ->
        AdminEditDeliveryDialog(
            delivery = delivery,
            customerName = customer?.name ?: "Customer",
            onDismiss = { selectedDeliveryForEdit = null },
            onSave = { del, ret ->
                viewModel.updateDelivery(delivery, del, ret)
                selectedDeliveryForEdit = null
            }
        )
    }
}

@Composable
private fun AdminDeliveryHistoryCard(delivery: Delivery, onClick: () -> Unit) {
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
                Text(
                    DateUtils.formatDateTime(delivery.createdAt),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Employee: ${delivery.employeeId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
        }
    }
}

@Composable
fun AdminEditDeliveryDialog(
    delivery: Delivery,
    customerName: String,
    onDismiss: () -> Unit,
    onSave: (delivered: Int, returned: Int) -> Unit
) {
    var deliveredJars by remember { mutableStateOf(delivery.deliveredJars) }
    var returnedJars by remember { mutableStateOf(delivery.returnedJars) }

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
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(deliveredJars, returnedJars) },
                enabled = deliveredJars > 0 || returnedJars > 0
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
