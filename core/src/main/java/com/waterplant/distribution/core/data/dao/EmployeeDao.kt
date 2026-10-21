package com.waterplant.distribution.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.waterplant.distribution.core.data.model.Employee
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE active = 1 ORDER BY name ASC")
    fun getActiveEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: String): Employee?

    @Query("SELECT * FROM employees WHERE (id = :identifier OR LOWER(name) = LOWER(:identifier)) AND active = 1 LIMIT 1")
    suspend fun getEmployeeForLogin(identifier: String): Employee?

    @Query("SELECT COUNT(*) FROM employees")
    fun getEmployeeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployee(id: String)

    @Query("UPDATE employees SET active = :active WHERE id = :id")
    suspend fun setActive(id: String, active: Boolean)
}
