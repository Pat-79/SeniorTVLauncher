package nl.awayfromhome.seniortvlauncher.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val dominantColor: Int
)
