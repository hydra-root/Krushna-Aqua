package com.waterplant.distribution.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val company: String = "",
    val phone: String = "",
    val address: String = "",
    val employeeId: String = "",
    val jarValue: Double = 20.0,
    val customerGroup: String = "POSTPAID", // Can be "CASH" or "POSTPAID"
    val area: String = "",
    val balance: Int = 0,
    val modifiedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
