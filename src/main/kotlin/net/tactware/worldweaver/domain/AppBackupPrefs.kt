package net.tactware.worldweaver.domain

import kotlinx.serialization.Serializable

@Serializable
internal data class AppBackupPrefs(
    val activeWorldId: String? = null,
    val activeCampaignId: String? = null,
    val activeSessionId: String? = null,
    val displayName: String,
    val email: String,
    val themeMode: String,
    val themeSkin: String,
    val navExpanded: Boolean,
    val diceColorStyle: String,
)
