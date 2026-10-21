package com.waterplant.distribution.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.waterplant.distribution.core.database.entity.DeliveryEntity
import com.waterplant.distribution.core.models.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryDao {
    @Query("SELECT * FROM deliveries")
    fun getAllDeliveries(): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE syncStatus = :status")
    suspend fun getDeliveriesBySyncStatus(status: SyncStatus): List<DeliveryEntity>

    @Query("SELECT * FROM deliveries WHERE uuid = :uuid")
    suspend fun getDeliveryByUuid(uuid: String): DeliveryEntity?

    @Upsert
    suspend fun upsertDelivery(delivery: DeliveryEntity)

    // Used by Plant Phone to safely merge incoming deliveries
    @Upsert
    fun upsertDeliveries(deliveries: List<DeliveryEntity>)

    @Transaction
    suspend fun mergeIncomingDeliveries(deliveries: List<DeliveryEntity>): List<String> {
        upsertDeliveries(deliveries)
        return deliveries.map { it.uuid }
    }

    @Query("UPDATE deliveries SET syncStatus = :status WHERE uuid IN (:uuids)")
    suspend fun updateSyncStatus(uuids: List<String>, status: SyncStatus)
}
