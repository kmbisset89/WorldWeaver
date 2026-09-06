package io.github.kmbisset89.worldweaver.ui.run

import io.github.kmbisset89.worldweaver.domain.SearchHit
import io.github.kmbisset89.worldweaver.domain.SessionClock
import io.github.kmbisset89.worldweaver.domain.SessionMicrophoneDevice
import io.github.kmbisset89.worldweaver.domain.SessionRecordingKind
import io.github.kmbisset89.worldweaver.domain.SessionReferencePeek
import io.github.kmbisset89.worldweaver.ui.advancement.AdvancementPrompt
import io.github.kmbisset89.worldweaver.ui.characters.PersonMembership

internal sealed class RunViewState {
    data object Loading : RunViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : RunViewState()

    data object NoActiveWorld : RunViewState()

    data object NoActiveCampaign : RunViewState()

    data class NoActiveSession(
        val worldName: String,
        val campaignName: String,
    ) : RunViewState()

    data class Content(
        val worldName: String,
        val campaignName: String,
        val sessionId: String,
        val sessionName: String,
        val sessionNotes: String,
        val scratchNotes: String,
        val recap: String,
        val inWorldDateLabel: String?,
        val calendarTodayLabel: String?,
        val observanceNames: List<String>,
        val party: List<PartyMember>,
        val questObjectives: List<QuestObjectiveLine>,
        val scenes: List<SceneLine>,
        val clocks: List<ClockLine>,
        val clockLabel: String,
        val clockSegmentCount: Int,
        val clockError: String?,
        val timerMinutesText: String,
        val timerRemainingSeconds: Int,
        val timerRunning: Boolean,
        val timerFinished: Boolean,
        val lookupQuery: String,
        val lookupResults: List<SearchHit>,
        val lookupPeek: SessionReferencePeek?,
        val activeEncounter: EncounterLine?,
        val partyLocations: List<String>,
        val whyItMatters: String,
        val isClosing: Boolean,
        val closeError: String?,
        val advancementPrompt: AdvancementPrompt?,
        val hasMicrophone: Boolean,
        val hasCamera: Boolean,
        val microphones: List<SessionMicrophoneDevice>,
        val selectedMicrophoneId: String?,
        val recordingMode: SessionRecordingKind,
        val isRecording: Boolean,
        val recordingElapsedLabel: String,
        val recordings: List<RecordingLine>,
        val recordingError: String?,
        val cameraNeedsPermission: Boolean,
    ) : RunViewState() {
        val timerLabel: String
            get() {
                if (timerFinished) {
                    return "Time"
                }
                val minutes = timerRemainingSeconds / 60
                val seconds = timerRemainingSeconds % 60
                return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
            }
    }

    data class PartyMember(
        val personId: String,
        val membership: PersonMembership,
        val name: String,
        val hitPoints: Int,
        val maxHitPoints: Int,
        val armorClass: Int,
        val concentratingSpell: String,
        val spellSlotsLabel: String,
    )

    data class QuestObjectiveLine(
        val questTitle: String,
        val objectiveTitle: String,
        val status: String,
    )

    data class SceneLine(
        val title: String,
        val notes: String,
    )

    data class ClockLine(
        val id: String,
        val label: String,
        val segmentCount: Int,
        val filledCount: Int,
    ) {
        constructor(clock: SessionClock) : this(
            id = clock.id,
            label = clock.label,
            segmentCount = clock.segmentCount,
            filledCount = clock.filledCount,
        )
    }

    data class EncounterLine(
        val name: String,
        val status: String,
        val hasMap: Boolean,
        val roundLabel: String?,
    )

    data class RecordingLine(
        val id: String,
        val kindLabel: String,
        val startedLabel: String,
        val sizeLabel: String,
        val path: String,
    )
}
