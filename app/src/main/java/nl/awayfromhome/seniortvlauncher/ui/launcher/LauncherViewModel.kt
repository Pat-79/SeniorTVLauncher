package nl.awayfromhome.seniortvlauncher.ui.launcher

import android.app.Application
import android.content.SharedPreferences
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

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as LauncherApplication).settingsRepository

    private val _settings = MutableLiveData<LauncherSettings>()
    val settings: LiveData<LauncherSettings> = _settings

    private val _allApps = MutableLiveData<List<AppInfo>>()
    val allApps: LiveData<List<AppInfo>> = _allApps

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        reloadSettings()
    }

    init {
        reloadSettings()
        loadApps()
        repository.registerChangeListener(prefsListener)
    }

    fun reloadSettings() {
        _settings.postValue(repository.load())
    }

    fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = AppUtils.getAllLaunchableApps(getApplication())
            _allApps.postValue(apps)
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.unregisterChangeListener(prefsListener)
    }
}
