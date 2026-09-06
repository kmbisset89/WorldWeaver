package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.HueBridgeDiscoveryTransformer
import io.github.kmbisset89.worldweaver.domain.HueBridgeHostParser
import io.github.kmbisset89.worldweaver.domain.HueClient
import io.github.kmbisset89.worldweaver.domain.HueClipV2LightColorCommandTransformer
import io.github.kmbisset89.worldweaver.domain.HueClipV2LightCommand
import io.github.kmbisset89.worldweaver.domain.HueClipV2LightListTransformer
import io.github.kmbisset89.worldweaver.domain.HueClipV2RecallResultTransformer
import io.github.kmbisset89.worldweaver.domain.HueClipV2SceneLightCommandTransformer
import io.github.kmbisset89.worldweaver.domain.HueClipV2SceneListTransformer
import io.github.kmbisset89.worldweaver.domain.HueConnection
import io.github.kmbisset89.worldweaver.domain.HueJsonArrayResultTransformer
import io.github.kmbisset89.worldweaver.domain.HueRgbXyTransformer
import io.github.kmbisset89.worldweaver.domain.HueSceneListTransformer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.ConnectException
import java.net.Socket
import java.net.URI
import java.net.UnknownHostException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedTrustManager

/**
 * Philips Hue local HTTP API translator.
 *
 * Pairing and discovery use CLIP v1. Scene catalogs use CLIP v2. Activation
 * applies each scene light's color and brightness, or a shared Look on selected
 * lamps, so switching looks changes chromaticity, not only dimming.
 */
internal class HueRestClient(
    private val httpClient: HttpClient = defaultLanHttpClient(),
    private val localHttpsClient: HttpClient = hueBridgeHttpsClient(),
    private val requestTimeout: Duration = Duration.ofSeconds(10),
    private val hostParser: HueBridgeHostParser = HueBridgeHostParser(),
    private val sceneListTransformer: HueSceneListTransformer = HueSceneListTransformer(),
    private val clipV2SceneListTransformer: HueClipV2SceneListTransformer = HueClipV2SceneListTransformer(),
    private val clipV2LightListTransformer: HueClipV2LightListTransformer = HueClipV2LightListTransformer(),
    private val clipV2SceneLightCommandTransformer: HueClipV2SceneLightCommandTransformer =
        HueClipV2SceneLightCommandTransformer(),
    private val clipV2RecallTransformer: HueClipV2RecallResultTransformer = HueClipV2RecallResultTransformer(),
    private val clipV2LightColorCommandTransformer: HueClipV2LightColorCommandTransformer =
        HueClipV2LightColorCommandTransformer(),
    private val rgbXyTransformer: HueRgbXyTransformer = HueRgbXyTransformer(),
    private val jsonArrayTransformer: HueJsonArrayResultTransformer = HueJsonArrayResultTransformer(),
    private val discoveryTransformer: HueBridgeDiscoveryTransformer = HueBridgeDiscoveryTransformer(),
    private val clipV2UsesHttps: Boolean = true,
) : HueClient {
    override suspend fun discoverBridges(): HueClient.DiscoverResult {
        val response = sendRaw(DISCOVERY_URL, GET) ?: return HueClient.DiscoverResult.Failed(
            UNREACHABLE_MESSAGE,
        )
        if (response.statusCode() != 200) {
            return HueClient.DiscoverResult.Failed("Hue discovery returned HTTP ${response.statusCode()}")
        }
        val bridges = discoveryTransformer.transform(response.body())
        if (bridges.isEmpty()) {
            return HueClient.DiscoverResult.Failed("No Hue bridges were found on this network")
        }
        return HueClient.DiscoverResult.Bridges(bridges)
    }

    override suspend fun testConnection(connection: HueConnection): HueClient.ConnectionResult {
        val response = send(connection, "/config", GET) ?: return HueClient.ConnectionResult.Unreachable(
            UNREACHABLE_MESSAGE,
        )
        return when {
            response.statusCode() != 200 -> HueClient.ConnectionResult.Unreachable(
                "Hue returned HTTP ${response.statusCode()}",
            )
            jsonArrayTransformer.isErrorArray(response.body()) -> HueClient.ConnectionResult.Unauthorized
            else -> HueClient.ConnectionResult.Connected
        }
    }

    override suspend fun pair(bridgeHost: String, deviceName: String): HueClient.PairResult {
        val host = when (val parsed = hostParser.parse(bridgeHost)) {
            is HueBridgeHostParser.Result.Valid -> parsed.host
            HueBridgeHostParser.Result.Blank -> return HueClient.PairResult.Failed("Enter a Hue bridge IP")
            HueBridgeHostParser.Result.Invalid -> return HueClient.PairResult.Failed("Enter a Hue bridge IP")
        }
        val safeName = deviceName.trim().ifBlank { "WorldWeaver" }.take(40)
        val response = sendRaw(
            url = "http://$host/api",
            method = POST,
            body = """{"devicetype":"$safeName"}""",
        ) ?: return HueClient.PairResult.Failed(UNREACHABLE_MESSAGE)
        if (response.statusCode() != 200) {
            return HueClient.PairResult.Failed("Hue returned HTTP ${response.statusCode()}")
        }
        return when (val parsed = jsonArrayTransformer.transformPair(response.body())) {
            is HueJsonArrayResultTransformer.Result.Username -> HueClient.PairResult.Paired(parsed.username)
            HueJsonArrayResultTransformer.Result.LinkButtonNotPressed -> HueClient.PairResult.LinkButtonNotPressed
            HueJsonArrayResultTransformer.Result.Unauthorized -> HueClient.PairResult.Failed("Hue rejected the request")
            HueJsonArrayResultTransformer.Result.Success -> HueClient.PairResult.Failed("Hue returned an unexpected response")
            is HueJsonArrayResultTransformer.Result.Failed -> HueClient.PairResult.Failed(parsed.message)
        }
    }

    override suspend fun listScenes(connection: HueConnection): HueClient.SceneListResult {
        val clipV2 = sendClipV2(connection, SCENE_COLLECTION_PATH, GET)
        if (clipV2 != null) {
            when (clipV2.statusCode()) {
                401, 403 -> return HueClient.SceneListResult.Unauthorized
                200 -> return HueClient.SceneListResult.Scenes(
                    clipV2SceneListTransformer.transform(clipV2.body()),
                )
            }
        }
        return listScenesClipV1(connection)
    }

    override suspend fun listLights(connection: HueConnection): HueClient.LightListResult {
        val response = sendClipV2(connection, LIGHT_PATH, GET) ?: return HueClient.LightListResult.Failed(
            UNREACHABLE_MESSAGE,
        )
        return when (response.statusCode()) {
            401, 403 -> HueClient.LightListResult.Unauthorized
            200 -> HueClient.LightListResult.Lights(clipV2LightListTransformer.transform(response.body()))
            else -> HueClient.LightListResult.Failed("Hue returned HTTP ${response.statusCode()}")
        }
    }

    override suspend fun activateScene(
        connection: HueConnection,
        sceneId: String,
        groupId: String,
        lightIds: List<String>,
    ): HueClient.ActivateResult {
        val requestedId = sceneId.trim()
        if (requestedId.isEmpty()) {
            return HueClient.ActivateResult.Failed("Hue scene is missing")
        }
        val clipV2Id = resolveClipV2SceneId(connection, requestedId)
        if (clipV2Id != null) {
            val applied = applyClipV2Scene(connection, clipV2Id, lightIds)
            if (applied != null) {
                return applied
            }
            if (isClipV2Id(requestedId)) {
                return HueClient.ActivateResult.Failed(UNREACHABLE_MESSAGE)
            }
        }
        return recallClipV1(connection, requestedId, groupId)
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
        val commands = clipV2LightColorCommandTransformer.transform(
            lightIds = lightIds,
            powerOn = powerOn,
            brightness = brightness,
            xy = rgbXyTransformer.transform(red, green, blue),
        )
        if (commands.isEmpty()) {
            return HueClient.ActivateResult.Failed("Select Hue lights for that look")
        }
        return applyLightCommands(connection, commands)
    }

    private suspend fun listScenesClipV1(connection: HueConnection): HueClient.SceneListResult {
        val response = send(connection, "/scenes", GET) ?: return HueClient.SceneListResult.Failed(
            UNREACHABLE_MESSAGE,
        )
        if (response.statusCode() != 200) {
            return HueClient.SceneListResult.Failed("Hue returned HTTP ${response.statusCode()}")
        }
        if (jsonArrayTransformer.isErrorArray(response.body())) {
            return HueClient.SceneListResult.Unauthorized
        }
        val scenes = sceneListTransformer.transform(response.body()) ?: return HueClient.SceneListResult.Unauthorized
        return HueClient.SceneListResult.Scenes(scenes)
    }

    private suspend fun resolveClipV2SceneId(connection: HueConnection, sceneId: String): String? {
        if (isClipV2Id(sceneId)) {
            return sceneId
        }
        val response = sendClipV2(connection, SCENE_COLLECTION_PATH, GET) ?: return null
        if (response.statusCode() != 200) {
            return null
        }
        return clipV2SceneListTransformer.resolveId(response.body(), sceneId)
    }

    private suspend fun applyClipV2Scene(
        connection: HueConnection,
        sceneId: String,
        lightIds: List<String>,
    ): HueClient.ActivateResult? {
        val selectedIds = lightIds.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val response = sendClipV2(connection, "$SCENE_COLLECTION_PATH/$sceneId", GET) ?: return null
        return when (response.statusCode()) {
            401, 403 -> HueClient.ActivateResult.Unauthorized
            200 -> {
                val commands = selectedLightCommands(
                    clipV2SceneLightCommandTransformer.transform(response.body()),
                    selectedIds,
                )
                when {
                    commands.isNotEmpty() -> applyLightCommands(connection, commands)
                    selectedIds.isNotEmpty() -> HueClient.ActivateResult.Failed(
                        "None of the selected Hue lights are in that scene",
                    )
                    else -> recallClipV2(connection, sceneId)
                }
            }
            else -> {
                if (selectedIds.isNotEmpty()) {
                    HueClient.ActivateResult.Failed("Could not load that Hue scene")
                } else {
                    recallClipV2(connection, sceneId)
                }
            }
        }
    }

    private fun selectedLightCommands(
        commands: List<HueClipV2LightCommand>,
        selectedIds: Set<String>,
    ): List<HueClipV2LightCommand> {
        if (selectedIds.isEmpty()) {
            return commands
        }
        return commands.filter { it.lightId in selectedIds }
    }

    private suspend fun applyLightCommands(
        connection: HueConnection,
        commands: List<HueClipV2LightCommand>,
    ): HueClient.ActivateResult {
        var anySuccess = false
        var unauthorized = false
        val errors = mutableListOf<String>()
        for (command in commands) {
            val response = sendClipV2(
                connection = connection,
                path = "$LIGHT_PATH/${command.lightId}",
                method = PUT,
                body = command.bodyJson,
            )
            if (response == null) {
                errors.add(UNREACHABLE_MESSAGE)
                continue
            }
            when (val parsed = clipV2RecallTransformer.transform(response.statusCode(), response.body())) {
                HueClipV2RecallResultTransformer.Result.Success -> anySuccess = true
                HueClipV2RecallResultTransformer.Result.Unauthorized -> unauthorized = true
                is HueClipV2RecallResultTransformer.Result.Failed -> errors.add(parsed.message)
            }
        }
        return when {
            unauthorized && !anySuccess -> HueClient.ActivateResult.Unauthorized
            anySuccess -> HueClient.ActivateResult.Activated
            else -> HueClient.ActivateResult.Failed(errors.firstOrNull() ?: "Could not change those Hue lights")
        }
    }

    private suspend fun recallClipV2(
        connection: HueConnection,
        sceneId: String,
    ): HueClient.ActivateResult? {
        val response = sendClipV2(
            connection = connection,
            path = "$SCENE_COLLECTION_PATH/$sceneId",
            method = PUT,
            body = RECALL_BODY,
        ) ?: return null
        return when (val parsed = clipV2RecallTransformer.transform(response.statusCode(), response.body())) {
            HueClipV2RecallResultTransformer.Result.Success -> HueClient.ActivateResult.Activated
            HueClipV2RecallResultTransformer.Result.Unauthorized -> HueClient.ActivateResult.Unauthorized
            is HueClipV2RecallResultTransformer.Result.Failed -> HueClient.ActivateResult.Failed(parsed.message)
        }
    }

    private suspend fun recallClipV1(
        connection: HueConnection,
        sceneId: String,
        groupId: String,
    ): HueClient.ActivateResult {
        val group = groupId.ifBlank { "0" }
        val response = send(
            connection = connection,
            path = "/groups/$group/action",
            method = PUT,
            body = buildJsonObject { put("scene", sceneId) }.toString(),
        ) ?: return HueClient.ActivateResult.Failed(UNREACHABLE_MESSAGE)
        if (response.statusCode() != 200) {
            return HueClient.ActivateResult.Failed("Hue returned HTTP ${response.statusCode()}")
        }
        return when (val parsed = jsonArrayTransformer.transformAction(response.body())) {
            HueJsonArrayResultTransformer.Result.Success -> HueClient.ActivateResult.Activated
            HueJsonArrayResultTransformer.Result.Unauthorized -> HueClient.ActivateResult.Unauthorized
            HueJsonArrayResultTransformer.Result.LinkButtonNotPressed -> HueClient.ActivateResult.Failed("Press the Hue link button")
            is HueJsonArrayResultTransformer.Result.Username -> HueClient.ActivateResult.Failed("Hue returned an unexpected response")
            is HueJsonArrayResultTransformer.Result.Failed -> HueClient.ActivateResult.Failed(parsed.message)
        }
    }

    private suspend fun send(
        connection: HueConnection,
        path: String,
        method: String,
        body: String? = null,
    ): HttpResponse<String>? {
        return sendRaw(
            url = "http://${connection.bridgeHost}/api/${connection.applicationKey}$path",
            method = method,
            body = body,
        )
    }

    private suspend fun sendClipV2(
        connection: HueConnection,
        path: String,
        method: String,
        body: String? = null,
    ): HttpResponse<String>? {
        val scheme = if (clipV2UsesHttps) "https" else "http"
        val client = if (clipV2UsesHttps) localHttpsClient else httpClient
        return sendRaw(
            url = "$scheme://${connection.bridgeHost}/clip/v2$path",
            method = method,
            body = body,
            headers = mapOf(APPLICATION_KEY_HEADER to connection.applicationKey),
            client = client,
        )
    }

    private suspend fun sendRaw(
        url: String,
        method: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
        client: HttpClient = httpClient,
    ): HttpResponse<String>? {
        return withContext(Dispatchers.IO) {
            try {
                val builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(requestTimeout)
                headers.forEach { (name, value) ->
                    builder.header(name, value)
                }
                val request = when (method) {
                    POST -> builder
                        .header(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE)
                        .POST(HttpRequest.BodyPublishers.ofString(body.orEmpty()))
                        .build()
                    PUT -> builder
                        .header(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE)
                        .PUT(HttpRequest.BodyPublishers.ofString(body.orEmpty()))
                        .build()
                    else -> builder.GET().build()
                }
                client.send(request, HttpResponse.BodyHandlers.ofString())
            } catch (_: HttpTimeoutException) {
                null
            } catch (_: ConnectException) {
                null
            } catch (_: UnknownHostException) {
                null
            } catch (_: Exception) {
                null
            }
        }
    }

    private companion object {
        const val GET = "GET"
        const val POST = "POST"
        const val PUT = "PUT"
        const val DISCOVERY_URL = "https://discovery.meethue.com/"
        const val UNREACHABLE_MESSAGE = "Could not reach the Hue bridge"
        const val SCENE_COLLECTION_PATH = "/resource/scene"
        const val LIGHT_PATH = "/resource/light"
        const val APPLICATION_KEY_HEADER = "hue-application-key"
        const val CONTENT_TYPE_HEADER = "Content-Type"
        const val JSON_CONTENT_TYPE = "application/json"
        const val RECALL_BODY = """{"recall":{"action":"active"}}"""
        val CLIP_V2_ID = Regex(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}",
        )

        fun isClipV2Id(id: String): Boolean = CLIP_V2_ID.matches(id)

        fun defaultLanHttpClient(): HttpClient {
            return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build()
        }

        fun hueBridgeHttpsClient(): HttpClient {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(HueBridgeLocalTrustManager()), SecureRandom())
            return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .sslContext(sslContext)
                .build()
        }
    }
}

/**
 * Accepts the Hue bridge's local self-signed certificate for CLIP v2 HTTPS.
 */
private class HueBridgeLocalTrustManager : X509ExtendedTrustManager() {
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String, socket: Socket) = Unit

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String, socket: Socket) = Unit

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String, engine: SSLEngine) = Unit

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String, engine: SSLEngine) = Unit

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit

    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
}
