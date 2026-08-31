package net.tactware.worldweaver.domain

internal enum class PersonKind(
    val displayName: String,
) {
    Npc("NPC"),
    Monster("Monster"),
    PlayerCharacter("PC"),
    ;

    companion object {
        fun fromStorage(value: String): PersonKind {
            return entries.firstOrNull { it.name == value } ?: Npc
        }
    }
}
