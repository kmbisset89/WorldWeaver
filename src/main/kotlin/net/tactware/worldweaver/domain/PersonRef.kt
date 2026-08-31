package net.tactware.worldweaver.domain

internal sealed class PersonRef {
    abstract val id: String

    data class World(override val id: String) : PersonRef()

    data class Campaign(override val id: String) : PersonRef()
}
