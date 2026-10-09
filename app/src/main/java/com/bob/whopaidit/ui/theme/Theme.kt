package com.bob.whopaidit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryAccent,
    onPrimary = OnPrimaryAccent,
    primaryContainer = PrimaryAccentActive,
    onPrimaryContainer = OnPrimaryAccent,
    secondary = SurfaceLevel1,
    onSecondary = ContentPrimary,
    tertiary = StatusInfo,
    onTertiary = OnPrimaryAccent,
    background = BaseBackground,
    onBackground = ContentPrimary,
    surface = SurfaceLevel1,
    onSurface = ContentPrimary,
    surfaceVariant = SurfaceLevel2,
    onSurfaceVariant = ContentSecondary,
    outline = DividerStroke,
    outlineVariant = StrokeMuted,
    error = StatusNegative,
    onError = OnPrimaryAccent,
)

@Composable
fun WhoPaidItTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content,
    )
}
