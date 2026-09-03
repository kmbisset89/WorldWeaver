package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal class InstantProvider(
    private val clock: () -> Instant = { Instant.now() },
) {
    fun now(): Instant = clock()
}
