package com.waterplant.distribution.admin.ui.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waterplant.distribution.core.data.model.Employee
import com.waterplant.distribution.core.data.repository.EmployeeRepository
import com.waterplant.distribution.core.utils.HashUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val repository: EmployeeRepository
) : ViewModel() {

    val employees = repository.getAllEmployees()

    private val _selectedEmployee = MutableStateFlow<Employee?>(null)
    val selectedEmployee = _selectedEmployee.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess = _saveSuccess.asStateFlow()

    fun loadEmployee(id: String) {
        viewModelScope.launch {
            _selectedEmployee.value = repository.getEmployeeById(id)
        }
    }

    fun saveEmployee(name: String, pin: String, id: String? = null) {
        viewModelScope.launch {
            // Generate ID from first name (e.g., "Ravi Kumar" → "RAVI")
            val generatedId = id ?: name.trim().split(" ").first().uppercase()
            val existing = if (id != null) repository.getEmployeeById(id) else null

            val salt = existing?.salt ?: HashUtils.generateSalt()
            val pinHash = if (pin.isNotBlank()) HashUtils.hashPin(pin, salt) else existing?.pinHash ?: ""

            val employee = Employee(
                id = generatedId,
                name = name,
                pinHash = pinHash,
                salt = salt,
                active = existing?.active ?: true,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis()
            )
            if (id != null) {
                repository.updateEmployee(employee)
            } else {
                repository.saveEmployee(employee)
            }
            _saveSuccess.value = true
        }
    }

    fun deleteEmployee(id: String) {
        viewModelScope.launch { repository.deleteEmployee(id) }
    }

    fun toggleActive(id: String, active: Boolean) {
        viewModelScope.launch { repository.setActive(id, active) }
    }

    fun resetSaveSuccess() { _saveSuccess.value = false }
}
