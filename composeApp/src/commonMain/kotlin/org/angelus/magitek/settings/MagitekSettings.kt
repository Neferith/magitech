// commonMain/kotlin/org/angelus/magitek/settings/MagitekSettings.kt

package org.angelus.magitek.settings

import androidx.compose.runtime.Composable

data class MagitekSettings(
    val locationId      : String  = "NONE",
    val randomVibration : Boolean = true,
    val humVolume       : Int     = 4,      // 0–100
)

@Composable
expect fun rememberMagitekSettings(): MagitekSettings