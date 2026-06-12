package com.example.services

import android.content.Context
import android.content.Intent
import com.example.models.VpnServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class VpnState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR
}

class VpnEngineService(private val context: Context) {
    private val _currentState = MutableStateFlow(VpnState.DISCONNECTED)
    val currentState: StateFlow<VpnState> = _currentState

    private var activeServer: VpnServer? = null

    suspend fun connect(server: VpnServer) {
        if (_currentState.value == VpnState.CONNECTED || _currentState.value == VpnState.CONNECTING) return
        
        activeServer = server
        _currentState.value = VpnState.CONNECTING
        
        // Start real Android VpnService
        val intent = Intent(context, RealVpnService::class.java).apply {
            action = RealVpnService.ACTION_CONNECT
            putExtra(RealVpnService.EXTRA_IP, server.ip)
            putExtra(RealVpnService.EXTRA_PORT, server.port)
            putExtra(RealVpnService.EXTRA_PROTOCOL, server.protocol)
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        // Simulate network connection time
        delay(2000)
        
        _currentState.value = VpnState.CONNECTED
    }

    suspend fun disconnect() {
        if (_currentState.value == VpnState.DISCONNECTED) return
        
        _currentState.value = VpnState.DISCONNECTING
        
        val intent = Intent(context, RealVpnService::class.java).apply {
            action = RealVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)

        // Simulate disconnect time
        delay(1000)
        
        _currentState.value = VpnState.DISCONNECTED
        activeServer = null
    }
}
