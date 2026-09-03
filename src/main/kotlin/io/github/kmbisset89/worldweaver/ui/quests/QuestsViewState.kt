package io.github.kmbisset89.worldweaver.ui.quests

import io.github.kmbisset89.worldweaver.domain.Location
import io.github.kmbisset89.worldweaver.domain.Lore
import io.github.kmbisset89.worldweaver.domain.Quest
import io.github.kmbisset89.worldweaver.domain.QuestLinkKind
import io.github.kmbisset89.worldweaver.domain.QuestObjectiveStatus
import io.github.kmbisset89.worldweaver.domain.QuestStatus
import io.github.kmbisset89.worldweaver.domain.Session

internal sealed class QuestsViewState {
    data object Loading : QuestsViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : QuestsViewState()

    data object NoActiveWorld : QuestsViewState()

    data object NoActiveCampaign : QuestsViewState()

    data class Empty(
        val worldName: String,
        val campaignName: String,
        val editor: QuestEditorState?,
    ) : QuestsViewState()

    data class Content(
        val worldName: String,
        val campaignName: String,
        val quests: List<Quest>,
        val selectedQuest: Quest?,
        val statusFilter: QuestStatus?,
        val locationName: String?,
        val links: List<QuestLinkRow>,
        val editor: QuestEditorState?,
        val pendingDelete: PendingDelete?,
    ) : QuestsViewState()

    data class QuestLinkRow(
        val kind: QuestLinkKind,
        val targetId: String,
        val label: String,
        val missing: Boolean,
    )

    data class QuestEditorState(
        val questId: String?,
        val title: String,
        val summary: String,
        val status: QuestStatus,
        val locationId: String?,
        val locationOptions: List<Location>,
        val objectives: List<ObjectiveEditorState>,
        val loreIds: List<String>,
        val loreOptions: List<Lore>,
        val worldPersonIds: List<String>,
        val campaignPersonIds: List<String>,
        val personOptions: List<PersonOption>,
        val sessionIds: List<String>,
        val sessionOptions: List<Session>,
        val titleError: String?,
    )

    data class ObjectiveEditorState(
        val id: String,
        val title: String,
        val status: QuestObjectiveStatus,
    )

    data class PersonOption(
        val id: String,
        val name: String,
        val worldOwned: Boolean,
    )

    data class PendingDelete(
        val questId: String,
        val questTitle: String,
    )
}
