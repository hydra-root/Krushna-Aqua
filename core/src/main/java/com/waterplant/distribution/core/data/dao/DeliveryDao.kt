package com.waterplant.distribution.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.waterplant.distribution.core.data.model.Delivery
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryDao {
    @Query("SELECT * FROM deliveries ORDER BY createdAt DESC")
    fun getAllDeliveries(): Flow<List<Delivery>>

    @Query("SELECT * FROM deliveries ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentDeliveries(limit: Int): Flow<List<Delivery>>

    @Query("SELECT * FROM deliveries WHERE employeeId = :employeeId ORDER BY createdAt DESC")
    fun getDeliveriesByEmployee(employeeId: String): Flow<List<Delivery>>

    @Query("SELECT * FROM deliveries WHERE employeeId = :employeeId AND createdAt >= :startOfDay AND createdAt < :endOfDay ORDER BY createdAt DESC")
    fun getTodayDeliveriesByEmployee(employeeId: String, startOfDay: Long, endOfDay: Long): Flow<List<Delivery>>

    @Query("SELECT * FROM deliveries WHERE createdAt >= :startOfDay AND createdAt < :endOfDay ORDER BY createdAt DESC")
    fun getTodayDeliveries(startOfDay: Long, endOfDay: Long): Flow<List<Delivery>>

    @Query("SELECT * FROM deliveries WHERE createdAt >= :startOfMonth AND createdAt < :endOfMonth ORDER BY createdAt DESC")
    fun getMonthlyDeliveries(startOfMonth: Long, endOfMonth: Long): Flow<List<Delivery>>

    @Query("SELECT * FROM deliveries WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getDeliveriesByCustomer(customerId: String): Flow<List<Delivery>>

    @Query("SELECT * FROM deliveries WHERE uuid = :uuid")
    suspend fun getDeliveryByUuid(uuid: String): Delivery?

    @Query("SELECT * FROM deliveries WHERE syncStatus = :status")
    suspend fun getDeliveriesBySyncStatus(status: String): List<Delivery>

    @Query("SELECT COUNT(*) FROM deliveries WHERE syncStatus = 'PENDING'")
    fun getPendingSyncCount(): Flow<Int>

    // Today's stats
    @Query("SELECT COALESCE(SUM(deliveredJars), 0) FROM deliveries WHERE createdAt >= :startOfDay AND createdAt < :endOfDay")
    fun getTodayDeliveredCount(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(returnedJars), 0) FROM deliveries WHERE createdAt >= :startOfDay AND createdAt < :endOfDay")
    fun getTodayReturnedCount(startOfDay: Long, endOfDay: Long): Flow<Int>

    // Monthly stats
    @Query("SELECT COALESCE(SUM(deliveredJars), 0) FROM deliveries WHERE createdAt >= :startOfMonth AND createdAt < :endOfMonth")
    fun getMonthlyDeliveredCount(startOfMonth: Long, endOfMonth: Long): Flow<Int>

    // Cash collected today
    @Query("SELECT COALESCE(SUM(cashPaid), 0.0) FROM deliveries WHERE createdAt >= :startOfDay AND createdAt < :endOfDay")
    fun getTodayCashCollected(startOfDay: Long, endOfDay: Long): Flow<Double>

    // Yesterday's deliveries (for jar collection tracking)
    @Query("SELECT * FROM deliveries WHERE createdAt >= :startOfYesterday AND createdAt < :endOfYesterday ORDER BY createdAt DESC")
    fun getYesterdayDeliveries(startOfYesterday: Long, endOfYesterday: Long): Flow<List<Delivery>>

    @Query("SELECT * FROM deliveries WHERE employeeId = :employeeId AND createdAt >= :startOfYesterday AND createdAt < :endOfYesterday ORDER BY createdAt DESC")
    fun getYesterdayDeliveriesByEmployee(employeeId: String, startOfYesterday: Long, endOfYesterday: Long): Flow<List<Delivery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: Delivery)

    @Query("UPDATE deliveries SET syncStatus = :status WHERE uuid IN (:uuids)")
    suspend fun updateSyncStatus(uuids: List<String>, status: String)

    /**
     * Idempotent merge: uses IGNORE so duplicate UUIDs are silently skipped.
     * Returns the list of UUIDs that were successfully inserted.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDeliveriesIgnoreDuplicates(deliveries: List<Delivery>): List<Long>

    @Transaction
    suspend fun mergeIncomingDeliveries(deliveries: List<Delivery>): List<String> {
        val results = insertDeliveriesIgnoreDuplicates(deliveries)
        val importedUuids = mutableListOf<String>()
        for (i in results.indices) {
            // rowId != -1 means the row was actually inserted (not a duplicate)
            if (results[i] != -1L) {
                importedUuids.add(deliveries[i].uuid)
            } else {
                // Still acknowledge duplicates so employee marks them synced
                importedUuids.add(deliveries[i].uuid)
            }
        }
        return importedUuids
    }
}
