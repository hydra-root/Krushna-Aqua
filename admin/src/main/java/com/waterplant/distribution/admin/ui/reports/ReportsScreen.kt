package com.waterplant.distribution.admin.ui.reports

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.waterplant.distribution.core.ui.components.ReportCard
import com.waterplant.distribution.core.ui.theme.*
import com.waterplant.distribution.core.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val message by viewModel.message.collectAsState()

    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
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
                .padding(Dimens.grid2)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.grid2)
        ) {
            Text("PDF Reports", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            ReportCard(
                title = "Daily Report",
                description = "Summary of today's deliveries and returns",
                icon = Icons.Default.Today,
                onGenerate = { viewModel.generateDailyReport(context) }
            )
            ReportCard(
                title = "Monthly Report",
                description = "Complete overview of the month's activities",
                icon = Icons.Default.DateRange,
                onGenerate = { viewModel.generateMonthlyReport(context) }
            )
            ReportCard(
                title = "Customer Ledger",
                description = "Detailed statement for customers",
                icon = Icons.Default.Person,
                onGenerate = { viewModel.generateCustomerReport(context) }
            )
            ReportCard(
                title = "Employee Performance",
                description = "Delivery stats per employee",
                icon = Icons.Default.Badge,
                onGenerate = { viewModel.generateEmployeeReport(context) }
            )
            ReportCard(
                title = "Outstanding Jars",
                description = "List of customers holding jars",
                icon = Icons.Default.Warning,
                onGenerate = { viewModel.generateOutstandingReport(context) }
            )

            Spacer(modifier = Modifier.height(Dimens.grid2))
            Text("Excel (CSV) Exports", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            ReportCard(
                title = "Daily Data (CSV)",
                description = "Raw spreadsheet data for today",
                icon = Icons.Default.TableChart,
                onGenerate = { viewModel.generateDailyReportCsv(context) }
            )
            ReportCard(
                title = "Monthly Data (CSV)",
                description = "Raw spreadsheet data for the month",
                icon = Icons.Default.TableChart,
                onGenerate = { viewModel.generateMonthlyReportCsv(context) }
            )
            ReportCard(
                title = "Outstanding Data (CSV)",
                description = "Raw spreadsheet data of pending jars",
                icon = Icons.Default.TableChart,
                onGenerate = { viewModel.generateOutstandingReportCsv(context) }
            )
        }
    }
}

