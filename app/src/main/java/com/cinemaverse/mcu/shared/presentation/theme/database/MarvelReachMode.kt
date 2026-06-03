package com.cinemaverse.mcu.shared.presentation.theme.database

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Stable
class MarvelReachModeState internal constructor(
    initial: Boolean,
    private val timeoutMillis: Long
) {
    var isReachable by mutableStateOf(initial)
        private set

    fun enter() { isReachable = true }
    fun exit() { isReachable = false }
    fun toggle() { isReachable = !isReachable }

    suspend fun autoExitIfNeeded() {
        if (isReachable && timeoutMillis > 0) {
            delay(timeoutMillis)
            isReachable = false
        }
    }
}

@Composable
fun rememberMarvelReachModeState(
    enabled: Boolean = true,
    timeoutMillis: Long = 5_000
): MarvelReachModeState {
    val state = remember(enabled, timeoutMillis) { MarvelReachModeState(false, timeoutMillis) }
    LaunchedEffect(state.isReachable, enabled) { if (enabled) state.autoExitIfNeeded() else state.exit() }
    return state
}

@Composable
fun Modifier.marvelReachModeOffset(
    state: MarvelReachModeState,
    reachOffset: Dp = 96.dp
): Modifier {
    val offset by animateDpAsState(
        targetValue = if (state.isReachable) reachOffset else 0.dp,
        animationSpec = tween(220),
        label = "marvelReachOffset"
    )
    return this
        .offset(y = offset)
        .pointerInput(state) {
            detectVerticalDragGestures { _, dragAmount ->
                when {
                    dragAmount > 18 -> state.enter()
                    dragAmount < -18 -> state.exit()
                }
            }
        }
}
