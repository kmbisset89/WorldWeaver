package io.github.kmbisset89.worldweaver.domain

internal class ScanGoveeDevicesUseCase(
    private val client: GoveeLightingClient,
    private val store: AtmosphereSettingsStore,
) {
    sealed interface Result {
        data class Found(val devices: List<GoveeDevice>) : Result
        data class Failed(val message: String) : Result
    }

    suspend operator fun invoke(): Result {
        return when (val response = client.scan()) {
            is GoveeLightingClient.ScanResult.Devices -> {
                store.setGoveeDevices(response.devices)
                Result.Found(response.devices)
            }
            is GoveeLightingClient.ScanResult.Failed -> Result.Failed(response.message)
        }
    }
}
