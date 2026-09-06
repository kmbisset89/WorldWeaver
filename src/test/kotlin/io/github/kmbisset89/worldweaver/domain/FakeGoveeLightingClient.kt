package io.github.kmbisset89.worldweaver.domain

internal class FakeGoveeLightingClient : GoveeLightingClient {
    var scanResult: GoveeLightingClient.ScanResult = GoveeLightingClient.ScanResult.Devices(emptyList())
    var applyResult: GoveeLightingClient.ApplyResult = GoveeLightingClient.ApplyResult.Applied
    var lastCommand: GoveeLightCommand? = null
    var lastDevices: List<GoveeDevice> = emptyList()

    override suspend fun scan(): GoveeLightingClient.ScanResult = scanResult

    override suspend fun apply(
        devices: List<GoveeDevice>,
        command: GoveeLightCommand,
    ): GoveeLightingClient.ApplyResult {
        lastDevices = devices
        lastCommand = command
        return applyResult
    }
}
