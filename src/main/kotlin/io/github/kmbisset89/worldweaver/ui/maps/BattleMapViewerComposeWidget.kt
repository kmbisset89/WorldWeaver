package io.github.kmbisset89.worldweaver.ui.maps

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import ovh.plrapps.mapcompose.api.onMarkerClick
import ovh.plrapps.mapcompose.api.onTap
import ovh.plrapps.mapcompose.api.scale
import ovh.plrapps.mapcompose.api.scroll
import ovh.plrapps.mapcompose.api.scrollTo
import ovh.plrapps.mapcompose.ui.MapUI
import ovh.plrapps.mapcompose.ui.state.MapState

@Composable
internal fun BattleMapViewerComposeWidget(
    mapState: MapState,
    modifier: Modifier = Modifier,
    onMapTapped: ((x: Double, y: Double) -> Unit)? = null,
    onMarkerClicked: ((id: String) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val currentOnMapTapped by rememberUpdatedState(onMapTapped)
    val currentOnMarkerClicked by rememberUpdatedState(onMarkerClicked)
    LaunchedEffect(mapState) {
        focusRequester.requestFocus()
        mapState.onTap { x, y ->
            currentOnMapTapped?.invoke(x, y)
        }
        mapState.onMarkerClick { id, _, _ ->
            currentOnMarkerClicked?.invoke(id)
        }
    }
    Box(
        modifier = modifier
            .background(SurfaceCard, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        MapUI(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) {
                        return@onPreviewKeyEvent false
                    }
                    val step = if (event.isCtrlPressed) 0.15 else 0.05
                    val zoomStep = if (event.isCtrlPressed) 1.25 else 1.10
                    when (event.key) {
                        Key.DirectionLeft, Key.A -> {
                            scope.launch {
                                val current = mapState.scroll
                                mapState.scrollTo(current.x - step, current.y, animationSpec = tween(80))
                            }
                            true
                        }
                        Key.DirectionRight, Key.D -> {
                            scope.launch {
                                val current = mapState.scroll
                                mapState.scrollTo(current.x + step, current.y, animationSpec = tween(80))
                            }
                            true
                        }
                        Key.DirectionUp, Key.W -> {
                            scope.launch {
                                val current = mapState.scroll
                                mapState.scrollTo(current.x, current.y - step, animationSpec = tween(80))
                            }
                            true
                        }
                        Key.DirectionDown, Key.S -> {
                            scope.launch {
                                val current = mapState.scroll
                                mapState.scrollTo(current.x, current.y + step, animationSpec = tween(80))
                            }
                            true
                        }
                        Key.Equals, Key.Plus, Key.NumPadAdd -> {
                            mapState.scale = mapState.scale * zoomStep
                            true
                        }
                        Key.Minus, Key.NumPadSubtract -> {
                            mapState.scale = mapState.scale / zoomStep
                            true
                        }
                        Key.Zero, Key.NumPad0 -> {
                            scope.launch {
                                mapState.scale = 1.0
                                mapState.scrollTo(0.5, 0.5, destScale = 1.0, animationSpec = tween(120))
                            }
                            true
                        }
                        else -> false
                    }
                },
            state = mapState,
        )
    }
}
