package net.tactware.worldweaver.domain

internal enum class SearchKind(
    val displayName: String,
) {
    World("Worlds"),
    Campaign("Campaigns"),
    Location("Locations"),
    Lore("Lore"),
    WorldPerson("People"),
    CampaignPerson("People"),
    Quest("Quests"),
    Session("Sessions"),
}
