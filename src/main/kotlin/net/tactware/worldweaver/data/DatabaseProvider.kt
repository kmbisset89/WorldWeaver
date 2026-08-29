package net.tactware.worldweaver.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import java.io.File

internal class DatabaseProvider {
    val database: WorldWeaverDatabase

    init {
        val databaseDir = File(System.getProperty("user.home"), ".worldweaver")
        if (!databaseDir.exists()) {
            databaseDir.mkdirs()
        }
        val databasePath = File(databaseDir, "ww.db").absolutePath
        database = Room.databaseBuilder<WorldWeaverDatabase>(
            name = databasePath,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}
