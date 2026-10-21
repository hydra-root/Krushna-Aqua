package com.waterplant.distribution.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val pinHash: String = "",
    val salt: String = "",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)
