package net.tactware.worldweaver.ui.characters

internal sealed interface CharactersViewEffect {
    data object OpenWorlds : CharactersViewEffect
    data class OpenLore(val loreId: String) : CharactersViewEffect
    data class OpenQuest(val questId: String) : CharactersViewEffect
}
