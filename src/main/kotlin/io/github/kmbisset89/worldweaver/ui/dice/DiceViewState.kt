package io.github.kmbisset89.worldweaver.ui.dice

import io.github.kmbisset89.worldweaver.domain.DiceRollResult
import io.github.kmbisset89.worldweaver.domain.DieSides
import io.github.kmbisset89.worldweaver.domain.RollMode

internal sealed class DiceViewState {
    data class Content(
        val selectedDie: DieSides,
        val count: Int,
        val modifierText: String,
        val rollMode: RollMode,
        val advantageEnabled: Boolean,
        val entryMode: DiceEntryMode,
        val notationText: String,
        val tableFacesText: String,
        val colorStyle: DiceColorStyle,
        val entryError: String?,
        val rollToken: Long,
        val lastResult: DiceRollResult?,
        val history: List<DiceRollResult>,
        val isFloatingOpen: Boolean,
        val isAlwaysOnTop: Boolean,
    ) : DiceViewState()
}
