package io.github.kmbisset89.worldweaver.domain

internal enum class SearchKind(
    val displayName: String,
) {
    World("Worlds"),
    Campaign("Campaigns"),
    Location("Locations"),
    Lore("Lore"),
    Observance("Holidays"),
    CelestialBody("Sky"),
    Faction("Factions"),
    WorldPerson("People"),
    CampaignPerson("People"),
    Quest("Quests"),
    Session("Sessions"),
}
