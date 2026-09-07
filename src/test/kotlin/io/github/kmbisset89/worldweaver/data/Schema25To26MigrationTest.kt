package io.github.kmbisset89.worldweaver.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class Schema25To26MigrationTest {
    @Test
    fun addsRandomTables() {
        val temp = Files.createTempDirectory("ww-schema-25").toFile()
        val dbFile = temp.resolve("ww.db")
        val connection = BundledSQLiteDriver().open(dbFile.absolutePath)
        try {
            seedSchema25(connection)
            WorldWeaverMigrations.MIGRATION_25_26.migrate(connection)

            connection.execSQL(
                """
                INSERT INTO `random_tables`
                VALUES ('tbl-1', 'world-1', 'Weather', '', 1, 1)
                """.trimIndent(),
            )
            connection.execSQL(
                """
                INSERT INTO `random_table_rows`
                VALUES ('row-1', 'tbl-1', 0, 'Clear', 2, NULL)
                """.trimIndent(),
            )

            val names = queryRows(
                connection,
                "SELECT `name` FROM `random_tables` WHERE `id` = 'tbl-1'",
            )
            assertEquals("Weather", names.single()[0])
            val rows = queryRows(
                connection,
                "SELECT `label`, `weight` FROM `random_table_rows` WHERE `id` = 'row-1'",
            )
            assertEquals("Clear", rows.single()[0])
            assertEquals("2", rows.single()[1])
            assertTrue(tableExists(connection, "random_tables"))
            assertTrue(tableExists(connection, "random_table_rows"))
        } finally {
            connection.close()
            temp.deleteRecursively()
        }
    }

    private fun seedSchema25(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `worlds` (
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        connection.execSQL("INSERT INTO `worlds` VALUES ('world-1')")
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
