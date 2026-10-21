package com.waterplant.distribution.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.waterplant.distribution.core.models.Employee

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val pin: String,
    val active: Boolean
)

fun EmployeeEntity.toDomainModel() = Employee(id, name, pin, active)
fun Employee.toEntity() = EmployeeEntity(id, name, pin, active)
