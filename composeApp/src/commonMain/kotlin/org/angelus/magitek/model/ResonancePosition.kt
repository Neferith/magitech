// commonMain/kotlin/org/angelus/magitek/model/ResonanceSystem.kt

package org.angelus.magitek.model

import kotlin.math.sqrt

// ── Position RP ───────────────────────────────────────────────────────────────

data class ResonancePosition(
    val x     : Float,
    val y     : Float,
    val radius: Float,   // distance max pour une détection partielle
)

// ── Niveau de résonance ───────────────────────────────────────────────────────

data class ResonanceLevel(
    val frequency  : ActivationFrequency,
    val level      : Float,              // 0f → 1f
    val isComplete : Boolean = level >= 0.95f,
) {
    val percent: Int get() = (level * 100).toInt()
    val label  : String get() = when {
        level <= 0f    -> "AUCUN"
        level < 0.25f  -> "RÉSIDU"
        level < 0.50f  -> "FAIBLE"
        level < 0.75f  -> "PARTIEL"
        level < 0.95f  -> "FORT"
        else           -> "COMPLET"
    }
}

// ── ActivationFrequency mise à jour ───────────────────────────────────────────

data class ActivationFrequency(
    val name      : String,
    val target    : Long,
    val tolerance : Long = 500L,
    val position  : ResonancePosition? = null,  // null = signal voisin, plafonné
    val maxLevel  : Float = 1f,                 // plafond si pas de position
)

// ── Calcul du niveau ──────────────────────────────────────────────────────────

fun ActivationFrequency.computeLevel(currentX: Float, currentY: Float): Float {
    val pos = position ?: return maxLevel   // pas de position → niveau plafonné

    val distance = sqrt(
        (currentX - pos.x) * (currentX - pos.x) +
        (currentY - pos.y) * (currentY - pos.y)
    )

    return if (distance >= pos.radius) 0f
    else (1f - distance / pos.radius).coerceIn(0f, 1f)
}

fun List<ActivationFrequency>.detectWithLevel(
    frequency: Long,
    currentX : Float,
    currentY : Float,
): ResonanceLevel? {
    val match = firstOrNull { abs(frequency - it.target) <= it.tolerance }
        ?: return null

    val level = match.computeLevel(currentX, currentY)
    return if (level <= 0f) null
    else ResonanceLevel(frequency = match, level = level)
}

private fun abs(x: Long) = if (x < 0) -x else x

// ── Fréquences par défaut mises à jour ───────────────────────────────────────

fun buildActivationFrequencies(): List<ActivationFrequency> = listOf(
    ActivationFrequency(
        name      = "ÉLÉONORE",
        target    = 42000L,
        tolerance = 400L,
        position  = ResonancePosition(x = 150f, y = 200f, radius = 300f),
    ),
    ActivationFrequency(
        name      = "RÉSONNANCE",
        target    = 131071L,
        tolerance = 600L,
        position  = ResonancePosition(x = 500f, y = 500f, radius = 200f),
    ),
    ActivationFrequency(
        name      = "PROTOCOLE",
        target    = 210000L,
        tolerance = 400L,
        position  = null,      // signal voisin — plafonné à 60%
        maxLevel  = 0.6f,
    ),
)