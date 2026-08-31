package net.tactware.worldweaver.ui.settings

import net.tactware.worldweaver.ui.theme.ThemeMode
import net.tactware.worldweaver.ui.theme.ThemeSkin
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ShellSettingsStoreTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)
    private val legacyPreferences = Preferences.userRoot().node(TEST_LEGACY_NODE)

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
        legacyPreferences.removeNode()
    }

    @Test
    fun defaultsAreSystemFantasyExpandedAndLocalAuthor() {
        val store = ShellSettingsStore(preferences, legacyPreferences)
        val settings = store.settings.value

        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertEquals(ThemeSkin.FANTASY, settings.themeSkin)
        assertEquals(true, settings.navExpanded)
        assertEquals(ShellSettings.DEFAULT_DISPLAY_NAME, settings.displayName)
        assertEquals(ShellSettings.DEFAULT_EMAIL, settings.email)
    }

    @Test
    fun writesSurviveANewStoreInstance() {
        val store = ShellSettingsStore(preferences, legacyPreferences)
        store.setThemeMode(ThemeMode.DARK)
        store.setThemeSkin(ThemeSkin.GOTHIC)
        store.setNavExpanded(false)
        store.setProfile(displayName = "Ada", email = "ada@local")

        val reloaded = ShellSettingsStore(preferences, legacyPreferences).settings.value
        assertEquals(ThemeMode.DARK, reloaded.themeMode)
        assertEquals(ThemeSkin.GOTHIC, reloaded.themeSkin)
        assertEquals(false, reloaded.navExpanded)
        assertEquals("Ada", reloaded.displayName)
        assertEquals("ada@local", reloaded.email)
    }

    @Test
    fun migratesLegacyThemeModeOnce() {
        legacyPreferences.put("theme_mode", ThemeMode.LIGHT.name)

        val store = ShellSettingsStore(preferences, legacyPreferences)
        assertEquals(ThemeMode.LIGHT, store.settings.value.themeMode)
        assertEquals(ThemeMode.LIGHT.name, preferences.get("theme_mode", null))

        legacyPreferences.put("theme_mode", ThemeMode.DARK.name)
        val reloaded = ShellSettingsStore(preferences, legacyPreferences)
        assertEquals(ThemeMode.LIGHT, reloaded.settings.value.themeMode)
    }

    private companion object {
        const val TEST_NODE = "net.tactware.worldweaver.test.shell"
        const val TEST_LEGACY_NODE = "net.tactware.worldweaver.test.shell.legacy"
    }
}
