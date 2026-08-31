package net.tactware.worldweaver.domain

internal data class QuestLink(
    val id: String,
    val kind: QuestLinkKind,
    val targetId: String,
)
