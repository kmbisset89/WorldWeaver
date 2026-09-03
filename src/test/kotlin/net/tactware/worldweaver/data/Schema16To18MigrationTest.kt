package net.tactware.worldweaver.data

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class Schema16To18MigrationTest {
    @Test
    fun factionLeanRewritesToFactionIdAndSheetColumnsDefault() {
        val temp = Files.createTempDirectory("ww-schema-16").toFile()
        val dbFile = temp.resolve("ww.db")
        val connection = BundledSQLiteDriver().open(dbFile.absolutePath)
        try {
            seedSchema16(connection)
            WorldWeaverMigrations.MIGRATION_16_17.migrate(connection)
            WorldWeaverMigrations.MIGRATION_17_18.migrate(connection)

            val factions = queryRows(
                connection,
                "SELECT `id`, `worldId`, `name` FROM `factions` ORDER BY `name`",
            )
            assertEquals(1, factions.size)
            val expectedFactionId = "fac-$WORLD_ID-${hexUtf8("harpers")}"
            assertEquals(expectedFactionId, factions.single()[0])
            assertEquals(WORLD_ID, factions.single()[1])
            assertEquals("Harpers", factions.single()[2])

            val relationshipFactions = queryRows(
                connection,
                "SELECT `id`, `factionId` FROM `person_relationships` ORDER BY `id`",
            )
            assertEquals(expectedFactionId, relationshipFactions.first { it[0] == "rel-harpers" }[1])
            assertEquals(expectedFactionId, relationshipFactions.first { it[0] == "rel-case" }[1])
            assertNull(relationshipFactions.first { it[0] == "rel-blank" }[1])

            val worldSheets = queryRows(
                connection,
                "SELECT `sheetSystem`, `pf2ePayload` FROM `world_people` WHERE `id` = 'wp-1'",
            )
            assertEquals("FifthEdition", worldSheets.single()[0])
            assertEquals("", worldSheets.single()[1])

            val campaignSheets = queryRows(
                connection,
                "SELECT `sheetSystem`, `pf2ePayload` FROM `campaign_people` LIMIT 1",
            )
            assertTrue(campaignSheets.isEmpty() || campaignSheets.single()[0] == "FifthEdition")
        } finally {
            connection.close()
            temp.deleteRecursively()
        }
    }

    private fun seedSchema16(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `worlds` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `defaultGameSystem` TEXT NOT NULL,
                `createdAtEpochMillis` INTEGER NOT NULL,
                `updatedAtEpochMillis` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
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
                PRIMARY KEY(`id`),
                FOREIGN KEY(`worldId`) REFERENCES `worlds`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent(),
        )
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `world_people` (
                `id` TEXT NOT NULL,
                `worldId` TEXT NOT NULL,
                `kind` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `race` TEXT NOT NULL,
                `classLevels` TEXT NOT NULL,
                `abilities` TEXT NOT NULL,
                `hitPoints` INTEGER NOT NULL,
                `maxHitPoints` INTEGER NOT NULL,
                `temporaryHitPoints` INTEGER NOT NULL,
                `armorClass` INTEGER NOT NULL,
                `walkSpeed` INTEGER NOT NULL,
                `deathSaves` TEXT NOT NULL,
                `items` TEXT NOT NULL,
                `features` TEXT NOT NULL,
                `spells` TEXT NOT NULL,
                `notes` TEXT NOT NULL,
                `createdAtEpochMillis` INTEGER NOT NULL,
                `updatedAtEpochMillis` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`worldId`) REFERENCES `worlds`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `campaign_people` (
                `id` TEXT NOT NULL,
                `campaignId` TEXT NOT NULL,
                `worldPersonId` TEXT,
                `kind` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `race` TEXT NOT NULL,
                `classLevels` TEXT NOT NULL,
                `abilities` TEXT NOT NULL,
                `hitPoints` INTEGER NOT NULL,
                `maxHitPoints` INTEGER NOT NULL,
                `temporaryHitPoints` INTEGER NOT NULL,
                `armorClass` INTEGER NOT NULL,
                `walkSpeed` INTEGER NOT NULL,
                `deathSaves` TEXT NOT NULL,
                `items` TEXT NOT NULL,
                `features` TEXT NOT NULL,
                `spells` TEXT NOT NULL,
                `notes` TEXT NOT NULL,
                `overlayHitPoints` INTEGER,
                `overlayNotes` TEXT NOT NULL,
                `createdAtEpochMillis` INTEGER NOT NULL,
                `updatedAtEpochMillis` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`campaignId`) REFERENCES `campaigns`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`worldPersonId`) REFERENCES `world_people`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent(),
        )
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `person_relationships` (
                `id` TEXT NOT NULL,
                `fromKind` TEXT NOT NULL,
                `fromId` TEXT NOT NULL,
                `toKind` TEXT NOT NULL,
                `toId` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `factionLean` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        connection.execSQL(
            """
            INSERT INTO `worlds` (
                `id`, `name`, `description`, `defaultGameSystem`,
                `createdAtEpochMillis`, `updatedAtEpochMillis`
            ) VALUES ('$WORLD_ID', 'Faerun', '', 'FifthEdition', 1, 1)
            """.trimIndent(),
        )
        connection.execSQL(
            """
            INSERT INTO `world_people` (
                `id`, `worldId`, `kind`, `name`, `description`, `race`, `classLevels`, `abilities`,
                `hitPoints`, `maxHitPoints`, `temporaryHitPoints`, `armorClass`, `walkSpeed`,
                `deathSaves`, `items`, `features`, `spells`, `notes`,
                `createdAtEpochMillis`, `updatedAtEpochMillis`
            ) VALUES
            ('wp-1', '$WORLD_ID', 'Npc', 'Bram', '', 'Human', '', '10,10,10,10,10,10',
             10, 10, 0, 10, 30, '0,0', '', '', '', '', 1, 1),
            ('wp-2', '$WORLD_ID', 'Npc', 'Cora', '', 'Human', '', '10,10,10,10,10,10',
             10, 10, 0, 10, 30, '0,0', '', '', '', '', 1, 1)
            """.trimIndent(),
        )
        connection.execSQL(
            """
            INSERT INTO `person_relationships` (
                `id`, `fromKind`, `fromId`, `toKind`, `toId`, `type`, `description`, `factionLean`
            ) VALUES
            ('rel-harpers', 'World', 'wp-1', 'World', 'wp-2', 'Ally', '', 'Harpers'),
            ('rel-case', 'World', 'wp-2', 'World', 'wp-1', 'Ally', '', 'harpers'),
            ('rel-blank', 'World', 'wp-1', 'World', 'wp-2', 'Rival', '', '')
            """.trimIndent(),
        )
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

    private fun hexUtf8(value: String): String {
        return value.encodeToByteArray().joinToString(separator = "") { byte ->
            "%02X".format(byte.toInt() and 0xFF)
        }
    }

    private companion object {
        const val WORLD_ID = "world-1"
    }
}
