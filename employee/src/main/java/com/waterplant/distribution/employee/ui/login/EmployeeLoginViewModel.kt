package com.waterplant.distribution.employee.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterplant.distribution.core.data.model.Employee
import com.waterplant.distribution.core.data.repository.CustomerRepository
import com.waterplant.distribution.core.data.repository.EmployeeRepository
import com.waterplant.distribution.core.network.SyncClient
import com.waterplant.distribution.core.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data class Success(val employee: Employee) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class EmployeeLoginViewModel @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val customerRepository: CustomerRepository,
    private val syncClient: SyncClient
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    private val _customIp = MutableStateFlow("")
    val customIp = _customIp.asStateFlow()

    fun setCustomIp(ip: String) {
        _customIp.value = ip
    }

    /**
     * Login flow:
     * 1. Try local database first (works offline after first login)
     * 2. If local fails, try remote login against Plant Phone via hotspot
     * 3. On remote success, cache employee + customers locally for future offline login
     */
    fun login(context: Context, employeeId: String, pin: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            // Step 1: Try local login
            val localEmployee = employeeRepository.login(employeeId, pin)
            if (localEmployee != null) {
                _loginState.value = LoginState.Success(localEmployee)
                // Also refresh customers in background if connected
                refreshCustomersInBackground(context, employeeId)
                return@launch
            }

            // Step 2: Try remote login via Plant Phone hotspot
            val gatewayIp = if (_customIp.value.isNotBlank()) _customIp.value else NetworkUtils.getGatewayIp(context)
            if (gatewayIp == null) {
                _loginState.value = LoginState.Error(
                    "Employee not found locally.\nConnect to Plant Phone hotspot or enter IP manually for first-time login."
                )
                return@launch
            }

            val response = syncClient.remoteLogin(gatewayIp, employeeId, pin)
            val emp = response.employee
            if (response.success && emp != null) {
                // Cache employee locally for future offline login
                employeeRepository.saveEmployee(emp)
                // Cache customers locally
                response.customers.forEach { customer ->
                    customerRepository.saveCustomer(customer)
                }
                _loginState.value = LoginState.Success(emp)
            } else {
                _loginState.value = LoginState.Error(response.message)
            }
        }
    }

    private fun refreshCustomersInBackground(context: Context, employeeId: String) {
        viewModelScope.launch {
            try {
                val gatewayIp = if (_customIp.value.isNotBlank()) _customIp.value else NetworkUtils.getGatewayIp(context) ?: return@launch
                if (!syncClient.ping(gatewayIp)) return@launch
                val customers = syncClient.downloadCustomers(gatewayIp, employeeId)
                customers.forEach { customer ->
                    customerRepository.saveCustomer(customer)
                }
            } catch (_: Exception) { /* Ignore — background refresh is best-effort */ }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
