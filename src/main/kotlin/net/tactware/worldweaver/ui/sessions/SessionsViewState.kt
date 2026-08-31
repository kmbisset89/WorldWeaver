package net.tactware.worldweaver.ui.sessions

import net.tactware.worldweaver.domain.AbilityScoreMethod
import net.tactware.worldweaver.domain.PersonRef
import net.tactware.worldweaver.domain.PlotThread
import net.tactware.worldweaver.domain.PlotThreadPriority
import net.tactware.worldweaver.domain.PlotThreadStatus
import net.tactware.worldweaver.domain.RandomNpcDraft
import net.tactware.worldweaver.domain.ReferenceDoc
import net.tactware.worldweaver.domain.Session
import net.tactware.worldweaver.domain.SessionNpcDraftDestination
import net.tactware.worldweaver.domain.WorldCalendarMonth

internal sealed class SessionsViewState {
    data object Loading : SessionsViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : SessionsViewState()

    data object NoActiveWorld : SessionsViewState()

    data object NoActiveCampaign : SessionsViewState()

    data class Empty(
        val worldName: String,
        val campaignName: String,
        val editor: SessionEditorState?,
    ) : SessionsViewState()

    data class Content(
        val worldName: String,
        val campaignName: String,
        val sessions: List<Session>,
        val sessionDateLabels: Map<String, String>,
        val selectedSession: Session?,
        val selectedDateLabel: String?,
        val checklist: ChecklistState,
        val linkedQuests: List<LinkedQuest>,
        val threads: List<PlotThread>,
        val docs: List<ReferenceDoc>,
        val personOptions: List<PersonOption>,
        val editor: SessionEditorState?,
        val threadEditor: ThreadEditorState?,
        val docEditor: DocEditorState?,
        val generator: GeneratorState?,
        val pendingDelete: PendingDelete?,
        val pendingThreadDelete: PendingThreadDelete?,
        val pendingDocDelete: PendingDocDelete?,
    ) : SessionsViewState()

    data class ChecklistState(
        val activeQuestTitles: List<String>,
        val lastSessionRecap: String?,
        val partyLocationNames: List<String>,
    )

    data class LinkedQuest(
        val questId: String,
        val title: String,
    )

    data class PersonOption(
        val person: PersonRef,
        val name: String,
    )

    data class SessionEditorState(
        val sessionId: String?,
        val name: String,
        val notes: String,
        val yearText: String,
        val monthId: String?,
        val dayText: String,
        val months: List<WorldCalendarMonth>,
        val datePreview: String?,
        val dateError: String?,
        val nameError: String?,
    )

    data class ThreadEditorState(
        val threadId: String?,
        val title: String,
        val details: String,
        val status: PlotThreadStatus,
        val priority: PlotThreadPriority,
        val attachToSession: Boolean,
        val titleError: String?,
    )

    data class DocEditorState(
        val docId: String?,
        val title: String,
        val pathOrUrl: String,
        val attachToSession: Boolean,
        val titleError: String?,
        val pathError: String?,
    )

    data class GeneratorState(
        val method: AbilityScoreMethod,
        val draft: RandomNpcDraft?,
        val destination: SessionNpcDraftDestination,
    )

    data class PendingDelete(
        val sessionId: String,
        val sessionName: String,
    )

    data class PendingThreadDelete(
        val threadId: String,
        val title: String,
    )

    data class PendingDocDelete(
        val docId: String,
        val title: String,
    )
}
