package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.services.VpnState
import com.example.viewmodel.VpnViewModel
import com.example.ui.theme.*

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.VpnService
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.content.Intent

@Composable
fun HomeScreen(viewModel: VpnViewModel, onNavigateToServerList: () -> Unit) {
    val state by viewModel.vpnState.collectAsState()
    val server by viewModel.selectedServer.collectAsState()
    val timer by viewModel.connectionTime.collectAsState()
    val downloadSpeed by viewModel.downloadSpeed.collectAsState()
    val uploadSpeed by viewModel.uploadSpeed.collectAsState()
    val autoConnect by viewModel.autoConnect.collectAsState()
    val killSwitch by viewModel.killSwitch.collectAsState()
    val context = LocalContext.current
    
    var showSettings by remember { mutableStateOf(false) }

    val vpnLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.toggleConnection()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Assuming we handle but for now no explicit action
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val formattedTime = remember(timer) {
        val h = timer / 3600
        val m = (timer % 3600) / 60
        val s = timer % 60
        if (h > 0) String.format("%02d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
    }

    val buttonScale by animateFloatAsState(
        targetValue = if (state == VpnState.CONNECTING) 0.95f else 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing)
    )

    Scaffold(
        containerColor = VpnDarkBackground,
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(VpnNavBackground)
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(Icons.Default.Security, "VPN", true)
                NavBarItem(Icons.Default.Public, "Servers", false) { onNavigateToServerList() }
                NavBarItem(Icons.Default.BarChart, "Stats", false)
                NavBarItem(Icons.Default.Person, "Account", false)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(VpnSurface)
                        .border(1.dp, VpnSurfaceBorder, CircleShape)
                        .clickable { showSettings = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "settings",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Server Component
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(VpnSurface)
                        .border(1.dp, VpnSurfaceBorder, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToServerList() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Small icon element
                        Box(
                            modifier = Modifier.size(width = 30.dp, height = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = server?.countryCode?.let { countryCodeToEmojiFlag(it) } ?: "🗺️",
                                fontSize = 24.sp
                            )
                        }
                        
                        Column {
                            Text("CURRENT SERVER", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "${server?.city ?: "Select Server"}, ${server?.countryCode?.uppercase() ?: ""}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "change_server",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Central Connect Button
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 16.dp)) {
                    // Glow background
                    if (state == VpnState.CONNECTED || state == VpnState.CONNECTING) {
                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .blur(80.dp)
                                .background(VpnCyan.copy(alpha = 0.2f), CircleShape)
                        )
                    }
                    
                    // Outer Ring
                    Box(
                        modifier = Modifier
                            .size(208.dp)
                            .border(4.dp, VpnCyan.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner button
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .scale(buttonScale)
                                .clip(CircleShape)
                                .background(Brush.verticalGradient(listOf(VpnGradientStart, VpnGradientEnd)))
                                .border(1.dp, VpnSurfaceBorder, CircleShape)
                                .clickable {
                                    if (state == VpnState.DISCONNECTED || state == VpnState.ERROR) {
                                        val intent = VpnService.prepare(context)
                                        if (intent != null) {
                                            vpnLauncher.launch(intent)
                                        } else {
                                            viewModel.toggleConnection()
                                        }
                                    } else {
                                        viewModel.toggleConnection()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        // Glow for icon
                                        .background(if (state == VpnState.DISCONNECTED || state == VpnState.ERROR) Color.Gray else Color(0xFF06B6D4)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = "power_button",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = when (state) {
                                        VpnState.DISCONNECTED -> "CONNECT"
                                        VpnState.CONNECTING -> "CONNECTING"
                                        VpnState.CONNECTED -> "CONNECTED"
                                        VpnState.DISCONNECTING -> "DISCONNECT"
                                        VpnState.ERROR -> "ERROR"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state == VpnState.DISCONNECTED || state == VpnState.ERROR) Color.Gray else VpnCyan,
                                    letterSpacing = 2.sp
                                )
                            }
                        }
                    }
                }

                // Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.ArrowDownward,
                        iconColor = VpnCyan,
                        title = "DOWNLOAD",
                        value = downloadSpeed,
                        unit = "Mbps"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.ArrowUpward,
                        iconColor = VpnPurple,
                        title = "UPLOAD",
                        value = uploadSpeed,
                        unit = "Mbps"
                    )
                }

                // Connection Duration
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "CONNECTION DURATION",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (state == VpnState.CONNECTED) formattedTime else "00:00",
                        fontSize = 32.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
    
    if (showSettings) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSettings = false },
            containerColor = VpnSurface,
            titleContentColor = Color.White,
            textContentColor = Color.Gray,
            title = {
                Text(text = "Settings", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Auto-Connect on Startup", color = Color.White, fontSize = 16.sp)
                        androidx.compose.material3.Switch(
                            checked = autoConnect,
                            onCheckedChange = { viewModel.toggleAutoConnect(it) },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = VpnCyan,
                                checkedTrackColor = VpnCyan.copy(alpha = 0.5f)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Kill Switch", color = Color.White, fontSize = 16.sp)
                        androidx.compose.material3.Switch(
                            checked = killSwitch,
                            onCheckedChange = { viewModel.toggleKillSwitch(it) },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = VpnCyan,
                                checkedTrackColor = VpnCyan.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showSettings = false }) {
                    Text("DONE", color = VpnCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    unit: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(VpnSurface)
            .border(1.dp, VpnSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(title, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.alignByBaseline())
                Text(unit, color = Color.Gray, fontSize = 10.sp, modifier = Modifier.alignByBaseline())
            }
        }
    }
}

@Composable
fun NavBarItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit = {}) {
    val color = if (isSelected) VpnCyan else Color.Gray
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
