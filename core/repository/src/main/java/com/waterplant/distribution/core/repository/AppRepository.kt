package com.waterplant.distribution.core.repository

import com.waterplant.distribution.core.database.dao.CustomerDao
import com.waterplant.distribution.core.database.dao.DeliveryDao
import com.waterplant.distribution.core.database.dao.EmployeeDao
import com.waterplant.distribution.core.database.entity.toEntity
import com.waterplant.distribution.core.database.entity.toDomainModel
import com.waterplant.distribution.core.models.Customer
import com.waterplant.distribution.core.models.Delivery
import com.waterplant.distribution.core.models.Employee
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val employeeDao: EmployeeDao,
    private val customerDao: CustomerDao,
    private val deliveryDao: DeliveryDao
) {
    fun getAllEmployees(): Flow<List<Employee>> = 
        employeeDao.getAllEmployees().map { list -> list.map { it.toDomainModel() } }
        
    suspend fun saveEmployee(employee: Employee) = 
        employeeDao.upsertEmployee(employee.toEntity())

    fun getAllCustomers(): Flow<List<Customer>> = 
        customerDao.getAllCustomers().map { list -> list.map { it.toDomainModel() } }
        
    suspend fun saveCustomer(customer: Customer) = 
        customerDao.upsertCustomer(customer.toEntity())

    fun getAllDeliveries(): Flow<List<Delivery>> = 
        deliveryDao.getAllDeliveries().map { list -> list.map { it.toDomainModel() } }
        
    suspend fun saveDelivery(delivery: Delivery) = 
        deliveryDao.upsertDelivery(delivery.toEntity())
        
    suspend fun mergeIncomingDeliveries(deliveries: List<Delivery>): List<String> =
        deliveryDao.mergeIncomingDeliveries(deliveries.map { it.toEntity() })
}
