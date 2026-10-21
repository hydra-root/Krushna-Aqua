package com.waterplant.distribution.admin.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterplant.distribution.core.data.model.Customer
import com.waterplant.distribution.core.data.model.Employee
import com.waterplant.distribution.core.data.repository.CustomerRepository
import com.waterplant.distribution.core.data.repository.EmployeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val employeeRepository: EmployeeRepository
) : ViewModel() {

    val customers = customerRepository.getAllCustomers()
    val employees = employeeRepository.getActiveEmployees()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer = _selectedCustomer.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess = _saveSuccess.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun loadCustomer(id: String) {
        viewModelScope.launch {
            _selectedCustomer.value = customerRepository.getCustomerById(id)
        }
    }

    fun saveCustomer(
        name: String,
        phone: String = "",
        address: String = "",
        company: String = "",
        jarValue: Double,
        customerGroup: String,
        id: String? = null
    ) {
        viewModelScope.launch {
            val existing = if (id != null) customerRepository.getCustomerById(id) else null
            val customer = Customer(
                id = id ?: UUID.randomUUID().toString().take(8).uppercase(),
                name = name,
                phone = phone,
                address = address,
                company = company,
                jarValue = jarValue,
                customerGroup = customerGroup,
                balance = existing?.balance ?: 0,
                employeeId = existing?.employeeId ?: "",
                area = existing?.area ?: ""
            )
            if (id != null) {
                customerRepository.updateCustomer(customer)
            } else {
                customerRepository.saveCustomer(customer)
            }
            _saveSuccess.value = true
        }
    }

    fun deleteCustomer(id: String) {
        viewModelScope.launch { customerRepository.deleteCustomer(id) }
    }

    fun resetSaveSuccess() { _saveSuccess.value = false }
}
