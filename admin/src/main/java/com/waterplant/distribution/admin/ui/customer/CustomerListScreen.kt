package com.waterplant.distribution.admin.ui.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waterplant.distribution.core.data.model.Customer
import com.waterplant.distribution.core.ui.components.EmptyState
import com.waterplant.distribution.core.ui.components.SearchBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    onBack: () -> Unit,
    onAddCustomer: () -> Unit,
    onEditCustomer: (String) -> Unit,
    onNavigateToHistory: (String) -> Unit,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val customers by viewModel.customers.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<Customer?>(null) }

    var selectedTab by remember { mutableStateOf("ALL") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val filtered = customers.filter {
        (selectedTab == "ALL" || it.customerGroup == selectedTab) &&
        (searchQuery.isBlank() || 
         it.name.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Customers (${customers.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCustomer) {
                Icon(Icons.Default.Add, "Add Customer")
            }
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
                    message = if (searchQuery.isNotBlank()) "No customers found" else "No customers in this group."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filtered, key = { it.id }) { customer ->
                        CustomerCard(
                            customer = customer,
                            onCardClick = { onNavigateToHistory(customer.id) },
                            onEdit = { onEditCustomer(customer.id) },
                            onDelete = { showDeleteDialog = customer }
                        )
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { cust ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Customer") },
            text = { Text("Delete ${cust.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    val deletedName = cust.name
                    viewModel.deleteCustomer(cust.id)
                    showDeleteDialog = null
                    scope.launch { snackbarHostState.showSnackbar("✓ $deletedName deleted") }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CustomerCard(
    customer: Customer,
    onCardClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(customer.name, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (customer.customerGroup == "CASH") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (customer.customerGroup == "CASH") "CASH" else "POSTPAID",
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = if (customer.customerGroup == "CASH") MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                if (customer.phone.isNotBlank()) {
                    Text(
                        customer.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    "₹${customer.jarValue.toInt()}/jar • ${customer.balance} jars held",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (customer.balance > 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
