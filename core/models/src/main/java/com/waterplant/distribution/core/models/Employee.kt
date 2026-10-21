package com.waterplant.distribution.core.models

import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val id: String,
    val name: String,
    val pin: String,
    val active: Boolean
)
