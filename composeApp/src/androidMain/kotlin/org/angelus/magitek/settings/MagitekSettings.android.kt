// androidMain/kotlin/org/angelus/magitek/settings/MagitekSettings.android.kt

package org.angelus.magitek.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberMagitekSettings(): MagitekSettings {
    val context = LocalContext.current
    val prefs   = remember {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var settings by remember { mutableStateOf(prefs.toSettings()) }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            settings = prefs.toSettings()
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    return settings
}

// Contexte global pour saveFrequency (initialisé dans MainActivity)
lateinit var appContext: Context

actual fun saveFrequency(frequency: Long) {
    appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putLong(PREF_LAST_FREQUENCY, frequency)
        .apply()
}

// Note : EditTextPreference stocke en String — adapter toSettings() :
private fun SharedPreferences.toSettings() = MagitekSettings(
    locationId      = getString(PREF_LOCATION_ID, "NONE") ?: "NONE",
    currentX        = getString(PREF_CURRENT_X, "0")?.toFloatOrNull() ?: 0f,
    currentY        = getString(PREF_CURRENT_Y, "0")?.toFloatOrNull() ?: 0f,
    randomVibration = getBoolean(PREF_RANDOM_VIBRATION, true),
    humVolume       = getInt(PREF_HUM_VOLUME, 4),
    lastFrequency = getLong(PREF_LAST_FREQUENCY, 0L),
)

fun saveFrequency(context: Context, frequency: Long) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putLong(PREF_LAST_FREQUENCY, frequency)
        .apply()
}

// Ajouter les constantes :
const val PREF_CURRENT_X = "current_x"
const val PREF_CURRENT_Y = "current_y"

const val PREF_LAST_FREQUENCY = "last_frequency"

const val PREFS_NAME            = "magitek_settings"
const val PREF_LOCATION_ID      = "location_id"
const val PREF_RANDOM_VIBRATION = "random_vibration"
const val PREF_HUM_VOLUME       = "hum_volume"