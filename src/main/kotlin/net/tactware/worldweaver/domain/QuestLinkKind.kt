package net.tactware.worldweaver.domain

internal enum class QuestLinkKind {
    LORE,
    WORLD_PERSON,
    CAMPAIGN_PERSON,
    SESSION,
    ;

    companion object {
        fun fromStorage(value: String): QuestLinkKind {
            return entries.firstOrNull { it.name == value } ?: LORE
        }
    }
}
