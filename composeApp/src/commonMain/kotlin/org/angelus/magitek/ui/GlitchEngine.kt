// commonMain/kotlin/org/angelus/magitek/ui/GlitchEngine.kt

package org.angelus.magitek.ui

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * Moteur de glitches visuels.
 * Expose des états observables pour chaque effet.
 */
@Stable
class GlitchEngine {

    // ── États observables ─────────────────────────────────────────────────────

    /** Texte corrompu à afficher à la place des lignes normales (null = pas de glitch) */
    var corruptedLines by mutableStateOf<List<String>?>(null)
        private set

    /** Flash d'inversion de couleurs (0f = normal, 1f = inversé) */
    var flashIntensity by mutableStateOf(0f)
        private set

    /** Décalage horizontal des scanlines en dp */
    var scanlineShift by mutableStateOf(0f)
        private set

    /** Scintillement diode — override l'animation normale */
    var diodeFlicker by mutableStateOf(false)
        private set

    /** Tremblement de la grille en dp */
    var gridShake by mutableStateOf(0f)
        private set

    // ── Déclenchement ─────────────────────────────────────────────────────────

    fun triggerGlitch(scope: CoroutineScope, currentLines: List<String>) {
        // Choisit 1 à 3 effets simultanés au hasard
        val effects = listOf(
            ::glitchTextCorruption,
            ::glitchFlash,
            ::glitchScanlineShift,
            ::glitchDiodeFlicker,
            ::glitchGridShake,
        ).shuffled().take(Random.nextInt(1, 4))

        effects.forEach { effect ->
            scope.launch { effect(currentLines) }
        }
    }

    // ── Effets ────────────────────────────────────────────────────────────────

    private suspend fun glitchTextCorruption(lines: List<String>) {
        val glitchChars = "█▓▒░▄▀■□▪▫◆◇○●∎∏∑∂∆∇⌂⌐¬╔╗╚╝║═╠╣╦╩╬"
        val iterations  = Random.nextInt(3, 8)

        repeat(iterations) {
            corruptedLines = lines.map { line ->
                line.map { c ->
                    if (Random.nextFloat() < 0.15f) glitchChars.random()
                    else c
                }.joinToString("")
            }
            delay(Random.nextLong(40L, 100L))
        }
        corruptedLines = null
    }

    private suspend fun glitchFlash(@Suppress("UNUSED_PARAMETER") lines: List<String>) {
        val pulses = Random.nextInt(2, 5)
        repeat(pulses) {
            flashIntensity = Random.nextFloat() * 0.4f + 0.1f
            delay(Random.nextLong(30L, 80L))
            flashIntensity = 0f
            delay(Random.nextLong(20L, 60L))
        }
        flashIntensity = 0f
    }

    private suspend fun glitchScanlineShift(@Suppress("UNUSED_PARAMETER") lines: List<String>) {
        val steps = Random.nextInt(4, 10)
        repeat(steps) {
            scanlineShift = Random.nextFloat() * 12f - 6f   // -6dp à +6dp
            delay(Random.nextLong(30L, 80L))
        }
        scanlineShift = 0f
    }

    private suspend fun glitchDiodeFlicker(@Suppress("UNUSED_PARAMETER") lines: List<String>) {
        val duration = Random.nextLong(200L, 600L)
        diodeFlicker = true
        delay(duration)
        diodeFlicker = false
    }

    private suspend fun glitchGridShake(@Suppress("UNUSED_PARAMETER") lines: List<String>) {
        val steps = Random.nextInt(5, 12)
        repeat(steps) {
            gridShake = Random.nextFloat() * 6f - 3f   // -3dp à +3dp
            delay(Random.nextLong(25L, 60L))
        }
        gridShake = 0f
    }
}

@Composable
fun rememberGlitchEngine(): GlitchEngine = remember { GlitchEngine() }