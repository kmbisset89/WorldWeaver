package io.github.kmbisset89.worldweaver.domain

internal enum class SearchKind(
    val displayName: String,
) {
    World("Worlds"),
    Campaign("Campaigns"),
    Location("Locations"),
    Lore("Lore"),
    Observance("Holidays"),
    Faction("Factions"),
    WorldPerson("People"),
    CampaignPerson("People"),
    Quest("Quests"),
    Session("Sessions"),
}
