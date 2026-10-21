package com.waterplant.distribution.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.waterplant.distribution.core.data.model.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE modifiedAt > :lastSyncTime")
    fun getChangedCustomers(lastSyncTime: Long): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE employeeId = :employeeId AND isDeleted = 0 ORDER BY name ASC")
    fun getCustomersByEmployee(employeeId: String): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id AND isDeleted = 0")
    suspend fun getCustomerById(id: String): Customer?

    @Query("SELECT * FROM customers WHERE id = :id AND isDeleted = 0")
    fun getCustomerByIdFlow(id: String): Flow<Customer?>

    @Query("SELECT * FROM customers WHERE isDeleted = 0 AND (name LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%')")
    fun searchCustomers(query: String): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE employeeId = :employeeId AND isDeleted = 0 AND (name LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%')")
    fun searchCustomersByEmployee(employeeId: String, query: String): Flow<List<Customer>>

    @Query("SELECT COUNT(*) FROM customers WHERE isDeleted = 0")
    fun getCustomerCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("UPDATE customers SET isDeleted = 1, modifiedAt = :timestamp WHERE id = :id")
    suspend fun deleteCustomer(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE customers SET balance = balance + :delivered - :returned, modifiedAt = :timestamp WHERE id = :id")
    suspend fun updateBalance(id: String, delivered: Int, returned: Int, timestamp: Long = System.currentTimeMillis())
}
