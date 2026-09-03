package io.github.kmbisset89.worldweaver.domain

internal enum class LoreCategory(
    val displayName: String,
) {
    History("History"),
    Myth("Myth"),
    Religion("Religion"),
    Culture("Culture"),
    Geography("Geography"),
    Magic("Magic"),
    Politics("Politics"),
    Other("Other"),
    ;

    companion object {
        fun fromStorage(value: String): LoreCategory {
            return entries.firstOrNull { it.name == value } ?: Other
        }
    }
}
