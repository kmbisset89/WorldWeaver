package io.github.kmbisset89.worldweaver.domain

/**
 * Builds a [OneShotDraft] from wizard answers and applies chip templates to empty fields only.
 */
internal class OneShotDraftFactory(
    private val catalog: OneShotTemplateCatalog = OneShotTemplateCatalog(),
) {
    fun applyGenre(answers: OneShotAnswers, genreId: String): OneShotAnswers {
        val genre = catalog.genre(genreId) ?: return answers.copy(genreId = genreId)
        return answers.copy(
            genreId = genreId,
            logline = fillIfBlank(answers.logline, genre.logline),
            realmName = fillIfBlank(answers.realmName, genre.realmName),
            regionName = fillIfBlank(answers.regionName, genre.regionName),
            settlementName = fillIfBlank(answers.settlementName, genre.settlementName),
        )
    }

    fun applyTone(answers: OneShotAnswers, toneId: String): OneShotAnswers {
        return answers.copy(toneId = toneId)
    }

    fun applyHook(answers: OneShotAnswers, hookId: String): OneShotAnswers {
        val hook = catalog.hook(hookId) ?: return answers.copy(hookId = hookId)
        return answers.copy(
            hookId = hookId,
            hook = fillIfBlank(answers.hook, hook.text),
            questTitle = fillIfBlank(answers.questTitle, hook.questTitle),
            stakes = fillIfBlank(answers.stakes, hook.stakes),
            objective1 = fillIfBlank(answers.objective1, hook.objective),
        )
    }

    fun applySiteType(answers: OneShotAnswers, siteTypeId: String): OneShotAnswers {
        val site = catalog.siteType(siteTypeId) ?: return answers.copy(siteTypeId = siteTypeId)
        return answers.copy(
            siteTypeId = siteTypeId,
            openingSiteName = fillIfBlank(answers.openingSiteName, site.openingName),
            openingSiteDescription = fillIfBlank(answers.openingSiteDescription, site.openingDescription),
            middleSiteName = fillIfBlank(answers.middleSiteName, site.middleName),
            middleSiteDescription = fillIfBlank(answers.middleSiteDescription, site.middleDescription),
            climaxSiteName = fillIfBlank(answers.climaxSiteName, site.climaxName),
            climaxSiteDescription = fillIfBlank(answers.climaxSiteDescription, site.climaxDescription),
        )
    }

    fun applyVillainType(answers: OneShotAnswers, villainTypeId: String): OneShotAnswers {
        val villain = catalog.villainType(villainTypeId)
            ?: return answers.copy(villainTypeId = villainTypeId)
        return answers.copy(
            villainTypeId = villainTypeId,
            villainName = fillIfBlank(answers.villainName, villain.name),
            villainDescription = fillIfBlank(answers.villainDescription, villain.description),
            villainKind = if (answers.villainName.isBlank()) {
                villain.kind
            } else {
                answers.villainKind
            },
            factionName = fillIfBlank(answers.factionName, villain.factionName),
            factionDescription = fillIfBlank(answers.factionDescription, villain.factionDescription),
        )
    }

    fun applyObjectiveChip(answers: OneShotAnswers, objectiveId: String): OneShotAnswers {
        val objective = catalog.objective(objectiveId) ?: return answers
        val label = objective.label
        val slots = listOf(
            answers.objective1,
            answers.objective2,
            answers.objective3,
            answers.objective4,
        )
        if (slots.any { it.trim().equals(label, ignoreCase = true) }) {
            return answers
        }
        val index = slots.indexOfFirst { it.isBlank() }
        if (index < 0) {
            return answers
        }
        return when (index) {
            0 -> answers.copy(objective1 = label)
            1 -> answers.copy(objective2 = label)
            2 -> answers.copy(objective3 = label)
            else -> answers.copy(objective4 = label)
        }
    }

    fun create(answers: OneShotAnswers): OneShotDraft {
        val genre = answers.genreId?.let { catalog.genre(it) }
        val tone = answers.toneId?.let { catalog.tone(it) }
        val hook = answers.hookId?.let { catalog.hook(it) }
        val siteType = answers.siteTypeId?.let { catalog.siteType(it) }
        val worldName = answers.worldName.trim()
        val campaignName = answers.campaignName.trim()
        val realmName = answers.realmName.trim()
            .ifBlank { genre?.realmName.orEmpty() }
            .ifBlank { derivedRealmName(worldName) }
        val regionName = answers.regionName.trim()
            .ifBlank { genre?.regionName.orEmpty() }
            .ifBlank { derivedRegionName(worldName) }
        val settlementName = answers.settlementName.trim()
            .ifBlank { genre?.settlementName.orEmpty() }
            .ifBlank { "Crossroads" }
        val sites = resolveSites(answers, siteType)
        val people = resolvePeople(answers)
        val factionName = answers.factionName.trim()
        val faction = if (factionName.isEmpty()) {
            null
        } else {
            OneShotDraft.Faction(
                name = factionName,
                description = answers.factionDescription.trim(),
                goals = answers.stakes.trim(),
            )
        }
        val hookText = answers.hook.trim().ifBlank { hook?.text.orEmpty() }
        val logline = answers.logline.trim().ifBlank { genre?.logline.orEmpty() }
        val worldDescription = joinSections(
            logline,
            genre?.worldDescription.orEmpty(),
            tone?.flavor.orEmpty(),
        )
        val questTitle = answers.questTitle.trim().ifBlank { hook?.questTitle.orEmpty() }
        val stakes = answers.stakes.trim().ifBlank { hook?.stakes.orEmpty() }
        val objectives = listOf(
            answers.objective1,
            answers.objective2,
            answers.objective3,
            answers.objective4,
        ).map { it.trim() }.filter { it.isNotEmpty() }
        val twist = answers.twist.trim()
        val sessionName = answers.sessionName.trim()
            .ifBlank { campaignName }
            .ifBlank { "Session 1" }
        val scenes = resolveScenes(answers, sites)
        val encounterName = resolveEncounterName(answers)
        return OneShotDraft(
            worldName = worldName,
            worldDescription = worldDescription,
            gameSystem = answers.gameSystem,
            campaignName = campaignName,
            campaignDescription = hookText,
            campaignNotes = stakes,
            realmName = realmName,
            realmDescription = genre?.worldDescription.orEmpty(),
            regionName = regionName,
            regionClimate = genre?.climate.orEmpty(),
            regionTerrain = genre?.terrain.orEmpty(),
            settlementName = settlementName,
            settlementDescription = "",
            sites = sites,
            people = people,
            faction = faction,
            loreTitle = if (campaignName.isEmpty()) "The premise" else "$campaignName — Premise",
            loreContent = joinSections(logline, hookText, stakes),
            loreSecretTitle = if (twist.isEmpty()) null else "The twist",
            loreSecret = twist.ifEmpty { null },
            questTitle = questTitle,
            questSummary = joinSections(hookText, stakes),
            questObjectives = objectives,
            sessionName = sessionName,
            sessionNotes = joinSections(logline, hookText),
            scenes = scenes,
            encounterName = encounterName,
            encounterDifficulty = encounterName?.let { answers.encounterDifficulty },
        )
    }

    private fun resolveSites(
        answers: OneShotAnswers,
        siteType: OneShotTemplateCatalog.SiteType?,
    ): List<OneShotDraft.Site> {
        val opening = namedSite(
            name = answers.openingSiteName.trim().ifBlank { siteType?.openingName.orEmpty() },
            description = answers.openingSiteDescription.trim()
                .ifBlank { siteType?.openingDescription.orEmpty() },
            role = OneShotDraft.Site.Role.Opening,
        )
        val middle = namedSite(
            name = answers.middleSiteName.trim(),
            description = answers.middleSiteDescription.trim(),
            role = OneShotDraft.Site.Role.Middle,
        )
        val climax = namedSite(
            name = answers.climaxSiteName.trim(),
            description = answers.climaxSiteDescription.trim(),
            role = OneShotDraft.Site.Role.Climax,
        )
        val named = listOfNotNull(opening, middle, climax)
        if (named.isNotEmpty()) {
            return named
        }
        return listOf(
            OneShotDraft.Site(
                name = "The Gathering Place",
                description = "",
                role = OneShotDraft.Site.Role.Opening,
            ),
        )
    }

    private fun namedSite(
        name: String,
        description: String,
        role: OneShotDraft.Site.Role,
    ): OneShotDraft.Site? {
        if (name.isEmpty()) {
            return null
        }
        return OneShotDraft.Site(
            name = name,
            description = description,
            role = role,
        )
    }

    private fun resolvePeople(answers: OneShotAnswers): List<OneShotDraft.Person> {
        val people = mutableListOf<OneShotDraft.Person>()
        val patron = answers.patronName.trim()
        if (patron.isNotEmpty()) {
            people += OneShotDraft.Person(
                name = patron,
                description = answers.patronDescription.trim(),
                kind = PersonKind.Npc,
                role = OneShotDraft.Person.Role.Patron,
            )
        }
        val villain = answers.villainName.trim()
        if (villain.isNotEmpty()) {
            people += OneShotDraft.Person(
                name = villain,
                description = answers.villainDescription.trim(),
                kind = answers.villainKind,
                role = OneShotDraft.Person.Role.Villain,
            )
        }
        val ally = answers.allyName.trim()
        if (ally.isNotEmpty()) {
            people += OneShotDraft.Person(
                name = ally,
                description = answers.allyDescription.trim(),
                kind = PersonKind.Npc,
                role = OneShotDraft.Person.Role.Ally,
            )
        }
        return people
    }

    private fun resolveScenes(
        answers: OneShotAnswers,
        sites: List<OneShotDraft.Site>,
    ): List<OneShotDraft.Scene> {
        val openingSite = sites.firstOrNull { it.role == OneShotDraft.Site.Role.Opening }
        val middleSite = sites.firstOrNull { it.role == OneShotDraft.Site.Role.Middle }
        val climaxSite = sites.firstOrNull { it.role == OneShotDraft.Site.Role.Climax }
            ?: sites.lastOrNull()
        val openingTitle = answers.openingSceneTitle.trim()
            .ifBlank { openingSite?.name.orEmpty() }
            .ifBlank { "Opening" }
        val middleTitle = answers.middleSceneTitle.trim()
            .ifBlank { middleSite?.name.orEmpty() }
            .ifBlank { "The turning point" }
        val climaxTitle = answers.climaxSceneTitle.trim()
            .ifBlank { climaxSite?.name.orEmpty() }
            .ifBlank { "Climax" }
        return listOf(
            OneShotDraft.Scene(title = openingTitle, notes = openingSite?.description.orEmpty()),
            OneShotDraft.Scene(title = middleTitle, notes = middleSite?.description.orEmpty()),
            OneShotDraft.Scene(title = climaxTitle, notes = climaxSite?.description.orEmpty()),
        )
    }

    private fun resolveEncounterName(answers: OneShotAnswers): String? {
        val named = answers.encounterName.trim()
        if (named.isNotEmpty()) {
            return named
        }
        if (!answers.includeEncounter) {
            return null
        }
        return "The confrontation"
    }

    private fun derivedRealmName(worldName: String): String {
        val name = worldName.ifBlank { "Known" }
        return "The $name Lands"
    }

    private fun derivedRegionName(worldName: String): String {
        val name = worldName.ifBlank { "Border" }
        return "$name Marches"
    }

    private fun fillIfBlank(current: String, candidate: String): String {
        return if (current.isBlank()) candidate else current
    }

    private fun joinSections(vararg sections: String): String {
        return sections.map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n\n")
    }
}
