package com.waterplant.distribution.core.models

import kotlinx.serialization.Serializable

@Serializable
data class Delivery(
    val uuid: String,
    val customerId: String,
    val employeeId: String,
    val deliveredJars: Int,
    val returnedJars: Int,
    val createdAt: Long,
    val modifiedAt: Long,
    val version: Int,
    val syncStatus: SyncStatus
)
