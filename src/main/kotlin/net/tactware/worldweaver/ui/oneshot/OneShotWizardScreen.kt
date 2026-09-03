package net.tactware.worldweaver.ui.oneshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.domain.EncounterDifficulty
import net.tactware.worldweaver.domain.GameSystem
import net.tactware.worldweaver.domain.PersonKind
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary

@Composable
internal fun OneShotWizardScreen(
    viewState: OneShotWizardViewState,
    onInteraction: (OneShotWizardInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(OneShotWizardInteraction.ScreenStarted)
    }
    val content = viewState as? OneShotWizardViewState.Content ?: return
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "One-shot wizard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Text(
            text = "${content.step.title()} · Step ${content.step.index()} of ${OneShotWizardViewState.Step.entries.size}",
            fontSize = 14.sp,
            color = TextSecondary,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (content.step) {
                OneShotWizardViewState.Step.Identity -> IdentityStep(
                    state = content,
                    onInteraction = onInteraction,
                )
                OneShotWizardViewState.Step.Hook -> HookStep(
                    state = content,
                    onInteraction = onInteraction,
                )
                OneShotWizardViewState.Step.Places -> PlacesStep(
                    state = content,
                    onInteraction = onInteraction,
                )
                OneShotWizardViewState.Step.People -> PeopleStep(
                    state = content,
                    onInteraction = onInteraction,
                )
                OneShotWizardViewState.Step.Conflict -> ConflictStep(
                    state = content,
                    onInteraction = onInteraction,
                )
                OneShotWizardViewState.Step.TablePlan -> TablePlanStep(
                    state = content,
                    onInteraction = onInteraction,
                )
                OneShotWizardViewState.Step.Review -> ReviewStep(state = content)
            }
            content.saveError?.let { error ->
                Text(text = error, color = TextSecondary)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { onInteraction(OneShotWizardInteraction.Dismissed) },
                enabled = !content.isSaving,
            ) {
                Text("Cancel")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!content.step.isFirst()) {
                    TextButton(
                        onClick = { onInteraction(OneShotWizardInteraction.BackSelected) },
                        enabled = !content.isSaving,
                    ) {
                        Text("Back")
                    }
                }
                if (content.step.isLast()) {
                    Button(
                        onClick = { onInteraction(OneShotWizardInteraction.Saved) },
                        enabled = !content.isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                    ) {
                        Text(if (content.isSaving) "Creating…" else "Create")
                    }
                } else {
                    Button(
                        onClick = { onInteraction(OneShotWizardInteraction.NextSelected) },
                        enabled = !content.isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                    ) {
                        Text("Next")
                    }
                }
            }
        }
    }
}

@Composable
private fun IdentityStep(
    state: OneShotWizardViewState.Content,
    onInteraction: (OneShotWizardInteraction) -> Unit,
) {
    val answers = state.answers
    OutlinedTextField(
        value = answers.worldName,
        onValueChange = { onInteraction(OneShotWizardInteraction.WorldNameChanged(it)) },
        label = { Text("World name") },
        isError = state.worldNameError != null,
        supportingText = state.worldNameError?.let { error -> { Text(error) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = answers.campaignName,
        onValueChange = { onInteraction(OneShotWizardInteraction.CampaignNameChanged(it)) },
        label = { Text("One-shot title") },
        isError = state.campaignNameError != null,
        supportingText = state.campaignNameError?.let { error -> { Text(error) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Text("Game system", color = TextSecondary)
    ChipRow(
        chips = GameSystem.entries.map { system ->
            OneShotWizardViewState.Chip(system.name, system.displayName)
        },
        selectedId = answers.gameSystem.name,
        onSelected = { id ->
            GameSystem.entries.firstOrNull { it.name == id }?.let { system ->
                onInteraction(OneShotWizardInteraction.GameSystemSelected(system))
            }
        },
    )
    Text("Genre", color = TextSecondary)
    ChipRow(
        chips = state.genres,
        selectedId = answers.genreId,
        onSelected = { onInteraction(OneShotWizardInteraction.GenreSelected(it)) },
    )
    Text("Tone", color = TextSecondary)
    ChipRow(
        chips = state.tones,
        selectedId = answers.toneId,
        onSelected = { onInteraction(OneShotWizardInteraction.ToneSelected(it)) },
    )
    OutlinedTextField(
        value = answers.logline,
        onValueChange = { onInteraction(OneShotWizardInteraction.LoglineChanged(it)) },
        label = { Text("Logline") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun HookStep(
    state: OneShotWizardViewState.Content,
    onInteraction: (OneShotWizardInteraction) -> Unit,
) {
    Text("What starts the night?", color = TextSecondary)
    ChipRow(
        chips = state.hooks,
        selectedId = state.answers.hookId,
        onSelected = { onInteraction(OneShotWizardInteraction.HookSelected(it)) },
    )
    OutlinedTextField(
        value = state.answers.hook,
        onValueChange = { onInteraction(OneShotWizardInteraction.HookChanged(it)) },
        label = { Text("Hook") },
        minLines = 3,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PlacesStep(
    state: OneShotWizardViewState.Content,
    onInteraction: (OneShotWizardInteraction) -> Unit,
) {
    val answers = state.answers
    OutlinedTextField(
        value = answers.realmName,
        onValueChange = { onInteraction(OneShotWizardInteraction.RealmNameChanged(it)) },
        label = { Text("Realm") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = answers.regionName,
        onValueChange = { onInteraction(OneShotWizardInteraction.RegionNameChanged(it)) },
        label = { Text("Region") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = answers.settlementName,
        onValueChange = { onInteraction(OneShotWizardInteraction.SettlementNameChanged(it)) },
        label = { Text("Settlement") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Text("Adventure site type", color = TextSecondary)
    ChipRow(
        chips = state.siteTypes,
        selectedId = answers.siteTypeId,
        onSelected = { onInteraction(OneShotWizardInteraction.SiteTypeSelected(it)) },
    )
    if (state.siteNameError != null) {
        Text(text = state.siteNameError, color = TextSecondary)
    }
    SiteFields(
        label = "Opening site",
        name = answers.openingSiteName,
        description = answers.openingSiteDescription,
        onNameChanged = { onInteraction(OneShotWizardInteraction.OpeningSiteNameChanged(it)) },
        onDescriptionChanged = {
            onInteraction(OneShotWizardInteraction.OpeningSiteDescriptionChanged(it))
        },
    )
    SiteFields(
        label = "Middle site",
        name = answers.middleSiteName,
        description = answers.middleSiteDescription,
        onNameChanged = { onInteraction(OneShotWizardInteraction.MiddleSiteNameChanged(it)) },
        onDescriptionChanged = {
            onInteraction(OneShotWizardInteraction.MiddleSiteDescriptionChanged(it))
        },
    )
    SiteFields(
        label = "Climax site",
        name = answers.climaxSiteName,
        description = answers.climaxSiteDescription,
        onNameChanged = { onInteraction(OneShotWizardInteraction.ClimaxSiteNameChanged(it)) },
        onDescriptionChanged = {
            onInteraction(OneShotWizardInteraction.ClimaxSiteDescriptionChanged(it))
        },
    )
}

@Composable
private fun PeopleStep(
    state: OneShotWizardViewState.Content,
    onInteraction: (OneShotWizardInteraction) -> Unit,
) {
    val answers = state.answers
    OutlinedTextField(
        value = answers.patronName,
        onValueChange = { onInteraction(OneShotWizardInteraction.PatronNameChanged(it)) },
        label = { Text("Patron") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = answers.patronDescription,
        onValueChange = { onInteraction(OneShotWizardInteraction.PatronDescriptionChanged(it)) },
        label = { Text("Patron one-liner") },
        modifier = Modifier.fillMaxWidth(),
    )
    Text("Villain type", color = TextSecondary)
    ChipRow(
        chips = state.villainTypes,
        selectedId = answers.villainTypeId,
        onSelected = { onInteraction(OneShotWizardInteraction.VillainTypeSelected(it)) },
    )
    Text("Villain kind", color = TextSecondary)
    ChipRow(
        chips = listOf(PersonKind.Npc, PersonKind.Monster).map { kind ->
            OneShotWizardViewState.Chip(kind.name, kind.displayName)
        },
        selectedId = answers.villainKind.name,
        onSelected = { id ->
            PersonKind.entries.firstOrNull { it.name == id }?.let { kind ->
                onInteraction(OneShotWizardInteraction.VillainKindSelected(kind))
            }
        },
    )
    OutlinedTextField(
        value = answers.villainName,
        onValueChange = { onInteraction(OneShotWizardInteraction.VillainNameChanged(it)) },
        label = { Text("Villain") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = answers.villainDescription,
        onValueChange = { onInteraction(OneShotWizardInteraction.VillainDescriptionChanged(it)) },
        label = { Text("Villain one-liner") },
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = answers.allyName,
        onValueChange = { onInteraction(OneShotWizardInteraction.AllyNameChanged(it)) },
        label = { Text("Ally (optional)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = answers.allyDescription,
        onValueChange = { onInteraction(OneShotWizardInteraction.AllyDescriptionChanged(it)) },
        label = { Text("Ally one-liner") },
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = answers.factionName,
        onValueChange = { onInteraction(OneShotWizardInteraction.FactionNameChanged(it)) },
        label = { Text("Antagonist faction (optional)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = answers.factionDescription,
        onValueChange = { onInteraction(OneShotWizardInteraction.FactionDescriptionChanged(it)) },
        label = { Text("Faction description") },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ConflictStep(
    state: OneShotWizardViewState.Content,
    onInteraction: (OneShotWizardInteraction) -> Unit,
) {
    val answers = state.answers
    OutlinedTextField(
        value = answers.questTitle,
        onValueChange = { onInteraction(OneShotWizardInteraction.QuestTitleChanged(it)) },
        label = { Text("Quest title") },
        isError = state.questTitleError != null,
        supportingText = state.questTitleError?.let { error -> { Text(error) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = answers.stakes,
        onValueChange = { onInteraction(OneShotWizardInteraction.StakesChanged(it)) },
        label = { Text("Stakes") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth(),
    )
    Text("Objectives", color = TextSecondary)
    ChipRow(
        chips = state.objectives,
        selectedId = null,
        onSelected = { onInteraction(OneShotWizardInteraction.ObjectiveChipSelected(it)) },
    )
    ObjectiveField(0, answers.objective1, onInteraction)
    ObjectiveField(1, answers.objective2, onInteraction)
    ObjectiveField(2, answers.objective3, onInteraction)
    ObjectiveField(3, answers.objective4, onInteraction)
    OutlinedTextField(
        value = answers.twist,
        onValueChange = { onInteraction(OneShotWizardInteraction.TwistChanged(it)) },
        label = { Text("Twist (optional lore secret)") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun TablePlanStep(
    state: OneShotWizardViewState.Content,
    onInteraction: (OneShotWizardInteraction) -> Unit,
) {
    val answers = state.answers
    OutlinedTextField(
        value = answers.sessionName,
        onValueChange = { onInteraction(OneShotWizardInteraction.SessionNameChanged(it)) },
        label = { Text("Session name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = answers.openingSceneTitle,
        onValueChange = { onInteraction(OneShotWizardInteraction.OpeningSceneTitleChanged(it)) },
        label = { Text("Opening scene") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = answers.middleSceneTitle,
        onValueChange = { onInteraction(OneShotWizardInteraction.MiddleSceneTitleChanged(it)) },
        label = { Text("Middle scene") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = answers.climaxSceneTitle,
        onValueChange = { onInteraction(OneShotWizardInteraction.ClimaxSceneTitleChanged(it)) },
        label = { Text("Climax scene") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = answers.includeEncounter,
            onCheckedChange = { onInteraction(OneShotWizardInteraction.IncludeEncounterChanged(it)) },
        )
        Text("Add a climax encounter")
    }
    if (answers.includeEncounter) {
        OutlinedTextField(
            value = answers.encounterName,
            onValueChange = { onInteraction(OneShotWizardInteraction.EncounterNameChanged(it)) },
            label = { Text("Encounter name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Difficulty", color = TextSecondary)
        ChipRow(
            chips = EncounterDifficulty.entries.filter { it != EncounterDifficulty.Other }.map { difficulty ->
                OneShotWizardViewState.Chip(difficulty.name, difficulty.displayName)
            },
            selectedId = answers.encounterDifficulty.name,
            onSelected = { id ->
                EncounterDifficulty.entries.firstOrNull { it.name == id }?.let { difficulty ->
                    onInteraction(OneShotWizardInteraction.EncounterDifficultySelected(difficulty))
                }
            },
        )
    }
}

@Composable
private fun ReviewStep(state: OneShotWizardViewState.Content) {
    Text("This one-shot will create:", color = TextSecondary)
    state.reviewItems.forEach { item ->
        Text(text = "• $item", color = TextPrimary)
    }
}

@Composable
private fun SiteFields(
    label: String,
    name: String,
    description: String,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChanged,
        label = { Text("$label name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChanged,
        label = { Text("$label description") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ObjectiveField(
    index: Int,
    value: String,
    onInteraction: (OneShotWizardInteraction) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onInteraction(OneShotWizardInteraction.ObjectiveChanged(index, it)) },
        label = { Text("Objective ${index + 1}") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(
    chips: List<OneShotWizardViewState.Chip>,
    selectedId: String?,
    onSelected: (String) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        chips.forEach { chip ->
            FilterChip(
                selected = chip.id == selectedId,
                onClick = { onSelected(chip.id) },
                label = { Text(chip.label) },
            )
        }
    }
}
