package com.waterplant.distribution.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import java.net.Inet4Address

object NetworkUtils {
    /**
     * Gets the Hotspot Gateway IP Address which is the Plant Phone's IP.
     */
    fun getGatewayIp(context: Context): String? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network: Network = connectivityManager.activeNetwork ?: return null
            val linkProperties: LinkProperties = connectivityManager.getLinkProperties(network) ?: return null
            
            // Find the default route (gateway) for IPv4
            for (route in linkProperties.routes) {
                if (route.isDefaultRoute && route.gateway is Inet4Address) {
                    return route.gateway?.hostAddress
                }
            }
        }
        
        // Fallback to WifiManager for older devices or if above fails
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcp = wifiManager.dhcpInfo
        if (dhcp != null && dhcp.gateway != 0) {
            val ipAddress = dhcp.gateway
            return String.format(
                "%d.%d.%d.%d",
                ipAddress and 0xff,
                ipAddress shr 8 and 0xff,
                ipAddress shr 16 and 0xff,
                ipAddress shr 24 and 0xff
            )
        }
        
        return null
    }
}
