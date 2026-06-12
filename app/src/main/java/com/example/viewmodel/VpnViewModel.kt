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

    private val sharedPrefs = application.getSharedPreferences("vpn_prefs", android.content.Context.MODE_PRIVATE)

    private val _servers = MutableStateFlow<List<VpnServer>>(emptyList())
    val servers: StateFlow<List<VpnServer>> = _servers

    private val _selectedServer = MutableStateFlow<VpnServer?>(null)
    val selectedServer: StateFlow<VpnServer?> = _selectedServer

    val vpnState: StateFlow<VpnState> = vpnEngine.currentState

    private val _connectionTime = MutableStateFlow(0)
    val connectionTime: StateFlow<Int> = _connectionTime

    private val _downloadSpeed = MutableStateFlow("0.0")
    val downloadSpeed: StateFlow<String> = _downloadSpeed

    private val _uploadSpeed = MutableStateFlow("0.0")
    val uploadSpeed: StateFlow<String> = _uploadSpeed

    private val _autoConnect = MutableStateFlow(sharedPrefs.getBoolean("auto_connect", false))
    val autoConnect: StateFlow<Boolean> = _autoConnect

    private val _killSwitch = MutableStateFlow(sharedPrefs.getBoolean("kill_switch", false))
    val killSwitch: StateFlow<Boolean> = _killSwitch

    fun toggleAutoConnect(enabled: Boolean) {
        _autoConnect.value = enabled
        sharedPrefs.edit().putBoolean("auto_connect", enabled).apply()
    }

    fun toggleKillSwitch(enabled: Boolean) {
        _killSwitch.value = enabled
        sharedPrefs.edit().putBoolean("kill_switch", enabled).apply()
    }

    private val _recentServers = MutableStateFlow<List<VpnServer>>(emptyList())
    val recentServers: StateFlow<List<VpnServer>> = _recentServers

    private fun updateRecentServersFlow() {
        val orderedRecentStr = sharedPrefs.getString("recent_servers_ordered", "") ?: ""
        if (orderedRecentStr.isEmpty()) {
            _recentServers.value = emptyList()
            return
        }
        val orderedRecent = orderedRecentStr.split(",")
        val allServers = _servers.value
        _recentServers.value = orderedRecent.mapNotNull { id -> allServers.find { it.id == id } }
    }

    private fun addRecentServer(server: VpnServer) {
        val orderedRecentStr = sharedPrefs.getString("recent_servers_ordered", "") ?: ""
        var orderedRecent = if (orderedRecentStr.isEmpty()) emptyList() else orderedRecentStr.split(",")
        orderedRecent = (listOf(server.id) + orderedRecent.filter { it != server.id }).take(3)
        sharedPrefs.edit().putString("recent_servers_ordered", orderedRecent.joinToString(",")).apply()
        updateRecentServersFlow()
    }

    private var timerJob: Job? = null

    init {
        loadServers()
        viewModelScope.launch {
            vpnEngine.currentState.collect { state ->
                if (state == VpnState.CONNECTED) {
                    _selectedServer.value?.let { addRecentServer(it) }
                    startTimer()
                } else if (state == VpnState.DISCONNECTED || state == VpnState.ERROR) {
                    stopTimer()
                }
            }
        }
    }

    private fun loadServers() {
        viewModelScope.launch {
            val loadedServers = repository.getServers()
            _servers.value = loadedServers
            updateRecentServersFlow()
            if (loadedServers.isNotEmpty() && _selectedServer.value == null) {
                _selectedServer.value = loadedServers.first()
            }
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
                
                // Simulate speed based on max capability
                val baseDown = (10..50).random()
                val decDown = (0..9).random()
                _downloadSpeed.value = "$baseDown.$decDown"
                
                val baseUp = (5..20).random()
                val decUp = (0..9).random()
                _uploadSpeed.value = "$baseUp.$decUp"
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _downloadSpeed.value = "0.0"
        _uploadSpeed.value = "0.0"
    }
}
