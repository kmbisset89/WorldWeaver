package io.github.kmbisset89.worldweaver.data

import com.sun.net.httpserver.HttpServer
import io.github.kmbisset89.worldweaver.domain.HomeAssistantClient
import io.github.kmbisset89.worldweaver.domain.HomeAssistantConnection
import io.github.kmbisset89.worldweaver.domain.HomeAssistantScene
import kotlinx.coroutines.test.runTest
import java.net.InetSocketAddress
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class HomeAssistantRestClientTest {
    private val server = HttpServer.create(InetSocketAddress(0), 0).also { http ->
        http.createContext("/api/") { exchange ->
            val status = if (authorized(exchange.requestHeaders.getFirst("Authorization"))) 200 else 401
            val body = """{"message":"API running."}"""
            exchange.sendResponseHeaders(status, body.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(body.toByteArray()) }
        }
        http.createContext("/api/states") { exchange ->
            val status = if (authorized(exchange.requestHeaders.getFirst("Authorization"))) 200 else 401
            val body = """[{"entity_id":"scene.tavern","attributes":{"friendly_name":"Tavern"}}]"""
            exchange.sendResponseHeaders(status, body.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(body.toByteArray()) }
        }
        http.createContext("/api/services/scene/turn_on") { exchange ->
            exchange.requestBody.use { it.readBytes() }
            val status = if (authorized(exchange.requestHeaders.getFirst("Authorization"))) 200 else 401
            val body = "[]"
            exchange.sendResponseHeaders(status, body.toByteArray().size.toLong())
            exchange.responseBody.use { it.write(body.toByteArray()) }
        }
        http.start()
    }
    private val client = HomeAssistantRestClient()
    private val valid = HomeAssistantConnection(
        baseUrl = "http://127.0.0.1:${server.address.port}",
        token = "good-token",
    )

    @AfterTest
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun testConnectionSucceedsWithValidToken() = runTest {
        assertIs<HomeAssistantClient.ConnectionResult.Connected>(client.testConnection(valid))
    }

    @Test
    fun testConnectionUnauthorizedWithBadToken() = runTest {
        assertIs<HomeAssistantClient.ConnectionResult.Unauthorized>(
            client.testConnection(valid.copy(token = "bad")),
        )
    }

    @Test
    fun listsSceneEntities() = runTest {
        val result = client.listScenes(valid)
        val scenes = assertIs<HomeAssistantClient.SceneListResult.Scenes>(result)
        assertEquals(listOf(HomeAssistantScene("scene.tavern", "Tavern")), scenes.scenes)
    }

    @Test
    fun activatesScene() = runTest {
        assertIs<HomeAssistantClient.ActivateResult.Activated>(
            client.activateScene(valid, "scene.tavern"),
        )
    }

    private fun authorized(header: String?): Boolean {
        return header == "Bearer good-token"
    }
}
