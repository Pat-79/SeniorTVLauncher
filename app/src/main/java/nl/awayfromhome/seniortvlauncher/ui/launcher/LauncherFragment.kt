package nl.awayfromhome.seniortvlauncher.ui.launcher

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import nl.awayfromhome.seniortvlauncher.R
import androidx.core.content.ContextCompat
import nl.awayfromhome.seniortvlauncher.data.LauncherSettings
import nl.awayfromhome.seniortvlauncher.databinding.FragmentLauncherBinding
import nl.awayfromhome.seniortvlauncher.ui.settings.SettingsPinDialogFragment
import nl.awayfromhome.seniortvlauncher.utils.AppUtils
import nl.awayfromhome.seniortvlauncher.utils.BlurUtils
import nl.awayfromhome.seniortvlauncher.utils.DateTimeUtils

class LauncherFragment : Fragment() {

    private var _binding: FragmentLauncherBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LauncherViewModel by viewModels()
    private lateinit var adapter: AppGridAdapter
    private var defaultTitle: String = ""
    private val dateHandler = Handler(Looper.getMainLooper())
    private val dateRunnable = object : Runnable {
        override fun run() {
            updateDate()
            dateHandler.postDelayed(this, 60_000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLauncherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultTitle = getString(R.string.app_name)

        applyHeaderFrostedEffect()
        setupAdapter()
        setupSettingsButton()
        observeViewModel()
    }

    fun focusFirstTile() {
        val grid = _binding?.appGrid ?: return
        for (i in 0 until grid.childCount) {
            val child = grid.getChildAt(i)
            if (child != null && child.isFocusable && child.isEnabled) {
                child.requestFocus()
                return
            }
        }
        _binding?.settingsButton?.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        dateHandler.post(dateRunnable)
        viewModel.reloadSettings()
        viewModel.loadApps()
        binding.appGrid.post { focusFirstTile() }
    }

    override fun onPause() {
        super.onPause()
        dateHandler.removeCallbacks(dateRunnable)
        settingsHoldRunnable?.let { settingsHoldHandler.removeCallbacks(it) }
        settingsHoldRunnable = null
    }

    private fun applyHeaderFrostedEffect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.topBar.setBackgroundBlurRadius(20)
        }
    }

    private fun setupAdapter() {
        adapter = AppGridAdapter(
            onAppClick = { appInfo ->
                AppUtils.launchApp(requireContext(), appInfo.packageName)
            },
            onEmptySlotClick = { _ ->
                // Empty slots are handled through settings
            },
            onAppFocused = { appInfo ->
                binding.appTitle.text = appInfo?.label ?: defaultTitle
            }
        )
        binding.appGrid.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.appGrid.adapter = adapter
    }

    private val settingsHoldHandler = Handler(Looper.getMainLooper())
    private var settingsHoldRunnable: Runnable? = null

    private fun setupSettingsButton() {
        binding.settingsButton.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    settingsHoldRunnable = Runnable { showPinDialog() }
                    settingsHoldHandler.postDelayed(settingsHoldRunnable!!, 3000)
                    true
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    settingsHoldRunnable?.let { settingsHoldHandler.removeCallbacks(it) }
                    settingsHoldRunnable = null
                    binding.settingsButton.performClick()
                    true
                }
                else -> false
            }
        }
        // Also support D-pad long press (KEY_DOWN held)
        binding.settingsButton.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
                keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                when (event.action) {
                    android.view.KeyEvent.ACTION_DOWN -> {
                        if (event.repeatCount == 0) {
                            settingsHoldRunnable = Runnable { showPinDialog() }
                            settingsHoldHandler.postDelayed(settingsHoldRunnable!!, 3000)
                        }
                        true
                    }
                    android.view.KeyEvent.ACTION_UP -> {
                        settingsHoldRunnable?.let { settingsHoldHandler.removeCallbacks(it) }
                        settingsHoldRunnable = null
                        true
                    }
                    else -> false
                }
            } else false
        }
        binding.settingsButton.setOnFocusChangeListener { _, hasFocus ->
            binding.settingsButton.alpha = if (hasFocus) 1.0f else 0.5f
            if (hasFocus) {
                binding.appTitle.text = defaultTitle
            }
        }
    }

    private fun showPinDialog() {
        val dialog = SettingsPinDialogFragment()
        dialog.show(parentFragmentManager, "pin_dialog")
    }

    private fun observeViewModel() {
        viewModel.settings.observe(viewLifecycleOwner) { settings ->
            applySettings(settings)
        }

        viewModel.allApps.observe(viewLifecycleOwner) { apps ->
            val currentSettings = viewModel.settings.value ?: return@observe
            val appMap = apps.associateBy { it.packageName }
            adapter.updateSettings(currentSettings, appMap)
            binding.appGrid.post { focusFirstTile() }
        }
    }

    private fun applySettings(settings: LauncherSettings) {
        // Clock visibility
        binding.clockView.visibility = if (settings.showClock) View.VISIBLE else View.GONE

        // Date visibility
        binding.dateView.visibility = if (settings.showDate) View.VISIBLE else View.GONE
        if (settings.showDate) updateDate()

        // Grid layout
        val columns = settings.columns
        val layoutManager = binding.appGrid.layoutManager
        if (layoutManager is GridLayoutManager) {
            layoutManager.spanCount = columns
        } else {
            binding.appGrid.layoutManager = GridLayoutManager(requireContext(), columns)
        }

        // Update adapter
        val apps = viewModel.allApps.value ?: emptyList()
        val appMap = apps.associateBy { it.packageName }
        adapter.updateSettings(settings, appMap)
        binding.appGrid.post { focusFirstTile() }

        // Background
        loadBackground(settings)
    }

    private fun updateDate() {
        val dateText = DateTimeUtils.getFormattedDate(requireContext())
        binding.dateView.text = dateText
    }

    private fun loadBackground(settings: LauncherSettings) {
        val uri = settings.backgroundImageUri
        if (uri != null) {
            try {
                val parsedUri = Uri.parse(uri)
                Glide.with(this)
                    .asBitmap()
                    .load(parsedUri)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            val finalBitmap = if (settings.backgroundBlurEnabled) {
                                BlurUtils.blur(requireContext(), resource, settings.backgroundBlurLevel)
                            } else {
                                resource
                            }
                            binding.root.background = BitmapDrawable(resources, finalBitmap)
                        }

                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                            binding.root.setBackgroundColor(
                                ContextCompat.getColor(requireContext(), R.color.background_default)
                            )
                        }
                    })
            } catch (e: Exception) {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.background_default)
                )
            }
        } else {
            binding.root.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_dot_pattern)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
