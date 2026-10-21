package com.waterplant.distribution.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.waterplant.distribution.core.database.entity.EmployeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: String): EmployeeEntity?

    @Upsert
    suspend fun upsertEmployee(employee: EmployeeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<EmployeeEntity>)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployee(id: String)
}
