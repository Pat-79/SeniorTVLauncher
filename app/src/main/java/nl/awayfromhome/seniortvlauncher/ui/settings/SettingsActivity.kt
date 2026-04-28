package nl.awayfromhome.seniortvlauncher.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import nl.awayfromhome.seniortvlauncher.R
import nl.awayfromhome.seniortvlauncher.data.ButtonShape
import nl.awayfromhome.seniortvlauncher.data.LauncherSettings
import nl.awayfromhome.seniortvlauncher.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity(), AppPickerDialogFragment.AppPickerListener {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    private var pendingSlotIndex: Int = -1

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val current = viewModel.getCurrentSettings()
                viewModel.updateSettings(current.copy(backgroundImageUri = uri.toString()))
                refreshUi()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        setupSaveButton()
    }

    private fun observeViewModel() {
        viewModel.settings.observe(this) { refreshUi() }
        viewModel.allApps.observe(this) { refreshAppSlots() }
    }

    private fun refreshUi() {
        val s = viewModel.getCurrentSettings()
        populateDisplaySection(s)
        populateBackgroundSection(s)
        populateClockSection(s)
        refreshAppSlots()
    }

    private fun populateDisplaySection(s: LauncherSettings) {
        binding.rowsValue.text = s.rows.toString()
        binding.columnsValue.text = s.columns.toString()

        binding.btnRowsMinus.setOnClickListener {
            val cur = viewModel.getCurrentSettings()
            if (cur.rows > 1) viewModel.updateSettings(cur.copy(rows = cur.rows - 1))
            binding.rowsValue.text = viewModel.getCurrentSettings().rows.toString()
            refreshAppSlots()
        }
        binding.btnRowsPlus.setOnClickListener {
            val cur = viewModel.getCurrentSettings()
            if (cur.rows < 6) viewModel.updateSettings(cur.copy(rows = cur.rows + 1))
            binding.rowsValue.text = viewModel.getCurrentSettings().rows.toString()
            refreshAppSlots()
        }
        binding.btnColumnsMinus.setOnClickListener {
            val cur = viewModel.getCurrentSettings()
            if (cur.columns > 1) viewModel.updateSettings(cur.copy(columns = cur.columns - 1))
            binding.columnsValue.text = viewModel.getCurrentSettings().columns.toString()
            refreshAppSlots()
        }
        binding.btnColumnsPlus.setOnClickListener {
            val cur = viewModel.getCurrentSettings()
            if (cur.columns < 8) viewModel.updateSettings(cur.copy(columns = cur.columns + 1))
            binding.columnsValue.text = viewModel.getCurrentSettings().columns.toString()
            refreshAppSlots()
        }

        binding.buttonSizeSeekbar.progress = s.buttonSizeDp - 80
        binding.buttonSizeLabel.text = "${s.buttonSizeDp}dp"
        binding.buttonSizeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val newSize = progress + 80
                binding.buttonSizeLabel.text = "${newSize}dp"
                viewModel.updateSettings(viewModel.getCurrentSettings().copy(buttonSizeDp = newSize))
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.switchShowAppName.isChecked = s.showAppName
        binding.switchShowAppName.setOnCheckedChangeListener { _, checked ->
            viewModel.updateSettings(viewModel.getCurrentSettings().copy(showAppName = checked))
        }

        when (s.buttonShape) {
            ButtonShape.CIRCLE -> binding.radioCircle.isChecked = true
            ButtonShape.ROUNDED_SQUARE -> binding.radioRoundedSquare.isChecked = true
            ButtonShape.SQUARE -> binding.radioSquare.isChecked = true
        }
        binding.radioGroupShape.setOnCheckedChangeListener { _, checkedId ->
            val shape = when (checkedId) {
                R.id.radio_circle -> ButtonShape.CIRCLE
                R.id.radio_rounded_square -> ButtonShape.ROUNDED_SQUARE
                R.id.radio_square -> ButtonShape.SQUARE
                else -> ButtonShape.ROUNDED_SQUARE
            }
            viewModel.updateSettings(viewModel.getCurrentSettings().copy(buttonShape = shape))
        }
    }

    private fun populateBackgroundSection(s: LauncherSettings) {
        binding.btnSetBackground.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            pickImageLauncher.launch(intent)
        }

        binding.btnRemoveBackground.isEnabled = s.backgroundImageUri != null
        binding.btnRemoveBackground.setOnClickListener {
            viewModel.updateSettings(viewModel.getCurrentSettings().copy(backgroundImageUri = null))
            binding.btnRemoveBackground.isEnabled = false
        }

        binding.switchEnableBlur.isChecked = s.backgroundBlurEnabled
        binding.blurLevelSeekbar.isEnabled = s.backgroundBlurEnabled
        binding.blurLevelSeekbar.progress = s.backgroundBlurLevel

        binding.switchEnableBlur.setOnCheckedChangeListener { _, checked ->
            viewModel.updateSettings(viewModel.getCurrentSettings().copy(backgroundBlurEnabled = checked))
            binding.blurLevelSeekbar.isEnabled = checked
        }

        binding.blurLevelLabel.text = "${s.backgroundBlurLevel}"
        binding.blurLevelSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.blurLevelLabel.text = "$progress"
                viewModel.updateSettings(viewModel.getCurrentSettings().copy(backgroundBlurLevel = progress))
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun populateClockSection(s: LauncherSettings) {
        binding.switchShowClock.isChecked = s.showClock
        binding.switchShowClock.setOnCheckedChangeListener { _, checked ->
            viewModel.updateSettings(viewModel.getCurrentSettings().copy(showClock = checked))
        }

        binding.switchShowDate.isChecked = s.showDate
        binding.switchShowDate.setOnCheckedChangeListener { _, checked ->
            viewModel.updateSettings(viewModel.getCurrentSettings().copy(showDate = checked))
        }

        binding.switchClickSound.isChecked = s.clickSoundEnabled
        binding.switchClickSound.setOnCheckedChangeListener { _, checked ->
            viewModel.updateSettings(viewModel.getCurrentSettings().copy(clickSoundEnabled = checked))
        }
    }

    private fun refreshAppSlots() {
        val s = viewModel.getCurrentSettings()
        val apps = viewModel.allApps.value ?: emptyList()
        val appMap = apps.associateBy { it.packageName }
        val totalSlots = s.rows * s.columns
        val assignments = s.buttonAssignments.toMutableMap()

        binding.appSlotsContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        for (i in 0 until totalSlots) {
            val pkg = assignments[i]
            val app = pkg?.let { appMap[it] }

            val row = inflater.inflate(R.layout.item_app_slot_row, binding.appSlotsContainer, false)
            val slotLabel = row.findViewById<TextView>(R.id.slot_label)
            val slotIcon = row.findViewById<ImageView>(R.id.slot_app_icon)
            val slotName = row.findViewById<TextView>(R.id.slot_app_name)
            val btnSelect = row.findViewById<Button>(R.id.btn_select_app)
            val btnRemove = row.findViewById<Button>(R.id.btn_remove_app)
            val btnMoveUp = row.findViewById<Button>(R.id.btn_move_up)
            val btnMoveDown = row.findViewById<Button>(R.id.btn_move_down)

            slotLabel.text = getString(R.string.slot_number, i + 1)

            if (app != null) {
                slotIcon.setImageDrawable(app.icon)
                slotIcon.visibility = View.VISIBLE
                slotName.text = app.label
                slotName.visibility = View.VISIBLE
                btnSelect.text = getString(R.string.change_app)
                btnRemove.isEnabled = true
            } else {
                slotIcon.visibility = View.GONE
                slotName.text = getString(R.string.empty_slot)
                slotName.visibility = View.VISIBLE
                btnSelect.text = getString(R.string.select_app)
                btnRemove.isEnabled = false
            }

            btnSelect.setOnClickListener {
                pendingSlotIndex = i
                openAppPicker()
            }

            btnRemove.setOnClickListener {
                val updated = assignments.toMutableMap()
                updated.remove(i)
                viewModel.updateSettings(s.copy(buttonAssignments = updated))
                refreshAppSlots()
            }

            btnMoveUp.isEnabled = i > 0
            btnMoveDown.isEnabled = i < totalSlots - 1

            btnMoveUp.setOnClickListener {
                val updated = assignments.toMutableMap()
                val tmp = updated[i - 1]
                if (updated[i] != null) updated[i - 1] = updated[i]!! else updated.remove(i - 1)
                if (tmp != null) updated[i] = tmp else updated.remove(i)
                viewModel.updateSettings(s.copy(buttonAssignments = updated))
                refreshAppSlots()
            }

            btnMoveDown.setOnClickListener {
                val updated = assignments.toMutableMap()
                val tmp = updated[i + 1]
                if (updated[i] != null) updated[i + 1] = updated[i]!! else updated.remove(i + 1)
                if (tmp != null) updated[i] = tmp else updated.remove(i)
                viewModel.updateSettings(s.copy(buttonAssignments = updated))
                refreshAppSlots()
            }

            binding.appSlotsContainer.addView(row)
        }
    }

    private fun openAppPicker() {
        val apps = viewModel.allApps.value ?: emptyList()
        val dialog = AppPickerDialogFragment()
        dialog.apps = apps
        dialog.listener = this
        dialog.show(supportFragmentManager, "app_picker")
    }

    override fun onAppSelected(packageName: String) {
        if (pendingSlotIndex >= 0) {
            val s = viewModel.getCurrentSettings()
            val updated = s.buttonAssignments.toMutableMap()
            updated[pendingSlotIndex] = packageName
            viewModel.updateSettings(s.copy(buttonAssignments = updated))
            pendingSlotIndex = -1
            refreshAppSlots()
        }
    }

    private fun setupSaveButton() {
        binding.btnOpenDeviceSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        binding.btnSave.setOnClickListener {
            viewModel.saveSettings()
            Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
