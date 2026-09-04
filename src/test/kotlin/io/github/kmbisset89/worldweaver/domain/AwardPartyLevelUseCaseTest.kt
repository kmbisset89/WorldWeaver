package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class AwardPartyLevelUseCaseTest {
    @Test
    fun awardsFifthEditionByIncrementingLastClass() = runTest {
        val harness = Harness()
        harness.insertPc(
            id = "pc-1",
            name = "Aelar",
            sheet = FifthEditionSheet.empty().copy(
                classLevels = listOf(
                    ClassLevel("Fighter", "Champion", 2),
                    ClassLevel("Rogue", "Thief", 1),
                ),
            ),
        )
        harness.insertSession()

        val result = harness.awardPartyLevel("campaign-1", "session-1")

        val awarded = assertIs<AwardPartyLevelUseCase.Result.Awarded>(result)
        assertEquals(1, awarded.partySize)
        assertEquals(4, awarded.partyLevel)
        val sheet = assertIs<FifthEditionSheet>(harness.campaignPeople.getById("pc-1")!!.sheet)
        assertEquals(2, sheet.classLevels[0].level)
        assertEquals(2, sheet.classLevels[1].level)
        assertEquals("Party reached level 4", harness.sessions.getById("session-1")!!.recap)
    }

    @Test
    fun awardsPathfinderByIncrementingLevel() = runTest {
        val harness = Harness()
        harness.insertPc(
            id = "pc-1",
            name = "Rixi",
            sheet = Pathfinder2ESheet.empty().copy(level = 3),
        )

        val result = harness.awardPartyLevel("campaign-1")

        val awarded = assertIs<AwardPartyLevelUseCase.Result.Awarded>(result)
        assertEquals(4, awarded.partyLevel)
        val sheet = assertIs<Pathfinder2ESheet>(harness.campaignPeople.getById("pc-1")!!.sheet)
        assertEquals(4, sheet.level)
    }

    @Test
    fun skipsFifthEditionPcsWithoutClassesAndNpcs() = runTest {
        val harness = Harness()
        harness.insertPc(
            id = "pc-1",
            name = "Blank",
            sheet = FifthEditionSheet.empty(),
        )
        harness.campaignPeople.insert(
            harness.person(
                id = "npc-1",
                name = "Greta",
                kind = PersonKind.Npc,
                sheet = FifthEditionSheet.empty().copy(
                    classLevels = listOf(ClassLevel("Commoner", "", 1)),
                ),
            ),
        )

        val result = harness.awardPartyLevel("campaign-1")

        assertIs<AwardPartyLevelUseCase.Result.NoPlayerCharacters>(result)
        val blank = assertIs<FifthEditionSheet>(harness.campaignPeople.getById("pc-1")!!.sheet)
        assertEquals(emptyList(), blank.classLevels)
    }

    @Test
    fun returnsNoPlayerCharactersWhenCampaignHasNone() = runTest {
        val harness = Harness()

        val result = harness.awardPartyLevel("campaign-1")

        assertIs<AwardPartyLevelUseCase.Result.NoPlayerCharacters>(result)
    }

    private class Harness {
        val campaignPeople = FakeCampaignPersonRepository()
        val sessions = FakeSessionRepository()
        private val instant = InstantProvider { Instant.parse("2026-09-04T12:00:00Z") }
        val awardPartyLevel = AwardPartyLevelUseCase(campaignPeople, sessions, instant)

        suspend fun insertSession() {
            val now = instant.now()
            sessions.insert(
                Session(
                    id = "session-1",
                    campaignId = "campaign-1",
                    name = "Session 1",
                    notes = "",
                    recap = "",
                    scenes = emptyList(),
                    marchOrder = emptyList(),
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }

        suspend fun insertPc(id: String, name: String, sheet: PersonSheet) {
            campaignPeople.insert(person(id = id, name = name, sheet = sheet))
        }

        fun person(
            id: String,
            name: String,
            kind: PersonKind = PersonKind.PlayerCharacter,
            sheet: PersonSheet,
        ): CampaignPerson {
            val now = instant.now()
            return CampaignPerson(
                id = id,
                campaignId = "campaign-1",
                worldPersonId = null,
                kind = kind,
                name = name,
                description = "",
                sheet = sheet,
                overlayHitPoints = null,
                overlayNotes = "",
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}
