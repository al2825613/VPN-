package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val VpnBlue = Color(0xFF00C6FF)
val VpnPurple = Color(0xFFC084FC) // matches text-purple-400
val VpnDarkBackground = Color(0xFF0B0D17)
val VpnSurface = Color(0x0CFFFFFF) // bg-white/5
val VpnSurfaceBorder = Color(0x19FFFFFF) // border-white/10
val VpnCyan = Color(0xFF22D3EE) // text-cyan-400
val VpnCyanShadow = Color(0x3322D3EE)
val VpnGradientStart = Color(0xFF1A1F35)
val VpnGradientEnd = Color(0xFF0B0D17)
val VpnNavBackground = Color(0xCC161B2E)

private val DarkColorScheme =
  darkColorScheme(
      primary = VpnCyan,
      secondary = VpnPurple,
      background = VpnDarkBackground,
      surface = VpnSurface,
      onPrimary = Color.White,
      onBackground = Color.White,
      onSurface = Color.White,
      outline = VpnSurfaceBorder
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = Color.Transparent.toArgb()
      window.navigationBarColor = Color.Transparent.toArgb()
      WindowCompat.setDecorFitsSystemWindows(window, false)
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
      WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
    }
  }

  MaterialTheme(colorScheme = DarkColorScheme, typography = Typography, content = content)
}
