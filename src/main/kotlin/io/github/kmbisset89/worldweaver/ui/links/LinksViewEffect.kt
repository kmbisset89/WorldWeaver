package io.github.kmbisset89.worldweaver.ui.links

import io.github.kmbisset89.worldweaver.ui.characters.CharactersViewState

internal sealed interface LinksViewEffect {
    data object OpenWorlds : LinksViewEffect
    data class OpenPerson(val key: CharactersViewState.PersonKey) : LinksViewEffect
    data class OpenFaction(val factionId: String) : LinksViewEffect
}
