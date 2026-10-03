package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SoulSoundColorScheme = darkColorScheme(
  primary = OrangePrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFF2A1505),
  onPrimaryContainer = OrangeSecondary,
  secondary = OrangeSecondary,
  onSecondary = Color.Black,
  tertiary = GoldAccent,
  onTertiary = Color.Black,
  background = DarkBg,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkCard,
  onSurfaceVariant = TextMuted,
  outline = BorderOrange,
  outlineVariant = BorderSubtle
)

@Composable
fun SoulSoundTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = SoulSoundColorScheme,
    typography = Typography,
    content = content
  )
}

