package com.waterplant.distribution.admin.ui.employee

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waterplant.distribution.core.ui.components.PrimaryButton
import com.waterplant.distribution.core.ui.components.PinField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEmployeeScreen(
    employeeId: String?,
    onBack: () -> Unit,
    viewModel: EmployeeViewModel = hiltViewModel()
) {
    val isEdit = employeeId != null
    val selectedEmployee by viewModel.selectedEmployee.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var name by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    LaunchedEffect(employeeId) {
        if (isEdit && employeeId != null) {
            viewModel.loadEmployee(employeeId)
        }
    }

    LaunchedEffect(selectedEmployee) {
        selectedEmployee?.let {
            name = it.name
            // Do not populate PIN when editing
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSaveSuccess()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Employee" else "Add Employee") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isEdit) {
                OutlinedTextField(
                    value = employeeId ?: "",
                    onValueChange = {},
                    label = { Text("Employee ID") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Employee Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            PinField(
                value = pin,
                onValueChange = { pin = it },
                label = "Login PIN"
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = if (isEdit) "Update Employee" else "Add Employee",
                onClick = {
                    if (name.isNotBlank() && (isEdit || pin.length >= 4)) {
                        viewModel.saveEmployee(name.trim(), pin, employeeId)
                    }
                },
                enabled = name.isNotBlank() && (isEdit || pin.length >= 4),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
