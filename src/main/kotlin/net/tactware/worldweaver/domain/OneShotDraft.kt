package net.tactware.worldweaver.domain

/**
 * Resolved records the one-shot wizard will persist.
 */
internal data class OneShotDraft(
    val worldName: String,
    val worldDescription: String,
    val gameSystem: GameSystem,
    val campaignName: String,
    val campaignDescription: String,
    val campaignNotes: String,
    val realmName: String,
    val realmDescription: String,
    val regionName: String,
    val regionClimate: String,
    val regionTerrain: String,
    val settlementName: String,
    val settlementDescription: String,
    val sites: List<Site>,
    val people: List<Person>,
    val faction: Faction?,
    val loreTitle: String,
    val loreContent: String,
    val loreSecretTitle: String?,
    val loreSecret: String?,
    val questTitle: String,
    val questSummary: String,
    val questObjectives: List<String>,
    val sessionName: String,
    val sessionNotes: String,
    val scenes: List<Scene>,
    val encounterName: String?,
    val encounterDifficulty: EncounterDifficulty?,
) {
    data class Site(
        val name: String,
        val description: String,
        val role: Role,
    ) {
        enum class Role {
            Opening,
            Middle,
            Climax,
        }
    }

    data class Person(
        val name: String,
        val description: String,
        val kind: PersonKind,
        val role: Role,
    ) {
        enum class Role {
            Patron,
            Villain,
            Ally,
        }
    }

    data class Faction(
        val name: String,
        val description: String,
        val goals: String,
    )

    data class Scene(
        val title: String,
        val notes: String,
    )
}
