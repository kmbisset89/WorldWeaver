package io.github.kmbisset89.worldweaver.ui.campaigns

import io.github.kmbisset89.worldweaver.domain.CampaignStatus
import io.github.kmbisset89.worldweaver.domain.GameSystem
import io.github.kmbisset89.worldweaver.domain.LevelingMode

internal sealed interface CampaignsInteraction {
    data object ScreenStarted : CampaignsInteraction
    data object RetrySelected : CampaignsInteraction
    data object CreateWorldSelected : CampaignsInteraction
    data object NewCampaignSelected : CampaignsInteraction
    data class CampaignSelected(val campaignId: String) : CampaignsInteraction
    data class CampaignOpened(val campaignId: String) : CampaignsInteraction
    data class EditCampaignSelected(val campaignId: String) : CampaignsInteraction
    data class DeleteCampaignSelected(val campaignId: String) : CampaignsInteraction
    data object DeleteConfirmed : CampaignsInteraction
    data object DeleteCancelled : CampaignsInteraction
    data class StatusSelected(val campaignId: String, val status: CampaignStatus) : CampaignsInteraction
    data object RetiredVisibilityToggled : CampaignsInteraction
    data class EditorNameChanged(val name: String) : CampaignsInteraction
    data class EditorDescriptionChanged(val description: String) : CampaignsInteraction
    data class EditorNotesChanged(val notes: String) : CampaignsInteraction
    data class EditorGameSystemSelected(val gameSystem: GameSystem) : CampaignsInteraction
    data class EditorLevelingModeSelected(val levelingMode: LevelingMode) : CampaignsInteraction
    data object EditorSaved : CampaignsInteraction
    data object EditorDismissed : CampaignsInteraction
    data object OpenCharactersSelected : CampaignsInteraction
    data object CreatePlayerCharacterSelected : CampaignsInteraction
    data object OpenQuestsSelected : CampaignsInteraction
    data object OpenSessionsSelected : CampaignsInteraction
}
