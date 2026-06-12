package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.HomeScreen
import com.example.ui.ServerListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.VpnViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val navController = rememberNavController()
        val vpnViewModel: VpnViewModel = viewModel()
        
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    viewModel = vpnViewModel,
                    onNavigateToServerList = {
                        navController.navigate("server_list")
                    }
                )
            }
            composable("server_list") {
                ServerListScreen(
                    viewModel = vpnViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
      }
    }
  }
}
