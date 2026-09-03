package io.github.kmbisset89.worldweaver.domain

internal enum class FifthEditionCondition(
    val displayName: String,
) {
    Blinded("Blinded"),
    Charmed("Charmed"),
    Deafened("Deafened"),
    Exhaustion("Exhaustion"),
    Frightened("Frightened"),
    Grappled("Grappled"),
    Incapacitated("Incapacitated"),
    Invisible("Invisible"),
    Paralyzed("Paralyzed"),
    Petrified("Petrified"),
    Poisoned("Poisoned"),
    Prone("Prone"),
    Restrained("Restrained"),
    Stunned("Stunned"),
    Unconscious("Unconscious"),
    ;

    companion object {
        fun fromDisplayName(value: String): FifthEditionCondition? {
            val trimmed = value.trim()
            return entries.firstOrNull { condition ->
                condition.displayName.equals(trimmed, ignoreCase = true) ||
                    condition.name.equals(trimmed, ignoreCase = true)
            }
        }
    }
}
