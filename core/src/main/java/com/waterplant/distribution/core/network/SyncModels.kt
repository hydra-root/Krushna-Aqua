package com.waterplant.distribution.core.network

import com.waterplant.distribution.core.data.model.Customer
import com.waterplant.distribution.core.data.model.Delivery
import com.waterplant.distribution.core.data.model.Employee
import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest(
    val employeeId: String,
    val deliveries: List<Delivery>,
    val lastCustomerSyncTime: Long = 0L
)

@Serializable
data class SyncResponse(
    val success: Boolean,
    val acknowledgedUuids: List<String> = emptyList(),
    val uploaded: Int = 0,
    val imported: Int = 0,
    val duplicates: Int = 0,
    val failed: Int = 0,
    val customers: List<Customer> = emptyList(), // Only changed customers
    val message: String = ""
)

@Serializable
data class LoginRequest(
    val employeeId: String,
    val pin: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val employee: Employee? = null,
    val customers: List<Customer> = emptyList(),
    val message: String = ""
)
