package io.github.kmbisset89.worldweaver.ui.oneshot

import io.github.kmbisset89.worldweaver.domain.OneShotAnswers

internal sealed class OneShotWizardViewState {
    data class Content(
        val step: Step,
        val answers: OneShotAnswers,
        val genres: List<Chip>,
        val tones: List<Chip>,
        val hooks: List<Chip>,
        val siteTypes: List<Chip>,
        val villainTypes: List<Chip>,
        val objectives: List<Chip>,
        val worldNameError: String?,
        val campaignNameError: String?,
        val siteNameError: String?,
        val questTitleError: String?,
        val saveError: String?,
        val isSaving: Boolean,
        val reviewItems: List<String>,
    ) : OneShotWizardViewState()

    data class Chip(
        val id: String,
        val label: String,
    )

    enum class Step {
        Identity,
        Hook,
        Places,
        People,
        Conflict,
        TablePlan,
        Review,
        ;

        fun title(): String {
            return when (this) {
                Identity -> "Identity"
                Hook -> "Hook"
                Places -> "Places"
                People -> "People"
                Conflict -> "Conflict"
                TablePlan -> "Table plan"
                Review -> "Review"
            }
        }

        fun index(): Int = entries.indexOf(this) + 1

        fun isFirst(): Boolean = this == Identity

        fun isLast(): Boolean = this == Review

        fun next(): Step? {
            return entries.getOrNull(entries.indexOf(this) + 1)
        }

        fun previous(): Step? {
            return entries.getOrNull(entries.indexOf(this) - 1)
        }
    }
}
