package net.tactware.worldweaver.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val LightBrandColors = BrandColors(
    primary = Color(0xFF7B141B),
    dark = Color(0xFF040404),
    accent = Color(0xFF771017),
    light = Color(0xFFA8323A),
    tintLight = Color(0xFFFBF0F1),
    tintMedium = Color(0xFFF5E6E7),
)

internal val DarkBrandColors = BrandColors(
    primary = Color(0xFFC94A54),
    dark = Color(0xFF040404),
    accent = Color(0xFFA8323A),
    light = Color(0xFFE06B74),
    tintLight = Color(0xFF2A1214),
    tintMedium = Color(0xFF35181A),
)

internal val LocalBrandColors = staticCompositionLocalOf { LightBrandColors }

/** TactWare burgundy — primary brand color. */
internal val NavyBlue: Color
    @Composable get() = LocalBrandColors.current.primary

/** TactWare charcoal — sidebar and dark surfaces. */
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

internal val WorldWeaverLightColors = lightColorScheme(
    primary = LightBrandColors.primary,
    onPrimary = Color.White,
    primaryContainer = LightBrandColors.tintMedium,
    onPrimaryContainer = LightBrandColors.dark,
    secondary = LightBrandColors.accent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCE8EA),
    onSecondaryContainer = LightBrandColors.primary,
    tertiary = Color(0xFF37474F),
    onTertiary = Color.White,
    background = Color(0xFFF8F8F8),
    onBackground = Color(0xFF333333),
    surface = Color.White,
    onSurface = Color(0xFF333333),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF7A7A7A),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFFEAEAEA)
)

internal val WorldWeaverDarkColors = darkColorScheme(
    primary = DarkBrandColors.primary,
    onPrimary = Color.White,
    primaryContainer = DarkBrandColors.tintMedium,
    onPrimaryContainer = Color(0xFFF5E6E7),
    secondary = DarkBrandColors.accent,
    onSecondary = Color.White,
    secondaryContainer = DarkBrandColors.tintLight,
    onSecondaryContainer = Color(0xFFFCE8EA),
    tertiary = Color(0xFF90A4AE),
    onTertiary = Color(0xFF040404),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE8E8E8),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE8E8E8),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFA8A8A8),
    error = Color(0xFFF87171),
    onError = Color(0xFF040404),
    outline = Color(0xFF3A3A3A)
)

@Composable
internal fun WorldWeaverTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val brandColors = if (darkTheme) DarkBrandColors else LightBrandColors

    MaterialTheme(
        colorScheme = if (darkTheme) WorldWeaverDarkColors else WorldWeaverLightColors,
        typography = Typography(),
        content = {
            CompositionLocalProvider(LocalBrandColors provides brandColors) {
                content()
            }
        }
    )
}
