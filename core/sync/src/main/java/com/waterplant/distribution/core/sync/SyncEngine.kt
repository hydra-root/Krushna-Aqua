package com.waterplant.distribution.core.sync

import com.waterplant.distribution.core.database.dao.DeliveryDao
import com.waterplant.distribution.core.models.SyncStatus
import com.waterplant.distribution.core.network.SyncHttpClient
import com.waterplant.distribution.core.database.entity.toDomainModel
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncEngine @Inject constructor(
    private val deliveryDao: DeliveryDao,
    private val httpClient: SyncHttpClient
) {
    suspend fun syncDeliveries(gatewayIp: String): Result<Int> {
        return try {
            val pendingDeliveries = deliveryDao.getDeliveriesBySyncStatus(SyncStatus.PENDING)
            if (pendingDeliveries.isEmpty()) return Result.success(0)

            val payload = pendingDeliveries.map { it.toDomainModel() }

            val response = httpClient.client.post("http://$gatewayIp:8080/sync") {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }

            if (response.status.value in 200..299) {
                val syncedUuids: List<String> = response.body()
                if (syncedUuids.isNotEmpty()) {
                    deliveryDao.updateSyncStatus(syncedUuids, SyncStatus.SYNCED)
                }
                Result.success(syncedUuids.size)
            } else {
                Result.failure(Exception("Server returned status: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
