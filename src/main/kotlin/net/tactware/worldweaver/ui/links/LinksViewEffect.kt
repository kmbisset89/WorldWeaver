package net.tactware.worldweaver.ui.links

import net.tactware.worldweaver.ui.characters.CharactersViewState

internal sealed interface LinksViewEffect {
    data object OpenWorlds : LinksViewEffect
    data class OpenPerson(val key: CharactersViewState.PersonKey) : LinksViewEffect
    data class OpenFaction(val factionId: String) : LinksViewEffect
}
