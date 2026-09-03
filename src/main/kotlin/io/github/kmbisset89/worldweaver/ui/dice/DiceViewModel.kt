package io.github.kmbisset89.worldweaver.ui.dice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.ActiveContextRepository
import io.github.kmbisset89.worldweaver.domain.DiceNotationParser
import io.github.kmbisset89.worldweaver.domain.DiceRollRequest
import io.github.kmbisset89.worldweaver.domain.DiceRollResult
import io.github.kmbisset89.worldweaver.domain.DiceRoller
import io.github.kmbisset89.worldweaver.domain.DieSides
import io.github.kmbisset89.worldweaver.domain.RollMode

internal class DiceViewModel(
    private val diceRoller: DiceRoller,
    private val notationParser: DiceNotationParser = DiceNotationParser(),
    initialColorStyle: DiceColorStyle = DiceColorStyle.load(),
    private val persistColorStyle: (DiceColorStyle) -> Unit = { DiceColorStyle.save(it) },
    initialAlwaysOnTop: Boolean = DiceAlwaysOnTopStore.load(),
    private val persistAlwaysOnTop: (Boolean) -> Unit = { DiceAlwaysOnTopStore.save(it) },
    private val activeContextRepository: ActiveContextRepository? = null,
    appScope: AppCoroutineScope? = null,
) {
    private val _state = MutableStateFlow<DiceViewState>(
        initialContent(
            colorStyle = initialColorStyle,
            alwaysOnTop = initialAlwaysOnTop,
        ),
    )
    val state: StateFlow<DiceViewState> = _state.asStateFlow()

    private val historyBySession = mutableMapOf<String, List<DiceRollResult>>()
    private val lastResultBySession = mutableMapOf<String, DiceRollResult?>()

    init {
        val repository = activeContextRepository
        if (repository != null && appScope != null) {
            appScope.scope.launch {
                repository.observe().collect {
                    showSessionHistory()
                }
            }
        }
    }

    fun onInteraction(interaction: DiceInteraction) {
        when (interaction) {
            DiceInteraction.ScreenStarted -> showSessionHistory()
            is DiceInteraction.DieSelected -> updateDie(interaction.die)
            is DiceInteraction.CountChanged -> updateCount(interaction.count)
            is DiceInteraction.ModifierChanged -> updateModifier(interaction.modifierText)
            is DiceInteraction.RollModeSelected -> updateRollMode(interaction.mode)
            is DiceInteraction.EntryModeSelected -> updateEntryMode(interaction.mode)
            is DiceInteraction.NotationChanged -> updateNotation(interaction.notationText)
            is DiceInteraction.TableFacesChanged -> updateTableFaces(interaction.facesText)
            is DiceInteraction.ColorStyleSelected -> updateColorStyle(interaction.style)
            DiceInteraction.RollSelected -> submitRoll()
            DiceInteraction.HistoryCleared -> clearHistory()
            DiceInteraction.FloatingOpened -> setFloatingOpen(true)
            DiceInteraction.FloatingClosed -> setFloatingOpen(false)
            DiceInteraction.AlwaysOnTopToggled -> toggleAlwaysOnTop()
        }
    }

    private fun updateDie(die: DieSides) {
        updateContent { current ->
            val rollMode = resolvedMode(die, current.count, current.rollMode)
            current.copy(
                selectedDie = die,
                rollMode = rollMode,
                advantageEnabled = advantageEnabled(die, current.count),
                notationText = formatNotation(
                    die = die,
                    count = current.count,
                    modifier = current.modifierText.toIntOrNull() ?: 0,
                    mode = rollMode,
                ),
                entryError = null,
            )
        }
    }

    private fun updateCount(count: Int) {
        updateContent { current ->
            val nextCount = count.coerceIn(MIN_COUNT, MAX_COUNT)
            val rollMode = resolvedMode(current.selectedDie, nextCount, current.rollMode)
            current.copy(
                count = nextCount,
                rollMode = rollMode,
                advantageEnabled = advantageEnabled(current.selectedDie, nextCount),
                notationText = formatNotation(
                    die = current.selectedDie,
                    count = nextCount,
                    modifier = current.modifierText.toIntOrNull() ?: 0,
                    mode = rollMode,
                ),
                entryError = null,
            )
        }
    }

    private fun updateModifier(modifierText: String) {
        updateContent { current ->
            current.copy(
                modifierText = modifierText,
                notationText = formatNotation(
                    die = current.selectedDie,
                    count = current.count,
                    modifier = modifierText.toIntOrNull() ?: 0,
                    mode = current.rollMode,
                ),
                entryError = null,
            )
        }
    }

    private fun updateRollMode(mode: RollMode) {
        updateContent { current ->
            val rollMode = resolvedMode(current.selectedDie, current.count, mode)
            current.copy(
                rollMode = rollMode,
                notationText = formatNotation(
                    die = current.selectedDie,
                    count = current.count,
                    modifier = current.modifierText.toIntOrNull() ?: 0,
                    mode = rollMode,
                ),
                entryError = null,
            )
        }
    }

    private fun updateEntryMode(mode: DiceEntryMode) {
        updateContent { current ->
            current.copy(
                entryMode = mode,
                entryError = null,
            )
        }
    }

    private fun updateNotation(notationText: String) {
        updateContent { current ->
            val parsed = notationParser.parse(notationText)
            if (parsed == null) {
                current.copy(notationText = notationText)
            } else {
                val die = DieSides.fromSides(parsed.sides) ?: current.selectedDie
                current.copy(
                    notationText = notationText,
                    selectedDie = die,
                    count = parsed.count,
                    modifierText = parsed.modifier.toString(),
                    rollMode = resolvedMode(die, parsed.count, parsed.mode),
                    advantageEnabled = advantageEnabled(die, parsed.count),
                    entryError = null,
                )
            }
        }
    }

    private fun updateTableFaces(facesText: String) {
        updateContent { current ->
            current.copy(
                tableFacesText = facesText,
                entryError = null,
            )
        }
    }

    private fun updateColorStyle(style: DiceColorStyle) {
        persistColorStyle(style)
        updateContent { current -> current.copy(colorStyle = style) }
    }

    private fun submitRoll() {
        val current = _state.value as? DiceViewState.Content ?: return
        when (current.entryMode) {
            DiceEntryMode.Digital -> rollDigital(current)
            DiceEntryMode.Table -> logTable(current)
        }
    }

    private fun rollDigital(current: DiceViewState.Content) {
        val result = diceRoller.roll(requestFrom(current))
        recordResult(result, bumpToken = true)
    }

    private fun logTable(current: DiceViewState.Content) {
        val expected = expectedFaceCount(current.rollMode, current.count)
        val faces = parseFaces(current.tableFacesText)
        if (faces == null || faces.size != expected) {
            updateContent { state ->
                state.copy(
                    entryError = tableCountError(expected, current.rollMode, current.selectedDie, current.count),
                )
            }
            return
        }
        val result = diceRoller.record(requestFrom(current), faces)
        if (result == null) {
            updateContent { state ->
                state.copy(
                    entryError = "Each face must be from 1 to ${current.selectedDie.sides}",
                )
            }
            return
        }
        recordResult(result, bumpToken = false)
    }

    private fun recordResult(result: DiceRollResult, bumpToken: Boolean) {
        persistCurrentSession(result)
        val key = sessionKey()
        updateContent { state ->
            state.copy(
                lastResult = lastResultBySession[key],
                history = historyBySession[key].orEmpty(),
                rollToken = if (bumpToken) state.rollToken + 1 else state.rollToken,
                entryError = null,
            )
        }
    }

    private fun persistCurrentSession(result: DiceRollResult) {
        val key = sessionKey()
        val history = historyForCurrentSession(listOf(result) + historyBySession[key].orEmpty())
        historyBySession[key] = history
        lastResultBySession[key] = result
    }

    private fun historyForCurrentSession(proposed: List<DiceRollResult>): List<DiceRollResult> {
        return proposed.take(HISTORY_LIMIT)
    }

    private fun showSessionHistory() {
        val key = sessionKey()
        updateContent { state ->
            state.copy(
                lastResult = lastResultBySession[key],
                history = historyBySession[key].orEmpty(),
            )
        }
    }

    private fun sessionKey(): String {
        return activeContextRepository?.get()?.activeSessionId.orEmpty()
    }

    private fun clearHistory() {
        val key = sessionKey()
        historyBySession[key] = emptyList()
        lastResultBySession.remove(key)
        updateContent { current ->
            current.copy(lastResult = null, history = emptyList())
        }
    }

    private fun setFloatingOpen(open: Boolean) {
        updateContent { current -> current.copy(isFloatingOpen = open) }
    }

    private fun toggleAlwaysOnTop() {
        val current = _state.value as? DiceViewState.Content ?: return
        val next = !current.isAlwaysOnTop
        persistAlwaysOnTop(next)
        updateContent { state -> state.copy(isAlwaysOnTop = next) }
    }

    private fun requestFrom(current: DiceViewState.Content): DiceRollRequest {
        return DiceRollRequest(
            sides = current.selectedDie.sides,
            count = current.count,
            modifier = current.modifierText.toIntOrNull() ?: 0,
            mode = current.rollMode,
        )
    }

    private fun updateContent(
        transform: (DiceViewState.Content) -> DiceViewState.Content,
    ) {
        _state.update { current ->
            when (current) {
                is DiceViewState.Content -> transform(current)
            }
        }
    }

    private companion object {
        const val MIN_COUNT = 1
        const val MAX_COUNT = 20
        const val HISTORY_LIMIT = 50

        fun initialContent(
            colorStyle: DiceColorStyle,
            alwaysOnTop: Boolean,
        ): DiceViewState.Content {
            val die = DieSides.D20
            val count = 1
            val modifier = 0
            val mode = RollMode.Normal
            return DiceViewState.Content(
                selectedDie = die,
                count = count,
                modifierText = modifier.toString(),
                rollMode = mode,
                advantageEnabled = true,
                entryMode = DiceEntryMode.Digital,
                notationText = formatNotation(die, count, modifier, mode),
                tableFacesText = "",
                colorStyle = colorStyle,
                entryError = null,
                rollToken = 0L,
                lastResult = null,
                history = emptyList(),
                isFloatingOpen = false,
                isAlwaysOnTop = alwaysOnTop,
            )
        }

        fun advantageEnabled(die: DieSides, count: Int): Boolean {
            return die == DieSides.D20 && count == 1
        }

        fun resolvedMode(die: DieSides, count: Int, mode: RollMode): RollMode {
            return if (advantageEnabled(die, count)) mode else RollMode.Normal
        }

        fun formatNotation(
            die: DieSides,
            count: Int,
            modifier: Int,
            mode: RollMode,
        ): String {
            val countPrefix = if (count == 1) "" else count.toString()
            val modifierPart = when {
                modifier > 0 -> "+$modifier"
                modifier < 0 -> modifier.toString()
                else -> ""
            }
            val modePart = when (mode) {
                RollMode.Normal -> ""
                RollMode.Advantage -> " adv"
                RollMode.Disadvantage -> " dis"
            }
            return "${countPrefix}d${die.sides}$modifierPart$modePart"
        }

        fun expectedFaceCount(mode: RollMode, count: Int): Int {
            return when (mode) {
                RollMode.Normal -> count
                RollMode.Advantage, RollMode.Disadvantage -> 2
            }
        }

        fun parseFaces(text: String): List<Int>? {
            val tokens = text.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            if (tokens.isEmpty()) {
                return null
            }
            val faces = tokens.map { it.toIntOrNull() }
            if (faces.any { it == null }) {
                return null
            }
            return faces.filterNotNull()
        }

        fun tableCountError(
            expected: Int,
            mode: RollMode,
            die: DieSides,
            count: Int,
        ): String {
            return when (mode) {
                RollMode.Advantage -> "Enter 2 faces for advantage"
                RollMode.Disadvantage -> "Enter 2 faces for disadvantage"
                RollMode.Normal -> {
                    val noun = if (expected == 1) "face" else "faces"
                    "Enter $expected $noun for ${count}${die.label}"
                }
            }
        }
    }
}
