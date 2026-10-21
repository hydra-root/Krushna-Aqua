package com.waterplant.distribution.core.models

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String,
    val name: String,
    val company: String,
    val phone: String,
    val address: String,
    val employeeId: String
)
