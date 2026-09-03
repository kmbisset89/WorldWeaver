package io.github.kmbisset89.worldweaver.ui.sheet

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.AbilityScores
import io.github.kmbisset89.worldweaver.domain.CampaignPerson
import io.github.kmbisset89.worldweaver.domain.ClassLevel
import io.github.kmbisset89.worldweaver.domain.DeathSaves
import io.github.kmbisset89.worldweaver.domain.FakeActiveContextRepository
import io.github.kmbisset89.worldweaver.domain.FakeCampaignPersonRepository
import io.github.kmbisset89.worldweaver.domain.FakeWorldPersonRepository
import io.github.kmbisset89.worldweaver.domain.FifthEditionSheet
import io.github.kmbisset89.worldweaver.domain.FifthEditionSkill
import io.github.kmbisset89.worldweaver.domain.FifthEditionSpellSlot
import io.github.kmbisset89.worldweaver.domain.InstantProvider
import io.github.kmbisset89.worldweaver.domain.ObservePeopleForActiveContextUseCase
import io.github.kmbisset89.worldweaver.domain.Pathfinder2ESheet
import io.github.kmbisset89.worldweaver.domain.PersonAvatarFileStore
import io.github.kmbisset89.worldweaver.domain.PersonKind
import io.github.kmbisset89.worldweaver.domain.UpdateCampaignPersonDeathSavesUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateCampaignPersonUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateWorldPersonUseCase
import io.github.kmbisset89.worldweaver.ui.characters.PersonMembership
import java.nio.file.Files
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CharacterSheetViewModelTest {
    private val scope = AppCoroutineScope()
    private val context = FakeActiveContextRepository()
    private val worldPeople = FakeWorldPersonRepository()
    private val campaignPeople = FakeCampaignPersonRepository()
    private val instant = InstantProvider { Instant.parse("2026-08-31T18:00:00Z") }
    private val tempDir = Files.createTempDirectory("ww-sheet-avatars").toFile()

    @AfterTest
    fun tearDown() {
        scope.cancel()
        tempDir.deleteRecursively()
    }

    @Test
    fun hiddenUntilAPersonIsOpened() {
        val viewModel = viewModel()
        assertIs<CharacterSheetViewState.Hidden>(viewModel.state.value)
    }

    @Test
    fun openingACampaignPersonShowsDerivedFifthEditionSheet() {
        runBlocking {
        seedCampaign()
        val viewModel = viewModel()

        viewModel.onInteraction(
            CharacterSheetInteraction.SheetOpened(
                CharacterSheetViewState.PersonKey(
                    membership = PersonMembership.ThisCampaign,
                    id = "pc-1",
                )
            )
        )

        val state = awaitContent(viewModel)
        assertEquals("Aelar", state.name)
        assertEquals("5E", state.systemBadge)
        assertEquals(3, state.proficiencyBonus)
        assertEquals(2, state.initiativeBonus)
        assertEquals(27, state.vitals.hitPoints)
        assertEquals(32, state.vitals.maxHitPoints)
        assertEquals(15, state.vitals.armorClass)
        val body = assertIs<CharacterSheetViewState.SheetBody.FifthEdition>(state.body)
        assertEquals("Proficiency bonus applied when marked", body.skillsCaption)
        assertEquals(18, body.skills.size)
        val perception = body.skills.first { it.name == "Perception" }
        assertTrue(perception.proficient)
        assertEquals(4, perception.modifier)
        assertEquals("Detect Magic", body.concentratingSpell)
        assertEquals(1, body.spellSlots.size)
        assertEquals(2, body.spellSlots.single().remaining)
        }
    }

    @Test
    fun deathSaveWritesGoThroughTheCampaignPerson() {
        runBlocking {
        seedCampaign()
        val viewModel = viewModel()
        viewModel.onInteraction(
            CharacterSheetInteraction.SheetOpened(
                CharacterSheetViewState.PersonKey(
                    membership = PersonMembership.ThisCampaign,
                    id = "pc-1",
                )
            )
        )
        awaitContent(viewModel)

        viewModel.onInteraction(CharacterSheetInteraction.DeathSaveSuccessesSelected(2))
        awaitContent(viewModel) { content ->
            content.vitals.fifthEdition?.deathSaves?.successes == 2
        }
        viewModel.onInteraction(CharacterSheetInteraction.DeathSaveFailuresSelected(1))

        val state = awaitContent(viewModel) { content ->
            content.vitals.fifthEdition?.deathSaves == DeathSaves(successes = 2, failures = 1)
        }
        assertEquals(2, state.vitals.fifthEdition?.deathSaves?.successes)
        assertEquals(1, state.vitals.fifthEdition?.deathSaves?.failures)
        val stored = campaignPeople.all().first { it.id == "pc-1" }.sheet as FifthEditionSheet
        assertEquals(2, stored.deathSaves.successes)
        assertEquals(1, stored.deathSaves.failures)
        }
    }

    @Test
    fun pathfinderDyingWritesToTheCampaignSheet() {
        runBlocking {
        seedPathfinder()
        val viewModel = viewModel()
        viewModel.onInteraction(
            CharacterSheetInteraction.SheetOpened(
                CharacterSheetViewState.PersonKey(
                    membership = PersonMembership.ThisCampaign,
                    id = "pf-1",
                )
            )
        )
        awaitContent(viewModel)

        viewModel.onInteraction(CharacterSheetInteraction.DyingSelected(2))
        awaitContent(viewModel) { content ->
            content.vitals.pathfinder?.dying == 2
        }
        viewModel.onInteraction(CharacterSheetInteraction.WoundedSelected(1))

        val state = awaitContent(viewModel) { content ->
            val pf = content.vitals.pathfinder
            pf != null && pf.dying == 2 && pf.wounded == 1
        }
        assertEquals("PF2E", state.systemBadge)
        assertNull(state.proficiencyBonus)
        assertEquals(2, state.vitals.pathfinder?.dying)
        assertEquals(1, state.vitals.pathfinder?.wounded)
        val stored = campaignPeople.all().first { it.id == "pf-1" }.sheet as Pathfinder2ESheet
        assertEquals(2, stored.dying)
        assertEquals(1, stored.wounded)
        }
    }

    @Test
    fun namelessCombatantShowsUnavailable() {
        runBlocking {
        val viewModel = viewModel()
        viewModel.onInteraction(CharacterSheetInteraction.UnavailableOpened)
        val state = viewModel.state.first { it is CharacterSheetViewState.Unavailable }
        assertIs<CharacterSheetViewState.Unavailable>(state)
        assertTrue(state.message.contains("no linked person"))
        }
    }

    @Test
    fun dismissReturnsToHidden() {
        runBlocking {
        seedCampaign()
        val viewModel = viewModel()
        viewModel.onInteraction(
            CharacterSheetInteraction.SheetOpened(
                CharacterSheetViewState.PersonKey(
                    membership = PersonMembership.ThisCampaign,
                    id = "pc-1",
                )
            )
        )
        awaitContent(viewModel)
        viewModel.onInteraction(CharacterSheetInteraction.SheetDismissed)
        assertIs<CharacterSheetViewState.Hidden>(viewModel.state.value)
        }
    }

    private fun viewModel(): CharacterSheetViewModel {
        return CharacterSheetViewModel(
            appScope = scope,
            observePeople = ObservePeopleForActiveContextUseCase(
                worldPersonRepository = worldPeople,
                campaignPersonRepository = campaignPeople,
                activeContextRepository = context,
            ),
            updateWorldPerson = UpdateWorldPersonUseCase(worldPeople, instant),
            updateCampaignPerson = UpdateCampaignPersonUseCase(campaignPeople, instant),
            updateDeathSaves = UpdateCampaignPersonDeathSavesUseCase(campaignPeople, instant),
            avatarFileStore = PersonAvatarFileStore(tempDir),
        )
    }

    private suspend fun seedCampaign() {
        context.setActiveWorldId("world-1")
        context.setActiveCampaignId("campaign-1")
        campaignPeople.insert(
            CampaignPerson(
                id = "pc-1",
                campaignId = "campaign-1",
                worldPersonId = null,
                kind = PersonKind.PlayerCharacter,
                name = "Aelar",
                description = "Elf wizard",
                sheet = FifthEditionSheet.empty().copy(
                    race = "Elf",
                    classLevels = listOf(
                        ClassLevel("Wizard", "Evocation", 5),
                    ),
                    abilityScores = AbilityScores(
                        strength = 10,
                        dexterity = 14,
                        constitution = 12,
                        intelligence = 18,
                        wisdom = 13,
                        charisma = 11,
                    ),
                    hitPoints = 27,
                    maxHitPoints = 32,
                    armorClass = 15,
                    skills = listOf(FifthEditionSkill("Perception", "WIS", true)),
                    spellSlots = listOf(FifthEditionSpellSlot(level = 1, maximum = 4, used = 2)),
                    concentratingSpell = "Detect Magic",
                ),
                overlayHitPoints = null,
                overlayNotes = "",
                createdAt = instant.now(),
                updatedAt = instant.now(),
            )
        )
    }

    private suspend fun seedPathfinder() {
        context.setActiveWorldId("world-1")
        context.setActiveCampaignId("campaign-1")
        campaignPeople.insert(
            CampaignPerson(
                id = "pf-1",
                campaignId = "campaign-1",
                worldPersonId = null,
                kind = PersonKind.PlayerCharacter,
                name = "Rixi",
                description = "",
                sheet = Pathfinder2ESheet.empty().copy(
                    ancestry = "Goblin",
                    className = "Rogue",
                    level = 3,
                    dying = 0,
                    wounded = 0,
                ),
                overlayHitPoints = null,
                overlayNotes = "",
                createdAt = instant.now(),
                updatedAt = instant.now(),
            )
        )
    }

    private suspend fun awaitContent(
        viewModel: CharacterSheetViewModel,
        predicate: (CharacterSheetViewState.Content) -> Boolean = { true },
    ): CharacterSheetViewState.Content {
        return withTimeout(2_000) {
            viewModel.state.first { state ->
                state is CharacterSheetViewState.Content && predicate(state)
            } as CharacterSheetViewState.Content
        }
    }
}
