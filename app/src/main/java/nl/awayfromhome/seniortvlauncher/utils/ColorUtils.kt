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
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    fun withAlpha(color: Int, alpha: Int): Int {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }
}
