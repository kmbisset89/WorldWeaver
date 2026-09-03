package io.github.kmbisset89.worldweaver.domain

internal data class ActiveContext(
    val activeWorldId: String?,
    val activeCampaignId: String?,
    val activeSessionId: String? = null,
)
