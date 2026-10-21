package com.waterplant.distribution.admin.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waterplant.distribution.core.data.model.Customer
import com.waterplant.distribution.core.data.model.Delivery
import com.waterplant.distribution.core.data.model.Employee
import com.waterplant.distribution.core.ui.components.SummaryCard
import com.waterplant.distribution.core.ui.theme.*
import com.waterplant.distribution.core.utils.DateUtils
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToEmployees: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToCustomerHistory: (String) -> Unit = {},
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val todayDelivered by viewModel.todayDelivered.collectAsState(initial = 0)
    val todayReturned by viewModel.todayReturned.collectAsState(initial = 0)
    val todayCash by viewModel.todayCashCollected.collectAsState(initial = 0.0)
    val pendingSync by viewModel.pendingSyncCount.collectAsState(initial = 0)
    
    val topCustomers by viewModel.topCustomers.collectAsState(initial = emptyList())
    val recentDeliveries by viewModel.recentDeliveries.collectAsState(initial = emptyList())
    val employees by viewModel.allEmployees.collectAsState(initial = emptyList())
    val allCustomers by viewModel.allCustomers.collectAsState(initial = emptyList())
    val outstandingTotal = topCustomers.sumOf { it.balance }.coerceAtLeast(0)
    
    // Lookup maps for resolving IDs to names
    val employeeNameMap = employees.associateBy({ it.id }, { it.name })
    val customerNameMap = allCustomers.associateBy({ it.id }, { it.name })
    
    var showAddBottomSheet by remember { mutableStateOf(false) }
    var showCustomerSelection by remember { mutableStateOf(false) }
    var selectedCustomerForDelivery by remember { mutableStateOf<Customer?>(null) }
    val context = LocalContext.current
    
    // Snackbar host for confirmations
    val snackbarHostState = remember { SnackbarHostState() }

    // Time-aware greeting
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(greeting, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(DateUtils.formatDate(System.currentTimeMillis()), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    Box(modifier = Modifier
                        .padding(end = 16.dp)
                        .clip(CircleShape)
                        .background(if (pendingSync == 0) SuccessGreenLight else WarningAmberLight)
                        .padding(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CloudSync, 
                            contentDescription = "Sync Status",
                            tint = if (pendingSync == 0) SuccessGreen else WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddBottomSheet = true },
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { Text("Add") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            
            // 1. Hero Metrics Section
            item {
                Column {
                    Text("Today's Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Delivered Today",
                            value = todayDelivered.toString(),
                            icon = Icons.Default.LocalShipping,
                            color = PrimaryBlueLight
                        )
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Returned Today",
                            value = todayReturned.toString(),
                            icon = Icons.AutoMirrored.Filled.KeyboardReturn,
                            color = SuccessGreenLight
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Outstanding Jars",
                            value = outstandingTotal.toString(),
                            icon = Icons.Default.ErrorOutline,
                            color = WarningAmberLight
                        )
                        SummaryCard(
                            modifier = Modifier.weight(1f),
                            title = "Cash Collected",
                            value = "₹${String.format(Locale.getDefault(), "%.0f", todayCash)}",
                            icon = Icons.Default.Payments,
                            color = SuccessGreenLight
                        )
                    }
                }
            }

            // 2. Quick Actions Section
            item {
                Column {
                    Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            label = "Add Customer",
                            icon = Icons.Default.PersonAdd,
                            onClick = onNavigateToCustomers
                        )
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            label = "Add Employee",
                            icon = Icons.Default.Badge,
                            onClick = onNavigateToEmployees
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            label = "Monthly Report",
                            icon = Icons.Default.Assessment,
                            onClick = onNavigateToReports
                        )
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            label = "Backup Data",
                            icon = Icons.Default.Save,
                            onClick = onNavigateToBackup
                        )
                    }
                }
            }

            // 3. Business Insights Section (Need Attention)
            if (topCustomers.any { it.balance > 0 }) {
                item {
                    Column {
                        Text("Need Attention", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Customers holding the most jars", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                topCustomers.filter { it.balance > 0 }
                                    .sortedByDescending { it.balance }
                                    .take(5)
                                    .forEachIndexed { index, customer ->
                                    OutstandingCustomerRow(
                                        customer = customer,
                                        onViewClick = { onNavigateToCustomerHistory(customer.id) }
                                    )
                                    if (index < 4 && index < topCustomers.filter { it.balance > 0 }.size - 1) {
                                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Employee Sync Status
            if (employees.isNotEmpty()) {
                item {
                    Column {
                        Text("Employee Sync Status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        employees.forEach { emp ->
                            val lastDelivery = recentDeliveries.firstOrNull { it.employeeId == emp.id }
                            EmployeeSyncCard(emp, lastDelivery)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // 5. Recent Activity Feed
            if (recentDeliveries.isNotEmpty()) {
                item {
                    Column {
                        Text("Recent Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        recentDeliveries.take(5).forEach { delivery ->
                            ActivityTimelineItem(
                                delivery = delivery,
                                employeeName = employeeNameMap[delivery.employeeId] ?: delivery.employeeId,
                                customerName = customerNameMap[delivery.customerId] ?: delivery.customerId
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
        
        if (showAddBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddBottomSheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(modifier = Modifier.padding(bottom = 32.dp)) {
                    Text("Add New", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                    
                    ListItem(
                        headlineContent = { Text("New Delivery") },
                        leadingContent = { Icon(Icons.Default.LocalShipping, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier.clickable { showAddBottomSheet = false; showCustomerSelection = true }
                    )
                    ListItem(
                        headlineContent = { Text("New Customer") },
                        leadingContent = { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier.clickable { showAddBottomSheet = false; onNavigateToCustomers() }
                    )
                    ListItem(
                        headlineContent = { Text("New Employee") },
                        leadingContent = { Icon(Icons.Default.Badge, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier.clickable { showAddBottomSheet = false; onNavigateToEmployees() }
                    )
                    ListItem(
                        headlineContent = { Text("Generate Report") },
                        leadingContent = { Icon(Icons.Default.Assessment, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier.clickable { showAddBottomSheet = false; onNavigateToReports() }
                    )
                } // Column
            } // ModalBottomSheet
        } // if
    } // Scaffold

    if (showCustomerSelection) {
        CustomerSelectionDialog(
            customers = allCustomers,
            onDismiss = { showCustomerSelection = false },
            onCustomerSelected = { customer ->
                showCustomerSelection = false
                selectedCustomerForDelivery = customer
            }
        )
    }

    selectedCustomerForDelivery?.let { customer ->
        QuickDeliveryDialog(
            customerName = customer.name,
            currentBalance = customer.balance,
            onDismiss = { selectedCustomerForDelivery = null },
            onSave = { delivered, returned ->
                viewModel.addDelivery(customer, delivered, returned)
                selectedCustomerForDelivery = null
            }
        )
    }
}



@Composable
fun OutstandingCustomerRow(customer: Customer, onViewClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(WarningAmberLight),
            contentAlignment = Alignment.Center
        ) {
            Text(customer.name.take(1).uppercase(), color = WarningAmber, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(customer.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${customer.balance} outstanding jars", style = MaterialTheme.typography.bodySmall, color = WarningAmber)
        }
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(onClick = onViewClick) {
            Text("View")
        }
    }
}

@Composable
fun EmployeeSyncCard(employee: Employee, lastDelivery: Delivery?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (lastDelivery != null && System.currentTimeMillis() - lastDelivery.createdAt < 86400000) SuccessGreen else WarningAmber)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(employee.name, fontWeight = FontWeight.SemiBold)
                if (lastDelivery != null) {
                    Text("Synced ${DateUtils.formatDateTime(lastDelivery.createdAt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Pending sync", style = MaterialTheme.typography.bodySmall, color = WarningAmber)
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun ActivityTimelineItem(delivery: Delivery, employeeName: String, customerName: String) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlueLight)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(DateUtils.formatDateTime(delivery.createdAt), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                buildString {
                    append(employeeName)
                    append(" → ")
                    append(customerName)
                    if (delivery.deliveredJars > 0) append(" • ${delivery.deliveredJars} delivered")
                    if (delivery.returnedJars > 0) append(" • ${delivery.returnedJars} returned")
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun CustomerSelectionDialog(
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onCustomerSelected: (Customer) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = customers.filter { it.name.contains(searchQuery, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Customer", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(filtered) { customer ->
                        ListItem(
                            headlineContent = { Text(customer.name) },
                            supportingContent = {
                                Text(
                                    buildString {
                                        if (customer.phone.isNotBlank()) append(customer.phone)
                                        if (customer.balance > 0) {
                                            if (isNotEmpty()) append(" • ")
                                            append("${customer.balance} jars")
                                        }
                                    }.ifEmpty { customer.customerGroup }
                                )
                            },
                            modifier = Modifier.clickable { onCustomerSelected(customer) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun QuickDeliveryDialog(
    customerName: String,
    currentBalance: Int = 0,
    onDismiss: () -> Unit,
    onSave: (delivered: Int, returned: Int) -> Unit
) {
    var deliveredJars by remember { mutableStateOf(0) }
    var returnedJars by remember { mutableStateOf(0) }
    val previewBalance = currentBalance + deliveredJars - returnedJars

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("New Entry: $customerName", fontWeight = FontWeight.Bold)
                if (currentBalance > 0) {
                    Text("Current balance: $currentBalance jars", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Delivered", fontWeight = FontWeight.SemiBold)
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
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Returned", fontWeight = FontWeight.SemiBold)
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

                // Balance preview
                if (deliveredJars > 0 || returnedJars > 0) {
                    HorizontalDivider()
                    Text(
                        "New balance: $previewBalance jars",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (previewBalance > 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(deliveredJars, returnedJars) },
                enabled = deliveredJars > 0 || returnedJars > 0
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
