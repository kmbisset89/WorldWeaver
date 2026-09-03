package io.github.kmbisset89.worldweaver.domain

internal sealed class PersonRef {
    abstract val id: String

    data class World(override val id: String) : PersonRef()

    data class Campaign(override val id: String) : PersonRef()
}
