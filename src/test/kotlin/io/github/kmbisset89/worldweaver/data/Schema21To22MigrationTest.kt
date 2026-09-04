package io.github.kmbisset89.worldweaver.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

internal class Schema21To22MigrationTest {
    @Test
    fun addsLevelingModeAndCurrentXpDefaults() {
        val temp = Files.createTempDirectory("ww-schema-21").toFile()
        val dbFile = temp.resolve("ww.db")
        val connection = BundledSQLiteDriver().open(dbFile.absolutePath)
        try {
            seedSchema21(connection)
            WorldWeaverMigrations.MIGRATION_21_22.migrate(connection)

            val campaigns = queryRows(
                connection,
                "SELECT `levelingMode` FROM `campaigns` WHERE `id` = 'c-1'",
            )
            assertEquals("Milestone", campaigns.single().single())

            val worldPeople = queryRows(
                connection,
                "SELECT `currentXp` FROM `world_people` WHERE `id` = 'wp-1'",
            )
            assertEquals("0", worldPeople.single().single())

            val campaignPeople = queryRows(
                connection,
                "SELECT `currentXp` FROM `campaign_people` WHERE `id` = 'cp-1'",
            )
            assertEquals("0", campaignPeople.single().single())
        } finally {
            connection.close()
            temp.deleteRecursively()
        }
    }

    private fun seedSchema21(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `campaigns` (
                `id` TEXT NOT NULL,
                `worldId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `notes` TEXT NOT NULL,
                `gameSystem` TEXT,
                `status` TEXT NOT NULL,
                `createdAtEpochMillis` INTEGER NOT NULL,
                `updatedAtEpochMillis` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `world_people` (
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `campaign_people` (
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        connection.execSQL(
            "INSERT INTO `campaigns` VALUES ('c-1', 'w-1', 'Icewind', '', '', 'FifthEdition', 'Active', 1, 1)",
        )
        connection.execSQL("INSERT INTO `world_people` VALUES ('wp-1')")
        connection.execSQL("INSERT INTO `campaign_people` VALUES ('cp-1')")
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
