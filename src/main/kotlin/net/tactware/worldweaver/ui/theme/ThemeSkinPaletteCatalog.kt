package net.tactware.worldweaver.ui.theme

import androidx.compose.ui.graphics.Color

internal object ThemeSkinPaletteCatalog {
    fun palette(skin: ThemeSkin, dark: Boolean): ThemeSkinPalette {
        return when (skin) {
            ThemeSkin.FANTASY -> if (dark) fantasyDark() else fantasyLight()
            ThemeSkin.SCI_FI -> if (dark) sciFiDark() else sciFiLight()
            ThemeSkin.MODERN -> if (dark) modernDark() else modernLight()
            ThemeSkin.DARK_ACADEMIA -> if (dark) darkAcademiaDark() else darkAcademiaLight()
            ThemeSkin.HIGH_FANTASY -> if (dark) highFantasyDark() else highFantasyLight()
            ThemeSkin.GOTHIC -> if (dark) gothicDark() else gothicLight()
            ThemeSkin.STEAMPUNK -> if (dark) steampunkDark() else steampunkLight()
            ThemeSkin.CYBERPUNK -> if (dark) cyberpunkDark() else cyberpunkLight()
            ThemeSkin.COZY_TAVERN -> if (dark) cozyTavernDark() else cozyTavernLight()
            ThemeSkin.MINIMAL_MONOCHROME -> if (dark) minimalMonoDark() else minimalMonoLight()
        }
    }

    private fun fantasyLight(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF2B1B12),
        background = Color(0xFFF4E9D2),
        onBackground = Color(0xFF2A1E12),
        surface = Color(0xFFFFF6E4),
        onSurface = Color(0xFF2A1E12),
        surfaceVariant = Color(0xFFE9D8B7),
        onSurfaceVariant = Color(0xFF5B4633),
        primary = Color(0xFF8B1E2D),
        onPrimary = Color(0xFFFFF5EF),
        primaryContainer = Color(0xFFF2D4D9),
        onPrimaryContainer = Color(0xFF3E0C14),
        secondary = Color(0xFFB08A2E),
        onSecondary = Color(0xFF2A1E12),
        secondaryContainer = Color(0xFFF2E4C2),
        onSecondaryContainer = Color(0xFF352607),
        outline = Color(0xFF6B5B4A),
    )

    private fun fantasyDark(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF20140D),
        background = Color(0xFF14100B),
        onBackground = Color(0xFFF4E9D2),
        surface = Color(0xFF1A130E),
        onSurface = Color(0xFFF4E9D2),
        surfaceVariant = Color(0xFF241B14),
        onSurfaceVariant = Color(0xFFCBBDA7),
        primary = Color(0xFFB03045),
        onPrimary = Color(0xFFFFF5EF),
        primaryContainer = Color(0xFF3A1A21),
        onPrimaryContainer = Color(0xFFFFD7DE),
        secondary = Color(0xFFD6B15A),
        onSecondary = Color(0xFF1A130A),
        secondaryContainer = Color(0xFF3A2D16),
        onSecondaryContainer = Color(0xFFFFF1D1),
        outline = Color(0xFF5C5248),
    )

    private fun sciFiLight(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF0B1220),
        background = Color(0xFFF4F7FF),
        onBackground = Color(0xFF0B1220),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0B1220),
        surfaceVariant = Color(0xFFE3EAF7),
        onSurfaceVariant = Color(0xFF24324A),
        primary = Color(0xFF3A6BFF),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDDE6FF),
        onPrimaryContainer = Color(0xFF0A2B86),
        secondary = Color(0xFF19C3FF),
        onSecondary = Color(0xFF06212A),
        secondaryContainer = Color(0xFFCCF2FF),
        onSecondaryContainer = Color(0xFF003544),
        outline = Color(0xFF3D4A64),
    )

    private fun sciFiDark(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF070B14),
        background = Color(0xFF070B14),
        onBackground = Color(0xFFEAF1FF),
        surface = Color(0xFF0B1220),
        onSurface = Color(0xFFEAF1FF),
        surfaceVariant = Color(0xFF111B31),
        onSurfaceVariant = Color(0xFFB7C4E6),
        primary = Color(0xFF6B8CFF),
        onPrimary = Color(0xFF06112A),
        primaryContainer = Color(0xFF1B2B5E),
        onPrimaryContainer = Color(0xFFDDE6FF),
        secondary = Color(0xFF4DD7FF),
        onSecondary = Color(0xFF041A22),
        secondaryContainer = Color(0xFF003646),
        onSecondaryContainer = Color(0xFFCCF2FF),
        outline = Color(0xFF3B4762),
    )

    private fun modernLight(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF111827),
        background = Color(0xFFF7F7F8),
        onBackground = Color(0xFF101114),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF101114),
        surfaceVariant = Color(0xFFEEF0F3),
        onSurfaceVariant = Color(0xFF2F3747),
        primary = Color(0xFF2D6CDF),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDCE8FF),
        onPrimaryContainer = Color(0xFF0A2A66),
        secondary = Color(0xFF6B7280),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE5E7EB),
        onSecondaryContainer = Color(0xFF111827),
        outline = Color(0xFF6B7280),
    )

    private fun modernDark(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF0B0D12),
        background = Color(0xFF0B0D12),
        onBackground = Color(0xFFEDEFF3),
        surface = Color(0xFF0F131B),
        onSurface = Color(0xFFEDEFF3),
        surfaceVariant = Color(0xFF151B26),
        onSurfaceVariant = Color(0xFFB8C0D2),
        primary = Color(0xFF7AA6FF),
        onPrimary = Color(0xFF081226),
        primaryContainer = Color(0xFF1B2B52),
        onPrimaryContainer = Color(0xFFDCE8FF),
        secondary = Color(0xFF9CA3AF),
        onSecondary = Color(0xFF0B0D12),
        secondaryContainer = Color(0xFF2A3345),
        onSecondaryContainer = Color(0xFFE5E7EB),
        outline = Color(0xFF465069),
    )

    private fun darkAcademiaLight(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF1B1713),
        background = Color(0xFFF3EEE4),
        onBackground = Color(0xFF1D1A16),
        surface = Color(0xFFFFFBF4),
        onSurface = Color(0xFF1D1A16),
        surfaceVariant = Color(0xFFE6DDCD),
        onSurfaceVariant = Color(0xFF3F382F),
        primary = Color(0xFF4C2A2A),
        onPrimary = Color(0xFFFFF2F2),
        primaryContainer = Color(0xFFE7D2D2),
        onPrimaryContainer = Color(0xFF2D1414),
        secondary = Color(0xFF6C5A3B),
        onSecondary = Color(0xFF1D1A16),
        secondaryContainer = Color(0xFFE8DEC9),
        onSecondaryContainer = Color(0xFF2A2214),
        outline = Color(0xFF5B5246),
    )

    private fun darkAcademiaDark(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF0E0C0A),
        background = Color(0xFF0E0C0A),
        onBackground = Color(0xFFF3EEE4),
        surface = Color(0xFF15110E),
        onSurface = Color(0xFFF3EEE4),
        surfaceVariant = Color(0xFF1F1914),
        onSurfaceVariant = Color(0xFFD5CABA),
        primary = Color(0xFFB97878),
        onPrimary = Color(0xFF241111),
        primaryContainer = Color(0xFF2A1717),
        onPrimaryContainer = Color(0xFFFFDADA),
        secondary = Color(0xFFD1B07A),
        onSecondary = Color(0xFF221B10),
        secondaryContainer = Color(0xFF2E2415),
        onSecondaryContainer = Color(0xFFFFE9C5),
        outline = Color(0xFF544B40),
    )

    private fun highFantasyLight(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF2A2A1F),
        background = Color(0xFFF6F1E3),
        onBackground = Color(0xFF1B1A14),
        surface = Color(0xFFFFFBF0),
        onSurface = Color(0xFF1B1A14),
        surfaceVariant = Color(0xFFE9E0C8),
        onSurfaceVariant = Color(0xFF403B2C),
        primary = Color(0xFF2E7D32),
        onPrimary = Color(0xFFF2FFF3),
        primaryContainer = Color(0xFFD6F1D6),
        onPrimaryContainer = Color(0xFF103515),
        secondary = Color(0xFF6A4C93),
        onSecondary = Color(0xFFFFF3FF),
        secondaryContainer = Color(0xFFE8D9F7),
        onSecondaryContainer = Color(0xFF2A0F45),
        outline = Color(0xFF5D5A48),
    )

    private fun highFantasyDark(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF0B0F0C),
        background = Color(0xFF0D120E),
        onBackground = Color(0xFFF6F1E3),
        surface = Color(0xFF111815),
        onSurface = Color(0xFFF6F1E3),
        surfaceVariant = Color(0xFF18231D),
        onSurfaceVariant = Color(0xFFD7D0BC),
        primary = Color(0xFF6FE37A),
        onPrimary = Color(0xFF05220C),
        primaryContainer = Color(0xFF12311A),
        onPrimaryContainer = Color(0xFFCFFAD4),
        secondary = Color(0xFFC7A3FF),
        onSecondary = Color(0xFF1C0C2C),
        secondaryContainer = Color(0xFF2B1C40),
        onSecondaryContainer = Color(0xFFF1E5FF),
        outline = Color(0xFF4E5648),
    )

    private fun gothicLight(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF141013),
        background = Color(0xFFF2ECEB),
        onBackground = Color(0xFF1B1415),
        surface = Color(0xFFFFF7F6),
        onSurface = Color(0xFF1B1415),
        surfaceVariant = Color(0xFFE5D8D6),
        onSurfaceVariant = Color(0xFF3F3334),
        primary = Color(0xFF5A0F24),
        onPrimary = Color(0xFFFFF1F4),
        primaryContainer = Color(0xFFF0D3DB),
        onPrimaryContainer = Color(0xFF2E0712),
        secondary = Color(0xFF2E2B36),
        onSecondary = Color(0xFFF2F0F6),
        secondaryContainer = Color(0xFFDCD9E3),
        onSecondaryContainer = Color(0xFF171421),
        outline = Color(0xFF5E4C4F),
    )

    private fun gothicDark(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF0C090B),
        background = Color(0xFF0C090B),
        onBackground = Color(0xFFF2ECEB),
        surface = Color(0xFF120E11),
        onSurface = Color(0xFFF2ECEB),
        surfaceVariant = Color(0xFF1A1217),
        onSurfaceVariant = Color(0xFFD5C7C7),
        primary = Color(0xFFE06A8A),
        onPrimary = Color(0xFF2A0A13),
        primaryContainer = Color(0xFF2C1219),
        onPrimaryContainer = Color(0xFFFFD9E2),
        secondary = Color(0xFFB7A9D6),
        onSecondary = Color(0xFF1A1425),
        secondaryContainer = Color(0xFF241B2B),
        onSecondaryContainer = Color(0xFFEDE7FF),
        outline = Color(0xFF514146),
    )

    private fun steampunkLight(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF2A1A12),
        background = Color(0xFFF3E6D2),
        onBackground = Color(0xFF20160E),
        surface = Color(0xFFFFF3E0),
        onSurface = Color(0xFF20160E),
        surfaceVariant = Color(0xFFE7D2B3),
        onSurfaceVariant = Color(0xFF4A3A2A),
        primary = Color(0xFF8A4B2A),
        onPrimary = Color(0xFFFFF6F0),
        primaryContainer = Color(0xFFF0D2C2),
        onPrimaryContainer = Color(0xFF2F1508),
        secondary = Color(0xFF2E6F78),
        onSecondary = Color(0xFFEFFFFF),
        secondaryContainer = Color(0xFFCDECEF),
        onSecondaryContainer = Color(0xFF0B2A2E),
        outline = Color(0xFF6A5848),
    )

    private fun steampunkDark(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF100B08),
        background = Color(0xFF120C08),
        onBackground = Color(0xFFF3E6D2),
        surface = Color(0xFF19100B),
        onSurface = Color(0xFFF3E6D2),
        surfaceVariant = Color(0xFF24160F),
        onSurfaceVariant = Color(0xFFD7C6AE),
        primary = Color(0xFFD28C6B),
        onPrimary = Color(0xFF2A140A),
        primaryContainer = Color(0xFF2A160F),
        onPrimaryContainer = Color(0xFFFFDDCF),
        secondary = Color(0xFF64D2D8),
        onSecondary = Color(0xFF042123),
        secondaryContainer = Color(0xFF0A2A2D),
        onSecondaryContainer = Color(0xFFCDECEF),
        outline = Color(0xFF5C4B3E),
    )

    private fun cyberpunkLight(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF120A1A),
        background = Color(0xFFF9F5FF),
        onBackground = Color(0xFF120A1A),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF120A1A),
        surfaceVariant = Color(0xFFEFE4FF),
        onSurfaceVariant = Color(0xFF2B1C3A),
        primary = Color(0xFFFF2D95),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFD6EA),
        onPrimaryContainer = Color(0xFF3A0820),
        secondary = Color(0xFF2DE2FF),
        onSecondary = Color(0xFF06212A),
        secondaryContainer = Color(0xFFCCF7FF),
        onSecondaryContainer = Color(0xFF003544),
        outline = Color(0xFF4B3A5E),
    )

    private fun cyberpunkDark(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF07000E),
        background = Color(0xFF07000E),
        onBackground = Color(0xFFF9F5FF),
        surface = Color(0xFF0D0416),
        onSurface = Color(0xFFF9F5FF),
        surfaceVariant = Color(0xFF140A22),
        onSurfaceVariant = Color(0xFFE0D2F2),
        primary = Color(0xFFFF4FA7),
        onPrimary = Color(0xFF240012),
        primaryContainer = Color(0xFF2A0918),
        onPrimaryContainer = Color(0xFFFFD6EA),
        secondary = Color(0xFF4DF0FF),
        onSecondary = Color(0xFF041A22),
        secondaryContainer = Color(0xFF003646),
        onSecondaryContainer = Color(0xFFCCF7FF),
        outline = Color(0xFF4B3A5E),
    )

    private fun cozyTavernLight(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF2A1B12),
        background = Color(0xFFF7E6D0),
        onBackground = Color(0xFF2A1B12),
        surface = Color(0xFFFFF0DE),
        onSurface = Color(0xFF2A1B12),
        surfaceVariant = Color(0xFFEBD0B0),
        onSurfaceVariant = Color(0xFF4E3A2C),
        primary = Color(0xFFB85C2A),
        onPrimary = Color(0xFFFFF4EE),
        primaryContainer = Color(0xFFFFD8C4),
        onPrimaryContainer = Color(0xFF3A1A0B),
        secondary = Color(0xFF6B3F2A),
        onSecondary = Color(0xFFFFF1E8),
        secondaryContainer = Color(0xFFEAD7CC),
        onSecondaryContainer = Color(0xFF2A160C),
        outline = Color(0xFF6B5A4C),
    )

    private fun cozyTavernDark(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF120B07),
        background = Color(0xFF140C08),
        onBackground = Color(0xFFF7E6D0),
        surface = Color(0xFF1B100B),
        onSurface = Color(0xFFF7E6D0),
        surfaceVariant = Color(0xFF24160F),
        onSurfaceVariant = Color(0xFFD7C6AE),
        primary = Color(0xFFFFA56B),
        onPrimary = Color(0xFF2A140A),
        primaryContainer = Color(0xFF2A160F),
        onPrimaryContainer = Color(0xFFFFD8C4),
        secondary = Color(0xFFD7B39A),
        onSecondary = Color(0xFF2A140A),
        secondaryContainer = Color(0xFF2A1A12),
        onSecondaryContainer = Color(0xFFEAD7CC),
        outline = Color(0xFF5C4B3E),
    )

    private fun minimalMonoLight(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF111111),
        background = Color(0xFFFAFAFA),
        onBackground = Color(0xFF111111),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF111111),
        surfaceVariant = Color(0xFFF0F0F0),
        onSurfaceVariant = Color(0xFF2B2B2B),
        primary = Color(0xFF111111),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE6E6E6),
        onPrimaryContainer = Color(0xFF111111),
        secondary = Color(0xFF6B6B6B),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFEEEEEE),
        onSecondaryContainer = Color(0xFF1A1A1A),
        outline = Color(0xFF6B6B6B),
    )

    private fun minimalMonoDark(): ThemeSkinPalette = ThemeSkinPalette(
        navigation = Color(0xFF0A0A0A),
        background = Color(0xFF0A0A0A),
        onBackground = Color(0xFFF2F2F2),
        surface = Color(0xFF0F0F0F),
        onSurface = Color(0xFFF2F2F2),
        surfaceVariant = Color(0xFF171717),
        onSurfaceVariant = Color(0xFFCBCBCB),
        primary = Color(0xFFF2F2F2),
        onPrimary = Color(0xFF0A0A0A),
        primaryContainer = Color(0xFF222222),
        onPrimaryContainer = Color(0xFFF2F2F2),
        secondary = Color(0xFF9A9A9A),
        onSecondary = Color(0xFF0A0A0A),
        secondaryContainer = Color(0xFF2A2A2A),
        onSecondaryContainer = Color(0xFFF2F2F2),
        outline = Color(0xFF565656),
    )
}
