package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class ListHomeAssistantScenesUseCaseTest {
    private val parser = HomeAssistantConnectionParser()
    private val client = FakeHomeAssistantClient()
    private val useCase = ListHomeAssistantScenesUseCase(parser, client)

    @Test
    fun returnsCatalogScenes() = runTest {
        val scenes = listOf(HomeAssistantScene("scene.tavern", "Tavern"))
        client.sceneListResult = HomeAssistantClient.SceneListResult.Scenes(scenes)

        val result = useCase("http://ha.local:8123", "token")

        val listed = assertIs<ListHomeAssistantScenesUseCase.Result.Listed>(result)
        assertEquals(scenes, listed.scenes)
    }

    @Test
    fun unauthorizedWhenTokenRejected() = runTest {
        client.sceneListResult = HomeAssistantClient.SceneListResult.Unauthorized
        assertIs<ListHomeAssistantScenesUseCase.Result.Unauthorized>(
            useCase("http://ha.local:8123", "token"),
        )
    }

    @Test
    fun blankUrlIsRejected() = runTest {
        assertIs<ListHomeAssistantScenesUseCase.Result.BlankUrl>(useCase(" ", "token"))
    }
}
