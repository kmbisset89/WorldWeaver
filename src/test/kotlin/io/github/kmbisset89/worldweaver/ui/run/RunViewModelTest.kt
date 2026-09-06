package io.github.kmbisset89.worldweaver.ui.run

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.AwardPartyExperienceUseCase
import io.github.kmbisset89.worldweaver.domain.AwardPartyLevelUseCase
import io.github.kmbisset89.worldweaver.domain.Campaign
import io.github.kmbisset89.worldweaver.domain.CampaignStatus
import io.github.kmbisset89.worldweaver.domain.CloseSessionUseCase
import io.github.kmbisset89.worldweaver.domain.CreateSessionClockUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteSessionClockUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteSessionRecordingUseCase
import io.github.kmbisset89.worldweaver.domain.FakeSessionRecordingCapture
import io.github.kmbisset89.worldweaver.domain.SessionCaptureDeviceProbe
import io.github.kmbisset89.worldweaver.domain.SessionCameraPermissionSettingsOpener
import io.github.kmbisset89.worldweaver.domain.SessionMicrophoneDevice
import io.github.kmbisset89.worldweaver.domain.SessionRecording
import io.github.kmbisset89.worldweaver.domain.SessionRecordingFileStore
import io.github.kmbisset89.worldweaver.domain.SessionRecordingKind
import io.github.kmbisset89.worldweaver.domain.EntityIdFactory
import io.github.kmbisset89.worldweaver.domain.FakeActiveContextRepository
import io.github.kmbisset89.worldweaver.domain.FakeCampaignPersonRepository
import io.github.kmbisset89.worldweaver.domain.FakeCampaignRepository
import io.github.kmbisset89.worldweaver.domain.FakeEncounterRepository
import io.github.kmbisset89.worldweaver.domain.FakeLocationOverlayRepository
import io.github.kmbisset89.worldweaver.domain.FakeLocationRepository
import io.github.kmbisset89.worldweaver.domain.FakeLoreRepository
import io.github.kmbisset89.worldweaver.domain.FakeQuestRepository
import io.github.kmbisset89.worldweaver.domain.FakeSessionClockRepository
import io.github.kmbisset89.worldweaver.domain.FakeSessionRepository
import io.github.kmbisset89.worldweaver.domain.FakeWorldCalendarObservanceRepository
import io.github.kmbisset89.worldweaver.domain.FakeWorldCalendarRepository
import io.github.kmbisset89.worldweaver.domain.FakeWorldCelestialBodyRepository
import io.github.kmbisset89.worldweaver.domain.FakeWorldPersonRepository
import io.github.kmbisset89.worldweaver.domain.FakeWorldRepository
import io.github.kmbisset89.worldweaver.domain.FakeFactionRepository
import io.github.kmbisset89.worldweaver.domain.GameSystem
import io.github.kmbisset89.worldweaver.domain.InstantProvider
import io.github.kmbisset89.worldweaver.domain.LoadSessionReferencePeekUseCase
import io.github.kmbisset89.worldweaver.domain.Location
import io.github.kmbisset89.worldweaver.domain.LocationType
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveEncountersForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLocationOverlaysForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLocationsForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObservePeopleForActiveContextUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveQuestsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveSessionClocksForActiveSessionUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveSessionsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveWorldCalendarForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveWorldCalendarObservancesForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.SearchRecordsUseCase
import io.github.kmbisset89.worldweaver.domain.SearchSessionReferencesUseCase
import io.github.kmbisset89.worldweaver.domain.Session
import io.github.kmbisset89.worldweaver.domain.UpdateSessionClockUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateSessionRunnerNotesUseCase
import io.github.kmbisset89.worldweaver.domain.World

internal class RunViewModelTest {
    @Test
    fun emptyContextShowsNoActiveWorld() {
        val viewModel = Harness().viewModel()
        assertIs<RunViewState.NoActiveWorld>(awaitState(viewModel) { it is RunViewState.NoActiveWorld })
    }

    @Test
    fun notesAndScratchSaveToTheSession() {
        val harness = Harness()
        harness.seedSession()
        val viewModel = harness.viewModel()
        val content = awaitContent(viewModel)
        assertEquals("Prep notes", content.sessionNotes)

        viewModel.onInteraction(RunInteraction.SessionNotesChanged("Updated prep"))
        viewModel.onInteraction(RunInteraction.ScratchNotesChanged("Live scratch"))
        awaitContent(viewModel) { it.sessionNotes == "Updated prep" && it.scratchNotes == "Live scratch" }
        runBlocking {
            withTimeout(2_000) {
                while (harness.sessions.getById("session-1")?.notes != "Updated prep") {
                    kotlinx.coroutines.delay(20)
                }
            }
            assertEquals("Live scratch", harness.sessions.getById("session-1")?.scratchNotes)
        }
    }

    @Test
    fun timerPresetAndStart() {
        val harness = Harness()
        harness.seedSession()
        val viewModel = harness.viewModel()
        awaitContent(viewModel)

        viewModel.onInteraction(RunInteraction.TimerPresetSelected(10))
        val preset = awaitContent(viewModel) { it.timerMinutesText == "10" && it.timerRemainingSeconds == 600 }
        assertFalse(preset.timerRunning)

        viewModel.onInteraction(RunInteraction.TimerStartSelected)
        val running = awaitContent(viewModel) { it.timerRunning }
        assertEquals(600, running.timerRemainingSeconds)
        assertEquals("10:00", running.timerLabel)
    }

    @Test
    fun createsAProgressClock() {
        val harness = Harness()
        harness.seedSession()
        val viewModel = harness.viewModel()
        awaitContent(viewModel)

        viewModel.onInteraction(RunInteraction.ClockLabelChanged("The ritual"))
        viewModel.onInteraction(RunInteraction.ClockSegmentCountSelected(8))
        viewModel.onInteraction(RunInteraction.ClockCreateSelected)

        val content = awaitContent(viewModel) { it.clocks.isNotEmpty() && it.clockLabel.isEmpty() }
        assertEquals("The ritual", content.clocks.single().label)
        assertEquals(8, content.clocks.single().segmentCount)
        assertEquals(0, content.clocks.single().filledCount)
        assertEquals("", content.clockLabel)
    }

    @Test
    fun lookupFindsALocationInTheActiveWorld() {
        val harness = Harness()
        harness.seedSession()
        runBlocking {
            harness.locations.insert(
                Location(
                    id = "loc-1",
                    worldId = "world-1",
                    type = LocationType.City,
                    parentLocationId = null,
                    name = "Harbor",
                    description = "Docks",
                    climate = "",
                    terrain = "",
                    government = "",
                    landmarks = emptyList(),
                    history = "",
                    notes = "",
                    createdAt = Instant.parse("2026-08-29T12:00:00Z"),
                    updatedAt = Instant.parse("2026-08-29T12:00:00Z"),
                )
            )
        }
        val viewModel = harness.viewModel()
        awaitContent(viewModel)

        viewModel.onInteraction(RunInteraction.LookupQueryChanged("ha"))
        val content = awaitContent(viewModel) { it.lookupResults.isNotEmpty() }
        assertEquals("Harbor", content.lookupResults.single().title)
    }

    @Test
    fun cameraModeStartsPreview() {
        val harness = Harness()
        harness.seedSession()
        val viewModel = harness.viewModel()
        awaitContent(viewModel)

        viewModel.onInteraction(RunInteraction.RecordingModeSelected(SessionRecordingKind.Video))
        val content = awaitContent(viewModel) { it.recordingMode == SessionRecordingKind.Video }
        assertEquals(SessionRecordingKind.Video, content.recordingMode)
        assertEquals(1, harness.recordingCapture.startPreviewCalls)
    }

    @Test
    fun recordAndStopUpdatesState() {
        val harness = Harness()
        harness.seedSession()
        val viewModel = harness.viewModel()
        awaitContent(viewModel)
        val recording = SessionRecording(
            id = "20260906T150000Z_audio.wav",
            sessionId = "session-1",
            kind = SessionRecordingKind.Audio,
            path = "/tmp/20260906T150000Z_audio.wav",
            startedAt = Instant.parse("2026-09-06T15:00:00Z"),
            byteSize = 40,
        )
        val file = harness.recordings.createFile(
            "session-1",
            SessionRecordingKind.Audio,
            Instant.parse("2026-09-06T15:00:00Z"),
        )
        file.writeBytes(ByteArray(40))
        harness.recordingCapture.enqueueStopResult(
            recording.copy(path = file.absolutePath, id = file.name),
        )

        viewModel.onInteraction(RunInteraction.RecordToggled)
        val recordingState = awaitContent(viewModel) { it.isRecording }
        assertTrue(recordingState.isRecording)
        assertEquals("session-1", harness.recordingCapture.lastStartSessionId)
        assertEquals(SessionRecordingKind.Audio, harness.recordingCapture.lastStartKind)
        assertEquals("built-in", harness.recordingCapture.lastMicrophoneDeviceId)

        viewModel.onInteraction(RunInteraction.RecordToggled)
        val stopped = awaitContent(viewModel) { !it.isRecording && it.recordings.isNotEmpty() }
        assertFalse(stopped.isRecording)
        assertEquals("Mic", stopped.recordings.single().kindLabel)
    }

    @Test
    fun closeSessionStopsCapture() {
        val harness = Harness()
        harness.seedSession()
        val viewModel = harness.viewModel()
        awaitContent(viewModel)
        viewModel.onInteraction(RunInteraction.RecordToggled)
        awaitContent(viewModel) { it.isRecording }

        viewModel.onInteraction(RunInteraction.CloseSessionSelected)
        val content = awaitContent(viewModel) { !it.isRecording }
        assertFalse(content.isRecording)
        assertFalse(harness.recordingCapture.isRecording)
    }

    @Test
    fun microphoneDropdownSelectsDeviceForCapture() {
        val harness = Harness()
        harness.seedSession()
        val viewModel = harness.viewModel()
        awaitContent(viewModel)

        viewModel.onInteraction(RunInteraction.MicrophoneSelected("usb"))
        val selected = awaitContent(viewModel) { it.selectedMicrophoneId == "usb" }
        assertEquals("usb", selected.selectedMicrophoneId)

        viewModel.onInteraction(RunInteraction.RecordToggled)
        awaitContent(viewModel) { it.isRecording }
        assertEquals("usb", harness.recordingCapture.lastMicrophoneDeviceId)
    }

    @Test
    fun allowCameraOpensPrivacySettingsWhenPreviewFails() {
        val harness = Harness()
        harness.seedSession()
        harness.recordingCapture.startPreviewResult = false
        val viewModel = harness.viewModel()
        awaitContent(viewModel)

        viewModel.onInteraction(RunInteraction.RecordingModeSelected(SessionRecordingKind.Video))
        val denied = awaitContent(viewModel) { it.cameraNeedsPermission }
        assertTrue(denied.cameraNeedsPermission)

        viewModel.onInteraction(RunInteraction.CameraPermissionRequested)
        val afterRequest = awaitContent(viewModel) {
            it.recordingError?.contains("privacy settings") == true
        }
        assertTrue(afterRequest.cameraNeedsPermission)
        assertEquals(
            listOf("open", "x-apple.systempreferences:com.apple.preference.security?Privacy_Camera"),
            harness.cameraSettingsCommands,
        )
    }

    private fun awaitContent(
        viewModel: RunViewModel,
        predicate: (RunViewState.Content) -> Boolean = { true },
    ): RunViewState.Content {
        return runBlocking {
            withTimeout(3_000) {
                viewModel.state.filterIsInstance<RunViewState.Content>().filter(predicate).first()
            }
        }
    }

    private fun awaitState(
        viewModel: RunViewModel,
        predicate: (RunViewState) -> Boolean,
    ): RunViewState {
        return runBlocking {
            withTimeout(3_000) {
                viewModel.state.filter(predicate).first()
            }
        }
    }

    private class Harness {
        val context = FakeActiveContextRepository()
        val worlds = FakeWorldRepository()
        val campaigns = FakeCampaignRepository()
        val sessions = FakeSessionRepository()
        val clocks = FakeSessionClockRepository()
        val quests = FakeQuestRepository()
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val encounters = FakeEncounterRepository()
        val calendars = FakeWorldCalendarRepository()
        val observances = FakeWorldCalendarObservanceRepository()
        val overlays = FakeLocationOverlayRepository()
        val locations = FakeLocationRepository()
        val lore = FakeLoreRepository()
        val celestialBodies = FakeWorldCelestialBodyRepository()
        val factions = FakeFactionRepository()
        val recordings = SessionRecordingFileStore(Files.createTempDirectory("ww-recordings").toFile())
        val recordingCapture = FakeSessionRecordingCapture()
        var cameraSettingsCommands: List<String>? = null
        private val now = Instant.parse("2026-08-29T12:00:00Z")
        private val instant = InstantProvider { now }
        private var nextId = 0
        private val ids = EntityIdFactory { "clock-${++nextId}" }

        fun seedSession() {
            runBlocking {
                worlds.insert(
                    World("world-1", "Faerun", "", GameSystem.FifthEdition, now, now),
                )
                campaigns.insert(
                    Campaign(
                        id = "campaign-1",
                        worldId = "world-1",
                        name = "Dragon Heist",
                        description = "",
                        notes = "",
                        gameSystem = null,
                        status = CampaignStatus.Active,
                        createdAt = now,
                        updatedAt = now,
                    )
                )
                sessions.insert(
                    Session(
                        id = "session-1",
                        campaignId = "campaign-1",
                        name = "Tonight",
                        notes = "Prep notes",
                        scratchNotes = "",
                        scenes = emptyList(),
                        marchOrder = emptyList(),
                        createdAt = now,
                        updatedAt = now,
                    )
                )
            }
            context.setActiveWorldId("world-1")
            context.setActiveCampaignId("campaign-1")
            context.setActiveSessionId("session-1")
        }

        fun viewModel(): RunViewModel {
            val searchRecords = SearchRecordsUseCase(
                worlds,
                campaigns,
                locations,
                lore,
                observances,
                celestialBodies,
                factions,
                worldPeople,
                campaignPeople,
                quests,
                sessions,
            )
            return RunViewModel(
                appScope = AppCoroutineScope(),
                observeActiveContext = ObserveActiveContextUseCase(context),
                observeActiveContextDetails = ObserveActiveContextDetailsUseCase(context, worlds, campaigns),
                observeSessions = ObserveSessionsForActiveCampaignUseCase(sessions, context),
                observeQuests = ObserveQuestsForActiveCampaignUseCase(quests, context),
                observePeople = ObservePeopleForActiveContextUseCase(worldPeople, campaignPeople, context),
                observeEncounters = ObserveEncountersForActiveCampaignUseCase(encounters, context),
                observeCalendar = ObserveWorldCalendarForActiveWorldUseCase(calendars, context),
                observeObservances = ObserveWorldCalendarObservancesForActiveWorldUseCase(observances, context),
                observeOverlays = ObserveLocationOverlaysForActiveCampaignUseCase(overlays, context),
                observeLocations = ObserveLocationsForActiveWorldUseCase(locations, context),
                observeClocks = ObserveSessionClocksForActiveSessionUseCase(clocks, context),
                updateRunnerNotes = UpdateSessionRunnerNotesUseCase(sessions, instant),
                createClock = CreateSessionClockUseCase(clocks, sessions, context, ids),
                updateClock = UpdateSessionClockUseCase(clocks),
                deleteClock = DeleteSessionClockUseCase(clocks),
                searchReferences = SearchSessionReferencesUseCase(searchRecords),
                loadPeek = LoadSessionReferencePeekUseCase(
                    locations,
                    overlays,
                    lore,
                    worldPeople,
                    campaignPeople,
                ),
                closeSession = CloseSessionUseCase(
                    sessions,
                    encounters,
                    quests,
                    overlays,
                    locations,
                    calendars,
                    campaigns,
                    instant,
                ),
                awardPartyLevel = AwardPartyLevelUseCase(campaignPeople, sessions, instant),
                awardPartyExperience = AwardPartyExperienceUseCase(campaignPeople, sessions, instant),
                recordingCapture = recordingCapture,
                recordingFileStore = recordings,
                captureDeviceProbe = SessionCaptureDeviceProbe(
                    listMicrophones = {
                        listOf(
                            SessionMicrophoneDevice("built-in", "Built-in Microphone"),
                            SessionMicrophoneDevice("usb", "USB Mic"),
                        )
                    },
                    cameraPresent = { true },
                ),
                cameraPermissionSettingsOpener = SessionCameraPermissionSettingsOpener(
                    osName = { "Mac OS X" },
                    startProcess = { command ->
                        cameraSettingsCommands = command
                        true
                    },
                ),
                deleteRecording = DeleteSessionRecordingUseCase(recordings),
            )
        }
    }
}
