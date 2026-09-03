package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class UpdateEncounterParticipantCombatUseCaseTest {
    @Test
    fun damageHitsTempHpThenClampsAtZeroAndMarksDowned() = runTest {
        val harness = Harness()
        harness.insertEncounter(hitPoints = 4, temporaryHitPoints = 2)

        harness.updateCombat(EncounterParticipantCombatAction.Damage(8))

        val participant = harness.participant()
        assertEquals(0, participant.hitPoints)
        assertEquals(0, participant.temporaryHitPoints)
        assertEquals(CombatState.Downed, participant.combatState)
    }

    @Test
    fun healRestoresHpAndConsciousWhenNotDead() = runTest {
        val harness = Harness()
        harness.insertEncounter(hitPoints = 0, combatState = CombatState.Downed)

        harness.updateCombat(EncounterParticipantCombatAction.Heal(5))

        val participant = harness.participant()
        assertEquals(5, participant.hitPoints)
        assertEquals(CombatState.Conscious, participant.combatState)
    }

    @Test
    fun healDoesNotReviveDead() = runTest {
        val harness = Harness()
        harness.insertEncounter(hitPoints = 0, combatState = CombatState.Dead)

        harness.updateCombat(EncounterParticipantCombatAction.Heal(3))

        val participant = harness.participant()
        assertEquals(3, participant.hitPoints)
        assertEquals(CombatState.Dead, participant.combatState)
    }

    @Test
    fun rollInitiativeUsesProvidedDie() = runTest {
        val harness = Harness()
        harness.insertEncounter()

        harness.updateCombat(EncounterParticipantCombatAction.RollInitiative)

        assertEquals(17, harness.participant().initiativeRoll)
    }

    @Test
    fun initiativeOrderBreaksTiesByNameThenId() = runTest {
        val harness = Harness()
        harness.insertEncounter()
        val encounter = harness.encounters.getById("enc-1")!!
        val tied = encounter.copy(
            participants = listOf(
                harness.participant().copy(id = "z", name = "Zed", initiativeRoll = 12),
                harness.participant().copy(id = "a", name = "Ann", initiativeRoll = 12),
                harness.participant().copy(id = "b", name = "Ann", initiativeRoll = 12),
            )
        )
        harness.encounters.update(tied)

        val order = harness.encounters.getById("enc-1")!!.initiativeOrder()
        assertEquals(listOf("a", "b", "z"), order.map { it.id })
    }

    @Test
    fun setInitiativeClearsRollWhenNull() = runTest {
        val harness = Harness()
        harness.insertEncounter()
        harness.updateCombat(EncounterParticipantCombatAction.SetInitiative(roll = 8, bonus = 2))
        assertEquals(10, harness.participant().initiativeTotal())

        harness.updateCombat(EncounterParticipantCombatAction.SetInitiative(roll = null, bonus = 2))

        assertNull(harness.participant().initiativeRoll)
        assertNull(harness.participant().initiativeTotal())
    }

    @Test
    fun hideFromPlayersPersistsAndLeavesOthersUnchanged() = runTest {
        val harness = Harness()
        harness.insertEncounter()
        val encounter = harness.encounters.getById("enc-1")!!
        harness.encounters.update(
            encounter.copy(
                participants = encounter.participants + encounter.participants.single().copy(
                    id = "p-2",
                    name = "Orc",
                    visibleToPlayers = true,
                ),
            ),
        )

        harness.updateCombat(EncounterParticipantCombatAction.SetVisibleToPlayers(false))

        val hidden = harness.participant()
        val other = harness.encounters.all().single().participants.single { it.id == "p-2" }
        assertEquals(false, hidden.visibleToPlayers)
        assertEquals(true, other.visibleToPlayers)
        assertEquals(CombatState.Conscious, hidden.combatState)
        assertEquals(10, hidden.hitPoints)
    }

    @Test
    fun invisibleHidesNamelessEnemyAndRevealsWhenCleared() = runTest {
        val harness = Harness()
        harness.insertEncounter()

        harness.updateCombat(
            EncounterParticipantCombatAction.SetConditions(
                listOf(FifthEditionCondition.Invisible.displayName),
            ),
        )

        assertEquals(false, harness.participant().visibleToPlayers)

        harness.updateCombat(EncounterParticipantCombatAction.SetConditions(emptyList()))

        assertEquals(true, harness.participant().visibleToPlayers)
        assertEquals(emptyList(), harness.participant().conditions)
    }

    @Test
    fun invisibleLeavesPlayerCharacterVisibleToPlayers() = runTest {
        val harness = Harness()
        val player = harness.insertCampaignPerson(kind = PersonKind.PlayerCharacter)
        harness.insertEncounter(
            source = EncounterParticipantSource.CampaignPerson,
            sourceId = player.id,
        )

        harness.updateCombat(
            EncounterParticipantCombatAction.SetConditions(
                listOf(FifthEditionCondition.Invisible.displayName),
            ),
        )

        val participant = harness.participant()
        assertEquals(listOf(FifthEditionCondition.Invisible.displayName), participant.conditions)
        assertEquals(true, participant.visibleToPlayers)
    }

    @Test
    fun setAttacksUsedClampsToAllowed() = runTest {
        val harness = Harness()
        harness.insertEncounter()
        harness.updateCombat(EncounterParticipantCombatAction.SetAttacksAllowed(2))

        harness.updateCombat(EncounterParticipantCombatAction.SetAttacksUsed(5))

        assertEquals(2, harness.participant().attacksAllowed)
        assertEquals(2, harness.participant().attacksUsed)
    }

    @Test
    fun shrinkingAttacksAllowedCoercesUsed() = runTest {
        val harness = Harness()
        harness.insertEncounter()
        harness.updateCombat(EncounterParticipantCombatAction.SetAttacksAllowed(4))
        harness.updateCombat(EncounterParticipantCombatAction.SetAttacksUsed(3))

        harness.updateCombat(EncounterParticipantCombatAction.SetAttacksAllowed(1))

        val participant = harness.participant()
        assertEquals(1, participant.attacksAllowed)
        assertEquals(1, participant.attacksUsed)
    }

    @Test
    fun setBonusActionAndReactionPersist() = runTest {
        val harness = Harness()
        harness.insertEncounter()

        harness.updateCombat(EncounterParticipantCombatAction.SetBonusActionUsed(true))
        harness.updateCombat(EncounterParticipantCombatAction.SetReactionUsed(true))

        val participant = harness.participant()
        assertEquals(true, participant.bonusActionUsed)
        assertEquals(true, participant.reactionUsed)
    }

    @Test
    fun invisibleHidesCampaignNpc() = runTest {
        val harness = Harness()
        val npc = harness.insertCampaignPerson(kind = PersonKind.Npc)
        harness.insertEncounter(
            source = EncounterParticipantSource.CampaignPerson,
            sourceId = npc.id,
        )

        harness.updateCombat(
            EncounterParticipantCombatAction.SetConditions(
                listOf(FifthEditionCondition.Invisible.displayName),
            ),
        )

        assertEquals(false, harness.participant().visibleToPlayers)
    }

    private class Harness {
        val encounters = FakeEncounterRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        val updateCombat = UpdateEncounterParticipantCombatUseCase(
            encounters,
            campaignPeople,
            instant,
            DiceRoller { _ -> 17 },
        )

        suspend fun updateCombat(
            action: EncounterParticipantCombatAction,
        ): UpdateEncounterParticipantCombatUseCase.Result {
            return updateCombat("enc-1", "p-1", action)
        }

        fun participant(): EncounterParticipant {
            return encounters.all().single().participants.single { it.id == "p-1" }
        }

        suspend fun insertCampaignPerson(kind: PersonKind): CampaignPerson {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val person = CampaignPerson(
                id = "campaign-person-${kind.name.lowercase()}",
                campaignId = "campaign-1",
                worldPersonId = null,
                kind = kind,
                name = kind.displayName,
                description = "",
                sheet = FifthEditionSheet.empty(),
                overlayHitPoints = null,
                overlayNotes = "",
                createdAt = now,
                updatedAt = now,
            )
            campaignPeople.insert(person)
            return person
        }

        suspend fun insertEncounter(
            hitPoints: Int = 10,
            temporaryHitPoints: Int = 0,
            combatState: CombatState = CombatState.Conscious,
            source: EncounterParticipantSource = EncounterParticipantSource.Nameless,
            sourceId: String? = null,
        ) {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            encounters.insert(
                Encounter(
                    id = "enc-1",
                    campaignId = "campaign-1",
                    name = "Fight",
                    locationId = null,
                    difficulty = EncounterDifficulty.Medium,
                    notes = "",
                    outcomeNote = "",
                    status = EncounterStatus.Active,
                    currentRound = 1,
                    currentTurnIndex = 0,
                    participants = listOf(
                        EncounterParticipant(
                            id = "p-1",
                            name = "Goblin",
                            source = source,
                            sourceId = sourceId,
                            initiativeRoll = null,
                            initiativeBonus = 1,
                            armorClass = 13,
                            hitPoints = hitPoints,
                            maxHitPoints = 10,
                            temporaryHitPoints = temporaryHitPoints,
                            conditions = emptyList(),
                            groupCount = 4,
                            combatState = combatState,
                        )
                    ),
                    createdAt = now,
                    updatedAt = now,
                )
            )
        }
    }
}
