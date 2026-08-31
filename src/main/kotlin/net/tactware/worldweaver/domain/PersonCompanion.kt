package net.tactware.worldweaver.domain

internal data class PersonCompanion(
    val id: String,
    val owner: PersonRef,
    val companion: PersonRef,
    val kind: CompanionKind,
)
