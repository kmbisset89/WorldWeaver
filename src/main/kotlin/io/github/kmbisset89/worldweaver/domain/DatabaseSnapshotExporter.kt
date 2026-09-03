package io.github.kmbisset89.worldweaver.domain

import java.io.File

internal interface DatabaseSnapshotExporter {
    suspend fun exportConsistentCopy(dest: File)

    fun close()
}
