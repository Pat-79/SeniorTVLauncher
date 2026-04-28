package nl.awayfromhome.seniortvlauncher

import android.os.Bundle
import android.view.WindowManager
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
    }
}
