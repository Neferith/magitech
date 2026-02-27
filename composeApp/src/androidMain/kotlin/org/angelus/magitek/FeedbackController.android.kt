// androidMain/platform/FeedbackController.android.kt

// androidMain/platform/FeedbackController.android.kt
package org.angelus.magitek
// androidMain/kotlin/org/angelus/magitek/FeedbackController.android.kt

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlin.math.*

actual class FeedbackController(private val context: Context) {

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private val sampleRate = 44100
    private val audioTrack: AudioTrack by lazy { buildAudioTrack() }

    private fun buildAudioTrack(): AudioTrack {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        val pcm = generateMagitekBeep()
        val track = AudioTrack(
            attributes, format,
            pcm.size * 2,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE,
        )
        track.write(pcm, 0, pcm.size)
        return track
    }

    private fun generateMagitekBeep(): ShortArray {
        val totalMs = 120
        val samples = sampleRate * totalMs / 1000
        val buffer  = ShortArray(samples)
        val split   = samples / 3

        for (i in 0 until samples) {
            val t        = i.toDouble() / sampleRate
            val envelope = exp(-t * 25.0)
            val signal   = if (i < split) {
                sin(2.0 * PI * 880.0 * t)
            } else {
                0.7 * sin(2.0 * PI * 440.0 * (i - split).toDouble() / sampleRate)
            }
            val distorted = tanh(signal * 2.5) * 0.6
            buffer[i] = (distorted * envelope * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        return buffer
    }

    actual fun triggerCommandFeedback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30L)
        }
        try {
            audioTrack.stop()
            audioTrack.reloadStaticData()
            audioTrack.play()
        } catch (_: Exception) {}
    }

    actual fun release() {
        try {
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {}
    }
}

@Composable
actual fun rememberFeedbackController(): FeedbackController {
    val context = LocalContext.current
    return remember { FeedbackController(context) }
}