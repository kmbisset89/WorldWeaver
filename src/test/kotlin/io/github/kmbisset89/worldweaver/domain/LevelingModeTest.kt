package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals

internal class LevelingModeTest {
    @Test
    fun fromStorageReadsKnownNames() {
        assertEquals(LevelingMode.Milestone, LevelingMode.fromStorage("Milestone"))
        assertEquals(LevelingMode.Experience, LevelingMode.fromStorage("Experience"))
    }

    @Test
    fun fromStorageFallsBackToMilestone() {
        assertEquals(LevelingMode.Milestone, LevelingMode.fromStorage("unknown"))
        assertEquals(LevelingMode.Milestone, LevelingMode.fromStorage(""))
    }
}
