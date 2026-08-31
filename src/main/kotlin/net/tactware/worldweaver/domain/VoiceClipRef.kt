package net.tactware.worldweaver.domain

internal sealed class VoiceClipRef {
    abstract val id: String

    data class WorldPerson(override val id: String) : VoiceClipRef()

    data class CampaignPerson(override val id: String) : VoiceClipRef()

    data class Location(override val id: String) : VoiceClipRef()
}
