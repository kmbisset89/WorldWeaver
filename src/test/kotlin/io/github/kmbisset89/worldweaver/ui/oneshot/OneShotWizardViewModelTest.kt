package io.github.kmbisset89.worldweaver.ui.oneshot

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.CreateCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.CreateEncounterUseCase
import io.github.kmbisset89.worldweaver.domain.CreateFactionUseCase
import io.github.kmbisset89.worldweaver.domain.CreateLocationUseCase
import io.github.kmbisset89.worldweaver.domain.CreateLoreUseCase
import io.github.kmbisset89.worldweaver.domain.CreateOneShotUseCase
import io.github.kmbisset89.worldweaver.domain.CreateQuestUseCase
import io.github.kmbisset89.worldweaver.domain.CreateSessionUseCase
import io.github.kmbisset89.worldweaver.domain.CreateWorldPersonUseCase
import io.github.kmbisset89.worldweaver.domain.CreateWorldUseCase
import io.github.kmbisset89.worldweaver.domain.DefaultWorldCalendarFactory
import io.github.kmbisset89.worldweaver.domain.EntityIdFactory
import io.github.kmbisset89.worldweaver.domain.FakeActiveContextRepository
import io.github.kmbisset89.worldweaver.domain.FakeBattleMapRepository
import io.github.kmbisset89.worldweaver.domain.FakeCampaignPersonRepository
import io.github.kmbisset89.worldweaver.domain.FakeCampaignRepository
import io.github.kmbisset89.worldweaver.domain.FakeEncounterRepository
import io.github.kmbisset89.worldweaver.domain.FakeFactionRepository
import io.github.kmbisset89.worldweaver.domain.FakeLocationRepository
import io.github.kmbisset89.worldweaver.domain.FakeLoreRepository
import io.github.kmbisset89.worldweaver.domain.FakeQuestRepository
import io.github.kmbisset89.worldweaver.domain.FakeSessionRepository
import io.github.kmbisset89.worldweaver.domain.FakeWorldCalendarRepository
import io.github.kmbisset89.worldweaver.domain.FakeWorldPersonRepository
import io.github.kmbisset89.worldweaver.domain.FakeWorldRepository
import io.github.kmbisset89.worldweaver.domain.InstantProvider
import io.github.kmbisset89.worldweaver.domain.SetActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.SetActiveWorldUseCase
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class OneShotWizardViewModelTest {
    private val scope = AppCoroutineScope()

    @AfterTest
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun nextWithoutRequiredIdentityShowsErrors() {
        val viewModel = viewModel()

        viewModel.onInteraction(OneShotWizardInteraction.NextSelected)

        val state = content(viewModel)
        assertEquals(OneShotWizardViewState.Step.Identity, state.step)
        assertEquals("Name is required", state.worldNameError)
        assertEquals("Name is required", state.campaignNameError)
    }

    @Test
    fun genreChipFillsBlankRealmAndLeavesTypedWorldName() {
        val viewModel = viewModel()

        viewModel.onInteraction(OneShotWizardInteraction.WorldNameChanged("My World"))
        viewModel.onInteraction(OneShotWizardInteraction.GenreSelected("high_fantasy"))

        val state = content(viewModel)
        assertEquals("My World", state.answers.worldName)
        assertEquals("The Crownlands", state.answers.realmName)
        assertEquals("high_fantasy", state.answers.genreId)
    }

    @Test
    fun backReturnsToThePreviousStep() {
        val viewModel = viewModel()

        viewModel.onInteraction(OneShotWizardInteraction.WorldNameChanged("Ashfen"))
        viewModel.onInteraction(OneShotWizardInteraction.CampaignNameChanged("Night Watch"))
        viewModel.onInteraction(OneShotWizardInteraction.NextSelected)
        viewModel.onInteraction(OneShotWizardInteraction.BackSelected)

        assertEquals(OneShotWizardViewState.Step.Identity, content(viewModel).step)
    }

    @Test
    fun saveCreatesAPlayableOneShot() = runBlocking {
        val harness = Harness(scope)
        val viewModel = harness.viewModel

        viewModel.onInteraction(OneShotWizardInteraction.WorldNameChanged("Ashfen"))
        viewModel.onInteraction(OneShotWizardInteraction.CampaignNameChanged("Night Watch"))
        viewModel.onInteraction(OneShotWizardInteraction.NextSelected)
        viewModel.onInteraction(OneShotWizardInteraction.NextSelected)
        viewModel.onInteraction(OneShotWizardInteraction.OpeningSiteNameChanged("The Lantern"))
        viewModel.onInteraction(OneShotWizardInteraction.NextSelected)
        viewModel.onInteraction(OneShotWizardInteraction.NextSelected)
        viewModel.onInteraction(OneShotWizardInteraction.QuestTitleChanged("Find the missing"))
        viewModel.onInteraction(OneShotWizardInteraction.NextSelected)
        viewModel.onInteraction(OneShotWizardInteraction.NextSelected)
        assertEquals(OneShotWizardViewState.Step.Review, content(viewModel).step)
        viewModel.onInteraction(OneShotWizardInteraction.Saved)

        withTimeout(2_000) {
            while (content(viewModel).isSaving) {
                yield()
                delay(10)
            }
        }

        assertEquals(1, harness.worlds.all().size)
        assertEquals("Ashfen", harness.worlds.all().single().name)
        assertEquals(1, harness.campaigns.all().size)
        assertEquals("Night Watch", harness.campaigns.all().single().name)
        assertTrue(harness.locations.all().any { it.name == "The Lantern" })
        assertEquals(1, harness.quests.all().size)
        assertEquals(1, harness.sessions.all().size)
        assertEquals("Ashfen", harness.worlds.all().single().name)
        assertNull(content(viewModel).saveError)
        assertEquals(OneShotWizardViewState.Step.Identity, content(viewModel).step)
    }

    private fun viewModel(): OneShotWizardViewModel {
        return Harness(scope).viewModel
    }

    private fun content(viewModel: OneShotWizardViewModel): OneShotWizardViewState.Content {
        return assertIs(viewModel.state.value)
    }

    private class Harness(
        appScope: AppCoroutineScope,
    ) {
        val worlds = FakeWorldRepository()
        val calendars = FakeWorldCalendarRepository()
        val campaigns = FakeCampaignRepository()
        val locations = FakeLocationRepository()
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val factions = FakeFactionRepository()
        val lore = FakeLoreRepository()
        val quests = FakeQuestRepository()
        val sessions = FakeSessionRepository()
        val encounters = FakeEncounterRepository()
        val battleMaps = FakeBattleMapRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "id-${++nextId}" }
        val viewModel = OneShotWizardViewModel(
            appScope = appScope,
            createOneShot = CreateOneShotUseCase(
                createWorld = CreateWorldUseCase(
                    worlds,
                    calendars,
                    DefaultWorldCalendarFactory(ids),
                    ids,
                    instant,
                    SetActiveWorldUseCase(worlds, campaigns, context, instant),
                ),
                createCampaign = CreateCampaignUseCase(
                    campaigns,
                    context,
                    ids,
                    instant,
                    SetActiveCampaignUseCase(campaigns, context),
                ),
                createLocation = CreateLocationUseCase(locations, context, ids, instant),
                createWorldPerson = CreateWorldPersonUseCase(worldPeople, context, ids, instant),
                createFaction = CreateFactionUseCase(factions, context, ids, instant),
                createLore = CreateLoreUseCase(lore, locations, context, ids, instant),
                createQuest = CreateQuestUseCase(
                    quests,
                    campaigns,
                    locations,
                    lore,
                    worldPeople,
                    campaignPeople,
                    sessions,
                    context,
                    ids,
                    instant,
                ),
                createSession = CreateSessionUseCase(
                    sessions,
                    campaigns,
                    calendars,
                    context,
                    ids,
                    instant,
                ),
                createEncounter = CreateEncounterUseCase(
                    encounters,
                    campaigns,
                    locations,
                    battleMaps,
                    worldPeople,
                    campaignPeople,
                    context,
                    ids,
                    instant,
                ),
            ),
        )
    }
}
