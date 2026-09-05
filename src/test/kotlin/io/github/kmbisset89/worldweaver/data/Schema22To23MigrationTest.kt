package io.github.kmbisset89.worldweaver.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class Schema22To23MigrationTest {
    @Test
    fun createsObservanceTables() {
        val temp = Files.createTempDirectory("ww-schema-22").toFile()
        val dbFile = temp.resolve("ww.db")
        val connection = BundledSQLiteDriver().open(dbFile.absolutePath)
        try {
            seedSchema22(connection)
            WorldWeaverMigrations.MIGRATION_22_23.migrate(connection)

            connection.execSQL(
                """
                INSERT INTO `world_calendar_observances`
                VALUES ('obs-1', 'w-1', 'Midwinter', '', 'Holiday', 'm-1', 12, NULL, 1, 1)
                """.trimIndent(),
            )
            connection.execSQL(
                "INSERT INTO `world_calendar_observance_lore` VALUES ('obs-1', 'lore-1')",
            )

            val observances = queryRows(
                connection,
                "SELECT `name`, `year` FROM `world_calendar_observances` WHERE `id` = 'obs-1'",
            )
            assertEquals("Midwinter", observances.single()[0])
            assertEquals(null, observances.single()[1])

            val links = queryRows(
                connection,
                "SELECT `loreId` FROM `world_calendar_observance_lore` WHERE `observanceId` = 'obs-1'",
            )
            assertEquals(listOf("lore-1"), links.map { it.single() })
            assertTrue(tableExists(connection, "world_calendar_observances"))
            assertTrue(tableExists(connection, "world_calendar_observance_lore"))
        } finally {
            connection.close()
            temp.deleteRecursively()
        }
    }

    private fun seedSchema22(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `worlds` (
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `lore` (
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        connection.execSQL("INSERT INTO `worlds` VALUES ('w-1')")
        connection.execSQL("INSERT INTO `lore` VALUES ('lore-1')")
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
