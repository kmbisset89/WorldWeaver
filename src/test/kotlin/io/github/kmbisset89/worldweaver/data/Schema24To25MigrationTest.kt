package io.github.kmbisset89.worldweaver.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class Schema24To25MigrationTest {
    @Test
    fun addsScratchNotesAndSessionClocks() {
        val temp = Files.createTempDirectory("ww-schema-24").toFile()
        val dbFile = temp.resolve("ww.db")
        val connection = BundledSQLiteDriver().open(dbFile.absolutePath)
        try {
            seedSchema24(connection)
            WorldWeaverMigrations.MIGRATION_24_25.migrate(connection)

            connection.execSQL(
                """
                INSERT INTO `sessions`
                VALUES ('sess-1', 'camp-1', 'Tonight', 'Prep', NULL, NULL, NULL, '', 1, 1, 'Scratch')
                """.trimIndent(),
            )
            connection.execSQL(
                """
                INSERT INTO `session_clocks`
                VALUES ('clock-1', 'sess-1', 'The ritual', 6, 2, 0)
                """.trimIndent(),
            )

            val notes = queryRows(
                connection,
                "SELECT `scratchNotes` FROM `sessions` WHERE `id` = 'sess-1'",
            )
            assertEquals("Scratch", notes.single()[0])
            val clocks = queryRows(
                connection,
                "SELECT `label`, `segmentCount`, `filledCount` FROM `session_clocks` WHERE `id` = 'clock-1'",
            )
            assertEquals("The ritual", clocks.single()[0])
            assertEquals("6", clocks.single()[1])
            assertEquals("2", clocks.single()[2])
            assertTrue(tableExists(connection, "session_clocks"))
        } finally {
            connection.close()
            temp.deleteRecursively()
        }
    }

    private fun seedSchema24(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `campaigns` (
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        connection.execSQL("INSERT INTO `campaigns` VALUES ('camp-1')")
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `sessions` (
                `id` TEXT NOT NULL,
                `campaignId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `notes` TEXT NOT NULL,
                `inWorldYear` INTEGER,
                `inWorldMonthId` TEXT,
                `inWorldDay` INTEGER,
                `recap` TEXT NOT NULL,
                `createdAtEpochMillis` INTEGER NOT NULL,
                `updatedAtEpochMillis` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`campaignId`) REFERENCES `campaigns`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
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
