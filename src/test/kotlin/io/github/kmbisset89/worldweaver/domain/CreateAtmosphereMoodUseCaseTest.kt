package io.github.kmbisset89.worldweaver.domain

import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class CreateAtmosphereMoodUseCaseTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)

    @AfterTest
    fun tearDown() {
        preferences.removeNode()
    }

    @Test
    fun createsCustomMood() {
        val store = AtmosphereSettingsStore(preferences)
        val useCase = CreateAtmosphereMoodUseCase(store, EntityIdFactory { "mood-1" })

        val result = useCase(
            name = "Temple",
            powerOn = true,
            brightness = 40,
            colorHex = "#C4A35A",
        )

        val created = assertIs<CreateAtmosphereMoodUseCase.Result.Created>(result)
        assertEquals("mood-1", created.mood.id)
        assertEquals("Temple", created.mood.name)
        assertEquals(true, created.mood.powerOn)
        assertEquals(40, created.mood.brightness)
        assertEquals("#C4A35A", created.mood.colorHex)
        assertEquals(0, created.mood.sortOrder)
        assertEquals(listOf(created.mood), store.settings.value.moods)
    }

    @Test
    fun incrementsSortOrder() {
        val store = AtmosphereSettingsStore(preferences)
        store.setMoods(
            listOf(
                AtmosphereMood(
                    id = "existing",
                    name = "Crypt",
                    powerOn = true,
                    brightness = 20,
                    colorHex = "#334455",
                    sortOrder = 3,
                ),
            ),
        )
        val useCase = CreateAtmosphereMoodUseCase(store, EntityIdFactory { "mood-2" })

        val created = assertIs<CreateAtmosphereMoodUseCase.Result.Created>(
            useCase("Temple", true, 40, "#C4A35A"),
        )
        assertEquals(4, created.mood.sortOrder)
    }

    @Test
    fun rejectsBlankName() {
        val useCase = CreateAtmosphereMoodUseCase(
            AtmosphereSettingsStore(preferences),
            EntityIdFactory { "mood-1" },
        )
        assertIs<CreateAtmosphereMoodUseCase.Result.InvalidName>(
            useCase("  ", true, 40, "#C4A35A"),
        )
    }

    @Test
    fun rejectsInvalidColor() {
        val useCase = CreateAtmosphereMoodUseCase(
            AtmosphereSettingsStore(preferences),
            EntityIdFactory { "mood-1" },
        )
        assertIs<CreateAtmosphereMoodUseCase.Result.InvalidColor>(
            useCase("Temple", true, 40, "not-a-color"),
        )
    }

    @Test
    fun rejectsDuplicateCustomName() {
        val store = AtmosphereSettingsStore(preferences)
        store.setMoods(
            listOf(
                AtmosphereMood("m1", "Temple", true, 40, "#C4A35A", 0),
            ),
        )
        val useCase = CreateAtmosphereMoodUseCase(store, EntityIdFactory { "mood-2" })
        assertIs<CreateAtmosphereMoodUseCase.Result.DuplicateName>(
            useCase("temple", true, 50, "#112233"),
        )
    }

    @Test
    fun rejectsBuiltInName() {
        val useCase = CreateAtmosphereMoodUseCase(
            AtmosphereSettingsStore(preferences),
            EntityIdFactory { "mood-1" },
        )
        assertIs<CreateAtmosphereMoodUseCase.Result.DuplicateName>(
            useCase("Warm", true, 55, "#E39B5A"),
        )
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.atmosphere.mood.create"
    }
}
