package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class AwardPartyExperienceUseCaseTest {
    @Test
    fun awardsTheSameAmountToEachPlayerCharacter() = runTest {
        val harness = Harness()
        harness.insertPc(
            id = "pc-1",
            name = "Aelar",
            sheet = FifthEditionSheet.empty().copy(currentXp = 100),
        )
        harness.insertPc(
            id = "pc-2",
            name = "Rixi",
            sheet = Pathfinder2ESheet.empty().copy(currentXp = 50),
        )
        harness.campaignPeople.insert(
            harness.person(
                id = "npc-1",
                name = "Greta",
                kind = PersonKind.Npc,
                sheet = FifthEditionSheet.empty().copy(currentXp = 0),
            )
        )
        harness.insertSession()

        val result = harness.awardPartyExperience("campaign-1", 250, "session-1")

        val awarded = assertIs<AwardPartyExperienceUseCase.Result.Awarded>(result)
        assertEquals(2, awarded.partySize)
        assertEquals(250, awarded.amount)
        assertEquals(350, harness.campaignPeople.getById("pc-1")!!.sheet.currentXp)
        assertEquals(300, harness.campaignPeople.getById("pc-2")!!.sheet.currentXp)
        assertEquals(0, harness.campaignPeople.getById("npc-1")!!.sheet.currentXp)
        assertEquals("Awarded 250 XP", harness.sessions.getById("session-1")!!.recap)
    }

    @Test
    fun rejectsNonPositiveAmounts() = runTest {
        val harness = Harness()
        harness.insertPc("pc-1", "Aelar", FifthEditionSheet.empty())

        assertIs<AwardPartyExperienceUseCase.Result.InvalidAmount>(
            harness.awardPartyExperience("campaign-1", 0),
        )
        assertIs<AwardPartyExperienceUseCase.Result.InvalidAmount>(
            harness.awardPartyExperience("campaign-1", -10),
        )
    }

    @Test
    fun returnsNoPlayerCharactersWhenCampaignHasNone() = runTest {
        val harness = Harness()

        val result = harness.awardPartyExperience("campaign-1", 100)

        assertIs<AwardPartyExperienceUseCase.Result.NoPlayerCharacters>(result)
    }

    private class Harness {
        val campaignPeople = FakeCampaignPersonRepository()
        val sessions = FakeSessionRepository()
        private val instant = InstantProvider { Instant.parse("2026-09-04T12:00:00Z") }
        val awardPartyExperience = AwardPartyExperienceUseCase(campaignPeople, sessions, instant)

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
