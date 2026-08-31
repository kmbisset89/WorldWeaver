package net.tactware.worldweaver.domain

internal data class SessionDraft(
    val name: String,
    val notes: String,
    val inWorldDate: WorldDate? = null,
    val scenes: List<SessionScene>,
    val marchOrder: List<MarchOrderEntry>,
)
