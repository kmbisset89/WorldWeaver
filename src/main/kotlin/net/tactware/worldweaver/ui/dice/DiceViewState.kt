package net.tactware.worldweaver.ui.dice

import net.tactware.worldweaver.domain.DiceRollResult
import net.tactware.worldweaver.domain.DieSides
import net.tactware.worldweaver.domain.RollMode

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
    ) : DiceViewState()
}
