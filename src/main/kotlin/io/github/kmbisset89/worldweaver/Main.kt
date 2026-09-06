package io.github.kmbisset89.worldweaver

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.application
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.di.appModule
import io.github.kmbisset89.worldweaver.domain.SessionRecordingCapture
import io.github.kmbisset89.worldweaver.ui.App
import io.github.kmbisset89.worldweaver.ui.AppViewEffect
import io.github.kmbisset89.worldweaver.ui.AppViewModel
import io.github.kmbisset89.worldweaver.ui.appWindowIcon
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

fun main() {
    val koin = startKoin {
        modules(appModule())
    }.koin
    val appScope = koin.get<AppCoroutineScope>()
    val viewModel = koin.get<AppViewModel>()
    val recordingCapture = koin.get<SessionRecordingCapture>()

    fun shutdownAndExit(exit: () -> Unit) {
        recordingCapture.shutdown()
        appScope.cancel()
        stopKoin()
        exit()
    }

    application {
        LaunchedEffect(Unit) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    AppViewEffect.ExitRequested -> {
                        shutdownAndExit { exitApplication() }
                    }
                }
            }
        }
        Window(
            onCloseRequest = {
                shutdownAndExit { exitApplication() }
            },
            title = "World Weaver",
            icon = appWindowIcon(),
            state = WindowState(
                size = DpSize(1280.dp, 800.dp),
                position = WindowPosition(Alignment.Center)
            )
        ) {
            App(viewModel = viewModel)
        }
    }
}
