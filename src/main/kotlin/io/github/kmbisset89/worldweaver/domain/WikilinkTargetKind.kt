package io.github.kmbisset89.worldweaver.domain

internal enum class WikilinkTargetKind(
    val searchKind: SearchKind,
    val tokenPrefix: String,
) {
    Location(SearchKind.Location, "location"),
    Lore(SearchKind.Lore, "lore"),
    Faction(SearchKind.Faction, "faction"),
    WorldPerson(SearchKind.WorldPerson, "worldperson"),
    CampaignPerson(SearchKind.CampaignPerson, "campaignperson"),
    Quest(SearchKind.Quest, "quest"),
    Session(SearchKind.Session, "session"),
    ;

    companion object {
        fun fromPrefix(prefix: String): WikilinkTargetKind? {
            return entries.firstOrNull { kind ->
                kind.tokenPrefix.equals(prefix, ignoreCase = true)
            }
        }
    }
}
