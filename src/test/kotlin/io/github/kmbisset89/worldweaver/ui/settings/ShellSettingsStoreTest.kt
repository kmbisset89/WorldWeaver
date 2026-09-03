package io.github.kmbisset89.worldweaver.ui.settings

import io.github.kmbisset89.worldweaver.ui.theme.ThemeMode
import io.github.kmbisset89.worldweaver.ui.theme.ThemeSkin
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ShellSettingsStoreTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @Test
    fun defaultsAreSystemFantasyExpandedAndLocalAuthor() {
        val store = ShellSettingsStore(preferences)
        val settings = store.settings.value

        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertEquals(ThemeSkin.FANTASY, settings.themeSkin)
        assertEquals(true, settings.navExpanded)
        assertEquals(ShellSettings.DEFAULT_DISPLAY_NAME, settings.displayName)
        assertEquals(ShellSettings.DEFAULT_EMAIL, settings.email)
    }

    @Test
    fun writesSurviveANewStoreInstance() {
        val store = ShellSettingsStore(preferences)
        store.setThemeMode(ThemeMode.DARK)
        store.setThemeSkin(ThemeSkin.GOTHIC)
        store.setNavExpanded(false)
        store.setProfile(displayName = "Ada", email = "ada@local")

        val reloaded = ShellSettingsStore(preferences).settings.value
        assertEquals(ThemeMode.DARK, reloaded.themeMode)
        assertEquals(ThemeSkin.GOTHIC, reloaded.themeSkin)
        assertEquals(false, reloaded.navExpanded)
        assertEquals("Ada", reloaded.displayName)
        assertEquals("ada@local", reloaded.email)
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.shell"
    }
}
