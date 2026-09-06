package io.github.kmbisset89.worldweaver.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class Schema23To24MigrationTest {
    @Test
    fun createsCelestialBodyTable() {
        val temp = Files.createTempDirectory("ww-schema-23").toFile()
        val dbFile = temp.resolve("ww.db")
        val connection = BundledSQLiteDriver().open(dbFile.absolutePath)
        try {
            seedSchema23(connection)
            WorldWeaverMigrations.MIGRATION_23_24.migrate(connection)

            connection.execSQL(
                """
                INSERT INTO `world_celestial_bodies`
                VALUES ('body-1', 'w-1', 'The Moon', '', 'Moon', 29, 4, 0, 1, 1)
                """.trimIndent(),
            )

            val bodies = queryRows(
                connection,
                "SELECT `name`, `periodDays`, `epochOffsetDays` FROM `world_celestial_bodies` WHERE `id` = 'body-1'",
            )
            assertEquals("The Moon", bodies.single()[0])
            assertEquals("29", bodies.single()[1])
            assertEquals("4", bodies.single()[2])
            assertTrue(tableExists(connection, "world_celestial_bodies"))
        } finally {
            connection.close()
            temp.deleteRecursively()
        }
    }

    private fun seedSchema23(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `worlds` (
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        connection.execSQL("INSERT INTO `worlds` VALUES ('w-1')")
    }

    private fun tableExists(connection: SQLiteConnection, tableName: String): Boolean {
        val rows = queryRows(
            connection,
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = '$tableName'",
        )
        return rows.isNotEmpty()
    }

    private fun queryRows(connection: SQLiteConnection, sql: String): List<List<String?>> {
        val statement = connection.prepare(sql)
        val rows = mutableListOf<List<String?>>()
        try {
            while (statement.step()) {
                val columns = statement.getColumnCount()
                rows += (0 until columns).map { index ->
                    if (statement.isNull(index)) {
                        null
                    } else {
                        statement.getText(index)
                    }
                }
            }
        } finally {
            statement.close()
        }
        return rows
    }
}
