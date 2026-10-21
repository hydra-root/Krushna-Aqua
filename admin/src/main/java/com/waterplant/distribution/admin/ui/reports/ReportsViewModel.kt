package com.waterplant.distribution.admin.ui.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterplant.distribution.core.data.model.Customer
import com.waterplant.distribution.core.data.model.Delivery
import com.waterplant.distribution.core.data.repository.CustomerRepository
import com.waterplant.distribution.core.data.repository.DeliveryRepository
import com.waterplant.distribution.core.data.repository.EmployeeRepository
import com.waterplant.distribution.core.reports.CsvExporter
import com.waterplant.distribution.core.reports.PdfGenerator
import com.waterplant.distribution.core.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val customerRepository: CustomerRepository,
    private val employeeRepository: EmployeeRepository,
    private val pdfGenerator: PdfGenerator,
    private val csvExporter: CsvExporter
) : ViewModel() {

    private val _message = MutableStateFlow("")
    val message = _message.asStateFlow()

    fun clearMessage() { _message.value = "" }

    fun generateDailyReport(context: Context) {
        viewModelScope.launch {
            val deliveries = deliveryRepository.getTodayDeliveries().first()
            val customers = customerRepository.getAllCustomers().first()
            val custMap = customers.associateBy { it.id }

            val headers = listOf("Customer", "Delivered", "Returned")
            val rows = deliveries.map { d ->
                listOf(custMap[d.customerId]?.name ?: d.customerId, "${d.deliveredJars}", "${d.returnedJars}")
            }
            val result = pdfGenerator.generateReport(context, "Daily Report - ${DateUtils.formatDate(System.currentTimeMillis())}", headers, rows, "Daily_Report")
            result.onSuccess { _message.value = "PDF saved: ${it.absolutePath}" }
            result.onFailure { _message.value = "Error: ${it.message}" }
        }
    }

    fun generateMonthlyReport(context: Context) {
        viewModelScope.launch {
            val deliveries = deliveryRepository.getMonthlyDeliveries().first()
            val customers = customerRepository.getAllCustomers().first()
            val custMap = customers.associateBy { it.id }

            // Group by customer
            val grouped = deliveries.groupBy { it.customerId }
            val headers = listOf("Customer", "Delivered", "Returned", "Balance")
            val rows = grouped.map { (custId, dels) ->
                val totalDel = dels.sumOf { it.deliveredJars }
                val totalRet = dels.sumOf { it.returnedJars }
                listOf(custMap[custId]?.name ?: custId, "$totalDel", "$totalRet", "${totalDel - totalRet}")
            }
            val result = pdfGenerator.generateReport(context, "Monthly Report - ${DateUtils.currentMonthYear()}", headers, rows, "Monthly_Report")
            result.onSuccess { _message.value = "PDF saved: ${it.absolutePath}" }
            result.onFailure { _message.value = "Error: ${it.message}" }
        }
    }

    fun generateCustomerReport(context: Context) {
        viewModelScope.launch {
            val customers = customerRepository.getAllCustomers().first()
            val headers = listOf("Customer", "Company", "Jar Balance")
            val rows = customers.map { c ->
                val balance = c.balance
                listOf(c.name, c.company, "$balance")
            }
            val result = pdfGenerator.generateReport(context, "Customer Report", headers, rows, "Customer_Report")
            result.onSuccess { _message.value = "PDF saved: ${it.absolutePath}" }
            result.onFailure { _message.value = "Error: ${it.message}" }
        }
    }

    fun generateEmployeeReport(context: Context) {
        viewModelScope.launch {
            val employees = employeeRepository.getAllEmployees().first()
            val deliveries = deliveryRepository.getAllDeliveries().first()
            val grouped = deliveries.groupBy { it.employeeId }

            val headers = listOf("Employee", "Delivered", "Returned")
            val rows = employees.map { emp ->
                val empDels = grouped[emp.id] ?: emptyList()
                listOf(emp.name, "${empDels.sumOf { it.deliveredJars }}", "${empDels.sumOf { it.returnedJars }}")
            }
            val result = pdfGenerator.generateReport(context, "Employee Report", headers, rows, "Employee_Report")
            result.onSuccess { _message.value = "PDF saved: ${it.absolutePath}" }
            result.onFailure { _message.value = "Error: ${it.message}" }
        }
    }

    fun generateOutstandingReport(context: Context) {
        viewModelScope.launch {
            val customers = customerRepository.getAllCustomers().first()
            val headers = listOf("Customer", "Company", "Outstanding Jars")
            val rows = customers.map { c ->
                val balance = c.balance
                listOf(c.name, c.company, "$balance")
            }.filter { it[2] != "0" }

            val result = pdfGenerator.generateReport(context, "Outstanding Jar Report", headers, rows, "Outstanding_Report")
            result.onSuccess { _message.value = "PDF saved: ${it.absolutePath}" }
            result.onFailure { _message.value = "Error: ${it.message}" }
        }
    }

    fun generateDailyReportCsv(context: Context) {
        viewModelScope.launch {
            val deliveries = deliveryRepository.getTodayDeliveries().first()
            val customers = customerRepository.getAllCustomers().first()
            val custMap = customers.associateBy { it.id }
            val headers = listOf("Customer", "Delivered", "Returned")
            val rows = deliveries.map { d ->
                listOf(custMap[d.customerId]?.name ?: d.customerId, "${d.deliveredJars}", "${d.returnedJars}")
            }
            val result = csvExporter.exportToCsv(context, "Daily_Report", headers, rows)
            result.onSuccess { _message.value = "CSV saved: ${it.absolutePath}" }
            result.onFailure { _message.value = "Error: ${it.message}" }
        }
    }

    fun generateMonthlyReportCsv(context: Context) {
        viewModelScope.launch {
            val deliveries = deliveryRepository.getMonthlyDeliveries().first()
            val customers = customerRepository.getAllCustomers().first()
            val custMap = customers.associateBy { it.id }
            val grouped = deliveries.groupBy { it.customerId }
            val headers = listOf("Customer", "Delivered", "Returned", "Balance")
            val rows = grouped.map { (custId, dels) ->
                val d = dels.sumOf { it.deliveredJars }; val r = dels.sumOf { it.returnedJars }
                listOf(custMap[custId]?.name ?: custId, "$d", "$r", "${d - r}")
            }
            val result = csvExporter.exportToCsv(context, "Monthly_Report", headers, rows)
            result.onSuccess { _message.value = "CSV saved: ${it.absolutePath}" }
            result.onFailure { _message.value = "Error: ${it.message}" }
        }
    }

    fun generateOutstandingReportCsv(context: Context) {
        viewModelScope.launch {
            val customers = customerRepository.getAllCustomers().first()
            val headers = listOf("Customer", "Company", "Outstanding Jars")
            val rows = customers.map { c ->
                val balance = c.balance
                listOf(c.name, c.company, "$balance")
            }.filter { it[2] != "0" }
            val result = csvExporter.exportToCsv(context, "Outstanding_Report", headers, rows)
            result.onSuccess { _message.value = "CSV saved: ${it.absolutePath}" }
            result.onFailure { _message.value = "Error: ${it.message}" }
        }
    }
}
