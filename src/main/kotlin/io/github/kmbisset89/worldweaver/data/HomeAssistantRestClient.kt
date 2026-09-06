package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.HomeAssistantClient
import io.github.kmbisset89.worldweaver.domain.HomeAssistantConnection
import io.github.kmbisset89.worldweaver.domain.HomeAssistantSceneListTransformer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.URI
import java.net.UnknownHostException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import java.time.Duration

/**
 * Home Assistant REST translator using the local LAN HTTP API.
 */
internal class HomeAssistantRestClient(
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build(),
    private val requestTimeout: Duration = Duration.ofSeconds(10),
    private val sceneListTransformer: HomeAssistantSceneListTransformer = HomeAssistantSceneListTransformer(),
) : HomeAssistantClient {
    override suspend fun testConnection(
        connection: HomeAssistantConnection,
    ): HomeAssistantClient.ConnectionResult {
        val response = send(connection, GET_API) ?: return HomeAssistantClient.ConnectionResult.Unreachable(
            UNREACHABLE_MESSAGE,
        )
        return when (response.statusCode()) {
            200 -> HomeAssistantClient.ConnectionResult.Connected
            401 -> HomeAssistantClient.ConnectionResult.Unauthorized
            else -> HomeAssistantClient.ConnectionResult.Unreachable(httpErrorMessage(response.statusCode()))
        }
    }

    override suspend fun listScenes(
        connection: HomeAssistantConnection,
    ): HomeAssistantClient.SceneListResult {
        val response = send(connection, GET_STATES) ?: return HomeAssistantClient.SceneListResult.Failed(
            UNREACHABLE_MESSAGE,
        )
        return when (response.statusCode()) {
            200 -> HomeAssistantClient.SceneListResult.Scenes(sceneListTransformer.transform(response.body()))
            401 -> HomeAssistantClient.SceneListResult.Unauthorized
            else -> HomeAssistantClient.SceneListResult.Failed(httpErrorMessage(response.statusCode()))
        }
    }

    override suspend fun activateScene(
        connection: HomeAssistantConnection,
        entityId: String,
    ): HomeAssistantClient.ActivateResult {
        val response = send(
            connection = connection,
            path = POST_TURN_ON,
            body = """{"entity_id":"$entityId"}""",
        ) ?: return HomeAssistantClient.ActivateResult.Failed(UNREACHABLE_MESSAGE)
        return when (response.statusCode()) {
            200 -> HomeAssistantClient.ActivateResult.Activated
            401 -> HomeAssistantClient.ActivateResult.Unauthorized
            else -> HomeAssistantClient.ActivateResult.Failed(httpErrorMessage(response.statusCode()))
        }
    }

    private suspend fun send(
        connection: HomeAssistantConnection,
        path: String,
        body: String? = null,
    ): HttpResponse<String>? {
        return withContext(Dispatchers.IO) {
            try {
                val builder = HttpRequest.newBuilder()
                    .uri(URI.create("${connection.baseUrl}$path"))
                    .header("Authorization", "Bearer ${connection.token}")
                    .timeout(requestTimeout)
                val request = if (body == null) {
                    builder.GET().build()
                } else {
                    builder
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build()
                }
                httpClient.send(request, HttpResponse.BodyHandlers.ofString())
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

    private fun httpErrorMessage(statusCode: Int): String {
        return "Home Assistant returned HTTP $statusCode"
    }

    private companion object {
        const val GET_API = "/api/"
        const val GET_STATES = "/api/states"
        const val POST_TURN_ON = "/api/services/scene/turn_on"
        const val UNREACHABLE_MESSAGE = "Could not reach Home Assistant"
    }
}
