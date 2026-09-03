package io.github.kmbisset89.worldweaver.domain

internal enum class EncounterParticipantSource {
    Nameless,
    WorldPerson,
    CampaignPerson,
    ;

    companion object {
        fun fromStorage(value: String): EncounterParticipantSource {
            return entries.firstOrNull { it.name == value } ?: Nameless
        }
    }
}
