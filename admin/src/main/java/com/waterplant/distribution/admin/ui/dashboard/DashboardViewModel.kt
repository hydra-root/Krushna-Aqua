package com.waterplant.distribution.admin.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterplant.distribution.core.data.model.Customer
import com.waterplant.distribution.core.data.model.Delivery
import com.waterplant.distribution.core.data.model.SyncStatus
import com.waterplant.distribution.core.data.repository.CustomerRepository
import com.waterplant.distribution.core.data.repository.DeliveryRepository
import com.waterplant.distribution.core.data.repository.EmployeeRepository
import com.waterplant.distribution.core.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val customerRepository: CustomerRepository,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {
    val todayDelivered = deliveryRepository.getTodayDeliveredCount()
    val todayReturned = deliveryRepository.getTodayReturnedCount()
    val todayCashCollected = deliveryRepository.getTodayCashCollected()
    val monthlyDelivered = deliveryRepository.getMonthlyDeliveredCount()
    val customerCount = customerRepository.getCustomerCount()
    val employeeCount = employeeRepository.getEmployeeCount()
    val pendingSyncCount = deliveryRepository.getPendingSyncCount()

    val topCustomers = customerRepository.getAllCustomers()
    val recentDeliveries = deliveryRepository.getRecentDeliveries(10)
    val allEmployees = employeeRepository.getAllEmployees()
    val allCustomers = customerRepository.getAllCustomers()

    private val _ipAddress = MutableStateFlow<String?>(null)
    val ipAddress = _ipAddress.asStateFlow()

    fun updateIpAddress(context: Context) {
        _ipAddress.value = NetworkUtils.getLocalIpAddress(context)
    }

    fun addDelivery(customer: Customer, delivered: Int, returned: Int) {
        viewModelScope.launch {
            deliveryRepository.saveDelivery(
                Delivery(
                    uuid = UUID.randomUUID().toString(),
                    customerId = customer.id,
                    employeeId = "admin",
                    deliveredJars = delivered,
                    returnedJars = returned,
                    totalAmount = delivered * customer.jarValue,
                    syncStatus = SyncStatus.SYNCED.name
                )
            )
        }
    }
}
