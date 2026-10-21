package com.waterplant.distribution.core.data.repository

import com.waterplant.distribution.core.data.dao.CustomerDao
import com.waterplant.distribution.core.data.model.Customer
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central repository managing customer profiles, account classifications (Cash vs Postpaid),
 * and route-based client searches.
 *
 * @author Pavan Pawar
 */
@Singleton
class CustomerRepository @Inject constructor(
    private val dao: CustomerDao
) {
    fun getAllCustomers(): Flow<List<Customer>> = dao.getAllCustomers()
    fun getChangedCustomers(lastSyncTime: Long): Flow<List<Customer>> = dao.getChangedCustomers(lastSyncTime)
    fun getCustomersByEmployee(employeeId: String): Flow<List<Customer>> = dao.getCustomersByEmployee(employeeId)
    fun searchCustomers(query: String): Flow<List<Customer>> = dao.searchCustomers(query)
    fun searchCustomersByEmployee(employeeId: String, query: String): Flow<List<Customer>> = dao.searchCustomersByEmployee(employeeId, query)
    fun getCustomerCount(): Flow<Int> = dao.getCustomerCount()
    fun getCustomerByIdFlow(id: String): Flow<Customer?> = dao.getCustomerByIdFlow(id)
    suspend fun getCustomerById(id: String): Customer? = dao.getCustomerById(id)
    suspend fun saveCustomer(customer: Customer) = dao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = dao.updateCustomer(customer)
    suspend fun deleteCustomer(id: String) = dao.deleteCustomer(id)
    suspend fun insertCustomers(customers: List<Customer>) = dao.insertCustomers(customers)
}
