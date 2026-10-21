package com.waterplant.distribution.admin.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waterplant.distribution.core.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerScreen(
    customerId: String?,
    onBack: () -> Unit,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val isEdit = customerId != null
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var jarValue by remember { mutableStateOf("20.0") }
    var customerGroup by remember { mutableStateOf("POSTPAID") }

    LaunchedEffect(customerId) {
        if (isEdit) viewModel.loadCustomer(customerId!!)
    }
    LaunchedEffect(selectedCustomer) {
        selectedCustomer?.let {
            name = it.name
            phone = it.phone
            address = it.address
            company = it.company
            jarValue = it.jarValue.toString()
            customerGroup = it.customerGroup
        }
    }
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) { viewModel.resetSaveSuccess(); onBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Customer" else "Add Customer") },
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
            // Required
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Customer Name *") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            // Contact info
            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            OutlinedTextField(
                value = company, onValueChange = { company = it },
                label = { Text("Company / Shop Name") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            OutlinedTextField(
                value = address, onValueChange = { address = it },
                label = { Text("Address / Area") },
                modifier = Modifier.fillMaxWidth(), singleLine = false, maxLines = 2
            )

            // Pricing
            OutlinedTextField(
                value = jarValue, onValueChange = { jarValue = it },
                label = { Text("Per Jar Cost (Rs)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Customer Group", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = customerGroup == "POSTPAID",
                    onClick = { customerGroup = "POSTPAID" },
                    label = { Text("Postpaid") }
                )
                FilterChip(
                    selected = customerGroup == "CASH",
                    onClick = { customerGroup = "CASH" },
                    label = { Text("Cash") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = if (isEdit) "Update Customer" else "Add Customer",
                onClick = {
                    if (name.isBlank()) return@PrimaryButton
                    viewModel.saveCustomer(
                        name = name.trim(),
                        phone = phone.trim(),
                        address = address.trim(),
                        company = company.trim(),
                        jarValue = jarValue.toDoubleOrNull() ?: 20.0,
                        customerGroup = customerGroup,
                        id = customerId
                    )
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
