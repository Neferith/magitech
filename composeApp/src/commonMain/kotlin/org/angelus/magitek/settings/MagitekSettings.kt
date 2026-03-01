// commonMain/kotlin/org/angelus/magitek/settings/MagitekSettings.kt

package org.angelus.magitek.settings

import androidx.compose.runtime.Composable

data class MagitekSettings(
    val locationId      : String  = "NONE",
    val currentX        : Float   = 0f,     // ← nouveau
    val currentY        : Float   = 0f,     // ← nouveau
    val randomVibration : Boolean = true,
    val humVolume       : Int     = 4,
    val lastFrequency   : Long    = 0L,    // ← nouveau
)

@Composable
expect fun rememberMagitekSettings(): MagitekSettings

expect fun saveFrequency(frequency: Long)

// commonMain
expect fun openSettings()