package com.waterplant.distribution.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import java.net.Inet4Address
import java.net.Socket

/**
 * Automatically discovers the Plant Phone IP address.
 * 
 * Priority:
 * 1. If both apps are on the SAME phone → uses localhost (127.0.0.1)
 * 2. If on different phones via hotspot → uses Wi-Fi gateway IP
 */
object NetworkUtils {

    private const val SYNC_PORT = 8080

    /**
     * Returns the IP address of the Plant Phone sync server.
     * Tries localhost first (same-device), then hotspot gateway.
     */
    fun getGatewayIp(context: Context): String? {
        // Try localhost first (both apps on same phone)
        if (isLocalhostReachable()) {
            return "127.0.0.1"
        }

        // Try hotspot gateway (separate phones)
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = cm.activeNetwork ?: return fallbackGateway(context)
                val linkProps = cm.getLinkProperties(network) ?: return fallbackGateway(context)

                for (route in linkProps.routes) {
                    if (route.isDefaultRoute && route.gateway is Inet4Address) {
                        return route.gateway?.hostAddress
                    }
                }
            }
            fallbackGateway(context)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Quick check: is the sync server running on localhost?
     * This means both Admin and Employee apps are on the same phone.
     */
    private fun isLocalhostReachable(): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(java.net.InetSocketAddress("127.0.0.1", SYNC_PORT), 500)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    @Suppress("DEPRECATION")
    private fun fallbackGateway(context: Context): String? {
        return try {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val dhcp = wm.dhcpInfo ?: return null
            if (dhcp.gateway == 0) return null
            val ip = dhcp.gateway
            "${ip and 0xff}.${ip shr 8 and 0xff}.${ip shr 16 and 0xff}.${ip shr 24 and 0xff}"
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns the local IP address of this device on the Wi-Fi network.
     */
    fun getLocalIpAddress(context: Context): String? {
        return try {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ipInt = wm.connectionInfo.ipAddress
            if (ipInt == 0) return null
            "${ipInt and 0xff}.${ipInt shr 8 and 0xff}.${ipInt shr 16 and 0xff}.${ipInt shr 24 and 0xff}"
        } catch (e: Exception) {
            null
        }
    }

    fun isWifiConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            return caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
        }
        @Suppress("DEPRECATION")
        return cm.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
    }
}

