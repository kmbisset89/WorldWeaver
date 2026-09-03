package io.github.kmbisset89.worldweaver.ui.run

import io.github.kmbisset89.worldweaver.ui.characters.PersonMembership

internal sealed interface RunInteraction {
    data object ScreenStarted : RunInteraction
    data object RetrySelected : RunInteraction
    data object CreateWorldSelected : RunInteraction
    data object CreateCampaignSelected : RunInteraction
    data object OpenSessionsSelected : RunInteraction
    data object OpenEncountersSelected : RunInteraction
    data object OpenMapsSelected : RunInteraction
    data object PlayerViewSelected : RunInteraction
    data object DiceTraySelected : RunInteraction
    data class PersonPeeked(
        val membership: PersonMembership,
        val personId: String,
    ) : RunInteraction
    data class WhyItMattersChanged(val value: String) : RunInteraction
    data object CloseSessionSelected : RunInteraction
}
