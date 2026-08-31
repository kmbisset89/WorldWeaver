package net.tactware.worldweaver.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val LocalBrandColors = staticCompositionLocalOf {
    ThemeSkinPaletteCatalog.palette(ThemeSkin.FANTASY, dark = false).brandColors()
}

/** Skin primary — used for brand accents. */
internal val NavyBlue: Color
    @Composable get() = LocalBrandColors.current.primary

/** Skin navigation surface. */
internal val DarkNavy: Color
    @Composable get() = LocalBrandColors.current.dark

internal val TextPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface

internal val TextSecondary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

internal val SurfaceCard: Color
    @Composable get() = MaterialTheme.colorScheme.surface

internal val SuccessGreen = Color(0xFF10B981)
internal val ErrorRed = Color(0xFFEF4444)

@Composable
internal fun WorldWeaverTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    themeSkin: ThemeSkin = ThemeSkin.FANTASY,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val palette = ThemeSkinPaletteCatalog.palette(themeSkin, darkTheme)

    MaterialTheme(
        colorScheme = palette.colorScheme(darkTheme),
        typography = Typography(),
        content = {
            CompositionLocalProvider(LocalBrandColors provides palette.brandColors()) {
                content()
            }
        }
    )
}
