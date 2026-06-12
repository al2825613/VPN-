package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.VpnServer
import com.example.repository.VpnRepository
import com.example.services.VpnEngineService
import com.example.services.VpnState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VpnViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = VpnRepository()
    private val vpnEngine = VpnEngineService(application)

    private val _servers = MutableStateFlow<List<VpnServer>>(emptyList())
    val servers: StateFlow<List<VpnServer>> = _servers

    private val _selectedServer = MutableStateFlow<VpnServer?>(null)
    val selectedServer: StateFlow<VpnServer?> = _selectedServer

    val vpnState: StateFlow<VpnState> = vpnEngine.currentState

    private val _connectionTime = MutableStateFlow(0)
    val connectionTime: StateFlow<Int> = _connectionTime

    private var timerJob: Job? = null

    init {
        loadServers()
        viewModelScope.launch {
            vpnEngine.currentState.collect { state ->
                if (state == VpnState.CONNECTED) {
                    startTimer()
                } else if (state == VpnState.DISCONNECTED || state == VpnState.ERROR) {
                    stopTimer()
                }
            }
        }
    }

    private fun loadServers() {
        val loadedServers = repository.getServers()
        _servers.value = loadedServers
        if (loadedServers.isNotEmpty()) {
            _selectedServer.value = loadedServers.first()
        }
    }

    fun selectServer(server: VpnServer) {
        _selectedServer.value = server
        if (vpnEngine.currentState.value == VpnState.CONNECTED) {
            viewModelScope.launch {
                vpnEngine.disconnect()
            }
        }
    }

    fun toggleConnection() {
        viewModelScope.launch {
            val server = _selectedServer.value ?: return@launch
            
            if (vpnEngine.currentState.value == VpnState.DISCONNECTED || vpnEngine.currentState.value == VpnState.ERROR) {
                vpnEngine.connect(server)
            } else if (vpnEngine.currentState.value == VpnState.CONNECTED) {
                vpnEngine.disconnect()
            }
        }
    }

    private fun startTimer() {
        _connectionTime.value = 0
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _connectionTime.value += 1
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}
