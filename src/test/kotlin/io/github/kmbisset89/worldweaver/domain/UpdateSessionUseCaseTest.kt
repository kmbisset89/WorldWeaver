package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class UpdateSessionUseCaseTest {
    @Test
    fun updateKeepsSceneOrderAndDoesNotTouchPeople() = runTest {
        val harness = Harness()
        val session = harness.insertSession()
        val originalSheet = FifthEditionSheet.empty().copy(hitPoints = 12)
        harness.campaignPeople.insert(
            CampaignPerson(
                id = "pc-1",
                campaignId = "campaign-1",
                worldPersonId = null,
                kind = PersonKind.PlayerCharacter,
                name = "Bram",
                description = "",
                sheet = originalSheet,
                overlayHitPoints = null,
                overlayNotes = "",
                createdAt = Instant.parse("2026-08-29T12:00:00Z"),
                updatedAt = Instant.parse("2026-08-29T12:00:00Z"),
            )
        )

        val result = harness.updateSession(
            session.id,
            SessionDraft(
                name = session.name,
                notes = "Updated notes",
                scenes = listOf(
                    SessionScene(id = "scene-2", title = "Ambush", notes = ""),
                    SessionScene(id = "scene-1", title = "Arrival", notes = ""),
                ),
                marchOrder = listOf(
                    MarchOrderEntry(
                        id = "march-1",
                        person = PersonRef.Campaign("pc-1"),
                        displayName = "Bram snapshot",
                    )
                ),
            ),
        )

        assertIs<UpdateSessionUseCase.Result.Updated>(result)
        val updated = harness.sessions.getById(session.id)
        assertEquals(listOf("Ambush", "Arrival"), updated?.scenes?.map { it.title })
        assertEquals("Bram snapshot", updated?.marchOrder?.single()?.displayName)
        assertEquals(12, harness.campaignPeople.getById("pc-1")?.sheet?.hitPoints)
    }

    @Test
    fun deleteSessionStripsQuestLinks() = runTest {
        val harness = Harness()
        val session = harness.insertSession()
        harness.quests.insert(
            Quest(
                id = "quest-1",
                campaignId = "campaign-1",
                title = "Rescue",
                summary = "",
                status = QuestStatus.Active,
                locationId = null,
                objectives = emptyList(),
                links = listOf(
                    QuestLink(id = "link-1", kind = QuestLinkKind.SESSION, targetId = session.id)
                ),
                createdAt = Instant.parse("2026-08-29T12:00:00Z"),
                updatedAt = Instant.parse("2026-08-29T12:00:00Z"),
            )
        )

        val result = harness.deleteSession(session.id)

        assertIs<DeleteSessionUseCase.Result.Deleted>(result)
        assertEquals(emptyList(), harness.quests.getById("quest-1")?.links)
        assertEquals(emptyList(), harness.sessions.all())
    }

    private class Harness {
        val sessions = FakeSessionRepository()
        val quests = FakeQuestRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T13:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "session-update-${++nextId}" }
        val campaigns = FakeCampaignRepository()
        val calendars = FakeWorldCalendarRepository()
        val updateSession = UpdateSessionUseCase(sessions, campaigns, calendars, ids, instant)
        val deleteSession = DeleteSessionUseCase(sessions, quests, FakeActiveContextRepository())

        suspend fun insertSession(): Session {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val session = Session(
                id = "session-1",
                campaignId = "campaign-1",
                name = "Session 1",
                notes = "Notes",
                scenes = listOf(
                    SessionScene(id = "scene-1", title = "Arrival", notes = ""),
                    SessionScene(id = "scene-2", title = "Ambush", notes = ""),
                ),
                marchOrder = emptyList(),
                createdAt = now,
                updatedAt = now,
            )
            sessions.insert(session)
            return session
        }
    }
}
