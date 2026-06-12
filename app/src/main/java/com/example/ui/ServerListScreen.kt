package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.VpnServer
import com.example.viewmodel.VpnViewModel
import com.example.ui.theme.*

fun countryCodeToEmojiFlag(countryCode: String): String {
    if (countryCode.length != 2) return "🗺️"
    val firstLetter = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(viewModel: VpnViewModel, onBack: () -> Unit) {
    val servers by viewModel.servers.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Server", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back_button", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VpnDarkBackground
                )
            )
        },
        containerColor = VpnDarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(servers) { server ->
                ServerItem(
                    server = server,
                    isSelected = selectedServer?.id == server.id,
                    onClick = {
                        viewModel.selectServer(server)
                        onBack()
                    }
                )
            }
        }
    }
}

@Composable
fun ServerItem(server: VpnServer, isSelected: Boolean, onClick: () -> Unit) {
    val speedMbps = String.format("%.1f", server.speed / 1_000_000f)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) VpnCyan.copy(alpha = 0.1f) else VpnSurface)
            .border(1.dp, if (isSelected) VpnCyan.copy(alpha = 0.5f) else VpnSurfaceBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Text(countryCodeToEmojiFlag(server.countryCode), fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                server.city,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val pingColor = when {
                    server.ping < 100 -> Color(0xFF22C55E) // Green
                    server.ping < 200 -> Color(0xFFEAB308) // Yellow
                    else -> Color(0xFFEF4444) // Red
                }
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(pingColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text("${server.ping} ms", color = Color.Gray, fontSize = 13.sp)
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(VpnCyan))
                Spacer(modifier = Modifier.width(6.dp))
                Text("$speedMbps Mbps", color = Color.Gray, fontSize = 13.sp)
            }
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "selected",
                tint = VpnCyan
            )
        }
    }
}
