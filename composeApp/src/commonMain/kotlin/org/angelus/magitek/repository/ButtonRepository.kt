package org.angelus.magitek.repository// commonMain/kotlin/org/angelus/magitek/repository/ButtonRepository.kt

import androidx.compose.runtime.Composable
import org.angelus.magitek.model.ButtonConfig

/**
 * Persistance de la configuration des boutons.
 * expect/actual : SharedPreferences sur Android.
 */
expect class ButtonRepository {
    fun loadConfigs(): Map<Int, ButtonConfig>
    fun saveConfig(config: ButtonConfig)
    fun deleteConfig(buttonIndex: Int)
}

@Composable
expect fun rememberButtonRepository(): ButtonRepository