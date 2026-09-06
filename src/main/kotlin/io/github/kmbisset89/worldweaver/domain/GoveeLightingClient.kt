package io.github.kmbisset89.worldweaver.domain

/**
 * Discovers Govee lights on the LAN and applies color and power commands.
 */
internal interface GoveeLightingClient {
    suspend fun scan(): ScanResult

    suspend fun apply(
        devices: List<GoveeDevice>,
        command: GoveeLightCommand,
    ): ApplyResult

    sealed interface ScanResult {
        data class Devices(val devices: List<GoveeDevice>) : ScanResult
        data class Failed(val message: String) : ScanResult
    }

    sealed interface ApplyResult {
        data object Applied : ApplyResult
        data class Failed(val message: String) : ApplyResult
    }
}
