package net.tactware.worldweaver.domain

internal data class CampaignPersonDraft(
    val kind: PersonKind,
    val name: String,
    val description: String,
    val sheet: PersonSheet,
    val overlayHitPoints: Int?,
    val overlayNotes: String,
)
