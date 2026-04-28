package nl.awayfromhome.seniortvlauncher.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): LauncherSettings {
        val assignmentsJson = prefs.getString(KEY_BUTTON_ASSIGNMENTS, "{}")
        val buttonAssignments = mutableMapOf<Int, String>()
        try {
            val json = JSONObject(assignmentsJson ?: "{}")
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                buttonAssignments[key.toInt()] = json.getString(key)
            }
        } catch (e: Exception) {
            // ignore malformed JSON, use empty map
        }

        val shapeOrdinal = prefs.getInt(KEY_BUTTON_SHAPE, ButtonShape.ROUNDED_SQUARE.ordinal)
        val buttonShape = ButtonShape.entries.getOrElse(shapeOrdinal) { ButtonShape.ROUNDED_SQUARE }

        return LauncherSettings(
            rows = prefs.getInt(KEY_ROWS, 3),
            columns = prefs.getInt(KEY_COLUMNS, 4),
            buttonSizeDp = prefs.getInt(KEY_BUTTON_SIZE_DP, 120),
            showAppName = prefs.getBoolean(KEY_SHOW_APP_NAME, true),
            buttonShape = buttonShape,
            backgroundImageUri = prefs.getString(KEY_BACKGROUND_IMAGE_URI, null),
            backgroundBlurEnabled = prefs.getBoolean(KEY_BACKGROUND_BLUR_ENABLED, false),
            backgroundBlurLevel = prefs.getInt(KEY_BACKGROUND_BLUR_LEVEL, 10),
            showClock = prefs.getBoolean(KEY_SHOW_CLOCK, true),
            showDate = prefs.getBoolean(KEY_SHOW_DATE, true),
            clickSoundEnabled = prefs.getBoolean(KEY_CLICK_SOUND_ENABLED, true),
            buttonAssignments = buttonAssignments
        )
    }

    fun save(settings: LauncherSettings) {
        val json = JSONObject()
        settings.buttonAssignments.forEach { (index, pkg) ->
            json.put(index.toString(), pkg)
        }

        prefs.edit()
            .putInt(KEY_ROWS, settings.rows)
            .putInt(KEY_COLUMNS, settings.columns)
            .putInt(KEY_BUTTON_SIZE_DP, settings.buttonSizeDp)
            .putBoolean(KEY_SHOW_APP_NAME, settings.showAppName)
            .putInt(KEY_BUTTON_SHAPE, settings.buttonShape.ordinal)
            .putString(KEY_BACKGROUND_IMAGE_URI, settings.backgroundImageUri)
            .putBoolean(KEY_BACKGROUND_BLUR_ENABLED, settings.backgroundBlurEnabled)
            .putInt(KEY_BACKGROUND_BLUR_LEVEL, settings.backgroundBlurLevel)
            .putBoolean(KEY_SHOW_CLOCK, settings.showClock)
            .putBoolean(KEY_SHOW_DATE, settings.showDate)
            .putBoolean(KEY_CLICK_SOUND_ENABLED, settings.clickSoundEnabled)
            .putString(KEY_BUTTON_ASSIGNMENTS, json.toString())
            .apply()
    }

    fun registerChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        const val PREFS_NAME = "launcher_settings"
        private const val KEY_ROWS = "rows"
        private const val KEY_COLUMNS = "columns"
        private const val KEY_BUTTON_SIZE_DP = "button_size_dp"
        private const val KEY_SHOW_APP_NAME = "show_app_name"
        private const val KEY_BUTTON_SHAPE = "button_shape"
        private const val KEY_BACKGROUND_IMAGE_URI = "background_image_uri"
        private const val KEY_BACKGROUND_BLUR_ENABLED = "background_blur_enabled"
        private const val KEY_BACKGROUND_BLUR_LEVEL = "background_blur_level"
        private const val KEY_SHOW_CLOCK = "show_clock"
        private const val KEY_SHOW_DATE = "show_date"
        private const val KEY_CLICK_SOUND_ENABLED = "click_sound_enabled"
        private const val KEY_BUTTON_ASSIGNMENTS = "button_assignments"
    }
}
