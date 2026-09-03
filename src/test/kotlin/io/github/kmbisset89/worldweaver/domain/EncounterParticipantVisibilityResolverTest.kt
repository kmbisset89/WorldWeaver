package io.github.kmbisset89.worldweaver.domain

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class EncounterParticipantVisibilityResolverTest {
    private val resolver = EncounterParticipantVisibilityResolver()

    @Test
    fun invisiblePlayerStaysOnPlayerView() {
        val player = campaignPerson(id = "pc-1", kind = PersonKind.PlayerCharacter)
        val participant = participant(
            source = EncounterParticipantSource.CampaignPerson,
            sourceId = player.id,
            conditions = listOf(FifthEditionCondition.Invisible.displayName),
            visibleToPlayers = true,
        )

        assertTrue(resolver.isVisibleToPlayers(participant, people(player)))
    }

    @Test
    fun invisibleEnemyIsRemovedFromPlayerView() {
        val npc = campaignPerson(id = "npc-1", kind = PersonKind.Npc)
        val participant = participant(
            source = EncounterParticipantSource.CampaignPerson,
            sourceId = npc.id,
            conditions = listOf(FifthEditionCondition.Invisible.displayName),
            visibleToPlayers = true,
        )

        assertFalse(resolver.isVisibleToPlayers(participant, people(npc)))
    }

    @Test
    fun invisibleNamelessEnemyIsRemovedFromPlayerView() {
        val participant = participant(
            source = EncounterParticipantSource.Nameless,
            sourceId = null,
            conditions = listOf(FifthEditionCondition.Invisible.displayName),
            visibleToPlayers = true,
        )

        assertFalse(resolver.isVisibleToPlayers(participant, PeopleSnapshot(emptyList(), emptyList())))
    }

    @Test
    fun manualHideStillHidesAPlayer() {
        val player = campaignPerson(id = "pc-1", kind = PersonKind.PlayerCharacter)
        val participant = participant(
            source = EncounterParticipantSource.CampaignPerson,
            sourceId = player.id,
            conditions = emptyList(),
            visibleToPlayers = false,
        )

        assertFalse(resolver.isVisibleToPlayers(participant, people(player)))
    }

    @Test
    fun applyingInvisibleHidesOnlyEnemies() {
        assertFalse(
            resolver.visibleToPlayersAfterConditions(
                currentVisible = true,
                currentConditions = emptyList(),
                nextConditions = listOf(FifthEditionCondition.Invisible.displayName),
                isPlayerCharacter = false,
            ),
        )
        assertTrue(
            resolver.visibleToPlayersAfterConditions(
                currentVisible = true,
                currentConditions = emptyList(),
                nextConditions = listOf(FifthEditionCondition.Invisible.displayName),
                isPlayerCharacter = true,
            ),
        )
    }

    @Test
    fun removingInvisibleRestoresEnemyVisibility() {
        assertTrue(
            resolver.visibleToPlayersAfterConditions(
                currentVisible = false,
                currentConditions = listOf(FifthEditionCondition.Invisible.displayName),
                nextConditions = emptyList(),
                isPlayerCharacter = false,
            ),
        )
    }

    private fun people(person: CampaignPerson): PeopleSnapshot {
        return PeopleSnapshot(worldPeople = emptyList(), campaignPeople = listOf(person))
    }

    private fun campaignPerson(id: String, kind: PersonKind): CampaignPerson {
        val now = Instant.parse("2026-08-30T12:00:00Z")
        return CampaignPerson(
            id = id,
            campaignId = "campaign-1",
            worldPersonId = null,
            kind = kind,
            name = id,
            description = "",
            sheet = FifthEditionSheet.empty(),
            overlayHitPoints = null,
            overlayNotes = "",
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun participant(
        source: EncounterParticipantSource,
        sourceId: String?,
        conditions: List<String>,
        visibleToPlayers: Boolean,
    ): EncounterParticipant {
        return EncounterParticipant(
            id = "p-1",
            name = "Token",
            source = source,
            sourceId = sourceId,
            initiativeRoll = null,
            initiativeBonus = 0,
            armorClass = 13,
            hitPoints = 10,
            maxHitPoints = 10,
            temporaryHitPoints = 0,
            conditions = conditions,
            groupCount = 1,
            combatState = CombatState.Conscious,
            visibleToPlayers = visibleToPlayers,
        )
    }
}
