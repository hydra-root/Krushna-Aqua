package com.waterplant.distribution.admin.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterplant.distribution.core.data.model.Customer
import com.waterplant.distribution.core.data.model.Delivery
import com.waterplant.distribution.core.data.repository.CustomerRepository
import com.waterplant.distribution.core.data.repository.DeliveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerHistoryViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {

    fun getCustomer(customerId: String): Flow<Customer?> =
        customerRepository.getCustomerByIdFlow(customerId)

    fun getCustomerHistory(customerId: String): Flow<List<Delivery>> =
        deliveryRepository.getDeliveriesByCustomer(customerId)
            
    fun getCustomerJarBalance(customerId: String): Flow<Int> =
        customerRepository.getCustomerByIdFlow(customerId).map { it?.balance ?: 0 }

    fun updateDelivery(delivery: Delivery, newDelivered: Int, newReturned: Int) {
        viewModelScope.launch {
            val updated = delivery.copy(
                deliveredJars = newDelivered,
                returnedJars = newReturned,
                syncStatus = com.waterplant.distribution.core.data.model.SyncStatus.PENDING.name
            )
            deliveryRepository.saveDelivery(updated)
        }
    }
}
