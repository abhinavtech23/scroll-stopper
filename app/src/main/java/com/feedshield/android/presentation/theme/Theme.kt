package com.feedshield.android.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PremiumDarkScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = RichBlack,
    primaryContainer = SurfaceDark,
    onPrimaryContainer = PureWhite,
    secondary = AccentWhite,
    onSecondary = RichBlack,
    background = RichBlack,
    onBackground = TextPrimary,
    surface = CardBlack,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderMedium
)

@Composable
fun FeedShieldTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PremiumDarkScheme,
        typography = Typography,
        content = content
    )
}
