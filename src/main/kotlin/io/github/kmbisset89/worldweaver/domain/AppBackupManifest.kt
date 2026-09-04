package io.github.kmbisset89.worldweaver.domain

import io.github.kmbisset89.worldweaver.generated.AppVersion
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
        const val APP_VERSION = AppVersion.VALUE
        const val DB_SCHEMA_VERSION = 21
    }
}
