package io.github.kmbisset89.worldweaver.ui.campaigns

import io.github.kmbisset89.worldweaver.domain.Campaign
import io.github.kmbisset89.worldweaver.domain.CampaignStatus
import io.github.kmbisset89.worldweaver.domain.GameSystem
import io.github.kmbisset89.worldweaver.domain.LevelingMode

internal sealed class CampaignsViewState {
    data object Loading : CampaignsViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : CampaignsViewState()

    data object NoActiveWorld : CampaignsViewState()

    data class Empty(
        val worldName: String,
        val worldDefaultGameSystem: GameSystem,
        val editor: CampaignEditorState?,
    ) : CampaignsViewState()

    data class Content(
        val worldName: String,
        val worldDefaultGameSystem: GameSystem,
        val campaigns: List<Campaign>,
        val selectedCampaign: Campaign?,
        val showRetired: Boolean,
        val partyMembers: List<PartyMember>,
        val activeQuests: List<OverviewQuest>,
        val lastSession: OverviewSession?,
        val nextSessionHint: String,
        val editor: CampaignEditorState?,
        val pendingDelete: PendingDelete?,
    ) : CampaignsViewState()

    data class PartyMember(
        val id: String,
        val name: String,
        val summary: String,
    )

    data class OverviewQuest(
        val id: String,
        val title: String,
    )

    data class OverviewSession(
        val id: String,
        val name: String,
        val recap: String,
        val dateLabel: String?,
    )

    data class CampaignEditorState(
        val campaignId: String?,
        val name: String,
        val description: String,
        val notes: String,
        val gameSystem: GameSystem,
        val levelingMode: LevelingMode,
        val nameError: String?,
    )

    data class PendingDelete(
        val campaignId: String,
        val campaignName: String,
    )

    companion object {
        fun statusLabel(status: CampaignStatus): String {
            return when (status) {
                CampaignStatus.Active -> "Active"
                CampaignStatus.Archived -> "Archived"
                CampaignStatus.Completed -> "Completed"
            }
        }
    }
}
