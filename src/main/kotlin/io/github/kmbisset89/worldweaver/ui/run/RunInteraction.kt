package io.github.kmbisset89.worldweaver.ui.run

import io.github.kmbisset89.worldweaver.domain.SearchHit
import io.github.kmbisset89.worldweaver.domain.SessionRecordingKind
import io.github.kmbisset89.worldweaver.ui.characters.PersonMembership

internal sealed interface RunInteraction {
    data object ScreenStarted : RunInteraction
    data object RetrySelected : RunInteraction
    data object CreateWorldSelected : RunInteraction
    data object CreateCampaignSelected : RunInteraction
    data object OpenSessionsSelected : RunInteraction
    data object OpenEncountersSelected : RunInteraction
    data object OpenMapsSelected : RunInteraction
    data object PlayerViewSelected : RunInteraction
    data object DiceTraySelected : RunInteraction
    data object AtmosphereTraySelected : RunInteraction
    data class PersonPeeked(
        val membership: PersonMembership,
        val personId: String,
    ) : RunInteraction
    data class SessionNotesChanged(val value: String) : RunInteraction
    data class ScratchNotesChanged(val value: String) : RunInteraction
    data class LookupQueryChanged(val query: String) : RunInteraction
    data class LookupResultSelected(val hit: SearchHit) : RunInteraction
    data object LookupPeekDismissed : RunInteraction
    data object LookupOpened : RunInteraction
    data class ClockLabelChanged(val value: String) : RunInteraction
    data class ClockSegmentCountSelected(val count: Int) : RunInteraction
    data object ClockCreateSelected : RunInteraction
    data class ClockFilledSelected(val clockId: String, val filledCount: Int) : RunInteraction
    data class ClockDeleteSelected(val clockId: String) : RunInteraction
    data class TimerMinutesChanged(val value: String) : RunInteraction
    data class TimerPresetSelected(val minutes: Int) : RunInteraction
    data object TimerStartSelected : RunInteraction
    data object TimerPauseSelected : RunInteraction
    data object TimerResetSelected : RunInteraction
    data class WhyItMattersChanged(val value: String) : RunInteraction
    data object CloseSessionSelected : RunInteraction
    data object AdvancementDismissed : RunInteraction
    data object AwardLevelConfirmed : RunInteraction
    data class AwardExperienceAmountChanged(val value: String) : RunInteraction
    data object AwardExperienceConfirmed : RunInteraction
    data class RecordingModeSelected(val kind: SessionRecordingKind) : RunInteraction
    data class MicrophoneSelected(val deviceId: String) : RunInteraction
    data object RecordToggled : RunInteraction
    data object CameraPermissionRequested : RunInteraction
    data object ScreenStopped : RunInteraction
    data class RecordingOpened(val path: String) : RunInteraction
    data class RecordingDeleteSelected(val recordingId: String) : RunInteraction
}
