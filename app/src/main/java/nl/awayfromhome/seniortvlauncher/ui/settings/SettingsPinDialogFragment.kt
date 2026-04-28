package nl.awayfromhome.seniortvlauncher.ui.settings

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import nl.awayfromhome.seniortvlauncher.R
import nl.awayfromhome.seniortvlauncher.databinding.DialogPinBinding
import nl.awayfromhome.seniortvlauncher.ui.launcher.LauncherFragment

class SettingsPinDialogFragment : DialogFragment() {

    private var _binding: DialogPinBinding? = null
    private val binding get() = _binding!!

    private var generatedCode: String = ""
    private var enteredCode: String = ""

    private val timeoutHandler = Handler(Looper.getMainLooper())
    private var remainingSeconds = TIMEOUT_SECONDS

    private val countdownRunnable = object : Runnable {
        override fun run() {
            remainingSeconds--
            if (remainingSeconds <= 0) {
                dismiss()
            } else {
                updateTimeoutHint()
                timeoutHandler.postDelayed(this, COUNTDOWN_INTERVAL_MS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_SeniorTVLauncher_Dialog)
        generatedCode = String.format("%04d", (0..9999).random())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.generatedCode.text = generatedCode
        binding.enteredCode.text = ""

        setupNumPad()

        binding.btn1.requestFocus()
        resetTimeout()
    }

    private fun resetTimeout() {
        timeoutHandler.removeCallbacks(countdownRunnable)
        remainingSeconds = TIMEOUT_SECONDS
        updateTimeoutHint()
        timeoutHandler.postDelayed(countdownRunnable, COUNTDOWN_INTERVAL_MS)
    }

    private fun updateTimeoutHint() {
        _binding?.timeoutHint?.text = getString(R.string.timeout_hint, remainingSeconds)
    }

    private fun setupNumPad() {
        val numButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3,
            binding.btn4, binding.btn5, binding.btn6, binding.btn7,
            binding.btn8, binding.btn9
        )

        numButtons.forEachIndexed { _, button ->
            button.setOnClickListener {
                resetTimeout()
                if (enteredCode.length < 4) {
                    enteredCode += (button.text as CharSequence).toString()
                    updateEnteredDisplay()
                }
            }
        }

        binding.btnDelete.setOnClickListener {
            resetTimeout()
            if (enteredCode.isNotEmpty()) {
                enteredCode = enteredCode.dropLast(1)
                updateEnteredDisplay()
            }
        }

        binding.btnConfirm.setOnClickListener {
            resetTimeout()
            verifyCode()
        }
    }

    private fun updateEnteredDisplay() {
        val masked = "●".repeat(enteredCode.length) + "○".repeat(4 - enteredCode.length)
        binding.enteredCode.text = masked
    }

    private fun verifyCode() {
        if (enteredCode == generatedCode) {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            dismiss()
            startActivity(intent)
        } else {
            enteredCode = ""
            updateEnteredDisplay()
            Toast.makeText(requireContext(), getString(R.string.incorrect_code), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (parentFragmentManager.findFragmentByTag("launcher_fragment") as? LauncherFragment)
            ?.focusFirstTile()
    }

    override fun onDestroyView() {
        timeoutHandler.removeCallbacks(countdownRunnable)
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TIMEOUT_SECONDS = 10
        private const val COUNTDOWN_INTERVAL_MS = 1_000L
    }
}
