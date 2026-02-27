// androidMain/kotlin/org/angelus/magitek/SettingsActivity.kt

package org.angelus.magitek

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import org.angelus.magitek.model.buildLocations

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, MagitekSettingsFragment())
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

class MagitekSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        val screen  = preferenceManager.createPreferenceScreen(context)
        preferenceManager.sharedPreferencesName = "magitek_settings"

        // ── Catégorie : Localisation ──────────────────────────────────────────
        val catLocation = PreferenceCategory(context).apply {
            title = "LOCALISATION"
        }
        screen.addPreference(catLocation)

        val locationPref = ListPreference(context).apply {
            key           = PREF_LOCATION_ID
            title         = "Lieu actuel"
            summary       = "%s"
            dialogTitle   = "Sélectionner le lieu"

            val locations = buildLocations()
            entries       = locations.map { it.name }.toTypedArray()
            entryValues   = locations.map { it.id  }.toTypedArray()
            value         = value ?: "NONE"
        }
        catLocation.addPreference(locationPref)

        // ── Catégorie : Appareil ──────────────────────────────────────────────
        val catDevice = PreferenceCategory(context).apply {
            title = "APPAREIL"
        }
        screen.addPreference(catDevice)

        val vibrationPref = SwitchPreferenceCompat(context).apply {
            key          = PREF_RANDOM_VIBRATION
            title        = "Vibration aléatoire"
            summaryOn    = "Activée"
            summaryOff   = "Désactivée"
            setDefaultValue(true)
        }
        catDevice.addPreference(vibrationPref)

        val humVolumePref = SeekBarPreference(context).apply {
            key          = PREF_HUM_VOLUME
            title        = "Volume grésillement"
            min          = 0
            max          = 100
            setDefaultValue(4)
            showSeekBarValue = true
        }
        catDevice.addPreference(humVolumePref)

        preferenceScreen = screen
    }

    companion object {
        const val PREF_LOCATION_ID       = "location_id"
        const val PREF_RANDOM_VIBRATION  = "random_vibration"
        const val PREF_HUM_VOLUME        = "hum_volume"
        const val PREFS_NAME             = "magitek_settings"
    }
}