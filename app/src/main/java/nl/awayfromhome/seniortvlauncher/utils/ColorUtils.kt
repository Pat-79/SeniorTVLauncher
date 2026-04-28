package nl.awayfromhome.seniortvlauncher.utils

import android.graphics.Bitmap
import android.graphics.Color
import androidx.palette.graphics.Palette

object ColorUtils {

    fun getDominantColor(bitmap: Bitmap): Int {
        return try {
            val palette = Palette.from(bitmap).generate()
            palette.getVibrantColor(
                palette.getDominantColor(Color.WHITE)
            )
        } catch (e: Exception) {
            Color.WHITE
        }
    }

    fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (android.graphics.Color.alpha(color) * factor).toInt()
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        return android.graphics.Color.argb(alpha, red, green, blue)
    }

    fun withAlpha(color: Int, alpha: Int): Int {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }
}
