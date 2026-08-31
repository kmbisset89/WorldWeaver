package net.tactware.worldweaver.domain

internal enum class CampaignStatus {
    Active,
    Archived,
    Completed,
    ;

    companion object {
        fun fromStorage(value: String): CampaignStatus {
            return entries.firstOrNull { it.name == value } ?: Active
        }
    }
}
