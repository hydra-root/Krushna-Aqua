package com.waterplant.distribution.employee.ui.history

import androidx.lifecycle.ViewModel
import com.waterplant.distribution.core.data.model.Delivery
import com.waterplant.distribution.core.data.repository.CustomerRepository
import com.waterplant.distribution.core.data.repository.DeliveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    val customers = customerRepository.getAllCustomers()

    fun getTodayDeliveries(employeeId: String): Flow<List<Delivery>> =
        deliveryRepository.getTodayDeliveriesByEmployee(employeeId)

    fun updateDelivery(delivery: Delivery, customer: com.waterplant.distribution.core.data.model.Customer?, newDelivered: Int, newReturned: Int, newCashPaid: Double) {
        viewModelScope.launch {
            val updated = delivery.copy(
                deliveredJars = newDelivered,
                returnedJars = newReturned,
                cashPaid = newCashPaid,
                totalAmount = newDelivered * (customer?.jarValue ?: 20.0),
                syncStatus = com.waterplant.distribution.core.data.model.SyncStatus.PENDING.name
            )
            deliveryRepository.saveDelivery(updated)
        }
    }
}
