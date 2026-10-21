package com.waterplant.distribution.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.waterplant.distribution.core.models.Delivery
import com.waterplant.distribution.core.models.SyncStatus

@Entity(tableName = "deliveries")
data class DeliveryEntity(
    @PrimaryKey val uuid: String,
    val customerId: String,
    val employeeId: String,
    val deliveredJars: Int,
    val returnedJars: Int,
    val createdAt: Long,
    val modifiedAt: Long,
    val version: Int,
    val syncStatus: SyncStatus
)

fun DeliveryEntity.toDomainModel() = Delivery(uuid, customerId, employeeId, deliveredJars, returnedJars, createdAt, modifiedAt, version, syncStatus)
fun Delivery.toEntity() = DeliveryEntity(uuid, customerId, employeeId, deliveredJars, returnedJars, createdAt, modifiedAt, version, syncStatus)
