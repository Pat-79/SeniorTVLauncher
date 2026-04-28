package nl.awayfromhome.seniortvlauncher.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.awayfromhome.seniortvlauncher.LauncherApplication
import nl.awayfromhome.seniortvlauncher.data.AppInfo
import nl.awayfromhome.seniortvlauncher.data.LauncherSettings
import nl.awayfromhome.seniortvlauncher.utils.AppUtils

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as LauncherApplication).settingsRepository

    private val _settings = MutableLiveData<LauncherSettings>()
    val settings: LiveData<LauncherSettings> = _settings

    private val _allApps = MutableLiveData<List<AppInfo>>()
    val allApps: LiveData<List<AppInfo>> = _allApps

    init {
        _settings.value = repository.load()
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = AppUtils.getAllLaunchableApps(getApplication())
            _allApps.postValue(apps)
        }
    }

    fun updateSettings(settings: LauncherSettings) {
        _settings.value = settings
    }

    fun saveSettings() {
        _settings.value?.let { repository.save(it) }
    }

    fun getCurrentSettings(): LauncherSettings {
        return _settings.value ?: repository.load()
    }
}
