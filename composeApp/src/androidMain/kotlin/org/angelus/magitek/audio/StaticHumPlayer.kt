// androidMain/kotlin/org/angelus/magitek/audio/StaticHumPlayer.android.kt

package org.angelus.magitek.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random

/**
 * Grésillement magitek constant — bruit rose léger + ronflement basse fréquence.
 * Tourne dans une coroutine, s'arrête proprement au release().
 */
actual class StaticHumPlayer {

    private val sampleRate  = 22050
    private val bufferSize  = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
    ).coerceAtLeast(2048)

    private val audioTrack: AudioTrack = AudioTrack(
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build(),
        AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build(),
        bufferSize,
        AudioTrack.MODE_STREAM,
        AudioManager.AUDIO_SESSION_ID_GENERATE,
    )

    private var job: Job? = null

    // Volume global du grésillement (0f → 1f)
    actual var volume: Float = 0.04f   // très discret par défaut

    actual fun start(scope: CoroutineScope) {
        if (job?.isActive == true) return
        audioTrack.play()
        job = scope.launch(Dispatchers.IO) {
            val buffer   = ShortArray(bufferSize / 2)
            var pinkB0   = 0.0; var pinkB1 = 0.0; var pinkB2 = 0.0
            var pinkB3   = 0.0; var pinkB4 = 0.0; var pinkB5 = 0.0
            var humPhase = 0.0
            val humFreq  = 60.0  // ronflement 60 Hz (alimentation magitek)

            while (isActive) {
                for (i in buffer.indices) {
                    val white = Random.nextDouble(-1.0, 1.0)

                    // Filtre bruit rose (Paul Kellet algorithm)
                    pinkB0 = 0.99886 * pinkB0 + white * 0.0555179
                    pinkB1 = 0.99332 * pinkB1 + white * 0.0750759
                    pinkB2 = 0.96900 * pinkB2 + white * 0.1538520
                    pinkB3 = 0.86650 * pinkB3 + white * 0.3104856
                    pinkB4 = 0.55000 * pinkB4 + white * 0.5329522
                    pinkB5 = -0.7616 * pinkB5 - white * 0.0168980
                    val pink = (pinkB0 + pinkB1 + pinkB2 + pinkB3 + pinkB4 + pinkB5 + white * 0.5362) * 0.11

                    // Ronflement basse fréquence
                    val hum = sin(2.0 * PI * humFreq * humPhase / sampleRate) * 0.3
                    humPhase++

                    // Mix + volume
                    val mixed = (pink * 0.7 + hum * 0.3) * volume
                    buffer[i] = (mixed * Short.MAX_VALUE)
                        .coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
                        .toInt().toShort()
                }
                audioTrack.write(buffer, 0, buffer.size)
            }
        }
    }

    actual fun stop() {
        job?.cancel()
        job = null
        try {
            audioTrack.pause()
            audioTrack.flush()
        } catch (_: Exception) {}
    }

    actual fun release() {
        stop()
        try { audioTrack.release() } catch (_: Exception) {}
    }
}

// ── Composable helper ─────────────────────────────────────────────────────────

@Composable
actual fun rememberStaticHumPlayer(): StaticHumPlayer {
    val player = remember { StaticHumPlayer() }
    val scope  = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        player.start(scope)
    }

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    return player
}