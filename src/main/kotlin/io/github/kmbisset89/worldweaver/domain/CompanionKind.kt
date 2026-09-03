package io.github.kmbisset89.worldweaver.domain

internal enum class CompanionKind(
    val displayName: String,
) {
    Familiar("Familiar"),
    AnimalCompanion("Animal companion"),
    ;

    companion object {
        fun fromStorage(value: String): CompanionKind {
            return entries.firstOrNull { it.name == value } ?: Familiar
        }
    }
}
