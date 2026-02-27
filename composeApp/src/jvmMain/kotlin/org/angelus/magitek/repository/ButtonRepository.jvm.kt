package org.angelus.magitek.repository

import androidx.compose.runtime.Composable
import org.angelus.magitek.model.ButtonConfig

actual class ButtonRepository {
    actual fun loadConfigs(): Map<Int, ButtonConfig> {
        TODO("Not yet implemented")
    }

    actual fun saveConfig(config: ButtonConfig) {
    }

    actual fun deleteConfig(buttonIndex: Int) {
    }
}

@Composable
actual fun rememberButtonRepository(): ButtonRepository {
    TODO("Not yet implemented")
}