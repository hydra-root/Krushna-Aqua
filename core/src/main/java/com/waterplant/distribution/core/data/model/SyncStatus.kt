package com.waterplant.distribution.core.data.model

/**
 * Sync status for delivery records.
 * PENDING: Not yet synced to plant phone.
 * SYNCED: Successfully synced and acknowledged by plant phone.
 */
enum class SyncStatus {
    PENDING,
    SYNCED
}
