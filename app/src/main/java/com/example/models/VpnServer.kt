package com.example.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VpnServer(
    val id: Int,
    @Json(name = "country_code") val countryCode: String,
    val city: String,
    val ip: String,
    val protocol: String,
    val port: Int,
    @Json(name = "vpn_username") val vpnUsername: String,
    @Json(name = "vpn_password") val vpnPassword: String,
    @Json(name = "is_free") val isFree: Int,
    val order: Int,
    @Json(name = "use_file") val useFile: Int,
    @Json(name = "free_connect_duration") val freeConnectDuration: Int,
    @Json(name = "connected_devices") val connectedDevices: Int
)
