package net.tactware.worldweaver.ui.oneshot

import net.tactware.worldweaver.domain.EncounterDifficulty
import net.tactware.worldweaver.domain.GameSystem
import net.tactware.worldweaver.domain.PersonKind

internal sealed interface OneShotWizardInteraction {
    data object ScreenStarted : OneShotWizardInteraction
    data object BackSelected : OneShotWizardInteraction
    data object NextSelected : OneShotWizardInteraction
    data object Saved : OneShotWizardInteraction
    data object Dismissed : OneShotWizardInteraction
    data class WorldNameChanged(val value: String) : OneShotWizardInteraction
    data class CampaignNameChanged(val value: String) : OneShotWizardInteraction
    data class GameSystemSelected(val gameSystem: GameSystem) : OneShotWizardInteraction
    data class LoglineChanged(val value: String) : OneShotWizardInteraction
    data class GenreSelected(val id: String) : OneShotWizardInteraction
    data class ToneSelected(val id: String) : OneShotWizardInteraction
    data class HookSelected(val id: String) : OneShotWizardInteraction
    data class HookChanged(val value: String) : OneShotWizardInteraction
    data class RealmNameChanged(val value: String) : OneShotWizardInteraction
    data class RegionNameChanged(val value: String) : OneShotWizardInteraction
    data class SettlementNameChanged(val value: String) : OneShotWizardInteraction
    data class SiteTypeSelected(val id: String) : OneShotWizardInteraction
    data class OpeningSiteNameChanged(val value: String) : OneShotWizardInteraction
    data class OpeningSiteDescriptionChanged(val value: String) : OneShotWizardInteraction
    data class MiddleSiteNameChanged(val value: String) : OneShotWizardInteraction
    data class MiddleSiteDescriptionChanged(val value: String) : OneShotWizardInteraction
    data class ClimaxSiteNameChanged(val value: String) : OneShotWizardInteraction
    data class ClimaxSiteDescriptionChanged(val value: String) : OneShotWizardInteraction
    data class PatronNameChanged(val value: String) : OneShotWizardInteraction
    data class PatronDescriptionChanged(val value: String) : OneShotWizardInteraction
    data class VillainNameChanged(val value: String) : OneShotWizardInteraction
    data class VillainDescriptionChanged(val value: String) : OneShotWizardInteraction
    data class VillainKindSelected(val kind: PersonKind) : OneShotWizardInteraction
    data class VillainTypeSelected(val id: String) : OneShotWizardInteraction
    data class AllyNameChanged(val value: String) : OneShotWizardInteraction
    data class AllyDescriptionChanged(val value: String) : OneShotWizardInteraction
    data class FactionNameChanged(val value: String) : OneShotWizardInteraction
    data class FactionDescriptionChanged(val value: String) : OneShotWizardInteraction
    data class QuestTitleChanged(val value: String) : OneShotWizardInteraction
    data class StakesChanged(val value: String) : OneShotWizardInteraction
    data class ObjectiveChanged(val index: Int, val value: String) : OneShotWizardInteraction
    data class ObjectiveChipSelected(val id: String) : OneShotWizardInteraction
    data class TwistChanged(val value: String) : OneShotWizardInteraction
    data class SessionNameChanged(val value: String) : OneShotWizardInteraction
    data class OpeningSceneTitleChanged(val value: String) : OneShotWizardInteraction
    data class MiddleSceneTitleChanged(val value: String) : OneShotWizardInteraction
    data class ClimaxSceneTitleChanged(val value: String) : OneShotWizardInteraction
    data class IncludeEncounterChanged(val include: Boolean) : OneShotWizardInteraction
    data class EncounterNameChanged(val value: String) : OneShotWizardInteraction
    data class EncounterDifficultySelected(val difficulty: EncounterDifficulty) : OneShotWizardInteraction
}
