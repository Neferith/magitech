// ─────────────────────────────────────────────────────────────────────────────
// androidMain/kotlin/org/angelus/magitek/platform/OrientationState.android.kt
// ─────────────────────────────────────────────────────────────────────────────

package org.angelus.magitek.platform

import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration

@Composable
actual fun rememberDeviceOrientation(): DeviceOrientation {
    val configuration = LocalConfiguration.current
    return remember(configuration.orientation) {
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            DeviceOrientation.LANDSCAPE
        else
            DeviceOrientation.PORTRAIT
    }
}