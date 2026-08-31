package net.tactware.worldweaver.ui.settings

import net.tactware.worldweaver.ui.session.LocalUser
import net.tactware.worldweaver.ui.theme.ThemeMode
import net.tactware.worldweaver.ui.theme.ThemeSkin

internal data class ShellSettings(
    val themeMode: ThemeMode,
    val themeSkin: ThemeSkin,
    val displayName: String,
    val email: String,
    val navExpanded: Boolean,
) {
    fun toLocalUser(): LocalUser {
        return LocalUser(
            displayName = displayName,
            email = email,
        )
    }

    companion object {
        const val DEFAULT_DISPLAY_NAME = "Local Author"
        const val DEFAULT_EMAIL = "author@local"
    }
}
