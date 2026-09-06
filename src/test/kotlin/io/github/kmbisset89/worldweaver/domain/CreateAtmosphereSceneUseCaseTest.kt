package io.github.kmbisset89.worldweaver.domain

import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class CreateAtmosphereSceneUseCaseTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @Test
    fun createsMappedScene() {
        val store = AtmosphereSettingsStore(preferences)
        val useCase = CreateAtmosphereSceneUseCase(store, EntityIdFactory { "scene-1" })

        val result = useCase("Tavern", "scene.tavern")

        val created = assertIs<CreateAtmosphereSceneUseCase.Result.Created>(result)
        assertEquals("scene-1", created.scene.id)
        assertEquals("Tavern", created.scene.name)
        assertEquals("scene.tavern", created.scene.entityId)
        assertEquals(0, created.scene.sortOrder)
        assertEquals(listOf(created.scene), store.settings.value.scenes)
    }

    @Test
    fun incrementsSortOrder() {
        val store = AtmosphereSettingsStore(preferences)
        store.setScenes(
            listOf(AtmosphereScene("existing", "Combat", "scene.combat", 4)),
        )
        val useCase = CreateAtmosphereSceneUseCase(store, EntityIdFactory { "scene-2" })

        val created = assertIs<CreateAtmosphereSceneUseCase.Result.Created>(
            useCase("Tavern", "scene.tavern"),
        )
        assertEquals(5, created.scene.sortOrder)
    }

    @Test
    fun rejectsBlankName() {
        val useCase = CreateAtmosphereSceneUseCase(
            AtmosphereSettingsStore(preferences),
            EntityIdFactory { "scene-1" },
        )
        assertIs<CreateAtmosphereSceneUseCase.Result.InvalidName>(useCase("  ", "scene.tavern"))
    }

    @Test
    fun rejectsInvalidEntityId() {
        val useCase = CreateAtmosphereSceneUseCase(
            AtmosphereSettingsStore(preferences),
            EntityIdFactory { "scene-1" },
        )
        assertIs<CreateAtmosphereSceneUseCase.Result.InvalidEntityId>(useCase("Tavern", "light.kitchen"))
        assertIs<CreateAtmosphereSceneUseCase.Result.InvalidEntityId>(useCase("Tavern", "scene.Tavern"))
    }

    @Test
    fun rejectsMissingTargets() {
        val useCase = CreateAtmosphereSceneUseCase(
            AtmosphereSettingsStore(preferences),
            EntityIdFactory { "scene-1" },
        )
        assertIs<CreateAtmosphereSceneUseCase.Result.NoTargets>(useCase("Tavern"))
    }

    @Test
    fun createsHueOnlyScene() {
        val store = AtmosphereSettingsStore(preferences)
        val useCase = CreateAtmosphereSceneUseCase(store, EntityIdFactory { "scene-1" })
        val created = assertIs<CreateAtmosphereSceneUseCase.Result.Created>(
            useCase(name = "Combat", hueSceneId = "abc", hueGroupId = "1", hueLightIds = listOf("light-1")),
        )
        assertEquals("abc", created.scene.hueSceneId)
        assertEquals(listOf("light-1"), created.scene.hueLightIds)
        assertEquals("", created.scene.entityId)
    }

    @Test
    fun createsHueLookWithoutScene() {
        val store = AtmosphereSettingsStore(preferences)
        val useCase = CreateAtmosphereSceneUseCase(store, EntityIdFactory { "scene-1" })
        val created = assertIs<CreateAtmosphereSceneUseCase.Result.Created>(
            useCase(
                name = "Warm",
                hueLightIds = listOf("light-1"),
                goveePowerOn = true,
                goveeBrightness = 55,
                goveeColorHex = "#E39B5A",
            ),
        )
        assertEquals("", created.scene.hueSceneId)
        assertEquals(listOf("light-1"), created.scene.hueLightIds)
        assertEquals("#E39B5A", created.scene.goveeColorHex)
    }

    @Test
    fun rejectsHueLookWithoutColor() {
        val useCase = CreateAtmosphereSceneUseCase(
            AtmosphereSettingsStore(preferences),
            EntityIdFactory { "scene-1" },
        )
        assertIs<CreateAtmosphereSceneUseCase.Result.InvalidGoveeColor>(
            useCase(name = "Warm", hueLightIds = listOf("light-1"), goveePowerOn = true, goveeColorHex = ""),
        )
    }

    @Test
    fun rejectsDuplicateNameAndEntity() {
        val store = AtmosphereSettingsStore(preferences)
        val useCase = CreateAtmosphereSceneUseCase(store, EntityIdFactory { "scene-2" })
        store.setScenes(listOf(AtmosphereScene("s1", "Tavern", "scene.tavern", 0)))

        assertIs<CreateAtmosphereSceneUseCase.Result.DuplicateName>(useCase("tavern", "scene.other"))
        assertIs<CreateAtmosphereSceneUseCase.Result.DuplicateEntityId>(useCase("Other", "scene.tavern"))
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.atmosphere.create"
    }
}
