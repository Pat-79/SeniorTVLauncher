package nl.awayfromhome.seniortvlauncher.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import nl.awayfromhome.seniortvlauncher.data.AppInfo
import nl.awayfromhome.seniortvlauncher.R

object AppUtils {

    fun getAllLaunchableApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val apps = mutableListOf<AppInfo>()

        // Collect packages from both CATEGORY_LAUNCHER and CATEGORY_LEANBACK_LAUNCHER
        val packagesSet = mutableSetOf<String>()

        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val leanbackIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }

        val launcherApps = pm.queryIntentActivities(launcherIntent, 0)
        val leanbackApps = pm.queryIntentActivities(leanbackIntent, 0)

        val allResolved = (launcherApps + leanbackApps).distinctBy { it.activityInfo.packageName }

        for (resolveInfo in allResolved) {
            val packageName = resolveInfo.activityInfo.packageName
            if (packageName == context.packageName) continue
            if (packagesSet.contains(packageName)) continue
            packagesSet.add(packageName)

            val label = resolveInfo.loadLabel(pm).toString()
            val icon = resolveInfo.loadIcon(pm)
                ?: ContextCompat.getDrawable(context, R.drawable.ic_add)!!
            val bitmap = drawableToBitmap(icon)
            val dominantColor = ColorUtils.getDominantColor(bitmap)

            apps.add(AppInfo(packageName, label, icon, dominantColor))
        }

        return apps.sortedBy { it.label.lowercase() }
    }

    fun launchApp(context: Context, packageName: String) {
        val pm = context.packageManager
        // Try leanback launcher first, then regular launcher
        var intent = pm.getLeanbackLaunchIntentForPackage(packageName)
        if (intent == null) {
            intent = pm.getLaunchIntentForPackage(packageName)
        }
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
