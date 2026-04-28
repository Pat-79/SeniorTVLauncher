package nl.awayfromhome.seniortvlauncher.ui.settings

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
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

    /**
     * Return a custom Dialog that intercepts number key presses from the remote control
     * (KEYCODE_0 – KEYCODE_9) so the user can enter digits without navigating to each
     * on-screen button first.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme) {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    val digit = event.keyCode - KeyEvent.KEYCODE_0
                    if (digit in 0..9) {
                        handleDirectDigit(digit.toString())
                        return true
                    }
                }
                return super.dispatchKeyEvent(event)
            }
        }
    }

    private fun handleDirectDigit(digit: String) {
        resetTimeout()
        if (enteredCode.length < 4) {
            enteredCode += digit
            _binding?.enteredCode?.text = enteredCode
            if (enteredCode.length == 4 && enteredCode == generatedCode) {
                val intent = Intent(requireContext(), SettingsActivity::class.java)
                dismiss()
                startActivity(intent)
            }
        }
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
                    // Auto-accept when the 4th digit completes the correct code
                    if (enteredCode.length == 4 && enteredCode == generatedCode) {
                        val intent = Intent(requireContext(), SettingsActivity::class.java)
                        dismiss()
                        startActivity(intent)
                    }
                    // If 4th digit is wrong: do nothing – let the user retry or press OK
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
        binding.enteredCode.text = enteredCode
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
