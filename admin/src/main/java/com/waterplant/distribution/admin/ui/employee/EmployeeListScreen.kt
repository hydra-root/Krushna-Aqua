package com.waterplant.distribution.admin.ui.employee

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.waterplant.distribution.core.data.model.Employee
import com.waterplant.distribution.core.ui.components.EmptyState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(
    onBack: () -> Unit,
    onAddEmployee: () -> Unit,
    onEditEmployee: (String) -> Unit,
    viewModel: EmployeeViewModel = hiltViewModel()
) {
    val employees by viewModel.employees.collectAsState(initial = emptyList())
    var showDeleteDialog by remember { mutableStateOf<Employee?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Employees") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEmployee) {
                Icon(Icons.Default.Add, "Add Employee")
            }
        }
    ) { padding ->
        if (employees.isEmpty()) {
            EmptyState(
                icon = Icons.Default.PersonOff,
                message = "No employees added yet.\nTap + to add your first employee.",
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
                items(employees, key = { it.id }) { employee ->
                    EmployeeCard(
                        employee = employee,
                        onEdit = { onEditEmployee(employee.id) },
                        onToggleActive = {
                            viewModel.toggleActive(employee.id, !employee.active)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (employee.active) "${employee.name} deactivated" else "${employee.name} activated"
                                )
                            }
                        },
                        onDelete = { showDeleteDialog = employee }
                    )
                }
            }
        }
    }

    showDeleteDialog?.let { emp ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Employee") },
            text = { Text("Are you sure you want to delete ${emp.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    val deletedName = emp.name
                    viewModel.deleteEmployee(emp.id)
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
private fun EmployeeCard(
    employee: Employee,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (employee.active)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "ID: ${employee.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = if (employee.active) "● Active" else "● Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (employee.active)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onToggleActive) {
                Icon(
                    if (employee.active) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                    contentDescription = "Toggle Active",
                    tint = if (employee.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
