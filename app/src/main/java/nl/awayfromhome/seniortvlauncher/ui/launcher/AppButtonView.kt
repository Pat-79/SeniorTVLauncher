package nl.awayfromhome.seniortvlauncher.ui.launcher

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import nl.awayfromhome.seniortvlauncher.R
import nl.awayfromhome.seniortvlauncher.data.AppInfo
import nl.awayfromhome.seniortvlauncher.data.ButtonShape
import nl.awayfromhome.seniortvlauncher.utils.ColorUtils

class AppButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val iconContainer: ConstraintLayout
    private val iconView: ImageView
    private val appNameView: TextView
    private val addIconView: ImageView

    private var dominantColor: Int = Color.WHITE
    private var isFocused: Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.item_app_button, this, true)
        iconContainer = findViewById(R.id.icon_container)
        iconView = findViewById(R.id.app_icon)
        appNameView = findViewById(R.id.app_name)
        addIconView = findViewById(R.id.add_icon)

        isFocusable = true
        isClickable = true
        descendantFocusability = FOCUS_BLOCK_DESCENDANTS
    }

    fun bindApp(appInfo: AppInfo, showName: Boolean, shape: ButtonShape) {
        dominantColor = appInfo.dominantColor
        iconView.setImageDrawable(appInfo.icon)
        iconView.visibility = VISIBLE
        addIconView.visibility = GONE
        appNameView.text = appInfo.label
        appNameView.visibility = if (showName) VISIBLE else GONE
        isFocusable = true
        isClickable = true
        isEnabled = true
        alpha = 1f
        applyShape(shape, false)
    }

    fun bindEmpty(shape: ButtonShape) {
        dominantColor = Color.WHITE
        iconView.setImageDrawable(null)
        iconView.visibility = GONE
        addIconView.visibility = GONE
        appNameView.visibility = GONE
        isFocusable = false
        isClickable = false
        isEnabled = false
        alpha = 0f
        applyShape(shape, true)
    }

    private fun applyShape(shape: ButtonShape, isEmpty: Boolean) {
        val background = GradientDrawable()
        val fillColor = if (isEmpty) {
            Color.argb(40, 255, 255, 255)
        } else {
            ColorUtils.withAlpha(dominantColor, 40)
        }
        val strokeColor = if (isEmpty) {
            Color.argb(80, 255, 255, 255)
        } else {
            dominantColor
        }

        background.setColor(fillColor)
        background.setStroke(if (isFocused) 6 else 3, strokeColor)

        when (shape) {
            ButtonShape.CIRCLE -> background.shape = GradientDrawable.OVAL
            ButtonShape.ROUNDED_SQUARE -> {
                background.shape = GradientDrawable.RECTANGLE
                background.cornerRadius = 24f * resources.displayMetrics.density
            }
            ButtonShape.SQUARE -> {
                background.shape = GradientDrawable.RECTANGLE
                background.cornerRadius = 0f
            }
        }

        iconContainer.background = background
        if (isFocused) {
            iconContainer.elevation = 16f * resources.displayMetrics.density
        } else {
            iconContainer.elevation = 4f * resources.displayMetrics.density
        }
    }

    fun setFocusedState(focused: Boolean, shape: ButtonShape) {
        isFocused = focused
        if (focused) {
            animate().scaleX(1.1f).scaleY(1.1f).setDuration(150).start()
        } else {
            animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
        }
        applyShape(shape, addIconView.visibility == VISIBLE)
    }
}
