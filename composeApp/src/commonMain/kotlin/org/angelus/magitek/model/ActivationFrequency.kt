// commonMain/kotlin/org/angelus/magitek/model/ActivationFrequency.kt

package org.angelus.magitek.model

/**
 * Fréquences d'activation hardcodées.
 * Quand la molette est dans la plage [target - tolerance, target + tolerance],
 * la diode devient fixe et un son spécial joue.
 */
/*data class ActivationFrequency(
    val name     : String,
    val target   : Long,
    val tolerance: Long = 500L,
)

fun buildActivationFrequencies(): List<ActivationFrequency> = listOf(
    ActivationFrequency(name = "ÉLÉONORE",   target = 42000L,  tolerance = 400L),
    ActivationFrequency(name = "RÉSONNANCE", target = 131071L, tolerance = 600L),  // centre exact
    ActivationFrequency(name = "PROTOCOLE",  target = 210000L, tolerance = 400L),
)

fun List<ActivationFrequency>.detect(frequency: Long): ActivationFrequency? =
    firstOrNull { abs(frequency - it.target) <= it.tolerance }

private fun abs(x: Long) = if (x < 0) -x else x*/