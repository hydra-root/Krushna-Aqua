package com.waterplant.distribution.core.network

import android.util.Log
import com.waterplant.distribution.core.models.Delivery
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncHttpServer @Inject constructor() {
    private var server: NettyApplicationEngine? = null
    var onSyncReceived: (suspend (List<Delivery>) -> List<String>)? = null

    fun startServer(port: Int = 8080) {
        if (server != null) return
        
        server = embeddedServer(Netty, port = port) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            routing {
                post("/sync") {
                    try {
                        val incomingDeliveries = call.receive<List<Delivery>>()
                        val processedUuids = onSyncReceived?.invoke(incomingDeliveries) ?: emptyList()
                        call.respond(processedUuids)
                    } catch (e: Exception) {
                        Log.e("SyncServer", "Error processing sync", e)
                        call.respond(io.ktor.http.HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
                    }
                }
            }
        }.start(wait = false)
    }

    fun stopServer() {
        server?.stop(1000, 2000)
        server = null
    }
}
