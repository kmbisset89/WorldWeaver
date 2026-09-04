package io.github.kmbisset89.worldweaver.domain

internal class PersonSheetFactory {
    fun empty(system: GameSystem): PersonSheet {
        return when (system) {
            GameSystem.FifthEdition -> FifthEditionSheet.empty()
            GameSystem.Pathfinder2E -> Pathfinder2ESheet.empty()
        }
    }

    fun sanitize(sheet: PersonSheet): PersonSheet {
        return when (sheet) {
            is FifthEditionSheet -> sheet.copy(
                race = sheet.race.trim(),
                classLevels = sheet.classLevels.map { level ->
                    level.copy(
                        className = level.className.trim(),
                        subclass = level.subclass.trim(),
                        level = level.level.coerceAtLeast(1),
                    )
                }.filter { it.className.isNotEmpty() },
                notes = sheet.notes.trim(),
                currentXp = sheet.currentXp.coerceAtLeast(0),
            )
            is Pathfinder2ESheet -> sheet.copy(
                ancestry = sheet.ancestry.trim(),
                heritage = sheet.heritage.trim(),
                background = sheet.background.trim(),
                className = sheet.className.trim(),
                subclass = sheet.subclass.trim(),
                level = sheet.level.coerceAtLeast(1),
                notes = sheet.notes.trim(),
                skills = sheet.skills.map { skill ->
                    skill.copy(name = skill.name.trim())
                }.filter { it.name.isNotEmpty() },
                feats = sheet.feats.map { feat ->
                    feat.copy(
                        name = feat.name.trim(),
                        type = feat.type.trim(),
                        description = feat.description.trim(),
                    )
                }.filter { it.name.isNotEmpty() },
                spells = sheet.spells.map { spell ->
                    spell.copy(
                        name = spell.name.trim(),
                        rank = spell.rank.coerceIn(0, 10),
                    )
                }.filter { it.name.isNotEmpty() },
                dying = sheet.dying.coerceAtLeast(0),
                wounded = sheet.wounded.coerceAtLeast(0),
                currentXp = sheet.currentXp.coerceAtLeast(0),
            )
        }
    }
}
