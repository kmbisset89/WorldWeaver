package net.tactware.worldweaver.domain

import java.util.UUID

internal class EntityIdFactory(
    private val nextId: () -> String = { UUID.randomUUID().toString() },
) {
    fun create(): String = nextId()
}
