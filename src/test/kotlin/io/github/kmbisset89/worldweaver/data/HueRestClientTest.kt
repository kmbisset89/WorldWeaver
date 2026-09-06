package io.github.kmbisset89.worldweaver.data

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.github.kmbisset89.worldweaver.domain.HueClient
import io.github.kmbisset89.worldweaver.domain.HueConnection
import io.github.kmbisset89.worldweaver.domain.HueLight
import io.github.kmbisset89.worldweaver.domain.HueScene
import kotlinx.coroutines.test.runTest
import java.net.InetSocketAddress
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class HueRestClientTest {
    private val recalledSceneIds = mutableListOf<String>()
    private val lightPuts = mutableListOf<Pair<String, String>>()
    private val server = HttpServer.create(InetSocketAddress(0), 0).also { http ->
        http.createContext("/clip/v2/resource/scene") { exchange ->
            exchange.requestBody.use { it.readBytes() }
            val authorized = exchange.requestHeaders.getFirst("hue-application-key") == APPLICATION_KEY
            val path = exchange.requestURI.path
            val method = exchange.requestMethod
            if (!authorized) {
                val body = """{"errors":[{"description":"unauthorized"}],"data":[]}"""
                exchange.sendResponseHeaders(403, body.toByteArray().size.toLong())
                exchange.responseBody.use { it.write(body.toByteArray()) }
                return@createContext
            }
            if (method == "GET" && path == "/clip/v2/resource/scene") {
                respond(exchange, 200, SCENE_LIST_BODY)
                return@createContext
            }
            if (method == "GET" && path.startsWith("/clip/v2/resource/scene/")) {
                val sceneId = path.substringAfterLast('/')
                respond(exchange, 200, sceneDetailBody(sceneId))
                return@createContext
            }
            if (method == "PUT" && path.startsWith("/clip/v2/resource/scene/")) {
                recalledSceneIds += path.substringAfterLast('/')
                val body = """{"errors":[],"data":[{"rid":"${path.substringAfterLast('/')}","rtype":"scene"}]}"""
                respond(exchange, 200, body)
                return@createContext
            }
            exchange.sendResponseHeaders(404, -1)
        }
        http.createContext("/clip/v2/resource/light") { exchange ->
            val authorized = exchange.requestHeaders.getFirst("hue-application-key") == APPLICATION_KEY
            val path = exchange.requestURI.path
            val requestBody = exchange.requestBody.use { it.readBytes().toString(Charsets.UTF_8) }
            if (!authorized) {
                val body = """{"errors":[{"description":"unauthorized"}],"data":[]}"""
                respond(exchange, 403, body)
                return@createContext
            }
            if (exchange.requestMethod == "GET" && path == "/clip/v2/resource/light") {
                respond(exchange, 200, LIGHT_LIST_BODY)
                return@createContext
            }
            if (exchange.requestMethod == "PUT" && path.startsWith("/clip/v2/resource/light/")) {
                lightPuts += path.substringAfterLast('/') to requestBody
                val body = """{"errors":[],"data":[{"rid":"${path.substringAfterLast('/')}","rtype":"light"}]}"""
                respond(exchange, 200, body)
                return@createContext
            }
            exchange.sendResponseHeaders(404, -1)
        }
        http.createContext("/api/$APPLICATION_KEY/groups/1/action") { exchange ->
            exchange.requestBody.use { it.readBytes() }
            val body = """[{"success":{"/groups/1/action/scene":"legacy"}}]"""
            respond(exchange, 200, body)
        }
        http.start()
    }
    private val client = HueRestClient(clipV2UsesHttps = false)
    private val connection = HueConnection(
        bridgeHost = "127.0.0.1:${server.address.port}",
        applicationKey = APPLICATION_KEY,
    )

    @AfterTest
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun listsClipV2Scenes() = runTest {
        val result = client.listScenes(connection)
        val scenes = assertIs<HueClient.SceneListResult.Scenes>(result)
        assertEquals(
            listOf(
                HueScene(id = COMBAT_ID, name = "Combat", groupId = "room-1"),
                HueScene(id = EMPTY_ID, name = "Empty", groupId = "room-1"),
                HueScene(id = TAVERN_ID, name = "Tavern", groupId = "room-1"),
            ),
            scenes.scenes,
        )
    }

    @Test
    fun appliesColorToEachLightWhenSwappingScenes() = runTest {
        assertIs<HueClient.ActivateResult.Activated>(
            client.activateScene(connection, COMBAT_ID, "room-1", emptyList()),
        )
        assertIs<HueClient.ActivateResult.Activated>(
            client.activateScene(connection, TAVERN_ID, "room-1", emptyList()),
        )
        assertEquals(listOf(LIGHT_ID, LAMP_ID, LIGHT_ID), lightPuts.map { it.first })
        assertTrue(lightPuts[0].second.contains("\"x\":0.64"))
        assertTrue(lightPuts[2].second.contains("\"x\":0.45"))
        assertTrue(lightPuts.all { it.second.contains("\"xy\"") })
        assertFalse(lightPuts.any { it.second.contains("no_effect") })
        assertTrue(recalledSceneIds.isEmpty())
    }

    @Test
    fun appliesOnlySelectedLights() = runTest {
        assertIs<HueClient.ActivateResult.Activated>(
            client.activateScene(connection, COMBAT_ID, "room-1", listOf(LAMP_ID)),
        )
        assertEquals(listOf(LAMP_ID), lightPuts.map { it.first })
        assertTrue(lightPuts.single().second.contains("\"x\":0.21"))
        assertTrue(recalledSceneIds.isEmpty())
    }

    @Test
    fun listsClipV2Lights() = runTest {
        val result = client.listLights(connection)
        val lights = assertIs<HueClient.LightListResult.Lights>(result)
        assertEquals(
            listOf(
                HueLight(id = LIGHT_ID, name = "Ceiling"),
                HueLight(id = LAMP_ID, name = "Table lamp"),
            ),
            lights.lights,
        )
    }

    @Test
    fun resolvesLegacyV1SceneIdBeforeApplyingLights() = runTest {
        assertIs<HueClient.ActivateResult.Activated>(
            client.activateScene(connection, "hue-combat", "1", emptyList()),
        )
        assertEquals(listOf(LIGHT_ID, LAMP_ID), lightPuts.map { it.first })
        assertTrue(lightPuts.all { it.second.contains("\"xy\"") })
    }

    @Test
    fun recallsWhenSceneHasNoLightActions() = runTest {
        assertIs<HueClient.ActivateResult.Activated>(
            client.activateScene(connection, EMPTY_ID, "room-1", emptyList()),
        )
        assertTrue(lightPuts.isEmpty())
        assertEquals(listOf(EMPTY_ID), recalledSceneIds)
    }

    @Test
    fun appliesSharedLookToSelectedLights() = runTest {
        assertIs<HueClient.ActivateResult.Activated>(
            client.applyColor(
                connection = connection,
                lightIds = listOf(LAMP_ID),
                powerOn = true,
                brightness = 90,
                red = 196,
                green = 30,
                blue = 58,
            ),
        )
        assertEquals(listOf(LAMP_ID), lightPuts.map { it.first })
        assertTrue(lightPuts.single().second.contains("\"xy\""))
        assertTrue(lightPuts.single().second.contains("\"brightness\":90.0"))
        assertTrue(recalledSceneIds.isEmpty())
    }

    @Test
    fun turnsSelectedLightsOffWithoutColor() = runTest {
        assertIs<HueClient.ActivateResult.Activated>(
            client.applyColor(
                connection = connection,
                lightIds = listOf(LIGHT_ID),
                powerOn = false,
                brightness = 1,
                red = 0,
                green = 0,
                blue = 0,
            ),
        )
        assertEquals("""{"on":{"on":false}}""", lightPuts.single().second)
    }

    @Test
    fun applyColorFailsWithoutLights() = runTest {
        val failed = assertIs<HueClient.ActivateResult.Failed>(
            client.applyColor(
                connection = connection,
                lightIds = emptyList(),
                powerOn = true,
                brightness = 55,
                red = 227,
                green = 155,
                blue = 90,
            ),
        )
        assertEquals("Select Hue lights for that look", failed.message)
        assertTrue(lightPuts.isEmpty())
    }

    private companion object {
        const val APPLICATION_KEY = "hue-key"
        const val COMBAT_ID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
        const val TAVERN_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
        const val EMPTY_ID = "cccccccc-cccc-cccc-cccc-cccccccccccc"
        const val LIGHT_ID = "11111111-1111-1111-1111-111111111111"
        const val LAMP_ID = "22222222-2222-2222-2222-222222222222"
        val LIGHT_LIST_BODY = """
            {
              "errors": [],
              "data": [
                {"id": "$LIGHT_ID", "metadata": {"name": "Ceiling"}},
                {"id": "$LAMP_ID", "metadata": {"name": "Table lamp"}}
              ]
            }
        """.trimIndent()
        val SCENE_LIST_BODY = """
            {
              "errors": [],
              "data": [
                {
                  "id": "$COMBAT_ID",
                  "id_v1": "/scenes/hue-combat",
                  "metadata": {"name": "Combat"},
                  "group": {"rid": "room-1", "rtype": "room"}
                },
                {
                  "id": "$TAVERN_ID",
                  "id_v1": "/scenes/hue-tavern",
                  "metadata": {"name": "Tavern"},
                  "group": {"rid": "room-1", "rtype": "room"}
                },
                {
                  "id": "$EMPTY_ID",
                  "id_v1": "/scenes/hue-empty",
                  "metadata": {"name": "Empty"},
                  "group": {"rid": "room-1", "rtype": "room"}
                }
              ]
            }
        """.trimIndent()

        fun sceneDetailBody(sceneId: String): String {
            val actions = when (sceneId) {
                COMBAT_ID -> """
                    [
                      {
                        "target": {"rid": "$LIGHT_ID", "rtype": "light"},
                        "action": {
                          "on": {"on": true},
                          "dimming": {"brightness": 90.0},
                          "color": {"xy": {"x": 0.64, "y": 0.33}},
                          "color_temperature": {"mirek": null},
                          "effects": {"effect": "no_effect"}
                        }
                      },
                      {
                        "target": {"rid": "$LAMP_ID", "rtype": "light"},
                        "action": {
                          "on": {"on": true},
                          "dimming": {"brightness": 50.0},
                          "color": {"xy": {"x": 0.21, "y": 0.12}},
                          "color_temperature": {"mirek": null},
                          "effects": {"effect": "no_effect"}
                        }
                      }
                    ]
                """.trimIndent()
                TAVERN_ID -> """
                    [{
                      "target": {"rid": "$LIGHT_ID", "rtype": "light"},
                      "action": {
                        "on": {"on": true},
                        "dimming": {"brightness": 40.0},
                        "color": {"xy": {"x": 0.45, "y": 0.41}},
                        "color_temperature": {"mirek": null},
                        "effects": {"effect": "no_effect"}
                      }
                    }]
                """.trimIndent()
                else -> "[]"
            }
            return """
                {
                  "errors": [],
                  "data": [{
                    "id": "$sceneId",
                    "actions": $actions
                  }]
                }
            """.trimIndent()
        }

        fun respond(exchange: HttpExchange, status: Int, body: String) {
            val bytes = body.toByteArray()
            exchange.sendResponseHeaders(status, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
    }
}
