package com.waterplant.distribution.employee.ui.customers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waterplant.distribution.core.data.model.Customer
import com.waterplant.distribution.core.ui.components.EmptyState
import com.waterplant.distribution.core.ui.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    employeeId: String,
    onNavigateToHistory: () -> Unit,
    onNavigateToSync: () -> Unit,
    onLogout: () -> Unit,
    viewModel: CustomerListViewModel = hiltViewModel()
) {
    val customers by viewModel.getCustomersForEmployee(employeeId).collectAsState(initial = emptyList())
    val yesterdayMap by viewModel.getYesterdayDeliveredMap(employeeId).collectAsState(initial = emptyMap())
    var searchQuery by remember { mutableStateOf("") }
    val pendingCount by viewModel.pendingSyncCount.collectAsState(initial = 0)
    var selectedCustomerForEntry by remember { mutableStateOf<Customer?>(null) }

    var selectedTab by remember { mutableStateOf("ALL") }
    
    val filtered = customers.filter {
        (selectedTab == "ALL" || it.customerGroup == selectedTab) &&
        (searchQuery.isBlank() || 
         it.name.contains(searchQuery, ignoreCase = true))
    }

    // Snackbar for delivery confirmation
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Customers", fontWeight = FontWeight.Bold)
                        Text("Employee: $employeeId", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                actions = {
                    // Pending sync badge
                    BadgedBox(
                        badge = {
                            if (pendingCount > 0) {
                                Badge { Text("$pendingCount") }
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToSync) {
                            Icon(Icons.Default.Sync, "Sync")
                        }
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, "History")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout")
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
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search customers...",
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ScrollableTabRow(
                selectedTabIndex = when(selectedTab) {
                    "ALL" -> 0
                    "POSTPAID" -> 1
                    "CASH" -> 2
                    else -> 0
                },
                edgePadding = 0.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                listOf("ALL", "POSTPAID", "CASH").forEachIndexed { index, tabName ->
                    Tab(
                        selected = selectedTab == tabName,
                        onClick = { selectedTab = tabName },
                        text = { Text(if (tabName == "ALL") "All" else if (tabName == "POSTPAID") "Postpaid" else "Cash") }
                    )
                }
            }

            if (filtered.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.PersonSearch,
                    message = if (searchQuery.isNotBlank()) "No customers found"
                    else "No customers in this group."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filtered, key = { it.id }) { customer ->
                        CustomerRow(
                            customer = customer,
                            yesterdayDelivered = yesterdayMap[customer.id] ?: 0,
                            onClick = { selectedCustomerForEntry = customer }
                        )
                    }
                }
            }
        }
    }

    selectedCustomerForEntry?.let { customer ->
        QuickDeliveryDialog(
            customer = customer,
            yesterdayDelivered = yesterdayMap[customer.id] ?: 0,
            onDismiss = { selectedCustomerForEntry = null },
            onSave = { del, ret, cash ->
                viewModel.saveDelivery(employeeId, customer, del, ret, cash)
                selectedCustomerForEntry = null
                scope.launch { snackbarHostState.showSnackbar("✓ Entry saved for ${customer.name}") }
            }
        )
    }
}

@Composable
private fun CustomerRow(
    customer: Customer,
    yesterdayDelivered: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    val balanceColor = when {
                        customer.balance == 0 -> com.waterplant.distribution.core.ui.theme.SuccessGreen
                        customer.balance in 1..5 -> com.waterplant.distribution.core.ui.theme.WarningAmber
                        else -> com.waterplant.distribution.core.ui.theme.ErrorRed
                    }
                    
                    Surface(
                        color = balanceColor.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${customer.balance} JARS",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = balanceColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = "Phone", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = customer.phone.ifBlank { "N/A" }, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Yesterday delivery + collection info
                if (yesterdayDelivered > 0 || customer.balance > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (yesterdayDelivered > 0) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(14.dp), tint = com.waterplant.distribution.core.ui.theme.PrimaryBlue)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Yesterday: $yesterdayDelivered delivered",
                                fontSize = 12.sp,
                                color = com.waterplant.distribution.core.ui.theme.PrimaryBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (yesterdayDelivered > 0 && customer.balance > 0) {
                            Text(" • ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (customer.balance > 0) {
                            Text(
                                text = "Collect: ${customer.balance} jars",
                                fontSize = 12.sp,
                                color = com.waterplant.distribution.core.ui.theme.WarningAmber,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickDeliveryDialog(
    customer: Customer,
    yesterdayDelivered: Int = 0,
    onDismiss: () -> Unit,
    onSave: (delivered: Int, returned: Int, cashPaid: Double) -> Unit
) {
    var deliveredJars by remember { mutableStateOf(0) }
    var returnedJars by remember { mutableStateOf(0) }
    var cashPaid by remember { mutableStateOf("") }
    
    val currentBalance = customer.balance
    val previewBalance = currentBalance + deliveredJars - returnedJars

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(customer.name, style = MaterialTheme.typography.displaySmall)

            Spacer(modifier = Modifier.height(8.dp))

            // Yesterday info + Collection target
            if (yesterdayDelivered > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = com.waterplant.distribution.core.ui.theme.PrimaryBlueLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Inventory2, contentDescription = null,
                            tint = com.waterplant.distribution.core.ui.theme.PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Yesterday delivered: $yesterdayDelivered jars",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = com.waterplant.distribution.core.ui.theme.PrimaryBlue
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (currentBalance > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = com.waterplant.distribution.core.ui.theme.WarningAmberLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AssignmentReturn, contentDescription = null,
                            tint = com.waterplant.distribution.core.ui.theme.WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "To collect: $currentBalance empty jars",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = com.waterplant.distribution.core.ui.theme.WarningAmber
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delivered
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Delivered", style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledIconButton(
                        onClick = { if (deliveredJars > 0) deliveredJars-- },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Remove, "Decrease")
                    }
                    Text(
                        text = "$deliveredJars", 
                        style = MaterialTheme.typography.displayLarge, 
                        modifier = Modifier.padding(horizontal = 16.dp).width(60.dp),
                        textAlign = TextAlign.Center
                    )
                    FilledIconButton(
                        onClick = { deliveredJars++ },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Add, "Increase")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Returned (Collected empty jars)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Returned", style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledIconButton(
                        onClick = { if (returnedJars > 0) returnedJars-- },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Remove, "Decrease")
                    }
                    Text(
                        text = "$returnedJars", 
                        style = MaterialTheme.typography.displayLarge, 
                        modifier = Modifier.padding(horizontal = 16.dp).width(60.dp),
                        textAlign = TextAlign.Center
                    )
                    FilledIconButton(
                        onClick = { returnedJars++ },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Add, "Increase")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Balance preview
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (previewBalance > 5) 
                        com.waterplant.distribution.core.ui.theme.ErrorRedLight
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("New Balance", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "$previewBalance jars",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (previewBalance > 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Cash
            OutlinedTextField(
                value = cashPaid,
                onValueChange = { cashPaid = it },
                label = { Text("Cash Paid (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Text("₹", modifier = Modifier.padding(start = 12.dp)) },
                shape = MaterialTheme.shapes.small,
                textStyle = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onSave(deliveredJars, returnedJars, cashPaid.toDoubleOrNull() ?: 0.0) },
                enabled = deliveredJars > 0 || returnedJars > 0 || (cashPaid.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier.fillMaxWidth().height(72.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("SAVE ENTRY", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
