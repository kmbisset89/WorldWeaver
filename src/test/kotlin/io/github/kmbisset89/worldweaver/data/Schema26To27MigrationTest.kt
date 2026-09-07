package io.github.kmbisset89.worldweaver.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class Schema26To27MigrationTest {
    @Test
    fun addsAssets() {
        val temp = Files.createTempDirectory("ww-schema-26").toFile()
        val dbFile = temp.resolve("ww.db")
        val connection = BundledSQLiteDriver().open(dbFile.absolutePath)
        try {
            seedSchema26(connection)
            WorldWeaverMigrations.MIGRATION_26_27.migrate(connection)

            connection.execSQL(
                """
                INSERT INTO `assets`
                VALUES ('asset-1', 'world-1', 'Harbor sketch', 'sketch.png', 'maybe the docks', 12, 1, 1)
                """.trimIndent(),
            )

            val names = queryRows(
                connection,
                "SELECT `displayName`, `originalFileName` FROM `assets` WHERE `id` = 'asset-1'",
            )
            assertEquals("Harbor sketch", names.single()[0])
            assertEquals("sketch.png", names.single()[1])
            assertTrue(tableExists(connection, "assets"))
        } finally {
            connection.close()
            temp.deleteRecursively()
        }
    }

    private fun seedSchema26(connection: SQLiteConnection) {
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
