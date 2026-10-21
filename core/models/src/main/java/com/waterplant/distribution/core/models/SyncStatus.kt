package com.waterplant.distribution.core.models

import kotlinx.serialization.Serializable

@Serializable
enum class SyncStatus {
    PENDING,
    SYNCED
}
