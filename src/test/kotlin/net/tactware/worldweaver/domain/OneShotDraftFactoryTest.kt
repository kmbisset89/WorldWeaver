package net.tactware.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class OneShotDraftFactoryTest {
    private val factory = OneShotDraftFactory()

    @Test
    fun genreChipFillsBlankPlaceNames() {
        val filled = factory.applyGenre(OneShotAnswers(), "high_fantasy")

        assertEquals("high_fantasy", filled.genreId)
        assertEquals("The Crownlands", filled.realmName)
        assertEquals("Silvervale", filled.regionName)
        assertEquals("Brightwater", filled.settlementName)
        assertTrue(filled.logline.isNotBlank())
    }

    @Test
    fun genreChipDoesNotOverwriteTypedText() {
        val filled = factory.applyGenre(
            OneShotAnswers(
                realmName = "My Realm",
                regionName = "My Region",
                logline = "A custom logline.",
            ),
            "grimdark",
        )

        assertEquals("grimdark", filled.genreId)
        assertEquals("My Realm", filled.realmName)
        assertEquals("My Region", filled.regionName)
        assertEquals("Hollowford", filled.settlementName)
        assertEquals("A custom logline.", filled.logline)
    }

    @Test
    fun createDerivesRealmAndRegionFromWorldNameWhenBlank() {
        val draft = factory.create(
            OneShotAnswers(
                worldName = "Eberron",
                campaignName = "Night in Sharn",
                openingSiteName = "The Cogs",
                questTitle = "Find the shard",
            ),
        )

        assertEquals("The Eberron Lands", draft.realmName)
        assertEquals("Eberron Marches", draft.regionName)
        assertEquals("Crossroads", draft.settlementName)
        assertEquals("The Cogs", draft.sites.single().name)
        assertEquals(OneShotDraft.Site.Role.Opening, draft.sites.single().role)
    }

    @Test
    fun objectiveChipFillsTheFirstEmptySlot() {
        val once = factory.applyObjectiveChip(OneShotAnswers(), "rescue")
        val twice = factory.applyObjectiveChip(once, "relic")
        val again = factory.applyObjectiveChip(twice, "rescue")

        assertEquals("Rescue the captive", once.objective1)
        assertEquals("Recover the relic", twice.objective2)
        assertEquals("Rescue the captive", again.objective1)
        assertEquals("Recover the relic", again.objective2)
        assertEquals("", again.objective3)
    }

    @Test
    fun encounterIsOmittedUntilIncluded() {
        val omitted = factory.create(
            OneShotAnswers(
                worldName = "World",
                campaignName = "Shot",
                openingSiteName = "Inn",
                questTitle = "Quest",
            ),
        )
        val included = factory.create(
            OneShotAnswers(
                worldName = "World",
                campaignName = "Shot",
                openingSiteName = "Inn",
                questTitle = "Quest",
                includeEncounter = true,
            ),
        )

        assertNull(omitted.encounterName)
        assertEquals("The confrontation", included.encounterName)
        assertEquals(EncounterDifficulty.Medium, included.encounterDifficulty)
    }
}
