package com.example.models

data class VpnServer(
    val id: String, // will act as ID (e.g. base64 or IP)
    val hostName: String,
    val ip: String,
    val score: Int,
    val ping: Int,
    val speed: Long,
    val countryLong: String,
    val countryShort: String,
    val openVpnConfigBase64: String
) {
    // For compatibility with previous UI fields:
    val countryCode: String get() = countryShort
    val city: String get() = countryLong
    val connectedDevices: Int get() = ping // Show ping instead of devices
    val protocol: String get() = "udp"
    val port: Int get() = 1194
}
