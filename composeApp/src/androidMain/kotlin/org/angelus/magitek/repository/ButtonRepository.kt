package org.angelus.magitek.repository// androidMain/kotlin/org/angelus/magitek/repository/ButtonRepository.android.kt

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.serialization.encodeToString
import org.angelus.magitek.model.ButtonConfig
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }
private const val PREFS_NAME = "magitek_buttons"
private const val KEY_PREFIX  = "btn_"

actual class ButtonRepository(private val prefs: SharedPreferences) {

    actual fun loadConfigs(): Map<Int, ButtonConfig> {
        val result = mutableMapOf<Int, ButtonConfig>()
        prefs.all.forEach { (key, value) ->
            if (key.startsWith(KEY_PREFIX) && value is String) {
                try {
                    val config = json.decodeFromString<ButtonConfig>(value)
                    result[config.buttonIndex] = config
                } catch (_: Exception) { /* ignorer les entrées corrompues */ }
            }
        }
        return result
    }

    actual fun saveConfig(config: ButtonConfig) {
        prefs.edit()
            .putString("$KEY_PREFIX${config.buttonIndex}", json.encodeToString(config))
            .apply()
    }

    actual fun deleteConfig(buttonIndex: Int) {
        prefs.edit().remove("$KEY_PREFIX$buttonIndex").apply()
    }
}

@Composable
actual fun rememberButtonRepository(): ButtonRepository {
    val context = LocalContext.current
    return remember {
        ButtonRepository(
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )
    }
}