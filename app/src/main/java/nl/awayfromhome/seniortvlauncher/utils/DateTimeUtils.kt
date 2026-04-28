package nl.awayfromhome.seniortvlauncher.utils

import android.content.Context
import android.provider.Settings
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    fun is24HourFormat(context: Context): Boolean {
        return DateFormat.is24HourFormat(context)
    }

    fun getTimeFormat(context: Context): String {
        return if (is24HourFormat(context)) "HH:mm" else "h:mm a"
    }

    fun getDateFormat(context: Context): java.text.DateFormat {
        return DateFormat.getDateFormat(context)
    }

    fun getFormattedDate(context: Context): String {
        val dateFormat = java.text.DateFormat.getDateInstance(
            java.text.DateFormat.FULL,
            Locale.getDefault()
        )
        return dateFormat.format(Date())
    }
}
