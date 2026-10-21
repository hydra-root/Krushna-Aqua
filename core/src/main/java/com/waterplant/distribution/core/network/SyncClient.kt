package com.waterplant.distribution.core.network

import android.content.Context
import com.waterplant.distribution.core.data.model.Customer
import com.waterplant.distribution.core.data.model.Delivery
import com.waterplant.distribution.core.data.model.SyncStatus
import com.waterplant.distribution.core.data.repository.CustomerRepository
import com.waterplant.distribution.core.data.repository.DeliveryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-performance HTTP client for the Employee app to communicate with the Master Plant Phone.
 * Uses zero-overhead [HttpURLConnection] to transmit pending delivery payloads and ingest
 * synchronized customer updates over the local Wi-Fi hotspot connection.
 *
 * @author Pavan Pawar
 */
@Singleton
class SyncClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deliveryRepository: DeliveryRepository,
    private val customerRepository: CustomerRepository
) {
    private val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val port = SyncServer.PORT

    /**
     * Ping the plant phone to check if sync server is reachable.
     */
    suspend fun ping(gatewayIp: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://$gatewayIp:$port/ping")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }
            val code = conn.responseCode
            conn.disconnect()
            code == 200
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sync all pending deliveries to the plant phone (Admin app).
     * 1. Retrieves all local deliveries where `syncStatus = PENDING`.
     * 2. Posts them to the `/sync` endpoint on the Admin app.
     * 3. The server responds with a list of `acknowledgedUuids`.
     * 4. Updates the local status of those UUIDs to `SYNCED`, effectively locking them.
     * 
     * Returns the [SyncResponse] with statistics.
     */
    suspend fun syncDeliveries(gatewayIp: String, employeeId: String): SyncResponse = withContext(Dispatchers.IO) {
        try {
            val pending = deliveryRepository.getPendingDeliveries()
            if (pending.isEmpty()) {
                return@withContext SyncResponse(true, uploaded = 0, imported = 0, message = "No pending records")
            }

            val lastSyncTime = prefs.getLong("last_customer_sync_time", 0L)
            val request = SyncRequest(
                employeeId = employeeId, 
                deliveries = pending,
                lastCustomerSyncTime = lastSyncTime
            )
            val requestBody = json.encodeToString(request)

            val url = URL("http://$gatewayIp:$port/sync")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 30000
                readTimeout = 30000
            }

            // Write request body
            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(requestBody)
                writer.flush()
            }

            // Read response
            val responseCode = conn.responseCode
            val responseBody = if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            } else {
                BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream)).use { it.readText() }
            }
            conn.disconnect()

            if (responseCode in 200..299) {
                val response = json.decodeFromString<SyncResponse>(responseBody)
                if (response.success) {
                    if (response.acknowledgedUuids.isNotEmpty()) {
                        deliveryRepository.markAsSynced(response.acknowledgedUuids)
                    }
                    if (response.customers.isNotEmpty()) {
                        customerRepository.insertCustomers(response.customers)
                        prefs.edit().putLong("last_customer_sync_time", System.currentTimeMillis()).apply()
                    }
                }
                response
            } else {
                SyncResponse(false, message = "Server error: $responseCode")
            }
        } catch (e: Exception) {
            SyncResponse(false, message = "Sync failed: ${e.message}")
        }
    }

    /**
     * Authenticate an employee against the Plant Phone's sync server.
     * On success, returns the employee details and their assigned customers.
     */
    suspend fun remoteLogin(gatewayIp: String, employeeId: String, pin: String): LoginResponse = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(employeeId = employeeId, pin = pin)
            val requestBody = json.encodeToString(request)

            val url = URL("http://$gatewayIp:$port/login")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 10000
                readTimeout = 10000
            }

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(requestBody)
                writer.flush()
            }

            val responseCode = conn.responseCode
            val responseBody = if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            } else if (responseCode == 401) {
                BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream)).use { it.readText() }
            } else {
                conn.disconnect()
                return@withContext LoginResponse(false, message = "Server error: $responseCode")
            }
            conn.disconnect()

            json.decodeFromString<LoginResponse>(responseBody)
        } catch (e: Exception) {
            LoginResponse(false, message = "Cannot connect to Plant Phone: ${e.message}")
        }
    }

    /**
     * Download the latest customer list from the plant phone.
     */
    suspend fun downloadCustomers(gatewayIp: String, employeeId: String): List<Customer> = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://$gatewayIp:$port/customers/$employeeId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
            }
            val responseBody = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            conn.disconnect()
            val response = json.decodeFromString<LoginResponse>(responseBody)
            response.customers
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Download the latest deliveries from the plant phone.
     */
    suspend fun downloadDeliveries(gatewayIp: String): List<Delivery> = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://$gatewayIp:$port/deliveries")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
            }
            val responseBody = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            conn.disconnect()
            val response = json.decodeFromString<SyncRequest>(responseBody)
            response.deliveries
        } catch (e: Exception) {
            emptyList()
        }
    }
}
