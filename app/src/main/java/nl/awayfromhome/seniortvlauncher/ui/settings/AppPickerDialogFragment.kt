package nl.awayfromhome.seniortvlauncher.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.awayfromhome.seniortvlauncher.R
import nl.awayfromhome.seniortvlauncher.data.AppInfo
import nl.awayfromhome.seniortvlauncher.databinding.DialogAppPickerBinding
import nl.awayfromhome.seniortvlauncher.databinding.ItemAppPickerBinding

class AppPickerDialogFragment : DialogFragment() {

    interface AppPickerListener {
        fun onAppSelected(packageName: String)
    }

    private var _binding: DialogAppPickerBinding? = null
    private val binding get() = _binding!!

    var apps: List<AppInfo> = emptyList()
    var listener: AppPickerListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_SeniorTVLauncher_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAppPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appPickerRecycler.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.appPickerRecycler.adapter = AppPickerAdapter(apps) { packageName ->
            listener?.onAppSelected(packageName)
            dismiss()
        }

        binding.btnCancel.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class AppPickerAdapter(
        private val items: List<AppInfo>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<AppPickerAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemAppPickerBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemAppPickerBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = items[position]
            holder.binding.appIcon.setImageDrawable(app.icon)
            holder.binding.appName.text = app.label
            holder.itemView.setOnClickListener { onClick(app.packageName) }
            holder.itemView.setOnFocusChangeListener { v, hasFocus ->
                v.scaleX = if (hasFocus) 1.1f else 1.0f
                v.scaleY = if (hasFocus) 1.1f else 1.0f
            }
        }

        override fun getItemCount() = items.size
    }
}
