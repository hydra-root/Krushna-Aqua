package com.waterplant.distribution.core.data.repository

import androidx.room.withTransaction
import com.waterplant.distribution.core.data.AppDatabase
import com.waterplant.distribution.core.data.dao.DeliveryDao
import com.waterplant.distribution.core.data.model.Delivery
import com.waterplant.distribution.core.data.model.SyncStatus
import com.waterplant.distribution.core.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository mediating access to delivery logs, jar tracking calculations, and synchronization statuses.
 * Exposes reactive Kotlin [Flow] streams to decouple UI components from direct database operations.
 *
 * @author Pavan Pawar
 */
@Singleton
class DeliveryRepository @Inject constructor(
    private val dao: DeliveryDao,
    private val db: AppDatabase
) {
    fun getAllDeliveries(): Flow<List<Delivery>> = dao.getAllDeliveries()
    fun getRecentDeliveries(limit: Int): Flow<List<Delivery>> = dao.getRecentDeliveries(limit)

    fun getDeliveriesByEmployee(employeeId: String): Flow<List<Delivery>> =
        dao.getDeliveriesByEmployee(employeeId)

    fun getTodayDeliveriesByEmployee(employeeId: String): Flow<List<Delivery>> =
        dao.getTodayDeliveriesByEmployee(employeeId, DateUtils.startOfToday(), DateUtils.endOfToday())

    fun getTodayDeliveries(): Flow<List<Delivery>> =
        dao.getTodayDeliveries(DateUtils.startOfToday(), DateUtils.endOfToday())

    fun getMonthlyDeliveries(): Flow<List<Delivery>> =
        dao.getMonthlyDeliveries(DateUtils.startOfMonth(), DateUtils.endOfMonth())

    fun getDeliveriesByCustomer(customerId: String): Flow<List<Delivery>> =
        dao.getDeliveriesByCustomer(customerId)

    fun getPendingSyncCount(): Flow<Int> = dao.getPendingSyncCount()

    fun getTodayDeliveredCount(): Flow<Int> =
        dao.getTodayDeliveredCount(DateUtils.startOfToday(), DateUtils.endOfToday())

    fun getTodayReturnedCount(): Flow<Int> =
        dao.getTodayReturnedCount(DateUtils.startOfToday(), DateUtils.endOfToday())

    fun getMonthlyDeliveredCount(): Flow<Int> =
        dao.getMonthlyDeliveredCount(DateUtils.startOfMonth(), DateUtils.endOfMonth())

    fun getTodayCashCollected(): Flow<Double> =
        dao.getTodayCashCollected(DateUtils.startOfToday(), DateUtils.endOfToday())

    fun getYesterdayDeliveries(): Flow<List<Delivery>> =
        dao.getYesterdayDeliveries(DateUtils.startOfYesterday(), DateUtils.endOfYesterday())

    fun getYesterdayDeliveriesByEmployee(employeeId: String): Flow<List<Delivery>> =
        dao.getYesterdayDeliveriesByEmployee(employeeId, DateUtils.startOfYesterday(), DateUtils.endOfYesterday())

    suspend fun saveDelivery(delivery: Delivery) {
        db.withTransaction {
            dao.insertDelivery(delivery)
            db.customerDao().updateBalance(delivery.customerId, delivery.deliveredJars, delivery.returnedJars)
        }
    }

    suspend fun getPendingDeliveries(): List<Delivery> =
        dao.getDeliveriesBySyncStatus(SyncStatus.PENDING.name)

    suspend fun markAsSynced(uuids: List<String>) =
        dao.updateSyncStatus(uuids, SyncStatus.SYNCED.name)

    suspend fun mergeIncomingDeliveries(deliveries: List<Delivery>): List<String> {
        return db.withTransaction {
            val acknowledged = dao.mergeIncomingDeliveries(deliveries)
            // Update balance for each successfully inserted delivery
            deliveries.filter { acknowledged.contains(it.uuid) }.forEach {
                db.customerDao().updateBalance(it.customerId, it.deliveredJars, it.returnedJars)
            }
            acknowledged
        }
    }
}
