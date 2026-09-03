package net.tactware.worldweaver.data

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

internal object WorldWeaverMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `locations` (
                    `id` TEXT NOT NULL,
                    `worldId` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `parentLocationId` TEXT,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `climate` TEXT NOT NULL,
                    `terrain` TEXT NOT NULL,
                    `government` TEXT NOT NULL,
                    `landmarks` TEXT NOT NULL,
                    `history` TEXT NOT NULL,
                    `notes` TEXT NOT NULL,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`worldId`) REFERENCES `worlds`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_locations_worldId` ON `locations` (`worldId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_locations_parentLocationId` ON `locations` (`parentLocationId`)"
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `location_overlays` (
                    `campaignId` TEXT NOT NULL,
                    `locationId` TEXT NOT NULL,
                    `hasPartyPresence` INTEGER NOT NULL,
                    `notes` TEXT NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`campaignId`, `locationId`),
                    FOREIGN KEY(`campaignId`) REFERENCES `campaigns`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`locationId`) REFERENCES `locations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_location_overlays_campaignId` ON `location_overlays` (`campaignId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_location_overlays_locationId` ON `location_overlays` (`locationId`)"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `lore` (
                    `id` TEXT NOT NULL,
                    `worldId` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `content` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `tags` TEXT NOT NULL,
                    `relatedEntryIds` TEXT NOT NULL,
                    `locationId` TEXT,
                    `characterId` TEXT,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`worldId`) REFERENCES `worlds`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`locationId`) REFERENCES `locations`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_lore_worldId` ON `lore` (`worldId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_lore_locationId` ON `lore` (`locationId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_lore_category` ON `lore` (`category`)"
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `lore_secrets` (
                    `id` TEXT NOT NULL,
                    `loreId` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `secret` TEXT NOT NULL,
                    `sortIndex` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`loreId`) REFERENCES `lore`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_lore_secrets_loreId` ON `lore_secrets` (`loreId`)"
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `lore_hints` (
                    `id` TEXT NOT NULL,
                    `secretId` TEXT NOT NULL,
                    `text` TEXT NOT NULL,
                    `revealed` INTEGER NOT NULL,
                    `sortIndex` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`secretId`) REFERENCES `lore_secrets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_lore_hints_secretId` ON `lore_hints` (`secretId`)"
            )
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(connection: SQLiteConnection) {
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
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_world_people_worldId` ON `world_people` (`worldId`)"
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
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_campaign_people_campaignId` ON `campaign_people` (`campaignId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_campaign_people_worldPersonId` ON `campaign_people` (`worldPersonId`)"
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
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_person_relationships_fromId` ON `person_relationships` (`fromId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_person_relationships_toId` ON `person_relationships` (`toId`)"
            )
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `quests` (
                    `id` TEXT NOT NULL,
                    `campaignId` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `summary` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `locationId` TEXT,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`campaignId`) REFERENCES `campaigns`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`locationId`) REFERENCES `locations`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_quests_campaignId` ON `quests` (`campaignId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_quests_locationId` ON `quests` (`locationId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_quests_status` ON `quests` (`status`)"
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `quest_objectives` (
                    `id` TEXT NOT NULL,
                    `questId` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `sortIndex` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`questId`) REFERENCES `quests`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_quest_objectives_questId` ON `quest_objectives` (`questId`)"
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `quest_links` (
                    `id` TEXT NOT NULL,
                    `questId` TEXT NOT NULL,
                    `kind` TEXT NOT NULL,
                    `targetId` TEXT NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`questId`) REFERENCES `quests`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_quest_links_questId` ON `quest_links` (`questId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_quest_links_kind` ON `quest_links` (`kind`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_quest_links_targetId` ON `quest_links` (`targetId`)"
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `sessions` (
                    `id` TEXT NOT NULL,
                    `campaignId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `notes` TEXT NOT NULL,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`campaignId`) REFERENCES `campaigns`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_sessions_campaignId` ON `sessions` (`campaignId`)"
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `session_scenes` (
                    `id` TEXT NOT NULL,
                    `sessionId` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `notes` TEXT NOT NULL,
                    `sortIndex` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`sessionId`) REFERENCES `sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_session_scenes_sessionId` ON `session_scenes` (`sessionId`)"
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `session_march_entries` (
                    `id` TEXT NOT NULL,
                    `sessionId` TEXT NOT NULL,
                    `personScope` TEXT NOT NULL,
                    `personId` TEXT NOT NULL,
                    `displayName` TEXT NOT NULL,
                    `sortIndex` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`sessionId`) REFERENCES `sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_session_march_entries_sessionId` ON `session_march_entries` (`sessionId`)"
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `plot_threads` (
                    `id` TEXT NOT NULL,
                    `campaignId` TEXT NOT NULL,
                    `sessionId` TEXT,
                    `title` TEXT NOT NULL,
                    `details` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `priority` TEXT NOT NULL,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`campaignId`) REFERENCES `campaigns`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`sessionId`) REFERENCES `sessions`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_plot_threads_campaignId` ON `plot_threads` (`campaignId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_plot_threads_sessionId` ON `plot_threads` (`sessionId`)"
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `reference_docs` (
                    `id` TEXT NOT NULL,
                    `campaignId` TEXT NOT NULL,
                    `sessionId` TEXT,
                    `title` TEXT NOT NULL,
                    `pathOrUrl` TEXT NOT NULL,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`campaignId`) REFERENCES `campaigns`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`sessionId`) REFERENCES `sessions`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_reference_docs_campaignId` ON `reference_docs` (`campaignId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_reference_docs_sessionId` ON `reference_docs` (`sessionId`)"
            )
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `encounters` (
                    `id` TEXT NOT NULL,
                    `campaignId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `locationId` TEXT,
                    `difficulty` TEXT NOT NULL,
                    `notes` TEXT NOT NULL,
                    `outcomeNote` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `currentRound` INTEGER NOT NULL,
                    `currentTurnIndex` INTEGER NOT NULL,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`campaignId`) REFERENCES `campaigns`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`locationId`) REFERENCES `locations`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_encounters_campaignId` ON `encounters` (`campaignId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_encounters_locationId` ON `encounters` (`locationId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_encounters_status` ON `encounters` (`status`)"
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `encounter_participants` (
                    `id` TEXT NOT NULL,
                    `encounterId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `source` TEXT NOT NULL,
                    `sourceId` TEXT,
                    `initiativeRoll` INTEGER,
                    `initiativeBonus` INTEGER NOT NULL,
                    `armorClass` INTEGER NOT NULL,
                    `hitPoints` INTEGER NOT NULL,
                    `maxHitPoints` INTEGER NOT NULL,
                    `temporaryHitPoints` INTEGER NOT NULL,
                    `conditions` TEXT NOT NULL,
                    `groupCount` INTEGER NOT NULL,
                    `combatState` TEXT NOT NULL,
                    `sortIndex` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`encounterId`) REFERENCES `encounters`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_encounter_participants_encounterId` ON `encounter_participants` (`encounterId`)"
            )
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `person_companions` (
                    `id` TEXT NOT NULL,
                    `ownerKind` TEXT NOT NULL,
                    `ownerId` TEXT NOT NULL,
                    `companionKind` TEXT NOT NULL,
                    `companionId` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_person_companions_ownerId` ON `person_companions` (`ownerId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_person_companions_companionId` ON `person_companions` (`companionId`)"
            )
            connection.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS
                `index_person_companions_ownerKind_ownerId_companionKind_companionId`
                ON `person_companions` (`ownerKind`, `ownerId`, `companionKind`, `companionId`)
                """.trimIndent()
            )
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `battle_maps` (
                    `id` TEXT NOT NULL,
                    `campaignId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `originalWidth` INTEGER NOT NULL,
                    `originalHeight` INTEGER NOT NULL,
                    `tileSizePx` INTEGER NOT NULL,
                    `minZoom` INTEGER NOT NULL,
                    `maxZoom` INTEGER NOT NULL,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`campaignId`) REFERENCES `campaigns`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_battle_maps_campaignId` ON `battle_maps` (`campaignId`)"
            )
            connection.execSQL("PRAGMA foreign_keys=OFF")
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `encounters_new` (
                    `id` TEXT NOT NULL,
                    `campaignId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `locationId` TEXT,
                    `battleMapId` TEXT,
                    `difficulty` TEXT NOT NULL,
                    `notes` TEXT NOT NULL,
                    `outcomeNote` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `currentRound` INTEGER NOT NULL,
                    `currentTurnIndex` INTEGER NOT NULL,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`campaignId`) REFERENCES `campaigns`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`locationId`) REFERENCES `locations`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
                    FOREIGN KEY(`battleMapId`) REFERENCES `battle_maps`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent()
            )
            connection.execSQL(
                """
                INSERT INTO `encounters_new` (
                    `id`, `campaignId`, `name`, `locationId`, `battleMapId`, `difficulty`,
                    `notes`, `outcomeNote`, `status`, `currentRound`, `currentTurnIndex`,
                    `createdAtEpochMillis`, `updatedAtEpochMillis`
                )
                SELECT
                    `id`, `campaignId`, `name`, `locationId`, NULL, `difficulty`,
                    `notes`, `outcomeNote`, `status`, `currentRound`, `currentTurnIndex`,
                    `createdAtEpochMillis`, `updatedAtEpochMillis`
                FROM `encounters`
                """.trimIndent()
            )
            connection.execSQL("DROP TABLE `encounters`")
            connection.execSQL("ALTER TABLE `encounters_new` RENAME TO `encounters`")
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_encounters_campaignId` ON `encounters` (`campaignId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_encounters_locationId` ON `encounters` (`locationId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_encounters_status` ON `encounters` (`status`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_encounters_battleMapId` ON `encounters` (`battleMapId`)"
            )
            connection.execSQL("PRAGMA foreign_keys=ON")
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("PRAGMA foreign_keys=OFF")
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `battle_maps_new` (
                    `id` TEXT NOT NULL,
                    `campaignId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `originalWidth` INTEGER NOT NULL,
                    `originalHeight` INTEGER NOT NULL,
                    `tileSizePx` INTEGER NOT NULL,
                    `minZoom` INTEGER NOT NULL,
                    `maxZoom` INTEGER NOT NULL,
                    `columns` INTEGER NOT NULL,
                    `rows` INTEGER NOT NULL,
                    `unitName` TEXT NOT NULL,
                    `unitsPerTile` REAL NOT NULL,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`campaignId`) REFERENCES `campaigns`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                """
                INSERT INTO `battle_maps_new` (
                    `id`, `campaignId`, `name`, `originalWidth`, `originalHeight`,
                    `tileSizePx`, `minZoom`, `maxZoom`, `columns`, `rows`,
                    `unitName`, `unitsPerTile`, `createdAtEpochMillis`, `updatedAtEpochMillis`
                )
                SELECT
                    `id`, `campaignId`, `name`, `originalWidth`, `originalHeight`,
                    `tileSizePx`, `minZoom`, `maxZoom`, 20, 20,
                    'ft', 5.0, `createdAtEpochMillis`, `updatedAtEpochMillis`
                FROM `battle_maps`
                """.trimIndent()
            )
            connection.execSQL("DROP TABLE `battle_maps`")
            connection.execSQL("ALTER TABLE `battle_maps_new` RENAME TO `battle_maps`")
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_battle_maps_campaignId` ON `battle_maps` (`campaignId`)"
            )
            connection.execSQL("PRAGMA foreign_keys=ON")
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `battle_map_situations` (
                    `id` TEXT NOT NULL,
                    `battleMapId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `visible` INTEGER NOT NULL,
                    `sortIndex` INTEGER NOT NULL,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`battleMapId`) REFERENCES `battle_maps`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_battle_map_situations_battleMapId` ON `battle_map_situations` (`battleMapId`)"
            )
        }
    }

    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "ALTER TABLE `world_people` ADD COLUMN `walkSpeed` INTEGER NOT NULL DEFAULT 30"
            )
            connection.execSQL(
                "ALTER TABLE `campaign_people` ADD COLUMN `walkSpeed` INTEGER NOT NULL DEFAULT 30"
            )
        }
    }

    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "ALTER TABLE `encounter_participants` ADD COLUMN `gridColumn` INTEGER"
            )
            connection.execSQL(
                "ALTER TABLE `encounter_participants` ADD COLUMN `gridRow` INTEGER"
            )
        }
    }

    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "ALTER TABLE `encounter_participants` ADD COLUMN `visibleToPlayers` INTEGER NOT NULL DEFAULT 1"
            )
        }
    }

    val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "ALTER TABLE `encounter_participants` ADD COLUMN `attacksAllowed` INTEGER NOT NULL DEFAULT 1"
            )
            connection.execSQL(
                "ALTER TABLE `encounter_participants` ADD COLUMN `attacksUsed` INTEGER NOT NULL DEFAULT 0"
            )
            connection.execSQL(
                "ALTER TABLE `encounter_participants` ADD COLUMN `bonusActionUsed` INTEGER NOT NULL DEFAULT 0"
            )
            connection.execSQL(
                "ALTER TABLE `encounter_participants` ADD COLUMN `reactionUsed` INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    val MIGRATION_14_15 = object : Migration(14, 15) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "ALTER TABLE `battle_maps` ADD COLUMN `fogEnabled` INTEGER NOT NULL DEFAULT 0"
            )
            connection.execSQL(
                "ALTER TABLE `battle_maps` ADD COLUMN `revealedCells` TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    val MIGRATION_15_16 = object : Migration(15, 16) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `world_calendars` (
                    `id` TEXT NOT NULL,
                    `worldId` TEXT NOT NULL,
                    `eraSuffix` TEXT NOT NULL,
                    `currentYear` INTEGER,
                    `currentMonthId` TEXT,
                    `currentDay` INTEGER,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`worldId`) REFERENCES `worlds`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_world_calendars_worldId` ON `world_calendars` (`worldId`)"
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `world_calendar_months` (
                    `id` TEXT NOT NULL,
                    `calendarId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `days` INTEGER NOT NULL,
                    `sortIndex` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`calendarId`) REFERENCES `world_calendars`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_world_calendar_months_calendarId` ON `world_calendar_months` (`calendarId`)"
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `world_calendar_weekdays` (
                    `id` TEXT NOT NULL,
                    `calendarId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `sortIndex` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`calendarId`) REFERENCES `world_calendars`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_world_calendar_weekdays_calendarId` ON `world_calendar_weekdays` (`calendarId`)"
            )
            connection.execSQL(
                """
                INSERT INTO `world_calendars` (
                    `id`, `worldId`, `eraSuffix`, `currentYear`, `currentMonthId`, `currentDay`,
                    `createdAtEpochMillis`, `updatedAtEpochMillis`
                )
                SELECT 'cal-' || `id`, `id`, '', NULL, NULL, NULL, `createdAtEpochMillis`, `updatedAtEpochMillis`
                FROM `worlds`
                """.trimIndent()
            )
            DEFAULT_MONTHS.forEachIndexed { index, month ->
                connection.execSQL(
                    """
                    INSERT INTO `world_calendar_months` (`id`, `calendarId`, `name`, `days`, `sortIndex`)
                    SELECT 'cal-' || `id` || '-m-$index', 'cal-' || `id`, '${month.first}', ${month.second}, $index
                    FROM `worlds`
                    """.trimIndent()
                )
            }
            DEFAULT_WEEKDAYS.forEachIndexed { index, name ->
                connection.execSQL(
                    """
                    INSERT INTO `world_calendar_weekdays` (`id`, `calendarId`, `name`, `sortIndex`)
                    SELECT 'cal-' || `id` || '-w-$index', 'cal-' || `id`, '$name', $index
                    FROM `worlds`
                    """.trimIndent()
                )
            }
            connection.execSQL(
                "ALTER TABLE `sessions` ADD COLUMN `inWorldYear` INTEGER"
            )
            connection.execSQL(
                "ALTER TABLE `sessions` ADD COLUMN `inWorldMonthId` TEXT"
            )
            connection.execSQL(
                "ALTER TABLE `sessions` ADD COLUMN `inWorldDay` INTEGER"
            )
        }
    }

    val MIGRATION_16_17 = object : Migration(16, 17) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `factions` (
                    `id` TEXT NOT NULL,
                    `worldId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `goals` TEXT NOT NULL,
                    `notes` TEXT NOT NULL,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`worldId`) REFERENCES `worlds`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_factions_worldId` ON `factions` (`worldId`)"
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `faction_memberships` (
                    `id` TEXT NOT NULL,
                    `personKind` TEXT NOT NULL,
                    `personId` TEXT NOT NULL,
                    `factionId` TEXT NOT NULL,
                    `role` TEXT NOT NULL,
                    `notes` TEXT NOT NULL,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`factionId`) REFERENCES `factions`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                )
                """.trimIndent()
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_faction_memberships_factionId` ON `faction_memberships` (`factionId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_faction_memberships_personKind_personId` ON `faction_memberships` (`personKind`, `personId`)"
            )
            connection.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_faction_memberships_personKind_personId_factionId` ON `faction_memberships` (`personKind`, `personId`, `factionId`)"
            )
            connection.execSQL(
                """
                CREATE TEMP TABLE `relationship_worlds` AS
                SELECT
                    r.`id` AS `relationshipId`,
                    TRIM(r.`factionLean`) AS `lean`,
                    COALESCE(wp.`worldId`, c.`worldId`, wp2.`worldId`, c2.`worldId`) AS `worldId`
                FROM `person_relationships` r
                LEFT JOIN `world_people` wp ON r.`fromKind` = 'World' AND r.`fromId` = wp.`id`
                LEFT JOIN `campaign_people` cp ON r.`fromKind` = 'Campaign' AND r.`fromId` = cp.`id`
                LEFT JOIN `campaigns` c ON cp.`campaignId` = c.`id`
                LEFT JOIN `world_people` wp2 ON r.`toKind` = 'World' AND r.`toId` = wp2.`id`
                LEFT JOIN `campaign_people` cp2 ON r.`toKind` = 'Campaign' AND r.`toId` = cp2.`id`
                LEFT JOIN `campaigns` c2 ON cp2.`campaignId` = c2.`id`
                WHERE TRIM(r.`factionLean`) != ''
                """.trimIndent()
            )
            connection.execSQL(
                """
                INSERT INTO `factions` (
                    `id`, `worldId`, `name`, `description`, `goals`, `notes`,
                    `createdAtEpochMillis`, `updatedAtEpochMillis`
                )
                SELECT
                    'fac-' || rw.`worldId` || '-' || hex(lower(rw.`lean`)),
                    rw.`worldId`,
                    MIN(rw.`lean`),
                    '',
                    '',
                    '',
                    COALESCE(w.`createdAtEpochMillis`, 0),
                    COALESCE(w.`updatedAtEpochMillis`, 0)
                FROM `relationship_worlds` rw
                LEFT JOIN `worlds` w ON w.`id` = rw.`worldId`
                WHERE rw.`worldId` IS NOT NULL
                GROUP BY rw.`worldId`, lower(rw.`lean`)
                """.trimIndent()
            )
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `person_relationships_new` (
                    `id` TEXT NOT NULL,
                    `fromKind` TEXT NOT NULL,
                    `fromId` TEXT NOT NULL,
                    `toKind` TEXT NOT NULL,
                    `toId` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `factionId` TEXT,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`factionId`) REFERENCES `factions`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                )
                """.trimIndent()
            )
            connection.execSQL(
                """
                INSERT INTO `person_relationships_new` (
                    `id`, `fromKind`, `fromId`, `toKind`, `toId`, `type`, `description`, `factionId`
                )
                SELECT
                    r.`id`,
                    r.`fromKind`,
                    r.`fromId`,
                    r.`toKind`,
                    r.`toId`,
                    r.`type`,
                    r.`description`,
                    CASE
                        WHEN TRIM(r.`factionLean`) = '' THEN NULL
                        WHEN rw.`worldId` IS NULL THEN NULL
                        ELSE 'fac-' || rw.`worldId` || '-' || hex(lower(TRIM(r.`factionLean`)))
                    END
                FROM `person_relationships` r
                LEFT JOIN `relationship_worlds` rw ON r.`id` = rw.`relationshipId`
                """.trimIndent()
            )
            connection.execSQL("DROP TABLE `person_relationships`")
            connection.execSQL("ALTER TABLE `person_relationships_new` RENAME TO `person_relationships`")
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_person_relationships_fromId` ON `person_relationships` (`fromId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_person_relationships_toId` ON `person_relationships` (`toId`)"
            )
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_person_relationships_factionId` ON `person_relationships` (`factionId`)"
            )
            connection.execSQL("DROP TABLE `relationship_worlds`")
        }
    }

    val MIGRATION_17_18 = object : Migration(17, 18) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "ALTER TABLE `world_people` ADD COLUMN `sheetSystem` TEXT NOT NULL DEFAULT 'FifthEdition'"
            )
            connection.execSQL(
                "ALTER TABLE `world_people` ADD COLUMN `pf2ePayload` TEXT NOT NULL DEFAULT ''"
            )
            connection.execSQL(
                "ALTER TABLE `campaign_people` ADD COLUMN `sheetSystem` TEXT NOT NULL DEFAULT 'FifthEdition'"
            )
            connection.execSQL(
                "ALTER TABLE `campaign_people` ADD COLUMN `pf2ePayload` TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    val MIGRATION_18_19 = object : Migration(18, 19) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "ALTER TABLE `world_people` ADD COLUMN `skills` TEXT NOT NULL DEFAULT ''"
            )
            connection.execSQL(
                "ALTER TABLE `world_people` ADD COLUMN `spellSlots` TEXT NOT NULL DEFAULT ''"
            )
            connection.execSQL(
                "ALTER TABLE `world_people` ADD COLUMN `concentratingSpell` TEXT NOT NULL DEFAULT ''"
            )
            connection.execSQL(
                "ALTER TABLE `world_people` ADD COLUMN `creatureSize` TEXT NOT NULL DEFAULT 'Medium'"
            )
            connection.execSQL(
                "ALTER TABLE `campaign_people` ADD COLUMN `skills` TEXT NOT NULL DEFAULT ''"
            )
            connection.execSQL(
                "ALTER TABLE `campaign_people` ADD COLUMN `spellSlots` TEXT NOT NULL DEFAULT ''"
            )
            connection.execSQL(
                "ALTER TABLE `campaign_people` ADD COLUMN `concentratingSpell` TEXT NOT NULL DEFAULT ''"
            )
            connection.execSQL(
                "ALTER TABLE `campaign_people` ADD COLUMN `creatureSize` TEXT NOT NULL DEFAULT 'Medium'"
            )
            connection.execSQL(
                "ALTER TABLE `sessions` ADD COLUMN `recap` TEXT NOT NULL DEFAULT ''"
            )
            connection.execSQL(
                "ALTER TABLE `battle_maps` ADD COLUMN `blockedCells` TEXT NOT NULL DEFAULT ''"
            )
            connection.execSQL(
                "ALTER TABLE `battle_maps` ADD COLUMN `difficultCells` TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    val MIGRATION_19_20 = object : Migration(19, 20) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                "ALTER TABLE `battle_maps` ADD COLUMN `items` TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    private val DEFAULT_MONTHS = listOf(
        "January" to 31,
        "February" to 28,
        "March" to 31,
        "April" to 30,
        "May" to 31,
        "June" to 30,
        "July" to 31,
        "August" to 31,
        "September" to 30,
        "October" to 31,
        "November" to 30,
        "December" to 31,
    )
    private val DEFAULT_WEEKDAYS = listOf(
        "Sunday",
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
    )
}
