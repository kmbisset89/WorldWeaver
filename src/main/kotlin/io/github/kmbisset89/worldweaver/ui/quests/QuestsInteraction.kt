package io.github.kmbisset89.worldweaver.ui.quests

import io.github.kmbisset89.worldweaver.domain.QuestObjectiveStatus
import io.github.kmbisset89.worldweaver.domain.QuestStatus

internal sealed interface QuestsInteraction {
    data object ScreenStarted : QuestsInteraction
    data object RetrySelected : QuestsInteraction
    data object CreateWorldSelected : QuestsInteraction
    data object CreateCampaignSelected : QuestsInteraction
    data object NewQuestSelected : QuestsInteraction
    data class QuestSelected(val questId: String) : QuestsInteraction
    data class QuestOpened(val questId: String) : QuestsInteraction
    data class EditQuestSelected(val questId: String) : QuestsInteraction
    data class DeleteQuestSelected(val questId: String) : QuestsInteraction
    data object DeleteConfirmed : QuestsInteraction
    data object DeleteCancelled : QuestsInteraction
    data class StatusFilterSelected(val status: QuestStatus?) : QuestsInteraction
    data class QuestStatusSelected(val questId: String, val status: QuestStatus) : QuestsInteraction
    data class ObjectiveStatusSelected(
        val questId: String,
        val objectiveId: String,
        val status: QuestObjectiveStatus,
    ) : QuestsInteraction
    data class LinkedLoreSelected(val loreId: String) : QuestsInteraction
    data class LinkedPersonSelected(val personId: String, val worldOwned: Boolean) : QuestsInteraction
    data class LinkedSessionSelected(val sessionId: String) : QuestsInteraction
    data class LinkedLocationSelected(val locationId: String) : QuestsInteraction
    data class EditorTitleChanged(val title: String) : QuestsInteraction
    data class EditorSummaryChanged(val summary: String) : QuestsInteraction
    data class EditorStatusSelected(val status: QuestStatus) : QuestsInteraction
    data class EditorLocationSelected(val locationId: String?) : QuestsInteraction
    data object EditorObjectiveAdded : QuestsInteraction
    data class EditorObjectiveRemoved(val index: Int) : QuestsInteraction
    data class EditorObjectiveTitleChanged(val index: Int, val title: String) : QuestsInteraction
    data class EditorObjectiveStatusSelected(
        val index: Int,
        val status: QuestObjectiveStatus,
    ) : QuestsInteraction
    data class EditorLoreToggled(val loreId: String) : QuestsInteraction
    data class EditorWorldPersonToggled(val personId: String) : QuestsInteraction
    data class EditorCampaignPersonToggled(val personId: String) : QuestsInteraction
    data class EditorSessionToggled(val sessionId: String) : QuestsInteraction
    data object EditorSaved : QuestsInteraction
    data object EditorDismissed : QuestsInteraction
}
