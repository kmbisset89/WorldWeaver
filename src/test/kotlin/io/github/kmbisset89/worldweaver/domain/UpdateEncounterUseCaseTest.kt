package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class UpdateEncounterUseCaseTest {
    @Test
    fun updateRejectsBattleMapFromAnotherCampaign() = runTest {
        val harness = Harness()
        harness.insertCampaign()
        harness.encounters.insert(harness.sampleEncounter())
        val other = harness.insertBattleMap(campaignId = "campaign-2")

        val result = harness.updateEncounter(
            "enc-1",
            harness.draft(battleMapId = other.id),
        )

        assertIs<UpdateEncounterUseCase.Result.InvalidBattleMap>(result)
        assertNull(harness.encounters.all().single().battleMapId)
    }

    @Test
    fun updateAttachesValidBattleMap() = runTest {
        val harness = Harness()
        harness.insertCampaign()
        harness.encounters.insert(harness.sampleEncounter())
        val battleMap = harness.insertBattleMap()

        val result = harness.updateEncounter(
            "enc-1",
            harness.draft(battleMapId = battleMap.id),
        )

        assertIs<UpdateEncounterUseCase.Result.Updated>(result)
        assertEquals(battleMap.id, harness.encounters.all().single().battleMapId)
    }

    private class Harness {
        val encounters = FakeEncounterRepository()
        val campaigns = FakeCampaignRepository()
        val locations = FakeLocationRepository()
        val battleMaps = FakeBattleMapRepository()
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private val ids = EntityIdFactory { "unused" }
        val updateEncounter = UpdateEncounterUseCase(
            encounters,
            campaigns,
            locations,
            battleMaps,
            worldPeople,
            campaignPeople,
            ids,
            instant,
        )

        suspend fun insertCampaign() {
            campaigns.insert(
                Campaign(
                    id = "campaign-1",
                    worldId = "world-1",
                    name = "Lost Mine",
                    description = "",
                    notes = "",
                    gameSystem = GameSystem.FifthEdition,
                    status = CampaignStatus.Active,
                    createdAt = Instant.parse("2026-08-29T12:00:00Z"),
                    updatedAt = Instant.parse("2026-08-29T12:00:00Z"),
                )
            )
        }

        suspend fun insertBattleMap(campaignId: String = "campaign-1"): BattleMap {
            val battleMap = BattleMap(
                id = "map-$campaignId",
                campaignId = campaignId,
                name = "Cave",
                originalWidth = 512,
                originalHeight = 512,
                tileSizePx = 256,
                minZoom = 0,
                maxZoom = 0,
                createdAt = Instant.parse("2026-08-29T12:00:00Z"),
                updatedAt = Instant.parse("2026-08-29T12:00:00Z"),
            )
            battleMaps.insert(battleMap)
            return battleMap
        }

        fun sampleEncounter(): Encounter {
            return Encounter(
                id = "enc-1",
                campaignId = "campaign-1",
                name = "Ambush",
                locationId = null,
                battleMapId = null,
                difficulty = EncounterDifficulty.Medium,
                notes = "",
                outcomeNote = "",
                status = EncounterStatus.Planned,
                currentRound = 1,
                currentTurnIndex = 0,
                participants = emptyList(),
                createdAt = Instant.parse("2026-08-29T12:00:00Z"),
                updatedAt = Instant.parse("2026-08-29T12:00:00Z"),
            )
        }

        fun draft(battleMapId: String?): EncounterDraft {
            return EncounterDraft(
                name = "Ambush",
                locationId = null,
                battleMapId = battleMapId,
                difficulty = EncounterDifficulty.Medium,
                notes = "",
                outcomeNote = "",
                participants = emptyList(),
            )
        }
    }
}
