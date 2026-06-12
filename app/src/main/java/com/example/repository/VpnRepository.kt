package com.example.repository

import com.example.models.VpnServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class VpnRepository {

    suspend fun getServers(): List<VpnServer> = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://www.vpngate.net/api/iphone/")
            .build()
        
        val serverList = mutableListOf<VpnServer>()
        
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@use
                    val lines = body.split("\n")
                    
                    for (line in lines) {
                        if (line.startsWith("*") || line.startsWith("#") || line.trim().isEmpty()) {
                            continue
                        }
                        
                        val serverData = line.split(",")
                        if (serverData.size >= 15) {
                            try {
                                val ping = serverData[3].toIntOrNull() ?: Int.MAX_VALUE
                                val speed = serverData[4].toLongOrNull() ?: 0L
                                val score = serverData[2].toIntOrNull() ?: 0
                                
                                val server = VpnServer(
                                    id = serverData[1], // Use IP as ID
                                    hostName = serverData[0],
                                    ip = serverData[1],
                                    score = score,
                                    ping = ping,
                                    speed = speed,
                                    countryLong = serverData[5],
                                    countryShort = serverData[6],
                                    openVpnConfigBase64 = serverData[14]
                                )
                                serverList.add(server)
                            } catch (e: Exception) {
                                // Skip malformed rows
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        
        // Return sorted (best servers first): lowest ping, highest speed, highest score
        serverList.sortedWith(
            compareBy<VpnServer> { it.ping }
                .thenByDescending { it.speed }
                .thenByDescending { it.score }
        )
    }
}
