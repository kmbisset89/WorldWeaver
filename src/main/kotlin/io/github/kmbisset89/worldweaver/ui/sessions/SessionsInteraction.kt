package io.github.kmbisset89.worldweaver.ui.sessions

import io.github.kmbisset89.worldweaver.domain.AbilityScoreMethod
import io.github.kmbisset89.worldweaver.domain.PersonRef
import io.github.kmbisset89.worldweaver.domain.PlotThreadPriority
import io.github.kmbisset89.worldweaver.domain.PlotThreadStatus
import io.github.kmbisset89.worldweaver.domain.SessionNpcDraftDestination

internal sealed interface SessionsInteraction {
    data object ScreenStarted : SessionsInteraction
    data object RetrySelected : SessionsInteraction
    data object CreateWorldSelected : SessionsInteraction
    data object CreateCampaignSelected : SessionsInteraction
    data object NewSessionSelected : SessionsInteraction
    data class SessionSelected(val sessionId: String) : SessionsInteraction
    data class SessionOpened(val sessionId: String) : SessionsInteraction
    data class EditSessionSelected(val sessionId: String) : SessionsInteraction
    data class DeleteSessionSelected(val sessionId: String) : SessionsInteraction
    data object DeleteConfirmed : SessionsInteraction
    data object DeleteCancelled : SessionsInteraction
    data class LinkedQuestSelected(val questId: String) : SessionsInteraction
    data class EditorNameChanged(val name: String) : SessionsInteraction
    data class EditorNotesChanged(val notes: String) : SessionsInteraction
    data class EditorYearChanged(val year: String) : SessionsInteraction
    data class EditorMonthSelected(val monthId: String?) : SessionsInteraction
    data class EditorDayChanged(val day: String) : SessionsInteraction
    data object EditorDateCleared : SessionsInteraction
    data object EditorSaved : SessionsInteraction
    data object EditorDismissed : SessionsInteraction
    data object SceneAdded : SessionsInteraction
    data class SceneRemoved(val index: Int) : SessionsInteraction
    data class SceneTitleChanged(val index: Int, val title: String) : SessionsInteraction
    data class SceneNotesChanged(val index: Int, val notes: String) : SessionsInteraction
    data class SceneMoved(val index: Int, val delta: Int) : SessionsInteraction
    data class MarchPersonAdded(val person: PersonRef) : SessionsInteraction
    data class MarchEntryRemoved(val index: Int) : SessionsInteraction
    data class MarchEntryMoved(val index: Int, val delta: Int) : SessionsInteraction
    data object ThreadEditorOpened : SessionsInteraction
    data class ThreadEditSelected(val threadId: String) : SessionsInteraction
    data class ThreadDeleteSelected(val threadId: String) : SessionsInteraction
    data object ThreadDeleteConfirmed : SessionsInteraction
    data object ThreadDeleteCancelled : SessionsInteraction
    data class ThreadTitleChanged(val title: String) : SessionsInteraction
    data class ThreadDetailsChanged(val details: String) : SessionsInteraction
    data class ThreadStatusSelected(val status: PlotThreadStatus) : SessionsInteraction
    data class ThreadPrioritySelected(val priority: PlotThreadPriority) : SessionsInteraction
    data class ThreadAttachToggled(val attach: Boolean) : SessionsInteraction
    data object ThreadSaved : SessionsInteraction
    data object ThreadEditorDismissed : SessionsInteraction
    data object DocEditorOpened : SessionsInteraction
    data class DocEditSelected(val docId: String) : SessionsInteraction
    data class DocDeleteSelected(val docId: String) : SessionsInteraction
    data object DocDeleteConfirmed : SessionsInteraction
    data object DocDeleteCancelled : SessionsInteraction
    data class DocTitleChanged(val title: String) : SessionsInteraction
    data class DocPathChanged(val pathOrUrl: String) : SessionsInteraction
    data class DocAttachToggled(val attach: Boolean) : SessionsInteraction
    data object DocSaved : SessionsInteraction
    data object DocEditorDismissed : SessionsInteraction
    data object GeneratorOpened : SessionsInteraction
    data object GeneratorDismissed : SessionsInteraction
    data class GeneratorMethodSelected(val method: AbilityScoreMethod) : SessionsInteraction
    data object GeneratorRolled : SessionsInteraction
    data class GeneratorDestinationSelected(val destination: SessionNpcDraftDestination) : SessionsInteraction
    data object GeneratorSaved : SessionsInteraction
}
