package com.waterplant.distribution.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.waterplant.distribution.core.models.Customer

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val company: String,
    val phone: String,
    val address: String,
    val employeeId: String
)

fun CustomerEntity.toDomainModel() = Customer(id, name, company, phone, address, employeeId)
fun Customer.toEntity() = CustomerEntity(id, name, company, phone, address, employeeId)
