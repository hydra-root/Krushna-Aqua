package com.waterplant.distribution.core.network

import android.util.Log
import com.waterplant.distribution.core.data.model.Delivery
import com.waterplant.distribution.core.data.model.SyncStatus
import com.waterplant.distribution.core.data.repository.CustomerRepository
import com.waterplant.distribution.core.data.repository.DeliveryRepository
import com.waterplant.distribution.core.data.repository.EmployeeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Embedded HTTP server running on the Admin Master device.
 *
 * Listens on port 8080 for incoming peer-to-peer sync requests from field Employee apps
 * connected to the local Wi-Fi Hotspot. Handles:
 * 1. Health check pings (`/ping`)
 * 2. Idempotent delivery ingestion with transactional conflict resolution
 * 3. Bidirectional customer registry synchronization back to employee devices
 *
 * @author Pavan Pawar
 */
@Singleton
class SyncServer @Inject constructor(
    private val deliveryRepository: DeliveryRepository,
    private val employeeRepository: EmployeeRepository,
    private val customerRepository: CustomerRepository
) {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    companion object {
        const val PORT = 8080
        private const val TAG = "SyncServer"
    }

    /**
     * Starts the server on a background thread.
     * Binds to 0.0.0.0 to accept connections from any device on the local network.
     */
    fun start() {
        if (isRunning) return
        isRunning = true
        scope.launch {
            try {
                serverSocket = ServerSocket(PORT)
                Log.i(TAG, "Sync server started on port $PORT")
                while (isRunning) {
                    try {
                        val client = serverSocket?.accept() ?: break
                        scope.launch { handleClient(client) }
                    } catch (e: Exception) {
                        if (isRunning) Log.e(TAG, "Error accepting client", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Server failed to start", e)
            }
        }
    }

    fun stop() {
        isRunning = false
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
    }

    private suspend fun handleClient(socket: Socket) {
        try {
            socket.soTimeout = 30000
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(socket.getOutputStream(), true)

            val requestLine = reader.readLine() ?: return
            val parts = requestLine.split(" ")
            val method = parts.getOrNull(0) ?: ""
            val path = parts.getOrNull(1) ?: ""

            // Read headers
            var contentLength = 0
            var line = reader.readLine()
            while (line != null && line.isNotEmpty()) {
                if (line.startsWith("Content-Length:", ignoreCase = true)) {
                    contentLength = line.substringAfter(":").trim().toIntOrNull() ?: 0
                }
                line = reader.readLine()
            }

            // Read body
            val body = if (contentLength > 0) {
                val chars = CharArray(contentLength)
                var totalRead = 0
                while (totalRead < contentLength) {
                    val read = reader.read(chars, totalRead, contentLength - totalRead)
                    if (read == -1) break
                    totalRead += read
                }
                String(chars, 0, totalRead)
            } else ""

            val response = when {
                method == "POST" && path == "/sync" -> handleSync(body)
                method == "POST" && path == "/login" -> handleLogin(body)
                method == "GET" && path.startsWith("/customers/") -> {
                    val empId = path.removePrefix("/customers/")
                    handleGetCustomers(empId)
                }
                method == "GET" && path == "/deliveries" -> handleGetDeliveries()
                method == "GET" && path == "/ping" -> {
                    val resp = SyncResponse(true, message = "Plant Phone Online")
                    buildHttpResponse(200, json.encodeToString(resp))
                }
                else -> buildHttpResponse(404, json.encodeToString(SyncResponse(false, message = "Not Found")))
            }

            writer.print(response)
            writer.flush()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling client", e)
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    /**
     * Handles the `/sync` POST request.
     * Receives a [SyncRequest] containing PENDING deliveries from an employee.
     * 1. Attempts to insert the deliveries into the master database.
     *    Duplicate UUIDs are ignored automatically by the [DeliveryDao]'s onConflict strategy.
     * 2. Returns a [SyncResponse] with `acknowledgedUuids` which the employee app will use to mark as SYNCED.
     * 
     * This idempotent design ensures that if a network connection drops during sync, 
     * the employee can safely retry without duplicating records.
     */
    private suspend fun handleSync(body: String): String {
        return try {
            if (body.isBlank()) {
                return buildHttpResponse(400, json.encodeToString(SyncResponse(false, message = "Empty request body")))
            }
            val request = json.decodeFromString<SyncRequest>(body)
            val incoming = request.deliveries.map {
                it.copy(syncStatus = SyncStatus.SYNCED.name)
            }
            val acknowledged = deliveryRepository.mergeIncomingDeliveries(incoming)

            val customers = customerRepository.getChangedCustomers(request.lastCustomerSyncTime).first()

            val response = SyncResponse(
                success = true,
                acknowledgedUuids = acknowledged,
                uploaded = request.deliveries.size,
                imported = acknowledged.size,
                duplicates = request.deliveries.size - acknowledged.size,
                failed = 0,
                customers = customers,
                message = "Sync completed successfully"
            )
            buildHttpResponse(200, json.encodeToString(response))
        } catch (e: Exception) {
            Log.e(TAG, "Sync error", e)
            buildHttpResponse(500, json.encodeToString(SyncResponse(false, message = "Sync failed: ${e.message}")))
        }
    }

    private suspend fun handleLogin(body: String): String {
        return try {
            val request = json.decodeFromString<LoginRequest>(body)
            val employee = employeeRepository.login(request.employeeId, request.pin)
            if (employee != null) {
                val customers = customerRepository.getAllCustomers().first()
                val response = LoginResponse(
                    success = true,
                    employee = employee,
                    customers = customers,
                    message = "Login successful"
                )
                buildHttpResponse(200, json.encodeToString(response))
            } else {
                buildHttpResponse(401, json.encodeToString(LoginResponse(false, message = "Invalid Employee ID or PIN")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            buildHttpResponse(500, json.encodeToString(LoginResponse(false, message = "Login failed: ${e.message}")))
        }
    }

    private suspend fun handleGetCustomers(employeeId: String): String {
        return try {
            val customers = customerRepository.getAllCustomers().first()
            val response = LoginResponse(
                success = true,
                customers = customers,
                message = "${customers.size} customers found"
            )
            buildHttpResponse(200, json.encodeToString(response))
        } catch (e: Exception) {
            Log.e(TAG, "Get customers error", e)
            buildHttpResponse(500, json.encodeToString(LoginResponse(false, message = "Failed: ${e.message}")))
        }
    }

    private suspend fun handleGetDeliveries(): String {
        return try {
            val deliveries = deliveryRepository.getAllDeliveries().first()
            val response = SyncRequest(
                employeeId = "admin",
                deliveries = deliveries
            )
            buildHttpResponse(200, json.encodeToString(response))
        } catch (e: Exception) {
            Log.e(TAG, "Get deliveries error", e)
            buildHttpResponse(500, json.encodeToString(SyncResponse(false, message = "Failed: ${e.message}")))
        }
    }

    private fun buildHttpResponse(statusCode: Int, body: String): String {
        val status = when (statusCode) {
            200 -> "200 OK"
            404 -> "404 Not Found"
            500 -> "500 Internal Server Error"
            else -> "$statusCode Error"
        }
        return "HTTP/1.1 $status\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: ${body.toByteArray().size}\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body
    }
}
