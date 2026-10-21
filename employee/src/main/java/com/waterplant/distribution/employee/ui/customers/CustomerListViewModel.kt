package com.waterplant.distribution.employee.ui.customers

import androidx.lifecycle.ViewModel
import com.waterplant.distribution.core.data.model.Customer
import com.waterplant.distribution.core.data.model.Delivery
import com.waterplant.distribution.core.data.repository.CustomerRepository
import com.waterplant.distribution.core.data.repository.DeliveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

/**
 * ViewModel orchestrating the delivery route view, customer balance tracking,
 * and delivery creation with yesterday-vs-today jar balance reconciliation.
 *
 * @author Pavan Pawar
 */
@HiltViewModel
class CustomerListViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    val pendingSyncCount = deliveryRepository.getPendingSyncCount()

    fun getCustomersForEmployee(employeeId: String): Flow<List<Customer>> =
        customerRepository.getAllCustomers()

    /**
     * Returns a map of customerId → total jars delivered yesterday.
     * Employee can see: "Yesterday I gave this customer X jars"
     */
    fun getYesterdayDeliveredMap(employeeId: String): Flow<Map<String, Int>> =
        deliveryRepository.getYesterdayDeliveriesByEmployee(employeeId)
            .map { deliveries ->
                deliveries.groupBy { it.customerId }
                    .mapValues { (_, dels) -> dels.sumOf { it.deliveredJars } }
            }

    fun saveDelivery(employeeId: String, customer: Customer, deliveredJars: Int, returnedJars: Int, cashPaid: Double) {
        viewModelScope.launch {
            val delivery = Delivery(
                uuid = java.util.UUID.randomUUID().toString(),
                employeeId = employeeId,
                customerId = customer.id,
                deliveredJars = deliveredJars,
                returnedJars = returnedJars,
                cashPaid = cashPaid,
                totalAmount = deliveredJars * customer.jarValue,
                createdAt = System.currentTimeMillis(),
                syncStatus = com.waterplant.distribution.core.data.model.SyncStatus.PENDING.name
            )
            deliveryRepository.saveDelivery(delivery)
        }
    }
}
