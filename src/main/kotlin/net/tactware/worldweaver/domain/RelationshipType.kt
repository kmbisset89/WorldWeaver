package net.tactware.worldweaver.domain

internal enum class RelationshipType(
    val displayName: String,
) {
    Parent("Parent"),
    Child("Child"),
    Sibling("Sibling"),
    Spouse("Spouse"),
    Ancestor("Ancestor"),
    Descendant("Descendant"),
    Mentor("Mentor"),
    Student("Student"),
    Ally("Ally"),
    Rival("Rival"),
    Enemy("Enemy"),
    Other("Other"),
    ;

    companion object {
        fun fromStorage(value: String): RelationshipType {
            return entries.firstOrNull { it.name == value } ?: Other
        }
    }
}
