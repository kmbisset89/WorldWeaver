package net.tactware.worldweaver.domain

import kotlinx.serialization.Serializable

@Serializable
internal data class AppBackupManifest(
    val formatVersion: Int,
    val appVersion: String,
    val dbSchemaVersion: Int,
    val exportedAtEpochMillis: Long,
) {
    companion object {
        const val FORMAT_VERSION = 1
        const val APP_VERSION = "1.0.0"
        const val DB_SCHEMA_VERSION = 20
    }
}
