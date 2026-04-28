package nl.awayfromhome.seniortvlauncher.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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

        // 1. Create intents for both standard and TV launchers
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val leanbackIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)

        // 2. Query both (using GET_META_DATA to be thorough)
        val launcherApps = pm.queryIntentActivities(launcherIntent, PackageManager.GET_META_DATA)
        val leanbackApps = pm.queryIntentActivities(leanbackIntent, PackageManager.GET_META_DATA)

        // 3. Combine and remove duplicates based on package name
        val allResolved = (launcherApps + leanbackApps).distinctBy { it.activityInfo.packageName }

        for (resolveInfo in allResolved) {
            val packageName = resolveInfo.activityInfo.packageName

            // Skip our own launcher app from the picker list
            if (packageName == context.packageName) continue

            val label = resolveInfo.loadLabel(pm).toString()

            // Efficiently load icon
            val icon = resolveInfo.loadIcon(pm)
                ?: ContextCompat.getDrawable(context, R.drawable.ic_add)!!

            // Extract dominant color for UI styling
            val bitmap = drawableToBitmap(icon)
            val dominantColor = ColorUtils.getDominantColor(bitmap)

            apps.add(AppInfo(packageName, label, icon, dominantColor))
        }

        return apps.sortedBy { it.label.lowercase() }
    }

    fun launchApp(context: Context, packageName: String) {
        val pm = context.packageManager
        // IMPORTANT for Google TV: Try Leanback intent first
        var intent = pm.getLeanbackLaunchIntentForPackage(packageName)

        // Fallback to standard mobile intent (e.g. for system settings)
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

        // Use standard icon size if intrinsic size is missing
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 128
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 128

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}