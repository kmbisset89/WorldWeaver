package net.tactware.worldweaver.ui.dice

import net.tactware.worldweaver.domain.DiceRollSource
import net.tactware.worldweaver.domain.DiceRoller
import net.tactware.worldweaver.domain.DieSides
import net.tactware.worldweaver.domain.FakeActiveContextRepository
import net.tactware.worldweaver.domain.RollMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class DiceViewModelTest {
    @Test
    fun digitalRollUpdatesLastResultHistoryAndToken() {
        val faces = sequenceOf(17).iterator()
        val viewModel = viewModel(DiceRoller { _ -> faces.next() })

        viewModel.onInteraction(DiceInteraction.RollSelected)

        val state = content(viewModel)
        assertEquals(listOf(17), state.lastResult?.faces)
        assertEquals(17, state.lastResult?.total)
        assertEquals(DiceRollSource.Automated, state.lastResult?.source)
        assertEquals(1, state.history.size)
        assertEquals(1L, state.rollToken)
        assertNull(state.entryError)
    }

    @Test
    fun tableLogRecordsManualFacesWithoutAnimating() {
        val viewModel = viewModel(DiceRoller())

        viewModel.onInteraction(DiceInteraction.EntryModeSelected(DiceEntryMode.Table))
        viewModel.onInteraction(DiceInteraction.TableFacesChanged("17"))
        viewModel.onInteraction(DiceInteraction.RollSelected)

        val state = content(viewModel)
        assertEquals(listOf(17), state.lastResult?.faces)
        assertEquals(17, state.lastResult?.total)
        assertEquals(DiceRollSource.Manual, state.lastResult?.source)
        assertEquals(1, state.history.size)
        assertEquals(0L, state.rollToken)
        assertNull(state.entryError)
    }

    @Test
    fun historyIsKeyedToTheActiveSession() {
        val context = FakeActiveContextRepository()
        context.setActiveSessionId("session-a")
        val faces = sequenceOf(17, 4).iterator()
        val viewModel = DiceViewModel(
            diceRoller = DiceRoller { _ -> faces.next() },
            initialColorStyle = DiceColorStyle.BONE,
            persistColorStyle = { },
            persistAlwaysOnTop = { },
            activeContextRepository = context,
        )

        viewModel.onInteraction(DiceInteraction.RollSelected)
        assertEquals(listOf(17), content(viewModel).lastResult?.faces)

        context.setActiveSessionId("session-b")
        viewModel.onInteraction(DiceInteraction.ScreenStarted)
        assertTrue(content(viewModel).history.isEmpty())
        assertNull(content(viewModel).lastResult)

        viewModel.onInteraction(DiceInteraction.RollSelected)
        assertEquals(listOf(4), content(viewModel).lastResult?.faces)
        assertEquals(1, content(viewModel).history.size)

        context.setActiveSessionId("session-a")
        viewModel.onInteraction(DiceInteraction.ScreenStarted)
        assertEquals(listOf(17), content(viewModel).lastResult?.faces)
        assertEquals(1, content(viewModel).history.size)
    }

    @Test
    fun invalidTableFacesStayOutOfHistory() {
        val viewModel = viewModel(DiceRoller())

        viewModel.onInteraction(DiceInteraction.EntryModeSelected(DiceEntryMode.Table))
        viewModel.onInteraction(DiceInteraction.TableFacesChanged("21"))
        viewModel.onInteraction(DiceInteraction.RollSelected)

        val state = content(viewModel)
        assertNull(state.lastResult)
        assertTrue(state.history.isEmpty())
        assertEquals("Each face must be from 1 to 20", state.entryError)
    }

    @Test
    fun wrongTableFaceCountStaysOutOfHistory() {
        val viewModel = viewModel(DiceRoller())

        viewModel.onInteraction(DiceInteraction.DieSelected(DieSides.D6))
        viewModel.onInteraction(DiceInteraction.CountChanged(2))
        viewModel.onInteraction(DiceInteraction.EntryModeSelected(DiceEntryMode.Table))
        viewModel.onInteraction(DiceInteraction.TableFacesChanged("4"))
        viewModel.onInteraction(DiceInteraction.RollSelected)

        val state = content(viewModel)
        assertNull(state.lastResult)
        assertTrue(state.history.isEmpty())
        assertEquals("Enter 2 faces for 2d6", state.entryError)
    }

    @Test
    fun notationUpdatesDieCountAndModifier() {
        val viewModel = viewModel(DiceRoller())

        viewModel.onInteraction(DiceInteraction.NotationChanged("2d6+3"))

        val state = content(viewModel)
        assertEquals(DieSides.D6, state.selectedDie)
        assertEquals(2, state.count)
        assertEquals("3", state.modifierText)
        assertEquals(RollMode.Normal, state.rollMode)
        assertEquals("2d6+3", state.notationText)
    }

    @Test
    fun chipChangeRewritesNotation() {
        val viewModel = viewModel(DiceRoller())

        viewModel.onInteraction(DiceInteraction.DieSelected(DieSides.D8))
        viewModel.onInteraction(DiceInteraction.CountChanged(2))
        viewModel.onInteraction(DiceInteraction.ModifierChanged("1"))

        val state = content(viewModel)
        assertEquals("2d8+1", state.notationText)
    }

    @Test
    fun invalidNotationDoesNotChangeDie() {
        val viewModel = viewModel(DiceRoller())

        viewModel.onInteraction(DiceInteraction.NotationChanged("2d"))

        val state = content(viewModel)
        assertEquals(DieSides.D20, state.selectedDie)
        assertEquals(1, state.count)
        assertEquals("0", state.modifierText)
        assertEquals("2d", state.notationText)
    }

    @Test
    fun floatingOpenedAndClosedTogglesWindowFlag() {
        val viewModel = viewModel(DiceRoller())

        assertEquals(false, content(viewModel).isFloatingOpen)
        viewModel.onInteraction(DiceInteraction.FloatingOpened)
        assertEquals(true, content(viewModel).isFloatingOpen)
        viewModel.onInteraction(DiceInteraction.FloatingClosed)
        assertEquals(false, content(viewModel).isFloatingOpen)
    }

    @Test
    fun alwaysOnTopTogglePersists() {
        var persisted: Boolean? = null
        val viewModel = DiceViewModel(
            diceRoller = DiceRoller(),
            initialColorStyle = DiceColorStyle.BONE,
            persistColorStyle = { },
            initialAlwaysOnTop = false,
            persistAlwaysOnTop = { persisted = it },
        )

        viewModel.onInteraction(DiceInteraction.AlwaysOnTopToggled)

        assertEquals(true, content(viewModel).isAlwaysOnTop)
        assertEquals(true, persisted)

        viewModel.onInteraction(DiceInteraction.AlwaysOnTopToggled)

        assertEquals(false, content(viewModel).isAlwaysOnTop)
        assertEquals(false, persisted)
    }

    @Test
    fun initialAlwaysOnTopIsRestored() {
        val viewModel = DiceViewModel(
            diceRoller = DiceRoller(),
            initialColorStyle = DiceColorStyle.BONE,
            persistColorStyle = { },
            initialAlwaysOnTop = true,
            persistAlwaysOnTop = { },
        )

        assertEquals(true, content(viewModel).isAlwaysOnTop)
        assertEquals(false, content(viewModel).isFloatingOpen)
    }

    private fun viewModel(roller: DiceRoller): DiceViewModel {
        return DiceViewModel(
            diceRoller = roller,
            initialColorStyle = DiceColorStyle.BONE,
            persistColorStyle = { },
            persistAlwaysOnTop = { },
        )
    }

    private fun content(viewModel: DiceViewModel): DiceViewState.Content {
        return assertIs(viewModel.state.value)
    }
}
