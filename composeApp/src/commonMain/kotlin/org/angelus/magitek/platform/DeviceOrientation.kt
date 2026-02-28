// ─────────────────────────────────────────────────────────────────────────────
// commonMain/kotlin/org/angelus/magitek/platform/OrientationState.kt
// ─────────────────────────────────────────────────────────────────────────────

package org.angelus.magitek.platform

import androidx.compose.runtime.Composable

enum class DeviceOrientation { PORTRAIT, LANDSCAPE }

@Composable
expect fun rememberDeviceOrientation(): DeviceOrientation