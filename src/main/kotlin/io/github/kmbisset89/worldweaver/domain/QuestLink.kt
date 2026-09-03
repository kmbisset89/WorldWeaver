package io.github.kmbisset89.worldweaver.domain

internal data class QuestLink(
    val id: String,
    val kind: QuestLinkKind,
    val targetId: String,
)
