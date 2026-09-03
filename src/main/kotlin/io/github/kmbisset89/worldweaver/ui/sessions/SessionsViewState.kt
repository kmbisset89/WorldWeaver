package io.github.kmbisset89.worldweaver.ui.sessions

import io.github.kmbisset89.worldweaver.domain.AbilityScoreMethod
import io.github.kmbisset89.worldweaver.domain.PersonRef
import io.github.kmbisset89.worldweaver.domain.PlotThread
import io.github.kmbisset89.worldweaver.domain.PlotThreadPriority
import io.github.kmbisset89.worldweaver.domain.PlotThreadStatus
import io.github.kmbisset89.worldweaver.domain.RandomNpcDraft
import io.github.kmbisset89.worldweaver.domain.ReferenceDoc
import io.github.kmbisset89.worldweaver.domain.Session
import io.github.kmbisset89.worldweaver.domain.SessionNpcDraftDestination
import io.github.kmbisset89.worldweaver.domain.WorldCalendarMonth

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
