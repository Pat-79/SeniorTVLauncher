package nl.awayfromhome.seniortvlauncher

import android.app.Application
import nl.awayfromhome.seniortvlauncher.data.SettingsRepository

class LauncherApplication : Application() {

    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(this)
    }
}
