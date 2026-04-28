package nl.awayfromhome.seniortvlauncher

import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import nl.awayfromhome.seniortvlauncher.databinding.ActivityMainBinding
import nl.awayfromhome.seniortvlauncher.ui.launcher.LauncherFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LauncherFragment(), "launcher_fragment")
                .commit()
        }

        maybePromptSetDefaultLauncher()
    }

    /**
     * On first launch, if SeniorTVLauncher is not already the default home app, ask the
     * user whether they want to set it as the default launcher.  The prompt is shown only
     * once (stored in SharedPreferences).
     */
    private fun maybePromptSetDefaultLauncher() {
        val prefs = getSharedPreferences("launcher_meta", MODE_PRIVATE)
        if (prefs.getBoolean("asked_default_launcher", false)) return

        prefs.edit().putBoolean("asked_default_launcher", true).apply()

        if (isDefaultLauncher()) return

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.default_launcher_dialog_title))
            .setMessage(getString(R.string.default_launcher_dialog_message))
            .setPositiveButton(getString(R.string.default_launcher_dialog_yes)) { _, _ ->
                launchSetDefaultHome()
            }
            .setNegativeButton(getString(R.string.default_launcher_dialog_no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val defaultPackage: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager
                .queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
                .firstOrNull()
                ?.activityInfo?.packageName
        } else {
            @Suppress("DEPRECATION")
            packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
        }
        return defaultPackage == packageName
    }

    private fun launchSetDefaultHome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                startActivity(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
                return
            }
        }
        // Fallback for older API levels
        startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
    }
}
