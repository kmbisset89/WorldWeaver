package net.tactware.worldweaver.ui.dice

import net.tactware.worldweaver.domain.DieSides
import net.tactware.worldweaver.domain.RollMode

internal sealed interface DiceInteraction {
    data object ScreenStarted : DiceInteraction
    data class DieSelected(val die: DieSides) : DiceInteraction
    data class CountChanged(val count: Int) : DiceInteraction
    data class ModifierChanged(val modifierText: String) : DiceInteraction
    data class RollModeSelected(val mode: RollMode) : DiceInteraction
    data class EntryModeSelected(val mode: DiceEntryMode) : DiceInteraction
    data class NotationChanged(val notationText: String) : DiceInteraction
    data class TableFacesChanged(val facesText: String) : DiceInteraction
    data class ColorStyleSelected(val style: DiceColorStyle) : DiceInteraction
    data object RollSelected : DiceInteraction
    data object HistoryCleared : DiceInteraction
    data object FloatingOpened : DiceInteraction
    data object FloatingClosed : DiceInteraction
    data object AlwaysOnTopToggled : DiceInteraction
}
