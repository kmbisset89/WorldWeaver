package net.tactware.worldweaver.ui.theme

internal enum class ThemeSkin {
    FANTASY,
    SCI_FI,
    MODERN,
    DARK_ACADEMIA,
    HIGH_FANTASY,
    GOTHIC,
    STEAMPUNK,
    CYBERPUNK,
    COZY_TAVERN,
    MINIMAL_MONOCHROME;

    fun label(): String = when (this) {
        FANTASY -> "Fantasy"
        SCI_FI -> "Sci-Fi"
        MODERN -> "Modern"
        DARK_ACADEMIA -> "Dark Academia"
        HIGH_FANTASY -> "High Fantasy"
        GOTHIC -> "Gothic"
        STEAMPUNK -> "Steampunk"
        CYBERPUNK -> "Cyberpunk"
        COZY_TAVERN -> "Cozy Tavern"
        MINIMAL_MONOCHROME -> "Minimal Monochrome"
    }
}
