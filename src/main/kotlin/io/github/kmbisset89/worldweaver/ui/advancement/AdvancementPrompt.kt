package io.github.kmbisset89.worldweaver.ui.advancement

internal sealed class AdvancementPrompt {
    data object AwardLevel : AdvancementPrompt()

    data class AwardExperience(
        val amountText: String,
        val amountError: String?,
    ) : AdvancementPrompt()
}
