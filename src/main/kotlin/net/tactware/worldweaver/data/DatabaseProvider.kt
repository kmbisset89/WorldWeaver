package net.tactware.worldweaver.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import net.tactware.worldweaver.domain.DatabaseSnapshotExporter
import net.tactware.worldweaver.domain.WorldWeaverDataDirectory
import java.io.File

internal class DatabaseProvider(
    private val dataDirectory: WorldWeaverDataDirectory,
) : DatabaseSnapshotExporter {
    val database: WorldWeaverDatabase

    init {
        dataDirectory.ensureExists()
        database = Room.databaseBuilder<WorldWeaverDatabase>(
            name = dataDirectory.databaseFile.absolutePath,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(
                WorldWeaverMigrations.MIGRATION_1_2,
                WorldWeaverMigrations.MIGRATION_2_3,
                WorldWeaverMigrations.MIGRATION_3_4,
                WorldWeaverMigrations.MIGRATION_4_5,
                WorldWeaverMigrations.MIGRATION_5_6,
                WorldWeaverMigrations.MIGRATION_6_7,
                WorldWeaverMigrations.MIGRATION_7_8,
                WorldWeaverMigrations.MIGRATION_8_9,
                WorldWeaverMigrations.MIGRATION_9_10,
                WorldWeaverMigrations.MIGRATION_10_11,
                WorldWeaverMigrations.MIGRATION_11_12,
                WorldWeaverMigrations.MIGRATION_12_13,
                WorldWeaverMigrations.MIGRATION_13_14,
                WorldWeaverMigrations.MIGRATION_14_15,
                WorldWeaverMigrations.MIGRATION_15_16,
            )
            .build()
    }

    override suspend fun exportConsistentCopy(dest: File) {
        dest.parentFile?.mkdirs()
        if (dest.exists()) {
            dest.delete()
        }
        val destPath = dest.absolutePath.replace("'", "''")
        val sourcePath = dataDirectory.databaseFile.absolutePath
        val connection = BundledSQLiteDriver().open(sourcePath)
        try {
            try {
                connection.execSQL("VACUUM INTO '$destPath'")
                if (dest.isFile && dest.length() > 0L) {
                    return
                }
            } catch (_: Exception) {
                if (dest.exists()) {
                    dest.delete()
                }
            }
            connection.execSQL("PRAGMA wal_checkpoint(FULL)")
        } finally {
            connection.close()
        }
        dataDirectory.databaseFile.copyTo(dest, overwrite = true)
    }

    override fun close() {
        database.close()
    }
}
