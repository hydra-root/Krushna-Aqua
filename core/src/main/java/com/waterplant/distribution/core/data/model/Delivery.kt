package com.waterplant.distribution.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "deliveries")
data class Delivery(
    @PrimaryKey val uuid: String = "",
    val customerId: String = "",
    val employeeId: String = "",
    val deliveredJars: Int = 0,
    val returnedJars: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val syncStatus: String = SyncStatus.PENDING.name,
    val cashPaid: Double = 0.0,
    val totalAmount: Double = 0.0
)
