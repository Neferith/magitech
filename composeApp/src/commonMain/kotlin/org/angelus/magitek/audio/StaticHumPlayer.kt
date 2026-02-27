// commonMain/kotlin/org/angelus/magitek/audio/StaticHumPlayer.kt

package org.angelus.magitek.audio

import androidx.compose.runtime.Composable

expect class StaticHumPlayer {
    var volume: Float
    fun start(scope: kotlinx.coroutines.CoroutineScope)
    fun stop()
    fun release()
}

@Composable
expect fun rememberStaticHumPlayer(): StaticHumPlayer