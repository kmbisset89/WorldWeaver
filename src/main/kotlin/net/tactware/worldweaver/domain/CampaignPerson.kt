package net.tactware.worldweaver.domain

import java.time.Instant

internal data class CampaignPerson(
    val id: String,
    val campaignId: String,
    val worldPersonId: String?,
    val kind: PersonKind,
    val name: String,
    val description: String,
    val sheet: FifthEditionSheet,
    val overlayHitPoints: Int?,
    val overlayNotes: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun isWorldReference(): Boolean {
        return worldPersonId != null
    }
}
