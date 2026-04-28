package nl.awayfromhome.seniortvlauncher.ui.settings

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import nl.awayfromhome.seniortvlauncher.R
import nl.awayfromhome.seniortvlauncher.databinding.DialogPinBinding

class SettingsPinDialogFragment : DialogFragment() {

    private var _binding: DialogPinBinding? = null
    private val binding get() = _binding!!

    private var generatedCode: String = ""
    private var enteredCode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_SeniorTVLauncher_Dialog)
        generatedCode = (1000..9999).random().toString()
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
    }

    private fun setupNumPad() {
        val numButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3,
            binding.btn4, binding.btn5, binding.btn6, binding.btn7,
            binding.btn8, binding.btn9
        )

        numButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (enteredCode.length < 4) {
                    enteredCode += index.toString()
                    updateEnteredDisplay()
                }
            }
        }

        binding.btnBackspace.setOnClickListener {
            if (enteredCode.isNotEmpty()) {
                enteredCode = enteredCode.dropLast(1)
                updateEnteredDisplay()
            }
        }

        binding.btnConfirm.setOnClickListener {
            verifyCode()
        }
    }

    private fun updateEnteredDisplay() {
        val masked = "●".repeat(enteredCode.length) + "○".repeat(4 - enteredCode.length)
        binding.enteredCode.text = masked
    }

    private fun verifyCode() {
        if (enteredCode == generatedCode) {
            dismiss()
            val intent = android.content.Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        } else {
            enteredCode = ""
            updateEnteredDisplay()
            Toast.makeText(requireContext(), getString(R.string.incorrect_code), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
