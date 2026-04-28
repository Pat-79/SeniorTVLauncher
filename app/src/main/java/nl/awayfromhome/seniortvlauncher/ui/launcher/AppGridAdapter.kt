package nl.awayfromhome.seniortvlauncher.ui.launcher

import android.util.TypedValue
import android.view.SoundEffectConstants
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nl.awayfromhome.seniortvlauncher.data.AppInfo
import nl.awayfromhome.seniortvlauncher.data.ButtonShape
import nl.awayfromhome.seniortvlauncher.data.LauncherSettings

class AppGridAdapter(
    private val onAppClick: (AppInfo) -> Unit,
    private val onEmptySlotClick: (Int) -> Unit,
    private val onAppFocused: ((AppInfo?) -> Unit)? = null,
    private val onAppFocusedPosition: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<AppGridAdapter.AppButtonViewHolder>() {

    private var settings: LauncherSettings = LauncherSettings()
    private var allApps: Map<String, AppInfo> = emptyMap()
    private var slots: List<AppInfo?> = emptyList()

    /** Cell dimensions computed from (grid width − padding) / columns and (grid height − padding) / rows. */
    private var cellWidthPx: Int = ViewGroup.LayoutParams.MATCH_PARENT
    private var cellHeightPx: Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CELL_HEIGHT_DP.toFloat(),
        android.content.res.Resources.getSystem().displayMetrics
    ).toInt()

    /** Called by the fragment once the RecyclerView has been measured. */
    fun setCellSize(widthPx: Int, heightPx: Int) {
        cellWidthPx = widthPx
        cellHeightPx = heightPx
    }

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
        val view = AppButtonView(parent.context)
        view.layoutParams = ViewGroup.LayoutParams(cellWidthPx, cellHeightPx)
        return AppButtonViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppButtonViewHolder, position: Int) {
        val appInfo = slots.getOrNull(position)
        val buttonView = holder.buttonView

        // Always keep layout params in sync with the current cell size (handles recycled holders)
        buttonView.layoutParams = ViewGroup.LayoutParams(cellWidthPx, cellHeightPx)

        if (appInfo != null) {
            buttonView.bindApp(appInfo, settings.showAppName, settings.buttonShape)
            buttonView.setOnClickListener { onAppClick(appInfo) }
        } else {
            buttonView.bindEmpty(settings.buttonShape)
            buttonView.setOnClickListener(null)
        }

        buttonView.setOnFocusChangeListener { _, hasFocus ->
            buttonView.setFocusedState(hasFocus, settings.buttonShape)
            if (hasFocus && appInfo != null) {
                onAppFocused?.invoke(appInfo)
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onAppFocusedPosition?.invoke(pos)
                }
                if (settings.clickSoundEnabled) {
                    buttonView.playSoundEffect(SoundEffectConstants.CLICK)
                }
            } else {
                onAppFocused?.invoke(null)
            }
        }
    }

    override fun getItemCount(): Int = slots.size

    companion object {
        /**
         * Fallback cell height used before [setCellSize] is called (i.e. before the
         * RecyclerView has been measured). Matches the old fixed `buttonSizeDp` default.
         */
        private const val DEFAULT_CELL_HEIGHT_DP = 120
    }

    class AppButtonViewHolder(val buttonView: AppButtonView) : RecyclerView.ViewHolder(buttonView)
}

