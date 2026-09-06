package io.github.kmbisset89.worldweaver.domain

internal class FakeHueClient : HueClient {
    var discoverResult: HueClient.DiscoverResult = HueClient.DiscoverResult.Bridges(emptyList())
    var connectionResult: HueClient.ConnectionResult = HueClient.ConnectionResult.Connected
    var pairResult: HueClient.PairResult = HueClient.PairResult.Paired("hue-key")
    var sceneListResult: HueClient.SceneListResult = HueClient.SceneListResult.Scenes(emptyList())
    var lightListResult: HueClient.LightListResult = HueClient.LightListResult.Lights(emptyList())
    var activateResult: HueClient.ActivateResult = HueClient.ActivateResult.Activated
    var lastActivatedSceneId: String? = null
    var lastActivatedLightIds: List<String> = emptyList()
    var lastColorLightIds: List<String> = emptyList()
    var lastColorPowerOn: Boolean? = null
    var lastColorBrightness: Int? = null
    var lastColorRed: Int? = null
    var lastColorGreen: Int? = null
    var lastColorBlue: Int? = null
    var lastPairHost: String? = null

    override suspend fun discoverBridges(): HueClient.DiscoverResult = discoverResult

    override suspend fun testConnection(connection: HueConnection): HueClient.ConnectionResult {
        return connectionResult
    }

    override suspend fun pair(bridgeHost: String, deviceName: String): HueClient.PairResult {
        lastPairHost = bridgeHost
        return pairResult
    }

    override suspend fun listScenes(connection: HueConnection): HueClient.SceneListResult = sceneListResult

    override suspend fun listLights(connection: HueConnection): HueClient.LightListResult = lightListResult

    override suspend fun activateScene(
        connection: HueConnection,
        sceneId: String,
        groupId: String,
        lightIds: List<String>,
    ): HueClient.ActivateResult {
        lastActivatedSceneId = sceneId
        lastActivatedLightIds = lightIds
        return activateResult
    }

    override suspend fun applyColor(
        connection: HueConnection,
        lightIds: List<String>,
        powerOn: Boolean,
        brightness: Int,
        red: Int,
        green: Int,
        blue: Int,
    ): HueClient.ActivateResult {
        lastColorLightIds = lightIds
        lastColorPowerOn = powerOn
        lastColorBrightness = brightness
        lastColorRed = red
        lastColorGreen = green
        lastColorBlue = blue
        return activateResult
    }
}
