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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

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

    actual fun triggerRandomVibration() {
        // Vibration courte et irrégulière — comme un parasite électrique
        val duration  = Random.nextLong(15L, 60L)
        val amplitude = Random.nextInt(40, 120)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    actual fun triggerActivationSound() {
        // Tonalité de verrouillage — deux impulsions montantes courtes
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
        scope.launch {
            playTone(frequency = 660.0, durationMs = 80, volume = 0.5)
            kotlinx.coroutines.delay(60L)
            playTone(frequency = 880.0, durationMs = 120, volume = 0.7)
        }
    }

    // Helper interne à ajouter dans la classe FeedbackController Android :
    private fun playTone(frequency: Double, durationMs: Int, volume: Double) {
        val samples = sampleRate * durationMs / 1000
        val buffer  = ShortArray(samples)
        for (i in 0 until samples) {
            val t        = i.toDouble() / sampleRate
            val envelope = kotlin.math.exp(-t * 8.0)
            val signal   = kotlin.math.sin(2.0 * kotlin.math.PI * frequency * t)
            buffer[i]    = (signal * envelope * volume * Short.MAX_VALUE)
                .coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
                .toInt().toShort()
        }
        val track = android.media.AudioTrack(
            android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
            android.media.AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            buffer.size * 2,
            android.media.AudioTrack.MODE_STATIC,
            android.media.AudioManager.AUDIO_SESSION_ID_GENERATE,
        )
        track.write(buffer, 0, buffer.size)
        track.play()
        // Le track se libère après lecture (non bloquant)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            kotlinx.coroutines.delay(durationMs.toLong() + 200L)
            track.release()
        }
    }

    actual fun triggerGlitchSound() {
        // Choisit aléatoirement parmi plusieurs types de glitch
        when (Random.nextInt(4)) {
            0 -> playGlitchBurst()      // rafale de bruit blanc court
            1 -> playFrequencyDrop()    // chute de fréquence rapide
            2 -> playStaticCrackle()    // craquement électrique sec
            3 -> playGlitchBurst()      // rafale courte (plus fréquent)
        }
    }

    /** Rafale de bruit blanc — comme une friture radio */
    private fun playGlitchBurst() {
        val durationMs = Random.nextInt(30, 120)
        val volume     = Random.nextDouble(0.05, 0.15)
        val samples    = sampleRate * durationMs / 1000
        val buffer     = ShortArray(samples)

        for (i in 0 until samples) {
            val t        = i.toDouble() / sampleRate
            val envelope = when {
                t < 0.005 -> t / 0.005                            // attaque 5ms
                t > (durationMs / 1000.0 - 0.01) ->
                    (durationMs / 1000.0 - t) / 0.01              // release 10ms
                else -> 1.0
            }
            // Bruit blanc avec légère teinte rose
            val noise = Random.nextDouble(-1.0, 1.0)
            buffer[i] = (noise * envelope * volume * Short.MAX_VALUE)
                .coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
                .toInt().toShort()
        }
        playSamples(buffer)
    }

    /** Chute de fréquence — signal qui décroche */
    private fun playFrequencyDrop() {
        val durationMs  = Random.nextInt(80, 200)
        val startFreq   = Random.nextDouble(400.0, 1200.0)
        val endFreq     = startFreq * Random.nextDouble(0.1, 0.4)
        val volume      = Random.nextDouble(0.04, 0.12)
        val samples     = sampleRate * durationMs / 1000
        val buffer      = ShortArray(samples)
        var phase       = 0.0

        for (i in 0 until samples) {
            val progress = i.toDouble() / samples
            val freq     = startFreq + (endFreq - startFreq) * progress
            val envelope = (1.0 - progress).pow(0.5)  // decay
            val noise    = Random.nextDouble(-0.3, 0.3)  // légère saleté
            phase       += freq / sampleRate
            val signal   = kotlin.math.sin(2.0 * kotlin.math.PI * phase) + noise
            buffer[i]    = (signal * envelope * volume * Short.MAX_VALUE)
                .coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
                .toInt().toShort()
        }
        playSamples(buffer)
    }

    /** Craquement électrique sec — impulsion très courte */
    private fun playStaticCrackle() {
        // Série de 2 à 5 impulsions rapprochées
        val count = Random.nextInt(2, 6)
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
        scope.launch {
            repeat(count) {
                val durationMs = Random.nextInt(8, 25)
                val volume     = Random.nextDouble(0.1, 0.25)
                val samples    = sampleRate * durationMs / 1000
                val buffer     = ShortArray(samples)
                for (i in 0 until samples) {
                    val t        = i.toDouble() / (samples - 1)
                    val envelope = kotlin.math.exp(-t * 20.0)
                    val noise    = Random.nextDouble(-1.0, 1.0)
                    buffer[i]    = (noise * envelope * volume * Short.MAX_VALUE)
                        .coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
                        .toInt().toShort()
                }
                playSamples(buffer)
                kotlinx.coroutines.delay(Random.nextLong(10L, 60L))
            }
        }
    }

    /** Lance la lecture d'un buffer PCM (non bloquant) */
    private fun playSamples(buffer: ShortArray) {
        try {
            val track = android.media.AudioTrack(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
                android.media.AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
                buffer.size * 2,
                android.media.AudioTrack.MODE_STATIC,
                android.media.AudioManager.AUDIO_SESSION_ID_GENERATE,
            )
            track.write(buffer, 0, buffer.size)
            track.play()
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                kotlinx.coroutines.delay(buffer.size.toLong() * 1000L / sampleRate + 200L)
                track.release()
            }
        } catch (_: Exception) {}
    }

    actual fun triggerDialClick() {
        // Vibration très courte et légère — sensation de cran
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(8L, 40))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(8L)
        }

        // Clic mécanique sec — tick bref à haute fréquence
        val durationMs = 12
        val samples    = sampleRate * durationMs / 1000
        val buffer     = ShortArray(samples)
        for (i in 0 until samples) {
            val t        = i.toDouble() / sampleRate
            val envelope = kotlin.math.exp(-t * 80.0)   // decay très rapide
            // Mix de deux fréquences pour un son plus riche
            val signal   = kotlin.math.sin(2.0 * kotlin.math.PI * 1800.0 * t) * 0.6 +
                    kotlin.math.sin(2.0 * kotlin.math.PI * 3200.0 * t) * 0.4
            buffer[i]    = (signal * envelope * 0.35 * Short.MAX_VALUE)
                .coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
                .toInt().toShort()
        }
        playSamples(buffer)
    }

    actual fun triggerErrorFeedback() {
        // Double impulsion vibration — pattern "erreur"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0L, 60L, 80L, 60L),   // délai, vib, pause, vib
                    intArrayOf(0, 180, 0, 180),         // amplitudes
                    -1,                                  // pas de repeat
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0L, 60L, 80L, 60L), -1)
        }

        // Son grave et court — deux tons descendants
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
        scope.launch {
            playErrorTone(freq = 220.0, durationMs = 80, volume = 0.3)
            delay(40L)
            playErrorTone(freq = 150.0, durationMs = 120, volume = 0.25)
        }
    }

    /** Génère un ton pur à la fréquence donnée */
    private fun playErrorTone(freq: Double, durationMs: Int, volume: Double) {
        val samples = sampleRate * durationMs / 1000
        val buffer  = ShortArray(samples)
        var phase   = 0.0
        for (i in 0 until samples) {
            val t        = i.toDouble() / samples
            val envelope = when {
                t < 0.05 -> t / 0.05
                t > 0.85 -> (1.0 - t) / 0.15
                else     -> 1.0
            }
            phase   += freq / sampleRate
            buffer[i] = (kotlin.math.sin(2.0 * kotlin.math.PI * phase) * envelope * volume * Short.MAX_VALUE)
                .coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble())
                .toInt().toShort()
        }
        playSamples(buffer)
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