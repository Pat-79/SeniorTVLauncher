package nl.awayfromhome.seniortvlauncher.ui.launcher

import android.util.TypedValue
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nl.awayfromhome.seniortvlauncher.data.AppInfo
import nl.awayfromhome.seniortvlauncher.data.ButtonShape
import nl.awayfromhome.seniortvlauncher.data.LauncherSettings

class AppGridAdapter(
    private val onAppClick: (AppInfo) -> Unit,
    private val onEmptySlotClick: (Int) -> Unit
) : RecyclerView.Adapter<AppGridAdapter.AppButtonViewHolder>() {

    private var settings: LauncherSettings = LauncherSettings()
    private var allApps: Map<String, AppInfo> = emptyMap()
    private var slots: List<AppInfo?> = emptyList()

    fun updateSettings(newSettings: LauncherSettings, apps: Map<String, AppInfo>) {
        settings = newSettings
        allApps = apps
        rebuildSlots()
    }

    private fun rebuildSlots() {
        val totalSlots = settings.rows * settings.columns
        val slotList = mutableListOf<AppInfo?>()
        for (i in 0 until totalSlots) {
            val packageName = settings.buttonAssignments[i]
            slotList.add(if (packageName != null) allApps[packageName] else null)
        }
        slots = slotList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppButtonViewHolder {
        val sizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            settings.buttonSizeDp.toFloat(),
            parent.context.resources.displayMetrics
        ).toInt()

        val view = AppButtonView(parent.context)
        val lp = ViewGroup.LayoutParams(sizePx, sizePx)
        view.layoutParams = lp

        return AppButtonViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppButtonViewHolder, position: Int) {
        val appInfo = slots.getOrNull(position)
        val buttonView = holder.buttonView

        if (appInfo != null) {
            buttonView.bindApp(appInfo, settings.showAppName, settings.buttonShape)
            buttonView.setOnClickListener { onAppClick(appInfo) }
        } else {
            buttonView.bindEmpty(settings.buttonShape)
            buttonView.setOnClickListener { onEmptySlotClick(position) }
        }

        buttonView.setOnFocusChangeListener { _, hasFocus ->
            buttonView.setFocusedState(hasFocus, settings.buttonShape)
        }
    }

    override fun getItemCount(): Int = slots.size

    class AppButtonViewHolder(val buttonView: AppButtonView) : RecyclerView.ViewHolder(buttonView)
}
