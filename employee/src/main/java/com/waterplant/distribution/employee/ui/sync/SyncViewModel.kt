package com.waterplant.distribution.employee.ui.sync

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterplant.distribution.core.data.repository.CustomerRepository
import com.waterplant.distribution.core.data.repository.DeliveryRepository
import com.waterplant.distribution.core.network.SyncClient
import com.waterplant.distribution.core.network.SyncResponse
import com.waterplant.distribution.core.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SyncState {
    data object Idle : SyncState()
    data object Syncing : SyncState()
    data class Success(val response: SyncResponse) : SyncState()
    data class Error(val message: String) : SyncState()
}

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val customerRepository: CustomerRepository,
    private val syncClient: SyncClient
) : ViewModel() {

    val pendingCount = deliveryRepository.getPendingSyncCount()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState = _syncState.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _customIp = MutableStateFlow("")
    val customIp = _customIp.asStateFlow()

    fun setCustomIp(ip: String) {
        _customIp.value = ip
    }

    fun checkConnection(context: Context) {
        viewModelScope.launch {
            val gateway = if (_customIp.value.isNotBlank()) _customIp.value else NetworkUtils.getGatewayIp(context)
            if (gateway != null) {
                // If manual IP is provided, we don't strictly require Wi-Fi connected check from NetworkUtils
                // as the user might be on a different network type but still reachable.
                _isConnected.value = syncClient.ping(gateway)
            } else {
                _isConnected.value = false
            }
        }
    }

    fun sync(context: Context, employeeId: String) {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing

            val gateway = if (_customIp.value.isNotBlank()) _customIp.value else NetworkUtils.getGatewayIp(context)
            if (gateway == null) {
                _syncState.value = SyncState.Error("Cannot detect Plant Phone. Connect to the hotspot or enter IP manually.")
                return@launch
            }

            // Sync customers from Admin -> Employee
            try {
                val customers = syncClient.downloadCustomers(gateway, employeeId)
                customers.forEach { customer ->
                    customerRepository.saveCustomer(customer)
                }
                
                // Also sync deliveries from Admin -> Employee
                val adminDeliveries = syncClient.downloadDeliveries(gateway)
                val incoming = adminDeliveries.map {
                    it.copy(syncStatus = com.waterplant.distribution.core.data.model.SyncStatus.SYNCED.name)
                }
                deliveryRepository.mergeIncomingDeliveries(incoming)
            } catch (e: Exception) {
                // Ignore download error if it fails, continue to delivery upload sync
            }

            // Sync deliveries from Employee -> Admin
            val response = syncClient.syncDeliveries(gateway, employeeId)
            if (response.success) {
                _syncState.value = SyncState.Success(response)
            } else {
                _syncState.value = SyncState.Error(response.message)
            }
        }
    }
}
