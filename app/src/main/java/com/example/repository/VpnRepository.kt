package com.example.repository

import com.example.models.VpnServer
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class VpnRepository {

    fun getServers(): List<VpnServer> {
        val moshi = Moshi.Builder().build()
        val listType = Types.newParameterizedType(List::class.java, VpnServer::class.java)
        val adapter = moshi.adapter<List<VpnServer>>(listType)
        return adapter.fromJson(serversJson) ?: emptyList()
    }

    private val serversJson = """
        [
          {
            "id": 74,
            "country_code": "de",
            "city": "Falkenstein",
            "ip": "138.199.146.42",
            "protocol": "udp",
            "port": 1194,
            "vpn_username": "ger11feb26",
            "vpn_password": "Hhui199205nLn",
            "is_free": 1,
            "order": 0,
            "use_file": 1,
            "free_connect_duration": 60,
            "connected_devices": 3614
          },
          {
            "id": 70,
            "country_code": "us",
            "city": "Chicago",
            "ip": "162.212.153.219",
            "protocol": "udp",
            "port": 1194,
            "vpn_username": "chicago01",
            "vpn_password": "UyugSYYDh@56679>_yjghj868gnNk",
            "is_free": 1,
            "order": 1,
            "use_file": 1,
            "free_connect_duration": 60,
            "connected_devices": 3816
          },
          {
            "id": 73,
            "country_code": "gb",
            "city": "London",
            "ip": "46.225.62.198",
            "protocol": "udp",
            "port": 1194,
            "vpn_username": "coolifyopenvpn",
            "vpn_password": "Ujhkb#5968JGh*Y^#",
            "is_free": 1,
            "order": 4,
            "use_file": 1,
            "free_connect_duration": 60,
            "connected_devices": 646
          },
          {
            "id": 63,
            "country_code": "fr",
            "city": "Paris",
            "ip": "188.165.76.120",
            "protocol": "udp",
            "port": 1194,
            "vpn_username": "mkhan",
            "vpn_password": "sqC39Pj6alnKE6GDVDXlj8c7H9t9BJS9",
            "is_free": 1,
            "order": 6,
            "use_file": 1,
            "free_connect_duration": 60,
            "connected_devices": 0
          },
          {
            "id": 57,
            "country_code": "in",
            "city": "Mumbai",
            "ip": "62.72.43.161",
            "protocol": "udp",
            "port": 1194,
            "vpn_username": "mkhan",
            "vpn_password": "Rfs8rvW9X9CyrND6uoaSe0CS7sxxsgL7",
            "is_free": 1,
            "order": 7,
            "use_file": 1,
            "free_connect_duration": 60,
            "connected_devices": 209
          },
          {
            "id": 53,
            "country_code": "ch",
            "city": "High Speed",
            "ip": "5.182.36.104",
            "protocol": "udp",
            "port": 1194,
            "vpn_username": "mkhan",
            "vpn_password": "7ZC9Lgrtr36r12whT0HbhuHUILymErhb",
            "is_free": 1,
            "order": 8,
            "use_file": 1,
            "free_connect_duration": 60,
            "connected_devices": 115
          }
        ]
    """.trimIndent()
}
