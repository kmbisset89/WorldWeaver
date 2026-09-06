package io.github.kmbisset89.worldweaver.ui.atmosphere

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.ActivateAtmosphereSceneUseCase
import io.github.kmbisset89.worldweaver.domain.ApplyAtmosphereLookUseCase
import io.github.kmbisset89.worldweaver.domain.AtmosphereMood
import io.github.kmbisset89.worldweaver.domain.AtmosphereScene
import io.github.kmbisset89.worldweaver.domain.AtmosphereSettingsStore
import io.github.kmbisset89.worldweaver.domain.CreateAtmosphereMoodUseCase
import io.github.kmbisset89.worldweaver.domain.CreateAtmosphereSceneUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteAtmosphereMoodUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteAtmosphereSceneUseCase
import io.github.kmbisset89.worldweaver.domain.DiscoverHueBridgesUseCase
import io.github.kmbisset89.worldweaver.domain.EntityIdFactory
import io.github.kmbisset89.worldweaver.domain.FakeGoveeLightingClient
import io.github.kmbisset89.worldweaver.domain.FakeHomeAssistantClient
import io.github.kmbisset89.worldweaver.domain.FakeHueClient
import io.github.kmbisset89.worldweaver.domain.GoveeDevice
import io.github.kmbisset89.worldweaver.domain.GoveeLightingClient
import io.github.kmbisset89.worldweaver.domain.GoveeLightingPreset
import io.github.kmbisset89.worldweaver.domain.HomeAssistantClient
import io.github.kmbisset89.worldweaver.domain.HomeAssistantConnection
import io.github.kmbisset89.worldweaver.domain.HomeAssistantConnectionParser
import io.github.kmbisset89.worldweaver.domain.HomeAssistantScene
import io.github.kmbisset89.worldweaver.domain.HueBridgeHostParser
import io.github.kmbisset89.worldweaver.domain.HueClient
import io.github.kmbisset89.worldweaver.domain.HueConnection
import io.github.kmbisset89.worldweaver.domain.HueLight
import io.github.kmbisset89.worldweaver.domain.HueScene
import io.github.kmbisset89.worldweaver.domain.ListHomeAssistantScenesUseCase
import io.github.kmbisset89.worldweaver.domain.ListHueLightsUseCase
import io.github.kmbisset89.worldweaver.domain.ListHueScenesUseCase
import io.github.kmbisset89.worldweaver.domain.PairHueBridgeUseCase
import io.github.kmbisset89.worldweaver.domain.SaveHomeAssistantConnectionUseCase
import io.github.kmbisset89.worldweaver.domain.SaveHueConnectionUseCase
import io.github.kmbisset89.worldweaver.domain.ScanGoveeDevicesUseCase
import io.github.kmbisset89.worldweaver.domain.TestHomeAssistantConnectionUseCase
import io.github.kmbisset89.worldweaver.domain.TestHueConnectionUseCase
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class AtmosphereViewModelTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)
    private val scope = AppCoroutineScope()
    private val client = FakeHomeAssistantClient()
    private val hueClient = FakeHueClient()
    private val goveeClient = FakeGoveeLightingClient()

    @AfterTest
    fun tearDown() {
        scope.cancel()
        preferences.removeNode()
    }

    @Test
    fun emptyConnectionShowsAsNotConfigured() {
        val viewModel = viewModel()
        val state = content(viewModel)
        assertFalse(state.isConfigured)
        assertFalse(state.isFloatingOpen)
        assertTrue(state.scenes.isEmpty())
    }

    @Test
    fun saveConnectionPersistsAndClearsDirtyFlag() {
        val viewModel = viewModel()

        viewModel.onInteraction(AtmosphereInteraction.BaseUrlChanged("http://ha.local:8123/"))
        viewModel.onInteraction(AtmosphereInteraction.TokenChanged("token"))
        viewModel.onInteraction(AtmosphereInteraction.ConnectionSaveSelected)

        val state = content(viewModel)
        assertEquals("http://ha.local:8123", state.savedBaseUrl)
        assertEquals("token", state.savedToken)
        assertFalse(state.isConnectionDirty)
        assertTrue(state.isConfigured)
        assertTrue(state.isHomeAssistantConfigured)
    }

    @Test
    fun invalidSaveShowsConnectionError() {
        val viewModel = viewModel()
        viewModel.onInteraction(AtmosphereInteraction.BaseUrlChanged("not-a-url"))
        viewModel.onInteraction(AtmosphereInteraction.TokenChanged("token"))
        viewModel.onInteraction(AtmosphereInteraction.ConnectionSaveSelected)

        val failed = assertIs<AtmosphereViewState.ConnectionCheck.Failed>(content(viewModel).connectionCheck)
        assertEquals("Enter a URL such as http://homeassistant.local:8123", failed.message)
    }

    @Test
    fun testConnectionLoadsCatalog() {
        client.sceneListResult = HomeAssistantClient.SceneListResult.Scenes(
            listOf(HomeAssistantScene("scene.tavern", "Tavern")),
        )
        val viewModel = viewModel()
        viewModel.onInteraction(AtmosphereInteraction.BaseUrlChanged("http://ha.local:8123"))
        viewModel.onInteraction(AtmosphereInteraction.TokenChanged("token"))
        viewModel.onInteraction(AtmosphereInteraction.ConnectionTestSelected)

        val state = awaitContent(viewModel) { it.catalog.isNotEmpty() && !it.isTestingConnection }
        assertEquals(AtmosphereViewState.ConnectionCheck.Connected, state.connectionCheck)
        assertEquals(listOf(HomeAssistantScene("scene.tavern", "Tavern")), state.catalog)
    }

    @Test
    fun pairHueStoresApplicationKey() {
        hueClient.pairResult = HueClient.PairResult.Paired("hue-app-key")
        val viewModel = viewModel()
        viewModel.onInteraction(AtmosphereInteraction.HueHostChanged("192.168.1.40"))
        viewModel.onInteraction(AtmosphereInteraction.HuePairSelected)

        val state = awaitContent(viewModel) { it.isHueConfigured && !it.isPairingHue }
        assertEquals("192.168.1.40", state.savedHueHost)
        assertEquals("hue-app-key", state.savedHueKey)
        assertTrue(state.isConfigured)
        assertEquals("192.168.1.40", hueClient.lastPairHost)
    }

    @Test
    fun scanGoveeMarksConfigured() {
        goveeClient.scanResult = GoveeLightingClient.ScanResult.Devices(
            listOf(GoveeDevice("AA:BB:CC", "192.168.1.50", "H6072")),
        )
        val viewModel = viewModel()
        viewModel.onInteraction(AtmosphereInteraction.GoveeScanSelected)

        val state = awaitContent(viewModel) { it.hasGoveeDevices && !it.isScanningGovee }
        assertEquals(1, state.goveeDevices.size)
        assertTrue(state.isConfigured)
    }

    @Test
    fun createAndActivateScene() {
        val store = AtmosphereSettingsStore(preferences)
        store.setConnection(HomeAssistantConnection("http://ha.local:8123", "token"))
        val viewModel = viewModel(store)

        viewModel.onInteraction(AtmosphereInteraction.DraftSceneNameChanged("Tavern"))
        viewModel.onInteraction(AtmosphereInteraction.DraftEntityIdChanged("scene.tavern"))
        viewModel.onInteraction(AtmosphereInteraction.SceneCreateSelected)

        val created = content(viewModel)
        assertEquals(1, created.scenes.size)
        assertEquals("Tavern", created.scenes.single().name)
        assertEquals("", created.draftSceneName)

        viewModel.onInteraction(AtmosphereInteraction.SceneActivateSelected(created.scenes.single().id))
        val activated = awaitContent(viewModel) { it.lastActivatedSceneId != null && !it.isActivating }
        assertEquals(created.scenes.single().id, activated.lastActivatedSceneId)
        assertNull(activated.activationError)
        assertEquals("scene.tavern", client.lastActivatedEntityId)
    }

    @Test
    fun createHueSceneFromCatalog() {
        hueClient.sceneListResult = HueClient.SceneListResult.Scenes(
            listOf(HueScene(id = "hue-combat", name = "Combat", groupId = "1")),
        )
        hueClient.lightListResult = HueClient.LightListResult.Lights(
            listOf(HueLight(id = "light-1", name = "Table lamp")),
        )
        val store = AtmosphereSettingsStore(preferences)
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))
        val viewModel = viewModel(store)

        viewModel.onInteraction(AtmosphereInteraction.HueCatalogRefreshSelected)
        awaitContent(viewModel) { it.hueCatalog.isNotEmpty() && !it.isLoadingHueCatalog }
        viewModel.onInteraction(AtmosphereInteraction.HueLightsRefreshSelected)
        awaitContent(viewModel) { it.hueLights.isNotEmpty() && !it.isLoadingHueLights }
        viewModel.onInteraction(AtmosphereInteraction.HueLightToggled("light-1"))
        viewModel.onInteraction(AtmosphereInteraction.HueCatalogSceneSelected("hue-combat"))
        viewModel.onInteraction(AtmosphereInteraction.DraftSceneNameChanged("Combat"))
        viewModel.onInteraction(AtmosphereInteraction.SceneCreateSelected)

        val created = content(viewModel).scenes.single()
        assertEquals("hue-combat", created.hueSceneId)
        assertEquals("1", created.hueGroupId)
        assertEquals(listOf("light-1"), created.hueLightIds)
        assertEquals("", created.entityId)
        assertEquals(listOf("light-1"), content(viewModel).selectedHueLightIds)
    }

    @Test
    fun createHueLookFromSelectedLights() {
        hueClient.lightListResult = HueClient.LightListResult.Lights(
            listOf(HueLight(id = "light-1", name = "Table lamp")),
        )
        val store = AtmosphereSettingsStore(preferences)
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))
        val viewModel = viewModel(store)

        viewModel.onInteraction(AtmosphereInteraction.HueLightsRefreshSelected)
        awaitContent(viewModel) { it.hueLights.isNotEmpty() && !it.isLoadingHueLights }
        viewModel.onInteraction(AtmosphereInteraction.HueLightToggled("light-1"))
        viewModel.onInteraction(AtmosphereInteraction.DraftSceneNameChanged("Warm"))
        viewModel.onInteraction(AtmosphereInteraction.SceneCreateSelected)

        val created = content(viewModel).scenes.single()
        assertEquals("", created.hueSceneId)
        assertEquals(listOf("light-1"), created.hueLightIds)
        assertEquals("#E39B5A", created.goveeColorHex)
        assertEquals(55, created.goveeBrightness)
        assertEquals(listOf("light-1"), content(viewModel).selectedHueLightIds)
        assertNull(content(viewModel).sceneError)
    }

    @Test
    fun lookPresetAppliesColorToSelectedHueLights() {
        hueClient.lightListResult = HueClient.LightListResult.Lights(
            listOf(HueLight(id = "light-1", name = "Table lamp")),
        )
        hueClient.sceneListResult = HueClient.SceneListResult.Scenes(
            listOf(HueScene(id = "hue-combat", name = "Combat", groupId = "1")),
        )
        val store = AtmosphereSettingsStore(preferences)
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))
        val viewModel = viewModel(store)

        viewModel.onInteraction(AtmosphereInteraction.HueLightsRefreshSelected)
        awaitContent(viewModel) { it.hueLights.isNotEmpty() && !it.isLoadingHueLights }
        viewModel.onInteraction(AtmosphereInteraction.HueCatalogRefreshSelected)
        awaitContent(viewModel) { it.hueCatalog.isNotEmpty() && !it.isLoadingHueCatalog }
        viewModel.onInteraction(AtmosphereInteraction.HueCatalogSceneSelected("hue-combat"))
        viewModel.onInteraction(AtmosphereInteraction.HueLightToggled("light-1"))
        viewModel.onInteraction(
            AtmosphereInteraction.LookPresetSelected(
                colorHex = GoveeLightingPreset.COMBAT.colorHex,
                brightness = GoveeLightingPreset.COMBAT.brightness,
                powerOn = GoveeLightingPreset.COMBAT.powerOn,
            ),
        )

        runBlocking {
            withTimeout(3_000) {
                while (hueClient.lastColorLightIds.isEmpty()) {
                    delay(10)
                }
            }
        }
        assertEquals(listOf("light-1"), hueClient.lastColorLightIds)
        assertEquals(196, hueClient.lastColorRed)
        assertEquals(90, hueClient.lastColorBrightness)
        assertNull(hueClient.lastActivatedSceneId)
        assertNull(content(viewModel).selectedHueSceneId)
    }

    @Test
    fun lookColorChangeAppliesAfterDebounceAndClearsHueScene() {
        hueClient.lightListResult = HueClient.LightListResult.Lights(
            listOf(HueLight(id = "light-1", name = "Table lamp")),
        )
        hueClient.sceneListResult = HueClient.SceneListResult.Scenes(
            listOf(HueScene(id = "hue-combat", name = "Combat", groupId = "1")),
        )
        val store = AtmosphereSettingsStore(preferences)
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))
        val viewModel = viewModel(store)

        viewModel.onInteraction(AtmosphereInteraction.HueLightsRefreshSelected)
        awaitContent(viewModel) { it.hueLights.isNotEmpty() && !it.isLoadingHueLights }
        viewModel.onInteraction(AtmosphereInteraction.HueCatalogRefreshSelected)
        awaitContent(viewModel) { it.hueCatalog.isNotEmpty() && !it.isLoadingHueCatalog }
        viewModel.onInteraction(AtmosphereInteraction.HueCatalogSceneSelected("hue-combat"))
        viewModel.onInteraction(AtmosphereInteraction.HueLightToggled("light-1"))
        viewModel.onInteraction(AtmosphereInteraction.LookColorHexChanged("#C41E3A"))

        awaitAppliedColor(hueClient)
        assertEquals(listOf("light-1"), hueClient.lastColorLightIds)
        assertEquals(196, hueClient.lastColorRed)
        assertEquals(30, hueClient.lastColorGreen)
        assertEquals(58, hueClient.lastColorBlue)
        assertNull(content(viewModel).selectedHueSceneId)
        assertEquals("#C41E3A", content(viewModel).draftLookColorHex)
    }

    @Test
    fun lookBrightnessChangeAppliesAfterDebounce() {
        hueClient.lightListResult = HueClient.LightListResult.Lights(
            listOf(HueLight(id = "light-1", name = "Table lamp")),
        )
        val store = AtmosphereSettingsStore(preferences)
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))
        val viewModel = viewModel(store)

        viewModel.onInteraction(AtmosphereInteraction.HueLightsRefreshSelected)
        awaitContent(viewModel) { it.hueLights.isNotEmpty() && !it.isLoadingHueLights }
        viewModel.onInteraction(AtmosphereInteraction.HueLightToggled("light-1"))
        viewModel.onInteraction(AtmosphereInteraction.LookBrightnessChanged("40"))

        awaitAppliedColor(hueClient)
        assertEquals(listOf("light-1"), hueClient.lastColorLightIds)
        assertEquals(40, hueClient.lastColorBrightness)
        assertEquals(227, hueClient.lastColorRed)
        assertEquals(true, hueClient.lastColorPowerOn)
    }

    @Test
    fun lookPowerOffAppliesAfterDebounce() {
        hueClient.lightListResult = HueClient.LightListResult.Lights(
            listOf(HueLight(id = "light-1", name = "Table lamp")),
        )
        val store = AtmosphereSettingsStore(preferences)
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))
        val viewModel = viewModel(store)

        viewModel.onInteraction(AtmosphereInteraction.HueLightsRefreshSelected)
        awaitContent(viewModel) { it.hueLights.isNotEmpty() && !it.isLoadingHueLights }
        viewModel.onInteraction(AtmosphereInteraction.HueLightToggled("light-1"))
        viewModel.onInteraction(AtmosphereInteraction.LookPowerChanged(false))

        awaitAppliedColor(hueClient)
        assertEquals(listOf("light-1"), hueClient.lastColorLightIds)
        assertEquals(false, hueClient.lastColorPowerOn)
        assertFalse(content(viewModel).draftLookPowerOn)
    }

    @Test
    fun invalidLookColorHexDoesNotApply() {
        hueClient.lightListResult = HueClient.LightListResult.Lights(
            listOf(HueLight(id = "light-1", name = "Table lamp")),
        )
        val store = AtmosphereSettingsStore(preferences)
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))
        val viewModel = viewModel(store)

        viewModel.onInteraction(AtmosphereInteraction.HueLightsRefreshSelected)
        awaitContent(viewModel) { it.hueLights.isNotEmpty() && !it.isLoadingHueLights }
        viewModel.onInteraction(AtmosphereInteraction.HueLightToggled("light-1"))
        viewModel.onInteraction(AtmosphereInteraction.LookColorHexChanged("#ZZZZZZ"))

        runBlocking { delay(400) }
        assertTrue(hueClient.lastColorLightIds.isEmpty())
        assertNull(hueClient.lastColorRed)
        assertNull(content(viewModel).lookError)
    }

    @Test
    fun saveCustomMoodFromCurrentLook() {
        val viewModel = viewModel()
        viewModel.onInteraction(AtmosphereInteraction.LookColorHexChanged("#C4A35A"))
        viewModel.onInteraction(AtmosphereInteraction.LookBrightnessChanged("40"))
        viewModel.onInteraction(AtmosphereInteraction.LookMoodNameChanged("Temple"))
        viewModel.onInteraction(AtmosphereInteraction.LookMoodSaveSelected)

        val state = content(viewModel)
        val mood = state.moods.single()
        assertEquals("mood-1", mood.id)
        assertEquals("Temple", mood.name)
        assertEquals("#C4A35A", mood.colorHex)
        assertEquals(40, mood.brightness)
        assertEquals("", state.draftMoodName)
        assertNull(state.moodError)
    }

    @Test
    fun saveMoodRejectsBuiltInName() {
        val viewModel = viewModel()
        viewModel.onInteraction(AtmosphereInteraction.LookMoodNameChanged("Warm"))
        viewModel.onInteraction(AtmosphereInteraction.LookMoodSaveSelected)

        val state = content(viewModel)
        assertEquals("A mood with that name already exists", state.moodError)
        assertEquals(emptyList(), content(viewModel).moods)
    }

    @Test
    fun selectedLightsPersistForLookTray() {
        hueClient.lightListResult = HueClient.LightListResult.Lights(
            listOf(HueLight(id = "light-1", name = "Table lamp")),
        )
        val store = AtmosphereSettingsStore(preferences)
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))
        val viewModel = viewModel(store)

        viewModel.onInteraction(AtmosphereInteraction.HueLightsRefreshSelected)
        awaitContent(viewModel) { it.hueLights.isNotEmpty() && !it.isLoadingHueLights }
        viewModel.onInteraction(AtmosphereInteraction.HueLightToggled("light-1"))

        assertEquals(listOf("light-1"), store.settings.value.selectedHueLightIds)
        val reopened = viewModel(store)
        assertEquals(listOf("light-1"), content(reopened).selectedHueLightIds)
        assertTrue(content(reopened).hasLookLights)
        assertTrue(content(reopened).hasSelectedLookLights)
    }

    @Test
    fun deleteCustomMood() {
        val store = AtmosphereSettingsStore(preferences)
        store.setMoods(listOf(AtmosphereMood("m1", "Temple", true, 40, "#C4A35A", 0)))
        val viewModel = viewModel(store)

        viewModel.onInteraction(AtmosphereInteraction.LookMoodDeleteSelected("m1"))
        assertEquals(emptyList(), content(viewModel).moods)
    }

    @Test
    fun customMoodAppliesToSelectedHueLights() {
        hueClient.lightListResult = HueClient.LightListResult.Lights(
            listOf(HueLight(id = "light-1", name = "Table lamp")),
        )
        val store = AtmosphereSettingsStore(preferences)
        store.setHueConnection(HueConnection("192.168.1.40", "hue-key"))
        store.setMoods(listOf(AtmosphereMood("m1", "Temple", true, 40, "#C4A35A", 0)))
        val viewModel = viewModel(store)

        viewModel.onInteraction(AtmosphereInteraction.HueLightsRefreshSelected)
        awaitContent(viewModel) { it.hueLights.isNotEmpty() && !it.isLoadingHueLights }
        viewModel.onInteraction(AtmosphereInteraction.HueLightToggled("light-1"))
        viewModel.onInteraction(
            AtmosphereInteraction.LookPresetSelected(
                colorHex = "#C4A35A",
                brightness = 40,
                powerOn = true,
            ),
        )

        awaitAppliedColor(hueClient)
        assertEquals(listOf("light-1"), hueClient.lastColorLightIds)
        assertEquals(196, hueClient.lastColorRed)
        assertEquals(163, hueClient.lastColorGreen)
        assertEquals(90, hueClient.lastColorBlue)
        assertEquals(40, hueClient.lastColorBrightness)
        assertEquals("#C4A35A", content(viewModel).draftLookColorHex)
    }

    @Test
    fun activateWithoutConnectionShowsError() {
        val store = AtmosphereSettingsStore(preferences)
        store.setScenes(listOf(AtmosphereScene("s1", "Tavern", "scene.tavern", 0)))
        val viewModel = viewModel(store)

        viewModel.onInteraction(AtmosphereInteraction.SceneActivateSelected("s1"))
        val state = awaitContent(viewModel) { it.activationError != null && !it.isActivating }
        assertEquals("Home Assistant is not connected.", state.activationError)
    }

    @Test
    fun floatingWindowAndAlwaysOnTop() {
        val viewModel = viewModel()
        viewModel.onInteraction(AtmosphereInteraction.FloatingOpened)
        assertTrue(content(viewModel).isFloatingOpen)

        viewModel.onInteraction(AtmosphereInteraction.AlwaysOnTopToggled)
        assertTrue(content(viewModel).isAlwaysOnTop)

        viewModel.onInteraction(AtmosphereInteraction.FloatingClosed)
        assertFalse(content(viewModel).isFloatingOpen)
    }

    private fun viewModel(
        store: AtmosphereSettingsStore = AtmosphereSettingsStore(preferences),
    ): AtmosphereViewModel {
        val parser = HomeAssistantConnectionParser()
        val hueParser = HueBridgeHostParser()
        return AtmosphereViewModel(
            store = store,
            saveConnection = SaveHomeAssistantConnectionUseCase(parser, store),
            testConnection = TestHomeAssistantConnectionUseCase(parser, client),
            listScenes = ListHomeAssistantScenesUseCase(parser, client),
            saveHueConnection = SaveHueConnectionUseCase(hueParser, store),
            pairHueBridge = PairHueBridgeUseCase(hueParser, hueClient, store),
            testHueConnection = TestHueConnectionUseCase(hueParser, hueClient),
            discoverHueBridges = DiscoverHueBridgesUseCase(hueClient),
            listHueScenes = ListHueScenesUseCase(store, hueClient),
            listHueLights = ListHueLightsUseCase(store, hueClient),
            scanGoveeDevices = ScanGoveeDevicesUseCase(goveeClient, store),
            createScene = CreateAtmosphereSceneUseCase(store, EntityIdFactory { "scene-1" }),
            deleteScene = DeleteAtmosphereSceneUseCase(store),
            createMood = CreateAtmosphereMoodUseCase(store, EntityIdFactory { "mood-1" }),
            deleteMood = DeleteAtmosphereMoodUseCase(store),
            activateScene = ActivateAtmosphereSceneUseCase(
                store,
                client,
                hueClient,
                ApplyAtmosphereLookUseCase(store, hueClient, goveeClient),
            ),
            applyLook = ApplyAtmosphereLookUseCase(store, hueClient, goveeClient),
            appScope = scope,
        )
    }

    private fun content(viewModel: AtmosphereViewModel): AtmosphereViewState.Content {
        return assertIs(viewModel.state.value)
    }

    private fun awaitContent(
        viewModel: AtmosphereViewModel,
        predicate: (AtmosphereViewState.Content) -> Boolean,
    ): AtmosphereViewState.Content = runBlocking {
        withTimeout(3_000) {
            viewModel.state.filterIsInstance<AtmosphereViewState.Content>().first(predicate)
        }
    }

    private fun awaitAppliedColor(hueClient: FakeHueClient) {
        runBlocking {
            withTimeout(3_000) {
                while (hueClient.lastColorLightIds.isEmpty()) {
                    delay(10)
                }
            }
        }
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.atmosphere.vm"
    }
}
