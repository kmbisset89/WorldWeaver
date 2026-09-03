package io.github.kmbisset89.worldweaver.domain

internal data class DeathSaves(
    val successes: Int,
    val failures: Int,
) {
    fun withSuccesses(count: Int): DeathSaves {
        return copy(successes = count.coerceIn(0, LIMIT))
    }

    fun withFailures(count: Int): DeathSaves {
        return copy(failures = count.coerceIn(0, LIMIT))
    }

    fun isStable(): Boolean {
        return successes >= LIMIT
    }

    fun isDead(): Boolean {
        return failures >= LIMIT
    }

    companion object {
        const val LIMIT = 3

        fun none(): DeathSaves {
            return DeathSaves(successes = 0, failures = 0)
        }
    }
}
