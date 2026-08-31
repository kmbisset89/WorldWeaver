package net.tactware.worldweaver.domain

internal enum class CombatState(
    val displayName: String,
) {
    Conscious("Conscious"),
    Downed("Downed"),
    Dead("Dead"),
    ;

    companion object {
        fun fromStorage(value: String): CombatState {
            return entries.firstOrNull { it.name == value } ?: Conscious
        }
    }
}
