package com.waterplant.distribution.core.data.repository

import com.waterplant.distribution.core.data.dao.EmployeeDao
import com.waterplant.distribution.core.data.model.Employee
import com.waterplant.distribution.core.utils.HashUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeRepository @Inject constructor(
    private val dao: EmployeeDao
) {
    fun getAllEmployees(): Flow<List<Employee>> = dao.getAllEmployees()
    fun getActiveEmployees(): Flow<List<Employee>> = dao.getActiveEmployees()
    fun getEmployeeCount(): Flow<Int> = dao.getEmployeeCount()
    suspend fun getEmployeeById(id: String): Employee? = dao.getEmployeeById(id)
    suspend fun login(id: String, pin: String): Employee? {
        val employee = dao.getEmployeeForLogin(id) ?: return null
        if (HashUtils.verifyPin(pin, employee.salt, employee.pinHash)) {
            return employee
        }
        return null
    }
    suspend fun saveEmployee(employee: Employee) = dao.insertEmployee(employee)
    suspend fun updateEmployee(employee: Employee) = dao.updateEmployee(employee)
    suspend fun deleteEmployee(id: String) = dao.deleteEmployee(id)
    suspend fun setActive(id: String, active: Boolean) = dao.setActive(id, active)
}
